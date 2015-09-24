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
 * Array adapter storing the list of reviews.
 */
public class ReviewsAdapter extends ArrayAdapter<Movie.Review> {

    // List of trailers.
    private List<Movie.Review> mReviewList;

    public ReviewsAdapter(Context context, List<Movie.Review> reviewList) {
        super(context, 0, reviewList);
        mReviewList = reviewList;
    }

    @Override
    public int getCount() {
        return mReviewList.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Movie.Review getItem(int position) {
        return mReviewList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        // Check if it's a recycled view.
        if(convertView == null) {
            // Create a new view.
            view = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item_reviews, parent, false);
        } else {
            // Use the recycled view.
            view = convertView;
        }

        // Set the review author name.
        ((TextView) view.findViewById(R.id.review_author_textview))
                .setText(mReviewList.get(position).getAuthor());

        // Set the review content.
        ((TextView) view.findViewById(R.id.review_content_textview))
                .setText(mReviewList.get(position).getContent());

        return view;
    }
}
