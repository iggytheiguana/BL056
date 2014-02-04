package biz.softtechnics.qodeme.core.io.gcm;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.provider.Settings.Secure;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import biz.softtechnics.qodeme.ApplicationConstants;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.io.RestAsyncHelper;
import biz.softtechnics.qodeme.core.io.responses.VoidResponse;
import biz.softtechnics.qodeme.core.io.utils.RestError;
import biz.softtechnics.qodeme.core.io.utils.RestListener;

public class GcmController {

    private static final String TAG = GcmController.class.getSimpleName();
    public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;
    public static final String GCM_TOKEN = "push_token";

    private static GcmController instance;
    private GoogleCloudMessaging gcm;
    private QodemePreferences pref;
    private Context context;


    private GcmController(Context c) {
        context = c.getApplicationContext();
        pref = QodemePreferences.getInstance();
        gcm = GoogleCloudMessaging.getInstance(context);
    }

    public static GcmController getInstance(Context context) {
        if (instance == null)
            synchronized (GcmController.class) {
                if (instance == null) {
                    instance = new GcmController(context);
                }
            }
        return instance;
    }

    /**
     * Update GCM token in cases:
     * 1 - AppVersion was updated
     * 2 - Token expired (7 days)
     */
    public void updateToken() {
        //if (isGcmRegistrationTokenInvalid()){
        registrateGCM();
        //}
    }


    private void registrateGCM() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                Log.d(TAG, "getting_registration_id");
                try {
                    String gcmToken = gcm.register(ApplicationConstants.GCM_SENDER_ID);

                    int appVersion = getAppVersion(context);
                    long expirationTime = System.currentTimeMillis()
                            + REGISTRATION_EXPIRY_TIME_MS;
                    pref.setAppVersion(appVersion);
                    pref.setGcmToken(gcmToken);
                    pref.setGcmTokenExpirationTime(expirationTime);
                    pref.setGcmTokenSycnWithRest(false);

                    String deviceId = Secure.getString(
                            context.getContentResolver(), Secure.ANDROID_ID);

                    RestAsyncHelper.getInstance().registerToken(gcmToken, new RestListener<VoidResponse>() {

                        @Override
                        public void onResponse(VoidResponse response) {
                        }

                        @Override
                        public void onServiceError(RestError error) {
                        }
                    });
                } catch (final IOException ex) {
                    Log.e(TAG, ex.getMessage());
                }
                return null;

            }

        }.execute(null, null, null);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Checks if the registration has expired.
     * <p/>
     * <p/>
     * To avoid the scenario where the device sends the registration to the
     * server but the server loses it, the app developer may choose to
     * re-register after REGISTRATION_EXPIRY_TIME_MS.
     *
     * @return true if the registration has expired.
     */
    private boolean isGcmRegistrationTokenInvalid() {
        long expirationTime = pref.getGcmTokenExpirationTime();
        int appVersion = pref.getAppVersion();
        if (expirationTime == -1L || appVersion < getAppVersion(context))
            return true;
        return System.currentTimeMillis() > expirationTime;
    }

}
