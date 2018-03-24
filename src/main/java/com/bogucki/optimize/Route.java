package com.bogucki.optimize;


import com.bogucki.databse.DistanceHelper;

import java.util.ArrayList;
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
        for (int k = 0; k < result.length; k++) {
            if (k > i && k < j) {
                result[k] = citiesOrder[j - (k-i)];
            } else {
                result[k] = citiesOrder[k];
            }
        }
        citiesOrder = Arrays.copyOf(result,result.length);
    }

    static Route newRandomRoute(int size, DistanceHelper helper) {
        Random generator = new Random();
        Route route = new Route(size, helper);
        for (int i = 0; i < size; i++) {
            int insertIndex;
            do {
                insertIndex = generator.nextInt(size);
            } while (route.getCity(insertIndex) != -1);
            route.setCity(i, insertIndex);
        }
        return route;
    }

    synchronized void countCost() {
        int result = 0;
        for (int i = 0; i < citiesOrder.length - 1; i++) {
            result += distanceHelper.getTime(citiesOrder[i], citiesOrder[i + 1], 0);
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
    Route generateNeightbourRoute(int distance) {
        ArrayList<Integer> indexesToBeMoved = new ArrayList<>();
        Random generator = new Random();
        int protectedIndex;
        for (int i = 0; i < distance; i++) {
            do {
                protectedIndex = generator.nextInt(citiesOrder.length);
            } while (indexesToBeMoved.indexOf(protectedIndex) != -1);
            indexesToBeMoved.add(protectedIndex);
        }

        Route result = new Route(this);
        for (int i = 0; i < indexesToBeMoved.size() - 1; i += 2) {
            result.setCity(citiesOrder[indexesToBeMoved.get(i)], indexesToBeMoved.get(i + 1));
            result.setCity(citiesOrder[indexesToBeMoved.get(i + 1)], indexesToBeMoved.get(i));
        }

        return result;
    }

    public boolean isFeasible() {
        int currentTime = 0;
        for (int i = 0; i < citiesOrder.length - 1; i++) {
            if (currentTime > distanceHelper.getMeetings().get(citiesOrder[i]).getLatestTimePossible()) {
                return false;
            }
            currentTime += distanceHelper.getTime(citiesOrder[i], citiesOrder[i + 1], currentTime) / 3600;
        }
        return true;
    }

    public void getRoute() {
        try {


            int currentTime = 0;
            for (int i = 0; i < citiesOrder.length - 1; i++) {
                int index = citiesOrder[i];
                System.out.println(distanceHelper.getMeetings().get(index).getAddress() + " ETA: " + currentTime / 3600);
                currentTime += distanceHelper.getTime(citiesOrder[i], citiesOrder[i + 1], currentTime / 3600);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
