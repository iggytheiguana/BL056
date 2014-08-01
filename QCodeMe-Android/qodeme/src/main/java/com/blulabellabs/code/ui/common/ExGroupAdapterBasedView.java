package com.blulabellabs.code.ui.common;

public interface ExGroupAdapterBasedView<T, C extends ExAdapterCallback> extends
		AdapterBasedView<T> {
	void fill(T t, C c, int position);
}
