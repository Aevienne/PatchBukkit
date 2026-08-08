package org.patchbukkit.command;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.kyori.adventure.text.Component;

public class PatchBukkitBlockCommandSender implements BlockCommandSender {
    @Override
    public Block getBlock() {
        return null;
    }

    @Override
    public boolean isPermissionSet(@NotNull String name) {
        return true;
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission perm) {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull String name) {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull Permission perm) {
        return true;
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
        return new PermissionAttachment(plugin, this);
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return new PermissionAttachment(plugin, this);
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value,
            int ticks) {
        return new PermissionAttachment(plugin, this);
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        return new PermissionAttachment(plugin, this);
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment attachment) {
    }

    @Override
    public void recalculatePermissions() {
    }

    @Override
    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return java.util.Collections.emptySet();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
    }

    @Override
    public void sendMessage(@NotNull String message) {
    }

    @Override
    public void sendMessage(@NotNull String... messages) {
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String message) {
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String... messages) {
    }

    @Override
    public @NotNull Server getServer() {
        return org.bukkit.Bukkit.getServer();
    }

    @Override
    public @NotNull String getName() {
        return "@";
    }

    @Override
    public @NotNull Spigot spigot() {
        return new Spigot();
    }

    @Override
    public @NotNull Component name() {
        return Component.text("@");
    }

}
