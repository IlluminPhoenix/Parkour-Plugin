package me.phoenixstyle.parkour.core;
import me.phoenixstyle.parkour.core.plane.PlaneManager;
import me.phoenixstyle.parkour.core.plane.PlayerManager;
import me.phoenixstyle.parkour.sqlite_database.Database;
import me.phoenixstyle.parkour.utility.Hologram;
import me.phoenixstyle.parkour.utility.tick_time;
import me.phoenixstyle.parkour.utility.Utility;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;


import javax.xml.crypto.Data;
import java.util.*;
import java.util.stream.Stream;


public final class Parkour extends JavaPlugin implements Listener {
    private HashMap<Player, Double> pk_times;
    private HashMap<Player, Double> pressure_plate_check;
    private HashMap<Location, ParkourBlock> parkour_blocks;
    private long tickTime;
    private World mainWorld;


    private BukkitScheduler scheduler;
    private static Parkour instance;
    private CommandPark commandPark;
    private Database database;
    public PlaneManager planeManager;
    public PlayerManager playerManager;
    public static Parkour getInstance() {
        return Parkour.instance;
    }


    @Override
    public void onEnable() {
        Parkour.instance = this;
        database = new Database(this.getDataFolder().getAbsolutePath(), "data.db");
        scheduler = Bukkit.getScheduler();
        commandPark = new CommandPark();
        planeManager = new PlaneManager();
        playerManager = new PlayerManager();

        Hologram.instantiate();
        ParkourItems.initiate();

        Objects.requireNonNull(this.getCommand("park")).setExecutor(commandPark);
        Objects.requireNonNull(this.getCommand("park")).setTabCompleter(commandPark);

        getServer().broadcastMessage("Reload");
        getServer().getPluginManager().registerEvents(this, this);

        loadSavedData();


        Optional<World> first = getServer().getWorlds().stream().findFirst();
        first.ifPresent(world -> {
            mainWorld = world;
            tickTime = world.getGameTime();
        });

        scheduler.runTaskTimer(instance, () -> {
            updateTimes();
            //sendDebugMessage("§3TICK: " + tickTime);

            for( HashMap.Entry<Player, Double> p : pressure_plate_check.entrySet()) {
                pressurePlateCheck(p);
            }
            //getServer().broadcastMessage("Remove if!");
            pressure_plate_check.entrySet().removeIf((x) -> {
                //getServer().broadcastMessage("Y: " + x.getKey().getLocation().getY() + " - " + x.getValue() + " = " + (x.getKey().getLocation().getY() <= x.getValue()));
                return x.getKey().getLocation().getY() <= x.getValue();
            });
        }, 0L, 1L);
    }


    @Override
    public void onDisable() {
        database.close();
        if(parkour_blocks == null || parkour_blocks.isEmpty()) {
            return;
        }
        parkour_blocks.forEach((x, y) -> {
            y.removeHologram();
        });
    }

    @EventHandler
    public void interactEvent(PlayerInteractEvent event) {
        ParkourItems.interactEvent(event);

    }

    @EventHandler
    public void playerFly(PlayerToggleFlightEvent event) {
        commandPark.playerFly(event);
        if(!event.isCancelled()) {
            parkourStop(event.getPlayer(), "Do not fly");
        }

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
        ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();
        //ItemMeta meta = stack.getItemMeta();
        //assert meta != null;
        //PersistentDataContainer container = meta.getPersistentDataContainer();
        Block block = event.getBlockPlaced();

        ParkourBlockType type = ParkourBlockType.NONE;
        if(Utility.hasDataKey(stack, new NamespacedKey(instance, "start"), PersistentDataType.BOOLEAN)) {
            type = ParkourBlockType.START;
        }
        else if(Utility.hasDataKey(stack, new NamespacedKey(instance, "end"), PersistentDataType.BOOLEAN)) {
            type = ParkourBlockType.END;
        }

        if(block.getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE &&
            type != ParkourBlockType.NONE) {
            String name = "";

            ParkourBlock pkBlock = new ParkourBlock(block.getLocation(), type, event.getPlayer());
            pkBlock.writeToDatabase();
            parkour_blocks.put(block.getLocation(), pkBlock);
            //getServer().broadcastMessage("Type: " + type);
        }

    }

    @EventHandler
    public void breakBlock(BlockBreakEvent event) {
        if(parkour_blocks.containsKey(event.getBlock().getLocation())) {
            removeParkourBlock(parkour_blocks.get(event.getBlock().getLocation()), event.getPlayer());
        }
    }

    @EventHandler
    public void entityLoad(EntitiesLoadEvent event) {
        Hologram.loadEntities(event.getEntities());
    }

    private void loadSavedData() {
        pk_times = new HashMap<>();
        pressure_plate_check = new HashMap<>();
        parkour_blocks = new HashMap<>();

        Optional<ArrayList<ParkourBlock>> optionalParkourBlocks = database.readPkBlocks();

        if(optionalParkourBlocks.isPresent()) {
            for(ParkourBlock pkBlock : optionalParkourBlocks.get()) {
                //System.out.println("PK loaded");
                parkour_blocks.put(pkBlock.location.getBlock().getLocation(), pkBlock);
            }
        }
    }


    //Checks the surrounding are for valid PK blocks
    private ParkourBlockType checkSingleLocation(Location precise_loc) {
        Block block = precise_loc.getBlock();
        Location loc = block.getLocation();
        if(parkour_blocks.containsKey(loc)) {
            ParkourBlockType type = parkour_blocks.get(loc).type;
            if(type != ParkourBlockType.NONE && block.getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
                return type;
            }
            removeParkourBlock(parkour_blocks.get(loc));

        }
        return ParkourBlockType.NONE;
    }

    private void removeParkourBlock(ParkourBlock block) {
        //getServer().broadcastMessage("Removed: " + block.type);
        parkour_blocks.remove(block.location);
        block.remove();
    }

    private void removeParkourBlock(ParkourBlock block, Player player) {
        //getServer().broadcastMessage("Removed: " + block.type);
        parkour_blocks.remove(block.location);
        block.remove(player);
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
                        parkourStart(player, 0, false);
                    }
                    else if(type == ParkourBlockType.END) {
                        parkourFinish(player, 0);
                    }
                    break;
                }
            }
        }

    }

    public void parkourStart(Player player, double offset, boolean mute) {
        //sendDebugMessage("Offset Start: " + offset);
        updateTimes();
        if(pk_times.containsKey(player)) {
            if(!mute) {
                player.sendMessage("§aReset your timer to 00:00! Get to the finish line!");
            }

        }
        else {
            if(!mute) {
                player.sendMessage("§aParkour challenge started!");
            }

        }

        pk_times.put(player, offset);
    }

    public void failParkour(Player player, String reason) {
        parkourStop(player, reason);
    }

    private void parkourStop(Player player, String reason) {
        if(pk_times.containsKey(player)) {
            player.sendMessage("§c§lParkour challenge failed! " + reason + "!");
        }
        pk_times.remove(player);
    }

    public void parkourFinish(Player player, double offset) {
        //sendDebugMessage("Offset Finish: " + offset);
        updateTimes();
        if(pk_times.containsKey(player)) {
            pk_times.put(player, pk_times.get(player) + offset);
            if (pk_times.get(player) < 0) {
                return;
            }
            getServer().broadcastMessage("§b" + player.getDisplayName() + "§a completed the parkour in §e§l"
                    + new tick_time(pk_times.get(player)).to_string() + "!");
            player.sendMessage("§aUnfortunately you did not break any of the records for this parkour!");
            pk_times.remove(player);
        }
        else {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 0.5F);
            player.sendMessage("§cYou need to go to the start first!");
        }

    }

    public static class ParkourBlock {
        public ParkourBlockType type;
        public Hologram hologram;
        public Location location;

        private String name;

        public ParkourBlock(Location location, ParkourBlockType type, Player player) {
            switch (type) {
                case START:
                    name = "§aParkour Start";
                    player.sendMessage("§aAdded start to the parkour challenge!");
                    break;

                case END:
                    name = "§aParkour End";
                    player.sendMessage("§aAdded end to the parkour challenge!");
                    break;
            }
            this.location = location;
            this.hologram = new Hologram(location.add(0.5, 0.5, 0.5), name);
            this.type = type;

        }


        public ParkourBlock(Location location, ParkourBlockType type) {
            switch (type) {
                case START:
                    name = "§aParkour Start";
                    break;

                case END:
                    name = "§aParkour End";
                    break;
            }
            this.location = location;
            this.hologram = new Hologram(location.add(0.5, 0.5, 0.5), name);
            this.type = type;
        }

        public void writeToDatabase() {
            Parkour.getInstance().database.modifyPkBlocks(this, Database.Action.WRITE);
        }

        public void remove(Player player) {
            switch (type) {
                case START:
                    player.sendMessage("§aRemoved start from the parkour challenge!");
                    break;

                case END:
                    player.sendMessage("§aRemoved end to from parkour challenge!");
                    break;
            }
            hologram.remove();
            Parkour.getInstance().database.modifyPkBlocks(this, Database.Action.REMOVE);
        }

        public void remove() {
            hologram.remove();
        }

        public void removeHologram() {
            hologram.remove();
        }

        public void changeName(String name) {
            this.name = name;
            hologram.changeName(name);
        }
    }

    public enum ParkourBlockType {
        NONE,
        START,
        END,
        CHECKPOINT;

        public int toInt() {
            switch (this) {
                case NONE:
                    return 0;
                case START:
                    return 1;
                case END:
                    return 2;
                case CHECKPOINT:
                    return 3;
            }
            return 0;
        }

        public static ParkourBlockType fromInt(int x) {
            switch (x) {
                case 0:
                    return NONE;
                case 1:
                    return START;
                case 2:
                    return END;
                case 3:
                    return CHECKPOINT;
            }
            return NONE;
        }
    }


    public void sendDebugMessage(String s) {
        getServer().broadcastMessage(s);
    }

    public void updateTimes() {
        if(pk_times.isEmpty() && mainWorld == null) {
            return;
        }

        if(mainWorld == null) {
            mainWorld = pk_times.keySet().stream().findFirst().get().getWorld();
            tickTime = mainWorld.getGameTime();
        }

        //sendDebugMessage("Update: " + tickTime + " - " + mainWorld.getGameTime());

        if(mainWorld.getGameTime() != tickTime) {
            //sendDebugMessage("Iterate");
            parkourIterateTimes();
            tickTime = mainWorld.getGameTime();
        }
        else {
            //sendDebugMessage("Double");
        }

    }

    public Database getDatabase() {
        return database;
    }
}


