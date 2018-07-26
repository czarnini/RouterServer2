package com.bogucki.optimize;


import com.bogucki.databse.DistanceHelper;
import com.bogucki.optimize.models.Meeting;

import java.util.ArrayList;
import java.util.Calendar;

class VNSOptimizer {
    private ArrayList<Meeting> meetings;
    private DistanceHelper distanceHelper;
    static Route currentBest = null;
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
            while (System.currentTimeMillis() - start < 70 * 1000) {
                Route X = createFeasibleRoute();
                Route tmp= (GVNS(X));
                if(tmp.getCost() < currentBest.getCost()){
                    System.out.println(String.format("Prev best %d \t curr best %d", currentBest.getCost(), tmp.getCost()));
                    currentBest = new Route(tmp);
                }
            }

            System.out.println("\n\n\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Route createFeasibleRoute() {
        Route x = opt2(Route.newRandomRoute(distanceHelper));
        while (!x.isFeasible()) {
            int level = 1;
            while (!x.isFeasible() && level < 50) {
                Route xPrim = opt2(x.generateNeighbourRoute(level));
                if (xPrim.getCost() < x.getCost()) {
                    x = new Route(xPrim);
                    level = 1;
                } else {
                    level++;
                }
            }
            x = opt2(Route.newRandomRoute(distanceHelper));
        }
        return x;
    }

    private Route GVNS(Route x) {
        Route localMinimum = new Route(VND(x));
        Route xPrim;
        int level = 1;
        while (level < 50) {
            xPrim = VND(localMinimum.generateNeighbourRoute(level));
            if (xPrim.getCost() < localMinimum.getCost()) {
                localMinimum = new Route(xPrim);
                level = 1;
            } else {
                level++;
            }
        }
        return localMinimum;
    }


    private Route VND(Route x) {
        Route xPrim;
        Route localOptimum = new Route(x);
            do {
                localOptimum = local1Shift(localOptimum);
                xPrim = opt2(x);
            } while (!x.equals(xPrim));

        return localOptimum;
    }

    public Route local1Shift(Route x) {
        Route localBest = new Route(x);
        Route xPrim = new Route(x);
        for (int i = 1; i < meetings.size(); i++) {
            for (int j = i + 1; j < meetings.size() - 1; j++) {
                xPrim.swap(i, j);
                if (xPrim.getCost() < localBest.getCost()) {
                    localBest = new Route(xPrim);
                }
            }
        }
        return localBest;
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
                        if(opt2ResultLocal.getCitiesOrder()[0] == opt2ResultLocal.getCitiesOrder()[1]){
                            System.out.println("OOOOOOOOOOO");
                        }
                    }
                }
            }
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

        return route;

    }
}
