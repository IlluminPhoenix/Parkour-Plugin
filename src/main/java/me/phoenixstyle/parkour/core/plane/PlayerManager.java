package me.phoenixstyle.parkour.core.plane;

import me.phoenixstyle.parkour.core.Parkour;
import me.phoenixstyle.parkour.utility.Utility;
import org.bukkit.Bukkit;
import org.bukkit.Particle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class PlayerManager {
    HashMap<UUID, Player> players;

    public PlayerManager() {
        players = new HashMap<>();
        for(org.bukkit.entity.Player player : new ArrayList<>(Parkour.getInstance().getServer().getOnlinePlayers())) {
            players.put(player.getUniqueId(), new Player(player));
        };

        Bukkit.getScheduler().runTaskTimer(Parkour.getInstance(), () -> {
            for(Player player : players.values()) {
                player.updateLocations();
            }

            HashSet<org.bukkit.entity.Player> online = new HashSet<>(Parkour.getInstance().getServer().getOnlinePlayers());
            HashSet<UUID> online_uuids = new HashSet<>();

            for(org.bukkit.entity.Player player : online) {
                online_uuids.add(player.getUniqueId());

                if(!players.containsKey(player.getUniqueId())) {
                    Parkour.getInstance().sendDebugMessage("§a" + player.getDisplayName() + " joined!");
                    players.put(player.getUniqueId(), new Player(player));
                }
            };

            for(UUID uuid : players.keySet()) {
                if(!online_uuids.contains(uuid)) {
                    Parkour.getInstance().sendDebugMessage("§a" + players.get(uuid).getPlayer().getDisplayName() + " left!");

                    players.remove(uuid).remove();
                }
            }

            checkAllCollisions();


        }, 0L, 1L);

        Bukkit.getScheduler().runTaskTimer(Parkour.getInstance(), () -> {
            for(Player player : players.values()) {
                player.updateLocations();
                //Utility.renderSphere(player.getTickCentre(),
                //      player.getTickCRadius(), Particle.DRAGON_BREATH, 50);
            }
        }, 0L, 20L);
    }

    public void checkAllCollisions() {
        PlaneManager planeManager = Parkour.getInstance().planeManager;
        HashMap<UUID, ArrayList<Plane>> planes = planeManager.getPlanes();

        for(Player player : players.values()) {
            if(!planes.containsKey(player.getPlayer().getWorld().getUID())) {
                continue;
            }

            for(Plane plane : planes.get(player.getPlayer().getWorld().getUID())) {

                double distance = plane.getCentre().subtract(player.getTickCentre()).length();
                if(distance > plane.getCRadius() + player.getTickCRadius()) {
                    //player.getPlayer().sendMessage("§cOutside of range!");
                    continue;
                }

                double t = plane.collide(player, Plane.CollisionType.VERTICES);

                if(!Double.isNaN(t)) {
                    if(plane.type == Parkour.ParkourBlockType.START) {
                        //player.getPlayer().sendMessage("§a§lT: " + t);
                        Parkour.getInstance().parkourStart(player.getPlayer(), 1 - t, false);
                    }
                    else if(plane.type == Parkour.ParkourBlockType.END) {
                        //player.getPlayer().sendMessage("§c§lT: " + t);
                        Parkour.getInstance().parkourFinish(player.getPlayer(), t - 1);
                    }
                    //player.getPlayer().sendMessage(String.format("-> §eT: %.4f --------\n", t));
                }
            }
        }
    }
}
