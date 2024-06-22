package me.phoenixstyle.parkour.core.plane;

import me.phoenixstyle.parkour.core.Parkour;
import me.phoenixstyle.parkour.sqlite_database.Database;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public class PlaneManager {
    private HashMap<UUID, HashMap<String, Plane>> worldPlaneMap;
    private HashMap<String, Plane> namePlaneMap;

    BukkitScheduler scheduler;

    public PlaneManager() {
        this.scheduler = Bukkit.getScheduler();
        loadPlaneData(Parkour.getInstance().getDatabase());
        scheduler.runTaskTimer(Parkour.getInstance(), () -> {
            for(HashMap<String, Plane> planesHashMap : worldPlaneMap.values()) {
                for(Plane plane : planesHashMap.values()) {
                    plane.visualizePlane(3);
                    //plane.visualizePlaneRadius();
                }
            }
        }, 0, 20);
    }

    public
    HashMap<UUID, HashMap<String, Plane>> getWorldPlaneMap() {
        return worldPlaneMap;
    }

    public void addPlane(Plane plane) {
        if(!namePlaneMap.containsKey(plane)) {
            namePlaneMap.put(plane.name, plane);
            if(worldPlaneMap.containsKey(plane.getWorld().getUID())) {
                worldPlaneMap.get(plane.getWorld().getUID()).put(plane.name, plane);
            }
            else {
                worldPlaneMap.put(plane.getWorld().getUID(), new HashMap<>());
                worldPlaneMap.get(plane.getWorld().getUID()).put(plane.name, plane);
            }
        }
    }

    public void removePlane(String name) {
        Plane plane = namePlaneMap.remove(name);
        if(plane != null) {
            plane.remove();
            HashMap<String, Plane> worldMap = worldPlaneMap.get(plane.getWorld().getUID());
            worldMap.remove(plane.name);
            if(worldMap.isEmpty()) {
                worldPlaneMap.remove(plane.getWorld().getUID());
            }
        }
    }

    public void loadPlaneData(Database database) {
        worldPlaneMap = new HashMap<>();
        namePlaneMap = new HashMap<>();
        Optional<ArrayList<Plane>> optional = database.readPkPlanes();
        if(optional.isPresent()) {
            ArrayList<Plane> planeList = optional.get();
            for(Plane plane : planeList) {
                addPlane(plane);
            }
        }
    }

    public HashMap<String, Plane> getNamePlaneMap() {
        return namePlaneMap;
    }
}
