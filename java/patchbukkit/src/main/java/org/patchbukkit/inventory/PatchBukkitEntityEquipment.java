package org.patchbukkit.inventory;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.patchbukkit.entity.PatchBukkitHumanEntity;

public class PatchBukkitEntityEquipment implements EntityEquipment {
    private final PatchBukkitHumanEntity holder;

    public PatchBukkitEntityEquipment(PatchBukkitHumanEntity holder) {
        this.holder = holder;
    }

    @Override
    public ItemStack getItemInMainHand() {
        return holder.getInventory().getItemInMainHand();
    }

    @Override
    public void setItemInMainHand(ItemStack item) {
        holder.getInventory().setItemInMainHand(item);
    }

    @Override
    public void setItemInMainHand(ItemStack item, boolean silent) {
        setItemInMainHand(item);
    }

    @Override
    public ItemStack getItemInOffHand() {
        return holder.getInventory().getItemInOffHand();
    }

    @Override
    public void setItemInOffHand(ItemStack item) {
        holder.getInventory().setItemInOffHand(item);
    }

    @Override
    public void setItemInOffHand(ItemStack item, boolean silent) {
        setItemInOffHand(item);
    }

    @Override
    public ItemStack getItemInHand() {
        return getItemInMainHand();
    }

    @Override
    public void setItemInHand(ItemStack stack) {
        setItemInMainHand(stack);
    }

    @Override
    public ItemStack getHelmet() {
        return holder.getInventory().getHelmet();
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        holder.getInventory().setHelmet(helmet);
    }

    @Override
    public void setHelmet(ItemStack helmet, boolean silent) {
        setHelmet(helmet);
    }

    @Override
    public ItemStack getChestplate() {
        return holder.getInventory().getChestplate();
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        holder.getInventory().setChestplate(chestplate);
    }

    @Override
    public void setChestplate(ItemStack chestplate, boolean silent) {
        setChestplate(chestplate);
    }

    @Override
    public ItemStack getLeggings() {
        return holder.getInventory().getLeggings();
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        holder.getInventory().setLeggings(leggings);
    }

    @Override
    public void setLeggings(ItemStack leggings, boolean silent) {
        setLeggings(leggings);
    }

    @Override
    public ItemStack getBoots() {
        return holder.getInventory().getBoots();
    }

    @Override
    public void setBoots(ItemStack boots) {
        holder.getInventory().setBoots(boots);
    }

    @Override
    public void setBoots(ItemStack boots, boolean silent) {
        setBoots(boots);
    }

    @Override
    public ItemStack[] getArmorContents() {
        return holder.getInventory().getArmorContents();
    }

    @Override
    public void setArmorContents(ItemStack[] items) {
        holder.getInventory().setArmorContents(items);
    }

    @Override
    public void clear() {
        holder.getInventory().clear();
    }

    @Override
    public Entity getHolder() {
        return holder;
    }

    @Override
    public void setItem(EquipmentSlot slot, ItemStack item) {
        holder.getInventory().setItem(slot, item);
    }

    @Override
    public void setItem(EquipmentSlot slot, ItemStack item, boolean silent) {
        setItem(slot, item);
    }

    @Override
    public ItemStack getItem(EquipmentSlot slot) {
        return holder.getInventory().getItem(slot);
    }

    @Override
    public void setDropChance(EquipmentSlot slot, float chance) {
    }

    @Override
    public float getDropChance(EquipmentSlot slot) {
        return 1.0f;
    }

    @Override
    public float getItemInMainHandDropChance() { return 1.0f; }
    @Override
    public void setItemInMainHandDropChance(float chance) {}

    @Override
    public float getItemInOffHandDropChance() { return 1.0f; }
    @Override
    public void setItemInOffHandDropChance(float chance) {}

    @Override
    public float getItemInHandDropChance() { return 1.0f; }
    @Override
    public void setItemInHandDropChance(float chance) {}

    @Override
    public float getHelmetDropChance() { return 1.0f; }
    @Override
    public void setHelmetDropChance(float chance) {}

    @Override
    public float getChestplateDropChance() { return 1.0f; }
    @Override
    public void setChestplateDropChance(float chance) {}

    @Override
    public float getLeggingsDropChance() { return 1.0f; }
    @Override
    public void setLeggingsDropChance(float chance) {}

    @Override
    public float getBootsDropChance() { return 1.0f; }
    @Override
    public void setBootsDropChance(float chance) {}
}
