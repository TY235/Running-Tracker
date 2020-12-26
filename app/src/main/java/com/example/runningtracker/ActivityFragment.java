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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activity, container, false);
        initialiseComponents(view);

        return view;
    }

    private void initialiseComponents(View view){
        statsOverviewViewPager = view.findViewById(R.id.statsOverview);
        dotsLayout = view.findViewById(R.id.dotsLayout);
        activityList = view.findViewById(R.id.activityList);
        previousBtn = view.findViewById(R.id.previousButton);
        nextBtn = view.findViewById(R.id.nextButton);
        sortByButton = view.findViewById(R.id.sortByFloatingButton);
        sortByDateButton = view.findViewById(R.id.sortByDateFloatingButton);
        sortByDistanceButton = view.findViewById(R.id.sortByDistanceFloatingButton);
        addDotsIndicator();
        sortByDistanceButton.setOnClickListener(this);
        sortByDateButton.setOnClickListener(this);
        previousBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        statsOverviewViewPager.registerOnPageChangeCallback(callback);
    }



    public void retrieveStatsOverviewList(ArrayList<StatsOverviewModel> statsOverviewModelList){
        statsOverviewSliderAdapter = new StatsOverviewSliderAdapter(statsOverviewModelList);
        statsOverviewViewPager.setAdapter(statsOverviewSliderAdapter);
    }

    public void addDotsIndicator(){
        dotsLayout.removeAllViews();
        dots = new TextView[3];
        for (int i = 0; i < dots.length; i++){
            dots[i] = new TextView(getContext());
            dots[i].setText(Html.fromHtml("&#8226", HtmlCompat.FROM_HTML_MODE_LEGACY));
            dots[i].setTextSize(28);
            dotsLayout.addView(dots[i]);
        }
        dots[0].setTextColor(ContextCompat.getColor(getContext(), R.color.black));
    }

    ViewPager2.OnPageChangeCallback callback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onPageSelected(int position) {

            currentPage = position;

            for (int i = 0; i < dots.length; i++) {
                if (i == position){
                    dots[i].setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                }
                else{
                    dots[i].setTextColor(ContextCompat.getColor(getContext(), android.R.color.tab_indicator_text));
                }
            }

            if (position == 0){
                previousBtn.setEnabled(false);
                previousBtn.setVisibility(View.INVISIBLE);
                nextBtn.setEnabled(true);
                nextBtn.setVisibility(View.VISIBLE);
            }
            else if (position == dots.length -1){
                previousBtn.setEnabled(true);
                previousBtn.setVisibility(View.VISIBLE);
                nextBtn.setEnabled(false);
                nextBtn.setVisibility(View.INVISIBLE);
            }
            else{
                previousBtn.setEnabled(true);
                previousBtn.setVisibility(View.VISIBLE);
                nextBtn.setEnabled(true);
                nextBtn.setVisibility(View.VISIBLE);
            }

        }
    };

    public void retrieveActivityList(ArrayList<ActivityModel> activityModelList){
        ActivityListAdapter recipesListAdapter = new ActivityListAdapter(activityModelList, this);
        activityList.setAdapter(recipesListAdapter);
        activityList.setLayoutManager(new LinearLayoutManager(getContext()));
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.sortByDateFloatingButton){
            listener.onSortByDateClicked();
            sortByButton.close(true);
        }
        else if (id == R.id.sortByDistanceFloatingButton){
            listener.onSortByDistanceClicked();
            sortByButton.close(true);

        }
        else if (id == R.id.previousButton){
            statsOverviewViewPager.setCurrentItem(--currentPage);
        }
        else if (id == R.id.nextButton){
            statsOverviewViewPager.setCurrentItem(++currentPage);
        }
    }

    @Override
    public void onActivityClicked(int activityID) {
        listener.onActivityClicked(activityID);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Check if MainActivity implemented ActivityFragmentListener interface
        if (context instanceof ActivityFragment.ActivityFragmentListener){
            listener = (ActivityFragment.ActivityFragmentListener) context;
        }
        else{
            throw new RuntimeException(context.toString() + " must implement ActivityFragmentListener.");
        }
    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//        listener = null;
//    }
}