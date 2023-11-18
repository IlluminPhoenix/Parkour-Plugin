package me.phoenixstyle.parkour.core;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CommandPark implements CommandExecutor {

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack start = new ItemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
            ItemMeta start_meta = start.getItemMeta();
            assert start_meta != null;
            start_meta.setDisplayName("§aStart Block");
            start_meta.getPersistentDataContainer().set(new NamespacedKey(Parkour.getInstance(), "start"),
                    PersistentDataType.BOOLEAN, true);
            start.setItemMeta(start_meta);

            ItemStack end = new ItemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
            ItemMeta end_meta = end.getItemMeta();
            end_meta.setDisplayName("§cEnd Block");
            end_meta.getPersistentDataContainer().set(new NamespacedKey(Parkour.getInstance(), "end"),
                    PersistentDataType.BOOLEAN, true);
            end.setItemMeta(end_meta);

            player.getInventory().addItem(start, end);
        }


        // If the player (or console) uses our command correct, we can return true
        return true;
    }
}