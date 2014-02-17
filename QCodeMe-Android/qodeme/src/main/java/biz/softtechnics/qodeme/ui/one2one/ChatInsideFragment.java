package biz.softtechnics.qodeme.ui.one2one;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.core.io.model.Message;
import biz.softtechnics.qodeme.ui.common.ExtendedListAdapter;
import biz.softtechnics.qodeme.ui.common.ScrollDisabledListView;
import biz.softtechnics.qodeme.utils.ChatFocusSaver;
import biz.softtechnics.qodeme.utils.Fonts;
import biz.softtechnics.qodeme.utils.Helper;

/**
 * Created by Alex on 10/7/13.
 */
public class ChatInsideFragment extends Fragment {

    private static final String CHAT_ID = "chat_id";
    private static final String CHAT_COLOR = "chat_color";
    private static final String CHAT_NAME = "chat_name";
    private static final String QRCODE = "contact_qr";
    private static final String LOCATION = "location";
    private static final String DATE = "date";
    private static final String FIRST_UPDATE = "first_update";


    private One2OneChatInsideFragmentCallback callback;
    private boolean isViewCreated;
    private ScrollDisabledListView mListView;
    private ExtendedListAdapter<ChatListSubItem, Message, ChatListSubAdapterCallback> mListAdapter;
    private GestureDetector mGestureDetector;
    private ImageButton mSendButton;
    private EditText mMessageField;
    private TextView mName;
    private TextView mDate;
    private TextView mLocation;
    private boolean mFirstUpdate = true;

    public static ChatInsideFragment newInstance(Contact c, boolean firstUpdate) {
        ChatInsideFragment f = new ChatInsideFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putLong(CHAT_ID, c.chatId);
        args.putInt(CHAT_COLOR, c.color);
        args.putString(CHAT_NAME, c.title);
        args.putString(QRCODE, c.qrCode);
        args.putString(LOCATION, c.location);
        args.putString(DATE, c.date);
        args.putBoolean(FIRST_UPDATE, firstUpdate);
        f.setArguments(args);
        return f;
    }

    public interface One2OneChatInsideFragmentCallback {
        List<Message> getChatMessages(long chatId);
        void sendMessage(long chatId, String message);
        Typeface getFont(Fonts font);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (One2OneChatInsideFragmentCallback)activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_one2one_chat, null);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());
        initListView();
        isViewCreated = true;

        initSendMessage();
        mName = (TextView) getView().findViewById(R.id.name);
        updateUi();


    }


    private void initSendMessage() {
        mSendButton = (ImageButton) getView().findViewById(R.id.button_message);
        mMessageField = (EditText) getView().findViewById(R.id.edit_message);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        /*mMessageField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == event.KEYCODE_ENTER) {
                    if (keyCode == event.KEYCODE_ENTER) {
                        sendMessage();
                        return true;
                    }
                }
                return false;
            }
        });*/

        mMessageField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    mSendButton.setVisibility(View.VISIBLE);
                } else {
                    mSendButton.setVisibility(View.GONE);
                }
            }
        });

    } 
    
    private void sendMessage(){
        String message = mMessageField.getText().toString();
        if (TextUtils.isEmpty(message) || TextUtils.isEmpty(message.trim())) {
            Toast.makeText(getActivity(), "Empty message can't be sent", Toast.LENGTH_SHORT).show();
            return;
        }
        mMessageField.setText("");
//        Helper.hideKeyboard(getActivity(), mMessageField);
        callback.sendMessage(getChatId(), message);
        mMessageField.post(new Runnable() {
            @Override
            public void run() {
                mMessageField.requestFocus();
                //Helper.showKeyboard(getActivity(), mMessageField);
            }
        });
    }


    private void initListView(){

        //final String myQrCOde = QodemePreferences.getInstance().getQrcode();
        final String senderQr = getSenderQr();
        final int oponentColor = getChatColor() == 0 ? Color.GRAY : getChatColor();
        final int myColor = getActivity().getResources().getColor(R.color.text_chat_name);

        mListView = (ScrollDisabledListView) getView().findViewById(R.id.listview);
        mDate = (TextView) getView().findViewById(R.id.date);
        mLocation = (TextView) getView().findViewById(R.id.location);
        List<Message> listForAdapter = Lists.newArrayList();
        mListAdapter = new ExtendedListAdapter<ChatListSubItem, Message, ChatListSubAdapterCallback>(getActivity(), R.layout.one2one_chat_list_item_list_item, listForAdapter, new ChatListSubAdapterCallback() {

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

        });

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

    public long getChatId(){
        return getArguments().getLong(CHAT_ID, 0L);
    }

    public int getChatColor(){
        return getArguments().getInt(CHAT_COLOR, 0);
    }

    public String getChatName(){
        return getArguments().getString(CHAT_NAME);
    }

    public String getSenderQr(){
        return getArguments().getString(QRCODE);
    }

    /**
     * Refresh data
     * can be called from activity
     */
    public void updateUi(){
        if (isViewCreated) {
            mListAdapter.clear();
            mListAdapter.addAll(callback.getChatMessages(getChatId()));
            mName.setText(getChatName());
            mName.setTextColor(getChatColor());
            if (QodemePreferences.getInstance().isSaveLocationDateChecked()){
                if (getDate() != null){
                    SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yy HH:mm");
                    String dateStr = fmtOut.format(new Date(Timestamp.valueOf(getDate()).getTime()));

                    mDate.setText(dateStr + ",");
                } else
                    mDate.setText("");
                mLocation.setText(getLocation());
            } else {
                mDate.setText("");
                mLocation.setText("");
            }

            String message = ChatFocusSaver.getCurrentMessage(getChatId());

            if (!TextUtils.isEmpty(message)){
                mMessageField.setText(message);
                //Helper.showKeyboard(getActivity(), mMessageField);
                //mMessageField.setSelection(mMessageField.getText().length());
                mMessageField.post(new Runnable() {
                    @Override
                    public void run() {
                        mMessageField.requestFocus();
                        mMessageField.setSelection(mMessageField.getText().length());
                        Context c = getActivity();
                        if (c != null)
                            Helper.showKeyboard(getActivity(), mMessageField);
                        mFirstUpdate = false;
                    }
                });
            } else if (getFirstUpdate()){
                Helper.hideKeyboard(getActivity(), mMessageField);
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
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Helper.hideKeyboard(getActivity(), mMessageField);
            getActivity().onBackPressed();
            return true;
        }
    }

    private String getDate(){
        return getArguments().getString(DATE);
    }

    private String getLocation(){
        return getArguments().getString(LOCATION);
    }

    private boolean getFirstUpdate(){
        boolean result = getArguments().getBoolean(FIRST_UPDATE) & mFirstUpdate;
        mFirstUpdate = false;
        return result;
    }

}
