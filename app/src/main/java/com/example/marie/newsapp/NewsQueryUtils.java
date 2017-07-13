package com.example.marie.newsapp;

import android.support.annotation.NonNull;
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

    static private JSONObject processJSON_getData(String request_data) {
        JSONObject output;
        try {
            output = new JSONObject(request_data);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Loading JSONObject data from String request_data failed", e);
            return null;
        }
        return output;
    }

    static private JSONObject processJSON_getResponse(JSONObject data) {
        JSONObject output;
        try {
            output = data.getJSONObject("response");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Loading JSONObject response from JSONObject data failed", e);
            return null;
        }
        return output;
    }

    static private JSONArray processJSON_getResults(JSONObject response) {
        JSONArray output;
        try {
            output = response.getJSONArray("results");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Loading JSONArray results from JSONObject response failed", e);
            return null;
        }
        return output;
    }

    static private JSONObject processJSON_getResultElement(JSONArray results, int i) {
        JSONObject output;
        try {
            output = results.getJSONObject(i);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Loading JSONObject Element #"+ String.valueOf(i) +" from JSONArray results failed", e);
            return null;
        }
        return output;
    }

    @NonNull
    static private String processJSON_getResultElementTitle(JSONObject results, int i) {
        String output;
        try {
            output = results.getString("webTitle");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Loading String title from JSONObject Element #"+ String.valueOf(i) +" failed", e);
            return "Title not found";
        }
        return output;
    }

    @NonNull
    static private String processJSON_getResultElementSection(JSONObject results, int i) {
        String output;
        try {
            output = results.getString("sectionName");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Loading String section from JSONObject Element #"+ String.valueOf(i) +" failed", e);
            return "Section not found";
        }
        return output;
    }

    @NonNull
    static private String processJSON_getResultElementReleased(JSONObject results, int i) {
        String output;
        try {
            output = results.getString("webPublicationDate");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Loading String released from JSONObject Element #"+ String.valueOf(i) +" failed", e);
            return "Release date not found";
        }
        return output;
    }

    @NonNull
    static private String processJSON_getResultElementUrl(JSONObject results, int i) {
        String output;
        try {
            output = results.getString("webUrl");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Loading String url from JSONObject Element #"+ String.valueOf(i) +" failed", e);
            return "http://www.google.com";
        }
        return output;
    }

    static private JSONArray processJSON_getResultElementTags(JSONObject results, int i) {
        JSONArray output;
        try {
            output = results.getJSONArray("tags");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Loading JSONArray tags from JSONObject Element #"+ String.valueOf(i) +" failed", e);
            return null;
        }
        return output;
    }

    static private JSONObject processJSON_getResultElementTag(JSONArray tags, int i, int j) {
        JSONObject output;
        try {
            output = tags.getJSONObject(j);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Loading JSONObject Tag #"+ String.valueOf(j) +" from JSONArray tags from JSONObject Element #"+ String.valueOf(i) +" failed", e);
            return null;
        }
        return output;
    }

    @NonNull
    static private String processJSON_getResultElementTagContributorName(JSONObject tag, int i, int j) {
        String output;
        try {
            output = tag.getString("webTitle");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Loading String addition from JSONObject Tag #"+ String.valueOf(j) +" from JSONArray tags from JSONObject Element #"+ String.valueOf(i) +" failed", e);
            return "Unknown Name";
        }
        return output;
    }

    @Nullable
    static private ArrayList<Article> processJSON(String request_data) {
        ArrayList<Article> output = new ArrayList<Article>();

        JSONObject data = processJSON_getData(request_data);
        if (data == null)
            return null;

        JSONObject response = processJSON_getResponse(data);
        if (response == null)
            return null;

        JSONArray results = processJSON_getResults(response);
        if (results == null)
            return null;

        for(int i = 0; i < results.length(); i++) {
            JSONObject element = processJSON_getResultElement(results, i);
            String new_title, new_section, new_released, new_url, new_author;
            if (element == null) {
                Article unloaded_article = new Article("Failed to load article data", "Error",
                                                            "Error", "Error", "http://www.google.com");
                output.add(unloaded_article);
                continue;
            }
            new_title = processJSON_getResultElementTitle(element, i);
            new_section = processJSON_getResultElementSection(element, i);
            new_released = processJSON_getResultElementReleased(element, i); // assuming query has "&show-fields=lastModified"
            String[] time = new_released.split("T");
            new_released = time[1].substring(0, time[1].length()-1) + ", " + time[0];
            // assuming the format of published date is YYYY-MM-DDTHH:MM:SSZ
            new_url = processJSON_getResultElementUrl(element, i);

            JSONArray tags = processJSON_getResultElementTags(element, i);
            if(tags == null) { // assuming the query was done with "&show-tags=contributor"
                new_author = "The Guardian";
            }
            else {
                new_author = "";
                for (int j = 0; j < tags.length(); j++){ // should skip the loop should the tags be empty
                    JSONObject tag = processJSON_getResultElementTag(tags, i, j);
                    if (tag == null)
                        new_author = new_author + "Unknown Tag";
                    else new_author = new_author + processJSON_getResultElementTagContributorName(tag, i, j);
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
