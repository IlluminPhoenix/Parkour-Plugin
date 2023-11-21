package me.phoenixstyle.parkour.core;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandPark implements CommandExecutor, TabCompleter {

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        StringBuilder full_command = new StringBuilder();
        full_command.append(label);
        for(String arg : args) {
            full_command.append(" ");
            full_command.append(arg);
        }
        if (sender instanceof Player) {
            if(args.length < 1) {
                sender.sendMessage("§cUnknown or incomplete command, see below for error");
                sender.sendMessage("§7" + full_command +"§c§o<--[Here]");
                return false;
            }
            Player player = (Player) sender;
            if(args[0].equals("blocks")) {
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
                player.sendMessage("§aGave you parkour blocks!");
            }
            else if(args[0].equals("checkpoints")) {

                player.sendMessage("§aGave you checkpoint items!");
            }

        }


        // If the player (or console) uses our command correct, we can return true
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> l = new ArrayList<String>();
        if(command.getName().equalsIgnoreCase("park")) {

            if (args.length == 1) {
                l.add("blocks");
                l.add("checkpoints");
            }

        }

        return l;
    }
}