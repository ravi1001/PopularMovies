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
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Displays a grid of movie poster thumbnails retrieved from the TMDB server. It allows the
 * user to view details of any movie by clicking on it.
 */
public class DiscoverFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<DiscoverLoaderResult>, GridView.OnItemClickListener {

    // Loader that asynchronously fetches the discover movies data.
    private static final int LOADER_ID = 1;

    // Grid view that displays the movie poster thumbnails.
    private GridView mGridView;

    // Adapter holding the list of movie data.
    private DiscoverAdapter mMovieListingAdapter;

    // Current sort order preference.
    private String mSortOrderPreference;

    // Text view for displaying appropriate user message when the grid is blank.
    private TextView mEmptyGridMessage;

    public DiscoverFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover, container, false);

        // Create the movie data adapter.
        mMovieListingAdapter = new DiscoverAdapter(getActivity(), new ArrayList<DiscoverData>());

        // Get the grid view.
        mGridView = (GridView) rootView.findViewById(R.id.discover_gridview);

        // Set adapter for grid view.
        mGridView.setAdapter(mMovieListingAdapter);

        // Set item click event handler for grid view.
        mGridView.setOnItemClickListener(this);

        // Get the text view for empty grid view message and set the text.
        mEmptyGridMessage = (TextView) rootView.findViewById(R.id.status_gridview);
        mEmptyGridMessage.setText(getString(R.string.msg_status_loading));

        // Set the text view for empty grid view.
        mGridView.setEmptyView(mEmptyGridMessage);

        // Get the current sort order from shared preferences.
        mSortOrderPreference = Utility.getSortOrderPreference(getActivity());

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize and start the discover movies loader.
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if the sort order preference has changed.
        if(mSortOrderPreference != Utility.getSortOrderPreference(getActivity())) {
            // Update the sort order from the shared preferences.
            mSortOrderPreference = Utility.getSortOrderPreference(getActivity());

            // Explicitly calling onLoaderReset() here as the LoaderManager does not invoke it
            // even after restartLoader() call.
            onLoaderReset(null);

            // Restart the loader as the sort order setting has changed and fresh data needs
            // to be fetched from the remote server.
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public Loader<DiscoverLoaderResult> onCreateLoader(int id, Bundle args) {
        // Create and return the discover movie loader.
        return new DiscoverLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<DiscoverLoaderResult> loader, DiscoverLoaderResult data) {
        // Clear the adapter.
        mMovieListingAdapter.clear();

        // Check if an error occurred while loading data.
        Exception exception = data.getException();
        if(exception == null) {
            // Data load successful. Add the loaded data into adapter.
            mMovieListingAdapter.addAll(data.getData());
        } else {
            // Data load failed. Display appropriate error message to user.
            mEmptyGridMessage.setText(Utility.getErrorMessage(getActivity(), exception));
        }
    }

    @Override
    public void onLoaderReset(Loader<DiscoverLoaderResult> loader) {
        // Loader was reset. Clear the adapter.
        mMovieListingAdapter.clear();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Get the movie data corresponding to the movie clicked by user from adapter.
        DiscoverData discoverData = (DiscoverData) parent.getItemAtPosition(position);

        // Notify the activity that the user clicked a movie and pass on the movie details.
        ((Callback) getActivity()).onMovieSelected(discoverData);
    }

    /**
     * Public interface that needs to be implemented by the hosting activity to receive
     * notifications from fragment whenever the user selects/clicks on a movie.
     */
    public interface Callback {
        void onMovieSelected(DiscoverData discoverData);
    }
}
