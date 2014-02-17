package biz.softtechnics.qodeme.core.io.model;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Alex on 12/2/13.
 */
public class Contacts {

    @SerializedName("contacts")
    public Contact[] contacts;

    public List<Contact> getList(){
        return Arrays.asList(contacts);
    }
}
