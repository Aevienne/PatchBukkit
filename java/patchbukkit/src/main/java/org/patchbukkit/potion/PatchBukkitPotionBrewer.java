package org.patchbukkit.potion;

import io.papermc.paper.potion.PotionMix;
import java.util.Collection;
import java.util.Collections;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

public class PatchBukkitPotionBrewer implements PotionBrewer {
    public static final PatchBukkitPotionBrewer INSTANCE = new PatchBukkitPotionBrewer();

    @Override
    public void addPotionMix(@NotNull PotionMix potionMix) {}

    @Override
    public void removePotionMix(@NotNull NamespacedKey key) {}

    @Override
    public void resetPotionMixes() {}

    @Override
    public @NotNull Collection<PotionEffect> getEffects(@NotNull PotionType type, boolean upgraded, boolean extended) {
        return Collections.emptyList();
    }
}
