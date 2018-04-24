package com.bogucki.databse;

import com.bogucki.MapsAPI.GoogleMaps;
import com.bogucki.optimize.Meeting;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DistanceHelper {

    private static final String ADDRESSES_DICT = "ADDRESSES_DICT";
    private final ArrayList<Meeting> meetings;
    private Connection c;


    private volatile List<Integer> addressesIds;
    private volatile HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> costs;

    public void createAddressDictionary() {
        try {
            System.out.println("Creating database");
            StringBuilder query = new StringBuilder("CREATE TABLE " + ADDRESSES_DICT + " ")
                    .append(" (")
                    .append("ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ")
                    .append("ADDRESS CHAR(100) NOT NULL);");

            //System.outprintln(query.toString());
            Statement statement = c.createStatement();
            statement.executeUpdate(query.toString());
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private String generateHoursColumns() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 24; i++) {
            builder.append("C").append(i).append(" INT NOT NULL ");
            if (i != 23) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    public DistanceHelper(ArrayList<Meeting> meetings) {
        this.meetings = meetings;
        try {
            String databaseUrl = "jdbc:sqlite:Distances.db";
            c = DriverManager.getConnection(databaseUrl);
            loadDistancesToRAM();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void loadDistancesToRAM() {
        if (null == meetings) {
            return;
        }

//        System.out.println("Loading requested cities from HDD to RAM");
        addressesIds = new ArrayList<>();
        costs = new HashMap<>();

        for (Meeting meeting : meetings) {
            try {
                int tmpCityID = mapAddressToID(meeting.getAddress());
                addressesIds.add(tmpCityID);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        for (int addressId : addressesIds) {
            try {
                costs.put(addressId, new HashMap<>(loadTimesFromAddress(addressId)));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }


    public void cleanUp() {
        try {
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Calculate Time between A and B when the trip starts at T
     * SELECT T FROM Ta WHERE DEST_ID = B;
     *
     * @param origin      - origin
     * @param destination - destination
     * @param timeOfStart - starting time
     */

    public int getTime(int origin, int destination, int timeOfStart) {
        int originID = addressesIds.get(origin);
        int destinationID = addressesIds.get(destination);
        if (originID == destinationID) {
            return 0;
        }
        return costs.get(originID).get(destinationID).get(0);

    }


    private HashMap<Integer, HashMap<Integer, Integer>> loadTimesFromAddress(int originID) throws SQLException {

        HashMap<Integer, HashMap<Integer, Integer>> result = new HashMap<>();
        String query = " SELECT dest_id, C0, C1, C2, C3, C4, C5, C6, C7, C8, C9, C10, C11, C12, C13, C14, C15,C16,C17,C18,C19,C20,C21,C22,C23 " +
                " FROM A" + originID +
                " WHERE  dest_id IN (" + StringUtils.join(addressesIds, " , ") + ")";
        Statement statement = c.createStatement();
        ResultSet rs = statement.executeQuery(query);

        while (rs.next()) {
            HashMap<Integer, Integer> hours = new HashMap<>();
            result.put(rs.getInt(1), hours);
            for (int i = 0; i < 24; i++) {
                hours.put(i, rs.getInt(i + 2));
            }
        }
        rs.close();
        statement.close();
        return result;
    }

    /**
     * a. Insert address into address_dict
     * b. Create table Tn (n -> id from dict)
     * c. Add row with time of trip to tables T1-Tn-1
     *
     * @param address - address to be added
     */
    private int addAddress(String address) throws SQLException {

        String currentAddresses = StringUtils.join(getAllAddresses(), "|");
        ArrayList<Integer> timesToNewAddress = GoogleMaps.getDistances(currentAddresses, address);
        ArrayList<Integer> timesFromNewAddress = GoogleMaps.getDistances(address, currentAddresses);

        if (timesFromNewAddress.size() == 0) {
            return -1;
        }

        int id;
        id = addAddressToDict(address);

        createAddressTable(id, timesFromNewAddress);
        insertTimes(1, id, timesToNewAddress);

        System.out.println("Adding " + address + "to database with ID: " + id);
        return id;
    }

    private int addAddressToDict(String address) throws SQLException {
        StringBuilder query = new StringBuilder("INSERT INTO " + ADDRESSES_DICT)
                .append("(ADDRESS) VALUES ('")
                .append(address)
                .append("');");


        Statement statement = c.createStatement();
        statement.executeUpdate(query.toString());
        int id = statement.getGeneratedKeys().getInt(1);
        statement.close();
        return id;
    }

    private void createAddressTable(int originId, ArrayList<Integer> timesFromNewAddress) throws SQLException {
        String query = "CREATE TABLE A" +
                originId +
                " (" +
                "DEST_ID INT NOT NULL, " +
                generateHoursColumns() +
                ");";

        c.createStatement().executeUpdate(query);

        for (int destinationId = 1; destinationId < originId; destinationId++) {
            timesFromNewAddress.set(0, timesFromNewAddress.get(destinationId - 1));
            insertTimes(originId, destinationId, timesFromNewAddress);
        }
    }

    private void insertTimes(int originId, int destinationId, ArrayList<Integer> times) throws SQLException {
        if (originId == destinationId && destinationId == 1) {
            return;
        }
        int helpfulIndex = 0;
        do {
            String query = "INSERT INTO A" +
                    originId +
                    "(dest_id, C0, C1, C2, C3, C4, C5, C6, C7, C8, C9, C10, C11, C12, C13, C14, C15,C16,C17,C18,C19,C20,C21,C22,C23) VALUES (" +
                    destinationId +
                    generateTimeDistribution(times.get(helpfulIndex)) +
                    ");";


            c.createStatement().executeUpdate(query);
            originId++;
            helpfulIndex++;
        } while (originId < destinationId);
    }


    private ArrayList<String> getAllAddresses() throws SQLException {
        String query = "SELECT ADDRESS" +
                " FROM " + ADDRESSES_DICT + ";";
        Statement statement = c.createStatement();
        ResultSet rs = statement.executeQuery(query);
        ArrayList<String> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rs.getString(1));
        }
        rs.close();
        statement.close();
        return result;
    }


    private int mapAddressToID(String addressToCheck) throws SQLException {
        int id = getAddressID(addressToCheck);
        return -1 == id ? addAddress(addressToCheck) : id;
    }


    private int getAddressID(String addressToCheck) throws SQLException {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ID")
                .append(" FROM " + ADDRESSES_DICT + " ")
                .append("WHERE ADDRESS  = '")
                .append(addressToCheck)
                .append("';");

        ResultSet rs = c.createStatement().executeQuery(query.toString());
        if (rs.isClosed()) {
            return -1;
        } else {
            int id = rs.getInt(1);
            rs.close();
            return id;
        }
    }


    public void addAddresses(List<String> addressesToAdd) throws SQLException {
        for (String address : addressesToAdd) {
            mapAddressToID(address);
        }

    }

    public void addAddressToDb(String addressToAdd) throws SQLException {
        mapAddressToID(addressToAdd);
    }

    private String generateTimeDistribution(int midnightTime) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 24; i++) {
            result.append(", ");
            if (i <= 6) {
                result.append(midnightTime);
            } else if (i <= 10) {
                result.append(5 * midnightTime);
            } else if (i <= 14) {
                result.append(2.5 * midnightTime);
            } else if (i <= 18) {
                result.append(5 * midnightTime);
            } else if (i <= 22) {
                result.append(1.5 * midnightTime);
            } else {
                result.append(midnightTime);
            }
        }

        return result.toString();
    }

    public ArrayList<Meeting> getMeetings() {
        return meetings;
    }
}
