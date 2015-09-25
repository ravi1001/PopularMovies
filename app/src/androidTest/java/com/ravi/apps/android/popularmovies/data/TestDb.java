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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Tests the movie database.
 */
public class TestDb extends AndroidTestCase {
    // Deletes the database.
    void deleteDatabase() {
        mContext.deleteDatabase(MovieDBHelper.DATABASE_NAME);
    }

    // Starts afresh every time.
    public void setUp() {
        deleteDatabase();
    }

    // Tests the db creation.
    public void testCreateDb() throws Throwable {
        // HashSet of all of the table names we need to check for.
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MovieContract.MovieEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.TrailerEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.ReviewEntry.TABLE_NAME);

        mContext.deleteDatabase(MovieDBHelper.DATABASE_NAME);
        SQLiteDatabase db = new MovieDBHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Check if any tables were created.
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: Database without any tables",
                c.moveToFirst());

        // Check if all of the tables have been created.
        do {
            tableNameHashSet.remove(c.getString(0));
        } while(c.moveToNext());

        assertTrue("Error: Database created without all the required tables",
                tableNameHashSet.isEmpty());

        // Check if the movie table contain the correct columns.
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieEntry.TABLE_NAME + ")", null);

        assertTrue("Error: Query to database for table meta information failed",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want.
        final HashSet<String> movieColumnHashSet = new HashSet<String>();
        movieColumnHashSet.add(MovieContract.MovieEntry._ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_POSTER_IMAGE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_RUNTIME);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_OVERVIEW);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            movieColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that it doesn't contain all of the required movie entry columns.
        assertTrue("Error: Database doesn't contain all of the required movie entry columns",
                movieColumnHashSet.isEmpty());

        // Check if the trailer table contain the correct columns.
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.TrailerEntry.TABLE_NAME + ")", null);

        assertTrue("Error: Query to database for table meta information failed",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want.
        final HashSet<String> trailerColumnHashSet = new HashSet<String>();
        trailerColumnHashSet.add(MovieContract.TrailerEntry._ID);
        trailerColumnHashSet.add(MovieContract.TrailerEntry.COLUMN_MOVIE_KEY);
        trailerColumnHashSet.add(MovieContract.TrailerEntry.COLUMN_URI);
        trailerColumnHashSet.add(MovieContract.TrailerEntry.COLUMN_NAME);

        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            trailerColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that it doesn't contain all of the required trailer entry columns.
        assertTrue("Error: Database doesn't contain all of the required trailer entry columns",
                trailerColumnHashSet.isEmpty());

        // Check if the review table contain the correct columns.
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.ReviewEntry.TABLE_NAME + ")", null);

        assertTrue("Error: Query to database for table meta information failed",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want.
        final HashSet<String> reviewColumnHashSet = new HashSet<String>();
        reviewColumnHashSet.add(MovieContract.ReviewEntry._ID);
        reviewColumnHashSet.add(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY);
        reviewColumnHashSet.add(MovieContract.ReviewEntry.COLUMN_AUTHOR);
        reviewColumnHashSet.add(MovieContract.ReviewEntry.COLUMN_CONTENT);

        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            reviewColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that it doesn't contain all of the required trailer entry columns.
        assertTrue("Error: Database doesn't contain all of the required review entry columns",
                reviewColumnHashSet.isEmpty());

        db.close();
    }

    // Tests the movie table.
    public void testMovieTable() throws Throwable {
        // Get reference to writable db.
        SQLiteDatabase db = new MovieDBHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Insert content values into database and get a row id back.
        ContentValues movieContent = new ContentValues();

        movieContent.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, 12345);
        movieContent.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, "Mad Max");
        movieContent.put(MovieContract.MovieEntry.COLUMN_POSTER_IMAGE, new byte[]{1, 2});
        movieContent.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, "2015-4-10");
        movieContent.put(MovieContract.MovieEntry.COLUMN_RUNTIME, 90);
        movieContent.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 7.5);
        movieContent.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "Good movie");

        long rowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, movieContent);

        // Verify the insert was successful.
        assertTrue("Error: Failed to insert values into movie table", rowId != -1);

        // Query the database and receive a cursor back.
        Cursor movieCursor = db.query(MovieContract.MovieEntry.TABLE_NAME,
                null, null, null, null, null, null);

        // Move the cursor to a valid database row.
        assertTrue("Error: Empty cursor returned from movie table", movieCursor.moveToFirst());

        // Close the cursor and database.
        movieCursor.close();
        db.close();
    }

    // Tests the trailer table.
    public void testTrailerTable() throws Throwable {
        // Get reference to writable db.
        SQLiteDatabase db = new MovieDBHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Insert content values into database and get a row id back.
        ContentValues trailerContent = new ContentValues();

        trailerContent.put(MovieContract.TrailerEntry.COLUMN_MOVIE_KEY, 12345);
        trailerContent.put(MovieContract.TrailerEntry.COLUMN_URI, "Mad Max URI");
        trailerContent.put(MovieContract.TrailerEntry.COLUMN_NAME, "Official Trailer");

        long rowId = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, trailerContent);

        // Verify the insert was successful.
        assertTrue("Error: Failed to insert values into movie table", rowId != -1);

        // Query the database and receive a cursor back.
        Cursor trailerCursor = db.query(MovieContract.TrailerEntry.TABLE_NAME,
                null, null, null, null, null, null);

        // Move the cursor to a valid database row.
        assertTrue("Error: Empty cursor returned from movie table", trailerCursor.moveToFirst());

        // Close the cursor and database.
        trailerCursor.close();
        db.close();
    }

    // Tests the review table.
    public void testReviewTable() throws Throwable {
        // Get reference to writable db.
        SQLiteDatabase db = new MovieDBHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Insert content values into database and get a row id back.
        ContentValues reviewContent = new ContentValues();

        reviewContent.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, 12345);
        reviewContent.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, "Zylo");
        reviewContent.put(MovieContract.ReviewEntry.COLUMN_CONTENT, "Awesome action movie!");

        long rowId = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, reviewContent);

        // Verify the insert was successful.
        assertTrue("Error: Failed to insert values into movie table", rowId != -1);

        // Query the database and receive a cursor back.
        Cursor reviewCursor = db.query(MovieContract.ReviewEntry.TABLE_NAME,
                null, null, null, null, null, null);

        // Move the cursor to a valid database row.
        assertTrue("Error: Empty cursor returned from movie table", reviewCursor.moveToFirst());

        // Close the cursor and database.
        reviewCursor.close();
        db.close();
    }
}
