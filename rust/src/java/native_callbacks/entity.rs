use pumpkin::entity::EntityBase;

use crate::{
    java::native_callbacks::{CALLBACK_CONTEXT, utils::with_player},
    proto::patchbukkit::{
        common::Uuid,
        entity::{
            DamageEntityRequest, EntityHealthResponse, SetEntityHealthRequest,
            SetEntityVelocityRequest, TeleportEntityRequest,
        },
    },
};

pub fn ffi_native_bridge_get_entity_health_impl(request: Uuid) -> Option<EntityHealthResponse> {
    let ctx = CALLBACK_CONTEXT.get()?;
    with_player(Some(&request), |player| {
        let health = tokio::task::block_in_place(|| {
            ctx.runtime
                .block_on(async { f64::from(player.living_entity.health.load()) })
        });

        EntityHealthResponse {
            health,
            max_health: 20.0,
        }
    })
}

pub fn ffi_native_bridge_set_entity_health_impl(request: SetEntityHealthRequest) -> Option<()> {
    let ctx = CALLBACK_CONTEXT.get()?;
    with_player(request.uuid.as_ref(), |player| {
        let health = request.health as f32;
        let player = player.clone();
        ctx.runtime.spawn(async move {
            player.set_health(health).await;
        });
    })
}

pub fn ffi_native_bridge_damage_entity_impl(request: DamageEntityRequest) -> Option<()> {
    let ctx = CALLBACK_CONTEXT.get()?;
    with_player(request.uuid.as_ref(), |player| {
        let amount = request.amount as f32;
        let player = player.clone();
        ctx.runtime.spawn(async move {
            let current = player.living_entity.health.load();
            player.set_health((current - amount).max(0.0)).await;
        });
    })
}

pub fn ffi_native_bridge_set_entity_velocity_impl(request: SetEntityVelocityRequest) -> Option<()> {
    with_player(request.uuid.as_ref(), |player| {
        let velocity = pumpkin_util::math::vector3::Vector3::new(request.x, request.y, request.z);
        player.living_entity.entity.set_velocity(velocity);
    })
}

pub fn ffi_native_bridge_teleport_entity_impl(request: TeleportEntityRequest) -> Option<()> {
    let ctx = CALLBACK_CONTEXT.get()?;
    let loc = request.location?;
    let pos = loc.position?;
    let yaw = loc.yaw;
    let pitch = loc.pitch;

    with_player(request.uuid.as_ref(), |player| {
        let position = pumpkin_util::math::vector3::Vector3::new(pos.x, pos.y, pos.z);
        let world = player.living_entity.entity.world.load_full();
        let player = player.clone();
        ctx.runtime.spawn(async move {
            player
                .teleport(position, Some(yaw), Some(pitch), world)
                .await;
        });
    })
}
