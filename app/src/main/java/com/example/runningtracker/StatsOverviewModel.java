package com.example.runningtracker;

public class StatsOverviewModel{

    private double totalDistance, highestSpeed, totalCaloriesBurned;
    private int totalRuns;

    public StatsOverviewModel(double totalDistance, int totalRuns, double bestSpeed, double totalCaloriesBurned) {
        this.totalDistance = totalDistance;
        this.totalRuns = totalRuns;
        this.highestSpeed = bestSpeed;
        this.totalCaloriesBurned = totalCaloriesBurned;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public double getHighestSpeed() {
        return highestSpeed;
    }

    public void setHighestSpeed(double highestSpeed) {
        this.highestSpeed = highestSpeed;
    }

    public double getTotalCaloriesBurned() {
        return totalCaloriesBurned;
    }

    public void setTotalCaloriesBurned(double totalCaloriesBurned) {
        this.totalCaloriesBurned = totalCaloriesBurned;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(int totalRuns) {
        this.totalRuns = totalRuns;
    }
}
