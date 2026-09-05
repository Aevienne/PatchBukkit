use crate::{
    java::native_callbacks::CALLBACK_CONTEXT,
    proto::patchbukkit::common::{Location, Uuid, Vec3, World},
};

#[repr(C)]
pub struct Vec3FFI {
    pub x: f64,
    pub y: f64,
    pub z: f64,
}

pub fn ffi_native_bridge_get_location_impl(entity_uuid: Uuid) -> Option<Location> {
    let ctx = CALLBACK_CONTEXT.get()?;
    let uuid = uuid::Uuid::parse_str(&entity_uuid.value).ok()?;

    // Entity lookup across all worlds (old TargetSelector helper is gone).
    for world in ctx.plugin_context.server.worlds.load().iter() {
        for entity in world.entities.load().iter() {
            let base = entity.get_entity();
            if base.entity_uuid != uuid {
                continue;
            }
            let position = base.pos.load();
            let world_uuid = base.world.load().uuid;
            let yaw = base.yaw.load();
            let pitch = base.pitch.load();

            return Some(Location {
                world: Some(World {
                    uuid: Some(Uuid {
                        value: world_uuid.to_string(),
                    }),
                }),
                position: Some(Vec3 {
                    x: position.x,
                    y: position.y,
                    z: position.z,
                }),
                yaw,
                pitch,
            });
        }
    }

    None
}
