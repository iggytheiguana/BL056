package com.blulabellabs.code.ui.one2one;

import java.lang.reflect.Array;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.blulabellabs.code.Application;
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
import com.blulabellabs.code.ui.common.CustomEdit;
import com.blulabellabs.code.ui.common.ExGroupAdapterBasedView;
import com.blulabellabs.code.ui.common.ExtendedListAdapter;
import com.blulabellabs.code.ui.common.ListAdapter;
import com.blulabellabs.code.ui.common.ScrollDisabledListView;
import com.blulabellabs.code.ui.one2one.ChatInsideFragment.One2OneChatListInsideFragmentCallback;
import com.blulabellabs.code.utils.ChatFocusSaver;
import com.blulabellabs.code.utils.Converter;
import com.blulabellabs.code.utils.DbUtils;
import com.blulabellabs.code.utils.Fonts;
import com.blulabellabs.code.utils.Helper;
import com.google.common.collect.Lists;

/**
 * Created by Alex on 10/23/13.
 */
public class ChatListGroupItem extends RelativeLayout implements
		ExGroupAdapterBasedView<ChatLoad, ChatListAdapterCallback> {

	private static final int MIN_CONTENT_SIZE_DP = 100;
	private static final int MAX_CONTENT_SIZE_DP = 350;

	private final int minContentSizePx;
	private final int maxContentSizePx;

	private int _startY;
	private int _yDelta;
	private boolean isDragMode = false;
	private int height;

	private final Context context;
	public TextView name;
	public TextView date;
	public TextView location, mTextViewMembers;
	public ScrollDisabledListView subList;
	public LinearLayout dragView, mLinearMemberList;
	public CustomEdit edit;
	public ImageView dragImage, memberListBottomLine;
	public ImageButton sendMessageBtn;
	public ImageButton sendImgMessageBtn;
	// private View cornerTop;
	// private View cornerBottom;
	// private View cornerLeft;
	// private View cornerRight;

	private GestureDetector gestureDetector;
	private ChatListAdapterCallback mCallback;
	private int mPosition;
	// private Contact mContact;
	private ChatLoad mChatLoad;
	public ImageButton shareChatBtn, mImgBtnFavorite;
	private EditText editTextTitle;
	public RelativeLayout mChatItem, mChatItemChild;
	public boolean isScrolling = false;
	public ImageView textViewUserTyping;
	private View mViewTypedMessage;
	CustomDotView mTypedMessageDot;
	boolean isCancel = false;

	public ChatListGroupItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		minContentSizePx = Converter.dipToPx(this.context, MIN_CONTENT_SIZE_DP);
		maxContentSizePx = Converter.dipToPx(this.context, MAX_CONTENT_SIZE_DP);
		gestureDetector = new GestureDetector(context, new GestureListener());

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

	private void messageRead() {
		mCallback.messageRead(mChatLoad.chatId);
	}

	public EditText getTitleEditText() {
		return editTextTitle = editTextTitle != null ? editTextTitle
				: (EditText) findViewById(R.id.editText_group_title);
	}

	public TextView getName() {
		return name = name != null ? name : (TextView) findViewById(R.id.name);
	}

	public TextView getDate() {
		return date = date != null ? date : (TextView) findViewById(R.id.date);
	}

	public TextView getMembersTextView() {
		return mTextViewMembers = mTextViewMembers != null ? mTextViewMembers
				: (TextView) findViewById(R.id.textView_memberList);
	}

	public TextView getLocation() {
		return location = location != null ? location : (TextView) findViewById(R.id.location);
	}

	public ImageView getUserTyping() {
		return textViewUserTyping = textViewUserTyping != null ? textViewUserTyping
				: (ImageView) findViewById(R.id.userTyping);
	}

	public ScrollDisabledListView getList() {
		return subList = subList != null ? subList
				: (ScrollDisabledListView) findViewById(R.id.subList);
	}

	public LinearLayout getMemberListView() {
		return mLinearMemberList = mLinearMemberList != null ? mLinearMemberList
				: (LinearLayout) findViewById(R.id.linear_memberlist);
	}

	public ImageView getMemberListBottomLine() {
		return memberListBottomLine = memberListBottomLine != null ? memberListBottomLine
				: (ImageView) findViewById(R.id.member_line);
	}

	public LinearLayout getDragView() {
		return dragView = dragView != null ? dragView : (LinearLayout) findViewById(R.id.drag);
	}

	public View getView() {
		return this;
	}

	public View getMessageTypedView() {
		return mViewTypedMessage = mViewTypedMessage != null ? mViewTypedMessage
				: (View) findViewById(R.id.linearTyping);
	}

	public CustomDotView getMessageTypedDot() {
		return mTypedMessageDot = mTypedMessageDot != null ? mTypedMessageDot
				: (CustomDotView) findViewById(R.id.dotView_userTyping1);
	}

	public CustomEdit getMessageEdit() {
		return edit = edit != null ? edit : (CustomEdit) findViewById(R.id.edit_message);
	}

	public ImageView getDragImage() {
		return dragImage = dragImage != null ? dragImage
				: (ImageView) findViewById(R.id.drag_image);
	}

	public ImageButton getSendMessage() {
		return sendMessageBtn = sendMessageBtn != null ? sendMessageBtn
				: (ImageButton) findViewById(R.id.button_message);
	}

	public ImageButton getSendImage() {
		return sendImgMessageBtn = sendImgMessageBtn != null ? sendImgMessageBtn
				: (ImageButton) findViewById(R.id.btn_camera);
	}

	public ImageButton getShareChatBtn() {
		return shareChatBtn = shareChatBtn != null ? shareChatBtn
				: (ImageButton) findViewById(R.id.btn_share);
	}

	public ImageButton getFavoriteBtn() {
		return mImgBtnFavorite = mImgBtnFavorite != null ? mImgBtnFavorite
				: (ImageButton) findViewById(R.id.btnFavorite);
	}

	public RelativeLayout getChatItem() {
		return mChatItem = mChatItem != null ? mChatItem
				: (RelativeLayout) findViewById(R.id.relative_chatItem);
	}

	public RelativeLayout getChatItemChild() {
		return mChatItemChild = mChatItemChild != null ? mChatItemChild
				: (RelativeLayout) findViewById(R.id.relative_chatItemChild);
	}

	// sendImgMessageBtn

	// public View getCornerTop() {
	// return cornerTop = cornerTop != null ? cornerTop :
	// findViewById(R.id.corner_top);
	// }
	//
	// public View getCornerBottom() {
	// return cornerBottom = cornerBottom != null ? cornerBottom
	// : findViewById(R.id.corner_bottom);
	// }
	//
	// public View getCornerLeft() {
	// return cornerLeft = cornerLeft != null ? cornerLeft :
	// findViewById(R.id.corner_left);
	// }
	//
	// public View getCornerRight() {
	// return cornerRight = cornerRight != null ? cornerRight :
	// findViewById(R.id.corner_right);
	// }

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		return super.onTouchEvent(e) ? true : gestureDetector.onTouchEvent(e);
	}

	public void fill(Contact contact, ChatListAdapterCallback one2OneAdapterCallback, int position) {
		// this.mContact = contact;
		this.mCallback = one2OneAdapterCallback;
		this.mPosition = position;
		// fill(contact);
	}

	private class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			// ChatLoad chatLoad = mCallback.getChatLoad(mChatLoad.chatId);
			try {
				if (mChatLoad.is_locked != 1 && mChatLoad.is_deleted != 1)
					showMessage();

				mCallback.onSingleTap(getView(), mPosition, mChatLoad);
				// messageRead();
			} catch (Exception ex) {
			}
			Log.i("GestureListener", "onSingleTap");
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			isCancel = true;
			Helper.hideKeyboard(getContext(), getMessageEdit());
			mCallback.onDoubleTap(getView(), mPosition, mChatLoad);
			// messageRead();
			Log.i("GestureListener", "onDoubleTap");
			return true;
		}
	}

	public void showMessage() {
		getSendMessage().setVisibility(View.VISIBLE);
		getMessageEdit().setInputType(
				InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		getMessageEdit().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() > 0) {
					// mSendButton.setVisibility(View.VISIBLE);
					// sendUserTypingMessage();// send user typing message
					MainActivity activity = (MainActivity) getContext();
					activity.sendUserTypingMessage(mChatLoad.chatId);
					getMessageTypedView().setVisibility(VISIBLE);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				ChatFocusSaver.setCurrentMessage(mChatLoad.chatId, s.toString());

				// if (s.length() > 0) {
				// getSendMessage().setVisibility(View.VISIBLE);
				// } else {
				// getSendMessage().setVisibility(View.GONE);
				// }
				if (s.length() > 0) {
					// mSendButton.setVisibility(View.VISIBLE);
					// sendUserTypingMessage();
					MainActivity activity = (MainActivity) getContext();
					activity.sendUserTypingMessage(mChatLoad.chatId);
					getMessageTypedView().setVisibility(VISIBLE);
				} else {
					// mSendButton.setVisibility(View.GONE);
					// sendUserStoppedTypingMessage();
					MainActivity activity = (MainActivity) getContext();
					activity.sendUserStoppedTypingMessage(mChatLoad.chatId);
					getMessageTypedView().setVisibility(GONE);
				}

			}
		});

		getMessageEdit().setOnEditTextImeBackListener(new CustomEdit.OnEditTextImeBackListener() {
			@Override
			public void onImeBack(CustomEdit ctrl) {
				ChatFocusSaver.setFocusedChatId(0);
				getSendMessage().setVisibility(View.GONE);
				getMessageEdit().setVisibility(View.GONE);
				MainActivity activity = (MainActivity) getContext();
				activity.sendUserStoppedTypingMessage(mChatLoad.chatId);
				getMessageTypedView().setVisibility(GONE);
			}
		});

		/*
		 * getMessageEdit().setOnEditorActionListener(new
		 * TextView.OnEditorActionListener() {
		 * 
		 * @Override public boolean onEditorAction(TextView v, int actionId,
		 * KeyEvent event) { if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
		 * ChatFocusSaver.setFocusedChatId(0); return true; } return false; }
		 * });
		 */

		String msg = ChatFocusSaver.getCurrentMessage(mChatLoad.chatId);
		if (!TextUtils.isEmpty(msg)) {
			getMessageEdit().setText(msg);
			getMessageEdit().setSelection(msg.length());
		}

		ChatFocusSaver.setFocusedChatId(mChatLoad.chatId);
		getMessageEdit().setVisibility(VISIBLE);
		getMessageEdit().post(new Runnable() {
			@Override
			public void run() {
				if (!isCancel) {
					getMessageEdit().requestFocus();
					Helper.showKeyboard(getContext(), getMessageEdit());
				}
			}
		});

	}

	private void sendMessage() {
		String message = getMessageEdit().getText().toString();
		getView().requestFocus();
		getMessageEdit().setVisibility(GONE);
		getMessageEdit().setText("");

		ChatFocusSaver.setCurrentMessage(mChatLoad.chatId, "");

		if (TextUtils.isEmpty(message) || TextUtils.isEmpty(message.trim())) {
			Toast.makeText(context, "Empty message can't be sent", Toast.LENGTH_SHORT).show();
			return;
		}
		mCallback.sendMessage(mChatLoad.chatId, message, "", 0, -1, 0, 0, "", "");
		mCallback.messageRead(mChatLoad.chatId);
	}

	private void setCornerColor(int index, int color) {
		int defColor = getResources().getColor(R.color.conversation_card_background);
		int c = (index == 0) ? defColor : color;
		// getCornerTop().setBackgroundColor(c);
		// getCornerBottom().setBackgroundColor(c);
		// getCornerLeft().setBackgroundColor(c);
		// getCornerRight().setBackgroundColor(c);
	}

	One2OneChatListInsideFragmentCallback callbackChatListInsideFragmentCallback = new One2OneChatListInsideFragmentCallback() {

		@Override
		public void stopTypingMessage() {
			MainActivity activity = (MainActivity) getContext();
			activity.sendUserStoppedTypingMessage(mChatLoad.chatId);
		}

		@Override
		public void startTypingMessage() {
			MainActivity activity = (MainActivity) getContext();
			activity.sendUserTypingMessage(mChatLoad.chatId);
		}

		@Override
		public void sendReplyMessage(long messageReplyId, String message, String photoUrl,
				int hashPhoto, long replyTo_Id, double latitude, double longitude, String senderName) {
			// TODO Auto-generated method stub
			ChatFocusSaver.setCurrentMessage(mChatLoad.chatId, "");

			if (TextUtils.isEmpty(message) || TextUtils.isEmpty(message.trim())) {
				Toast.makeText(context, "Empty message can't be sent", Toast.LENGTH_SHORT).show();
				return;
			}
			mCallback.sendMessage(mChatLoad.chatId, message, photoUrl, hashPhoto, replyTo_Id,
					latitude, longitude, senderName, "");
			mCallback.messageRead(mChatLoad.chatId);
		}

		@Override
		public ImageFetcher getImageFetcher() {
			// TODO Auto-generated method stub
			return mCallback.getImageFetcher();
		}

		@Override
		public int getChatType(long chatId) {
			// TODO Auto-generated method stub
			return mCallback.getChatType(chatId);
		}

		// @Override
		// public void sendReplyMessage(int replyToId, String message) {
		//
		// ChatFocusSaver.setCurrentMessage(mContact.chatId, "");
		//
		// if (TextUtils.isEmpty(message) || TextUtils.isEmpty(message.trim()))
		// {
		// Toast.makeText(context, "Empty message can't be sent",
		// Toast.LENGTH_SHORT).show();
		// return;
		// }
		// mCallback.sendMessage(mContact, message);
		// mCallback.messageRead(mContact.chatId);
		//
		// }
	};
	List<Message> temp = Lists.newArrayList();

	@SuppressWarnings("unchecked")
	@Override
	public void fill(ChatLoad t) {

		getSendImage().setVisibility(View.VISIBLE);
		getFavoriteBtn().setClickable(true);
		getTitleEditText().setEnabled(true);
		getTitleEditText().setTypeface(Application.typefaceRegular);
		getShareChatBtn().setEnabled(true);
		getMessageTypedView().setVisibility(GONE);
		isCancel = false;
		try {
			mChatLoad = t;

			if (mChatLoad.isTyping) {
				// getUserTyping().setTextColor(Color.RED);
				getUserTyping().setBackgroundResource(R.drawable.bg_user_typing_h);
			} else {
				getUserTyping().setBackgroundResource(R.drawable.bg_user_typing);
			}

			if (t.is_favorite == 1) {
				Bitmap bm = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_chat_favorite);
				getFavoriteBtn().setImageBitmap(bm);
			} else {
				Bitmap bm = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_chat_favorite_h);
				getFavoriteBtn().setImageBitmap(bm);
			}
			getFavoriteBtn().setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int num_of_favorite = mChatLoad.number_of_likes;
					int is_favorite = 1;
					if (mChatLoad.is_favorite == 1) {
						is_favorite = 2;
						num_of_favorite--;
					} else {
						is_favorite = 1;
						if (num_of_favorite <= 0) {
							num_of_favorite = 1;
						} else
							num_of_favorite++;
					}
					if (mChatLoad.isSearchResult) {
						mChatLoad.number_of_likes = num_of_favorite;
						mChatLoad.is_favorite = is_favorite;
						if (is_favorite == 1) {
							Bitmap bm = BitmapFactory.decodeResource(getResources(),
									R.drawable.ic_chat_favorite);
							getFavoriteBtn().setImageBitmap(bm);
						} else {
							Bitmap bm = BitmapFactory.decodeResource(getResources(),
									R.drawable.ic_chat_favorite_h);
							getFavoriteBtn().setImageBitmap(bm);
						}

						String date = Converter.getCurrentGtmTimestampString();
						RestAsyncHelper.getInstance().setFavorite(date, is_favorite,
								mChatLoad.chatId, new RestListener<SetFavoriteResponse>() {

									@Override
									public void onResponse(SetFavoriteResponse response) {

									}

									@Override
									public void onServiceError(RestError error) {

									}
								});
						RestAsyncHelper.getInstance().chatAddMember(mChatLoad.chatId,
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
						getContext().getContentResolver().update(QodemeContract.Chats.CONTENT_URI,
								QodemeContract.Chats.updateFavorite(is_favorite, num_of_favorite),
								QodemeContract.Chats.CHAT_ID + " = " + mChatLoad.chatId, null);
						SyncHelper.requestManualSync();
					}
				}
			});

			if (t.type == 1) {
				getMemberListView().setVisibility(VISIBLE);
				getMemberListBottomLine().setVisibility(VISIBLE);

				String memberNames = "";
				if (t.members != null) {
					int i = 0;
					ArrayList<String> nameList = new ArrayList<String>();
					for (String memberQr : t.members) {
						if (!QodemePreferences.getInstance().getQrcode().equals(memberQr)) {
							Contact c = mCallback.getContact(memberQr);
							if (c != null) {
								nameList.add(c.title);
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
				getMembersTextView().setText(memberNames);
			}
			if (QodemePreferences.getInstance().getNewPublicGroupChatId() == t.chatId
					|| QodemePreferences.getInstance().getNewPrivateGroupChatId() == t.chatId) {
				getTitleEditText().setVisibility(VISIBLE);
				getTitleEditText().setText(mChatLoad.title);
				getName().setVisibility(GONE);
				getSendMessage().setVisibility(View.VISIBLE);
				getMessageEdit().setVisibility(VISIBLE);
				// if (mChatLoad.title.trim().length() > 0) {
				// } else {
				// getTitleEditText().setFocusable(true);
				// getTitleEditText().requestFocus();
				// InputMethodManager
				// inputMethodManager=(InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				// inputMethodManager.toggleSoftInputFromWindow(getTitleEditText().getWindowToken(),
				// InputMethodManager.SHOW_FORCED, 0);

				// getTitleEditText().post(new Runnable() {
				// @Override
				// public void run() {
				// getTitleEditText().requestFocus();
				// Helper.showKeyboard(getContext(), getTitleEditText());
				// }
				// });
				getTitleEditText().setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

				getTitleEditText().setOnEditorActionListener(new OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEARCH
								|| actionId == EditorInfo.IME_ACTION_GO
								|| actionId == EditorInfo.IME_ACTION_SEND
								|| actionId == EditorInfo.IME_ACTION_DONE
								&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

							String title = v.getText().toString().trim();

							if (!title.trim().equals("")) {

								int updated = mChatLoad.updated;

								try {
									ArrayList<String> tagsList = findTagFromTitle(title);
									
									if (mChatLoad.tag != null && !mChatLoad.tag.trim().equals("")) {
										String[] chatTag = mChatLoad.tag.split(",");
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
										for(int i=0; i<chatTag.length; i++){
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
									mChatLoad.tag = tags;
									// tags = mChatLoad.tag;
									// if(tags.trim().startsWith(",")){
									// tags = tags.trim();
									// tags = tags.substring(1,tags.length());
									// mChatLoad.tag = tags;
									// }

									getContext().getContentResolver().update(
											QodemeContract.Chats.CONTENT_URI,
											QodemeContract.Chats.updateChatInfoValues(title, -1,
													"", 0, "", tags, updated, 5),
											QodemeContract.Chats.CHAT_ID + "=?",
											DbUtils.getWhereArgsForId(mChatLoad.chatId));
								} catch (Exception e) {
								}

								getContext().getContentResolver().update(
										QodemeContract.Chats.CONTENT_URI,
										QodemeContract.Chats.updateChatInfoValues(title, -1, "", 0,
												"", "", updated, 0),
										QodemeContract.Chats.CHAT_ID + "=?",
										DbUtils.getWhereArgsForId(mChatLoad.chatId));
								// setChatInfo(chatload.chatId, title, null,
								// null,
								// null,
								// null, null);
								mChatLoad.title = title;
								setChatInfo(mChatLoad.chatId, null, mChatLoad.color, mChatLoad.tag,
										mChatLoad.description, mChatLoad.status,
										mChatLoad.is_locked, mChatLoad.title, mChatLoad.latitude,
										mChatLoad.longitude);

								Helper.hideKeyboard(getContext(), getTitleEditText());
								// QodemePreferences.getInstance().setNewPublicGroupChatId(-1l);
								return true;
							}
						}
						return false;
					}
				});

				// }
			} else {
				getTitleEditText().setVisibility(GONE);
				getName().setVisibility(VISIBLE);
				getTitleEditText().setText("");
			}

			final String oponentQr = mChatLoad.qrcode;
			final int oponentColor = mChatLoad.color == 0 ? Color.GRAY : mChatLoad.color;
			final int myColor = context.getResources().getColor(R.color.text_chat_name);
			getName().setText(mChatLoad.title != null ? mChatLoad.title : "");
			// getName().setTextColor(oponentColor);
			setCornerColor(mCallback.getNewMessagesCount(mChatLoad.chatId), oponentColor);
			// getName().setTypeface(mCallback.getFont(Fonts.ROBOTO_BOLD));
			if (QodemePreferences.getInstance().isSaveLocationDateChecked()) {
				// if (ce.date != null) {
				// SimpleDateFormat fmtOut = new
				// SimpleDateFormat("MM/dd/yy HH:mm a");
				// String dateStr = fmtOut.format(new Date(Converter
				// .getCrurentTimeFromTimestamp(ce.date)));
				// getDate().setText(dateStr + ",");
				// } else
				// getDate().setText("");
				// getLocation().setText(ce.location);
			} else {
				getDate().setText("");
				getLocation().setText("");
			}

			getMessageTypedDot().setDotColor(getResources().getColor(R.color.user_typing));
			getMessageTypedDot().setOutLine(true);
			getMessageTypedDot().setSecondVerticalLine(true);
			getMessageTypedDot().invalidate();

			// List preparation
			List<Message> listForAdapter = Lists.newArrayList();
			List<Message> listData = Lists.newArrayList();
			// if (mChatLoad.isSearchResult) {
			// for(Message msg:mChatLoad.messages)
			// listData.add(msg);
			// }else{
			// listData = mCallback.getMessages(mChatLoad.chatId);
			// }
			if (mChatLoad.isSearchResult) {
				// listData = mChatLoad.messages;
				if (mChatLoad.messages != null) {
					listData = Lists.newArrayList();
					for (int i = 0; i < mChatLoad.messages.length; i++) {
						listData.add(mChatLoad.messages[i]);
					}
				}
			} else
				listData = mCallback.getMessages(mChatLoad.chatId);
			// listData = mCallback.getMessages(mChatLoad.chatId);
			boolean isContainUnread = false;
			temp.clear();
			listData = sortMessages(listData);
			if (listData != null) {
				List<Message> replyMessage = new ArrayList<Message>();
				final List<Message> tempMessage = new ArrayList<Message>();
				tempMessage.addAll(listData);

				for (Message message : tempMessage) {
					if (message.replyTo_id > 0) {
						replyMessage.add(message);
						listData.remove(message);
					}
					if (message.state == 3) {
						Contact contact = mCallback.getContact(message.qrcode);
						if (contact != null
								&& contact.state != QodemeContract.Contacts.State.BLOCKED_BY)
							isContainUnread = true;
					}
				}

				HashMap<Long, List<Message>> map = new HashMap<Long, List<Message>>();
				ArrayList<Long> chatId = new ArrayList<Long>();
				for (Message m : listData) {

					List<Message> arrayList = new ArrayList<Message>();
					for (Message message : replyMessage) {
						if (message.replyTo_id == m.messageId) {
							arrayList.add(message);
						}
					}
					arrayList = sortMessages(arrayList);
					if (arrayList.size() > 0) {
						if (arrayList.size() > 1) {
							int i = 0;
							for (Message me : arrayList) {
								if (i == 0)
									me.isLast = true;
								else if (i == arrayList.size() - 1)
									me.isFirst = true;
								else {
									me.isFirst = true;
									me.isLast = true;
								}
								i++;
							}
						}

						map.put(m.messageId, arrayList);
						chatId.add(m.messageId);
					}

				}
				for (Long id : chatId) {
					int i = 0;
					for (Message m : listData) {
						if (m.messageId == id) {
							if (i < listData.size()) {
								listData.addAll(i + 1, map.get(id));
							} else {
								listData.addAll(map.get(id));
								// break;
							}
							break;
						}
						i++;
					}
				}
				if (listData.size() > 10) {
					for (int i = listData.size() - 10; i < listData.size(); i++)
						temp.add(listData.get(i));
				} else {
					temp.addAll(listData);
				}

				// for (Message message : replyMessage) {
				// int i = 0;
				// for (Message m : listData) {
				// if (message.replyTo_id == m.messageId) {
				// if (i != listData.size() - 1) {
				// if (listData.get(i + 1).replyTo_id > 0
				// && listData.get(i + 1).replyTo_id == message.replyTo_id) {
				// } else
				// listData.add(i + 1, message);
				// } else
				// listData.add(message);
				// break;
				// }
				// i++;
				// }
				// }
			}

			final ListAdapter listAdapter = new ExtendedListAdapter<ChatListSubItem, Message, ChatListSubAdapterCallback>(
					context, R.layout.one2one_chat_list_item_list_item, listForAdapter,
					new ChatListSubAdapterCallback() {
						@Override
						public int getColor(String senderQrcode) {
							// if (TextUtils.equals(oponentQr, senderQrcode))
							// return oponentColor;
							// else
							if (QodemePreferences.getInstance().getQrcode().equals(senderQrcode))
								return myColor;
							else {
								Contact contact = mCallback.getContact(senderQrcode);
								if (contact != null)
									return mCallback.getContact(senderQrcode).color;
								else
									return myColor;
							}

						}

						@Override
						public Typeface getFont(Fonts font) {
							return mCallback.getFont(font);
						}

						@Override
						public Contact getContact(String senderQrcode) {
							return mCallback.getContact(senderQrcode);
						}

						@Override
						public ChatLoad getChatLoad(long chatId) {
							return mCallback.getChatLoad(chatId);
						}
					}, callbackChatListInsideFragmentCallback);

			if (isContainUnread)
				getChatItem().setBackgroundResource(R.drawable.bg_shadow);
			else {
				getChatItem().setBackgroundResource(R.drawable.bg_box);
			}
			if (mChatLoad.color != 0 && mChatLoad.color != -1)
				getChatItemChild().setBackgroundColor(mChatLoad.color);
			else {
				getChatItemChild().setBackgroundResource(0);
			}
			if (!isScrolling) {
				if (listData != null) {
					listAdapter.addAll(temp);
				}
			} else {
				if (listData != null) {
					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							listAdapter.addAll(temp);

						}
					}, 200);
				}

			}

			getList().setAdapter(listAdapter);
			getList().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
			getList().setStackFromBottom(true);
			// getList().setDisabled(true);
			getList().setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {

					return gestureDetector.onTouchEvent(event);
				}
			});

			height = mCallback.getChatHeight(mChatLoad.chatId);
			ListView.LayoutParams lParams = new ListView.LayoutParams(
					ListView.LayoutParams.MATCH_PARENT, height);
			setLayoutParams(lParams);
			getDragView().setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					final int Y = (int) event.getRawY();
					switch (event.getAction() & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_DOWN: {
						ListView.LayoutParams lParams = getLayoutParams();
						_yDelta = Y;
						_startY = lParams.height;
						mCallback.setDragModeEnabled(true);
						getDragImage().setImageResource(R.drawable.chat_panel_resizer_pressed);
						return true;
					}
					case MotionEvent.ACTION_MOVE: {
						ListView.LayoutParams lParams = getLayoutParams();
						int delta = _startY + Y - _yDelta;
						lParams.height = delta < minContentSizePx ? minContentSizePx
								: delta > maxContentSizePx ? maxContentSizePx : delta;
						getView().setLayoutParams(lParams);
						getView().invalidate();
						return true;
					}
					case MotionEvent.ACTION_CANCEL:
						cancelMotion();
						break;
					case MotionEvent.ACTION_UP:
						cancelMotion();
						break;

					case MotionEvent.ACTION_OUTSIDE:
						cancelMotion();
						break;
					}
					return false;
				}

				private void cancelMotion() {
					mCallback.setChatHeight(mChatLoad.chatId, getLayoutParams().height);
					mCallback.setDragModeEnabled(false);
					getDragImage().setImageResource(R.drawable.chat_panel_resizer);
				}

				private ListView.LayoutParams getLayoutParams() {
					return (ListView.LayoutParams) getView().getLayoutParams();
				}

			});

			if (ChatFocusSaver.getFocusedChatId() == mChatLoad.chatId) {
				showMessage();
			}

			getSendMessage().setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					sendMessage();
				}
			});

			ChatLoad chatLoad = mCallback.getChatLoad(mChatLoad.chatId);

			if (chatLoad != null && chatLoad.is_locked == 1
					&& !QodemePreferences.getInstance().getQrcode().equals(chatLoad.user_qrcode)) {
				getSendImage().setVisibility(View.GONE);
				getFavoriteBtn().setClickable(false);
				getTitleEditText().setEnabled(false);

			} else {
				getSendImage().setVisibility(View.VISIBLE);
				getFavoriteBtn().setClickable(true);
				getTitleEditText().setEnabled(true);

			}
			if (mChatLoad != null && mChatLoad.is_deleted == 1) {
				getSendImage().setVisibility(View.GONE);
				getFavoriteBtn().setClickable(false);
				getTitleEditText().setEnabled(false);
				getSendMessage().setVisibility(View.GONE);
				getMessageEdit().setVisibility(View.GONE);
				getShareChatBtn().setEnabled(false);
			}

			getSendImage().setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					MainActivity activity = (MainActivity) v.getContext();
					activity.setCurrentChatId(mChatLoad.chatId);
					activity.takePhoto();
				}
			});

			getShareChatBtn().setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					MainActivity activity = (MainActivity) v.getContext();
					activity.setCurrentChatId(mChatLoad.chatId);
					activity.addMemberInExistingChat();
				}
			});
		} catch (Exception e) {
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

	public void setChatInfo(long chatId, String title, Integer color, String tag, String desc,
			String status, Integer isLocked, String chat_title, String latitude, String longitude) {
		RestAsyncHelper.getInstance().chatSetInfo(mChatLoad.chatId, title, color, tag, desc,
				isLocked, status, chat_title, latitude, longitude,
				new RestListener<VoidResponse>() {

					@Override
					public void onResponse(VoidResponse response) {
					}

					@Override
					public void onServiceError(RestError error) {
						Log.d("Error", error.getMessage() + "");
					}
				});
	}

	@Override
	public void fill(ChatLoad t, ChatListAdapterCallback c, int position) {
		this.mCallback = c;
		this.mPosition = position;
		this.mChatLoad = t;
		this.mPosition = position;
		fill(t);

	}

}
