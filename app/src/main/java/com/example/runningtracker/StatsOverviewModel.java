package com.example.runningtracker;

/* A model class to store the stats overviews */
public class StatsOverviewModel{

    private final double totalDistance, highestSpeed, totalCaloriesBurned;
    private final int totalRuns;

    public StatsOverviewModel(double totalDistance, int totalRuns, double bestSpeed, double totalCaloriesBurned) {
        this.totalDistance = totalDistance;
        this.totalRuns = totalRuns;
        this.highestSpeed = bestSpeed;
        this.totalCaloriesBurned = totalCaloriesBurned;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public double getHighestSpeed() {
        return highestSpeed;
    }

    public double getTotalCaloriesBurned() {
        return totalCaloriesBurned;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

}
