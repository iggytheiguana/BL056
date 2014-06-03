package com.blulabellabs.code.ui.one2one;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.blulabellabs.code.ApplicationConstants;
import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.RestAsyncHelper;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.io.responses.VoidResponse;
import com.blulabellabs.code.core.io.utils.RestError;
import com.blulabellabs.code.core.io.utils.RestListener;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.sync.SyncHelper;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.one2one.ChatInsideFragment.One2OneChatInsideFragmentCallback;
import com.blulabellabs.code.utils.DbUtils;
import com.blulabellabs.code.utils.QrUtils;
import com.google.android.gms.internal.co;
import com.google.common.collect.Lists;

import android.R.mipmap;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class ChatGroupProfileFragment extends Fragment implements OnClickListener {

	private static final String CHAT_ID = "chat_id";
	private static final String CHAT_COLOR = "chat_color";
	private static final String CHAT_NAME = "chat_name";
	private static final String QRCODE = "contact_qr";
	private static final String LOCATION = "location";
	private static final String DATE = "date";
	private static final String FIRST_UPDATE = "first_update";

	private TextView mTextViewDate, mTextViewLocation, mTextViewStatus, mTextViewTotalMessages,
			mTextViewTotalPhoto, mTextViewTotalMember, mTextViewGroupTitle, mTextViewGroupDesc,
			mTextViewTags;
	private ImageButton mImgBtnColorWheel, mImgBtnLocked, mImgBtnSearch, mImgBtnShare;
	private ImageButton mBtnEditStatus, mBtnDelete, mBtnEditDesc;
	private Button mBtnSetStatus, mBtnSetDesc;
	private ImageView mImgQr;
	private EditText mEditTextStatus, mEditTextTitle, mEditTextDesc, mEditTextTags;
	private RelativeLayout mRelativeLayoutDesc, mRelativeLayoutSetDesc;
	private ChatLoad chatload;
	private TextView mTextViewMemberList;
	boolean isChangeByUser = false;

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
		mTextViewTags = (TextView) getView().findViewById(R.id.textView_groupTag);
		mTextViewMemberList= (TextView) getView().findViewById(R.id.textView_memberList);

		mEditTextTags = (EditText) getView().findViewById(R.id.editText_GroupTags);
		mEditTextTitle = (EditText) getView().findViewById(R.id.editText_GroupTitle);
		mEditTextDesc = (EditText) getView().findViewById(R.id.editText_Desc);
		mImgQr = (ImageView) getView().findViewById(R.id.img_qr);

		mRelativeLayoutDesc = (RelativeLayout) getView().findViewById(R.id.relative_desc);
		mRelativeLayoutSetDesc = (RelativeLayout) getView().findViewById(R.id.relative_editDesc);

		mBtnDelete.setOnClickListener(this);
		mImgBtnColorWheel.setOnClickListener(this);
		mBtnEditStatus.setOnClickListener(this);
		mBtnSetStatus.setOnClickListener(this);
		mBtnEditDesc.setOnClickListener(this);
		mBtnSetDesc.setOnClickListener(this);
		mImgBtnShare.setOnClickListener(this);
		mImgBtnLocked.setOnClickListener(this);

		String mQrCodeText = chatload != null ? chatload.qrcode : "";
		mImgQr.setImageBitmap(QrUtils.encodeQrCode((TextUtils.isEmpty(mQrCodeText) ? "Qr Code"
				: ApplicationConstants.QR_CODE_CONTACT_PREFIX + mQrCodeText), 500, 500,
				getResources().getColor(R.color.login_qrcode), Color.TRANSPARENT));
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
					setChatInfo(getChatload().chatId, null, getChatload().color, getChatload().tag,
							getChatload().description, status, getChatload().is_locked,
							getChatload().title);
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
					setChatInfo(getChatload().chatId, null, getChatload().color, getChatload().tag,
							getChatload().description, getChatload().status,
							getChatload().is_locked, getChatload().title);
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
					setChatInfo(getChatload().chatId, null, getChatload().color, getChatload().tag,
							getChatload().description, getChatload().status,
							getChatload().is_locked, getChatload().title);
					return true;
				}

				return false;
			}
		});

		if (getChatload().type == 1) {
			mTextViewTags.setVisibility(View.GONE);
			mEditTextTags.setVisibility(View.GONE);
		} else {
			mTextViewTags.setVisibility(View.VISIBLE);
			mEditTextTags.setVisibility(View.VISIBLE);
			mEditTextTags.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					if (count >= 1)
						if (isChangeByUser)
							if (s.charAt(start) == ',')
								setChips();

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					// TODO Auto-generated method stub

				}

				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub

				}
			});
			mEditTextTags.setOnEditorActionListener(new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_SEARCH
							|| actionId == EditorInfo.IME_ACTION_DONE
							|| event.getAction() == KeyEvent.ACTION_DOWN
							&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
						isChangeByUser = false;
						mRelativeLayoutDesc.setVisibility(View.VISIBLE);
						mRelativeLayoutSetDesc.setVisibility(View.GONE);

						setChips();
						String tags = v.getText().toString().trim();

						mTextViewTags.setText(tags);

						int updated = getChatload().updated;
						getActivity().getContentResolver().update(
								QodemeContract.Chats.CONTENT_URI,
								QodemeContract.Chats.updateChatInfoValues("", -1, "", 0, "", tags,
										updated, 5), QodemeContract.Chats.CHAT_ID + "=?",
								DbUtils.getWhereArgsForId(getChatload().chatId));
						// setChatInfo(chatload.chatId, title, null, null, null,
						// null, null);
						getChatload().tag = tags;
						setChatInfo(getChatload().chatId, null, getChatload().color,
								getChatload().tag, getChatload().description, getChatload().status,
								getChatload().is_locked, getChatload().title);
						return true;
					}

					return false;
				}
			});
		}
		setData();
	}

	public void setChips() {
		if (mEditTextTags.getText().toString().contains(",")) // check comman in
																// string
		{

			// SpannableStringBuilder ssb = new
			// SpannableStringBuilder(mEditTextTags.getText()
			// .toString());
			StringBuilder stringBuilder = new StringBuilder();
			// split string wich comma
			String chips[] = mEditTextTags.getText().toString().trim().split(",");
			int x = 0;
			// loop will generate ImageSpan for every country name separated by
			// comma
			for (String c : chips) {
				c = c.replace("#", "");
				c = "#" + c + ",";
				// inflate chips_edittext layout
				// LayoutInflater lf = (LayoutInflater)
				// getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				// TextView textView = (TextView)
				// lf.inflate(R.layout.edittext_item, null);
				// textView.setText(c); // set text
				// setFlags(textView, c); // set flag image
				// capture bitmapt of genreated textview
				// int spec = MeasureSpec.makeMeasureSpec(0,
				// MeasureSpec.UNSPECIFIED);
				// textView.measure(spec, spec);
				// textView.layout(0, 0, textView.getMeasuredWidth(),
				// textView.getMeasuredHeight());
				// Bitmap b = Bitmap.createBitmap(textView.getWidth(),
				// textView.getHeight(),Bitmap.Config.ARGB_8888);
				// Canvas canvas = new Canvas(b);
				// canvas.translate(-textView.getScrollX(),
				// -textView.getScrollY());
				// textView.draw(canvas);
				// textView.setDrawingCacheEnabled(true);
				// Bitmap cacheBmp = textView.getDrawingCache();
				// Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888,
				// true);
				// textView.destroyDrawingCache(); // destory drawable
				// create bitmap drawable for imagespan
				// BitmapDrawable bmpDrawable = new BitmapDrawable(viewBmp);
				// bmpDrawable.setBounds(0,
				// 0,bmpDrawable.getIntrinsicWidth(),bmpDrawable.getIntrinsicHeight());
				// create and set imagespan
				// ssb.setSpan(new SpannableString(c), x, x + c.length(),
				// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				stringBuilder.append(c);
				x = x + c.length() + 1;
			}
			// set chips span
			// if (isChangeByUser)
			mEditTextTags.setText(stringBuilder);
			// move cursor to last
			mEditTextTags.setSelection(mEditTextTags.getText().toString().length());
		}

	}

	@SuppressWarnings("static-access")
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
				if (getChatload().is_locked == 1) {
					mImgBtnLocked.setImageBitmap(new BitmapFactory().decodeResource(getResources(),
							R.drawable.ic_lock_close));
				} else
					mImgBtnLocked.setImageBitmap(new BitmapFactory().decodeResource(getResources(),
							R.drawable.ic_lock_open));
				mTextViewStatus.setText(getChatload().status);
				mTextViewGroupDesc.setText(getChatload().description);
				mTextViewGroupTitle.setText(getChatload().title);
				mTextViewTags.setText(getChatload().tag);
				mEditTextDesc.setText(getChatload().description);
				mEditTextStatus.setText(getChatload().status);
				mEditTextTitle.setText(getChatload().title);
				mEditTextTags.setText(getChatload().tag);

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
				if(chatload.type == 1){
					((LinearLayout) getView().findViewById(R.id.linear_memberlist)).setVisibility(View.VISIBLE);
					String memberNames = "";
					if (chatload.members != null){
						int i=0;
						for (String memberQr : chatload.members) {
							Contact c = callback.getContact(memberQr);
							if(c != null){
								if(i==0)
									memberNames += c.title+"";
								else
									memberNames += ", "+c.title+"";
							}
							i++;
						}
					}
					mTextViewMemberList.setText(memberNames);
				}else{
					((LinearLayout) getView().findViewById(R.id.linear_memberlist)).setVisibility(View.GONE);
				}
				if (QodemePreferences.getInstance().getQrcode().equals(getChatload().user_qrcode)) {
					if (chatload.type == 2) {
						mImgBtnSearch.setVisibility(View.VISIBLE);
						mImgBtnShare.setVisibility(View.VISIBLE);
					} else {
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
					mImgBtnSearch.setVisibility(View.GONE);
					if (chatload.type == 2) {
						mImgBtnShare.setVisibility(View.VISIBLE);
					} else {
						mImgBtnShare.setVisibility(View.GONE);
					}
				}

			} else {
				mBtnDelete.setVisibility(View.GONE);
				mBtnEditDesc.setVisibility(View.GONE);
				mBtnEditStatus.setVisibility(View.GONE);
				mImgBtnLocked.setVisibility(View.GONE);
				mImgBtnSearch.setVisibility(View.GONE);
				if (chatload.type == 2) {
					mImgBtnShare.setVisibility(View.VISIBLE);
				} else {
					mImgBtnShare.setVisibility(View.GONE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void callColorPicker() {
		MainActivity activity = (MainActivity) getActivity();
		activity.callColorPicker(getChatload(), 1);
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
			// callColorPicker();
			break;
		case R.id.btnEditStatus:
			mTextViewStatus.setVisibility(View.GONE);
			mBtnEditStatus.setVisibility(View.GONE);
			mEditTextStatus.setVisibility(View.VISIBLE);
			// mBtnSetStatus.setVisibility(View.VISIBLE);
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

				setChatInfo(getChatload().chatId, null, getChatload().color, getChatload().tag,
						getChatload().description, status, getChatload().is_locked,
						getChatload().title);
				// SyncHelper.requestManualSync();
			}

			break;
		case R.id.btnEditDesc:
			mRelativeLayoutDesc.setVisibility(View.GONE);
			mRelativeLayoutSetDesc.setVisibility(View.VISIBLE);
			isChangeByUser = true;
			break;
		case R.id.btnSetDesc:
			mRelativeLayoutDesc.setVisibility(View.VISIBLE);
			mRelativeLayoutSetDesc.setVisibility(View.GONE);
			break;
		case R.id.btnDelete:
			deleteContact();
			break;
		case R.id.btnShare:
			MainActivity activity = (MainActivity) getActivity();
			activity.addMemberInExistingChat();
			break;
		case R.id.btnLock:
			if (getChatload().is_locked != 1)
				getChatload().is_locked = 1;
			else
				getChatload().is_locked = 0;

			getActivity().getContentResolver().update(
					QodemeContract.Chats.CONTENT_URI,
					QodemeContract.Chats.updateChatInfoValues("", -1, "", getChatload().is_locked,
							"", "", QodemeContract.Sync.DONE, 3),
					QodemeContract.Chats.CHAT_ID + "=?",
					DbUtils.getWhereArgsForId(getChatload().chatId));

			setChatInfo(getChatload().chatId, null, getChatload().color, getChatload().tag,
					getChatload().description, getChatload().status, getChatload().is_locked,
					getChatload().title);
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
