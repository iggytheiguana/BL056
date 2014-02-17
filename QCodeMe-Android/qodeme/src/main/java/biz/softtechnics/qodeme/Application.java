package biz.softtechnics.qodeme;

import java.lang.ref.WeakReference;

import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.io.RestAsyncHelper;
import biz.softtechnics.qodeme.ui.MainActivity;
import biz.softtechnics.qodeme.utils.AnalyticsHelper;
import biz.softtechnics.qodeme.utils.FontUtils;

import static biz.softtechnics.qodeme.utils.Fonts.CALIBRI_BOLD;
import static biz.softtechnics.qodeme.utils.Fonts.CALIBRI_BOLD_ITALIC;
import static biz.softtechnics.qodeme.utils.Fonts.CALIBRI_ITALIC;
import static biz.softtechnics.qodeme.utils.Fonts.CALIBRI_REGULAR;

/**
 * Created by Alex on 10/7/13.
 */
public class Application extends android.app.Application {

    private WeakReference<MainActivity> weekRefMainActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        AnalyticsHelper.onCreateApplication(this);
        QodemePreferences.initialize(getApplicationContext());
        RestAsyncHelper.initialize(getApplicationContext());
        FontUtils.setDefaultFontFormAssets(getAssets(), CALIBRI_REGULAR.toString(), CALIBRI_BOLD.toString(), CALIBRI_ITALIC.toString(), CALIBRI_BOLD_ITALIC.toString());
    }

    public void setMainActivity(MainActivity mainActivity){
        weekRefMainActivity = new WeakReference<MainActivity>(mainActivity);
    }

    public MainActivity getMainActivity() {
        if (weekRefMainActivity == null)
            return null;
        return weekRefMainActivity.get();
    }

    public boolean isActive(){
        MainActivity activity = getMainActivity();
        if (activity != null && activity.isActive())
            return true;
        return false;
    }


}
