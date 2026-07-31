use crate::{
    java::native_callbacks::CALLBACK_CONTEXT,
    proto::patchbukkit::world::{
        GetBlockDataRequest, GetBlockDataResponse, SetBlockDataRequest, SpawnParticleRequest,
    },
};

pub fn ffi_native_bridge_get_block_data_impl(
    request: GetBlockDataRequest,
) -> Option<GetBlockDataResponse> {
    let ctx = CALLBACK_CONTEXT.get()?;
    let uuid_str = &request.world_uuid.as_ref()?.value;
    let world_uuid = uuid::Uuid::parse_str(uuid_str).ok()?;

    let worlds = ctx.plugin_context.server.worlds.load_full();
    let world = worlds
        .iter()
        .find(|w| w.uuid == world_uuid)
        .cloned()
        .or_else(|| worlds.first().cloned())?;
    let pos = pumpkin_util::math::position::BlockPos::new(request.x, request.y, request.z);

    let state_id = world.get_block_state(&pos).id;

    Some(GetBlockDataResponse {
        block_state: format!("minecraft:block_{state_id}"),
    })
}

pub fn ffi_native_bridge_set_block_data_impl(request: SetBlockDataRequest) -> Option<()> {
    let ctx = CALLBACK_CONTEXT.get()?;
    let uuid_str = &request.world_uuid.as_ref()?.value;
    let world_uuid = uuid::Uuid::parse_str(uuid_str).ok()?;

    let worlds = ctx.plugin_context.server.worlds.load_full();
    let world = worlds
        .iter()
        .find(|w| w.uuid == world_uuid)
        .cloned()
        .or_else(|| worlds.first().cloned())?;
    let pos = pumpkin_util::math::position::BlockPos::new(request.x, request.y, request.z);

    ctx.runtime.spawn(async move {
        world
            .set_block_state(
                &pos,
                pumpkin_data::BlockStateId::new_or_air(1),
                pumpkin::world::BlockFlags::NOTIFY_ALL,
            )
            .await;
    });

    Some(())
}

pub fn ffi_native_bridge_spawn_particle_impl(_request: SpawnParticleRequest) -> Option<()> {
    Some(())
}

pub fn ffi_native_bridge_get_worlds_impl(
    _request: crate::proto::patchbukkit::common::EmptyRequest,
) -> Option<crate::proto::patchbukkit::world::GetWorldsResponse> {
    let ctx = CALLBACK_CONTEXT.get()?;
    let worlds = ctx.plugin_context.server.worlds.load_full();
    let world_uuids = worlds
        .iter()
        .map(|w| crate::proto::patchbukkit::common::Uuid {
            value: w.uuid.to_string(),
        })
        .collect();

    Some(crate::proto::patchbukkit::world::GetWorldsResponse { world_uuids })
}

pub fn ffi_native_bridge_get_world_border_impl(
    request: crate::proto::patchbukkit::world::GetWorldBorderRequest,
) -> Option<crate::proto::patchbukkit::world::WorldBorderData> {
    let ctx = CALLBACK_CONTEXT.get()?;
    let uuid_str = &request.world_uuid.as_ref()?.value;
    let world_uuid = uuid::Uuid::parse_str(uuid_str).ok()?;

    let worlds = ctx.plugin_context.server.worlds.load_full();
    let world = worlds
        .iter()
        .find(|w| w.uuid == world_uuid)
        .cloned()
        .or_else(|| worlds.first().cloned())?;

    let wb = world.worldborder.blocking_lock();

    Some(crate::proto::patchbukkit::world::WorldBorderData {
        center_x: wb.center_x,
        center_z: wb.center_z,
        size: wb.old_diameter,
        target_size: wb.new_diameter,
        speed: wb.speed,
        warning_time: wb.warning_time,
        warning_blocks: wb.warning_blocks,
        damage_per_block: wb.damage_per_block as f64,
        damage_buffer: wb.buffer as f64,
        max_center_coordinate: wb.portal_teleport_boundary,
    })
}

pub fn ffi_native_bridge_set_world_border_impl(
    request: crate::proto::patchbukkit::world::SetWorldBorderRequest,
) -> Option<()> {
    let ctx = CALLBACK_CONTEXT.get()?;
    let uuid_str = &request.world_uuid.as_ref()?.value;
    let world_uuid = uuid::Uuid::parse_str(uuid_str).ok()?;
    let border_data = request.border?;

    let worlds = ctx.plugin_context.server.worlds.load_full();
    let world = worlds
        .iter()
        .find(|w| w.uuid == world_uuid)
        .cloned()
        .or_else(|| worlds.first().cloned())?;

    let mut wb = world.worldborder.blocking_lock();
    wb.center_x = border_data.center_x;
    wb.center_z = border_data.center_z;
    wb.old_diameter = border_data.size;
    wb.new_diameter = border_data.target_size;
    wb.speed = border_data.speed;
    wb.warning_time = border_data.warning_time;
    wb.warning_blocks = border_data.warning_blocks;
    wb.damage_per_block = border_data.damage_per_block as f32;
    wb.buffer = border_data.damage_buffer as f32;
    wb.portal_teleport_boundary = border_data.max_center_coordinate;

    Some(())
}

