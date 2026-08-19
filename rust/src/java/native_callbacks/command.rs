use pumpkin_util::permission::{Permission, PermissionDefault};

use crate::{commands::init_java_command, proto::patchbukkit::command::RegisterCommandRequest};

pub fn ffi_native_bridge_register_command_impl(request: RegisterCommandRequest) -> Option<()> {
    let context = super::CALLBACK_CONTEXT.get()?;

    let cmd_name = request.cmd_name;
    let aliases = request.aliases;
    let description = request.description;
    let plugin_name = request.plugin_name;
    let command_tx = context.command_tx.clone();
    let plugin_context = context.plugin_context.clone();

    context.runtime.spawn(async move {
        let mut raw_names: Vec<String> = vec![cmd_name.clone()];
        raw_names.extend(aliases.clone());

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

        let node = init_java_command(cmd_name.clone(), command_tx, names, description);

        let clean_perm = cmd_name.trim_start_matches('/');
        let permission = format!("patchbukkit:command.{clean_perm}");

        let registry = {
            let permission_manager = plugin_context.permission_manager.read().await;
            permission_manager.registry.clone()
        };

        let _ = registry.write().await.register_permission(Permission::new(
            &permission,
            &format!("Allows running command `{cmd_name}` from `{plugin_name}`"),
            PermissionDefault::Allow,
        ));

        plugin_context.register_command(node, permission).await;
    });

    Some(())
}
