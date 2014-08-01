package com.blulabellabs.code.ui.one2one;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
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
import com.blulabellabs.code.core.io.utils.RestError;
import com.blulabellabs.code.core.io.utils.RestListener;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.sync.SyncHelper;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.common.CustomDotView;
import com.blulabellabs.code.ui.common.CustomEdit;
import com.blulabellabs.code.ui.common.EditTextPreIme;
import com.blulabellabs.code.ui.common.ExGroupAdapterBasedView;
import com.blulabellabs.code.ui.common.ExtendedListAdapter;
import com.blulabellabs.code.ui.common.ListAdapter;
import com.blulabellabs.code.ui.common.ScrollDisabledListView;
import com.blulabellabs.code.ui.one2one.ChatInsideFragment.One2OneChatListInsideFragmentCallback;
import com.blulabellabs.code.utils.ChatFocusSaver;
import com.blulabellabs.code.utils.Converter;
import com.blulabellabs.code.utils.Helper;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatListGroupItem extends RelativeLayout implements ExGroupAdapterBasedView<ChatLoad, ChatListAdapterCallback> {

    private static final int MIN_CONTENT_SIZE_DP = 100;
    private static final int MAX_CONTENT_SIZE_DP = 350;

    private final int minContentSizePx;
    private final int maxContentSizePx;

    private int _startY;
    private int _yDelta;

    private final Context context;
    private int mPosition;
    private ChatLoad mChatLoad;
    boolean isCancel = false;
    public boolean isScrolling = false;
    private Message lastMessage;
    List<Message> temp = Lists.newArrayList();
    private ChatListAdapterCallback mCallback;

    public TextView name;
    public TextView date;
    public TextView location, mTextViewMembers;
    public ScrollDisabledListView subList;
    public LinearLayout mLinearMemberList;
    private EditTextPreIme editTextTitle;
    public CustomEdit edit;
    public ImageButton shareChatBtn, mImgBtnFavorite;
    public ImageButton sendMessageBtn;
    public ImageButton sendImgMessageBtn;
    public ImageView dragImage, memberListBottomLine;
    public ImageView textViewUserTyping, imgReplyBottom;
    private GestureDetector gestureDetector;
    public RelativeLayout mChatItemChild;
    private RelativeLayout mViewTypedMessage;
    private CustomDotView mTypedMessageDot;
    private View mAnimatedLine;
    boolean haveFocus =false;

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

    public EditTextPreIme getTitleEditText() {
        if (editTextTitle != null)
            return editTextTitle;
        else {
            editTextTitle = (EditTextPreIme) findViewById(R.id.editText_group_title);
            editTextTitle.setParent((MainActivity) context);
            return editTextTitle;
        }
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

    public ImageView getReplyImg() {
        return imgReplyBottom = imgReplyBottom != null ? imgReplyBottom
                : (ImageView) findViewById(R.id.reply_image);
    }

    public View getView() {
        return this;
    }

    public RelativeLayout getMessageTypedView() {
        return mViewTypedMessage = mViewTypedMessage != null ? mViewTypedMessage
                : (RelativeLayout ) findViewById(R.id.layout_message);
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

//    public RelativeLayout getChatItem() {
//        return mChatItem = mChatItem != null ? mChatItem
//                : (RelativeLayout) findViewById(R.id.relative_chatItem);
//    }

    public RelativeLayout getChatItemChild() {
        return mChatItemChild = mChatItemChild != null ? mChatItemChild
                : (RelativeLayout) findViewById(R.id.relative_chatItemChild);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return super.onTouchEvent(e) || gestureDetector.onTouchEvent(e);
    }

    public void fill(ChatListAdapterCallback one2OneAdapterCallback, int position) {
        this.mCallback = one2OneAdapterCallback;
        this.mPosition = position;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            try {
                if (mChatLoad.is_locked != 1 && mChatLoad.is_deleted != 1 && mChatLoad.isCreated)
                    showMessage();
                mCallback.onSingleTap(getView(), mPosition, mChatLoad);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Log.i("GestureListener", "onSingleTap");
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            isCancel = true;
            Helper.hideKeyboard(getContext(), getMessageEdit());
            if (mChatLoad.isCreated)
                mCallback.onDoubleTap(getView(), mPosition, mChatLoad);
            Log.i("GestureListener", "onDoubleTap");
            return true;
        }
    }

    public void showMessage() {
        getMessageTypedView().setVisibility(VISIBLE);

        Animation animationLineIn = AnimationUtils.loadAnimation(getContext(),
                R.anim.line_in);
        if (mAnimatedLine==null) {
            mAnimatedLine = findViewById(R.id.animated_line);
        }
        mAnimatedLine.startAnimation(animationLineIn);
        animationLineIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAnimatedLine.setVisibility(View.VISIBLE);
                showCircleAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void showCircleAnimation() {
        Animation animationCircleGrow = AnimationUtils.loadAnimation(getContext(),
                R.anim.circle_grow);
        getMessageTypedDot().setDotColor(getResources().getColor(R.color.user_typing));
        getMessageTypedDot().startAnimation(animationCircleGrow);
        animationCircleGrow.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                getMessageTypedDot().setVisibility(View.VISIBLE);
                getMessageEdit().setVisibility(View.VISIBLE);
                showMessageAfterAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });
    }


    private void showMessageAfterAnimation() {
        if (lastMessage != null) {
            lastMessage.isVerticleLineHide = false;
        }
        getReplyImg().setVisibility(GONE);
        getSendMessage().setVisibility(View.VISIBLE);
        getList().setSelection(getList().getAdapter().getCount() - 1);
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
                    activity.sendUserTypingMessage(mChatLoad.chatId);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                ChatFocusSaver.setCurrentMessage(mChatLoad.chatId, s.toString());
                if (s.length() > 0) {
                    MainActivity activity = (MainActivity) getContext();
                    activity.sendUserTypingMessage(mChatLoad.chatId);
                } else {
                    MainActivity activity = (MainActivity) getContext();
                    activity.sendUserStoppedTypingMessage(mChatLoad.chatId);
                }
            }
        });

        getMessageEdit().setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
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
                ChatFocusSaver.setFocusedChatId(0);
                getSendMessage().setVisibility(View.GONE);
                getReplyImg().setVisibility(VISIBLE);
                getMessageTypedView().setVisibility(GONE);
                getMessageEdit().setVisibility(View.GONE);
                MainActivity activity = (MainActivity) getContext();
                activity.sendUserStoppedTypingMessage(mChatLoad.chatId);
                getMessageTypedDot().setVisibility(View.INVISIBLE);
                mAnimatedLine.setVisibility(View.GONE);
                haveFocus= false;
            }
        });
        String msg = ChatFocusSaver.getCurrentMessage(mChatLoad.chatId);
        if (!TextUtils.isEmpty(msg)) {
            getMessageEdit().setText(msg);
            getMessageEdit().setSelection(msg.length());
        }

        ChatFocusSaver.setFocusedChatId(mChatLoad.chatId);
        getMessageEdit().setVisibility(VISIBLE);
        getMessageEdit().postDelayed(new Runnable() {
            @Override
            public void run() {
                haveFocus= true;
            }
        }, 500);
        getMessageEdit().setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (haveFocus) {
                    if (!hasFocus) {
                        getMessageTypedDot().setVisibility(View.INVISIBLE);
                        mAnimatedLine.setVisibility(View.GONE);
                        getMessageEdit().setVisibility(View.INVISIBLE);
                        getMessageTypedView().setVisibility(GONE);
                        getReplyImg().setVisibility(VISIBLE);
                        haveFocus= false;
                    }
                }
            }
        });
        getMessageEdit().requestFocus();
        if (!isCancel) {
            Helper.showKeyboard(getContext(), getMessageEdit());
        }
    }

    private void sendMessage() {
        String message = getMessageEdit().getText().toString();
        getView().requestFocus();
        ChatFocusSaver.setFocusedChatId(0);
        getMessageEdit().setVisibility(GONE);
        getMessageEdit().setText("");
        getSendMessage().setVisibility(View.GONE);
        getReplyImg().setVisibility(VISIBLE);
        getMessageTypedView().setVisibility(GONE);
        MainActivity activity = (MainActivity) getContext();
        getMessageTypedDot().setVisibility(View.INVISIBLE);
        mAnimatedLine.setVisibility(View.GONE);
        haveFocus= false;

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
            return mCallback.getImageFetcher();
        }

        @Override
        public int getChatType(long chatId) {
            return mCallback.getChatType(chatId);
        }
    };

    @SuppressWarnings("unchecked")
    @Override
    public void fill(ChatLoad t) {
        getFavoriteBtn().setClickable(true);
        getTitleEditText().setEnabled(true);
        getTitleEditText().setTypeface(Application.typefaceRegular);
        getShareChatBtn().setEnabled(true);
        isCancel = false;
        try {
            mChatLoad = t;
            getUserTyping().setBackgroundResource(mChatLoad.isTyping ? R.drawable.bg_user_typing_h : R.drawable.bg_user_typing);
            getFavoriteBtn().setImageResource(t.is_favorite == 1 ? R.drawable.ic_chat_favorite : R.drawable.ic_chat_favorite_h);
            getFavoriteBtn().setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mChatLoad.isCreated) {
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
                            getFavoriteBtn().setImageResource(is_favorite == 1 ? R.drawable.ic_chat_favorite : R.drawable.ic_chat_favorite_h);
                            String date = Converter.getCurrentGtmTimestampString();
                            RestAsyncHelper.getInstance().setFavorite(date, is_favorite,
                                    mChatLoad.chatId, new RestListener<SetFavoriteResponse>() {
                                        @Override
                                        public void onResponse(SetFavoriteResponse response) {
                                        }

                                        @Override
                                        public void onServiceError(RestError error) {
                                        }
                                    }
                            );
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
                                    }
                            );
                        } else {
                            getContext().getContentResolver().update(QodemeContract.Chats.CONTENT_URI,
                                    QodemeContract.Chats.updateFavorite(is_favorite, num_of_favorite),
                                    QodemeContract.Chats.CHAT_ID + " = " + mChatLoad.chatId, null);
                            SyncHelper.requestManualSync();
                        }
                    }
                }
            });

            if (t.type == 1) {
                getMemberListView().setVisibility(VISIBLE);
                getMemberListBottomLine().setVisibility(VISIBLE);
                String memberNames = "";
                if (t.members != null) {
                    int i = 0;
                    List<String> nameList = new ArrayList<String>();
                    for (String memberQr : t.members) {
                        if (!QodemePreferences.getInstance().getQrcode().equals(memberQr)) {
                            Contact c = mCallback.getContact(memberQr);
                            nameList.add(c != null ? c.title : "User");
                        }
                    }
                    Collections.sort(nameList);
                    for (String memberQr : nameList) {
                        if (i > 5) {
                            memberNames += "...";
                            break;
                        }
                        memberNames += i == 0 ? memberQr + "" : ", " + memberQr + "";
                        i++;
                    }
                }
                getMembersTextView().setText(memberNames);
            }
            if (!mChatLoad.isCreated) {
                if (mChatLoad.title != null && mChatLoad.title.trim().length() > 0) {
                    getTitleEditText().setVisibility(GONE);
                    getName().setVisibility(VISIBLE);
                } else {
                    getTitleEditText().setVisibility(VISIBLE);
                    getName().setVisibility(GONE);
                }
                getTitleEditText().setText(mChatLoad.title);
                getName().setText(mChatLoad.title);
                getTitleEditText().setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                getTitleEditText().setOnEditorActionListener(new OnEditorActionListener() {

                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH
                                || actionId == EditorInfo.IME_ACTION_GO
                                || actionId == EditorInfo.IME_ACTION_SEND
                                || actionId == EditorInfo.IME_ACTION_DONE
                                || actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            String title = v.getText().toString().trim();
                            if (!title.trim().equals("")) {
                                Helper.hideKeyboard(getContext(), getTitleEditText());
                                MainActivity activity = (MainActivity) getContext();
                                mChatLoad.title = title;
                                activity.createChat(title, mChatLoad.type);
                                return true;
                            } else {
                                ((MainActivity) context).deleteChat(mChatLoad);
                            }
                        }
                        return false;
                    }
                });
            } else {
                getTitleEditText().setVisibility(GONE);
                getName().setVisibility(VISIBLE);
                getTitleEditText().setText("");
            }

            final int oponentColor = mChatLoad.color == 0 ? Color.GRAY : mChatLoad.color;
            final int myColor = context.getResources().getColor(R.color.text_chat_name);
            getName().setText(mChatLoad.title != null ? mChatLoad.title : "");
            setCornerColor(mCallback.getNewMessagesCount(mChatLoad.chatId), oponentColor);
            if (!QodemePreferences.getInstance().isSaveLocationDateChecked()) {
                getDate().setText("");
                getLocation().setText("");
            }

            getMessageTypedDot().setDotColor(getResources().getColor(R.color.user_typing));
            getMessageTypedDot().setOutLine(true);
            getMessageTypedDot().setSecondVerticalLine(true);
            getMessageTypedDot().invalidate();

            List<Message> listForAdapter = Lists.newArrayList();
            List<Message> listData = Lists.newArrayList();
            if (mChatLoad.isSearchResult) {
                try {
                    if (mChatLoad.messages != null) {
                        listData = Lists.newArrayList();
                        Collections.addAll(listData, mChatLoad.messages);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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

            final ListAdapter listAdapter = new ExtendedListAdapter<ChatListSubItem, Message, ChatListSubAdapterCallback>(
                    context, R.layout.one2one_chat_list_item_list_item, listForAdapter,
                    new ChatListSubAdapterCallback() {
                        @Override
                        public int getColor(String senderQrcode) {
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
                getChatItemChild().setBackgroundResource(R.drawable.bg_shadow_old);
            else {
                getChatItemChild().setBackgroundColor(Color.WHITE);
            }
            if (mChatLoad.color != 0 && mChatLoad.color != -1)
                getChatItemChild().setBackgroundColor(mChatLoad.color);
            else {
                getChatItemChild().setBackgroundResource(0);
            }
            if (!isScrolling) {
                if (listData != null) {
                    if (listData.size() > 0) {
                        lastMessage = temp.get(temp.size() - 1);
                        lastMessage.chatColor = mChatLoad.color;
                    }
                    listAdapter.addAll(temp);
                }
            } else {
                if (listData != null) {
                    if (listData.size() > 0) {
                        lastMessage = temp.get(temp.size() - 1);
                        lastMessage.chatColor = mChatLoad.color;
                    }
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
            getList().setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });

            int height = mCallback.getChatHeight(mChatLoad.chatId);
            ListView.LayoutParams lParams = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, height);
            setLayoutParams(lParams);
            getDragImage().setOnTouchListener(new OnTouchListener() {

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
                getFavoriteBtn().setClickable(true);
                getTitleEditText().setEnabled(true);
            }
            if (mChatLoad != null && mChatLoad.is_deleted == 1) {
                getSendImage().setVisibility(View.GONE);
                getFavoriteBtn().setClickable(false);
                getTitleEditText().setEnabled(false);
                getSendMessage().setVisibility(View.GONE);
                getReplyImg().setVisibility(GONE);
                getMessageTypedView().setVisibility(GONE);
                getMessageEdit().setVisibility(View.GONE);
                getShareChatBtn().setEnabled(false);
            }
            getSendImage().setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mChatLoad.isCreated) {
                        MainActivity activity = (MainActivity) v.getContext();
                        activity.setCurrentChatId(mChatLoad.chatId);
                        activity.takePhoto();
                    }
                }
            });

            getShareChatBtn().setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mChatLoad.isCreated) {
                        MainActivity activity = (MainActivity) v.getContext();
                        activity.setCurrentChatId(mChatLoad.chatId);
                        activity.addMemberInExistingChat();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        openKeyboadOnTitle();

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
                    if (mChatLoad.is_locked != 1 && mChatLoad.is_deleted != 1 && mChatLoad.isCreated)
                        showMessage();
                    mCallback.onSingleTap(getView(), mPosition, mChatLoad);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void openKeyboadOnTitle() {
        MainActivity activity = (MainActivity) getContext();
        if (getTitleEditText().getVisibility() == View.VISIBLE
                && activity.getSupportActionBar().isShowing()) {
            getTitleEditText().requestFocus();
            getTitleEditText().post(new Runnable() {

                @Override
                public void run() {
                    getTitleEditText().requestFocus();
                    Helper.showKeyboard(getContext(), getTitleEditText());
                }
            });
        }
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
