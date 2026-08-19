#![allow(clippy::async_yields_async)]

use std::sync::Arc;

use pumpkin::plugin::Context;
use pumpkin_api_macros::{plugin_impl, plugin_method};

pub mod commands;
pub mod config;
pub mod directories;
pub mod events;
pub mod java;
pub mod proto;

use directories::setup_directories;
use java::jar::discover_jar_files;
use tokio::sync::{
    mpsc::{self, Receiver},
    oneshot,
};

use crate::{
    config::patchbukkit::PatchBukkitConfig,
    java::{
        jvm::{commands::JvmCommand, worker::JvmWorker},
        resources::setup_j4rs,
    },
};

async fn on_load_inner(plugin: &PatchBukkitPlugin, server: Arc<Context>) -> Result<(), String> {
    server.init_log();
    tracing::info!("Starting PatchBukkit");

    // Setup directories
    let dirs = setup_directories(&server)?;

    let mut config_path = dirs.base.clone();
    config_path.push("patchbukkit.config.toml");
    let config = PatchBukkitConfig::get_or_create(config_path)
        .map_err(|e| format!("Failed to setup PatchBukkit config: {e}"))?;

    // Manage embedded resources
    setup_j4rs(&dirs.j4rs).map_err(|e| format!("Failed to setup J4RS: {e}"))?;

    {
        let (tx, rx) = oneshot::channel();
        plugin
            .command_tx
            .send(JvmCommand::Initialize {
                j4rs_path: dirs.j4rs,
                respond_to: tx,
                context: server.clone(),
                command_tx: plugin.command_tx.clone(),
                config,
            })
            .await
            .map_err(|e| format!("Failed to send command to initialize J4RS: {e}"))?;
        rx.await
            .map_err(|e| format!("Unable to receive response from J4RS initialization: {e}"))?
            .map_err(|e| format!("Failed to initialize all plugins: {e}"))?;
    }

    {
        let (tx, rx) = oneshot::channel();
        plugin
            .command_tx
            .send(JvmCommand::InstantiateAllPlugins {
                plugins_dir: dirs.plugins.clone(),
                respond_to: tx,
                server: server.clone(),
                command_tx: plugin.command_tx.clone(),
            })
            .await
            .map_err(|e| format!("Failed to send command to instantiate plugins: {e}"))?;
        rx.await
            .map_err(|e| format!("Unable to receive response from instantiate plugins: {e}"))?
            .map_err(|e| format!("Failed to instantiate all plugins: {e}"))?;
    }

    {
        let (tx, rx) = oneshot::channel();
        plugin
            .command_tx
            .send(JvmCommand::EnableAllPlugins { respond_to: tx })
            .await
            .map_err(|e| format!("Failed to send command to enable all plugins: {e}"))?;
        rx.await
            .map_err(|e| format!("Unable to receive response from enable all plugins: {e}"))?
            .map_err(|e| format!("Failed to enable all plugins: {e}"))?;
    };

    // Register Bukkit plugin loader with Pumpkin so Bukkit/Paper plugins appear in Pumpkin's native /plugins command
    let bukkit_loader = Arc::new(crate::java::plugin::loader::BukkitPluginLoader);
    let rt = tokio::runtime::Builder::new_current_thread()
        .enable_all()
        .build()
        .map_err(|e| format!("Failed to build Tokio runtime: {e}"))?;

    let server_ref = server.clone();
    let jar_paths: Vec<_> = discover_jar_files(&dirs.plugins).collect();
    rt.block_on(async move {
        server_ref
            .plugin_manager
            .add_loader(&server_ref.server, bukkit_loader)
            .await;
        for jar_path in &jar_paths {
            let _ = server_ref
                .plugin_manager
                .try_load_plugin(&server_ref.server, jar_path)
                .await;
        }
    });

    Ok(())
}

async fn on_unload_inner(plugin: &PatchBukkitPlugin, _server: Arc<Context>) -> Result<(), String> {
    {
        let (tx, rx) = oneshot::channel();
        plugin
            .command_tx
            .send(JvmCommand::DisableAllPlugins { respond_to: tx })
            .await
            .map_err(|e| format!("Failed to send command to disable all plugins: {e}"))?;
        rx.await
            .map_err(|e| format!("Unable to receive response from disable all plugins: {e}"))?
            .map_err(|e| format!("Failed to disable all plugins: {e}"))?;
    }

    {
        let (tx, rx) = oneshot::channel();
        plugin
            .command_tx
            .send(JvmCommand::Shutdown { respond_to: tx })
            .await
            .map_err(|e| format!("Failed to send command to shutdown: {e}"))?;
        rx.await
            .map_err(|e| format!("Unable to receive response from shutdown: {e}"))?
            .map_err(|e| format!("Failed to shutdown: {e}"))?;
    }

    Ok(())
}

#[plugin_method]
async fn on_load(&self, server: Arc<Context>) -> Result<(), String> {
    on_load_inner(self, server).await
}

#[plugin_method]
async fn on_unload(&self, server: Arc<Context>) -> Result<(), String> {
    on_unload_inner(self, server).await
}

#[plugin_impl]
pub struct PatchBukkitPlugin {
    pub command_tx: mpsc::Sender<JvmCommand>,
}

impl PatchBukkitPlugin {
    #[must_use]
    pub fn new() -> Self {
        let (tx, rx) = mpsc::channel(100);

        #[tokio::main]
        pub async fn jvm_thread_task(rx: Receiver<JvmCommand>) {
            JvmWorker::new(rx).attach_thread().await;
        }

        std::thread::Builder::new()
            .name("patchbukkit-jvm-worker".to_string())
            .spawn(move || {
                jvm_thread_task(rx);
            })
            .unwrap();
        Self { command_tx: tx }
    }
}

impl Default for PatchBukkitPlugin {
    fn default() -> Self {
        Self::new()
    }
}
