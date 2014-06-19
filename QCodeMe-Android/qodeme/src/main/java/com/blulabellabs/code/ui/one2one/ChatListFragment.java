package com.blulabellabs.code.ui.one2one;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.blulabellabs.code.R;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.MainActivity.LoadMoreChatListener;
import com.blulabellabs.code.ui.common.ExListAdapter;
import com.blulabellabs.code.ui.common.ScrollDisabledListView;
import com.blulabellabs.code.utils.ChatFocusSaver;
import com.blulabellabs.code.utils.Fonts;
import com.google.common.collect.Lists;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

/**
 * Created by Alex on 10/7/13.
 */
@SuppressLint("DefaultLocale")
@SuppressWarnings("unused")
public class ChatListFragment extends Fragment {

	private One2OneChatListFragmentCallback callback;
	private boolean isViewCreated = false;
	private ScrollDisabledListView mListView;
	private ExListAdapter<ChatListItem, Contact, ChatListAdapterCallback> mListAdapter;
	private View mMessageLayout;
	private ImageButton mMessageButton, mImgBtnSearch, mImgBtnClear, mImgBtnLocationFilter,
			mImgBtnFavoriteFilter;
	private EditText mMessageEdit;
	int lastVisibleItem = 0;
	boolean isScrollingDown = false;
	private EditText mEditTextSearch;
	private LinearLayout mLinearLayoutSearch;
	int lastFirstvisibleItem = 0;

	int pageNo = 1;
	boolean isMoreData = true;
	private boolean isThreadRunning;
	private LinearLayout mFooterLayout;
	private String searchString = "";

	private static final String TAG = "ChatListFragment";
	private final WebSocketConnection mConnection = new WebSocketConnection();
	protected boolean isLocationFilter = false;
	protected boolean isFavoriteFilter = false;

	public interface One2OneChatListFragmentCallback {
		List<Contact> getContactList();

		List<ChatLoad> getChatList(int type);

		List<Message> getChatMessages(long chatId);

		void sendMessage(long chatId, String message, String photoUrl, int hashPhoto,
				long replyTo_Id, double latitude, double longitude, String senderName,
				String localUrl);

		int getHeight(long chatId);

		void setChatHeight(long chatId, int height);

		void showChat(Contact c, boolean firstUpdate, View view);

		void showChat(ChatLoad c, boolean firstUpdate, View view);

		Typeface getFont(Fonts font);

		int getNewMessagesCount(long chatId);

		void messageRead(long chatId);

		Contact getContact(String qrString);

		ImageFetcher getImageFetcher();

		int getChatType(long chatId);

		ChatLoad getChatLoad(long chatId);
	}

	public static ChatListFragment getInstance(){
		ChatListFragment chatListFragment = new ChatListFragment();
		Bundle args = new Bundle();
		args.putInt("index", 1);
		chatListFragment.setArguments(args);
		return chatListFragment;
	}
	
	@Override
	public void onResume() {
		super.onResume();

		// we need to start up the websocket connection again
		start();
	}

	@Override
	public void onPause() {
		super.onPause();

		// we need to close the websocket connection
		stop();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_one2one_chat_list, null);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		callback = (One2OneChatListFragmentCallback) activity;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mMessageLayout = getView().findViewById(R.id.layout_message);
		mMessageButton = (ImageButton) getView().findViewById(R.id.button_message);
		mMessageEdit = (EditText) getView().findViewById(R.id.edit_message);
		mLinearLayoutSearch = (LinearLayout) getView().findViewById(R.id.linearLayout_search);
		mImgBtnSearch = (ImageButton) getView().findViewById(R.id.imgBtn_search);
		mImgBtnClear = (ImageButton) getView().findViewById(R.id.imgBtn_clear);
		mImgBtnLocationFilter = (ImageButton) getView().findViewById(R.id.imgBtn_locationFilter);

		mEditTextSearch = (EditText) getView().findViewById(R.id.editText_Search);
		mImgBtnFavoriteFilter = (ImageButton) getView().findViewById(R.id.imgBtn_favoriteFilter);

		initListView();
		isViewCreated = true;

		// we add a listener to the image button so we can track when a person
		// stops/starts typing
		mMessageEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				Log.d(TAG, "beforeTextChanged");
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				Log.d(TAG, "onTextChanged");
			}

			@Override
			public void afterTextChanged(Editable editable) {
				Log.d(TAG, "afterTextChanged");
			}
		});
		mImgBtnClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MainActivity activity = (MainActivity) callback;
				activity.setOneToOneSearch(false);
				activity.setOneToOneSearchString("");
				mEditTextSearch.setText("");
				mImgBtnClear.setVisibility(View.GONE);
				mImgBtnSearch.setVisibility(View.VISIBLE);
				mEditTextSearch.setEnabled(true);
				mEditTextSearch.setText("");
				pageNo = 1;
				searchString = "";
				Bitmap bm = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_location_gray);
				mImgBtnLocationFilter.setImageBitmap(bm);
				isLocationFilter = false;

				isFavoriteFilter = false;
				Bitmap bm1 = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_chat_favorite_h);
				mImgBtnFavoriteFilter.setImageBitmap(bm1);
				updateUi();
			}
		});
		MainActivity activity = (MainActivity) callback;
		if (activity.isOneToOneSearch()) {
			searchString = activity.getOneToOneSearchString();
			mEditTextSearch.setText(searchString);
			mImgBtnClear.setVisibility(View.VISIBLE);
			mImgBtnSearch.setVisibility(View.GONE);
		} else {
			mImgBtnClear.setVisibility(View.GONE);
			mImgBtnSearch.setVisibility(View.VISIBLE);
		}

		mImgBtnLocationFilter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isFavoriteFilter) {
					isFavoriteFilter = false;
					Bitmap bm = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_chat_favorite_h);
					mImgBtnFavoriteFilter.setImageBitmap(bm);
				}

				if (isLocationFilter) {
					isLocationFilter = false;
					Bitmap bm = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_location_gray);
					mImgBtnLocationFilter.setImageBitmap(bm);
					updateUi();
				} else {
					Bitmap bm = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_location_blue_big);
					mImgBtnLocationFilter.setImageBitmap(bm);
					isLocationFilter = true;
					List<Contact> temp = Lists.newArrayList();
					List<Contact> searchList = searchContact(searchString);
					for (Contact c : searchList) {
						ChatLoad chatLoad = callback.getChatLoad(c.chatId);

						if (chatLoad != null && chatLoad.latitude != null
								&& chatLoad.longitude != null && !chatLoad.latitude.equals("")
								&& !chatLoad.longitude.equals("") && !chatLoad.latitude.equals("0")
								&& !chatLoad.latitude.equals("0.0")
								&& !chatLoad.latitude.equals("-1")
								&& !chatLoad.longitude.equals("0")
								&& !chatLoad.longitude.equals("0.0")
								&& !chatLoad.longitude.equals("-1")) {
							Log.d("latLong", chatLoad.latitude + " " + chatLoad.longitude);
							Log.d("latLong", c.latitude + " " + c.longitude);
							temp.add(c);
						}
					}

					Collections.sort(temp, new CustomComparator());
					mListAdapter.clear();
					mListAdapter.addAll(temp);
				}
			}
		});

		mImgBtnFavoriteFilter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isLocationFilter) {
					isLocationFilter = false;
					Bitmap bm = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_location_gray);
					mImgBtnLocationFilter.setImageBitmap(bm);
				}
				if (isFavoriteFilter) {
					isFavoriteFilter = false;
					Bitmap bm = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_chat_favorite_h);
					mImgBtnFavoriteFilter.setImageBitmap(bm);
					updateUi();
				} else {
					Bitmap bm = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_star_blue);
					mImgBtnFavoriteFilter.setImageBitmap(bm);
					isFavoriteFilter = true;
					List<Contact> temp = Lists.newArrayList();
					List<Contact> searchList = searchContact(searchString);
					for (Contact c : searchList) {
						ChatLoad chatLoad = callback.getChatLoad(c.chatId);

						if (chatLoad != null && chatLoad.is_favorite == 1) {
							temp.add(c);
						}
					}

					// Collections.sort(temp, new CustomComparator());
					mListAdapter.clear();
					mListAdapter.addAll(temp);
				}
			}
		});

		updateUi();

		mImgBtnSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// if (isLocationFilter) {
				// isLocationFilter = false;
				// Bitmap bm = BitmapFactory.decodeResource(getResources(),
				// R.drawable.ic_location_gray);
				// mImgBtnLocationFilter.setImageBitmap(bm);
				// }
				// if (isFavoriteFilter) {
				// isFavoriteFilter = false;
				// Bitmap bm = BitmapFactory.decodeResource(getResources(),
				// R.drawable.ic_chat_favorite);
				// mImgBtnFavoriteFilter.setImageBitmap(bm);
				// }

				String data = mEditTextSearch.getText().toString();
				searchString = data;
				MainActivity activity = (MainActivity) callback;
				activity.setOneToOneSearch(true);
				activity.setOneToOneSearchString(data);

				List<Contact> searchList = searchContact(searchString);

				List<Contact> temp = Lists.newArrayList();
				if (isFavoriteFilter) {
					for (Contact c : searchList) {
						ChatLoad chatLoad = callback.getChatLoad(c.chatId);

						if (chatLoad != null && chatLoad.is_favorite == 1) {
							temp.add(c);
						}
					}
				} else if (isLocationFilter) {
					// List<Contact> temp = Lists.newArrayList();
					// List<Contact> searchList =
					// searchContact(searchString);
					for (Contact c : searchList) {
						ChatLoad chatLoad = callback.getChatLoad(c.chatId);

						if (chatLoad != null && chatLoad.latitude != null
								&& chatLoad.longitude != null && !chatLoad.latitude.equals("")
								&& !chatLoad.longitude.equals("") && !chatLoad.latitude.equals("0")
								&& !chatLoad.latitude.equals("0.0")
								&& !chatLoad.latitude.equals("-1")
								&& !chatLoad.longitude.equals("0")
								&& !chatLoad.longitude.equals("0.0")
								&& !chatLoad.longitude.equals("-1")) {
							Log.d("latLong", chatLoad.latitude + " " + chatLoad.longitude);
							Log.d("latLong", c.latitude + " " + c.longitude);
							temp.add(c);
						}
					}

					Collections.sort(temp, new CustomComparator());
				} else {
					temp.addAll(searchList);
				}

				mListAdapter.clear();
				mListAdapter.addAll(temp);

				mImgBtnClear.setVisibility(View.VISIBLE);
				mImgBtnSearch.setVisibility(View.GONE);
			}
		});
		mEditTextSearch.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH
						|| actionId == EditorInfo.IME_ACTION_GO
						|| actionId == EditorInfo.IME_ACTION_DONE
						|| event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

					String data = v.getText().toString();
					searchString = data;
					MainActivity activity = (MainActivity) callback;
					activity.setOneToOneSearch(true);
					activity.setOneToOneSearchString(data);
					// activity.searchChats(data, 0, 1, chatListener);
					// mEditTextSearch.setEnabled(false);
					// isMoreData = true;
					// Bitmap bm = BitmapFactory.decodeResource(getResources(),
					// R.drawable.ic_location_gray);
					// mImgBtnLocationFilter.setImageBitmap(bm);
					// isLocationFilter = false;
					//
					// isFavoriteFilter = false;
					// Bitmap bm1 = BitmapFactory.decodeResource(getResources(),
					// R.drawable.ic_chat_favorite);
					// mImgBtnFavoriteFilter.setImageBitmap(bm1);

					// List<Contact> temp = searchContact(searchString);
					List<Contact> searchList = searchContact(searchString);

					List<Contact> temp = Lists.newArrayList();
					if (isFavoriteFilter) {
						for (Contact c : searchList) {
							ChatLoad chatLoad = callback.getChatLoad(c.chatId);

							if (chatLoad != null && chatLoad.is_favorite == 1) {
								temp.add(c);
							}
						}
					} else if (isLocationFilter) {
						// List<Contact> temp = Lists.newArrayList();
						// List<Contact> searchList =
						// searchContact(searchString);
						for (Contact c : searchList) {
							ChatLoad chatLoad = callback.getChatLoad(c.chatId);

							if (chatLoad != null && chatLoad.latitude != null
									&& chatLoad.longitude != null && !chatLoad.latitude.equals("")
									&& !chatLoad.longitude.equals("")
									&& !chatLoad.latitude.equals("0")
									&& !chatLoad.latitude.equals("0.0")
									&& !chatLoad.latitude.equals("-1")
									&& !chatLoad.longitude.equals("0")
									&& !chatLoad.longitude.equals("0.0")
									&& !chatLoad.longitude.equals("-1")) {
								Log.d("latLong", chatLoad.latitude + " " + chatLoad.longitude);
								Log.d("latLong", c.latitude + " " + c.longitude);
								temp.add(c);
							}
						}

						Collections.sort(temp, new CustomComparator());
					} else {
						temp.addAll(searchList);
					}

					mListAdapter.clear();
					mListAdapter.addAll(temp);

					mImgBtnClear.setVisibility(View.VISIBLE);
					mImgBtnSearch.setVisibility(View.GONE);
					return true;
				}
				return false;
			}
		});
	}

	public class CustomComparator implements Comparator<Contact> {

		@Override
		public int compare(Contact lhs, Contact rhs) {
			MainActivity activity = (MainActivity) callback;
			Location location = activity.getCurrentLocation();
			Integer leftDistance = 0;
			Integer rightDistance = 0;
			ChatLoad lhsChat = callback.getChatLoad(lhs.chatId);
			ChatLoad rhsChat = callback.getChatLoad(rhs.chatId);
			if (location != null && lhsChat != null && rhsChat != null) {
				try {
					double lat = Double.parseDouble(lhsChat.latitude);
					double lng = Double.parseDouble(lhsChat.longitude);
					leftDistance = (int) distance(lat, lng, location.getLatitude(),
							location.getLongitude(), 'M');

				} catch (Exception e) {
				}
				try {
					double lat = Double.parseDouble(rhsChat.latitude);
					double lng = Double.parseDouble(rhsChat.longitude);
					rightDistance = (int) distance(lat, lng, location.getLatitude(),
							location.getLongitude(), 'M');
				} catch (Exception e) {
				}
			}
			return leftDistance.compareTo(rightDistance);
		}

	}

	private void start() {
		final String wsuri = "ws://54.204.45.228/python";

		try {
			if (!mConnection.isConnected()) {
				mConnection.connect(wsuri, new WebSocketHandler() {

					@Override
					public void onOpen() {
						Log.d(TAG, "Status: Connected to " + wsuri);
						// mConnection.sendTextMessage("Hello, world!");
					}

					@Override
					public void onTextMessage(String payload) {
						Log.d(TAG, "Got echo: " + payload);
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
				// let's disconnect the socket
				mConnection.disconnect();
				Log.d(TAG, "Disconnected web socket");
			}

		} catch (Exception e) {
			Log.d(TAG, e.toString());

		}
	}

	private void initListView() {
		mListView = (ScrollDisabledListView) getView().findViewById(R.id.listview);

		View headerSearchView = getLayoutInflater(getArguments()).inflate(
				R.layout.linear_search_header, null);

		View footerView = getLayoutInflater(getArguments()).inflate(R.layout.list_footer_load_more,
				null);
		mFooterLayout = (LinearLayout) footerView.findViewById(R.id.list_footer);
		mListView.addHeaderView(headerSearchView);
		// mListView.addFooterView(footerView);

		List<Contact> listForAdapter = Lists.newArrayList();
		// mListView.setEmptyView(getView().findViewById(R.id.empty_view));

		mListAdapter = new ExListAdapter<ChatListItem, Contact, ChatListAdapterCallback>(
				getActivity(), R.layout.one2one_chat_list_item, listForAdapter,
				new ChatListAdapterCallback() {

					public void onSingleTap(View view, int position, Contact ce) {
						// TODO
					}

					public void onDoubleTap(View view, int position, Contact c) {
						InputMethodManager imm = (InputMethodManager) getActivity()
								.getSystemService(Activity.INPUT_METHOD_SERVICE);
						imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
						callback.showChat(c, true, view);
					}

					public int getChatHeight(long chatId) {
						return callback.getHeight(chatId);
					}

					public void setChatHeight(long chatId, int height) {
						callback.setChatHeight(chatId, height);
					}

					public void setDragModeEnabled(boolean value) {
						mListView.setDragMode(value);
					}

					// public void sendMessage(Contact c, String message) {
					// callback.sendMessage(c.chatId, message);
					// }

					public List<Message> getMessages(Contact c) {
						return callback.getChatMessages(c.chatId);
					}

					@Override
					public Typeface getFont(Fonts font) {
						return callback.getFont(font);
					}

					@Override
					public void refreshUi() {
						updateUi();
					}

					@Override
					public int getNewMessagesCount(long chatId) {
						return callback.getNewMessagesCount(chatId);

					}

					@Override
					public void messageRead(long chatId) {
						callback.messageRead(chatId);
					}

					@Override
					public void sendMessage(Contact c, String message, String photoUrl,
							int hashPhoto, long replyTo_Id, double latitude, double longitude,
							String senderName, String localUrl) {
						callback.sendMessage(c.chatId, message, photoUrl, hashPhoto, replyTo_Id,
								latitude, longitude, senderName, localUrl);

					}

					@Override
					public List<Message> getMessages(long chatId) {
						return null;
					}

					@Override
					public void sendMessage(long c, String message, String photoUrl, int hashPhoto,
							long replyTo_Id, double latitude, double longitude, String senderName,
							String localUrl) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onDoubleTap(View view, int position, ChatLoad c) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onSingleTap(View view, int position, ChatLoad c) {
						// TODO Auto-generated method stub

					}

					@Override
					public Contact getContact(String qrCode) {
						// TODO Auto-generated method stub
						return callback.getContact(qrCode);
					}

					@Override
					public ImageFetcher getImageFetcher() {
						return callback.getImageFetcher();
					}

					@Override
					public int getChatType(long chatId) {
						// TODO Auto-generated method stub
						return callback.getChatType(chatId);
					}

					@Override
					public ChatLoad getChatLoad(long chatId) {
						// TODO Auto-generated method stub
						return callback.getChatLoad(chatId);
					}
				});
		mListView.setAdapter(mListAdapter);

		mListView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@SuppressLint("NewApi")
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
					int totalItemCount) {
				if (lastFirstvisibleItem != firstVisibleItem) {
					if (lastFirstvisibleItem > firstVisibleItem) {
						if (mLinearLayoutSearch.getVisibility() != View.VISIBLE) {
							mLinearLayoutSearch.setAlpha(0f);
							mLinearLayoutSearch.setVisibility(View.VISIBLE);
							mLinearLayoutSearch.animate().alpha(1f).setDuration(200)
									.setListener(null);
						}

					} else {
						if (mLinearLayoutSearch.getVisibility() == View.VISIBLE) {
							mLinearLayoutSearch.animate().alpha(0f).setDuration(500)
									.setListener(new AnimatorListenerAdapter() {
										@Override
										public void onAnimationEnd(Animator animation) {
											mLinearLayoutSearch.setVisibility(View.INVISIBLE);
										}
									});
						}
					}

				}
				lastFirstvisibleItem = firstVisibleItem;

				// // what is the bottom iten that is visible
				// int lastInScreen = firstVisibleItem + visibleItemCount;
				//
				// // is the bottom item visible & not loading more already ?
				// Load
				// // more
				// // !
				// if ((lastInScreen == totalItemCount)) {
				// // if ((dataArray.size() - 1) > visibleChildCount) {
				// if (!isThreadRunning && isMoreData) {
				// // String data = mEditTextSearch.getText().toString();
				// if (!searchString.trim().equals("")) {
				// mFooterLayout.setVisibility(View.VISIBLE);
				// isThreadRunning = true;
				// MainActivity activity = (MainActivity) callback;
				// activity.setOneToOneSearch(true);
				// activity.searchChats(searchString, 0, pageNo, chatListener);
				// }
				// }
				// // }
				// }
			}
		});
	}

	/**
	 * Refresh data can be called from activity
	 */
	List<Contact> contacts = Lists.newArrayList();

	public void updateUi() {
		if (isViewCreated && callback.getContactList() != null) {
			// List<Contact>
			contacts.clear();
			contacts = Lists.newArrayList();
			for (Contact c : callback.getContactList()) {
				if (c.isArchive != 1)
					contacts.add(c);
			}

			if (!isLocationFilter && !isFavoriteFilter) {
				mListAdapter.clear();
				mListAdapter.addAll(searchContact(searchString));
			} else {
				if (isLocationFilter) {
					List<Contact> temp = Lists.newArrayList();
					List<Contact> searchList = searchContact(searchString);
					for (Contact c : searchList) {
						ChatLoad chatLoad = callback.getChatLoad(c.chatId);

						if (chatLoad != null && chatLoad.latitude != null
								&& chatLoad.longitude != null && !chatLoad.latitude.equals("")
								&& !chatLoad.longitude.equals("") && !chatLoad.latitude.equals("0")
								&& !chatLoad.latitude.equals("0.0")
								&& !chatLoad.latitude.equals("-1")
								&& !chatLoad.longitude.equals("0")
								&& !chatLoad.longitude.equals("0.0")
								&& !chatLoad.longitude.equals("-1")) {
							Log.d("latLong", chatLoad.latitude + " " + chatLoad.longitude);
							Log.d("latLong", c.latitude + " " + c.longitude);
							temp.add(c);
						}
					}

					Collections.sort(temp, new CustomComparator());
					mListAdapter.clear();
					mListAdapter.addAll(temp);
				} else if (isFavoriteFilter) {
					List<Contact> temp = Lists.newArrayList();
					List<Contact> searchList = searchContact(searchString);
					for (Contact c : searchList) {
						ChatLoad chatLoad = callback.getChatLoad(c.chatId);

						if (chatLoad != null && chatLoad.is_favorite == 1) {
							temp.add(c);
						}
					}

					// Collections.sort(temp, new CustomComparator());
					mListAdapter.clear();
					mListAdapter.addAll(temp);
				}
			}
			// addTestData();
			long focusedChat = ChatFocusSaver.getFocusedChatId();
			selectChat(focusedChat);

		}
	}

	private List<Contact> searchContact(String searchString) {
		if (searchString.trim().equals("")) {
			return contacts;
		}
		List<Contact> temp = Lists.newArrayList();

		for (Contact c : contacts) {
			if (c.title != null && c.title.toLowerCase().contains(searchString.toLowerCase())) {
				temp.add(c);
			}
		}
		return temp;
	}

	private void addTestData() {
		LinearLayout layout = (LinearLayout) getView().findViewById(R.id.listview1);
		layout.removeAllViews();
		for (int i = 0; i < mListAdapter.getCount(); i++) {
			View v = mListAdapter.getView(i, null, null);
			if (v != null) {
				layout.addView(v);
			}
		}
	}

	private void selectChat(long chatId) {
		for (int i = 0; i < callback.getContactList().size(); i++) {
			Contact contact = callback.getContactList().get(i);
			if (contact.chatId == chatId) {

				final int position = i;
				mListView.post(new Runnable() {
					public void run() {
						showItemInListView(position);
					}
				});
				return;
			}
		}
	}

	private void showItemInListView(int position) {
		if (mListView.getFirstVisiblePosition() > position
				|| mListView.getLastVisiblePosition() < position) {
			mListView.setSelection(position);
		}
	}

	public void openChat(String name) {
		for (int i = 0; i < callback.getContactList().size(); i++) {
			Contact contact = callback.getContactList().get(i);
			if (contact.title.equalsIgnoreCase(name)) {
				mListView.setSelection(i);

				final int position = i;
				mListView.post(new Runnable() {
					public void run() {

						int firstPosition = mListView.getFirstVisiblePosition()
								- mListView.getHeaderViewsCount(); // This is
																	// the same
																	// as child
																	// #0
						int wantedChild = position - firstPosition;
						// Say, first visible position is 8, you want position
						// 10, wantedChild will now be 2
						// So that means your view is child #2 in the ViewGroup:
						if (wantedChild < 0 || wantedChild >= mListView.getChildCount()) {
							return;
						}
						// Could also check if wantedPosition is between
						// listView.getFirstVisiblePosition() and
						// listView.getLastVisiblePosition() instead.
						ChatListItem chatListItem1 = (ChatListItem) mListView
								.getChildAt(wantedChild);
						if (chatListItem1 != null) {
							chatListItem1.showMessage();
						}
					}
				});
				break;
			}
		}

	}

	LoadMoreChatListener chatListener = new LoadMoreChatListener() {

		@Override
		public void onSearchResult(int count, int responseCode) {
			mEditTextSearch.setEnabled(true);
			mFooterLayout.setVisibility(View.GONE);
			isThreadRunning = false;
			if (count > 0)
				pageNo++;
			else
				isMoreData = false;
		}
	};

	/**
	 * This function is calculate the distance between two Geo Points
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @param unit
	 * @return
	 */
	private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
				* Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == 'K') {
			dist = dist * 1.609344;
		} else if (unit == 'N') {
			dist = dist * 0.8684;
		}
		return (dist);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts decimal degrees to radians : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts radians to decimal degrees : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	private double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}
}
