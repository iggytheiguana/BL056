package com.blulabellabs.code.ui.one2one;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.common.CustomDotView;
import com.blulabellabs.code.ui.common.CustomEdit;
import com.blulabellabs.code.ui.common.ExAdapterBasedView;
import com.blulabellabs.code.ui.common.ExtendedListAdapter;
import com.blulabellabs.code.ui.common.ListAdapter;
import com.blulabellabs.code.ui.common.ScrollDisabledListView;
import com.blulabellabs.code.ui.one2one.ChatInsideFragment.One2OneChatListInsideFragmentCallback;
import com.blulabellabs.code.utils.ChatFocusSaver;
import com.blulabellabs.code.utils.Converter;
import com.blulabellabs.code.utils.Helper;
import com.google.common.collect.Lists;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatListItem extends RelativeLayout implements
        ExAdapterBasedView<Contact, ChatListAdapterCallback> {

    private static final int MIN_CONTENT_SIZE_DP = 100;
    private static final int MAX_CONTENT_SIZE_DP = 350;

    private final int minContentSizePx;
    private final int maxContentSizePx;

    private int _startY;
    private int _yDelta;

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
    public RelativeLayout mChatItem, mChatItemChild;
    public ImageView textViewUserTyping, imgReplyBottom;

    private GestureDetector gestureDetector;
    private ChatListAdapterCallback mCallback;
    private int mPosition;
    private Contact mContact;
    public boolean isScrolling = false;
    private View mViewTypedMessage;
    CustomDotView mTypedMessageDot;
    boolean isCancel = false;
    private Message lastMessage;
    List<Message> temp = Lists.newArrayList();

    public ChatListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        minContentSizePx = Converter.dipToPx(this.context, MIN_CONTENT_SIZE_DP);
        maxContentSizePx = Converter.dipToPx(this.context, MAX_CONTENT_SIZE_DP);
        gestureDetector = new GestureDetector(context, new GestureListener());

    }

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

            Map<Long, List<Message>> map = new HashMap<Long, List<Message>>();
            List<Long> chatId = new ArrayList<Long>();
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
        }

        final ListAdapter<ChatListSubItem, Message> listAdapter = new ExtendedListAdapter<ChatListSubItem, Message, ChatListSubAdapterCallback>(
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
                    public Contact getContact(String senderQrcode) {
                        return mCallback.getContact(senderQrcode);
                    }

                    @Override
                    public ChatLoad getChatLoad(long chatId) {
                        return mCallback.getChatLoad(chatId);
                    }
                }, callbackChatListInsideFragmentCallback
        );

        if (isContainUnread)
            getChatItem().setBackgroundResource(R.drawable.bg_shadow_old);
        else {
            getChatItem().setBackgroundColor(Color.WHITE);
        }
        if (listData != null) {
            if (listData.size() > 0) {
                lastMessage = temp.get(temp.size() - 1);
            }
            listAdapter.addAll(temp);
        }
        getList().setAdapter(listAdapter);
        getList().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        getList().setStackFromBottom(true);
        getList().setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return gestureDetector.onTouchEvent(event);
            }
        });
        getMessageTypedDot().setDotColor(getResources().getColor(R.color.user_typing));
        getMessageTypedDot().setOutLine(true);
        getMessageTypedDot().setSecondVerticalLine(true);
        getMessageTypedDot().invalidate();
        int height = mCallback.getChatHeight(mContact.chatId);
        ListView.LayoutParams lParams = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, height);
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
        if (mContact.chatColor != null) {
            getChatItemChild().setBackgroundColor(mContact.chatColor);
        } else {
            getChatItemChild().setBackgroundResource(0);
        }
        if (chatLoad != null) {
            if (chatLoad.color != 0 && chatLoad.color != -1) {
                getChatItemChild().setBackgroundColor(chatLoad.color);
                mContact.chatColor = chatLoad.color;
                if (lastMessage != null)
                    lastMessage.chatColor = chatLoad.color;
            } else {
                getChatItemChild().setBackgroundResource(0);
            }

            if (chatLoad.isTyping) {
                getUserTyping().setBackgroundResource(R.drawable.bg_user_typing_h);
            } else {
                getUserTyping().setBackgroundResource(R.drawable.bg_user_typing);
            }
            if (chatLoad.is_locked == 1
                    && !QodemePreferences.getInstance().getQrcode().equals(chatLoad.user_qrcode)) {
                getFavoriteBtn().setClickable(false);
            } else {
                getFavoriteBtn().setClickable(true);
            }
            if (chatLoad.is_deleted == 1) {
                getFavoriteBtn().setClickable(false);
                getSendMessage().setVisibility(View.GONE);
                getMessageEdit().setVisibility(View.GONE);
            }
            if (chatLoad.is_favorite == 1) {
                getFavoriteBtn().setImageResource(R.drawable.ic_chat_favorite);
            } else {
                getFavoriteBtn().setImageResource(R.drawable.ic_chat_favorite_h);
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

        int numLines = getName().getLineCount();
        if (numLines > 0) {
            getName().setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        } else {
            getName().setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        }

        getReplyImg().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    ChatLoad chatLoad = mCallback.getChatLoad(mContact.chatId);
                    if (chatLoad != null && chatLoad.is_locked != 1 && chatLoad.is_deleted != 1)
                        showMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    public ImageView getReplyImg() {
        return imgReplyBottom = imgReplyBottom != null ? imgReplyBottom
                : (ImageView) findViewById(R.id.reply_image);
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
                : findViewById(R.id.linearTyping);
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

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return super.onTouchEvent(e) || gestureDetector.onTouchEvent(e);
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
            Log.i("GestureListener", "onSingleTap");
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            isCancel = true;
            Helper.hideKeyboard(getContext(), getMessageEdit());
            mCallback.onDoubleTap(getView(), mPosition, mContact);
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
        if (lastMessage != null) {
            lastMessage.isVerticleLineHide = false;
        }
        getReplyImg().setVisibility(GONE);
        getSendMessage().setVisibility(View.VISIBLE);
        getMessageTypedView().setVisibility(VISIBLE);
        getMessageEdit().setInputType(
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        getMessageEdit().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    MainActivity activity = (MainActivity) getContext();
                    activity.sendUserTypingMessage(mContact.chatId);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                ChatFocusSaver.setCurrentMessage(mContact.chatId, s.toString());
                if (s.length() > 0) {
                    MainActivity activity = (MainActivity) getContext();
                    activity.sendUserTypingMessage(mContact.chatId);
                } else {
                    MainActivity activity = (MainActivity) getContext();
                    activity.sendUserStoppedTypingMessage(mContact.chatId);
                }
            }
        });

        getMessageEdit().setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    sendMessage();

                    return true;
                }
                return false;
            }
        });

        getMessageEdit().setOnEditTextImeBackListener(new CustomEdit.OnEditTextImeBackListener() {
            @Override
            public void onImeBack(CustomEdit ctrl) {
                if (lastMessage != null) {
                    lastMessage.isVerticleLineHide = true;
                }
                getReplyImg().setVisibility(VISIBLE);
                ChatFocusSaver.setFocusedChatId(0);
                getSendMessage().setVisibility(GONE);
                getMessageEdit().setVisibility(GONE);
                getMessageTypedView().setVisibility(GONE);
            }
        });
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

    One2OneChatListInsideFragmentCallback callbackChatListInsideFragmentCallback = new One2OneChatListInsideFragmentCallback() {

        @Override
        public void stopTypingMessage() {
            MainActivity activity = (MainActivity) getContext();
            activity.sendUserStoppedTypingMessage(mContact.chatId);
        }

        @Override
        public void startTypingMessage() {
            MainActivity activity = (MainActivity) getContext();
            activity.sendUserTypingMessage(mContact.chatId);
        }

        @Override
        public void sendReplyMessage(long messageReplyId, String message, String photoUrl,
                                     int hashPhoto, long replyTo_Id, double latitude, double longitude, String senderName) {
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
            return mCallback.getChatType(chatId);
        }
    };
}
