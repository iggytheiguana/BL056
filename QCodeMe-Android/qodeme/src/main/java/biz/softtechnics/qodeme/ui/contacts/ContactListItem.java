package biz.softtechnics.qodeme.ui.contacts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.ui.common.ExAdapterBasedView;
import biz.softtechnics.qodeme.utils.Converter;

/**
 * Created by Alex on 10/20/13.
 */
public class ContactListItem extends LinearLayout implements ExAdapterBasedView<ContactListItemEntity, ContactListAdapterCallback> {

    private final Context mContext;
    private ImageView mQrCode;
    private TextView mName;
    private View mDragView;
    private Contact mContact;
    private Object mCallback;
    private int mPosition;
    private TextView mContactInfoTextView;

    public ContactListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    @Override
    public void fill(ContactListItemEntity contactListItemEntity) {
        this.mContact = contactListItemEntity.getContact();
        int color = mContact.color == 0 ? Color.GRAY : mContact.color;
        String sQrCode = mContact.qrCode;
        Drawable d = mContext.getResources().getDrawable(R.drawable.white_color_logo);

        getName().setText(mContact.title != null ? mContact.title : "User");
        getName().setTextColor(color);

        SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yy HH:mm");
        String dateStr = fmtOut.format(new Date(Converter.getCrurentTimeFromTimestamp(mContact.date)));
        getContactInfo().setText(dateStr + ", " + mContact.location);
    }

    ;

    public TextView getName() {
        return mName = mName != null ? mName : (TextView) findViewById(R.id.name);
    }

    public TextView getContactInfo() {
        return mContactInfoTextView = mContactInfoTextView != null ? mContactInfoTextView :
                (TextView) findViewById(R.id.contact_info);
    }

    public Contact getContact() {
        return mContact;
    }

    @Override
    public void fill(ContactListItemEntity contactListItemEntity, ContactListAdapterCallback callback, int position) {
        this.mCallback = callback;
        this.mPosition = position;
        fill(contactListItemEntity);
    }
}