package com.blulabellabs.code.ui.common;

/**
 * Created by Alex on 10/24/13.
 */
@SuppressWarnings("hiding")
public interface ExGroupAdapterBasedView<T, C extends ExAdapterCallback> extends
		AdapterBasedView<T> {
	void fill(T t, C c, int position);
}
