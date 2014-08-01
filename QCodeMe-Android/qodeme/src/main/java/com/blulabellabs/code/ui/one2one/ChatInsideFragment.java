package com.blulabellabs.code.ui.one2one;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blulabellabs.code.ApplicationConstants;
import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.sync.SyncHelper;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.common.CustomDotView;
import com.blulabellabs.code.ui.common.CustomEdit;
import com.blulabellabs.code.ui.common.ExtendedListAdapter;
import com.blulabellabs.code.utils.ChatFocusSaver;
import com.blulabellabs.code.utils.Converter;
import com.blulabellabs.code.utils.Helper;
import com.google.common.collect.Lists;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class ChatInsideFragment extends Fragment {

    private static final String CHAT_ID = "chat_id";
    private static final String CHAT_COLOR = "chat_color";
    private static final String CHAT_NAME = "chat_name";
    private static final String QRCODE = "contact_qr";
    private static final String LOCATION = "location";
    private static final String DATE = "date";

    private final WebSocketConnection mConnection = new WebSocketConnection();
    private static final String TAG = "ChatInsideFragment";
    private One2OneChatInsideFragmentCallback callback;
    private boolean isViewCreated;
    private ListView mListView;
    private ExtendedListAdapter<ChatListSubItem, Message, ChatListSubAdapterCallback> mListAdapter;
    private GestureDetector mGestureDetector;
    private ImageButton mSendButton, mBtnImageSend, mBtnImageSendBottom, mImgFavorite;
    private CustomEdit mMessageField;
    private TextView mName, mStatus, mStatusUpdate;
    private TextView mDate;
    private TextView mLocation;
    private LinearLayout mLinearLayStatusUpdte;
    private TextView mTextViewDeleteBaner;

    private ImageView imgUserTyping;
    private CustomDotView customDotViewUserTyping;
    private View footerView, footerView1;
    private boolean isUsertyping = false;
    private Message lastMessage;
    CustomDotView customDotView;
    private ImageView imageViewReply;
    private LinearLayout mLinearMessage;

    public static ChatInsideFragment newInstance(Contact c) {
        ChatInsideFragment f = new ChatInsideFragment();
        Bundle args = new Bundle();
        args.putLong(CHAT_ID, c.chatId);
        args.putInt(CHAT_COLOR, c.color);
        args.putString(CHAT_NAME, c.title);
        args.putString(QRCODE, c.qrCode);
        args.putString(LOCATION, c.location);
        args.putString(DATE, c.date);
        f.setArguments(args);
        return f;
    }

    public interface One2OneChatInsideFragmentCallback {

        List<Message> getChatMessages(long chatId);

        void sendMessage(long chatId, String message, String photoUrl, int hashPhoto,
                         long replyTo_Id, double latitude, double longitude, String senderName, String localUrl);

        ImageFetcher getImageFetcher();

        int getChatType(long chatId);

        Contact getContact(String qrCode);

        ChatLoad getChatLoad(long chatId);

    }

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
        isViewCreated = true;
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());
        mName = (TextView) getView().findViewById(R.id.name);
        mStatus = (TextView) getView().findViewById(R.id.textView_status);
        mStatusUpdate = (TextView) getView().findViewById(R.id.textView_status_update);
        mLinearMessage = (LinearLayout) getView().findViewById(R.id.linearTyping);
        mImgFavorite = (ImageButton) getView().findViewById(R.id.btnFavorite);
        mTextViewDeleteBaner = (TextView) getView().findViewById(R.id.textView_deleteBanner);
        imageViewReply = (ImageView) getView().findViewById(R.id.reply_image);
        customDotView = (CustomDotView) getView().findViewById(R.id.dotView_reply);
        mLinearLayStatusUpdte = (LinearLayout) getView().findViewById(R.id.linear_status_update);
        imgUserTyping = (ImageView) getView().findViewById(R.id.img_typing);
        mListView = (ListView) getView().findViewById(R.id.listview);
        mDate = (TextView) getView().findViewById(R.id.date);
        mLocation = (TextView) getView().findViewById(R.id.location);
        mBtnImageSend = (ImageButton) getView().findViewById(R.id.btn_camera);
        mSendButton = (ImageButton) getView().findViewById(R.id.button_message);
        mBtnImageSendBottom = (ImageButton) getView().findViewById(R.id.imageButton_imgMessage);
        mMessageField = (CustomEdit) getView().findViewById(R.id.edit_message);

        initListView();
        initSendMessage();
        mStatus.setVisibility(View.GONE);
        getView().findViewById(R.id.img_statusline).setVisibility(View.GONE);
        getView().findViewById(R.id.img_memberline).setVisibility(View.GONE);
        customDotView.setDotColor(getResources().getColor(R.color.user_typing));
        customDotView.setOutLine(true);
        customDotView.setSecondVerticalLine(true);
        customDotView.invalidate();
        mImgFavorite.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int is_favorite;
                MainActivity activity = (MainActivity) getActivity();
                ChatLoad chatLoad = activity.getChatLoad(getArguments().getLong(CHAT_ID));
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
                    getActivity().getContentResolver().update(QodemeContract.Chats.CONTENT_URI,
                            QodemeContract.Chats.updateFavorite(is_favorite, num_of_favorite),
                            QodemeContract.Chats.CHAT_ID + " = " + chatLoad.chatId, null);
                    SyncHelper.requestManualSync();
                }
            }
        });
        updateUi();
        imageViewReply.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (lastMessage != null) {
                    lastMessage.isVerticleLineHide = false;
                }
                mLinearMessage.setVisibility(View.VISIBLE);
                mMessageField.setVisibility(View.VISIBLE);
                mSendButton.setVisibility(View.VISIBLE);
                imageViewReply.setVisibility(View.GONE);
                MainActivity.isKeyboardVisible = true;
                mMessageField.post(new Runnable() {
                    @Override
                    public void run() {
                        mMessageField.requestFocus();
                        Helper.showKeyboard(getActivity(), mMessageField);
                    }
                });
                mMessageField.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mMessageField.requestFocus();
                    }
                }, 1000);
            }
        });

        mMessageField.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    footerView1.setVisibility(View.GONE);
                    sendMessage();
                    return true;
                }
                return false;
            }
        });

        mMessageField.setOnEditTextImeBackListener(new CustomEdit.OnEditTextImeBackListener() {
            @Override
            public void onImeBack(CustomEdit ctrl) {
                MainActivity.isKeyboardVisible = false;
                if (lastMessage != null) {
                    lastMessage.isVerticleLineHide = true;
                }
                mLinearMessage.setVisibility(View.INVISIBLE);
                mMessageField.setVisibility(View.INVISIBLE);
                mSendButton.setVisibility(View.INVISIBLE);
                imageViewReply.setVisibility(View.VISIBLE);
            }
        });
        if (MainActivity.isKeyboardHide) {
            MainActivity.isKeyboardHide = false;
            Helper.hideKeyboard(getActivity(), mMessageField);
        } else {
            if (MainActivity.isKeyboardVisible) {
                mLinearMessage.setVisibility(View.VISIBLE);
                mMessageField.setVisibility(View.VISIBLE);
                mSendButton.setVisibility(View.VISIBLE);
                imageViewReply.setVisibility(View.GONE);
                mMessageField.post(new Runnable() {
                    @Override
                    public void run() {
                        mMessageField.requestFocus();
                    }
                });
            }
        }
    }

    private void initSendMessage() {
        mMessageField.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
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
        mMessageField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    Log.d("CHATINSIDE", "user stopped typing");
                    sendUserStoppedTypingMessage();
                }
            }
        });

        mMessageField.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    footerView1.setVisibility(View.GONE);
                    sendMessage();
                    return true;
                }
                return false;
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
                    sendUserTypingMessage();
                    footerView1.setVisibility(View.VISIBLE);
                } else {
                    mSendButton.setVisibility(View.GONE);
                    footerView1.setVisibility(View.GONE);
                    sendUserStoppedTypingMessage();
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
        }
    };

    private void sendRegisterForChatEvents() {
        String activityName = "sendRegisterForChatEvents:";
        if (mConnection.isConnected()) {
            long chatId = getChatId();
            String restToken = QodemePreferences.getInstance().getRestToken();
            int event = GetEventForChatEvents();
            Log.d(TAG, activityName + "Sending register for chat event message...");
            sendWebSocketMessageWith(chatId, restToken, event);
        }
    }

    private void receiveWebSocketMessageWith(String message) {
        String activityName = "receiveWebSocketMessageWith:";
        try {
            JSONObject messageJson = new JSONObject(message);
            long chatId = messageJson.getLong("chatId");
            int event = messageJson.getInt("event");
            Log.d(TAG, activityName + "Received event: " + event + " in chat: " + chatId);
            if (event == GetEventForUserStartedTypingMessage()) {
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
            customDotViewUserTyping.setVisibility(View.INVISIBLE);
            footerView.setVisibility(View.GONE);
            isUsertyping = false;
        }
    }

    private void receiveOtherUserStartedTypingEvent(long chatId) {
        if (chatId == getChatId()) {
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
            long chatId = getChatId();
            String restToken = QodemePreferences.getInstance().getRestToken();
            int event = GetEventForUserStoppedTypingMessage();
            Log.d(TAG, activityName + "Sending user stopped typing message...");
            sendWebSocketMessageWith(chatId, restToken, event);
        }
    }

    private void sendUserTypingMessage() {
        String activityName = "sendUserTypingMessage:";
        if (mConnection.isConnected()) {
            long chatId = getChatId();
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
        sendUserStoppedTypingMessage();
        mMessageField.setText("");
        callback.sendMessage(getChatId(), message, "", 0, -1, 0, 0, "", "");
        mMessageField.post(new Runnable() {
            @Override
            public void run() {
                mMessageField.requestFocus();
            }
        });
    }

    public void sendImageMessage(String message) {
        sendUserStoppedTypingMessage();
        mMessageField.setText("");
        callback.sendMessage(getChatId(), "", "", 1, -1, 0, 0, "", message);
        mMessageField.post(new Runnable() {
            @Override
            public void run() {
                mMessageField.requestFocus();
            }
        });
    }

    private void initListView() {

        final String senderQr = getSenderQr();
        final int oponentColor = getChatColor() == 0 ? Color.GRAY : getChatColor();
        final int myColor = getActivity().getResources().getColor(R.color.text_chat_name);

        View view = getActivity().getLayoutInflater().inflate(R.layout.footer_user_typing, null);
        Bitmap bmp = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(oponentColor);

        Canvas c = new Canvas(bmp);
        c.drawCircle(20, 20, 20, paint);
        imgUserTyping.setImageBitmap(bmp);
        imgUserTyping.setVisibility(View.GONE);

        footerView = view.findViewById(R.id.linearFooter_userTyping);
        footerView1 = view.findViewById(R.id.linearTyping);
        customDotViewUserTyping = (CustomDotView) footerView.findViewById(R.id.dotView_userTyping);

        footerView.setVisibility(View.GONE);
        footerView1.setVisibility(View.GONE);
        CustomDotView dotView = (CustomDotView) view.findViewById(R.id.dotView_userTyping1);
        dotView.setDotColor(getResources().getColor(R.color.user_typing));
        dotView.setOutLine(true);
        dotView.setSecondVerticalLine(true);
        dotView.invalidate();

        customDotViewUserTyping.setDotColor(getResources().getColor(R.color.user_typing));
        customDotViewUserTyping.setSecondVerticalLine(true);
        customDotViewUserTyping.invalidate();

        List<Message> listForAdapter = Lists.newArrayList();
        mListAdapter = new ExtendedListAdapter<ChatListSubItem, Message, ChatListSubAdapterCallback>(
                getActivity(), R.layout.one2one_chat_list_item_list_item, listForAdapter,
                new ChatListSubAdapterCallback() {

                    @Override
                    public int getColor(String senderQrcode) {
                        if (TextUtils.equals(senderQr, senderQrcode))
                            return oponentColor;
                        else
                            return myColor;
                    }

                    @Override
                    public Contact getContact(String senderQrcode) {
                        return callback.getContact(senderQrcode);
                    }

                    @Override
                    public ChatLoad getChatLoad(long chatId) {
                        return callback.getChatLoad(chatId);
                    }

                }, chatListInsideFragmentCallback
        );

        mListView.setAdapter(mListAdapter);

        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
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
        return getArguments().getString(CHAT_NAME);
    }

    public String getSenderQr() {
        return getArguments().getString(QRCODE);
    }

    /**
     * Refresh data can be called from activity
     */
    public void updateUi() {
        if (isViewCreated) {
            String statusUpdate = QodemePreferences.getInstance().get(
                    "" + getArguments().getLong(CHAT_ID), "");
            if (statusUpdate.equals("")) {
                mLinearLayStatusUpdte.setVisibility(View.GONE);
            } else {
                mLinearLayStatusUpdte.setVisibility(View.VISIBLE);
                mStatusUpdate.setText(statusUpdate);
            }
            List<Message> listData = callback.getChatMessages(getChatId());
            mListAdapter.clear();
            mListAdapter.addAll(callback.getChatMessages(getChatId()));
            if (listData != null && listData.size() > 0) {
                lastMessage = listData.get(listData.size() - 1);
            }
            mName.setText(getChatName());
            MainActivity activity = (MainActivity) callback;
            ChatLoad chatLoad = activity.getChatLoad(getChatId());
            if (chatLoad != null) {
                mStatus.setText(chatLoad.status);
                mImgFavorite.setImageResource(chatLoad.is_favorite == 1 ? R.drawable.ic_chat_favorite : R.drawable.ic_chat_favorite_h);
                if (chatLoad.is_deleted == 1) {
                    mTextViewDeleteBaner.setVisibility(View.VISIBLE);
                    mImgFavorite.setClickable(false);
                    mSendButton.setVisibility(View.INVISIBLE);
                    mMessageField.setVisibility(View.INVISIBLE);
                    mBtnImageSend.setVisibility(View.INVISIBLE);
                    mBtnImageSendBottom.setVisibility(View.INVISIBLE);
                }
            }
            if (QodemePreferences.getInstance().isSaveLocationDateChecked()) {
                if (getDate() != null) {
                    SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yy HH:mm a");
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
            mMessageField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    ChatFocusSaver.setCurrentMessage(getChatId(), s.toString());
                }
            });
        }
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

    private String getDate() {
        return getArguments().getString(DATE);
    }

    private String getLocation() {
        return getArguments().getString(LOCATION);
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
        public void sendReplyMessage(long messageReplyId, String message, String photoUrl,
                                     int hashPhoto, long replyTo_Id, double latitude, double longitude, String senderName) {
            if (TextUtils.isEmpty(message) || TextUtils.isEmpty(message.trim())) {
                Toast.makeText(getActivity(), "Empty message can't be sent", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            sendUserStoppedTypingMessage();
            callback.sendMessage(getChatId(), message, photoUrl, hashPhoto, replyTo_Id, latitude,
                    longitude, senderName, "");
        }

        @Override
        public ImageFetcher getImageFetcher() {
            return getFetcher();
        }

        @Override
        public int getChatType(long chatId) {
            return getChatTypeFromMain(chatId);
        }
    };

    public ImageFetcher getFetcher() {
        return callback.getImageFetcher();
    }

    int getChatTypeFromMain(long chatId) {
        return callback.getChatType(chatId);
    }

}
