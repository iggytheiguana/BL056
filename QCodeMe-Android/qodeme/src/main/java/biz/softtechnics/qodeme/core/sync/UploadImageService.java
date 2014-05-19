package biz.softtechnics.qodeme.core.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.IntentService;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import biz.softtechnics.qodeme.core.io.RestAsyncHelper;
import biz.softtechnics.qodeme.core.io.responses.UploadImageResponse1;
import biz.softtechnics.qodeme.core.io.utils.RestError;
import biz.softtechnics.qodeme.core.io.utils.RestListener;
import biz.softtechnics.qodeme.core.provider.QodemeContract;
import biz.softtechnics.qodeme.utils.DbUtils;

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
		//RestSyncHelper rest = RestSyncHelper.getInstance(this);
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
			RestAsyncHelper.getInstance().chatImage(messageId, imageLocalPath,
					new RestListener<UploadImageResponse1>() {

						@Override
						public void onResponse(UploadImageResponse1 response) {
							Log.d("Upload", "Url = "+response.getUrl());
							getContentResolver().update(QodemeContract.Messages.CONTENT_URI, QodemeContract.Messages.updateMessageImageUrl(response.getUrl()), DbUtils.getWhereClauseForId(), DbUtils.getWhereArgsForId(response.getMessageId()));
							
						}

						@Override
						public void onServiceError(RestError error) {

						}
					});
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
