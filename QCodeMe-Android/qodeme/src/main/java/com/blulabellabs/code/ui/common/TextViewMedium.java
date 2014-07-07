package com.blulabellabs.code.ui.common;

import com.blulabellabs.code.Application;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;


public class TextViewMedium extends TextView {
	Context mContext;

	public TextViewMedium(Context context) {
		super(context);
		this.mContext = context;
		init();
	}

	public TextViewMedium(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init();
	}

	public TextViewMedium(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		init();
	}

	public void init() {
		if (!isInEditMode()) {
			setTypeface(Application.typefaceMedium);
		} else {
		}
	}
}
