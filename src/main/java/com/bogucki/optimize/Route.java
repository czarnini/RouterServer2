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

        for (int tmp = 0; tmp < result.length; tmp++) {
            if (tmp >= i && tmp <= j) {
                result[tmp] = citiesOrder[j - (tmp - i)];
            } else {
                result[tmp] = citiesOrder[tmp];
            }
        }

        for (int tmp = i; tmp <= j +1 ; tmp++) {
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
        result.countCost();
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
                /*System.out.println(
                        String.format("Time form %s to %s is: %d seconds",
                        distanceHelper.getMeetings().get(index).getAddress(),
                        distanceHelper.getMeetings().get(citiesOrder[i+1]).getAddress(),
                        distanceHelper.getTime(citiesOrder[i], citiesOrder[i + 1], 9 )
                        )
                );
*/
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
