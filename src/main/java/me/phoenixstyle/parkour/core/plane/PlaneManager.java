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

    BukkitScheduler scheduler;

    public PlaneManager() {
        this.scheduler = Bukkit.getScheduler();
        planes = new HashMap<>();
        scheduler.runTaskTimer(Parkour.getInstance(), () -> {
            for(ArrayList<Plane> arrayList : planes.values()) {
                for(Plane plane : arrayList) {
                    plane.visualizePlane(3);
                    //plane.visualizePlaneRadius();
                }
            }
        }, 0, 20);
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
