package com.blulabellabs.code.ui.one2one;

import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
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
import android.widget.Toast;

import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.entities.LookupChatEntity;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.RestAsyncHelper;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.io.responses.LookupResponse;
import com.blulabellabs.code.core.io.utils.RestError;
import com.blulabellabs.code.core.io.utils.RestListener;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.common.ExGroupListAdapter;
import com.blulabellabs.code.ui.common.ScrollDisabledListView;
import com.blulabellabs.code.ui.one2one.ChatListFragment.One2OneChatListFragmentCallback;
import com.blulabellabs.code.utils.Fonts;
import com.google.android.gms.internal.ac;
import com.google.common.collect.Lists;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

/**
 * Created by Alex on 10/7/13.
 */
@SuppressLint("ValidFragment")
public class ChatListGroupPublicFragment extends Fragment {

	private One2OneChatListFragmentCallback callback;
	private boolean isViewCreated = false;
	private ScrollDisabledListView mListView;
	private ExGroupListAdapter<ChatListGroupItem, ChatLoad, ChatListAdapterCallback> mListAdapter;
	private View mMessageLayout;
	private ImageButton mMessageButton, mImgBtnSearch, mImgBtnClear;
	private EditText mMessageEdit;
	private int chatType;
	private EditText mEditTextSearch;
	private LinearLayout mLinearLayoutSearch;
	int lastFirstvisibleItem = 0;

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
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mMessageLayout = getView().findViewById(R.id.layout_message);
		mMessageButton = (ImageButton) getView().findViewById(R.id.button_message);
		mMessageEdit = (EditText) getView().findViewById(R.id.edit_message);
		mLinearLayoutSearch = (LinearLayout) getView().findViewById(R.id.linearLayout_search);
		mImgBtnSearch   = (ImageButton) getView().findViewById(R.id.imgBtn_search);
		mImgBtnClear = (ImageButton) getView().findViewById(R.id.imgBtn_clear);
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
				mImgBtnClear.setVisibility(View.GONE);
				mImgBtnSearch.setVisibility(View.VISIBLE);	
				updateUi();
			}
		});
		MainActivity activity = (MainActivity) callback;
		if(activity.isPublicSearch()){
			mImgBtnClear.setVisibility(View.VISIBLE);
			mImgBtnSearch.setVisibility(View.GONE);	
		}else{
			mImgBtnClear.setVisibility(View.GONE);
			mImgBtnSearch.setVisibility(View.VISIBLE);	
		}
		mEditTextSearch = (EditText) getView().findViewById(R.id.editText_Search);
		mEditTextSearch.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH
						|| actionId == EditorInfo.IME_ACTION_GO
						|| actionId == EditorInfo.IME_ACTION_DONE
						|| event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

					String data = v.getText().toString();
					MainActivity activity = (MainActivity) callback;
					activity.setPublicSearch(true);
					activity.searchChats(data, 2);
//					RestAsyncHelper.getInstance().lookup(data, new RestListener<LookupResponse>() {
//
//						@Override
//						public void onResponse(LookupResponse response) {
//							if (response.getChatList() != null)
//								for (LookupChatEntity entity : response.getChatList()) {
//									Log.d("lookup", entity.getTitle() + " " + entity.getTags()
//											+ " " + entity.getId());
//								}
//							else {
//								Log.d("lookup", "null response");
//							}
//						}
//
//						@Override
//						public void onServiceError(RestError error) {
//							Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT)
//									.show();
//						}
//					});
					
					mImgBtnClear.setVisibility(View.VISIBLE);
					mImgBtnSearch.setVisibility(View.GONE);
					return true;
				}
				return false;
			}
		});
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
		mListView.addHeaderView(headerSearchView);

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
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

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
		if (isViewCreated && callback.getChatList(chatType) != null) {
			mListAdapter.clear();
			List<ChatLoad> chatLoads = callback.getChatList(chatType);
			if (chatLoads != null
					&& QodemePreferences.getInstance().getNewPublicGroupChatId() != -1) {
				ChatLoad newChatLoad = null;
				for (ChatLoad c : chatLoads) {
					if (QodemePreferences.getInstance().getNewPublicGroupChatId() == c.chatId) {
						newChatLoad = c;
						break;
					}
				}
				chatLoads.remove(newChatLoad);
				chatLoads.add(0, newChatLoad);
				mListAdapter.addAll(chatLoads);
				mListView.setSelection(0);
			} else {
				mListAdapter.addAll(chatLoads);
			}

			// long focusedChat = ChatFocusSaver.getFocusedChatId();
			// selectChat(focusedChat);
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

}
