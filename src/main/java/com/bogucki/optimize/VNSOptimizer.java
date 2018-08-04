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
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 10 * 1000) {
                Route X = createFeasibleRoute();
                Route tmp = (GVNS(X));
                if (tmp.getCost() < currentBest.getCost()) {
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
            while (!x.isFeasible() && level < 52) {
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
        while (level < 5) {
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
        Route a, b;
        Route localOptimum = new Route(x);
        do {
            a = local1Shift(localOptimum);
            b = opt2(a);
        } while (!a.equals(b));
        return b;
    }

    Route local1Shift(Route x) {
        int fromCity, toCity;
        Route localBest = new Route(x);
        Route xPrim = new Route(x);
        for (int from = 1; from < meetings.size(); from++) {
            for (int to = 1; to < meetings.size(); to++) {
                fromCity = xPrim.getCity(from);
                toCity = xPrim.getCity(to);
                if (from == to) {
                    continue;
                }else if(fromCity < toCity){
                    if(isFrobidedArc(fromCity, toCity)){
                        continue;
                    } if(isFrobidedArc(fromCity-1, fromCity+1)){
                        continue;
                    } if(toCity != meetings.size()-1 && isFrobidedArc(fromCity, toCity+1)){
                        continue;
                    }
                }else {
                    if(isFrobidedArc(fromCity, toCity)){
                        continue;
                    } if(isFrobidedArc(toCity-1, fromCity)){
                        continue;
                    } if(fromCity != meetings.size()-1 && isFrobidedArc(fromCity-1, fromCity+1)){
                        continue;
                    }
                }
                xPrim.swap(from, to);
                if (xPrim.getCost() < localBest.getCost()) {
                    localBest = new Route(xPrim);
                }
            }
        }
        return localBest;
    }

    private boolean isFrobidedArc(int from, int to) {
        return arcsToAvoid.get(from).indexOf(to) != -1;
    }


    private synchronized void initialize() {
        if (null == currentBest) {
            currentBest = Route.getInitialRoute(distanceHelper);
        }
        generateArcsToAvoid();
    }

    private void generateArcsToAvoid() {
        arcsToAvoid = new HashMap<>();
        for (int from = 0, meetingsSize = meetings.size(); from < meetingsSize; from++) {
            arcsToAvoid.put(from, new ArrayList<>());
            for (int to = 0, meetingsSize1 = meetings.size(); to < meetingsSize1; to++) {
                if (meetings.get(from).getEarliestTimePossible() + distanceHelper.getTime(from, to, (int) meetings.get(from).getEarliestTimePossible()) > meetings.get(to).getLatestTimePossible()) {
                    arcsToAvoid.get(from).add(to);
                }
            }
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

                int timeOfStartFromIth = 0;//opt2ResultLocal.getHourOfStart() + opt2ResultLocal.getCostAt(i) / 3600;
                int timeOfStartFromJth = 0;//opt2ResultLocal.getHourOfStart() + opt2ResultLocal.getCostAt(j) / 3600;

                distA = distanceHelper.getTime(IthCity, IthPlusOneCity, timeOfStartFromIth) + distanceHelper.getTime(jThCity, jThPlusOneCity, timeOfStartFromJth);
                distB = distanceHelper.getTime(IthCity, jThCity, timeOfStartFromIth) + distanceHelper.getTime(IthPlusOneCity, jThPlusOneCity, timeOfStartFromJth);

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
        route.countCost();
        route.getRoute();

        return route;

    }
}
