package org.patchbukkit.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class PatchBukkitScheduledTask implements ScheduledTask {
    private final Plugin plugin;
    private final boolean repeating;
    private final AtomicReference<ExecutionState> state = new AtomicReference<>(ExecutionState.IDLE);
    private BukkitTask bukkitTask;
    private Future<?> future;

    public PatchBukkitScheduledTask(@NotNull Plugin plugin, boolean repeating) {
        this.plugin = plugin;
        this.repeating = repeating;
    }

    public void setBukkitTask(BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }

    public void setState(ExecutionState newState) {
        this.state.set(newState);
    }

    @Override
    public @NotNull Plugin getOwningPlugin() {
        return this.plugin;
    }

    @Override
    public boolean isRepeatingTask() {
        return this.repeating;
    }

    @Override
    public @NotNull CancelledState cancel() {
        if (bukkitTask != null) {
            bukkitTask.cancel();
        }
        if (future != null) {
            future.cancel(false);
        }
        state.set(ExecutionState.CANCELLED);
        return CancelledState.CANCELLED_BY_CALLER;
    }

    @Override
    public @NotNull ExecutionState getExecutionState() {
        return this.state.get();
    }
}
