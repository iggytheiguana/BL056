package com.blulabellabs.code.ui.common;


public interface ExAdapterBasedView<T, C extends ExAdapterCallback> extends AdapterBasedView<T> {
    void fill(T t, C c, int position);
}
