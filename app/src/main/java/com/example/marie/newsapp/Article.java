package com.example.marie.newsapp;


/**
 * Created by Marie on 7/12/2017.
 */

public class Article {
    private String title, section, author, dateReleased, webUrl;

    public Article(String in_title, String in_section, String in_author,
                    String in_released, String in_url){
        if (in_title != null)
            title = in_title;
        else
            title = "init title";
        
        if (in_section != null)
            section = in_section;
        else
            section = "init section";
        
        if (in_author != null)
            author = in_author;
        else
            author = "init author";

        if (in_released != null)
            dateReleased = in_released;
        else
            dateReleased = "init released";
        
        if (in_url != null)
            webUrl = in_url;
        else
            webUrl = "http://www.google.com";
    }
    
    public String get_title() {return title;}
    public String get_section() {return section;}
    public String get_author() {return author;}
    public String get_released() {return dateReleased;}
    public String get_url() {return webUrl;}
}
