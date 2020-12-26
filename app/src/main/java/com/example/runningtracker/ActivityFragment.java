package com.example.runningtracker;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;

public class ActivityFragment extends Fragment implements View.OnClickListener, RecyclerViewClickInterface{

    StatsOverviewSliderAdapter statsOverviewSliderAdapter;
    ViewPager2 statsOverviewViewPager;
    RecyclerView activityList;
    LinearLayout dotsLayout;
    TextView[] dots;
    ImageButton previousBtn, nextBtn;
    FloatingActionMenu sortByButton;
    FloatingActionButton sortByDateButton, sortByDistanceButton;
    int currentPage;

    private ActivityFragment.ActivityFragmentListener listener;

    /* Interface for passing the values or triggers an event when the buttons are clicked */
    public interface ActivityFragmentListener {
        void onActivityClicked(int activityID);
        void onSortByDateClicked();
        void onSortByDistanceClicked();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);
        initialiseComponents(view);
        return view;
    }

    /* Initialise the components of the layout */
    private void initialiseComponents(View view){
        statsOverviewViewPager = view.findViewById(R.id.statsOverview);
        dotsLayout = view.findViewById(R.id.dotsLayout);
        activityList = view.findViewById(R.id.activityList);
        previousBtn = view.findViewById(R.id.previousButton);
        nextBtn = view.findViewById(R.id.nextButton);
        sortByButton = view.findViewById(R.id.sortByFloatingButton);
        sortByDateButton = view.findViewById(R.id.sortByDateFloatingButton);
        sortByDistanceButton = view.findViewById(R.id.sortByDistanceFloatingButton);
        sortByDistanceButton.setOnClickListener(this);
        sortByDateButton.setOnClickListener(this);
        previousBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        addDotsIndicator();
        statsOverviewViewPager.registerOnPageChangeCallback(callback);
    }

    /* Set the slider adapter when MainActivity has completed the query */
    public void retrieveStatsOverviewList(ArrayList<StatsOverviewModel> statsOverviewModelList){
        statsOverviewSliderAdapter = new StatsOverviewSliderAdapter(statsOverviewModelList);
        statsOverviewViewPager.setAdapter(statsOverviewSliderAdapter);
    }

    /* Function that adds dots indicator to the layout */
    public void addDotsIndicator(){
        /* Remove any existing dots to avoid duplicating the number of dots */
        dotsLayout.removeAllViews();
        /* Create 3 dots for AlL Time, This Month and Today's stats overview */
        dots = new TextView[3];
        for (int i = 0; i < dots.length; i++){
            dots[i] = new TextView(getContext());
            dots[i].setText(Html.fromHtml("&#8226", HtmlCompat.FROM_HTML_MODE_LEGACY));
            dots[i].setTextSize(28);
            dotsLayout.addView(dots[i]);
        }
        /* Set the first dot as black colour */
        dots[0].setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
    }

    ViewPager2.OnPageChangeCallback callback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageScrollStateChanged(int state) {}

        @Override
        public void onPageSelected(int position) {

            currentPage = position;

            /* Loop through the dots and set the active page dots as black while others non-active page dots as default colour */
            for (int i = 0; i < dots.length; i++) {
                if (i == position){
                    dots[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                }
                else{
                    dots[i].setTextColor(ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text));
                }
            }

            /* Disable and hide the previous button if it is on the first page */
            if (position == 0){
                previousBtn.setEnabled(false);
                previousBtn.setVisibility(View.INVISIBLE);
                nextBtn.setEnabled(true);
                nextBtn.setVisibility(View.VISIBLE);
            }
            /* Disable and hide the next button if it is on the last page */
            else if (position == dots.length -1){
                previousBtn.setEnabled(true);
                previousBtn.setVisibility(View.VISIBLE);
                nextBtn.setEnabled(false);
                nextBtn.setVisibility(View.INVISIBLE);
            }
            /* Enabled and show both buttons if it is neither first nor last page */
            else{
                previousBtn.setEnabled(true);
                previousBtn.setVisibility(View.VISIBLE);
                nextBtn.setEnabled(true);
                nextBtn.setVisibility(View.VISIBLE);
            }

        }
    };

    /* Set the recycler view adapter when MainActivity has completed the query */
    public void retrieveActivityList(ArrayList<ActivityModel> activityModelList){
        ActivityListAdapter recipesListAdapter = new ActivityListAdapter(activityModelList, this);
        activityList.setAdapter(recipesListAdapter);
        activityList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.sortByDateFloatingButton){
            /* Call sort by date function of the class that implemented the interface (MainActivity) and close the fab menu */
            listener.onSortByDateClicked();
            sortByButton.close(true);
        }
        else if (id == R.id.sortByDistanceFloatingButton){
            /* Call sort by distance function of the class that implemented the interface (MainActivity) and close the fab menu */
            listener.onSortByDistanceClicked();
            sortByButton.close(true);

        }
        else if (id == R.id.previousButton){
            /* Sets the current page to current page-1 if previous button is clicked */
            statsOverviewViewPager.setCurrentItem(--currentPage);
        }
        else if (id == R.id.nextButton){
            /* Sets the current page to current page+1 if next button is clicked */
            statsOverviewViewPager.setCurrentItem(++currentPage);
        }
    }

    /* Send activity ID to the class that implemented the interface (MainActivity) */
    @Override
    public void onActivityClicked(int activityID) {
        listener.onActivityClicked(activityID);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        /* Check if MainActivity implemented ActivityFragmentListener interface when the fragment is attached to the class that implemented the interface (MainActivity) */
        if (context instanceof ActivityFragment.ActivityFragmentListener){
            listener = (ActivityFragment.ActivityFragmentListener) context;
        }
        else{
            throw new RuntimeException(context.toString() + " must implement ActivityFragmentListener.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}