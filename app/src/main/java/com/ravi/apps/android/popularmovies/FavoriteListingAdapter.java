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

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

/**
 * Cursor adapter for favorite movies.
 */
public class FavoriteListingAdapter extends CursorAdapter {

    public FavoriteListingAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public Object getItem(int position) {
        // Get the cursor and move to position requested.
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);

        // Extract the movie id from the cursor.
        int movieId = cursor.getInt(DiscoverFragment.COL_MOVIE_ID);

        // Create a movie object with the extracted movie id and return it.
        Movie movie = new Movie(movieId, null, null, null, null, 0, 0, null, null, null);

        return movie;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate the image view for the poster image.
        View view = LayoutInflater.from(context)
                .inflate(R.layout.fragment_discover_image, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Get the image view.
        ImageView posterImage = (ImageView) view;

        // Get the poster image blob from cursor.
        byte[] posterByteStream = cursor.getBlob(DiscoverFragment.COL_POSTER_IMAGE);

        // Get the poster image bitmap from byte array.
        Bitmap posterBitmap = BitmapFactory.decodeByteArray(posterByteStream, 0, posterByteStream.length);

        // Set the poster image bitmap into the image view.
        posterImage.setImageBitmap(posterBitmap);
        posterImage.setScaleType(ImageView.ScaleType.FIT_XY);
    }
}
