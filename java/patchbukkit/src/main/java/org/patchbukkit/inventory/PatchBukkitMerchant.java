package org.patchbukkit.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatchBukkitMerchant implements Merchant {
    private final Component title;
    private List<MerchantRecipe> recipes = new ArrayList<>();
    private HumanEntity trader;

    public PatchBukkitMerchant(@Nullable Component title) {
        this.title = title != null ? title : Component.text("Merchant");
    }

    @Override
    public @NotNull List<MerchantRecipe> getRecipes() {
        return Collections.unmodifiableList(this.recipes);
    }

    @Override
    public void setRecipes(@NotNull List<MerchantRecipe> recipes) {
        this.recipes = new ArrayList<>(recipes);
    }

    @Override
    public @NotNull MerchantRecipe getRecipe(int i) throws IndexOutOfBoundsException {
        return this.recipes.get(i);
    }

    @Override
    public void setRecipe(int i, @NotNull MerchantRecipe recipe) throws IndexOutOfBoundsException {
        this.recipes.set(i, recipe);
    }

    @Override
    public int getRecipeCount() {
        return this.recipes.size();
    }

    @Override
    public boolean isTrading() {
        return this.trader != null;
    }

    @Override
    public @Nullable HumanEntity getTrader() {
        return this.trader;
    }
}
