package com.bogucki.networking;

import com.bogucki.databse.DistanceHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EchoPostNewAddressHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        // parse request
        System.out.println("POST Add New Address  queried with:");
        System.out.println(httpExchange.getRequestBody());
        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);

        StringBuilder query = new StringBuilder();
        String tmp;
        while ((tmp = br.readLine()) != null) {
            query.append(tmp);
        }
        System.out.println(query.toString());
        List<String> addressesToAdd = new ArrayList<>(parseQuery(query.toString()));
        DistanceHelper helper = new DistanceHelper(null);
            try {
                helper.addAddresses(addressesToAdd);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        helper.cleanUp();

        // send response
        StringBuilder response = new StringBuilder();
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }

    private ArrayList<String> parseQuery(String query) {
        ArrayList<String> result = new ArrayList<>();
        JSONArray addressesJSON = new JSONObject(query).getJSONArray("addresses");
        if (null != addressesJSON) {
            int length = addressesJSON.length();
            for (int i = 0; i < length; ++i) {
                result.add(addressesJSON.getString(i).toLowerCase().trim().replaceAll(" *, *",","));
                System.out.println(addressesJSON.getString(i));
            }
        }
        return result;
    }
}
