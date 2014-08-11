package com.blulabellabs.code.ui.common;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.ui.one2one.ChatListAdapterCallback;
import com.blulabellabs.code.ui.one2one.ChatListGroupItem;
import com.blulabellabs.code.ui.one2one.ChatListItem;
import com.google.android.gms.internal.cn;

public class ExListAdapter	extends ArrayAdapter<Contact> {

    protected LayoutInflater layoutInflater;
    protected int layoutResId;
	private final ChatListAdapterCallback callback;
	public boolean isScroll = false;
    private SparseArray<ChatListItem> views = new SparseArray<ChatListItem>();

	public ExListAdapter(Context context, int layoutResId, List<Contact> list, ChatListAdapterCallback callback) {
		super(context, layoutResId, list);
		this.callback = callback;
        this.layoutInflater = LayoutInflater.from(context);
        this.layoutResId = layoutResId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        if (views.get(position)!=null) {
            convertView = views.get(position);
        } else {
            convertView = layoutInflater.inflate(layoutResId, null);
            ChatListItem chatListItem = (ChatListItem) convertView;
            ChatListItem view = (ChatListItem) convertView;
            Contact e = getItem(position);
            view.fill(e, callback, position);
            views.put(position, chatListItem);
        }
        return convertView;
	}

    public void clearViews() {
        clear();
        views.clear();
    }
}
