package com.bogucki.optimize;


import com.bogucki.databse.DistanceHelper;

import java.util.Arrays;
import java.util.Random;

/**
 * Klasa opisująca trasę przejazdu. Reprezentacją trasy będzie tablica indeksów.
 */
public class Route {
    private int[] citiesOrder;
    private DistanceHelper distanceHelper;
    private int cost;
    private boolean feasible;

    private Route(int routeLength, DistanceHelper helper) {
        citiesOrder = new int[routeLength];
        distanceHelper = helper;
        Arrays.fill(citiesOrder, -1);
    }


    Route(Route baseRoute) {
        this.citiesOrder = baseRoute.getCitiesOrder().clone();
        this.distanceHelper = baseRoute.distanceHelper;
        this.cost = baseRoute.cost;
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


    void swap(int i, int j) {
        int result[] = new int[citiesOrder.length];

        for (int tmp = 0; tmp < result.length; tmp++) {
            if (tmp >= i && tmp <= j) {
                result[tmp] = citiesOrder[j - (tmp - i)];
            } else {
                result[tmp] = citiesOrder[tmp];
            }
        }

        for (int tmp = i; tmp <= j + 1; tmp++) {
            cost -= distanceHelper.getTime(getCity(tmp - 1), getCity(tmp), 9);
            cost += distanceHelper.getTime(result[tmp - 1], result[tmp], 9);
        }

        citiesOrder = Arrays.copyOf(result, result.length);
    }

    static Route getInitialRoute(int size, DistanceHelper helper) {
        Route route = new Route(size, helper);
        for (int i = 0; i < size; i++) {
            route.setCity(i, i);
        }
        route.countCost();
        return route;
    }

    static Route newRandomRoute(int size, DistanceHelper helper) {
        Random generator = new Random();
        Route route = new Route(size, helper);
        for (int i = 0; i < size; i++) {
            int insertIndex;
            do {
                insertIndex = generator.nextInt(size);
            } while (route.getCity(insertIndex) != -1);
            route.setCity(i, insertIndex); //insertIndex
        }
        route.countCost();
        return route;
    }

    synchronized void countCost() {
        int result = 0;
        for (int i = 0; i < citiesOrder.length - 1; i++) {
            result += distanceHelper.getTime(citiesOrder[i], citiesOrder[i + 1], 9);
        }
        cost = result;
    }

    public int getCost() {
        return cost;
    }


    /**
     * @param distance musi być parzysty - jedna krawędź to dwa wierzchołki
     * @return
     */
    Route generateNeighbourRoute(int distance) {
        Route result = new Route(this);
        for (int i = 0; i < distance-1; i++) {
            int tmp = result.getCity(i);
            result.setCity(getCity(i+1), i);
            result.setCity(tmp, i+1);
        }

        for (int i = 0; i < distance; i++) {
            result.setCost(
                    result.getCost()
                            - distanceHelper.getTime(citiesOrder[i],citiesOrder[i+1],9)
                            + distanceHelper.getTime(result.getCity(i),result.getCity(i+1),9)
            );

        }
        return result;
    }

    public boolean isFeasible() {
        int currentTime = 0;
/*        for (int i = 0; i < citiesOrder.length - 1; i++) {
            if (currentTime > distanceHelper.getMeetings().get(citiesOrder[i]).getLatestTimePossible()) {
                return false;
            }
            currentTime += distanceHelper.getTime(citiesOrder[i], citiesOrder[i + 1], currentTime) / 3600;
        }*/
        return true;
    }

    public void getRoute() {
        try {
            int currentTime = 0;
            for (int i = 0; i < citiesOrder.length - 1; i++) {
                int index = citiesOrder[i];
                System.out.println(distanceHelper.getMeetings().get(index).getAddress() + " ETA: " + currentTime);
                currentTime += distanceHelper.getTime(citiesOrder[i], citiesOrder[i + 1], 9);
            }
            System.out.println(distanceHelper.getMeetings().get(citiesOrder.length - 1).getAddress() + " ETA: " + currentTime);


            System.out.println("\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void setCost(int cost) {
        this.cost = cost;
    }
}
