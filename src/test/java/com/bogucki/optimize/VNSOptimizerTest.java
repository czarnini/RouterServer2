package com.bogucki.optimize;

import com.bogucki.databse.DistanceHelper;
import com.bogucki.optimize.models.Meeting;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class VNSOptimizerTest {

    private String[] addresses;
    private ArrayList<Meeting> meetings;
    private DistanceHelper distanceHelper;
    private VNSOptimizer optimizer;

    @Before
    public void setup() throws Exception {
        addresses = new String[]{"wspólna 73,warszawa",
                "mielczarskiego 10,warszawa",
                "janowskiego 13,warszawa",
                "konduktorska 2,warszawa",
                "komorska 29, warszawa",
                "herbsta 4, warszawa",
                "świętokrzyska 31, warszawa",
                "berensona 12b, warszawa"};
        meetings = new ArrayList<>();
        for (String address : addresses) {
            meetings.add(new Meeting(address));
        }
        distanceHelper = new DistanceHelper(meetings);
        optimizer = new VNSOptimizer(distanceHelper);
    }

    @Test
    public void testOpt2() throws Exception {
        for (int i = 0; i < 10; i++) {
            Route opt2 = optimizer.opt2(Route.getInitialRoute(meetings.size(), distanceHelper));

            for (int j = 0; j < meetings.size(); j++) {
                System.out.print(opt2.getCitiesOrder()[j]);
            }
            System.out.println();
        }
    }


    @Test
    public void testVNS() throws Exception {

        for (int i = 0; i < 10; ++i) {
            optimizer = new VNSOptimizer(new DistanceHelper(meetings));
            optimizer.optimize();
            for (int j = 0; j < meetings.size(); j++) {
                System.out.print(optimizer.getCurrentBest().getCitiesOrder()[j]);
            }
            System.out.println();
            VNSOptimizer.currentBest = null;
        }
    }
}