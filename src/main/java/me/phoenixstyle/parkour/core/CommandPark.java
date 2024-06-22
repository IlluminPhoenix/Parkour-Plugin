package me.phoenixstyle.parkour.core;

import me.phoenixstyle.parkour.core.plane.Plane;
import me.phoenixstyle.parkour.utility.Utility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

public class CommandPark implements CommandExecutor, TabCompleter {

    //Player UUID
    public HashMap<UUID, Boolean> fly_perms;

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

            CommandActionI1 action = parseArgumentAction(0, args[0]);
            if(action == CommandActionI1.BLOCK) {
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
            else if(action == CommandActionI1.CHECKPOINT) {
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
            else if(action == CommandActionI1.FLY) {
                if(fly_perms.containsKey(player.getUniqueId()) && !fly_perms.get(player.getUniqueId())) {
                    player.sendMessage("§aTurned on flight!");
                    fly_perms.remove(player.getUniqueId());
                    player.setAllowFlight(true);
                }
                else {
                    player.sendMessage("§aTurned off flight!");
                    fly_perms.put(player.getUniqueId(), false);
                    player.setAllowFlight(false);

                }
            }
            else if (action == CommandActionI1.PLANE) {
                if(args.length < 2) {
                    sendErrorMessageResponse(player, full_command.toString());
                    return true;
                }

                String name = args[2];
                //Parkour.getInstance().sendDebugMessage(name);

                if(args[1].equalsIgnoreCase("add")) {
                    if(args.length < 13) {
                        sendErrorMessageResponse(player, full_command.toString());
                        return true;
                    }
                    Parkour.ParkourBlockType type = Utility.parseParkourBlockType(args[3]);
                    if(type == Parkour.ParkourBlockType.NONE) {
                        sendErrorMessageResponse(player, full_command.toString());
                        return true;
                    }

                    Location[] vecs = new Location[3];

                    for(int i = 0; i < 3; i++) {
                        String[] arr = {args[i * 3 + 4], args[i * 3 + 5], args[i * 3 + 6]};
                        try{
                            Vector vec = Utility.parseVector(arr);
                            vecs[i] = new Location(player.getWorld(), vec.getX(), vec.getY(), vec.getZ());
                        }
                        catch (Exception e) {
                            sendErrorMessageResponse(player, full_command.toString());
                            return true;
                        }
                    }

                    Plane plane = new Plane(type, vecs[0], vecs[1], vecs[2], name);
                    plane.writeToDatabase();
                    Parkour.getInstance().planeManager.addPlane(plane);
                }
                else if (args[1].equalsIgnoreCase("remove")) {
                    Parkour.getInstance().planeManager.removePlane(name);
                }
                else {
                    sendErrorMessageResponse(player, full_command.toString());
                }

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
                l.add("fly");
                l.add("plane");
            }
            else if(args.length == 2) {
                if(args[0].equalsIgnoreCase("plane")) {
                    l.add("add");
                    l.add("remove");
                }
            }
            else if(args.length == 3) {
                if(args[0].equalsIgnoreCase("plane") && args[1].equals("remove")) {
                    HashMap<String, Plane> planeHashMap = Parkour.getInstance().planeManager.getNamePlaneMap();
                    int i = 0;
                    for(String name : planeHashMap.keySet()) {
                        if(i >= 100) {break;}
                        l.add(name);

                        i++;
                    }
                }
            }
            else if(args.length == 4) {
                if(args[0].equalsIgnoreCase("plane") && args[1].equals("add")) {
                    l.add("start");
                    l.add("end");
                }
            }

        }

        return l;
    }

    private CommandActionI1 parseArgumentAction(Integer index, String arg) {
        if(index == 0) {
            switch (arg) {
                case "blocks":
                    return CommandActionI1.BLOCK;

                case "checkpoints":
                    return CommandActionI1.CHECKPOINT;

                case "fly":
                    return CommandActionI1.FLY;

                case "plane":
                    return CommandActionI1.PLANE;

            }

        }
        return CommandActionI1.UNPARSEABLE;
    }

    private void sendErrorMessageResponse(Player caller, String cmd) {
        caller.sendMessage("§cUnknown or incomplete command, see below for error");
        caller.sendMessage("§7" + cmd +"§c§o<--[HERE]");
    }

    public void playerFly(PlayerToggleFlightEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if(fly_perms.containsKey(uuid) && !fly_perms.get(uuid)) {
            event.setCancelled(true);
        }
    }

    enum CommandActionI1 {
        BLOCK,
        CHECKPOINT,
        FLY,
        PLANE,
        UNPARSEABLE,
        WRONG_PARSER
    }

    public CommandPark() {
        fly_perms = new HashMap<>();
    }
}