package com.blulabellabs.code.ui.qr;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.blulabellabs.code.ApplicationConstants;
import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.IntentKey;
import com.blulabellabs.code.utils.AnalyticsHelper;
import com.blulabellabs.code.utils.QrUtils;
import com.google.zxing.Result;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import zxing.library.DecodeCallback;
import zxing.library.ZXingFragment;

/**
 * Created by Alex on 10/8/13.
 */
public class QrCodeCaptureActivity extends FragmentActivity {

	private static final String TAG = QrCodeCaptureActivity.class.getSimpleName();

	public static final int QODEME_CONTACT = 1;
	public static final int QODEME_PRIVATE_CHAT = 2;
	public static final int QODEME_PUBLIC_CHAT = 4;
	public static final int QODEME_GROUP_CHAT = 3;
	public static final int QODEME_ALL = 7;

	private Pattern patternQodemeContact = Pattern.compile(String
			.format(ApplicationConstants.QR_CODE_CONTACT_PATTERN));
	private Pattern patternQodemeChat = Pattern.compile(String
			.format(ApplicationConstants.QR_CODE_CONTACT_PATTERN));

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AnalyticsHelper.onCreateActivity(this);
		setContentView(R.layout.activity_capture_qr);
		// getSupportActionBar().hide();
		// ActionBar actionBar = getSupportActionBar();
		// actionBar.setDisplayHomeAsUpEnabled(true);
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

	/*
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { switch
	 * (item.getItemId()) { case android.R.id.home: finish(); return true;
	 * default: return super.onOptionsItemSelected(item); } }
	 */

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
						String qrCode = "";
						String title = "";
						int type = 0;
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
//							Intent i = new Intent();
//							i.putExtra(IntentKey.QR_CODE, QrUtils.removeContactPrefix(sResult));
//							i.putExtra(IntentKey.CHAT_TYPE, QODEME_CONTACT);
//							i.putExtra("Type", type);
//							i.putExtra(IntentKey.CONTACT_NAME, title);
//							i.putExtra(IntentKey.CHAT_ID, -1);
//							setResult(RESULT_OK, i);
//							Log.i(TAG, QrUtils.removeContactPrefix(sResult));
//							finish();
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
						// TODO need to add ability to define public/privat chat
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
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			// do nothing
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
