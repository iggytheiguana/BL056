package com.blulabellabs.code.ui.common;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.blulabellabs.code.R;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.ui.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridAdapter extends BaseAdapter {
    private Context mContext = null;
    ImageFetcher mImageFetcher;
    private List<HashMap<String, Message>> mArrayList = new ArrayList<HashMap<String, Message>>();

    public GridAdapter(Context context, List<HashMap<String, Message>> mMapList, ImageFetcher fetcher) {
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
    public Map<String, Message> getItem(int position) {
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
            holder.imageViewLeft = (ImageView) convertView.findViewById(R.id.img1);
            holder.imageViewRight = (ImageView) convertView.findViewById(R.id.img2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Map<String, Message> map = getItem(position);
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
            }
        });
        holder.imageViewLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) mContext;
                activity.fullImageView(v, leftMessage);
            }
        });
        return convertView;
    }

    class ViewHolder {
        ImageView imageViewLeft, imageViewRight;
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }

}
