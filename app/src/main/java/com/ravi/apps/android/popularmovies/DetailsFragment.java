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
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Displays detailed information about the movie.
 */
public class DetailsFragment extends Fragment {

    // Key used to get the movie data parcelable from bundle.
    public static final String DISCOVER_MOVIE = "Discover Movie";

    // References to the views being displayed.
    private Movie mMovie;
    private TextView mOriginalTitleView;
    private ImageView mPosterView;
    private TextView mReleaseDateView;
    private TextView mVoteAverageView;
    private TextView mOverviewView;

    // Current sort order preference.
    private String mSortOrderPreference;

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
            mMovie = arguments.getParcelable(DISCOVER_MOVIE);
        }

        // Get references for the views.
        mOriginalTitleView = (TextView) rootView.findViewById(R.id.original_title_textview);
        mPosterView = (ImageView) rootView.findViewById(R.id.poster_imageview);
        mReleaseDateView = (TextView) rootView.findViewById(R.id.release_date_textview);
        mVoteAverageView = (TextView) rootView.findViewById(R.id.vote_average_textview);
        mOverviewView = (TextView) rootView.findViewById(R.id.overview_textview);

        // Validate and set the data onto all the views.
        setViews();

        // Get the current sort order from shared preferences.
        mSortOrderPreference = Utility.getSortOrderPreference(getActivity());

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save the current sort order preference.
        outState.putString(getString(R.string.pref_sort_order_key), mSortOrderPreference);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

    /**
     * Public interface that needs to be implemented by the hosting activity to receive
     * notifications from the fragment whenever the user changes the sort order preference.
     */
    public interface OnSortPreferenceChangedListener {
        void onSortPreferenceChanged(DetailsFragment detailsFragment);
    }

    /*
     * Validates and sets the appropriate movie data onto all the views.
     */
    private void setViews() {
        // Check for errors or invalid data.
        if(mMovie.getOriginalTitle() != null
                && !mMovie.getOriginalTitle().isEmpty()
                && !mMovie.getOriginalTitle().equals("null")) {
            // Set data for original title view.
            bindDataToView(mOriginalTitleView);
        } else {
            // Set error message.
            bindErrorTextToView(mOriginalTitleView);
        }

        // Check for errors or invalid data.
        if(mMovie.getPosterPath() != null
                && !mMovie.getPosterPath().isEmpty()
                && !mMovie.getPosterPath().equals("null")) {
            // Set data for poster image view.
            bindDataToView(mPosterView);
        }

        // Check for errors or invalid data.
        if(mMovie.getReleaseDate() != null
                && !mMovie.getReleaseDate().isEmpty()
                && !mMovie.getReleaseDate().equals("null")) {
            // Set data for release date view.
            bindDataToView(mReleaseDateView);
        } else {
            // Set error message.
            bindErrorTextToView(mReleaseDateView);
        }

        // Set data for average rating view.
        bindDataToView(mVoteAverageView);

        // Check for errors or invalid data.
        if(mMovie.getOverview() != null
                && !mMovie.getOverview().isEmpty()
                && !mMovie.getOverview().equals("null")) {
            // Set data for plot synopsis view.
            bindDataToView(mOverviewView);
        } else {
            // Set error message.
            bindErrorTextToView(mOverviewView);
        }
    }

    /*
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
                    .into(mPosterView);
        } else if(view == mReleaseDateView) {
            // Bind the release date.
            mReleaseDateView.setText(mMovie.getReleaseDate().substring(0, 4));
        } else if(view == mVoteAverageView) {
            // Bind the average rating.
            mVoteAverageView.setText(((Double) mMovie.getVoteAverage()).toString() + "/10");
        } else if(view == mOverviewView) {
            // bind the plot synopsis.
            mOverviewView.setText(mMovie.getOverview());
        }
    }

    /*
     * Binds the appropriate error text onto the view passed in.
     */
    private void bindErrorTextToView(TextView textView) {
        // Set the text size.
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        // Set appropriate error text depending on the view.
        if(textView == mOriginalTitleView) {
            mOriginalTitleView.setText(getText(R.string.msg_err_title_unavailable));
        } else if(textView == mReleaseDateView) {
            mReleaseDateView.setText(getText(R.string.msg_err_release_date_unavailable));
        } else if(textView == mOverviewView) {
            mOverviewView.setText(getText(R.string.msg_err_overview_unavailable));
        }
    }
}
