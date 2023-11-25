package me.phoenixstyle.parkour.core.plane;

import me.phoenixstyle.parkour.core.Parkour;
import org.bukkit.Bukkit;

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

            for(Player player : players.values()) {
                player.updatePreviousLocation();
            }
        }, 0L, 1L);
    }

    public void checkAllCollisions() {
        PlaneManager planeManager = Parkour.getInstance().planeManager;
        HashMap<UUID, ArrayList<Plane>> planes = planeManager.getPlanes();

        for(Player player : players.values()) {
            if(!planes.containsKey(player.getPlayer().getWorld().getUID())) {
                continue;
            }


            for(Plane plane : planes.get(player.getPlayer().getWorld().getUID())) {

                double t = plane.collide(player, Plane.CollisionType.VERTICES);

                if(!Double.isNaN(t)) {
                    player.getPlayer().sendMessage(String.format("-> §eT: %.4f --------\n", t));
                }
            }
        }
    }
}
