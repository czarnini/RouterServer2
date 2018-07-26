package com.bogucki.optimize;


import com.bogucki.databse.DistanceHelper;
import com.bogucki.optimize.models.Meeting;

import java.util.ArrayList;
import java.util.Calendar;

class VNSOptimizer {
    private ArrayList<Meeting> meetings;
    private DistanceHelper distanceHelper;
    static  Route currentBest = null;
    private static int INITIAL_DISTANCE = 1;
    private static int DISTANCE_STEP = 1;

    VNSOptimizer(DistanceHelper distanceHelper) {
        this.distanceHelper = distanceHelper;
        this.meetings = distanceHelper.getMeetings();
        initialize();
    }

    void optimize() {
        try {
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 30 * 1000) {
                int distance = INITIAL_DISTANCE;
                while (distance < meetings.size()) {
                    Route opt2Result = opt2(currentBest.generateNeighbourRoute(distance));
                    opt2Result.countFeasibilityRating();
                        if (isBetterRouteFound(opt2Result)) {
                            distance = INITIAL_DISTANCE;
                        } else {
                            distance += DISTANCE_STEP;
                        }
                }
                currentBest = Route.newRandomRoute(distanceHelper);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    synchronized private boolean isBetterRouteFound(Route opt2Result) {
        if ((opt2Result.getCost() >= currentBest.getCost())) {
            return false;
        } else {
            opt2Result.getRoute();
            System.out.println(Thread.currentThread().getName() + "new best found! " + String.format("%.2f", opt2Result.getCost() / 1.0));
            currentBest = new Route(opt2Result);
            return true;
        }
    }

    private synchronized void initialize() {
        if (null == currentBest) {
            currentBest = Route.getInitialRoute(distanceHelper);
        }
    }


    Route opt2(Route opt2ResultLocal) {
        int distA;
        int distB;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int prevCost = Integer.MAX_VALUE;
        while (opt2ResultLocal.getCost() < prevCost) {
            for (int i = 0; i < meetings.size() - 2; i++) {
                for (int j = i + 2; j < meetings.size() - 1; j++) {
                    prevCost = opt2ResultLocal.getCost();
                    int IthCity = opt2ResultLocal.getCity(i);
                    int IthPlusOneCity = opt2ResultLocal.getCity(i + 1);
                    int jThCity = opt2ResultLocal.getCity(j);
                    int jThPlusOneCity = opt2ResultLocal.getCity(j + 1);

                    int timeOfStartFromIth = opt2ResultLocal.getHourOfStart() + opt2ResultLocal.getCostAt(i) / 3600;
                    int timeOfStartFromJth = opt2ResultLocal.getHourOfStart() + opt2ResultLocal.getCostAt(j) / 3600;

                    distA = distanceHelper.getTime(IthCity, IthPlusOneCity, timeOfStartFromIth) + distanceHelper.getTime(jThCity, jThPlusOneCity, timeOfStartFromJth);
                    distB = distanceHelper.getTime(IthCity, jThCity, timeOfStartFromIth) + distanceHelper.getTime(IthPlusOneCity, jThPlusOneCity, timeOfStartFromJth);

                    if (distA > distB) {
                        opt2ResultLocal.swap(i + 1, j);
                    }

                }
            }

        }

        opt2ResultLocal.countCostVector();
        return opt2ResultLocal;
    }


    synchronized Route getCurrentBest() {
        return currentBest;
    }

    public static Route recalculate(DistanceHelper distanceHelper) {
        System.out.println("Updating");
        Route route = Route.getInitialRoute(distanceHelper);
        route.countCost();
        route.getRoute();

        return  route;

    }
}
