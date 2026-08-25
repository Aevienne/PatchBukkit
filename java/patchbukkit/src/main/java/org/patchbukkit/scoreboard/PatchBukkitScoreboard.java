package org.patchbukkit.scoreboard;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class PatchBukkitScoreboard implements Scoreboard {
    private final Map<String, Objective> objectives = new ConcurrentHashMap<>();
    private final Map<DisplaySlot, Objective> slots = new ConcurrentHashMap<>();
    private final Map<String, Team> teams = new ConcurrentHashMap<>();
    private final Map<String, Map<Objective, Score>> scores = new ConcurrentHashMap<>();

    @Override
    public @NotNull Objective registerNewObjective(
        @NotNull String name,
        @NotNull String criteria,
        @Nullable Component displayName,
        @NotNull RenderType renderType
    ) throws IllegalArgumentException {
        return registerNewObjective(name, Criteria.create(criteria), displayName, renderType);
    }

    @Override
    public @NotNull Objective registerNewObjective(
        @NotNull String name,
        @NotNull Criteria criteria,
        @Nullable Component displayName,
        @NotNull RenderType renderType
    ) throws IllegalArgumentException {
        Objective obj = new PatchBukkitObjective(this, name, criteria, displayName, renderType);
        this.objectives.put(name, obj);
        return obj;
    }

    @Override
    public @NotNull Objective registerNewObjective(
        @NotNull String name,
        @NotNull String criteria,
        @NotNull String displayName,
        @NotNull RenderType renderType
    ) {
        return registerNewObjective(name, Criteria.create(criteria), LegacyComponentSerializer.legacySection().deserialize(displayName), renderType);
    }

    @Override
    public @Nullable Objective getObjective(@NotNull String name) {
        return this.objectives.get(name);
    }

    @Override
    public @NotNull Set<Objective> getObjectivesByCriteria(@NotNull String criteria) {
        Set<Objective> set = new HashSet<>();
        for (Objective obj : this.objectives.values()) {
            if (obj.getCriteria().equalsIgnoreCase(criteria)) {
                set.add(obj);
            }
        }
        return set;
    }

    @Override
    public @NotNull Set<Objective> getObjectivesByCriteria(@NotNull Criteria criteria) {
        return getObjectivesByCriteria(criteria.getName());
    }

    @Override
    public @NotNull Set<Objective> getObjectives() {
        return Collections.unmodifiableSet(new HashSet<>(this.objectives.values()));
    }

    @Override
    public @Nullable Objective getObjective(@NotNull DisplaySlot slot) {
        return this.slots.get(slot);
    }

    void setObjectiveSlot(@NotNull DisplaySlot slot, @Nullable Objective obj) {
        if (obj == null) this.slots.remove(slot);
        else this.slots.put(slot, obj);
    }

    void unregisterObjective(Objective obj) {
        this.objectives.remove(obj.getName());
        this.slots.values().remove(obj);
        for (Map<Objective, Score> map : scores.values()) {
            map.remove(obj);
        }
    }

    @Override
    public @NotNull Set<Score> getScores(@NotNull OfflinePlayer player) {
        return getScores(player.getName() != null ? player.getName() : player.getUniqueId().toString());
    }

    @Override
    public @NotNull Set<Score> getScores(@NotNull String entry) {
        Map<Objective, Score> map = this.scores.get(entry);
        if (map == null) return Collections.emptySet();
        return Collections.unmodifiableSet(new HashSet<>(map.values()));
    }

    @Override
    public void resetScores(@NotNull OfflinePlayer player) {
        resetScores(player.getName() != null ? player.getName() : player.getUniqueId().toString());
    }

    @Override
    public void resetScores(@NotNull String entry) {
        this.scores.remove(entry);
    }

    @Override
    public @Nullable Team getPlayerTeam(@NotNull OfflinePlayer player) {
        return getEntryTeam(player.getName() != null ? player.getName() : player.getUniqueId().toString());
    }

    @Override
    public @Nullable Team getEntryTeam(@NotNull String entry) {
        for (Team team : this.teams.values()) {
            if (team.hasEntry(entry)) return team;
        }
        return null;
    }

    @Override
    public @Nullable Team getTeam(@NotNull String teamName) {
        return this.teams.get(teamName);
    }

    @Override
    public @NotNull Set<Team> getTeams() {
        return Collections.unmodifiableSet(new HashSet<>(this.teams.values()));
    }

    @Override
    public @NotNull Team registerNewTeam(@NotNull String name) throws IllegalArgumentException {
        if (this.teams.containsKey(name)) {
            throw new IllegalArgumentException("Team '" + name + "' already exists");
        }
        Team team = new PatchBukkitTeam(this, name);
        this.teams.put(name, team);
        return team;
    }

    void unregisterTeam(Team team) {
        this.teams.remove(team.getName());
    }

    @Override
    public @NotNull Set<OfflinePlayer> getPlayers() {
        Set<OfflinePlayer> set = new HashSet<>();
        for (String entry : getEntries()) {
            set.add(org.bukkit.Bukkit.getOfflinePlayer(entry));
        }
        return set;
    }

    @Override
    public @NotNull Set<String> getEntries() {
        return Collections.unmodifiableSet(new HashSet<>(this.scores.keySet()));
    }

    @Override
    public void clearSlot(@NotNull DisplaySlot slot) {
        this.slots.remove(slot);
    }

    @Override
    public @NotNull Set<Score> getScoresFor(@NotNull Entity entity) throws IllegalArgumentException {
        return getScores(entity.getUniqueId().toString());
    }

    @Override
    public void resetScoresFor(@NotNull Entity entity) throws IllegalArgumentException {
        resetScores(entity.getUniqueId().toString());
    }

    @Override
    public @Nullable Team getEntityTeam(@NotNull Entity entity) throws IllegalArgumentException {
        return getEntryTeam(entity.getUniqueId().toString());
    }

    Score getScore(Objective objective, String entry) {
        return scores.computeIfAbsent(entry, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(objective, k -> new PatchBukkitScore(this, objective, entry));
    }
}
