package com.blulabellabs.code.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.provider.QodemeContract.Contacts.Sync;
import com.blulabellabs.code.core.sync.SyncHelper;
import com.blulabellabs.code.images.utils.ImageCache;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.images.utils.ImageResizer;
import com.blulabellabs.code.images.utils.Utils;
import com.blulabellabs.code.ui.common.CustomDotView;
import com.blulabellabs.code.utils.Converter;
import com.blulabellabs.code.utils.DbUtils;
import com.blulabellabs.code.utils.Helper;

public class FlaggedMessageListActivity extends ActionBarActivity {
	private static final String MEMBER_LIST = "MemberList";
	private static final String MESSAGE_LIST = "MessageList";
	private static final String CHAT_ID = "chat_id";
	ListView mListView;
	ImageFetcher fetcher;
	private static final String IMAGE_CACHE_DIR = "thumbs";

	private long chat_id;
	private ArrayList<Contact> arrayListContact;
	private ArrayList<Message> arrayListMessages;

	MemberListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().hide();

		setContentView(R.layout.fragment_group_flagged_messages);

		initImageFetcher();

		mListView = (ListView) findViewById(R.id.listview_groupMember);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			chat_id = bundle.getLong(CHAT_ID);
			arrayListContact = bundle.getParcelableArrayList(MEMBER_LIST);
			arrayListMessages = bundle.getParcelableArrayList(MESSAGE_LIST);
		}

		adapter = new MemberListAdapter(this);
		// if (arrayList != null) {
		// for (Contact c : arrayList)
		// adapter.add(c);
		// }
		// adapter.add(new Contact());
		// adapter.add(new Contact());
		// adapter.add(new Contact());

		if (arrayListMessages != null) {
			for (Message msg : arrayListMessages)
				adapter.add(msg);
		}
		mListView.setAdapter(adapter);

		// getSupportFragmentManager().beginTransaction()
		// .replace(R.id.container,
		// GroupMemberFragment.newInstance(null)).commit();

	}

	private void initImageFetcher() {
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final int height = displayMetrics.heightPixels;
		final int width = displayMetrics.widthPixels;

		final int longest = (height > width ? height : width) / 2;

		// mImageThumbSize =
		// getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		// mImageThumbSpacing =
		// getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(this,
				IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		fetcher = new ImageFetcher(this, longest);// mImageThumbSize
		// mImageFetcher.setLoadingImage(R.drawable.empty_photo);
		fetcher.addImageCache(getSupportFragmentManager(), cacheParams);
	}

	@SuppressLint("SimpleDateFormat")
	class MemberListAdapter extends ArrayAdapter<Message> {

		public MemberListAdapter(Context context) {
			super(context, 0);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = getLayoutInflater().inflate(R.layout.group_chat_flag_list_item, null);

				holder.textViewMessage = (TextView) convertView.findViewById(R.id.message);
				holder.customDotView = (CustomDotView) convertView.findViewById(R.id.customDotView);
				holder.imageLayout = (LinearLayout) convertView.findViewById(R.id.linearLayout_img);
				holder.imageView = (ImageView) convertView.findViewById(R.id.imageView_item);
				holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar_img);

				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();

			final Message me = getItem(position);
			holder.textViewMessage.setText(me.message);

			if (me.hasPhoto == 1) {
				// String sss = me.photoUrl;
				// byte data[] = Base64.decode(sss, Base64.NO_WRAP);
				// Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
				// data.length);
				// getImageMessage().setImageBitmap(bitmap);
				if (me.localImgPath != null && !me.localImgPath.trim().equals("")) {
					int size = 200;
					// ImageFetcher fetcher = callback2.getImageFetcher();
					if (fetcher != null)
						size = fetcher.getRequiredSize();

					Bitmap bitmap = ImageResizer.decodeSampledBitmapFromFile(me.localImgPath, size,
							size, null);
					// getImageMessage().setImageURI(Uri.parse(me.localImgPath));
					holder.imageView.setImageBitmap(bitmap);
					holder.progressBar.setVisibility(View.GONE);
				} else {
					Log.d("imgUrl", me.photoUrl + "");
					// ImageFetcher fetcher = callback2.getImageFetcher();
					if (fetcher != null)
						fetcher.loadImage(me.photoUrl, holder.imageView, holder.progressBar);
				}
				// ImageFetcher fetcher = callback2.getImageFetcher();
				// if (fetcher != null)
				// fetcher.loadImage(me.photoUrl, getImageMessage(),
				// getImageProgress());
				holder.imageView.setVisibility(View.VISIBLE);
				holder.imageLayout.setVisibility(View.VISIBLE);
				holder.imageView.setOnClickListener(new OnClickListener() {

					@SuppressLint("NewApi")
					@Override
					public void onClick(View v) {
						final Intent i = new Intent(getContext(), ImageDetailActivity.class);
						i.putExtra(ImageDetailActivity.EXTRA_IMAGE, me.photoUrl);
						i.putExtra("flag", me.is_flagged);
						i.putExtra("message_id", me.messageId);
						if (Utils.hasJellyBean()) {
							ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0,
									v.getWidth(), v.getHeight());
							getContext().startActivity(i, options.toBundle());
						} else {
							getContext().startActivity(i);
						}
					}
				});
				holder.imageView.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						showPopupMenu(holder.textViewMessage, me);
						return true;
					}
				});
			} else {
				holder.imageView.setImageBitmap(null);
				holder.imageView.setVisibility(View.GONE);
				holder.imageLayout.setVisibility(View.GONE);
			}
			int color;// = callback.getColor(me.qrcode);
			Contact contact = getContact(me.qrcode);
			if (contact != null) {
				color = contact.color;
			} else {
				color = Color.GRAY;
			}
			holder.customDotView.setDotColor(color);
			holder.customDotView.invalidate();
			String dateString = "";
			String createdDate = me.created;
			try {

				if (me.hasPhoto == 1) {
					try {
						if (me.created != null && !(me.created.contains(".")))
							createdDate += ".000";
						SimpleDateFormat SIMPLE_DATE_FORMAT_MAIN = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss.SSS");
						Date currentDate = SIMPLE_DATE_FORMAT_MAIN.parse(createdDate);

						SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd");
						dateString = " " + dateFormat.format(currentDate);

					} catch (Exception e) {
						dateString = Helper.getLocalTimeFromGTM(me.created);// Helper.getTimeAMPM(Converter.getCrurentTimeFromTimestamp(createdDate));
						dateString = " " + dateString;
					}
				} else {

					dateString = Helper.getLocalTimeFromGTM(me.created);// Helper.getTimeAMPM(Converter.getCrurentTimeFromTimestamp(createdDate));
					dateString = " " + dateString;
				}
			} catch (Exception e) {
				Log.d("timeError", e + "");
				dateString = Helper.getTimeAMPM(Converter.getCrurentTimeFromTimestamp(createdDate));
				dateString = " " + dateString;
			}
			String str = me.message;

			String mainString = str + dateString + " ";

			// Create our span sections, and assign a format to each.
			SpannableString ss1 = new SpannableString(mainString);
			ss1.setSpan(new RelativeSizeSpan(0.6f), str.length(), mainString.length(), 0); // set
																							// size
			ss1.setSpan(new ForegroundColorSpan(Color.GRAY), str.length(), mainString.length(), 0); // set
																									// size
			holder.textViewMessage.setText(ss1);

			holder.textViewMessage.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					showPopupMenu(holder.textViewMessage, me);
					return true;
				}
			});

			return convertView;

		}

		class ViewHolder {
			TextView textViewMessage;
			CustomDotView customDotView;
			ImageView imageView;
			LinearLayout imageLayout;
			ProgressBar progressBar;
		}
	}

	private Contact getContact(String qrCode) {

		if (arrayListContact != null) {
			for (Contact c : arrayListContact) {
				if (c.qrCode.equals(qrCode))
					return c;
			}
		}
		return null;
	}

	private void showPopupMenu(View v, final Message message) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.flag_context_menu_layout, null);

		final PopupWindow popupWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);

		if (QodemePreferences.getInstance().getQrcode().equals(message.qrcode)) {
			view.findViewById(R.id.textView_block).setVisibility(View.GONE);
			view.findViewById(R.id.view_divider2).setVisibility(View.GONE);
		}
		view.findViewById(R.id.textView_deleteMessage).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// getContentResolver().insert(QodemeContract.Contacts.CONTENT_URI,
				// QodemeContract.Contacts.addNewContactValues(message.qrcode));
				// SyncHelper.requestManualSync();
				getContentResolver().update(QodemeContract.Messages.CONTENT_URI,
						QodemeContract.Messages.deleteMessage(), DbUtils.getWhereClauseForId(),
						DbUtils.getWhereArgsForId(message._id));
				SyncHelper.requestManualSync();
				adapter.remove(message);
				popupWindow.dismiss();
			}
		});
		view.findViewById(R.id.textView_block).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getContentResolver()
						.update(QodemeContract.Contacts.CONTENT_URI,
								QodemeContract.Contacts.blockContactValues(Sync.STATE_UPDATED),
								QodemeContract.Contacts.CONTACT_QRCODE + "= '" + message.qrcode
										+ "'", null);
				SyncHelper.requestManualSync();

				popupWindow.dismiss();

			}
		});
		view.findViewById(R.id.textView_unflagged).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getContentResolver().update(QodemeContract.Messages.CONTENT_URI,
						QodemeContract.Messages.updateMessageUnFlagged(),
						DbUtils.getWhereClauseForId(), DbUtils.getWhereArgsForId(message._id));
				SyncHelper.requestManualSync();
				adapter.remove(message);
				popupWindow.dismiss();
			}
		});

		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setContentView(view);
		popupWindow.setOutsideTouchable(true);
		popupWindow.showAsDropDown(v);
	}
}
