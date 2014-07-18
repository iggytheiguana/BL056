package com.blulabellabs.code.ui.qr;

import org.json.JSONObject;

import com.blulabellabs.code.ApplicationConstants;
import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.IntentKey;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.sync.SyncHelper;
import com.blulabellabs.code.utils.AnalyticsHelper;
import com.blulabellabs.code.utils.Helper;
import com.blulabellabs.code.utils.QrUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * Created by Alex on 10/8/13.
 */
public class QrCodeShowActivity extends Activity {

	private static final int REQUEST_ACTIVITY_SCAN_QR_CODE = 2;
	private ImageButton mQrCode;
	private Button mEmailButton, mScanButton;
	private Bitmap mBitmap;
	private EditText editTextName;
	private String qrCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AnalyticsHelper.onCreateActivity(this);
		setContentView(R.layout.activity_show_qr);
		mQrCode = (ImageButton) findViewById(R.id.qr_code);
		 qrCode = getIntent().getStringExtra(IntentKey.QR_CODE);
		refreshQrCode(qrCode);

		editTextName = (EditText) findViewById(R.id.edit_name);

		mEmailButton = (Button) findViewById(R.id.email);
		mEmailButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				email();
			}
		});
		mScanButton = (Button) findViewById(R.id.scanQR);
		mScanButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent1 = new Intent();
				intent1.putExtra("type", 2);
				setResult(RESULT_OK, intent1);
				finish();
			}
		});

		editTextName.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH
						|| actionId == EditorInfo.IME_ACTION_GO
						|| actionId == EditorInfo.IME_ACTION_DONE
						|| event.getAction() == KeyEvent.ACTION_DOWN
						|| actionId == EditorInfo.IME_ACTION_SEND
						&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

					QodemePreferences.getInstance()
							.setPublicName(editTextName.getText().toString());
					QodemePreferences.getInstance().setUserSettingsUpToDate(false);
					SyncHelper.requestManualSync();
					Helper.hideKeyboard(QrCodeShowActivity.this, editTextName);

					return true;
				}
				return false;
			}
		});

		String name = QodemePreferences.getInstance().getPublicName();
		if (name != null && !name.trim().equals(""))
			editTextName.setText(name);

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
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			// do nothing
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void refreshQrCode(String qrCode) {
		JSONObject jsonObject = new JSONObject();
		try {
			String name = QodemePreferences.getInstance().getPublicName();
			if(name == null || name.trim().equals(""))
				name = "User";
			jsonObject.put(IntentKey.QR_CODE, qrCode);
			jsonObject.put(IntentKey.CHAT_TYPE, 0);
			jsonObject.put(IntentKey.CONTACT_NAME, name);
			jsonObject.put(IntentKey.CHAT_ID, -1);
		} catch (Exception e) {
		}
//		mBitmap = QrUtils.encodeQrCode((TextUtils.isEmpty(qrCode) ? "Qr Code"
//				: ApplicationConstants.QR_CODE_CONTACT_PREFIX + qrCode), 500, 500, Color.BLACK,
//				Color.WHITE);
		mBitmap = QrUtils.encodeQrCode(( ApplicationConstants.QR_CODE_CONTACT_PREFIX + jsonObject), 500, 500, Color.BLACK,
				Color.WHITE);
		mQrCode.setImageBitmap(QrUtils.encodeQrCode((TextUtils.isEmpty(qrCode) ? "Qr Code"
				: ApplicationConstants.QR_CODE_CONTACT_PREFIX + qrCode), 500, 500, Color.BLACK,
				Color.WHITE));
	}

	/*
	 * private Context getContext(){ return this; }
	 */

	private void email() {
		String public_name = editTextName.getText().toString();
		if (public_name.trim().equals(""))
			public_name = "User";
		
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(IntentKey.QR_CODE, qrCode);
			jsonObject.put(IntentKey.CHAT_TYPE, 0);
			jsonObject.put(IntentKey.CONTACT_NAME, public_name);
			jsonObject.put(IntentKey.CHAT_ID, -1);
		} catch (Exception e) {
		}
		Bitmap mBitmap = QrUtils.encodeQrCode(( ApplicationConstants.QR_CODE_CONTACT_PREFIX + jsonObject), 500, 500, Color.BLACK,
				Color.WHITE);
		
		String path = MediaStore.Images.Media.insertImage(getContentResolver(), mBitmap, "title",
				null);
		if (path == null) {
			showMessage(getString(R.string.alert_no_access_to_external_storage));
			return;
		}
//		String public_name = editTextName.getText().toString();
//		if (public_name.trim().equals(""))
//			public_name = "User";
		String location = "";
		try {
			location = QodemePreferences.getInstance().getLastLocation().getCity();
		} catch (Exception e) {
		}

		String data = "<html><body><h1>Contact Request</h1><hr><br><p>"
				+ public_name
				+ " would like to add you as a contact. Scan the QR code attached to add the contact and get a conversation started.</p><br><br>"
				+ "<h2>"
				+ public_name
				+ "</h2><h3>"
				+ location
				+ "</h3><a href=\"code:other/parameter\"> Add Contact </a> <br><hr><h2>What is Code!?</h2><br><p>Lorem ipsum dolor sit amet, sldfha consectetur adipisicing elit, sed do eiusmod tempor incididunt ut lab et dolore magna eliqua.</p><br><h2>Available On</h2><br><a href=\"http://play.google.com/store/apps/details?id=com.blulabellabs.code\"> Google Play </a></body></html>";

		Uri screenshotUri = Uri.parse(path);
		final Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		emailIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
		emailIntent.setType("image/png");
		emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(data));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Code! contact");
		startActivity(Intent.createChooser(emailIntent, "Send email using"));
	}

	private void showMessage(String message) {
		new AlertDialog.Builder(getContext()).setTitle("Attention").setMessage(message)
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create().show();
	}

	private Context getContext() {
		return this;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK)
			switch (requestCode) {
			case REQUEST_ACTIVITY_SCAN_QR_CODE: {
				final String qrCode = data.getStringExtra(IntentKey.QR_CODE);
				if (QodemePreferences.getInstance().getQrcode().equals(qrCode)) {
					showMessage("You can't add own QR code!");
					return;
				}

				int type = data.getIntExtra(IntentKey.CHAT_TYPE, -1);
				if ((type & QrCodeCaptureActivity.QODEME_CONTACT) == QrCodeCaptureActivity.QODEME_CONTACT
						&& !TextUtils.isEmpty(qrCode)) {
					Cursor c = getContentResolver().query(QodemeContract.Contacts.CONTENT_URI,
							new String[] { QodemeContract.Contacts._ID },
							QodemeContract.Contacts.CONTACT_QRCODE + " = '" + qrCode + "'", null,
							null);
					if (!c.moveToFirst()) {
						getContentResolver().insert(QodemeContract.Contacts.CONTENT_URI,
								QodemeContract.Contacts.addNewContactValues(qrCode,""));
						SyncHelper.requestManualSync();
					} else {
						showMessage("It's already your contact!");
					}
				}
				break;
			}
			}
	}
}
