package com.blulabellabs.code.ui.common;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blulabellabs.code.ui.one2one.ChatListGroupItem;

import java.util.List;

public class ExGroupListAdapter<T extends ExGroupAdapterBasedView<E, C>, E, C extends ExAdapterCallback> extends ListGroupAdapter<T, E> {

	private final C callback;
	public boolean isScroll;

	public ExGroupListAdapter(Context context, int layoutResId, List<E> list, C callback) {
		super(context, layoutResId, list);
		this.callback = callback;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = layoutInflater.inflate(layoutResId, null);

			ChatListGroupItem chatListItem = (ChatListGroupItem) convertView;
			holder.date = chatListItem.getDate();
			holder.name = chatListItem.getName();
			holder.location = chatListItem.getLocation();
			holder.subList = chatListItem.getList();
			holder.edit = chatListItem.getMessageEdit();
			holder.dragImage = chatListItem.getDragImage();
			holder.sendMessageBtn = chatListItem.getSendMessage();
			holder.sendImgMessageBtn = chatListItem.getSendImage();
			holder.mImgBtnFavorite = chatListItem.getFavoriteBtn();
			holder.mChatItem = chatListItem.getChatItem();

			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();

		ChatListGroupItem chatListItem = (ChatListGroupItem) convertView;
		chatListItem.date = holder.date;
		chatListItem.name = holder.name;
		chatListItem.location = holder.location;
		chatListItem.subList = holder.subList;
		chatListItem.edit = holder.edit;
		chatListItem.dragImage = holder.dragImage;
		chatListItem.sendMessageBtn = holder.sendMessageBtn;
		chatListItem.sendImgMessageBtn = holder.sendImgMessageBtn;
		chatListItem.mImgBtnFavorite = holder.mImgBtnFavorite;
		chatListItem.mChatItem = holder.mChatItem;
		chatListItem.isScrolling = isScroll;
		if (!isScroll) {
			view = (T) convertView;
			E e = (E) getItem(position);
			view.fill(e, callback, position);
			viewMap.put(position, view);
		}

		return convertView;
	}

	public class ViewHolder {
		public TextView name;
		public TextView date;
		public TextView location;
		public ScrollDisabledListView subList;
		public LinearLayout dragView;
		public CustomEdit edit;
		public ImageView dragImage;
		public ImageButton sendMessageBtn;
		public ImageButton sendImgMessageBtn, mImgBtnFavorite;
		public RelativeLayout mChatItem;
	}
}
