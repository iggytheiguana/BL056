package com.blulabellabs.code.ui.one2one;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.blulabellabs.code.R;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.sync.SyncHelper;
import com.blulabellabs.code.images.utils.ImageCache;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.common.GridAdapter;
import com.blulabellabs.code.ui.common.SeparatedListAdapter;
import com.blulabellabs.code.ui.one2one.ChatInsideFragment.One2OneChatInsideFragmentCallback;
import com.blulabellabs.code.utils.Converter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class ChatPhotosFragment extends Fragment {

	/**
	 * Load Images by url parameter for cache
	 */
	private static final String TAG = "ImageGridFragment";
	private static final String IMAGE_CACHE_DIR = "thumbs";

	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private ImageFetcher mImageFetcher;

	private static final String CHAT_ID = "chat_id";
	private static final String CHAT_COLOR = "chat_color";
	private static final String CHAT_NAME = "chat_name";
	private static final String QRCODE = "contact_qr";
	private static final String LOCATION = "location";
	private static final String DATE = "date";
	private static final String FIRST_UPDATE = "first_update";
	private One2OneChatInsideFragmentCallback callback;
	private boolean isViewCreated;
	SeparatedListAdapter mListAdapter;
	ListView mListViewPhotos;
	private ImageButton mBtnImageSend, mImgFavorite;

	private TextView mTextViewProfileName, mTextViewStatus;

	private static final SimpleDateFormat SIMPLE_DATE_FORMAT_MAIN = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT_HEADER = new SimpleDateFormat(
			"MMM dd yyyy", Locale.US);

	public static ChatPhotosFragment newInstance(Contact c, boolean firstUpdate) {
		ChatPhotosFragment f = new ChatPhotosFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putLong(CHAT_ID, c.chatId);
		args.putInt(CHAT_COLOR, c.color);
		args.putString(CHAT_NAME, c.title);
		args.putString(QRCODE, c.qrCode);
		args.putString(LOCATION, c.location);
		args.putString(DATE, c.date);
		args.putBoolean(FIRST_UPDATE, firstUpdate);
		// f.contact = c;
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_chat_photos, null);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		callback = (One2OneChatInsideFragmentCallback) activity;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final int height = displayMetrics.heightPixels;
		final int width = displayMetrics.widthPixels;

		final int longest = (height > width ? height : width) / 2;

		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(),
				IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(getActivity(), longest);// mImageThumbSize
		mImageFetcher.setLoadingImage(R.drawable.empty_photo);
		mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);

		mTextViewProfileName = (TextView) getView().findViewById(R.id.name);
		mTextViewStatus = (TextView) getView().findViewById(R.id.textView_status);
		// Log.e(">>>>>>>>>>SIZE", "==== " + mMapList.size());

		// SeparatedListAdapter adapter = new
		// SeparatedListAdapter(getActivity());
		mListAdapter = new SeparatedListAdapter(getActivity());

		mListViewPhotos = (ListView) getView().findViewById(R.id.listview);
		mListViewPhotos.setAdapter(mListAdapter);
		isViewCreated = true;

		mBtnImageSend = (ImageButton) getView().findViewById(R.id.btn_camera);
		mBtnImageSend.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				MainActivity activity = (MainActivity) getActivity();
				activity.setCurrentChatId(getArguments().getLong(CHAT_ID));
				activity.takePhoto();
			}
		});

		mImgFavorite = (ImageButton) getView().findViewById(R.id.btnFavorite);
		mImgFavorite.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int is_favorite = 1;
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
	}

	public long getChatId() {
		return getArguments().getLong(CHAT_ID, 0L);
	}

	public void sendImageMessage(String message) {

		// we need to send websocket message that the user has stopped typing
		callback.sendMessage(getChatId(), "", message, 1, -1, 0, 0, "", "");
	}

	class PhotoMessage {
		String date;
		ArrayList<Message> arrayList = new ArrayList<Message>();
	}

	public void updateUi() {
		if (isViewCreated && callback != null) {
			// mListAdapter.clear();
			// mListAdapter.addAll(callback.getChatMessages(getChatId()));
			mListAdapter = new SeparatedListAdapter((MainActivity) callback);
			// mListAdapter.clearData();
			// mListAdapter.notifyDataSetChanged();
			List<Message> messages = callback.getChatMessages(getChatId());

			mTextViewProfileName.setText(getArguments().getString(CHAT_NAME));
			MainActivity activity = (MainActivity) callback;
			ChatLoad chatLoad = activity.getChatLoad(getChatId());
			if (chatLoad != null) {
				mTextViewStatus.setText(chatLoad.status);
				if (chatLoad.is_favorite == 1) {
					Bitmap bm = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_chat_favorite);
					mImgFavorite.setImageBitmap(bm);
				} else {
					Bitmap bm = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_chat_favorite_h);
					mImgFavorite.setImageBitmap(bm);
				}
				
				
				if(chatLoad.is_deleted == 1){
					mImgFavorite.setClickable(false);
					mBtnImageSend.setVisibility(View.INVISIBLE);
				}
			}

			// mTextViewProfileName.setText(getArguments().getString(CHAT_NAME));
			if (messages != null) {
				Message previousMessage = null;
				List<Message> temp = new ArrayList<Message>();
				for (Message me : messages) {
					if (me.hasPhoto == 1) {
						temp.add(me);
					}
				}
				// for(Message message:temp){
				// messages.removeAll(temp);
				// }
				ArrayList<PhotoMessage> arrayListMessage = new ArrayList<ChatPhotosFragment.PhotoMessage>();
				for (Message me : temp) {
					if (previousMessage != null /*
												 * && !TextUtils.isEmpty(
												 * previousMessage .created )
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
							if (previousMessage.created != null
									&& (!previousMessage.created.contains(".")))
								preDate = preDate + ".000";
							// Log.d("preDate", preDate);
							previousDate.setTime(SIMPLE_DATE_FORMAT_MAIN.parse(preDate));

							if (currentDate.get(Calendar.DATE) != previousDate.get(Calendar.DATE)) {
								Date dateTemp = new Date(
										Converter.getCrurentTimeFromTimestamp(date));
								// Converter.getCrurentTimeFromTimestamp(me.created)
								// getDateHeader().setText(SIMPLE_DATE_FORMAT_HEADER.format(dateTemp));
								// getHeaderContainer().setVisibility(View.VISIBLE);
								PhotoMessage photoMessage = new PhotoMessage();
								if (me.created != null && !(me.created.contains(".")))
									date += ".000";
								photoMessage.date = SIMPLE_DATE_FORMAT_HEADER.format(dateTemp);
								photoMessage.arrayList.add(me);
								arrayListMessage.add(photoMessage);
							} else if (!me.qrcode.equalsIgnoreCase(previousMessage.qrcode)) {
								// getOpponentSeparator().setVisibility(View.VISIBLE);
							} else {
								if (arrayListMessage.size() > 0) {
									PhotoMessage message = arrayListMessage.get(arrayListMessage
											.size() - 1);
									message.arrayList.add(me);
								}
							}

							// if
							// (me.qrcode.equalsIgnoreCase(previousMessage.qrcode)
							// && currentDate.get(Calendar.MINUTE) ==
							// previousDate
							// .get(Calendar.MINUTE)
							// && currentDate.get(Calendar.HOUR_OF_DAY) ==
							// previousDate
							// .get(Calendar.HOUR_OF_DAY)) {
							//
							// // getDate().setVisibility(View.INVISIBLE);
							// // getDate().setVisibility(View.VISIBLE);
							// } else {
							// // getDate().setVisibility(View.VISIBLE);
							// }

						} catch (ParseException e) {
							e.printStackTrace();
						}
					} else {
						PhotoMessage photoMessage = new PhotoMessage();
						String date = me.created;
						if (me.created != null && !(me.created.contains(".")))
							date += ".000";
						Date dateTemp = new Date(Converter.getCrurentTimeFromTimestamp(date));
						photoMessage.date = SIMPLE_DATE_FORMAT_HEADER.format(dateTemp);
						photoMessage.arrayList.add(me);
						arrayListMessage.add(photoMessage);
					}
					previousMessage = me;
				}

				for (PhotoMessage photoMessage : arrayListMessage) {
					ArrayList<HashMap<String, Message>> mMapList = new ArrayList<HashMap<String, Message>>();
					int count = 0;
					if (photoMessage.arrayList.size() % 2 == 0) {
						count = photoMessage.arrayList.size() / 2;
					} else {
						count = (photoMessage.arrayList.size() / 2) + 1;
					}

					int j = 0;
					for (int i = 0; i < count; i++) {
						HashMap<String, Message> mHashMapLeft = new HashMap<String, Message>();

						mHashMapLeft.put("0", photoMessage.arrayList.get(j));
						if ((j + 1) <= (photoMessage.arrayList.size() - 1)) {
							mHashMapLeft.put("1", photoMessage.arrayList.get(j + 1));
						}
						mMapList.add(mHashMapLeft);
						j = j + 2;
					}
					mListAdapter.addSection(photoMessage.date, new GridAdapter(getActivity(),
							mMapList, mImageFetcher));
				}
				mListViewPhotos.setAdapter(mListAdapter);
			}
			// mListAdapter.notifyDataSetChanged();
			// mName.setText(getChatName());
			// mName.setTextColor(getChatColor());
			// if (QodemePreferences.getInstance().isSaveLocationDateChecked())
			// {
			// if (getDate() != null) {
			// SimpleDateFormat fmtOut = new
			// SimpleDateFormat("MM/dd/yy HH:mm a");
			// // String dateStr = fmtOut.format(new
			// // Date(Timestamp.valueOf(getDate()).getTime()));
			// String dateStr = fmtOut.format(new Date(Converter
			// .getCrurentTimeFromTimestamp(getDate())));
			// mDate.setText(dateStr + ",");
			// } else
			// mDate.setText("");
			// mLocation.setText(getLocation());
			// } else {
			// mDate.setText("");
			// mLocation.setText("");
			// }

			// String message = ChatFocusSaver.getCurrentMessage(getChatId());

			// if (!TextUtils.isEmpty(message)) {
			// mMessageField.setText(message);
			// // Helper.showKeyboard(getActivity(), mMessageField);
			// // mMessageField.setSelection(mMessageField.getText().length());
			// mMessageField.post(new Runnable() {
			// @Override
			// public void run() {
			// mMessageField.requestFocus();
			// mMessageField.setSelection(mMessageField.getText().length());
			// Context c = getActivity();
			// if (c != null)
			// Helper.showKeyboard(getActivity(), mMessageField);
			// mFirstUpdate = false;
			// }
			// });
			// } else if (getFirstUpdate()) {
			// Helper.hideKeyboard(getActivity(), mMessageField);
			// }
			//
			// }
		}
	}
}
