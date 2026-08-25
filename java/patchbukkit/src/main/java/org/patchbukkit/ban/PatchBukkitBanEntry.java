package org.patchbukkit.ban;

import java.util.Date;
import java.util.Objects;
import org.bukkit.BanEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatchBukkitBanEntry<T> implements BanEntry<T> {
    private final String target;
    private final T banTarget;
    private Date created;
    private String source;
    private Date expiration;
    private String reason;
    private final PatchBukkitBanList<T> list;

    public PatchBukkitBanEntry(
        @NotNull String target,
        @Nullable T banTarget,
        @Nullable Date created,
        @Nullable String source,
        @Nullable Date expiration,
        @Nullable String reason,
        @Nullable PatchBukkitBanList<T> list
    ) {
        this.target = target;
        this.banTarget = banTarget;
        this.created = created != null ? created : new Date();
        this.source = source != null ? source : "Server";
        this.expiration = expiration;
        this.reason = reason != null ? reason : "Banned by operator";
        this.list = list;
    }

    @Override
    public @NotNull String getTarget() {
        return this.target;
    }

    @Override
    public @Nullable T getBanTarget() {
        return this.banTarget;
    }

    @Override
    public @NotNull Date getCreated() {
        return this.created;
    }

    @Override
    public void setCreated(@NotNull Date created) {
        this.created = created;
    }

    @Override
    public @NotNull String getSource() {
        return this.source;
    }

    @Override
    public void setSource(@NotNull String source) {
        this.source = source;
    }

    @Override
    public @Nullable Date getExpiration() {
        return this.expiration;
    }

    @Override
    public void setExpiration(@Nullable Date expiration) {
        this.expiration = expiration;
    }

    @Override
    public @Nullable String getReason() {
        return this.reason;
    }

    @Override
    public void setReason(@Nullable String reason) {
        this.reason = reason;
    }

    @Override
    public void save() {
        if (this.list != null) {
            this.list.saveEntry(this);
        }
    }

    @Override
    public void remove() {
        if (this.list != null) {
            this.list.pardon(this.target);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BanEntry<?> that)) return false;
        return Objects.equals(this.target, that.getTarget());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.target);
    }
}
