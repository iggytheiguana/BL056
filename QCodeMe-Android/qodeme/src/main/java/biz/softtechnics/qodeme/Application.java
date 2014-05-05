package biz.softtechnics.qodeme;

//import static biz.softtechnics.qodeme.utils.Fonts.CALIBRI_BOLD;
//import static biz.softtechnics.qodeme.utils.Fonts.CALIBRI_BOLD_ITALIC;
//import static biz.softtechnics.qodeme.utils.Fonts.CALIBRI_ITALIC;
//import static biz.softtechnics.qodeme.utils.Fonts.CALIBRI_REGULAR;
import static biz.softtechnics.qodeme.utils.Fonts.ROBOTO_BOLD;
import static biz.softtechnics.qodeme.utils.Fonts.ROBOTO_BOLD_ITALIC;
import static biz.softtechnics.qodeme.utils.Fonts.ROBOTO_ITALIC;
import static biz.softtechnics.qodeme.utils.Fonts.ROBOTO_REGULAR;
//import static biz.softtechnics.qodeme.utils.Fonts.CALIBRI_BOLD_ITALIC;
//import static biz.softtechnics.qodeme.utils.Fonts.CALIBRI_ITALIC;
//import static biz.softtechnics.qodeme.utils.Fonts.CALIBRI_REGULAR;

import java.lang.ref.WeakReference;

import android.graphics.Typeface;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.io.RestAsyncHelper;
import biz.softtechnics.qodeme.ui.MainActivity;
import biz.softtechnics.qodeme.utils.AnalyticsHelper;
import biz.softtechnics.qodeme.utils.FontUtils;

/**
 * Created by Alex on 10/7/13.
 */
public class Application extends android.app.Application {

	private WeakReference<MainActivity> weekRefMainActivity;
	public static Typeface typefaceRegular;
	public static Typeface typefaceBold;
	public static Typeface typefaceItalic;
	public static Typeface typefaceItalicBold;
	public static Typeface typefaceMedium;
	public static Typeface typefaceMediumItalic;

	@Override
	public void onCreate() {
		super.onCreate();
		AnalyticsHelper.onCreateApplication(this);
		QodemePreferences.initialize(getApplicationContext());
		RestAsyncHelper.initialize(getApplicationContext());
		// FontUtils.setDefaultFontFormAssets(getAssets(),
		// CALIBRI_REGULAR.toString(), CALIBRI_BOLD.toString(),
		// CALIBRI_ITALIC.toString(), CALIBRI_BOLD_ITALIC.toString());
		FontUtils.setDefaultFontFormAssets(getAssets(), ROBOTO_REGULAR.toString(),
				ROBOTO_BOLD.toString(), ROBOTO_ITALIC.toString(), ROBOTO_BOLD_ITALIC.toString());

		typefaceRegular = Typeface.createFromAsset(getAssets(), "fonts/RobotoRegular.ttf");
		typefaceBold = Typeface.createFromAsset(getAssets(), "fonts/RobotoBold.ttf");
		typefaceItalic = Typeface.createFromAsset(getAssets(), "fonts/RobotoItalic.ttf");
		typefaceItalicBold = Typeface.createFromAsset(getAssets(), "fonts/RobotoBoldItalic.ttf");
		typefaceMedium = Typeface.createFromAsset(getAssets(), "fonts/Roboto_Medium_2.ttf");
		typefaceMediumItalic = Typeface.createFromAsset(getAssets(),
				"fonts/Roboto_MediumItalic_2.ttf");
	}

	public void setMainActivity(MainActivity mainActivity) {
		weekRefMainActivity = new WeakReference<MainActivity>(mainActivity);
	}

	public MainActivity getMainActivity() {
		if (weekRefMainActivity == null)
			return null;
		return weekRefMainActivity.get();
	}

	public boolean isActive() {
		MainActivity activity = getMainActivity();
		if (activity != null && activity.isActive())
			return true;
		return false;
	}

}
