package org.patchbukkit.scheduler;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PatchBukkitAsyncScheduler implements AsyncScheduler {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

    @Override
    public @NotNull ScheduledTask runNow(
        @NotNull Plugin plugin,
        @NotNull Consumer<ScheduledTask> task
    ) {
        PatchBukkitScheduledTask scheduledTask = new PatchBukkitScheduledTask(plugin, false);
        var future = executor.submit(() -> {
            scheduledTask.setState(ScheduledTask.ExecutionState.RUNNING);
            try {
                task.accept(scheduledTask);
            } finally {
                scheduledTask.setState(ScheduledTask.ExecutionState.FINISHED);
            }
        });
        scheduledTask.setFuture(future);
        return scheduledTask;
    }

    @Override
    public @NotNull ScheduledTask runDelayed(
        @NotNull Plugin plugin,
        @NotNull Consumer<ScheduledTask> task,
        long delay,
        @NotNull TimeUnit unit
    ) {
        PatchBukkitScheduledTask scheduledTask = new PatchBukkitScheduledTask(plugin, false);
        var future = executor.schedule(() -> {
            scheduledTask.setState(ScheduledTask.ExecutionState.RUNNING);
            try {
                task.accept(scheduledTask);
            } finally {
                scheduledTask.setState(ScheduledTask.ExecutionState.FINISHED);
            }
        }, delay, unit);
        scheduledTask.setFuture(future);
        return scheduledTask;
    }

    @Override
    public @NotNull ScheduledTask runAtFixedRate(
        @NotNull Plugin plugin,
        @NotNull Consumer<ScheduledTask> task,
        long initialDelay,
        long period,
        @NotNull TimeUnit unit
    ) {
        PatchBukkitScheduledTask scheduledTask = new PatchBukkitScheduledTask(plugin, true);
        var future = executor.scheduleAtFixedRate(() -> {
            scheduledTask.setState(ScheduledTask.ExecutionState.RUNNING);
            try {
                task.accept(scheduledTask);
            } finally {
                scheduledTask.setState(ScheduledTask.ExecutionState.IDLE);
            }
        }, initialDelay, period, unit);
        scheduledTask.setFuture(future);
        return scheduledTask;
    }

    @Override
    public void cancelTasks(@NotNull Plugin plugin) {
    }
}
