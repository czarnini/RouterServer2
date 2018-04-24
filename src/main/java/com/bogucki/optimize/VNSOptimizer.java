package com.bogucki.optimize;


import com.bogucki.databse.DistanceHelper;

import java.util.ArrayList;

public class VNSOptimizer {
    private volatile ArrayList<Meeting2> meetings;
    private volatile DistanceHelper distanceHelper;

    public static volatile Route currentBest = null;

    private Route myCurrentBest = null;
    private Route inputRoute = null;


    private static int INITIAL_DISTANCE = 4;

    private static int DISTANCE_STEP = 8;

    public VNSOptimizer(ArrayList<Meeting2> meetings) {
        this.meetings = meetings;
        distanceHelper = new DistanceHelper(meetings);
    }

    public void optimize() {
        try {
            initialize();
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
                        if (getDistance(opt2Result)) {

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
                    //  myCurrentBest.countCost();
                }

                if (i - lastSuccessIndex > 1000000) {
                    break;
                }

            }
        } catch (
                Exception e)

        {
            e.printStackTrace();
        }

    }

    synchronized private boolean getDistance(Route opt2Result) {
//       currentBest.countCost();
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
        inputRoute = Route.getInitialRoute(meetings.size(), distanceHelper);
        myCurrentBest = Route.getInitialRoute(meetings.size(), distanceHelper);
        if (null == currentBest) {
            currentBest = new Route(myCurrentBest);
        }
    }


    public Route opt2(Route opt2ResultLocal) {
        int distA ;
        int distB;
        for (int i = 0; i < meetings.size() - 2; i++) {
            for (int j = i + 2; j < meetings.size() - 1; j++) {

                int IthCity = opt2ResultLocal.getCity(i), IthPlusOneCity = opt2ResultLocal.getCity(i + 1),
                        jThCity = opt2ResultLocal.getCity(j), jThPlusOneCity = opt2ResultLocal.getCity(j + 1);

                distA = distanceHelper.getTime(IthCity, IthPlusOneCity, 9) + distanceHelper.getTime(jThCity, jThPlusOneCity, 9);
                distB = distanceHelper.getTime(IthCity, jThCity, 9) + distanceHelper.getTime(IthPlusOneCity, jThPlusOneCity, 9);

                if (distA > distB) {
                    opt2ResultLocal.swap(i+1, j-1);
                }
            }
        }

        return opt2ResultLocal;
    }


    public synchronized Route getCurrentBest() {
        System.out.println(currentBest.getCost() + " vs " + inputRoute.getCost() +" better than input route found? " + (inputRoute.getCost() < currentBest.getCost() )+ "");
        return inputRoute.getCost() < currentBest.getCost() ? inputRoute : currentBest;
    }

}
