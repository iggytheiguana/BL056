package biz.softtechnics.qodeme.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import biz.softtechnics.qodeme.Application;
import biz.softtechnics.qodeme.ApplicationConstants;
import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.data.IntentKey;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.io.RestAsyncHelper;
import biz.softtechnics.qodeme.core.io.responses.AccountLoginResponse;
import biz.softtechnics.qodeme.core.io.utils.RestError;
import biz.softtechnics.qodeme.core.io.utils.RestListener;
import biz.softtechnics.qodeme.core.sync.SyncHelper;
import biz.softtechnics.qodeme.ui.qr.QrCodeCaptureActivity;
import biz.softtechnics.qodeme.utils.AnalyticsHelper;
import biz.softtechnics.qodeme.utils.PlayServicesUtils;
import biz.softtechnics.qodeme.utils.QrUtils;
import biz.softtechnics.qodeme.utils.RestUtils;

import com.android.volley.VolleyError;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

/**
 * Created by Alex on 10/7/13.
 */
public class LoginActivity extends Activity implements OnClickListener {

	private static final int REQUEST_ACTIVITY_REGISTRATION = 1;
	private static final int REQUEST_ACTIVITY_SCAN_QR_CODE = 2;

	private ImageButton mQrCode;
	private Button mSignin;
	private EditText mPassword;
	private String mQrCodeText;
	private TextView mTextViewNotYou, mTextViewClear, mTextViewEmptyQR;

	private Tracker mGaTracker;
	private GoogleAnalytics mGaInstance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AnalyticsHelper.onCreateActivity(this);
		setContentView(R.layout.activity_login);
		mQrCode = (ImageButton) findViewById(R.id.qr_code);
		mSignin = (Button) findViewById(R.id.signin);
		mPassword = (EditText) findViewById(R.id.password);
		mQrCodeText = QodemePreferences.getInstance().getQrcode();
		mTextViewNotYou = (TextView) findViewById(R.id.textView_not_you);
		mTextViewClear = (TextView) findViewById(R.id.textView_clearQR);
		mTextViewEmptyQR = (TextView) findViewById(R.id.textView_emptyQRText);
		mTextViewClear.setOnClickListener(this);
		mTextViewNotYou.setOnClickListener(this);

		mPassword.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(mPassword.getText().toString().trim().length()>0)
					mSignin.setEnabled(true);
				else
					mSignin.setEnabled(false);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		
		refreshQrCode();
		
		
		mQrCode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(LoginActivity.this, QrCodeCaptureActivity.class);
				i.putExtra(IntentKey.CHAT_TYPE, QrCodeCaptureActivity.QODEME_CONTACT);
				startActivityForResult(i, REQUEST_ACTIVITY_SCAN_QR_CODE);
			}
		});

		mQrCode.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				startActivityForResult(new Intent(LoginActivity.this, RegistrationActivity.class),
						REQUEST_ACTIVITY_REGISTRATION);
				return true;
			}
		});

		mSignin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String password = mPassword.getText().toString();
				String qrCode = mQrCodeText;
				if (TextUtils.isEmpty(password)) {
					showMessage(getString(R.string.alert_empty_password));
					return;
				} else if (TextUtils.isEmpty(qrCode)) {
					showMessage(getString(R.string.alert_empty_qr_code));
					return;
				}

				mSignin.setEnabled(false);
				final String passwordMd5 = RestUtils.getMd5(mPassword.getText().toString());

				RestAsyncHelper.getInstance().accountLogin(qrCode, passwordMd5,
						new RestListener<AccountLoginResponse>() {
							@Override
							public void onResponse(AccountLoginResponse response) {
								SyncHelper.requestAfterLoginSync();
								startActivity(new Intent(LoginActivity.this, MainActivity.class));
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

		mPassword.setTypeface(Application.typefaceMediumItalic);
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
		
		if(mQrCodeText == null || mQrCodeText.trim().equals("")){
			mQrCode.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_logo_with_qr));
			mTextViewEmptyQR.setVisibility(View.VISIBLE);
			mTextViewClear.setVisibility(View.GONE);
			mTextViewNotYou.setVisibility(View.GONE);
		}else{
			mTextViewEmptyQR.setVisibility(View.GONE);
			mTextViewClear.setVisibility(View.VISIBLE);
			mTextViewNotYou.setVisibility(View.VISIBLE);
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
		mQrCode.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_logo_with_qr));
		mTextViewEmptyQR.setVisibility(View.VISIBLE);
		mTextViewClear.setVisibility(View.GONE);
		mTextViewNotYou.setVisibility(View.GONE);
	}

}
