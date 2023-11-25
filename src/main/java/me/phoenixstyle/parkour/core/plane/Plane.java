package me.phoenixstyle.parkour.core.plane;

import me.phoenixstyle.parkour.core.Parkour;
import me.phoenixstyle.parkour.utility.Utility;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Objects;
import java.util.TreeSet;

public class Plane {
    public Location posx;
    public Vector posy;
    public Vector posz;

    public double CRadius;
    Parkour.ParkourBlockType type;

    public Plane(Parkour.ParkourBlockType type, Location x, Location y, Location z) {
        this.type = type;
        posx = x;
        posy = y.subtract(x).toVector();
        posz = z.subtract(x).toVector();
        //Parkour.getInstance().sendDebugMessage(x + " \n- " + y + " \n- " + z + "\n- " + type);
    }

    public void visualizePlane(int gridDensity) {
        ArrayList<Vector> points = new ArrayList<>();
        Vector x = posx.clone().toVector();
        Vector y = posy.clone().multiply(1.0 / ((float)gridDensity - 1.0));
        Vector z = posz.clone().multiply(1.0 / ((float)gridDensity - 1.0));

        Vector ivec = x.clone();
        for(int i = 0; i < gridDensity; i++) {
            Vector jvec = ivec.clone();
            for(int j = 0; j < gridDensity; j++) {
                points.add(jvec.clone());
                jvec.add(z);
            }
            ivec.add(y);

        }

        for(Vector point : points) {
            Objects.requireNonNull(posx.getWorld()).spawnParticle(Particle.DRAGON_BREATH, point.getX(), point.getY(), point.getZ(), 0, 0, 0, 0, 0.25);
        }
    }

    public void visualizePlaneRadius() {
        Utility.renderSphere(getCentre(), getCRadius(), Particle.END_ROD, 50);
    }

    public Location getCentre() {
        return posx.clone().add(posy.clone().multiply(0.5)).add(posz.clone().multiply(0.5));
    }

    public double getCRadius() {
        return posy.clone().add(posz).multiply(0.5).length();
    }

    public Location getPosx() {
        return posx;
    }

    public Vector getPosy() {
        return posy;
    }

    public Vector getPosz() {
        return posz;
    }

    public World getWorld() {
        return posx.getWorld();
    }

    public double collide(Player player, CollisionType type) {
        if(player.getPreviousLocation() == null) {
            return Double.NaN;
        }
        Vector la = player.getPreviousLocation().toVector();
        Vector lb = player.getCurrentLocation().toVector();
        Vector lab = la.clone().subtract(lb);

        ArrayList<Vector> vertices = new ArrayList<>();
        ArrayList<Line> edges = new ArrayList<>();
        ArrayList<Plane> faces = new ArrayList<>();

        if(type == CollisionType.POINT) {
            vertices.add(Player.getCentre(la));
        }
        else if (type == CollisionType.VERTICES) {
            vertices = player.getVertices();
            for(Vector vec : vertices) {
                vec.add(la);
            }
        }

        TreeSet<Double> collisions = new TreeSet<>();

        for(Vector ver : vertices) {
            double t = linePlaneIntersectionT(ver, lab);
            double u = linePlaneIntersectionU(ver, lab);
            double v = linePlaneIntersectionV(ver, lab);

            if(t >= 0 && t <= 1 && u >= 0 && u <= 1 && v >= 0 && v <= 1) {
                collisions.add(t);
                //player.getPlayer().sendMessage(String.format("§aT: %.4f", t));
                //player.getPlayer().sendMessage(String.format("§7U: %.4f", u));
                //player.getPlayer().sendMessage(String.format("§7V: %.4f", v));

                Vector collision = new Vector(0.0, 0.0, 0.0).add(ver).add(lab.clone().multiply(t));


                posx.getWorld().spawnParticle(Particle.DRAGON_BREATH, collision.getX(), collision.getY(), collision.getZ(), 0, 0, 0, 0, 0.25);
            }

        }

        if(collisions.isEmpty()) {
            return Double.NaN;
        }
        return collisions.first();
    }

    /*
    I'm not going to explain all of this, please just read this
    wikipedia article, if you want to understand the next 3 functions:
    https://en.wikipedia.org/wiki/Line%E2%80%93plane_intersection
     */
    public double linePlaneIntersectionT(Vector la, Vector lab) {
        Vector p0, p01, p02;
        p0 = posx.toVector();
        p01 = posy;
        p02 = posz;

        Vector p012cross = p01.clone().crossProduct(p02.clone());
        double top = p012cross.clone().dot(la.clone().subtract(p0.clone()));

        double bottom = lab.clone().multiply(-1).dot(p012cross.clone());

        return top / bottom;
    }

    public double linePlaneIntersectionU(Vector la, Vector lab) {
        Vector p0, p01, p02;
        p0 = posx.toVector();
        p01 = posy;
        p02 = posz;

        Vector p012cross = p01.clone().crossProduct(p02.clone());
        double top = p02.clone().crossProduct(lab.clone().multiply(-1)).dot(la.clone().subtract(p0.clone()));

        double bottom = lab.clone().multiply(-1).dot(p012cross.clone());

        return top / bottom;
    }

    public double linePlaneIntersectionV(Vector la, Vector lab) {
        Vector p0, p01, p02;
        p0 = posx.toVector();
        p01 = posy;
        p02 = posz;

        Vector p012cross = p01.clone().crossProduct(p02.clone());
        double top = lab.clone().multiply(-1).crossProduct(p01.clone()).dot(la.clone().subtract(p0.clone()));

        double bottom = lab.clone().multiply(-1).dot(p012cross.clone());

        return top / bottom;
    }

    enum CollisionType {
        POINT,
        VERTICES,
        EDGES,
        FULL,
    }

    class Line {
        public Vector la;
        public Vector lab;

        public Line(Vector la, Vector lab) {
            this.la = la;
            this.lab = lab;
        }

    }

    //Issue: Doesn't take into consideration if your sneaking or crawling/elytra-flying.
}