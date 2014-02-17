package biz.softtechnics.qodeme.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.bugsense.trace.BugSenseHandler;
import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;

import static biz.softtechnics.qodeme.ApplicationConstants.BUGSENSE_ENABLED;
import static biz.softtechnics.qodeme.ApplicationConstants.BUGSENSE_KEY;
import static biz.softtechnics.qodeme.ApplicationConstants.FLURRY_KEY;
import static biz.softtechnics.qodeme.ApplicationConstants.GOOGLE_ANALYTICS_ENABLED;

//import com.bugsense.trace.BugSenseHandler;
//import com.bugsense.trace.DefaultExceptionHandler;

/**
 * Created by Alex on 12/19/13.
 */
public class AnalyticsHelper {
    private AnalyticsHelper(){}

    public static void onCreateApplication(Application application){
        /*if ( FLURYY_ENABLED )
            initFlurry(application);*/





    }

    public static void onCreateActivity(Activity activity){


        /*Thread.UncaughtExceptionHandler myHandler = new ExceptionReporter(
                GoogleAnalytics.getInstance(activity).getDefaultTracker(), // Tracker, may return null if not yet initialized.
                GAServiceManager.getInstance(),                        // GAServiceManager singleton.
                Thread.getDefaultUncaughtExceptionHandler(), activity);          // Current default uncaught exception handler.

        // Make myHandler the new default uncaught exception handler.
        Thread.setDefaultUncaughtExceptionHandler(myHandler);*/



        if (BUGSENSE_ENABLED )
            initBugsense(activity);

        /*if ( GOOGLE_ANALYTICS_ENABLED ) {
            Thread.UncaughtExceptionHandler myHandler = new ExceptionReporter(
                    GoogleAnalytics.getInstance(activity).getDefaultTracker(), // Tracker, may return null if not yet initialized.
                    GAServiceManager.getInstance(),                        // GAServiceManager singleton.
                    Thread.getDefaultUncaughtExceptionHandler(),
                    activity);          // Current default uncaught exception handler.

            // Make myHandler the new default uncaught exception handler.
            Thread.setDefaultUncaughtExceptionHandler(myHandler);
        }*/
    }

    public static void onStartActivity(Activity activity){
        /*if ( FLURYY_ENABLED ){
            //FlurryAgent.setCaptureUncaughtExceptions(true);
            //FlurryAgent.setLogEnabled(true);
            Thread.setDefaultUncaughtExceptionHandler(
                    new ExceptionHandler(activity));
            FlurryAgent.onStartSession(activity, FLURRY_KEY);
            //startFlurry(activity);
        }*/
        if (GOOGLE_ANALYTICS_ENABLED )
            startGoogleAnalytics(activity);
    }

    public static void onStopActivity(Activity activity){
        /*if ( FLURYY_ENABLED )
            stopFlurry(activity);*/
        if ( GOOGLE_ANALYTICS_ENABLED )
            stopGoogleAnalytics(activity);
    }

    private static void initBugsense(Activity activity){
        BugSenseHandler.initAndStartSession(activity, BUGSENSE_KEY);
    }

    private static void startGoogleAnalytics(Activity activity){
        EasyTracker t = EasyTracker.getInstance(activity);
        t.activityStart(activity);
        /*t.set(Fields.SCREEN_NAME, "Home Screen");
        t.send(MapBuilder
                .createAppView()
                .build());

        int u = 7/0;*/
    }

    private static void stopGoogleAnalytics(Activity activity){
        EasyTracker.getInstance(activity).activityStop(activity);
    }

    private static void initFlurry(Context context){
        FlurryAgent.setUseHttps(true);
        FlurryAgent.setReportLocation(true);
        FlurryAgent.setCaptureUncaughtExceptions(false);
    }

    private static void startFlurry(Activity activity){
        FlurryAgent.onStartSession(activity, FLURRY_KEY);
    }

    private static void stopFlurry(Activity activity){
        FlurryAgent.onEndSession(activity);
    }

}
