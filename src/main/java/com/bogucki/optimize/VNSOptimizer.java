package com.bogucki.optimize;


import com.bogucki.databse.DistanceHelper;
import com.bogucki.optimize.models.Meeting;

import java.util.ArrayList;
import java.util.Calendar;

class VNSOptimizer {
    private volatile ArrayList<Meeting> meetings;
    private volatile DistanceHelper distanceHelper;

    static volatile Route currentBest = null;

    private Route myCurrentBest = null;


    private static int INITIAL_DISTANCE = 2;
    private static int DISTANCE_STEP = 1;

    VNSOptimizer(DistanceHelper distanceHelper) {
        this.distanceHelper = distanceHelper;
        this.meetings = distanceHelper.getMeetings();
        initialize();
    }

    void optimize() {
        try {
            long start = System.currentTimeMillis();
            int i = 0;
            int lastSuccessIndex = 0;
            while (System.currentTimeMillis() - start < 50 * 1000) {
                ++i;
                int distance = INITIAL_DISTANCE;
                int notFeasibleCount = 0;
                while (distance < meetings.size()) {
                    Route opt2Result = opt2(myCurrentBest.generateNeighbourRoute(distance));
                    if (opt2Result.isFeasible()) {
                        if (isBetterRouteFound(opt2Result)) {
                            distance = INITIAL_DISTANCE;
                            lastSuccessIndex = i;
                        } else {
                            distance += DISTANCE_STEP;
                        }
                    } else {
                        notFeasibleCount++;
                        if (notFeasibleCount > 1000) {
                            break;
                        }
                    }
                }

                if (i - lastSuccessIndex > 10000) {
                    myCurrentBest = Route.newRandomRoute(distanceHelper);
                }

                if (i - lastSuccessIndex > 1000000) {
                    break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    synchronized private boolean isBetterRouteFound(Route opt2Result) {
        if ((opt2Result.getCost() >= currentBest.getCost())) {
            return false;
        } else {
            System.out.println(Thread.currentThread().getName() + "new best found! " + String.format("%.2f", opt2Result.getCost() / 3600.0));
            currentBest = new Route(opt2Result);
            myCurrentBest = new Route(currentBest);
            return true;
        }
    }

    private synchronized void initialize() {
        myCurrentBest = Route.getInitialRoute(distanceHelper);
        if (null == currentBest) {
            currentBest = new Route(myCurrentBest);
        }
    }


    Route opt2(Route opt2ResultLocal) {
        int distA;
        int distB;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        for (int i = 0; i < meetings.size() - 2; i++) {
            for (int j = i + 2; j < meetings.size() - 1; j++) {

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


        return opt2ResultLocal;
    }


    synchronized Route getCurrentBest() {
        //currentBest.getRoute();
        return currentBest;
    }

}
