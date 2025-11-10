package com.example.weatherappfinals;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.weatherappfinals.databinding.FragmentWeatherBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherFragment extends Fragment {

    private FragmentWeatherBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private WeatherApi weatherApi;

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private String apiKey = BuildConfig.OPENWEATHER_API_KEY;

    private double currentLat = 14.5995; // Default: Manila
    private double currentLon = 120.9842;

    // For cycling layers (0 = off, 1 = temp, 2 = clouds, 3 = rain, 4 = wind)
    private int layerMode = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWeatherBinding.inflate(inflater, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Retrofit setup
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherApi = retrofit.create(WeatherApi.class);

        // Get location and load weather/map
        getDeviceLocation();

        // Refresh button
        binding.btnRefresh.setOnClickListener(v -> refreshWeather());

        // Cycle through layers when clicking the Layers button
        binding.btnLayers.setOnClickListener(v -> {
            layerMode = (layerMode + 1) % 5;
            setupWebView(currentLat, currentLon, layerMode);
            String[] names = {"Default map ☀️", "Temperature 🌡️", "Clouds ☁️", "Precipitation 🌧️", "Wind 💨"};
            Toast.makeText(requireContext(), "Layer: " + names[layerMode], Toast.LENGTH_SHORT).show();
        });

        // "Live" blinking animation
        ObjectAnimator animator = ObjectAnimator.ofFloat(binding.tvLive, "alpha", 1f, 0.3f);
        animator.setDuration(800);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();

        return binding.getRoot();
    }

    /** ✅ Get device location or default to Manila */
    private void getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
            setupWebView(currentLat, currentLon, layerMode);
            fetchWeatherData(currentLat, currentLon);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLon = location.getLongitude();
            }
            setupWebView(currentLat, currentLon, layerMode);
            fetchWeatherData(currentLat, currentLon);
        });
    }

    /** 🔁 Refresh weather and map */
    private void refreshWeather() {
        getDeviceLocation();
        Toast.makeText(requireContext(), "Refreshing weather data...", Toast.LENGTH_SHORT).show();
    }

    /** 🗺️ Setup Leaflet map with OpenWeather free layers */
    private void setupWebView(double lat, double lon, int mode) {
        WebView webView = binding.webViewMap;
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        String layerUrl = "";
        switch (mode) {
            case 1:
                layerUrl = "https://tile.openweathermap.org/map/temp_new/{z}/{x}/{y}.png?appid=" + apiKey;
                break;
            case 2:
                layerUrl = "https://tile.openweathermap.org/map/clouds_new/{z}/{x}/{y}.png?appid=" + apiKey;
                break;
            case 3:
                layerUrl = "https://tile.openweathermap.org/map/precipitation_new/{z}/{x}/{y}.png?appid=" + apiKey;
                break;
            case 4:
                layerUrl = "https://tile.openweathermap.org/map/wind_new/{z}/{x}/{y}.png?appid=" + apiKey;
                break;
            default:
                layerUrl = "";
        }

        String overlayScript = "";
        if (!layerUrl.isEmpty()) {
            overlayScript = "L.tileLayer('" + layerUrl + "', {opacity:0.6}).addTo(map);";
        }

        String html = "<html><head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.3/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.9.3/dist/leaflet.js'></script>" +
                "<style>html,body,#map{height:100%;margin:0;}</style></head>" +
                "<body><div id='map'></div>" +
                "<script>" +
                "var map=L.map('map').setView([" + lat + "," + lon + "],7);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:10}).addTo(map);" +
                overlayScript +
                "L.marker([" + lat + "," + lon + "]).addTo(map).bindPopup('You are here').openPopup();" +
                "</script></body></html>";

        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    /** 🌦️ Fetch current weather + forecast */
    private void fetchWeatherData(double lat, double lon) {
        //Current weather
        weatherApi.getCurrentWeather(lat, lon, apiKey, "metric").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Weather API error: " + response.code(), Toast.LENGTH_LONG).show();
                    return;
                }

                WeatherResponse data = response.body();
                if (data == null) return;

                String temp = Math.round(data.main.temp) + "°C";
                String condition = data.weather.get(0).description;
                String humidity = "Humidity: " + data.main.humidity + "%";
                String wind = "Wind: " + data.wind.speed + " m/s";

                binding.tvTemperature.setText(temp);
                binding.tvCondition.setText(condition);
                binding.tvHumidity.setText(humidity);
                binding.tvWind.setText(wind);
                binding.tvLocation.setText(data.name);

                // Save city name for AnnouncementsFragment use
                if (data.name != null && !data.name.isEmpty()) {
                    SharedPrefManager.getInstance(requireContext()).setUserCity(data.name);
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Failed to load weather: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Forecast (3-hourly)
        weatherApi.getForecast(lat, lon, apiKey, "metric").enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(@NonNull Call<ForecastResponse> call, @NonNull Response<ForecastResponse> response) {
                if (!response.isSuccessful()) return;

                ForecastResponse forecast = response.body();
                if (forecast == null) return;

                binding.hourlyContainer.removeAllViews();
                for (int i = 0; i < 6 && i < forecast.list.size(); i++) {
                    ForecastResponse.ListItem item = forecast.list.get(i);
                    String time = item.dt_txt.substring(11, 16);
                    String text = time + "\n" + Math.round(item.main.temp) + "°C";

                    TextView tv = new TextView(requireContext());
                    tv.setText(text);
                    tv.setPadding(12, 8, 12, 8);
                    tv.setTextSize(14);
                    binding.hourlyContainer.addView(tv,
                            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ForecastResponse> call, @NonNull Throwable t) { }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
