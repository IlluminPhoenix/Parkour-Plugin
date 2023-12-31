package me.phoenixstyle.parkour.core;

import me.phoenixstyle.parkour.utility.Utility;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class ParkourItems {

    //Player UUID
    static HashMap<UUID, Location> saved_cps;

    public static void initiate() {
        saved_cps = new HashMap<>();
    }

    public static void interactEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        assert item != null;
        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(Utility.hasDataKey(item, new NamespacedKey(Parkour.getInstance(), "setcp"), PersistentDataType.BOOLEAN)) {
                setNewCheckpoint(player);
            }
            else if(Utility.hasDataKey(item, new NamespacedKey(Parkour.getInstance(), "restart"), PersistentDataType.BOOLEAN)) {
                resetToCheckpoint(player);
            }
        }
    }

    private static void setNewCheckpoint(Player player) {
        saved_cps.put(player.getUniqueId(), player.getLocation());
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
        player.sendMessage("§eNew checkpoint set!");
    }

    private static void resetToCheckpoint(Player player) {
        player.teleport(saved_cps.get(player.getUniqueId()));
        //player.sendMessage("Reset");
    }
}
