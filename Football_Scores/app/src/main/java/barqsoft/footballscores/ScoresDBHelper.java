package barqsoft.footballscores;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import barqsoft.footballscores.DatabaseContract.scores_table;
import barqsoft.footballscores.DatabaseContract.teams_table;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresDBHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "Scores.db";
    private static final int DATABASE_VERSION = 2;
    public ScoresDBHelper(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        final String CreateScoresTable = "CREATE TABLE " + DatabaseContract.SCORES_TABLE + " ("
                + scores_table._ID + " INTEGER PRIMARY KEY,"
                + scores_table.DATE_COL + " TEXT NOT NULL,"
                + scores_table.TIME_COL + " INTEGER NOT NULL,"
                + scores_table.HOME_COL + " TEXT NOT NULL,"
                + scores_table.AWAY_COL + " TEXT NOT NULL,"
                + scores_table.LEAGUE_COL + " INTEGER NOT NULL,"
                + scores_table.HOME_GOALS_COL + " TEXT NOT NULL,"
                + scores_table.AWAY_GOALS_COL + " TEXT NOT NULL,"
                + scores_table.MATCH_ID + " INTEGER NOT NULL,"
                + scores_table.HOMEID_COL + " INTEGER NOT NULL,"
                + scores_table.AWAYID_COL + " INTEGER NOT NULL,"
                + scores_table.MATCH_DAY + " INTEGER NOT NULL,"
                + " UNIQUE ("+scores_table.MATCH_ID+") ON CONFLICT REPLACE"
                + " );";
        db.execSQL(CreateScoresTable);
        final String CreateTeamsTable = "CREATE TABLE " + DatabaseContract.TEAMS_TABLE + " ("
                + teams_table._ID + " INTEGER PRIMARY KEY,"
                + teams_table.TEAM_ID + " INTEGER NOT NULL,"
                + teams_table.FULL_NAME + " TEXT NOT NULL,"
                + teams_table.SHORT_NAME + " TEXT,"
                + teams_table.CREST + " TEXT NOT NULL,"
                + " UNIQUE ("+teams_table.TEAM_ID+") ON CONFLICT REPLACE"
                + " );";
        db.execSQL(CreateTeamsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Remove old values when upgrading.
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.SCORES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.TEAMS_TABLE);
    }
}
