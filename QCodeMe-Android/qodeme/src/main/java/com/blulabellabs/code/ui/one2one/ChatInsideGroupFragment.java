package com.blulabellabs.code.ui.one2one;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.blulabellabs.code.ApplicationConstants;
import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.RestAsyncHelper;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.io.responses.ChatAddMemberResponse;
import com.blulabellabs.code.core.io.responses.SetFavoriteResponse;
import com.blulabellabs.code.core.io.responses.VoidResponse;
import com.blulabellabs.code.core.io.utils.RestError;
import com.blulabellabs.code.core.io.utils.RestListener;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.sync.SyncHelper;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.common.CustomDotView;
import com.blulabellabs.code.ui.common.ExtendedGroupListAdapter;
import com.blulabellabs.code.ui.common.ScrollDisabledListView;
import com.blulabellabs.code.ui.one2one.ChatInsideFragment.One2OneChatInsideFragmentCallback;
import com.blulabellabs.code.utils.ChatFocusSaver;
import com.blulabellabs.code.utils.Converter;
import com.blulabellabs.code.utils.DbUtils;
import com.blulabellabs.code.utils.Fonts;
import com.blulabellabs.code.utils.Helper;
import com.google.common.collect.Lists;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

/**
 * Created by Alex on 10/7/13.
 */
@SuppressLint("SimpleDateFormat")
public class ChatInsideGroupFragment extends Fragment {

	private static final String CHAT_ID = "chat_id";
	private static final String CHAT_COLOR = "chat_color";
	// private static final String CHAT_NAME = "chat_name";
	private static final String QRCODE = "contact_qr";
	private static final String LOCATION = "location";
	private static final String STATUS = "status";
	private static final String DESCRIPTION = "description";

	// private static final String DATE = "date";
	private static final String FIRST_UPDATE = "first_update";

	private final WebSocketConnection mConnection = new WebSocketConnection();
	private static final String TAG = "ChatInsideFragment";

	private One2OneChatInsideFragmentCallback callback;
	private boolean isViewCreated;
	private ScrollDisabledListView mListView;
	private ExtendedGroupListAdapter<ChatListGroupSubItem, Message, ChatListSubAdapterCallback> mListAdapter;
	private GestureDetector mGestureDetector;
	private ImageButton mSendButton, mBtnImageSend, mBtnImageSendBottom, mImgFavorite;
	private EditText mMessageField, mStatusField;
	private TextView mName, mStatus, mStatusUpdate;
	private TextView mDate;
	private TextView mLocation;
	private LinearLayout mLinearLayStatusUpdte;
	private TextView mTextViewMembers, mTextViewMembersLabel, mTextViewNumFavorite,
			mTextViewDeleteBaner;
	private ImageView mImgMemberBottomLine;

	private boolean mFirstUpdate = true;
	private ImageView imgUserTyping;
	private CustomDotView customDotViewUserTyping;
	private View footerView,footerView1;
	private boolean isUsertyping = false;
	private ChatLoad chatLoad;

	public static ChatInsideGroupFragment newInstance(ChatLoad c, boolean firstUpdate) {
		ChatInsideGroupFragment f = new ChatInsideGroupFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putLong(CHAT_ID, c.chatId);
		args.putInt(CHAT_COLOR, c.color);
		// args.putString(CHAT_NAME, c.title);
		args.putString(QRCODE, c.qrcode);
		args.putString(STATUS, c.status);
		args.putString(DESCRIPTION, c.description);
		// args.putString(LOCATION, c.location);
		// args.putString(DATE, c.date);
		args.putBoolean(FIRST_UPDATE, firstUpdate);
		f.setArguments(args);
		f.setChatLoad(c);
		return f;
	}

	// public interface One2OneChatInsideFragmentCallback {
	// List<Message> getChatMessages(long chatId);
	//
	// void sendMessage(long chatId, String message, String photoUrl, int
	// hashPhoto,
	// long replyTo_Id, double latitude, double longitude, String senderName);
	//
	// Typeface getFont(Fonts font);
	// }

	/**
	 * callback for reply messages
	 * 
	 * @author
	 * 
	 */
	public interface One2OneChatListInsideFragmentCallback {
		void startTypingMessage();

		void stopTypingMessage();

		void sendReplyMessage(long messageReplyId, String message, String photoUrl, int hashPhoto,
				long replyTo_Id, double latitude, double longitude, String senderName);

		ImageFetcher getImageFetcher();

		int getChatType(long chatId);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		callback = (One2OneChatInsideFragmentCallback) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_one2one_chat, null);
	}

	@Override
	public void onPause() {
		super.onPause();

		stop();
	}

	@Override
	public void onResume() {
		super.onResume();
		start();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mGestureDetector = new GestureDetector(getActivity(), new GestureListener());

		initListView();
		isViewCreated = true;

		initSendMessage();
		mName = (TextView) getView().findViewById(R.id.name);
		mStatus = (TextView) getView().findViewById(R.id.textView_status);
		mStatusUpdate = (TextView) getView().findViewById(R.id.textView_status_update);
		mLinearLayStatusUpdte = (LinearLayout) getView().findViewById(R.id.linear_status_update);
		mStatusField = (EditText) getView().findViewById(R.id.edit_status);

		mTextViewMembers = (TextView) getView().findViewById(R.id.textView_member1);
		mImgMemberBottomLine = (ImageView) getView().findViewById(R.id.img_memberline);
		mTextViewMembersLabel = (TextView) getView().findViewById(R.id.textView_member);

		mImgFavorite = (ImageButton) getView().findViewById(R.id.btnFavorite);
		mTextViewNumFavorite = (TextView) getView().findViewById(R.id.textView_totalFavorite);
		mTextViewDeleteBaner = (TextView) getView().findViewById(R.id.textView_deleteBanner);

		mImgFavorite.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int is_favorite = 1;
				// MaiNACTIVITY ACTIVITY = (MAINACTIVITY) GETACTIVITY();
				// CHATLoad chatLoad =
				// activity.getChatLoad(getArguments().getLong(CHAT_ID));
				if (chatLoad != null) {
					int num_of_favorite = chatLoad.number_of_likes;
					if (chatLoad.is_favorite == 1) {
						is_favorite = 2;
						num_of_favorite--;
					} else {
						is_favorite = 1;
						if (num_of_favorite <= 0) {
							num_of_favorite = 1;
						} else
							num_of_favorite++;
					}
					if (chatLoad.isSearchResult) {
						chatLoad.number_of_likes = num_of_favorite;
						chatLoad.is_favorite = is_favorite;
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
								chatLoad.chatId, new RestListener<SetFavoriteResponse>() {

									@Override
									public void onResponse(SetFavoriteResponse response) {

									}

									@Override
									public void onServiceError(RestError error) {

									}
								});
						
						RestAsyncHelper.getInstance().chatAddMember(chatLoad.chatId,
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
								QodemeContract.Chats.CHAT_ID + " = " + chatLoad.chatId, null);
						SyncHelper.requestManualSync();
					}
				}
			}
		});

		updateUi();

		if (QodemePreferences.getInstance().getQrcode().equals(chatLoad.user_qrcode)) {
			mStatus.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mStatusField.setVisibility(View.VISIBLE);
					mStatusField.setText(mStatus.getText());
					v.setVisibility(View.GONE);
				}
			});
			mStatusField.setOnEditorActionListener(new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_SEARCH
							|| actionId == EditorInfo.IME_ACTION_DONE
							|| event.getAction() == KeyEvent.ACTION_DOWN
							&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

						mStatusField.setVisibility(View.GONE);
						mStatus.setVisibility(View.VISIBLE);

						String status = v.getText().toString().trim();

						mStatus.setText(status);

						callback.sendMessage(chatLoad.chatId, status, "", 2, -1, 0, 0,
								QodemePreferences.getInstance().getPublicName(), "");

						int updated = chatLoad.updated;
						getActivity().getContentResolver().update(
								QodemeContract.Chats.CONTENT_URI,
								QodemeContract.Chats.updateChatInfoValues("", -1, "", 0, status,
										"", updated, 4), QodemeContract.Chats.CHAT_ID + "=?",
								DbUtils.getWhereArgsForId(chatLoad.chatId));
						// setChatInfo(chatload.chatId, null, null, null, null,
						// status, null);
						chatLoad.status = status;
						setChatInfo(chatLoad.chatId, null, chatLoad.color, chatLoad.tag,
								chatLoad.description, status, chatLoad.is_locked, chatLoad.title,
								chatLoad.latitude, chatLoad.longitude);

						return true;
					}
					return false;
				}
			});

		}

	}

	public void setChatInfo(long chatId, String title, Integer color, String tag, String desc,
			String status, Integer isLocked, String chat_title, String latitude, String longitude) {
		RestAsyncHelper.getInstance().chatSetInfo(chatId, title, color, tag, desc, isLocked,
				status, chat_title, latitude, longitude, new RestListener<VoidResponse>() {

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

	private void initSendMessage() {
		mBtnImageSend = (ImageButton) getView().findViewById(R.id.btn_camera);
		mSendButton = (ImageButton) getView().findViewById(R.id.button_message);
		mBtnImageSendBottom = (ImageButton) getView().findViewById(R.id.imageButton_imgMessage);
		mMessageField = (EditText) getView().findViewById(R.id.edit_message);
		
		
		mMessageField.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		
		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				footerView1.setVisibility(View.GONE);
				sendMessage();
			}
		});

		mBtnImageSend.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				MainActivity activity = (MainActivity) getActivity();
				activity.takePhoto();
			}
		});
		mBtnImageSendBottom.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				MainActivity activity = (MainActivity) getActivity();
				activity.takePhoto();
			}
		});
		/*
		 * mMessageField.setOnKeyListener(new View.OnKeyListener() {
		 * 
		 * @Override public boolean onKey(View v, int keyCode, KeyEvent event) {
		 * if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode ==
		 * event.KEYCODE_ENTER) { if (keyCode == event.KEYCODE_ENTER) {
		 * sendMessage(); return true; } } return false; } });
		 */

		mMessageField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean b) {
				if (!b) {
					// edit view doesn't have focus anymore
					Log.d("CHATINSIDE", "user stopped typing");
					sendUserStoppedTypingMessage();
				}
			}
		});

		mMessageField.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				Log.d("CHATINSIDE", "beforeTextChanged called");

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Log.d("CHATINSIDE", "onTextChanged called");
				if (s.length() > 0) {
					mSendButton.setVisibility(View.VISIBLE);
					sendUserTypingMessage();// send user typing message
					footerView1.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				Log.d("CHATINSIDE", "afterTextChanged called");
				if (s.length() > 0) {
					mSendButton.setVisibility(View.VISIBLE);
					footerView1.setVisibility(View.VISIBLE);
					sendUserTypingMessage();
				} else {
					mSendButton.setVisibility(View.GONE);
					sendUserStoppedTypingMessage();
					footerView1.setVisibility(View.GONE);
				}
			}
		});

	}

	Handler handlerForUserTyping = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (isUsertyping) {
				if (customDotViewUserTyping.getVisibility() == View.INVISIBLE
						|| customDotViewUserTyping.getVisibility() == View.GONE)
					customDotViewUserTyping.setVisibility(View.VISIBLE);
				else
					customDotViewUserTyping.setVisibility(View.INVISIBLE);

				handlerForUserTyping.sendEmptyMessageDelayed(0, 500);
			}
		};
	};

	private void sendRegisterForChatEvents() {
		// this method sends a message over the websocket registering for
		// notifications that
		// are related to the current chat id
		String activityName = "sendRegisterForChatEvents:";

		if (mConnection.isConnected()) {
			// we need the chat id
			long chatId = getChatId();
			// the auth token
			String restToken = QodemePreferences.getInstance().getRestToken();

			int event = GetEventForChatEvents();
			Log.d(TAG, activityName + "Sending register for chat event message...");
			sendWebSocketMessageWith(chatId, restToken, event);

		}
	}

	// This method is called to handle the data received as part of a web socket
	// message
	private void receiveWebSocketMessageWith(String message) {
		String activityName = "receiveWebSocketMessageWith:";

		try {
			JSONObject messageJson = new JSONObject(message);
			long chatId = messageJson.getLong("chatId");
			int event = messageJson.getInt("event");
			// String token = messageJson.getString("authToken");

			Log.d(TAG, activityName + "Received event: " + event + " in chat: " + chatId);
			if (event == GetEventForUserStartedTypingMessage()) {
				// if
				// (!QodemePreferences.getInstance().getRestToken().equals(token))
				receiveOtherUserStartedTypingEvent(chatId);
			} else if (event == GetEventForUserStoppedTypingMessage()) {
				receiveOtherUserStoppedTypingEvent(chatId);
			}

		} catch (JSONException je) {
			Log.e(TAG, activityName + je.toString());
		}
	}

	private void receiveOtherUserStoppedTypingEvent(long chatId) {
		if (chatId == getChatId()) {
			// this.imgUserTyping.setVisibility(View.INVISIBLE);
			customDotViewUserTyping.setVisibility(View.INVISIBLE);
			footerView.setVisibility(View.GONE);
			isUsertyping = false;
		}
	}

	private void receiveOtherUserStartedTypingEvent(long chatId) {
		if (chatId == getChatId()) {
			// we make visible the image view to show the other user is typing
			// this.imgUserTyping.setVisibility(View.VISIBLE);
			customDotViewUserTyping.setVisibility(View.VISIBLE);
			footerView.setVisibility(View.VISIBLE);
			if (!isUsertyping) {
				handlerForUserTyping.sendEmptyMessageDelayed(0, 500);
				isUsertyping = true;
			}
		}
	}

	private void sendWebSocketMessageWith(long chatId, String authToken, int event) {
		String activityName = "sendWebSocketMessageWith:";

		if (mConnection.isConnected()) {

			try {
				JSONObject json = new JSONObject();
				json.put("chatId", chatId);
				json.put("authToken", authToken);
				json.put("event", event);

				// now we send the message
				mConnection.sendTextMessage(json.toString());

				Log.d(TAG, activityName + "Successfully sent payload " + json.toString());
			} catch (JSONException e) {
				Log.e(TAG, activityName + "Received JSONException: " + e.toString());
			} catch (Exception e) {
				Log.e(TAG, activityName + "Received Exception: " + e.toString());

			}
		}
	}

	private void sendUserStoppedTypingMessage() {
		String activityName = "sendUserStoppedTypingMessage:";
		if (mConnection.isConnected()) {
			// we have a open web socket connection
			long chatId = getChatId();
			String restToken = QodemePreferences.getInstance().getRestToken();
			int event = GetEventForUserStoppedTypingMessage();
			Log.d(TAG, activityName + "Sending user stopped typing message...");
			sendWebSocketMessageWith(chatId, restToken, event);
		}
	}

	private void sendUserTypingMessage() {
		String activityName = "sendUserTypingMessage:";
		// this will send over the web socket a message that the user has begun
		// typing
		if (mConnection.isConnected()) {
			// we need the chat id
			long chatId = getChatId();
			// the auth token
			String restToken = QodemePreferences.getInstance().getRestToken();

			int event = GetEventForUserStartedTypingMessage();
			Log.d(TAG, activityName + "Sending user typing message...");
			sendWebSocketMessageWith(chatId, restToken, event);

		}
	}

	private int GetEventForChatEvents() {
		return 0;
	}

	private int GetEventForUserStartedTypingMessage() {
		return 1;
	}

	private int GetEventForUserStoppedTypingMessage() {
		return 2;
	}

	private void start() {
		final String wsuri = ApplicationConstants.WEB_SOCKET_BASE_URL;

		try {
			if (!mConnection.isConnected()) {
				mConnection.connect(wsuri, new WebSocketHandler() {

					@Override
					public void onOpen() {
						Log.d(TAG, "Status: Connected to " + wsuri);
						sendRegisterForChatEvents();

					}

					@Override
					public void onTextMessage(String payload) {
						Log.d(TAG, "Got echo: " + payload);
						receiveWebSocketMessageWith(payload);
					}

					@Override
					public void onClose(int code, String reason) {
						Log.d(TAG, "Connection lost.");
					}
				});
			}
		} catch (WebSocketException e) {

			Log.d(TAG, e.toString());
		}
	}

	private void stop() {
		try {
			if (mConnection.isConnected()) {
				// let's disconnect the socket
				mConnection.disconnect();
				Log.d(TAG, "Disconnected web socket");
			}

		} catch (Exception e) {
			Log.d(TAG, e.toString());

		}
	}

	private void sendMessage() {

		String message = mMessageField.getText().toString().trim();
		if (TextUtils.isEmpty(message) || TextUtils.isEmpty(message.trim())) {
			Toast.makeText(getActivity(), "Empty message can't be sent", Toast.LENGTH_SHORT).show();
			return;
		}
		// we need to send websocket message that the user has stopped typing
		sendUserStoppedTypingMessage();
		mMessageField.setText("");
		// Helper.hideKeyboard(getActivity(), mMessageField);
		callback.sendMessage(getChatId(), message, "", 0, -1, 0, 0, "", "");
		mMessageField.post(new Runnable() {
			@Override
			public void run() {
				mMessageField.requestFocus();
				// Helper.showKeyboard(getActivity(), mMessageField);
			}
		});
	}

	public void sendImageMessage(String message) {

		// we need to send websocket message that the user has stopped typing
		sendUserStoppedTypingMessage();
		mMessageField.setText("");
		// Helper.hideKeyboard(getActivity(), mMessageField);
		callback.sendMessage(getChatId(), "", "", 1, -1, 0, 0, "", message);
		mMessageField.post(new Runnable() {
			@Override
			public void run() {
				mMessageField.requestFocus();
				// Helper.showKeyboard(getActivity(), mMessageField);
			}
		});
	}

	private void initListView() {

		// final String myQrCOde = QodemePreferences.getInstance().getQrcode();
		final String senderQr = getSenderQr();
		final int oponentColor = getChatColor() == 0 ? Color.GRAY : getChatColor();
		final int myColor = getActivity().getResources().getColor(R.color.text_chat_name);

		imgUserTyping = (ImageView) getView().findViewById(R.id.img_typing);

		Bitmap bmp = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(oponentColor);

		Canvas c = new Canvas(bmp);
		c.drawCircle(20, 20, 20, paint);
		imgUserTyping.setImageBitmap(bmp);
		imgUserTyping.setVisibility(View.GONE);

//		footerView = getActivity().getLayoutInflater().inflate(R.layout.footer_user_typing, null);
//		footerView.setVisibility(View.GONE);
		View view  = getActivity().getLayoutInflater().inflate(R.layout.footer_user_typing, null);
		footerView = view.findViewById(R.id.linearFooter_userTyping);
		footerView.setVisibility(View.GONE);
		
		footerView1 = view.findViewById(R.id.linearTyping);
		footerView1.setVisibility(View.GONE);
		CustomDotView dotView = (CustomDotView) view.findViewById(R.id.dotView_userTyping1);
		dotView.setDotColor(getResources().getColor(R.color.user_typing));
		dotView.setOutLine(true);
		dotView.setSecondVerticalLine(true);
		dotView.invalidate();

		customDotViewUserTyping = (CustomDotView) footerView.findViewById(R.id.dotView_userTyping);

		customDotViewUserTyping.setDotColor(getResources().getColor(R.color.user_typing));
		customDotViewUserTyping.setSecondVerticalLine(true);
		customDotViewUserTyping.invalidate();

		mListView = (ScrollDisabledListView) getView().findViewById(R.id.listview);
		mDate = (TextView) getView().findViewById(R.id.date);
		mLocation = (TextView) getView().findViewById(R.id.location);
		List<Message> listForAdapter = Lists.newArrayList();
		mListAdapter = new ExtendedGroupListAdapter<ChatListGroupSubItem, Message, ChatListSubAdapterCallback>(
				getActivity(), R.layout.group_chat_list_item_list_item, listForAdapter,
				new ChatListSubAdapterCallback() {

					@Override
					public int getColor(String senderQrcode) {
						if (TextUtils.equals(senderQr, senderQrcode))
							return oponentColor;
						else
							return myColor;
					}

					@Override
					public Typeface getFont(Fonts font) {
						return callback.getFont(font);
					}

					@Override
					public Contact getContact(String senderQrcode) {
						return callback.getContact(senderQrcode);
					}

					@Override
					public ChatLoad getChatLoad(long chatId) {
						// TODO Auto-generated method stub
						return callback.getChatLoad(chatId);
					}

				}, chatListInsideFragmentCallback);

		mListView.addFooterView(view);
		mListView.setAdapter(mListAdapter);

		mListView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});
		mListView.setStackFromBottom(true);
		mListView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				return mGestureDetector.onTouchEvent(e);
			}
		});
		mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
	}

	public long getChatId() {
		return getArguments().getLong(CHAT_ID, 0L);
	}

	public int getChatColor() {
		return getArguments().getInt(CHAT_COLOR, 0);
	}

	public String getChatName() {
		return "Group Title";// getArguments().getString(CHAT_NAME);
	}

	public String getSenderQr() {
		return getArguments().getString(QRCODE);
	}

	/**
	 * Refresh data can be called from activity
	 */
	public void updateUi() {
		if (isViewCreated) {

			if (chatLoad.is_favorite == 1) {
				Bitmap bm = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_chat_favorite);
				mImgFavorite.setImageBitmap(bm);
			} else {
				Bitmap bm = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_chat_favorite_h);
				mImgFavorite.setImageBitmap(bm);
			}
			if (chatLoad.type == 2) {
				mTextViewNumFavorite.setText(chatLoad.number_of_likes + "");
				mTextViewNumFavorite.setVisibility(View.VISIBLE);
			}
			
			if (getChatLoad() != null) {
				if (getChatLoad().type == 1) {
					mImgMemberBottomLine.setVisibility(View.VISIBLE);
					mTextViewMembers.setVisibility(View.VISIBLE);
					mTextViewMembersLabel.setVisibility(View.VISIBLE);
					String memberNames = "";
					if (getChatLoad().members != null) {
						int i = 0;
						ArrayList<String> nameList = new ArrayList<String>();
						for (String memberQr : getChatLoad().members) {
							if (!QodemePreferences.getInstance().getQrcode().equals(memberQr)) {
								Contact c = callback.getContact(memberQr);
								if (c != null) {
									nameList.add(c.title);
								} else {
									nameList.add("User");
								}
							}
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
					mTextViewMembers.setText(memberNames);
				} else {
					mImgMemberBottomLine.setVisibility(View.GONE);
					mTextViewMembers.setVisibility(View.GONE);
					mTextViewMembersLabel.setVisibility(View.GONE);
				}
			}

			String statusUpdate = QodemePreferences.getInstance().get(
					"" + getArguments().getLong(CHAT_ID), "");
			if (statusUpdate.equals("")) {
				mLinearLayStatusUpdte.setVisibility(View.GONE);
			} else {
				mLinearLayStatusUpdte.setVisibility(View.VISIBLE);
				mStatusUpdate.setText(statusUpdate);
				// QodemePreferences.getInstance().set("" +
				// getArguments().getLong(CHAT_ID), null);
			}

			mListAdapter.clear();

			mListAdapter.addAll(callback.getChatMessages(getChatId()));
			if (mListAdapter.getCount() > 0)
				mListView.setSelection(mListAdapter.getCount() - 1);
			mName.setText(chatLoad.title);
			mStatus.setText(chatLoad.status);
			// mName.setTextColor(getChatColor());

			if (QodemePreferences.getInstance().isSaveLocationDateChecked()) {
				if (getDate() != null) {
					SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yy HH:mm a");
					// String dateStr = fmtOut.format(new
					// Date(Timestamp.valueOf(getDate()).getTime()));
					String dateStr = fmtOut.format(new Date(Converter
							.getCrurentTimeFromTimestamp(getDate())));
					mDate.setText(dateStr + ",");
				} else
					mDate.setText("");
				mLocation.setText(getLocation());
			} else {
				mDate.setText("");
				mLocation.setText("");
			}

			// String message = ChatFocusSaver.getCurrentMessage(getChatId());
			//
			// if (!TextUtils.isEmpty(message)) {
			// mMessageField.setText(message);
			// // Helper.showKeyboard(getActivity(), mMessageField);
			// // mMessageField.setSelection(mMessageField.getText().length());
			// mMessageField.post(new Runnable() {
			// @Override
			// public void run() {
			// mMessageField.requestFocus();
			// mMessageField.setSelection(mMessageField.getText().length());
			// Context c = getActivity();
			// if (c != null)
			// Helper.showKeyboard(getActivity(), mMessageField);
			// mFirstUpdate = false;
			// }
			// });
			// } else if (getFirstUpdate()) {
			// Helper.hideKeyboard(getActivity(), mMessageField);
			// }

//			mMessageField.addTextChangedListener(new TextWatcher() {
//				@Override
//				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//				}
//
//				@Override
//				public void onTextChanged(CharSequence s, int start, int before, int count) {
//				}
//
//				@Override
//				public void afterTextChanged(Editable s) {
//					ChatFocusSaver.setCurrentMessage(getChatId(), s.toString());
//				}
//			});

			if (getChatLoad() != null
					&& getChatLoad().is_locked == 1
					&& !QodemePreferences.getInstance().getQrcode()
							.equals(getChatLoad().user_qrcode)) {
				mBtnImageSend.setVisibility(View.INVISIBLE);
				mSendButton.setVisibility(View.INVISIBLE);
				mBtnImageSendBottom.setVisibility(View.INVISIBLE);
				mMessageField.setVisibility(View.INVISIBLE);
			} else {
				mBtnImageSend.setVisibility(View.VISIBLE);
				mSendButton.setVisibility(View.VISIBLE);
				mBtnImageSendBottom.setVisibility(View.VISIBLE);
				mMessageField.setVisibility(View.VISIBLE);
			}
			

			if (chatLoad.is_deleted == 1) {
				mTextViewDeleteBaner.setVisibility(View.VISIBLE);
				mBtnImageSend.setVisibility(View.INVISIBLE);
				mBtnImageSendBottom.setVisibility(View.INVISIBLE);
				mSendButton.setVisibility(View.INVISIBLE);
				mMessageField.setVisibility(View.INVISIBLE);
				mImgFavorite.setClickable(false);
			}

		}
	}

	private List<Message> sortMessages(List<Message> messages) {

		if (messages != null) {
			Collections.sort(messages, new Comparator<Message>() {
				@Override
				public int compare(Message u1, Message u2) {
					return u1.timeStamp.compareTo(u2.timeStamp);
				}
			});
		}
		return messages;
	}

	private class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Helper.hideKeyboard(getActivity(), mMessageField);
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			Helper.hideKeyboard(getActivity(), mMessageField);
			sendUserStoppedTypingMessage();
			getActivity().onBackPressed();
			return true;
		}
	}

	public void setArgument(ChatLoad c) {

		this.chatLoad = c;
	}

	private String getDate() {
		return null;// getArguments().getString(DATE);
	}

	private String getLocation() {
		return getArguments().getString(LOCATION);
	}

	private boolean getFirstUpdate() {
		boolean result = getArguments().getBoolean(FIRST_UPDATE) & mFirstUpdate;
		mFirstUpdate = false;
		return result;
	}

	One2OneChatListInsideFragmentCallback chatListInsideFragmentCallback = new One2OneChatListInsideFragmentCallback() {

		@Override
		public void stopTypingMessage() {
			sendUserStoppedTypingMessage();
		}

		@Override
		public void startTypingMessage() {
			sendUserTypingMessage();

		}

		@Override
		public int getChatType(long chatId) {
			return getChatTypeFromActivity(chatId);
		}

		@Override
		public void sendReplyMessage(long messageReplyId, String message, String photoUrl,
				int hashPhoto, long replyTo_Id, double latitude, double longitude, String senderName) {
			// TODO Auto-generated method stub
			if (TextUtils.isEmpty(message) || TextUtils.isEmpty(message.trim())) {
				Toast.makeText(getActivity(), "Empty message can't be sent", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			// we need to send websocket message that the user has stopped
			// typing
			sendUserStoppedTypingMessage();
			// Helper.hideKeyboard(getActivity(), mMessageField);
			callback.sendMessage(getChatId(), message, photoUrl, hashPhoto, replyTo_Id, latitude,
					longitude, senderName, "");
		}

		@Override
		public ImageFetcher getImageFetcher() {
			return getFetcher();
		}

		// @Override
		// public void sendReplyMessage(String qrCode, String message) {
		// if (TextUtils.isEmpty(message) || TextUtils.isEmpty(message.trim()))
		// {
		// Toast.makeText(getActivity(), "Empty message can't be sent",
		// Toast.LENGTH_SHORT)
		// .show();
		// return;
		// }
		// // we need to send websocket message that the user has stopped
		// // typing
		// sendUserStoppedTypingMessage();
		// // Helper.hideKeyboard(getActivity(), mMessageField);
		// callback.sendMessage(getChatId(), message);
		//
		// }
	};

	private int getChatTypeFromActivity(long chatId) {
		return callback.getChatType(chatId);
	}

	public ImageFetcher getFetcher() {
		return callback.getImageFetcher();
	}

	public void setChatLoad(ChatLoad chatLoad) {
		this.chatLoad = chatLoad;
	}

	public ChatLoad getChatLoad() {
		return chatLoad;
	}
}
