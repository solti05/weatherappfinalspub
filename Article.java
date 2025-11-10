package com.example.weatherappfinals;

import java.util.List;

public class Article {
    public String id;
    public String title;
    public String description;
    public String author;
    public String url;
    public String image;
    public String language;
    public List<String> category;
    public String published;

    public String getUrlToImage() {
        return image;
    }
}
