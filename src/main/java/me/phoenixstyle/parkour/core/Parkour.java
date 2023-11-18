package me.phoenixstyle.parkour.core;
import me.phoenixstyle.parkour.utility.tick_time;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.*;


public final class Parkour extends JavaPlugin implements Listener {
    private HashMap<Player, Integer> pk_times;
    private HashMap<Player, Double> pressure_plate_check;
    private HashMap<Location, ParkourBlockType> parkour_blocks;


    private BukkitScheduler scheduler;
    private static Parkour instance;
    public static Parkour getInstance() {
        return Parkour.instance;
    }


    @Override
    public void onEnable() {
        Objects.requireNonNull(this.getCommand("park")).setExecutor(new CommandPark());
        Parkour.instance = this;
        getServer().broadcastMessage("Reload");
        getServer().getPluginManager().registerEvents(this, this);
        scheduler = Bukkit.getScheduler();

        pk_times = new HashMap<>();
        pressure_plate_check = new HashMap<>();
        parkour_blocks = new HashMap<>();

        scheduler.runTaskTimer(instance, () -> {

            for( HashMap.Entry<Player, Double> p : pressure_plate_check.entrySet()) {
                pressurePlateCheck(p);
            }
            //getServer().broadcastMessage("Remove if!");
            pressure_plate_check.entrySet().removeIf((x) -> {
                //getServer().broadcastMessage("Y: " + x.getKey().getLocation().getY() + " - " + x.getValue() + " = " + (x.getKey().getLocation().getY() <= x.getValue()));
                return x.getKey().getLocation().getY() <= x.getValue();
            });
            parkourIterateTimes();
        }, 0L, 1L);
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void playerFly(PlayerToggleFlightEvent event) {
        parkourStop(event.getPlayer());
    }

    @EventHandler
    public void pressurePlate(PlayerInteractEvent event) {
        if(event.getAction() == Action.PHYSICAL) {
            //getServer().broadcastMessage("Check Start: " + pressure_plate_check.size());
            pressure_plate_check.put(event.getPlayer(), Math.floor(event.getPlayer().getLocation().getY()));

            //pressurePlateCheck(pair);
        }
    }

    @EventHandler
    public void placeBlock(BlockPlaceEvent event) {
        ItemMeta meta = event.getPlayer().getInventory().getItemInMainHand().getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Block block = event.getBlockPlaced();

        ParkourBlockType type = ParkourBlockType.NONE;
        if(container.has(new NamespacedKey(instance, "start"), PersistentDataType.BOOLEAN)) {
            type = ParkourBlockType.START;
        }
        else if(container.has(new NamespacedKey(instance, "end"), PersistentDataType.BOOLEAN)) {
            type = ParkourBlockType.END;
        }

        if(block.getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE &&
            type != ParkourBlockType.NONE) {
            parkour_blocks.put(block.getLocation(), type);
            getServer().broadcastMessage("Type: " + type);
        }

    }

    //Checks the surrounding are for valid PK blocks
    private ParkourBlockType checkSingleLocation(Location precise_loc) {
        Block block = precise_loc.getBlock();
        Location loc = block.getLocation();
        if(parkour_blocks.containsKey(loc)) {
            ParkourBlockType type = parkour_blocks.get(loc);
            if(type != ParkourBlockType.NONE && block.getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
                return type;
            }
            getServer().broadcastMessage("Removed: " + type);
            parkour_blocks.remove(loc);
        }
        return ParkourBlockType.NONE;
    }

    private void parkourIterateTimes() {
        pk_times.forEach((x, y) -> {
            pk_times.put(x, y + 1);
        });
    }

    //Checks for a pressure plate, then calls appropriate Method, called if Player just caused a physical action
    private void pressurePlateCheck(HashMap.Entry<Player, Double> pair) {

        Player player = pair.getKey();

        for(int i = 0; i < 4; i++) {
            int x = i / 2;
            int y = i % 2;

            Location point = player.getLocation();
            point.add(new Vector((double)x * 0.6 - 0.3, 0, (double)y * 0.6 - 0.3));

            Location block = point.getBlock().getLocation().add(0.5, 0, 0.5);

            block.subtract(point);
            ParkourBlockType type = checkSingleLocation(point);
            if(type != ParkourBlockType.NONE) {
                if(Math.abs(block.getX()) < 0.4375 && Math.abs(block.getZ()) < 0.4375 && point.getY() == pair.getValue()) {


                    if(type == ParkourBlockType.START) {
                        parkourStart(player);
                    }
                    else if(type == ParkourBlockType.END) {
                        parkourFinish(player);
                    }
                    break;
                }
            }
        }

    }

    private void parkourStart(Player player) {
        if(pk_times.containsKey(player)) {
            getServer().broadcastMessage("§aReset your timer to 00:00! Get to the finish line!");
        }
        else {
            getServer().broadcastMessage("§aParkour challenge started!");
        }

        pk_times.put(player, 0);
    }

    private void parkourStop(Player player) {
        if(pk_times.containsKey(player)) {
            getServer().broadcastMessage("§c§lFailed Parkour");
        }
        pk_times.remove(player);
    }

    private void parkourFinish(Player player) {
        getServer().broadcastMessage("§b" + player.getDisplayName() + "§a completed the parkour in §e§l"
                + new tick_time(pk_times.get(player)).to_string() + "!");
        getServer().broadcastMessage("§aUnfortunately you did not break any of the records for this parkour!");
        pk_times.remove(player);
    }

    enum ParkourBlockType {
        NONE,
        START,
        END,
        CHECKPOINT
    }
}


