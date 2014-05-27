package biz.softtechnics.qodeme.ui.one2one;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.android.volley.VolleyError;
import com.google.android.gms.internal.ac;
import com.google.android.gms.internal.co;
import com.google.android.gms.internal.cu;

import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.io.RestAsyncHelper;
import biz.softtechnics.qodeme.core.io.model.ChatLoad;
import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.core.io.responses.AccountLoginResponse;
import biz.softtechnics.qodeme.core.io.responses.VoidResponse;
import biz.softtechnics.qodeme.core.io.utils.RestError;
import biz.softtechnics.qodeme.core.io.utils.RestListener;
import biz.softtechnics.qodeme.core.provider.QodemeContract;
import biz.softtechnics.qodeme.core.sync.SyncHelper;
import biz.softtechnics.qodeme.ui.LoginActivity;
import biz.softtechnics.qodeme.ui.MainActivity;
import biz.softtechnics.qodeme.ui.common.CustomDotView;
import biz.softtechnics.qodeme.ui.contacts.ContactDetailsActivity;
import biz.softtechnics.qodeme.ui.one2one.ChatInsideFragment.One2OneChatInsideFragmentCallback;
import biz.softtechnics.qodeme.utils.Converter;
import biz.softtechnics.qodeme.utils.DbUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class ChatProfileFragment extends Fragment implements OnClickListener {

	private static final String CHAT_ID = "chat_id";
	private static final String CHAT_COLOR = "chat_color";
	private static final String CHAT_NAME = "chat_name";
	private static final String QRCODE = "contact_qr";
	private static final String LOCATION = "location";
	private static final String DATE = "date";
	private static final String FIRST_UPDATE = "first_update";

	private TextView mTextViewProfileName, mTextViewDate, mTextViewDatelocation, mTextViewLocation,
			mTextViewStatus, mTextViewTotalMessages, mTextViewTotalPhoto;
	private ImageButton mImgBtnColorWheelSmall, mImgBtnColorWheel;
	private CustomDotView customDotView;
	private ImageButton mBtnEditStatus, mBtnEditName, mBtnDelete;
	private Button mBtnSetStatus, mBtnSetName;
	private EditText mEditTextStatus, mEdittextName;
	private RelativeLayout mRelativeLayoutName, mRelativeLayoutSetName;
	Contact contact;
	private One2OneChatInsideFragmentCallback callback;

	public static ChatProfileFragment newInstance(Contact c, boolean firstUpdate) {
		ChatProfileFragment f = new ChatProfileFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putLong(CHAT_ID, c.chatId);
		args.putInt(CHAT_COLOR, c.color);
		args.putString(CHAT_NAME, c.title);
		args.putString(QRCODE, c.qrCode);
		args.putString(LOCATION, c.location);
		args.putString(DATE, c.date);
		args.putBoolean(FIRST_UPDATE, firstUpdate);
		f.contact = c;
		f.setArguments(args);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		callback = (One2OneChatInsideFragmentCallback) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_profile, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mTextViewProfileName = (TextView) getView().findViewById(R.id.textView_profilename);
		mTextViewDate = (TextView) getView().findViewById(R.id.textView_createdDate);
		mTextViewLocation = (TextView) getView().findViewById(R.id.textView_location);
		mTextViewStatus = (TextView) getView().findViewById(R.id.textView_status);
		mTextViewDatelocation = (TextView) getView().findViewById(
				R.id.textView_creationDateLocation);
		mTextViewTotalMessages = (TextView) getView().findViewById(R.id.textView_totalMessages);
		mTextViewTotalPhoto = (TextView) getView().findViewById(R.id.textView_totalPhoto);
		customDotView = (CustomDotView) getView().findViewById(R.id.textView_profileDot);
		mImgBtnColorWheelSmall = (ImageButton) getView().findViewById(R.id.imgBtn_colorWheelSmall);
		mImgBtnColorWheel = (ImageButton) getView().findViewById(R.id.imgBtn_colorWheelBig);
		mBtnEditStatus = (ImageButton) getView().findViewById(R.id.btnEditStatus);
		mBtnEditName = (ImageButton) getView().findViewById(R.id.btnEdit);
		mBtnSetName = (Button) getView().findViewById(R.id.btnSetName);
		mBtnSetStatus = (Button) getView().findViewById(R.id.btnSetStatus);
		mEditTextStatus = (EditText) getView().findViewById(R.id.editText_status);
		mEdittextName = (EditText) getView().findViewById(R.id.editText_name);
		mRelativeLayoutName = (RelativeLayout) getView().findViewById(R.id.relative_Name);
		mRelativeLayoutSetName = (RelativeLayout) getView().findViewById(R.id.relative_editName);
		mBtnDelete = (ImageButton) getView().findViewById(R.id.btnDelete);

		mBtnDelete.setOnClickListener(this);
		mImgBtnColorWheel.setOnClickListener(this);
		mImgBtnColorWheelSmall.setOnClickListener(this);
		mBtnEditName.setOnClickListener(this);
		mBtnEditStatus.setOnClickListener(this);
		mBtnSetName.setOnClickListener(this);
		mBtnSetStatus.setOnClickListener(this);

		mEditTextStatus.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH
						|| actionId == EditorInfo.IME_ACTION_GO
						|| actionId == EditorInfo.IME_ACTION_DONE
						|| event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

					String status = mEditTextStatus.getText().toString();

					// if (!status.trim().equals("")) {
					mTextViewStatus.setText(status);
					mTextViewStatus.setVisibility(View.VISIBLE);
					mBtnEditStatus.setVisibility(View.VISIBLE);
					mEditTextStatus.setVisibility(View.GONE);
					
					setChatInfo(getArguments().getLong(CHAT_ID), "", null, "", "", status,
							0, "");
					// }
					// mEditTextStatus.setVisibility(View.GONE);
					// mTextViewStatus.setVisibility(View.VISIBLE);
					// mBtnEditStatus.setVisibility(View.VISIBLE);
					//
					// String status = v.getText().toString().trim();
					//
					// mTextViewStatus.setText(status);
					//
					// int updated = contact.updated;
					// getActivity().getContentResolver().update(
					// QodemeContract.Chats.CONTENT_URI,
					// QodemeContract.Chats.updateChatInfoValues("", -1, "", 0,
					// status, "",
					// updated, 4), QodemeContract.Chats.CHAT_ID + "=?",
					// DbUtils.getWhereArgsForId(contact.chatId));
					// // setChatInfo(chatload.chatId, null, null, null, null,
					// // status, null);
					// // getChatload().status = status;
					// // setChatInfo(getChatload().chatId, null,
					// // getChatload().color, getChatload().tag,
					// // getChatload().description, status,
					// // getChatload().is_locked,
					// // getChatload().title);
					return true;
				}

				return false;
			}
		});

		setData();
	}

	public void setData() {
		// if (callback != null) {
		// try {
		// MainActivity activity = (MainActivity) callback;
		// ChatLoad chatLoad = activity.getChatLoad(contact.chatId);
		mTextViewStatus.setText(QodemePreferences.getInstance().getStatus());
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		mTextViewProfileName.setText(getArguments().getString(CHAT_NAME));
		mTextViewProfileName.setTextColor(getArguments().getInt(CHAT_COLOR));
		customDotView.setDotColor(getArguments().getInt(CHAT_COLOR));
		customDotView.invalidate();

		SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yy h:mm a", Locale.US);
		// SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yy HH:mm");
		String dateStr = fmtOut.format(new Date(Converter
				.getCrurentTimeFromTimestamp(getArguments().getString(DATE))));
		mTextViewLocation.setText(contact.location + "");
		mTextViewDatelocation.setText(dateStr + ", " + contact.location != null?contact.location:"");

		SimpleDateFormat fmtOut1 = new SimpleDateFormat("MMMMM dd,yyyy, h:mm a", Locale.US);
		String dateStr1 = fmtOut1.format(new Date(Converter
				.getCrurentTimeFromTimestamp(getArguments().getString(DATE))));
		mTextViewDate.setText(dateStr1);

	}

	private void callColorPicker(int type) {
		MainActivity activity = (MainActivity) getActivity();
		activity.callColorPicker(contact,type);
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
			callColorPicker(0);
			break;
		case R.id.imgBtn_colorWheelBig:
			callColorPicker(1);
			break;
		case R.id.btnEditStatus:
			mTextViewStatus.setVisibility(View.GONE);
			mBtnEditStatus.setVisibility(View.GONE);
			mEditTextStatus.setVisibility(View.VISIBLE);
//			mBtnSetStatus.setVisibility(View.VISIBLE);
			break;
		case R.id.btnEdit:
			mRelativeLayoutName.setVisibility(View.GONE);
			mRelativeLayoutSetName.setVisibility(View.VISIBLE);
			break;
		case R.id.btnSetStatus:
			mTextViewStatus.setVisibility(View.VISIBLE);
			mBtnEditStatus.setVisibility(View.VISIBLE);
			mEditTextStatus.setVisibility(View.GONE);
			mBtnSetStatus.setVisibility(View.GONE);

			String status = mEditTextStatus.getText().toString();

			if (!status.trim().equals("")) {
				mTextViewStatus.setText(status);
				// int updated = contact.updated;
				// getActivity().getContentResolver().update(
				// QodemeContract.Chats.CONTENT_URI,
				// QodemeContract.Chats.updateChatInfoValues("", -1, "", 0,
				// status, "",
				// updated, 4), QodemeContract.Chats.CHAT_ID + "=?",
				// DbUtils.getWhereArgsForId(contact.chatId));
				//
				// QodemePreferences.getInstance().setStatus(status);

				setChatInfo(getArguments().getLong(CHAT_ID), "", contact.color, "", "", status, 0,
						"");
				// String title, int color,
				// String description, int is_locked, String status, String
				// tags, int updated,
				// int updateType) {

				// SyncHelper.requestManualSync();
			}

			break;
		case R.id.btnSetName:
			mRelativeLayoutName.setVisibility(View.VISIBLE);
			mRelativeLayoutSetName.setVisibility(View.GONE);
			String title = mEdittextName.getText().toString();

			if (!title.trim().equals("")) {
				mTextViewProfileName.setText(title);
				int updated = contact.updated;
				getActivity().getContentResolver().update(
						QodemeContract.Contacts.CONTENT_URI,
						QodemeContract.Contacts.updateContactInfoValues(title, contact.color,
								updated), DbUtils.getWhereClauseForId(),
						DbUtils.getWhereArgsForId(contact._id));
				SyncHelper.requestManualSync();
			}
			break;
		case R.id.btnDelete:
			deleteContact();
			break;
		default:
			break;
		}
	}

	public void setChatInfo(long chatId, String title, Integer color, String tag, String desc,
			String status, Integer isLocked, String chat_title) {
		RestAsyncHelper.getInstance().chatSetInfo(chatId, title, color, tag, desc, isLocked,
				status, chat_title, new RestListener<VoidResponse>() {

					@Override
					public void onResponse(VoidResponse response) {
						Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_LONG).show();
					}

					@Override
					public void onServiceError(RestError error) {
						Log.d("Error", error.getMessage() + "");
						Toast.makeText(getActivity(), "Connection error", Toast.LENGTH_LONG).show();
					}
				});
	}

	private void deleteContact() {
		RestAsyncHelper.getInstance().contactRemove(contact.qrCode,
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
