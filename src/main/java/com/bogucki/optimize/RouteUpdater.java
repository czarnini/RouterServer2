package com.bogucki.optimize;

import com.bogucki.databse.DistanceHelper;
import com.bogucki.optimize.models.Meeting;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RouteUpdater extends OptimizationManager implements Runnable {


    public RouteUpdater(DatabaseReference routeToUpdate, onRequestDoneListener listener) {
        this.listener = listener;
        System.out.println(routeToUpdate.getRef());
        this.routeToOptimize = routeToUpdate;
    }

    @Override
    public void run() {
        routeToOptimize.orderByChild("meetingOrder").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    ArrayList<Meeting> meetings = new ArrayList<>();
                    for (DataSnapshot tmp : snapshot.getChildren()) {
                        meetings.add(tmp.getValue(Meeting.class));
                    }
                    distanceHelper = new DistanceHelper(meetings);
                    result = VNSOptimizer.recalculate(distanceHelper);
                    publishOptimalRoute();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("Error while parsing request");
            }
        });
    }


}
