package com.bogucki.optimize;

import com.bogucki.databse.DistanceHelper;
import com.bogucki.optimize.models.Meeting;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class RouteTest {

    public static final int ROUTE_SIZE = 52;
    private String[] addresses;
    private ArrayList<Meeting> meetings;
    private DistanceHelper distanceHelper;
    private Route route;

    @Before
    public void setUp() throws Exception {
        addresses = new String[ROUTE_SIZE];
        meetings = new ArrayList<>();


        for (int i = 0; i < ROUTE_SIZE; i++) {
            addresses[i] = String.valueOf(i + 1);
        }
        for (String address : addresses) {
            meetings.add(new Meeting(address, 0, 10));
        }

        distanceHelper = new DistanceHelper(meetings);
        route = Route.getInitialRoute(distanceHelper);
        route.getRoute();

    }

    @Test
    public void testSwapByRandom(){
        for (int i = 0; i < 30; i++) {
            route = this.route.generateNeighbourRoute(i);
            for (int j = 0; j < route.getCitiesOrder().length; j++) {
                for (int k = 0; k < route.getCitiesOrder().length; k++) {
                    if(j!=k && route.getCitiesOrder()[j] == route.getCitiesOrder()[k] ){
                        System.out.println("oo");
                    }
                }
            }
        }
    }


    @Test
    public void shouldSwapRouteBetweenFirstElementWithForth() throws Exception {
        int[] orderBeforeSwap = route.getCitiesOrder();
        int costBeforeSwap = route.getCost();
        route.swapForOpt2(3,5);
        route.getRoute();



        int[] orderAfterSwap = route.getCitiesOrder();
        route.getRoute();

        System.out.println(costBeforeSwap + "   " + route.getCost());
        for (int i = 0; i < orderAfterSwap.length; i++) {
            System.out.println("index: " + i + "\t\tBefore: " + orderBeforeSwap[i] + "\t\tAfter: " + orderAfterSwap[i]);
        }
    }


    @Test
    public void testOrdinarySwapping() throws Exception {
        int[] orderBeforeSwap = route.getCitiesOrder();
        int costBeforeSwap = route.getCost();
        route.swap(5,2);
        route.getRoute();



        int[] orderAfterSwap = route.getCitiesOrder();
        route.getRoute();

        System.out.println(costBeforeSwap + "   " + route.getCost());
        for (int i = 0; i < orderAfterSwap.length; i++) {
            System.out.println("index: " + i + "\t\tBefore: " + orderBeforeSwap[i] + "\t\tAfter: " + orderAfterSwap[i]);
        }
    }

    @Test
    public void shouldGenerateNeighbourdRoute() throws Exception {
        int distance = route.getCitiesOrder().length - 1;
        int[] orderBeforeSwap = route.getCitiesOrder();
        Route result = route.generateNeighbourRoute(4);
        int[] orderAfterSwap = result.getCitiesOrder();
        for (int i = 0; i < orderAfterSwap.length; i++) {
            System.out.println("index: " + i + "\t\tBefore: " + orderBeforeSwap[i] + "\t\tAfter: " + orderAfterSwap[i]);
        }

        for (int i = 0; i < orderAfterSwap.length - 1; i++) {
            if (orderBeforeSwap[i] == orderAfterSwap[i] && orderBeforeSwap[i + 1] == orderAfterSwap[i + 1]) {
                --distance;
            }
            System.out.println(String.format("Edge %d before: %d - %d after %d - %d", i, orderBeforeSwap[i], orderBeforeSwap[i + 1], orderAfterSwap[i], orderAfterSwap[i + 1]));
        }
        System.out.println("Distance: " + String.valueOf(distance));
        result.getRoute();
    }
}