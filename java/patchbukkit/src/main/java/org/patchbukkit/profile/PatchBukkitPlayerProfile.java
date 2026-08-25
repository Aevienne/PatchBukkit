package org.patchbukkit.profile;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatchBukkitPlayerProfile implements PlayerProfile {
    private UUID uuid;
    private String name;
    private final Set<ProfileProperty> properties = new HashSet<>();
    private PlayerTextures textures = new EmptyPlayerTextures();

    public PatchBukkitPlayerProfile(@Nullable UUID uuid, @Nullable String name) {
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public @Nullable UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public @Nullable UUID getId() {
        return this.uuid;
    }

    @Override
    public @Nullable UUID setId(@Nullable UUID uuid) {
        UUID old = this.uuid;
        this.uuid = uuid;
        return old;
    }

    @Override
    public @Nullable String getName() {
        return this.name;
    }

    @Override
    public @Nullable String setName(@Nullable String name) {
        String old = this.name;
        this.name = name;
        return old;
    }

    @Override
    public @NotNull PlayerTextures getTextures() {
        return this.textures;
    }

    @Override
    public void setTextures(@Nullable PlayerTextures textures) {
        this.textures = textures != null ? textures : new EmptyPlayerTextures();
    }

    @Override
    public @NotNull Set<ProfileProperty> getProperties() {
        return new HashSet<>(this.properties);
    }

    @Override
    public boolean hasProperty(@NotNull String propertyName) {
        return this.properties.stream().anyMatch(p -> p.getName().equalsIgnoreCase(propertyName));
    }

    @Override
    public void setProperty(@NotNull ProfileProperty property) {
        this.properties.removeIf(p -> p.getName().equalsIgnoreCase(property.getName()));
        this.properties.add(property);
    }

    @Override
    public void setProperties(@NotNull Collection<ProfileProperty> properties) {
        for (ProfileProperty p : properties) {
            setProperty(p);
        }
    }

    @Override
    public boolean removeProperty(@NotNull String propertyName) {
        return this.properties.removeIf(p -> p.getName().equalsIgnoreCase(propertyName));
    }

    @Override
    public void clearProperties() {
        this.properties.clear();
    }

    @Override
    public boolean isComplete() {
        return this.uuid != null && this.name != null && !this.name.isEmpty();
    }

    @Override
    public boolean completeFromCache() {
        return completeFromCache(false, false);
    }

    @Override
    public boolean completeFromCache(boolean onlineMode) {
        return completeFromCache(onlineMode, false);
    }

    @Override
    public boolean completeFromCache(boolean onlineMode, boolean setRequestProperties) {
        return isComplete();
    }

    @Override
    public boolean complete(boolean textures) {
        return complete(textures, false);
    }

    @Override
    public boolean complete(boolean textures, boolean onlineMode) {
        return isComplete();
    }

    @Override
    public @NotNull CompletableFuture<PlayerProfile> update() {
        return CompletableFuture.completedFuture(this);
    }

    @Override
    public @NotNull PlayerProfile clone() {
        PatchBukkitPlayerProfile copy = new PatchBukkitPlayerProfile(this.uuid, this.name);
        copy.properties.addAll(this.properties);
        copy.textures = this.textures;
        return copy;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        if (this.uuid != null) map.put("uniqueId", this.uuid.toString());
        if (this.name != null) map.put("name", this.name);
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerProfile that)) return false;
        return Objects.equals(this.uuid, that.getId()) && Objects.equals(this.name, that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uuid, this.name);
    }

    private static class EmptyPlayerTextures implements PlayerTextures {
        private URL skin;
        private URL cape;
        private SkinModel skinModel = SkinModel.CLASSIC;

        @Override
        public boolean isEmpty() {
            return skin == null && cape == null;
        }

        @Override
        public void clear() {
            skin = null;
            cape = null;
        }

        @Override
        public @Nullable URL getSkin() {
            return skin;
        }

        @Override
        public void setSkin(@Nullable URL skinUrl) {
            this.skin = skinUrl;
        }

        @Override
        public void setSkin(@Nullable URL skinUrl, @Nullable SkinModel skinModel) {
            this.skin = skinUrl;
            this.skinModel = skinModel != null ? skinModel : SkinModel.CLASSIC;
        }

        @Override
        public @NotNull SkinModel getSkinModel() {
            return skinModel;
        }

        @Override
        public @Nullable URL getCape() {
            return cape;
        }

        @Override
        public void setCape(@Nullable URL capeUrl) {
            this.cape = capeUrl;
        }

        @Override
        public long getTimestamp() {
            return System.currentTimeMillis();
        }

        @Override
        public boolean isSigned() {
            return false;
        }
    }
}
