package biz.softtechnics.qodeme.ui.one2one;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.internal.co;
import com.google.common.collect.Lists;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.io.RestAsyncHelper;
import biz.softtechnics.qodeme.core.io.model.ChatLoad;
import biz.softtechnics.qodeme.core.io.model.Message;
import biz.softtechnics.qodeme.core.io.responses.VoidResponse;
import biz.softtechnics.qodeme.core.io.utils.RestError;
import biz.softtechnics.qodeme.core.io.utils.RestListener;
import biz.softtechnics.qodeme.core.provider.QodemeContract;
import biz.softtechnics.qodeme.core.sync.SyncHelper;
import biz.softtechnics.qodeme.ui.MainActivity;
import biz.softtechnics.qodeme.ui.one2one.ChatInsideFragment.One2OneChatInsideFragmentCallback;
import biz.softtechnics.qodeme.utils.DbUtils;

public class ChatGroupProfileFragment extends Fragment implements OnClickListener {

	private static final String CHAT_ID = "chat_id";
	private static final String CHAT_COLOR = "chat_color";
	private static final String CHAT_NAME = "chat_name";
	private static final String QRCODE = "contact_qr";
	private static final String LOCATION = "location";
	private static final String DATE = "date";
	private static final String FIRST_UPDATE = "first_update";

	private TextView mTextViewDate, mTextViewLocation, mTextViewStatus, mTextViewTotalMessages,
			mTextViewTotalPhoto, mTextViewTotalMember, mTextViewGroupTitle, mTextViewGroupDesc;
	private ImageButton mImgBtnColorWheel, mImgBtnLocked, mImgBtnSearch, mImgBtnShare;
	private ImageButton mBtnEditStatus, mBtnDelete, mBtnEditDesc;
	private Button mBtnSetStatus, mBtnSetDesc;
	private EditText mEditTextStatus, mEditTextTitle, mEditTextDesc;
	private RelativeLayout mRelativeLayoutDesc, mRelativeLayoutSetDesc;
	private ChatLoad chatload;

	private One2OneChatInsideFragmentCallback callback;

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
		f.setChatload(c);
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_profile_group, null);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		callback = (One2OneChatInsideFragmentCallback) activity;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mTextViewDate = (TextView) getView().findViewById(R.id.textView_createdDate);
		mTextViewLocation = (TextView) getView().findViewById(R.id.textView_location);
		mTextViewStatus = (TextView) getView().findViewById(R.id.textView_status);
		mTextViewGroupDesc = (TextView) getView().findViewById(R.id.textView_groupDesc);
		mTextViewGroupTitle = (TextView) getView().findViewById(R.id.textView_groupTitle);
		mTextViewTotalMessages = (TextView) getView().findViewById(R.id.textView_totalMessages);
		mTextViewTotalPhoto = (TextView) getView().findViewById(R.id.textView_totalPhoto);
		mTextViewTotalMember = (TextView) getView().findViewById(R.id.textView_member);
		mImgBtnColorWheel = (ImageButton) getView().findViewById(R.id.imgBtn_colorWheelBig);
		mBtnEditStatus = (ImageButton) getView().findViewById(R.id.btnEditStatus);
		mBtnSetStatus = (Button) getView().findViewById(R.id.btnSetStatus);
		mEditTextStatus = (EditText) getView().findViewById(R.id.editText_status);
		mBtnDelete = (ImageButton) getView().findViewById(R.id.btnDelete);
		mBtnEditDesc = (ImageButton) getView().findViewById(R.id.btnEditDesc);
		mBtnSetDesc = (Button) getView().findViewById(R.id.btnSetDesc);
		mImgBtnLocked = (ImageButton) getView().findViewById(R.id.btnLock);
		mImgBtnSearch = (ImageButton) getView().findViewById(R.id.btnSearch);
		mImgBtnShare = (ImageButton) getView().findViewById(R.id.btnShare);

		mEditTextTitle = (EditText) getView().findViewById(R.id.editText_GroupTitle);
		mEditTextDesc = (EditText) getView().findViewById(R.id.editText_Desc);

		mRelativeLayoutDesc = (RelativeLayout) getView().findViewById(R.id.relative_desc);
		mRelativeLayoutSetDesc = (RelativeLayout) getView().findViewById(R.id.relative_editDesc);

		mBtnDelete.setOnClickListener(this);
		mImgBtnColorWheel.setOnClickListener(this);
		mBtnEditStatus.setOnClickListener(this);
		mBtnSetStatus.setOnClickListener(this);
		mBtnEditDesc.setOnClickListener(this);
		mBtnSetDesc.setOnClickListener(this);

		mEditTextStatus.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH
						|| actionId == EditorInfo.IME_ACTION_GO
						|| actionId == EditorInfo.IME_ACTION_DONE
						|| event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

					mEditTextStatus.setVisibility(View.GONE);
					mTextViewStatus.setVisibility(View.VISIBLE);
					mBtnEditStatus.setVisibility(View.VISIBLE);
					
					String status = v.getText().toString().trim();
					
					mTextViewStatus.setText(status);
					
					int updated = getChatload().updated;
					getActivity().getContentResolver().update(
							QodemeContract.Chats.CONTENT_URI,
							QodemeContract.Chats.updateChatInfoValues("", -1, "", 0, status, "",
									updated, 4), QodemeContract.Chats.CHAT_ID + "=?",
							DbUtils.getWhereArgsForId(getChatload().chatId));
					// setChatInfo(chatload.chatId, null, null, null, null,
					// status, null);
					getChatload().status = status;
					setChatInfo(getChatload().chatId, null, getChatload().color,
							getChatload().tag, getChatload().description, status,
							getChatload().is_locked, getChatload().title);
					return true;
				}

				return false;
			}
		});
		mEditTextTitle.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH
						|| actionId == EditorInfo.IME_ACTION_DONE
						|| event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

					mRelativeLayoutDesc.setVisibility(View.VISIBLE);
					mRelativeLayoutSetDesc.setVisibility(View.GONE);
					String title = v.getText().toString().trim();
					
					mTextViewGroupTitle.setText(title);
					
					int updated = getChatload().updated;
					getActivity().getContentResolver().update(
							QodemeContract.Chats.CONTENT_URI,
							QodemeContract.Chats.updateChatInfoValues(title, -1, "", 0, "", "",
									updated, 0), QodemeContract.Chats.CHAT_ID + "=?",
							DbUtils.getWhereArgsForId(getChatload().chatId));
					// setChatInfo(chatload.chatId, title, null, null, null,
					// null, null);
					getChatload().title = title;
					setChatInfo(getChatload().chatId, null, getChatload().color,
							getChatload().tag, getChatload().description, getChatload().status,
							getChatload().is_locked,getChatload().title);
					return true;
				}

				return false;
			}
		});
		mEditTextDesc.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH
						|| actionId == EditorInfo.IME_ACTION_DONE
						|| event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

					mRelativeLayoutDesc.setVisibility(View.VISIBLE);
					mRelativeLayoutSetDesc.setVisibility(View.GONE);
					
					String desc = v.getText().toString().trim();
					
					mTextViewGroupDesc.setText(desc);
					
					int updated = getChatload().updated;
					getActivity().getContentResolver().update(
							QodemeContract.Chats.CONTENT_URI,
							QodemeContract.Chats.updateChatInfoValues("", -1, desc, 0, "", "",
									updated, 2), QodemeContract.Chats.CHAT_ID + "=?",
							DbUtils.getWhereArgsForId(getChatload().chatId));
					// setChatInfo(chatload.chatId, title, null, null, null,
					// null, null);
					getChatload().description = desc;
					setChatInfo(getChatload().chatId, null, getChatload().color,
							getChatload().tag, getChatload().description, getChatload().status,
							getChatload().is_locked,getChatload().title);
					return true;
				}

				return false;
			}
		});

		if (getChatload().type == 1) {

		} else {

		}
		setData();
	}

	public void setData() {

		try {
			SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yy h:mm a", Locale.US);
			// SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yy HH:mm");
			// String dateStr = fmtOut.format(new Date(Converter
			// .getCrurentTimeFromTimestamp(getArguments().getString(DATE))));
			// mTextViewLocation.setText(chatload.location + "");
			// mTextViewDatelocation.setText(dateStr + ", " +
			// chatload.location);

			SimpleDateFormat fmtOut1 = new SimpleDateFormat("MMMMM dd,yyyy, h:mm a", Locale.US);
			// String dateStr1 = fmtOut1.format(new Date(Converter
			// .getCrurentTimeFromTimestamp(getArguments().getString(DATE))));
			// mTextViewDate.setText(dateStr1);

			if (getChatload() != null) {
				mTextViewStatus.setText(getChatload().status);
				mTextViewGroupDesc.setText(getChatload().description);
				mTextViewGroupTitle.setText(getChatload().title);
				mEditTextDesc.setText(getChatload().description);
				mEditTextStatus.setText(getChatload().status);
				mEditTextTitle.setText(getChatload().title);
				mTextViewTotalMember.setText(getChatload().number_of_members + " members");
				if (callback != null) {
					List<Message> messages = callback.getChatMessages(getChatload().chatId);

					mTextViewTotalMessages.setText((messages != null ? messages.size() : 0)
							+ " messages");
					if (messages != null) {
						List<Message> temp = Lists.newArrayList();

						for (Message message : messages) {
							if (message.hasPhoto == 1)
								temp.add(message);
						}
						mTextViewTotalPhoto.setText(temp.size() + " photos");
					}
				}
				if (QodemePreferences.getInstance().getQrcode().equals(getChatload().user_qrcode)) {
					if(chatload.type == 2){
						mImgBtnSearch.setVisibility(View.VISIBLE);
						mImgBtnShare.setVisibility(View.VISIBLE);
					}else
					{
						mImgBtnSearch.setVisibility(View.GONE);
						mImgBtnShare.setVisibility(View.GONE);
					}
					mBtnDelete.setVisibility(View.VISIBLE);
					mBtnEditDesc.setVisibility(View.VISIBLE);
					mBtnEditStatus.setVisibility(View.VISIBLE);
					mImgBtnLocked.setVisibility(View.VISIBLE);

				} else {
					mBtnDelete.setVisibility(View.GONE);
					mBtnEditDesc.setVisibility(View.GONE);
					mBtnEditStatus.setVisibility(View.GONE);
					mImgBtnLocked.setVisibility(View.GONE);
				}
			} else {
				mBtnDelete.setVisibility(View.GONE);
				mBtnEditDesc.setVisibility(View.GONE);
				mBtnEditStatus.setVisibility(View.GONE);
				mImgBtnLocked.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void callColorPicker() {
		MainActivity activity = (MainActivity) getActivity();
		activity.callColorPicker(getChatload());
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
			//callColorPicker();
			break;
		case R.id.btnEditStatus:
			mTextViewStatus.setVisibility(View.GONE);
			mBtnEditStatus.setVisibility(View.GONE);
			mEditTextStatus.setVisibility(View.VISIBLE);
//			mBtnSetStatus.setVisibility(View.VISIBLE);
			break;

		case R.id.btnSetStatus:
			mTextViewStatus.setVisibility(View.VISIBLE);
			mBtnEditStatus.setVisibility(View.VISIBLE);
			mEditTextStatus.setVisibility(View.GONE);
			mBtnSetStatus.setVisibility(View.GONE);

			String status = mEditTextStatus.getText().toString();

			if (!status.trim().equals("")) {
				mTextViewStatus.setText(status);
				int updated = getChatload().updated;
				getActivity().getContentResolver().update(
						QodemeContract.Chats.CONTENT_URI,
						QodemeContract.Chats.updateChatInfoValues("", -1, "", 0, status, "",
								updated, 4), QodemeContract.Chats.CHAT_ID + "=?",
						DbUtils.getWhereArgsForId(getChatload().chatId));

				getChatload().status = status;
				// String title, int color,
				// String description, int is_locked, String status, String
				// tags, int updated,
				// int updateType) {

				setChatInfo(getChatload().chatId, null, getChatload().color,
						getChatload().tag, getChatload().description, status,
						getChatload().is_locked,getChatload().title);
				// SyncHelper.requestManualSync();
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

	public void setChatInfo(long chatId, String title, Integer color, String tag, String desc,
			String status, Integer isLocked, String chat_title) {
		RestAsyncHelper.getInstance().chatSetInfo(getChatload().chatId, title, color, tag, desc,
				isLocked, status, chat_title, new RestListener<VoidResponse>() {

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
		RestAsyncHelper.getInstance().contactRemove(getChatload().qrcode,
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

	public void setChatload(ChatLoad chatload) {
		this.chatload = chatload;
	}

	public ChatLoad getChatload() {
		return chatload;
	}
}
