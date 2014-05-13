package biz.softtechnics.qodeme.ui.one2one;

import android.graphics.Typeface;

import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.ui.common.ExAdapterCallback;
import biz.softtechnics.qodeme.utils.Fonts;

/**
 * Created by Alex on 10/25/13.
 */
public interface ChatListSubAdapterCallback extends ExAdapterCallback {
    int getColor(String senderQrcode);
    Contact getContact(String senderQrcode);
    Typeface getFont(Fonts font);
}
