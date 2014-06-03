package com.blulabellabs.code.ui.common;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Alex on 10/24/13.
 */
public class ExGroupListAdapter<T extends ExGroupAdapterBasedView<E, C>, E, C extends ExAdapterCallback> extends ListGroupAdapter<T, E> {


    private final C callback;
    private List<E> mItems;

    public ExGroupListAdapter(Context context, int layoutResId, List<E> list, C callback) {
        super(context, layoutResId, list);
        this.callback = callback;
        mItems = list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResId, null);
        }

        view = (T) convertView;
        E e = (E) getItem(position);
        view.fill(e, callback, position);
        viewMap.put(position, view);

        return convertView;
    }


}
