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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines URIs, table and column names for the movie database.
 */
public class MovieContract {
    // Content authority for the content provider.
    public static final String CONTENT_AUTHORITY = "com.ravi.apps.android.popularmovies";

    // Base URI for the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Alternative paths that can be appended to the base content URI for framing the complete URI.
    public static final String PATH_MOVIE = "movie";
    public static final String PATH_TRAILER = "trailer";
    public static final String PATH_REVIEW = "review";

    /**
     * Defines the movie table contents.
     */
    public static final class MovieEntry implements BaseColumns {
        // Movie table name.
        public static final String TABLE_NAME = "movie";

        // Movie id, stored as int.
        public static final String COLUMN_MOVIE_ID = "movie_id";

        // Original title, stored as string.
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";

        // Poster image, stored as blob.
        public static final String COLUMN_POSTER_IMAGE = "poster_image";

        // Release date, stored as string.
        public static final String COLUMN_RELEASE_DATE = "release_date";

        // Runtime, stored as int.
        public static final String COLUMN_RUNTIME = "runtime";

        // Vote average, stored as real.
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";

        // Overview, stored as string.
        public static final String COLUMN_OVERVIEW = "overview";

        // Build the base movie URI.
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        // Directory content type.
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        // Item content type.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // Append the movie id to the URI.
        public static Uri appendMovieIdToUri(int movieId) {
            return CONTENT_URI.buildUpon().appendPath(((Integer) movieId).toString()).build();
        }

        // Extract the movie id from the URI.
        public static int getMovieIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }

    /**
     * Defines the trailer table contents.
     */
    public static final class TrailerEntry implements BaseColumns {
        // Trailer table name.
        public static final String TABLE_NAME = "trailer";

        // Foreign key into the movie table, stored as int.
        public static final String COLUMN_MOVIE_KEY = "movie_key";

        // URI for the YouTube trailer, stored as string.
        public static final String COLUMN_URI = "uri";

        // Name, stored as string.
        public static final String COLUMN_NAME = "name";

        // Build the base trailer URI.
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();

        // Directory content type.
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;

        // Item content type.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;

        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * Defines the review table contents.
     */
    public static final class ReviewEntry implements BaseColumns {
        // Review table name.
        public static final String TABLE_NAME = "review";

        // Foreign key into the movie table, stored as int.
        public static final String COLUMN_MOVIE_KEY = "movie_key";

        // Author, stored as string.
        public static final String COLUMN_AUTHOR = "author";

        // Content, stored as string.
        public static final String COLUMN_CONTENT = "content";

        // Build the base review URI.
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        // Directory content type.
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        // Item content type.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        public static Uri buildReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
