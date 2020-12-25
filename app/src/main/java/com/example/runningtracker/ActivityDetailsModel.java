package com.example.runningtracker;

public class ActivityDetailsModel {

    private double totalDistance, avgSpeed, totalCaloriesBurned, pace;
    private int date, time, activityID, totalTimeTaken;
    private String weather, satisfaction, notes;

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

    public double getPace() {
        return pace;
    }

    public void setPace(double pace) {
        this.pace = pace;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
