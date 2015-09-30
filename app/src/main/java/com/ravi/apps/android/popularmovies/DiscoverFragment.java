/*
 * Copyright (C) 2015 Ravi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ravi.apps.android.popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.ravi.apps.android.popularmovies.data.MovieContract;

import java.util.ArrayList;

/**
 * Displays a grid of movie poster thumbnails retrieved from the TMDB server. It allows the
 * user to view details of any movie by clicking on it.
 */
public class DiscoverFragment extends Fragment implements GridView.OnItemClickListener {

    // Loader to fetch the list of movies from the TMDB server.
    private static final int LOADER_DISCOVER_ID = 1;

    // Loader to fetch the list of favorite movies from the movie content provider.
    private static final int LOADER_FAVORITE_ID = 2;

    // Discover loader callback handler.
    DiscoverLoaderHandler mDiscoverLoaderHandler;

    // Favorite loader callback handler.
    FavoriteLoaderHandler mFavoriteLoaderHandler;

    // Projection for favorite cursor loader.
    public static final String[] FAVORITE_PROJECTION = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_POSTER_IMAGE
    };

    // Column indices tied to the favorite cursor loader projection.
    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_POSTER_IMAGE = 2;

    // Grid view item index key.
    private static String GRID_INDEX_KEY = "Grid index key";

    // Grid view that displays the movie poster thumbnails.
    private GridView mGridView;

    // Discover movie data adapter.
    private DiscoverAdapter mDiscoverAdapter;

    // Favorite movie data adapter.
    private FavoriteDiscoverAdapter mFavoriteDiscoverAdapter;

    // Text view for displaying appropriate user message when the grid is blank.
    private TextView mEmptyGridMessage;

    // Current sort order preference.
    private String mSortOrderPreference;

    // Index of selected item or first visible item in the grid view.
    private int mGridIndex;

    // Whether a movie has been selected.
    private boolean mIsMovieSelected;

    public DiscoverFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover, container, false);

        // Create the loader callback handlers.
        mDiscoverLoaderHandler = new DiscoverLoaderHandler();
        mFavoriteLoaderHandler = new FavoriteLoaderHandler();

        // Create the data adapters.
        mDiscoverAdapter = new DiscoverAdapter(getActivity(), new ArrayList<Movie>());
        mFavoriteDiscoverAdapter = new FavoriteDiscoverAdapter(getActivity(), null, 0);

        // Get the grid view.
        mGridView = (GridView) rootView.findViewById(R.id.discover_gridview);

        // Set item click event handler for grid view.
        mGridView.setOnItemClickListener(this);

        // Get the text view for empty grid view message and set the text.
        mEmptyGridMessage = (TextView) rootView.findViewById(R.id.status_gridview);
        mEmptyGridMessage.setText(getString(R.string.msg_status_loading));

        // Set the text view for empty grid view.
        mGridView.setEmptyView(mEmptyGridMessage);

        // Get the current sort order from shared preferences.
        mSortOrderPreference = Utility.getSortOrderPreference(getActivity());

        // Check whether there was a configuration change.
        if(savedInstanceState != null) {
            // Restore the grid index only if the sort order preference has not changed.
            restoreGridIndex(savedInstanceState);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Determine the loader to start based on sort order preference.
        if(mSortOrderPreference.equals(getString(R.string.pref_sort_order_favorites))) {
            // Initialize and start the favorite movies loader.
            getLoaderManager().initLoader(LOADER_FAVORITE_ID, null, mFavoriteLoaderHandler);
        } else {
            // Initialize and start the discover movies loader.
            getLoaderManager().initLoader(LOADER_DISCOVER_ID, null, mDiscoverLoaderHandler);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if the sort order preference has changed.
        if(!mSortOrderPreference.equals(Utility.getSortOrderPreference(getActivity()))) {
            // Update the sort order from the shared preferences.
            mSortOrderPreference = Utility.getSortOrderPreference(getActivity());

            // Reset the grid index.
            mGridIndex = 0;

            // Display status message in empty grid view.
            mEmptyGridMessage.setText(getString(R.string.msg_status_loading));

            // Based on the current sort order, start/reset the loaders appropriately.
            if(mSortOrderPreference.equals(getString(R.string.pref_sort_order_favorites))) {
                // Reset the discover movies loader.
                mDiscoverLoaderHandler.onLoaderReset(null);

                // Start the favorite movies loader.
                getLoaderManager().initLoader(LOADER_FAVORITE_ID, null, mFavoriteLoaderHandler);
            } else {
                // Reset the favorite movies loader.
                mFavoriteLoaderHandler.onLoaderReset(null);

                // Explicitly calling onLoaderReset() here as the LoaderManager does not invoke it
                // even after restartLoader() call.
                mDiscoverLoaderHandler.onLoaderReset(null);

                // Restart the favorite movies loader.
                getLoaderManager().restartLoader(LOADER_DISCOVER_ID, null, mDiscoverLoaderHandler);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save the current sort order preference.
        outState.putString(getString(R.string.pref_sort_order_key), mSortOrderPreference);

        // Check if any movie was selected, if not save first visible position.
        if(mIsMovieSelected = false) {
            mGridIndex = mGridView.getFirstVisiblePosition();
        }

        // Save the grid index.
        if(mGridIndex != GridView.INVALID_POSITION) {
            outState.putInt(GRID_INDEX_KEY, mGridIndex);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Set movie selected flag and store the click position into grid index.
        mIsMovieSelected = true;
        mGridIndex = position;

        // Get the movie data corresponding to the movie clicked by user from adapter.
        Movie movie = (Movie) parent.getItemAtPosition(position);

        // Notify the activity that the user clicked a movie and pass on the movie details.
        ((OnMovieSelectedListener) getActivity()).onMovieSelected(movie);
    }

    /**
     * Public interface that needs to be implemented by the hosting activity to receive
     * notifications from fragment whenever the user selects/clicks on a movie.
     */
    public interface OnMovieSelectedListener {
        void onMovieSelected(Movie movie);
    }

    /**
     * Retrieves and restores the grid index after configuration change only if
     * the sort order preference was not changed.
     */
    private void restoreGridIndex(Bundle savedInstanceState) {
        // Check if the sort order preference is unchanged.
        if(savedInstanceState.containsKey(getString(R.string.pref_sort_order_key))) {
            if(mSortOrderPreference
                    .equals(savedInstanceState.get(getString(R.string.pref_sort_order_key)))) {
                // Retrieve and restore the saved grid index.
                if(savedInstanceState.containsKey(GRID_INDEX_KEY)) {
                    mGridIndex = savedInstanceState.getInt(GRID_INDEX_KEY);
                }
            }
        }
    }

    /**
     * Loader callback handler for the discover movies loader.
     */
    public final class DiscoverLoaderHandler
            implements LoaderManager.LoaderCallbacks<DiscoverLoaderResult> {

        @Override
        public Loader<DiscoverLoaderResult> onCreateLoader(int id, Bundle args) {
            // Create and return the discover movie loader.
            return new DiscoverLoader(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<DiscoverLoaderResult> loader, DiscoverLoaderResult data) {
            // Check if sort order is favorites, if so, then ignore and return.
            if(mSortOrderPreference.equals(getString(R.string.pref_sort_order_favorites))) {
                return;
            }

            // Set the discover adapter onto the grid view.
            mGridView.setAdapter(mDiscoverAdapter);

            // Clear the adapter.
            mDiscoverAdapter.clear();

            // Check if an error occurred while loading data.
            Exception exception = data.getException();
            if(exception == null) {
                // Data load successful. Add the loaded data into adapter.
                mDiscoverAdapter.addAll(data.getData());

                // Move to appropriate index.
                if(mGridIndex != GridView.INVALID_POSITION) {
                    mGridView.setSelection(mGridIndex);
                }
            } else {
                // Data load failed. Display appropriate error message to user.
                mEmptyGridMessage.setText(Utility.getErrorMessage(getActivity(), exception));
            }
        }

        @Override
        public void onLoaderReset(Loader<DiscoverLoaderResult> loader) {
            // Loader was reset. Clear the adapter.
            mDiscoverAdapter.clear();
        }
    }

    /**
     * Loader callback handler for the favorite movies loader.
     */
    public final class FavoriteLoaderHandler
            implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // Sort order for the query.
            String sortOrder = MovieContract.MovieEntry.COLUMN_RELEASE_DATE + " DESC";

            // Create loader to retrieve favorite movies from database through content provider.
            return new CursorLoader(
                    getActivity(),
                    MovieContract.MovieEntry.CONTENT_URI,
                    FAVORITE_PROJECTION,
                    null,
                    null,
                    sortOrder);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // Check if sort order is favorites, if not, then ignore and return.
            if(!mSortOrderPreference.equals(getString(R.string.pref_sort_order_favorites))) {
                // Relinquish the cursor attached to the favorite adapter.
                mFavoriteDiscoverAdapter.swapCursor(null);
                return;
            }

            // Check if valid cursor was returned.
            if(data != null) {
                // Check if the cursor is empty.
                if(data.moveToFirst() == false) {
                    // Display appropriate status message to user and return.
                    mEmptyGridMessage.setText(getString(R.string.msg_err_no_favorites));
                    return;
                }

                // Set the favorite adapter onto the grid view.
                mGridView.setAdapter(mFavoriteDiscoverAdapter);

                // Assign the cursor to the favorite adapter.
                mFavoriteDiscoverAdapter.swapCursor(data);
            } else {
                // Display appropriate error message to user.
                mEmptyGridMessage.setText(getString(R.string.msg_err_fetch_favorites));
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // Relinquish the cursor attached to the favorite adapter.
            mFavoriteDiscoverAdapter.swapCursor(null);
        }
    }
}
