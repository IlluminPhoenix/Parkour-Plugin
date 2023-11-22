package me.phoenixstyle.parkour.core.plane;

import me.phoenixstyle.parkour.core.Parkour;
import org.bukkit.Location;

public class Plane {
    public Location posx;
    public Location posy;
    public Location posz;
    Parkour.ParkourBlockType type;

    public Plane(Parkour.ParkourBlockType type, Location x, Location y, Location z) {
        this.type = type;
        posx = x;
        posy = y;
        posz = z;
        //Parkour.getInstance().sendDebugMessage(x + " \n- " + y + " \n- " + z + "\n- " + type);
    }
}