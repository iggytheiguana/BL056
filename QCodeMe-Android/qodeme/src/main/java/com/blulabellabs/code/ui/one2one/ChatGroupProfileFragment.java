package com.blulabellabs.code.ui.one2one;

import java.io.IOException;
import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
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

import com.blulabellabs.code.ApplicationConstants;
import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.IntentKey;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.RestAsyncHelper;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.io.responses.ChatAddMemberResponse;
import com.blulabellabs.code.core.io.responses.ChatLoadResponse;
import com.blulabellabs.code.core.io.responses.DeleteChatResponse;
import com.blulabellabs.code.core.io.responses.SetFavoriteResponse;
import com.blulabellabs.code.core.io.responses.SetSearchableResponse;
import com.blulabellabs.code.core.io.responses.VoidResponse;
import com.blulabellabs.code.core.io.utils.RestError;
import com.blulabellabs.code.core.io.utils.RestListener;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.sync.SyncHelper;
import com.blulabellabs.code.ui.FlaggedMessageListActivity;
import com.blulabellabs.code.ui.GroupMemberListActivity;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.one2one.ChatInsideFragment.One2OneChatInsideFragmentCallback;
import com.blulabellabs.code.ui.qr.PublicChatQrCodeShowActivity;
import com.blulabellabs.code.utils.Converter;
import com.blulabellabs.code.utils.DbUtils;
import com.blulabellabs.code.utils.Helper;
import com.blulabellabs.code.utils.LatLonCity;
import com.blulabellabs.code.utils.QrUtils;
import com.google.android.gms.internal.ac;
import com.google.common.collect.Lists;

@SuppressWarnings("unused")
public class ChatGroupProfileFragment extends Fragment implements OnClickListener {

	private static final String CHAT_ID = "chat_id";
	private static final String CHAT_COLOR = "chat_color";
	private static final String CHAT_NAME = "chat_name";
	private static final String QRCODE = "contact_qr";

	private static final String LOCATION = "location";
	private static final String DATE = "date";
	private static final String FIRST_UPDATE = "first_update";
	private static final String MEMBER_LIST = "MemberList";
	private static final String MESSAGE_LIST = "MessageList";

	private TextView mTextViewDate, mTextViewLocation, mTextViewStatus, mTextViewTotalMessages,
			mTextViewTotalPhoto, mTextViewTotalMember, mTextViewGroupTitle, mTextViewGroupDesc,
			mTextViewTags;
	private ImageButton mImgBtnColorWheel, mImgBtnLocked, mImgBtnSearch, mImgBtnShare;
	private ImageButton mBtnEditStatus, mBtnDelete, mBtnEditDesc, mImgFavorite;
	private Button mBtnSetStatus, mBtnSetDesc;
	private ImageView mImgQr;
	private EditText mEditTextStatus, mEditTextTitle, mEditTextDesc, mEditTextTags;
	private RelativeLayout mRelativeLayoutDesc, mRelativeLayoutSetDesc;
	private ChatLoad chatload;
	private TextView mTextViewMemberList, mTextViewPublicThreadLable, mTextViewTotalFlagged,
			mTextViewNumFavorite;
	boolean isChangeByUser = false;
	private LinearLayout mLinearLayFlag;
	private ArrayList<Contact> memberList = new ArrayList<Contact>();
	private ArrayList<Message> totalFlaggedMessagelist = new ArrayList<Message>();
	private Button mBtnAddLocaton, mBtnRemoveLocation;

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
		args.putString(DATE, c.created);
		args.putBoolean(FIRST_UPDATE, firstUpdate);
		f.setChatload(c);
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (getChatload().type == 2) {
			return inflater.inflate(R.layout.fragment_profile_group_public, null);
		} else {
			return inflater.inflate(R.layout.fragment_profile_group, null);
		}
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
		mTextViewMemberList = (TextView) getView().findViewById(R.id.textView_memberList);
		mTextViewPublicThreadLable = (TextView) getView().findViewById(
				R.id.textView_public_lable_bottom);
		mImgFavorite = (ImageButton) getView().findViewById(R.id.btnFavorite);
		mTextViewNumFavorite = (TextView) getView().findViewById(R.id.textView_totalFavorite);

		mEditTextTags = (EditText) getView().findViewById(R.id.editText_GroupTags);
		mEditTextTitle = (EditText) getView().findViewById(R.id.editText_GroupTitle);
		mEditTextDesc = (EditText) getView().findViewById(R.id.editText_Desc);
		mImgQr = (ImageView) getView().findViewById(R.id.img_qr);
		mTextViewTotalFlagged = (TextView) getView().findViewById(R.id.textView_totolFlagged);

		mLinearLayFlag = (LinearLayout) getView().findViewById(R.id.linear_flagged);
		mRelativeLayoutDesc = (RelativeLayout) getView().findViewById(R.id.relative_desc);
		mRelativeLayoutSetDesc = (RelativeLayout) getView().findViewById(R.id.relative_editDesc);
		mBtnAddLocaton = (Button) getView().findViewById(R.id.btn_addLocation);
		mBtnRemoveLocation = (Button) getView().findViewById(R.id.btn_removeLocation);

		mEditTextTags.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		mEditTextTitle.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		mEditTextDesc.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

		mBtnRemoveLocation.setOnClickListener(this);
		mBtnAddLocaton.setOnClickListener(this);
		mBtnDelete.setOnClickListener(this);
		mImgBtnColorWheel.setOnClickListener(this);
		mBtnEditStatus.setOnClickListener(this);
		mBtnSetStatus.setOnClickListener(this);
		mBtnEditDesc.setOnClickListener(this);
		mBtnSetDesc.setOnClickListener(this);
		mImgBtnShare.setOnClickListener(this);
		mImgBtnLocked.setOnClickListener(this);
		mImgFavorite.setOnClickListener(this);
		mImgBtnSearch.setOnClickListener(this);
		mTextViewTotalMember.setOnClickListener(this);
		mTextViewTotalFlagged.setOnClickListener(this);

		String mQrCodeText = chatload != null ? chatload.qrcode : "";
		mImgQr.setImageBitmap(QrUtils.encodeQrCode((TextUtils.isEmpty(mQrCodeText) ? "Qr Code"
				: ApplicationConstants.QR_CODE_CONTACT_PREFIX + mQrCodeText), 500, 500,
				getResources().getColor(R.color.login_qrcode), Color.TRANSPARENT));

		mImgQr.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), PublicChatQrCodeShowActivity.class);
				intent.putExtra(IntentKey.CHAT_TYPE, 2);
				intent.putExtra(IntentKey.QR_CODE, getChatload().qrcode);
				intent.putExtra(IntentKey.CONTACT_NAME, getChatload().title);

				List<Message> messages = callback.getChatMessages(getChatload().chatId);
				int total = 0;
				int photo = 0;
				if (messages != null) {
					total = messages.size();
					for (Message me : messages)
						if (me.hasPhoto == 1)
							photo++;
				} else {
					if (getChatload().isSearchResult) {
						Message[] messages2 = getChatload().messages;
						if (messages2 != null) {
							total = messages2.length;
							for (Message me : messages2)
								if (me.hasPhoto == 1)
									photo++;
						}
					}
				}
				int member = getChatload().number_of_members == 0 ? 1
						: getChatload().number_of_members;

				String text = member + " members, " + total + " messages, " + photo + " photos";
				intent.putExtra("text", text);
				startActivityForResult(intent, MainActivity.REQUEST_ACTIVITY_SCAN_QR_CODE_2);
			}
		});

		mEditTextStatus.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH
						|| actionId == EditorInfo.IME_ACTION_GO
						|| actionId == EditorInfo.IME_ACTION_DONE
						|| event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

					Helper.hideKeyboard(getActivity(), mEditTextStatus);
					mEditTextStatus.setVisibility(View.GONE);
					mTextViewStatus.setVisibility(View.VISIBLE);
					mBtnEditStatus.setVisibility(View.VISIBLE);

					String status = v.getText().toString().trim();

					mTextViewStatus.setText(status);

					callback.sendMessage(getChatload().chatId, status, "", 2, -1, 0, 0,
							QodemePreferences.getInstance().getPublicName(), "");

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

					Helper.hideKeyboard(getActivity(), mEditTextTitle);
					mRelativeLayoutDesc.setVisibility(View.VISIBLE);
					mRelativeLayoutSetDesc.setVisibility(View.GONE);
					String title = v.getText().toString().trim();

					int updated = getChatload().updated;

					try {
						// String tags = findTagFromTitle(title);
						// if (tags.endsWith(",")) {
						// tags = tags.substring(0, tags.length() - 1);
						// }
						//
						// getChatload().tag = getChatload().tag != null ?
						// getChatload().tag + ","
						// + tags : tags;
						// tags = getChatload().tag;
						ArrayList<String> tagsList = findTagFromTitle(title);

						if (getChatload().tag != null && !getChatload().tag.trim().equals("")) {
							String[] chatTag = getChatload().tag.split(",");
							ArrayList<String> duplicateTag = Lists.newArrayList();
							for (String t : tagsList) {
								boolean isAvail = false;
								for (int i = 0; i < chatTag.length; i++) {
									if (chatTag[i].equals(t)) {
										isAvail = true;
										break;
									}
								}
								if (isAvail)
									duplicateTag.add(t);
							}
							tagsList.removeAll(duplicateTag);
							for (int i = 0; i < chatTag.length; i++) {
								tagsList.add(chatTag[i]);
							}
						}
						String tags = "";
						for (String tag : tagsList) {
							tags += tag + ",";
						}

						if (tags.endsWith(",")) {
							tags = tags.substring(0, tags.length() - 1);
						}
						getChatload().tag = tags;

						getActivity().getContentResolver().update(
								QodemeContract.Chats.CONTENT_URI,
								QodemeContract.Chats.updateChatInfoValues(title, -1, "", 0, "",
										tags, updated, 5), QodemeContract.Chats.CHAT_ID + "=?",
								DbUtils.getWhereArgsForId(getChatload().chatId));
					} catch (Exception e) {
					}

					mTextViewGroupTitle.setText(title);

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

					Helper.hideKeyboard(getActivity(), mEditTextDesc);
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
			mImgQr.setVisibility(View.GONE);
		} else {
			mTextViewNumFavorite.setVisibility(View.VISIBLE);
			if (QodemePreferences.getInstance().getQrcode().equals(getChatload().user_qrcode)) {
				mTextViewPublicThreadLable.setVisibility(View.GONE);
				mLinearLayFlag.setVisibility(View.VISIBLE);
			} else {
				mLinearLayFlag.setVisibility(View.GONE);
				mTextViewPublicThreadLable.setVisibility(View.VISIBLE);
			}

//			mTextViewTags.setVisibility(View.VISIBLE);
//			mEditTextTags.setVisibility(View.VISIBLE);
			mEditTextTags.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					if (count >= 1)
						if (isChangeByUser)
							if (s.charAt(start) == ',' || s.charAt(start) == ' ')
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

						Helper.hideKeyboard(getActivity(), mEditTextTags);
						isChangeByUser = false;
						mRelativeLayoutDesc.setVisibility(View.VISIBLE);
						mRelativeLayoutSetDesc.setVisibility(View.GONE);

						if (v.getText().toString().trim().length() > 0) {
							setChips();
							String tags = v.getText().toString().trim();
							if (tags.endsWith(",")) {
								tags = tags.substring(0, tags.length() - 1);
							}
							mTextViewTags.setText(tags);
							try {
								final String tagArray[] = tags.split(",");
								SpannableString ss = new SpannableString(tags);
								int lastPositon = 0;
								for (int i = 0; i < tagArray.length; i++) {
									String text = tagArray[i];
									final int j = i;
									ClickableSpan span1 = new ClickableSpan() {
										@Override
										public void onClick(View textView) {
											String tagText = tagArray[j];
											MainActivity activity = (MainActivity) getActivity();

											activity.searchChats(tagText, 2, 1,
													activity.publicChatListFragment.chatListener);

										}
									};

									ss.setSpan(span1, lastPositon, lastPositon + text.length(),
											Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
									lastPositon = lastPositon + text.length();

								}
								mTextViewTags.setText(ss);
								// textView.setText(ss);
								mTextViewTags.setMovementMethod(LinkMovementMethod.getInstance());
							} catch (Exception e) {
							}

							int updated = getChatload().updated;
							getActivity().getContentResolver().update(
									QodemeContract.Chats.CONTENT_URI,
									QodemeContract.Chats.updateChatInfoValues("", -1, "", 0, "",
											tags, updated, 5), QodemeContract.Chats.CHAT_ID + "=?",
									DbUtils.getWhereArgsForId(getChatload().chatId));
							// setChatInfo(chatload.chatId, title, null, null,
							// null,
							// null, null);
							getChatload().tag = tags;
							setChatInfo(getChatload().chatId, null, getChatload().color,
									getChatload().tag, getChatload().description,
									getChatload().status, getChatload().is_locked,
									getChatload().title);
						}
						return true;
					}

					return false;
				}
			});
		}
		setData();

		if (!isRefress() && !getChatload().isSearchResult)
			loadChat();
	}

	private boolean isRefress() {

		try {
			boolean isRefress = false;
			MainActivity activity = (MainActivity) getActivity();
			for (long id : activity.refressedChatId) {
				if (id == getChatload().chatId) {
					isRefress = true;
					break;
				}
			}
			return isRefress;

		} catch (Exception e) {
		}
		return true;
	}

	private void loadChat() {

		RestAsyncHelper.getInstance().chatLoad(getChatload().chatId, 1, 1,
				new RestListener<ChatLoadResponse>() {

					@Override
					public void onResponse(ChatLoadResponse response) {
						// Log.d("Member",
						// response.getChatLoad().title+" "+response.getChatLoad().number_of_members);
						try {
							ChatLoad chatLoad = response.getChatLoad();
							MainActivity activity = (MainActivity) getActivity();
							activity.refressedChatId.add(chatLoad.chatId);

							String[] members = chatLoad.members;
							String memberQR = "";
							if (members != null && members.length > 0) {
								for (String qr : members) {
									if (!QodemePreferences.getInstance().getQrcode()
											.equals(qr.trim())) {
										if (memberQR.equals(""))
											memberQR = qr;
										else
											memberQR += "," + qr;
									}
								}
							}

							ContentValues contentValues = new ContentValues();
							//
							contentValues.put(QodemeContract.Chats.CHAT_MEMBER_QRCODES, memberQR);
							contentValues.put(QodemeContract.Chats.CHAT_NUMBER_OF_FAVORITE,
									chatLoad.number_of_likes);
							contentValues.put(QodemeContract.Chats.CHAT_NUMBER_OF_MEMBER,
									chatLoad.number_of_members);
							contentValues.put(QodemeContract.Chats.CHAT_IS_FAVORITE,
									chatLoad.is_favorite);

							getActivity().getContentResolver().update(
									QodemeContract.Chats.CONTENT_URI, contentValues,
									QodemeContract.Chats.CHAT_ID + " = " + chatLoad.chatId, null);

							// getActivity().getContentResolver().update(
							// QodemeContract.Chats.CONTENT_URI,
							// QodemeContract.Chats.updateChatInfoValuesAll(chatLoad.title,
							// chatLoad.color, chatLoad.description,
							// chatLoad.is_locked, chatLoad.status,
							// chatLoad.tag,
							// chatLoad.number_of_flagged,
							// chatLoad.number_of_members,
							// chatLoad.latitude, chatLoad.longitude),
							// QodemeContract.Chats.CHAT_ID + " = " +
							// chatLoad.chatId, null);
						} catch (Exception e) {
						}
					}

					@Override
					public void onServiceError(RestError error) {

					}
				});
	}

	private void getLocation() {
		final LatLonCity latLonCity = new LatLonCity();
		if (getChatload().latitude != null && !getChatload().latitude.trim().equals("")
				&& getChatload().longitude != null && !getChatload().longitude.trim().equals("")
				&& !getChatload().latitude.trim().equals("0")
				&& !getChatload().longitude.trim().equals("0")
				&& !getChatload().latitude.trim().equals("-1")
				&& !getChatload().longitude.trim().equals("-1")
				&& !getChatload().latitude.trim().equals("0.0")
				&& !getChatload().longitude.trim().equals("0.0")) {

			if (QodemePreferences.getInstance().getQrcode().equals(getChatload().user_qrcode))
				mBtnRemoveLocation.setVisibility(View.VISIBLE);
			double lat = Double.parseDouble(getChatload().latitude);
			double lng = Double.parseDouble(getChatload().longitude);

			// getGeoCode(lat, lng);

			latLonCity.setLat((int) (lat * 1E6));
			latLonCity.setLon((int) (lng * 1E6));
			new AsyncTask<LatLonCity, Void, String>() {

				@Override
				protected String doInBackground(LatLonCity... params) {
					try {
						List<Address> addresses = new Geocoder(getActivity(), Locale.ENGLISH)
								.getFromLocation(latLonCity.getLat() / 1E6,
										latLonCity.getLon() / 1E6, 1);
						if (!addresses.isEmpty()) {
							String city = addresses.get(0).getAddressLine(1);// .getLocality();
							String country = addresses.get(0).getCountryName();
							String add = null;
							if (city != null && !city.equals("") && !city.equals("null"))
								add = city;

							if (country != null && !country.equals("") && !country.equals("null"))
								if (add != null)
									add += ", " + country;
								else
									add = country;

							return add;
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
					}

					return null;
				}

				@SuppressLint("SimpleDateFormat")
				@Override
				protected void onPostExecute(String s) {
					super.onPostExecute(s);
					if (s != null) {
						latLonCity.setCity(s);
						SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yy HH:mm");
						mTextViewLocation.setText(s + "");
					}
				}
			}.execute(latLonCity);
		} else {
			if (QodemePreferences.getInstance().getQrcode().equals(getChatload().user_qrcode))
				mBtnAddLocaton.setVisibility(View.VISIBLE);
		}
	}

	public void getGeoCode(double lat, double lng) {
		final Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
		// final String zip = "380051";
		try {
			// double latitude = TruckRatingApplication.getLatitude();
			// double longituede = TruckRatingApplication.getLongitude();
			List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
			if (addresses != null && !addresses.isEmpty()) {
				Address address = addresses.get(0);
				// Use the address as needed
				// String message = String.format("Latitude: %f, Longitude: %f",
				// address.getLatitude(), address.getLongitude()) +
				// address.getAddressLine(0);
				// setLatitude(address.getLatitude() + "");
				// setLongitude(address.getLongitude() + "");
				// setCountryName(address.getCountryName());
				// setCityName(address.getLocality());
				mTextViewLocation.setText(address.getLocality() + ", " + address.getCountryName());

			} else {
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ArrayList<String> findTagFromTitle(String c) {

		StringBuilder stringBuilder = new StringBuilder();
		BreakIterator bi = BreakIterator.getWordInstance(Locale.US);
		//
		// Set the text string to be scanned.
		//
		bi.setText(c);
		//
		// Iterates the boundary / breaks
		//
		// System.out.println("Iterates each word: ");
		// int count = 0;
		ArrayList<String> taglist = Lists.newArrayList();
		int lastIndex = bi.first();
		while (lastIndex != BreakIterator.DONE) {
			int firstIndex = lastIndex;
			lastIndex = bi.next();

			if (lastIndex != BreakIterator.DONE && Character.isLetterOrDigit(c.charAt(firstIndex))) {
				String word = c.substring(firstIndex, lastIndex);
				String preWord = "";
				if (firstIndex > 0)
					preWord = c.substring(firstIndex - 1, firstIndex);
				else
					preWord = c.substring(firstIndex, firstIndex + 1);
				// word = "#" + word + ",";
				if (preWord.startsWith("#")) {
					String dd = "#" + word;
					word = "#" + word + ",";
					stringBuilder.append(word);

					taglist.add(dd);
				}
				// System.out.println("'" + word + "' found at (" + firstIndex +
				// ", " + lastIndex
				// + ")");

			}
		}
		// return stringBuilder.toString();
		return taglist;
		// System.out.println("final " + stringBuilder.toString());
	}

	public void setChips() {

		StringBuilder stringBuilder = new StringBuilder();
		// split string wich comma
		String chips[] = mEditTextTags.getText().toString().trim().split(",");
		for (String c : chips) {

			BreakIterator bi = BreakIterator.getWordInstance(Locale.US);
			//
			// Set the text string to be scanned.
			//
			bi.setText(c);
			//
			// Iterates the boundary / breaks
			//
			// System.out.println("Iterates each word: ");
			// int count = 0;
			int lastIndex = bi.first();
			while (lastIndex != BreakIterator.DONE) {
				int firstIndex = lastIndex;
				lastIndex = bi.next();

				if (lastIndex != BreakIterator.DONE
						&& Character.isLetterOrDigit(c.charAt(firstIndex))) {
					String word = c.substring(firstIndex, lastIndex);
					word = "#" + word + ",";
					stringBuilder.append(word);
					// System.out.println("'" + word + "' found at (" +
					// firstIndex + ", " + lastIndex + ")");

				}
			}

			// c = c.replace("#", "");
			// c = "#" + c + ",";
			// stringBuilder.append(c);
			// x = x + c.length() + 1;
			// i++;
		}
		// set chips span
		// if (isChangeByUser)
		mEditTextTags.setText(stringBuilder);
		// move cursor to last
		mEditTextTags.setSelection(mEditTextTags.getText().toString().length());
		// }

	}

	@SuppressLint("SimpleDateFormat")
	@SuppressWarnings("static-access")
	public void setData() {

		try {
			if (getChatload().is_favorite == 1) {
				Bitmap bm = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_profile_favorite);
				mImgFavorite.setImageBitmap(bm);
			} else {
				Bitmap bm = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_profile_favorite_h);
				mImgFavorite.setImageBitmap(bm);
			}
			mTextViewNumFavorite.setText(getChatload().number_of_likes + "");
			try {
				// SimpleDateFormat fmtOut = new
				// SimpleDateFormat("MM/dd/yy h:mm a", Locale.US);
				// SimpleDateFormat fmtOut = new
				// SimpleDateFormat("MM/dd/yy HH:mm");
				// String dateStr = fmtOut.format(new Date(Converter
				// .getCrurentTimeFromTimestamp(getArguments().getString(DATE))));
				// mTextViewLocation.setText(chatload.location + "");
				// mTextViewDatelocation.setText(dateStr + ", " +
				// chatload.location);

				SimpleDateFormat fmtOut1 = new SimpleDateFormat("MMMM dd,yyyy, HH:mm");
				String dateStr1 = fmtOut1.format(new Date(Converter
						.getCrurentTimeFromTimestamp(getArguments().getString(DATE))));
				mTextViewDate.setText(dateStr1);

				getLocation();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (getChatload() != null) {

				if (getChatload().is_locked == 1) {
					mImgBtnLocked.setImageBitmap(new BitmapFactory().decodeResource(getResources(),
							R.drawable.ic_lock_close));
				} else
					mImgBtnLocked.setImageBitmap(new BitmapFactory().decodeResource(getResources(),
							R.drawable.ic_lock_open));
				mTextViewStatus.setText(getChatload().status != null
						&& !getChatload().status.trim().equals("null") ? getChatload().status : "");
				mTextViewGroupDesc
						.setText(getChatload().description != null
								&& !getChatload().description.trim().equals("null") ? getChatload().description
								: "");
				
				// for tag clickable in title start
				final String title = getChatload().title != null
				&& !getChatload().title.equals("null") ? getChatload().title : "";
				SpannableString ss = new SpannableString(title);
				int lastPositon = 0;
//				for (int i = 0; i < tagArray.length; i++) {
//					String text = tagArray[i];
//					final int j = i;
				
//				StringBuilder stringBuilder = new StringBuilder();
				BreakIterator bi = BreakIterator.getWordInstance(Locale.US);
				//
				// Set the text string to be scanned.
				//
				bi.setText(title);
				//
				// Iterates the boundary / breaks
				//
				// System.out.println("Iterates each word: ");
				// int count = 0;
//				ArrayList<String> taglist = Lists.newArrayList();
				int lastIndex = bi.first();
				while (lastIndex != BreakIterator.DONE) {
					int firstIndex = lastIndex;
					lastIndex = bi.next();

					if (lastIndex != BreakIterator.DONE && Character.isLetterOrDigit(title.charAt(firstIndex))) {
						String word = title.substring(firstIndex, lastIndex);
						String preWord = "";
						if (firstIndex > 0)
							preWord = title.substring(firstIndex - 1, firstIndex);
						else
							preWord = title.substring(firstIndex, firstIndex + 1);
						// word = "#" + word + ",";
						final String searchItem = word; 
						if (preWord.startsWith("#")) {
							String dd = "#" + word;
							word = "#" + word + ",";
							
//							stringBuilder.append(word);
							
							ClickableSpan span1 = new ClickableSpan() {
								@Override
								public void onClick(View textView) {
//									String tagText = tagArray[j];
									MainActivity activity = (MainActivity) getActivity();
									activity.setPublicSearch(true);
									activity.setPublicSearchString(searchItem);
									activity.searchChats(searchItem, 2, 1,
											activity.publicChatListFragment.chatListener);
									activity.onBackPressed();
									

								}
							};
							
							ss.setSpan(span1, firstIndex-1, lastIndex,
									Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

						}
						// System.out.println("'" + word + "' found at (" + firstIndex +
						// ", " + lastIndex
						// + ")");

					}
					
				}
				mTextViewGroupTitle.setText(ss);
				mTextViewGroupTitle.setMovementMethod(LinkMovementMethod.getInstance());
				
//					ClickableSpan span1 = new ClickableSpan() {
//						@Override
//						public void onClick(View textView) {
////							String tagText = tagArray[j];
//							MainActivity activity = (MainActivity) getActivity();
//
//							activity.searchChats(title, 2, 1,
//									activity.publicChatListFragment.chatListener);
//
//						}
//					};
					// end
//				mTextViewGroupTitle.setText(getChatload().title != null
//						&& !getChatload().title.equals("null") ? getChatload().title : "");
				mTextViewTags.setText(getChatload().tag != null
						&& !getChatload().tag.trim().equals("null") ? getChatload().tag : "");
				mEditTextDesc.setText(getChatload().description);
				mEditTextStatus.setText(getChatload().status);
				mEditTextTitle.setText(getChatload().title);
				mEditTextTags.setText(getChatload().tag);

				mTextViewTotalMember.setText((getChatload().number_of_members == 0 ? 1
						: getChatload().number_of_members) + " members");

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
				if (chatload.type == 1) {
					((LinearLayout) getView().findViewById(R.id.linear_memberlist))
							.setVisibility(View.VISIBLE);
					((View) getView().findViewById(R.id.view_memberListBottomLine))
							.setVisibility(View.VISIBLE);
					String memberNames = "";
					if (chatload.members != null) {
						int i = 0;
						ArrayList<String> nameList = new ArrayList<String>();
						memberList.clear();
						for (String memberQr : chatload.members) {
							if (!QodemePreferences.getInstance().getQrcode().equals(memberQr)) {
								Contact c = callback.getContact(memberQr);
								if (c != null) {
									nameList.add(c.title);
									memberList.add(c);
									// if (i == 0)
									// memberNames += c.title + "";
									// else
									// memberNames += ", " + c.title + "";
								} else {
									nameList.add("User");
								}
							}
							// i++;
						}
						Collections.sort(nameList);
						for (String memberQr : nameList) {
							// Contact c = callback.getContact(memberQr);
							// if (c != null) {
							if (i > 5) {
								memberNames += "...";
								break;
							}
							if (i == 0)
								memberNames += memberQr + "";
							else
								memberNames += ", " + memberQr + "";
							// }
							i++;
						}
					}
					mTextViewMemberList.setText(memberNames);
				} else {
					((LinearLayout) getView().findViewById(R.id.linear_memberlist))
							.setVisibility(View.GONE);

					if (chatload.members != null) {
						memberList.clear();
						for (String memberQr : chatload.members) {
							if (!QodemePreferences.getInstance().getQrcode().equals(memberQr)) {
								Contact c = callback.getContact(memberQr);
								if (c != null)
									memberList.add(c);
							}
							// i++;
						}
					}
				}
				if (QodemePreferences.getInstance().getQrcode().equals(getChatload().user_qrcode)) {

					if (chatload.type == 2) {
						mImgBtnSearch.setVisibility(View.VISIBLE);
						mImgBtnShare.setVisibility(View.VISIBLE);

						if (getChatload().is_searchable == 1) {
							Bitmap bm = BitmapFactory.decodeResource(getResources(),
									R.drawable.ic_search_blue);
							mImgBtnSearch.setImageBitmap(bm);
						} else {
							Bitmap bm = BitmapFactory.decodeResource(getResources(),
									R.drawable.ic_search_gray);
							mImgBtnSearch.setImageBitmap(bm);
						}

						List<Message> messages = callback.getChatMessages(getChatload().chatId);
						int totalFlagged = 0;
						totalFlaggedMessagelist.clear();
						if (messages != null) {

							for (Message msg : messages) {
								if (msg.is_flagged == 1) {
									totalFlagged++;
									totalFlaggedMessagelist.add(msg);
								}
							}
						}
//						if (totalFlagged <= 1)
//							mTextViewTotalFlagged.setText(totalFlagged + " Message");
//						else
//							mTextViewTotalFlagged.setText(totalFlagged + " Messages");
					} else {
						mImgBtnSearch.setVisibility(View.GONE);
						mImgBtnShare.setVisibility(View.GONE);
					}
					mBtnDelete.setVisibility(View.VISIBLE);
					mBtnEditDesc.setVisibility(View.VISIBLE);
					mBtnEditStatus.setVisibility(View.VISIBLE);
					mImgBtnLocked.setVisibility(View.VISIBLE);
					mImgBtnColorWheel.setVisibility(View.VISIBLE);

					mTextViewGroupDesc.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							mRelativeLayoutDesc.setVisibility(View.GONE);
							mRelativeLayoutSetDesc.setVisibility(View.VISIBLE);
							isChangeByUser = true;
							try {
								Helper.showKeyboard(getActivity(), mEditTextDesc);
							} catch (Exception e) {
							}
						}
					});
					mTextViewTags.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							mRelativeLayoutDesc.setVisibility(View.GONE);
							mRelativeLayoutSetDesc.setVisibility(View.VISIBLE);
							isChangeByUser = true;
							try {
								Helper.showKeyboard(getActivity(), mEditTextTags);
							} catch (Exception e) {
							}

						}
					});
					mTextViewGroupTitle.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
//							mRelativeLayoutDesc.setVisibility(View.GONE);
//							mRelativeLayoutSetDesc.setVisibility(View.VISIBLE);
							mTextViewGroupTitle.setVisibility(View.GONE);
							mEditTextTitle.setVisibility(View.VISIBLE);
							isChangeByUser = true;
							try {
								Helper.showKeyboard(getActivity(), mEditTextTitle);
							} catch (Exception e) {
							}
						}
					});

				} else {
					mTextViewStatus.setHint(R.string.no_status_hint);
					mTextViewGroupDesc.setHint(R.string.no_description);
					mTextViewGroupTitle.setHint(R.string.no_title);
					mTextViewTags.setHint(R.string.no_hashtags);
					mEditTextDesc.setHint(R.string.no_description);
					mEditTextStatus.setHint(R.string.no_status_hint);
					mEditTextTitle.setHint(R.string.no_title);
					mEditTextTags.setHint(R.string.no_hashtags);

					mBtnDelete.setVisibility(View.GONE);
					mBtnEditDesc.setVisibility(View.GONE);
					mBtnEditStatus.setVisibility(View.GONE);
					mImgBtnLocked.setVisibility(View.GONE);
					mImgBtnSearch.setVisibility(View.GONE);
					mImgBtnColorWheel.setVisibility(View.GONE);
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

		if (getChatload().is_deleted == 1) {
			mBtnDelete.setVisibility(View.INVISIBLE);
			mBtnEditDesc.setVisibility(View.INVISIBLE);
			mBtnEditStatus.setVisibility(View.INVISIBLE);
			mImgBtnLocked.setVisibility(View.INVISIBLE);
			mImgBtnSearch.setVisibility(View.INVISIBLE);
			mImgBtnShare.setVisibility(View.INVISIBLE);
			mImgBtnColorWheel.setVisibility(View.INVISIBLE);
			mImgFavorite.setClickable(false);
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
		if (getChatload().is_deleted != 1) {
			switch (v.getId()) {
			case R.id.imgBtn_colorWheelSmall:
			case R.id.imgBtn_colorWheelBig:
				callColorPicker();
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
				QodemePreferences.getInstance().setNewPublicGroupChatId(-1l);
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
						QodemeContract.Chats.updateChatInfoValues("", -1, "",
								getChatload().is_locked, "", "", QodemeContract.Sync.DONE, 3),
						QodemeContract.Chats.CHAT_ID + "=?",
						DbUtils.getWhereArgsForId(getChatload().chatId));

				setChatInfo(getChatload().chatId, null, getChatload().color, getChatload().tag,
						getChatload().description, getChatload().status, getChatload().is_locked,
						getChatload().title);
				break;
			case R.id.btnFavorite:
				int is_favorite = 1;
				int num_of_favorite = getChatload().number_of_likes;
				if (getChatload().is_favorite == 1) {
					is_favorite = 2;
					num_of_favorite--;
				} else {
					is_favorite = 1;
					if (num_of_favorite <= 0) {
						num_of_favorite = 1;
					} else
						num_of_favorite++;
				}
				if (getChatload().isSearchResult) {
					getChatload().number_of_likes = num_of_favorite;
					getChatload().is_favorite = is_favorite;
					if (is_favorite == 1) {
						Bitmap bm = BitmapFactory.decodeResource(getResources(),
								R.drawable.ic_chat_favorite);
						mImgFavorite.setImageBitmap(bm);
					} else {
						Bitmap bm = BitmapFactory.decodeResource(getResources(),
								R.drawable.ic_chat_favorite_h);
						mImgFavorite.setImageBitmap(bm);
					}

					String date = Converter.getCurrentGtmTimestampString();
					RestAsyncHelper.getInstance().setFavorite(date, is_favorite,
							getChatload().chatId, new RestListener<SetFavoriteResponse>() {

								@Override
								public void onResponse(SetFavoriteResponse response) {

								}

								@Override
								public void onServiceError(RestError error) {

								}
							});

					RestAsyncHelper.getInstance().chatAddMember(getChatload().chatId,
							QodemePreferences.getInstance().getQrcode(),
							new RestListener<ChatAddMemberResponse>() {

								@Override
								public void onResponse(ChatAddMemberResponse response) {
									Log.d("Chat add in public ", "Chat add mem "
											+ response.getChat().getId());
								}

								@Override
								public void onServiceError(RestError error) {
									Log.d("Error", "Chat add member");
								}
							});
				} else {
					getActivity().getContentResolver().update(QodemeContract.Chats.CONTENT_URI,
							QodemeContract.Chats.updateFavorite(is_favorite, num_of_favorite),
							QodemeContract.Chats.CHAT_ID + " = " + getChatload().chatId, null);
					SyncHelper.requestManualSync();
				}
				// setFavorite();
				break;
			case R.id.textView_member:
				if (QodemePreferences.getInstance().getQrcode().equals(getChatload().user_qrcode)) {
					Intent intent = new Intent(getActivity(), GroupMemberListActivity.class);
					intent.putExtra(CHAT_ID, getArguments().getLong(CHAT_ID));
					intent.putParcelableArrayListExtra(MEMBER_LIST, memberList);
					getActivity().startActivity(intent);
				}

				break;
			case R.id.btnSearch:
				if (getChatload().is_searchable == 1)
					setSearchable(0);
				else
					setSearchable(1);
				break;
			case R.id.textView_totolFlagged:
				Intent intent = new Intent(getActivity(), FlaggedMessageListActivity.class);
				intent.putExtra(CHAT_ID, getArguments().getLong(CHAT_ID));
				intent.putParcelableArrayListExtra(MEMBER_LIST, memberList);
				intent.putParcelableArrayListExtra(MESSAGE_LIST, totalFlaggedMessagelist);

				getActivity().startActivity(intent);
				break;
			case R.id.btn_addLocation:
				MainActivity activity2 = (MainActivity) getActivity();
				Location location = activity2.getCurrentLocation();
				if (location != null) {
					getActivity().getContentResolver().update(
							QodemeContract.Chats.CONTENT_URI,
							QodemeContract.Chats.updateLocation(location.getLatitude() + "",
									location.getLongitude() + ""),
							QodemeContract.Chats.CHAT_ID + "=?",
							DbUtils.getWhereArgsForId(getChatload().chatId));

					getChatload().latitude = location.getLatitude() + "";
					getChatload().longitude = location.getLongitude() + "";

					setChatInfo(getChatload().chatId, null, getChatload().color, getChatload().tag,
							getChatload().description, getChatload().status,
							getChatload().is_locked, getChatload().title);
				} else {
					Toast.makeText(getActivity(),
							"Location not found, please check your GPS settings. ",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.btn_removeLocation:
				getActivity().getContentResolver().update(QodemeContract.Chats.CONTENT_URI,
						QodemeContract.Chats.updateLocation("0", "0"),
						QodemeContract.Chats.CHAT_ID + "=?",
						DbUtils.getWhereArgsForId(getChatload().chatId));

				getChatload().latitude = "0";
				getChatload().longitude = "0";

				setChatInfo(getChatload().chatId, null, getChatload().color, getChatload().tag,
						getChatload().description, getChatload().status, getChatload().is_locked,
						getChatload().title);
				break;
			default:
				break;
			}
		}
	}

	private void setSearchable(final int is_searchable) {
		RestAsyncHelper.getInstance().setSearchable(is_searchable, getChatload().chatId,
				new RestListener<SetSearchableResponse>() {

					@Override
					public void onResponse(SetSearchableResponse response) {
						// Log.d("searchabel", "successfull");
						try {
							getActivity().getContentResolver().update(
									QodemeContract.Chats.CONTENT_URI,
									QodemeContract.Chats.updateSearchabel(is_searchable),
									QodemeContract.Chats.CHAT_ID + " = " + getChatload().chatId,
									null);
						} catch (Exception e) {
						}
					}

					@Override
					public void onServiceError(RestError error) {
						// Log.d("searchabel", "Error");
						try {
							Toast toast = new Toast(getActivity());
							toast.setGravity(Gravity.TOP, 0, 50);
							toast.setText("Nerwork Problem, Check your Internet connection.");
							toast.setDuration(Toast.LENGTH_SHORT);
							toast.show();
						} catch (Exception e) {
						}
					}
				});
	}

	private void setFavorite() {
		String date = Converter.getCurrentGtmTimestampString();
		RestAsyncHelper.getInstance().setFavorite(date, 1, getChatload().chatId,
				new RestListener<SetFavoriteResponse>() {

					@Override
					public void onResponse(SetFavoriteResponse response) {
						Log.d("favorite", "successfull");
					}

					@Override
					public void onServiceError(RestError error) {
						Log.d("favorite", "Error" + error.getMessage());
					}
				});

	}

	public void setChatInfo(long chatId, String title, Integer color, String tag, String desc,
			String status, Integer isLocked, String chat_title) {
		RestAsyncHelper.getInstance().chatSetInfo(getChatload().chatId, title, color, tag, desc,
				isLocked, status, chat_title, getChatload().latitude, getChatload().longitude,
				new RestListener<VoidResponse>() {

					@Override
					public void onResponse(VoidResponse response) {
						// Toast.makeText(getActivity(), "Profile updated",
						// Toast.LENGTH_LONG).show();
					}

					@Override
					public void onServiceError(RestError error) {
						Log.d("Error", error.getMessage() + "");
						// Toast.makeText(getActivity(), "Connection error",
						// Toast.LENGTH_LONG).show();
					}
				});
	}

	private void deleteContact() {
		getActivity().getContentResolver().delete(QodemeContract.Chats.CONTENT_URI,
				QodemeContract.Chats.CHAT_ID + "=" + String.valueOf(getChatload().chatId), null);
		RestAsyncHelper.getInstance().deleteChat(getChatload().chatId,
				new RestListener<DeleteChatResponse>() {

					@Override
					public void onResponse(DeleteChatResponse response) {
						Log.d("Response", "successfull remove chat");
					}

					@Override
					public void onServiceError(RestError error) {
						Log.d("Error", "Error in remove chat");

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
