package me.phoenixstyle.parkour.utility;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class Utility {
    public static <T, Z> ItemStack setDataKey(ItemStack stack, NamespacedKey key, PersistentDataType<T, Z> type, Z value, OverrideType override) {
        ItemMeta meta = stack.getItemMeta();
        assert meta != null;
        PersistentDataContainer container = meta.getPersistentDataContainer();

        boolean write = false;
        if(container.has(key, type)) {
            if(override == OverrideType.OVERRIDE || override == OverrideType.OVERRIDE_AND_CREATE) {
                write = true;
            }
        }
        else {
            if(override == OverrideType.OVERRIDE_AND_CREATE || override == OverrideType.LEAVE_AND_CREATE) {
                write = true;
            }
        }

        if(write) {
            container.set(key, type, value);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static <T, Z> ItemStack setDataKey(ItemStack stack, NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        ItemMeta meta = stack.getItemMeta();
        assert meta != null;
        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(key, type, value);
        stack.setItemMeta(meta);
        return stack;
    }

    public static <T, Z> Z getDataKey(ItemStack stack, NamespacedKey key, PersistentDataType<T, Z> type) {
         return Objects.requireNonNull(stack.getItemMeta()).getPersistentDataContainer().get(key, type);
    }

    public static <T, Z> boolean hasDataKey(ItemStack stack, NamespacedKey key, PersistentDataType<T, Z> type) {
        return Objects.requireNonNull(stack.getItemMeta()).getPersistentDataContainer().has(key, type);
    }

    public enum OverrideType {
        OVERRIDE,
        OVERRIDE_AND_CREATE,
        LEAVE, //LEAVE is such a just a joke, I'm leaving it though (Pun intended)
        LEAVE_AND_CREATE,
    }
}
