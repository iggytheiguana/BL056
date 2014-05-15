package biz.softtechnics.qodeme.ui.one2one;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.io.RestAsyncHelper;
import biz.softtechnics.qodeme.core.io.model.ChatLoad;
import biz.softtechnics.qodeme.core.io.responses.VoidResponse;
import biz.softtechnics.qodeme.core.io.utils.RestError;
import biz.softtechnics.qodeme.core.io.utils.RestListener;
import biz.softtechnics.qodeme.core.provider.QodemeContract;
import biz.softtechnics.qodeme.core.sync.SyncHelper;
import biz.softtechnics.qodeme.ui.MainActivity;
import biz.softtechnics.qodeme.utils.DbUtils;

public class ChatGroupProfileFragment extends Fragment implements OnClickListener {

	private static final String CHAT_ID = "chat_id";
	private static final String CHAT_COLOR = "chat_color";
	private static final String CHAT_NAME = "chat_name";
	private static final String QRCODE = "contact_qr";
	private static final String LOCATION = "location";
	private static final String DATE = "date";
	private static final String FIRST_UPDATE = "first_update";

	private TextView  mTextViewDate,  mTextViewLocation,
			mTextViewStatus, mTextViewTotalMessages, mTextViewTotalPhoto, mTextViewGroupTitle,mTextViewGroupDesc;
	private ImageButton  mImgBtnColorWheel;
	private ImageButton mBtnEditStatus, mBtnDelete, mBtnEditDesc;
	private Button mBtnSetStatus, mBtnSetDesc;
	private EditText mEditTextStatus;
	private RelativeLayout mRelativeLayoutDesc, mRelativeLayoutSetDesc;
	ChatLoad chatload;

	public static ChatGroupProfileFragment newInstance(ChatLoad c, boolean firstUpdate) {
		ChatGroupProfileFragment f = new ChatGroupProfileFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putLong(CHAT_ID, c.chatId);
		args.putInt(CHAT_COLOR, c.color);
		args.putString(CHAT_NAME, c.title);
		args.putString(QRCODE, c.qrcode);
		// args.putString(LOCATION, c.location);
		// args.putString(DATE, c.date);
		args.putBoolean(FIRST_UPDATE, firstUpdate);
		f.chatload = c;
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_profile_group, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mTextViewDate = (TextView) getView().findViewById(R.id.textView_createdDate);
		mTextViewLocation = (TextView) getView().findViewById(R.id.textView_location);
		mTextViewStatus = (TextView) getView().findViewById(R.id.textView_status);
		mTextViewTotalMessages = (TextView) getView().findViewById(R.id.textView_totalMessages);
		mTextViewTotalPhoto = (TextView) getView().findViewById(R.id.textView_totalPhoto);
		mImgBtnColorWheel = (ImageButton) getView().findViewById(R.id.imgBtn_colorWheelBig);
		mBtnEditStatus = (ImageButton) getView().findViewById(R.id.btnEditStatus);
		mBtnSetStatus = (Button) getView().findViewById(R.id.btnSetStatus);
		mEditTextStatus = (EditText) getView().findViewById(R.id.editText_status);
		mBtnDelete = (ImageButton) getView().findViewById(R.id.btnDelete);
		mBtnEditDesc = (ImageButton) getView().findViewById(R.id.btnEditDesc);
		mBtnSetDesc = (Button) getView().findViewById(R.id.btnSetDesc);

		mRelativeLayoutDesc = (RelativeLayout) getView().findViewById(R.id.relative_desc);
		mRelativeLayoutSetDesc = (RelativeLayout) getView().findViewById(R.id.relative_editDesc);
		
		mBtnDelete.setOnClickListener(this);
		mImgBtnColorWheel.setOnClickListener(this);
		mBtnEditStatus.setOnClickListener(this);
		mBtnSetStatus.setOnClickListener(this);
		mBtnEditDesc.setOnClickListener(this);
		mBtnSetDesc.setOnClickListener(this);

		if (chatload.type == 1) {

		} else {

		}
		setData();
	}

	private void setData() {

		SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yy h:mm a", Locale.US);
		// SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yy HH:mm");
		// String dateStr = fmtOut.format(new Date(Converter
		// .getCrurentTimeFromTimestamp(getArguments().getString(DATE))));
		// mTextViewLocation.setText(chatload.location + "");
		// mTextViewDatelocation.setText(dateStr + ", " + chatload.location);

		SimpleDateFormat fmtOut1 = new SimpleDateFormat("MMMMM dd,yyyy, h:mm a", Locale.US);
		// String dateStr1 = fmtOut1.format(new Date(Converter
		// .getCrurentTimeFromTimestamp(getArguments().getString(DATE))));
		// mTextViewDate.setText(dateStr1);
	}

	private void callColorPicker() {
		MainActivity activity = (MainActivity) getActivity();
		activity.callColorPicker(chatload);
		// Intent i = new Intent((MainActivity)getActivity(),
		// ContactDetailsActivity.class);
		// i.putExtra(QodemeContract.Contacts._ID,
		// getArguments().getLong(CHAT_ID));
		// i.putExtra(QodemeContract.Contacts.CONTACT_TITLE,
		// getArguments().getString(CHAT_NAME));
		// i.putExtra(QodemeContract.Contacts.CONTACT_COLOR,
		// getArguments().getInt(CHAT_COLOR));
		// i.putExtra(QodemeContract.Contacts.UPDATED, contact.updated);
		// startActivityForResult(i, REQUEST_ACTIVITY_CONTACT_DETAILS);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imgBtn_colorWheelSmall:
		case R.id.imgBtn_colorWheelBig:
			callColorPicker();
			break;
		case R.id.btnEditStatus:
			mTextViewStatus.setVisibility(View.GONE);
			mBtnEditStatus.setVisibility(View.GONE);
			mEditTextStatus.setVisibility(View.VISIBLE);
			mBtnSetStatus.setVisibility(View.VISIBLE);
			break;

		case R.id.btnSetStatus:
			mTextViewStatus.setVisibility(View.VISIBLE);
			mBtnEditStatus.setVisibility(View.VISIBLE);
			mEditTextStatus.setVisibility(View.GONE);
			mBtnSetStatus.setVisibility(View.GONE);

			String status = mEditTextStatus.getText().toString();

			if (!status.trim().equals("")) {
				mTextViewStatus.setText(status);
				int updated = chatload.updated;
				getActivity().getContentResolver().update(
						QodemeContract.Chats.CONTENT_URI,
						QodemeContract.Chats.updateChatInfoValues("", -1, "", 0, status, "",
								updated, 4), QodemeContract.Chats.CHAT_ID + "=?",
						DbUtils.getWhereArgsForId(chatload.chatId));

				// String title, int color,
				// String description, int is_locked, String status, String
				// tags, int updated,
				// int updateType) {

				SyncHelper.requestManualSync();
			}

			break;
		case R.id.btnEditDesc:
			mRelativeLayoutDesc.setVisibility(View.GONE);
			mRelativeLayoutSetDesc.setVisibility(View.VISIBLE);
			break;
		case R.id.btnSetDesc:
			mRelativeLayoutDesc.setVisibility(View.VISIBLE);
			mRelativeLayoutSetDesc.setVisibility(View.GONE);
			break;
		case R.id.btnDelete:
			deleteContact();
			break;
		default:
			break;
		}
	}

	private void deleteContact() {
		RestAsyncHelper.getInstance().contactRemove(chatload.qrcode,
				new RestListener<VoidResponse>() {

					@Override
					public void onResponse(VoidResponse response) {
						Log.d("Response", "successfull remove contact");
					}

					@Override
					public void onServiceError(RestError error) {
						Log.d("Error", "Error in remove contact");

					}
				});
	}
}
