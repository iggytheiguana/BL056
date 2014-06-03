package com.blulabellabs.code.core.io.gcm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.blulabellabs.code.core.io.gcm.push.BasePushHandler;
import com.blulabellabs.code.core.io.gcm.push.PushHendlerFactory;
import com.blulabellabs.code.utils.LogUtils;
import com.google.android.gms.gcm.GoogleCloudMessaging;


import static com.blulabellabs.code.utils.LogUtils.LOGE;
import static com.blulabellabs.code.utils.LogUtils.LOGV;
import static com.blulabellabs.code.utils.LogUtils.makeLogTag;
import static com.blulabellabs.code.utils.NotificationUtils.sendNotification;

public class GcmBroadcastReceiver  extends BroadcastReceiver {

    private static final String TAG = makeLogTag(GcmBroadcastReceiver.class);
    
    @Override
    public void onReceive(Context context, Intent intent) {

        //todo for test notification of push
        //Toast.makeText(context, "Receive push", Toast.LENGTH_SHORT).show();
        for (String key : intent.getExtras().keySet()) {
            Object value = intent.getExtras().get(key);
            LogUtils.addToLogFile(String.format("%s %s (%s)", key,
                    value.toString(), value.getClass().getName()));
        }

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        String messageType = gcm.getMessageType(intent);
        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
        	Log.i(TAG, "GCM_Error" + intent.getExtras().toString());
            sendNotification("Send error: " + intent.getExtras().toString(), context, 0);
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            sendNotification("Deleted messages on server: " +
                    intent.getExtras().toString(), context, 0);
            Log.i(TAG, "GCM_Delete" + intent.getExtras().toString());
        } else {
            BasePushHandler basePushHandler = null;
            try {
                basePushHandler = PushHendlerFactory.getPushHandler(context, intent.getExtras());
                LOGV(TAG, "Push_type:" + intent.getExtras().getString("type"));
                if (basePushHandler != null){
                    basePushHandler.handle();
                } else
                    LOGE(TAG, String.format("A push wasn't recognized :[%s]", intent.getExtras().toString()));
            } catch (NullPointerException e){
                LOGE(TAG, "Push_wrong_format" + intent.getExtras().toString());
            }

        }
        setResultCode(Activity.RESULT_OK);
    }
}