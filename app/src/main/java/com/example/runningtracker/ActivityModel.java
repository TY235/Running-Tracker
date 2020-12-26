package com.example.runningtracker;

public class ActivityModel {

    private final double totalDistance, avgSpeed, totalCaloriesBurned;
    private final int date, time, activityID, totalTimeTaken;
    private final String weather, satisfaction;

    public ActivityModel(int activityID, int date, int time, double totalDistance, int totalTimeTaken, double avgSpeed, double totalCaloriesBurned, String weather, String satisfaction) {
        this.totalDistance = totalDistance;
        this.avgSpeed = avgSpeed;
        this.totalCaloriesBurned = totalCaloriesBurned;
        this.date = date;
        this.time = time;
        this.activityID = activityID;
        this.totalTimeTaken = totalTimeTaken;
        this.weather = weather;
        this.satisfaction = satisfaction;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public double getTotalCaloriesBurned() {
        return totalCaloriesBurned;
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

}
