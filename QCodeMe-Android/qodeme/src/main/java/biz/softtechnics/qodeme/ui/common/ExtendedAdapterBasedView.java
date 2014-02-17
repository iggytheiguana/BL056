package biz.softtechnics.qodeme.ui.common;

/**
 * Created by Alex on 10/24/13.
 */
public interface ExtendedAdapterBasedView<T, C extends ExAdapterCallback> extends AdapterBasedView<T> {
    void fill(T t, C c, int position, T previousElement);
}
