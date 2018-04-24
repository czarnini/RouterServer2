package com.bogucki.optimize;

import com.bogucki.databse.DistanceHelper;
import com.bogucki.optimize.models.Meeting;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OptimizationManager implements Runnable {
    private Route result;
    private DatabaseReference routeToOptimize;
    private ArrayList<Meeting> meetings = new ArrayList<>();
    private DistanceHelper distanceHelper;

    public OptimizationManager(DatabaseReference routeToOptimize) {
        System.out.println(routeToOptimize.getRef());
        this.routeToOptimize = routeToOptimize;
    }

    @Override
    public void run() {
        routeToOptimize.orderByChild("meetingOrder").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot tmp : snapshot.getChildren()) {
                    meetings.add(tmp.getValue(Meeting.class));
                }
                optimize();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("Error while parsing request");
            }
        });
    }


    private void optimize() {
        distanceHelper = new DistanceHelper(meetings);
        VNSOptimizer optimizer = new VNSOptimizer(distanceHelper);
        Thread[] threads = new Thread[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(optimizer::optimize);
            threads[i].start();
        }


        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        result = optimizer.getCurrentBest();
        result.getRoute();
        publishOptimalRoute();
        VNSOptimizer.currentBest = null;


    }

    private void publishOptimalRoute() {
        Map<String, Object> valuesToSend = new HashMap<>();
        int[] order = result.getCitiesOrder();
        for (int i = 0; i < order.length; i++) {
            meetings.get(order[i]).setMeetingOrder(i);
            valuesToSend.put(meetings.get(order[i]).getPushId(), meetings.get(order[i]).toMap());
        }
        routeToOptimize.updateChildren(valuesToSend, this::cleanUpRequest);
    }

    private void cleanUpRequest(DatabaseError error, DatabaseReference ref) {
        FirebaseDatabase.getInstance().getReference().child("requests").child(routeToOptimize.getKey()).removeValueAsync();
    }
}
