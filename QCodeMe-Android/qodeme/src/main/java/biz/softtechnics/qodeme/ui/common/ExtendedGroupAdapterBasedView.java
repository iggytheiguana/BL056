package biz.softtechnics.qodeme.ui.common;

import biz.softtechnics.qodeme.ui.one2one.ChatInsideGroupFragment.One2OneChatListInsideFragmentCallback;


/**
 * Created by Alex on 10/24/13.
 */
public interface ExtendedGroupAdapterBasedView<T, C extends ExAdapterCallback> extends AdapterBasedView<T> {
    void fill(T t, C c, int position, T previousElement, T nextElement, One2OneChatListInsideFragmentCallback callback);
}
