package org.patchbukkit;

import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import io.papermc.paper.entity.EntitySerializationFlag;
import io.papermc.paper.inventory.tooltip.TooltipContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.registry.RegistryKey;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import patchbukkit.bridge.NativeBridgeFfi;

import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageSource.Builder;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;
import org.bukkit.potion.PotionType.InternalPotionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.patchbukkit.events.PatchBukkitLifecycleEventManager;
import org.patchbukkit.versioning.ApiVersion;
import org.patchbukkit.versioning.Versioning;
import patchbukkit.common.EmptyRequest;

public class PatchBukkitUnsafeValues implements UnsafeValues {

    public static final PatchBukkitUnsafeValues INSTANCE =
        new PatchBukkitUnsafeValues();

    @Override
    public boolean isSupportedApiVersion(String apiVersion) {
        if (apiVersion == null) return false;
        final ApiVersion toCheck = ApiVersion.getOrCreateVersion(apiVersion);
        var minimumApi = NativeBridgeFfi.getPatchBukkitConfig(EmptyRequest.newBuilder().build()).getMinimumSupportedPluginApi();
        final ApiVersion minimumVersion = ApiVersion.getOrCreateVersion(minimumApi);

        return !toCheck.isNewerThan(ApiVersion.CURRENT) && !toCheck.isOlderThan(minimumVersion);
    }

    @Override
    public void checkSupported(PluginDescriptionFile pdf)
        throws InvalidPluginException {
        String api = pdf.getAPIVersion();
        if (api != null && !isSupportedApiVersion(api)) {
            throw new InvalidPluginException("Unsupported API: " + api);
        }
    }



	@Override
	public Material toLegacy(Material material) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'toLegacy'");
	}

	@Override
	public Material fromLegacy(Material material) {
	    return PatchBukkitLegacy.fromLegacy(material);
	}

	@Override
	public Material fromLegacy(MaterialData material) {
	    return PatchBukkitLegacy.fromLegacy(material);
	}

	@Override
	public Material fromLegacy(MaterialData material, boolean itemPriority) {
	    return PatchBukkitLegacy.fromLegacy(material, itemPriority);
	}

	@Override
	public BlockData fromLegacy(Material material, byte data) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'fromLegacy'");
	}

	@Override
	public Material getMaterial(String material, int version) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getMaterial'");
	}

	@Override
	public int getDataVersion() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getDataVersion'");
	}

	@Override
	public ItemStack modifyItemStack(ItemStack stack, String arguments) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'modifyItemStack'");
	}

	@Override
	public byte[] processClass(PluginDescriptionFile pdf, String path, byte[] clazz) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'processClass'");
	}

	@Override
	public Advancement loadAdvancement(NamespacedKey key, String advancement) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'loadAdvancement'");
	}

	@Override
	public boolean removeAdvancement(NamespacedKey key) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'removeAdvancement'");
	}

	@Override
	public String get(Class<?> aClass, String value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'get'");
	}

	@Override
	public <B extends Keyed> B get(RegistryKey<B> registry, NamespacedKey key) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'get'");
	}

	@Override
	public @NotNull JsonObject serializeItemAsJson(@NotNull ItemStack itemStack) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'serializeItemAsJson'");
	}

	@Override
	public @NotNull ItemStack deserializeItemFromJson(@NotNull JsonObject data) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deserializeItemFromJson'");
	}

	@Override
	public byte @NotNull [] serializeEntity(@NotNull Entity entity,
			@NotNull EntitySerializationFlag... serializationFlags) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'serializeEntity'");
	}

	@Override
	public @NotNull Entity deserializeEntity(byte @NotNull [] data, @NotNull World world, boolean preserveUUID,
			boolean preservePassengers) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deserializeEntity'");
	}


	@Override
	public @NotNull String getMainLevelName() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getMainLevelName'");
	}

	@Override
	public int getProtocolVersion() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getProtocolVersion'");
	}



	@Override
	public @NotNull ItemStack deserializeStack(@NotNull Map<String, Object> args) {
	    System.out.println("Deserializing itemstack: " + args);
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deserializeStack'");
	}

	@Override
	public @NotNull ItemStack deserializeItemHover(@NotNull ShowItem itemHover) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deserializeItemHover'");
	}

	@Override
	public InternalPotionData getInternalPotionData(NamespacedKey key) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getInternalPotionData'");
	}

	@Override
	public int nextEntityId(World world) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'nextEntityId'");
	}

	
}
