package biz.softtechnics.qodeme.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.Collection;
import java.util.List;

import biz.softtechnics.qodeme.core.provider.QodemeContract;
import biz.softtechnics.qodeme.ui.contacts.ContactListItem;
import biz.softtechnics.qodeme.ui.contacts.ContactListItemEntity;
import biz.softtechnics.qodeme.ui.contacts.ContactListItemInvited;

public class MenuListAdapter<E extends ContactListItemEntity> extends ArrayAdapter<E> {

    protected LayoutInflater layoutInflater;
    protected int layoutResId;
//    protected T view;
//    protected SparseArray<T> viewMap = new SparseArray<T>();

    public MenuListAdapter(Context context, int layoutResId, List<E> list) {
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

//    public T getView(int position) {
//        return viewMap.get(position);
//    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResId, null);
        }

        E e = (E) getItem(position);
        if (e.getState() == QodemeContract.Contacts.State.INVITED) {
            ContactListItemInvited contactListItemInvited = (ContactListItemInvited) convertView;
            contactListItemInvited.fill(e);
            return contactListItemInvited;
        } else {
            ContactListItem view = (ContactListItem) convertView;
            view.fill(e);
//            viewMap.put(position, view);
            return view;
        }
//        return convertView;
    }

}
