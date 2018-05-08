package com.bogucki.databse;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

public class Berlin52DistanceHelperTest {

    @Test
    public void shouldCreateDatabaseWithB52Distances() throws Exception {
        Berlin52DistanceHelper berlin52DistanceHelper;
        if (!new File("Berlin52Distances.db").exists()) {
            berlin52DistanceHelper = new Berlin52DistanceHelper(null);
        } else
            berlin52DistanceHelper = new Berlin52DistanceHelper(new ArrayList<>());

        berlin52DistanceHelper.getTime(0, 0, 0);

        System.out.println("hejka");
    }
}