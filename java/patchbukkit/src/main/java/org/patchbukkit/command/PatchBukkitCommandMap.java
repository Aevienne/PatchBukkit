package org.patchbukkit.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import patchbukkit.bridge.NativeBridgeFfi;
import patchbukkit.command.RegisterCommandRequest;

public class PatchBukkitCommandMap extends SimpleCommandMap {

    public PatchBukkitCommandMap(Server server) {
        super(server != null ? server : org.bukkit.Bukkit.getServer(), new HashMap<>());
    }

    public PatchBukkitCommandMap() {
        super(org.bukkit.Bukkit.getServer(), new HashMap<>());
    }

    private String cleanLabel(String label) {
        if (label == null) return "";
        String clean = label.trim();
        while (clean.startsWith("/")) {
            clean = clean.substring(1).trim();
        }
        return clean.toLowerCase();
    }

    private void registerVariants(String key, Command command) {
        if (key == null || key.isEmpty()) return;
        key = key.toLowerCase().trim();
        if (!knownCommands.containsKey(key)) {
            knownCommands.put(key, command);
        }
        String clean = cleanLabel(key);
        if (!clean.isEmpty()) {
            if (!knownCommands.containsKey(clean)) {
                knownCommands.put(clean, command);
            }
            String single = "/" + clean;
            if (!knownCommands.containsKey(single)) {
                knownCommands.put(single, command);
            }
            String doubleSlash = "//" + clean;
            if (!knownCommands.containsKey(doubleSlash)) {
                knownCommands.put(doubleSlash, command);
            }
        }
    }

    @Override
    public void registerAll(@NotNull String fallbackPrefix, @NotNull List<Command> commands) {
        for (Command command : commands) {
            register(fallbackPrefix, command);
        }
    }

    @Override
    public boolean register(@NotNull String label, @NotNull String fallbackPrefix, @NotNull Command command) {
        if (label == null || fallbackPrefix == null || command == null) {
            return false;
        }
        label = label.toLowerCase().trim();
        fallbackPrefix = fallbackPrefix.toLowerCase().trim();
        String clean = cleanLabel(label);

        boolean registeredDirectly = false;
        if (!knownCommands.containsKey(label) && !knownCommands.containsKey(clean)) {
            registeredDirectly = true;
        }

        registerVariants(label, command);
        registerVariants(fallbackPrefix + ":" + label, command);
        if (!clean.isEmpty()) {
            registerVariants(clean, command);
            registerVariants(fallbackPrefix + ":" + clean, command);
        }

        if (command.getAliases() != null) {
            for (String alias : command.getAliases()) {
                if (alias == null) continue;
                alias = alias.toLowerCase().trim();
                registerVariants(alias, command);
                registerVariants(fallbackPrefix + ":" + alias, command);
            }
        }

        try {
            List<String> aliasesList = command.getAliases() != null ? command.getAliases() : Collections.emptyList();
            String desc = command.getDescription() != null ? command.getDescription() : "";
            RegisterCommandRequest request = RegisterCommandRequest.newBuilder()
                    .setCmdName(label)
                    .addAllAliases(aliasesList)
                    .setDescription(desc)
                    .setPluginName(fallbackPrefix)
                    .build();
            NativeBridgeFfi.registerCommand(request);
        } catch (Throwable ignored) {
        }

        return registeredDirectly;
    }

    @Override
    public boolean register(@NotNull String fallbackPrefix, @NotNull Command command) {
        if (command == null) return false;
        return register(command.getName(), fallbackPrefix, command);
    }

    @Override
    public boolean dispatch(@NotNull CommandSender sender, @NotNull String cmdLine) throws CommandException {
        if (cmdLine == null) return false;
        String rawLine = cmdLine.trim();
        if (rawLine.isEmpty()) return false;

        String[] split = rawLine.split("\\s+");
        if (split.length == 0) return false;

        String rawLabel = split[0].toLowerCase();
        Command command = knownCommands.get(rawLabel);

        if (command == null) {
            String clean = cleanLabel(rawLabel);
            command = knownCommands.get(clean);
        }

        if (command == null) {
            return false; // Command not found
        }

        try {
            String[] args = Arrays.copyOfRange(split, 1, split.length);
            return command.execute(sender, rawLabel, args);
        } catch (Exception ex) {
            throw new CommandException("Unhandled exception executing '" + cmdLine + "'", ex);
        }
    }

    @Override
    public void clearCommands() {
        knownCommands.clear();
    }

    @Override
    public @Nullable Command getCommand(@NotNull String name) {
        if (name == null) return null;
        String lower = name.toLowerCase().trim();
        Command cmd = knownCommands.get(lower);
        if (cmd == null) {
            cmd = knownCommands.get(cleanLabel(lower));
        }
        return cmd;
    }

    @Override
    public @NotNull Map<String, Command> getKnownCommands() {
        return Collections.unmodifiableMap(knownCommands);
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String cmdLine) {
        return tabComplete(sender, cmdLine, null);
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String cmdLine, @Nullable Location location) {
        if (cmdLine == null) return new ArrayList<>();
        String rawLine = cmdLine.trim();
        String[] split = rawLine.split(" ", -1);
        if (split.length == 0) return new ArrayList<>();

        String label = split[0].toLowerCase();
        Command command = knownCommands.get(label);
        if (command == null) {
            command = knownCommands.get(cleanLabel(label));
        }

        if (split.length == 1) {
            List<String> completions = new ArrayList<>();
            for (String key : knownCommands.keySet()) {
                if (key.startsWith(label)) completions.add(key);
            }
            return completions;
        }

        if (command != null) {
            String[] args = Arrays.copyOfRange(split, 1, split.length);
            return command.tabComplete(sender, label, args, location);
        }

        return null;
    }
}