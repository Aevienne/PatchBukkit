package org.patchbukkit.registry;

import io.papermc.paper.registry.PaperRegistries;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.entry.RegistryEntry;
import io.papermc.paper.registry.entry.RegistryEntryMeta;
import net.minecraft.resources.ResourceKey;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.jspecify.annotations.Nullable;

import patchbukkit.registry.GetRegistryDataResponse;
import patchbukkit.registry.RegistryType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class PatchBukkitRegistryAccess extends io.papermc.paper.registry.PaperRegistryAccess {
    private static final Map<RegistryKey<?>, Registry<?>> instances = new ConcurrentHashMap<>();

    /**
    * Maps a RegistryKey to (native registry name, factory function).
    * Add an entry here for each registry you support.
    */
    private static final Map<RegistryKey<?>, RegistryFactory<?, ?>> FACTORIES = Map.of(
        RegistryKey.SOUND_EVENT, new RegistryFactory<>(
            RegistryType.SOUND_EVENT,
            response -> (response != null && response.hasSoundEvent()) ? response.getSoundEvent().getSoundEventsList() : Collections.emptyList(),
            data -> new PatchBukkitSound(
                data.getName(),
                data.getId()
            )
        )
    );

    private record RegistryFactory<P, B extends Keyed>(
            RegistryType registryType,
            Function<GetRegistryDataResponse, List<P>> extractor,
            Function<P, B> factory
    ) {}

    @Override
    public <T extends Keyed> @Nullable Registry<T> getRegistry(Class<T> type) {
        final RegistryKey<T> registryKey = byType(type);
        return this.getRegistry(registryKey);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T extends Keyed> Registry<T> getRegistry(RegistryKey<T> registryKey) {
        if (registryKey == null) return null;

        // Prefer the vanilla CraftRegistry that Paper registered via registerRegistry
        // (wrapping the NMS MappedRegistry). Only fallback to PatchBukkitRegistry for
        // custom registries (e.g. SOUND_EVENT via FFI) or when super not yet available.
        try {
            Registry<T> superReg = super.getRegistry(registryKey);
            if (superReg != null) {
                return superReg;
            }
        } catch (NoSuchElementException | IllegalArgumentException ignored) {
            // invalid key or not available yet — fall through to PatchBukkit handling
        } catch (Exception ignored) {}

        Registry<?> existing = instances.get(registryKey);
        if (existing != null) {
            return (Registry<T>) existing;
        }

        PatchBukkitRegistry reg;
        RegistryFactory<?, ?> factoryEntry = FACTORIES.get(registryKey);
        if (factoryEntry != null) {
            reg = new PatchBukkitRegistry(factoryEntry.registryType(), factoryEntry.extractor(), factoryEntry.factory(), registryKey);
        } else {
            reg = new PatchBukkitRegistry(registryKey);
        }

        Registry<?> winner = instances.putIfAbsent(registryKey, reg);
        return (Registry<T>) (winner != null ? winner : reg);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <M> void lockReferenceHolders(final ResourceKey<? extends net.minecraft.core.Registry<M>> resourceKey) {
        final RegistryEntry<M, Keyed> entry = PaperRegistries.getEntry(resourceKey);
        if (entry == null || !(entry.meta() instanceof final RegistryEntryMeta.ServerSide<M, Keyed> serverSide) || !serverSide.registryTypeMapper().constructorUsesHolder()) {
            return;
        }
        Registry<?> reg;
        try {
            reg = this.getRegistry(entry.apiKey());
        } catch (Exception e) {
            return;
        }
        if (reg instanceof CraftRegistry craft) {
            craft.lockReferenceHolders();
        } else if (reg instanceof PatchBukkitRegistry) {
            // PatchBukkitRegistry doesn't use NMS holder locking (it synthesizes holders via FFI); no-op
            return;
        } else {
            // Unknown type — try to avoid ClassCastException, best-effort no-op
            try {
                if (reg instanceof CraftRegistry) {
                    ((CraftRegistry) reg).lockReferenceHolders();
                }
            } catch (ClassCastException ignored) {}
        }
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    public static <T extends Keyed> @Nullable RegistryKey<T> byType(final Class<T> type) {
        if (type == null) return null;
        return (RegistryKey<T>) LegacyRegistryIdentifiers.CLASS_TO_KEY_MAP.get(type);
    }
}
