package org.patchbukkit.world;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class PatchBukkitChunk implements Chunk {
    private final World world;
    private final int x;
    private final int z;

    public PatchBukkitChunk(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getZ() {
        return this.z;
    }

    @Override
    public @NotNull World getWorld() {
        return this.world;
    }

    @Override
    public @NotNull Block getBlock(int x, int y, int z) {
        return this.world.getBlockAt((this.x << 4) + x, y, (this.z << 4) + z);
    }

    @Override
    public @NotNull ChunkSnapshot getChunkSnapshot() {
        return getChunkSnapshot(true, false, false);
    }

    @Override
    public @NotNull ChunkSnapshot getChunkSnapshot(boolean includeMaxBlocky, boolean includeBiome, boolean includeBiomeTempRain) {
        return getChunkSnapshot(includeMaxBlocky, includeBiome, includeBiomeTempRain, false);
    }

    @Override
    public @NotNull ChunkSnapshot getChunkSnapshot(boolean includeMaxBlocky, boolean includeBiome, boolean includeBiomeTempRain, boolean includeBiome3D) {
        throw new UnsupportedOperationException("Unimplemented method 'getChunkSnapshot'");
    }

    @Override
    public boolean isEntitiesLoaded() {
        return true;
    }

    @Override
    public @NotNull Entity[] getEntities() {
        return new Entity[0];
    }

    @Override
    public @NotNull BlockState[] getTileEntities() {
        return new BlockState[0];
    }

    @Override
    public @NotNull BlockState[] getTileEntities(boolean useSnapshot) {
        return new BlockState[0];
    }

    @Override
    public boolean isGenerated() {
        return true;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public boolean load(boolean generate) {
        return true;
    }

    @Override
    public boolean load() {
        return true;
    }

    @Override
    public boolean unload(boolean save) {
        return true;
    }

    @Override
    public boolean unload() {
        return true;
    }

    @Override
    public boolean isSlimeChunk() {
        return false;
    }

    @Override
    public boolean isForceLoaded() {
        return false;
    }

    @Override
    public void setForceLoaded(boolean progress) {
    }

    @Override
    public boolean addPluginChunkTicket(@NotNull Plugin plugin) {
        return true;
    }

    @Override
    public boolean removePluginChunkTicket(@NotNull Plugin plugin) {
        return true;
    }

    @Override
    public @NotNull Collection<Plugin> getPluginChunkTickets() {
        return Collections.emptyList();
    }

    @Override
    public long getInhabitedTime() {
        return 0;
    }

    @Override
    public void setInhabitedTime(long ticks) {
    }

    @Override
    public boolean contains(@NotNull BlockData block) {
        return false;
    }

    @Override
    public @NotNull Collection<org.bukkit.entity.Player> getPlayersSeeingChunk() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull Collection<org.bukkit.generator.structure.GeneratedStructure> getStructures() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull Collection<org.bukkit.generator.structure.GeneratedStructure> getStructures(@NotNull org.bukkit.generator.structure.Structure structure) {
        return Collections.emptyList();
    }

    @Override
    public @NotNull LoadLevel getLoadLevel() {
        return LoadLevel.ENTITY_TICKING;
    }

    @Override
    public boolean contains(@NotNull org.bukkit.block.Biome biome) {
        return false;
    }

    private final org.bukkit.persistence.PersistentDataContainer pdc = new org.patchbukkit.persistence.PatchBukkitPersistentDataContainer();

    @Override
    public @NotNull org.bukkit.persistence.PersistentDataContainer getPersistentDataContainer() {
        return this.pdc;
    }

    @Override
    public @NotNull Collection<BlockState> getTileEntities(@NotNull java.util.function.Predicate<? super Block> blockPredicate, boolean useSnapshot) {
        return Collections.emptyList();
    }
}
