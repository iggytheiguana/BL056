package com.blulabellabs.code.ui.common;

import com.blulabellabs.code.ui.one2one.ChatInsideFragment.One2OneChatListInsideFragmentCallback;

public interface ExtendedAdapterBasedView<T, C extends ExAdapterCallback> extends AdapterBasedView<T> {
    void fill(T t, C c, int position, T previousElement, T nextElement, One2OneChatListInsideFragmentCallback callback);
}
