package com.example.runningtracker;

public class ActivityModel {

    private double totalDistance, avgSpeed, totalCaloriesBurned;
    private int date, time, activityID, totalTimeTaken;
    private String weather, satisfaction;

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

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public double getTotalCaloriesBurned() {
        return totalCaloriesBurned;
    }

    public void setTotalCaloriesBurned(double totalCaloriesBurned) {
        this.totalCaloriesBurned = totalCaloriesBurned;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getActivityID() {
        return activityID;
    }

    public void setActivityID(int activityID) {
        this.activityID = activityID;
    }

    public int getTotalTimeTaken() {
        return totalTimeTaken;
    }

    public void setTotalTimeTaken(int totalTimeTaken) {
        this.totalTimeTaken = totalTimeTaken;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(String satisfaction) {
        this.satisfaction = satisfaction;
    }

}
