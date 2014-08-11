package com.blulabellabs.code.ui.one2one;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blulabellabs.code.Application;
import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.provider.QodemeContract.Contacts.Sync;
import com.blulabellabs.code.core.sync.SyncHelper;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.images.utils.ImageResizer;
import com.blulabellabs.code.images.utils.Utils;
import com.blulabellabs.code.ui.ImageDetailActivity;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.common.CustomDotView;
import com.blulabellabs.code.ui.common.CustomEdit;
import com.blulabellabs.code.ui.one2one.ChatInsideGroupFragment.One2OneChatListInsideFragmentCallback;
import com.blulabellabs.code.utils.ChatFocusSaver;
import com.blulabellabs.code.utils.Converter;
import com.blulabellabs.code.utils.DbUtils;
import com.blulabellabs.code.utils.Helper;
import com.blulabellabs.code.utils.RandomColorGenerator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ChatListGroupSubItem extends RelativeLayout {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT_MAIN = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

    private final Context context;
    private ChatListSubAdapterCallback callback;

    private Message previousMessage;
    private Message nextMessage;
    private Message currentMessage;

    public CustomEdit mMessageField;
    public TextView message, messagerName;
    public TextView dateHeader, mTextViewStatus;
    public CustomDotView date;
    public LinearLayout mRelativeSendMessage;
    public LinearLayout  mLinearLayoutStatus, mLinearLayoutMessage;
    public FrameLayout mLinearLayout;
    public ImageButton mSendButton;
    public ImageView mImageViewItem;
    public ProgressBar mProgressBar;
    public View opponentSeparator;
    public View viewUserSpace;
    private CustomDotView mTypedMessageDot;
    private View mAnimatedLine;
    boolean haveFocus = false;

    public ChatListGroupSubItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public CustomDotView getMessageTypedDot() {
        return mTypedMessageDot = mTypedMessageDot != null ? mTypedMessageDot
                : (CustomDotView) findViewById(R.id.dotView_userTyping1);
    }

    public TextView getMessage() {
        return message = message != null ? message : (TextView) findViewById(R.id.message);
    }

    public TextView getStatus() {
        return mTextViewStatus = mTextViewStatus != null ? mTextViewStatus
                : (TextView) findViewById(R.id.textView_status_update);
    }

    public TextView getMessagerName() {
        return messagerName = messagerName != null ? messagerName
                : (TextView) findViewById(R.id.textView_messagerName);
    }

    public CustomDotView getDate() {
        return date = date != null ? date : (CustomDotView) findViewById(R.id.date);
    }

    public View getUserSpace() {
        return viewUserSpace = viewUserSpace != null ? viewUserSpace
                : findViewById(R.id.view_space);
    }

    public TextView getDateHeader() {
        return dateHeader = dateHeader != null ? dateHeader
                : (TextView) findViewById(R.id.date_header);
    }

    public ImageView getImageMessage() {
        return mImageViewItem = mImageViewItem != null ? mImageViewItem
                : (ImageView) findViewById(R.id.imageView_item);
    }

    public ProgressBar getImageProgress() {
        return mProgressBar = mProgressBar != null ? mProgressBar
                : (ProgressBar) findViewById(R.id.progressBar_img);
    }

    public FrameLayout getImageLayout() {
        return mLinearLayout = mLinearLayout != null ? mLinearLayout
                : (FrameLayout) findViewById(R.id.linearLayout_img);
    }

    public LinearLayout getStatusLayout() {
        return mLinearLayoutStatus = mLinearLayoutStatus != null ? mLinearLayoutStatus
                : (LinearLayout) findViewById(R.id.linear_status_update);
    }

    public LinearLayout getMessageLayout() {
        return mLinearLayoutMessage = mLinearLayoutMessage != null ? mLinearLayoutMessage
                : (LinearLayout) findViewById(R.id.linear_dot);
    }

    public View getOpponentSeparator() {
        return opponentSeparator = opponentSeparator != null ? opponentSeparator
                : findViewById(R.id.opponent_separator);
    }

    One2OneChatListInsideFragmentCallback callback2;

    public void fill(Message me, ChatListSubAdapterCallback callback, int position,
                     Message previousMessage, Message nextMessage,
                     One2OneChatListInsideFragmentCallback callback2) {
        this.callback = callback;
        this.previousMessage = previousMessage;
        this.nextMessage = nextMessage;
        this.currentMessage = me;
        this.callback2 = callback2;
        fill(me);
    }

    public void fill(Message me) {
        final Message msg = me;
        setDefault();

        getMessage().setText(me.message);
        if (me.hasPhoto == 2) {
            if (QodemePreferences.getInstance().getQrcode().equals(me.qrcode)) {
                this.setVisibility(GONE);
                getUserSpace().setVisibility(GONE);
                getStatusLayout().setVisibility(GONE);
                getMessageLayout().setVisibility(GONE);
                getDateHeader().setVisibility(GONE);
                getImageMessage().setVisibility(View.GONE);
                getImageLayout().setVisibility(View.GONE);
            } else {
                getStatusLayout().setVisibility(VISIBLE);
                getMessageLayout().setVisibility(GONE);
                getDateHeader().setVisibility(GONE);
                getImageMessage().setVisibility(View.GONE);
                getImageLayout().setVisibility(View.GONE);
                String createdDate = me.created;
                String dateString;
                try {
                    dateString = Helper.getLocalTimeFromGTM(me.created);
                    dateString = " " + dateString;
                } catch (Exception e) {
                    Log.d("timeError", e + "");
                    dateString = Helper.getTimeAMPM(Converter
                            .getCrurentTimeFromTimestamp(createdDate));
                    dateString = " " + dateString;
                }

                String str = getMessage().getText().toString();
                String mainString = str + dateString + " ";
                SpannableString ss1 = new SpannableString(mainString);
                ss1.setSpan(new RelativeSizeSpan(0.6f), str.length(), mainString.length(), 0);
                ss1.setSpan(new ForegroundColorSpan(Color.GRAY), str.length(), mainString.length(), 0);
                getStatus().setText(ss1);
            }
        } else {
            getStatusLayout().setVisibility(GONE);
            getMessageLayout().setVisibility(VISIBLE);
            if (me.hasPhoto == 1) {
                if (me.localImgPath != null && !me.localImgPath.trim().equals("")) {
                    int size = 200;
                    ImageFetcher fetcher = callback2.getImageFetcher();
                    if (fetcher != null)
                        size = fetcher.getRequiredSize();

                    Bitmap bitmap = ImageResizer.decodeSampledBitmapFromFile(me.localImgPath, size,
                            size, null);
                    getImageMessage().setImageBitmap(bitmap);
                    getImageProgress().setVisibility(View.GONE);
                } else {
                    Log.d("imgUrl", me.photoUrl + "");
                    ImageFetcher fetcher = callback2.getImageFetcher();
                    if (fetcher != null)
                        fetcher.loadImage(me.photoUrl, getImageMessage(), getImageProgress());
                }
                getImageMessage().setVisibility(View.VISIBLE);
                getImageLayout().setVisibility(View.VISIBLE);
                getImageMessage().setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        final Intent i = new Intent(getContext(), ImageDetailActivity.class);
                        i.putExtra(ImageDetailActivity.EXTRA_IMAGE, msg.photoUrl);
                        i.putExtra("flag", msg.is_flagged);
                        i.putExtra("message_id", msg.messageId);
                        if (Utils.hasJellyBean()) {
                            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0,
                                    v.getWidth(), v.getHeight());
                            getContext().startActivity(i, options.toBundle());
                        } else {
                            getContext().startActivity(i);
                        }
                    }
                });
                getImageMessage().setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        ChatLoad chatLoad = callback.getChatLoad(msg.chatId);
                        if (chatLoad != null && chatLoad.is_deleted != 1)
                            showPopupMenu(getMessage(), msg);
                        return true;
                    }
                });
            } else {
                getImageMessage().setImageBitmap(null);
                getImageMessage().setVisibility(View.GONE);
                getImageLayout().setVisibility(View.GONE);
            }

            int color;// = callback.getColor(me.qrcode);
            Contact contact = callback.getContact(me.qrcode);
            if (contact != null) {
                color = contact.color;
                if (contact.state == QodemeContract.Contacts.State.BLOCKED_BY) {
                    getMessage().setVisibility(GONE);
                    getImageMessage().setVisibility(View.GONE);
                    getImageLayout().setVisibility(View.GONE);
                    getSendMessageLayout().setVisibility(GONE);
                    this.setVisibility(GONE);
                    InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getMessage().getWindowToken(), 0);
                }
            } else {
                color = Color.GRAY;
            }
            getDate().invalidate();
            int chatType = callback2.getChatType(me.chatId);
            if (chatType == 1 && !QodemePreferences.getInstance().getQrcode().equals(msg.qrcode)) {
                getMessagerName().setVisibility(View.VISIBLE);
                if (contact != null) {
                    getMessagerName().setText(contact.title);
                    getMessagerName().setBackgroundColor(color);
                } else {
                    getMessagerName().setText("User");
                    getMessagerName().setBackgroundColor(color);
                }
            } else {
                getMessagerName().setVisibility(View.GONE);
            }
            String createdDate = me.created;
            String dateString;
            try {
                dateString = Helper.getLocalTimeFromGTM(me.created);// Helper.getTimeAMPM(Converter.getCrurentTimeFromTimestamp(createdDate));
                dateString = " " + dateString;
            } catch (Exception e) {
                Log.d("timeError", e + "");
                dateString = Helper.getTimeAMPM(Converter.getCrurentTimeFromTimestamp(createdDate));
                dateString = " " + dateString;
            }
            String str = getMessage().getText().toString();
            if (me.hasPhoto == 1)
                str = "I";
            String mainString = str + dateString + " ";
            String flag = "f";
            if (me.is_flagged == 1) {
                mainString = mainString + flag;
            }
            SpannableString ss1 = new SpannableString(mainString);
            ss1.setSpan(new RelativeSizeSpan(0.6f), str.length(), mainString.length(), 0);
            ss1.setSpan(new ForegroundColorSpan(Color.GRAY), str.length(), mainString.length(), 0);
            if (me.hasPhoto == 1)
                ss1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, str.length(), 0);
            if (me.is_flagged == 1) {
                Drawable bm = getResources().getDrawable(R.drawable.ic_flag_small);
                bm.setBounds(0, 0, bm.getIntrinsicWidth(), bm.getIntrinsicHeight());
                ImageSpan is = new ImageSpan(bm, ImageSpan.ALIGN_BASELINE);
                ss1.setSpan(is, mainString.length() - flag.length(), mainString.length(),
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            getMessage().setText(ss1);
            if (me.replyTo_id > 0) {
                android.widget.LinearLayout.LayoutParams param = (android.widget.LinearLayout.LayoutParams) getDate()
                        .getLayoutParams();
                param.width = (int) getDate().convertDpToPixel(70, getContext());
                if (previousMessage != null) {
                    if (previousMessage.replyTo_id > 0) {
                        param.topMargin = 0;
                    }
                }
                getDate().setLayoutParams(param);
                getDate().setReply(true);
                getDate().setSecondVerticalLine(me.isFirst);
                getDate().setSecondVerticalLine2(me.isLast);
                getDate().invalidate();
                getMessage().setClickable(false);
                getMessage().setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        ChatLoad chatLoad = callback.getChatLoad(msg.chatId);
                        if (chatLoad != null && chatLoad.is_deleted != 1)
                            showPopupMenu(v, msg);
                        return true;
                    }
                });
            } else {
                getMessage().setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ChatLoad chatLoad = callback.getChatLoad(msg.chatId);
                        if (chatLoad != null && chatLoad.is_locked != 1 && chatLoad.is_deleted != 1 && nextMessage != null)
                            initSendMessage();
                    }
                });
                getMessage().setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        ChatLoad chatLoad = callback.getChatLoad(msg.chatId);
                        if (chatLoad != null && chatLoad.is_deleted != 1)
                            showPopupMenu(v, msg);
                        return true;
                    }
                });
                getDate().setSecondVerticalLine(me.isFirst);
                getDate().setSecondVerticalLine2(me.isLast);
                android.widget.LinearLayout.LayoutParams param = (android.widget.LinearLayout.LayoutParams) getDate()
                        .getLayoutParams();
                param.width = (int) getDate().convertDpToPixel(20, getContext());
                getDate().setLayoutParams(param);
                getDate().setReply(false);
                getDate().invalidate();
            }
            getMessage().setTypeface(Application.typefaceRegular);
            getMessage().setTextColor(Color.BLACK);
            if (isMyMessage(me.qrcode)) {
                switch (me.state) {
                    case QodemeContract.Messages.State.LOCAL:
                        getDate().setDotColor(context.getResources().getColor(R.color.user_typing));
                        getDate().setOutLine(true);
                        getMessage().setTextColor(getResources().getColor(R.color.user_typing));
                        getDate().invalidate();
                        break;
                    case QodemeContract.Messages.State.SENT:
                        getDate().setDotColor(Color.BLACK);
                        break;
                    case QodemeContract.Messages.State.READ:
                    case QodemeContract.Messages.State.NOT_READ:
                    case QodemeContract.Messages.State.READ_LOCAL:
                    case QodemeContract.Messages.State.WAS_READ:
                        getDate().setDotColor(
                                context.getResources().getColor(R.color.text_message_not_read));
                        break;
                }
            } else {
                if (chatType == 2) {
                    MainActivity activity = (MainActivity) getContext();
                    Integer integerColor = activity.messageColorMap.get(me.messageId);
                    if (integerColor == null) {
                        int randomColor = RandomColorGenerator.getInstance().nextColor();
                        getDate().setDotColor(randomColor);
                        activity.messageColorMap.put(me.messageId, randomColor);
                    } else {
                        getDate().setDotColor(integerColor);
                    }
                } else
                    getDate().setDotColor(color);
                if (QodemeContract.Messages.State.NOT_READ == me.state) {
                    getDate().setOutLine(true);
                    getMessage().setTypeface(Application.typefaceBold);
                }
                getDate().invalidate();
            }
            getUserSpace().setVisibility(GONE);
            getDateHeader().setVisibility(View.GONE);
            getOpponentSeparator().setVisibility(View.GONE);
            if (nextMessage != null) {
                if (me.qrcode.equalsIgnoreCase(nextMessage.qrcode)) {
                    getUserSpace().setVisibility(GONE);
                }
            }
            if (previousMessage != null && previousMessage.hasPhoto != 2) {
                try {
                    Calendar currentDate = Calendar.getInstance();
                    String date = me.created;
                    if (me.created != null && !(me.created.contains(".")))
                        date += ".000";
                    currentDate.setTime(SIMPLE_DATE_FORMAT_MAIN.parse(date));

                    Calendar previousDate = Calendar.getInstance();
                    String preDate = previousMessage.created;
                    if (previousMessage.created != null && (!previousMessage.created.contains(".")))
                        preDate = preDate + ".000";
                    previousDate.setTime(SIMPLE_DATE_FORMAT_MAIN.parse(preDate));
                    if (currentDate.get(Calendar.DATE) != previousDate.get(Calendar.DATE)) {
                        Date dateTemp = new Date(Converter.getCrurentTimeFromTimestamp(date));
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd-yyyy", Locale.US);
                        getDateHeader().setText(dateFormat.format(dateTemp));
                        getDateHeader().setVisibility(View.VISIBLE);
                    } else if (!me.qrcode.equalsIgnoreCase(previousMessage.qrcode)) {
                        getOpponentSeparator().setVisibility(View.VISIBLE);
                    }
                    if (me.qrcode.equalsIgnoreCase(previousMessage.qrcode)
                            && currentDate.get(Calendar.MINUTE) == previousDate
                            .get(Calendar.MINUTE)
                            && currentDate.get(Calendar.HOUR_OF_DAY) == previousDate
                            .get(Calendar.HOUR_OF_DAY)) {
                        getDate().setVisibility(View.VISIBLE);
                    } else {
                        getDate().setVisibility(View.VISIBLE);
                    }
                    /**
                     * Grouped Same user message
                     */
                    if (me.qrcode.equalsIgnoreCase(previousMessage.qrcode)) {
                        long diffInMs = currentDate.getTimeInMillis()
                                - previousDate.getTimeInMillis();
                        long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
                        if (diffInSec < 60) {
                            if (previousMessage.replyTo_id > 0) {
                                if (chatType == 1)
                                    getMessagerName().setVisibility(View.GONE);
                                if (me.replyTo_id > 0)
                                    getDate().setCircle(false);
                                else {
                                    getDate().setCircle(true);
                                }
                            } else if (me.replyTo_id > 0) {
                                getDate().setCircle(true);
                            } else {
                                if (chatType == 1)
                                    getMessagerName().setVisibility(View.GONE);
                                getDate().setCircle(false);
                                getDate().setVisibility(View.VISIBLE);
                            }
                        } else {
                            getDate().setCircle(true);
                            getDate().setVisibility(View.VISIBLE);
                        }
                    } else {
                        getDate().setCircle(true);
                        getDate().setVisibility(View.VISIBLE);
                    }
                    getDate().invalidate();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setDefault() {
        this.setVisibility(VISIBLE);
        getStatusLayout().setVisibility(GONE);
        getMessageLayout().setVisibility(VISIBLE);
        getDateHeader().setVisibility(VISIBLE);
        getImageMessage().setVisibility(View.VISIBLE);
        getImageLayout().setVisibility(View.VISIBLE);
    }

    private void showPopupMenu(View v, final Message message) {
        boolean isContactAvail = false;
        if (QodemePreferences.getInstance().getQrcode().equals(message.qrcode))
            isContactAvail = true;
        else {
            Cursor cursor = getContext().getContentResolver().query(
                    QodemeContract.Contacts.CONTENT_URI,
                    QodemeContract.Contacts.ContactQuery.PROJECTION,
                    QodemeContract.Contacts.CONTACT_QRCODE + " = '" + message.qrcode + "'", null,
                    null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    isContactAvail = true;
                }
            }
        }
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.context_menu_layout, null);
        final PopupWindow popupWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        if (isContactAvail) {
            view.findViewById(R.id.textView_addContact).setVisibility(GONE);
            view.findViewById(R.id.view_divider1).setVisibility(GONE);
        }
        if (QodemePreferences.getInstance().getQrcode().equals(message.qrcode)) {
            view.findViewById(R.id.textView_block).setVisibility(GONE);
            view.findViewById(R.id.view_divider2).setVisibility(GONE);
        }
        view.findViewById(R.id.textView_addContact).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getContext().getContentResolver().insert(QodemeContract.Contacts.CONTENT_URI,
                        QodemeContract.Contacts.addNewContactValues(message.qrcode, message.senderName));
                SyncHelper.requestManualSync();
                popupWindow.dismiss();
            }
        });
        view.findViewById(R.id.textView_block).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getContext().getContentResolver()
                        .update(QodemeContract.Contacts.CONTENT_URI,
                                QodemeContract.Contacts.blockContactValues(Sync.STATE_UPDATED),
                                QodemeContract.Contacts.CONTACT_QRCODE + "= '" + message.qrcode
                                        + "'", null
                        );
                SyncHelper.requestManualSync();
                popupWindow.dismiss();

            }
        });
        view.findViewById(R.id.textView_flagged).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getContext().getContentResolver().update(QodemeContract.Messages.CONTENT_URI,
                        QodemeContract.Messages.updateMessageFlagged(),
                        DbUtils.getWhereClauseForId(), DbUtils.getWhereArgsForId(message._id));
                SyncHelper.requestManualSync();
                popupWindow.dismiss();
            }
        });
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setContentView(view);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(v);
    }

    private ImageButton getSendButton() {
        return mSendButton = mSendButton != null ? mSendButton
                : (ImageButton) findViewById(R.id.button_message);
    }

    private CustomEdit getMessageEditText() {
        return mMessageField = mMessageField != null ? mMessageField
                : (CustomEdit) findViewById(R.id.sub_item_edit_message);
    }

    private LinearLayout getSendMessageLayout() {
        return mRelativeSendMessage = mRelativeSendMessage != null ? mRelativeSendMessage
                : (LinearLayout) findViewById(R.id.layout_message_send);

    }

//    private void initSendMessage() {
//        getSendMessageLayout().setVisibility(VISIBLE);
//        mSendButton = getSendButton();
//        mMessageField = getMessageEditText();
//        mMessageField.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
//        mSendButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String message = mMessageField.getText().toString().trim();
//                if (TextUtils.isEmpty(message) || TextUtils.isEmpty(message.trim())) {
//                    Toast.makeText(getContext(), "Empty message can't be sent", Toast.LENGTH_SHORT)
//                            .show();
//                } else {
//                    callback2.sendReplyMessage(currentMessage.messageId, mMessageField.getText()
//                            .toString(), "", 0, currentMessage.messageId, 0, 0, "");
//                    mMessageField.getText().clear();
//                    getSendMessageLayout().setVisibility(GONE);
//                }
//            }
//        });
//        mMessageField.setOnEditTextImeBackListener(new CustomEdit.OnEditTextImeBackListener() {
//            @Override
//            public void onImeBack(CustomEdit ctrl) {
//                getSendMessageLayout().setVisibility(GONE);
//            }
//        });
//        mMessageField.requestFocus();
//        mMessageField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if (!b) {
//                    Log.d("CHATINSIDE", "user stopped typing");
//                    callback2.stopTypingMessage();
//                }
//            }
//        });
//
//        mMessageField.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                Log.d("CHATINSIDE", "beforeTextChanged called");
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Log.d("CHATINSIDE", "onTextChanged called");
//                if (s.length() > 0) {
//                    mSendButton.setVisibility(View.VISIBLE);
//                    callback2.startTypingMessage();
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                Log.d("CHATINSIDE", "afterTextChanged called");
//                if (s.length() > 0) {
//                    mSendButton.setVisibility(View.VISIBLE);
//                    callback2.startTypingMessage();
//                } else {
//                    mSendButton.setVisibility(View.GONE);
//                    callback2.stopTypingMessage();
//                }
//            }
//        });
//        Helper.showKeyboard(getContext(), mMessageField);
//    }

    private void initSendMessage() {
        getSendMessageLayout().setVisibility(VISIBLE);

        Animation animationLineIn = AnimationUtils.loadAnimation(getContext(),
                R.anim.line_in);
        if (mAnimatedLine == null) {
            mAnimatedLine = findViewById(R.id.horizontal_animated_line);
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
        getMessageTypedDot().setVisibility(VISIBLE);
        getMessageTypedDot().setDotColor(getResources().getColor(R.color.user_typing));
        getMessageTypedDot().setOutLine(true);
        getMessageTypedDot().startAnimation(animationCircleGrow);
        animationCircleGrow.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showMessageAfterAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    private void showMessageAfterAnimation() {
        getSendMessageLayout().setVisibility(VISIBLE);
        getMessageTypedDot().setVisibility(VISIBLE);
        getSendButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback2.sendReplyMessage(currentMessage.messageId, getMessageEditText().getText()
                        .toString(), "", 0, currentMessage.messageId, 0, 0, "");
                getMessageEditText().getText().clear();
                getSendMessageLayout().setVisibility(GONE);
                getMessageTypedDot().setVisibility(View.INVISIBLE);
                mAnimatedLine.setVisibility(View.GONE);
                ChatFocusSaver.setFocusedChatId(0);
                getMessageEditText().setVisibility(INVISIBLE);
            }
        });


        getMessageEditText().setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
//        getMessageEditText().addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() > 0) {
//                    getSendButton().setVisibility(View.VISIBLE);
//                    callback2.startTypingMessage();
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (s.length() > 0) {
//                    getSendButton().setVisibility(View.VISIBLE);
//                    callback2.startTypingMessage();
//                } else {
//                    getSendButton().setVisibility(View.GONE);
//                    callback2.stopTypingMessage();
//                }
//            }
//        });

        getMessageEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE) {

//                    || actionId == EditorInfo.IME_ACTION_UNSPECIFIED
//                            || event.getAction() == KeyEvent.ACTION_DOWN
//                            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER


                    mSendButton.callOnClick();
                    return true;
                }
                return false;
            }
        });

        getMessageEditText().setOnEditTextImeBackListener(new CustomEdit.OnEditTextImeBackListener() {
            @Override
            public void onImeBack(CustomEdit ctrl) {
                getSendMessageLayout().setVisibility(GONE);
                getMessageEditText().setVisibility(View.INVISIBLE);
                mAnimatedLine.setVisibility(View.INVISIBLE);
                getMessageTypedDot().setVisibility(View.INVISIBLE);
                haveFocus = false;
            }
        });

        getMessageEditText().setVisibility(VISIBLE);
        getMessageEditText().postDelayed(new Runnable() {
            @Override
            public void run() {
                haveFocus = true;
            }
        }, 1000);
        getMessageEditText().setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (haveFocus) {
                    if (!hasFocus) {
                        getSendMessageLayout().setVisibility(GONE);
                        getMessageTypedDot().setVisibility(View.INVISIBLE);
                        mAnimatedLine.setVisibility(View.INVISIBLE);
                        getMessageEditText().setVisibility(View.INVISIBLE);
                        getMessageTypedDot().setVisibility(View.INVISIBLE);
                        callback2.stopTypingMessage();
                        haveFocus= false;
                    }
                }
            }
        });
        getMessageEditText().requestFocus();
        Helper.showKeyboard(getContext(), getMessageEditText());
    }


    private boolean isMyMessage(String qr) {
        return TextUtils.equals(qr, QodemePreferences.getInstance().getQrcode());
    }
}
