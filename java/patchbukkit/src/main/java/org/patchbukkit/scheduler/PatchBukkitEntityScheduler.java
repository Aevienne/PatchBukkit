package org.patchbukkit.scheduler;

import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatchBukkitEntityScheduler implements EntityScheduler {
    private final Entity entity;

    public PatchBukkitEntityScheduler(Entity entity) {
        this.entity = entity;
    }

    @Override
    public boolean execute(@NotNull Plugin plugin, @NotNull Runnable run, @Nullable Runnable retired, long delay) {
        if (retired != null && entity.isDead()) {
            retired.run();
            return false;
        }
        if (delay <= 0) {
            Bukkit.getScheduler().runTask(plugin, run);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, run, Math.max(1L, delay));
        }
        return true;
    }

    @Override
    public @Nullable ScheduledTask run(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, @Nullable Runnable retired) {
        PatchBukkitScheduledTask scheduledTask = new PatchBukkitScheduledTask(plugin, false);
        var bukkitTask = Bukkit.getScheduler().runTask(plugin, () -> {
            scheduledTask.setState(ScheduledTask.ExecutionState.RUNNING);
            task.accept(scheduledTask);
            scheduledTask.setState(ScheduledTask.ExecutionState.FINISHED);
        });
        scheduledTask.setBukkitTask(bukkitTask);
        return scheduledTask;
    }

    @Override
    public @Nullable ScheduledTask runDelayed(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, @Nullable Runnable retired, long delayTicks) {
        PatchBukkitScheduledTask scheduledTask = new PatchBukkitScheduledTask(plugin, false);
        var bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            scheduledTask.setState(ScheduledTask.ExecutionState.RUNNING);
            task.accept(scheduledTask);
            scheduledTask.setState(ScheduledTask.ExecutionState.FINISHED);
        }, Math.max(1L, delayTicks));
        scheduledTask.setBukkitTask(bukkitTask);
        return scheduledTask;
    }

    @Override
    public @Nullable ScheduledTask runAtFixedRate(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, @Nullable Runnable retired, long initialDelayTicks, long periodTicks) {
        PatchBukkitScheduledTask scheduledTask = new PatchBukkitScheduledTask(plugin, true);
        var bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            scheduledTask.setState(ScheduledTask.ExecutionState.RUNNING);
            task.accept(scheduledTask);
            scheduledTask.setState(ScheduledTask.ExecutionState.IDLE);
        }, Math.max(1L, initialDelayTicks), Math.max(1L, periodTicks));
        scheduledTask.setBukkitTask(bukkitTask);
        return scheduledTask;
    }

}
