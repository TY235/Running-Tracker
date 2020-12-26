package com.example.runningtracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;


/* Adapter for activity list in the activity fragment */
public class ActivityListAdapter extends RecyclerView.Adapter<ActivityListAdapter.ViewHolder>{

    private final ArrayList<ActivityModel> activityModelList;
    private final RecyclerViewClickInterface recyclerViewClickInterface;

    /* Get the ArrayList of activity that consists the required details for displaying in the recycler view and the fragment that implements the interface */
    public ActivityListAdapter(ArrayList<ActivityModel>activityModelList, RecyclerViewClickInterface recyclerViewClickInterface){
        this.activityModelList = activityModelList;
        this.recyclerViewClickInterface = recyclerViewClickInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.idView.setText(String.valueOf(activityModelList.get(position).getActivityID()));
        holder.dateView.setText(Utilities.formatDateToDDMMYYY(activityModelList.get(position).getDate()));
        holder.timeView.setText(Utilities.formatTimeToHHMM(activityModelList.get(position).getTime()));
        holder.distanceView.setText(String.format(Locale.ENGLISH, "%.2f km", activityModelList.get(position).getTotalDistance()));
        holder.timeTakenView.setText(Utilities.convertStoHMS(activityModelList.get(position).getTotalTimeTaken()));
        holder.speedView.setText(String.format(Locale.ENGLISH, "%s m/s", activityModelList.get(position).getAvgSpeed()));
        holder.caloriesBurnedView.setText(String.format(Locale.ENGLISH, "%.1f kcal", activityModelList.get(position).getTotalCaloriesBurned()));
        holder.weatherView.setImageResource(Utilities.convertWeatherIcon(activityModelList.get(position).getWeather()));
        holder.satisfactionView.setImageResource(Utilities.convertSatisfactionIcon(activityModelList.get(position).getSatisfaction()));
    }

    @Override
    public int getItemCount() {
        return activityModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView dateView, timeView, distanceView, timeTakenView, speedView, caloriesBurnedView, idView;
        ImageView weatherView, satisfactionView;
        ConstraintLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateView = itemView.findViewById(R.id.date);
            timeView = itemView.findViewById(R.id.time);
            distanceView = itemView.findViewById(R.id.distance);
            timeTakenView = itemView.findViewById(R.id.runningTimeTaken);
            speedView = itemView.findViewById(R.id.speedInMS);
            caloriesBurnedView = itemView.findViewById(R.id.caloriesBurned);
            idView = itemView.findViewById(R.id.activityID);
            weatherView = itemView.findViewById(R.id.weatherIcon);
            satisfactionView = itemView.findViewById(R.id.satisfactionIcon);
            parentLayout = itemView.findViewById(R.id.activityItems);

            itemView.setOnClickListener(v -> {
                /* Pass the activity ID to the class that implemented the interface (ActivityFragment) when the card view of the recycler view is clicked */
                recyclerViewClickInterface.onActivityClicked(Integer.parseInt(idView.getText().toString()));
            });
        }
    }
}
