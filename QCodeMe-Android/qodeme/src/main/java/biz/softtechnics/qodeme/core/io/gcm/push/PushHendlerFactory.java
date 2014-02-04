package biz.softtechnics.qodeme.core.io.gcm.push;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by Alex on 12/10/13.
 */
public class PushHendlerFactory {

    private static final String TYPE = "type";
    private static final String PUSH_CONTACT_ADDED = "Contact added";
    private static final String PUSH_MESSAGE_IN_CHAT = "Message in Chat";
    private static final String PUSH_CONTACT_ACCEPTED = "contact accept";
    private static final String PUSH_CONTACT_REJECTED = "contact reject";
    private static final String PUSH_CONTACT_BLOCK = "contact block";
    private static final String PUSH_MESSAGE_WAS_READ = "Message was read";


    private PushHendlerFactory(){}

    public static BasePushHandler getPushHandler(Context context, Bundle bundle){

        BasePushHandler instance = null;

        if (bundle.getString(TYPE).equals(PUSH_CONTACT_ADDED)){
            instance = new ContactAddPushHandler(context);
        } else if (bundle.getString(TYPE).equals(PUSH_MESSAGE_IN_CHAT)){
            instance = new MessageInChatPushHandler(context);
        } else if (bundle.getString(TYPE).equals(PUSH_CONTACT_ACCEPTED)){
            instance = new ContactAcceptPushHandler(context);
        } else if (bundle.getString(TYPE).equals(PUSH_CONTACT_REJECTED)){
            instance = new ContactRejectPushHandler(context);
        } else if (bundle.getString(TYPE).equals(PUSH_CONTACT_BLOCK)){
            instance = new ContactBlockPushHandler(context);
        } else if (bundle.getString(TYPE).equals(PUSH_MESSAGE_WAS_READ)){
            instance = new MessageWasReadPushHandler(context);
        }

        if (instance != null)
            instance.parse(bundle);

        return instance;
    }

}
