package org.patchbukkit.inventory;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class PatchBukkitItemStack extends ItemStack {

    private Material type;
    private int amount;
    private ItemMeta meta;

    public PatchBukkitItemStack(Material type) {
        this(type, 1);
    }

    public PatchBukkitItemStack(Material type, int amount) {
        this.type = type != null ? type : Material.AIR;
        this.amount = amount;
    }

    @Override
    public @NonNull Material getType() {
        return type;
    }

    @Override
    public void setType(@Nullable Material type) {
        this.type = type != null ? type : Material.AIR;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public @Nullable ItemMeta getItemMeta() {
        return meta;
    }

    @Override
    public boolean setItemMeta(@Nullable ItemMeta itemMeta) {
        this.meta = itemMeta;
        return true;
    }

    @Override
    public boolean hasItemMeta() {
        return meta != null;
    }

    @Override
    public boolean isEmpty() {
        return type == Material.AIR || amount <= 0;
    }

    @Override
    public @NonNull ItemStack clone() {
        PatchBukkitItemStack cloned = new PatchBukkitItemStack(type, amount);
        if (meta != null) {
            cloned.meta = meta.clone();
        }
        return cloned;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ItemStack other)) return false;
        return this.type == other.getType() && this.amount == other.getAmount() && Objects.equals(this.meta, other.getItemMeta());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, amount, meta);
    }

    @Override
    public String toString() {
        return "ItemStack{" + type + " x " + amount + "}";
    }
}
