package org.patchbukkit.scoreboard;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class PatchBukkitScore implements Score {
    private final PatchBukkitScoreboard scoreboard;
    private final Objective objective;
    private final String entry;
    private int score = 0;
    private boolean scoreSet = false;
    private boolean triggerable = false;
    private Component customName;
    private NumberFormat numberFormat;

    public PatchBukkitScore(PatchBukkitScoreboard scoreboard, Objective objective, String entry) {
        this.scoreboard = scoreboard;
        this.objective = objective;
        this.entry = entry;
    }

    @Override
    public @NotNull OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(this.entry);
    }

    @Override
    public @NotNull String getEntry() {
        return this.entry;
    }

    @Override
    public @NotNull Objective getObjective() {
        return this.objective;
    }

    @Override
    public int getScore() {
        return this.score;
    }

    @Override
    public void setScore(int score) {
        this.score = score;
        this.scoreSet = true;
    }

    @Override
    public boolean isScoreSet() {
        return this.scoreSet;
    }

    @Override
    public @NotNull Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    @Override
    public void resetScore() {
        this.scoreSet = false;
        this.score = 0;
    }

    @Override
    public boolean isTriggerable() {
        return this.triggerable;
    }

    @Override
    public void setTriggerable(boolean triggerable) {
        this.triggerable = triggerable;
    }

    @Override
    public @Nullable Component customName() {
        return this.customName;
    }

    @Override
    public void customName(@Nullable Component customName) {
        this.customName = customName;
    }

    @Override
    public @Nullable NumberFormat numberFormat() {
        return this.numberFormat;
    }

    @Override
    public void numberFormat(@Nullable NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
    }
}
