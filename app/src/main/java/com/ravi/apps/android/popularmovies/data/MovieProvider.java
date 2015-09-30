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

package com.ravi.apps.android.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.ravi.apps.android.popularmovies.R;
import com.ravi.apps.android.popularmovies.data.MovieContract.MovieEntry;
import com.ravi.apps.android.popularmovies.data.MovieContract.TrailerEntry;
import com.ravi.apps.android.popularmovies.data.MovieContract.ReviewEntry;

/**
 * Movie content provider.
 */
public class MovieProvider extends ContentProvider {

    // URI matcher.
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // Movie db helper class.
    private MovieDbHelper mMovieDbHelper;

    // Constants to match each of the URIs supported by this content provider.
    static final int MOVIE = 100;
    static final int MOVIE_WITH_TRAILERS_AND_REVIEWS = 101;
    static final int TRAILER = 200;
    static final int REVIEW = 300;

    // Query builder for join on movie, trailer and review tables.
    private static final SQLiteQueryBuilder sMovieWithTrailerAndReviewQueryBuilder;

    // Movie id selection string.
    private static final String sMovieIdSelection =
            MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID + " = ? ";

    // Static constructor.
    static {
        sMovieWithTrailerAndReviewQueryBuilder = new SQLiteQueryBuilder();

        // Inner join between movie, trailer and review tables on the movie id column.
        sMovieWithTrailerAndReviewQueryBuilder.setTables(
                MovieEntry.TABLE_NAME + " LEFT JOIN " + TrailerEntry.TABLE_NAME + " ON " +
                        MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID + " = " +
                        TrailerEntry.TABLE_NAME + "." + TrailerEntry.COLUMN_MOVIE_KEY +
                        " LEFT JOIN " + ReviewEntry.TABLE_NAME + " ON " +
                        MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID + " = " +
                        ReviewEntry.TABLE_NAME + "." + ReviewEntry.COLUMN_MOVIE_KEY );
    }

    // Builds and returns the uri matcher.
    private static UriMatcher buildUriMatcher() {
        // Instantiate the uri matcher.
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // Define the mapping from the URIs to the constants.
        uriMatcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        uriMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/*", MOVIE_WITH_TRAILERS_AND_REVIEWS);
        uriMatcher.addURI(authority, MovieContract.PATH_TRAILER, TRAILER);
        uriMatcher.addURI(authority, MovieContract.PATH_REVIEW, REVIEW);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        // Create and hold the movie db helper.
        mMovieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // Determine the type of URI passed in.
        final int uriMatch = sUriMatcher.match(uri);

        // Return the appropriate type.
        switch (uriMatch) {
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_WITH_TRAILERS_AND_REVIEWS:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case TRAILER:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            case REVIEW:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException(getContext()
                        .getString(R.string.msg_err_unknown_uri) + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Get a readable database.
        final SQLiteDatabase readDb = mMovieDbHelper.getReadableDatabase();

        // Get the match for the passed in URI.
        final int uriMatch = sUriMatcher.match(uri);

        // Create a cursor to hold the result of the database query.
        Cursor resultCursor;

        // Perform appropriate query.
        switch(uriMatch) {
            case MOVIE_WITH_TRAILERS_AND_REVIEWS: {
                // Extract the movie id from the uri.
                Integer movieId = MovieEntry.getMovieIdFromUri(uri);

                // Query the database.
                resultCursor = sMovieWithTrailerAndReviewQueryBuilder.query(
                        readDb,
                        projection,
                        sMovieIdSelection,
                        new String[]{movieId.toString()},
                        null,
                        null,
                        sortOrder);
                break;
            }
            case MOVIE: {
                // Query the database.
                resultCursor = readDb.query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case TRAILER: {
                // Query the database.
                resultCursor = readDb.query(
                        TrailerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case REVIEW: {
                // Query the database.
                resultCursor = readDb.query(
                        ReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default: {
                throw new UnsupportedOperationException(getContext()
                        .getString(R.string.msg_err_unknown_uri) + uri);
            }
        }

        // Set notification.
        resultCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return resultCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Get a writable database.
        final SQLiteDatabase writeDb = mMovieDbHelper.getWritableDatabase();

        // Get the uri match for the passed in URI.
        final int uriMatch = sUriMatcher.match(uri);

        // Uri to hold the result.
        Uri resultUri;

        switch (uriMatch) {
            case MOVIE: {
                // Insert values into movie table.
                long id = writeDb.insert(MovieEntry.TABLE_NAME, null, values);

                // Check if insert was successful.
                if (id > 0) {
                    resultUri = MovieEntry.buildMovieUri(id);
                } else {
                    // Close the db.
                    writeDb.close();

                    // Throw sql exception.
                    throw new android.database.SQLException(getContext()
                            .getString(R.string.err_insert_failed) + uri);
                }

                break;
            }
            case TRAILER: {
                // Insert values into trailer table.
                long id = writeDb.insert(TrailerEntry.TABLE_NAME, null, values);

                // Check if insert was successful.
                if (id > 0) {
                    resultUri = TrailerEntry.buildTrailerUri(id);
                } else {
                    // Close the db.
                    writeDb.close();

                    // Throw sql exception.
                    throw new android.database.SQLException(getContext()
                            .getString(R.string.err_insert_failed) + uri);
                }

                break;
            }
            case REVIEW: {
                // Insert values into review table.
                long id = writeDb.insert(ReviewEntry.TABLE_NAME, null, values);

                // Check if insert was successful.
                if (id > 0) {
                    resultUri = ReviewEntry.buildReviewUri(id);
                } else {
                    // Close the db.
                    writeDb.close();

                    // Throw sql exception.
                    throw new android.database.SQLException(getContext()
                            .getString(R.string.err_insert_failed) + uri);
                }

                break;
            }
            default: {
                // Close the db.
                writeDb.close();

                // Throw unsupported operation exception.
                throw new UnsupportedOperationException(getContext()
                        .getString(R.string.msg_err_unknown_uri) + uri);
            }
        }

        // Notify any observers of the change.
        getContext().getContentResolver().notifyChange(uri, null);

        // Close the db.
        writeDb.close();

        return resultUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        // Get a writable database.
        final SQLiteDatabase writeDb = mMovieDbHelper.getWritableDatabase();

        // Get the uri match for the passed in URI.
        final int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch) {
            case TRAILER: {
                // Begin the db transaction.
                writeDb.beginTransaction();

                // Count the insertions made.
                int insertCount = 0;
                try {
                    for(ContentValues value : values) {
                        // Insert values into trailer table.
                        long id = writeDb.insert(TrailerEntry.TABLE_NAME, null, value);

                        // Check if insert was successful and increment count.
                        if (id != -1) {
                            insertCount++;
                        }
                    }
                    // Transaction successful.
                    writeDb.setTransactionSuccessful();
                } finally {
                    // End the db transaction.
                    writeDb.endTransaction();
                }

                // Notify observers of change.
                getContext().getContentResolver().notifyChange(uri, null);

                // Close the db.
                writeDb.close();

                return insertCount;
            }
            case REVIEW: {
                // Begin the db transaction.
                writeDb.beginTransaction();

                // Count the insertions made.
                int insertCount = 0;
                try {
                    for(ContentValues value : values) {
                        // Insert values into review table.
                        long id = writeDb.insert(ReviewEntry.TABLE_NAME, null, value);

                        // Check if insert was successful and increment count.
                        if (id != -1) {
                            insertCount++;
                        }
                    }
                    // Transaction successful.
                    writeDb.setTransactionSuccessful();
                } finally {
                    // End the db transaction.
                    writeDb.endTransaction();
                }

                // Notify observers of change.
                getContext().getContentResolver().notifyChange(uri, null);

                // Close the db.
                writeDb.close();

                return insertCount;
            }
            default: {
                return super.bulkInsert(uri, values);
            }
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Get a writable database.
        final SQLiteDatabase writeDb = mMovieDbHelper.getWritableDatabase();

        // Get the uri match for the passed in URI.
        final int uriMatch = sUriMatcher.match(uri);

        // Number of rows updated.
        int rowsUpdated = 0;

        switch (uriMatch) {
            case MOVIE: {
                // Update values in movie table.
                rowsUpdated = writeDb.update(MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case TRAILER: {
                // Update values in trailer table.
                rowsUpdated = writeDb.update(TrailerEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case REVIEW: {
                // Update values in review table.
                rowsUpdated = writeDb.update(ReviewEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException(getContext()
                        .getString(R.string.msg_err_unknown_uri) + uri);
            }
        }

        // Notify any observers only if any updates were made.
        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        // Close the db.
        writeDb.close();

        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get a writable database.
        final SQLiteDatabase writeDb = mMovieDbHelper.getWritableDatabase();

        // Get the uri match for the passed in URI.
        final int uriMatch = sUriMatcher.match(uri);

        // Number of rows deleted.
        int rowsDeleted = 0;

        if(selection == null) selection = "1";

        switch (uriMatch) {
            case MOVIE: {
                // Delete from movie table.
                rowsDeleted = writeDb.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case TRAILER: {
                // Delete from trailer table.
                rowsDeleted = writeDb.delete(TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case REVIEW: {
                // Delete from review table.
                rowsDeleted = writeDb.delete(ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException(getContext()
                        .getString(R.string.msg_err_unknown_uri) + uri);
            }
        }

        // Notify any observers only if any deletions were made.
        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        // Close the db.
        writeDb.close();

        return rowsDeleted;
    }
}
