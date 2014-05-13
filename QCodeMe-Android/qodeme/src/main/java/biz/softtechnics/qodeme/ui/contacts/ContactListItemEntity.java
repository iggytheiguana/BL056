package biz.softtechnics.qodeme.ui.contacts;

import biz.softtechnics.qodeme.core.io.model.Contact;

/**
 * Created by Alex on 12/10/13.
 */
public class ContactListItemEntity {

    private Contact mContact;
    private int mState;
    private boolean mHeader;
    private boolean isChecked = false;

    public ContactListItemEntity(boolean header, int state, Contact contact) {
        this.mContact = contact;
        this.mState = state;
        this.mHeader = header;
    }

    public Contact getContact() {
        return mContact;
    }

    public void setContact(Contact mContact) {
        this.mContact = mContact;
    }

    public int getState() {
        return mState;
    }

    public void setState(int mState) {
        this.mState = mState;
    }

    public boolean isHeader() {
        return mHeader;
    }

    public void setHeader(boolean mHeader) {
        this.mHeader = mHeader;
    }

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	public boolean isChecked() {
		return isChecked;
	}

}

