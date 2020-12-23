package com.example.runningtracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

public final class Utilities {

    public static double convertMtoKM(double meter){
        if (meter == 0){
            return 0;
        }
        return round((meter/1000), 2);
    }

    public static String convertStoHMS(int totalTimeTakenInSeconds){
        int hours = totalTimeTakenInSeconds / 3600;
        int minutes = (totalTimeTakenInSeconds % 3600) / 60;
        int seconds = totalTimeTakenInSeconds % 60;
        if (hours ==0){
            return String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds);
        }
        return String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static double calculatePaceInMinutesPerKM(double totalDistanceTravelledInMeters, int totalTimeTakenInSeconds){
        double pace;
        double totalDistanceTravelledInKM = totalDistanceTravelledInMeters / 1000;
        double totalTimeTakenInMinutes = totalTimeTakenInSeconds / 60.0;
        if (totalTimeTakenInSeconds != 0){
            pace = totalTimeTakenInMinutes / totalDistanceTravelledInKM;
            if (!Double.isNaN(pace) && !Double.isInfinite(pace)){
                return round(pace, 2);
            }
        }
        return 0.00;
    }

    public static double calculateSpeedInMetersPerSecond(double totalDistanceTravelledInMeters, int totalTimeTakenInSeconds){
        double speed;
        if (totalTimeTakenInSeconds != 0){
            speed = totalDistanceTravelledInMeters / totalTimeTakenInSeconds;
            if (!Double.isNaN(speed) && !Double.isInfinite(speed)) {
                return round(speed, 2);
            }
        }
        return 0.00;
    }

    public static double calculateCaloriesBurned(double heightInMeter, double weightInKG, double speedInMetersPerSecond, int totalTimeTakenInSeconds){
        double calories;
        double totalTimeTakenInMinutes;
        if (totalTimeTakenInSeconds != 0){
            totalTimeTakenInMinutes = totalTimeTakenInSeconds/60.0;
            calories =  ((0.035 * weightInKG) + ((speedInMetersPerSecond * speedInMetersPerSecond) / heightInMeter) * (0.029) * (weightInKG)) * totalTimeTakenInMinutes;
            if (!Double.isNaN(calories) && !Double.isInfinite(calories)){
                return round(calories, 1);
            }
        }
        return 0.00;
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /* Bitmap descriptor below is obtained from https://stackoverflow.com/a/45564994 by user Leo Droidcoder */
    /* It is used to convert vector drawable resources to bitmap so that the resources are compatible to be used as map markers icon. */
    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        Objects.requireNonNull(vectorDrawable).setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }
}
