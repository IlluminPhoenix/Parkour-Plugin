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

public class PlaneManager {

    private ArrayList<Plane> planes;
    private HashMap<Player, Location> previousLocation;

    BukkitScheduler scheduler;

    public PlaneManager() {
        this.scheduler = Bukkit.getScheduler();
        planes = new ArrayList<>();
        previousLocation = new HashMap<>();
        scheduler.runTaskTimer(Parkour.getInstance(), () -> {
            for(Plane plane : planes) {
                plane.visualizePlane(3);
            }
        }, 0, 4);

        scheduler.runTaskTimer(Parkour.getInstance(), () -> {

            for(Plane plane : planes) {
                Player player = Objects.requireNonNull(plane.getPosx().getWorld()).getPlayers().get(0);
                if(!previousLocation.containsKey(player)) {
                    continue;
                }
                Vector la =  previousLocation.get(player).toVector();
                Vector lb = player.getLocation().toVector();
                Vector lab =  lb.clone().subtract(la);


                //player.sendMessage("la: " + Utility.displayVector(la) + "\nlb: " + Utility.displayVector(lb) + "\nlab: " + Utility.displayVector(lab));
                //player.sendMessage("p0: " + Utility.displayVector(plane.posx.toVector()) + "\np01: " + Utility.displayVector(plane.posy.toVector()) + "\np02: " + Utility.displayVector(plane.posz.toVector()));

                double t = plane.linePlaneIntersectionT(la, lb, lab);
                double u = plane.linePlaneIntersectionU(la, lb, lab);
                double v = plane.linePlaneIntersectionV(la, lb, lab);

                if(t >= 0 && t <= 1 && u >= 0 && u <= 1 && v >= 0 && v <= 1) {
                    player.sendMessage("§aT: " + t);
                    player.sendMessage("§aU: " + u);
                    player.sendMessage("§aV: " + v);
                }


            }

            //Major exploit when teleporting
            for(Player player : Parkour.getInstance().getServer().getOnlinePlayers()) {
                previousLocation.put(player, player.getLocation());
            }
            //Parkour.getInstance().sendDebugMessage("Players: " + Parkour.getInstance().getServer().getOnlinePlayers());



        }, 0, 1);

    }

    public void addPlane(Plane plane) {
        planes.add(plane);
    }

}
