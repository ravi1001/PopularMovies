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
 * Asynchronously loads the specified movie data including trailers and
 * reviews from the TMDB server.
 */
public class DetailsLoader extends AsyncTaskLoader<DetailsLoaderResult> {
    // Tag for logging messages.
    public final String LOG_TAG = DetailsLoader.class.getSimpleName();

    // Store the movie id.
    private long mMovieId;

    // Store the result from the asynchronous load.
    private DetailsLoaderResult mLoaderResult;

    // Base URL for the query.
    private final String TMDB_MOVIE_DETAILS_BASE_URL = "http://api.themoviedb.org/3/movie/";

    // Base URL for YouTube trailer.
    private final String YOU_TUBE_BASE_URL = "https://www.youtube.com/watch?v=";

    // Base URL and size for movie poster image.
    private final String TMDB_POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
    private final String TMDB_POSTER_SIZE = "w185/";

    // Query parameters.
    private final String TMDB_API_KEY_PARAM = "api_key";
    private final String TMDB_APPEND_TO_RESPONSE_PARAM = "append_to_response";

    // Query parameter values.
    private final String TMDB_API_KEY = getContext().getString(R.string.tmdb_api_key);
    private final String TMDB_APPEND_TO_RESPONSE = "videos,reviews";

    // JSON fields in the query response.
    private final String JSON_ID = "id";
    private final String JSON_ORIGINAL_TITLE = "original_title";
    private final String JSON_POSTER_PATH = "poster_path";
    private final String JSON_RELEASE_DATE = "release_date";
    private final String JSON_RUNTIME = "runtime";
    private final String JSON_VOTE_AVERAGE = "vote_average";
    private final String JSON_OVERVIEW = "overview";
    private final String JSON_VIDEOS = "videos";
    private final String JSON_REVIEWS = "reviews";
    private final String JSON_RESULTS = "results";
    private final String JSON_KEY = "key";
    private final String JSON_NAME = "name";
    private final String JSON_SITE = "site";
    private final String JSON_AUTHOR = "author";
    private final String JSON_CONTENT = "content";
    private final String JSON_URL = "url";

    public DetailsLoader(Context context, long movieId) {
        super(context);

        // Save the movie id.
        mMovieId = movieId;
        Log.e(LOG_TAG, "DetailsLoader()");
    }

    /**
     * Asynchronously loads the discover movies data from the TMDB server and returns the result.
     *
     * @return result of the data load operation
     */
    @Override
    public DetailsLoaderResult loadInBackground() {

        Log.e(LOG_TAG, "loadInBackground");
        DetailsLoaderResult loaderResult = new DetailsLoaderResult();
        Movie movie = null;

        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;

        try {
            String movieJsonStr = null;

            String base = TMDB_MOVIE_DETAILS_BASE_URL + mMovieId + "?";

            // Build the uri for querying data from TMDb api.
            Uri uri = Uri.parse(base).buildUpon()
                    .appendQueryParameter(TMDB_API_KEY_PARAM, TMDB_API_KEY)
                    .appendQueryParameter(TMDB_APPEND_TO_RESPONSE_PARAM, TMDB_APPEND_TO_RESPONSE)
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
            movieJsonStr = stringBuffer.toString();

            // Parse the JSON string and extract the movie data.
            movie = getMovieDetailsFromJson(movieJsonStr);

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

        // Store the movie data into the load result.
        loaderResult.setData(movie);

        return loaderResult;
    }

    /*
     * Parses the input JSON string and returns a list of movie data. Throws a
     * JSON exception in case of any error.
     */
    private Movie getMovieDetailsFromJson(String jsonResponseString)
            throws JSONException {

        // Create the JSON object from the input string.
        JSONObject movieJson = new JSONObject(jsonResponseString);

        // Extract and store the movie data from the JSON object.
        int id = movieJson.getInt(JSON_ID);
        String title = movieJson.getString(JSON_ORIGINAL_TITLE);
        String poster = TMDB_POSTER_BASE_URL + TMDB_POSTER_SIZE + movieJson.getString(JSON_POSTER_PATH);
        String date = movieJson.getString(JSON_RELEASE_DATE);
        int runtime = movieJson.getInt(JSON_RUNTIME);
        double vote = movieJson.getDouble(JSON_VOTE_AVERAGE);
        String overview = movieJson.getString(JSON_OVERVIEW);

        // Extract the trailers json array.
        JSONObject videosJson = movieJson.getJSONObject(JSON_VIDEOS);
        JSONArray videosJsonArray = videosJson.getJSONArray(JSON_RESULTS);

        // List of trailers to be stored inside movie object.
        List<Movie.Trailer> trailerList = null;

        // Check if there are any trailers.
        if(videosJsonArray.length() > 0) {
            trailerList = new ArrayList<>();

            // Extract each trailer from the array.
            for(int i = 0; i < videosJsonArray.length(); i++) {
                // Get the trailer json object.
                JSONObject trailerJson = videosJsonArray.getJSONObject(i);

                // Get the trailer site.
                String trailerSite = trailerJson.getString(JSON_SITE);

                // Check if the trailer site is youtube.
                if(trailerSite.equals(getContext().getString(R.string.trailer_site_youtube))) {
                    // Extract the trailer data.
                    String trailerId = trailerJson.getString(JSON_ID);
                    String trailerUrl = YOU_TUBE_BASE_URL + trailerJson.getString(JSON_KEY);
                    String trailerName = trailerJson.getString(JSON_NAME);

                    // Create the trailer object and add it to the list.
                    Movie.Trailer trailer = new Movie.Trailer(trailerId, trailerUrl, trailerName);
                    trailerList.add(trailer);
                }
            }
        }

        // Extract the reviews json array.
        JSONObject reviewsJson = movieJson.getJSONObject(JSON_REVIEWS);
        JSONArray reviewsJsonArray = reviewsJson.getJSONArray(JSON_RESULTS);

        // List of reviews to be stored inside movie object.
        List<Movie.Review> reviewList = null;

        // Check if there are any reviews.
        if(reviewsJsonArray.length() > 0) {
            reviewList = new ArrayList<>();

            // Extract each review from the array.
            for(int i = 0; i < reviewsJsonArray.length(); i++) {
                // Get the review json object.
                JSONObject reviewJson = reviewsJsonArray.getJSONObject(i);

                // Extract the review data.
                String reviewId = reviewJson.getString(JSON_ID);
                String reviewAuthor = reviewJson.getString(JSON_AUTHOR);
                String reviewContent = reviewJson.getString(JSON_CONTENT);

                // Create the review object and add it to the list.
                Movie.Review review = new Movie.Review(reviewId, reviewAuthor, reviewContent);
                reviewList.add(review);
            }
        }

        // Create the movie data object.
        Movie movie = new Movie(id, title, poster, date, runtime, vote, overview, trailerList, reviewList);

        return movie;
    }

    @Override
    public void deliverResult(DetailsLoaderResult data) {

        Log.e(LOG_TAG, "deliverResult()");
        // Return if the loader was reset.
        if(isReset()) {
            return;
        }

        // Hold the old data till result has been delivered.
        DetailsLoaderResult oldResult = mLoaderResult;
        mLoaderResult = data;

        // Loader is in started state so deliver the result.
        if(isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        Log.e(LOG_TAG, "onStartLoading()");
        // Force load only if results are not available.
        if(mLoaderResult != null) {
            deliverResult(mLoaderResult);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        Log.e(LOG_TAG, "onStopLoading()");
        cancelLoad();
    }

    @Override
    public void onCanceled(DetailsLoaderResult data) {
        super.onCanceled(data);
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        Log.e(LOG_TAG, "onReset");

        if(mLoaderResult != null) {
            mLoaderResult = null;
        }
    }
}
