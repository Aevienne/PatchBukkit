package org.patchbukkit.boss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatchBukkitBossBar implements BossBar {
    private String title;
    private BarColor color;
    private BarStyle style;
    private final Set<BarFlag> flags = EnumSet.noneOf(BarFlag.class);
    private double progress = 1.0;
    private boolean visible = true;
    private final List<Player> players = new CopyOnWriteArrayList<>();

    public PatchBukkitBossBar(
        @Nullable String title,
        @NotNull BarColor color,
        @NotNull BarStyle style,
        @NotNull BarFlag... flags
    ) {
        this.title = title != null ? title : "";
        this.color = color;
        this.style = style;
        if (flags != null) {
            Collections.addAll(this.flags, flags);
        }
    }

    @Override
    public @NotNull String getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(@Nullable String title) {
        this.title = title != null ? title : "";
    }

    @Override
    public @NotNull BarColor getColor() {
        return this.color;
    }

    @Override
    public void setColor(@NotNull BarColor color) {
        this.color = color;
    }

    @Override
    public @NotNull BarStyle getStyle() {
        return this.style;
    }

    @Override
    public void setStyle(@NotNull BarStyle style) {
        this.style = style;
    }

    @Override
    public void removeFlag(@NotNull BarFlag flag) {
        this.flags.remove(flag);
    }

    @Override
    public void addFlag(@NotNull BarFlag flag) {
        this.flags.add(flag);
    }

    @Override
    public boolean hasFlag(@NotNull BarFlag flag) {
        return this.flags.contains(flag);
    }

    @Override
    public void setProgress(double progress) {
        this.progress = Math.max(0.0, Math.min(1.0, progress));
    }

    @Override
    public double getProgress() {
        return this.progress;
    }

    @Override
    public void addPlayer(@NotNull Player player) {
        if (!this.players.contains(player)) {
            this.players.add(player);
        }
    }

    @Override
    public void removePlayer(@NotNull Player player) {
        this.players.remove(player);
    }

    @Override
    public void removeAll() {
        this.players.clear();
    }

    @Override
    public @NotNull List<Player> getPlayers() {
        return Collections.unmodifiableList(new ArrayList<>(this.players));
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void show() {
        setVisible(true);
    }

    @Override
    public void hide() {
        setVisible(false);
    }
}
