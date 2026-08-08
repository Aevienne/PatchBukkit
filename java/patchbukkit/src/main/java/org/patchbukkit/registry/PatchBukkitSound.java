package org.patchbukkit.registry;

import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;

public class PatchBukkitSound implements Sound {

    private final NamespacedKey namespacedKey;
    private final Key adventureKey;
    private final String enumName;
    private final String originalName;
    private final int protocolId;

    public PatchBukkitSound(String soundName, int protocolId) {
        if (soundName == null) soundName = "unknown";
        String keyPath = soundName.startsWith("minecraft:") ? soundName.substring(10) : soundName;
        keyPath = keyPath.toLowerCase(java.util.Locale.ROOT);

        NamespacedKey key = NamespacedKey.fromString(keyPath);
        if (key == null) {
            key = NamespacedKey.minecraft(keyPath.replaceAll("[^a-z0-9/._-]", "_"));
        }
        this.namespacedKey = key;
        this.adventureKey = Key.key(key.namespace(), key.value());
        this.protocolId = protocolId;
        this.originalName = soundName;
        this.enumName = key.value().toUpperCase(java.util.Locale.ROOT).replace('.', '_').replace('-', '_').replace('/', '_');
    }

    public String getOriginalName() {
        return originalName;
    }

    @Override
    public NamespacedKey getKey() {
        return namespacedKey;
    }

    @Override
    public Key key() {
        return adventureKey;
    }

    @Override
    public int ordinal() {
        return protocolId;
    }

    @Override
    public String name() {
        return enumName;
    }

    @Override
    public int compareTo(Sound other) {
        return Integer.compare(this.ordinal(), other.ordinal());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Sound other)) return false;
        return namespacedKey.equals(other.getKey());
    }

    @Override
    public int hashCode() {
        return namespacedKey.hashCode();
    }

    @Override
    public String toString() {
        return "PatchBukkitSound{" + namespacedKey + "}";
    }
}
