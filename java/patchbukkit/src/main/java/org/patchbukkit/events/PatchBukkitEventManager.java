package org.patchbukkit.events;

import org.bukkit.Server;
import org.bukkit.Warning;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.patchbukkit.bridge.BridgeUtils;

import com.google.common.collect.Sets;

import co.aikar.timings.TimedEventExecutor;

import org.jetbrains.annotations.NotNull;
import patchbukkit.bridge.NativeBridgeFfi;
import patchbukkit.events.BlockBreakEvent;
import patchbukkit.events.BlockPlaceEvent;
import patchbukkit.events.CallEventRequest;
import patchbukkit.events.PlayerChatEvent;
import patchbukkit.events.PlayerGameModeChangeEvent;
import patchbukkit.events.PlayerInteractEvent;
import patchbukkit.events.PlayerJoinEvent;
import patchbukkit.events.PlayerInteractEntityEvent;
import patchbukkit.events.PlayerMoveEvent;
import patchbukkit.events.PlayerQuitEvent;
import patchbukkit.events.PlayerToggleFlightEvent;
import patchbukkit.events.PlayerToggleSneakEvent;
import patchbukkit.events.PlayerToggleSprintEvent;
import patchbukkit.events.PluginDisableEvent;
import patchbukkit.events.PluginEnableEvent;
import patchbukkit.events.RegisterEventRequest;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class PatchBukkitEventManager {

    private final Server server;

    public PatchBukkitEventManager(Server server) {
        this.server = server;
    }

    public void callEvent(@NotNull Event event) throws IllegalStateException {
        if (event.isAsynchronous() && this.server.isPrimaryThread()) {
            throw new IllegalStateException(event.getEventName() + " may only be triggered asynchronously.");
        } else if (!event.isAsynchronous() && !this.server.isPrimaryThread() && !this.server.isStopping()) {
            throw new IllegalStateException(event.getEventName() + " may only be triggered synchronously.");
        }

        var request = CallEventRequest.newBuilder();
        switch (event.getEventName()) {
            case "org.bukkit.event.player.PlayerJoinEvent":
                var castedEvent = (org.bukkit.event.player.PlayerJoinEvent) event;
                request.setEvent(
                    patchbukkit.events.Event.newBuilder().setPlayerJoin(
                        PlayerJoinEvent.newBuilder()
                            .setJoinMessage(castedEvent.joinMessage().toString())
                            .setPlayerUuid(BridgeUtils.convertUuid(castedEvent.getPlayer().getUniqueId())).build()
                    ).build()
                );
                break;
            case "org.bukkit.event.player.PlayerQuitEvent":
                var castedQuit = (org.bukkit.event.player.PlayerQuitEvent) event;
                String qMsg = castedQuit.quitMessage() != null ? castedQuit.quitMessage().toString() : "";
                request.setEvent(
                    patchbukkit.events.Event.newBuilder().setPlayerQuit(
                        PlayerQuitEvent.newBuilder()
                            .setQuitMessage(qMsg)
                            .setPlayerUuid(BridgeUtils.convertUuid(castedQuit.getPlayer().getUniqueId())).build()
                    ).build()
                );
                break;
            case "org.bukkit.event.player.PlayerGameModeChangeEvent":
                var castedGM = (org.bukkit.event.player.PlayerGameModeChangeEvent) event;
                request.setEvent(
                    patchbukkit.events.Event.newBuilder().setPlayerGamemodeChange(
                        PlayerGameModeChangeEvent.newBuilder()
                            .setNewGamemode(castedGM.getNewGameMode().name())
                            .setPlayerUuid(BridgeUtils.convertUuid(castedGM.getPlayer().getUniqueId())).build()
                    ).build()
                );
                break;
            case "org.bukkit.event.player.PlayerInteractEvent":
                var castedInteract = (org.bukkit.event.player.PlayerInteractEvent) event;
                var block = castedInteract.getClickedBlock();
                int x = block != null ? block.getX() : 0;
                int y = block != null ? block.getY() : 0;
                int z = block != null ? block.getZ() : 0;
                request.setEvent(
                    patchbukkit.events.Event.newBuilder().setPlayerInteract(
                        PlayerInteractEvent.newBuilder()
                            .setAction(castedInteract.getAction().name())
                            .setClickedX(x)
                            .setClickedY(y)
                            .setClickedZ(z)
                            .setBlockFace(castedInteract.getBlockFace().name())
                            .setHand(castedInteract.getHand() != null ? castedInteract.getHand().name() : "HAND")
                            .setPlayerUuid(BridgeUtils.convertUuid(castedInteract.getPlayer().getUniqueId())).build()
                    ).build()
                );
                break;
            case "org.bukkit.event.server.PluginEnableEvent":
                var castedEnable = (org.bukkit.event.server.PluginEnableEvent) event;
                request.setEvent(
                    patchbukkit.events.Event.newBuilder().setPluginEnable(
                        PluginEnableEvent.newBuilder()
                            .setPluginName(castedEnable.getPlugin().getName()).build()
                    ).build()
                );
                break;
            case "org.bukkit.event.block.SignChangeEvent":
                var castedSign = (org.bukkit.event.block.SignChangeEvent) event;
                var signBlock = castedSign.getBlock();
                var signBuilder = patchbukkit.events.SignChangeEvent.newBuilder()
                    .setBlockX(signBlock.getX())
                    .setBlockY(signBlock.getY())
                    .setBlockZ(signBlock.getZ());
                if (castedSign.getPlayer() != null) {
                    signBuilder.setPlayerUuid(BridgeUtils.convertUuid(castedSign.getPlayer().getUniqueId()));
                }
                for (String line : castedSign.getLines()) {
                    signBuilder.addLines(line != null ? line : "");
                }
                request.setEvent(patchbukkit.events.Event.newBuilder().setSignChange(signBuilder.build()).build());
                break;
            case "org.bukkit.event.block.BlockDamageEvent":
                var castedDamage = (org.bukkit.event.block.BlockDamageEvent) event;
                var dmgBlock = castedDamage.getBlock();
                var dmgBuilder = patchbukkit.events.BlockDamageEvent.newBuilder()
                    .setBlockX(dmgBlock.getX())
                    .setBlockY(dmgBlock.getY())
                    .setBlockZ(dmgBlock.getZ())
                    .setInstaBreak(castedDamage.getInstaBreak());
                if (castedDamage.getPlayer() != null) {
                    dmgBuilder.setPlayerUuid(BridgeUtils.convertUuid(castedDamage.getPlayer().getUniqueId()));
                }
                request.setEvent(patchbukkit.events.Event.newBuilder().setBlockDamage(dmgBuilder.build()).build());
                break;
            case "org.bukkit.event.block.BlockIgniteEvent":
                var castedIgnite = (org.bukkit.event.block.BlockIgniteEvent) event;
                var igniteBlock = castedIgnite.getBlock();
                var igniteBuilder = patchbukkit.events.BlockIgniteEvent.newBuilder()
                    .setBlockX(igniteBlock.getX())
                    .setBlockY(igniteBlock.getY())
                    .setBlockZ(igniteBlock.getZ())
                    .setIgniteCause(castedIgnite.getCause() != null ? castedIgnite.getCause().name() : "FLINT_AND_STEEL");
                if (castedIgnite.getPlayer() != null) {
                    igniteBuilder.setPlayerUuid(BridgeUtils.convertUuid(castedIgnite.getPlayer().getUniqueId()));
                }
                request.setEvent(patchbukkit.events.Event.newBuilder().setBlockIgnite(igniteBuilder.build()).build());
                break;
            case "org.bukkit.event.block.BlockGrowEvent":
                var castedGrow = (org.bukkit.event.block.BlockGrowEvent) event;
                var growBlock = castedGrow.getBlock();
                request.setEvent(patchbukkit.events.Event.newBuilder().setBlockGrow(
                    patchbukkit.events.BlockGrowEvent.newBuilder()
                        .setBlockX(growBlock.getX())
                        .setBlockY(growBlock.getY())
                        .setBlockZ(growBlock.getZ())
                        .setNewBlockType(castedGrow.getNewState().getType().name()).build()
                ).build());
                break;
            case "org.bukkit.event.block.BlockFormEvent":
                var castedForm = (org.bukkit.event.block.BlockFormEvent) event;
                var formBlock = castedForm.getBlock();
                request.setEvent(patchbukkit.events.Event.newBuilder().setBlockForm(
                    patchbukkit.events.BlockFormEvent.newBuilder()
                        .setBlockX(formBlock.getX())
                        .setBlockY(formBlock.getY())
                        .setBlockZ(formBlock.getZ())
                        .setNewBlockType(castedForm.getNewState().getType().name()).build()
                ).build());
                break;
            case "org.bukkit.event.block.BlockFadeEvent":
                var castedFade = (org.bukkit.event.block.BlockFadeEvent) event;
                var fadeBlock = castedFade.getBlock();
                request.setEvent(patchbukkit.events.Event.newBuilder().setBlockFade(
                    patchbukkit.events.BlockFadeEvent.newBuilder()
                        .setBlockX(fadeBlock.getX())
                        .setBlockY(fadeBlock.getY())
                        .setBlockZ(fadeBlock.getZ())
                        .setNewBlockType(castedFade.getNewState().getType().name()).build()
                ).build());
                break;
            case "org.bukkit.event.server.ServerCommandEvent":
                var castedCmd = (org.bukkit.event.server.ServerCommandEvent) event;
                request.setEvent(
                    patchbukkit.events.Event.newBuilder().setServerCommand(
                        patchbukkit.events.ServerCommandEvent.newBuilder()
                            .setSenderName(castedCmd.getSender().getName())
                            .setCommand(castedCmd.getCommand()).build()
                    ).build()
                );
                break;
            case "org.bukkit.event.server.PluginDisableEvent":
                var castedDisable = (org.bukkit.event.server.PluginDisableEvent) event;
                request.setEvent(
                    patchbukkit.events.Event.newBuilder().setPluginDisable(
                        PluginDisableEvent.newBuilder()
                            .setPluginName(castedDisable.getPlugin().getName()).build()
                    ).build()
                );
                break;
            case "org.bukkit.event.block.BlockBreakEvent":
                var castedBreak = (org.bukkit.event.block.BlockBreakEvent) event;
                var breakBlock = castedBreak.getBlock();
                var bBuilder = BlockBreakEvent.newBuilder()
                    .setBlockX(breakBlock.getX())
                    .setBlockY(breakBlock.getY())
                    .setBlockZ(breakBlock.getZ())
                    .setBlockType(breakBlock.getType().name())
                    .setExp(castedBreak.getExpToDrop())
                    .setDropItems(castedBreak.isDropItems());
                if (castedBreak.getPlayer() != null) {
                    bBuilder.setPlayerUuid(BridgeUtils.convertUuid(castedBreak.getPlayer().getUniqueId()));
                }
                request.setEvent(patchbukkit.events.Event.newBuilder().setBlockBreak(bBuilder.build()).build());
                break;
            case "org.bukkit.event.block.BlockPlaceEvent":
                var castedPlace = (org.bukkit.event.block.BlockPlaceEvent) event;
                var placedBlock = castedPlace.getBlockPlaced();
                var againstBlock = castedPlace.getBlockAgainst();
                request.setEvent(
                    patchbukkit.events.Event.newBuilder().setBlockPlace(
                        BlockPlaceEvent.newBuilder()
                            .setBlockX(placedBlock.getX())
                            .setBlockY(placedBlock.getY())
                            .setBlockZ(placedBlock.getZ())
                            .setBlockPlacedType(placedBlock.getType().name())
                            .setBlockAgainstType(againstBlock.getType().name())
                            .setCanBuild(castedPlace.canBuild())
                            .setPlayerUuid(BridgeUtils.convertUuid(castedPlace.getPlayer().getUniqueId())).build()
                    ).build()
                );
                break;
            case "org.bukkit.event.player.PlayerToggleSneakEvent":
                var castedSneak = (org.bukkit.event.player.PlayerToggleSneakEvent) event;
                request.setEvent(
                    patchbukkit.events.Event.newBuilder().setPlayerToggleSneak(
                        PlayerToggleSneakEvent.newBuilder()
                            .setIsSneaking(castedSneak.isSneaking())
                            .setPlayerUuid(BridgeUtils.convertUuid(castedSneak.getPlayer().getUniqueId())).build()
                    ).build()
                );
                break;
            case "org.bukkit.event.player.PlayerToggleSprintEvent":
                var castedSprint = (org.bukkit.event.player.PlayerToggleSprintEvent) event;
                request.setEvent(
                    patchbukkit.events.Event.newBuilder().setPlayerToggleSprint(
                        PlayerToggleSprintEvent.newBuilder()
                            .setIsSprinting(castedSprint.isSprinting())
                            .setPlayerUuid(BridgeUtils.convertUuid(castedSprint.getPlayer().getUniqueId())).build()
                    ).build()
                );
                break;
            case "org.bukkit.event.player.PlayerToggleFlightEvent":
                var castedFlight = (org.bukkit.event.player.PlayerToggleFlightEvent) event;
                request.setEvent(
                    patchbukkit.events.Event.newBuilder().setPlayerToggleFlight(
                        PlayerToggleFlightEvent.newBuilder()
                            .setIsFlying(castedFlight.isFlying())
                            .setPlayerUuid(BridgeUtils.convertUuid(castedFlight.getPlayer().getUniqueId())).build()
                    ).build()
                );
                break;
            case "org.bukkit.event.player.PlayerMoveEvent":
                var castedMove = (org.bukkit.event.player.PlayerMoveEvent) event;
                request.setEvent(
                    patchbukkit.events.Event.newBuilder().setPlayerMove(
                        PlayerMoveEvent.newBuilder()
                            .setPlayerUuid(BridgeUtils.convertUuid(castedMove.getPlayer().getUniqueId()))
                            .setFrom(BridgeUtils.convertLocation(castedMove.getFrom()))
                            .setTo(BridgeUtils.convertLocation(castedMove.getTo())).build()
                    ).build()
                );
                break;
            case "org.bukkit.event.player.PlayerInteractEntityEvent":
            case "org.bukkit.event.player.PlayerInteractAtEntityEvent":
                var castedInteractEntity = (org.bukkit.event.player.PlayerInteractEntityEvent) event;
                request.setEvent(
                    patchbukkit.events.Event.newBuilder().setPlayerInteractEntity(
                        PlayerInteractEntityEvent.newBuilder()
                            .setPlayerUuid(BridgeUtils.convertUuid(castedInteractEntity.getPlayer().getUniqueId()))
                            .setTargetUuid(BridgeUtils.convertUuid(castedInteractEntity.getRightClicked().getUniqueId()))
                            .setAction("INTERACT")
                            .setIsSneaking(castedInteractEntity.getPlayer().isSneaking())
                            .build()
                    ).build()
                );
                break;
            case "org.bukkit.event.entity.EntityDamageByEntityEvent":
                var castedDamageByEntity = (org.bukkit.event.entity.EntityDamageByEntityEvent) event;
                if (castedDamageByEntity.getDamager() instanceof org.bukkit.entity.Player damagerPlayer) {
                    request.setEvent(
                        patchbukkit.events.Event.newBuilder().setPlayerInteractEntity(
                            PlayerInteractEntityEvent.newBuilder()
                                .setPlayerUuid(BridgeUtils.convertUuid(damagerPlayer.getUniqueId()))
                                .setTargetUuid(BridgeUtils.convertUuid(castedDamageByEntity.getEntity().getUniqueId()))
                                .setAction("ATTACK")
                                .setIsSneaking(damagerPlayer.isSneaking())
                                .build()
                        ).build()
                    );
                }
                break;
            case "org.bukkit.event.player.AsyncPlayerChatEvent":
            case "org.bukkit.event.player.PlayerChatEvent":
                var castedChat = (org.bukkit.event.player.AsyncPlayerChatEvent) event;
                request.setEvent(
                    patchbukkit.events.Event.newBuilder().setPlayerChat(
                        PlayerChatEvent.newBuilder()
                            .setMessage(castedChat.getMessage())
                            .setFormat(castedChat.getFormat())
                            .setPlayerUuid(BridgeUtils.convertUuid(castedChat.getPlayer().getUniqueId())).build()
                    ).build()
                );
                break;
        }
        var response = NativeBridgeFfi.callEvent(request.build());

        boolean handledByPumpkin;
        if (response == null) handledByPumpkin = false;
        else handledByPumpkin = response.getHandled();

        if (!handledByPumpkin) {
            // Pumpkin doesn't know this event type, dispatch Java-only
            callEventJavaOnly(event);
        }
    }

    /**
    * Java-only event dispatch for events that don't have Pumpkin equivalents.
    * Used for custom plugin events or unsupported Bukkit events.
    */
    private void callEventJavaOnly(@NotNull Event event) {
        HandlerList handlers = event.getHandlers();
        RegisteredListener[] listeners = handlers.getRegisteredListeners();

        for (RegisteredListener registration : listeners) {
            if (!registration.getPlugin().isEnabled()) {
                continue;
            }

            try {
                registration.callEvent(event);
            } catch (AuthorNagException ex) {
                Plugin plugin = registration.getPlugin();

                if (plugin.isNaggable()) {
                    plugin.setNaggable(false);

                    this.server.getLogger().log(Level.SEVERE, String.format(
                        "Nag author(s): '%s' of '%s' about the following: %s",
                        plugin.getPluginMeta().getAuthors(),
                        plugin.getPluginMeta().getDisplayName(),
                        ex.getMessage()
                    ));
                }
            } catch (Throwable ex) {
                this.server.getLogger().log(
                    Level.SEVERE,
                    "Could not pass event " + event.getEventName()
                        + " to " + registration.getPlugin().getPluginMeta().getDisplayName(),
                    ex
                );
                ex.printStackTrace();
            }
        }
    }

    /**
     * Called from Rust (via j4rs) when a Pumpkin event fires for a specific plugin.
     *
     * Iterates PatchBukkitEvent's HandlerList, filters to the target plugin
     * by name, and invokes its executors. Cancellation state is set on the
     * event and read back by Rust after this returns.
     *
     * @param event      The PatchBukkitEvent populated by Rust
     * @param pluginName The plugin whose handlers should execute
     */
    public void fireEvent(@NotNull Event event, @NotNull String pluginName) {
        for (RegisteredListener listener : event.getHandlers().getRegisteredListeners()) {
            if (!listener.getPlugin().getName().equals(pluginName)) continue;
            if (!listener.getPlugin().isEnabled()) continue;

            try {
                listener.callEvent(event);
            } catch (Throwable ex) {
                this.server.getLogger().log(
                    Level.SEVERE,
                    "Could not pass event " + event.getEventName()
                        + " to " + listener.getPlugin().getPluginMeta().getDisplayName(),
                    ex
                );
                ex.printStackTrace();
            }
        }
    }

    public void registerEvents(@NotNull Listener listener, @NotNull Plugin plugin) {
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register " + listener + " while not enabled");
        }

        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : this.createRegisteredListeners(listener, plugin).entrySet()) {
            this.getEventListeners(this.getRegistrationClass(entry.getKey())).registerAll(entry.getValue());

            for (RegisteredListener rl : entry.getValue()) {
                int priorityOrdinal = Math.min(rl.getPriority().ordinal(), 4);
                var request = RegisterEventRequest.newBuilder().setEventType(entry.getKey().getName()).setPluginName(plugin.getName()).setPriority(priorityOrdinal).setBlocking(true).build();
                NativeBridgeFfi.registerEvent(request);
            }
        }
    }

    public void registerEvent(@NotNull Class<? extends Event> event, @NotNull Listener listener, @NotNull EventPriority priority, @NotNull EventExecutor executor, @NotNull Plugin plugin) {
        this.registerEvent(event, listener, priority, executor, plugin, false);
    }

    public void registerEvent(@NotNull Class<? extends Event> event, @NotNull Listener listener, @NotNull EventPriority priority, @NotNull EventExecutor executor, @NotNull Plugin plugin, boolean ignoreCancelled) {
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register " + event + " while not enabled");
        }

        executor = new TimedEventExecutor(executor, plugin, null, event);
        this.getEventListeners(event).register(new RegisteredListener(listener, executor, priority, plugin, ignoreCancelled));

        int priorityOrdinal = Math.min(priority.ordinal(), 4);
        var request = RegisterEventRequest.newBuilder().setEventType(event.getName()).setPluginName(plugin.getName()).setPriority(priorityOrdinal).setBlocking(true).build();
        NativeBridgeFfi.registerEvent(request);

    }

    @NotNull
    private HandlerList getEventListeners(@NotNull Class<? extends Event> type) {
        try {
            Method method = this.getRegistrationClass(type).getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (HandlerList) method.invoke(null);
        } catch (Exception e) {
            throw new IllegalPluginAccessException(e.toString());
        }
    }

    @NotNull
    private Class<? extends Event> getRegistrationClass(@NotNull Class<? extends Event> clazz) {
        try {
            clazz.getDeclaredMethod("getHandlerList");
            return clazz;
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null
                && !clazz.getSuperclass().equals(Event.class)
                && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return this.getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));
            } else {
                throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName() + ". Static getHandlerList method required!");
            }
        }
    }

    @NotNull
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(@NotNull Listener listener, @NotNull final Plugin plugin) {
        Map<Class<? extends Event>, Set<RegisteredListener>> ret = new HashMap<>();

        Set<Method> methods;
        try {
            Class<?> listenerClazz = listener.getClass();
            methods = Sets.union(
                Set.of(listenerClazz.getMethods()),
                Set.of(listenerClazz.getDeclaredMethods())
            );
        } catch (NoClassDefFoundError e) {
            plugin.getLogger().severe("Failed to register events for " + listener.getClass() + " because " + e.getMessage() + " does not exist.");
            return ret;
        }

        for (final Method method : methods) {
            final EventHandler eh = method.getAnnotation(EventHandler.class);
            if (eh == null) continue;
            // Do not register bridge or synthetic methods to avoid event duplication
            // Fixes SPIGOT-893
            if (method.isBridge() || method.isSynthetic()) {
                continue;
            }
            final Class<?> checkClass;
            if (method.getParameterTypes().length != 1 || !Event.class.isAssignableFrom(checkClass = method.getParameterTypes()[0])) {
                plugin.getLogger().severe(plugin.getPluginMeta().getDisplayName() + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.getClass());
                continue;
            }
            final Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
            method.setAccessible(true);
            Set<RegisteredListener> eventSet = ret.computeIfAbsent(eventClass, k -> new HashSet<>());

            for (Class<?> clazz = eventClass; Event.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass()) {
                // This loop checks for extending deprecated events
                if (clazz.getAnnotation(Deprecated.class) != null) {
                    Warning warning = clazz.getAnnotation(Warning.class);
                    Warning.WarningState warningState = this.server.getWarningState();
                    if (!warningState.printFor(warning)) {
                        break;
                    }
                    plugin.getLogger().log(
                        Level.WARNING,
                        String.format(
                            "\"%s\" has registered a listener for %s on method \"%s\", but the event is Deprecated. \"%s\"; please notify the authors %s.",
                            plugin.getPluginMeta().getDisplayName(),
                            clazz.getName(),
                            method.toGenericString(),
                            (warning != null && warning.reason().length() != 0) ? warning.reason() : "Server performance will be affected",
                            Arrays.toString(plugin.getPluginMeta().getAuthors().toArray())),
                        warningState == Warning.WarningState.ON ? new AuthorNagException(null) : null);
                    break;
                }
            }

            EventExecutor executor = new TimedEventExecutor(EventExecutor.create(method, eventClass), plugin, method, eventClass);
            eventSet.add(new RegisteredListener(listener, executor, eh.priority(), plugin, eh.ignoreCancelled()));
        }
        return ret;
    }

    public void clearEvents() {
        HandlerList.unregisterAll();
    }
}
