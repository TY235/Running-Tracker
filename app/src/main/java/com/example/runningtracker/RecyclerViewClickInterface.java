package com.example.runningtracker;

/* Interface for activity list recycler view adapter to pass activity ID back to activity fragment */
public interface RecyclerViewClickInterface {
    void onActivityClicked(int activityID);
}
