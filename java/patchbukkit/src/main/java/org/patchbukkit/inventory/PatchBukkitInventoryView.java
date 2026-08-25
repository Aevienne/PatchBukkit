package org.patchbukkit.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatchBukkitInventoryView implements InventoryView {
    private final HumanEntity player;
    private final Inventory topInventory;
    private String title;

    public PatchBukkitInventoryView(@NotNull HumanEntity player, @NotNull Inventory topInventory) {
        this.player = player;
        this.topInventory = topInventory;
        this.title = topInventory.getType().getDefaultTitle();
    }

    public PatchBukkitInventoryView(@NotNull HumanEntity player, @NotNull Inventory topInventory, @NotNull String title) {
        this.player = player;
        this.topInventory = topInventory;
        this.title = title;
    }

    @Override
    public @NotNull Inventory getTopInventory() {
        return this.topInventory;
    }

    @Override
    public @NotNull Inventory getBottomInventory() {
        return this.player.getInventory();
    }

    @Override
    public @NotNull HumanEntity getPlayer() {
        return this.player;
    }

    @Override
    public @NotNull InventoryType getType() {
        return this.topInventory.getType();
    }

    @Override
    public void setItem(int slot, @Nullable ItemStack item) {
        if (slot >= 0 && slot < getTopInventory().getSize()) {
            getTopInventory().setItem(slot, item);
        } else if (slot >= getTopInventory().getSize()) {
            getBottomInventory().setItem(slot - getTopInventory().getSize(), item);
        }
    }

    @Override
    public @Nullable ItemStack getItem(int slot) {
        if (slot >= 0 && slot < getTopInventory().getSize()) {
            return getTopInventory().getItem(slot);
        } else if (slot >= getTopInventory().getSize()) {
            return getBottomInventory().getItem(slot - getTopInventory().getSize());
        }
        return null;
    }

    @Override
    public void setCursor(@Nullable ItemStack item) {
        getPlayer().setItemOnCursor(item);
    }

    @Override
    public @Nullable ItemStack getCursor() {
        return getPlayer().getItemOnCursor();
    }

    @Override
    public @Nullable Inventory getInventory(int rawSlot) {
        if (rawSlot >= 0 && rawSlot < getTopInventory().getSize()) {
            return getTopInventory();
        } else if (rawSlot >= getTopInventory().getSize()) {
            return getBottomInventory();
        }
        return null;
    }

    @Override
    public int convertSlot(int rawSlot) {
        if (rawSlot >= getTopInventory().getSize()) {
            return rawSlot - getTopInventory().getSize();
        }
        return rawSlot;
    }

    @Override
    public @NotNull SlotType getSlotType(int slot) {
        if (slot < getTopInventory().getSize()) {
            return SlotType.CONTAINER;
        }
        return SlotType.QUICKBAR;
    }

    @Override
    public void open() {
    }

    @Override
    public void close() {
        getPlayer().closeInventory();
    }

    @Override
    public int countSlots() {
        return getTopInventory().getSize() + getBottomInventory().getSize();
    }

    @Override
    public boolean setProperty(@NotNull Property prop, int value) {
        return false;
    }

    @Override
    public @NotNull String getTitle() {
        return this.title != null ? this.title : getType().getDefaultTitle();
    }

    @Override
    public @NotNull String getOriginalTitle() {
        return getType().getDefaultTitle();
    }

    @Override
    public void setTitle(@NotNull String title) {
        this.title = title;
    }

    @Override
    public @NotNull MenuType getMenuType() {
        return MenuType.GENERIC_9X3;
    }
}
