package biz.softtechnics.qodeme.utils;


import android.graphics.Color;

import java.util.Random;

/**
 * Created by Alex on 11/4/13.
 */
public class RandomColorGenerator {

    private static RandomColorGenerator instance = new RandomColorGenerator();

    private Random mRandomGenerator;

    private RandomColorGenerator(){
        mRandomGenerator = new Random();
    }

    public static RandomColorGenerator getInstance(){
        return instance;
    }

    public int nextColor(){
        int red = mRandomGenerator.nextInt(255);
        int green = mRandomGenerator.nextInt(255);
        int blue = mRandomGenerator.nextInt(255);
        return Color.rgb(red,green,blue);
    }

}
