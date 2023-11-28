package me.phoenixstyle.parkour.sqlite_database;

import me.phoenixstyle.parkour.core.Parkour;
import org.bukkit.Location;
import org.bukkit.World;

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
                Parkour.getInstance().sendDebugMessage("The driver name is " + meta.getDriverName());
                Parkour.getInstance().sendDebugMessage("A new database has been created.");
                conn.close();
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
            conn.close();
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
                conn.close();
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
