package barqsoft.footballscores;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import barqsoft.footballscores.sync.FootballScoresSyncAdapter;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    public scoresAdapter mAdapter;
    public static final int SCORES_LOADER = 0;
    private String[] fragmentdate = new String[1];

    public MainScreenFragment()
    {
    }

    public void setFragmentDate(String date)
    {
        fragmentdate[0] = date;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView score_list = (ListView) rootView.findViewById(R.id.scores_list);
        mAdapter = new scoresAdapter(getActivity(),null,0);
        score_list.setAdapter(mAdapter);
        TextView emptyView = (TextView) rootView.findViewById(R.id.empty);
        score_list.setEmptyView(emptyView);
        getLoaderManager().initLoader(SCORES_LOADER,null,this);

        mAdapter.detail_match_id = MainActivity.selected_match_id;
        score_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                ViewHolder selected = (ViewHolder) view.getTag();
                mAdapter.detail_match_id = selected.match_id;
                MainActivity.selected_match_id = (int) selected.match_id;
                mAdapter.notifyDataSetChanged();
            }
        });
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return new CursorLoader(getActivity(),DatabaseContract.scores_table.buildScoreWithDate(),
                null,null,fragmentdate,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        updateEmptyView();
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            cursor.moveToNext();
        }
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        mAdapter.swapCursor(null);
    }

    private void updateEmptyView() {
        if(mAdapter.getCount() == 0) {
            TextView emptyView = (TextView) getView().findViewById(R.id.empty);
            if(emptyView != null) {
                int message = R.string.error_empty_matches_list;

                @FootballScoresSyncAdapter.LocationStatus int status = Utilities.getLocationStatus(getActivity());
                switch (status) {
                    case FootballScoresSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                        message = R.string.error_server_down;
                        break;
                    case FootballScoresSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                        message = R. string.error_server_error;
                        break;
                    case FootballScoresSyncAdapter.LOCATION_STATUS_INVALID_REQUEST:
                        message = R.string.error_invalid_request;
                    default:
                        if (!Utilities.isNetworkAvailable(getActivity())) {
                            message = R.string.error_no_network_connection;
                        }
                }
                emptyView.setText(message);
                emptyView.setContentDescription(getString(message));
            }
        }
    }

}
