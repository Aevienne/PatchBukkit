package org.patchbukkit.tag;

import java.util.Collections;
import java.util.Set;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;

public class PatchBukkitTag<T extends Keyed> implements Tag<T> {
    private final NamespacedKey key;
    private final Set<T> values;

    public PatchBukkitTag(@NotNull NamespacedKey key, @NotNull Set<T> values) {
        this.key = key;
        this.values = values;
    }

    public PatchBukkitTag(@NotNull NamespacedKey key) {
        this(key, Collections.emptySet());
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public boolean isTagged(@NotNull T item) {
        return values.contains(item);
    }

    @Override
    public @NotNull Set<T> getValues() {
        return values;
    }
}
