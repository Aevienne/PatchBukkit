use std::{path::PathBuf, sync::Arc};

use j4rs::{InvocationArg, Jvm, JvmBuilder};
use pumpkin::plugin::Context;
use tokio::sync::mpsc;

use crate::{
    java::{
        jar::read_configs_from_jar,
        jvm::commands::{JvmCommand, LoadPluginResult},
        native_callbacks::{init_callback_context, initialize_callbacks},
        plugin::{
            command_manager::CommandManager, event_manager::EventManager, manager::PluginManager,
        },
    },
    proto::patchbukkit::events::FireEventResponse,
};

pub struct JvmWorker {
    command_rx: mpsc::Receiver<JvmCommand>,
    pub plugin_manager: PluginManager,
    pub event_manager: EventManager,
    pub command_manager: CommandManager,
    jvm: Option<j4rs::Jvm>,
    context: Option<Arc<Context>>,
}

impl JvmWorker {
    #[must_use]
    pub fn new(command_rx: mpsc::Receiver<JvmCommand>) -> Self {
        Self {
            command_rx,
            plugin_manager: PluginManager::new(),
            event_manager: EventManager::new(),
            command_manager: CommandManager::new(),
            jvm: None,
            context: None,
        }
    }

    pub async fn attach_thread(mut self) {
        tracing::info!("JVM worker thread started");

        while let Some(command) = self.command_rx.recv().await {
            match command {
                JvmCommand::Initialize {
                    j4rs_path,
                    respond_to,
                    context,
                    command_tx,
                    config,
                } => {
                    init_callback_context(
                        context.clone(),
                        tokio::runtime::Handle::current(),
                        command_tx.clone(),
                        config,
                    )
                    .unwrap();
                    self.context = Some(context);
                    let result = self.initialize_jvm(&j4rs_path);
                    let _ = respond_to.send(result);
                }
                JvmCommand::LoadPlugin {
                    plugin_path,
                    respond_to,
                } => {
                    let _ = match read_configs_from_jar(&plugin_path) {
                        Ok(configs) => match configs {
                            (Some(paper_plugin_config), spigot) => {
                                match self.plugin_manager.load_paper_plugin(
                                    &plugin_path,
                                    &paper_plugin_config,
                                    &spigot,
                                ) {
                                    Ok(()) => {
                                        respond_to.send(LoadPluginResult::SuccessfullyLoadedPaper)
                                    }
                                    Err(err) => respond_to
                                        .send(LoadPluginResult::FailedToLoadPaperPlugin(err)),
                                }
                            }
                            (None, Some(spigot)) => {
                                match self
                                    .plugin_manager
                                    .load_spigot_plugin(&plugin_path, &spigot)
                                {
                                    Ok(()) => {
                                        respond_to.send(LoadPluginResult::SuccessfullyLoadedSpigot)
                                    }
                                    Err(err) => respond_to
                                        .send(LoadPluginResult::FailedToLoadSpigotPlugin(err)),
                                }
                            }
                            (None, None) => respond_to.send(LoadPluginResult::NoConfigurationFile),
                        },
                        Err(err) => {
                            respond_to.send(LoadPluginResult::FailedToReadConfigurationFile(err))
                        }
                    };
                }
                JvmCommand::InstantiateAllPlugins {
                    plugins_dir,
                    respond_to,
                    server,
                    command_tx,
                } => {
                    let jvm = match self.jvm {
                        Some(ref jvm) => jvm,
                        None => &Jvm::attach_thread().unwrap(),
                    };

                    let _ = respond_to.send(
                        self.plugin_manager
                            .instantiate_all_plugins(
                                jvm,
                                &plugins_dir,
                                &server,
                                command_tx,
                                &mut self.command_manager,
                            )
                            .await,
                    );
                }
                JvmCommand::EnableAllPlugins { respond_to } => {
                    let jvm = match self.jvm {
                        Some(ref jvm) => jvm,
                        None => &Jvm::attach_thread().unwrap(),
                    };

                    let _ = respond_to.send(self.plugin_manager.enable_all_plugins(jvm));
                }
                JvmCommand::DisableAllPlugins { respond_to } => {
                    let jvm = match self.jvm {
                        Some(ref jvm) => jvm,
                        None => &Jvm::attach_thread().unwrap(),
                    };

                    let _ = respond_to.send(self.plugin_manager.disable_all_plugins(jvm));
                }
                JvmCommand::Shutdown { respond_to } => {
                    let _ = respond_to.send(self.plugin_manager.unload_all_plugins());
                    break;
                }
                JvmCommand::FireEvent {
                    respond_to,
                    plugin,
                    payload,
                } => {
                    let jvm = match self.jvm {
                        Some(ref jvm) => jvm,
                        None => &Jvm::attach_thread().unwrap(),
                    };

                    let original_event = payload.event.clone();
                    let cancelled = match self.event_manager.fire_event(jvm, payload, plugin) {
                        Ok(c) => c,
                        Err(e) => {
                            tracing::error!("Failed to fire event: {e}");
                            FireEventResponse {
                                cancelled: false,
                                data: Some(original_event),
                            }
                        }
                    };

                    let _ = respond_to.send(cancelled);
                }
                JvmCommand::TriggerCommand {
                    full_command,
                    command_sender,
                    respond_to,
                } => {
                    let jvm = match self.jvm {
                        Some(ref jvm) => jvm,
                        None => &Jvm::attach_thread().unwrap(),
                    };

                    let result =
                        self.command_manager
                            .trigger_command(jvm, full_command, command_sender);

                    let _ = respond_to.send(result);
                }
                JvmCommand::GetCommandTabComplete {
                    command_sender,
                    full_command,
                    respond_to,
                    location,
                } => {
                    let jvm = match self.jvm {
                        Some(ref jvm) => jvm,
                        None => &Jvm::attach_thread().unwrap(),
                    };

                    let result = self.command_manager.get_tab_complete(
                        jvm,
                        command_sender,
                        full_command,
                        location,
                    );

                    let _ = respond_to.send(result);
                }
            }
        }

        tracing::info!("JVM worker thread exited");
    }

    fn initialize_jvm(&mut self, j4rs_path: &PathBuf) -> anyhow::Result<()> {
        tracing::info!("Initializing JVM with path: {j4rs_path:?}");

        let jassets = j4rs_path.join("jassets");
        let mut classpath_entries = Vec::new();

        if let Ok(entries) = std::fs::read_dir(&jassets) {
            for entry in entries.flatten() {
                let path = entry.path();
                if let Some(filename) = path.file_name().and_then(|n| n.to_str())
                    && filename.ends_with(".jar")
                {
                    classpath_entries.push(j4rs::ClasspathEntry::new(&path));
                }
            }
        }

        let mut jvm_builder = JvmBuilder::new();
        let mut jvm_builder = jvm_builder.with_base_path(j4rs_path).java_opts(vec![
            j4rs::JavaOpt::new("--enable-native-access=ALL-UNNAMED"),
            j4rs::JavaOpt::new("--enable-final-field-mutation=ALL-UNNAMED"),
            j4rs::JavaOpt::new("--add-opens=java.base/java.lang=ALL-UNNAMED"),
            j4rs::JavaOpt::new("--add-opens=java.base/java.lang.reflect=ALL-UNNAMED"),
            j4rs::JavaOpt::new("-Dcom.google.protobuf.useUnsafe=false"),
        ]);

        for entry in classpath_entries {
            jvm_builder = jvm_builder.classpath_entry(entry);
        }

        let jvm = jvm_builder.build()?;

        initialize_callbacks(&jvm)?;

        setup_patchbukkit_server(&jvm)?;

        self.jvm = Some(jvm);

        tracing::info!("JVM initialized successfully");
        Ok(())
    }
}

pub fn setup_patchbukkit_server(jvm: &Jvm) -> anyhow::Result<()> {
    jvm.invoke_static(
        "org.patchbukkit.PatchBukkitServer",
        "initServer",
        InvocationArg::empty(),
    )?;

    Ok(())
}
