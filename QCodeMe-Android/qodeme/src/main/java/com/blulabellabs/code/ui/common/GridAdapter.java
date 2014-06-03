package com.blulabellabs.code.ui.common;

import java.util.ArrayList;
import java.util.HashMap;

import com.blulabellabs.code.R;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.ui.MainActivity;
import com.google.android.gms.internal.ac;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class GridAdapter extends BaseAdapter {
	private Context mContext = null;
	ImageFetcher mImageFetcher;
	private ArrayList<HashMap<String, Message>> mArrayList = new ArrayList<HashMap<String, Message>>();

	public GridAdapter(Context context, ArrayList<HashMap<String, Message>> mMapList,
			ImageFetcher fetcher) {
		this.mContext = context;
		this.mArrayList = mMapList;
		this.mImageFetcher = fetcher;
	}

	@Override
	public int getCount() {
		Log.e("SIZE", "==== " + mArrayList.size());
		return mArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return mArrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			LayoutInflater Inflater = LayoutInflater.from(mContext);
			convertView = Inflater.inflate(R.layout.photo_list_item, null);
			holder = new ViewHolder();

			// holder.gridView = (GridView) convertView
			// .findViewById(R.id.grid_view);
			holder.imageViewLeft = (ImageView) convertView.findViewById(R.id.img1);
			holder.imageViewRight = (ImageView) convertView.findViewById(R.id.img2);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		HashMap<String, Message> map = (HashMap<String, Message>) getItem(position);
		final Message leftMessage = map.get("0");
		final Message rightMessage = map.get("1");

		try {
			mImageFetcher.loadImage(leftMessage.photoUrl, holder.imageViewLeft, null);

			if (rightMessage == null)
				holder.imageViewRight.setVisibility(View.INVISIBLE);
			else {
				holder.imageViewRight.setVisibility(View.VISIBLE);
				mImageFetcher.loadImage(rightMessage.photoUrl, holder.imageViewRight, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		holder.imageViewRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MainActivity activity = (MainActivity) mContext;
				activity.fullImageView(v, rightMessage);
				// activity.zoomImageFromThumb(v, R.drawable.logo);
			}
		});
		holder.imageViewLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MainActivity activity = (MainActivity) mContext;
				activity.fullImageView(v, leftMessage);
				// activity.zoomImageFromThumb(v, R.drawable.logo1);
			}
		});
		// holder.gridView.setAdapter(new ImageAdapter(mContext, mArrayList));
		return convertView;
	}

	static class ViewHolder {
		LinearLayout linearLayout;
		ImageView imageViewLeft, imageViewRight;
		GridView gridView;
	}
}
