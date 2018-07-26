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
        addresses = new String[52];
        for (int i = 0; i < 52; i++) {
            addresses[i] = String.valueOf(i + 1);
        }
        meetings = new ArrayList<>();

        int meetingIndex = 0;
        int tmpETP;
        int tmpLTP;

        for (String address : addresses) {
            if(meetingIndex < 20){
                tmpETP = 0;
                tmpLTP = 8000;
            } else if(meetingIndex < 30){
                tmpETP = 0;
                tmpLTP = 7500;
            }else{
                tmpETP = 0;
                tmpLTP = 7000;
            }
            meetings.add(new Meeting(address, tmpETP, tmpLTP));
            meetingIndex++;
        }
        distanceHelper = new DistanceHelper(meetings);
        optimizer = new VNSOptimizer(distanceHelper);
    }

    @Test
    public void testOpt2() throws Exception {

        Route opt2 = optimizer.opt2(Route.getInitialRoute(distanceHelper));
        opt2.getRoute();
//        for (int j = 0; j < meetings.size(); j++) {
//            System.out.print(meetings.get(opt2.getCitiesOrder()[j]));
//        }
        System.out.println();

    }


    @Test
    public void testVNS() throws Exception {
            optimizer = new VNSOptimizer(distanceHelper);
            optimizer.optimize();

      /*  for (int j = 0; j < meetings.size(); j++) {
            System.out.print(meetings.get(optimizer.getCurrentBest().getCity(j)).getAddress() + ", ");
        }

        System.out.println("\n" + optimizer.getCurrentBest().getCost());*/
        VNSOptimizer.currentBest.getRoute();
        VNSOptimizer.currentBest = null;

    }
}