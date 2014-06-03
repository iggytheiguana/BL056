package com.blulabellabs.code.ui.qr;

import com.blulabellabs.code.ApplicationConstants;
import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.IntentKey;
import com.blulabellabs.code.utils.AnalyticsHelper;
import com.blulabellabs.code.utils.QrUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;


/**
 * Created by Alex on 10/8/13.
 */
public class QrCodeShowActivity extends Activity {

    private ImageButton mQrCode;
    private Button mEmailButton;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsHelper.onCreateActivity(this);
        setContentView(R.layout.activity_show_qr);
        mQrCode = (ImageButton)findViewById(R.id.qr_code);
        final String qrCode = getIntent().getStringExtra(IntentKey.QR_CODE);
        refreshQrCode(qrCode);
        mEmailButton = (Button)findViewById(R.id.email);
        mEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email();
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
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            // do nothing
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void refreshQrCode(String qrCode){
        mBitmap = QrUtils.encodeQrCode((TextUtils.isEmpty(qrCode) ? "Qr Code" : ApplicationConstants.QR_CODE_CONTACT_PREFIX + qrCode), 500, 500, Color.BLACK, Color.WHITE);
        mQrCode.setImageBitmap(QrUtils.encodeQrCode((TextUtils.isEmpty(qrCode) ? "Qr Code" : ApplicationConstants.QR_CODE_CONTACT_PREFIX + qrCode), 500, 500, Color.BLACK, Color.WHITE));
    }

    /*private Context getContext(){
        return this;
    }*/

    private void email(){
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), mBitmap,"title", null);
        if (path == null){
            showMessage(getString(R.string.alert_no_access_to_external_storage));
            return;
        }

        Uri screenshotUri = Uri.parse(path);
        final Intent emailIntent = new Intent(     Intent.ACTION_SEND);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
        emailIntent.setType("image/png");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "QODEME contact");
        startActivity(Intent.createChooser(emailIntent, "Send email using"));
    }

    private void showMessage(String message) {
        new AlertDialog.Builder(getContext()).setTitle("Attention")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    private Context getContext(){
        return this;
    }
}
