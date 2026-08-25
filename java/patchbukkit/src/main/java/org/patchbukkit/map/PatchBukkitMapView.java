package org.patchbukkit.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.World;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatchBukkitMapView implements MapView {
    private final int id;
    private Scale scale = Scale.NORMAL;
    private int centerX;
    private int centerZ;
    private World world;
    private final List<MapRenderer> renderers = new ArrayList<>();
    private boolean trackingPosition = true;
    private boolean unlimitedTracking = false;
    private boolean locked = false;

    public PatchBukkitMapView(int id, @Nullable World world) {
        this.id = id;
        this.world = world;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    @Override
    public @NotNull Scale getScale() {
        return this.scale;
    }

    @Override
    public void setScale(@NotNull Scale scale) {
        this.scale = scale;
    }

    @Override
    public int getCenterX() {
        return this.centerX;
    }

    @Override
    public int getCenterZ() {
        return this.centerZ;
    }

    @Override
    public void setCenterX(int x) {
        this.centerX = x;
    }

    @Override
    public void setCenterZ(int z) {
        this.centerZ = z;
    }

    @Override
    public @Nullable World getWorld() {
        return this.world;
    }

    @Override
    public void setWorld(@Nullable World world) {
        this.world = world;
    }

    @Override
    public @NotNull List<MapRenderer> getRenderers() {
        return Collections.unmodifiableList(this.renderers);
    }

    @Override
    public void addRenderer(@NotNull MapRenderer renderer) {
        this.renderers.add(renderer);
    }

    @Override
    public boolean removeRenderer(@Nullable MapRenderer renderer) {
        return this.renderers.remove(renderer);
    }

    @Override
    public boolean isTrackingPosition() {
        return this.trackingPosition;
    }

    @Override
    public void setTrackingPosition(boolean trackingPosition) {
        this.trackingPosition = trackingPosition;
    }

    @Override
    public boolean isUnlimitedTracking() {
        return this.unlimitedTracking;
    }

    @Override
    public void setUnlimitedTracking(boolean unlimitedTracking) {
        this.unlimitedTracking = unlimitedTracking;
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    @Override
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
