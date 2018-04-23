package com.bogucki.optimize;

import com.bogucki.databse.DistanceHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class RouteTest {

    private String[] addresses = new String [] {"janowskiego 13,warszawa", "wspólna 73,warszawa", "mielczarskiego 10,warszawa", "konduktorska 2,warszawa",
            "komorska 29/33,warszawa", "mielczarskiego 10,warszawa"};
    private ArrayList<Meeting2> meetings = new ArrayList<>();
    private Route route;

    @Before
    public void setUp() throws Exception {
        for (String address : addresses) {
            meetings.add(new Meeting2(address));
        }
        route = Route.getInitialRoute(meetings.size(), new DistanceHelper(meetings));
        route.getRoute();

    }

    @Test
    public void shouldSwapRouteBetweenFirstElementWithForth() throws Exception {
        int [] orderBeforeSwap = route.getCitiesOrder();
        int costBeforeSwap = route.getCost();
        route.swap(1+1, 4-1);
        int[] orderAfterSwap = route.getCitiesOrder();
        route.getRoute();

        System.out.println(costBeforeSwap + "   " + route.getCost());
        for (int i = 0; i < orderAfterSwap.length; i++) {
            System.out.println("index: " + i +"\t\tBefore: " + orderBeforeSwap[i] +"\t\tAfter: " + orderAfterSwap[i]);
        }
    }


    @Test
    public void shouldGenerateNeighbourdRoute() throws Exception {
        int [] orderBeforeSwap = route.getCitiesOrder();
        int[] orderAfterSwap =  route.generateNeighbourRoute(2).getCitiesOrder();
        for (int i = 0; i < orderAfterSwap.length; i++) {
            System.out.println("index: " + i +"\t\tBefore: " + orderBeforeSwap[i] +"\t\tAfter: " + orderAfterSwap[i]);
        }
    }
}