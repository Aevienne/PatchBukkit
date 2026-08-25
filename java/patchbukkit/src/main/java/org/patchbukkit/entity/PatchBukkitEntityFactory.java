package org.patchbukkit.entity;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityFactory;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class PatchBukkitEntityFactory implements EntityFactory {
    public static final PatchBukkitEntityFactory INSTANCE = new PatchBukkitEntityFactory();

    @Override
    public @NotNull EntitySnapshot createEntitySnapshot(@NotNull String input) throws IllegalArgumentException {
        return new EntitySnapshot() {
            @Override
            public @NotNull Entity createEntity(@NotNull World world) {
                return world.spawnEntity(world.getSpawnLocation(), getEntityType());
            }

            @Override
            public @NotNull Entity createEntity(@NotNull Location location) {
                return location.getWorld().spawnEntity(location, getEntityType());
            }

            @Override
            public @NotNull EntityType getEntityType() {
                try {
                    return EntityType.valueOf(input.toUpperCase());
                } catch (Throwable e) {
                    return EntityType.PIG;
                }
            }

            @Override
            public @NotNull String getAsString() {
                return input;
            }
        };
    }
}
