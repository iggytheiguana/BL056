package biz.softtechnics.qodeme.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import biz.softtechnics.qodeme.R;

/**
 * Created by Alex on 10/28/13.
 */
public class Helper {

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideKeyboard(Activity a) {
        InputMethodManager imm = (InputMethodManager)a.getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = a.getCurrentFocus();
        if (v != null){
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public static void doNotShowKeyboardOnStartActivity(Activity a){
        a.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public static void showKeyboard(Context context, View inputView) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(inputView, InputMethodManager.SHOW_FORCED);
    }

    public static String getTimeAMPM(long time) {
        Date date=new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("h:mm a",Locale.US);
        return format.format(date);
    }

    public static String getTime24(Long time) {
        if (time == null)
            return null;
        Date date=new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm",Locale.US);
        return format.format(date);
    }

    public static Drawable changePixelColor(Resources res, Drawable d, int inColor, int outColor){
        Bitmap src = ((BitmapDrawable) d).getBitmap();
        Bitmap bitmap = src.copy(Bitmap.Config.ARGB_8888, true);
        Log.i("TAG_COLOR", String.format("height = %d", bitmap.getHeight()));
        Log.i("TAG_COLOR", String.format("width = %d", bitmap.getWidth()));
        for(int x = 0;x < bitmap.getWidth();x++)
            for(int y = 0;y < bitmap.getHeight();y++) {
                int aColor = bitmap.getPixel(x, y);
                Log.i("TAG_COLOR", String.format("%d", aColor));
                if(((aColor & 0xffffff) == (inColor & 0xffffff)))
                    bitmap.setPixel(x, y, outColor | (aColor & 0xff000000));
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            return new BitmapDrawable(res, bitmap);
        else
            //noinspection deprecation
            return new BitmapDrawable(bitmap);
    }

    public static Bitmap getPaintedQr(Context context, int outColor) {
        Bitmap obm = null;
        Bitmap ob = BitmapFactory.decodeResource(context.getResources(), R.drawable.white_color_logo);
        obm = Bitmap.createBitmap(ob.getWidth(), ob.getHeight(), ob.getConfig());
        Canvas canvas = new Canvas(obm);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(outColor, PorterDuff.Mode.MULTIPLY));
        canvas.drawBitmap(ob, 0f, 0f, paint);
        return obm;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

}
