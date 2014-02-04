package biz.softtechnics.qodeme.ui;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;

import biz.softtechnics.qodeme.ApplicationConstants;
import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.accounts.GenericAccountService;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.sync.SyncUtils;
import biz.softtechnics.qodeme.utils.AnalyticsHelper;

/**
 * Created with IntelliJ IDEA.
 * User: Alex Vegner
 * Date: 8/14/13
 * Time: 3:07 PM
 */
public class SplashScreen extends Activity {

    private ContentResolver mResolver;
    private Account mAccount;
    private long SYNC_INTERVAL = 1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsHelper.onCreateActivity(this);
        setContentView(R.layout.activity_splash);

        SyncUtils.CreateSyncAccount(this);

        if (QodemePreferences.getInstance().isLogged()){
            Intent i = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(i);
            finish();
        } else {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    Intent i = new Intent(SplashScreen.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            }, ApplicationConstants.SPLASH_TIME_OUT);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        AnalyticsHelper.onStartActivity(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        AnalyticsHelper.onStopActivity(this);
    }

    private double getDiag(){
        Display display = getWindowManager().getDefaultDisplay();
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


    //
    public void onRefreshButtonClick() {
        // Pass the settings flags by inserting them in a bundle
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        /*
         * Request the sync for the default account, authority, and
         * manual sync settings
         */
        ContentResolver.requestSync(mAccount, GenericAccountService.ACCOUNT_TYPE, settingsBundle);
    }

}
