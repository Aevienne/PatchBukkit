package org.patchbukkit.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import patchbukkit.bridge.NativeBridgeFfi;
import patchbukkit.registry.GetRegistryDataRequest;
import patchbukkit.registry.GetRegistryDataResponse;
import patchbukkit.registry.RegistryType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class PatchBukkitRegistry<P, B extends Keyed> implements Registry<B> {

    private final Map<NamespacedKey, B> entries = new LinkedHashMap<>();
    private final Map<String, PatchBukkitTag<B>> tags = new LinkedHashMap<>();

    private final RegistryKey<B> registryKey;

    public PatchBukkitRegistry(
            RegistryType registryType,
            Function<GetRegistryDataResponse, List<P>> extractor,
            Function<P, B> factory
    ) {
        this(registryType, extractor, factory, null);
    }

    public PatchBukkitRegistry(
            RegistryType registryType,
            Function<GetRegistryDataResponse, List<P>> extractor,
            Function<P, B> factory,
            RegistryKey<B> registryKey
    ) {
        this.registryKey = registryKey;
        if (registryType == null) return;

        GetRegistryDataRequest request = GetRegistryDataRequest.newBuilder()
                .setRegistry(registryType)
                .build();

        GetRegistryDataResponse response = NativeBridgeFfi.getRegistryData(request);
        if (response == null) return;

        List<P> protoEntries = extractor.apply(response);
        for (P protoEntry : protoEntries) {
            B value = factory.apply(protoEntry);
            if (value != null) {
                entries.put(value.getKey(), value);
            }
        }
    }

    @Override
    public @Nullable B get(NamespacedKey key) {
        if (key == null) {
            return null;
        }
        B value = entries.get(key);
        if (value == null && (RegistryKey.SOUND_EVENT.equals(registryKey) || "sound_event".equalsIgnoreCase(registryKey != null ? registryKey.key().value() : ""))) {
            PatchBukkitSound dynamicSound = new PatchBukkitSound(key.getKey(), entries.size());
            entries.put(key, (B) dynamicSound);
            return (B) dynamicSound;
        }
        return value;
    }

    @Override
    public @Nullable NamespacedKey getKey(B value) {
        return value.getKey();
    }

    @Override
    public boolean hasTag(TagKey<B> key) {
        return tags.containsKey(key.key().asString());
    }

    @Override
    public @NonNull Tag<B> getTag(TagKey<B> key) {
        PatchBukkitTag<B> tag = tags.get(key.key().asString());
        if (tag == null) {
            throw new NoSuchElementException("Unknown tag: " + key.key().asString());
        }
        return tag;
    }

    public static <B extends Keyed> PatchBukkitRegistry<Object, B> empty(RegistryKey<B> registryKey) {
        return new PatchBukkitRegistry<>(
                null,
                response -> Collections.emptyList(),
                obj -> null,
                registryKey
        );
    }

    @Override
    public @NonNull Collection<Tag<B>> getTags() {
        return Collections.unmodifiableCollection(tags.values());
    }

    @Override
    public @NonNull Stream<B> stream() {
        return entries.values().stream();
    }

    @Override
    public @NonNull Stream<NamespacedKey> keyStream() {
        return entries.keySet().stream();
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public @NonNull Iterator<B> iterator() {
        return Collections.unmodifiableCollection(entries.values()).iterator();
    }
}
