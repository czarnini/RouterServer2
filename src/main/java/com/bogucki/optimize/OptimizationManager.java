package com.bogucki.optimize;

import com.bogucki.databse.DistanceHelper;
import com.bogucki.optimize.models.Meeting;
import com.google.firebase.database.*;

import java.awt.*;
import java.awt.image.AreaAveragingScaleFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OptimizationManager implements Runnable {
    private Route result;
    private DatabaseReference routeToOptimize;
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
                ArrayList<Meeting> meetings = new ArrayList<>();
                for (DataSnapshot tmp : snapshot.getChildren()) {
                    meetings.add(tmp.getValue(Meeting.class));
                }
                distanceHelper = new DistanceHelper(meetings);
                optimize();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("Error while parsing request");
            }
        });
    }


    private void optimize() {
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
        if(Desktop.isDesktopSupported()){
            try {
                Desktop.getDesktop().browse(result.getGoogleMapsUrl());
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
        publishOptimalRoute();
        VNSOptimizer.currentBest = null;


    }

    private void publishOptimalRoute() {
        long currentTime = System.currentTimeMillis();
        Map<String, Object> valuesToSend = new HashMap<>();
        int[] order = result.getCitiesOrder();
        for (int i = 0; i < order.length - 1; i++) {
            distanceHelper.getMeetings().get(order[i]).setMeetingOrder(i);
            distanceHelper.getMeetings().get(order[i]).setPlanedTimeOfVisit(currentTime);
            currentTime += distanceHelper.getTime(order[i], order[i+1],9) *1000;
            valuesToSend.put(distanceHelper.getMeetings().get(order[i]).getPushId(), distanceHelper.getMeetings().get(order[i]).toMap());
        }

        distanceHelper.getMeetings().get(order[order.length-1]).setMeetingOrder(order.length-1);
        distanceHelper.getMeetings().get(order[order.length-1]).setPlanedTimeOfVisit(currentTime);
        valuesToSend.put(distanceHelper.getMeetings().get(order[order.length-1]).getPushId(), distanceHelper.getMeetings().get(order[order.length-1]).toMap());

        routeToOptimize.updateChildren(valuesToSend, this::cleanUpRequest);
    }

    private void cleanUpRequest(DatabaseError error, DatabaseReference ref) {
        FirebaseDatabase.getInstance().getReference().child("requests").child(routeToOptimize.getKey()).removeValueAsync();
    }
}
