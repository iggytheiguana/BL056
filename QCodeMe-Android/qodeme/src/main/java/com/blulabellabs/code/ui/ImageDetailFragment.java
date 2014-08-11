/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blulabellabs.code.ui;

import com.blulabellabs.code.R;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.images.utils.ImageWorker;
import com.blulabellabs.code.images.utils.Utils;
import com.google.android.gms.internal.ac;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImageDetailFragment extends Fragment {
	private static final String IMAGE_DATA_EXTRA = "extra_image_data";
	private String mImageUrl;
	private ImageView mImageView, mImgFlag;
	private ImageFetcher mImageFetcher;

	public static ImageDetailFragment newInstance(String imageUrl, int isFlag) {
		final ImageDetailFragment f = new ImageDetailFragment();
	final Bundle args = new Bundle();
		args.putString(IMAGE_DATA_EXTRA, imageUrl);
		args.putInt("flag", isFlag);
		f.setArguments(args);

		return f;
	}

	public ImageDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImageUrl = getArguments() != null ? getArguments().getString(IMAGE_DATA_EXTRA) : null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
		mImageView = (ImageView) v.findViewById(R.id.imageView);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (ImageDetailActivity.class.isInstance(getActivity())) {
			mImageFetcher = ((ImageDetailActivity) getActivity()).getImageFetcher();
			mImageFetcher.loadImage(mImageUrl, mImageView, null);
		}
		if (OnClickListener.class.isInstance(getActivity()) && Utils.hasHoneycomb()) {
			mImageView.setOnClickListener((OnClickListener) getActivity());
		}
		mImgFlag = (ImageView) getView().findViewById(R.id.img_flag);
		mImgFlag.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ImageDetailActivity activity = (ImageDetailActivity) getActivity();
				long messageId = activity.getMessage_id();
				activity.getContentResolver().update(QodemeContract.Messages.CONTENT_URI,
						QodemeContract.Messages.updateMessageFlagged(),
						QodemeContract.Messages.MESSAGE_ID + "=" + messageId, null);
			}
		});

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mImageView != null) {
			ImageWorker.cancelWork(mImageView);
			mImageView.setImageDrawable(null);
		}
	}
}
