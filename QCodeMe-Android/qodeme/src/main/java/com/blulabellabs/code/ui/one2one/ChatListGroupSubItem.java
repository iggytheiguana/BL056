package com.blulabellabs.code.ui.one2one;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
import com.blulabellabs.code.ui.common.ExtendedGroupAdapterBasedView;
import com.blulabellabs.code.ui.one2one.ChatInsideGroupFragment.One2OneChatListInsideFragmentCallback;
import com.blulabellabs.code.utils.Converter;
import com.blulabellabs.code.utils.DbUtils;
import com.blulabellabs.code.utils.Helper;
import com.blulabellabs.code.utils.RandomColorGenerator;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

/**
 * Created by Alex on 10/23/13.
 */
public class ChatListGroupSubItem extends RelativeLayout implements
		ExtendedGroupAdapterBasedView<Message, ChatListSubAdapterCallback> {

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
	private Message nextMessage;
	private Message currentMessage;
	private CustomDotView date;
	private TextView dateHeader, mTextViewStatus, lastMessageLineHider;
	private RelativeLayout headerContainer;
	private LinearLayout mLinearLayout, mLinearLayoutStatus, mLinearLayoutMessage;
	private View opponentSeparator;
	private ImageButton mSendButton;
	private CustomEdit mMessageField;
	private ImageView mImageViewItem;
	private ProgressBar mProgressBar;
	private View viewUserSpace, viewUserSpace1;

	public ChatListGroupSubItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public TextView getMessage() {
		return message = message != null ? message : (TextView) findViewById(R.id.message);
	}
	public TextView getLstMessageLineHider() {
		return lastMessageLineHider = lastMessageLineHider != null ? lastMessageLineHider
				: (TextView) findViewById(R.id.lastMessageLineInvisible);
	}
	public TextView getStatus() {
		return mTextViewStatus = mTextViewStatus != null ? mTextViewStatus
				: (TextView) findViewById(R.id.textView_status_update);
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

	// public CustomDotView getLine() {
	// return line ;//= line != null ? line : (CustomDotView)
	// findViewById(R.id.custom_line);
	// }
	public View getUserSpace() {
		return viewUserSpace = viewUserSpace != null ? viewUserSpace
				: (View) findViewById(R.id.view_space);
	}

	public View getUserSpace1() {
		return viewUserSpace1 = viewUserSpace1 != null ? viewUserSpace1
				: (View) findViewById(R.id.view_space1);
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

	public LinearLayout getStatusLayout() {
		return mLinearLayoutStatus = mLinearLayoutStatus != null ? mLinearLayoutStatus
				: (LinearLayout) findViewById(R.id.linear_status_update);
	}

	public LinearLayout getMessageLayout() {
		return mLinearLayoutMessage = mLinearLayoutMessage != null ? mLinearLayoutMessage
				: (LinearLayout) findViewById(R.id.linear_dot);
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
			Message previousMessage, Message nextMessage,
			One2OneChatListInsideFragmentCallback callback2) {
		this.callback = callback;
		this.position = position;
		this.previousMessage = previousMessage;
		this.nextMessage = nextMessage;
		this.currentMessage = me;
		this.callback2 = callback2;
		fill(me);
	}

	@SuppressLint("NewApi")
	@Override
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
				getHeaderContainer().setVisibility(GONE);
				getImageMessage().setVisibility(View.GONE);
				getImageLayout().setVisibility(View.GONE);
			} else {

				// getUserSpace().setVisibility(VISIBLE);
				getStatusLayout().setVisibility(VISIBLE);
				getMessageLayout().setVisibility(GONE);
				getHeaderContainer().setVisibility(GONE);
				getImageMessage().setVisibility(View.GONE);
				getImageLayout().setVisibility(View.GONE);

				String createdDate = me.created;
				// Log.d("me.Date", createdDate + "");
				String dateString = "";
				try {
					dateString = Helper.getLocalTimeFromGTM(me.created);// Helper.getTimeAMPM(Converter.getCrurentTimeFromTimestamp(createdDate));
					dateString = " " + dateString;
				} catch (Exception e) {
					Log.d("timeError", e + "");
					dateString = Helper.getTimeAMPM(Converter
							.getCrurentTimeFromTimestamp(createdDate));
					dateString = " " + dateString;
				}

				String str = getMessage().getText().toString();
				String mainString = str + dateString + " ";

				// Create our span sections, and assign a format to each.
				SpannableString ss1 = new SpannableString(mainString);
				ss1.setSpan(new RelativeSizeSpan(0.6f), str.length(), mainString.length(), 0); // set
																								// size
				ss1.setSpan(new ForegroundColorSpan(Color.GRAY), str.length(), mainString.length(),
						0); // set
							// size
				getStatus().setText(ss1);
			}
		} else {
			getStatusLayout().setVisibility(GONE);
			getMessageLayout().setVisibility(VISIBLE);

			if (me.hasPhoto == 1) {
				// String sss = me.photoUrl;
				// byte data[] = Base64.decode(sss, Base64.NO_WRAP);
				// Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
				// data.length);
				// getImageMessage().setImageBitmap(bitmap);
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
				// ImageFetcher fetcher = callback2.getImageFetcher();
				// if (fetcher != null)
				// fetcher.loadImage(me.photoUrl, getImageMessage(),
				// getImageProgress());
				getImageMessage().setVisibility(View.VISIBLE);
				getImageLayout().setVisibility(View.VISIBLE);
				getImageMessage().setOnClickListener(new OnClickListener() {

					@SuppressLint("NewApi")
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

				}
			} else {
				color = Color.GRAY;
			}
			getDate().invalidate();

			// Bitmap bmp = Bitmap.createBitmap(getDate().textSize ,
			// getDate().textSize,
			// Bitmap.Config.ARGB_8888);
			// Paint paint = new Paint();
			// paint.setAntiAlias(true);
			// paint.setColor(color);

			// Canvas c = new Canvas(bmp);
			// c.drawCircle((getDate().textSize / 2) -4, (getDate().textSize /
			// 2) -4, (getDate().textSize / 2)-4,
			// paint);
			//
			// BitmapDrawable bitmapDrawable = new
			// BitmapDrawable(getResources(), bmp);

			// getMessage().setCompoundDrawablesRelativeWithIntrinsicBounds(bitmapDrawable,
			// null,
			// null, null);

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
			// getMessage().setTextColor(color);
			// getMessage().setTypeface(callback.getFont(Fonts.ROBOTO_LIGHT));

			// convert 24 hour date formate to 12 hours format
			// getDate().setText(Helper.getTime24(Converter.getCrurentTimeFromTimestamp(me.created)));
			// getDate().setText(Helper.getTimeAMPM(Converter.getCrurentTimeFromTimestamp(me.created)));//new
			String createdDate = me.created;
			// Log.d("me.Date", createdDate + "");
			// String dateString =
			// Helper.getTimeAMPM(Converter.getCrurentTimeFromTimestamp(createdDate));
			String dateString = "";
			try {
				dateString = Helper.getLocalTimeFromGTM(me.created);// Helper.getTimeAMPM(Converter.getCrurentTimeFromTimestamp(createdDate));
				dateString = " " + dateString;
			} catch (Exception e) {
				Log.d("timeError", e + "");
				dateString = Helper.getTimeAMPM(Converter.getCrurentTimeFromTimestamp(createdDate));
				dateString = " " + dateString;
			}
			// dateString = " " + dateString;
			// dateString = "<font size=\"30\" color=\"#c5c5c5\">" + dateString
			// +
			// "</font>";
			String str = getMessage().getText().toString();
			if (me.hasPhoto == 1)
				str = "I";
			String mainString = str + dateString + " ";
			String flag = "f";
			if (me.is_flagged == 1) {
				mainString = mainString + flag;
			}

			// Create our span sections, and assign a format to each.
			SpannableString ss1 = new SpannableString(mainString);
			ss1.setSpan(new RelativeSizeSpan(0.6f), str.length(), mainString.length(), 0); // set
																							// size
			ss1.setSpan(new ForegroundColorSpan(Color.GRAY), str.length(), mainString.length(), 0); // set
																									// size
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
				// if (previousMessage != null && previousMessage.replyTo_id >
				// 0) {
				// getDate().setSecondVerticalLine(true);
				// }
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

						if (chatLoad != null && chatLoad.is_locked != 1 && chatLoad.is_deleted != 1 && nextMessage!= null)
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
			// Log.d("me.Datetime", dateString + " ");
			// getMessage().setText(Html.fromHtml(getMessage().getText() + " " +
			// dateString));
			// changes
			getMessage().setTypeface(Application.typefaceRegular);
			getMessage().setTextColor(Color.BLACK);
			if (isMyMessage(me.qrcode)) {
				switch (me.state) {
				case QodemeContract.Messages.State.LOCAL:
					// getDate().setTextColor(context.getResources().getColor(R.color.text_message_not_send));
					getDate().setDotColor(context.getResources().getColor(R.color.user_typing));
					getDate().setOutLine(true);
					getMessage().setTextColor(getResources().getColor(R.color.user_typing));
					getDate().invalidate();
					break;
				case QodemeContract.Messages.State.SENT:
					// getDate().setTextColor(context.getResources().getColor(R.color.text_message_sent));
					// getDate().setDotColor(
					// context.getResources().getColor(R.color.text_message_sent));
					getDate().setDotColor(Color.BLACK);
					break;
				case QodemeContract.Messages.State.READ:
				case QodemeContract.Messages.State.NOT_READ:
				case QodemeContract.Messages.State.READ_LOCAL:
				case QodemeContract.Messages.State.WAS_READ:
					// getDate().setTextColor(context.getResources().getColor(R.color.text_message_reed));
					getDate().setDotColor(
							context.getResources().getColor(R.color.text_message_not_read));
					break;
				}
			} else {
				// getDate().setTextColor(color);
				if (chatType == 2) {
					MainActivity activity = (MainActivity) getContext();
					Integer integerColor = activity.messageColorMap.get(me.messageId);
					if(integerColor == null){
						
						int randomColor = RandomColorGenerator.getInstance().nextColor();
						getDate().setDotColor(randomColor);
						activity.messageColorMap.put(me.messageId, randomColor);
					}else{
						getDate().setDotColor(integerColor);
					}
				} else
					getDate().setDotColor(color);
				if (QodemeContract.Messages.State.NOT_READ == me.state) {
					getDate().setOutLine(true);
					getMessage().setTypeface(Application.typefaceBold);
				} else {

				}
				getDate().invalidate();
			}
			getUserSpace().setVisibility(GONE);
			getUserSpace1().setVisibility(GONE);
			getHeaderContainer().setVisibility(View.GONE);
			getOpponentSeparator().setVisibility(View.GONE);
			if (nextMessage != null) {
				if (me.qrcode.equalsIgnoreCase(nextMessage.qrcode)) {
					getUserSpace().setVisibility(GONE);
				} else {

//					if (me.replyTo_id > 0)
//						getUserSpace1().setVisibility(VISIBLE);
//					else
//						getUserSpace().setVisibility(VISIBLE);
					getLstMessageLineHider().setVisibility(GONE);				}
			}else{
				getLstMessageLineHider().setVisibility(VISIBLE);
				if (!me.isVerticleLineHide ) {
					getLstMessageLineHider().setBackgroundColor(Color.TRANSPARENT);
				} else {
					getLstMessageLineHider().setBackgroundColor(Color.WHITE);
				}
			}
			/*
			 * && !TextUtils.isEmpty(previousMessage. created )
			 */
			if (previousMessage != null && previousMessage.hasPhoto != 2) {
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
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"MMM-dd-yyyy", Locale.US);
//						getDateHeader().setText(SIMPLE_DATE_FORMAT_HEADER.format(dateTemp));
						getDateHeader().setText(dateFormat.format(dateTemp));
						
						getHeaderContainer().setVisibility(View.VISIBLE);
					} else if (!me.qrcode.equalsIgnoreCase(previousMessage.qrcode)) {
						getOpponentSeparator().setVisibility(View.VISIBLE);
					}

					if (me.qrcode.equalsIgnoreCase(previousMessage.qrcode)
							&& currentDate.get(Calendar.MINUTE) == previousDate
									.get(Calendar.MINUTE)
							&& currentDate.get(Calendar.HOUR_OF_DAY) == previousDate
									.get(Calendar.HOUR_OF_DAY)) {

						// getDate().setVisibility(View.INVISIBLE);
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
						// Log.d("timeDef", diffInSec+"");
						if (diffInSec < 60) {
							if (previousMessage.replyTo_id > 0) {
								// getDate().setVisibility(View.INVISIBLE);
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
						// getUserSpace().setVisibility(VISIBLE);
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
		// getUserSpace().setVisibility(VISIBLE);
		getStatusLayout().setVisibility(GONE);
		getMessageLayout().setVisibility(VISIBLE);
		getHeaderContainer().setVisibility(VISIBLE);
		getImageMessage().setVisibility(View.VISIBLE);
		getImageLayout().setVisibility(View.VISIBLE);

	}

	@SuppressWarnings("deprecation")
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
		// if (!isContactAvail) {
		// if
		// (QodemePreferences.getInstance().getQrcode().equals(message.qrcode))
		// isContactAvail = true;
		// }
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
						QodemeContract.Contacts.addNewContactValues(message.qrcode,message.senderName));
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
										+ "'", null);
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

		mMessageField.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// sendMessage();
				String message = mMessageField.getText().toString().trim();
				if (TextUtils.isEmpty(message) || TextUtils.isEmpty(message.trim())) {
					Toast.makeText(getContext(), "Empty message can't be sent", Toast.LENGTH_SHORT)
							.show();
					return;
				} else {
					callback2.sendReplyMessage(currentMessage.messageId, mMessageField.getText()
							.toString(), "", 0, currentMessage.messageId, 0, 0, "");
					mMessageField.setText("");
					getSendMessageLayout().setVisibility(GONE);
				}
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
		Helper.showKeyboard(getContext(), mMessageField);
	}

	private boolean isMyMessage(String qr) {
		return TextUtils.equals(qr, QodemePreferences.getInstance().getQrcode());
	}
}
