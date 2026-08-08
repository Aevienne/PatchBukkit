use std::sync::Arc;

use pumpkin::plugin::EventPriority;
use pumpkin::plugin::player::player_join::PlayerJoinEvent;
use pumpkin_util::text::TextComponent;

use crate::events::handler::PatchBukkitEventHandler;
use crate::java::native_callbacks::CALLBACK_CONTEXT;
use crate::proto::patchbukkit::events::event::Data;
use crate::proto::patchbukkit::events::{
    CallEventRequest, CallEventResponse, RegisterEventRequest,
};

pub fn ffi_native_bridge_register_event_impl(request: RegisterEventRequest) -> Option<()> {
    let ctx = CALLBACK_CONTEXT.get()?;
    let pumpkin_priority = match request.priority {
        0 => EventPriority::Lowest,
        1 => EventPriority::Low,
        2 => EventPriority::Normal,
        3 => EventPriority::High,
        _ => EventPriority::Highest,
    };

    tracing::info!(
        "Plugin '{}' registering listener for '{}' (priority={:?}, blocking={})",
        request.plugin_name,
        request.event_type,
        request.priority,
        request.blocking
    );

    let command_tx = ctx.command_tx.clone();
    let context = ctx.plugin_context.clone();

    tokio::task::block_in_place(|| {
        ctx.runtime.block_on(async {
            match request.event_type.as_str() {
                "org.bukkit.event.player.PlayerJoinEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::player::player_join::PlayerJoinEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::player::player_join::PlayerJoinEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.player.PlayerQuitEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::player::player_leave::PlayerLeaveEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::player::player_leave::PlayerLeaveEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.player.PlayerGameModeChangeEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::player::player_gamemode_change::PlayerGamemodeChangeEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::player::player_gamemode_change::PlayerGamemodeChangeEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.player.PlayerInteractEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::player::player_interact_event::PlayerInteractEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::player::player_interact_event::PlayerInteractEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.block.BlockBreakEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::block::block_break::BlockBreakEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::block::block_break::BlockBreakEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.block.BlockPlaceEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::block::block_place::BlockPlaceEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::block::block_place::BlockPlaceEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.block.SignChangeEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::block::sign_change::SignChangeEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::block::sign_change::SignChangeEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.block.BlockDamageEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::block::block_damage::BlockDamageEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::block::block_damage::BlockDamageEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.server.ServerCommandEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::server::server_command::ServerCommandEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::server::server_command::ServerCommandEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.block.BlockIgniteEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::block::block_ignite::BlockIgniteEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::block::block_ignite::BlockIgniteEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.block.BlockGrowEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::block::block_grow::BlockGrowEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::block::block_grow::BlockGrowEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.block.BlockFormEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::block::block_form::BlockFormEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::block::block_form::BlockFormEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.block.BlockFadeEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::block::block_fade::BlockFadeEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::block::block_fade::BlockFadeEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.player.PlayerToggleSneakEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::player::player_toggle_sneak_event::PlayerToggleSneakEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::player::player_toggle_sneak_event::PlayerToggleSneakEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.player.PlayerToggleSprintEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::player::player_toggle_sprint_event::PlayerToggleSprintEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::player::player_toggle_sprint_event::PlayerToggleSprintEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.player.PlayerToggleFlightEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::player::player_toggle_flight_event::PlayerToggleFlightEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::player::player_toggle_flight_event::PlayerToggleFlightEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.player.PlayerMoveEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::player::player_move::PlayerMoveEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::player::player_move::PlayerMoveEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.player.PlayerInteractEntityEvent" | "org.bukkit.event.player.PlayerInteractAtEntityEvent" | "org.bukkit.event.entity.EntityDamageByEntityEvent" | "org.bukkit.event.entity.EntityDamageEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::player::player_interact_entity_event::PlayerInteractEntityEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::player::player_interact_entity_event::PlayerInteractEntityEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                "org.bukkit.event.player.AsyncPlayerChatEvent" | "org.bukkit.event.player.PlayerChatEvent" => {
                    context
                        .register_event::<
                            pumpkin::plugin::player::player_chat::PlayerChatEvent,
                            PatchBukkitEventHandler<pumpkin::plugin::player::player_chat::PlayerChatEvent>,
                        >(
                            Arc::new(PatchBukkitEventHandler::new(
                                request.plugin_name.clone(),
                                command_tx.clone(),
                            )),
                            pumpkin_priority,
                            request.blocking,
                        )
                        .await;
                }
                // Player Events
                "org.bukkit.event.player.PlayerDropItemEvent"
                | "org.bukkit.event.player.PlayerItemHeldEvent"
                | "org.bukkit.event.player.PlayerCommandPreprocessEvent"
                | "org.bukkit.event.player.PlayerRespawnEvent"
                | "org.bukkit.event.player.PlayerTeleportEvent"
                | "org.bukkit.event.player.PlayerChangedWorldEvent"
                | "org.bukkit.event.player.PlayerBedEnterEvent"
                | "org.bukkit.event.player.PlayerBedLeaveEvent"
                | "org.bukkit.event.player.PlayerItemConsumeEvent"
                | "org.bukkit.event.player.PlayerItemDamageEvent"
                | "org.bukkit.event.player.PlayerItemBreakEvent"
                | "org.bukkit.event.player.PlayerAnimationEvent"
                | "org.bukkit.event.player.PlayerBucketEmptyEvent"
                | "org.bukkit.event.player.PlayerBucketFillEvent"
                | "org.bukkit.event.player.PlayerAdvancementDoneEvent"
                | "org.bukkit.event.player.PlayerExpChangeEvent"
                | "org.bukkit.event.player.PlayerLevelChangeEvent"
                | "org.bukkit.event.player.PlayerResourcePackStatusEvent"
                | "org.bukkit.event.player.PlayerStatisticIncrementEvent"
                | "org.bukkit.event.player.PlayerPortalEvent"
                | "org.bukkit.event.player.PlayerKickEvent"
                | "org.bukkit.event.player.PlayerLocaleChangeEvent"
                | "org.bukkit.event.player.PlayerArmorStandManipulateEvent"
                | "org.bukkit.event.player.PlayerTakeLecternBookEvent"
                | "org.bukkit.event.player.PlayerUnleashEntityEvent"
                | "org.bukkit.event.player.PlayerShearEntityEvent"
                | "org.bukkit.event.player.PlayerEggThrowEvent"
                | "org.bukkit.event.player.PlayerFishEvent"
                | "org.bukkit.event.player.PlayerEvent"
                // Entity Events
                | "org.bukkit.event.entity.EntityDamageByBlockEvent"
                | "org.bukkit.event.entity.EntityDeathEvent"
                | "org.bukkit.event.entity.PlayerDeathEvent"
                | "org.bukkit.event.entity.CreatureSpawnEvent"
                | "org.bukkit.event.entity.EntitySpawnEvent"
                | "org.bukkit.event.entity.EntityTargetEvent"
                | "org.bukkit.event.entity.EntityTargetLivingEntityEvent"
                | "org.bukkit.event.entity.EntityCombustEvent"
                | "org.bukkit.event.entity.EntityCombustByEntityEvent"
                | "org.bukkit.event.entity.EntityRegainHealthEvent"
                | "org.bukkit.event.entity.EntityShootBowEvent"
                | "org.bukkit.event.entity.EntityToggleGlideEvent"
                | "org.bukkit.event.entity.EntityPickupItemEvent"
                | "org.bukkit.event.entity.EntityDropItemEvent"
                | "org.bukkit.event.entity.EntityExplodeEvent"
                | "org.bukkit.event.entity.ExplosionPrimeEvent"
                | "org.bukkit.event.entity.FoodLevelChangeEvent"
                | "org.bukkit.event.entity.ProjectileHitEvent"
                | "org.bukkit.event.entity.ProjectileLaunchEvent"
                | "org.bukkit.event.entity.EntityInteractEvent"
                | "org.bukkit.event.entity.EntityTransformEvent"
                | "org.bukkit.event.entity.EntityDismountEvent"
                | "org.bukkit.event.entity.EntityMountEvent"
                | "org.bukkit.event.entity.EntityEvent"
                // Block Events
                | "org.bukkit.event.block.BlockSpreadEvent"
                | "org.bukkit.event.block.BlockBurnEvent"
                | "org.bukkit.event.block.BlockPhysicsEvent"
                | "org.bukkit.event.block.BlockRedstoneEvent"
                | "org.bukkit.event.block.BlockPistonExtendEvent"
                | "org.bukkit.event.block.BlockPistonRetractEvent"
                | "org.bukkit.event.block.BlockExplodeEvent"
                | "org.bukkit.event.block.BlockFromToEvent"
                | "org.bukkit.event.block.BlockDispenseEvent"
                | "org.bukkit.event.block.LeavesDecayEvent"
                | "org.bukkit.event.block.BlockCanBuildEvent"
                | "org.bukkit.event.block.BlockEvent"
                // Inventory Events
                | "org.bukkit.event.inventory.InventoryClickEvent"
                | "org.bukkit.event.inventory.InventoryCloseEvent"
                | "org.bukkit.event.inventory.InventoryOpenEvent"
                | "org.bukkit.event.inventory.InventoryDragEvent"
                | "org.bukkit.event.inventory.InventoryCreativeEvent"
                | "org.bukkit.event.inventory.CraftItemEvent"
                | "org.bukkit.event.inventory.PrepareItemCraftEvent"
                | "org.bukkit.event.inventory.PrepareAnvilEvent"
                | "org.bukkit.event.inventory.PrepareSmithingEvent"
                | "org.bukkit.event.inventory.FurnaceSmeltEvent"
                | "org.bukkit.event.inventory.FurnaceBurnEvent"
                | "org.bukkit.event.inventory.InventoryEvent"
                // Server / World / Weather Events
                | "org.bukkit.event.server.PluginEnableEvent"
                | "org.bukkit.event.server.PluginDisableEvent"
                | "org.bukkit.event.server.PluginEvent"
                | "org.bukkit.event.server.ServerListPingEvent"
                | "org.bukkit.event.server.ServiceRegisterEvent"
                | "org.bukkit.event.server.ServiceUnregisterEvent"
                | "org.bukkit.event.server.MapInitializeEvent"
                | "org.bukkit.event.server.ServerEvent"
                | "org.bukkit.event.world.WorldInitEvent"
                | "org.bukkit.event.world.WorldLoadEvent"
                | "org.bukkit.event.world.WorldUnloadEvent"
                | "org.bukkit.event.world.WorldSaveEvent"
                | "org.bukkit.event.world.ChunkLoadEvent"
                | "org.bukkit.event.world.ChunkUnloadEvent"
                | "org.bukkit.event.world.WorldEvent"
                | "org.bukkit.event.weather.WeatherChangeEvent"
                | "org.bukkit.event.weather.ThunderChangeEvent"
                | "org.bukkit.event.weather.WeatherEvent" => {
                    tracing::info!(
                        "Registered Bukkit event listener '{}' from plugin '{}'",
                        request.event_type,
                        request.plugin_name
                    );
                }
                _ => {
                    tracing::warn!(
                        "Unsupported Bukkit event type '{}' from plugin '{}'",
                        request.event_type, request.plugin_name
                    );
                }
            }
        });
    });

    Some(())
}

pub fn ffi_native_bridge_call_event_impl(request: CallEventRequest) -> Option<CallEventResponse> {
    let ctx = CALLBACK_CONTEXT.get()?;
    let event = request.event?;
    tracing::debug!("Java calling event {:?}", event);

    let context = ctx.plugin_context.clone();

    let handled = tokio::task::block_in_place(|| {
        ctx.runtime.block_on(async {
            match event.data? {
                Data::PlayerJoin(player_join_event_data) => {
                    let uuid =
                        uuid::Uuid::parse_str(&player_join_event_data.player_uuid?.value).ok()?;
                    let player = context.server.get_player_by_uuid(uuid)?;
                    let mut pumpkin_event = PlayerJoinEvent::new(
                        player,
                        TextComponent::from_legacy_string(&player_join_event_data.join_message),
                    );
                    context
                        .server
                        .plugin_manager
                        .fire(&context.server, &mut pumpkin_event)
                        .await;
                    Some(true)
                }
                Data::PlayerQuit(player_quit_event_data) => {
                    let uuid =
                        uuid::Uuid::parse_str(&player_quit_event_data.player_uuid?.value).ok()?;
                    let player = context.server.get_player_by_uuid(uuid)?;
                    let mut pumpkin_event = pumpkin::plugin::player::player_leave::PlayerLeaveEvent::new(
                        player,
                        TextComponent::from_legacy_string(&player_quit_event_data.quit_message),
                    );
                    context
                        .server
                        .plugin_manager
                        .fire(&context.server, &mut pumpkin_event)
                        .await;
                    Some(true)
                }
                Data::PlayerGamemodeChange(event_data) => {
                    let uuid =
                        uuid::Uuid::parse_str(&event_data.player_uuid?.value).ok()?;
                    let player = context.server.get_player_by_uuid(uuid)?;
                    let new_gamemode = match event_data.new_gamemode.to_lowercase().as_str() {
                        "creative" => pumpkin_util::GameMode::Creative,
                        "adventure" => pumpkin_util::GameMode::Adventure,
                        "spectator" => pumpkin_util::GameMode::Spectator,
                        _ => pumpkin_util::GameMode::Survival,
                    };
                    let old_gamemode = player.gamemode.load();
                    let mut pumpkin_event = pumpkin::plugin::player::player_gamemode_change::PlayerGamemodeChangeEvent::new(
                        player,
                        old_gamemode,
                        new_gamemode,
                    );
                    context
                        .server
                        .plugin_manager
                        .fire(&context.server, &mut pumpkin_event)
                        .await;
                    Some(true)
                }
                Data::PlayerInteract(event_data) => {
                    let uuid =
                        uuid::Uuid::parse_str(&event_data.player_uuid?.value).ok()?;
                    let player = context.server.get_player_by_uuid(uuid)?;
                    let action = match event_data.action.as_str() {
                        "LEFT_CLICK_BLOCK" => pumpkin::plugin::player::player_interact_event::InteractAction::LeftClickBlock,
                        "LEFT_CLICK_AIR" => pumpkin::plugin::player::player_interact_event::InteractAction::LeftClickAir,
                        "RIGHT_CLICK_AIR" => pumpkin::plugin::player::player_interact_event::InteractAction::RightClickAir,
                        _ => pumpkin::plugin::player::player_interact_event::InteractAction::RightClickBlock,
                    };
                    let block_pos = pumpkin_util::math::position::BlockPos::new(event_data.clicked_x, event_data.clicked_y, event_data.clicked_z);
                    let mut pumpkin_event = pumpkin::plugin::player::player_interact_event::PlayerInteractEvent::new(
                        &player,
                        action,
                        &pumpkin_data::Block::AIR,
                        Some(block_pos),
                    );
                    context
                        .server
                        .plugin_manager
                        .fire(&context.server, &mut pumpkin_event)
                        .await;
                    Some(true)
                }
                Data::BlockBreak(event_data) => {
                    let player = if let Some(uuid_proto) = event_data.player_uuid {
                        let uuid = uuid::Uuid::parse_str(&uuid_proto.value).ok()?;
                        context.server.get_player_by_uuid(uuid)
                    } else {
                        None
                    };
                    let block_pos = pumpkin_util::math::position::BlockPos::new(event_data.block_x, event_data.block_y, event_data.block_z);
                    let mut pumpkin_event = pumpkin::plugin::block::block_break::BlockBreakEvent::new(
                        player,
                        &pumpkin_data::Block::AIR,
                        block_pos,
                        event_data.exp,
                        event_data.drop_items,
                    );
                    context
                        .server
                        .plugin_manager
                        .fire(&context.server, &mut pumpkin_event)
                        .await;
                    Some(true)
                }
                Data::BlockPlace(event_data) => {
                    let uuid =
                        uuid::Uuid::parse_str(&event_data.player_uuid?.value).ok()?;
                    let player = context.server.get_player_by_uuid(uuid)?;
                    let block_pos = pumpkin_util::math::position::BlockPos::new(event_data.block_x, event_data.block_y, event_data.block_z);
                    let mut pumpkin_event = pumpkin::plugin::block::block_place::BlockPlaceEvent::new(
                        player,
                        &pumpkin_data::Block::AIR,
                        &pumpkin_data::Block::AIR,
                        block_pos,
                        event_data.can_build,
                    );
                    context
                        .server
                        .plugin_manager
                        .fire(&context.server, &mut pumpkin_event)
                        .await;
                    Some(true)
                }
                Data::PlayerToggleSneak(event_data) => {
                    let uuid =
                        uuid::Uuid::parse_str(&event_data.player_uuid?.value).ok()?;
                    let player = context.server.get_player_by_uuid(uuid)?;
                    let mut pumpkin_event = pumpkin::plugin::player::player_toggle_sneak_event::PlayerToggleSneakEvent::new(
                        player,
                        event_data.is_sneaking,
                    );
                    context
                        .server
                        .plugin_manager
                        .fire(&context.server, &mut pumpkin_event)
                        .await;
                    Some(true)
                }
                Data::PlayerToggleSprint(event_data) => {
                    let uuid =
                        uuid::Uuid::parse_str(&event_data.player_uuid?.value).ok()?;
                    let player = context.server.get_player_by_uuid(uuid)?;
                    let mut pumpkin_event = pumpkin::plugin::player::player_toggle_sprint_event::PlayerToggleSprintEvent::new(
                        player,
                        event_data.is_sprinting,
                    );
                    context
                        .server
                        .plugin_manager
                        .fire(&context.server, &mut pumpkin_event)
                        .await;
                    Some(true)
                }
                Data::PlayerToggleFlight(event_data) => {
                    let uuid =
                        uuid::Uuid::parse_str(&event_data.player_uuid?.value).ok()?;
                    let player = context.server.get_player_by_uuid(uuid)?;
                    let mut pumpkin_event = pumpkin::plugin::player::player_toggle_flight_event::PlayerToggleFlightEvent::new(
                        player,
                        event_data.is_flying,
                    );
                    context
                        .server
                        .plugin_manager
                        .fire(&context.server, &mut pumpkin_event)
                        .await;
                    Some(true)
                }
                Data::PlayerMove(event_data) => {
                    let uuid =
                        uuid::Uuid::parse_str(&event_data.player_uuid?.value).ok()?;
                    let player = context.server.get_player_by_uuid(uuid)?;
                    let from = event_data
                        .from
                        .and_then(|l| l.position)
                        .map(|p| pumpkin_util::math::vector3::Vector3::new(p.x, p.y, p.z))
                        .unwrap_or_default();
                    let to = event_data
                        .to
                        .and_then(|l| l.position)
                        .map(|p| pumpkin_util::math::vector3::Vector3::new(p.x, p.y, p.z))
                        .unwrap_or_default();
                    let mut pumpkin_event = pumpkin::plugin::player::player_move::PlayerMoveEvent::new(
                        player, from, to,
                    );
                    context
                        .server
                        .plugin_manager
                        .fire(&context.server, &mut pumpkin_event)
                        .await;
                    Some(true)
                }
                Data::PlayerInteractEntity(event_data) => {
                    let player_uuid =
                        uuid::Uuid::parse_str(&event_data.player_uuid?.value).ok()?;
                    let player = context.server.get_player_by_uuid(player_uuid)?;
                    let target_uuid =
                        uuid::Uuid::parse_str(&event_data.target_uuid?.value).ok()?;
                    let target = context.server.get_player_by_uuid(target_uuid);
                    if let Some(target_player) = target {
                        let action = match event_data.action.as_str() {
                            "ATTACK" => pumpkin_protocol::java::server::play::ActionType::Attack,
                            "INTERACT_AT" => pumpkin_protocol::java::server::play::ActionType::InteractAt,
                            _ => pumpkin_protocol::java::server::play::ActionType::Interact,
                        };
                        let mut pumpkin_event = pumpkin::plugin::player::player_interact_entity_event::PlayerInteractEntityEvent::new(
                            &player,
                            target_player,
                            action,
                            None,
                            event_data.is_sneaking,
                        );
                        context
                            .server
                            .plugin_manager
                            .fire(&context.server, &mut pumpkin_event)
                            .await;
                    }
                    Some(true)
                }
                Data::PlayerChat(event_data) => {
                    let uuid =
                        uuid::Uuid::parse_str(&event_data.player_uuid?.value).ok()?;
                    let player = context.server.get_player_by_uuid(uuid)?;
                    let mut pumpkin_event = pumpkin::plugin::player::player_chat::PlayerChatEvent::new(
                        player,
                        event_data.message,
                        Vec::new(),
                    );
                    context
                        .server
                        .plugin_manager
                        .fire(&context.server, &mut pumpkin_event)
                        .await;
                    Some(true)
                }
                Data::SignChange(event_data) => {
                    let uuid =
                        uuid::Uuid::parse_str(&event_data.player_uuid?.value).ok()?;
                    let player = context.server.get_player_by_uuid(uuid)?;
                    let block_pos = pumpkin_util::math::position::BlockPos::new(event_data.block_x, event_data.block_y, event_data.block_z);
                    let mut pumpkin_event = pumpkin::plugin::block::sign_change::SignChangeEvent::new(
                        player,
                        block_pos,
                        event_data.lines,
                    );
                    context
                        .server
                        .plugin_manager
                        .fire(&context.server, &mut pumpkin_event)
                        .await;
                    Some(true)
                }
                Data::BlockDamage(event_data) => {
                    let player = if let Some(uuid_proto) = event_data.player_uuid {
                        let uuid = uuid::Uuid::parse_str(&uuid_proto.value).ok()?;
                        context.server.get_player_by_uuid(uuid)
                    } else {
                        None
                    };
                    if let Some(player) = player {
                        let block_pos = pumpkin_util::math::position::BlockPos::new(event_data.block_x, event_data.block_y, event_data.block_z);
                        let mut pumpkin_event = pumpkin::plugin::block::block_damage::BlockDamageEvent::new(
                            player,
                            &pumpkin_data::Block::AIR,
                            block_pos,
                            event_data.insta_break,
                        );
                        context
                            .server
                            .plugin_manager
                            .fire(&context.server, &mut pumpkin_event)
                            .await;
                    }
                    Some(true)
                }
                Data::ServerCommand(event_data) => {
                    let mut pumpkin_event = pumpkin::plugin::server::server_command::ServerCommandEvent::new(
                        event_data.command,
                    );
                    context
                        .server
                        .plugin_manager
                        .fire(&context.server, &mut pumpkin_event)
                        .await;
                    Some(true)
                }
                Data::BlockIgnite(event_data) => {
                    let player = if let Some(uuid_proto) = event_data.player_uuid {
                        let uuid = uuid::Uuid::parse_str(&uuid_proto.value).ok()?;
                        context.server.get_player_by_uuid(uuid)
                    } else {
                        None
                    };
                    let block_pos = pumpkin_util::math::position::BlockPos::new(event_data.block_x, event_data.block_y, event_data.block_z);
                    let mut pumpkin_event = pumpkin::plugin::block::block_ignite::BlockIgniteEvent::new(
                        block_pos,
                        &pumpkin_data::Block::AIR,
                        player,
                    );
                    context
                        .server
                        .plugin_manager
                        .fire(&context.server, &mut pumpkin_event)
                        .await;
                    Some(true)
                }
                Data::BlockGrow(event_data) => {
                    let block_pos = pumpkin_util::math::position::BlockPos::new(event_data.block_x, event_data.block_y, event_data.block_z);
                    let world = context.server.worlds.load().first().cloned()?;
                    let mut pumpkin_event = pumpkin::plugin::block::block_grow::BlockGrowEvent::new(
                        world,
                        &pumpkin_data::Block::AIR,
                        pumpkin_data::BlockStateId::new_or_air(0),
                        &pumpkin_data::Block::AIR,
                        pumpkin_data::BlockStateId::new_or_air(0),
                        block_pos,
                    );
                    context
                        .server
                        .plugin_manager
                        .fire(&context.server, &mut pumpkin_event)
                        .await;
                    Some(true)
                }
                Data::BlockForm(event_data) => {
                    let block_pos = pumpkin_util::math::position::BlockPos::new(event_data.block_x, event_data.block_y, event_data.block_z);
                    let mut pumpkin_event = pumpkin::plugin::block::block_form::BlockFormEvent::new(
                        block_pos,
                        &pumpkin_data::Block::AIR,
                    );
                    context
                        .server
                        .plugin_manager
                        .fire(&context.server, &mut pumpkin_event)
                        .await;
                    Some(true)
                }
                Data::BlockFade(event_data) => {
                    let block_pos = pumpkin_util::math::position::BlockPos::new(event_data.block_x, event_data.block_y, event_data.block_z);
                    let mut pumpkin_event = pumpkin::plugin::block::block_fade::BlockFadeEvent::new(
                        block_pos,
                        &pumpkin_data::Block::AIR,
                    );
                    context
                        .server
                        .plugin_manager
                        .fire(&context.server, &mut pumpkin_event)
                        .await;
                    Some(true)
                }
                Data::PluginEnable(plugin_enable_event) => {
                    tracing::info!("Plugin enable event for {}", plugin_enable_event.plugin_name);
                    Some(true)
                }
                Data::PluginDisable(plugin_disable_event) => {
                    tracing::info!("Plugin disable event for {}", plugin_disable_event.plugin_name);
                    Some(true)
                }
            }
        })
    })?;

    Some(CallEventResponse { handled })
}
