package biz.softtechnics.qodeme.ui.one2one;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.io.model.Message;
import biz.softtechnics.qodeme.core.provider.QodemeContract;
import biz.softtechnics.qodeme.ui.common.ExtendedAdapterBasedView;
import biz.softtechnics.qodeme.utils.Converter;
import biz.softtechnics.qodeme.utils.Helper;

/**
 * Created by Alex on 10/23/13.
 */
public class ChatListSubItem extends RelativeLayout implements ExtendedAdapterBasedView<Message, ChatListSubAdapterCallback> {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT_MAIN = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT_HEADER = new SimpleDateFormat("MMM dd yyyy", Locale.US);

    private final Context context;
    private TextView message;
    private ListView subList;
    private ChatListSubAdapterCallback callback;
    private int position;
    private Message previousMessage;
    private TextView date;
    private TextView dateHeader;
    private RelativeLayout headerContainer;
    private View opponentSeparator;

    public ChatListSubItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public TextView getMessage() {
        return message = message != null ? message : (TextView) findViewById(R.id.message);
    }

    public TextView getDate() {
        return date = date != null ? date : (TextView) findViewById(R.id.date);
    }

    public TextView getDateHeader() {
        return dateHeader = dateHeader != null ? dateHeader : (TextView) findViewById(R.id.date_header);
    }

    public RelativeLayout getHeaderContainer() {
        return headerContainer = headerContainer != null ? headerContainer : (RelativeLayout) findViewById(R.id.header_container);
    }

    public View getOpponentSeparator() {
        return opponentSeparator = opponentSeparator != null ? opponentSeparator : (View) findViewById(R.id.opponent_separator);
    }

    @Override
    public void fill(Message me, ChatListSubAdapterCallback callback, int position, Message previousMessage) {
        this.callback = callback;
        this.position = position;
        this.previousMessage = previousMessage;
        fill(me);
    }

    @Override
    public void fill(Message me) {
        getMessage().setText(me.message);
        int color = callback.getColor(me.qrcode);
        getMessage().setTextColor(color);
        //getMessage().setTypeface(callback.getFont(Fonts.ROBOTO_LIGHT));
        getDate().setText(Helper.getTime24(Converter.getCrurentTimeFromTimestamp(me.created)));
        if (isMyMessage(me.qrcode)) {
            switch (me.state) {
                case QodemeContract.Messages.State.LOCAL:
                    getDate().setTextColor(context.getResources().getColor(R.color.text_message_not_send));
                    break;
                case QodemeContract.Messages.State.SENT:
                    getDate().setTextColor(context.getResources().getColor(R.color.text_message_sent));
                    break;
                case QodemeContract.Messages.State.READ:
                case QodemeContract.Messages.State.NOT_READ:
                case QodemeContract.Messages.State.READ_LOCAL:
                case QodemeContract.Messages.State.WAS_READ:
                    getDate().setTextColor(context.getResources().getColor(R.color.text_message_reed));
                    break;
            }
        } else {
            getDate().setTextColor(color);
        }

        getHeaderContainer().setVisibility(View.GONE);
        getOpponentSeparator().setVisibility(View.GONE);
        if (previousMessage != null /*&& !TextUtils.isEmpty(previousMessage.created)*/) {
            try {
                Calendar currentDate = Calendar.getInstance();
                currentDate.setTime(SIMPLE_DATE_FORMAT_MAIN.parse(me.created));

                Calendar previousDate = Calendar.getInstance();
                previousDate.setTime(SIMPLE_DATE_FORMAT_MAIN.parse(previousMessage.created));

                if (currentDate.get(Calendar.DATE) != previousDate.get(Calendar.DATE)) {
                    Date dateTemp = new Date(Converter.getCrurentTimeFromTimestamp(me.created));
                    getDateHeader().setText(SIMPLE_DATE_FORMAT_HEADER.format(dateTemp));
                    getHeaderContainer().setVisibility(View.VISIBLE);
                } else if (!me.qrcode.equalsIgnoreCase(previousMessage.qrcode)) {
                    getOpponentSeparator().setVisibility(View.VISIBLE);
                }

                if (me.qrcode.equalsIgnoreCase(previousMessage.qrcode) &&
                        currentDate.get(Calendar.MINUTE) == previousDate.get(Calendar.MINUTE) &&
                        currentDate.get(Calendar.HOUR_OF_DAY) == previousDate.get(Calendar.HOUR_OF_DAY)) {

                    getDate().setVisibility(View.INVISIBLE);
                } else {
                    getDate().setVisibility(View.VISIBLE);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean isMyMessage(String qr) {
        return TextUtils.equals(qr, QodemePreferences.getInstance().getQrcode());
    }
}
