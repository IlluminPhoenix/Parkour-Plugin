package me.phoenixstyle.parkour.utility;

import com.google.errorprone.annotations.FormatString;
import me.phoenixstyle.parkour.core.Parkour;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

import java.util.*;

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

    public static Parkour.ParkourBlockType parseParkourBlockType(String s) {
        switch (s) {
            case "start":
                return Parkour.ParkourBlockType.START;

            case "end":
                return Parkour.ParkourBlockType.END;

            case "checkpoint":
                return Parkour.ParkourBlockType.CHECKPOINT;
        }

        return Parkour.ParkourBlockType.NONE;
    }

    public static Vector parseVector(String[] args) {
        if(args.length != 3) {throw new RuntimeException();}

        double[] ds = new double[3];

        for(int i = 0; i < 3; i++) {
            try {
                ds[i] = Double.parseDouble(args[i]);
            }
            catch (Exception e) {
                throw new RuntimeException();
            }
        }

        return new Vector(ds[0], ds[1], ds[2]);
    }

    public static String displayVector(Vector vec) {
        return String.format("%.2f, %.2f, %.2f", vec.getX(), vec.getY(), vec.getZ());
    }

    public static void renderSphere(Location center, double radius, Particle type, int amount) {
        HashSet<Vector> points = new HashSet<>();
        int size = (int)Math.sqrt(amount);
        //Parkour.getInstance().sendDebugMessage(String.format("§aRadius: %.4f", radius));

        double angle = 2 * Math.PI * 1 / (size - 1);

        Vector ivec = new Vector(1, 0, 0);
        for(int i = 0; i < size; i++) {
            Vector jvec = new Vector(0, 1, 0);
            for(int j = 0; j < size; j++) {
                points.add(jvec.clone());
                jvec.rotateAroundNonUnitAxis(ivec, angle / 2);
            }
            ivec.rotateAroundY(angle);
        }

        for(Vector point : points) {
            point.multiply(radius);
            point.add(center.toVector());
            Objects.requireNonNull(center.getWorld()).spawnParticle(type, point.getX(), point.getY(), point.getZ(), 0, 0, 0, 0, 0.25);
        }

    }
}
