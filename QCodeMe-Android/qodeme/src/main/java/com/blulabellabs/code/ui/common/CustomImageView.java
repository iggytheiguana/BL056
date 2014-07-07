package com.blulabellabs.code.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CustomImageView extends ImageView {

	public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CustomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomImageView(Context context) {
		super(context);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = 200;
		if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
			width = MeasureSpec.getSize(widthMeasureSpec);
		}
		int height = width;
		setMeasuredDimension(width, height);
	}
}
