package com.example.weatherappfinals;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AnnouncementsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AnnouncementAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvNoAnnouncements;
    private Button btnFilter, btnRefresh;
    private FusedLocationProviderClient fusedLocationClient;

    private static final String BASE_URL_CURRENTS = "https://api.currentsapi.services/v1/";
    private static final String BASE_URL_NEWSDATA = "https://newsdata.io/api/1/";
    private static final String CURRENTS_API_KEY = "qvvGRAsoZSaiEKvytZw8l-C6zfbC3aeypqCFL_2z4vFI6Qno";
    private static final String NEWSDATA_API_KEY = "pub_cb1b8ae7120b4ef3a5a51589f620427b";

    private List<Article> allArticles = new ArrayList<>();
    private String userCity = "Philippines";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_announcements, container, false);

        recyclerView = view.findViewById(R.id.recyclerAnnouncements);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        tvNoAnnouncements = view.findViewById(R.id.tvNoAnnouncements);
        btnFilter = view.findViewById(R.id.btnFilter);
        btnRefresh = view.findViewById(R.id.btnRefresh);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AnnouncementAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        swipeRefreshLayout.setOnRefreshListener(this::fetchUserLocation);
        btnFilter.setOnClickListener(v -> showCategoryFilterDialog());
        btnRefresh.setOnClickListener(v -> fetchUserLocation());

        fetchUserLocation();
        return view;
    }

    private void fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
            fetchNewsFromCurrents();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) userCity = getCityFromLocation(location);
            fetchNewsFromCurrents();
        }).addOnFailureListener(e -> fetchNewsFromCurrents());
    }

    private String getCityFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String city = addresses.get(0).getLocality();
                return city != null ? city : "Philippines";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Philippines";
    }

   //currentsapi
    private void fetchNewsFromCurrents() {
        swipeRefreshLayout.setRefreshing(true);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_CURRENTS)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NewsApi api = retrofit.create(NewsApi.class);

        Calendar cal = Calendar.getInstance();
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
        cal.add(Calendar.MONTH, -3);
        String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());


        String query = "walang pasok";

        Call<NewsResponse> call = api.getNews(
                CURRENTS_API_KEY,
                "walang pasok",
                "en",
                "PH",
                startDate,
                endDate
        );


        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                swipeRefreshLayout.setRefreshing(false);

                if (response.code() == 429 || response.code() == 400) {
                    Log.w("NEWS_DEBUG", "CurrentsAPI limit/error. Falling back to NewsData.io");
                    fetchNewsFromNewsData();
                    return;
                }

                if (!response.isSuccessful() || response.body() == null || response.body().news == null) {
                    fetchNewsFromNewsData();
                    return;
                }

                Log.d("NEWS_DEBUG", "CurrentsAPI response: " + new Gson().toJson(response.body()));

                List<Article> articles = filterRelevantArticles(response.body().news);
                if (articles.isEmpty()) {
                    fetchNewsFromNewsData();
                } else {
                    displayArticles(articles);
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e("NEWS_DEBUG", "CurrentsAPI failed: " + t.getMessage());
                fetchNewsFromNewsData();
            }
        });
    }

    //newsdata fallback
    private void fetchNewsFromNewsData() {
        swipeRefreshLayout.setRefreshing(true);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_NEWSDATA)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NewsDataApi newsDataApi = retrofit.create(NewsDataApi.class);

        //fallback query
        String query = "walang pasok";

        Call<NewsDataResponse> call = newsDataApi.getNews(
                NEWSDATA_API_KEY, query, "ph", "en"
        );

        call.enqueue(new Callback<NewsDataResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsDataResponse> call, @NonNull Response<NewsDataResponse> response) {
                swipeRefreshLayout.setRefreshing(false);

                if (!response.isSuccessful() || response.body() == null) {
                    try {
                        Log.e("NEWS_DEBUG", "NewsData Error: " + response.code() + " -> " + response.errorBody().string());
                    } catch (Exception ignored) {}
                    tvNoAnnouncements.setVisibility(View.VISIBLE);
                    return;
                }

                Log.d("NEWS_DEBUG", "Raw NewsData response: " + new Gson().toJson(response.body()));

                if (response.body().results == null || response.body().results.isEmpty()) {
                    tvNoAnnouncements.setVisibility(View.VISIBLE);
                    return;
                }

                List<Article> articles = new ArrayList<>();
                for (NewsDataArticle n : response.body().results) {
                    Article a = new Article();
                    a.title = n.title;
                    a.description = n.description;
                    a.url = n.link;
                    a.image = n.image_url;
                    a.author = n.source_id;
                    articles.add(a);
                }

                List<Article> filtered = filterRelevantArticles(articles);
                if (filtered.isEmpty()) {
                    Log.w("NEWS_DEBUG", "No relevant announcements after filtering");
                    tvNoAnnouncements.setVisibility(View.VISIBLE);
                } else {
                    displayArticles(filtered);
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsDataResponse> call, @NonNull Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e("NEWS_DEBUG", "NewsData Fallback Error: " + t.getMessage());
                tvNoAnnouncements.setVisibility(View.VISIBLE);
            }
        });
    }

    //articlespls
    private void displayArticles(List<Article> articles) {
        allArticles = articles;
        tvNoAnnouncements.setVisibility(articles.isEmpty() ? View.VISIBLE : View.GONE);
        adapter = new AnnouncementAdapter(getContext(), articles);
        recyclerView.setAdapter(adapter);
    }

    //filter ata
    private List<Article> filterRelevantArticles(List<Article> articles) {
        List<Article> filtered = new ArrayList<>();
        for (Article a : articles) {
            if (a.title == null && a.description == null) continue;
            String text = ((a.title != null ? a.title : "") + " " + (a.description != null ? a.description : "")).toLowerCase();

            if (text.matches(".*(walang pasok|class suspension|no classes|school closure|deped|pagasa|ndrrmc|typhoon|storm|flood|earthquake).*")) {
                filtered.add(a);
            }
        }
        return filtered;
    }

    //categoryfilter
    private void showCategoryFilterDialog() {
        String[] options = {"All", "Weather Advisories", "Class Suspensions", "Earthquakes"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Filter Announcements");
        builder.setItems(options, (dialog, which) -> {
            List<Article> filtered = new ArrayList<>();
            for (Article a : allArticles) {
                String text = ((a.title != null ? a.title : "") + " " + (a.description != null ? a.description : "")).toLowerCase();
                switch (which) {
                    case 1:
                        if (text.contains("typhoon") || text.contains("storm") || text.contains("rain") || text.contains("pagasa"))
                            filtered.add(a);
                        break;
                    case 2:
                        if (text.contains("class suspension") || text.contains("no classes") || text.contains("school closure") || text.contains("walang pasok"))
                            filtered.add(a);
                        break;
                    case 3:
                        if (text.contains("earthquake") || text.contains("aftershock"))
                            filtered.add(a);
                        break;
                    default:
                        filtered.addAll(allArticles);
                }
            }

            adapter = new AnnouncementAdapter(getContext(), filtered);
            recyclerView.setAdapter(adapter);
            tvNoAnnouncements.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        });
        builder.show();
    }
}
