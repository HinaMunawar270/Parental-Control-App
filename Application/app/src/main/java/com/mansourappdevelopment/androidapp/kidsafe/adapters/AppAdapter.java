package com.mansourappdevelopment.androidapp.kidsafe.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mansourappdevelopment.androidapp.kidsafe.R;
import com.mansourappdevelopment.androidapp.kidsafe.interfaces.OnAppClickListener;
import com.mansourappdevelopment.androidapp.kidsafe.models.App;
import com.mansourappdevelopment.androidapp.kidsafe.utils.BackgroundGenerator;

import java.util.ArrayList;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppAdapterViewHolder> {
	private static final String TAG = "AppAdapterTAG";
	private Context context;
	private ArrayList<App> apps;
	private OnAppClickListener onAppClickListener;

	
	public AppAdapter(Context context, ArrayList<App> apps) {
		this.context = context;
		this.apps = apps;
	}
	
	public void setOnAppClickListener(OnAppClickListener onAppClickListener) {
		this.onAppClickListener = onAppClickListener;
	}
	
	@NonNull
	@Override
	public AppAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_app, viewGroup, false);
		return new AppAdapterViewHolder(view);
	}
	
	@Override
	public void onBindViewHolder(@NonNull AppAdapterViewHolder appAdapterViewHolder, int i) {
		App app = apps.get(i);
		if (app != null) {
			appAdapterViewHolder.txtAppName.setText(app.getAppName());
			int hr=0;
			if (hr>3){
				hr = 4;
			}
			appAdapterViewHolder.txtAppUsage.setText(hr+":"+app.getMinutes()+":"+app.getSeconds());
			appAdapterViewHolder.switchAppState.setChecked(app.isBlocked());
			appAdapterViewHolder.txtAppBackground.setText(BackgroundGenerator.getFirstCharacters(app.getAppName()));
			appAdapterViewHolder.txtAppBackground.setBackground(BackgroundGenerator.getBackground(context));
			long secondUsage = getTotalUsageTimeInSeconds(hr, app.getMinutes(), app.getSeconds());

			appAdapterViewHolder.progressBar.setProgress((int) secondUsage);
		}
	}
	
	@Override
	public int getItemCount() {
		return apps.size();
	}
	
	public class AppAdapterViewHolder extends RecyclerView.ViewHolder {
		private TextView txtAppBackground;
		private TextView txtAppName;
		private TextView txtAppUsage;
		private Switch switchAppState;
		private ProgressBar progressBar;
		
		public AppAdapterViewHolder(@NonNull View itemView) {
			super(itemView);
			txtAppBackground = itemView.findViewById(R.id.txtAppBackground);
			txtAppName = itemView.findViewById(R.id.txtAppName);
			txtAppUsage = itemView.findViewById(R.id.text_statusTime);
			switchAppState = itemView.findViewById(R.id.switchAppState);
			progressBar = itemView.findViewById(R.id.progress);
			switchAppState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (buttonView.isPressed()) {
						onAppClickListener.onItemClick(apps.get(getAdapterPosition()).getPackageName(), apps.get(getAdapterPosition()).getAppName(), isChecked); //changed from txtAppName.getText()
						Log.i(TAG, "onCheckedChanged: packageName: " + apps.get(getAdapterPosition()).getPackageName());
						Log.i(TAG, "onCheckedChanged: appName: " + apps.get(getAdapterPosition()).getAppName());
					}
					
				}
			});
		}
	}
	public long getTotalUsageTimeInSeconds(int hours, int minutes, int seconds) {
		return (hours * 3600) + (minutes * 60) + seconds;
	}

}
