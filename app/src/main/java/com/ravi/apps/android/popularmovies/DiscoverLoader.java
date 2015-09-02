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

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Asynchronously loads the discover movies data from the TMDB server.
 */
public class DiscoverLoader extends AsyncTaskLoader<DiscoverLoaderResult> {

    // Tag for logging messages.
    public final String LOG_TAG = DiscoverLoader.class.getSimpleName();

    // Store the result from the asynchronous load.
    private DiscoverLoaderResult mLoaderResult;

    // Base URL for the query.
    private final String TMDB_DISCOVER_MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";

    // Query parameters.
    private final String TMDB_API_KEY_PARAM = "api_key";
    private final String TMDB_SORT_BY_PARAM = "sort_by";

    // Query parameter values.
    private final String TMDB_API_KEY = getContext().getString(R.string.tmdb_api_key);
    private final String TMDB_SORT_BY_POPULARITY = "popularity.desc";
    private final String TMDB_SORT_BY_VOTE_AVERAGE = "vote_average.desc";

    // JSON fields in the query response.
    private final String TMDB_RESULTS = "results";
    private final String TMDB_ID = "id";
    private final String TMDB_ORIGINAL_TITLE = "original_title";
    private final String TMDB_OVERVIEW = "overview";
    private final String TMDB_RELEASE_DATE = "release_date";
    private final String TMDB_POSTER_PATH = "poster_path";
    private final String TMDB_VOTE_AVERAGE = "vote_average";

    // Base URL and size for movie poster image.
    private final String TMDB_POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
    private final String TMDB_POSTER_SIZE = "w185/";

    public DiscoverLoader(Context context) {
        super(context);
    }

    /**
     * Asynchronously loads the discover movies data from the TMDB server and returns the result.
     *
     * @return result of the data load operation
     */
    @Override
    public DiscoverLoaderResult loadInBackground() {

        DiscoverLoaderResult loaderResult = new DiscoverLoaderResult();
        List<Movie> movieList = null;

        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;

        try {
            String movieListJsonStr = null;

            // Get the sort order preference from shared preferences.
            String sortOrderPreference = Utility.getSortOrderPreference(getContext());
            String sortByMostPopular = getContext().getString(R.string.pref_sort_order_most_popular);

            // Set the query param value based on preference.
            String sortBy = null;
            if(sortOrderPreference.equals(sortByMostPopular)) {
                sortBy = TMDB_SORT_BY_POPULARITY;
            } else {
                sortBy = TMDB_SORT_BY_VOTE_AVERAGE;
            }

            // Build the uri for querying data from TMDb api.
            Uri uri = Uri.parse(TMDB_DISCOVER_MOVIES_BASE_URL).buildUpon()
                    .appendQueryParameter(TMDB_API_KEY_PARAM, TMDB_API_KEY)
                    .appendQueryParameter(TMDB_SORT_BY_PARAM, sortBy)
                    .build();

            // Create the url for connecting to TMDb server.
            URL url = new URL(uri.toString());

            // Create the request to TMDB server and open the connection.
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            // Read the response input stream into a string buffer.
            InputStream inputStream = httpURLConnection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuffer stringBuffer = new StringBuffer();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            // Convert the string buffer to string.
            movieListJsonStr = stringBuffer.toString();

            // Parse the JSON string and extract the movie data.
            movieList = getMovieListFromJson(movieListJsonStr);

        } catch (IOException | JSONException e) {
            Log.e(LOG_TAG, "Error: loadInBackground(): " + e.getLocalizedMessage());

            // Store the exception into the load result.
            loaderResult.setException(e);
        } finally {
            // Close url connection, if open.
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }

            // Close the buffered reader, if open.
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error: loadInBackground(): " + e.getLocalizedMessage());
                }
            }
        }

        // Store the list of movie data into the load result.
        loaderResult.setData(movieList);

        return loaderResult;
    }

    /*
     * Parses the input JSON string and returns a list of movie data. Throws a
     * JSON exception in case of any error.
     */
    private List<Movie> getMovieListFromJson(String jsonResponseString)
            throws JSONException {

        // Create the JSON object from the input string.
        JSONObject moviesJson = new JSONObject(jsonResponseString);

        // Get the JSON array containing the results.
        JSONArray resultsJsonArray = moviesJson.getJSONArray(TMDB_RESULTS);

        // Get length of the results array.
        int resultsLength = resultsJsonArray.length();

        // Parse the results array only if it's not empty.
        if(resultsLength != 0) {
            // Create the list to store the movie data objects.
            List<Movie> movieList = new ArrayList<>();

            // Traverse each result.
            for(int i = 0; i < resultsLength; i++) {
                // Get the JSON object containing a movie.
                JSONObject jsonMovie = resultsJsonArray.getJSONObject(i);

                // Extract and store the movie data from the JSON object.
                int id = jsonMovie.getInt(TMDB_ID);
                String title = jsonMovie.getString(TMDB_ORIGINAL_TITLE);
                String overview = jsonMovie.getString(TMDB_OVERVIEW);
                String date = jsonMovie.getString(TMDB_RELEASE_DATE);
                String poster = TMDB_POSTER_BASE_URL + TMDB_POSTER_SIZE + jsonMovie.getString(TMDB_POSTER_PATH);
                double vote = jsonMovie.getDouble(TMDB_VOTE_AVERAGE);

                // Create the movie data object.
                Movie movie = new Movie(id, title, poster, date, vote, overview);

                // Add the movie data object to the list.
                movieList.add(movie);
            }

            return movieList;
        } else {
            // No results found in the JSON response string.
            throw new JSONException(getContext().getString(R.string.err_zero_results));
        }
    }

    @Override
    public void deliverResult(DiscoverLoaderResult data) {
        // Return if the loader was reset.
        if(isReset()) {
            return;
        }

        // Hold the old data till result has been delivered.
        DiscoverLoaderResult oldResult = mLoaderResult;
        mLoaderResult = data;

        // Loader is in started state so deliver the result.
        if(isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        // Force load only if results are not available.
        if(mLoaderResult != null) {
            deliverResult(mLoaderResult);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(DiscoverLoaderResult data) {
        super.onCanceled(data);
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        if(mLoaderResult != null) {
            mLoaderResult = null;
        }
    }
}
