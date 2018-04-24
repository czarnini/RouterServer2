package com.bogucki;

import com.bogucki.databse.DistanceHelper;
import com.bogucki.optimize.Client;
import com.bogucki.optimize.OptimizationHandler;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Main {

    private static DatabaseReference routerDataBase;

    public static void main(String[] args) throws IOException {
        handleDistanceHelper();
        initFireBase();
        routerDataBase = FirebaseDatabase.getInstance().getReference();
        handleOptimizeRequest();
        handleNewClient();
        keepAlive();
    }

    private static void handleDistanceHelper() {
        if (!new File("Distances.db").exists()) {
            DistanceHelper helper = new DistanceHelper(null);
            helper.createAddressDictionary();
            helper.cleanUp();
        }
    }

    private static void handleNewClient() {
        routerDataBase.child("clients").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                try {
                    Client client = snapshot.getValue(Client.class);
                    DistanceHelper distanceHelper = new DistanceHelper(null);
                    distanceHelper.addAddressToDb(client.getAddress());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                System.out.println(snapshot.getKey() + " finished");
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private static void handleOptimizeRequest() {
        routerDataBase.child("requests").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                DatabaseReference routeToOptimize = FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child("meetings")
                        .child(snapshot.getKey());

                new Thread(new OptimizationHandler(routeToOptimize)).start();
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                System.out.println(snapshot.getKey() + " finished");
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private static void initFireBase() {
        FileInputStream serviceAccount;
        try {
            serviceAccount = new FileInputStream(new File("").getAbsolutePath().concat("\\src\\main\\java\\com\\bogucki\\Router-b74c37ecf7a0.json"));
            FirebaseOptions options;
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://router-35b8b.firebaseio.com/")
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void keepAlive() {
        while (true) {

        }
    }
}
