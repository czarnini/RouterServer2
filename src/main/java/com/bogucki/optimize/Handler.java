package com.bogucki.optimize;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Handler implements Runnable, DataReadListener {
    Route result;
    DatabaseReference routeToOptimize;
    private ArrayList<Meeting2> meetings = new ArrayList<>();

    public Handler(DatabaseReference routeToOptimize) {
        System.out.println(routeToOptimize.getRef());
        this.routeToOptimize = routeToOptimize;
    }

    @Override
    public void run() {
        getMeetings(this);
    }

    @Override
    public void onDataRead() {
        optimize();
    }

    private void optimize() {
        VNSOptimizer optimizer = new VNSOptimizer(meetings);
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

        updateRoute();
        VNSOptimizer.currentBest = null;
        FirebaseDatabase.getInstance().getReference().child("requests").child(routeToOptimize.getKey()).removeValueAsync();


    }

    private void getMeetings(DataReadListener listener) {
        routeToOptimize.orderByChild("meetingOrder").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot tmp : snapshot.getChildren()) {
                    meetings.add(tmp.getValue(Meeting2.class));
                }
                listener.onDataRead();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("Error while parsing request");
            }
        });
    }

    private void updateRoute() {
        Map<String, Object> valuesToSend = new HashMap<>();
        int[] order = result.getCitiesOrder();
        for (int i = 0; i < order.length; i++) {
            meetings.get(order[i]).setMeetingOrder(i);
            valuesToSend.put(meetings.get(order[i]).getPushId(), meetings.get(order[i]).toMap());
        }
        routeToOptimize.updateChildren(valuesToSend);
    }
}
