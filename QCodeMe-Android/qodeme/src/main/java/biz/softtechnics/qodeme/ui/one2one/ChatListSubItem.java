package biz.softtechnics.qodeme.ui.one2one;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import biz.softtechnics.qodeme.Application;
import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.core.io.model.Message;
import biz.softtechnics.qodeme.core.provider.QodemeContract;
import biz.softtechnics.qodeme.images.utils.ImageFetcher;
import biz.softtechnics.qodeme.images.utils.ImageResizer;
import biz.softtechnics.qodeme.ui.common.CustomDotView;
import biz.softtechnics.qodeme.ui.common.CustomEdit;
import biz.softtechnics.qodeme.ui.common.ExtendedAdapterBasedView;
import biz.softtechnics.qodeme.ui.one2one.ChatInsideFragment.One2OneChatListInsideFragmentCallback;
import biz.softtechnics.qodeme.utils.Converter;
import biz.softtechnics.qodeme.utils.Helper;

/**
 * Created by Alex on 10/23/13.
 */
public class ChatListSubItem extends RelativeLayout implements
		ExtendedAdapterBasedView<Message, ChatListSubAdapterCallback> {

	private static final SimpleDateFormat SIMPLE_DATE_FORMAT_MAIN = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT_HEADER = new SimpleDateFormat(
			"MMM dd yyyy", Locale.US);

	private final Context context;
	private TextView message, messagerName;
	private ListView subList;
	private ChatListSubAdapterCallback callback;

	private int position;
	private Message previousMessage;
	private Message currentMessage;
	private CustomDotView date;
	private TextView dateHeader;
	private RelativeLayout headerContainer;
	private LinearLayout mLinearLayout;
	private View opponentSeparator;
	private ImageButton mSendButton;
	private CustomEdit mMessageField;
	private ImageView mImageViewItem;
	private ProgressBar mProgressBar;

	public ChatListSubItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public TextView getMessage() {
		return message = message != null ? message : (TextView) findViewById(R.id.message);
	}

	public TextView getMessagerName() {
		return messagerName = messagerName != null ? messagerName
				: (TextView) findViewById(R.id.textView_messagerName);
	}

	// Change Textview (for display time/date) to CustomDotView
	// public TextView getDate() {
	// return date = date != null ? date : (TextView) findViewById(R.id.date);
	// }
	public CustomDotView getDate() {
		return date = date != null ? date : (CustomDotView) findViewById(R.id.date);
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

	public LinearLayout getImageLayout() {
		return mLinearLayout = mLinearLayout != null ? mLinearLayout
				: (LinearLayout) findViewById(R.id.linearLayout_img);
	}

	public RelativeLayout getHeaderContainer() {
		return headerContainer = headerContainer != null ? headerContainer
				: (RelativeLayout) findViewById(R.id.header_container);
	}

	public View getOpponentSeparator() {
		return opponentSeparator = opponentSeparator != null ? opponentSeparator
				: (View) findViewById(R.id.opponent_separator);
	}

	One2OneChatListInsideFragmentCallback callback2;

	@Override
	public void fill(Message me, ChatListSubAdapterCallback callback, int position,
			Message previousMessage, One2OneChatListInsideFragmentCallback callback2) {
		this.callback = callback;
		this.position = position;
		this.previousMessage = previousMessage;
		this.currentMessage = me;
		this.callback2 = callback2;
		fill(me);
	}

	@Override
	public void fill(Message me) {
		getMessage().setText(me.message);

		if (me.hasPhoto == 1) {
			getMessage().setText(" ");
			if (me.localImgPath != null && !me.localImgPath.trim().equals("")) {
				int size = 200;
				ImageFetcher fetcher = callback2.getImageFetcher();
				if (fetcher != null)
					size = fetcher.getRequiredSize();

				Bitmap bitmap = ImageResizer.decodeSampledBitmapFromFile(me.localImgPath, size,
						size, null);
				// getImageMessage().setImageURI(Uri.parse(me.localImgPath));
				getImageMessage().setImageBitmap(bitmap);
				getImageProgress().setVisibility(View.GONE);
			} else {
				Log.d("imgUrl", me.photoUrl + "");
				ImageFetcher fetcher = callback2.getImageFetcher();
				if (fetcher != null)
					fetcher.loadImage(me.photoUrl, getImageMessage(), getImageProgress());
			}
			// String sss = me.localImgPath;
			// if(sss == null || sss.trim().equals(""))
			// sss = me.photoUrl;
			// byte data[] = Base64.decode(sss, Base64.NO_WRAP);
			// Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
			// data.length);
			// getImageMessage().setImageBitmap(bitmap);
			getImageMessage().setVisibility(View.VISIBLE);
			getImageLayout().setVisibility(View.VISIBLE);
		} else {
			getImageMessage().setImageBitmap(null);
			getImageMessage().setVisibility(View.GONE);
			getImageLayout().setVisibility(View.GONE);
		}

		int color = callback.getColor(me.qrcode);
		Contact contact = callback.getContact(me.qrcode);

		if (callback2.getChatType(me.chatId) == 1) {
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

		// getMessage().setTextColor(color);
		// getMessage().setTypeface(callback.getFont(Fonts.ROBOTO_LIGHT));

		// convert 24 hour date formate to 12 hours format
		// getDate().setText(Helper.getTime24(Converter.getCrurentTimeFromTimestamp(me.created)));
		// getDate().setText(Helper.getTimeAMPM(Converter.getCrurentTimeFromTimestamp(me.created)));//new
		String createdDate = me.created;
		// Log.d("me.Date", createdDate + "");
		String dateString = "";
		try {
			dateString = Helper.getLocalTimeFromGTM(me.created);// Helper.getTimeAMPM(Converter.getCrurentTimeFromTimestamp(createdDate));
			dateString = " " + dateString;
		} catch (Exception e) {
			Log.d("timeError", e+"");
			dateString = Helper.getTimeAMPM(Converter.getCrurentTimeFromTimestamp(createdDate));
			dateString = " " + dateString;
		}
		// dateString = " " + dateString;
		// dateString = "<font size=\"30\" color=\"#c5c5c5\">" + dateString +
		// "</font>";
		String str = getMessage().getText().toString();
		String mainString = str + dateString;
		// Create our span sections, and assign a format to each.
		SpannableString ss1 = new SpannableString(mainString);
		ss1.setSpan(new RelativeSizeSpan(0.6f), str.length(), mainString.length(), 0); // set
																						// size
		ss1.setSpan(new ForegroundColorSpan(Color.GRAY), str.length(), mainString.length(), 0); // set
																								// size
		getMessage().setText(ss1);
		if (me.replyTo_id > 0) {
			android.widget.LinearLayout.LayoutParams param = (android.widget.LinearLayout.LayoutParams) getDate()
					.getLayoutParams();
			param.width = (int) getDate().convertDpToPixel(70, getContext());
			getDate().setLayoutParams(param);
			getDate().setReply(true);
			// if (previousMessage != null && previousMessage.replyTo_id > 0) {
			// getDate().setSecondVerticalLine(true);
			// }
			getDate().setSecondVerticalLine(me.isFirst);
			getDate().setSecondVerticalLine2(me.isLast);
			getDate().invalidate();
			getMessage().setClickable(false);
		} else {

			getMessage().setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					initSendMessage();
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
		// Log.d("me.Datetime", dateString + " ");
		// getMessage().setText(Html.fromHtml(getMessage().getText() + " " +
		// dateString));
		// changes
		getDate().setOutLine(false);
		getMessage().setTypeface(Application.typefaceRegular);
		getMessage().setTextColor(Color.BLACK);
		if (isMyMessage(me.qrcode)) {
			switch (me.state) {
			case QodemeContract.Messages.State.LOCAL:
				// getDate().setTextColor(context.getResources().getColor(R.color.text_message_not_send));
				getDate().setDotColor(context.getResources().getColor(R.color.user_typing));
				getMessage().setTextColor(getResources().getColor(R.color.user_typing));
				getDate().invalidate();
				break;
			case QodemeContract.Messages.State.SENT:
				// getDate().setTextColor(context.getResources().getColor(R.color.text_message_sent));
				getDate().setDotColor(context.getResources().getColor(R.color.text_message_sent));
				getDate().invalidate();
				break;
			case QodemeContract.Messages.State.NOT_READ:
			case QodemeContract.Messages.State.READ:
			case QodemeContract.Messages.State.READ_LOCAL:
			case QodemeContract.Messages.State.WAS_READ:
				// getDate().setTextColor(context.getResources().getColor(R.color.text_message_reed));
				getDate().setDotColor(
						context.getResources().getColor(R.color.text_message_not_read));
				getDate().invalidate();
				break;
			}
		} else {
			// getDate().setTextColor(color);
			getDate().setDotColor(color);
			if (QodemeContract.Messages.State.NOT_READ == me.state) {
				getDate().setOutLine(true);
				getMessage().setTypeface(Application.typefaceBold);
			} else {

			}
			getDate().invalidate();
		}

		getHeaderContainer().setVisibility(View.GONE);
		getOpponentSeparator().setVisibility(View.GONE);
		if (previousMessage != null /*
									 * &&
									 * !TextUtils.isEmpty(previousMessage.created
									 * )
									 */) {
			try {
				Calendar currentDate = Calendar.getInstance();
				String date = me.created;
				if (me.created != null && !(me.created.contains(".")))
					date += ".000";
				currentDate.setTime(SIMPLE_DATE_FORMAT_MAIN.parse(date));

				Calendar previousDate = Calendar.getInstance();
				String preDate = previousMessage.created;
				// Log.d("preDate", preDate);
				if (previousMessage.created != null && (!previousMessage.created.contains(".")))
					preDate = preDate + ".000";
				// Log.d("preDate", preDate);
				previousDate.setTime(SIMPLE_DATE_FORMAT_MAIN.parse(preDate));

				if (currentDate.get(Calendar.DATE) != previousDate.get(Calendar.DATE)) {
					Date dateTemp = new Date(Converter.getCrurentTimeFromTimestamp(date));
					// Converter.getCrurentTimeFromTimestamp(me.created)
					getDateHeader().setText(SIMPLE_DATE_FORMAT_HEADER.format(dateTemp));
					getHeaderContainer().setVisibility(View.VISIBLE);
				} else if (!me.qrcode.equalsIgnoreCase(previousMessage.qrcode)) {
					getOpponentSeparator().setVisibility(View.VISIBLE);
				}

				if (me.qrcode.equalsIgnoreCase(previousMessage.qrcode)
						&& currentDate.get(Calendar.MINUTE) == previousDate.get(Calendar.MINUTE)
						&& currentDate.get(Calendar.HOUR_OF_DAY) == previousDate
								.get(Calendar.HOUR_OF_DAY)) {

					// getDate().setVisibility(View.INVISIBLE);
					getDate().setVisibility(View.VISIBLE);
				} else {
					getDate().setVisibility(View.VISIBLE);
				}

			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

	}

	private ImageButton getSendButton() {
		return mSendButton = mSendButton != null ? mSendButton
				: (ImageButton) findViewById(R.id.button_message);
	}

	private CustomEdit getMessageEditText() {
		return mMessageField = mMessageField != null ? mMessageField
				: (CustomEdit) findViewById(R.id.edit_message);
	}

	private RelativeLayout mRelativeSendMessage;

	private RelativeLayout getSendMessageLayout() {
		return mRelativeSendMessage = mRelativeSendMessage != null ? mRelativeSendMessage
				: (RelativeLayout) findViewById(R.id.layout_message_send);

	}

	private void initSendMessage() {
		getSendMessageLayout().setVisibility(VISIBLE);
		mSendButton = getSendButton();// (ImageButton)
										// getView().findViewById(R.id.button_message);
		mMessageField = getMessageEditText();// (EditText)
												// getView().findViewById(R.id.edit_message);
		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// sendMessage();
				callback2.sendReplyMessage(currentMessage.messageId, mMessageField.getText()
						.toString(), "", 0, currentMessage.messageId, 0, 0, "");
				mMessageField.setText("");
				getSendMessageLayout().setVisibility(GONE);
			}
		});
		mMessageField.setOnEditTextImeBackListener(new CustomEdit.OnEditTextImeBackListener() {
			@Override
			public void onImeBack(CustomEdit ctrl) {
				// getSendImage().setVisibility(View.GONE);
				// getMessageEdit().setVisibility(View.GONE);
				getSendMessageLayout().setVisibility(GONE);
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
		mMessageField.requestFocus();
		mMessageField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean b) {
				if (!b) {
					// edit view doesn't have focus anymore
					Log.d("CHATINSIDE", "user stopped typing");
					// sendUserStoppedTypingMessage();
					callback2.stopTypingMessage();
					// getSendMessageLayout().setVisibility(GONE);

				} else {
					// getSendMessageLayout().setVisibility(GONE);
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
					// sendUserTypingMessage();// send user typing message
					callback2.startTypingMessage();
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				Log.d("CHATINSIDE", "afterTextChanged called");
				if (s.length() > 0) {
					mSendButton.setVisibility(View.VISIBLE);
					// sendUserTypingMessage();
					callback2.startTypingMessage();
				} else {
					mSendButton.setVisibility(View.GONE);
					// sendUserStoppedTypingMessage();
					callback2.stopTypingMessage();
				}
			}
		});

	}

	private boolean isMyMessage(String qr) {
		return TextUtils.equals(qr, QodemePreferences.getInstance().getQrcode());
	}
}
