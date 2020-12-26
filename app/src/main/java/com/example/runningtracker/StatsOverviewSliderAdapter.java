package com.example.runningtracker;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class StatsOverviewSliderAdapter extends RecyclerView.Adapter<StatsOverviewSliderAdapter.ViewHolder>{

    private final ArrayList<StatsOverviewModel> statsOverviewModels;
    String[] headings = {"All Time", "This Month", "Today"};

    public StatsOverviewSliderAdapter(ArrayList<StatsOverviewModel> statsOverviewModels){
        this.statsOverviewModels = statsOverviewModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stats_overview_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (statsOverviewModels.get(position).getTotalRuns() == 0){
            holder.highestSpeedView.setText("N/A");
        }
        else {
            holder.highestSpeedView.setText(String.format(Locale.ENGLISH, "%.2f", statsOverviewModels.get(position).getHighestSpeed()));
        }
        Log.d("vp", "on bind distance: " + statsOverviewModels.get(position).getTotalDistance());
        Log.d("vp", "on bind runs: " + statsOverviewModels.get(position).getTotalRuns());
        Log.d("vp", "on bind speed: " + statsOverviewModels.get(position).getHighestSpeed());
        Log.d("vp", "on bind calories: " + statsOverviewModels.get(position).getTotalCaloriesBurned());
        holder.headingView.setText(headings[position]);
        holder.totalDistanceView.setText(String.format(Locale.ENGLISH, "%.2f", statsOverviewModels.get(position).getTotalDistance()));
        holder.totalRunsView.setText(String.format(Locale.ENGLISH, "%d", statsOverviewModels.get(position).getTotalRuns()));
        holder.totalCaloriesView.setText(String.format(Locale.ENGLISH, "%.1f", statsOverviewModels.get(position).getTotalCaloriesBurned()));
    }

    @Override
    public int getItemCount() {
        return statsOverviewModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView totalDistanceView, totalRunsView, highestSpeedView, totalCaloriesView, headingView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d("vp", "ViewHolder: view founded!!!!!!!!" );
            totalDistanceView = itemView.findViewById(R.id.statsOverviewItems_totalDistance);
            totalRunsView = itemView.findViewById(R.id.statsOverviewItems_totalRuns);
            highestSpeedView = itemView.findViewById(R.id.statsOverviewItems_highestSpeed);
            totalCaloriesView = itemView.findViewById(R.id.statsOverviewItems_totalCaloriesBurned);
            headingView = itemView.findViewById(R.id.statsOverview_heading);
        }
    }

}
