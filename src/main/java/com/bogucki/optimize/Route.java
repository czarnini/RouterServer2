package com.bogucki.optimize;


import com.bogucki.databse.DistanceHelper;
import com.bogucki.optimize.models.Meeting;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

public class Route {
    private int[] citiesOrder;
    private int[] timeVector;
    private int[] distanceVector;

    private int hourOfStart;
    private DistanceHelper distanceHelper;


    private int infeasiblityFactor;
    private int infeasibleCount;
    private ArrayList<Integer> delayedClients;

    private Route(int routeLength, DistanceHelper helper) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        citiesOrder = new int[routeLength];
        timeVector = new int[routeLength];
        distanceVector = new int[routeLength];
        distanceHelper = helper;
        Arrays.fill(citiesOrder, -1);
        Arrays.fill(timeVector, -1);
        Arrays.fill(distanceVector, -1);
        hourOfStart = 0;//todo calendar.get(Calendar.HOUR_OF_DAY);
        infeasiblityFactor = 0;
        infeasibleCount = 0;
        delayedClients = new ArrayList<>();
    }


    Route(Route baseRoute) {
        this.citiesOrder = baseRoute.getCitiesOrder().clone();
        this.timeVector = baseRoute.timeVector.clone();
        this.distanceVector = baseRoute.distanceVector.clone();
        this.distanceHelper = baseRoute.distanceHelper;
        this.infeasiblityFactor = baseRoute.infeasiblityFactor;
        this.infeasibleCount = baseRoute.infeasibleCount;
        this.delayedClients = baseRoute.delayedClients;
    }

    public int getHourOfStart() {
        return hourOfStart;
    }

    int getCity(int index) {
        return citiesOrder[index];
    }

    private void setCity(int city, int index) {
        this.citiesOrder[index] = city;
    }

    int[] getCitiesOrder() {
        return citiesOrder;
    }

    public int getTimeAt(int city) {
        return timeVector[city];
    }

    void swapForOpt2(int i, int j) {

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
        }
        citiesOrder = Arrays.copyOf(result, result.length);
        countVectors();
    }


    void swap(int from, int to) {
        if (from > citiesOrder.length - 1 || from > citiesOrder.length - 1) {
            System.out.println("FROM " + from + "  TO  " + to + "  len was " + citiesOrder.length);
            return;
        }
        int result[] = new int[citiesOrder.length];
        if (from > to) {
            for (int i = 0; i < result.length; i++) {
                if (i < to || i > from) {
                    result[i] = citiesOrder[i];
                } else if (i == to) {
                    result[i] = citiesOrder[from];
                } else if (i <= from) {
                    result[i] = citiesOrder[i - 1];
                }
            }
        } else {
            for (int i = 0; i < result.length; i++) {
                if (i < from || i > to) {
                    result[i] = citiesOrder[i];
                } else if (i < to) {
                    result[i] = citiesOrder[i + 1];
                } else if (i == to) {
                    result[i] = citiesOrder[from];
                }
            }
        }
        citiesOrder = Arrays.copyOf(result, result.length);
        countVectors();

    }

    static Route getInitialRoute(DistanceHelper helper) {
        Route route = new Route(helper.getMeetings().size(), helper);
        for (int i = 0; i < helper.getMeetings().size(); i++) {
            route.setCity(i, i);
        }
        route.countVectors();
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
        route.countVectors();

        return route;
    }

    private void countVectors() {
        delayedClients = new ArrayList<>();
        infeasiblityFactor = 0;
        infeasibleCount = 0;
        int currentTime = 0;
        int currentDistance = 0;
        for (int i = 0; i < citiesOrder.length - 1; i++) {
            long timeDelta = currentTime - distanceHelper.getMeetings().get(citiesOrder[i]).getLatestTimePossible();
            if (timeDelta > 0) {
                delayedClients.add(i);
                infeasiblityFactor += (int) timeDelta;
                infeasibleCount++;
            }
            distanceVector[i] = currentDistance;
            timeVector[i] = currentTime;
            currentTime += distanceHelper.getTime(citiesOrder[i], citiesOrder[i + 1], hourOfStart + (currentTime / 3600));
            currentDistance += distanceHelper.getTime(citiesOrder[i], citiesOrder[i + 1], 0);

        }

        if ((int) (currentTime - distanceHelper.getMeetings().get(citiesOrder[citiesOrder.length - 1]).getLatestTimePossible()) > 0) {
            infeasibleCount++;
            infeasiblityFactor += (int) (currentTime - distanceHelper.getMeetings().get(citiesOrder[citiesOrder.length - 1]).getLatestTimePossible());
        }

        distanceVector[citiesOrder.length - 1] = currentDistance;
        timeVector[citiesOrder.length - 1] = currentTime;

    }


    public int getCost() {
        return (int) (2 * infeasiblityFactor + timeVector[timeVector.length - 1]);
    }


    /**
     * @param distance musi być parzysty - jedna krawędź to dwa wierzchołki
     * @return
     */
    Route generateNeighbourRoute(int distance) {
        Route result = new Route(this);
        Random generator = new Random();
        int a, b;
        for (int i = 0; i < distance; i++) {
            do {
                a = 1 + generator.nextInt(citiesOrder.length - 1);
                b = a + generator.nextInt(citiesOrder.length - a);
            } while (a == b);
            result.swap(a, b);
        }
        result.countVectors();
        return result;
    }

    public boolean isFeasible() {
        int currentTime = hourOfStart;
        for (int i = 0; i < citiesOrder.length - 1; i++) {
            if (currentTime > distanceHelper.getMeetings().get(citiesOrder[i]).getLatestTimePossible()) {
                return false;
            }
            currentTime += distanceHelper.getTime(citiesOrder[i], citiesOrder[i + 1], currentTime / 3600);
        }
        return true;
    }

    public void getRoute() {
        try {
            int currentTime = 0;
            for (int i = 0; i < citiesOrder.length; i++) {
                int iThCity = citiesOrder[i];
                int hour = timeVector[i] / 3600;
                int minute = (timeVector[i] - hour * 3600) / 60;
                System.out.println(String.format("%1$-" + 40 + "s", distanceHelper.getMeetings().get(iThCity).getAddress()) + "\t(" + getTimeAt(i) + "/" + distanceHelper.getMeetings().get(iThCity).getLatestTimePossible() + ")\tETA: " + String.format("%02d:%02d", hourOfStart + hour, minute));
                if (i + 1 != citiesOrder.length)
                    currentTime += distanceHelper.getTime(citiesOrder[i], citiesOrder[i + 1], hourOfStart + (currentTime / 3600));
            }

            System.out.println("\nclients late: " + infeasibleCount + " Factor " + infeasiblityFactor + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

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


    public int getInfeasiblityFactor() {
        return infeasiblityFactor;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Route && Arrays.equals(citiesOrder, ((Route) obj).citiesOrder);
    }

    public ArrayList<Integer> getDelayedClients() {
        return delayedClients;
    }
}
