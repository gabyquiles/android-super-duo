package barqsoft.footballscores.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by gabrielquiles-perez on 11/18/15.
 */
public class Fixture {
    @SerializedName("date")
    @Expose
    private Date mDate;

    @SerializedName("status")
    @Expose
    private String mStatus;

    @SerializedName("homeTeamName")
    @Expose
    private String mHomeTeamName;

    @SerializedName("awayTeamName")
    @Expose
    private String mAwayTeamName;

    @SerializedName("matchday")
    @Expose
    private Long mMatchday;

    @SerializedName("_links")
    @Expose
    private Map<String, URI> mLinks;



    @SerializedName("result")
    @Expose
    private Map<String, Long> mResult;


    private Team mHomeTeam;
    private Team mAwayTeam;
    private Long mHomeGoals;
    private Long mAwayGoals;


    public Long getId() {
        return extractId(mLinks.get("self"));
    }

    public Long getHomeTeamId() {
        return extractId(mLinks.get("homeTeam"));
    }

    public Long getAwayTeamId() {
        return extractId(mLinks.get("awayTeam"));
    }

    public Long getSeasonId() {
        return extractId(mLinks.get("awayTeam"));
    }

    public String getDateStr() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(mDate);
    }

    public String getTimeStr() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(mDate);
    }

    public String getHomeTeamName() {
        return mHomeTeamName;
    }

    public String getAwayTeamName() {
        return mAwayTeamName;
    }

    public Long getMatchday() {
        return mMatchday;
    }

    public Long getHomeGoals() {
        return getGoals("goalsHomeTeam");
    }

    public Long getAwayGoals() {
        return getGoals("goalsAwayTeam");
    }

    private Long getGoals(String key) {
        Long goals = mResult.get(key);
        if( goals == null) {
            goals = new Long(-1);
        }
        return goals;
    }

    private Long extractId(URI url) {
        String path = url.getPath();
        String idStr = path.substring(path.lastIndexOf('/') + 1);
        return Long.parseLong(idStr);
    }
}
