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
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
import android.widget.Toast;

import com.ravi.apps.android.popularmovies.data.MovieContract.MovieEntry;
import com.ravi.apps.android.popularmovies.data.MovieContract.ReviewEntry;
import com.ravi.apps.android.popularmovies.data.MovieContract.TrailerEntry;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Displays detailed information about the movie.
 */
public class DetailsFragment extends Fragment
        implements AdapterView.OnItemClickListener, View.OnClickListener {

    // Tag for logging messages.
    public final String LOG_TAG = DetailsFragment.class.getSimpleName();

    // Key used to get the movie data parcelable from bundle.
    public static final String DISCOVER_MOVIE = "Discover Movie";

    // Key used to pass the favorite movie data to the intent service.
    public static final String FAVORITE_MOVIE = "Favorite Movie";

    // Loader to fetch movie details from the TMDB server.
    private static final int LOADER_DETAILS_ID = 1;

    // Loader to fetch favorite movie details from the database through the content provider.
    private static final int LOADER_FAVORITE_ID = 2;

    // Details loader callback handler.
    DetailsLoaderHandler mDetailsLoaderHandler;

    // Favorite loader callback handler.
    FavoriteLoaderHandler mFavoriteLoaderHandler;

    // Projection for favorite cursor loader.
    public static final String[] FAVORITE_DETAILS_PROJECTION = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieEntry.COLUMN_POSTER_IMAGE,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_RUNTIME,
            MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieEntry.COLUMN_OVERVIEW,
            TrailerEntry.COLUMN_TRAILER_ID,
            TrailerEntry.COLUMN_URI,
            TrailerEntry.COLUMN_NAME,
            ReviewEntry.COLUMN_REVIEW_ID,
            ReviewEntry.COLUMN_AUTHOR,
            ReviewEntry.COLUMN_CONTENT
    };

    // Column indices tied to the favorite cursor loader projection.
    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_ORIGINAL_TITLE = 2;
    public static final int COL_POSTER_IMAGE = 3;
    public static final int COL_RELEASE_DATE = 4;
    public static final int COL_RUNTIME = 5;
    public static final int COL_VOTE_AVERAGE = 6;
    public static final int COL_OVERVIEW = 7;
    public static final int COL_TRAILER_ID = 8;
    public static final int COL_URI = 9;
    public static final int COL_NAME = 10;
    public static final int COL_REVIEW_ID = 11;
    public static final int COL_AUTHOR = 12;
    public static final int COL_CONTENT = 13;

    // Holds the movie ID passed in to the fragment.
    private int mMovieId;

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
        setHasOptionsMenu(true);
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

        // Set the mark as favorite button click listener.
        mMarkFavoriteView.setOnClickListener(this);

        // Set the empty list view messages.
        mTrailersView.setEmptyView(mTrailersEmpty);
        mReviewsView.setEmptyView(mReviewsEmpty);

        // Get the current sort order from shared preferences.
        mSortOrderPreference = Utility.getSortOrderPreference(getActivity());

        // Disable mark as favorite button if sort order preference is favorites.
        if(mSortOrderPreference.equals(getString(R.string.pref_sort_order_favorites))) {
            mMarkFavoriteView.setClickable(false);
        }

        // Hide all views except load status till data is loaded.
        hideViews();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.e(LOG_TAG, "onActivityCreated");

        // Restore the saved sort order preference.
        if(savedInstanceState != null
                && savedInstanceState.containsKey(getString(R.string.pref_sort_order_key))) {
            mSortOrderPreference =
                    savedInstanceState.getString(getString(R.string.pref_sort_order_key));
            Log.e(LOG_TAG, "onActivityCreated: orientation change");

        }

        // Determine the loader to start based on sort order preference.
        if(mSortOrderPreference.equals(getString(R.string.pref_sort_order_favorites))) {

            Log.e(LOG_TAG, "onActivityCreated: start fav loader");

            // Create the favorite loader callback handler.
            mFavoriteLoaderHandler = new FavoriteLoaderHandler();

            // Initialize and start the favorite movie details loader.
            getLoaderManager().initLoader(LOADER_FAVORITE_ID, null, mFavoriteLoaderHandler);
        } else {

            Log.e(LOG_TAG, "onActivityCreated: start details loader");

            // Create the details loader callback handler.
            mDetailsLoaderHandler = new DetailsLoaderHandler();

            // Initialize and start the movie details loader.
            getLoaderManager().initLoader(LOADER_DETAILS_ID, null, mDetailsLoaderHandler);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if the sort order preference has changed.
        if(!mSortOrderPreference.equals(Utility.getSortOrderPreference(getActivity()))) {
            Log.e(LOG_TAG, "onStart: sort order changed");

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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Create implicit intent to play trailer.
        Intent trailerIntent = new Intent();

        // Set the intent action and data.
        trailerIntent.setAction(Intent.ACTION_VIEW)
                .setData(Uri.parse(mTrailersAdapter.getItem(position).getUrl()));

        // Pass intent to play trailer.
        startActivity(trailerIntent);
    }

    @Override
    public void onClick(View v) {
        // Confirm it's button click event.
        if(v instanceof Button) {

            // Disallow further clicks and disable button.
            mMarkFavoriteView.setClickable(false);

            Toast.makeText(getActivity(), "Adding favorite movie into database...", Toast.LENGTH_SHORT).show();

            // Create intent to add movie into database.
            Intent intent = new Intent(getActivity(), AddFavoriteService.class);
            intent.putExtra(FAVORITE_MOVIE, mMovie);

            // Send intent to start add favorite intent service.
            getActivity().startService(intent);
        }
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
        mPosterView.setScaleType(ImageView.ScaleType.FIT_XY);
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

        // Check for errors or invalid poster image data based on sort order.
        if(mSortOrderPreference.equals(getString(R.string.pref_sort_order_favorites))) {
            if(mMovie.getPosterByteArray() != null && mMovie.getPosterByteArray().length != 0) {
                // Get the poster image bitmap from byte array.
                Bitmap posterBitmap = BitmapFactory.decodeByteArray(
                        mMovie.getPosterByteArray(), 0, mMovie.getPosterByteArray().length);

                // Set the poster image bitmap into the image view.
                mPosterView.setImageBitmap(posterBitmap);
            }
        } else {
            // Check for errors or invalid poster path data.
            if(mMovie.getPosterPath() != null
                    && !mMovie.getPosterPath().isEmpty()
                    && !mMovie.getPosterPath().equals("null")) {
                // Set data for poster image view.
                bindDataToView(mPosterView);
            }
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
        if(mMovie.getTrailerList() != null && mMovie.getTrailerList().size() != 0) {
            // Add data into adapter for trailers list view.
            bindDataToView(mTrailersView);
        } else {
            // Set error message.
//            bindErrorTextToView(mTrailersView);
        }

        // Check for errors or invalid reviews data.
        if(mMovie.getReviewList() != null && mMovie.getReviewList().size() != 0) {
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
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            // Get byte array from poster bitmap.
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                            // Set the poster byte array in the movie object.
                            mMovie.setPosterByteArray(stream.toByteArray());

                            // Set poster bitmap onto the image view.
                            mPosterView.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
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

    /**
     * Loader callback handler for movie details loader, fetching data from TMDB server.
     */
    public final class DetailsLoaderHandler
            implements LoaderManager.LoaderCallbacks<DetailsLoaderResult> {

        @Override
        public Loader<DetailsLoaderResult> onCreateLoader(int id, Bundle args) {
            Log.e(LOG_TAG, "onCreateLoader: Details loader");

            // Create and return the movie details loader.
            return new DetailsLoader(getActivity(), mMovieId);
        }

        @Override
        public void onLoadFinished(Loader<DetailsLoaderResult> loader, DetailsLoaderResult data) {
            Log.e(LOG_TAG, "onLoadFinished: Details loader");

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
            Log.e(LOG_TAG, "onLoaderReset: details loader");

            // Clear the adapters.
            mReviewsAdapter.clear();
            mTrailersAdapter.clear();
        }
    }

    /**
     * Loader callback handler for favorite movie details loader, fetching data from database.
     */
    public final class FavoriteLoaderHandler
            implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.e(LOG_TAG, "onCreateLoader: favorite loader");

            // Get uri after appending movie id.
            Uri movieWithTrailersAndReviewsUri = MovieEntry.appendMovieIdToUri(mMovieId);

            // Create loader to retrieve favorite movie details from database through content provider.
            return new CursorLoader(
                    getActivity(),
                    movieWithTrailersAndReviewsUri,
                    FAVORITE_DETAILS_PROJECTION,
                    null,
                    null,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.e(LOG_TAG, "onLoadFinished: favorite loader");

            // Clear the adapters.
            mTrailersAdapter.clear();
            mReviewsAdapter.clear();

            // Check if valid cursor was returned.
            if(data != null) {
                // Check if the cursor is empty.
                if(data.moveToFirst() == false) {
                    // Display appropriate status message to user and return.
                    mLoadStatusView.setText(getString(R.string.msg_err_no_favorites));
                    return;
                }

                // Extract and hold the detailed movie data.
                int movieId = data.getInt(COL_MOVIE_ID);
                String title = data.getString(COL_ORIGINAL_TITLE);
                byte[] posterByteArray = data.getBlob(COL_POSTER_IMAGE);
                String releaseDate = data.getString(COL_RELEASE_DATE);
                int runtime = data.getInt(COL_RUNTIME);
                double rating = data.getDouble(COL_VOTE_AVERAGE);
                String overview = data.getString(COL_OVERVIEW);

                // Store trailers into a list.
                List<Movie.Trailer> trailerList = new ArrayList<>();

                // Temporarily hold the unique trailer ids.
                Set<String> uniqueTrailerId = new HashSet<>();

                // Iterate through the cursor and extract all the trailers.
                do {
                    // Extract trailer id.
                    String trailerId = data.getString(COL_TRAILER_ID);

                    if(trailerId == null)
                        continue;

                    // Check if it's already added.
                    if(!uniqueTrailerId.add(trailerId)) {
                        // This trailer id has already been extracted, move to next row.
                        continue;
                    }

                    // Extract other trailer data.
                    String uri = data.getString(COL_URI);
                    String name = data.getString(COL_NAME);

                    // Add trailer object to list.
                    trailerList.add(new Movie.Trailer(trailerId, uri, name));
                } while(data.moveToNext());

                // Store reviews into a list.
                List<Movie.Review> reviewList = new ArrayList<>();

                // Temporarily hold the unique trailer ids.
                Set<String> uniqueReviewId = new HashSet<>();

                // Move to first row.
                data.moveToFirst();

                // Iterate through the cursor and extract all the reviews.
                do {
                    // Extract review id.
                    String reviewId = data.getString(COL_REVIEW_ID);

                    if(reviewId == null)
                        continue;

                    // Check if it's unique.
                    if(!uniqueReviewId.add(reviewId)) {
                        // This trailer id has already been extracted, move to next row.
                        continue;
                    }

                    // Extract other review data.
                    String author = data.getString(COL_AUTHOR);
                    String content = data.getString(COL_CONTENT);

                    // Add review object to list.
                    reviewList.add(new Movie.Review(reviewId, author, content));
                } while(data.moveToNext());

                // Create movie object with extracted data.
                mMovie = new Movie(movieId, title, null, posterByteArray, releaseDate,
                        runtime, rating, overview, trailerList, reviewList);

                // Remove the load status text view.
                mLoadStatusView.setVisibility(View.GONE);

                // Unhide all the remaining views.
                unhideViews();

                // Validate and set the data onto all the views.
                setViews();
            } else {
                // Display appropriate error message to user.
                mLoadStatusView.setText(getString(R.string.msg_err_fetch_favorites));
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            Log.e(LOG_TAG, "onLoaderReset: favorite loader");

            // Clear the adapters.
            mTrailersAdapter.clear();
            mReviewsAdapter.clear();
        }
    }
}
