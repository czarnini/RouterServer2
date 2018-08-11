package com.bogucki.optimize;

import com.bogucki.databse.DistanceHelper;
import com.bogucki.optimize.models.Meeting;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

import static com.bogucki.optimize.RouteTest.ROUTE_SIZE;

public class VNSOptimizerTest {

    private String[] addresses;
    private ArrayList<Meeting> meetings;
    private DistanceHelper distanceHelper;
    private VNSOptimizer optimizer;

    @Before
    public void setup() throws Exception {
        addresses = new String[ROUTE_SIZE];
        for (int i = 0; i < ROUTE_SIZE; i++) {
            addresses[i] = String.valueOf(i + 1);
        }
        meetings = new ArrayList<>();

        Random generator = new Random();
        int tmpETP;
        int tmpLTP;

        for (String address : addresses) {
            tmpETP = generator.nextInt(8000);
            tmpLTP = tmpETP + 4500;
            meetings.add(new Meeting(address, tmpETP, tmpLTP));
        }
        distanceHelper = new DistanceHelper(meetings);
        optimizer = new VNSOptimizer(distanceHelper);
    }

    @Test
    public void testOpt2() throws Exception {

        Route opt2 = optimizer.opt2(Route.getInitialRoute(distanceHelper));
        opt2.getRoute();
        System.out.println();

    }


    @Test
    public void testVNS() throws Exception {
        optimizer = new VNSOptimizer(distanceHelper);
        for (int i = 0; i < 100; i++) {
            optimizer.optimize();
            VNSOptimizer.currentBest = null;
        }

    }


    @Test
    public void testLocal1Shift() {
        Route x = Route.newRandomRoute(distanceHelper);
        x.getRoute();
        optimizer.local1Shift(x, true).getRoute();
    }
}