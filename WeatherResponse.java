package com.example.weatherappfinals;

import java.util.List;

public class WeatherResponse {
    public Coord coord;
    public List<Weather> weather;
    public Main main;
    public Wind wind;
    public String name;

    public static class Coord {
        public double lon;
        public double lat;
    }

    public static class Weather {
        public String main;
        public String description;
        public String icon;
    }

    public static class Main {
        public double temp;
        public int humidity;
    }

    public static class Wind {
        public double speed;
    }
}
