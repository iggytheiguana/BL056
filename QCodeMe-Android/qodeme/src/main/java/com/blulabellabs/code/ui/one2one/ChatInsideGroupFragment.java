package com.blulabellabs.code.ui.one2one;

import android.app.Activity;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

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
import com.blulabellabs.code.ui.common.CustomEdit;
import com.blulabellabs.code.ui.common.ExtendedGroupListAdapter;
import com.blulabellabs.code.ui.one2one.ChatInsideFragment.One2OneChatInsideFragmentCallback;
import com.blulabellabs.code.utils.Converter;
import com.blulabellabs.code.utils.DbUtils;
import com.blulabellabs.code.utils.Helper;
import com.google.common.collect.Lists;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class ChatInsideGroupFragment extends Fragment {

    private static final String CHAT_ID = "chat_id";
    private static final String CHAT_COLOR = "chat_color";
    private static final String QRCODE = "contact_qr";
    private static final String LOCATION = "location";
    private static final String STATUS = "status";
    private static final String DESCRIPTION = "description";
    private final WebSocketConnection mConnection = new WebSocketConnection();
    private static final String TAG = "ChatInsideFragment";

    private One2OneChatInsideFragmentCallback callback;
    private boolean isViewCreated;
    private ListView mListView;
    private ExtendedGroupListAdapter mListAdapter;
    private GestureDetector mGestureDetector;
    private ImageButton mSendButton, mBtnImageSend, mImgFavorite;
    private EditText mStatusField;
    private CustomEdit mMessageField;
    private TextView mName, mStatus, mStatusUpdate;
    private TextView mDate;
    private TextView mLocation;
    private LinearLayout mLinearLayStatusUpdte;
    private LinearLayout mLinearMessage;
    private TextView mTextViewMembers, mTextViewMembersLabel, mTextViewNumFavorite,
            mTextViewDeleteBaner;
    private ImageView mImgMemberBottomLine;

    private CustomDotView customDotViewUserTyping;
    private View footerView, footerView1;
    private boolean isUsertyping = false;
    private ChatLoad chatLoad;
    CustomDotView customDotView;
    List<Long> idList = Lists.newArrayList();
    private ImageView imageViewReply;
    private Message lastMessage;

    public static ChatInsideGroupFragment newInstance(ChatLoad c) {
        ChatInsideGroupFragment f = new ChatInsideGroupFragment();
        Bundle args = new Bundle();
        args.putLong(CHAT_ID, c.chatId);
        args.putInt(CHAT_COLOR, c.color);
        args.putString(QRCODE, c.qrcode);
        args.putString(STATUS, c.status);
        args.putString(DESCRIPTION, c.description);
        f.setArguments(args);
        f.setChatLoad(c);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
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

        imageViewReply = (ImageView) getView().findViewById(R.id.reply_image);

        customDotView = (CustomDotView) getView().findViewById(R.id.dotView_reply);
        customDotView.setDotColor(getResources().getColor(R.color.user_typing));
        customDotView.setOutLine(true);
        customDotView.setSecondVerticalLine(true);
        customDotView.invalidate();

        mImgFavorite.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int is_favorite;
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
                        mImgFavorite.setImageResource(is_favorite == 1 ? R.drawable.ic_chat_favorite : R.drawable.ic_chat_favorite_h);
                        String date = Converter.getCurrentGtmTimestampString();
                        RestAsyncHelper.getInstance().setFavorite(date, is_favorite,
                                chatLoad.chatId, new RestListener<SetFavoriteResponse>() {
                                    @Override
                                    public void onResponse(SetFavoriteResponse response) {
                                    }

                                    @Override
                                    public void onServiceError(RestError error) {
                                    }
                                }
                        );
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
                                }
                        );
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
                            || actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                            || event.getAction() == KeyEvent.ACTION_DOWN
                            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                        mStatusField.setVisibility(View.GONE);
                        mStatus.setVisibility(View.VISIBLE);
                        String status = v.getText().toString().trim();
                        mStatus.setText(status);
                        callback.sendMessage(chatLoad.chatId, status, "", 2, -1, 0, 0,
                                QodemePreferences.getInstance().getPublicName(), "");
                        int updated = chatLoad.updated;
                        getActivity().getContentResolver().update(QodemeContract.Chats.CONTENT_URI,
                                QodemeContract.Chats.updateChatInfoValues("", -1, "", 0, status,
                                        "", updated, 4), QodemeContract.Chats.CHAT_ID + "=?",
                                DbUtils.getWhereArgsForId(chatLoad.chatId)
                        );
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
        mLinearMessage = (LinearLayout) getView().findViewById(R.id.linearTyping);
        imageViewReply.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (getChatLoad() == null || getChatLoad().is_locked != 1 || QodemePreferences.getInstance().getQrcode().equals(getChatLoad().user_qrcode)) {
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            mMessageField.post(new Runnable() {
                @Override
                public void run() {
                    mMessageField.requestFocus();
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
                }
        );
    }

    private void initSendMessage() {
        mBtnImageSend = (ImageButton) getView().findViewById(R.id.btn_camera);
        mSendButton = (ImageButton) getView().findViewById(R.id.button_message);
        ImageButton mBtnImageSendBottom = (ImageButton) getView().findViewById(R.id.imageButton_imgMessage);
        mMessageField = (CustomEdit) getView().findViewById(R.id.edit_message);
        mMessageField.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

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
        mMessageField.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Helper.showKeyboard(getActivity(), mMessageField);
                return false;
            }
        });

        mMessageField.setOnEditorActionListener(new OnEditorActionListener() {

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
                    footerView1.setVisibility(View.VISIBLE);
                    sendUserTypingMessage();
                } else {
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
        }
    };
    protected int lastFirstVisibleItem = 0;

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
            if (getChatLoad().type != 2) {
                customDotViewUserTyping.setVisibility(View.VISIBLE);
                footerView.setVisibility(View.VISIBLE);
                if (!isUsertyping) {
                    handlerForUserTyping.sendEmptyMessageDelayed(0, 500);
                    isUsertyping = true;
                }
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
        mMessageField.getText().clear();
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
        mListView = (ListView) getView().findViewById(R.id.listview);
        mDate = (TextView) getView().findViewById(R.id.date);
        mLocation = (TextView) getView().findViewById(R.id.location);
        View view = getActivity().getLayoutInflater().inflate(R.layout.footer_user_typing, null);
        CustomDotView dotView = (CustomDotView) view.findViewById(R.id.dotView_userTyping1);
        footerView = view.findViewById(R.id.linearFooter_userTyping);
        customDotViewUserTyping = (CustomDotView) footerView.findViewById(R.id.dotView_userTyping);
        footerView.setVisibility(View.GONE);
        footerView1 = view.findViewById(R.id.linearTyping);
        footerView1.setVisibility(View.GONE);
        dotView.setDotColor(getResources().getColor(R.color.user_typing));
        dotView.setOutLine(true);
        dotView.setSecondVerticalLine(true);
        dotView.invalidate();
        customDotViewUserTyping.setDotColor(getResources().getColor(R.color.user_typing));
        customDotViewUserTyping.setSecondVerticalLine(true);
        customDotViewUserTyping.invalidate();

        List<Message> listForAdapter = Lists.newArrayList();
        mListAdapter = new ExtendedGroupListAdapter (getActivity(), R.layout.group_chat_list_item_list_item, listForAdapter,

                new ChatListSubAdapterCallback() {

                    @Override
                    public int getColor(String senderQrcode) {
                        return TextUtils.equals(senderQr, senderQrcode) ? oponentColor : myColor;
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
        mListView.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE)
                    handler.sendEmptyMessage(0);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {

                if (lastFirstVisibleItem != firstVisibleItem) {
                    if (lastFirstVisibleItem > firstVisibleItem) {
                        try {
                            for (int i = mListView.getLastVisiblePosition(); i < totalItemCount - 1; i++) {
                                if (mListAdapter.getItem(i).state == QodemeContract.Messages.State.NOT_READ) {
                                    mListAdapter.getItem(i).state = QodemeContract.Messages.State.READ_LOCAL;
                                    idList.add(mListAdapter.getItem(i)._id);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            for (int i = mListView.getLastVisiblePosition(); i < totalItemCount - 1; i++) {
                                if (mListAdapter.getItem(i).state == QodemeContract.Messages.State.NOT_READ) {
                                    mListAdapter.getItem(i).state = QodemeContract.Messages.State.READ_LOCAL;
                                    idList.add(mListAdapter.getItem(i)._id);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                lastFirstVisibleItem = firstVisibleItem;
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            new ReadMessage().execute("");
        }
    };

    class ReadMessage extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            for (long id : idList) {
                ContentValues selectionArgs = new ContentValues();
                selectionArgs.put(QodemeContract.SyncColumns.UPDATED, QodemeContract.Sync.UPDATED);
                selectionArgs.put(QodemeContract.Messages.MESSAGE_STATE,
                        QodemeContract.Messages.State.READ_LOCAL);
                getActivity().getContentResolver().update(QodemeContract.Messages.CONTENT_URI,
                        selectionArgs, QodemeContract.Messages._ID + "=" + id, null);
            }
            idList.clear();
            return null;
        }

    }

    public long getChatId() {
        return getArguments().getLong(CHAT_ID, 0L);
    }

    public int getChatColor() {
        return getArguments().getInt(CHAT_COLOR, 0);
    }

    public String getSenderQr() {
        return getArguments().getString(QRCODE);
    }

    public void updateUi() {
        if (isViewCreated) {
            customDotView.setDotColor(getResources().getColor(R.color.user_typing));
            customDotView.setOutLine(true);
            customDotView.invalidate();

            mImgFavorite.setImageResource(chatLoad.is_favorite == 1 ? R.drawable.ic_chat_favorite : R.drawable.ic_chat_favorite_h);
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
                        List<String> nameList = new ArrayList<String>();
                        for (String memberQr : getChatLoad().members) {
                            if (!QodemePreferences.getInstance().getQrcode().equals(memberQr)) {
                                Contact c = callback.getContact(memberQr);
                                nameList.add(c != null ? c.title : "User");
                            }
                        }
                        Collections.sort(nameList);
                        for (String memberQr : nameList) {
                            if (i > 5) {
                                memberNames += "...";
                                break;
                            }
                            if (i == 0)
                                memberNames += memberQr + "";
                            else
                                memberNames += ", " + memberQr + "";
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
            }
            mListAdapter.clearViews();
            if (getChatLoad().isSearchResult) {
                if (getChatLoad().messages != null) {
                    List<Message> listData = Lists.newArrayList();
                    Collections.addAll(listData, getChatLoad().messages);
                    listData = sortMessages(listData);
                    mListAdapter.addAll(listData);
                    if (listData.size() > 0) {
                        lastMessage = listData.get(listData.size() - 1);
                    }
                }
            } else {
                List<Message> listData = callback.getChatMessages(getChatId());
                mListAdapter.addAll(listData);
                if (listData != null && listData.size() > 0) {
                    lastMessage = listData.get(listData.size() - 1);
                }
            }
            mName.setText(chatLoad.title);
            mStatus.setText(chatLoad.status);
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
            if (getChatLoad() != null
                    && getChatLoad().is_locked == 1
                    && !QodemePreferences.getInstance().getQrcode()
                    .equals(getChatLoad().user_qrcode)) {
                mBtnImageSend.setVisibility(View.INVISIBLE);
            } else {
                mBtnImageSend.setVisibility(View.VISIBLE);
            }
            if (chatLoad.is_deleted == 1) {
                mTextViewDeleteBaner.setVisibility(View.VISIBLE);
                mBtnImageSend.setVisibility(View.INVISIBLE);
                mSendButton.setVisibility(View.INVISIBLE);
                mMessageField.setVisibility(View.INVISIBLE);
                mImgFavorite.setClickable(false);
            }
            if (chatLoad == null || chatLoad.status == null || chatLoad.status.trim().equals("")) {
                mStatus.setVisibility(View.GONE);
                getView().findViewById(R.id.img_statusline).setVisibility(View.GONE);
            } else {
                mStatus.setVisibility(View.VISIBLE);
                getView().findViewById(R.id.img_statusline).setVisibility(View.VISIBLE);
            }
            mListView.post(new Runnable() {
                @Override
                public void run() {
                    mListView.setSelectionFromTop(mListAdapter.getCount() - 1, -100000 - mListView.getPaddingTop());
                }
            });
        }
    }

    private List<Message> sortMessages(List<Message> messages) {
        if (messages != null) {
            Collections.sort(messages, new Comparator<Message>() {
                @Override
                public int compare(Message u1, Message u2) {
                    u1.timeStamp = Converter.getCrurentTimeFromTimestamp(u1.created);
                    u2.timeStamp = Converter.getCrurentTimeFromTimestamp(u2.created);
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
            try {
                Helper.hideKeyboard(getActivity(), mMessageField);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            try {
                Helper.hideKeyboard(getActivity(), mMessageField);
                sendUserStoppedTypingMessage();
                getActivity().onBackPressed();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return true;
        }
    }

//    public void setArgument(ChatLoad c) {
//
//        this.chatLoad = c;
//    }

    private String getDate() {
        return null;// getArguments().getString(DATE);
    }

    private String getLocation() {
        return getArguments().getString(LOCATION);
    }
//
//    private boolean getFirstUpdate() {
//        boolean result = getArguments().getBoolean(FIRST_UPDATE) & mFirstUpdate;
//        mFirstUpdate = false;
//        return result;
//    }

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
            if (TextUtils.isEmpty(message) || TextUtils.isEmpty(message.trim())) {
                Toast.makeText(getActivity(), "Empty message can not be sent", Toast.LENGTH_SHORT).show();
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
