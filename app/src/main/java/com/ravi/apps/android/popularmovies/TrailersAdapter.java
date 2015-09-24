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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Array adapter storing the list of trailers.
 */
public class TrailersAdapter extends ArrayAdapter<Movie.Trailer> {

    // List of trailers.
    private List<Movie.Trailer> mTrailerList;

    public TrailersAdapter(Context context, List<Movie.Trailer> trailerList) {
        super(context, 0, trailerList);
        mTrailerList = trailerList;
    }

    @Override
    public int getCount() {
        return mTrailerList.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Movie.Trailer getItem(int position) {
        return mTrailerList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        // Check if it's a recycled view.
        if(convertView == null) {
            // Create a new view.
            view = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item_trailers, parent, false);
        } else {
            // Use the recycled view.
            view = convertView;
        }

        // Set the trailer name.
        TextView name = (TextView) view.findViewById(R.id.trailer_name_textview);
        name.setText(mTrailerList.get(position).getName());

        return view;
    }
}
