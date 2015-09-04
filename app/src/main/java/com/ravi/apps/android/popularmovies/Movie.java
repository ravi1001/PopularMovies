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

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the detailed information about a specific movie and implements the parcelable
 * interface to enable it to be passed to other activities through intents.
 */
public class Movie implements Parcelable {

    // Detailed movie information.
    private final int mId;
    private final String mOriginalTitle;
    private final String mPosterPath;
    private final String mReleaseDate;
    private final double mVoteAverage;
    private final String mOverview;
    private final List<Trailer> mTrailerList = new ArrayList<>();
    private final List<Review> mReviewList = new ArrayList<>();

    /**
     * Stores the movie trailer information and implements the parcelable interface.
     */
    public static class Trailer implements Parcelable {
        // Trailer information.
        private final int mId;
        private final String mKey;
        private final String mName;

        // Public constructor.
        public Trailer(int id, String key, String name) {
            // Store the trailer details data into respective member variables.
            mId = id;
            mKey = key;
            mName = name;
        }

        // Private constructor used to re-create the object from the parcel.
        private Trailer(Parcel source) {
            // Extract the trailer details data from the parcel and store it into
            // the respective member variables.
            mId = source.readInt();
            mKey = source.readString();
            mName = source.readString();
        }

        // Reference used to recreate the object from the parcel.
        public static final Parcelable.Creator<Trailer> CREATOR =
                new Parcelable.Creator<Trailer>() {
                    @Override
                    public Trailer createFromParcel(Parcel source) {
                        return new Trailer(source);
                    }

                    @Override
                    public Trailer[] newArray(int size) {
                        return new Trailer[size];
                    }
                };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            // Write the trailer details data into the parcel.
            dest.writeInt(mId);
            dest.writeString(mKey);
            dest.writeString(mName);
        }

        // Returns the trailer id.
        public int getId() {
            return mId;
        }

        // Returns the trailer key.
        public String getKey() {
            return mKey;
        }

        // Returns the trailer name.
        public String getName() {
            return mName;
        }
    }

    /**
     * Stores the movie review information and implements the parcelable interface.
     */
    public static class Review implements Parcelable  {
        // Review information.
        private final int mId;
        private final String mAuthor;
        private final String mContent;
        private final String mUrl;

        // Public constructor.
        public Review(int id, String author, String content, String url) {
            // Store the review details data into respective member variables.
            mId = id;
            mAuthor = author;
            mContent = content;
            mUrl = url;
        }

        // Private constructor used to re-create the object from the parcel.
        private Review(Parcel source) {
            // Extract the review details data from the parcel and store it into
            // the respective member variables.
            mId = source.readInt();
            mAuthor = source.readString();
            mContent = source.readString();
            mUrl = source.readString();
        }

        // Reference used to recreate the object from the parcel.
        public static final Parcelable.Creator<Review> CREATOR =
                new Parcelable.Creator<Review>() {
                    @Override
                    public Review createFromParcel(Parcel source) {
                        return new Review(source);
                    }

                    @Override
                    public Review[] newArray(int size) {
                        return new Review[size];
                    }
                };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            // Write the review details data into the parcel.
            dest.writeInt(mId);
            dest.writeString(mAuthor);
            dest.writeString(mContent);
            dest.writeString(mUrl);
        }

        // Returns the review id.
        public int getId() {
            return mId;
        }

        // Returns the review author.
        public String getAuthor() {
            return mAuthor;
        }

        // Returns the review content.
        public String getContent() {
            return mContent;
        }

        // Returns the review url.
        public String getUrl() {
            return mUrl;
        }
    }

    // Public constructor.
    public Movie(int id, String title, String url, String date, double rating,
                 String synopsis, List<Trailer> trailerList, List<Review> reviewList) {
        // Store the movie details data into respective member variables.
        mId = id;
        mOriginalTitle = title;
        mPosterPath = url;
        mReleaseDate = date;
        mVoteAverage = rating;
        mOverview = synopsis;

        if(trailerList != null) {
            mTrailerList.addAll(trailerList);
        }

        if(reviewList != null) {
            mReviewList.addAll(reviewList);
        }
    }

    // Private constructor used to re-create the object from the parcel.
    private Movie(Parcel source) {
        // Extract the movie details data from the parcel and store it into
        // the respective member variables.
        mId = source.readInt();
        mOriginalTitle = source.readString();
        mPosterPath = source.readString();
        mReleaseDate = source.readString();
        mVoteAverage = source.readDouble();
        mOverview = source.readString();
        source.readTypedList(mTrailerList, Trailer.CREATOR);
        source.readTypedList(mReviewList, Review.CREATOR);
    }

    // Reference used to recreate the object from the parcel.
    public static final Parcelable.Creator<Movie> CREATOR =
            new Parcelable.Creator<Movie>() {
                @Override
                public Movie createFromParcel(Parcel source) {
                    return new Movie(source);
                }

                @Override
                public Movie[] newArray(int size) {
                    return new Movie[size];
                }
            };

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
        dest.writeTypedList(mTrailerList);
        dest.writeTypedList(mReviewList);
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
