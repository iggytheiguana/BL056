package com.blulabellabs.code.ui.contacts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.sync.SyncHelper;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.common.ExAdapterBasedView;
import com.blulabellabs.code.utils.DbUtils;
import com.blulabellabs.code.utils.LatLonCity;

/**
 * Created by Alex on 10/20/13.
 */
public class ContactListItemInvited extends LinearLayout implements
		ExAdapterBasedView<ContactListItemEntity, ContactListAdapterCallback> {

	private final Context mContext;
	private ImageView mQrCode;
	private TextView mName;
	private View mDragView;
	private Contact mContact;
	private Object mCallback;
	private int mPosition;
	private TextView mContactInfoTextView;
	private TextView mInviteMessageTextView;
	private Button mAcceptImageView;
	private Button mRegectImageView;

	public ContactListItemInvited(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;

	}

	@Override
	public void fill(ContactListItemEntity contactListItemEntity) {
		this.mContact = contactListItemEntity.getContact();
		int color = mContact.color == 0 ? Color.GRAY : mContact.color;
		String sQrCode = mContact.qrCode;
		// Drawable d =
		// mContext.getResources().getDrawable(R.drawable.white_color_logo);
		getName().setText(mContact.title != null ? mContact.title : "User");
		getName().setTextColor(color);

		SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yy HH:mm");
		String dateStr = fmtOut.format(new Date(Timestamp.valueOf(mContact.date).getTime()));
		getContactInfo().setText(dateStr + ", " + mContact.location);

		getInviteMessage().setText(mContact.message);
		getInviteMessage().setTextColor(color);

		getAcceptButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mContext.getContentResolver().update(QodemeContract.Contacts.CONTENT_URI,
						QodemeContract.Contacts.acceptContactValues(mContact.updated),
						DbUtils.getWhereClauseForId(), DbUtils.getWhereArgsForId(mContact._id));
				try {
					Cursor cursor = getContext().getContentResolver().query(
							QodemeContract.Chats.CONTENT_URI,
							QodemeContract.Chats.ChatQuery.PROJECTION,
							QodemeContract.Chats.CHAT_ID + " = " + mContact.chatId, null, null);
					LatLonCity latLonCity = QodemePreferences.getInstance().getLastLocation();
					String latitude = "0";
					String longitude = "0";

					if (latLonCity != null) {
						latitude = latLonCity.getLatitude();// (latLonCity.getLat()
															// / 1E6) + "";
						longitude = latLonCity.getLongitude();// (latLonCity.getLon()
																// / 1E6) + "";

					}
					if (cursor != null) {
						if (cursor.getCount() > 0) {
						} else {
							getContext().getContentResolver().insert(
									QodemeContract.Chats.CONTENT_URI,
									QodemeContract.Chats.addNewPushChatValues(mContact.chatId, 0,
											"", "", latitude, longitude, "", "", 0, 0, "", "", 0,
											""));
						}
					} else {
						getContext().getContentResolver().insert(
								QodemeContract.Chats.CONTENT_URI,
								QodemeContract.Chats.addNewPushChatValues(mContact.chatId, 0, "",
										"", latitude, longitude, "", "", 0, 0, "", "", 0, ""));
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				SyncHelper.requestManualSync();
			}
		});

		getRegectButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(R.string.reject_user);
				builder.setMessage(R.string.are_you_sure)
						.setPositiveButton(R.string.button_ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										mContext.getContentResolver().update(
												QodemeContract.Contacts.CONTENT_URI,
												QodemeContract.Contacts
														.rejectContactValues(mContact.updated),
												DbUtils.getWhereClauseForId(),
												DbUtils.getWhereArgsForId(mContact._id));
										SyncHelper.requestManualSync();
									}
								})
						.setNegativeButton(R.string.button_cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										// User cancelled the dialog
									}
								});
				builder.create().show();
			}
		});

		setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(R.string.block_user);
				builder.setMessage(R.string.are_you_sure)
						.setPositiveButton(R.string.button_ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										mContext.getContentResolver().update(
												QodemeContract.Contacts.CONTENT_URI,
												QodemeContract.Contacts
														.blockContactValues(mContact.updated),
												DbUtils.getWhereClauseForId(),
												DbUtils.getWhereArgsForId(mContact._id));
										SyncHelper.requestManualSync();

										Log.i("~S~", "set blocked + " + mContact._id);
									}
								})
						.setNegativeButton(R.string.button_cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										// User cancelled the dialog
									}
								});
				builder.create().show();
				return false;
			}
		});
	}

	public TextView getName() {
		return mName = mName != null ? mName : (TextView) findViewById(R.id.name);
	}

	public TextView getContactInfo() {
		return mContactInfoTextView = mContactInfoTextView != null ? mContactInfoTextView
				: (TextView) findViewById(R.id.contact_info);
	}

	public TextView getInviteMessage() {
		return mInviteMessageTextView = mInviteMessageTextView != null ? mInviteMessageTextView
				: (TextView) findViewById(R.id.invite_message);
	}

	public Button getAcceptButton() {
		return mAcceptImageView = mAcceptImageView != null ? mAcceptImageView
				: (Button) findViewById(R.id.accept);
	}

	public Button getRegectButton() {
		return mRegectImageView = mRegectImageView != null ? mRegectImageView
				: (Button) findViewById(R.id.regect);
	}

	public Contact getContact() {
		return mContact;
	}

	@Override
	public void fill(ContactListItemEntity contactListItemEntity,
			ContactListAdapterCallback callback, int position) {
		this.mCallback = callback;
		this.mPosition = position;
		fill(contactListItemEntity);
	}
}