package com.bogucki.networking;

import com.bogucki.optimize.Meeting;
import com.bogucki.optimize.Route;
import com.bogucki.optimize.VNSOptimizer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class EchoPostOptimizeHandler implements HttpHandler {
    private Route result;

    @Override
    public void handle(HttpExchange he) throws IOException {

/*
        // parse request
        System.out.println("POST queried");
        System.out.println(he.getRequestBody());
        InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);

        StringBuilder query = new StringBuilder();
        String tmp;
        while ((tmp = br.readLine()) != null) {
            query.append(tmp);
        }

        int avgCost = 0;

        ArrayList<Meeting> meetings = parseQuery(query.toString());
        VNSOptimizer optimizer = new VNSOptimizer(meetings);


        Thread[] threads = new Thread[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(optimizer::optimize);
            threads[i].start();
        }


        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        result = optimizer.getCurrentBest();
        avgCost += result.getCost();


        System.out.println("COST: " + String.format("%.2f\n\n\n\n\n", avgCost / 3600.0));
        VNSOptimizer.currentBest = null;


        //result.getRoute();
        sendResponse(he);
*/
    }


    private void sendResponse(HttpExchange he) throws IOException {
        StringBuilder response = new StringBuilder("{\"result\":[");
        JSONObject tmp = new JSONObject();


        for (int i = 0; i < result.getCitiesOrder().length; i++) {
            if(i!=0){
                response.append(", ");
            }
            response.append( result.getCity(i));
        }
        response.append("]}");
        he.sendResponseHeaders(200, response.length());
        OutputStream os = he.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
        VNSOptimizer.currentBest = null;
    }

    private ArrayList<Meeting> parseQuery(String query) {
        ArrayList<Meeting> result = new ArrayList<>();
        try {
            JSONObject jsonQUERY = new JSONObject(query);
            JSONArray sentMeetings = jsonQUERY.getJSONArray("meetings");
            if (null != sentMeetings) {
                int length = sentMeetings.length();
                for (int i = 0; i < length; ++i) {
                    JSONObject currentMeeting = sentMeetings.getJSONObject(i);
                    result.add(new Meeting(
                            currentMeeting.getString("address").replaceAll(" *, *", ",").trim().toLowerCase(),
                            currentMeeting.getInt("ETP"),
                            currentMeeting.getInt("LTP")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


}



