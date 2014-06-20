package com.blulabellabs.code.ui.one2one;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
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
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.RestAsyncHelper;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.io.responses.ClearSearchResponse;
import com.blulabellabs.code.core.io.utils.RestError;
import com.blulabellabs.code.core.io.utils.RestListener;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.MainActivity.LoadMoreChatListener;
import com.blulabellabs.code.ui.common.ExGroupListAdapter;
import com.blulabellabs.code.ui.common.ScrollDisabledListView;
import com.blulabellabs.code.ui.one2one.ChatListFragment.One2OneChatListFragmentCallback;
import com.blulabellabs.code.utils.Fonts;
import com.google.common.collect.Lists;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

/**
 * Created by Alex on 10/7/13.
 */
@SuppressLint("ValidFragment")
@SuppressWarnings("unused")
public class ChatListGroupPublicFragment extends Fragment {

	private One2OneChatListFragmentCallback callback;
	private boolean isViewCreated = false;
	private ScrollDisabledListView mListView;
	private ExGroupListAdapter<ChatListGroupItem, ChatLoad, ChatListAdapterCallback> mListAdapter;
	private View mMessageLayout;
	private ImageButton mMessageButton, mImgBtnSearch, mImgBtnClear, mImgBtnLocationFilter,
			mImgBtnFavoriteFilter;
	private EditText mMessageEdit;
	private int chatType;
	private EditText mEditTextSearch;
	private LinearLayout mLinearLayoutSearch;
	int lastFirstvisibleItem = 0;
	int pageNo = 1;
	boolean isMoreData = true;
	private boolean isThreadRunning;
	private LinearLayout mFooterLayout;
	private String searchString = "";
	List<ChatLoad> chatLoads = Lists.newArrayList();
	private boolean isLocationFilter = false;
	private boolean isFavoriteFilter = false;

	private static final String TAG = "ChatListGroupFragment";
	private final WebSocketConnection mConnection = new WebSocketConnection();

	// public interface One2OneChatListFragmentCallback {
	// List<Contact> getContactList();
	//
	// List<Message> getChatMessages(long chatId);
	//
	// void sendMessage(long chatId, String message, String photoUrl, int
	// hashPhoto,
	// long replyTo_Id, double latitude, double longitude, String senderName);
	//
	// int getHeight(long chatId);
	//
	// void setChatHeight(long chatId, int height);
	//
	// void showChat(Contact c, boolean firstUpdate);
	// void showChat(ChatLoad c, boolean firstUpdate);
	//
	// Typeface getFont(Fonts font);
	//
	// int getNewMessagesCount(long chatId);
	//
	// void messageRead(long chatId);
	// }

	public ChatListGroupPublicFragment() {
		super();
	}

	public static ChatListGroupPublicFragment getInstance() {
		ChatListGroupPublicFragment chatListGroupPublicFragment = new ChatListGroupPublicFragment();
		Bundle args = new Bundle();
		args.putInt("index", 1);
		chatListGroupPublicFragment.chatType = 2;
		chatListGroupPublicFragment.setArguments(args);
		return chatListGroupPublicFragment;
	}

	public ChatListGroupPublicFragment(int chatType) {
		this.chatType = chatType;
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
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
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
		mImgBtnFavoriteFilter = (ImageButton) getView().findViewById(R.id.imgBtn_favoriteFilter);

		mEditTextSearch = (EditText) getView().findViewById(R.id.editText_Search);

		initListView();
		isViewCreated = true;
		updateUi();

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
				activity.setPublicSearch(false);
				activity.setPublicSearchString("");
				mImgBtnClear.setVisibility(View.GONE);
				mImgBtnSearch.setVisibility(View.VISIBLE);
				mEditTextSearch.setEnabled(true);
				mEditTextSearch.setText("");
				searchString = "";
				pageNo = 1;
				Bitmap bm = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_location_gray);
				mImgBtnLocationFilter.setImageBitmap(bm);
				isLocationFilter = false;

				isFavoriteFilter = false;
				Bitmap bm1 = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_chat_favorite_h);
				mImgBtnFavoriteFilter.setImageBitmap(bm1);
				updateUi();

				RestAsyncHelper.getInstance().clearSearchChats(2,
						new RestListener<ClearSearchResponse>() {

							@Override
							public void onResponse(ClearSearchResponse response) {
								Log.d("clearSearch", "Ok");
							}

							@Override
							public void onServiceError(RestError error) {
								Log.d("clearSearch", "Error " + error.getMessage());
							}

						});
			}
		});

		mImgBtnFavoriteFilter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// if (isLocationFilter) {
				// isLocationFilter = false;
				// Bitmap bm = BitmapFactory.decodeResource(getResources(),
				// R.drawable.ic_location_gray);
				// mImgBtnLocationFilter.setImageBitmap(bm);
				// }
				if (isFavoriteFilter) {
					isFavoriteFilter = false;
					updateUi();
					Bitmap bm = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_chat_favorite_h);
					mImgBtnFavoriteFilter.setImageBitmap(bm);
				} else {
					Bitmap bm = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_star_blue);
					mImgBtnFavoriteFilter.setImageBitmap(bm);
					isFavoriteFilter = true;
					if (chatLoads != null) {

						mListAdapter.clear();
						mListAdapter.addAll(filterMessages());
					}
				}
			}
		});

		mImgBtnLocationFilter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// MainActivity activity = (MainActivity) callback;
				// if (activity.isPrivateSearch()) {
				// if (isFavoriteFilter) {
				// isFavoriteFilter = false;
				// Bitmap bm = BitmapFactory.decodeResource(getResources(),
				// R.drawable.ic_chat_favorite_h);
				// mImgBtnFavoriteFilter.setImageBitmap(bm);
				// }

				if (isLocationFilter) {
					isLocationFilter = false;
					updateUi();
					Bitmap bm = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_location_gray);
					mImgBtnLocationFilter.setImageBitmap(bm);
				} else {
					Bitmap bm = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_location_blue_big);
					mImgBtnLocationFilter.setImageBitmap(bm);
					isLocationFilter = true;
					if (chatLoads != null) {
						// List<ChatLoad> temp = Lists.newArrayList();
						// for (ChatLoad c : chatLoads) {
						// try {
						// if (c.latitude != null && c.longitude != null
						// && !c.latitude.equals("") && !c.longitude.equals("")
						// && !c.latitude.equals("0") &&
						// !c.latitude.equals("0.0")
						// && !c.latitude.equals("-1") &&
						// !c.longitude.equals("0")
						// && !c.longitude.equals("0.0") &&
						// !c.longitude.equals("-1")) {
						// Log.d("latLong", c.latitude + " " + c.longitude);
						// temp.add(c);
						// }
						// } catch (Exception e) {
						// // TODO: handle exception
						// }
						// }
						//
						// Collections.sort(temp, new CustomComparator());
						mListAdapter.clear();
						mListAdapter.addAll(filterMessages());
					}
				}
			}
		});

		MainActivity activity = (MainActivity) callback;
		if (activity.isPublicSearch()) {
			searchString = activity.getPublicSearchString();
			mEditTextSearch.setText(searchString);
			mImgBtnClear.setVisibility(View.VISIBLE);
			mImgBtnSearch.setVisibility(View.GONE);
		} else {
			mImgBtnClear.setVisibility(View.GONE);
			mImgBtnSearch.setVisibility(View.VISIBLE);
		}
		mImgBtnSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String data = mEditTextSearch.getText().toString();
				if (!data.trim().equals("")) {

					// if (isLocationFilter) {
					// isLocationFilter = false;
					// Bitmap bm = BitmapFactory.decodeResource(getResources(),
					// R.drawable.ic_location_gray);
					// mImgBtnLocationFilter.setImageBitmap(bm);
					// }
					// if (isFavoriteFilter) {
					// isFavoriteFilter = false;
					// Bitmap bm = BitmapFactory.decodeResource(getResources(),
					// R.drawable.ic_chat_favorite_h);
					// mImgBtnFavoriteFilter.setImageBitmap(bm);
					// }

					searchString = data;
					MainActivity activity = (MainActivity) callback;
					activity.setPublicSearch(true);
					activity.setPublicSearchString(data);
					activity.searchChats(data, 2, pageNo, chatListener);
					mEditTextSearch.setEnabled(false);
					isMoreData = true;
					// RestAsyncHelper.getInstance().lookup(data, new
					// RestListener<LookupResponse>() {
					//
					// @Override
					// public void onResponse(LookupResponse response) {
					// if (response.getChatList() != null)
					// for (LookupChatEntity entity : response.getChatList()) {
					// Log.d("lookup", entity.getTitle() + " " +
					// entity.getTags()
					// + " " + entity.getId());
					// }
					// else {
					// Log.d("lookup", "null response");
					// }
					// }
					//
					// @Override
					// public void onServiceError(RestError error) {
					// Toast.makeText(getActivity(), error.getMessage(),
					// Toast.LENGTH_SHORT)
					// .show();
					// }
					// });
					isThreadRunning = true;
					mImgBtnClear.setVisibility(View.VISIBLE);
					mImgBtnSearch.setVisibility(View.GONE);
				}
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
					activity.setPublicSearch(true);
					activity.setPublicSearchString(data);
					activity.searchChats(data, 2, pageNo, chatListener);
					mEditTextSearch.setEnabled(false);
					isMoreData = true;

					// BITMAP BM = BITMAPFACTORY.DECODERESOURCE(GETRESOURCES(),
					// R.DRAWABLE.IC_LOCATION_GRAY);
					// MIMGBTNLOCATIONFILTER.SETIMAGEBITMAP(BM);
					// ISLOCATIONFILTER = FALSE;
					//
					// ISFAVORITEFILTER = FALSE;
					// BITMAP BM1 = BITMAPFACTORY.DECODERESOURCE(GETRESOURCES(),
					// R.DRAWABLE.IC_CHAT_FAVORITE);
					// MImgBtnFavoriteFilter.setImageBitmap(bm1);

					isThreadRunning = true;
					mImgBtnClear.setVisibility(View.VISIBLE);
					mImgBtnSearch.setVisibility(View.GONE);
					return true;
				}
				return false;
			}
		});
	}

	private List<ChatLoad> filterMessages() {
		List<ChatLoad> temp = Lists.newArrayList();
		List<ChatLoad> temp1 = Lists.newArrayList();
		if (isFavoriteFilter) {
			for (ChatLoad c : chatLoads) {
				try {
					if (c.is_favorite == 1) {
						// Log.d("latLong", c.latitude + " " +
						// c.longitude);
						temp.add(c);
					}
				} catch (Exception e) {
				}
			}
		} else {
			temp.addAll(chatLoads);
		}

		if (isLocationFilter) {
			for (ChatLoad c : temp) {
				try {
					if (c.latitude != null && c.longitude != null && !c.latitude.equals("")
							&& !c.longitude.equals("") && !c.latitude.equals("0")
							&& !c.latitude.equals("0.0") && !c.latitude.equals("-1")
							&& !c.longitude.equals("0") && !c.longitude.equals("0.0")
							&& !c.longitude.equals("-1")) {
						Log.d("latLong", c.latitude + " " + c.longitude);
						temp1.add(c);
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			Collections.sort(temp1, new CustomComparator());

			// Collections.sort(temp, new CustomComparator());
		} else {
			temp1.addAll(temp);
		}
		return temp1;
	}

	public class CustomComparator implements Comparator<ChatLoad> {

		@Override
		public int compare(ChatLoad lhs, ChatLoad rhs) {
			MainActivity activity = (MainActivity) callback;
			Location location = activity.getCurrentLocation();
			Integer leftDistance = 0;
			Integer rightDistance = 0;
			if (location != null) {
				try {
					double lat = Double.parseDouble(lhs.latitude);
					double lng = Double.parseDouble(lhs.longitude);
					leftDistance = (int) distance(lat, lng, location.getLatitude(),
							location.getLongitude(), 'M');

				} catch (Exception e) {
				}
				try {
					double lat = Double.parseDouble(rhs.latitude);
					double lng = Double.parseDouble(rhs.longitude);
					rightDistance = (int) distance(lat, lng, location.getLatitude(),
							location.getLongitude(), 'M');
				} catch (Exception e) {
				}
			}
			return leftDistance.compareTo(rightDistance);
		}

	}

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
		mListView.addFooterView(footerView);

		List<ChatLoad> listForAdapter = Lists.newArrayList();
		// mListView.setEmptyView(getView().findViewById(R.id.empty_view));

		mListAdapter = new ExGroupListAdapter<ChatListGroupItem, ChatLoad, ChatListAdapterCallback>(
				getActivity(), R.layout.group_public_chat_list_item, listForAdapter,
				new ChatListAdapterCallback() {

					public void onSingleTap(View view, int position, Contact ce) {
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
						return callback.getChatMessages(chatId);
					}

					@Override
					public void sendMessage(long c, String message, String photoUrl, int hashPhoto,
							long replyTo_Id, double latitude, double longitude, String senderName,
							String localUrl) {
						// TODO Auto-generated method stub
						callback.sendMessage(c, message, photoUrl, hashPhoto, replyTo_Id, latitude,
								longitude, senderName, localUrl);
					}

					@Override
					public void onDoubleTap(View view, int position, ChatLoad c) {
						InputMethodManager imm = (InputMethodManager) getActivity()
								.getSystemService(Activity.INPUT_METHOD_SERVICE);
						imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
						callback.showChat(c, true, view);

					}

					@Override
					public void onSingleTap(View view, int position, ChatLoad c) {

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
						return callback.getChatType(chatId);
					}

					@Override
					public ChatLoad getChatLoad(long chatId) {
						return callback.getChatLoad(chatId);
					}
				});
		mListView.setAdapter(mListAdapter);

		// mListView.setOnScrollUpAndDownListener(new
		// ScrollDisabledListView.OnScrollListener() {
		//
		// @SuppressLint("NewApi")
		// @Override
		// public void onScrollUpAndDownChanged(int x, int y, int oldx, int
		// oldy) {
		// if (oldy < y) {
		// mLinearLayoutSearch.setAlpha(0f);
		// mLinearLayoutSearch.setVisibility(View.VISIBLE);
		// mLinearLayoutSearch.animate().alpha(1f).setDuration(500).setListener(null);
		// } else {
		// mLinearLayoutSearch.animate().alpha(0f).setDuration(500)
		// .setListener(new AnimatorListenerAdapter() {
		// @Override
		// public void onAnimationEnd(Animator animation) {
		// mLinearLayoutSearch.setVisibility(View.GONE);
		// }
		// });
		// }
		// }
		// });
		mListView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});
		// final GestureDetector gestureDetector = new
		// GestureDetector(getActivity(), new Gestures());
		// View.OnTouchListener gestureListener = new View.OnTouchListener() {
		// public boolean onTouch(View v, MotionEvent event) {
		// return gestureDetector.onTouchEvent(event);
		// }
		// };
		// mListView.setOnTouchListener(gestureListener);

		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == SCROLL_STATE_FLING || scrollState == SCROLL_STATE_TOUCH_SCROLL)
					mListAdapter.isScroll = true;
				else {
					mListAdapter.isScroll = false;
					// mListAdapter.notifyDataSetChanged();
				}

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

//						for(int i=0; i<firstVisibleItem;i++){
//							callback.messageRead(mListAdapter.getItem(i).chatId);
//						}
						
					} else {
//						for(int i=mListView.getLastVisiblePosition(); i<mListView.getLastVisiblePosition()+1;i++){
//							callback.messageRead(mListAdapter.getItem(i).chatId);
//						}
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

				// what is the bottom iten that is visible
				int lastInScreen = firstVisibleItem + visibleItemCount;

				// is the bottom item visible & not loading more already ? Load
				// more
				// !
				if ((lastInScreen == totalItemCount)) {
					// if ((dataArray.size() - 1) > visibleChildCount) {
					if (!isThreadRunning && isMoreData) {
						// String data = mEditTextSearch.getText().toString();
						if (!searchString.trim().equals("")) {
							mFooterLayout.setVisibility(View.VISIBLE);
							isThreadRunning = true;
							MainActivity activity = (MainActivity) callback;
							activity.setPublicSearch(true);
							activity.searchChats(searchString, 2, pageNo, chatListener);
						}
					}
					// }
				}
			}
		});

	}

	class Gestures implements OnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

		@SuppressLint("NewApi")
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			// try {
			// if (e1.getRawY() < e2.getRawY()) {
			//
			// // ((ImageView) findViewById(R.id.image_place_holder))
			// // .setImageResource(R.drawable.down);
			// Toast.makeText(getActivity(), "down scroll",
			// Toast.LENGTH_SHORT).show();
			//
			// } else {
			// Toast.makeText(getActivity(), "up scroll",
			// Toast.LENGTH_SHORT).show();
			// // ((ImageView)
			// //
			// findViewById(R.id.image_place_holder)).setImageResource(R.drawable.up);
			//
			// }
			// } catch (Exception e) {
			// // TODO: handle exception
			// }
			// return true;
			// return false;
			try {
				if (e1.getY() < e2.getY()) {

					// ((ImageView) findViewById(R.id.image_place_holder))
					// .setImageResource(R.drawable.down);
					// Toast.makeText(getActivity(), "down scroll",
					// Toast.LENGTH_SHORT).show();
					// Log.d("scroll", "Up");
					new Handler().post(new Runnable() {

						@Override
						public void run() {
							if (mLinearLayoutSearch.getVisibility() != View.VISIBLE) {
								mLinearLayoutSearch.setAlpha(0f);
								mLinearLayoutSearch.setVisibility(View.VISIBLE);
								mLinearLayoutSearch.animate().alpha(1f).setDuration(500)
										.setListener(null);
							}

						}
					});

				} else {
					new Handler().post(new Runnable() {

						@Override
						public void run() {

							mLinearLayoutSearch.animate().alpha(0f).setDuration(500)
									.setListener(new AnimatorListenerAdapter() {
										@Override
										public void onAnimationEnd(Animator animation) {
											mLinearLayoutSearch.setVisibility(View.GONE);
										}
									});
						}
					});
					// Toast.makeText(getActivity(), "up scroll",
					// Toast.LENGTH_SHORT).show();
					// ((ImageView)
					// findViewById(R.id.image_place_holder)).setImageResource(R.drawable.up);
					Log.d("scroll", "Down");

				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {

		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}

	}

	private void sortByMember() {
		Collections.sort(chatLoads, new Comparator<ChatLoad>() {

			@Override
			public int compare(ChatLoad lhs, ChatLoad rhs) {
				Integer left = 0;
				Integer right = 0;
				if (lhs.members != null)
					left = lhs.members.length;
				if (rhs.members != null)
					right = rhs.members.length;

				return right.compareTo(left);
			}
		});
	}

	/**
	 * Refresh data can be called from activity
	 */

	public void updateUi() {
		// if (isViewCreated && callback.getContactList() != null) {
		// mListAdapter.clear();
		// //mListAdapter.addAll(callback.getContactList());
		// // addTestData();
		// long focusedChat = ChatFocusSaver.getFocusedChatId();
		// selectChat(focusedChat);
		//
		// }
		try {
			if (isViewCreated && callback != null && callback.getChatList(chatType) != null) {

				chatLoads = callback.getChatList(chatType);
				if (chatLoads != null
						&& QodemePreferences.getInstance().getNewPublicGroupChatId() != -1) {
					try {
						sortByMember();
						ChatLoad newChatLoad = null;
						for (ChatLoad c : chatLoads) {
							if (QodemePreferences.getInstance().getNewPublicGroupChatId() == c.chatId) {
								newChatLoad = c;
								break;
							}
						}
						chatLoads.remove(newChatLoad);
						chatLoads.add(0, newChatLoad);
					} catch (Exception e) {
					}

					if (!isLocationFilter && !isFavoriteFilter) {
						mListAdapter.clear();

						mListAdapter.addAll(chatLoads);
						mListView.setSelection(0);
					} else {
						// if (isLocationFilter) {
						// List<ChatLoad> temp = Lists.newArrayList();
						// for (ChatLoad c : chatLoads) {
						// try {
						// if (c.latitude != null && c.longitude != null
						// && !c.latitude.equals("") && !c.longitude.equals("")
						// && !c.latitude.equals("0") &&
						// !c.latitude.equals("0.0")
						// && !c.latitude.equals("-1") &&
						// !c.longitude.equals("0")
						// && !c.longitude.equals("0.0")
						// && !c.longitude.equals("-1")) {
						// Log.d("latLong", c.latitude + " " + c.longitude);
						// temp.add(c);
						// }
						// } catch (Exception e) {
						// // TODO: handle exception
						// }
						// }
						//
						// Collections.sort(temp, new CustomComparator());
						// mListAdapter.clear();
						// mListAdapter.addAll(temp);
						// } else if (isFavoriteFilter) {
						// List<ChatLoad> temp = Lists.newArrayList();
						// for (ChatLoad c : chatLoads) {
						// try {
						// if (c.is_favorite == 1) {
						// // Log.d("latLong", c.latitude + " " +
						// // c.longitude);
						// temp.add(c);
						// }
						// } catch (Exception e) {
						// }
						// }
						//
						// // Collections.sort(temp, new CustomComparator());
						// mListAdapter.clear();
						// mListAdapter.addAll(temp);
						// }
						mListAdapter.clear();
						mListAdapter.addAll(filterMessages());
					}
				} else {
					if (!isLocationFilter && !isFavoriteFilter) {
						mListAdapter.clear();
						sortByMember();
						mListAdapter.addAll(chatLoads);
					} else {
						// if (isLocationFilter) {
						// List<ChatLoad> temp = Lists.newArrayList();
						// for (ChatLoad c : chatLoads) {
						// try {
						// if (c.latitude != null && c.longitude != null
						// && !c.latitude.equals("") && !c.longitude.equals("")
						// && !c.latitude.equals("0") &&
						// !c.latitude.equals("0.0")
						// && !c.latitude.equals("-1") &&
						// !c.longitude.equals("0")
						// && !c.longitude.equals("0.0")
						// && !c.longitude.equals("-1")) {
						// Log.d("latLong", c.latitude + " " + c.longitude);
						// temp.add(c);
						// }
						// } catch (Exception e) {
						// }
						// }
						//
						// Collections.sort(temp, new CustomComparator());
						// mListAdapter.clear();
						// mListAdapter.addAll(temp);
						// } else if (isFavoriteFilter) {
						// List<ChatLoad> temp = Lists.newArrayList();
						// for (ChatLoad c : chatLoads) {
						// try {
						// if (c.is_favorite == 1) {
						// // Log.d("latLong", c.latitude + " " +
						// // c.longitude);
						// temp.add(c);
						// }
						// } catch (Exception e) {
						// }
						// }
						//
						// // Collections.sort(temp, new CustomComparator());
						// mListAdapter.clear();
						// mListAdapter.addAll(temp);
						// }
						mListAdapter.clear();
						mListAdapter.addAll(filterMessages());
					}
				}

				// long focusedChat = ChatFocusSaver.getFocusedChatId();
				// selectChat(focusedChat);

			}
		} catch (Exception e) {
		}
	}

	public void notifyUi(long chatId, ChatLoad chatLoad) {
		// mListAdapter.notifyDataSetChanged();
		ChatListGroupItem chatListGroupItem = null;
		for (int i = mListView.getFirstVisiblePosition(); i < mListView.getLastVisiblePosition(); i++) {
			ChatLoad chatLoad2 = mListAdapter.getItem(i);
			Log.d("chatid", chatLoad2.chatId + "");
			if (chatLoad2.chatId == chatId) {
				chatListGroupItem = (ChatListGroupItem) mListView.getChildAt(i
						- mListView.getFirstVisiblePosition() + 1);
				break;
			}
		}
		if (chatListGroupItem != null) {
			if (chatLoad.isTyping)
				chatListGroupItem.getUserTyping()
						.setBackgroundResource(R.drawable.bg_user_typing_h);
			else
				chatListGroupItem.getUserTyping().setBackgroundResource(R.drawable.bg_user_typing);
		}

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
		for (int i = 0; i < callback.getChatList(chatType).size(); i++) {
			ChatLoad contact = callback.getChatList(chatType).get(i);
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
}
