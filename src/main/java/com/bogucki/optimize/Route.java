package com.bogucki.optimize;


import com.bogucki.databse.DistanceHelper;
import com.bogucki.optimize.models.Meeting;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

public class Route {
    private int[] citiesOrder;
    private int[] costVector;
    private int hourOfStart;
    private DistanceHelper distanceHelper;
    private int cost;
    private boolean feasible;
    private int feasiblityRating;

    private Route(int routeLength, DistanceHelper helper) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        citiesOrder = new int[routeLength];
        costVector = new int[routeLength];
        distanceHelper = helper;
        Arrays.fill(citiesOrder, -1);
        Arrays.fill(costVector, -1);
        hourOfStart = 0;//todo calendar.get(Calendar.HOUR_OF_DAY);
    }


    Route(Route baseRoute) {
        this.citiesOrder = baseRoute.getCitiesOrder().clone();
        this.costVector = baseRoute.getCostVector().clone();
        this.distanceHelper = baseRoute.distanceHelper;
        this.cost = baseRoute.cost;
    }

    public int getHourOfStart() {
        return hourOfStart;
    }

    public int getCity(int index) {
        return citiesOrder[index];
    }

    private void setCity(int city, int index) {
        this.citiesOrder[index] = city;
    }

    public int[] getCitiesOrder() {
        return citiesOrder;
    }

    public int[] getCostVector() {
        return costVector;
    }

    public void setCostVector(int[] costVector) {
        this.costVector = costVector;
    }

    public int getCostAt(int city) {
        return costVector[city];
    }

    void swap(int i, int j) {

        int result[] = new int[citiesOrder.length];
        for (int tmp = 0; tmp < result.length; tmp++) {
            if (tmp >= i && tmp <= j) {
                result[tmp] = citiesOrder[j - (tmp - i)];
            } else {
                result[tmp] = citiesOrder[tmp];
            }
        }

        for (int tmp = i - 1; tmp <= j; tmp++) {
            if (tmp == result.length - 1) {
                break;
            }
            cost -= distanceHelper.getTime(getCity(tmp), getCity(tmp + 1), 9);
            cost += distanceHelper.getTime(result[tmp], result[tmp + 1], 9);
        }
        citiesOrder = Arrays.copyOf(result, result.length);
        countCostVector();

    }

    static Route getInitialRoute(DistanceHelper helper) {
        Route route = new Route(helper.getMeetings().size(), helper);
        for (int i = 0; i < helper.getMeetings().size(); i++) {
            route.setCity(i, i);
        }
        route.countCost();
        route.countCostVector();
        return route;
    }

    static Route newRandomRoute(DistanceHelper helper) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        Random generator = new Random();
        Route route = new Route(helper.getMeetings().size(), helper);
        route.setCity(0, 0);
        int insertIndex;

        for (int i = 1; i < helper.getMeetings().size(); i++) {
            do {
                insertIndex = 1 + generator.nextInt(helper.getMeetings().size() - 1);
            } while (route.getCity(insertIndex) != -1);
            route.setCity(i, insertIndex);
        }
        route.countCost();
        route.countCostVector();

        return route;
    }

    synchronized void countCost() {
        int result = 0;
        for (int i = 0; i < citiesOrder.length - 1; i++) {
            result += distanceHelper.getTime(citiesOrder[i], citiesOrder[i + 1], hourOfStart + (result / 3600));
        }
        cost = result;
    }

    synchronized void countCostVector() {
        int result = 0;

        for (int i = 0; i < citiesOrder.length - 1; i++) {
            costVector[i] = result;
            result += distanceHelper.getTime(citiesOrder[i], citiesOrder[i + 1], hourOfStart + (result / 3600));
        }
        costVector[citiesOrder.length - 1] = result;

    }


    public int getCost() {
        return costVector[costVector.length - 1] + distanceHelper.getTime(citiesOrder[citiesOrder.length - 1], citiesOrder[0], 0) + feasiblityRating;
    }


    /**
     * @param distance musi być parzysty - jedna krawędź to dwa wierzchołki
     * @return
     */
    Route generateNeighbourRoute(int distance) {
        Route result = new Route(this);
        Random generator = new Random();
        int a,b;
        for (int i = 0; i < distance; i++) {
            do{
                a = 1 + generator.nextInt(citiesOrder.length - 1);
                b = a + generator.nextInt(citiesOrder.length - a);
            }while(a==b);
            result.swap(a,b);
        }
        result.countCostVector();
        return result;
    }

    public boolean isFeasible() {
        int currentTime = hourOfStart;
        for (int i = 0; i < citiesOrder.length - 1; i++) {
            if (currentTime > distanceHelper.getMeetings().get(citiesOrder[i]).getLatestTimePossible()) {
                return false;
            }
            currentTime += distanceHelper.getTime(citiesOrder[i], citiesOrder[i + 1], currentTime);// 3600;
        }
        return true;
    }


    public void countFeasibilityRating() {
        int result = 0;
        int currentTime = hourOfStart;
        int timeDiff;

        for (int i = 0; i < citiesOrder.length - 1; i++) {
            timeDiff = (int) (currentTime -  distanceHelper.getMeetings().get(citiesOrder[i]).getLatestTimePossible());
            if ( timeDiff > 0) {
                result += 0.5 * timeDiff;
            }
            currentTime += distanceHelper.getTime(citiesOrder[i], citiesOrder[i + 1], currentTime);// 3600;
        }
        feasiblityRating = result;
    }

    public void getRoute() {
        try {
            int currentTime = 0;
            for (int i = 0; i < citiesOrder.length; i++) {
                int iThCity = citiesOrder[i];
                int hour = costVector[i] / 3600;
                int minute = (costVector[i] - hour * 3600) / 60;
                System.out.println(String.format("%1$-" + 40 + "s", distanceHelper.getMeetings().get(iThCity).getAddress()) + "\t(" + currentTime + " // "+  String.valueOf(distanceHelper.getMeetings().get(iThCity).getLatestTimePossible() - currentTime)  + ")\tETA: " + String.format("%02d:%02d", hourOfStart + hour, minute));
                if (i + 1 != citiesOrder.length)
                    currentTime += distanceHelper.getTime(citiesOrder[i], citiesOrder[i + 1], 0);
            }

            System.out.println("\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void setCost(int cost) {
        this.cost = cost;
    }

    public URI getGoogleMapsUrl() throws URISyntaxException {
        StringBuilder result = new StringBuilder("https://www.google.pl/maps/dir/");
        for (Meeting meeting :
                distanceHelper.getMeetings()) {
            result.append(meeting.getAddress().replaceAll(" ", "%20") + "/");
        }

        System.out.println(result.toString());

        return new URI(result.toString());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Route && Arrays.equals(citiesOrder, ((Route) obj).citiesOrder);
    }
}
