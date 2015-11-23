package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RemoteViews;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;

/**
 * Created by gabrielquiles-perez on 10/26/15.
 */
public class LatestScoreIntentService extends IntentService {
    private static final String[] MATCH_COLUMNS = {
            DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOMEID_COL,
            DatabaseContract.scores_table.AWAYID_COL,
    };
    // these indices must match the projection
    private static final int COL_ID = 0;
    private static final int COL_HOME = 1;
    private static final int COL_AWAY = 2;
    private static final int COL_HOME_GOALS = 3;
    private static final int COL_AWAY_GOALS = 4;
    private static final int COL_DATE = 5;
    private static final int COL_TIME = 6;
    private static final int COL_HOME_ID = 7;
    private static final int COL_AWAY_ID = 8;

    public LatestScoreIntentService() {
        super("LatestScoreIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                LatestScoreWidgetProvider.class));
        Uri latestMatchWithScoreUri = DatabaseContract.scores_table.buildScoredMatches();
        Cursor data = getContentResolver().query(latestMatchWithScoreUri, MATCH_COLUMNS, null,
                null, DatabaseContract.scores_table.DATE_COL + " DESC, "
                        + DatabaseContract.scores_table.TIME_COL + " DESC" );
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        // Extract the weather data from the Cursor
        int matchId = data.getInt(COL_ID);
        String homeTeam = data.getString(COL_HOME);
        String awayTeam = data.getString(COL_AWAY);
        int homeGoals = data.getInt(COL_HOME_GOALS);
        int awayGoals = data.getInt(COL_AWAY_GOALS);
        Bitmap homeCrest = Utilities.getTeamCrest(getApplicationContext(), data.getLong(COL_HOME_ID));
        Bitmap awayCrest = Utilities.getTeamCrest(getApplicationContext(), data.getLong(COL_AWAY_ID));
        String dateTime = Utilities.getDateTime(data.getString(COL_DATE), data.getString(COL_TIME));
        data.close();

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            // Find the correct layout based on the widget's width
            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
            int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_latest_match_large_width);
            int layoutId;
            if (widgetWidth >= largeWidth) {
                layoutId = R.layout.widget_latest_match_large;
            } else {
                layoutId = R.layout.widget_latest_match;
            }
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, homeTeam, awayTeam);
            }
            String score = Utilities.getScores(homeGoals, awayGoals);
            views.setImageViewBitmap(R.id.home_crest, homeCrest);
            views.setImageViewBitmap(R.id.away_crest, awayCrest);

            views.setTextViewText(R.id.score_textview, score);
            views.setTextViewText(R.id.datetime_textview, dateTime);

            if (widgetWidth >= largeWidth) {
                views.setTextViewText(R.id.home_name, homeTeam);
                views.setTextViewText(R.id.away_name, awayTeam);
            }

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_latest_match_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_latest_match_default_width);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String homeTeam, String awayTeam) {
        views.setContentDescription(R.id.home_crest, homeTeam);
        views.setContentDescription(R.id.away_crest, awayTeam);
    }
}
