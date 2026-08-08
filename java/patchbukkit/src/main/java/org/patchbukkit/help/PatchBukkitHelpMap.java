package org.patchbukkit.help;

import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PatchBukkitHelpMap implements HelpMap {

    private final Map<String, HelpTopic> helpTopics = new ConcurrentHashMap<>();
    private final Map<Class<?>, HelpTopicFactory<?>> topicFactories = new ConcurrentHashMap<>();
    private final List<String> ignoredPlugins = new ArrayList<>();

    @Override
    public @Nullable HelpTopic getHelpTopic(@NonNull String topicName) {
        if (topicName == null) return null;
        return helpTopics.get(topicName);
    }

    @Override
    public @NonNull Collection<HelpTopic> getHelpTopics() {
        return Collections.unmodifiableCollection(helpTopics.values());
    }

    @Override
    public void addTopic(@NonNull HelpTopic topic) {
        if (topic != null && topic.getName() != null) {
            helpTopics.put(topic.getName(), topic);
        }
    }

    @Override
    public void clear() {
        helpTopics.clear();
        topicFactories.clear();
    }

    @Override

    public void registerHelpTopicFactory(@NonNull Class<?> commandClass, @NonNull HelpTopicFactory<?> factory) {
        if (commandClass != null && factory != null) {
            topicFactories.put(commandClass, factory);
        }
    }

    @Override
    public @NonNull List<String> getIgnoredPlugins() {
        return Collections.unmodifiableList(ignoredPlugins);
    }
}
