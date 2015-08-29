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

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements DiscoverFragment.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set default values only the first time.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get id of the menu item selected.
        int id = item.getItemId();

        // Check if the settings item was selected.
        if (id == R.id.action_settings) {
            // Start the settings activity.
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMovieSelected(DiscoverData discoverData) {
        // Create intent to launch details activity.
        Intent intent = new Intent(this, DetailsActivity.class);

        // Add movie details into extra.
        intent.putExtra(DetailsFragment.DISCOVER_MOVIE, discoverData);

        // Start details activity.
        startActivity(intent);
    }
}
