package biz.softtechnics.qodeme.ui.contacts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.ui.common.CustomDotView;
import biz.softtechnics.qodeme.ui.common.ExAdapterBasedView;
import biz.softtechnics.qodeme.utils.Converter;

/**
 * Created by Alex on 10/20/13.
 */
public class ContactListItem extends LinearLayout implements ExAdapterBasedView<ContactListItemEntity, ContactListAdapterCallback> {

    private final Context mContext;
    private ImageView mQrCode;
    private TextView mName;
    private CustomDotView mTextViewDot;
    private View mDragView;
    private Contact mContact;
    private Object mCallback;
    private int mPosition;
    private CheckBox mCheckBox;
    private TextView mContactInfoTextView;
    private boolean isAddContactToChat = false;

    public ContactListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    @Override
    public void fill(final ContactListItemEntity contactListItemEntity) {
        this.mContact = contactListItemEntity.getContact();
        int color = mContact.color == 0 ? Color.GRAY : mContact.color;
        String sQrCode = mContact.qrCode;
        Drawable d = mContext.getResources().getDrawable(R.drawable.white_color_logo);

        getName().setText(mContact.title != null ? mContact.title : "User");
        getName().setTextColor(color);
        
        getDot().setDotColor(color);
        getDot().invalidate();
        if(isAddContactToChat){
        	getCheckBox().setVisibility(View.VISIBLE);
        	getCheckBox().setChecked(contactListItemEntity.isChecked());
        	getCheckBox().setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					contactListItemEntity.setChecked(isChecked);
				}
			});
        }else{
        	getCheckBox().setVisibility(View.GONE);
        }

//        SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.US);
        SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yy h:mm a",Locale.US);
        String dateStr = fmtOut.format(new Date(Converter.getCrurentTimeFromTimestamp(mContact.date)));
        getContactInfo().setText(dateStr + ", " + mContact.location);
        
        getCheckBox().setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				contactListItemEntity.setChecked(isChecked);
			}
		});
    }

    ;

    public TextView getName() {
        return mName = mName != null ? mName : (TextView) findViewById(R.id.name);
    }
    
    public CheckBox getCheckBox() {
        return mCheckBox = mCheckBox != null ? mCheckBox : (CheckBox) findViewById(R.id.checkbox_add);
    }
    
    public CustomDotView getDot() {
        return mTextViewDot = mTextViewDot != null ? mTextViewDot : (CustomDotView) findViewById(R.id.textView_dot);
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

	public void setAddContactToChat(boolean isAddContactToChat) {
		this.isAddContactToChat = isAddContactToChat;
	}

	public boolean isAddContactToChat() {
		return isAddContactToChat;
	}
}