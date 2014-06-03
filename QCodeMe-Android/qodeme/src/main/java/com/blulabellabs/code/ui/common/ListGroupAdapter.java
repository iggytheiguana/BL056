package com.blulabellabs.code.ui.common;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.Collection;
import java.util.List;

public class ListGroupAdapter<T extends AdapterBasedView<E>, E> extends ArrayAdapter<E> {

    protected LayoutInflater layoutInflater;
    protected int layoutResId;
    protected T view;
    protected SparseArray<T> viewMap = new SparseArray<T>();

    public ListGroupAdapter(Context context, int layoutResId, List<E> list) {
        super(context, layoutResId, 0, list);
        this.layoutInflater = LayoutInflater.from(context);
        this.layoutResId = layoutResId;
    }

    @Override
    public void addAll(Collection<? extends E> collection) {
        if (collection == null) return; // Protect from null object
        for (E elem : collection) {
            add(elem);
        }
    }

    public T getView(int position) {
        return viewMap.get(position);
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResId, null);
        }

        view = (T) convertView;
        E e = (E) getItem(position);
        view.fill(e);
        viewMap.put(position, view);

        return convertView;
    }

}
