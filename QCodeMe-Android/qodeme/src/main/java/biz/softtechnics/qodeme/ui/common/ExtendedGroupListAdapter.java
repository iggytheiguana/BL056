package biz.softtechnics.qodeme.ui.common;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import biz.softtechnics.qodeme.ui.one2one.ChatInsideGroupFragment.One2OneChatListInsideFragmentCallback;

import java.util.List;

/**
 * Created by Alex on 10/24/13.
 */
public class ExtendedGroupListAdapter<T extends ExtendedGroupAdapterBasedView<E, C>, E, C extends ExAdapterCallback> extends ListAdapter<T, E> {


    private final C callback;
    private final One2OneChatListInsideFragmentCallback callback2;

    public ExtendedGroupListAdapter(Context context, int layoutResId, List<E> list, C callback, One2OneChatListInsideFragmentCallback callback2) {
        super(context, layoutResId, list);
        this.callback = callback;
        this.callback2 = callback2;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResId, null);
        }

        view = (T) convertView;
        E e = (E) getItem(position);
        E previous = position > 0 ? (E) getItem(position - 1) : null;
        E next;
		try {
			next = (E) getItem(position + 1);
		} catch (Exception ex) {
			next = null;
		}
        view.fill(e, callback, position, previous,next, callback2);
        viewMap.put(position, view);

        return convertView;
    }


}
