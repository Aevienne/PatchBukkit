package org.patchbukkit.inventory;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PatchBukkitItemFactory {
    public static final ItemFactory INSTANCE = createFactory();

    private static ItemFactory createFactory() {
        return (ItemFactory) Proxy.newProxyInstance(
            ItemFactory.class.getClassLoader(),
            new Class<?>[] { ItemFactory.class },
            (proxy, method, args) -> {
                String name = method.getName();
                if ("getItemMeta".equals(name) || "createItemMeta".equals(name) || "asMetaFor".equals(name)) {
                    if (args != null && args.length > 0 && args[0] instanceof Material mat && mat == Material.AIR) {
                        return null;
                    }
                    return createMeta();
                }
                if ("isApplicable".equals(name)) {
                    return true;
                }
                if ("equals".equals(name)) {
                    if (args != null && args.length == 2) {
                        return Objects.equals(args[0], args[1]);
                    }
                    return false;
                }
                if ("ensureServerConform".equals(name)) {
                    if (args != null && args.length > 0 && args[0] instanceof ItemStack stack) {
                        if (stack instanceof PatchBukkitItemStack) {
                            return stack;
                        }
                        try {
                            return new PatchBukkitItemStack(stack.getType(), stack.getAmount());
                        } catch (Throwable ignored) {
                            return new PatchBukkitItemStack(Material.AIR, 0);
                        }
                    }
                    return new PatchBukkitItemStack(Material.AIR, 0);
                }
                if ("isItemEmpty".equals(name)) {
                    if (args != null && args.length > 0 && args[0] instanceof ItemStack stack) {
                        return PatchBukkitPlayerInventory.isItemEmpty(stack);
                    }
                    return true;
                }
                Class<?> returnType = method.getReturnType();
                if (returnType == boolean.class) return false;
                if (returnType == int.class) return 0;
                if (returnType == long.class) return 0L;
                if (returnType == double.class || returnType == float.class) return 0.0;
                return null;
            }
        );
    }

    public static ItemMeta createMeta() {
        Map<String, Object> state = new HashMap<>();
        return (ItemMeta) Proxy.newProxyInstance(
            ItemMeta.class.getClassLoader(),
            new Class<?>[] { ItemMeta.class },
            (proxy, method, args) -> {
                String name = method.getName();
                if ("hasDisplayName".equals(name)) return state.containsKey("displayName");
                if ("getDisplayName".equals(name)) return state.get("displayName");
                if ("setDisplayName".equals(name)) {
                    if (args != null && args.length > 0 && args[0] != null) state.put("displayName", args[0]);
                    else state.remove("displayName");
                    return null;
                }
                if ("hasLore".equals(name)) return state.containsKey("lore");
                if ("getLore".equals(name)) return state.get("lore");
                if ("setLore".equals(name)) {
                    if (args != null && args.length > 0 && args[0] != null) state.put("lore", args[0]);
                    else state.remove("lore");
                    return null;
                }
                if ("clone".equals(name)) return createMeta();
                if ("equals".equals(name)) {
                    if (args != null && args.length > 0) return proxy == args[0];
                    return false;
                }
                Class<?> returnType = method.getReturnType();
                if (returnType == boolean.class) return false;
                if (returnType == int.class) return 0;
                if (returnType == long.class) return 0L;
                if (returnType == double.class || returnType == float.class) return 0.0;
                return null;
            }
        );
    }
}
