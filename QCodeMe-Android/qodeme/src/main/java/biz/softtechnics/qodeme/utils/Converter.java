package biz.softtechnics.qodeme.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by Alex on 10/20/13.
 */
public class Converter {

    /**
     * Convert dips to pixels
     * @param dips
     * @param context - context to get display metrics
     * @return pixels equivalent of dips
     */
    public static int dipToPx(Context context, float dips) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                (float) dips,
                context.getResources().getDisplayMetrics());
    }

    public static String booleanToIntString(boolean withMessage) {
        return String.valueOf(withMessage ? 1 : 0);
    }

    public static String getCurrentGtmTimestampString(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(new Date(System.currentTimeMillis()));
    }

    public static Long getCrurentTimeFromTimestamp(String timestamp){
        if (TextUtils.isEmpty(timestamp))
            return null;
        if(!timestamp.contains(".")){
        	timestamp+=".0";
        }
        TimeZone tz = TimeZone.getDefault();
        return Timestamp.valueOf(timestamp).getTime() + tz.getRawOffset();
    }
}
