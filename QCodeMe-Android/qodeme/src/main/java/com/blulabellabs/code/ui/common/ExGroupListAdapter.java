package com.blulabellabs.code.ui.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.one2one.ChatListAdapterCallback;
import com.blulabellabs.code.ui.one2one.ChatListGroupItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExGroupListAdapter extends ArrayAdapter<ChatLoad> {

    private final ChatListAdapterCallback callback;
    public boolean isScroll;
    protected LayoutInflater layoutInflater;
    protected int layoutResId;
    private SparseArray<ChatListGroupItem> views = new SparseArray<ChatListGroupItem>();

    public ExGroupListAdapter(Context context, int layoutResId, List<ChatLoad> list, ChatListAdapterCallback callback) {
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
            ChatListGroupItem chatListItem = (ChatListGroupItem) convertView;
            ChatLoad e = getItem(position);
            chatListItem.fill(e, callback, position);
            views.put(position, chatListItem);
        }
        return convertView;

    }

    public void clearViews() {
        clear();
        views.clear();
    }
}
