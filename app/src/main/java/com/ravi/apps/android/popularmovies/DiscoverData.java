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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Stores the detailed information about a specific movie and implements the parcelable
 * interface to enable it to be passed to other activities through intents.
 */
public class DiscoverData implements Parcelable {

    // Detailed movie information.
    private final int mId;
    private final String mOriginalTitle;
    private final String mPosterPath;
    private final String mReleaseDate;
    private final double mVoteAverage;
    private final String mOverview;

    // Reference used to recreate the object from the parcel.
    public static final Parcelable.Creator<DiscoverData> CREATOR =
            new Parcelable.Creator<DiscoverData>() {
                @Override
                public DiscoverData createFromParcel(Parcel source) {
                    return new DiscoverData(source);
                }

                @Override
                public DiscoverData[] newArray(int size) {
                    return new DiscoverData[size];
                }
            };

    // Public constructor.
    public DiscoverData(int id, String title, String url, String date, double rating, String synopsis) {
        // Store the movie details data into respective member variables.
        mId = id;
        mOriginalTitle = title;
        mPosterPath = url;
        mReleaseDate = date;
        mVoteAverage = rating;
        mOverview = synopsis;
    }

    // Private constructor used to re-create the object from the parcel.
    private DiscoverData(Parcel source) {
        // Extract the movie details data from the parcel and store it into
        // the respective member variables.
        mId = source.readInt();
        mOriginalTitle = source.readString();
        mPosterPath = source.readString();
        mReleaseDate = source.readString();
        mVoteAverage = source.readDouble();
        mOverview = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write the movie details data into the parcel.
        dest.writeInt(mId);
        dest.writeString(mOriginalTitle);
        dest.writeString(mPosterPath);
        dest.writeString(mReleaseDate);
        dest.writeDouble(mVoteAverage);
        dest.writeString(mOverview);
    }

    // Returns the movie id.
    public int getId() {
        return mId;
    }

    // Returns the movie original title.
    public String getOriginalTitle() {
        return mOriginalTitle;
    }

    // Returns the movie poster path.
    public String getPosterPath() {
        return mPosterPath;
    }

    // Returns the movie release date.
    public String getReleaseDate() {
        return mReleaseDate;
    }

    // Returns the movie average rating.
    public double getVoteAverage() {
        return mVoteAverage;
    }

    // Returns the movie plot synopsis.
    public String getOverview() {
        return mOverview;
    }
}
