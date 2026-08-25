package org.patchbukkit.entity;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.destroystokyo.paper.entity.ai.MobGoals;
import java.util.Collection;
import java.util.Collections;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatchBukkitMobGoals implements MobGoals {
    public static final PatchBukkitMobGoals INSTANCE = new PatchBukkitMobGoals();

    @Override
    public <T extends Mob> void addGoal(@NotNull T mob, int priority, @NotNull Goal<T> goal) {}

    @Override
    public <T extends Mob> void removeGoal(@NotNull T mob, @NotNull Goal<T> goal) {}

    @Override
    public <T extends Mob> void removeAllGoals(@NotNull T mob) {}

    @Override
    public <T extends Mob> void removeAllGoals(@NotNull T mob, @NotNull GoalType type) {}

    @Override
    public <T extends Mob> void removeGoal(@NotNull T mob, @NotNull GoalKey<T> key) {}

    @Override
    public <T extends Mob> boolean hasGoal(@NotNull T mob, @NotNull GoalKey<T> key) {
        return false;
    }

    @Override
    public <T extends Mob> @Nullable Goal<T> getGoal(@NotNull T mob, @NotNull GoalKey<T> key) {
        return null;
    }

    @Override
    public <T extends Mob> @NotNull Collection<Goal<T>> getGoals(@NotNull T mob, @NotNull GoalKey<T> key) {
        return Collections.emptyList();
    }

    @Override
    public <T extends Mob> @NotNull Collection<Goal<T>> getAllGoals(@NotNull T mob) {
        return Collections.emptyList();
    }

    @Override
    public <T extends Mob> @NotNull Collection<Goal<T>> getAllGoals(@NotNull T mob, @NotNull GoalType type) {
        return Collections.emptyList();
    }

    @Override
    public <T extends Mob> @NotNull Collection<Goal<T>> getAllGoalsWithout(@NotNull T mob, @NotNull GoalType type) {
        return Collections.emptyList();
    }

    @Override
    public <T extends Mob> @NotNull Collection<Goal<T>> getRunningGoals(@NotNull T mob) {
        return Collections.emptyList();
    }

    @Override
    public <T extends Mob> @NotNull Collection<Goal<T>> getRunningGoals(@NotNull T mob, @NotNull GoalType type) {
        return Collections.emptyList();
    }

    @Override
    public <T extends Mob> @NotNull Collection<Goal<T>> getRunningGoalsWithout(@NotNull T mob, @NotNull GoalType type) {
        return Collections.emptyList();
    }
}
