package com.blulabellabs.code.ui.common;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.ui.one2one.ChatInsideGroupFragment;
import com.blulabellabs.code.ui.one2one.ChatListGroupSubItem;
import com.blulabellabs.code.ui.one2one.ChatListSubAdapterCallback;

import java.util.Collection;
import java.util.List;


public class ExtendedGroupListAdapter extends ArrayAdapter<Message> {

    private final ChatListSubAdapterCallback callback;
    private final ChatInsideGroupFragment.One2OneChatListInsideFragmentCallback callback2;
    protected LayoutInflater layoutInflater;
    protected int layoutResId;
    private SparseArray<ChatListGroupSubItem> views = new SparseArray<ChatListGroupSubItem>();

    public ExtendedGroupListAdapter(Context context, int layoutResId, List<Message> list, ChatListSubAdapterCallback _callback,
                                    ChatInsideGroupFragment.One2OneChatListInsideFragmentCallback _callback2) {

        super(context, layoutResId, list);
        this.layoutInflater = LayoutInflater.from(context);
        this.layoutResId = layoutResId;
        this.callback = _callback;
        this.callback2 = _callback2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (views.get(position)!=null) {
            convertView = views.get(position);
        } else {
            convertView = layoutInflater.inflate(layoutResId, null);
            ChatListGroupSubItem chatListSubItem = (ChatListGroupSubItem) convertView;
            Message e = getItem(position);
            Message previous = position > 0 ? getItem(position - 1) : null;
            Message next;
            try {
                next = getItem(position + 1);
            } catch (Exception ex) {
                next = null;
            }
            ((ChatListGroupSubItem) convertView).fill(e, callback, position, previous, next, callback2);
            views.put(position, chatListSubItem);
        }
        return convertView;
    }

    @Override
    public void addAll(Collection<? extends Message> collection) {
        if (collection == null) return; // Protect from null object
        for (Message elem : collection) {
            add(elem);
        }
    }

    public void clearViews() {
        clear();
        views.clear();
    }
}
