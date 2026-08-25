package org.patchbukkit.scheduler;

import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PatchBukkitRegionScheduler implements RegionScheduler {
    @Override
    public void execute(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ, @NotNull Runnable run) {
        Bukkit.getScheduler().runTask(plugin, run);
    }

    @Override
    public @NotNull ScheduledTask run(
        @NotNull Plugin plugin,
        @NotNull World world,
        int chunkX,
        int chunkZ,
        @NotNull Consumer<ScheduledTask> task
    ) {
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
    public @NotNull ScheduledTask runDelayed(
        @NotNull Plugin plugin,
        @NotNull World world,
        int chunkX,
        int chunkZ,
        @NotNull Consumer<ScheduledTask> task,
        long delayTicks
    ) {
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
    public @NotNull ScheduledTask runAtFixedRate(
        @NotNull Plugin plugin,
        @NotNull World world,
        int chunkX,
        int chunkZ,
        @NotNull Consumer<ScheduledTask> task,
        long initialDelayTicks,
        long periodTicks
    ) {
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
