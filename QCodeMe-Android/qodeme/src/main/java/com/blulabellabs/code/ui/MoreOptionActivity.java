package com.blulabellabs.code.ui;

import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.IntentKey;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.ui.qr.QrCodeShowActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MoreOptionActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more);

		((TextView) findViewById(R.id.textView_share)).setOnClickListener(this);
		((TextView) findViewById(R.id.textView_scanQR)).setOnClickListener(this);
		((TextView) findViewById(R.id.textView_logout)).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.textView_share:
			// Intent i = new Intent(this, QrCodeShowActivity.class);
			// i.putExtra(IntentKey.QR_CODE,
			// QodemePreferences.getInstance().getQrcode());
			// startActivity(i);
			Intent intent = new Intent();
			intent.putExtra("type", 3);
			setResult(RESULT_OK, intent);
			finish();
			break;
		case R.id.textView_logout:
			// RestAsyncHelper.getInstance().registerToken("", new
			// RestListener() {
			//
			// @Override
			// public void onResponse(BaseResponse response) {
			// RestAsyncHelper.getInstance().accountLogout(new RestListener() {
			// @Override
			// public void onResponse(BaseResponse response) {
			// logoutHandler();
			// }
			//
			// @Override
			// public void onServiceError(RestError error) {
			// showMessage(RestErrorType.getMessage(MoreOptionActivity.this,
			// error.getErrorType())
			// + error.getServerMsg());
			// }
			//
			// @Override
			// public void onNetworkError(VolleyError error) {
			// super.onNetworkError(error);
			// showMessage(error.getMessage());
			// }
			// });
			// }
			//
			// @Override
			// public void onServiceError(RestError error) {
			// showMessage(RestErrorType.getMessage(MoreOptionActivity.this,
			// error.getErrorType())
			// + error.getServerMsg());
			// }
			//
			// @Override
			// public void onNetworkError(VolleyError error) {
			// super.onNetworkError(error);
			// showMessage("No internet connection!");
			// }
			//
			// private void logoutHandler() {
			// QodemePreferences.getInstance().setLogged(false);
			// QodemePreferences.getInstance().setGcmTokenSycnWithRest(false);
			// startActivity(new Intent(getApplicationContext(),
			// LoginActivity.class));
			// finish();
			//
			// }
			// });

			Intent intent1 = new Intent();
			intent1.putExtra("type", 1);
			setResult(RESULT_OK, intent1);
			finish();
			break;
		case R.id.textView_scanQR:
			Intent intent2 = new Intent();
			intent2.putExtra("type", 2);
			setResult(RESULT_OK, intent2);
			finish();
			break;
		default:
			break;
		}
	}
	// private void showMessage(String message) {
	// new AlertDialog.Builder(this).setTitle("Attention").setMessage(message)
	// .setCancelable(true)
	// .setPositiveButton(android.R.string.ok, new
	// DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// dialog.dismiss();
	// }
	// }).create().show();
	// }
}
