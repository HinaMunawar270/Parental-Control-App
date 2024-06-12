package com.mansourappdevelopment.androidapp.kidsafe.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mansourappdevelopment.androidapp.kidsafe.models.App;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class UploadAppsUsageService extends JobService {
    public static final String TAG = "UploadAppsUsageService";
    private boolean jobCancelled;

    private HashMap<String, Long> appUsageStats; // For storing app usage statistics (package name and usage time in milliseconds)
    private PackageManager packageManager;
    private DatabaseReference databaseReference;
    private String childEmail;

    @Override
    public boolean onStartJob(JobParameters params) {
        appUsageStats = new HashMap<>(); // Initialize app usage statistics map
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        childEmail = user.getEmail();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("usersTest");
        uploadApps(params); // Call method to upload app usage stats
        return true; // To keep the device awake
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true; // True for rescheduling the job if it failed
    }

    private void uploadApps(JobParameters params) {
        getAppUsageStats(); // Retrieve app usage statistics
        uploadAppUsageStatsToFirebase(params); // Upload app usage stats to Firebase
    }

    private void getAppUsageStats() {
        packageManager = getPackageManager();
        List<ApplicationInfo> applicationInfoList = packageManager.getInstalledApplications(0);
        Collections.sort(applicationInfoList, new ApplicationInfo.DisplayNameComparator(packageManager));
        Iterator<ApplicationInfo> iterator = applicationInfoList.iterator();
        while (iterator.hasNext()) {
            ApplicationInfo applicationInfo = iterator.next();
            if (applicationInfo.packageName.contains("com.google") || applicationInfo.packageName.matches("com.android.chrome")) {
                continue;
            }
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                iterator.remove();
            }
        }

        // Calculate app usage time and store it in appUsageStats map
        for (ApplicationInfo applicationInfo : applicationInfoList) {
            String packageName = applicationInfo.packageName;
            long usageTime = calculateAppUsageTime(packageName); // Implement your method to calculate app usage time
            appUsageStats.put(packageName, usageTime);
        }
    }

    private long calculateAppUsageTime(String packageName) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long currentTime = System.currentTimeMillis();
        long usageTimeInMillis = 0;
        // We'll get app usage stats for the last 24 hours (adjust as needed)
        long startTime = currentTime - (24 * 60 * 60 * 1000);
        List<UsageStats> appUsageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, currentTime);

        if (appUsageStatsList != null) {
            for (UsageStats usageStats : appUsageStatsList) {
                if (usageStats.getPackageName().equals(packageName)) {
                    usageTimeInMillis += usageStats.getTotalTimeInForeground();
                }
            }
        }
        return usageTimeInMillis;
    }

    private void uploadAppUsageStatsToFirebase(JobParameters params) {
        Query query = databaseReference.child("childsTest").orderByChild("email").equalTo(childEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot nodeShot = dataSnapshot.getChildren().iterator().next();
                    String key = nodeShot.getKey();
                    DatabaseReference appsRef = databaseReference.child("childsTest").child(key).child("apps");

                    for (DataSnapshot appSnapshot : nodeShot.child("apps").getChildren()) {
                        App app = appSnapshot.getValue(App.class);
                        String packageName = app.getPackageName();

                        if (appUsageStats.containsKey(packageName)) {
                            long usageTime = appUsageStats.get(packageName);
                            // Update the app's usage time in the database
                            appsRef.child(appSnapshot.getKey()).child("usageTime").setValue(usageTime);
                        }
                    }
                }
                // Call jobFinished to indicate that the job is complete
                jobFinished(params, false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled if needed
                // Call jobFinished to indicate that the job is complete with a rescheduling request (true)
                jobFinished(params, true);
            }
        });
    }
}
