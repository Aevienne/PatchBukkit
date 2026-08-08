package org.patchbukkit.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.checkerframework.checker.index.qual.NonNegative;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.patchbukkit.bridge.BridgeUtils;
import org.patchbukkit.PatchBukkitServer;

import patchbukkit.bridge.NativeBridgeFfi;
import patchbukkit.entity.DamageEntityRequest;
import patchbukkit.entity.SetEntityHealthRequest;

import com.destroystokyo.paper.block.TargetBlockInfo;
import com.destroystokyo.paper.block.TargetBlockInfo.FluidMode;
import com.destroystokyo.paper.entity.TargetEntityInfo;

import io.papermc.paper.world.damagesource.CombatTracker;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.TriState;

@SuppressWarnings({ "deprecation", "removal" })
public class PatchBukkitLivingEntity
    extends PatchBukkitEntity
    implements LivingEntity {

    public PatchBukkitLivingEntity(UUID uuid,
        String name) {
        super(uuid, name);
    }
    @Override
    public double getEyeHeight() {
        return 1.62;
    }

    @Override
    public double getEyeHeight(boolean ignorePose) {
        return 1.62;
    }

    @Override
    public @NotNull Location getEyeLocation() {
        return getLocation().add(0, getEyeHeight(), 0);
    }

    @Override
    public @Nullable RayTraceResult rayTraceBlocks(double maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) {
        return getLocation().getWorld().rayTraceBlocks(getEyeLocation(), getEyeLocation().getDirection(), maxDistance, fluidCollisionMode);
    }

    @Override
    public @Nullable Block getTargetBlockExact(int maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) {
        RayTraceResult result = rayTraceBlocks(maxDistance, fluidCollisionMode);
        return result != null ? result.getHitBlock() : null;
    }

    @Override
    public @NotNull Block getTargetBlock(@Nullable Set<Material> transparent, int maxDistance) {
        Block hit = getTargetBlockExact(maxDistance, FluidCollisionMode.NEVER);
        return hit != null ? hit : getLocation().getBlock();
    }

    @Override
    public @NotNull List<Block> getLineOfSight(@Nullable Set<Material> transparent, int maxDistance) {
        List<Block> blocks = new ArrayList<>();
        Block target = getTargetBlock(transparent, maxDistance);
        if (target != null) {
            blocks.add(target);
        }
        return blocks;
    }

    @Override
    public @Nullable Block getTargetBlock(int maxDistance, @NotNull FluidMode fluidMode) {
        return getTargetBlock(null, maxDistance);
    }

    @Override
    public @NotNull List<Block> getLastTwoTargetBlocks(@Nullable Set<Material> transparent, int maxDistance) {
        Block target = getTargetBlock(transparent, maxDistance);
        return List.of(target, target);
    }

    @Override
    public @Nullable Entity getTargetEntity(int maxDistance, boolean ignoreBlocks) {
        RayTraceResult result = rayTraceEntities(maxDistance, ignoreBlocks);
        return result != null ? result.getHitEntity() : null;
    }

    @Override
    public @Nullable TargetEntityInfo getTargetEntityInfo(int maxDistance, boolean ignoreBlocks) {
        Entity entity = getTargetEntity(maxDistance, ignoreBlocks);
        return entity != null ? new TargetEntityInfo(entity, entity.getLocation().toVector().subtract(getEyeLocation().toVector())) : null;
    }

    @Override
    public @Nullable TargetBlockInfo getTargetBlockInfo(int maxDistance, @NotNull FluidMode fluidMode) {
        Block block = getTargetBlockExact(maxDistance, FluidCollisionMode.NEVER);
        return block != null ? new TargetBlockInfo(block, BlockFace.UP) : null;
    }

    @Override
    public @Nullable BlockFace getTargetBlockFace(int maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) {
        RayTraceResult result = rayTraceBlocks(maxDistance, fluidCollisionMode);
        return result != null ? result.getHitBlockFace() : null;
    }

    @Override
    public @Nullable BlockFace getTargetBlockFace(int maxDistance, @NotNull FluidMode fluidMode) {
        return getTargetBlockFace(maxDistance, FluidCollisionMode.NEVER);
    }

    @Override
    public @Nullable RayTraceResult rayTraceEntities(int maxDistance, boolean ignoreBlocks) {
        return getLocation().getWorld().rayTraceEntities(getEyeLocation(), getEyeLocation().getDirection(), (double) maxDistance, (double) maxDistance);
    }
    private TriState frictionState = TriState.NOT_SET;

    @Override
    public TriState getFrictionState() {
        return this.frictionState;
    }

    @Override
    public void setFrictionState(TriState state) {
        this.frictionState = state != null ? state : TriState.NOT_SET;
    }

    private final Map<Attribute, AttributeInstance> attributes = new HashMap<>();

    @Override
    public @Nullable AttributeInstance getAttribute(@NotNull Attribute attribute) {
        return attributes.get(attribute);
    }

    @Override
    public void registerAttribute(@NotNull Attribute attribute) {
    }

    @Override
    public <T extends Projectile> @NotNull T launchProjectile(@NotNull Class<? extends T> projectileClass) {
        return launchProjectile(projectileClass, null);
    }

    @Override
    public <T extends Projectile> @NotNull T launchProjectile(@NotNull Class<? extends T> projectileClass, @Nullable Vector velocity) {
        return launchProjectile(projectileClass, velocity, null);
    }

    @Override
    public <T extends Projectile> @NotNull T launchProjectile(@NotNull Class<? extends T> projectileClass, @Nullable Vector velocity, @Nullable Consumer<? super T> function) {
        Location spawnLoc = getEyeLocation();
        Vector dir = velocity != null ? velocity : spawnLoc.getDirection();
        T entity = spawnLoc.getWorld().spawn(spawnLoc, projectileClass, function);
        entity.setVelocity(dir);
        entity.setShooter(this);
        return entity;
    }



    private Player killer;

    @Override
    public @Nullable Player getKiller() {
        return this.killer;
    }

    @Override
    public void setKiller(@Nullable Player killer) {
        this.killer = killer;
    }

    private double health = 20.0;
    private double maxHealth = 20.0;
    private double absorption = 0.0;
    private int arrowsInBody = 0;
    private int arrowCooldown = 0;
    private int beeStingersInBody = 0;
    private int beeStingerCooldown = 0;

    @Override
    public int getArrowsInBody() {
        return this.arrowsInBody;
    }

    @Override
    public void setArrowsInBody(int count) {
        this.arrowsInBody = count;
    }

    @Override
    public void setArrowsInBody(int count, boolean fireEvent) {
        this.arrowsInBody = count;
    }

    @Override
    public int getArrowCooldown() {
        return this.arrowCooldown;
    }

    @Override
    public void setArrowCooldown(int ticks) {
        this.arrowCooldown = ticks;
    }

    @Override
    public int getBeeStingersInBody() {
        return this.beeStingersInBody;
    }

    @Override
    public void setBeeStingersInBody(int count) {
        this.beeStingersInBody = count;
    }

    @Override
    public int getBeeStingerCooldown() {
        return this.beeStingerCooldown;
    }

    @Override
    public void setBeeStingerCooldown(int ticks) {
        this.beeStingerCooldown = ticks;
    }

    private int noActionTicks = 0;

    @Override
    public int getNoActionTicks() {
        return this.noActionTicks;
    }

    @Override
    public void setNoActionTicks(int ticks) {
        this.noActionTicks = ticks;
    }

    private int itemInUseTicks = 0;
    private ItemStack itemInUse = null;

    @Override
    public @Nullable ItemStack getItemInUse() {
        return this.itemInUse;
    }

    @Override
    public int getItemInUseTicks() {
        return this.itemInUseTicks;
    }

    @Override
    public void setItemInUseTicks(int ticks) {
        this.itemInUseTicks = ticks;
    }

    private int remainingAir = 300;
    private int maximumAir = 300;
    private int noDamageTicks = 0;
    private int maximumNoDamageTicks = 20;
    private double lastDamage = 0.0;
    private final Map<PotionEffectType, PotionEffect> potionEffects = new HashMap<>();

    @Override
    public double getHealth() {
        try {
            var response = NativeBridgeFfi.getEntityHealth(BridgeUtils.convertUuid(this.getUniqueId()));
            if (response != null) {
                return response.getHealth();
            }
        } catch (Throwable ignored) {}
        return this.health;
    }

    @Override
    public void setHealth(double health) {
        this.health = Math.max(0.0, Math.min(health, getMaxHealth()));
        var request = SetEntityHealthRequest.newBuilder()
            .setUuid(BridgeUtils.convertUuid(this.getUniqueId()))
            .setHealth(this.health)
            .build();
        NativeBridgeFfi.setEntityHealth(request);
    }

    @Override
    public void damage(double amount) {
        var request = DamageEntityRequest.newBuilder()
            .setUuid(BridgeUtils.convertUuid(this.getUniqueId()))
            .setAmount(amount)
            .build();
        NativeBridgeFfi.damageEntity(request);
    }

    @Override
    public void damage(double amount, @Nullable Entity source) {
        setHealth(getHealth() - amount);
    }

    @Override
    public void damage(double amount, @NotNull DamageSource damageSource) {
        setHealth(getHealth() - amount);
    }

    @Override
    public void heal(double amount, @NotNull RegainReason reason) {
        setHealth(getHealth() + amount);
    }

    @Override
    public double getMaxHealth() {
        return this.maxHealth;
    }

    @Override
    public void setMaxHealth(double health) {
        this.maxHealth = Math.max(0.1, health);
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
    }

    @Override
    public void resetMaxHealth() {
        setMaxHealth(20.0);
    }

    @Override
    public double getAbsorptionAmount() {
        return this.absorption;
    }

    @Override
    public void setAbsorptionAmount(double amount) {
        this.absorption = Math.max(0.0, amount);
    }

    @Override
    public int getRemainingAir() {
        return this.remainingAir;
    }

    @Override
    public void setRemainingAir(int ticks) {
        this.remainingAir = ticks;
    }

    @Override
    public int getMaximumAir() {
        return this.maximumAir;
    }

    @Override
    public void setMaximumAir(int ticks) {
        this.maximumAir = ticks;
    }

    @Override
    public int getNoDamageTicks() {
        return this.noDamageTicks;
    }

    @Override
    public void setNoDamageTicks(int ticks) {
        this.noDamageTicks = ticks;
    }

    @Override
    public int getMaximumNoDamageTicks() {
        return this.maximumNoDamageTicks;
    }

    @Override
    public void setMaximumNoDamageTicks(int ticks) {
        this.maximumNoDamageTicks = ticks;
    }

    @Override
    public double getLastDamage() {
        return this.lastDamage;
    }

    @Override
    public void setLastDamage(double damage) {
        this.lastDamage = damage;
    }

    @Override
    public boolean addPotionEffect(@NotNull PotionEffect effect) {
        return addPotionEffect(effect, false);
    }

    @Override
    public boolean addPotionEffect(@NotNull PotionEffect effect, boolean force) {
        PotionEffectType type = effect.getType();
        PotionEffect existing = potionEffects.get(type);
        if (existing == null || force || effect.getAmplifier() > existing.getAmplifier()
                || (effect.getAmplifier() == existing.getAmplifier() && effect.getDuration() > existing.getDuration())) {
            potionEffects.put(type, effect);
            return true;
        }
        return false;
    }

    @Override
    public boolean addPotionEffects(@NotNull Collection<PotionEffect> effects) {
        boolean success = false;
        for (PotionEffect effect : effects) {
            if (addPotionEffect(effect)) {
                success = true;
            }
        }
        return success;
    }

    @Override
    public boolean hasPotionEffect(@NotNull PotionEffectType type) {
        return potionEffects.containsKey(type);
    }

    @Override
    public @Nullable PotionEffect getPotionEffect(@NotNull PotionEffectType type) {
        return potionEffects.get(type);
    }

    @Override
    public void removePotionEffect(@NotNull PotionEffectType type) {
        potionEffects.remove(type);
    }

    @Override
    public @NotNull Collection<PotionEffect> getActivePotionEffects() {
        return Collections.unmodifiableCollection(potionEffects.values());
    }

    @Override
    public boolean clearActivePotionEffects() {
        boolean hadEffects = !potionEffects.isEmpty();
        potionEffects.clear();
        return hadEffects;
    }

    @Override
    public boolean hasLineOfSight(@NotNull Entity other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasLineOfSight'");
    }

    @Override
    public boolean hasLineOfSight(@NotNull Location location) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasLineOfSight'");
    }

    @Override
    public boolean getRemoveWhenFarAway() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRemoveWhenFarAway'");
    }

    @Override
    public void setRemoveWhenFarAway(boolean remove) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setRemoveWhenFarAway'");
    }

    @Override
    public @Nullable EntityEquipment getEquipment() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEquipment'");
    }

    @Override
    public void setCanPickupItems(boolean pickup) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCanPickupItems'");
    }

    @Override
    public boolean getCanPickupItems() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCanPickupItems'");
    }

    @Override
    public boolean isLeashed() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isLeashed'");
    }

    @Override
    public @NotNull Entity getLeashHolder() throws IllegalStateException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLeashHolder'");
    }

    @Override
    public boolean setLeashHolder(@Nullable Entity holder) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setLeashHolder'");
    }

    @Override
    public boolean isGliding() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isGliding'");
    }

    @Override
    public void setGliding(boolean gliding) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setGliding'");
    }

    @Override
    public boolean isSwimming() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isSwimming'");
    }

    @Override
    public void setSwimming(boolean swimming) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSwimming'");
    }

    @Override
    public boolean isRiptiding() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isRiptiding'");
    }

    @Override
    public void setRiptiding(boolean riptiding) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setRiptiding'");
    }

    @Override
    public boolean isSleeping() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isSleeping'");
    }

    @Override
    public boolean isClimbing() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isClimbing'");
    }

    @Override
    public void setAI(boolean ai) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setAI'");
    }

    @Override
    public boolean hasAI() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasAI'");
    }

    @Override
    public void attack(@NotNull Entity target) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'attack'");
    }

    @Override
    public void swingMainHand() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'swingMainHand'");
    }

    @Override
    public void swingOffHand() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'swingOffHand'");
    }

    @Override
    public void playHurtAnimation(float yaw) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'playHurtAnimation'");
    }

    @Override
    public void setCollidable(boolean collidable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCollidable'");
    }

    @Override
    public boolean isCollidable() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isCollidable'");
    }

    @Override
    public @NotNull Set<UUID> getCollidableExemptions() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCollidableExemptions'");
    }

    @Override
    public <T> @Nullable T getMemory(@NotNull MemoryKey<T> memoryKey) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMemory'");
    }

    @Override
    public <T> void setMemory(@NotNull MemoryKey<T> memoryKey, @Nullable T memoryValue) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setMemory'");
    }

    @Override
    public @Nullable Sound getHurtSound() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHurtSound'");
    }

    @Override
    public @Nullable Sound getDeathSound() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDeathSound'");
    }

    @Override
    public @NotNull Sound getFallDamageSound(int fallHeight) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFallDamageSound'");
    }

    @Override
    public @NotNull Sound getFallDamageSoundSmall() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFallDamageSoundSmall'");
    }

    @Override
    public @NotNull Sound getFallDamageSoundBig() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFallDamageSoundBig'");
    }

    @Override
    public @NotNull Sound getDrinkingSound(@NotNull ItemStack itemStack) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDrinkingSound'");
    }

    @Override
    public @NotNull Sound getEatingSound(@NotNull ItemStack itemStack) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEatingSound'");
    }

    @Override
    public boolean canBreatheUnderwater() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'canBreatheUnderwater'");
    }

    @Override
    public @NotNull EntityCategory getCategory() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCategory'");
    }

    @Override
    public float getSidewaysMovement() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSidewaysMovement'");
    }

    @Override
    public float getUpwardsMovement() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUpwardsMovement'");
    }

    @Override
    public float getForwardsMovement() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getForwardsMovement'");
    }

    @Override
    public void startUsingItem(@NotNull EquipmentSlot hand) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'startUsingItem'");
    }

    @Override
    public void completeUsingActiveItem() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'completeUsingActiveItem'");
    }

    @Override
    public @NotNull ItemStack getActiveItem() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getActiveItem'");
    }

    @Override
    public void clearActiveItem() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clearActiveItem'");
    }

    @Override
    public int getActiveItemRemainingTime() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getActiveItemRemainingTime'");
    }

    @Override
    public void setActiveItemRemainingTime(@Range(from = 0, to = 2147483647) int ticks) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setActiveItemRemainingTime'");
    }

    @Override
    public boolean hasActiveItem() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasActiveItem'");
    }

    @Override
    public int getActiveItemUsedTime() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getActiveItemUsedTime'");
    }

    @Override
    public @NotNull EquipmentSlot getActiveItemHand() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getActiveItemHand'");
    }

    @Override
    public boolean isJumping() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isJumping'");
    }

    @Override
    public void setJumping(boolean jumping) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setJumping'");
    }

    @Override
    public void playPickupItemAnimation(@NotNull Item item, int quantity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'playPickupItemAnimation'");
    }

    @Override
    public float getHurtDirection() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHurtDirection'");
    }

    @Override
    public void setHurtDirection(float hurtDirection) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setHurtDirection'");
    }

    @Override
    public void knockback(double strength, double directionX, double directionZ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'knockback'");
    }

    @Override
    public void broadcastSlotBreak(@NotNull EquipmentSlot slot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'broadcastSlotBreak'");
    }

    @Override
    public void broadcastSlotBreak(@NotNull EquipmentSlot slot, @NotNull Collection<Player> players) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'broadcastSlotBreak'");
    }

    @Override
    public @NotNull ItemStack damageItemStack(@NotNull ItemStack stack, int amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'damageItemStack'");
    }

    @Override
    public void damageItemStack(@NotNull EquipmentSlot slot, int amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'damageItemStack'");
    }

    @Override
    public float getBodyYaw() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBodyYaw'");
    }

    @Override
    public void setBodyYaw(float bodyYaw) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setBodyYaw'");
    }

    @Override
    public boolean canUseEquipmentSlot(@NotNull EquipmentSlot slot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'canUseEquipmentSlot'");
    }

    public @NotNull CombatTracker getCombatTracker() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCombatTracker'");
    }

    @Override
    public void setShieldBlockingDelay(int delay) {
    }

    @Override
    public int getShieldBlockingDelay() {
        return 0;
    }

    public void setWaypointStyle(@Nullable Key key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setWaypointStyle'");
    }

    public void setWaypointColor(@Nullable Color color) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setWaypointColor'");
    }

    public @NotNull Key getWaypointStyle() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWaypointStyle'");
    }

    public @Nullable Color getWaypointColor() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWaypointColor'");
    }

    public void kill(DamageSource damageSource) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'kill'");
    }

    public float getSoundVolume() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSoundVolume'");
    }

    public float getSoundPitch() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSoundPitch'");
    }

    public @Nullable Sound getHurtSound(@NotNull DamageSource damageSource) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHurtSound'");
    }}
