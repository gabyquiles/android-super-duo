package barqsoft.footballscores;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresProvider extends ContentProvider
{
    private static final String LOG_TAG = ScoresProvider.class.getSimpleName();
    private static ScoresDBHelper mOpenHelper;
    private static final int MATCHES = 100;
    private static final int MATCHES_WITH_LEAGUE = 101;
    private static final int MATCHES_WITH_ID = 102;
    private static final int MATCHES_WITH_DATE = 103;
    private static final int MATCHES_WITH_SCORE = 104;
    private static final int TEAM = 105;
    private UriMatcher muriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder ScoreQuery =
            new SQLiteQueryBuilder();
    private static final String SCORES_BY_LEAGUE = DatabaseContract.scores_table.LEAGUE_COL + " = ?";
    private static final String SCORES_BY_DATE =
            DatabaseContract.scores_table.DATE_COL + " LIKE ?";
    private static final String SCORES_BY_ID =
            DatabaseContract.scores_table.MATCH_ID + " = ?";
    private static final String SCORED_MATCHES = DatabaseContract.scores_table.HOME_GOALS_COL +
            " >= 0 AND " + DatabaseContract.scores_table.HOME_GOALS_COL + " >= 0";
    private static final String TEAM_BY_ID =
            DatabaseContract.teams_table.TEAM_ID + " = ?";


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.CONTENT_AUTHORITY.toString();
        matcher.addURI(authority, null , MATCHES);
        matcher.addURI(authority, DatabaseContract.PATH+"/league" , MATCHES_WITH_LEAGUE);
        matcher.addURI(authority, DatabaseContract.PATH+"/id" , MATCHES_WITH_ID);
        matcher.addURI(authority, DatabaseContract.PATH+"/date" , MATCHES_WITH_DATE);
        matcher.addURI(authority, DatabaseContract.PATH+"/scored", MATCHES_WITH_SCORE);
        matcher.addURI(authority, DatabaseContract.TEAM_PATH+"/#", TEAM);
        matcher.addURI(authority, DatabaseContract.TEAM_PATH, TEAM);
        return matcher;
    }

//    private int match_uri(Uri uri)
//    {
//        String link = uri.toString();
//        {
//           if(link.contentEquals(DatabaseContract.BASE_CONTENT_URI.toString()))
//           {
//               return MATCHES;
//           }
//           else if(link.contentEquals(DatabaseContract.scores_table.buildScoreWithDate().toString()))
//           {
//               return MATCHES_WITH_DATE;
//           }
//           else if(link.contentEquals(DatabaseContract.scores_table.buildScoreWithId().toString()))
//           {
//               return MATCHES_WITH_ID;
//           }
//           else if(link.contentEquals(DatabaseContract.scores_table.buildScoreWithLeague().toString()))
//           {
//               return MATCHES_WITH_LEAGUE;
//           }
//           else if(link.contentEquals(DatabaseContract.scores_table.buildScoredMatches().toString())) {
//               return MATCHES_WITH_SCORE;
//           }
//           else if(link.contentEquals(DatabaseContract.scores_table.buildScoredMatches().toString())) {
//               return MATCHES_WITH_SCORE;
//           }
//        }
//        return -1;
//    }
    @Override
    public boolean onCreate()
    {
        mOpenHelper = new ScoresDBHelper(getContext());
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        return 0;
    }

    @Override
    public String getType(Uri uri)
    {
        final int match = muriMatcher.match(uri);
        switch (match) {
            case MATCHES:
                return DatabaseContract.scores_table.CONTENT_TYPE;
            case MATCHES_WITH_LEAGUE:
                return DatabaseContract.scores_table.CONTENT_TYPE;
            case MATCHES_WITH_ID:
                return DatabaseContract.scores_table.CONTENT_ITEM_TYPE;
            case MATCHES_WITH_DATE:
                return DatabaseContract.scores_table.CONTENT_TYPE;
            case MATCHES_WITH_SCORE:
                return DatabaseContract.scores_table.CONTENT_ITEM_TYPE;
            case TEAM:
                return DatabaseContract.scores_table.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri :" + uri );
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Cursor retCursor;
        //Log.v(FetchScoreTask.LOG_TAG,uri.getPathSegments().toString());
//        int match = match_uri(uri);
        //Log.v(FetchScoreTask.LOG_TAG,SCORES_BY_LEAGUE);
        //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[0]);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(match));
        switch (muriMatcher.match(uri))
        {
            case MATCHES: retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.SCORES_TABLE,
                    projection,null,null,null,null,sortOrder); break;
            case MATCHES_WITH_DATE:
                    //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[1]);
                    //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[2]);
                    retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.SCORES_TABLE,
                    projection,SCORES_BY_DATE,selectionArgs,null,null,sortOrder); break;
            case MATCHES_WITH_ID: retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.SCORES_TABLE,
                    projection,SCORES_BY_ID,selectionArgs,null,null,sortOrder); break;
            case MATCHES_WITH_LEAGUE: retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.SCORES_TABLE,
                    projection,SCORES_BY_LEAGUE,selectionArgs,null,null,sortOrder); break;
            case MATCHES_WITH_SCORE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, SCORED_MATCHES,null,null,null,sortOrder); break;
            case TEAM:
                retCursor = getTeamById(uri, projection); break;
            default: throw new UnsupportedOperationException("Unknown Uri" + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return retCursor;
    }

    private Cursor getTeamById(Uri uri, String[] projection) {
        String id = DatabaseContract.teams_table.getIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = TEAM_BY_ID;
        selectionArgs = new String[]{id};

        Log.v(LOG_TAG, TEAM_BY_ID);
        return mOpenHelper.getReadableDatabase().query(DatabaseContract.TEAMS_TABLE,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
//        final int match = match_uri(uri);
        Uri returnUri;

        switch (muriMatcher.match(uri)) {
            case TEAM: {
                long _id = db.insert(DatabaseContract.TEAMS_TABLE, null, values);
                if ( _id > 0 )
                    returnUri = DatabaseContract.teams_table.buildTeamUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        //db.delete(DatabaseContract.SCORES_TABLE,null,null);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(muriMatcher.match(uri)));
        switch (muriMatcher.match(uri))
        {
            case MATCHES:
                db.beginTransaction();
                int returncount = 0;
                try
                {
                    for(ContentValues value : values)
                    {
                        long _id = db.insertWithOnConflict(DatabaseContract.SCORES_TABLE, null, value,
                                SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1)
                        {
                            returncount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri,null);
                return returncount;
            default:
                return super.bulkInsert(uri,values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
}
