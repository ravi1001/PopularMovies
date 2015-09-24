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

/**
 * Stores the result from the load operation that fetches the specific movie data
 * including trailers and reviews from the TMDB server.
 */
public class DetailsLoaderResult {

    // Movie data.
    private Movie mMovie;

    // Stores the error that occurred during the load operation. Null if no errors.
    private Exception mException;

    // Returns the movie data.
    public Movie getData() {
        return mMovie;
    }

    // Returns the error.
    public Exception getException() {
        return mException;
    }

    // Sets the movie data.
    public void setData(Movie data) {
        mMovie = data;
    }

    // Sets the exception.
    public void setException(Exception exception) {
        mException = exception;
    }
}
