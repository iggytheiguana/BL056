package biz.softtechnics.qodeme.core.io.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Alex on 12/4/13.
 */
public class UserSettings {

    /** Invitation message */
    public String message;
    /** Invite with message */
    @SerializedName("with_message")
    public Integer withMessage;
    /** Public name */
    @SerializedName("public_name")
    public String publicName;
    /** Invite with public name */
    @SerializedName("with_pubname")
    public Integer withPublicName;
    /** Automatically accept contact invitations */
    @SerializedName("auto_accept")
    public Integer withAutoAccept;
    /** Location str. City name */
    public String location;
    /** Set the app to not store date/time/location info of your connections on/off */
    @SerializedName("set_timeloc")
    public Integer seveTimeLocation;
}
