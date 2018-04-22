package com.bogucki.optimize;


import com.bogucki.databse.DistanceHelper;

import java.util.ArrayList;

public class VNSOptimizer {
    private volatile ArrayList<Meeting2> meetings;
    private volatile DistanceHelper distanceHelper;

    public static volatile Route currentBest = null;

    private Route myCurrentBest = null;


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
            int i =0;
            int lastSuccessIndex = 0;
            while (System.currentTimeMillis() - start < 5 *1000) {
                ++i;
                int distance = INITIAL_DISTANCE;
                int notFeasibleCount = 0;
                while (distance < meetings.size()) {
                    Route opt2Result = opt2(myCurrentBest.generateNeightbourRoute(distance));
                    if (opt2Result.isFeasible()) {
                        if ( getDistance( opt2Result) ){

                            distance = INITIAL_DISTANCE;
                            lastSuccessIndex = i;
                        } else {
                            distance += DISTANCE_STEP;
                        }
                    } else {
                        notFeasibleCount++;
                        if(notFeasibleCount > 1000){
                            break;
                        }
                    }
                }

                if(i - lastSuccessIndex > 10000){
                    myCurrentBest = Route.newRandomRoute(meetings.size(), distanceHelper);
                  //  myCurrentBest.countCost();
                }

                if(i - lastSuccessIndex > 1000000){
                    break;
                }

            }
        } catch (
                Exception e)

        {
            e.printStackTrace();
        }

    }

   synchronized private boolean getDistance( Route opt2Result) {
//       currentBest.countCost();
       if ((opt2Result.getCost() >= currentBest.getCost())) {
            return false;
        } else {
            System.out.println(Thread.currentThread().getName()+"new best found! " + String.format("%.2f",opt2Result.getCost()/3600.0));
            currentBest = new Route(opt2Result);
            myCurrentBest = new Route(currentBest);

           return true;
        }
    }

    private synchronized void initialize() {
        myCurrentBest = Route.newRandomRoute(meetings.size(), distanceHelper);
        if (null == currentBest) {
            currentBest = new Route(myCurrentBest);
        }
    }


    private Route opt2(Route opt2ResultLocal) {

        for (int i = 0; i < meetings.size() - 2; i++) {
            for (int j = i + 2; j < meetings.size() - 1; j++) {
                int distA = distanceHelper.getTime(i, i + 1, 9)
                        + distanceHelper.getTime(j, j + 1, 9);
                int distB = distanceHelper.getTime(i, j, 9)
                        + distanceHelper.getTime(i + 1, j + 1, 9);

                if (distA > distB) {
                    opt2ResultLocal.swap(i + 1, j);
                }
            }
        }

        //opt2ResultLocal.countCost();
        return opt2ResultLocal;
    }


    public synchronized Route getCurrentBest() {
        currentBest.getRoute();
        return currentBest;
    }

}
