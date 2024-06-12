package com.mansourappdevelopment.androidapp.kidsafe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mansourappdevelopment.androidapp.kidsafe.R;
import com.mansourappdevelopment.androidapp.kidsafe.dialogfragments.InformationDialogFragment;
import com.mansourappdevelopment.androidapp.kidsafe.dialogfragments.PasswordValidationDialogFragment;
import com.mansourappdevelopment.androidapp.kidsafe.dialogfragments.PermissionExplanationDialogFragment;
import com.mansourappdevelopment.androidapp.kidsafe.interfaces.OnPasswordValidationListener;
import com.mansourappdevelopment.androidapp.kidsafe.interfaces.OnPermissionExplanationListener;
import com.mansourappdevelopment.androidapp.kidsafe.services.MainForegroundService;
import com.mansourappdevelopment.androidapp.kidsafe.utils.Constant;
import com.mansourappdevelopment.androidapp.kidsafe.utils.SharedPrefsUtils;
import com.mansourappdevelopment.androidapp.kidsafe.utils.Validators;

import java.util.ArrayList;

public class ChildSignedInActivity extends AppCompatActivity implements OnPermissionExplanationListener, OnPasswordValidationListener {
	public static final int JOB_ID = 38;
	public static final String CHILD_EMAIL = "childEmail";
	private static final String TAG = "ChildSignedInTAG";
	private FirebaseAuth auth;
	private FirebaseUser user;
	private ImageButton btnBack;
	private ImageButton btnSettings;
	private TextView txtTitle;
	private FrameLayout toolbar;
	private static ArrayList<String> blockedWebsites;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_child_signed_in);
		
		boolean childFirstLaunch = SharedPrefsUtils.getBooleanPreference(this, Constant.CHILD_FIRST_LAUNCH, true);
		if (childFirstLaunch) startActivity(new Intent(this, PermissionsActivity.class));
		else {
			
			auth = FirebaseAuth.getInstance();
			user = auth.getCurrentUser();
			
			String email = user.getEmail();
            /*PersistableBundle bundle = new PersistableBundle();
            bundle.putString(CHILD_EMAIL, email);*/
			
			toolbar = findViewById(R.id.toolbar);
			btnBack = findViewById(R.id.btnBack);
			btnBack.setImageDrawable(getResources().getDrawable(R.drawable.ic_home_));
			btnSettings = findViewById(R.id.btnSettings);
			btnSettings.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startPasswordValidationDialogFragment();
				}
			});
			txtTitle = findViewById(R.id.txtTitle);
			txtTitle.setText(getString(R.string.home));
			
			//schedualJob(bundle);
			startMainForegroundService(email);
			
			if (!Validators.isLocationOn(this)) startPermissionExplanationDialogFragment();
			
			if (!Validators.isInternetAvailable(this))
				startInformationDialogFragment(getResources().getString(R.string.you_re_offline_ncheck_your_connection_and_try_again));
			
		}
		try {
			fetchBlockedWebsites(auth.getUid());
		}catch (Exception ex){}	}
	
	private void startMainForegroundService(String email) {
		Intent intent = new Intent(this, MainForegroundService.class);
		intent.putExtra(CHILD_EMAIL, email);
		ContextCompat.startForegroundService(this, intent);
		
	}
	
	private void startPermissionExplanationDialogFragment() {
		PermissionExplanationDialogFragment permissionExplanationDialogFragment = new PermissionExplanationDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(Constant.PERMISSION_REQUEST_CODE, Constant.CHILD_LOCATION_PERMISSION_REQUEST_CODE);
		permissionExplanationDialogFragment.setArguments(bundle);
		permissionExplanationDialogFragment.setCancelable(false);
		permissionExplanationDialogFragment.show(getSupportFragmentManager(), Constant.PERMISSION_EXPLANATION_FRAGMENT_TAG);
	}
	
	private void startInformationDialogFragment(String message) {
		InformationDialogFragment informationDialogFragment = new InformationDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constant.INFORMATION_MESSAGE, message);
		informationDialogFragment.setArguments(bundle);
		informationDialogFragment.setCancelable(false);
		informationDialogFragment.show(getSupportFragmentManager(), Constant.INFORMATION_DIALOG_FRAGMENT_TAG);
	}
	
	private void startPasswordValidationDialogFragment() {
		PasswordValidationDialogFragment passwordValidationDialogFragment = new PasswordValidationDialogFragment();
		passwordValidationDialogFragment.setCancelable(false);
		passwordValidationDialogFragment.show(getSupportFragmentManager(), Constant.PASSWORD_VALIDATION_DIALOG_FRAGMENT_TAG);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constant.DEVICE_ADMIN_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Log.i(TAG, "onActivityResult: DONE");
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	public void onOk(int requestCode) {
		startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	}
	
	@Override
	public void onCancel(int switchId) {
		Toast.makeText(this, getString(R.string.canceled), Toast.LENGTH_SHORT).show();
		
	}
	
	@Override
	public void onValidationOk() {
		Intent intent = new Intent(ChildSignedInActivity.this, SettingsActivity.class);
		startActivity(intent);
	}

	public void goToWebView(View view) {
		String url;
		TextView textView = findViewById(R.id.url_txt);

		url = textView.getText().toString();

		String urlTemp = url;
		if (url.isEmpty()){
			Toast.makeText(this, "Enter Text to search", Toast.LENGTH_SHORT).show();

		}else {
			if (isContain(urlTemp.toLowerCase())){
				Toast.makeText(this, "The Website has blocked", Toast.LENGTH_SHORT).show();

			}else {

					Intent intent = new Intent(ChildSignedInActivity.this, WebViewActivity.class);
					intent.putExtra("Url", url);
					intent.putExtra("Web", blockedWebsites);
					startActivity(intent);

			}

		}




	}

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void schedualJob(PersistableBundle bundle) {
//        ComponentName componentName = new ComponentName(this, UploadAppsService.class);
//        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, componentName)
//                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
//                .setPersisted(true)
//                .setPeriodic(15 * 60 * 1000)
//                .setExtras(bundle)
//                .build();
//        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
//        int resultCode = jobScheduler.schedule(jobInfo);
//
//        if (resultCode == JobScheduler.RESULT_SUCCESS) {
//            //Success
//        } else {
//            //Failure
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void cancelJob() {
//        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
//        jobScheduler.cancel(JOB_ID);
//        //Job cancelled
//    }
//

	private void fetchBlockedWebsites(String childUid) {
		DatabaseReference blockWebsitesRef = FirebaseDatabase.getInstance().getReference().child("users").child("childs").child(childUid).child("blockWebsites");
		blockWebsitesRef.addListenerForSingleValueEvent(new ValueEventListener() {

			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					blockedWebsites = new ArrayList<>();
					for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
						String website = childSnapshot.getValue(String.class);
						blockedWebsites.add(website.toLowerCase());
					}


				} else {
					// No blocked websites found
//				Toast.makeText(ChildSignedInActivity.this, "No blocked websites found", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				// Handle error
//			Toast.makeText(getContext(), "Error fetching blocked websites", Toast.LENGTH_SHORT).show();
			}
		});
	}
	public static boolean isContain(String url) {
		for (String url1: blockedWebsites){
			System.out.println("DATATATTA"+ url1+"\n"+url);
		 	if (url1.contains(url) || url.contains(url1)  ){
				System.out.println("DAARARRARAR"+ url);
				return true;
			}
		}
		return  false;
	}
}
