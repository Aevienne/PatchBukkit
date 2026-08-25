package org.patchbukkit.entity;

import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.patchbukkit.PatchBukkitServer;

public class PatchBukkitOfflinePlayer implements OfflinePlayer {
    private final UUID uuid;
    private final String name;

    public PatchBukkitOfflinePlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public PatchBukkitOfflinePlayer(UUID uuid) {
        this(uuid, null);
    }

    public PatchBukkitOfflinePlayer(String name) {
        this(null, name);
    }

    @Override
    public boolean isOnline() {
        return getPlayer() != null;
    }

    @Override
    public boolean isConnected() {
        return isOnline();
    }

    @Override
    public @Nullable String getName() {
        Player player = getPlayer();
        return player != null ? player.getName() : this.name;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        if (this.uuid != null) return this.uuid;
        Player player = getPlayer();
        if (player != null) return player.getUniqueId();
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + (this.name != null ? this.name : "")).getBytes());
    }

    @Override
    public boolean isOp() {
        Player player = getPlayer();
        if (player != null) return player.isOp();
        return PatchBukkitServer.getInstance().isOp(getUniqueId(), getName());
    }

    @Override
    public void setOp(boolean value) {
        PatchBukkitServer.getInstance().setOperator(getUniqueId(), getName(), value);
        Player player = getPlayer();
        if (player != null) {
            player.setOp(value);
        }
    }

    @Override
    public boolean isBanned() {
        return false;
    }

    @Override
    public <E extends org.bukkit.BanEntry<? super com.destroystokyo.paper.profile.PlayerProfile>> E ban(String reason, java.util.Date expires, String source) {
        return null;
    }

    @Override
    public <E extends org.bukkit.BanEntry<? super com.destroystokyo.paper.profile.PlayerProfile>> E ban(String reason, java.time.Instant expires, String source) {
        return null;
    }

    @Override
    public <E extends org.bukkit.BanEntry<? super com.destroystokyo.paper.profile.PlayerProfile>> E ban(String reason, java.time.Duration duration, String source) {
        return null;
    }

    @Override
    public boolean isWhitelisted() {
        return false;
    }

    @Override
    public void setWhitelisted(boolean value) {}

    @Override
    public @Nullable Player getPlayer() {
        if (this.uuid != null) {
            Player p = PatchBukkitServer.getInstance().getPlayer(this.uuid);
            if (p != null) return p;
        }
        if (this.name != null) {
            return PatchBukkitServer.getInstance().getPlayer(this.name);
        }
        return null;
    }

    @Override
    public long getFirstPlayed() { return 0; }

    @Override
    public long getLastPlayed() { return 0; }

    @Override
    public boolean hasPlayedBefore() { return true; }

    @Override
    public @Nullable Location getLocation() {
        Player player = getPlayer();
        return player != null ? player.getLocation() : null;
    }

    @Override
    public @Nullable Location getBedSpawnLocation() { return null; }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic) {}

    @Override
    public void decrementStatistic(@NotNull Statistic statistic) {}

    @Override
    public int getStatistic(@NotNull Statistic statistic) { return 0; }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, int amount) {}

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, int amount) {}

    @Override
    public void setStatistic(@NotNull Statistic statistic, int newValue) {}

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull org.bukkit.Material material) {}

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull org.bukkit.Material material) {}

    @Override
    public int getStatistic(@NotNull Statistic statistic, @NotNull org.bukkit.Material material) { return 0; }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull org.bukkit.Material material, int amount) {}

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull org.bukkit.Material material, int amount) {}

    @Override
    public void setStatistic(@NotNull Statistic statistic, @NotNull org.bukkit.Material material, int newValue) {}

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) {}

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) {}

    @Override
    public int getStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) { return 0; }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int amount) {}

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int amount) {}

    @Override
    public void setStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int newValue) {}

    @Override
    public @Nullable Location getLastDeathLocation() {
        Player player = getPlayer();
        return player != null ? player.getLastDeathLocation() : null;
    }

    @Override
    public @Nullable Location getRespawnLocation() {
        Player player = getPlayer();
        return player != null ? player.getBedSpawnLocation() : null;
    }

    public @Nullable Location getRespawnLocation(boolean anchor) {
        Player player = getPlayer();
        return player != null ? player.getBedSpawnLocation() : null;
    }

    @Override
    public com.destroystokyo.paper.profile.@NotNull PlayerProfile getPlayerProfile() {
        Player player = getPlayer();
        if (player != null) return player.getPlayerProfile();
        return org.bukkit.Bukkit.getServer().createProfile(getUniqueId(), getName());
    }

    @Override
    public long getLastLogin() { return getLastPlayed(); }

    @Override
    public long getLastSeen() { return getLastPlayed(); }

    @Override
    public @NotNull org.bukkit.persistence.PersistentDataContainer getPersistentDataContainer() {
        return new org.patchbukkit.persistence.PatchBukkitPersistentDataContainer();
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of("uuid", getUniqueId().toString(), "name", getName() != null ? getName() : "");
    }
}
