package org.patchbukkit.bridge;

import java.util.UUID;

public class BridgeUtils {
    public static UUID convertUuid(patchbukkit.common.UUID uuid) {
        return UUID.fromString(uuid.getValue());
    }

    public static patchbukkit.common.UUID convertUuid(UUID uuid) {
        return patchbukkit.common.UUID.newBuilder().setValue(uuid.toString()).build();
    }

    public static patchbukkit.common.Location convertLocation(org.bukkit.Location location) {
        var pos = patchbukkit.common.Vec3.newBuilder()
            .setX(location.getX())
            .setY(location.getY())
            .setZ(location.getZ())
            .build();
        var worldUuid = convertUuid(location.getWorld().getUID());
        return patchbukkit.common.Location.newBuilder()
            .setWorld(patchbukkit.common.World.newBuilder().setUuid(worldUuid).build())
            .setPosition(pos)
            .setYaw(location.getYaw())
            .setPitch(location.getPitch())
            .build();
    }
}
