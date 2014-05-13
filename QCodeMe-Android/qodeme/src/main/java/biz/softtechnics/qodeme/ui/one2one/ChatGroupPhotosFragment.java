package biz.softtechnics.qodeme.ui.one2one;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.io.model.ChatLoad;
import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.core.io.model.Message;
import biz.softtechnics.qodeme.ui.common.GridAdapter;
import biz.softtechnics.qodeme.ui.common.SeparatedListAdapter;
import biz.softtechnics.qodeme.ui.one2one.ChatInsideFragment.One2OneChatInsideFragmentCallback;
import biz.softtechnics.qodeme.utils.ChatFocusSaver;
import biz.softtechnics.qodeme.utils.Converter;
import biz.softtechnics.qodeme.utils.Helper;

public class ChatGroupPhotosFragment extends Fragment {

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
	public ChatLoad chatLoad;

	private static final SimpleDateFormat SIMPLE_DATE_FORMAT_MAIN = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT_HEADER = new SimpleDateFormat(
			"MMM dd yyyy", Locale.US);

	public static ChatGroupPhotosFragment newInstance(ChatLoad c, boolean firstUpdate) {
		ChatGroupPhotosFragment f = new ChatGroupPhotosFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putLong(CHAT_ID, c.chatId);
		args.putInt(CHAT_COLOR, c.color);
		args.putString(CHAT_NAME, c.title);
		args.putString(QRCODE, c.qrcode);
//		args.putString(LOCATION, c.location);
//		args.putString(DATE, c.date);
		args.putBoolean(FIRST_UPDATE, firstUpdate);
		f.chatLoad = c;
		// f.contact = c;
		f.setArguments(args);
		return f;
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

		ArrayList<String> arrayList = new ArrayList<String>();
		ArrayList<HashMap<String, String>> mMapList = new ArrayList<HashMap<String, String>>();

		arrayList.add("a");
		arrayList.add("b");
		arrayList.add("c");
		int count = 0;
		if (arrayList.size() % 2 == 0) {
			count = arrayList.size() / 2;
		} else {
			count = (arrayList.size() / 2) + 1;
		}

		int j = 0;
		for (int i = 0; i < count; i++) {
			HashMap<String, String> mHashMapLeft = new HashMap<String, String>();

			mHashMapLeft.put("0", arrayList.get(j) + "");
			if ((j + 1) <= (arrayList.size() - 1)) {
				mHashMapLeft.put("1", arrayList.get(j + 1) + "");
			}
			mMapList.add(mHashMapLeft);
			j = j + 2;
		}
		// Log.e(">>>>>>>>>>SIZE", "==== " + mMapList.size());

//		SeparatedListAdapter adapter = new SeparatedListAdapter(getActivity());
		mListAdapter  = new SeparatedListAdapter(getActivity());

//		adapter.addSection("April 29, 2014", new GridAdapter(getActivity(), mMapList));
//
//		ArrayList<String> dd = new ArrayList<String>();
//		dd.add("a");
//		dd.add("b");
//		dd.add("c");
//		dd.add("d");
//		if (dd.size() % 2 == 0) {
//			count = dd.size() / 2;
//		} else {
//			count = (dd.size() / 2) + 1;
//		}
//
//		j = 0;
//		ArrayList<HashMap<String, String>> mm = new ArrayList<HashMap<String, String>>();
//		for (int i = 0; i < count; i++) {
//			HashMap<String, String> mHashMapLeft = new HashMap<String, String>();
//
//			mHashMapLeft.put("0", dd.get(j) + "");
//			if ((j + 1) <= (dd.size() - 1)) {
//				mHashMapLeft.put("1", dd.get(j + 1) + "");
//			}
//			mm.add(mHashMapLeft);
//			j = j + 2;
//		}
//
//		adapter.addSection("April 30, 2014", new GridAdapter(getActivity(), mm));

		mListViewPhotos = (ListView) getView().findViewById(R.id.listview);
		isViewCreated = true;
		updateUi();
	}

	public long getChatId() {
		return getArguments().getLong(CHAT_ID, 0L);
	}

	public void sendImageMessage(String message) {

		// we need to send websocket message that the user has stopped typing
		callback.sendMessage(getChatId(), "", message, 1, -1, 0, 0, "");
	}

	class PhotoMessage {
		String date;
		ArrayList<Message> arrayList = new ArrayList<Message>();
	}

	public void updateUi() {
		if (isViewCreated) {
			// mListAdapter.clear();
			// mListAdapter.addAll(callback.getChatMessages(getChatId()));
			mListAdapter = new SeparatedListAdapter(getActivity());
			List<Message> messages = callback.getChatMessages(getChatId());
			Message previousMessage = null;
			List<Message> temp = new ArrayList<Message>();
			for (Message me : messages) {
				if (me.hasPhoto == 1) {
					temp.add(me);
				}
			}
			// for(Message message:temp){
//			messages.removeAll(temp);
			// }
			ArrayList<PhotoMessage> arrayListMessage = new ArrayList<ChatGroupPhotosFragment.PhotoMessage>();
			for (Message me : temp) {
				if (previousMessage != null /*
											 * &&
											 * !TextUtils.isEmpty(previousMessage
											 * .created )
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
							Date dateTemp = new Date(Converter.getCrurentTimeFromTimestamp(date));
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
								PhotoMessage message = arrayListMessage
										.get(arrayListMessage.size() - 1);
								message.arrayList.add(me);
							}
						}

						// if
						// (me.qrcode.equalsIgnoreCase(previousMessage.qrcode)
						// && currentDate.get(Calendar.MINUTE) == previousDate
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
				mListAdapter.addSection(photoMessage.date, new GridAdapter(getActivity(), mMapList));
			}
			mListViewPhotos.setAdapter(mListAdapter);
		}
		// mName.setText(getChatName());
		// mName.setTextColor(getChatColor());
		// if (QodemePreferences.getInstance().isSaveLocationDateChecked()) {
		// if (getDate() != null) {
		// SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yy HH:mm a");
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
