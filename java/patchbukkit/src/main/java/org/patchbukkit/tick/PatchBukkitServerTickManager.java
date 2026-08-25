package org.patchbukkit.tick;

import org.bukkit.ServerTickManager;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import patchbukkit.bridge.NativeBridgeFfi;
import patchbukkit.common.EmptyRequest;
import patchbukkit.server.ServerTickInfoResponse;
import patchbukkit.server.SetServerTickRateRequest;

public class PatchBukkitServerTickManager implements ServerTickManager {
    private float tickRate = 20.0f;
    private boolean frozen = false;

    @Override
    public boolean isRunningNormally() {
        return !isFrozen() && !isSprinting();
    }

    @Override
    public boolean isStepping() {
        try {
            ServerTickInfoResponse info = NativeBridgeFfi.getServerTickInfo(EmptyRequest.getDefaultInstance());
            if (info != null) return info.getIsStepping();
        } catch (Throwable ignored) {}
        return false;
    }

    @Override
    public boolean isSprinting() {
        try {
            ServerTickInfoResponse info = NativeBridgeFfi.getServerTickInfo(EmptyRequest.getDefaultInstance());
            if (info != null) return info.getIsSprinting();
        } catch (Throwable ignored) {}
        return false;
    }

    @Override
    public boolean isFrozen() {
        try {
            ServerTickInfoResponse info = NativeBridgeFfi.getServerTickInfo(EmptyRequest.getDefaultInstance());
            if (info != null) return info.getIsFrozen();
        } catch (Throwable ignored) {}
        return this.frozen;
    }

    @Override
    public float getTickRate() {
        try {
            ServerTickInfoResponse info = NativeBridgeFfi.getServerTickInfo(EmptyRequest.getDefaultInstance());
            if (info != null) return info.getTickRate();
        } catch (Throwable ignored) {}
        return this.tickRate;
    }

    @Override
    public void setTickRate(float tickRate) {
        this.tickRate = tickRate;
        try {
            NativeBridgeFfi.setServerTickRate(SetServerTickRateRequest.newBuilder()
                .setTickRate(tickRate)
                .setFrozen(this.frozen)
                .build());
        } catch (Throwable ignored) {}
    }

    @Override
    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
        try {
            NativeBridgeFfi.setServerTickRate(SetServerTickRateRequest.newBuilder()
                .setTickRate(this.tickRate)
                .setFrozen(frozen)
                .build());
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean stepGameIfFrozen(int ticks) {
        return true;
    }

    @Override
    public boolean stopStepping() {
        return true;
    }

    @Override
    public boolean requestGameToSprint(int ticks) {
        return true;
    }

    @Override
    public boolean stopSprinting() {
        return true;
    }

    @Override
    public boolean isFrozen(@NotNull Entity entity) {
        return isFrozen();
    }

    @Override
    public int getFrozenTicksToRun() {
        return 0;
    }
}
