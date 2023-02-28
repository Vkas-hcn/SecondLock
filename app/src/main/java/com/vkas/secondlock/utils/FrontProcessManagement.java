package com.vkas.secondlock.utils;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Calendar;

public class FrontProcessManagement {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public static String getForegroundPackageName(Context context) {
        //Get the app record in the last month
        Calendar calendar = Calendar.getInstance();
        final long end = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, -1);
        final long start = calendar.getTimeInMillis();

        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        UsageEvents usageEvents = usageStatsManager.queryEvents(start, end);
        UsageEvents.Event event = new UsageEvents.Event();
        String packageName = null;
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                packageName = event.getPackageName();
            }
        }
        return packageName;
    }
}
