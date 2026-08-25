package org.patchbukkit.ban;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import patchbukkit.bridge.NativeBridgeFfi;
import patchbukkit.server.BanEntryProto;
import patchbukkit.server.GetBanListRequest;
import patchbukkit.server.SetBanEntryRequest;

@SuppressWarnings("unchecked")
public class PatchBukkitBanList<T> implements BanList<T> {
    private final String banType;
    private final Map<String, BanEntry<T>> entries = new ConcurrentHashMap<>();

    public PatchBukkitBanList(@NotNull String banType) {
        this.banType = banType;
        refresh();
    }

    public void refresh() {
        try {
            var res = NativeBridgeFfi.getBanList(GetBanListRequest.newBuilder().setBanType(banType).build());
            if (res != null) {
                entries.clear();
                for (BanEntryProto proto : res.getEntriesList()) {
                    Date created = new Date(proto.getCreated() * 1000);
                    Date expires = proto.getExpires() > 0 ? new Date(proto.getExpires() * 1000) : null;
                    T targetObj = parseTarget(proto.getTarget());
                    PatchBukkitBanEntry<T> entry = new PatchBukkitBanEntry<>(
                        proto.getTarget(),
                        targetObj,
                        created,
                        proto.getSource(),
                        expires,
                        proto.getReason(),
                        this
                    );
                    entries.put(proto.getTarget().toLowerCase(), entry);
                }
            }
        } catch (Throwable ignored) {}
    }

    private T parseTarget(String target) {
        if ("IP".equalsIgnoreCase(banType)) {
            try {
                return (T) InetAddress.getByName(target);
            } catch (Throwable ignored) {}
        }
        return (T) target;
    }

    public void saveEntry(PatchBukkitBanEntry<T> entry) {
        entries.put(entry.getTarget().toLowerCase(), entry);
        try {
            var proto = BanEntryProto.newBuilder()
                .setTarget(entry.getTarget())
                .setSource(entry.getSource())
                .setCreated(entry.getCreated().getTime() / 1000)
                .setExpires(entry.getExpiration() != null ? entry.getExpiration().getTime() / 1000 : 0)
                .setReason(entry.getReason() != null ? entry.getReason() : "")
                .build();
            NativeBridgeFfi.setBanEntry(SetBanEntryRequest.newBuilder()
                .setBanType(banType)
                .setEntry(proto)
                .setRemove(false)
                .build());
        } catch (Throwable ignored) {}
    }

    @Override
    public <E extends BanEntry<? super T>> @Nullable E getBanEntry(@NotNull String target) {
        return (E) entries.get(target.toLowerCase());
    }

    @Override
    public @Nullable BanEntry<T> getBanEntry(@NotNull T target) {
        return entries.get(targetToString(target).toLowerCase());
    }

    @Override
    public <E extends BanEntry<? super T>> @Nullable E addBan(
        @NotNull String target,
        @Nullable String reason,
        @Nullable Date expires,
        @Nullable String source
    ) {
        PatchBukkitBanEntry<T> entry = new PatchBukkitBanEntry<>(
            target,
            parseTarget(target),
            new Date(),
            source,
            expires,
            reason,
            this
        );
        saveEntry(entry);
        return (E) entry;
    }

    @Override
    public @Nullable BanEntry<T> addBan(
        @NotNull T target,
        @Nullable String reason,
        @Nullable Date expires,
        @Nullable String source
    ) {
        return addBan(targetToString(target), reason, expires, source);
    }

    @Override
    public @Nullable BanEntry<T> addBan(
        @NotNull T target,
        @Nullable String reason,
        @Nullable Instant expires,
        @Nullable String source
    ) {
        return addBan(target, reason, expires != null ? Date.from(expires) : null, source);
    }

    @Override
    public @Nullable BanEntry<T> addBan(
        @NotNull T target,
        @Nullable String reason,
        @Nullable Duration duration,
        @Nullable String source
    ) {
        Date expires = duration != null ? Date.from(Instant.now().plus(duration)) : null;
        return addBan(target, reason, expires, source);
    }

    @Override
    public @NotNull Set<BanEntry> getBanEntries() {
        return new HashSet<>(entries.values());
    }

    @Override
    public <E extends BanEntry<? super T>> @NotNull Set<E> getEntries() {
        return (Set<E>) new HashSet<>(entries.values());
    }

    @Override
    public boolean isBanned(@NotNull T target) {
        return isBanned(targetToString(target));
    }

    @Override
    public boolean isBanned(@NotNull String target) {
        BanEntry<T> entry = entries.get(target.toLowerCase());
        if (entry == null) return false;
        if (entry.getExpiration() != null && entry.getExpiration().before(new Date())) {
            pardon(target);
            return false;
        }
        return true;
    }

    @Override
    public void pardon(@NotNull T target) {
        pardon(targetToString(target));
    }

    @Override
    public void pardon(@NotNull String target) {
        entries.remove(target.toLowerCase());
        try {
            var proto = BanEntryProto.newBuilder().setTarget(target).build();
            NativeBridgeFfi.setBanEntry(SetBanEntryRequest.newBuilder()
                .setBanType(banType)
                .setEntry(proto)
                .setRemove(true)
                .build());
        } catch (Throwable ignored) {}
    }

    private String targetToString(T target) {
        if (target instanceof InetAddress addr) {
            return addr.getHostAddress();
        }
        return String.valueOf(target);
    }
}
