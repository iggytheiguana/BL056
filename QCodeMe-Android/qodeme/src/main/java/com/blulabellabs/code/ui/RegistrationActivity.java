package com.blulabellabs.code.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.blulabellabs.code.Application;
import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.IntentKey;
import com.blulabellabs.code.core.io.RestAsyncHelper;
import com.blulabellabs.code.core.io.responses.AccountCreateResponse;
import com.blulabellabs.code.core.io.utils.RestError;
import com.blulabellabs.code.core.io.utils.RestErrorType;
import com.blulabellabs.code.core.io.utils.RestListener;
import com.blulabellabs.code.utils.AnalyticsHelper;
import com.blulabellabs.code.utils.QrUtils;
import com.blulabellabs.code.utils.RestUtils;


/**
 * Created by Alex on 10/7/13.
 */
public class RegistrationActivity extends Activity {

	private ImageView mQrCode;
	private String mQrCodeText;
	private EditText mPassword1;
	private EditText mPassword2;
	private TextView mTextViewErrorMessage;
	private ProgressDialog mProgressDialog;
	private Button mQodeme;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AnalyticsHelper.onCreateActivity(this);
		setContentView(R.layout.activity_registration);
		mQrCode = (ImageView) findViewById(R.id.qr_code);
		mTextViewErrorMessage = (TextView) findViewById(R.id.textView_errorMessage);

		//refreshQrCode();
		mPassword1 = (EditText) findViewById(R.id.password);
		mPassword2 = (EditText) findViewById(R.id.password2);
		mPassword1.requestFocus();

		mQodeme = (Button) findViewById(R.id.qodeme);
		mQodeme.setTypeface(Application.typefaceMedium);
		mQodeme.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(mPassword1.getText().toString())) {
					// showMessage(getString(R.string.alert_empty_password));
					mTextViewErrorMessage.setVisibility(View.VISIBLE);
					mTextViewErrorMessage.setText(getString(R.string.alert_empty_password));
					mPassword1.requestFocus();
					return;
				}

				if (TextUtils.isEmpty(mPassword1.getText().toString())) {
					// showMessage(getString(R.string.alert_empty_password));
					mTextViewErrorMessage.setVisibility(View.VISIBLE);
					mTextViewErrorMessage.setText(getString(R.string.alert_empty_password));
					mPassword2.requestFocus();
					return;
				}

				if (!mPassword1.getText().toString().equals(mPassword2.getText().toString())) {
					// showMessage(getString(R.string.alert_password_not_match));
					mTextViewErrorMessage.setVisibility(View.VISIBLE);
					mTextViewErrorMessage.setText(getString(R.string.password_must_match));
					mPassword2.requestFocus();
					return;
				}

				mTextViewErrorMessage.setVisibility(View.GONE);
				mQodeme.setEnabled(false);

				// mProgressDialog = new ProgressDialog(getContext());
				// mProgressDialog.show();
				final String passwordMd5 = RestUtils.getMd5(mPassword1.getText().toString());

				RestAsyncHelper.getInstance().accountCreate(passwordMd5,
						new RestListener<AccountCreateResponse>() {
							@Override
							public void onResponse(AccountCreateResponse response) {
								Intent i = new Intent();
								i.putExtra(IntentKey.QR_CODE, response.getQrcode());
								setResult(RESULT_OK, i);
								finish();
							}

							@Override
							public void onServiceError(RestError error) {
								showMessage(RestErrorType.getMessage(getContext(),
										error.getErrorType()));
								mQodeme.setEnabled(true);
							}

							@Override
							public void onNetworkError(VolleyError error) {
								super.onNetworkError(error);
								showMessage(getString(R.string.alert_no_internet));
								mQodeme.setEnabled(true);
							}
						});
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

	private void refreshQrCode() {
		
		mQrCode.setImageBitmap(QrUtils.encodeQrCode((TextUtils.isEmpty(mQrCodeText) ? "Qr Code"
				: mQrCodeText), 500, 500, getResources().getColor(R.color.login_qrcode),
				Color.TRANSPARENT));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			// do nothing
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public Context getContext() {
		return this;
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

}
