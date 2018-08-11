package com.bogucki.optimize;


import com.bogucki.databse.DistanceHelper;
import com.bogucki.optimize.models.Meeting;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

class VNSOptimizer {
    private ArrayList<Meeting> meetings;
    private DistanceHelper distanceHelper;
    static Route currentBest = null;
    private HashMap<Integer, ArrayList<Integer>> arcsToAvoid;

    VNSOptimizer(DistanceHelper distanceHelper) {
        this.distanceHelper = distanceHelper;
        this.meetings = distanceHelper.getMeetings();
        initialize();
    }


    void optimize() {
        try {
            currentBest = Route.newRandomRoute(distanceHelper);
            generateArcsToAvoid();
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 8 * 1000) {
                Route X = Route.newRandomRoute(distanceHelper);
                Route tmp = (GVNS(X));
                if (tmp.getCost() < currentBest.getCost()) {
                    currentBest = new Route(tmp);
                }
            }
            System.out.println(currentBest.getCost() + "\t" + currentBest.getInfeasiblityFactor() / 3600.0 + "\t" + currentBest.getDelayedClients());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Route GVNS(Route x) {
        Route localMinimum = new Route(VND(x));
        Route xPrim;
        int level = 1;
        while (level < meetings.size()) {
            xPrim = VND(localMinimum.generateNeighbourRoute(level));
            if (xPrim.getCost() < localMinimum.getCost()) {
                localMinimum = new Route(xPrim);
                level = 1;
            } else {
                level += 1;
            }
        }
        return localMinimum;
    }


    private Route VND(Route x) {
/*        Route a, b;
        Route localOptimum = new Route(x);
        do {
            a = local1Shift(localOptimum, true);
            b = opt2(a);
        } while (!a.equals(b));*/

        return opt2(x);
    }

    Route local1Shift(Route x, boolean isContructionPhase) {
        int fromCity, toCity;
        Route localBest = new Route(x);
        Route xPrim = new Route(x);
        if (isContructionPhase) {
            ArrayList<Integer> foo = xPrim.getDelayedClients();
            for (int from : foo) {
                for (int to = 1; to < from; to++) {
                    xPrim.swap(from, to);
                    if (xPrim.getInfeasiblityFactor() < localBest.getInfeasiblityFactor()) {
                        localBest = new Route(xPrim);
                    }
                }
            }

        } else {
            for (int from = 1; from < meetings.size(); from++) {
                for (int to = 1; to < meetings.size(); to++) {
                    fromCity = xPrim.getCity(from);
                    toCity = xPrim.getCity(to);
                    if (from == to) {
                        continue;
                    } else if (fromCity < toCity) {
                        if (isForbiddenArc(fromCity, toCity)) {
                            continue;
                        }
                        if (isForbiddenArc(fromCity - 1, fromCity + 1)) {
                            continue;
                        }
                        if (toCity != meetings.size() - 1 && isForbiddenArc(fromCity, toCity + 1)) {
                            continue;
                        }
                    } else {
                        if (isForbiddenArc(fromCity, toCity)) {
                            continue;
                        }
                        if (isForbiddenArc(toCity - 1, fromCity)) {
                            continue;
                        }
                        if (fromCity != meetings.size() - 1 && isForbiddenArc(fromCity - 1, fromCity + 1)) {
                            continue;
                        }
                    }
                    xPrim.swap(from, to);
                    if (xPrim.getCost() < localBest.getCost()) {
                        localBest = new Route(xPrim);
                    }
                }
            }
        }
        return localBest;
    }

    private boolean isForbiddenArc(int from, int to) {
        return arcsToAvoid.get(from).indexOf(to) != -1;
    }


    private void initialize() {
        if (null == currentBest) {
            currentBest = Route.getInitialRoute(distanceHelper);
        }
    }

    private void generateArcsToAvoid() {
        arcsToAvoid = new HashMap<>();
        for (int from = 0, meetingsSize = meetings.size(); from < meetingsSize; from++) {
            arcsToAvoid.put(from, new ArrayList<>());
            for (int to = 0, meetingsSize1 = meetings.size(); to < meetingsSize1; to++) {
                if (shouldAvoid(from, to)) {
                    arcsToAvoid.get(from).add(to);
                }
            }
        }
    }

    private boolean shouldAvoid(int from, int to) {
        return meetings.get(from).getEarliestTimePossible() + distanceHelper.getTime(from, to, (int) meetings.get(from).getEarliestTimePossible() / 3600) > meetings.get(to).getLatestTimePossible();
    }


    Route opt2(Route opt2ResultLocal) //todo sprawdzanie zakazanych  łuków
    {
        int distA;
        int distB;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        for (int i = 0; i < meetings.size() - 2; i++) {
            for (int j = i + 2; j < meetings.size() - 1; j++) {
                int ithCity = opt2ResultLocal.getCity(i);
                int IthPlusOneCity = opt2ResultLocal.getCity(i + 1);
                int jThCity = opt2ResultLocal.getCity(j);
                int jThPlusOneCity = opt2ResultLocal.getCity(j + 1);

                int timeOfStartFromIth = opt2ResultLocal.getHourOfStart() + opt2ResultLocal.getTimeAt(i) / 3600;
                int timeOfStartFromJth = opt2ResultLocal.getHourOfStart() + opt2ResultLocal.getTimeAt(j) / 3600;

                distA = distanceHelper.getTime(ithCity, IthPlusOneCity, timeOfStartFromIth) + distanceHelper.getTime(jThCity, jThPlusOneCity, timeOfStartFromJth);
                distB = distanceHelper.getTime(ithCity, jThCity, timeOfStartFromIth) + distanceHelper.getTime(IthPlusOneCity, jThPlusOneCity, timeOfStartFromJth);

                if (distA > distB) {
                    opt2ResultLocal.swapForOpt2(i + 1, j);
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
        route.getRoute();
        return route;

    }
}
