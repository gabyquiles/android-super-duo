package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;
import barqsoft.footballscores.images.ImageLoader;
import barqsoft.footballscores.images.PNGLoader;
import barqsoft.footballscores.images.SVGLoader;


/**
 * Remote Views Service for widget showing today's matches. Is in charge of displaying the
 * date in the widget.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TodayMatchesWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = TodayMatchesWidgetRemoteViewsService.class.getSimpleName();
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

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                Uri todayMatchesUri = DatabaseContract.scores_table.buildScoreWithDate();
                Date todaysDate = new Date(System.currentTimeMillis());
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                String[] dateArgs = {mformat.format(todaysDate)};
                data = getContentResolver().query(todayMatchesUri,
                        MATCH_COLUMNS,
                        null,
                        dateArgs,
                        DatabaseContract.scores_table.DATE_COL + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_today_matches_list_item);
                views.setTextViewText(R.id.home_name, data.getString(COL_HOME));
                views.setTextViewText(R.id.away_name, data.getString(COL_AWAY));
                views.setTextViewText(R.id.date_textview, data.getString(COL_TIME));
                views.setTextViewText(R.id.score_textview, Utilities.getScores(data.getInt(COL_HOME_GOALS), data.getInt(COL_AWAY_GOALS)));

                //Had to export provider, was giving the error "requires the provider be exported, or grantUriPermission()"
                String homeCrestURL = Utilities.getTeamCrestUrl(TodayMatchesWidgetRemoteViewsService.this, data.getLong(COL_HOME_ID));
                String awayCrestURL = Utilities.getTeamCrestUrl(TodayMatchesWidgetRemoteViewsService.this, data.getLong(COL_AWAY_ID));

                Bitmap homeCrest = null;
                Bitmap awayCrest = null;
                ImageLoader loader = null;
                if(homeCrestURL != null) {
                    if(homeCrestURL.toLowerCase().endsWith("svg")) {
                        loader = SVGLoader.getInstance(getApplicationContext());
                    } else if(homeCrestURL.toLowerCase().endsWith("png")) {
                        loader = PNGLoader.getInstance(getApplicationContext());
                    }
                    if(loader != null) {
                        homeCrest = loader.getTeamCrestAsBitmap(homeCrestURL);
                    }
                }

                if(awayCrestURL != null) {
                    if(awayCrestURL.toLowerCase().endsWith("svg")) {
                        loader = SVGLoader.getInstance(getApplicationContext());
                    } else if(awayCrestURL.toLowerCase().endsWith("png")) {
                        loader = PNGLoader.getInstance(getApplicationContext());
                    }
                    if(loader != null) {
                        awayCrest = loader.getTeamCrestAsBitmap(awayCrestURL);
                    }
                }

                if(homeCrest != null) {
                    views.setImageViewBitmap(R.id.home_crest, homeCrest);
                } else {
                    views.setImageViewResource(R.id.home_crest, R.drawable.no_icon);
                }
                if(awayCrest != null) {
                    views.setImageViewBitmap(R.id.away_crest, awayCrest);
                } else {
                    views.setImageViewResource(R.id.away_crest, R.drawable.no_icon);
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    views.setContentDescription(R.id.home_name, data.getString(COL_HOME));
                    views.setContentDescription(R.id.away_name, data.getString(COL_AWAY));
                    views.setContentDescription(R.id.date_textview, data.getString(COL_TIME));
                    views.setContentDescription(R.id.score_textview, Utilities.getScores(data.getInt(COL_HOME_GOALS), data.getInt(COL_AWAY_GOALS)));
                }

                final Intent fillInIntent = new Intent();

                Uri todaysUri = DatabaseContract.scores_table.buildScoreWithDate();
                fillInIntent.setData(todaysUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_today_matches_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(COL_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
