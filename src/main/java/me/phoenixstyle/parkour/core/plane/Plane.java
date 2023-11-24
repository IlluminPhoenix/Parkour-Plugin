package me.phoenixstyle.parkour.core.plane;

import me.phoenixstyle.parkour.core.Parkour;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Objects;

public class Plane {
    public Location posx;
    public Location posy;
    public Location posz;
    Parkour.ParkourBlockType type;

    public Plane(Parkour.ParkourBlockType type, Location x, Location y, Location z) {
        this.type = type;
        posx = x;
        posy = y.subtract(x);
        posz = z.subtract(x);
        //Parkour.getInstance().sendDebugMessage(x + " \n- " + y + " \n- " + z + "\n- " + type);
    }

    public void visualizePlane(int gridDensity) {
        ArrayList<Vector> points = new ArrayList<>();
        Vector x = posx.clone().toVector();
        Vector y = posy.clone().toVector().multiply(1.0 / ((float)gridDensity - 1.0));
        Vector z = posz.clone().toVector().multiply(1.0 / ((float)gridDensity - 1.0));

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

    public Location getPosx() {
        return posx;
    }

    public double linePlaneIntersectionT(Vector la, Vector lb, Vector lab) {
        Vector p0, p01, p02;
        p0 = posx.toVector();
        p01 = posy.toVector();
        p02 = posz.toVector();

        Vector p012cross = p01.clone().crossProduct(p02.clone());
        double top = p012cross.clone().dot(la.clone().subtract(p0.clone()));

        double bottom = lab.clone().multiply(-1).dot(p012cross.clone());

        return top / bottom;
    }

    public double linePlaneIntersectionU(Vector la, Vector lb, Vector lab) {
        Vector p0, p01, p02;
        p0 = posx.toVector();
        p01 = posy.toVector();
        p02 = posz.toVector();

        Vector p012cross = p01.clone().crossProduct(p02.clone());
        double top = p02.clone().crossProduct(lab.clone().multiply(-1)).dot(la.clone().subtract(p0.clone()));

        double bottom = lab.clone().multiply(-1).dot(p012cross.clone());

        return top / bottom;
    }

    public double linePlaneIntersectionV(Vector la, Vector lb, Vector lab) {
        Vector p0, p01, p02;
        p0 = posx.toVector();
        p01 = posy.toVector();
        p02 = posz.toVector();

        Vector p012cross = p01.clone().crossProduct(p02.clone());
        double top = lab.clone().multiply(-1).crossProduct(p01.clone()).dot(la.clone().subtract(p0.clone()));

        double bottom = lab.clone().multiply(-1).dot(p012cross.clone());

        return top / bottom;
    }
}