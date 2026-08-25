package org.patchbukkit.scoreboard;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class PatchBukkitObjective implements Objective {
    private final PatchBukkitScoreboard scoreboard;
    private final String name;
    private final Criteria criteria;
    private Component displayName;
    private RenderType renderType;
    private DisplaySlot displaySlot;
    private boolean autoUpdateDisplay = true;
    private NumberFormat numberFormat;

    public PatchBukkitObjective(
        PatchBukkitScoreboard scoreboard,
        String name,
        Criteria criteria,
        Component displayName,
        RenderType renderType
    ) {
        this.scoreboard = scoreboard;
        this.name = name;
        this.criteria = criteria;
        this.displayName = displayName != null ? displayName : Component.text(name);
        this.renderType = renderType != null ? renderType : RenderType.INTEGER;
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public @NotNull Component displayName() {
        return this.displayName;
    }

    @Override
    public void displayName(@Nullable Component displayName) {
        this.displayName = displayName != null ? displayName : Component.text(this.name);
    }

    @Override
    public @NotNull String getDisplayName() {
        return LegacyComponentSerializer.legacySection().serialize(this.displayName);
    }

    @Override
    public void setDisplayName(@NotNull String displayName) {
        this.displayName = LegacyComponentSerializer.legacySection().deserialize(displayName);
    }

    @Override
    public @NotNull String getCriteria() {
        return this.criteria.getName();
    }

    @Override
    public @NotNull Criteria getTrackedCriteria() {
        return this.criteria;
    }

    @Override
    public boolean isModifiable() {
        return !this.criteria.isReadOnly();
    }

    @Override
    public @NotNull Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    @Override
    public void unregister() {
        this.scoreboard.unregisterObjective(this);
    }

    @Override
    public void setDisplaySlot(@Nullable DisplaySlot slot) {
        this.displaySlot = slot;
        if (slot != null) {
            this.scoreboard.setObjectiveSlot(slot, this);
        }
    }

    @Override
    public @Nullable DisplaySlot getDisplaySlot() {
        return this.displaySlot;
    }

    @Override
    public void setRenderType(@NotNull RenderType renderType) {
        this.renderType = renderType;
    }

    @Override
    public @NotNull RenderType getRenderType() {
        return this.renderType;
    }

    @Override
    public @NotNull Score getScore(@NotNull OfflinePlayer player) {
        return getScore(player.getName() != null ? player.getName() : player.getUniqueId().toString());
    }

    @Override
    public @NotNull Score getScore(@NotNull String entry) {
        return this.scoreboard.getScore(this, entry);
    }

    @Override
    public @NotNull Score getScoreFor(@NotNull Entity entity) {
        return getScore(entity.getUniqueId().toString());
    }

    @Override
    public boolean willAutoUpdateDisplay() {
        return this.autoUpdateDisplay;
    }

    @Override
    public void setAutoUpdateDisplay(boolean autoUpdateDisplay) {
        this.autoUpdateDisplay = autoUpdateDisplay;
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
