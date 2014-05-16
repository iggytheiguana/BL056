package biz.softtechnics.qodeme.ui.common;

import java.util.ArrayList;
import java.util.HashMap;

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
import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.io.model.Message;
import biz.softtechnics.qodeme.images.utils.ImageFetcher;
import biz.softtechnics.qodeme.ui.MainActivity;

public class GridAdapter extends BaseAdapter {
	private Context mContext = null;
	ImageFetcher mImageFetcher;
	private ArrayList<HashMap<String, Message>> mArrayList = new ArrayList<HashMap<String, Message>>();

	public GridAdapter(Context context, ArrayList<HashMap<String, Message>> mMapList, ImageFetcher fetcher) {
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
		Message leftMessage = map.get("0");
		Message rightMessage = map.get("1");
		
		
		  mImageFetcher.loadImage(leftMessage.photoUrl, holder.imageViewLeft, null);
		if (rightMessage == null)
			holder.imageViewRight.setVisibility(View.INVISIBLE);
		else{
			holder.imageViewRight.setVisibility(View.VISIBLE);
			  mImageFetcher.loadImage(rightMessage.photoUrl, holder.imageViewRight, null);
		}
		holder.imageViewRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainActivity activity = (MainActivity) mContext;
				//activity.zoomImageFromThumb(v, R.drawable.logo);
			}
		});
		holder.imageViewLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainActivity activity = (MainActivity) mContext;
//				activity.zoomImageFromThumb(v, R.drawable.logo1);
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
