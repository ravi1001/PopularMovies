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
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Array adapter storing the list of movie data.
 */
public class DiscoverAdapter extends ArrayAdapter<DiscoverData> {

    // List of movie data.
    private List<DiscoverData> mDiscoverDataList;

    public DiscoverAdapter(Context context, List<DiscoverData> discoverDataList) {
        super(context, 0, discoverDataList);
        mDiscoverDataList = discoverDataList;
    }

    @Override
    public int getCount() {
        return mDiscoverDataList.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public DiscoverData getItem(int position) {
        return mDiscoverDataList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        // Check if it's a recycled view.
        if(convertView == null) {
            // Create a new image view.
            imageView = (ImageView) LayoutInflater.from(getContext())
                    .inflate(R.layout.fragment_discover_image, parent, false);
        } else {
            // Use the recycled image view.
            imageView = (ImageView) convertView;
        }

        // Extract the movie poster path url.
        String posterUrl = mDiscoverDataList.get(position)
                .getPosterPath();

        // Load the movie poster into image view using Picasso.
        Picasso.with(getContext())
                .load(posterUrl)
                .fit()
                .into(imageView);

        return imageView;
    }
}
