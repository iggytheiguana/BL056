package biz.softtechnics.qodeme.utils;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Display;

/**
 * Created by Alex on 11/23/13.
 */
public class Device{

    public static boolean isTablet(Activity activity){
        return (getInch(activity) >= 7);
    }

    public static double getInch(Activity activity){
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        float widthDpi = displayMetrics.xdpi;
        float heightDpi = displayMetrics.ydpi;
        float widthInches = widthPixels / widthDpi;
        float heightInches = heightPixels / heightDpi;
        double diagonalInches = Math.sqrt(
                (widthInches * widthInches)
                        + (heightInches * heightInches));
        return diagonalInches;
    }

}
