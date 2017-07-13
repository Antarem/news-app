package com.example.marie.newsapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Article>> {

    final private static String LOG_TAG = "MainActivity";
    private ArticleAdapter frontend;
    private String lastSectionLoaded = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView news_stand = (ListView) findViewById(R.id.news_list);
        frontend = new ArticleAdapter(this, new ArrayList<Article>());
        TextView too_bad = (TextView) findViewById(R.id.empty_list_text);
        news_stand.setAdapter(frontend);
        news_stand.setEmptyView(too_bad);

        ConnectivityManager connMag = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ntwInfo = connMag.getActiveNetworkInfo();
        if(ntwInfo != null && ntwInfo.isConnected()) {
            getLoaderManager().initLoader(1, null, this);
        }
        else {
            findViewById(R.id.ouboros).setVisibility(View.GONE);
            too_bad.setText(R.string.no_internet);
        }

        news_stand.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current earthquake that was clicked on
                Article currentEarthquake = frontend.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri earthquakeUri = Uri.parse(currentEarthquake.get_url());

                // Create a new intent to view the earthquake URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });
    }

    @Override
    public Loader<ArrayList<Article>> onCreateLoader(int i, Bundle of_sticks) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String section = sharedPrefs.getString(
                getString(R.string.settings_section_key),
                getString(R.string.settings_section_default));
        lastSectionLoaded = section;
        Log.i(LOG_TAG, "Creating new Loader for section " + section);
        return new NewsLoader(this, section);
    }

    @Override
    public void onLoadFinished (Loader<ArrayList<Article>> news_boy, ArrayList<Article> news) {
        findViewById(R.id.ouboros).setVisibility(View.GONE);

        frontend.clear();

        if (news != null && !news.isEmpty())
            frontend.addAll(news);
        else {
            TextView too_bad = (TextView) findViewById(R.id.empty_list_text);
            too_bad.setText(R.string.no_news);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Article>> news_boy) {
        frontend.clear();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        String currentSection = PreferenceManager.getDefaultSharedPreferences(this).getString(
            getString(R.string.settings_section_key),
            getString(R.string.settings_section_default));
        if (lastSectionLoaded.equals(currentSection)){
            Log.i(LOG_TAG, "Skipping most of onResume");
            return;
        }
        ListView news_stand = (ListView) findViewById(R.id.news_list);
        frontend.clear();
        TextView too_bad = (TextView) findViewById(R.id.empty_list_text);

        ConnectivityManager connMag = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ntwInfo = connMag.getActiveNetworkInfo();
        if(ntwInfo != null && ntwInfo.isConnected()) {
            getLoaderManager().restartLoader(1, null, this);
        }
        else {
            findViewById(R.id.ouboros).setVisibility(View.GONE);
            too_bad.setText(R.string.no_internet);
        }

        news_stand.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current earthquake that was clicked on
                Article currentEarthquake = frontend.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri earthquakeUri = Uri.parse(currentEarthquake.get_url());

                // Create a new intent to view the earthquake URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle of_sticks) {
        super.onSaveInstanceState(of_sticks);
    }

}
