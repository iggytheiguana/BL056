package com.blulabellabs.code.ui.one2one;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.sync.SyncHelper;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.common.CustomEdit;
import com.blulabellabs.code.ui.common.ExAdapterBasedView;
import com.blulabellabs.code.ui.common.ExListAdapter.ViewHolder;
import com.blulabellabs.code.ui.common.CustomDotView;
import com.blulabellabs.code.ui.common.ExtendedListAdapter;
import com.blulabellabs.code.ui.common.ListAdapter;
import com.blulabellabs.code.ui.common.ScrollDisabledListView;
import com.blulabellabs.code.ui.one2one.ChatInsideFragment.One2OneChatListInsideFragmentCallback;
import com.blulabellabs.code.utils.ChatFocusSaver;
import com.blulabellabs.code.utils.Converter;
import com.blulabellabs.code.utils.Fonts;
import com.blulabellabs.code.utils.Helper;
import com.google.common.collect.Lists;

/**
 * Created by Alex on 10/23/13.
 */
public class ChatListItem extends RelativeLayout implements
		ExAdapterBasedView<Contact, ChatListAdapterCallback> {

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
	public TextView location;
	public ScrollDisabledListView subList;
	public LinearLayout dragView;
	public CustomEdit edit;
	public ImageView dragImage;
	public ImageButton sendMessageBtn;
	public ImageButton sendImgMessageBtn, mImgBtnFavorite;
	private View cornerTop;
	private View cornerBottom;
	private View cornerLeft;
	private View cornerRight;
	public RelativeLayout mChatItem, mChatItemChild;
	public ImageView textViewUserTyping;

	private GestureDetector gestureDetector;
	private ChatListAdapterCallback mCallback;
	private int mPosition;
	private Contact mContact;
	private ViewHolder viewHolder;
	public boolean isScrolling = false;
	private View mViewTypedMessage;
	CustomDotView mTypedMessageDot;
	boolean isCancel = false;

	public ChatListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		minContentSizePx = Converter.dipToPx(this.context, MIN_CONTENT_SIZE_DP);
		maxContentSizePx = Converter.dipToPx(this.context, MAX_CONTENT_SIZE_DP);
		gestureDetector = new GestureDetector(context, new GestureListener());

	}

	View footerView1;
	List<Message> temp = Lists.newArrayList();

	@SuppressWarnings("unchecked")
	@SuppressLint("SimpleDateFormat")
	@Override
	public void fill(Contact ce) {

		mContact = ce;
		isCancel = false;

		getMessageTypedView().setVisibility(GONE);

		final String oponentQr = mContact.qrCode;
		final int oponentColor = mContact.color == 0 ? Color.GRAY : mContact.color;
		final int myColor = context.getResources().getColor(R.color.text_chat_name);
		getName().setText(mContact.title != null ? mContact.title : "User");
		getName().setTextColor(oponentColor);
		setCornerColor(mCallback.getNewMessagesCount(mContact.chatId), oponentColor);
		// getName().setTypeface(mCallback.getFont(Fonts.ROBOTO_BOLD));
		if (QodemePreferences.getInstance().isSaveLocationDateChecked()) {
			if (ce.date != null) {
				SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yy HH:mm a");
				String dateStr = fmtOut.format(new Date(Converter
						.getCrurentTimeFromTimestamp(ce.date)));
				getDate().setText(dateStr + ",");
			} else
				getDate().setText("");
			getLocation().setText(ce.location);
		} else {
			getDate().setText("");
			getLocation().setText("");
		}

		// List preparation
		List<Message> listForAdapter = Lists.newArrayList();
		List<Message> listData = mCallback.getMessages(mContact);
		temp.clear();
		listData = sortMessages(listData);
		boolean isContainUnread = false;
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
						if (TextUtils.equals(oponentQr, senderQrcode))
							return oponentColor;
						else
							return myColor;
					}

					@Override
					public Typeface getFont(Fonts font) {
						return mCallback.getFont(font);
					}

					@Override
					public Contact getContact(String senderQrcode) {
						// TODO Auto-generated method stub
						return mCallback.getContact(senderQrcode);
					}

					@Override
					public ChatLoad getChatLoad(long chatId) {
						// TODO Auto-generated method stub
						return mCallback.getChatLoad(chatId);
					}
				}, callbackChatListInsideFragmentCallback);

		if (listData != null) {
			// if (!isScrolling) {
			if (listData != null) {
				listAdapter.addAll(temp);
			}
			// } else {
			// if (listData != null) {
			// new Handler().postDelayed(new Runnable() {
			//
			// @Override
			// public void run() {
			// listAdapter.addAll(temp);
			//
			// }
			// }, 200);
			// }
			// }

		}
		if (isContainUnread)
			getChatItem().setBackgroundResource(R.drawable.bg_shadow);
		else {
			getChatItem().setBackgroundResource(R.drawable.bg_box);
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

		// View view = ((Activity) getContext()).getLayoutInflater().inflate(
		// R.layout.footer_user_typing, null);
		//
		// footerView1 = view.findViewById(R.id.linearTyping);
		// footerView1.setVisibility(View.GONE);
		//
		// CustomDotView dotView = (CustomDotView)
		// view.findViewById(R.id.dotView_userTyping1);
		getMessageTypedDot().setDotColor(getResources().getColor(R.color.user_typing));
		getMessageTypedDot().setOutLine(true);
		getMessageTypedDot().setSecondVerticalLine(true);
		getMessageTypedDot().invalidate();

		height = mCallback.getChatHeight(mContact.chatId);
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
				mCallback.setChatHeight(mContact.chatId, getLayoutParams().height);
				mCallback.setDragModeEnabled(false);
				getDragImage().setImageResource(R.drawable.chat_panel_resizer);
			}

			private ListView.LayoutParams getLayoutParams() {
				return (ListView.LayoutParams) getView().getLayoutParams();
			}

		});

		if (ChatFocusSaver.getFocusedChatId() == mContact.chatId) {
			showMessage();
		}

		getSendMessage().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				sendMessage();
			}
		});

		final ChatLoad chatLoad = mCallback.getChatLoad(mContact.chatId);
		getChatItemChild().setBackgroundResource(0);
		if (chatLoad != null) {
			if (chatLoad.color != 0 && chatLoad.color != -1)
				getChatItemChild().setBackgroundColor(chatLoad.color);
			else {
				getChatItemChild().setBackgroundResource(0);
			}

			if (chatLoad.isTyping) {
				// getUserTyping().setTextColor(Color.RED);
				getUserTyping().setBackgroundResource(R.drawable.bg_user_typing_h);
			} else {
				getUserTyping().setBackgroundResource(R.drawable.bg_user_typing);
			}
			if (chatLoad.is_locked == 1
					&& !QodemePreferences.getInstance().getQrcode().equals(chatLoad.user_qrcode)) {
				getSendImage().setVisibility(View.INVISIBLE);
				getFavoriteBtn().setClickable(false);
			} else {
				getSendImage().setVisibility(View.VISIBLE);
				getFavoriteBtn().setClickable(true);
			}

			if (chatLoad != null && chatLoad.is_deleted == 1) {
				getSendImage().setVisibility(View.INVISIBLE);
				getFavoriteBtn().setClickable(false);
				getSendMessage().setVisibility(View.GONE);
				getMessageEdit().setVisibility(View.GONE);
			}

			if (chatLoad.is_favorite == 1) {
				Bitmap bm = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_chat_favorite);
				getFavoriteBtn().setImageBitmap(bm);
			} else {
				Bitmap bm = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_chat_favorite_h);
				getFavoriteBtn().setImageBitmap(bm);
			}

		}
		getSendImage().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MainActivity activity = (MainActivity) v.getContext();
				activity.setCurrentChatId(mContact.chatId);
				activity.takePhoto();
			}
		});

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
		mCallback.messageRead(mContact.chatId);
	}

	public TextView getName() {
		return name = name != null ? name : (TextView) findViewById(R.id.name);
	}

	public TextView getDate() {
		return date = date != null ? date : (TextView) findViewById(R.id.date);
	}

	public TextView getLocation() {
		return location = location != null ? location : (TextView) findViewById(R.id.location);
	}

	public ScrollDisabledListView getList() {
		return subList = subList != null ? subList
				: (ScrollDisabledListView) findViewById(R.id.subList);
	}

	public LinearLayout getDragView() {
		return dragView = dragView != null ? dragView : (LinearLayout) findViewById(R.id.drag);
	}

	public View getView() {
		return this;
	}

	public CustomEdit getMessageEdit() {
		return edit = edit != null ? edit : (CustomEdit) findViewById(R.id.edit_message);
	}

	public View getMessageTypedView() {
		return mViewTypedMessage = mViewTypedMessage != null ? mViewTypedMessage
				: (View) findViewById(R.id.linearTyping);
	}

	public CustomDotView getMessageTypedDot() {
		return mTypedMessageDot = mTypedMessageDot != null ? mTypedMessageDot
				: (CustomDotView) findViewById(R.id.dotView_userTyping1);
	}

	public ImageView getDragImage() {
		return dragImage = dragImage != null ? dragImage
				: (ImageView) findViewById(R.id.drag_image);
	}

	public ImageView getUserTyping() {
		return textViewUserTyping = textViewUserTyping != null ? textViewUserTyping
				: (ImageView) findViewById(R.id.userTyping);
	}

	public ImageButton getSendMessage() {
		return sendMessageBtn = sendMessageBtn != null ? sendMessageBtn
				: (ImageButton) findViewById(R.id.button_message);
	}

	public ImageButton getSendImage() {
		return sendImgMessageBtn = sendImgMessageBtn != null ? sendImgMessageBtn
				: (ImageButton) findViewById(R.id.btn_camera);
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
		// return true;
	}

	@Override
	public void fill(Contact contact, ChatListAdapterCallback one2OneAdapterCallback, int position) {
		this.mContact = contact;
		this.mCallback = one2OneAdapterCallback;
		this.mPosition = position;
		fill(contact);
	}

	private class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			ChatLoad chatLoad = mCallback.getChatLoad(mContact.chatId);

			if (chatLoad != null && chatLoad.is_locked != 1 && chatLoad.is_deleted != 1)
				showMessage();

			mCallback.onSingleTap(getView(), mPosition, mContact);
			// messageRead();
			Log.i("GestureListener", "onSingleTap");
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			isCancel = true;
			Helper.hideKeyboard(getContext(), getMessageEdit());
			mCallback.onDoubleTap(getView(), mPosition, mContact);
			// messageRead();
			Log.i("GestureListener", "onDoubleTap");
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			super.onLongPress(e);
			Toast.makeText(getContext(), "Long Press", Toast.LENGTH_LONG).show();

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
					activity.sendUserTypingMessage(mContact.chatId);
					getMessageTypedView().setVisibility(VISIBLE);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				ChatFocusSaver.setCurrentMessage(mContact.chatId, s.toString());

				if (s.length() > 0) {
					// mSendButton.setVisibility(View.VISIBLE);
					// sendUserTypingMessage();
					MainActivity activity = (MainActivity) getContext();
					activity.sendUserTypingMessage(mContact.chatId);
					getMessageTypedView().setVisibility(VISIBLE);
				} else {
					// mSendButton.setVisibility(View.GONE);
					// sendUserStoppedTypingMessage();
					MainActivity activity = (MainActivity) getContext();
					activity.sendUserStoppedTypingMessage(mContact.chatId);
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

		String msg = ChatFocusSaver.getCurrentMessage(mContact.chatId);
		if (!TextUtils.isEmpty(msg)) {
			getMessageEdit().setText(msg);
			getMessageEdit().setSelection(msg.length());
		}

		ChatFocusSaver.setFocusedChatId(mContact.chatId);
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

		ChatFocusSaver.setCurrentMessage(mContact.chatId, "");

		if (TextUtils.isEmpty(message) || TextUtils.isEmpty(message.trim())) {
			Toast.makeText(context, "Empty message can't be sent", Toast.LENGTH_SHORT).show();
			return;
		}
		mCallback.sendMessage(mContact, message, "", 0, -1, 0, 0, "", "");
		mCallback.messageRead(mContact.chatId);
	}

	private void setCornerColor(int index, int color) {
		int defColor = getResources().getColor(R.color.conversation_card_background);
		int c = (index == 0) ? defColor : color;
		// getCornerTop().setBackgroundColor(c);
		// getCornerBottom().setBackgroundColor(c);
		// getCornerLeft().setBackgroundColor(c);
		// getCornerRight().setBackgroundColor(c);
	}

	public void setViewHolder(ViewHolder viewHolder) {
		this.viewHolder = viewHolder;
	}

	public ViewHolder getViewHolder() {
		return viewHolder;
	}

	One2OneChatListInsideFragmentCallback callbackChatListInsideFragmentCallback = new One2OneChatListInsideFragmentCallback() {

		@Override
		public void stopTypingMessage() {
			MainActivity activity = (MainActivity) getContext();
			activity.sendUserStoppedTypingMessage(mContact.chatId);

		}

		@Override
		public void startTypingMessage() {
			// TODO Auto-generated method stub
			MainActivity activity = (MainActivity) getContext();
			activity.sendUserTypingMessage(mContact.chatId);
		}

		@Override
		public void sendReplyMessage(long messageReplyId, String message, String photoUrl,
				int hashPhoto, long replyTo_Id, double latitude, double longitude, String senderName) {
			// TODO Auto-generated method stub
			ChatFocusSaver.setCurrentMessage(mContact.chatId, "");

			if (TextUtils.isEmpty(message) || TextUtils.isEmpty(message.trim())) {
				Toast.makeText(context, "Empty message can't be sent", Toast.LENGTH_SHORT).show();
				return;
			}
			mCallback.sendMessage(mContact, message, photoUrl, hashPhoto, replyTo_Id, latitude,
					longitude, senderName, "");
			mCallback.messageRead(mContact.chatId);
		}

		@Override
		public ImageFetcher getImageFetcher() {
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
}
