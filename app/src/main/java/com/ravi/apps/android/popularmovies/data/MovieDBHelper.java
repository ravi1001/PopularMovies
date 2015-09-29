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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ravi.apps.android.popularmovies.data.MovieContract.MovieEntry;
import com.ravi.apps.android.popularmovies.data.MovieContract.ReviewEntry;
import com.ravi.apps.android.popularmovies.data.MovieContract.TrailerEntry;

/**
 * Creates, upgrades and deletes the local movie database.
 */
public class MovieDbHelper extends SQLiteOpenHelper {
    // Database schema version.
    public static final int DATABASE_VERSION = 2;

    // Database name.
    public static final String DATABASE_NAME = "movie.db";

    // SQL statement for creating the movie table.
    private static final String SQL_CREATE_MOVIE_TABLE =
            "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
            MovieEntry._ID + " INTEGER PRIMARY KEY, " +
            MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
            MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT, " +
            MovieEntry.COLUMN_POSTER_IMAGE + " BLOB, " +
            MovieEntry.COLUMN_RELEASE_DATE + " TEXT, " +
            MovieEntry.COLUMN_RUNTIME + " INTEGER, " +
            MovieEntry.COLUMN_VOTE_AVERAGE + " REAL, " +
            MovieEntry.COLUMN_OVERVIEW + " TEXT, " +
            "UNIQUE (" + MovieEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

    // SQL statement for creating the trailer table.
    private static final String SQL_CREATE_TRAILER_TABLE =
            "CREATE TABLE " + TrailerEntry.TABLE_NAME + " (" +
            TrailerEntry._ID + " INTEGER PRIMARY KEY, " +
            TrailerEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
            TrailerEntry.COLUMN_TRAILER_ID + " TEXT NOT NULL, " +
            TrailerEntry.COLUMN_URI + " TEXT, " +
            TrailerEntry.COLUMN_NAME + " TEXT, " +
            " FOREIGN KEY (" + TrailerEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
            MovieEntry.TABLE_NAME + " (" + MovieEntry.COLUMN_MOVIE_ID + ") " +
            "UNIQUE (" + TrailerEntry.COLUMN_TRAILER_ID + ") ON CONFLICT REPLACE);";


    // SQL statement for creating the review table.
    private static final String SQL_CREATE_REVIEW_TABLE =
            "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
            ReviewEntry._ID + " INTEGER PRIMARY KEY, " +
            ReviewEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
            ReviewEntry.COLUMN_REVIEW_ID + " TEXT NOT NULL, " +
            ReviewEntry.COLUMN_AUTHOR + " TEXT, " +
            ReviewEntry.COLUMN_CONTENT + " TEXT, " +
            " FOREIGN KEY (" + ReviewEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
            MovieEntry.TABLE_NAME + " (" + MovieEntry.COLUMN_MOVIE_ID + ") " +
            "UNIQUE (" + ReviewEntry.COLUMN_REVIEW_ID + ") ON CONFLICT REPLACE);";


    // SQL statement for deleting the movie table.
    private static final String SQL_DELETE_MOVIE_TABLE =
            "DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME;

    // SQL statement for deleting the trailer table.
    private static final String SQL_DELETE_TRAILER_TABLE =
            "DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME;

    // SQL statement for deleting the review table.
    private static final String SQL_DELETE_REVIEW_TABLE =
            "DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the movie, trailer and review tables.
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_TRAILER_TABLE);
        db.execSQL(SQL_CREATE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Delete the data on upgrade as it's a cache for web data.
        db.execSQL(SQL_DELETE_MOVIE_TABLE);
        db.execSQL(SQL_DELETE_TRAILER_TABLE);
        db.execSQL(SQL_DELETE_REVIEW_TABLE);

        // Create the database with new schema.
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
