package com.blulabellabs.code.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

/**
 * Created by Alex on 10/28/13.
 */
public class ScrollDisabledListView extends ListView {

	private boolean dragMode;
	private boolean disabled;
	private OnScrollListener onScrollListener = null;

	public boolean isDisabled() {
		return disabled;
	}
//
//	public void setDisabled(boolean disabled) {
//		this.disabled = disabled;
//	}

	public boolean isDragMode() {
		return dragMode;
	}

	public void setDragMode(boolean dragMode) {
		this.dragMode = dragMode;
	}

	public ScrollDisabledListView(Context context) {
		super(context);
	}

	public ScrollDisabledListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollDisabledListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (isDisabled())
			return false;
		if (isDragMode()) {
			int childcount = getChildCount();
			for (int i = 0; i < childcount; i++) {
				View v = getChildAt(i);
				v.dispatchTouchEvent(ev);
			}
			return true;
		} else {
			return super.dispatchTouchEvent(ev);
		}
		// final int actionMasked = ev.getActionMasked() &
		// MotionEvent.ACTION_MASK;

		/*
		 * if (actionMasked == MotionEvent.ACTION_DOWN) { // Record the position
		 * the list the touch landed on mPosition = pointToPosition((int)
		 * ev.getX(), (int) ev.getY()); return super.dispatchTouchEvent(ev); }
		 */

		/*
		 * if (actionMasked == MotionEvent.ACTION_MOVE) { // Ignore move events
		 * return false; }
		 */
		/*
		 * if (actionMasked == MotionEvent.ACTION_UP) { // Check if we are still
		 * within the same view if (pointToPosition((int) ev.getX(), (int)
		 * ev.getY()) == mPosition) { super.dispatchTouchEvent(ev); } else { //
		 * Clear pressed state, cancel the action setPressed(false);
		 * invalidate(); return true; } }
		 */

		// return super.dispatchTouchEvent(ev);
	}

//	public void setOnScrollUpAndDownListener(OnScrollListener onScrollListener) {
//		this.onScrollListener = onScrollListener;
//	}

	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		super.onScrollChanged(x, y, oldx, oldy);
		if (onScrollListener != null) {
			onScrollListener.onScrollUpAndDownChanged(x, y, oldx, oldy);
		}
	}

	public interface OnScrollListener {
		void onScrollUpAndDownChanged(int x, int y, int oldx, int oldy);
	}

}
