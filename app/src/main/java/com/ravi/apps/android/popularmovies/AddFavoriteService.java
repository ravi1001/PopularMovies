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
import android.content.ContentValues;
import android.content.Intent;

import com.ravi.apps.android.popularmovies.data.MovieContract;

import java.util.List;

/**
 * Adds a favorite movie into the database through a content provider.
 */
public class AddFavoriteService extends IntentService {

    // Favorite movie to store in the database.
    private Movie mMovie;

    // Content resolver.
    private ContentResolver mContentResolver;

    public AddFavoriteService() {
        super("AddFavoriteService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Get the movie from the parcel.
        mMovie = intent.getParcelableExtra(DetailsFragment.FAVORITE_MOVIE);

        // Get the content resolver.
        mContentResolver = getContentResolver();

        // Insert movie, trailers and reviews into database.
        insertMovie();
        insertTrailers();
        insertReviews();
    }

    private void insertMovie() {
        // Create content values.
        ContentValues movieValues = new ContentValues();

        // Add movie details into content values.
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, mMovie.getId());
        movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, mMovie.getOriginalTitle());
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_IMAGE, mMovie.getPosterByteArray());
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDate());
        movieValues.put(MovieContract.MovieEntry.COLUMN_RUNTIME, mMovie.getRuntime());
        movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, mMovie.getVoteAverage());
        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, mMovie.getOverview());

        // Insert into database through content provider.
        mContentResolver.insert(MovieContract.MovieEntry.CONTENT_URI, movieValues);
    }

    private void insertTrailers() {
        // Get the list of trailers.
        List<Movie.Trailer> trailerList = mMovie.getTrailerList();

        // Check if there are any trailers.
        if(trailerList == null) {
            return;
        }

        // Get number of trailers in list.
        int numTrailers = trailerList.size();

        // Create content values array for bulk insert of trailers.
        ContentValues[] trailers = new ContentValues[numTrailers];

        // Extract each trailer and add into content values array.
        for(int i = 0; i < numTrailers; i++) {
            // Extract trailer from list.
            Movie.Trailer trailer = trailerList.get(i);

            // Create content values.
            ContentValues trailerValues = new ContentValues();

            // Add trailer details into content values.
            trailerValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_KEY, mMovie.getId());
            trailerValues.put(MovieContract.TrailerEntry.COLUMN_TRAILER_ID, trailer.getId());
            trailerValues.put(MovieContract.TrailerEntry.COLUMN_URI, trailer.getUrl());
            trailerValues.put(MovieContract.TrailerEntry.COLUMN_NAME, trailer.getName());

            // Add content values into array
            trailers[i] = trailerValues;
        }

        // Bulk insert trailers into database through content provider.
        mContentResolver.bulkInsert(MovieContract.TrailerEntry.CONTENT_URI, trailers);
    }

    private void insertReviews() {
        // Get the list of reviews.
        List<Movie.Review> reviewList = mMovie.getReviewList();

        // Check if there are any reviews.
        if(reviewList == null) {
            return;
        }

        // Get number of reviews in list.
        int numReviews = reviewList.size();

        // Create content values array for bulk insert of reviews.
        ContentValues[] reviews = new ContentValues[numReviews];

        // Extract each trailer and add into content values array.
        for(int i = 0; i < numReviews; i++) {
            // Extract review from list.
            Movie.Review review = reviewList.get(i);

            // Create content values.
            ContentValues reviewValues = new ContentValues();

            // Add review details into content values.
            reviewValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, mMovie.getId());
            reviewValues.put(MovieContract.ReviewEntry.COLUMN_REVIEW_ID, review.getId());
            reviewValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, review.getAuthor());
            reviewValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT, review.getContent());

            // Add content values into array
            reviews[i] = reviewValues;
        }

        // Bulk insert reviews into database through content provider.
        mContentResolver.bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, reviews);
    }
}
