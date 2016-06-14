package com.jshepdevelopment.customerservicehelper;

import java.util.Random;

/**
 * Created by Jason Shepherd on 6/14/2016.
 */

public class Customer {

    public boolean isGood = true;

    public boolean setCustomerType() {

        Random rand = new Random();
        int randomNum = rand.nextInt(10) + 1;

        if(randomNum <= 5) {
            isGood = true;
        } else {
            isGood = false;
        }

        return isGood;
    }

}
