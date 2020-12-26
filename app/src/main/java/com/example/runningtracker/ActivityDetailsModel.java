package com.example.runningtracker;

public class ActivityDetailsModel {

    private final double totalDistance,avgSpeed, totalCaloriesBurned, pace;
    private final int date, time, activityID, totalTimeTaken;
    private final String weather, satisfaction, notes;

    public double getTotalDistance() {
        return totalDistance;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public double getTotalCaloriesBurned() {
        return totalCaloriesBurned;
    }

    public double getPace() {
        return pace;
    }

    public int getDate() {
        return date;
    }

    public int getTime() {
        return time;
    }

    public int getActivityID() {
        return activityID;
    }

    public int getTotalTimeTaken() {
        return totalTimeTaken;
    }

    public String getWeather() {
        return weather;
    }

    public String getSatisfaction() {
        return satisfaction;
    }

    public String getNotes() {
        return notes;
    }

    public ActivityDetailsModel(int activityID, int date, int time, double totalDistance, int totalTimeTaken, double avgSpeed, double pace,  double totalCaloriesBurned, String weather, String satisfaction, String notes) {
        this.totalDistance = totalDistance;
        this.avgSpeed = avgSpeed;
        this.totalCaloriesBurned = totalCaloriesBurned;
        this.pace = pace;
        this.date = date;
        this.time = time;
        this.activityID = activityID;
        this.totalTimeTaken = totalTimeTaken;
        this.weather = weather;
        this.satisfaction = satisfaction;
        this.notes = notes;
    }
}
