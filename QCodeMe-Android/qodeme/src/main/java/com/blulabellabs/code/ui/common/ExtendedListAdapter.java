package com.blulabellabs.code.ui.common;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.ui.one2one.ChatInsideGroupFragment.One2OneChatListInsideFragmentCallback;
import com.blulabellabs.code.ui.one2one.ChatListSubAdapterCallback;
import com.blulabellabs.code.ui.one2one.ChatListSubItem;

import java.util.List;

public class ExtendedListAdapter extends ArrayAdapter<Message> {

    private final ChatListSubAdapterCallback callback;
    private final One2OneChatListInsideFragmentCallback callback2;
    protected LayoutInflater layoutInflater;
    protected int layoutResId;
    private SparseArray<ChatListSubItem> views = new SparseArray<ChatListSubItem>();

    public ExtendedListAdapter(Context context, int layoutResId, List<Message> list, ChatListSubAdapterCallback callback,
                               One2OneChatListInsideFragmentCallback callback2) {
        super(context, layoutResId, list);
        this.layoutInflater = LayoutInflater.from(context);
        this.layoutResId = layoutResId;
        this.callback = callback;
        this.callback2 = callback2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (views.get(position) != null) {
            convertView = views.get(position);
        } else {
            convertView = layoutInflater.inflate(layoutResId, null);
            ChatListSubItem chatListSubItem = (ChatListSubItem) convertView;
            Message e = getItem(position);
            Message previous = position > 0 ? getItem(position - 1) : null;
            Message next;
            try {
                next = getItem(position + 1);
            } catch (Exception ex) {
                next = null;
            }
            chatListSubItem.fill(e, callback, position, previous, next, callback2);
            views.put(position, chatListSubItem);
        }
        return convertView;
    }

    public void clearViews() {
        clear();
        views.clear();
    }
}
