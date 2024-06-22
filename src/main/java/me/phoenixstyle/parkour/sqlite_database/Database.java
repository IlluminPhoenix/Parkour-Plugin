package me.phoenixstyle.parkour.sqlite_database;

import me.phoenixstyle.parkour.core.Parkour;
import me.phoenixstyle.parkour.core.plane.Plane;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class Database {
    private String databaseURL;
    private ArrayList<String> setupCommands;


    public Database(String path, String fileName) {
        if(!connectDatabase(path, fileName)) {
            return;
        }
        setup();
        //System.out.println("Setup Done!");
    }

    private boolean connectDatabase(String path, String fileName) {
        String stringPath = path.replace('\\', '/') + "/" + fileName;
        Parkour.getInstance().sendDebugMessage(stringPath);

        Path fullPath = Paths.get(path + "\\" + fileName).getParent();
        File parentDir = new File(fullPath.toUri());
        parentDir.mkdirs();

        databaseURL = "jdbc:sqlite:" + stringPath;
        Parkour.getInstance().sendDebugMessage(databaseURL);

        try (Connection conn = connect()) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
            }
        } catch (SQLException e) {
            Parkour.getInstance().sendDebugMessage(e.getMessage());
            return false;
        }
        return true;
    }

    private Connection connect() {
        //System.out.println("Connect");
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(databaseURL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    private void registerAllTables() {
        // SQL statement for creating a new table
        setupCommands.add("CREATE TABLE IF NOT EXISTS pk_blocks (x integer, y integer, z integer, w Text, type integer, PRIMARY KEY ( x,y,z,w ));");
        setupCommands.add("CREATE TABLE IF NOT EXISTS pk_planes (name Text PRIMARY KEY NOT NULL," +
                "xx real, xy real, xz real, xw Text, " +
                "yx real, yy real, yz real, " +
                "zx real, zy real, zz real, type integer);");
    }

    private void setup() {
        setupCommands = new ArrayList<>();
        registerAllTables();

        for(String stmt : setupCommands) {
            //System.out.println("Executing setup");
            executeStatement(stmt);
        }
    }

    private boolean executeStatement(String statement) {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(statement);
        } catch (SQLException e) {
            System.out.println(e.getMessage());

            return false;
        }
        return true;
    }

    public void modifyPkBlocks(Parkour.ParkourBlock block, Action action) {
        if(action == Action.WRITE) {
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement("REPLACE INTO pk_blocks VALUES(?,?,?,?,?)");
            ){
                pstmt.setInt(1, block.location.getBlockX());
                pstmt.setInt(2, block.location.getBlockY());
                pstmt.setInt(3, block.location.getBlockZ());
                pstmt.setString(4, Objects.requireNonNull(block.location.getWorld()).getUID().toString());
                pstmt.setInt(5, block.type.toInt());
                pstmt.executeUpdate();
                //Parkour.getInstance().sendDebugMessage("Write");
                conn.close();
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        else {
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM pk_blocks WHERE x = ? AND y = ? AND z = ? AND w = ?");
            ){
                pstmt.setInt(1, block.location.getBlockX());
                pstmt.setInt(2, block.location.getBlockY());
                pstmt.setInt(3, block.location.getBlockZ());
                pstmt.setString(4, Objects.requireNonNull(block.location.getWorld()).getUID().toString());
                pstmt.executeUpdate();
                //Parkour.getInstance().sendDebugMessage("Delete");
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }

        }
    }

    public Optional<ArrayList<Parkour.ParkourBlock>> readPkBlocks() {
        //System.out.println("ReadPKBlocks");
         //resultSet;
        ArrayList<Parkour.ParkourBlock> pkBlocks = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT * FROM pk_blocks")) {
            while(resultSet.next()) {

                //System.out.println("STMT Closed: " + stmt.isClosed());
                //System.out.println("Closed: " + resultSet.isClosed());
                World world = Parkour.getInstance().getServer().getWorld(UUID.fromString(resultSet.getString(4)));

                if(world == null) {
                    System.out.println("World is null!");
                    continue;
                }

                double x = resultSet.getInt(1);
                double y = resultSet.getInt(2);
                double z = resultSet.getInt(3);
                Parkour.ParkourBlockType type = Parkour.ParkourBlockType.fromInt(resultSet.getInt(5));
                Location loc = new Location(world, x, y, z);
                Parkour.ParkourBlock pkBlock = new Parkour.ParkourBlock(loc, type);
                pkBlocks.add(pkBlock);


            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            return Optional.empty();
        }
        //System.out.println("Query Success!");

        if(pkBlocks.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(pkBlocks);
    }

    public void modifyPkPlanes(Plane plane, Action action) {

        if(action == Action.WRITE) {
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement("REPLACE INTO pk_planes VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
            ){
                Location x = plane.getPosx();
                Vector y = plane.getPosy();
                Vector z = plane.getPosz();
                pstmt.setString(1, plane.name);
                pstmt.setDouble(2, x.getX());
                pstmt.setDouble(3, x.getY());
                pstmt.setDouble(4, x.getZ());
                pstmt.setString(5, Objects.requireNonNull(x.getWorld()).getUID().toString());
                pstmt.setDouble(6, y.getX());
                pstmt.setDouble(7, y.getY());
                pstmt.setDouble(8, y.getZ());
                pstmt.setDouble(9, z.getX());
                pstmt.setDouble(10, z.getY());
                pstmt.setDouble(11, z.getZ());
                pstmt.setDouble(12, plane.getType().toInt());
                pstmt.executeUpdate();
                //Parkour.getInstance().sendDebugMessage("Write");
                conn.close();
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        else {
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM pk_planes WHERE name = ?");
            ){
                pstmt.setString(1, plane.name);
                pstmt.executeUpdate();
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }

        }
    }

    public Optional<ArrayList<Plane>> readPkPlanes() {
        //System.out.println("ReadPKBlocks");
        //resultSet;
        ArrayList<Plane> pkBlocks = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT * FROM pk_planes")) {
            while(resultSet.next()) {

                //System.out.println("STMT Closed: " + stmt.isClosed());
                //System.out.println("Closed: " + resultSet.isClosed());
                World world = Parkour.getInstance().getServer().getWorld(UUID.fromString(resultSet.getString(5)));

                if(world == null) {
                    System.out.println("World is null!");
                    continue;
                }

                String name = resultSet.getString(1);

                double xx = resultSet.getDouble(2);
                double xy = resultSet.getDouble(3);
                double xz = resultSet.getDouble(4);
                Location loc = new Location(world, xx, xy, xz);

                Vector y = new Vector(resultSet.getDouble(6), resultSet.getDouble(7), resultSet.getDouble(8));
                Vector z = new Vector(resultSet.getDouble(9), resultSet.getDouble(10), resultSet.getDouble(11));

                Parkour.ParkourBlockType type = Parkour.ParkourBlockType.fromInt(resultSet.getInt(12));
                Plane plane = new Plane(type, loc, y.toLocation(world).add(loc), z.toLocation(world).add(loc),  name);
                pkBlocks.add(plane);


            }
            //conn.close();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            return Optional.empty();
        }
        //System.out.println("Query Success!");

        if(pkBlocks.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(pkBlocks);
    }

    public void close() {
        //Empty, kept for later purposes
    }


    public enum Action {
        WRITE,
        REMOVE,
    }
}
