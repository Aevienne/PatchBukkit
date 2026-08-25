package org.patchbukkit.world;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

@SuppressWarnings("deprecation")
public class PatchBukkitBlockState implements BlockState {
    private final Block block;
    private BlockData data;

    public PatchBukkitBlockState(Block block) {
        this.block = block;
        this.data = block.getBlockData().clone();
    }

    @Override
    public @NotNull Block getBlock() {
        return this.block;
    }

    @Override
    public @NotNull Material getType() {
        return this.data.getMaterial();
    }

    @Override
    public void setType(@NotNull Material type) {
        this.data = org.bukkit.Bukkit.createBlockData(type);
    }

    @Override
    public @NotNull BlockData getBlockData() {
        return this.data;
    }

    @Override
    public void setBlockData(@NotNull BlockData data) {
        this.data = data.clone();
    }

    @Override
    public byte getRawData() {
        return 0;
    }

    @Override
    public void setRawData(byte data) {
    }

    @Override
    public boolean update() {
        return update(false);
    }

    @Override
    public boolean update(boolean force) {
        return update(force, true);
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        this.block.setBlockData(this.data, applyPhysics);
        return true;
    }

    @Override
    public @NotNull World getWorld() {
        return this.block.getWorld();
    }

    @Override
    public int getX() {
        return this.block.getX();
    }

    @Override
    public int getY() {
        return this.block.getY();
    }

    @Override
    public int getZ() {
        return this.block.getZ();
    }

    @Override
    public @NotNull Location getLocation() {
        return this.block.getLocation();
    }

    @Override
    public @Nullable Location getLocation(@Nullable Location loc) {
        return this.block.getLocation(loc);
    }

    @Override
    public @NotNull Chunk getChunk() {
        return this.block.getChunk();
    }

    @Override
    public void setMetadata(@NotNull String metadataKey, @NotNull MetadataValue newMetadataValue) {
        this.block.setMetadata(metadataKey, newMetadataValue);
    }

    @Override
    public @NotNull List<MetadataValue> getMetadata(@NotNull String metadataKey) {
        return this.block.getMetadata(metadataKey);
    }

    @Override
    public boolean hasMetadata(@NotNull String metadataKey) {
        return this.block.hasMetadata(metadataKey);
    }

    @Override
    public void removeMetadata(@NotNull String metadataKey, @NotNull Plugin owningPlugin) {
        this.block.removeMetadata(metadataKey, owningPlugin);
    }

    @Override
    public boolean isPlaced() {
        return true;
    }

    @Override
    public boolean isCollidable() {
        return this.block.isCollidable();
    }

    @Override
    public boolean isSuffocating() {
        return this.block.isSuffocating();
    }

    @Override
    public @NotNull MaterialData getData() {
        return new MaterialData(getType());
    }

    @Override
    public void setData(@NotNull MaterialData data) {
    }

    @Override
    public @NotNull BlockState copy() {
        return new PatchBukkitBlockState(this.block);
    }

    @Override
    public @NotNull BlockState copy(@NotNull Location location) {
        return new PatchBukkitBlockState(location.getBlock());
    }

    @Override
    public byte getLightLevel() {
        return this.block.getLightLevel();
    }

    @Override
    public @NotNull @Unmodifiable Collection<ItemStack> getDrops(@Nullable ItemStack tool, @Nullable Entity entity) {
        return this.block.getDrops(tool, entity);
    }
}
