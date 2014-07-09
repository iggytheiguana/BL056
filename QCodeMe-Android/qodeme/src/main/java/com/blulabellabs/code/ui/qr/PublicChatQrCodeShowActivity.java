package com.blulabellabs.code.ui.qr;

import java.util.List;

import org.json.JSONObject;

import com.blulabellabs.code.ApplicationConstants;
import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.IntentKey;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.sync.SyncHelper;
import com.blulabellabs.code.utils.AnalyticsHelper;
import com.blulabellabs.code.utils.Helper;
import com.blulabellabs.code.utils.QrUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
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
public class PublicChatQrCodeShowActivity extends Activity {

	private static final int REQUEST_ACTIVITY_SCAN_QR_CODE = 2;
	private ImageButton mQrCode;
	private Button mEmailButton;
	private Bitmap mBitmap;
	private TextView editTextName;
	private ProgressDialog progressDialog;
	private String memberMessagesText = "";
	private long chat_id;
	private String chatTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AnalyticsHelper.onCreateActivity(this);
		setContentView(R.layout.activity_show_public_chat_qr);
		mQrCode = (ImageButton) findViewById(R.id.qr_code);
		final String qrCode = getIntent().getStringExtra(IntentKey.QR_CODE);
		chatTitle = getIntent().getStringExtra(IntentKey.CONTACT_NAME);
		chat_id = getIntent().getLongExtra(IntentKey.CHAT_ID, -1);

		memberMessagesText = getIntent().getStringExtra("text");
		refreshQrCode(qrCode);

		editTextName = (TextView) findViewById(R.id.edit_name);
		editTextName.setText(chatTitle);
		// editTextName.setEnabled(false);

		mEmailButton = (Button) findViewById(R.id.email);
		mEmailButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				email();
			}
		});
		// mScanButton = (Button) findViewById(R.id.scanQR);
		// mScanButton.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Intent intent1 = new Intent();
		// intent1.putExtra("type", 2);
		// setResult(RESULT_OK, intent1);
		// finish();
		// }
		// });

		// editTextName.setOnEditorActionListener(new OnEditorActionListener() {
		//
		// @Override
		// public boolean onEditorAction(TextView v, int actionId, KeyEvent
		// event) {
		// if (actionId == EditorInfo.IME_ACTION_SEARCH
		// || actionId == EditorInfo.IME_ACTION_GO
		// || actionId == EditorInfo.IME_ACTION_DONE
		// || event.getAction() == KeyEvent.ACTION_DOWN
		// || actionId == EditorInfo.IME_ACTION_SEND
		// && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
		//
		// QodemePreferences.getInstance()
		// .setPublicName(editTextName.getText().toString());
		// QodemePreferences.getInstance().setUserSettingsUpToDate(false);
		// SyncHelper.requestManualSync();
		// Helper.hideKeyboard(PublicChatQrCodeShowActivity.this, editTextName);
		//
		// return true;
		// }
		// return false;
		// }
		// });

		// String name = QodemePreferences.getInstance().getPublicName();
		// if (name != null && !name.trim().equals(""))
		// editTextName.setText(name);

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
//		mBitmap = QrUtils.encodeQrCode((TextUtils.isEmpty(qrCode) ? "Qr Code"
//				: ApplicationConstants.QR_CODE_CONTACT_PREFIX + qrCode), 500, 500, Color.BLACK,
//				Color.WHITE);
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(IntentKey.QR_CODE, qrCode);
			jsonObject.put(IntentKey.CHAT_TYPE, 2);
			jsonObject.put(IntentKey.CONTACT_NAME, chatTitle);
			jsonObject.put(IntentKey.CHAT_ID, chat_id);
		} catch (Exception e) {
		}
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
		progressDialog = ProgressDialog.show(this, "", "Sharing...");
		ShareAsyncTask asyncTask = new ShareAsyncTask();
		asyncTask.execute("");
	}

	class ShareAsyncTask extends AsyncTask<String, String, String> {

		String data = "";
		String path;

		public ShareAsyncTask() {
		}

		@Override
		protected String doInBackground(String... params) {
			path = MediaStore.Images.Media
					.insertImage(getContentResolver(), mBitmap, "title", null);


			data = "<html><body><h1>Join the Conversation</h1><hr><br><p>The conversation "
					+ editTextName.getText().toString()
					+ " has been shared with you. Scan the attached code to join the conversation.</p><br><br><h2>"
					+ editTextName.getText().toString()
					+ "</h2><br><p>"
					+ memberMessagesText
					+ "</p>"
					+ "<a href=\"code:other/parameter\"> View Conversation </a> <br><hr><h2>What is Code!?</h2><br><p>Lorem ipsum dolor sit amet, sldfha consectetur adipisicing elit, sed do eiusmod tempor incididunt ut lab et dolore magna eliqua.</p><br><h2>Available On</h2><br><a href=\"http://play.google.com/store/apps/details?id=com.blulabellabs.code\"> Google Play </a></body></html>";
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (progressDialog != null && progressDialog.isShowing())
				progressDialog.dismiss();
			if (path == null) {
				showMessage(getString(R.string.alert_no_access_to_external_storage));
				return;
			}
			Uri screenshotUri = Uri.parse(path);
			final Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			emailIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
			emailIntent.setType("image/png");
			emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(data));
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Code! contact");
			startActivity(Intent.createChooser(emailIntent, "Send email using"));
		}

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
