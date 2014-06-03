package com.blulabellabs.code.core.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.IntentService;
import android.content.Entity;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.blulabellabs.code.core.io.RestAsyncHelper;
import com.blulabellabs.code.core.io.responses.UploadImageResponse1;
import com.blulabellabs.code.core.io.utils.RequestType;
import com.blulabellabs.code.core.io.utils.RestError;
import com.blulabellabs.code.core.io.utils.RestKeyMap;
import com.blulabellabs.code.core.io.utils.RestListener;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.utils.DbUtils;
import com.blulabellabs.code.utils.RestUtils;
import com.google.common.io.ByteStreams;

public class UploadImageService extends IntentService {

	public static final String MESSAGE_ID = "message_id";
	public static final String LOCAL_PATH = "local_path";

	public UploadImageService() {
		super("UploadImageService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		long messageId = intent.getLongExtra(MESSAGE_ID, 0);
		String imageLocalPath = intent.getStringExtra(LOCAL_PATH);
		String mProfileImageBase64 = null;
		// RestSyncHelper rest = RestSyncHelper.getInstance(this);
		try {
			File file = new File(imageLocalPath);
			byte[] byteArray = convertFileToByteArray(file);

			mProfileImageBase64 = Base64.encodeToString(byteArray, Base64.NO_WRAP);

		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mProfileImageBase64 != null) {
			// UploadImageResponse imageResponse = rest.chatImage(messageId,
			// mProfileImageBase64);
			// new ChatImageUploadHandler(this,
			// messageId).parseAndApply(imageResponse);

//			HttpClient client = new DefaultHttpClient();
//			HttpPost httpPost = new HttpPost(RestUtils.getAbsoluteUrl(RequestType.UPLOAD_IMAGE));
//
//			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
//			urlParameters.add(new BasicNameValuePair(RestKeyMap.IMAGE, mProfileImageBase64));
//			urlParameters.add(new BasicNameValuePair(RestKeyMap.MESSAGE_ID, String.valueOf(messageId)));
//			try {
//				httpPost.setEntity(urlParameters);
//				
//				HttpResponse httpResponse = client.execute(httpPost);
//				
//				String response = EntityUtils.toString(httpResponse.getEntity());
//				Log.d("ImgeUpload response", response+"");
//			} catch (Exception e) {
//				// TODO: handle exception
//				e.printStackTrace();
//			}

			// RestAsyncHelper.getInstance().chatImage(messageId,
			// mProfileImageBase64,
			// new RestListener<UploadImageResponse1>() {
			//
			// @Override
			// public void onResponse(UploadImageResponse1 response) {
			// Log.d("Upload", "Url = "+response.getUrl());
			// getContentResolver().update(QodemeContract.Messages.CONTENT_URI,
			// QodemeContract.Messages.updateMessageImageUrl(response.getUrl()),
			// DbUtils.getWhereClauseForId(),
			// DbUtils.getWhereArgsForId(response.getMessageId()));
			//
			// }
			//
			// @Override
			// public void onServiceError(RestError error) {
			//
			// }
			// });
		}
	}

	public static byte[] convertFileToByteArray(File f) {
		byte[] byteArray = null;
		try {
			InputStream inputStream = new FileInputStream(f);

			byteArray = ByteStreams.toByteArray(inputStream);
			// ByteArrayOutputStream bos = new ByteArrayOutputStream();
			// byte[] b = new byte[1024 * 8];
			// int bytesRead = 0;
			//
			// while ((bytesRead = inputStream.read(b)) != -1) {
			// bos.write(b, 0, bytesRead);
			// }
			//
			// byteArray = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteArray;
	}

}
