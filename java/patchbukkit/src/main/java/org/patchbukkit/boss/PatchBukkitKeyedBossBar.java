package org.patchbukkit.boss;

import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatchBukkitKeyedBossBar extends PatchBukkitBossBar implements KeyedBossBar {
    private final NamespacedKey key;

    public PatchBukkitKeyedBossBar(
        @NotNull NamespacedKey key,
        @Nullable String title,
        @NotNull BarColor color,
        @NotNull BarStyle style,
        @NotNull BarFlag... flags
    ) {
        super(title, color, style, flags);
        this.key = key;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return this.key;
    }
}
