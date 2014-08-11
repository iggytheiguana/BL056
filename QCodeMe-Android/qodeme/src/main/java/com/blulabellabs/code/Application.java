package com.blulabellabs.code;

//import static com.blulabellabs.code.utils.Fonts.CALIBRI_BOLD;
//import static com.blulabellabs.code.utils.Fonts.CALIBRI_BOLD_ITALIC;
//import static com.blulabellabs.code.utils.Fonts.CALIBRI_ITALIC;
//import static com.blulabellabs.code.utils.Fonts.CALIBRI_REGULAR;
import static com.blulabellabs.code.utils.Fonts.ROBOTO_BOLD;
import static com.blulabellabs.code.utils.Fonts.ROBOTO_BOLD_ITALIC;
import static com.blulabellabs.code.utils.Fonts.ROBOTO_ITALIC;
import static com.blulabellabs.code.utils.Fonts.ROBOTO_REGULAR;
//import static com.blulabellabs.code.utils.Fonts.CALIBRI_BOLD_ITALIC;
//import static com.blulabellabs.code.utils.Fonts.CALIBRI_ITALIC;
//import static com.blulabellabs.code.utils.Fonts.CALIBRI_REGULAR;

import java.lang.ref.WeakReference;

import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.RestAsyncHelper;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.one2one.ChatListFragment;
import com.blulabellabs.code.ui.one2one.ChatListGroupPublicFragment;
import com.blulabellabs.code.utils.AnalyticsHelper;
import com.blulabellabs.code.utils.FontUtils;

import android.graphics.Typeface;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.json.JSONException;
import org.json.JSONObject;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

@ReportsCrashes(formKey = "", mailTo = "sas404@tut.by", mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_message)
public class Application extends android.app.Application {

	private WeakReference<MainActivity> weekRefMainActivity;
	public static Typeface typefaceRegular;
	public static Typeface typefaceBold;
	public static Typeface typefaceItalic;
	public static Typeface typefaceItalicBold;
	public static Typeface typefaceMedium;
	public static Typeface typefaceMediumItalic;
	public static Typeface typefaceThin;
    private final WebSocketConnection mConnection = new WebSocketConnection();


	@Override
	public void onCreate() {
        ACRA.init(this);
        super.onCreate();
		AnalyticsHelper.onCreateApplication(this);
		QodemePreferences.initialize(getApplicationContext());
		RestAsyncHelper.initialize(getApplicationContext());
		// FontUtils.setDefaultFontFormAssets(getAssets(),
		// CALIBRI_REGULAR.toString(), CALIBRI_BOLD.toString(),
		// CALIBRI_ITALIC.toString(), CALIBRI_BOLD_ITALIC.toString());
		FontUtils.setDefaultFontFormAssets(getAssets(), ROBOTO_REGULAR.toString(),
				ROBOTO_BOLD.toString(), ROBOTO_ITALIC.toString(), ROBOTO_BOLD_ITALIC.toString());

//		typefaceRegular = Typeface.createFromAsset(getAssets(), "fonts/RobotoRegular.ttf");
		typefaceRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
		typefaceThin = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
		typefaceBold = Typeface.createFromAsset(getAssets(), "fonts/RobotoBold.ttf");
		typefaceItalic = Typeface.createFromAsset(getAssets(), "fonts/RobotoItalic.ttf");
		typefaceItalicBold = Typeface.createFromAsset(getAssets(), "fonts/RobotoBoldItalic.ttf");
		typefaceMedium = Typeface.createFromAsset(getAssets(), "fonts/Roboto_Medium_2.ttf");
		typefaceMediumItalic = Typeface.createFromAsset(getAssets(),
				"fonts/Roboto_MediumItalic_2.ttf");
	}

	public void setMainActivity(MainActivity mainActivity) {
		weekRefMainActivity = new WeakReference<MainActivity>(mainActivity);
	}

	public MainActivity getMainActivity() {
		if (weekRefMainActivity == null)
			return null;
		return weekRefMainActivity.get();
	}

	public boolean isActive() {
		MainActivity activity = getMainActivity();
        return activity != null && activity.isActive();
    }


    public void start() {
        final String wsuri = "ws://54.204.45.228/python";
        try {
            if (!mConnection.isConnected()) {
                mConnection.connect(wsuri, new WebSocketHandler() {
                    @Override
                    public void onOpen() {
                        if (getMainActivity().mChatList != null)
                            for (ChatLoad chatLoad : getMainActivity().mChatList) {
                                if (chatLoad.type != 2)
                                    sendRegisterForChatEvents(chatLoad.chatId);
                            }
                    }

                    @Override
                    public void onTextMessage(String payload) {
                        receiveWebSocketMessageWith(payload);
                    }

                    @Override
                    public void onClose(int code, String reason) {
                    }
                });
            }
        } catch (WebSocketException e) {
            Log.d("", e.toString());
        }
    }

    public void stop() {
        try {
            if (mConnection.isConnected()) {
                mConnection.disconnect();
                Log.d("", "Disconnected web socket");
            }
        } catch (Exception e) {
            Log.d("", e.toString());
        }
    }

    public void sendUserStoppedTypingMessage(long chatId) {
        String activityName = "sendUserStoppedTypingMessage:";
        if (mConnection.isConnected()) {
            String restToken = QodemePreferences.getInstance().getRestToken();
            int event = 2;
            Log.d("", activityName + "Sending user stopped typing message...");
            sendWebSocketMessageWith(chatId, restToken, event);
        }
    }

    public void sendWebSocketMessageWith(long chatId, String authToken, int event) {
        String activityName = "sendWebSocketMessageWith:";
        if (mConnection.isConnected()) {
            try {
                JSONObject json = new JSONObject();
                json.put("chatId", chatId);
                json.put("authToken", authToken);
                json.put("event", event);
                mConnection.sendTextMessage(json.toString());
                Log.d("", activityName + "Successfully sent payload " + json.toString());
            } catch (JSONException e) {
                Log.e("", activityName + "Received JSONException: " + e.toString());
            } catch (Exception e) {
                Log.e("", activityName + "Received Exception: " + e.toString());
            }
        }
    }

    public void sendRegisterForChatEvents(long chatId) {
        String activityName = "sendRegisterForChatEvents:";
        if (mConnection.isConnected()) {
            String restToken = QodemePreferences.getInstance().getRestToken();
            int event = 0;
            Log.d("", activityName + "Sending register for chat event message...");
            sendWebSocketMessageWith(chatId, restToken, event);
        }
    }

    public void sendUserTypingMessage(long chatId) {
        String activityName = "sendUserTypingMessage:";
        if (mConnection.isConnected()) {
            sendRegisterForChatEvents(chatId);
            String restToken = QodemePreferences.getInstance().getRestToken();
            int event = 1;
            Log.d("", activityName + "Sending user typing message...");
            sendWebSocketMessageWith(chatId, restToken, event);
        }
    }

    public void receiveWebSocketMessageWith(String message) {
        String activityName = "receiveWebSocketMessageWith:";
        try {
            JSONObject messageJson = new JSONObject(message);
            long chatId = messageJson.getLong("chatId");
            int event = messageJson.getInt("event");
            Log.d("", activityName + "Received event: " + event + " in chat: " + chatId);
            if (event == 1) {
                getMainActivity().receiveOtherUserStartedTypingEvent(chatId);
            } else if (event == 2) {
                getMainActivity().receiveOtherUserStoppedTypingEvent(chatId);
            }
        } catch (JSONException je) {
            Log.e("", activityName + je.toString());
        }
    }

}
