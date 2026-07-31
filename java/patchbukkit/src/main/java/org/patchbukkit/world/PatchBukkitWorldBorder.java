package org.patchbukkit.world;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;

import patchbukkit.bridge.NativeBridgeFfi;
import patchbukkit.world.GetWorldBorderRequest;
import patchbukkit.world.SetWorldBorderRequest;
import patchbukkit.world.WorldBorderData;
import org.patchbukkit.bridge.BridgeUtils;

import java.util.concurrent.TimeUnit;

public class PatchBukkitWorldBorder implements WorldBorder {
    private final World world;

    public PatchBukkitWorldBorder(World world) {
        this.world = world;
    }

    private WorldBorderData getData() {
        if (this.world == null) {
            return WorldBorderData.newBuilder()
                .setCenterX(0.0)
                .setCenterZ(0.0)
                .setSize(59999968.0)
                .setTargetSize(59999968.0)
                .setSpeed(0)
                .setWarningTime(15)
                .setWarningBlocks(5)
                .setDamagePerBlock(0.2)
                .setDamageBuffer(5.0)
                .setMaxCenterCoordinate(29999984)
                .build();
        }
        return NativeBridgeFfi.getWorldBorder(
            GetWorldBorderRequest.newBuilder()
                .setWorldUuid(BridgeUtils.convertUuid(this.world.getUID()))
                .build()
        );
    }

    private void updateData(WorldBorderData.Builder builder) {
        if (this.world == null) {
            return;
        }
        NativeBridgeFfi.setWorldBorder(
            SetWorldBorderRequest.newBuilder()
                .setWorldUuid(BridgeUtils.convertUuid(this.world.getUID()))
                .setBorder(builder)
                .build()
        );
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public void reset() {
        if (this.world == null) return;
        WorldBorderData.Builder builder = getData().toBuilder();
        builder.setCenterX(0.0);
        builder.setCenterZ(0.0);
        builder.setSize(59999968.0);
        builder.setTargetSize(59999968.0);
        builder.setSpeed(0);
        builder.setWarningTime(15);
        builder.setWarningBlocks(5);
        builder.setDamagePerBlock(0.2);
        builder.setDamageBuffer(5.0);
        updateData(builder);
    }

    @Override
    public double getSize() {
        return getData().getSize();
    }

    @Override
    public void setSize(double newSize) {
        if (this.world == null) return;
        WorldBorderData.Builder builder = getData().toBuilder();
        builder.setSize(newSize);
        builder.setTargetSize(newSize);
        builder.setSpeed(0);
        updateData(builder);
    }

    @Override
    public void setSize(double newSize, long seconds) {
        setSize(newSize, TimeUnit.SECONDS, seconds);
    }

    @Override
    public void setSize(double newSize, TimeUnit unit, long time) {
        if (this.world == null) return;
        long timeInMillis = unit.toMillis(time);
        WorldBorderData.Builder builder = getData().toBuilder();
        builder.setTargetSize(newSize);
        builder.setSpeed(timeInMillis);
        updateData(builder);
    }

    @Override
    public void changeSize(double newSize, long seconds) {
        setSize(getSize() + newSize, seconds);
    }

    public void changeSize(double newSize, TimeUnit unit, long time) {
        setSize(getSize() + newSize, unit, time);
    }

    @Override
    public Location getCenter() {
        WorldBorderData data = getData();
        return new Location(this.world, data.getCenterX(), 0.0, data.getCenterZ());
    }

    @Override
    public void setCenter(double x, double z) {
        if (this.world == null) return;
        WorldBorderData.Builder builder = getData().toBuilder();
        builder.setCenterX(x);
        builder.setCenterZ(z);
        updateData(builder);
    }

    @Override
    public void setCenter(Location location) {
        setCenter(location.getX(), location.getZ());
    }

    @Override
    public double getDamageBuffer() {
        return getData().getDamageBuffer();
    }

    @Override
    public void setDamageBuffer(double blocks) {
        if (this.world == null) return;
        WorldBorderData.Builder builder = getData().toBuilder();
        builder.setDamageBuffer(blocks);
        updateData(builder);
    }

    @Override
    public double getDamageAmount() {
        return getData().getDamagePerBlock();
    }

    @Override
    public void setDamageAmount(double damage) {
        if (this.world == null) return;
        WorldBorderData.Builder builder = getData().toBuilder();
        builder.setDamagePerBlock(damage);
        updateData(builder);
    }

    @Override
    public int getWarningTime() {
        return getData().getWarningTime();
    }

    @Override
    public void setWarningTime(int seconds) {
        if (this.world == null) return;
        WorldBorderData.Builder builder = getData().toBuilder();
        builder.setWarningTime(seconds);
        updateData(builder);
    }

    @Override
    public int getWarningTimeTicks() {
        return getWarningTime() * 20;
    }

    @Override
    public void setWarningTimeTicks(int ticks) {
        setWarningTime(ticks / 20);
    }

    @Override
    public int getWarningDistance() {
        return getData().getWarningBlocks();
    }

    @Override
    public void setWarningDistance(int distance) {
        if (this.world == null) return;
        WorldBorderData.Builder builder = getData().toBuilder();
        builder.setWarningBlocks(distance);
        updateData(builder);
    }

    @Override
    public boolean isInside(Location location) {
        if (location == null) return false;
        if (this.world != null && location.getWorld() != null && !this.world.equals(location.getWorld())) {
            return false;
        }
        WorldBorderData data = getData();
        double radius = data.getSize() / 2.0;
        double minX = data.getCenterX() - radius;
        double maxX = data.getCenterX() + radius;
        double minZ = data.getCenterZ() - radius;
        double maxZ = data.getCenterZ() + radius;
        return location.getX() >= minX && location.getX() <= maxX && location.getZ() >= minZ && location.getZ() <= maxZ;
    }

    @Override
    public double getMaxCenterCoordinate() {
        return getData().getMaxCenterCoordinate();
    }

    @Override
    public double getMaxSize() {
        return getData().getMaxCenterCoordinate() * 2.0;
    }
}
