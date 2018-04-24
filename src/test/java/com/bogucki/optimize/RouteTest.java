package com.bogucki.optimize;

import com.bogucki.databse.DistanceHelper;
import com.bogucki.optimize.models.Meeting;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class RouteTest {

    private String[] addresses = new String[]{"janowskiego 13,warszawa", "wspólna 73,warszawa", "mielczarskiego 10,warszawa", "konduktorska 2,warszawa",
            "komorska 29/33,warszawa", "mielczarskiego 10,warszawa", "komorska 29/33,warszawa", "mielczarskiego 10,warszawa", "komorska 29/33,warszawa", "mielczarskiego 10,warszawa"};
    private ArrayList<Meeting> meetings = new ArrayList<>();
    private Route route;

    @Before
    public void setUp() throws Exception {
        for (String address : addresses) {
            meetings.add(new Meeting(address));
        }
        route = Route.getInitialRoute(meetings.size(), new DistanceHelper(meetings));
        route.getRoute();

    }

    @Test
    public void shouldSwapRouteBetweenFirstElementWithForth() throws Exception {
        int[] orderBeforeSwap = route.getCitiesOrder();
        int costBeforeSwap = route.getCost();
        route.swap(1, 4);
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