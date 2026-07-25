package org.patchbukkit.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatchBukkitCommandMap implements CommandMap {
    // We use Command as the value to support both PluginCommand and vanilla commands
    private final Map<String, Command> knownCommands = new HashMap<>();

    @Override
    public void registerAll(@NotNull String fallbackPrefix, @NotNull List<Command> commands) {
        for (Command command : commands) {
            register(fallbackPrefix, command);
        }
    }

    @Override
    public boolean register(@NotNull String label, @NotNull String fallbackPrefix, @NotNull Command command) {
        label = label.toLowerCase().trim();
        fallbackPrefix = fallbackPrefix.toLowerCase().trim();
        
        // 1. Register under the direct label if not taken
        boolean registeredDirectly = false;
        if (!knownCommands.containsKey(label)) {
            knownCommands.put(label, command);
            registeredDirectly = true;
        }

        // 2. Always register under the fallback prefix (pluginname:label)
        knownCommands.put(fallbackPrefix + ":" + label, command);

        // 3. Register aliases
        for (String alias : command.getAliases()) {
            alias = alias.toLowerCase().trim();
            if (!knownCommands.containsKey(alias)) {
                knownCommands.put(alias, command);
            }
            knownCommands.put(fallbackPrefix + ":" + alias, command);
        }

        return registeredDirectly;
    }

    @Override
    public boolean register(@NotNull String fallbackPrefix, @NotNull Command command) {
        return register(command.getName(), fallbackPrefix, command);
    }

    @Override
    public boolean dispatch(@NotNull CommandSender sender, @NotNull String cmdLine) throws CommandException {
        if (cmdLine == null) return false;
        String cleanLine = cmdLine.trim();
        if (cleanLine.startsWith("/")) {
            cleanLine = cleanLine.substring(1).trim();
        }
        if (cleanLine.isEmpty()) return false;

        String[] split = cleanLine.split("\\s+");
        if (split.length == 0) return false;

        String sentLabel = split[0].toLowerCase();
        Command command = knownCommands.get(sentLabel);

        if (command == null) {
            return false; // Command not found
        }

        try {
            // Extract arguments (everything after the label)
            String[] args = Arrays.copyOfRange(split, 1, split.length);
            // This triggers the PluginCommand.execute() method you implemented earlier
            return command.execute(sender, sentLabel, args);
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
        return knownCommands.get(name.toLowerCase());
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
        String[] split = cmdLine.split(" ", -1);
        if (split.length == 0) return new ArrayList<>();

        String label = split[0].toLowerCase();
        Command command = knownCommands.get(label);

        // If typing the first word, return matching command names
        if (split.length == 1) {
            List<String> completions = new ArrayList<>();
            for (String key : knownCommands.keySet()) {
                if (key.startsWith(label)) completions.add(key);
            }
            return completions;
        }

        // Otherwise, delegate to the specific command's tab completer
        if (command != null) {
            String[] args = Arrays.copyOfRange(split, 1, split.length);
            return command.tabComplete(sender, label, args, location);
        }

        return null;
    }
}