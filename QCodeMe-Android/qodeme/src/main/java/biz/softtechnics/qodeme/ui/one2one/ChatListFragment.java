package biz.softtechnics.qodeme.ui.one2one;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.common.collect.Lists;

import java.util.List;

import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.core.io.model.Message;
import biz.softtechnics.qodeme.ui.common.ExListAdapter;
import biz.softtechnics.qodeme.ui.common.ScrollDisabledListView;
import biz.softtechnics.qodeme.utils.ChatFocusSaver;
import biz.softtechnics.qodeme.utils.Fonts;

/**
 * Created by Alex on 10/7/13.
 */
public class ChatListFragment extends Fragment {

    private One2OneChatListFragmentCallback callback;
    private boolean isViewCreated = false;
    private ScrollDisabledListView mListView;
    private ExListAdapter<ChatListItem, Contact, ChatListAdapterCallback> mListAdapter;
    private View mMessageLayout;
    private ImageButton mMessageButton;
    private EditText mMessageEdit;


    public interface One2OneChatListFragmentCallback {
        List<Contact> getContactList();

        List<Message> getChatMessages(long chatId);

        void sendMessage(long chatId, String message);

        int getHeight(long chatId);

        void setChatHeight(long chatId, int height);

        void showChat(Contact c, boolean firstUpdate);

        Typeface getFont(Fonts font);

        int getNewMessagesCount(long chatId);

        void messageRead(long chatId);
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
        initListView();
        isViewCreated = true;
        updateUi();
    }

    private void initListView() {
        mListView = (ScrollDisabledListView) getView().findViewById(R.id.listview);
        List<Contact> listForAdapter = Lists.newArrayList();
//        mListView.setEmptyView(getView().findViewById(R.id.empty_view));

        mListAdapter = new ExListAdapter<ChatListItem, Contact, ChatListAdapterCallback>(getActivity(), R.layout.one2one_chat_list_item, listForAdapter, new ChatListAdapterCallback() {

            public void onSingleTap(View view, int position, Contact ce) {
                //TODO
            }

            public void onDoubleTap(View view, int position, Contact c) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                callback.showChat(c, true);
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

            public void sendMessage(Contact c, String message) {
                callback.sendMessage(c.chatId, message);
            }

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
        });
        mListView.setAdapter(mListAdapter);
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

    /**
     * Refresh data
     * can be called from activity
     */
    public void updateUi() {
        if (isViewCreated && callback.getContactList() != null) {
            mListAdapter.clear();
            mListAdapter.addAll(callback.getContactList());

            long focusedChat = ChatFocusSaver.getFocusedChatId();
            selectChat(focusedChat);

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
        if (mListView.getFirstVisiblePosition() > position || mListView.getLastVisiblePosition() < position) {
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

                        int firstPosition = mListView.getFirstVisiblePosition() - mListView.getHeaderViewsCount(); // This is the same as child #0
                        int wantedChild = position - firstPosition;
                        // Say, first visible position is 8, you want position 10, wantedChild will now be 2
                        // So that means your view is child #2 in the ViewGroup:
                        if (wantedChild < 0 || wantedChild >= mListView.getChildCount()) {
                            return;
                        }
                        // Could also check if wantedPosition is between listView.getFirstVisiblePosition() and listView.getLastVisiblePosition() instead.
                        ChatListItem chatListItem1 = (ChatListItem) mListView.getChildAt(wantedChild);
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
