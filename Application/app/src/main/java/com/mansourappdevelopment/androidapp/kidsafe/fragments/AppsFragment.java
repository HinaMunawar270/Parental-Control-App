package com.mansourappdevelopment.androidapp.kidsafe.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mansourappdevelopment.androidapp.kidsafe.R;
import com.mansourappdevelopment.androidapp.kidsafe.activities.ParentSignedInActivity;
import com.mansourappdevelopment.androidapp.kidsafe.adapters.AppAdapter;
import com.mansourappdevelopment.androidapp.kidsafe.dialogfragments.AppLockDialogFragment;
import com.mansourappdevelopment.androidapp.kidsafe.interfaces.OnAppClickListener;
import com.mansourappdevelopment.androidapp.kidsafe.models.App;
import com.mansourappdevelopment.androidapp.kidsafe.models.ScreenLock;
import com.mansourappdevelopment.androidapp.kidsafe.utils.Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import static com.mansourappdevelopment.androidapp.kidsafe.activities.ParentSignedInActivity.CHILD_EMAIL_EXTRA;

public class AppsFragment extends Fragment implements OnAppClickListener {
	public static final String TAG = "AppsFragmentTAG";
	private FirebaseDatabase firebaseDatabase;
	private DatabaseReference databaseReference;
	private ArrayList<App> apps;
	private AppAdapter appAdapter;
	private RecyclerView recyclerViewApps;
	private Context context;
	private String appName;
	private String packageName;
	private String childEmail;
	private Bundle bundle;


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_apps, container, false);
		// Find the BarChart view by its ID
		BarChart barChart = view.findViewById(R.id.chart1);

// Sample data (replace with your actual data)
		String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
		int[] screenUsage = {23000, 11000, 66000, 12000, 11030, 34000, 34000};

// Create a list of BarEntries (x-index, value)
		ArrayList<BarEntry> entries = new ArrayList<>();
		for (int i = 0; i < days.length; i++) {
			entries.add(new BarEntry(i, screenUsage[i]));
		}

// Create a BarDataSet from the entries
		BarDataSet dataSet = new BarDataSet(entries, "Screen Usage");

// Customize the appearance of the chart
		BarData barData = new BarData(dataSet);
		barChart.setData(barData);

// Customize the X-axis labels
		XAxis xAxis = barChart.getXAxis();
		xAxis.setValueFormatter(new IndexAxisValueFormatter(days));

// Refresh the chart
		barChart.invalidate();

		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		context = getContext();
		firebaseDatabase = FirebaseDatabase.getInstance();
		databaseReference = firebaseDatabase.getReference("users");
		
		
		recyclerViewApps = view.findViewById(R.id.recyclerViewApps);
		recyclerViewApps.setHasFixedSize(true);
		recyclerViewApps.setLayoutManager(new LinearLayoutManager(getContext()));
		
		getData();
		initializeAdapter(this);

		
	}
	
	public void getData() {
		bundle = getActivity().getIntent().getExtras();
		if (bundle != null) {
			apps = bundle.getParcelableArrayList(ParentSignedInActivity.APPS_EXTRA);
			childEmail = bundle.getString(CHILD_EMAIL_EXTRA);
		}
//		DatabaseReference screenUsageRef = databaseReference.child("childs").child(childEmail).child("ScreenUse");  // Change this path to match your Firebase structure
//		screenUsageRef.addListenerForSingleValueEvent(new ValueEventListener() {
//			@Override
//			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//				if (dataSnapshot.exists()) {
//					String day[] = new String[7];
//					long usageTimeArray[] = new long[7];
//					int i= 0;
//					// Loop through the dataSnapshot and retrieve screen usage data for each day
//					for (DataSnapshot daySnapshot : dataSnapshot.getChildren()) {
//						try {
//							day[i] = daySnapshot.getKey();
//							long usageTime = daySnapshot.getValue(Long.class);
//							usageTimeArray[i] = secondsToMinutes(usageTime);
//							i++;
//
//						}catch (Exception ex){}
//
//
//						// Store the usageTime in your screenUsage array or data structure
//						// You can convert the usageTime from milliseconds to minutes or hours if needed
//					}
//
//					// Update your chart with the retrieved screen usage data
//					updateScreenUsageChart(day, usageTimeArray);
//				}
//			}
//
//			@Override
//			public void onCancelled(@NonNull DatabaseError databaseError) {
//				// Handle the error if needed
//			}
//		});
		DatabaseReference screenUsageRef = databaseReference.child("childs");

		Query query = screenUsageRef.orderByChild("email").equalTo(childEmail);
		query.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					DataSnapshot childNodeShot = dataSnapshot.getChildren().iterator().next();
					String childKey = childNodeShot.getKey();
					DatabaseReference screenUsageRef = databaseReference.child("childs").child(childKey).child("ScreenUse");

					screenUsageRef.addListenerForSingleValueEvent(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
							if (dataSnapshot.exists()) {
								String day[] = new String[7];
					long usageTimeArray[] = new long[7];
					int i= 0;
					// Loop through the dataSnapshot and retrieve screen usage data for each day
					for (DataSnapshot daySnapshot : dataSnapshot.getChildren()) {
						try {
							day[i] = daySnapshot.getKey();
							long usageTime = daySnapshot.getValue(Long.class);
							usageTimeArray[i] = secondsToMinutes(usageTime);
							i++;

						}catch (Exception ex){}


						// Store the usageTime in your screenUsage array or data structure
						// You can convert the usageTime from milliseconds to minutes or hours if needed
					}

					// Update your chart with the retrieved screen usage data
					updateScreenUsageChart(day, usageTimeArray);
							}
						}

						@Override
						public void onCancelled(@NonNull DatabaseError databaseError) {
							// Handle the error if needed
						}
					});
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				// Handle the error if needed
			}
		});


//		ArrayList<App> appList = new ArrayList<>();
//		// Add App objects to appList

		// Sort the appList based on the total usage time in seconds
		Collections.sort(apps, new Comparator<App>() {
			@Override
			public int compare(App app1, App app2) {
				long totalTime1 = app1.getTotalUsageTimeInSeconds();
				long totalTime2 = app2.getTotalUsageTimeInSeconds();
				return Long.compare(totalTime2, totalTime1);
			}
		});


	}
	private void updateScreenUsageChart(String[] day, long[] usageTimeArray) {
		BarChart barChart = getView().findViewById(R.id.chart1);

		ArrayList<BarEntry> entries = new ArrayList<>();
		for (int i = 0; i < day.length; i++) {
			entries.add(new BarEntry(i, usageTimeArray[i]));
		}

		BarDataSet dataSet = new BarDataSet(entries, "Screen Usage/min");
		BarData barData = new BarData(dataSet);
		barChart.setData(barData);

		XAxis xAxis = barChart.getXAxis();
		xAxis.setValueFormatter(new IndexAxisValueFormatter(day));
		barChart.invalidate();
	}

	public void initializeAdapter(OnAppClickListener onAppClickListener) {
		appAdapter = new AppAdapter(context, apps);
		appAdapter.setOnAppClickListener(onAppClickListener);
		recyclerViewApps.setAdapter(appAdapter);
	}
	
	@Override
	public void onItemClick(final String packageName, String appName, boolean blocked) {
		if (blocked) {
			Toast.makeText(context, appName + " " + "blocked", Toast.LENGTH_SHORT).show();
			updateAppState(packageName, blocked);
			
		} else {
			Toast.makeText(context, appName + " enabled", Toast.LENGTH_SHORT).show();
			updateAppState(packageName, blocked);
			
		}
        this.appName = appName;
        this.packageName = packageName;

        if (blocked) {
            Bundle bundle = new Bundle();
            bundle.putString(Constant.CHILD_APP_NAME_EXTRA, appName);

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            AppLockDialogFragment appLockFragment = new AppLockDialogFragment();
            appLockFragment.setCancelable(false);
            appLockFragment.setArguments(bundle);
            appLockFragment.setTargetFragment(AppsFragment.this, Constant.APP_LOCK_FRAGMENT_REQUEST_CODE);
            appLockFragment.show(fragmentManager, "AppLockDialogFragment");
        } else {
            Toast.makeText(context, appName + " " +"enabled", Toast.LENGTH_SHORT).show();
            final ScreenLock screenLock = new ScreenLock(0, 0, false);
            updateAppLockState(screenLock);
        }
	}


    @Override
    public void onLockAppSet(int hours, int minutes) {
        if (hours == 0 && minutes == 0) {
            Toast.makeText(context, appName + " " + getString(R.string.blocked), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, appName + " " + getString(R.string.will_be_blocked_after) + " " + hours + " " + getString(R.string.hours) + " "
                    + getString(R.string.and) + " " + minutes + " " + getString(R.string.minutes), Toast.LENGTH_SHORT).show();

        }

        ScreenLock screenLock = new ScreenLock(hours, minutes, true);
        updateAppLockState(screenLock);


    }

    @Override
    public void onLockCanceled() {
        Toast.makeText(context, getString(R.string.canceled), Toast.LENGTH_SHORT).show();
        initializeAdapter(this);

    }


    private void updateAppLockState(final ScreenLock screenLock) {
        Query childQuery = databaseReference.child("childs").orderByChild("email").equalTo(childEmail);
        childQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: DONE");
                if (dataSnapshot.exists()) {
                    DataSnapshot childNodeShot = dataSnapshot.getChildren().iterator().next();
                    final String childKey = childNodeShot.getKey();
                    Log.i(TAG, "onDataChange: childKey: " + childKey);
                    Log.i(TAG, "onDataChange: packageName: " + packageName);
                    Query appQuery = databaseReference.child("childs").child(childKey).child("apps").orderByChild("packageName").equalTo(packageName);  //changed from appName
                    appQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.i(TAG, "onDataChange: DONE 2");
                            Log.i(TAG, "onDataChange: dataSnapShotValue: " + dataSnapshot.getValue());
                            Log.i(TAG, "onDataChange: dataSnapShotChildren: " + dataSnapshot.getChildren());
                            if (dataSnapshot.exists()) {
                                DataSnapshot appNodeShot = dataSnapshot.getChildren().iterator().next();
                                String appKey = appNodeShot.getKey();
                                Log.i(TAG, "onDataChange: appKey: " + appKey);
                                Log.i(TAG, "onDataChange: appNodeShotValue: " + appNodeShot.getValue());
                                Log.i(TAG, "onDataChange: appNodeShotChildren: " + appNodeShot.getChildren());

                                databaseReference.child("childs").child(childKey).child("apps").child(appKey).child("screenLock").setValue(screenLock);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
	
	
	private void updateAppState(final String packageName, final boolean blocked) {
		Query query = databaseReference.child("childs").orderByChild("email").equalTo(childEmail);
		query.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				DataSnapshot nodeShot = dataSnapshot.getChildren().iterator().next();
				final String key = nodeShot.getKey();
				Log.i(TAG, "onDataChange: key: " + key);
				Query query = databaseReference.child("childs").child(key).child("apps").orderByChild("packageName").equalTo(packageName);  //changed from appName
				query.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						if (dataSnapshot.exists()) {
							DataSnapshot snapshot = dataSnapshot.getChildren().iterator().next();
							HashMap<String, Object> update = new HashMap<>();
							update.put("blocked", blocked);
							databaseReference.child("childs").child(key).child("apps").child(snapshot.getKey()).updateChildren(update);
						}
					}
					
					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
					
					}
				});
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	public static long secondsToMinutes(long seconds) {
		return seconds / 60;
	}
}
