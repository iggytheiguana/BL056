package com.blulabellabs.code.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import com.android.volley.VolleyError;
import com.blulabellabs.code.ApplicationConstants;
import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.IntentKey;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.RestAsyncHelper;
import com.blulabellabs.code.core.io.responses.AccountLoginResponse;
import com.blulabellabs.code.core.io.utils.RestError;
import com.blulabellabs.code.core.io.utils.RestListener;
import com.blulabellabs.code.core.sync.SyncHelper;
import com.blulabellabs.code.utils.AnalyticsHelper;
import com.blulabellabs.code.utils.PlayServicesUtils;
import com.blulabellabs.code.utils.QrUtils;
import com.blulabellabs.code.utils.RestUtils;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

/**
 * Created by Alex on 10/7/13.
 */
public class EmailActivity extends Activity implements OnClickListener {

	private static final int REQUEST_ACTIVITY_REGISTRATION = 1;
	private static final int REQUEST_ACTIVITY_SCAN_QR_CODE = 2;

	private ImageButton mQrCode;
	private Button mSignin, mBtnSignUp;
//	private EditText mPassword;
	private String mQrCodeText;
//	private TextView mTextViewNotYou, mTextViewClear, mTextViewEmptyQR;

	private Tracker mGaTracker;
	private GoogleAnalytics mGaInstance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AnalyticsHelper.onCreateActivity(this);
		setContentView(R.layout.activity_email);
		mQrCode = (ImageButton) findViewById(R.id.qr_code);
		mSignin = (Button) findViewById(R.id.email);
		mBtnSignUp = (Button) findViewById(R.id.cancel);
//		mPassword = (EditText) findViewById(R.id.password);
		mQrCodeText = QodemePreferences.getInstance().getQrcode();
//		mTextViewNotYou = (TextView) findViewById(R.id.textView_not_you);
//		mTextViewClear = (TextView) findViewById(R.id.textView_clearQR);
//		mTextViewEmptyQR = (TextView) findViewById(R.id.textView_emptyQRText);
//		mTextViewClear.setOnClickListener(this);
//		mTextViewNotYou.setOnClickListener(this);
		mSignin.setEnabled(true);

//		mPassword.addTextChangedListener(new TextWatcher() {
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				if (mPassword.getText().toString().trim().length() > 0)
//					mSignin.setEnabled(true);
//				else
//					mSignin.setEnabled(false);
//			}
//
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//			}
//
//			@Override
//			public void afterTextChanged(Editable s) {
//
//			}
//		});

		refreshQrCode();

//		mQrCode.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent i = new Intent(EmailActivity.this, QrCodeCaptureActivity.class);
//				i.putExtra(IntentKey.CHAT_TYPE, QrCodeCaptureActivity.QODEME_CONTACT);
//				startActivityForResult(i, REQUEST_ACTIVITY_SCAN_QR_CODE);
//			}
//		});
//
//		mQrCode.setOnLongClickListener(new View.OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View v) {
//				startActivityForResult(new Intent(EmailActivity.this, RegistrationActivity.class),
//						REQUEST_ACTIVITY_REGISTRATION);
//				return true;
//			}
//		});
		mBtnSignUp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				startActivityForResult(new Intent(EmailActivity.this, RegistrationActivity.class),
//						REQUEST_ACTIVITY_REGISTRATION);
				String password = QodemePreferences.getInstance().getPassword();
				String qrCode = mQrCodeText;
				if (TextUtils.isEmpty(password)) {
					showMessage(getString(R.string.alert_empty_password));
					return;
				} else if (TextUtils.isEmpty(qrCode)) {
					showMessage(getString(R.string.alert_empty_qr_code));
					return;
				}

				mSignin.setEnabled(false);
				final String passwordMd5 = RestUtils.getMd5(password);//mPassword.getText().toString()

				RestAsyncHelper.getInstance().accountLogin(qrCode, passwordMd5,
						new RestListener<AccountLoginResponse>() {
							@Override
							public void onResponse(AccountLoginResponse response) {
								SyncHelper.requestAfterLoginSync();
								startActivity(new Intent(EmailActivity.this, MainActivity.class));
								finish();
							}

							@Override
							public void onServiceError(RestError error) {
								showMessage(error.getServerMsg());
								mSignin.setEnabled(true);
							}

							@Override
							public void onNetworkError(VolleyError error) {
								super.onNetworkError(error);
								showMessage(getString(R.string.alert_no_internet));
								mSignin.setEnabled(true);
							}
						});
			}
		});
		mSignin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String password = QodemePreferences.getInstance().getPassword();//mPassword.getText().toString();
				String qrCode = mQrCodeText;
				
//				email(mQrCodeText);
				
				if (TextUtils.isEmpty(password)) {
					showMessage(getString(R.string.alert_empty_password));
					return;
				} else if (TextUtils.isEmpty(qrCode)) {
					showMessage(getString(R.string.alert_empty_qr_code));
					return;
				}

				mSignin.setEnabled(false);
				final String passwordMd5 = RestUtils.getMd5(password);//mPassword.getText().toString()

				RestAsyncHelper.getInstance().accountLogin(qrCode, passwordMd5,
						new RestListener<AccountLoginResponse>() {
							@Override
							public void onResponse(AccountLoginResponse response) {
								SyncHelper.requestAfterLoginSync();
								startActivity(new Intent(EmailActivity.this, MainActivity.class));
								email(mQrCodeText);
								finish();
							}

							@Override
							public void onServiceError(RestError error) {
								showMessage(error.getServerMsg());
								mSignin.setEnabled(true);
							}

							@Override
							public void onNetworkError(VolleyError error) {
								super.onNetworkError(error);
								showMessage(getString(R.string.alert_no_internet));
								mSignin.setEnabled(true);
							}
						});
			}
		});

		// Check if play service available
		PlayServicesUtils.checkGooglePlaySevices(this);

//		mPassword.setTypeface(Application.typefaceMediumItalic);
	}
	private void email(String qrCode) {
		Bitmap mBitmap = QrUtils.encodeQrCode((TextUtils.isEmpty(qrCode) ? "Qr Code"
				: ApplicationConstants.QR_CODE_CONTACT_PREFIX + qrCode), 500, 500,
				Color.BLACK, Color.WHITE);
		String path = MediaStore.Images.Media.insertImage(getContentResolver(), mBitmap, "title",
				null);
		if (path == null) {
			showMessage(getString(R.string.alert_no_access_to_external_storage));
			return;
		}

		String data = "<html><body><h1>Welcome to Code Me!</h1><hr><br><p>Attached QR Code is your unique QR Code. This is your identity on Thred.You will need this code to sign in to Thread-keep it safe.</p><br><br>"
				+ "<a href=\"code:other/parameter\"> Log In </a> </body></html>";
		Uri screenshotUri = Uri.parse(path);
		final Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		emailIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
		emailIntent.setType("image/png");
		emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(data));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "QODEME contact");
		startActivity(Intent.createChooser(emailIntent, "Send email using"));
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_ACTIVITY_REGISTRATION) {
				mQrCodeText = data.getStringExtra(IntentKey.QR_CODE);
				QodemePreferences.getInstance().setQrcode(mQrCodeText);
				refreshQrCode();
			} else if (requestCode == REQUEST_ACTIVITY_SCAN_QR_CODE) {
				mQrCodeText = data.getStringExtra(IntentKey.QR_CODE);
				refreshQrCode();
			}

		}
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

	private void refreshQrCode() {
		mQrCode.setImageBitmap(QrUtils.encodeQrCode((TextUtils.isEmpty(mQrCodeText) ? "Qr Code"
				: ApplicationConstants.QR_CODE_CONTACT_PREFIX + mQrCodeText), 500, 500,
				getResources().getColor(R.color.login_qrcode), Color.TRANSPARENT));

		if (mQrCodeText == null || mQrCodeText.trim().equals("")) {
			mQrCode.setImageBitmap(BitmapFactory.decodeResource(getResources(),
					R.drawable.ic_logo_with_qr));
//			mTextViewEmptyQR.setVisibility(View.VISIBLE);
//			mTextViewClear.setVisibility(View.GONE);
//			mTextViewNotYou.setVisibility(View.GONE);
		} else {
//			mTextViewEmptyQR.setVisibility(View.GONE);
//			mTextViewClear.setVisibility(View.VISIBLE);
//			mTextViewNotYou.setVisibility(View.VISIBLE);
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

	public Context getContext() {
		return this;
	}

	@Override
	public void onClick(View v) {
		QodemePreferences.getInstance().setQrcode(null);
		mQrCode.setImageBitmap(BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_logo_with_qr));
//		mTextViewEmptyQR.setVisibility(View.VISIBLE);
//		mTextViewClear.setVisibility(View.GONE);
//		mTextViewNotYou.setVisibility(View.GONE);
	}

}
