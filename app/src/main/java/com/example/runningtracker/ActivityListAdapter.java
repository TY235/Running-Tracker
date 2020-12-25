package com.example.runningtracker;

import android.content.Context;
import android.util.Log;
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


public class ActivityListAdapter extends RecyclerView.Adapter<ActivityListAdapter.ViewHolder>{

    private ArrayList<ActivityModel> activityModelList;

    private ArrayList<Integer> activityIDList;
    private ArrayList<Integer> dateList;
    private ArrayList<Integer> timeList;
    private ArrayList<Integer> timeTakenList;
    private ArrayList<Double> distanceList;
    private ArrayList<Double> speedList;
    private ArrayList<Double> caloriesBurnedList;
    private ArrayList<String> weatherList;
    private ArrayList<String> satisfactionList;
    private RecyclerViewClickInterface recyclerViewClickInterface;

    public ActivityListAdapter(ArrayList<ActivityModel>activityModelList, RecyclerViewClickInterface recyclerViewClickInterface){
        this.activityModelList = activityModelList;
        this.recyclerViewClickInterface = recyclerViewClickInterface;
//        this.activityIDList = activityIDList;
//        this.dateList = dateList;
//        this.timeList = timeList;
//        this.distanceList = distanceList;
//        this.timeTakenList = timeTakenList;
//        this.speedList = speedList;
//        this.caloriesBurnedList = caloriesBurnedList;
//        this.weatherList = weatherList;
//        this.satisfactionList = satisfactionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Log.d("activitymodel", "id: " + activityModelList.get(position).getActivityID());
        holder.idView.setText(String.valueOf(activityModelList.get(position).getActivityID()));
        holder.dateView.setText(Utilities.formatDateToDDMMYYY(activityModelList.get(position).getDate()));
        holder.timeView.setText(Utilities.formatTimeToHHMM(activityModelList.get(position).getTime()));
        holder.distanceView.setText(String.format(Locale.ENGLISH, "%.2f km", activityModelList.get(position).getTotalDistance()));
        holder.timeTakenView.setText(Utilities.convertStoHMS(activityModelList.get(position).getTotalTimeTaken()));
        holder.speedView.setText(String.format(Locale.ENGLISH, "%s m/s", activityModelList.get(position).getAvgSpeed()));
        holder.caloriesBurnedView.setText(String.format(Locale.ENGLISH, "%.1f kCal", activityModelList.get(position).getTotalCaloriesBurned()));
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("activitymodel", "id: " + idView.getText().toString());
                    recyclerViewClickInterface.onIDSent(Integer.parseInt(idView.getText().toString()));
                }
            });
        }
    }
}
