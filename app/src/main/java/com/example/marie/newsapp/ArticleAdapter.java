package com.example.marie.newsapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Marie on 7/12/2017.
 */

public class ArticleAdapter extends ArrayAdapter<Article>{

    public ArticleAdapter (Context context, ArrayList<Article> the_news) {
        super(context, 0 , the_news);
    }

    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        Article currentNews = getItem(position);
        if (currentNews == null)
            return listItemView;

        TextView title_view = (TextView) listItemView.findViewById(R.id.article_title);
        TextView released_view = (TextView) listItemView.findViewById(R.id.article_released);
        TextView author_view = (TextView) listItemView.findViewById(R.id.article_author);
        TextView section_view = (TextView) listItemView.findViewById(R.id.article_section);

        title_view.setText(currentNews.get_title());
        released_view.setText(currentNews.get_released());
        author_view.setText(currentNews.get_author());
        section_view.setText(currentNews.get_section());

        return listItemView;
    }

}
