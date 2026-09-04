use std::borrow::Cow;

use pumpkin::command::{
    CommandExecutor, CommandSender,
    argument_builder::{ArgumentBuilder, argument, command},
    argument_types::core::string::StringArgumentType,
    context::command_context::CommandContext,
    node::detached::CommandDetachedNode,
    suggestion::{
        provider::SuggestionProvider,
        suggestions::{Suggestions, SuggestionsBuilder},
    },
};
use tokio::sync::{mpsc, oneshot};

use crate::java::jvm::commands::{JvmCommand, Location, Rotation};

const ARG_ANY: &str = "any";

pub struct JavaCommandExecutor {
    pub cmd_name: String,
    pub command_tx: mpsc::Sender<JvmCommand>,
}

#[derive(Clone)]
pub enum SimpleCommandSender {
    Console,
    /// UUID, Name, is_op
    Player(String, String, bool),
}

/// Greedy catch-all argument node: forwards the rest of the input to the JVM.
pub struct AnyCommandNode {
    command_tx: mpsc::Sender<JvmCommand>,
}

impl SuggestionProvider for AnyCommandNode {
    fn suggest(
        &self,
        context: &CommandContext,
        builder: SuggestionsBuilder,
    ) -> Suggestions {
        let input = context.input.clone();
        let sender: SimpleCommandSender = (&context.source.output).into();

        let location = {
            let pos = context.source.position;
            let rotation = context.source.output.as_player().map(|player| {
                Rotation::new(player.living_entity.entity.yaw.load(), player.living_entity.entity.pitch.load())
            });
            context
                .source
                .world
                .clone()
                .map(|w| Location::new(w.uuid, pos.x, pos.y, pos.z, rotation))
        };

        match self.tab_complete(sender, input, location) {
            Ok(Some(list)) => {
                let mut b = builder;
                for s in list {
                    b = b.suggest(s.suggestion);
                }
                b.build()
            }
            Ok(None) | Err(_) => builder.build(),
        }
    }
}

impl AnyCommandNode {
    fn tab_complete(
        &self,
        command_sender: SimpleCommandSender,
        full_command: String,
        location: Option<Location>,
    ) -> anyhow::Result<Option<Vec<pumpkin_protocol::java::client::play::CommandSuggestion>>>
    {
        if let Ok(handle) = tokio::runtime::Handle::try_current() {
            tokio::task::block_in_place(|| {
                handle.block_on(async move {
                    let (tx, rx) = oneshot::channel();
                    if let Err(e) = self
                        .command_tx
                        .send(JvmCommand::GetCommandTabComplete {
                            command_sender,
                            full_command,
                            respond_to: tx,
                            location,
                        })
                        .await
                    {
                        tracing::warn!("Failed to send tab complete to JVM worker: {e}");
                        return Ok(None);
                    }

                    match rx.await {
                        Ok(res) => res.map_err(|e| anyhow::anyhow!("tab complete failed: {e:?}")),
                        Err(_) => Ok(None),
                    }
                })
            })
        } else {
            let (tx, rx) = oneshot::channel();
            if let Err(e) = self
                .command_tx
                .blocking_send(JvmCommand::GetCommandTabComplete {
                    command_sender,
                    full_command,
                    respond_to: tx,
                    location,
                })
            {
                tracing::warn!("Failed to send tab complete to JVM worker: {e}");
                return Ok(None);
            }

            match rx.blocking_recv() {
                Ok(res) => res.map_err(|e| anyhow::anyhow!("tab complete failed: {e:?}")),
                Err(_) => Ok(None),
            }
        }
    }
}

impl From<&CommandSender> for SimpleCommandSender {
    fn from(val: &CommandSender) -> Self {
        match val {
            CommandSender::Console | CommandSender::Rcon(_) | CommandSender::Dummy => Self::Console,
            CommandSender::Player(player) => {
                let is_op =
                    player.permission_lvl.load() >= pumpkin_util::permission::PermissionLvl::Two;
                Self::Player(
                    player.gameprofile.id.to_string(),
                    player.gameprofile.name.clone(),
                    is_op,
                )
            }
            CommandSender::CommandBlock(_block_entity, _world) => Self::Console,
        }
    }
}

impl CommandExecutor for JavaCommandExecutor {
    fn execute(&self, context: &CommandContext) -> pumpkin::command::CommandExecutorResult {
        if let CommandSender::Player(player) = &context.source.output {
            crate::java::native_callbacks::utils::cache_player(player.clone());
        }

        let rest = StringArgumentType::get(context, ARG_ANY).unwrap_or("");
        let full_command = if rest.is_empty() {
            if self.cmd_name.starts_with('/') {
                self.cmd_name.clone()
            } else {
                format!("/{}", self.cmd_name)
            }
        } else if self.cmd_name.starts_with('/') {
            format!("{} {rest}", self.cmd_name)
        } else {
            format!("/{} {rest}", self.cmd_name)
        };

        let command_sender: SimpleCommandSender = (&context.source.output).into();

        let res: anyhow::Result<()> = if let Ok(handle) = tokio::runtime::Handle::try_current() {
            tokio::task::block_in_place(|| {
                handle.block_on(async move {
                    let (tx, rx) = oneshot::channel();
                    if let Err(e) = self
                        .command_tx
                        .send(JvmCommand::TriggerCommand {
                            full_command,
                            respond_to: tx,
                            command_sender,
                        })
                        .await
                    {
                        tracing::error!("Failed to send command to JVM worker: {e}");
                        return Err(anyhow::anyhow!("{e}"));
                    }

                    let _ = rx.await;
                    Ok(())
                })
            })
        } else {
            let (tx, rx) = oneshot::channel();
            if let Err(e) = self.command_tx.blocking_send(JvmCommand::TriggerCommand {
                full_command,
                respond_to: tx,
                command_sender,
            }) {
                tracing::error!("Failed to send command to JVM worker: {e}");
                return Err(
                    pumpkin::command::errors::error_types::DISPATCHER_UNKNOWN_COMMAND
                        .create_without_context(),
                );
            }

            let _ = rx.blocking_recv();
            Ok(())
        };

        match res {
            Ok(()) => Ok(1),
            Err(_) => Err(
                pumpkin::command::errors::error_types::DISPATCHER_UNKNOWN_COMMAND
                    .create_without_context(),
            ),
        }
    }
}

pub fn init_java_command(
    cmd_name: impl Into<String>,
    command_tx: mpsc::Sender<JvmCommand>,
    names: impl IntoIterator<Item: Into<String>>,
    description: impl Into<Cow<'static, str>>,
) -> CommandDetachedNode {
    let cmd_name = cmd_name.into();
    let description = description.into();
    let mut names_iter = names.into_iter();
    let primary: String = names_iter.next().map(Into::into).unwrap_or_else(|| cmd_name.clone());

    let executor = JavaCommandExecutor {
        cmd_name: cmd_name.clone(),
        command_tx: command_tx.clone(),
    };

    // primary: /name + greedy passthrough, registered with aliases below
    // (register_with_aliases on the dispatcher handles the rest).
        let node = command(primary, description)
        .executes_arc(std::sync::Arc::new(executor))
        .then(
            argument(
                ARG_ANY,
                StringArgumentType::GreedyPhrase,
            )
            .suggests_arc(std::sync::Arc::new(AnyCommandNode {
                command_tx: command_tx.clone(),
            }))
            .executes_arc(std::sync::Arc::new(JavaCommandExecutor {
                cmd_name,
                command_tx,
            })),
        )
        .build();

    let _ = names_iter;
    node
}

#[cfg(test)]
mod tests {
    use super::*;
    use pumpkin::command::node::dispatcher::CommandDispatcher;

    #[test]
    fn test_init_java_command_registration() {
        let mut dispatcher = CommandDispatcher::default();

        let (tx, _rx) = mpsc::channel(100);
        let tree = init_java_command("fly", tx, ["fly"], "Fly command");
    let id = dispatcher.register(tree);
    let node = &dispatcher.tree[id];

    assert!(dispatcher.has_command("fly"));
    assert_eq!(node.meta.literal.as_ref(), "fly");
    }
}
