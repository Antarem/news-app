package com.example.marie.newsapp;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by Marie on 7/12/2017.
 */

public class NewsQueryUtils {

    final private static String LOG_TAG = "NewsQueryUtils";
    final private static String GUARDIAN_URL = "https://content.guardianapis.com/search?&section=INPUT&show-fields=lastModified&show-tags=contributor&api-key=test";

    static private JSONObject loadNewJSONObject(String request_data, String outputId) {
        JSONObject output;
        try {
            output = new JSONObject(request_data);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Loading new JSONObject " + outputId + " failed", e);
            return null;
        }
        return output;
    }

    static private JSONObject getJSONObjectFromJSONObject(JSONObject source, String key, String outputId) {
        JSONObject output;
        try {
            output = source.getJSONObject(key);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Loading JSONObject " + outputId + " failed", e);
            return null;
        }
        return output;
    }

    static private JSONArray getJSONArrayFromJSONObject(JSONObject source, String key, String outputId) {
        JSONArray output;
        try {
            output = source.getJSONArray(key);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Loading JSONArray " + outputId + " failed", e);
            return null;
        }
        return output;
    }

    static private JSONObject getJSONObjectFromJSONArray(JSONArray source, int index, String outputId) {
        JSONObject output;
        try {
            output = source.getJSONObject(index);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Loading JSONObject " + outputId + " failed", e);
            return null;
        }
        return output;
    }

    static private String getStringFromJSONObject(JSONObject source, String key, String failsafe, String outputId) {
        String output;
        try {
            output = source.getString(key);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Loading String " + outputId + " failed", e);
            return failsafe;
        }
        return output;
    }

    @Nullable
    static private ArrayList<Article> processJSON(String request_data) {
        ArrayList<Article> output = new ArrayList<Article>();

        JSONObject data = loadNewJSONObject(request_data, "data");
        if (data == null)
            return null;

        JSONObject response = getJSONObjectFromJSONObject(data, "response", "response");
        if (response == null)
            return null;

        JSONArray results = getJSONArrayFromJSONObject(response, "results", "results");
        if (results == null)
            return null;

        for(int i = 0; i < results.length(); i++) {
            JSONObject element = getJSONObjectFromJSONArray(results, i, "element #" + String.valueOf(i));
            String new_title, new_section, new_released, new_url, new_author;
            if (element == null) {
                Article unloaded_article = new Article("Failed to load article data", "Error",
                                                            "Error", "Error", "http://www.google.com");
                output.add(unloaded_article);
                continue;
            }
            new_title = getStringFromJSONObject(element, "webTitle", "Title not found", "title from element #" + String.valueOf(i));
            new_section = getStringFromJSONObject(element, "sectionName", "Section not found", "section from element #" + String.valueOf(i));
            new_released = getStringFromJSONObject(element, "webPublicationDate", "Publish date not found", "released from element #" + String.valueOf(i));
            String[] time = new_released.split("T");
            new_released = time[1].substring(0, time[1].length()-1) + ", " + time[0];
            // assuming the format of published date is YYYY-MM-DDTHH:MM:SSZ
            new_url = getStringFromJSONObject(element, "webUrl", "http://www.google.com", "url from element #" + String.valueOf(i));

            JSONArray tags = getJSONArrayFromJSONObject(element, "tags", "tags from element #" + String.valueOf(i));
            if(tags == null) { // assuming the query was done with "&show-tags=contributor"
                new_author = "The Guardian";
            }
            else {
                new_author = "";
                for (int j = 0; j < tags.length(); j++){ // should skip the loop should the tags be empty
                    JSONObject tag = getJSONObjectFromJSONArray(tags, j, "tag #" + String.valueOf(j) +
                            " from element #" + String.valueOf(i));
                    if (tag == null)
                        new_author = new_author + "Tag not found";
                    else new_author = new_author + getStringFromJSONObject(tag, "webTitle", "Author not found",
                            "author from tag #" + String.valueOf(j) + " from element #" + String.valueOf(i));
                    new_author = new_author + ", ";
                }
                if(new_author.length() > 2)
                    new_author = new_author.substring(0, new_author.length()-2); // clean up trailing ", "
                else
                    new_author = "The Guardian";
            }
            Article loaded_article = new Article(new_title, new_section, new_author, new_released, new_url);
            output.add(loaded_article);
        }

        return output;
    }

    static public ArrayList<Article> getTheNews(String section_to_get) {
        String URL_String = GUARDIAN_URL.replace("INPUT", section_to_get), request_data;
        URL kiosk = createUrl(URL_String);
        try {request_data = makeHttpRequest(kiosk);}
        catch (IOException e) {return null;}
        return processJSON(request_data);
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

}
