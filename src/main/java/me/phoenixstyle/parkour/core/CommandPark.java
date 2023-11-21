package me.phoenixstyle.parkour.core;

import me.phoenixstyle.parkour.utility.Utility;
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
            Player player = (Player) sender;
            if(args.length < 1) {
                sendErrorMessageResponse(player, full_command.toString());
                return true;
            }

            CommandAction action = parseArgumentAction(0, args[0]);
            if(action == CommandAction.BLOCK) {
                ItemStack start = new ItemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
                ItemMeta start_meta = start.getItemMeta();
                assert start_meta != null;
                start_meta.setDisplayName("§aStart Block");
                start.setItemMeta(start_meta);
                Utility.setDataKey(start, new NamespacedKey(Parkour.getInstance(), "start"), PersistentDataType.BOOLEAN, true);


                ItemStack end = new ItemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
                ItemMeta end_meta = end.getItemMeta();
                assert end_meta != null;
                end_meta.setDisplayName("§cEnd Block");
                end.setItemMeta(end_meta);
                Utility.setDataKey(end, new NamespacedKey(Parkour.getInstance(), "end"), PersistentDataType.BOOLEAN, true);


                player.getInventory().addItem(start, end);
                player.sendMessage("§aGave you parkour blocks!");
            }
            else if(action == CommandAction.CHECKPOINT) {
                ItemStack set = new ItemStack(Material.EMERALD);
                //ItemStack checkpoint = new ItemStack(Material.EMERALD);
                ItemStack restart = new ItemStack(Material.RED_DYE);

                ItemMeta x = set.getItemMeta();
                assert x != null;
                x.setDisplayName("§aSet new checkpoint");
                set.setItemMeta(x);

                x = restart.getItemMeta();
                assert x != null;
                x.setDisplayName("§cReset");
                restart.setItemMeta(x);

                Utility.setDataKey(set, new NamespacedKey(Parkour.getInstance(), "setcp"), PersistentDataType.BOOLEAN, true);
                Utility.setDataKey(restart, new NamespacedKey(Parkour.getInstance(), "restart"), PersistentDataType.BOOLEAN, true);

                player.getInventory().addItem(restart, set);
                player.sendMessage("§aGave you checkpoint items!");
            }
            else {
                sendErrorMessageResponse(player, full_command.toString());
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

    private CommandAction parseArgumentAction(Integer index, String arg) {
        if(index == 0) {
            switch (arg) {
                case "blocks":
                    return CommandAction.BLOCK;

                case "checkpoints":
                    return CommandAction.CHECKPOINT;
            }

        }
        return CommandAction.UNPARSEABLE;
    }

    private void sendErrorMessageResponse(Player caller, String cmd) {
        caller.sendMessage("§cUnknown or incomplete command, see below for error");
        caller.sendMessage("§7" + cmd +"§c§o<--[HERE]");
    }

    enum CommandAction {
        BLOCK,
        CHECKPOINT,
        UNPARSEABLE,
        WRONG_PARSER
    }
}