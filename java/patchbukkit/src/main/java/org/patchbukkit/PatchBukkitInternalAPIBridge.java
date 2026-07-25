package org.patchbukkit;

import com.destroystokyo.paper.SkinParts;
import com.destroystokyo.paper.util.VersionFetcher;
import io.papermc.paper.InternalAPIBridge;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.entity.poi.PoiType;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.world.damagesource.CombatEntry;
import io.papermc.paper.world.damagesource.FallLocationType;
import java.io.IOException;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.attribute.Attributable;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.damage.DamageEffect;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

public class PatchBukkitInternalAPIBridge implements InternalAPIBridge {
    @Override
    public ComponentFlattener componentFlattener() {
        return ComponentFlattener.basic();
    }

    @Override
    public Component resolveWithContext(
        Component component,
        @Nullable CommandSender context,
        @Nullable Entity entity,
        boolean hasPermission
    ) throws IOException {
        return component;
    }

    @Override
    public ItemStack createEmptyStack() {
        return new ItemStack(Material.AIR);
    }

    @Override
    public LifecycleEventManager<Plugin> createPluginLifecycleEventManager(
        JavaPlugin plugin,
        BooleanSupplier registrationCheck
    ) {
        return null;
    }

    @Override
    public String getStatisticCriteriaKey(Statistic statistic) {
        return statistic.name().toLowerCase();
    }

    @Override
    public @Nullable Attributable getDefaultEntityAttributes(NamespacedKey key) {
        return null;
    }

    @Override
    public boolean hasDefaultEntityAttributes(NamespacedKey key) {
        return false;
    }

    @Override
    public ItemStack deserializeItem(byte[] bytes) {
        return new ItemStack(Material.AIR);
    }

    @Override
    public String getTranslationKey(EntityType entityType) {
        return entityType.translationKey();
    }

    @Override
    public @Nullable DamageEffect getDamageEffect(String key) {
        return null;
    }

    @Override
    public DamageSource.Builder createDamageSourceBuilder(DamageType damageType) {
        return null;
    }

    @Override
    public PoiType.Occupancy createOccupancy(String type) {
        return null;
    }

    @Override
    public Set<Pose> validMannequinPoses() {
        return Set.of();
    }

    @Override
    public <MODERN, LEGACY> GameRule<LEGACY> legacyGameRuleBridge(
        GameRule<MODERN> modernRule,
        Function<LEGACY, MODERN> toModern,
        Function<MODERN, LEGACY> toLegacy,
        Class<LEGACY> legacyType
    ) {
        return null;
    }

    @Override
    public Component defaultMannequinDescription() {
        return Component.empty();
    }

    @Override
    public SkinParts.Mutable allSkinParts() {
        return null;
    }

    @Override
    public ResolvableProfile defaultMannequinProfile() {
        return null;
    }

    @Override
    public Predicate<CommandSourceStack> restricted(Predicate<CommandSourceStack> predicate) {
        return predicate;
    }

    @Override
    public CombatEntry createCombatEntry(
        DamageSource damageSource,
        float damage,
        FallLocationType fallLocationType,
        float fallDistance
    ) {
        return null;
    }

    @Override
    public CombatEntry createCombatEntry(
        LivingEntity entity,
        DamageSource damageSource,
        float damage
    ) {
        return null;
    }

    @Override
    public Biome constructLegacyCustomBiome() {
        return null;
    }
}
