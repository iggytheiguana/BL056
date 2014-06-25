package com.blulabellabs.code.utils;

import com.blulabellabs.code.ApplicationConstants;
import com.blulabellabs.code.R;
import com.blulabellabs.code.ui.MainActivity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


/**
 * Created by Alex on 12/10/13.
 */
public final class NotificationUtils {

    public static final int NOTIFICATION_REQUEST_NEW_CONTACT = 1;
    public static final int NOTIFICATION_REQUEST_NEW_MESSAGE = 2;

    private NotificationUtils(){}

    /** Put the GCM message into a notification and post it.
     *
     * @param msg
     * @param context
     * @param requestCode
     */
    public static void sendNotification(String msg, Context context, int requestCode) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent i = new Intent(context, MainActivity.class);
        i.setAction(ApplicationConstants.ACTION_RECEIVE_GCM_MESSAGE);
        PendingIntent contentIntent = PendingIntent.getActivity(context, requestCode, i, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(msg)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
        Log.i("Push", msg);
        mNotificationManager.notify(msg.hashCode(), mBuilder.getNotification());
    }
    
    public static void sendNotificationForOneToOne(String msg,long chatId, Context context, int requestCode) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent i = new Intent(context, MainActivity.class);
        i.setAction(ApplicationConstants.ACTION_RECEIVE_GCM_MESSAGE);
        i.putExtra("chat_id", chatId);
        PendingIntent contentIntent = PendingIntent.getActivity(context, requestCode, i, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(msg)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
        Log.i("Push", msg);
        mNotificationManager.notify(msg.hashCode(), mBuilder.getNotification());
    }

}
