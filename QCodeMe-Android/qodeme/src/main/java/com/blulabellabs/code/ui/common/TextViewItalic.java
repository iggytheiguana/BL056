package com.blulabellabs.code.ui.common;

import com.blulabellabs.code.Application;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;


public class TextViewItalic extends TextView {
	Context mContext;

	public TextViewItalic(Context context) {
		super(context);
		this.mContext = context;
		init();
	}

	public TextViewItalic(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init();
	}

	public TextViewItalic(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		init();
	}

	public void init() {
		if (!isInEditMode()) {
			setTypeface(Application.typefaceItalic);
		} else {
		}
	}
}
