package com.example.marie.newsapp;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Marie on 7/13/2017.
 */

public class NewsLoader extends AsyncTaskLoader<ArrayList<Article>> {

    private String section;

    public NewsLoader(Context ctx, String section_name) {
        super(ctx);
        section = section_name;
    }

    @Override
    public ArrayList<Article> loadInBackground() {
        return NewsQueryUtils.getTheNews(section);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}
