package me.phoenixstyle.parkour.utility;

import me.phoenixstyle.parkour.core.Parkour;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class Hologram {
    private static HashMap<Location, Hologram> holos;

    private Location loc;
    private String name;
    private ArmorStand armour_stand;

    public static void instantiate() {
        holos = new HashMap<>();
    }

    public Hologram(Location loc, String name) {
        this.loc = loc;
        this.name = name;
        spawnArmourStand(loc);

        if(holos.containsKey(loc)) {

            holos.get(loc).remove();
        }
        holos.put(loc, this);

    }

    public void remove() {
        armour_stand.remove();
        holos.remove(this.loc);
        //Parkour.getInstance().sendDebugMessage("Removed " + this.loc.toVector().toString());
    }

    public static void loadEntities(List<Entity> entities) {
        for(Entity entity : entities) {
            Parkour.getInstance().sendDebugMessage("Entity Loaded");
            if (entity.getType() != EntityType.ARMOR_STAND) {
                Parkour.getInstance().sendDebugMessage("Not Armour Stand");
                continue;
            }
            Parkour.getInstance().sendDebugMessage("1");
            ArmorStand stand = (ArmorStand) entity;
            if (!stand.hasMetadata("hologram")){
                Parkour.getInstance().sendDebugMessage("No Hologram");
                continue;
            }
            Parkour.getInstance().sendDebugMessage("2");
            if(!holos.containsKey(entity.getLocation())) {
                Parkour.getInstance().sendDebugMessage("Holos doesn't contain Key");
                entity.remove();
                continue;
            }
            Parkour.getInstance().sendDebugMessage("3");
            Hologram holo = holos.get(entity.getLocation());
            if(!(holo.armour_stand.getEntityId() == stand.getEntityId())) {
                Parkour.getInstance().sendDebugMessage("Removed and replaced!");
                holo.armour_stand.remove();
                holo.armour_stand = stand;
                continue;
            }
            Parkour.getInstance().sendDebugMessage("All fine!");
        }
    }

    private void spawnArmourStand(Location loc) {
        //Parkour.getInstance().sendDebugMessage("Spawn " + loc.toVector().toString());
        armour_stand = (ArmorStand) Objects.requireNonNull(loc.getWorld()).spawnEntity(loc, EntityType.ARMOR_STAND);
        armour_stand.setInvisible(true);
        armour_stand.setInvulnerable(true); //Fix this
        armour_stand.setSilent(true);
        armour_stand.setGravity(false);
        armour_stand.setCustomNameVisible(true);
        armour_stand.setCustomName(name);
        armour_stand.setSmall(true);
        armour_stand.setMetadata("hologram", new FixedMetadataValue(Parkour.getInstance(), true));
        armour_stand.teleport(loc);
    }
}
