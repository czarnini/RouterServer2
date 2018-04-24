package com.bogucki.optimize.models;

public class Client {
    private String address;
    private String name;

    public Client() {
    }

    public Client(String address, String name) {
        this.address = address;
        this.name = name;
    }

    public String getAddress() {
        return address.replaceAll(" *, *", ",").trim().toLowerCase();
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Address:%s\nName:%s", address,name);
    }
}
