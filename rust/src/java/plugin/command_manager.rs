use std::sync::{Arc, Mutex};

use anyhow::Result;
use j4rs::{Instance, InvocationArg, Jvm};
use pumpkin::{command::dispatcher::CommandError, plugin::Context};
use pumpkin_protocol::java::client::play::CommandSuggestion;
use pumpkin_util::permission::{Permission, PermissionDefault};
use tokio::sync::mpsc;

use crate::{
    commands::{SimpleCommandSender, init_java_command},
    config,
    java::{
        jvm::commands::{JvmCommand, Location},
        plugin::manager::Plugin,
    },
};

const PATCHBUKKIT_PERMISSION_NAMESPACE: &str = "patchbukkit";

pub struct CommandManager {
    command_map: Option<Instance>,
}

impl Default for CommandManager {
    fn default() -> Self {
        Self::new()
    }
}

impl CommandManager {
    #[must_use]
    pub const fn new() -> Self {
        Self { command_map: None }
    }

    pub fn init(&mut self, jvm: &Jvm) -> Result<()> {
        let server_instance =
            jvm.invoke_static("org.bukkit.Bukkit", "getServer", InvocationArg::empty())?;

        let command_map = jvm.invoke(&server_instance, "getCommandMap", InvocationArg::empty())?;
        let command_map = jvm.cast(
            &command_map,
            "org.patchbukkit.command.PatchBukkitCommandMap",
        )?;

        self.command_map = Some(command_map);

        Ok(())
    }

    fn get_or_init_command_map(&mut self, jvm: &Jvm) -> Result<&Instance> {
        if self.command_map.is_none() {
            self.init(jvm)?;
        }
        Ok(self.command_map.as_ref().unwrap())
    }

    pub fn get_tab_complete(
        &mut self,
        jvm: &Jvm,
        sender: SimpleCommandSender,
        full_command: String,
        location: Option<Location>,
    ) -> Result<Option<Vec<CommandSuggestion>>, CommandError> {
        match self.try_tab_complete(jvm, sender, full_command, location) {
            Ok(suggestions) => Ok(suggestions),
            Err(e) => {
                tracing::warn!("Tab completion failed: {e}");
                Ok(None)
            }
        }
    }

    fn try_tab_complete(
        &mut self,
        jvm: &Jvm,
        sender: SimpleCommandSender,
        full_command: String,
        location: Option<Location>,
    ) -> Result<Option<Vec<CommandSuggestion>>> {
        let command_map = self.get_or_init_command_map(jvm)?;

        let sender = Self::sender_to_jsender(jvm, sender)?;

        let completions = if let Some(location) = location {
            let world = jvm.invoke_static(
                "org.patchbukkit.world.PatchBukkitWorld",
                "getOrCreate",
                &[InvocationArg::try_from(location.world.to_string())?],
            )?;

            let location = match location.rotation {
                Some(rotation) => jvm.create_instance(
                    "org.bukkit.Location",
                    &[
                        InvocationArg::try_from(world)?,
                        InvocationArg::try_from(location.x)?.into_primitive()?,
                        InvocationArg::try_from(location.y)?.into_primitive()?,
                        InvocationArg::try_from(location.z)?.into_primitive()?,
                        InvocationArg::try_from(rotation.yaw)?.into_primitive()?,
                        InvocationArg::try_from(rotation.pitch)?.into_primitive()?,
                    ],
                )?,
                None => jvm.create_instance(
                    "org.bukkit.Location",
                    &[
                        InvocationArg::try_from(world)?,
                        InvocationArg::try_from(location.x)?.into_primitive()?,
                        InvocationArg::try_from(location.y)?.into_primitive()?,
                        InvocationArg::try_from(location.z)?.into_primitive()?,
                    ],
                )?,
            };

            jvm.invoke(
                command_map,
                "tabComplete",
                &[
                    InvocationArg::try_from(sender)?,
                    InvocationArg::try_from(full_command)?,
                    InvocationArg::try_from(location)?,
                ],
            )?
        } else {
            jvm.invoke(
                command_map,
                "tabComplete",
                &[
                    InvocationArg::try_from(sender)?,
                    InvocationArg::try_from(full_command)?,
                ],
            )?
        };

        let completions: Vec<String> = jvm.to_rust(completions)?;

        Ok(Some(
            completions
                .into_iter()
                .map(|completion| CommandSuggestion::new(completion, None))
                .collect(),
        ))
    }

    pub async fn register_command(
        &mut self,
        jvm: &Jvm,
        context: &Arc<Context>,
        plugin: &Plugin,
        cmd_name: String,
        cmd_data: &config::spigot::Command,
        command_tx: mpsc::Sender<JvmCommand>,
    ) -> Result<()> {
        let command_map = self.get_or_init_command_map(jvm)?;

        let created_cmd = match jvm.invoke_static(
            "org.patchbukkit.command.CommandFactory",
            "create",
            &[
                InvocationArg::try_from(&cmd_name)?,
                InvocationArg::try_from(&plugin.name)?,
            ],
        ) {
            Ok(inst) => {
                if jvm
                    .invoke(&inst, "getName", InvocationArg::empty())
                    .is_err()
                {
                    tracing::error!(
                        "Failed to create PluginCommand for {}: Java returned null",
                        cmd_name
                    );
                    return Ok(());
                }
                inst
            }
            Err(e) => {
                tracing::error!("Failed to create PluginCommand for {}: {:?}", cmd_name, e);
                return Ok(());
            }
        };
        let j_plugin_cmd = Arc::new(Mutex::new(created_cmd));
        tracing::info!("Registering Bukkit command: {}", &cmd_name);
        let cmd_aliases: Vec<String> = cmd_data
            .aliases
            .as_ref()
            .map_or_else(Vec::new, |a| a.to_vec());
        let mut raw_names: Vec<String> = vec![cmd_name.clone()];
        raw_names.extend(cmd_aliases.clone());

        let mut names: Vec<String> = Vec::new();
        for name in raw_names {
            let clean = name.trim_start_matches('/').to_string();
            if !clean.is_empty() {
                if !names.contains(&clean) {
                    names.push(clean.clone());
                }
                let single = format!("/{}", clean);
                if !names.contains(&single) {
                    names.push(single);
                }
                let double = format!("//{}", clean);
                if !names.contains(&double) {
                    names.push(double);
                }
            }
        }

        {
            let cmd_lock = j_plugin_cmd.lock().unwrap();
            let j_plugin_cmd_owned = jvm.clone_instance(&cmd_lock)?;

            if let Some(ref desc) = cmd_data.description {
                let _ = jvm.invoke(
                    &j_plugin_cmd_owned,
                    "setDescription",
                    &[InvocationArg::try_from(desc)?],
                );
            }
            if let Some(ref usage) = cmd_data.usage {
                let _ = jvm.invoke(
                    &j_plugin_cmd_owned,
                    "setUsage",
                    &[InvocationArg::try_from(usage)?],
                );
            }
            if let Some(ref perm_msg) = cmd_data.permission_message {
                let _ = jvm.invoke(
                    &j_plugin_cmd_owned,
                    "setPermissionMessage",
                    &[InvocationArg::try_from(perm_msg)?],
                );
            }
            if let Some(ref perm) = cmd_data.permission {
                let _ = jvm.invoke(
                    &j_plugin_cmd_owned,
                    "setPermission",
                    &[InvocationArg::try_from(perm)?],
                );
            }
            if !cmd_aliases.is_empty() {
                let j_aliases = jvm.create_java_array(
                    "java.lang.String",
                    &cmd_aliases
                        .iter()
                        .map(|a| InvocationArg::try_from(a).unwrap())
                        .collect::<Vec<_>>(),
                )?;
                let j_list = jvm.invoke_static(
                    "java.util.Arrays",
                    "asList",
                    &[InvocationArg::from(j_aliases)],
                )?;
                let _ = jvm.invoke(
                    &j_plugin_cmd_owned,
                    "setAliases",
                    &[InvocationArg::from(j_list)],
                );
            }

            let j_command_cast = match jvm.cast(&j_plugin_cmd_owned, "org.bukkit.command.Command") {
                Ok(casted) => casted,
                Err(_) => j_plugin_cmd_owned,
            };

            jvm.invoke(
                command_map,
                "register",
                &[
                    InvocationArg::try_from(&cmd_name)?,
                    InvocationArg::try_from(&plugin.name)?,
                    InvocationArg::from(j_command_cast),
                ],
            )?;
        }

        let node = init_java_command(
            cmd_name.clone(),
            command_tx.clone(),
            names,
            cmd_data.description.clone().unwrap_or_default(),
        );

        let (permission, permission_default) = build_permission_node(&cmd_name, cmd_data);

        let registry = {
            let permission_manager = context.permission_manager.read().await;
            permission_manager.registry.clone()
        };

        if let Err(e) = registry.write().await.register_permission(Permission::new(
            &permission,
            &format!(
                "Allows running the Bukkit command `{cmd_name}` from `{}`",
                plugin.name
            ),
            permission_default,
        )) {
            if e.contains("already registered") {
                tracing::debug!(
                    "Permission already registered for command {}: {}",
                    cmd_name,
                    e
                );
            } else {
                tracing::warn!(
                    "Failed to register permission for command {}: {:?}",
                    cmd_name,
                    e
                );
            }
        }

        context.register_command(node, permission).await;

        Ok(())
    }

    pub fn trigger_command(
        &mut self,
        jvm: &Jvm,
        full_command: String,
        sender: SimpleCommandSender,
    ) -> Result<()> {
        let command_map = self.get_or_init_command_map(jvm)?;

        let j_sender = Self::sender_to_jsender(jvm, sender)?;

        let dispatch_result = jvm.invoke(
            command_map,
            "dispatch",
            &[
                InvocationArg::from(j_sender),
                InvocationArg::try_from(full_command.clone())?,
            ],
        );

        match dispatch_result {
            Ok(result) => {
                let handled: bool = jvm.to_rust(result)?;
                if !handled {
                    tracing::warn!("Command not handled by any Java plugin: {full_command}");
                }
            }
            Err(e) => {
                tracing::error!("Java exception during command dispatch '{full_command}': {e:?}");
            }
        }

        Ok(())
    }

    pub fn sender_to_jsender(jvm: &Jvm, sender: SimpleCommandSender) -> Result<Instance> {
        match sender {
            SimpleCommandSender::Console => Ok(jvm.invoke_static(
                "org.bukkit.Bukkit",
                "getConsoleSender",
                InvocationArg::empty(),
            )?),

            SimpleCommandSender::Player(uuid_str, name) => {
                let server =
                    jvm.invoke_static("org.bukkit.Bukkit", "getServer", InvocationArg::empty())?;
                let patch_server = jvm.cast(&server, "org.patchbukkit.PatchBukkitServer")?;

                let j_uuid = jvm.invoke_static(
                    "java.util.UUID",
                    "fromString",
                    &[InvocationArg::try_from(uuid_str)?],
                )?;

                let j_player = jvm.invoke(
                    &patch_server,
                    "getPlayer",
                    &[InvocationArg::from(jvm.clone_instance(&j_uuid)?)],
                )?;

                let is_null: bool = jvm.to_rust(jvm.invoke_static(
                    "java.util.Objects",
                    "isNull",
                    &[InvocationArg::from(jvm.clone_instance(&j_player)?)],
                )?)?;

                if is_null {
                    tracing::info!(
                        "Player not found in Java, creating PatchBukkitPlayer for {}",
                        name
                    );
                    let j_player = jvm.create_instance(
                        "org.patchbukkit.entity.PatchBukkitPlayer",
                        &[InvocationArg::from(j_uuid), InvocationArg::try_from(name)?],
                    )?;

                    jvm.invoke(
                        &j_player,
                        "setOp",
                        &[InvocationArg::try_from(true)?.into_primitive()?],
                    )?;

                    jvm.invoke(
                        &patch_server,
                        "registerPlayer",
                        &[InvocationArg::from(jvm.clone_instance(&j_player)?)],
                    )?;

                    Ok(j_player)
                } else {
                    Ok(j_player)
                }
            }
        }
    }
}

fn build_permission_node(
    cmd_name: &str,
    cmd_data: &config::spigot::Command,
) -> (String, PermissionDefault) {
    let clean = cmd_name.trim_start_matches('/');
    if let Some(permission) = &cmd_data.permission {
        let perm_node = if permission.contains(':') {
            permission.clone()
        } else {
            format!("{PATCHBUKKIT_PERMISSION_NAMESPACE}:{permission}")
        };
        (perm_node, PermissionDefault::Allow)
    } else {
        (
            format!("{PATCHBUKKIT_PERMISSION_NAMESPACE}:command.{clean}"),
            PermissionDefault::Allow,
        )
    }
}

#[cfg(test)]
mod tests {
    use pumpkin_util::permission::PermissionDefault;

    use crate::config::spigot::Command;

    use super::build_permission_node;

    #[test]
    fn permission_node_uses_explicit_plugin_permission() {
        let command = Command {
            description: None,
            usage: None,
            aliases: None,
            permission: Some("simplespawn.spawn".to_string()),
            permission_message: None,
        };

        assert_eq!(
            build_permission_node("spawn", &command),
            (
                "patchbukkit:simplespawn.spawn".to_string(),
                PermissionDefault::Allow
            )
        );
    }

    #[test]
    fn permission_node_falls_back_to_patchbukkit_namespace() {
        let command = Command {
            description: None,
            usage: None,
            aliases: None,
            permission: None,
            permission_message: None,
        };

        assert_eq!(
            build_permission_node("spawn", &command),
            (
                "patchbukkit:command.spawn".to_string(),
                PermissionDefault::Allow
            )
        );
    }
}
