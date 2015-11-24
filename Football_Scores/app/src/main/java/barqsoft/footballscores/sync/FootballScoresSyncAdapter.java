package barqsoft.footballscores.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.text.format.Time;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.api.FootballDataApiFactory;
import barqsoft.footballscores.api.FootballDataService;
import barqsoft.footballscores.models.Fixture;
import barqsoft.footballscores.models.Team;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Retrofit;

/**
 * Created by gabrielquiles-perez on 11/9/15.
 */
public class FootballScoresSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = FootballScoresSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the weather, in milliseconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private static final String[] NOTIFY_MATCHES_PROJECTION = new String[] {
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.MATCH_DAY,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
    };
    // these indices must match the projection
    private static final int INDEX_MATCH_ID = 0;
    private static final int INDEX_DATE = 1;
    private static final int INDEX_TIME = 2;
    private static final int INDEX_LEAGUE = 3;
    private static final int INDEX_MATCH_DAY = 4;
    private static final int INDEX_HOME_TEAM =5;
    private static final int INDEX_AWAY_TEAM = 6;
    private static final int INDEX_HOME_GOALS = 7;
    private static final int INDEX_AWAY_GOALS = 8;


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOCATION_STATUS_OK, LOCATION_STATUS_SERVER_DOWN,
            LOCATION_STATUS_SERVER_INVALID, LOCATION_STATUS_UNKNOWN, LOCATION_STATUS_INVALID_LOCATION})
    public @interface LocationStatus{};

    public static final int LOCATION_STATUS_OK = 0;
    public static final int LOCATION_STATUS_SERVER_DOWN = 1;
    public static final int LOCATION_STATUS_SERVER_INVALID = 2;
    public static final int LOCATION_STATUS_UNKNOWN = 3;
    public static final int LOCATION_STATUS_INVALID_LOCATION = 4;
    public static final String ACTION_DATA_UPDATED = "barqsoft.footballscores.ACTION_DATA_UPDATED";

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int FOOTBALL_SCORE_NOTIFICATION_ID = 3004;

    private FootballDataService mApiService;

    public FootballScoresSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        mApiService = FootballDataApiFactory.getInstance(getContext());
        Call<List<Fixture>> call =  mApiService.getFixtures("p2", null);
        processApiCall(call);

        call =  mApiService.getFixtures("n2", null);
        processApiCall(call);

        return;

    }

    private void processApiCall(Call call) {
        call.enqueue(new Callback<List<Fixture>>() {
            @Override
            public void onResponse(retrofit.Response<List<Fixture>> response, Retrofit retrofit) {
                List<Fixture> list = response.body();
                if(list != null) {
                    insertFixtures(getContext().getApplicationContext(), list);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // Log error here since request failed
            }
        });
    }

    private int insertFixtures(Context context, List matches) {

        Vector<ContentValues> values = new Vector <ContentValues> (matches.size());

        for(int i = 0;i < matches.size();i++)
        {
            Fixture fixture = (Fixture) matches.get(i);

            getTeam(fixture.getHomeTeamId());
            getTeam(fixture.getAwayTeamId());

            ContentValues match_values = new ContentValues();
            match_values.put(DatabaseContract.scores_table.MATCH_ID, fixture.getId());
            match_values.put(DatabaseContract.scores_table.DATE_COL, fixture.getDateStr());
            match_values.put(DatabaseContract.scores_table.TIME_COL,fixture.getTimeStr());
            match_values.put(DatabaseContract.scores_table.HOME_COL,fixture.getHomeTeamName());
            match_values.put(DatabaseContract.scores_table.AWAY_COL,fixture.getAwayTeamName());
            match_values.put(DatabaseContract.scores_table.HOMEID_COL,fixture.getHomeTeamId());
            match_values.put(DatabaseContract.scores_table.AWAYID_COL,fixture.getAwayTeamId());
            match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL,fixture.getHomeGoals());
            match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL,fixture.getAwayGoals());
            match_values.put(DatabaseContract.scores_table.LEAGUE_COL,fixture.getSeasonId());
            match_values.put(DatabaseContract.scores_table.MATCH_DAY,fixture.getMatchday());

            values.add(match_values);
        }
        int inserted_data = 0;
        if(values.size() > 0) {
            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);
            inserted_data = context.getContentResolver().bulkInsert(
                    DatabaseContract.BASE_CONTENT_URI, insert_data);

            //Delete old data
            Time dayTime = new Time();
            dayTime.setToNow();
            // Get current day and substract 4 days
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis() - (4 * DAY_IN_MILLIS),
                    dayTime.gmtoff);

            getContext().getContentResolver().delete(DatabaseContract.scores_table.CONTENT_URI,
                    DatabaseContract.scores_table.DATE_COL + " <= ?",
                    new String[] {Long.toString(dayTime.setJulianDay(julianStartDay - 1))});

            //update Widgets
            updateWidgets();
        }
        return inserted_data;

    }

    private void getTeam(final Long team_id) {
        String[] MATCH_COLUMNS = { DatabaseContract.scores_table._ID };
        //If team does not exists
        Uri teamUri = DatabaseContract.teams_table.buildTeamUri(team_id);
        Cursor data = getContext().getContentResolver().query(teamUri, MATCH_COLUMNS, null,
                null, null);
        if (data.getCount() == 0) {
            //Insert Team information
            Call<Team> call = mApiService.getTeam(team_id);
            call.enqueue(new Callback<Team>() {
                @Override
                public void onResponse(retrofit.Response<Team> response, Retrofit retrofit) {
                    Team team = response.body();
                    if(team != null) {
                        ContentValues team_values = new ContentValues();
                        team_values.put(DatabaseContract.teams_table.TEAM_ID, team_id);
                        team_values.put(DatabaseContract.teams_table.FULL_NAME, team.getFullName());
                        team_values.put(DatabaseContract.teams_table.SHORT_NAME, team.getShortName());
                        team_values.put(DatabaseContract.teams_table.CREST, team.getCrestUrl());
                        getContext().getContentResolver().insert(
                                DatabaseContract.teams_table.CONTENT_URI, team_values);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    // Log error here since request failed
                }
            });
        }
        data.close();
    }

    private void updateWidgets() {
        Context context = getContext();
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);

    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        FootballScoresSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}