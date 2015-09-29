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

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.util.Log;

import com.ravi.apps.android.popularmovies.data.MovieContract;

import java.util.List;

/**
 * Adds a favorite movie into the database through a content provider.
 */
public class DeleteFavoriteService extends IntentService {

    public final String LOG_TAG = DeleteFavoriteService.class.getSimpleName();

    // Favorite movie to delete from the database.
    private Movie mMovie;

    // Content resolver.
    private ContentResolver mContentResolver;

    public DeleteFavoriteService() {
        super("DeleteFavoriteService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(LOG_TAG, "onHandleIntent");

        // Get the movie from the parcel.
        mMovie = intent.getParcelableExtra(DetailsFragment.FAVORITE_MOVIE);

        // Get the content resolver.
        mContentResolver = getContentResolver();

        // Insert movie, trailers and reviews into database.
        deleteMovie();
        deleteTrailers();
        deleteReviews();
    }

    private void deleteMovie() {
        Log.e(LOG_TAG, "deleteMovie");

        String selection = MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";
        String[] selectionArgs = {((Integer) mMovie.getId()).toString()};

        int r = mContentResolver.delete(MovieContract.MovieEntry.CONTENT_URI, selection, selectionArgs);


        Log.e(LOG_TAG, "no. of movies deleted: " + r);
    }

    private void deleteTrailers() {
        Log.e(LOG_TAG, "deleteTrailers");


        // Get the list of trailers.
        List<Movie.Trailer> trailerList = mMovie.getTrailerList();

        // Check if there are any trailers.
        if(trailerList == null) {
            return;
        }

        String selection = MovieContract.TrailerEntry.COLUMN_MOVIE_KEY + " = ? ";
        String[] selectionArgs = {((Integer) mMovie.getId()).toString()};

        int r = mContentResolver.delete(MovieContract.TrailerEntry.CONTENT_URI, selection, selectionArgs);

        Log.e(LOG_TAG, "no. of trailers deleted: " + r);
    }

    private void deleteReviews() {

        Log.e(LOG_TAG, "deleteReviews");


        // Get the list of reviews.
        List<Movie.Review> reviewList = mMovie.getReviewList();

        // Check if there are any reviews.
        if(reviewList == null) {
            return;
        }

        String selection = MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + " = ? ";
        String[] selectionArgs = {((Integer) mMovie.getId()).toString()};

        int r = mContentResolver.delete(MovieContract.ReviewEntry.CONTENT_URI, selection, selectionArgs);

        Log.e(LOG_TAG, "no. of reviews deleted: " + r);

    }

}
