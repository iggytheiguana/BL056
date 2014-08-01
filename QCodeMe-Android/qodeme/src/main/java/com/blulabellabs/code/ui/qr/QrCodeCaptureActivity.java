package com.blulabellabs.code.ui.qr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.blulabellabs.code.ApplicationConstants;
import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.IntentKey;
import com.blulabellabs.code.utils.AnalyticsHelper;
import com.blulabellabs.code.utils.QrUtils;
import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import zxing.library.DecodeCallback;
import zxing.library.ZXingFragment;

public class QrCodeCaptureActivity extends FragmentActivity {

    private static final String TAG = QrCodeCaptureActivity.class.getSimpleName();

    public static final int QODEME_CONTACT = 1;
    //    public static final int QODEME_PRIVATE_CHAT = 2;
//    public static final int QODEME_PUBLIC_CHAT = 4;
    public static final int QODEME_GROUP_CHAT = 3;
    public static final int QODEME_ALL = 7;

    private Pattern patternQodemeContact = Pattern.compile(String
            .format(ApplicationConstants.QR_CODE_CONTACT_PATTERN));
    private Pattern patternQodemeChat = Pattern.compile(String
            .format(ApplicationConstants.QR_CODE_CONTACT_PATTERN));

    GestureDetector gestureListener;
    ImageView gc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsHelper.onCreateActivity(this);
        setContentView(R.layout.activity_capture_qr);
        gestureListener = new GestureDetector(new SwipeGestureListener(this));
        gc = (ImageView) findViewById(R.id.gestureCapturer);
        gc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                return gestureListener.onTouchEvent(event);
            }
        });
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
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        final ZXingFragment xf = (ZXingFragment) getSupportFragmentManager().findFragmentById(
                R.id.scanner);
        xf.setDecodeCallback(new DecodeCallback() {

            @Override
            public void handleBarcode(Result result, Bitmap arg1, float arg2) {
                String sResult = result.getText();
                int chatType = getIntent().getIntExtra(IntentKey.CHAT_TYPE, QODEME_ALL);
                if ((chatType & QODEME_CONTACT) == QODEME_CONTACT) {
                    Matcher matcher = patternQodemeContact.matcher(sResult);
                    if (matcher.find()) {
                        String qrCode;
                        String title;
                        int type;
                        long chat_id = -1;
                        try {
                            JSONObject jsonObject = new JSONObject(QrUtils
                                    .removeContactPrefix(sResult));
                            qrCode = jsonObject.getString(IntentKey.QR_CODE);
                            title = jsonObject.getString(IntentKey.CONTACT_NAME);
                            type = jsonObject.getInt(IntentKey.CHAT_TYPE);
                            try {
                                chat_id = jsonObject.getLong(IntentKey.CHAT_ID);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Intent i = new Intent();
                            i.putExtra(IntentKey.QR_CODE, qrCode);
                            i.putExtra(IntentKey.CHAT_TYPE, QODEME_CONTACT);
                            i.putExtra("Type", type);
                            i.putExtra(IntentKey.CONTACT_NAME, title);
                            i.putExtra(IntentKey.CHAT_ID, chat_id);
                            setResult(RESULT_OK, i);
                            Log.i(TAG, QrUtils.removeContactPrefix(sResult));
                            finish();
                            return;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            new AlertDialog.Builder(QrCodeCaptureActivity.this)
                                    .setTitle(R.string.app_name)
                                    .setMessage("Error on Parsing the QR Code.")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).create().show();
                        }
                    }
                } else if ((chatType & QODEME_GROUP_CHAT) == QODEME_GROUP_CHAT) {
                    Matcher matcher = patternQodemeChat.matcher(sResult);
                    if (matcher.find()) {
                        Intent i = new Intent();
                        i.putExtra(IntentKey.QR_CODE, QrUtils.removeChatPrefix(sResult));
                        i.putExtra(IntentKey.CHAT_TYPE, QODEME_GROUP_CHAT);
                        setResult(RESULT_OK, i);
                        Log.i(TAG, QrUtils.removeChatPrefix(sResult));
                        finish();
                        return;
                    }
                }
                Toast.makeText(QrCodeCaptureActivity.this, "Incorrect format!", Toast.LENGTH_SHORT)
                        .show();
                xf.restartScanningIn(600);
            }

        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_MENU || super.onKeyDown(keyCode, event);
    }

}


class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener  {
    Context context;
    GestureDetector gDetector;
    static final int SWIPE_MIN_DISTANCE = 120;

    public SwipeGestureListener(Context context) {
        this(context, null);
    }

    public SwipeGestureListener(Context context, GestureDetector gDetector) {

        if (gDetector == null)
            gDetector = new GestureDetector(context, this);

        this.context = context;
        this.gDetector = gDetector;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {

        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
            ((QrCodeCaptureActivity)context).finish();
        }

        return super.onFling(e1, e2, velocityX, velocityY);

    }

}
