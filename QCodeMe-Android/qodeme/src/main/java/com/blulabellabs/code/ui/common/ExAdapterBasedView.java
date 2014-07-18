package com.blulabellabs.code.ui.common;

/**
 * Created by Alex on 10/24/13.
 */
public interface ExAdapterBasedView<T, C extends ExAdapterCallback> extends AdapterBasedView<T> {
    void fill(T t, C c, int position);
}
