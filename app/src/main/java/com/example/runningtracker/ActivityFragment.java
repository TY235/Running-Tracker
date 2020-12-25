package com.example.runningtracker;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ActivityFragment extends Fragment {

    StatsOverviewSliderAdapter statsOverviewSliderAdapter;
    ViewPager2 statsOverviewViewPager;
    RecyclerView activityList;
    LinearLayout dotsLayout;
    TextView[] dots;
    ImageButton previousBtn, nextBtn;
    int currentPage;


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
        statsOverviewViewPager = (ViewPager2) view.findViewById(R.id.statsOverview);
        dotsLayout = (LinearLayout) view.findViewById(R.id.dotsLayout);
        activityList = (RecyclerView) view.findViewById(R.id.activityList);
        previousBtn = (ImageButton) view.findViewById(R.id.previousButton);
        nextBtn = (ImageButton) view.findViewById(R.id.nextButton);
        addDotsIndicator();
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statsOverviewViewPager.setCurrentItem(--currentPage);
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statsOverviewViewPager.setCurrentItem(++currentPage);
            }
        });
        statsOverviewViewPager.registerOnPageChangeCallback(callback);
    }
//
//    public void retrieveActivityListData(){
//    }

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
        dots[0].setTextColor(ContextCompat.getColor(getContext(), R.color.maximum_blue_green));
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
                    dots[i].setTextColor(ContextCompat.getColor(getContext(), R.color.maximum_blue_green));
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



}