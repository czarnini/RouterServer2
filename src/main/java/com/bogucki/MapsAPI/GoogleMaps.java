package com.bogucki.MapsAPI;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class GoogleMaps {
    public static ArrayList<Integer> getDistances(String origin, String destination) {
        if("".equals(origin) | "".equals(destination)){
            return new ArrayList<>(0);
        }
        System.out.println("Get distance from API started " + origin);
        HttpUrl url = getHttpUrl(origin, destination);
        Request request = new Request.Builder().url(url).build();
        try {
            return getResponse(request);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>(0);
        }
    }


    private static ArrayList<Integer> getResponse (Request request) throws IOException {
        Response response = new OkHttpClient().newCall(request).execute();
        if (null != response.body()) {
            String textResponse = response.body().string();
            JSONObject jsonResponse = new JSONObject(textResponse);
            System.out.println("Get distance from API finished");
            ArrayList<Integer> result = new ArrayList<>();

            JSONArray rows = jsonResponse.getJSONArray("rows");
            if(null != rows) {
                int rowsSize = rows.length();
                for (int i = 0; i < rowsSize; i++) {
                    JSONArray elements = rows.getJSONObject(i)
                                             .getJSONArray("elements");

                    if (null != elements) {
                        int elementsSize = elements.length();
                        for (int j = 0; j < elementsSize; j++) {
                            result.add(elements.getJSONObject(j)
                                    .getJSONObject("duration")
                                    .getInt("value"));
                        }
                    }
                }
            }
            return result;
        } else {
            return new ArrayList<>();
        }
    }

    private static HttpUrl getHttpUrl(String origin, String destination) {
        String key = "AIzaSyBDG4CZIG5D3gpQz5WOCt5xHw60_vayWc8";

        return new HttpUrl.Builder()
                .scheme("https")
                .host("maps.googleapis.com")
                .addPathSegment("maps")
                .addPathSegment("api")
                .addPathSegment("distancematrix")
                .addPathSegment("json")
                .addQueryParameter("origins", origin)
                .addQueryParameter("destinations", destination)
                .addQueryParameter("departure_time ","1544702400")
                .addQueryParameter("key", key)
                .build();
    }
}
