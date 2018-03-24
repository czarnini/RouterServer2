package com.bogucki;

import com.bogucki.databse.DistanceHelper;
import com.bogucki.networking.EchoPostNewAddressHandler;
import com.bogucki.networking.EchoPostOptimizeHandler;
import com.bogucki.networking.RootHandler;
import com.bogucki.optimize.Handler;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws IOException {


        if (!new File("Distances.db").exists()) {
            DistanceHelper helper = new DistanceHelper(null);
            helper.createAddressDictionary();
            helper.cleanUp();
        }

        initFireBase();

        DatabaseReference requests = FirebaseDatabase.getInstance().getReference().child("requests");
        requests.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                System.out.println(snapshot.getKey() + " started");
                DatabaseReference routeToOptimize = FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child("meetings")
                        .child( snapshot.getKey());

                new Thread(new Handler(routeToOptimize)).start();
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

        while (true){

        }

    }


    private static void initFireBase() {
        FileInputStream serviceAccount = null;
        try {
            serviceAccount = new FileInputStream("C:\\Users\\boguc\\IdeaProjects\\bogucki\\src\\main\\java\\com\\bogucki\\Router-b74c37ecf7a0.json");
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
}
