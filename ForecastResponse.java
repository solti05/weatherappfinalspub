package com.example.weatherappfinals;

import java.util.List;

public class ForecastResponse {
    public City city;
    public List<ListItem> list;

    public static class City {
        public String name;
        public Coord coord;
        public String country;
    }

    public static class Coord {
        public double lat;
        public double lon;
    }

    // Represents each forecast entry (every 3 hours)
    public static class ListItem {
        public long dt;
        public Main main;
        public List<Weather> weather;
        public Clouds clouds;
        public Wind wind;
        public String dt_txt;
    }

    public static class Main {
        public double temp;
        public double feels_like;
        public double temp_min;
        public double temp_max;
        public int pressure;
        public int humidity;
    }

    public static class Weather {
        public int id;
        public String main;
        public String description;
        public String icon;
    }

    public static class Clouds {
        public int all;
    }

    public static class Wind {
        public double speed;
        public double deg;
    }
}
