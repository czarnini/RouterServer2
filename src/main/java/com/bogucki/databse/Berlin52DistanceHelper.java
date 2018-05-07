package com.bogucki.databse;

import com.bogucki.optimize.models.Meeting;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Berlin52DistanceHelper extends DistanceHelper {

    private ArrayList<ArrayList<Integer>> distances = new ArrayList<>();
    private ArrayList<Point> points = new ArrayList<>();

    public Berlin52DistanceHelper(ArrayList<Meeting> meetings) {
        this.meetings = meetings;
        try {

            String databaseUrl = "jdbc:sqlite:Berlin52Distances.db";
            c = DriverManager.getConnection(databaseUrl);

            if (meetings == null) {
                createAddressDictionary();
                BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\boguc\\IdeaProjects\\bogucki\\src\\main\\java\\com\\bogucki\\databse\\berlin52.txt"));
                String line = reader.readLine();
                while (line != null) {
                    String[] split = line.split(" ");
                    points.add(new Point(Integer.valueOf(split[1]), Integer.valueOf(split[2])));
                    line = reader.readLine();
                }
                for (Point start : points) {
                    ArrayList<Integer> tmp = new ArrayList<>();
                    for (Point end : points) {
                        tmp.add((int) Math.sqrt(Math.pow(start.x - end.x, 2) + Math.pow(start.y - end.y, 2)));
                    }
                    distances.add(tmp);
                }


                for (int i = 0; i < distances.size(); i++) {
                    int j = i + 1;
                    ArrayList<Integer> distance = distances.get(i);
                    int id = addAddressToDict(String.valueOf(j));
                    createAddressTable(id, distance);
                }
            } else {
                loadDistancesToRAM();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    void createAddressTable(int originId, ArrayList<Integer> timesFromNewAddress) throws SQLException {
        String query = "CREATE TABLE A" +
                originId +
                " (" +
                "DEST_ID INT NOT NULL, " +
                generateHoursColumns() +
                ");";

        c.createStatement().executeUpdate(query);

        for (int destinationId = 1; destinationId <= 52; destinationId++) {
            timesFromNewAddress.set(0, timesFromNewAddress.get(destinationId - 1));
            insertTimes(originId, destinationId, timesFromNewAddress);
        }
    }

    @Override
    void loadDistancesToRAM() {
        addressesIds = new ArrayList<>();
        costs = new HashMap<>();
        if (null == meetings) {
            return;
        } else {
            for (int i = 1; i <= 52; i++) {
                addressesIds.add(i);
            }
            for (int i = 1; i <= 52; i++) {
                try {
                    costs.put(i, new HashMap<>(loadTimesFromAddress(i)));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
