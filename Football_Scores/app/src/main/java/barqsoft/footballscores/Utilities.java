package barqsoft.footballscores;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import barqsoft.footballscores.sync.FootballScoresSyncAdapter;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilities
{
    private static  String LOG_TAG = "Utilities";
    public static final int SERIE_A = 357;
    public static final int PREMIER_LEGAUE = 354;
    public static final int CHAMPIONS_LEAGUE = 362;
    public static final int PRIMERA_DIVISION = 358;
    public static final int BUNDESLIGA = 351;
    public static String getLeague(int league_num)
    {
        switch (league_num)
        {
            case SERIE_A : return "Series A";
            case PREMIER_LEGAUE : return "Premier League";
            case CHAMPIONS_LEAGUE : return "UEFA Champions League";
            case PRIMERA_DIVISION : return "Primera Division";
            case BUNDESLIGA : return "Bundesliga";
            default: return "Not known League Please report";
        }
    }
    public static String getMatchDay(int match_day,int league_num)
    {
        if(league_num == CHAMPIONS_LEAGUE)
        {
            if (match_day <= 6)
            {
                return "Group Stages, Matchday : 6";
            }
            else if(match_day == 7 || match_day == 8)
            {
                return "First Knockout round";
            }
            else if(match_day == 9 || match_day == 10)
            {
                return "QuarterFinal";
            }
            else if(match_day == 11 || match_day == 12)
            {
                return "SemiFinal";
            }
            else
            {
                return "Final";
            }
        }
        else
        {
            return "Matchday : " + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals,int awaygoals)
    {
        if(home_goals < 0 || awaygoals < 0)
        {
            return " - ";
        }
        else
        {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static int getTeamCrestByTeamName (String teamname)
    {
        if (teamname==null){return R.drawable.no_icon;}
        switch (teamname)
        { //This is the set of icons that are currently in the app. Feel free to find and add more
            //as you go.
            case "Arsenal London FC" : return R.drawable.arsenal;
            case "Manchester United FC" : return R.drawable.manchester_united;
            case "Swansea City" : return R.drawable.swansea_city_afc;
            case "Leicester City" : return R.drawable.leicester_city_fc_hd_logo;
            case "Everton FC" : return R.drawable.everton_fc_logo1;
            case "West Ham United FC" : return R.drawable.west_ham;
            case "Tottenham Hotspur FC" : return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion" : return R.drawable.west_bromwich_albion_hd_logo;
            case "Sunderland AFC" : return R.drawable.sunderland;
            case "Stoke City FC" : return R.drawable.stoke_city;
            default: return R.drawable.no_icon;
        }
    }

    public static String getDateTime(String date, String time) {
        SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm");
        match_date.setTimeZone(TimeZone.getDefault());
        Date parseddate;
        SimpleDateFormat new_format;
        try {
            parseddate = match_date.parse(date + time);
            new_format = new SimpleDateFormat("MM/dd HH:mm");
            return new_format.format(parseddate);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error parsing date");
        }
        return "Unknown";
    }

    public static String getTeamCrestUrl(Context context, Long team_id) {
        String[] columns = {
                DatabaseContract.teams_table.SHORT_NAME,
                DatabaseContract.teams_table.CREST,
        };

        Uri homeUri = DatabaseContract.teams_table.buildTeamUri(team_id);
        Cursor teamCursor = context.getContentResolver().query(homeUri, columns, null,
                null, null);
        if(teamCursor.moveToFirst()) {
            String url = teamCursor.getString(1);
            teamCursor.close();
            return url;
        } else {
            teamCursor.close();
        }
        return null;
    }

    @SuppressWarnings("ResourceType")
    static public @FootballScoresSyncAdapter.LocationStatus int getLocationStatus(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(context.getString(R.string.pref_location_status_key),
                FootballScoresSyncAdapter.LOCATION_STATUS_UNKNOWN);
    }

    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return
     */
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
