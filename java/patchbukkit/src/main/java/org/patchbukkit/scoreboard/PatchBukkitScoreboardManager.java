package org.patchbukkit.scoreboard;

import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.jetbrains.annotations.NotNull;

public class PatchBukkitScoreboardManager implements ScoreboardManager {
    private final Scoreboard mainScoreboard = new PatchBukkitScoreboard();

    @Override
    public @NotNull Scoreboard getMainScoreboard() {
        return this.mainScoreboard;
    }

    @Override
    public @NotNull Scoreboard getNewScoreboard() {
        return new PatchBukkitScoreboard();
    }
}
