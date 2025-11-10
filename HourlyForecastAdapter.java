package com.example.weatherappfinals;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherappfinals.ForecastResponse.ListItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder> {

    private List<ListItem> hourlyData;

    public HourlyForecastAdapter(List<ListItem> hourlyData) {
        this.hourlyData = hourlyData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hourly_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListItem hour = hourlyData.get(position);

        // 🕒 Format time (e.g., "3 PM")
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("h a", Locale.getDefault());
            Date date = inputFormat.parse(hour.dt_txt);
            holder.tvTime.setText(outputFormat.format(date));
        } catch (Exception e) {
            holder.tvTime.setText("--");
        }

        // 🌡️ Temperature
        holder.tvTemp.setText(String.format(Locale.getDefault(), "%.0f°C", hour.main.temp));

        // 🌤️ Weather icon logic
        if (hour.weather != null && !hour.weather.isEmpty()) {
            String iconCode = hour.weather.get(0).icon;
            String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";

            // ✅ Load icon from OpenWeather + fallback to local drawable
            Glide.with(holder.itemView.getContext())
                    .load(iconUrl)
                    .placeholder(getLocalIcon(iconCode)) // show local icon while loading
                    .error(getLocalIcon(iconCode))       // fallback if URL fails
                    .into(holder.imgCondition);
        } else {
            // ❌ No weather data, show default cloud icon
            holder.imgCondition.setImageResource(R.drawable.ic_cloud);
        }
    }

    @Override
    public int getItemCount() {
        return (hourlyData != null) ? hourlyData.size() : 0;
    }

    public void updateData(List<ListItem> newData) {
        this.hourlyData = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTemp;
        ImageView imgCondition;

        ViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_hour_time);
            tvTemp = itemView.findViewById(R.id.tv_hour_temp);
            imgCondition = itemView.findViewById(R.id.img_hour_condition);
        }
    }

    /** 🎨 Maps OpenWeather icon codes to your local drawable icons */
    private static int getLocalIcon(String iconCode) {
        if (iconCode == null) return R.drawable.ic_cloud;

        switch (iconCode) {
            case "01d":
                return R.drawable.ic_clear_day;     // ☀️ clear sky day
            case "01n":
                return R.drawable.ic_clear_night;   // 🌙 clear night
            case "02d":
            case "02n":
                return R.drawable.ic_partly_cloudy; // ⛅ few clouds
            case "03d":
            case "03n":
                return R.drawable.ic_cloud;         // ☁️ scattered clouds
            case "04d":
            case "04n":
                return R.drawable.ic_broken_clouds; // ☁️☁️ overcast
            case "09d":
            case "09n":
                return R.drawable.ic_shower_rain;   // 🌧️ shower rain
            case "10d":
            case "10n":
                return R.drawable.ic_rain;          // 🌦️ rain
            case "11d":
            case "11n":
                return R.drawable.ic_thunderstorm;  // ⛈️ thunderstorm
            case "13d":
            case "13n":
                return R.drawable.ic_snow;          // ❄️ snow
            case "50d":
            case "50n":
                return R.drawable.ic_mist;          // 🌫️ mist/fog
            default:
                return R.drawable.ic_cloud;         // fallback
        }
    }
}
