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
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Displays detailed information about the movie.
 */
public class DetailsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<DetailsLoaderResult>,
        AdapterView.OnItemClickListener {

    // Tag for logging messages.
    public final String LOG_TAG = DetailsFragment.class.getSimpleName();

    // Key used to get the movie data parcelable from bundle.
    public static final String DISCOVER_MOVIE = "Discover Movie";

    // Loader that asynchronously fetches the movie details data.
    private static final int LOADER_ID = 1;

    // Holds the movie ID passed in to the fragment.
    private long mMovieId;

    // Detailed data for the movie.
    private Movie mMovie;

    // Trailers and reviews adapters.
    private TrailersAdapter mTrailersAdapter;
    private ReviewsAdapter mReviewsAdapter;

    // Current sort order preference.
    private String mSortOrderPreference;

    // References to the views being displayed.
    private TextView mOriginalTitleView;
    private ImageView mPosterView;
    private TextView mReleaseDateView;
    private TextView mRuntimeView;
    private TextView mVoteAverageView;
    private Button mMarkFavoriteView;
    private TextView mOverviewView;
    private TextView mTrailersLabelView;
    private TextView mTrailersEmpty;
    private ListView mTrailersView;
    private TextView mReviewsLabelView;
    private TextView mReviewsEmpty;
    private ListView mReviewsView;

    // Reference to the load status text view.
    private TextView mLoadStatusView;

    ViewGroup mViewGroup;

    public DetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout.
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        // Get the arguments set for this fragment.
        Bundle arguments = getArguments();
        if(arguments != null) {
            // Extract the movie id.
            mMovieId = ((Movie) arguments.getParcelable(DISCOVER_MOVIE)).getId();
        }

        // Set references for all the views.
        setReferencesToViews(rootView);

        // Create the trailers and reviews adapters.
        mTrailersAdapter = new TrailersAdapter(getActivity(), new ArrayList<Movie.Trailer>());
        mReviewsAdapter = new ReviewsAdapter(getActivity(), new ArrayList<Movie.Review>());

        // Set the adapters to respective list views.
        mTrailersView.setAdapter(mTrailersAdapter);
        mReviewsView.setAdapter(mReviewsAdapter);

        // Set the trailer list view item click listener.
        mTrailersView.setOnItemClickListener(this);

        // Set the empty list view messages.
        mTrailersView.setEmptyView(mTrailersEmpty);
        mReviewsView.setEmptyView(mReviewsEmpty);

        // Get the current sort order from shared preferences.
        mSortOrderPreference = Utility.getSortOrderPreference(getActivity());

        // Hide all views except load status till data is loaded.
        hideViews();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize and start the movie details loader.
        getLoaderManager().initLoader(LOADER_ID, null, this);

        // Restore the saved sort order preference.
        if(savedInstanceState != null
                && savedInstanceState.containsKey(getString(R.string.pref_sort_order_key))) {
           mSortOrderPreference =
                   savedInstanceState.getString(getString(R.string.pref_sort_order_key));
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if the sort order preference has changed.
        if(mSortOrderPreference != Utility.getSortOrderPreference(getActivity())) {
            // Update the sort order preference flag.
            mSortOrderPreference = Utility.getSortOrderPreference(getActivity());

            // Notify the hosting activity of the sort order change event.
            ((OnSortPreferenceChangedListener) getActivity()).onSortPreferenceChanged(this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save the current sort order preference.
        outState.putString(getString(R.string.pref_sort_order_key), mSortOrderPreference);

        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<DetailsLoaderResult> onCreateLoader(int id, Bundle args) {
        Log.e(LOG_TAG, "onCreateLoader()");

        // Create and return the movie details loader.
        return new DetailsLoader(getActivity(), mMovieId);
    }

    @Override
    public void onLoadFinished(Loader<DetailsLoaderResult> loader, DetailsLoaderResult data) {
        Log.e(LOG_TAG, "onLoadFinished()");

        // Clear the adapters.
        mTrailersAdapter.clear();
        mReviewsAdapter.clear();

        // Check if an error occurred while loading data.
        Exception exception = data.getException();

        // Data load successful.
        if(exception == null) {
            // Extract and hold the detailed movie data.
            mMovie = data.getData();

            // Remove the load status text view.
            mLoadStatusView.setVisibility(View.GONE);

            // Unhide all the remaining views.
            unhideViews();

            // Validate and set the data onto all the views.
            setViews();

        } else {
            // Data load failed. Display appropriate error message to user.
            mLoadStatusView.setText(Utility.getErrorMessage(getActivity(), exception));
        }
    }

    @Override
    public void onLoaderReset(Loader<DetailsLoaderResult> loader) {
        Log.e(LOG_TAG, "onLoaderReset()");

        // Clear the adapters.
        mReviewsAdapter.clear();
        mTrailersAdapter.clear();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Create implicit intent to play trailer.
        Intent trailerIntent = new Intent();

        // Set the intent action and data.
        trailerIntent.setAction(Intent.ACTION_VIEW)
                .setData(Uri.parse(mTrailersAdapter.getItem(position).getKey()));

        // Pass intent to play trailer.
        startActivity(trailerIntent);
    }

    /**
     * Public interface that needs to be implemented by the hosting activity to receive
     * notifications from the fragment whenever the user changes the sort order preference.
     */
    public interface OnSortPreferenceChangedListener {
        void onSortPreferenceChanged(DetailsFragment detailsFragment);
    }

    /**
     * Sets the references to all the views.
     */
    private void setReferencesToViews(View rootView) {
        mLoadStatusView = (TextView) rootView.findViewById(R.id.load_status_textview);
        mOriginalTitleView = (TextView) rootView.findViewById(R.id.original_title_textview);
        mPosterView = (ImageView) rootView.findViewById(R.id.poster_imageview);
        mReleaseDateView = (TextView) rootView.findViewById(R.id.release_date_textview);
        mRuntimeView = (TextView) rootView.findViewById(R.id.runtime_textview);
        mVoteAverageView = (TextView) rootView.findViewById(R.id.vote_average_textview);
        mMarkFavoriteView = (Button) rootView.findViewById(R.id.mark_favorite_button);
        mOverviewView = (TextView) rootView.findViewById(R.id.overview_textview);
        mTrailersLabelView = (TextView) rootView.findViewById(R.id.trailers_label_textview);
        mTrailersEmpty = (TextView) rootView.findViewById(R.id.trailers_empty_textview);
        mTrailersView = (ListView) rootView.findViewById(R.id.trailers_listview);
        mReviewsLabelView = (TextView) rootView.findViewById(R.id.reviews_label_textview);
        mReviewsEmpty = (TextView) rootView.findViewById(R.id.reviews_empty_textview);
        mReviewsView = (ListView) rootView.findViewById(R.id.reviews_listview);
    }

    /**
     * Hides all the views except load status.
     */
    private void hideViews() {
        mOriginalTitleView.setVisibility(View.INVISIBLE);
        mPosterView.setVisibility(View.INVISIBLE);
        mReleaseDateView.setVisibility(View.INVISIBLE);
        mRuntimeView.setVisibility(View.INVISIBLE);
        mVoteAverageView.setVisibility(View.INVISIBLE);
        mMarkFavoriteView.setVisibility(View.INVISIBLE);
        mOverviewView.setVisibility(View.INVISIBLE);
        mTrailersLabelView.setVisibility(View.INVISIBLE);
        mTrailersView.setVisibility(View.INVISIBLE);
        mReviewsLabelView.setVisibility(View.INVISIBLE);
        mReviewsView.setVisibility(View.INVISIBLE);
    }

    /**
     * Unhides all the views except load status.
     */
    private void unhideViews() {
        mOriginalTitleView.setVisibility(View.VISIBLE);
        mPosterView.setVisibility(View.VISIBLE);
        mReleaseDateView.setVisibility(View.VISIBLE);
        mRuntimeView.setVisibility(View.VISIBLE);
        mVoteAverageView.setVisibility(View.VISIBLE);
        mMarkFavoriteView.setVisibility(View.VISIBLE);
        mOverviewView.setVisibility(View.VISIBLE);
        mTrailersLabelView.setVisibility(View.VISIBLE);
        mTrailersView.setVisibility(View.VISIBLE);
        mReviewsLabelView.setVisibility(View.VISIBLE);
        mReviewsView.setVisibility(View.VISIBLE);
    }

    /**
     * Validates and sets the appropriate movie data onto all the views.
     */
    private void setViews() {
        // Check for errors or invalid original title data.
        if(mMovie.getOriginalTitle() != null
                && !mMovie.getOriginalTitle().isEmpty()
                && !mMovie.getOriginalTitle().equals("null")) {
            // Set data for original title view.
            bindDataToView(mOriginalTitleView);
        } else {
            // Set error message.
            bindErrorTextToView(mOriginalTitleView);
        }

        // Check for errors or invalid poster path data.
        if(mMovie.getPosterPath() != null
                && !mMovie.getPosterPath().isEmpty()
                && !mMovie.getPosterPath().equals("null")) {
            // Set data for poster image view.
            bindDataToView(mPosterView);
        }

        // Check for errors or invalid release date data.
        if(mMovie.getReleaseDate() != null
                && !mMovie.getReleaseDate().isEmpty()
                && !mMovie.getReleaseDate().equals("null")) {
            // Set data for release date view.
            bindDataToView(mReleaseDateView);
        } else {
            // Set error message.
            bindErrorTextToView(mReleaseDateView);
        }

        // Check for errors or invalid runtime data.
        if(mMovie.getRuntime() != 0) {
            // Set data for runtime view.
            bindDataToView(mRuntimeView);
        } else {
            // Set error message.
            bindErrorTextToView(mRuntimeView);
        }

        // Set data for average rating view.
        bindDataToView(mVoteAverageView);

        // Check for errors or invalid overview data.
        if(mMovie.getOverview() != null
                && !mMovie.getOverview().isEmpty()
                && !mMovie.getOverview().equals("null")) {
            // Set data for plot synopsis view.
            bindDataToView(mOverviewView);
        } else {
            // Set error message.
            bindErrorTextToView(mOverviewView);
        }

        // Check for errors or invalid trailer data.
        if(mMovie.getTrailerList() != null) {
            // Add data into adapter for trailers list view.
            bindDataToView(mTrailersView);
        } else {
            // Set error message.
//            bindErrorTextToView(mTrailersView);
        }

        // Check for errors or invalid reviews data.
        if(mMovie.getReviewList() != null) {
            // Add data into adapter for reviews list view.
            bindDataToView(mReviewsView);
        } else {
            // Set error message.
//            bindErrorTextToView(mReviewsView);
        }
    }

    /**
     * Binds the appropriate movie data onto the view passed in.
     */
    private void bindDataToView(View view) {
        // Check which view was passed in.
        if(view == mOriginalTitleView) {
            // Bind the original title.
            mOriginalTitleView.setText(mMovie.getOriginalTitle());
        } else if(view == mPosterView) {
            // Load the poster image using Picasso.
            Picasso.with(getActivity())
                    .load(mMovie.getPosterPath())
                    .fit()
                    .into(mPosterView);
        } else if(view == mReleaseDateView) {
            // Bind the release date.
            mReleaseDateView.setText(mMovie.getReleaseDate().substring(0, 4));
        } else if(view == mRuntimeView) {
            // Bind the runtime.
            mRuntimeView.setText(((Integer) mMovie.getRuntime()).toString()
                    + getText(R.string.runtime_units));
        } else if(view == mVoteAverageView) {
            // Bind the average rating.
            mVoteAverageView.setText(((Double) mMovie.getVoteAverage()).toString()
                    + getText(R.string.rating_denominator));
        } else if(view == mOverviewView) {
            // Bind the plot synopsis.
            mOverviewView.setText(mMovie.getOverview());
        } else if(view == mTrailersView) {
            // Add the trailer list to adapter.
            mTrailersAdapter.addAll(mMovie.getTrailerList());
        } else if(view == mReviewsView) {
            // Add the trailer list to adapter.
            mReviewsAdapter.addAll(mMovie.getReviewList());
        }
    }

    /**
     * Binds the appropriate error text onto the view passed in.
     */
    private void bindErrorTextToView(TextView textView) {
        // Set appropriate error text depending on the view.
        if(textView == mOriginalTitleView) {
            mOriginalTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            mOriginalTitleView.setText(getText(R.string.msg_err_title_unavailable));
        } else if(textView == mReleaseDateView) {
            mReleaseDateView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            mReleaseDateView.setText(getText(R.string.msg_err_release_date_unavailable));
        } else if(textView == mRuntimeView) {
            mRuntimeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            mRuntimeView.setText(R.string.msg_err_runtime_unavailable);
        } else if(textView == mOverviewView) {
            mOverviewView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            mOverviewView.setText(getText(R.string.msg_err_overview_unavailable));
        }
    }
}
