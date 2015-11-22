package barqsoft.footballscores.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.net.URI;
import java.util.Map;

/**
 * Created by gabrielquiles-perez on 11/18/15.
 */
public class Team {

    @SerializedName("name")
    @Expose
    private String mFullName;

    @SerializedName("shortName")
    @Expose
    private String mShortName;

    @SerializedName("crestUrl")
    @Expose
    private String mCrestUrl;

    @SerializedName("_links")
    @Expose
    private Map<String, URI> mLinks;

    public String getFullName() {
        return mFullName;
    }

    public String getShortName() {
        return mShortName;
    }

    public String getCrestUrl() {
        return mCrestUrl;
    }
}
