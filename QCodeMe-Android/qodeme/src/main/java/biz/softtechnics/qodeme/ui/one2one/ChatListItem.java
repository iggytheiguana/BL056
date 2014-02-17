package biz.softtechnics.qodeme.ui.one2one;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.core.io.model.Message;
import biz.softtechnics.qodeme.ui.common.CustomEdit;
import biz.softtechnics.qodeme.ui.common.ExAdapterBasedView;
import biz.softtechnics.qodeme.ui.common.ExtendedListAdapter;
import biz.softtechnics.qodeme.ui.common.ListAdapter;
import biz.softtechnics.qodeme.ui.common.ScrollDisabledListView;
import biz.softtechnics.qodeme.utils.ChatFocusSaver;
import biz.softtechnics.qodeme.utils.Converter;
import biz.softtechnics.qodeme.utils.Fonts;
import biz.softtechnics.qodeme.utils.Helper;

/**
 * Created by Alex on 10/23/13.
 */
public class ChatListItem extends RelativeLayout implements ExAdapterBasedView<Contact, ChatListAdapterCallback> {

    private static final int MIN_CONTENT_SIZE_DP = 100;
    private static final int MAX_CONTENT_SIZE_DP = 350;

    private final int minContentSizePx;
    private final int maxContentSizePx;

    private int _startY;
    private int _yDelta;
    private boolean isDragMode = false;
    private int height;

    private final Context context;
    private TextView name;
    private TextView date;
    private TextView location;
    private ScrollDisabledListView subList;
    private LinearLayout dragView;
    private CustomEdit edit;
    private ImageView dragImage;
    private ImageButton sendImage;
    private View cornerTop;
    private View cornerBottom;
    private View cornerLeft;
    private View cornerRight;


    private GestureDetector gestureDetector;
    private ChatListAdapterCallback mCallback;
    private int mPosition;
    private Contact mContact;

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

        final String oponentQr = mContact.qrCode;
        final int oponentColor = mContact.color == 0 ? Color.GRAY : mContact.color;
        final int myColor = context.getResources().getColor(R.color.text_chat_name);
        getName().setText(mContact.title != null ? mContact.title : "User");
        getName().setTextColor(oponentColor);
        setCornerColor(mCallback.getNewMessagesCount(mContact.chatId), oponentColor);
        //getName().setTypeface(mCallback.getFont(Fonts.ROBOTO_BOLD));
        if (QodemePreferences.getInstance().isSaveLocationDateChecked()) {
            if (ce.date != null) {
                SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yy HH:mm");
                String dateStr = fmtOut.format(new Date(Converter.getCrurentTimeFromTimestamp(ce.date)));
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
        ListAdapter listAdapter = new ExtendedListAdapter<ChatListSubItem, Message, ChatListSubAdapterCallback>(context, R.layout.one2one_chat_list_item_list_item, listForAdapter, new ChatListSubAdapterCallback() {
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
        });

        if (listData != null) listAdapter.addAll(listData);
        getList().setAdapter(listAdapter);
        getList().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        getList().setStackFromBottom(true);
        getList().setDisabled(true);

        height = mCallback.getChatHeight(mContact.chatId);
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
                        lParams.height = delta < minContentSizePx ? minContentSizePx : delta > maxContentSizePx ? maxContentSizePx : delta;
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

        getSendImage().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

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
        return subList = subList != null ? subList : (ScrollDisabledListView) findViewById(R.id.subList);
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

    public ImageView getDragImage() {
        return dragImage = dragImage != null ? dragImage : (ImageView) findViewById(R.id.drag_image);
    }

    public ImageButton getSendImage() {
        return sendImage = sendImage != null ? sendImage : (ImageButton) findViewById(R.id.button_message);
    }

    public View getCornerTop() {
        return cornerTop = cornerTop != null ? cornerTop : findViewById(R.id.corner_top);
    }

    public View getCornerBottom() {
        return cornerBottom = cornerBottom != null ? cornerBottom : findViewById(R.id.corner_bottom);
    }

    public View getCornerLeft() {
        return cornerLeft = cornerLeft != null ? cornerLeft : findViewById(R.id.corner_left);
    }

    public View getCornerRight() {
        return cornerRight = cornerRight != null ? cornerRight : findViewById(R.id.corner_right);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return super.onTouchEvent(e) ? true : gestureDetector.onTouchEvent(e);
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
            showMessage();

            mCallback.onSingleTap(getView(), mPosition, mContact);
            messageRead();
            Log.i("GestureListener", "onSingleTap");
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //Helper.hideKeyboard(getContext(), getMessageEdit());
            mCallback.onDoubleTap(getView(), mPosition, mContact);
            messageRead();
            Log.i("GestureListener", "onDoubleTap");
            return true;
        }
    }

    public void showMessage() {


        getMessageEdit().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ChatFocusSaver.setCurrentMessage(mContact.chatId, s.toString());

                if (s.length() > 0) {
                    getSendImage().setVisibility(View.VISIBLE);
                } else {
                    getSendImage().setVisibility(View.GONE);
                }

            }
        });

        getMessageEdit().setOnEditTextImeBackListener(new CustomEdit.OnEditTextImeBackListener(){
            @Override
            public void onImeBack(CustomEdit ctrl) {
                ChatFocusSaver.setFocusedChatId(0);
                getSendImage().setVisibility(View.GONE);
                getMessageEdit().setVisibility(View.GONE);
            }
        });

       /* getMessageEdit().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    ChatFocusSaver.setFocusedChatId(0);
                    return true;
                }
                return false;
            }
        });*/

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
                getMessageEdit().requestFocus();
                Helper.showKeyboard(getContext(), getMessageEdit());
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
        mCallback.sendMessage(mContact, message);
        mCallback.messageRead(mContact.chatId);
    }

    private void setCornerColor(int index, int color){
        int defColor = getResources().getColor(R.color.conversation_card_background);
        int c = (index == 0) ? defColor : color;
        getCornerTop().setBackgroundColor(c);
        getCornerBottom().setBackgroundColor(c);
        getCornerLeft().setBackgroundColor(c);
        getCornerRight().setBackgroundColor(c);
    }

}
