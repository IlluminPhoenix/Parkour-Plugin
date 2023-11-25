package me.phoenixstyle.parkour.core.plane;

import me.phoenixstyle.parkour.core.Parkour;
import me.phoenixstyle.parkour.utility.Utility;
import me.phoenixstyle.parkour.utility.Utility.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class PlaneManager {
    private HashMap<UUID, ArrayList<Plane>> planes;
    private HashMap<Player, Location> previousLocation;

    BukkitScheduler scheduler;

    public PlaneManager() {
        this.scheduler = Bukkit.getScheduler();
        planes = new HashMap<>();
        previousLocation = new HashMap<>();
        scheduler.runTaskTimer(Parkour.getInstance(), () -> {
            for(ArrayList<Plane> arrayList : planes.values()) {
                for(Plane plane : arrayList) {
                    plane.visualizePlane(3);
                    //plane.visualizePlaneRadius();
                }
            }
        }, 0, 20);
        /*
        scheduler.runTaskTimer(Parkour.getInstance(), () -> {


            for(ArrayList<Plane> arrayList : planes.values()) {
                for(Plane plane : arrayList) {
                    Player player = Objects.requireNonNull(plane.getPosx().getWorld()).getPlayers().get(0);
                    if(!previousLocation.containsKey(player)) {
                        continue;
                    }
                    Vector la =  previousLocation.get(player).toVector();
                    Vector lb = player.getLocation().toVector();
                    Vector lab =  lb.clone().subtract(la);


                    //player.sendMessage("la: " + Utility.displayVector(la) + "\nlb: " + Utility.displayVector(lb) + "\nlab: " + Utility.displayVector(lab));
                    //player.sendMessage("p0: " + Utility.displayVector(plane.posx.toVector()) + "\np01: " + Utility.displayVector(plane.posy.toVector()) + "\np02: " + Utility.displayVector(plane.posz.toVector()));

                    double t = plane.linePlaneIntersectionT(la, lab);
                    double u = plane.linePlaneIntersectionU(la, lab);
                    double v = plane.linePlaneIntersectionV(la, lab);

                    if(t >= 0 && t <= 1 && u >= 0 && u <= 1 && v >= 0 && v <= 1) {
                        player.sendMessage(String.format("§aT: %.4f", t));
                        player.sendMessage(String.format("§7U: %.4f", u));
                        player.sendMessage(String.format("§7V: %.4f", v));
                    }
                }


            }

            //Major exploit when teleporting
            for(Player player : Parkour.getInstance().getServer().getOnlinePlayers()) {
                previousLocation.put(player, player.getLocation());
            }
            //Parkour.getInstance().sendDebugMessage("Players: " + Parkour.getInstance().getServer().getOnlinePlayers());



        }, 0, 1);
         */
    }

    public HashMap<UUID, ArrayList<Plane>> getPlanes() {
        return planes;
    }

    public void addPlane(Plane plane) {
        if(planes.containsKey(plane.getWorld().getUID())) {
            planes.get(plane.getWorld().getUID()).add(plane);
        }
        else {
            planes.put(plane.getWorld().getUID(), new ArrayList<>());
            planes.get(plane.getWorld().getUID()).add(plane);
        }
    }
}
