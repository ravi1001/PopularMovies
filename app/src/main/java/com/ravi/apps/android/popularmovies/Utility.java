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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Provides various utility methods.
 */
public class Utility {
    /**
     * Returns a String object containing the current sort order preference
     * retrieved from the shared preferences.
     *
     * @param context the application context
     * @return        the current sort order preference
     */
    public static String getSortOrderPreference(Context context) {
        // Get shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Get the sort order key and default value from resources.
        String sortOrderKey = context.getString(R.string.pref_sort_order_key);
        String sortOrderDefault = context.getString(R.string.pref_sort_order_most_popular);

        // Retrieve the sort order value from shared preferences.
        String sortOrderValue = sharedPreferences.getString(sortOrderKey, sortOrderDefault);

        return sortOrderValue;
    }

    /**
     * Returns the appropriate error message to be displayed to the user based on the
     * exception that has occurred.
     *
     * @param context   the application context
     * @param exception the exception that occurred
     * @return          the error message to be displayed to the user
     */
    public static String getErrorMessage(Context context, Exception exception) {
        // Extract the exception message from the exception.
        String exceptionMessage = exception.getMessage();

        // Determine the error message to be displayed.
        String errMessage = null;
        if(exceptionMessage.contains(context.getString(R.string.err_unknown_host))) {
            errMessage = context.getString(R.string.msg_err_unknown_host);
        } else if(exceptionMessage.contains(context.getString(R.string.err_authentication_failed))) {
            errMessage = context.getString(R.string.msg_err_authentication_failed);
        } else {
            errMessage = context.getString(R.string.msg_err_default);
        }

        return errMessage;
    }
}
