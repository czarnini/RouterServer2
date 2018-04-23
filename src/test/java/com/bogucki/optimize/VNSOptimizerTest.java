package com.bogucki.optimize;

import org.junit.Test;

import java.util.ArrayList;

public class VNSOptimizerTest {

    @Test
    public void foo() throws Exception {
        long start = System.currentTimeMillis();
        String [] addresses = new String [] {"janowskiego 13,warszawa", "wspólna 73,warszawa", "mielczarskiego 10,warszawa", "konduktorska 2,warszawa",
                "komorska 29/33,warszawa", "herbsta 4,warszawa", "waszyngtona 12/14,warszawa", "świętokrzyska 31/33a,warszawa", "berensona 12b,warszawa",
                "woronicza 50,warszawa", "zawiszy 5,warszawa", "kredytowa 5,warszawa", "al. ken 36,warszawa" };
        ArrayList<Meeting2> meetings = new ArrayList<>();
        for (String address : addresses) {
            meetings.add(new Meeting2(address));
        }
        VNSOptimizer optimizer = new VNSOptimizer(meetings);
        optimizer.optimize();
        optimizer.getCurrentBest().getRoute();

        long duration = System.currentTimeMillis() - start;
        System.out.println(duration);
    }
}