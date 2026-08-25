package org.patchbukkit.scoreboard;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class PatchBukkitTeam implements Team {
    private final PatchBukkitScoreboard scoreboard;
    private final String name;
    private Component displayName;
    private Component prefix = Component.empty();
    private Component suffix = Component.empty();
    private TextColor color = NamedTextColor.WHITE;
    private boolean friendlyFire = true;
    private boolean seeFriendlyInvisibles = false;
    private NameTagVisibility nameTagVisibility = NameTagVisibility.ALWAYS;
    private final Set<String> entries = new HashSet<>();
    private final Map<Option, OptionStatus> options = new EnumMap<>(Option.class);

    public PatchBukkitTeam(PatchBukkitScoreboard scoreboard, String name) {
        this.scoreboard = scoreboard;
        this.name = name;
        this.displayName = Component.text(name);
    }

    @Override
    public @NotNull Iterable<? extends Audience> audiences() {
        return Collections.emptyList();
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
    public @NotNull Component prefix() {
        return this.prefix;
    }

    @Override
    public void prefix(@Nullable Component prefix) {
        this.prefix = prefix != null ? prefix : Component.empty();
    }

    @Override
    public @NotNull Component suffix() {
        return this.suffix;
    }

    @Override
    public void suffix(@Nullable Component suffix) {
        this.suffix = suffix != null ? suffix : Component.empty();
    }

    @Override
    public boolean hasColor() {
        return this.color != null;
    }

    @Override
    public @NotNull TextColor color() {
        return this.color;
    }

    @Override
    public void color(@Nullable NamedTextColor color) {
        this.color = color != null ? color : NamedTextColor.WHITE;
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
    public @NotNull String getPrefix() {
        return LegacyComponentSerializer.legacySection().serialize(this.prefix);
    }

    @Override
    public void setPrefix(@NotNull String prefix) {
        this.prefix = LegacyComponentSerializer.legacySection().deserialize(prefix);
    }

    @Override
    public @NotNull String getSuffix() {
        return LegacyComponentSerializer.legacySection().serialize(this.suffix);
    }

    @Override
    public void setSuffix(@NotNull String suffix) {
        this.suffix = LegacyComponentSerializer.legacySection().deserialize(suffix);
    }

    @Override
    public @NotNull ChatColor getColor() {
        return ChatColor.WHITE;
    }

    @Override
    public void setColor(@NotNull ChatColor color) {
    }

    @Override
    public boolean allowFriendlyFire() {
        return this.friendlyFire;
    }

    @Override
    public void setAllowFriendlyFire(boolean enabled) {
        this.friendlyFire = enabled;
    }

    @Override
    public boolean canSeeFriendlyInvisibles() {
        return this.seeFriendlyInvisibles;
    }

    @Override
    public void setCanSeeFriendlyInvisibles(boolean enabled) {
        this.seeFriendlyInvisibles = enabled;
    }

    @Override
    public @NotNull NameTagVisibility getNameTagVisibility() {
        return this.nameTagVisibility;
    }

    @Override
    public void setNameTagVisibility(@NotNull NameTagVisibility visibility) {
        this.nameTagVisibility = visibility;
    }

    @Override
    public @NotNull Set<OfflinePlayer> getPlayers() {
        Set<OfflinePlayer> players = new HashSet<>();
        for (String entry : this.entries) {
            players.add(Bukkit.getOfflinePlayer(entry));
        }
        return players;
    }

    @Override
    public @NotNull Set<String> getEntries() {
        return Collections.unmodifiableSet(new HashSet<>(this.entries));
    }

    @Override
    public int getSize() {
        return this.entries.size();
    }

    @Override
    public @NotNull Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    @Override
    public void addPlayer(@NotNull OfflinePlayer player) {
        addEntry(player.getName() != null ? player.getName() : player.getUniqueId().toString());
    }

    @Override
    public void addEntry(@NotNull String entry) {
        this.entries.add(entry);
    }

    @Override
    public void addEntities(@NotNull Collection<Entity> entities) {
        for (Entity e : entities) {
            addEntity(e);
        }
    }

    @Override
    public void addEntries(@NotNull Collection<String> entries) {
        this.entries.addAll(entries);
    }

    @Override
    public boolean removePlayer(@NotNull OfflinePlayer player) {
        return removeEntry(player.getName() != null ? player.getName() : player.getUniqueId().toString());
    }

    @Override
    public boolean removeEntry(@NotNull String entry) {
        return this.entries.remove(entry);
    }

    @Override
    public boolean removeEntities(@NotNull Collection<Entity> entities) {
        boolean changed = false;
        for (Entity e : entities) {
            if (removeEntity(e)) changed = true;
        }
        return changed;
    }

    @Override
    public boolean removeEntries(@NotNull Collection<String> entries) {
        return this.entries.removeAll(entries);
    }

    @Override
    public void unregister() {
        this.scoreboard.unregisterTeam(this);
    }

    @Override
    public boolean hasPlayer(@NotNull OfflinePlayer player) {
        return hasEntry(player.getName() != null ? player.getName() : player.getUniqueId().toString());
    }

    @Override
    public boolean hasEntry(@NotNull String entry) {
        return this.entries.contains(entry);
    }

    @Override
    public @NotNull OptionStatus getOption(@NotNull Option option) {
        return this.options.getOrDefault(option, OptionStatus.ALWAYS);
    }

    @Override
    public void setOption(@NotNull Option option, @NotNull OptionStatus status) {
        this.options.put(option, status);
    }

    @Override
    public void addEntity(@NotNull Entity entity) {
        addEntry(entity.getUniqueId().toString());
    }

    @Override
    public boolean removeEntity(@NotNull Entity entity) {
        return removeEntry(entity.getUniqueId().toString());
    }

    @Override
    public boolean hasEntity(@NotNull Entity entity) {
        return hasEntry(entity.getUniqueId().toString());
    }
}
