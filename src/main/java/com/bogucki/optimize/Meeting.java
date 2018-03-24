package com.bogucki.optimize;

public class Meeting {
    private String address;
    private long earliestTimePossible;
    private long latestTimePossible;

    public Meeting(String address, long earliestTimePossible, long latestTimePossible) {
        this.address = address;
        this.earliestTimePossible = earliestTimePossible;
        this.latestTimePossible = latestTimePossible;
    }

    public String getAddress() {
        return address;
    }

    public long getEarliestTimePossible() {
        return earliestTimePossible;
    }

    public long getLatestTimePossible() {
        return latestTimePossible;
    }
}
