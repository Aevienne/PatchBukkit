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
