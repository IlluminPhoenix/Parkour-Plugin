package me.phoenixstyle.parkour.core.plane;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.UUID;

public class Player {

    private org.bukkit.entity.Player player;
    private UUID player_uuid;
    private Location previousLocation;

    public Player(org.bukkit.entity.Player player) {
        this.player = player;
        this.player_uuid = player.getUniqueId();
    }

    public static Vector getCentre(Vector playerLocation /*, State */) {
        return playerLocation.clone().add(new Vector(0.0, 0.9, 0.0));
    }

    public ArrayList<Vector> getVertices() {
        ArrayList<Vector> result = new ArrayList<>();
        Vector origin = new Vector(-0.3, 0.0, -0.3);

        for(int i = 0; i < 8; i++) {
            int x = i / 4;
            int y = i % 4 / 2;
            int z = i % 2;

            result.add(new Vector(x, y, z));
        }

        for(Vector vec : result) {
            vec.setX(vec.getX() * 0.6);
            vec.setY(vec.getY() * 1.8);
            vec.setZ(vec.getZ() * 0.6);

            vec.add(origin);
        }

        return result;

    }

    public void updatePreviousLocation() {
        previousLocation = player.getLocation();
    }

    public Vector getCentre() {
        return player.getLocation().add(0.0, 0.9, 0.0).toVector();
    }

    public Location getTickCentre() {
        Location origin = new Location(previousLocation.getWorld(), 0, 0, 0);
        origin.add(Player.getCentre(previousLocation.toVector()));
        Vector tickVector = player.getLocation().subtract(previousLocation).toVector();
        return origin.add(tickVector.multiply(0.5));
    }

    public double getTickCRadius() {
        return player.getLocation().subtract(previousLocation).multiply(0.5).length() + getStaticCRadius();
    }

    public double getStaticCRadius() {
        return 1.0;
    }

    public org.bukkit.entity.Player getPlayer() {
        return player;
    }

    public UUID getPlayer_uuid() {
        return player_uuid;
    }

    public Location getPreviousLocation() {
        return previousLocation;
    }

    public void remove() {

    }
}
