package com.bogucki.optimize;


import com.bogucki.databse.DistanceHelper;
import com.bogucki.optimize.models.Meeting;

import java.util.ArrayList;

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
            while (System.currentTimeMillis() - start < 5 * 1000) {
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
                    myCurrentBest = Route.newRandomRoute(meetings.size(), distanceHelper);
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
        myCurrentBest = Route.getInitialRoute(meetings.size(), distanceHelper);
        if (null == currentBest) {
            currentBest = new Route(myCurrentBest);
        }
    }


    public Route opt2(Route opt2ResultLocal) {
        int distA;
        int distB;
        for (int i = 0; i < meetings.size() - 2; i++) {
            for (int j = i + 2; j < meetings.size() - 1; j++) {

                int IthCity = opt2ResultLocal.getCity(i), IthPlusOneCity = opt2ResultLocal.getCity(i + 1),
                        jThCity = opt2ResultLocal.getCity(j), jThPlusOneCity = opt2ResultLocal.getCity(j + 1);

                distA = distanceHelper.getTime(IthCity, IthPlusOneCity, 9) + distanceHelper.getTime(jThCity, jThPlusOneCity, 9);
                distB = distanceHelper.getTime(IthCity, jThCity, 9) + distanceHelper.getTime(IthPlusOneCity, jThPlusOneCity, 9);

                if (distA > distB) {
                    opt2ResultLocal.swap(i + 1, j - 1);
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
