package biz.softtechnics.qodeme;


public abstract class ApplicationConstants {

	public static final boolean DEVELOP_MODE = true;
	public static final String PACKAGE_NAME = "biz.softtechnics.qodeme";

    /** Bugsense integration */
    public static final boolean BUGSENSE_ENABLED = true;
    public static final String BUGSENSE_KEY = "15af222a"; //Alex
    //public static final String BUGSENSE_KEY = "a7c5da70";   //Pasha

    /** Google analytics */
    public static final boolean GOOGLE_ANALYTICS_ENABLED = true;

    /** Flurry analytics */
    public static final boolean FLURYY_ENABLED = true;
    public static final String FLURRY_KEY = "72VHX8725WVWGZ6MX2MR";

    /*Debug backend */
    //public static final String BASE_URL = "http://qrchat.softtechnics.biz/api";
    /*Production backend*/
    public static final String BASE_URL = "http://54.208.112.252/api";

    /* GCM */
    public static final String GCM_SENDER_ID = "826323905162";

	/* Actions */
	public static final String ACTION_RECEIVE_GCM_MESSAGE = "ACTION_RECEIVE_GCM_MESSAGE";
	public static final String ACTION_NAVIGATE_TO_INTENT = "ACTION_NAVIGATE_TO_INTENT";
    public static final String QR_CODE_CONTACT_PATTERN = "(qodeme:contact:).*";
    public static final String QR_CODE_CHAT_PATTERN = "(qodeme:chat:).*";
    public static final String QR_CODE_CONTACT_PREFIX = "qodeme:contact:";
    public static final String QR_CODE_CHAT_PREFIX = "qodeme:chat:";
    public static final int REST_SOCKET_TIMEOUT_MS = 5000;


    /**
     * Splash screen duration
     */
    public static int SPLASH_TIME_OUT = 0;

    /**
     * Default conversation card height
     */
    public static final int DEFAULT_HEIGHT_DP = 100;

}


