package com.mansourappdevelopment.androidapp.kidsafe.fragments;

import static com.mansourappdevelopment.androidapp.kidsafe.activities.ParentSignedInActivity.CHILD_EMAIL_EXTRA;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mansourappdevelopment.androidapp.kidsafe.R;

import java.util.ArrayList;

public class WebFragment extends Fragment {

	public static final String TAG = "WebFragmentTAG";
	private FirebaseDatabase firebaseDatabase;
	private DatabaseReference databaseReference;
	private ListView listViewApps; // Add this declaration
	private Context context;
	private FloatingActionButton floatingActionButton;
	private EditText urlEditText;
	private String childEmail;
	private String childUid;
	private Bundle bundle;



	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_web, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		context = getContext();
		firebaseDatabase = FirebaseDatabase.getInstance();
		databaseReference = firebaseDatabase.getReference("users");

		listViewApps = view.findViewById(R.id.listViewWeb); // Initialize the ListView

		getData();

		floatingActionButton = view.findViewById(R.id.fab_add_web);
		floatingActionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddUrlDialog();
			}
		});
	}

	public void getData() {
		bundle = getActivity().getIntent().getExtras();
		if (bundle != null) {
			 childEmail = bundle.getString(CHILD_EMAIL_EXTRA);
			fetchChildUid(childEmail);
		}

	}

	@SuppressLint("MissingInflatedId")
	private void showAddUrlDialog() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		LayoutInflater inflater = requireActivity().getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_add_url, null);
		dialogBuilder.setView(dialogView);

		urlEditText = dialogView.findViewById(R.id.editTextUrl);

		dialogBuilder.setTitle("Add URL");
		dialogBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String url = urlEditText.getText().toString();
				if (!url.isEmpty()) {
					addUrlToFirebase(url);
					System.out.println("Childdd"+childEmail);

				}
			}
		});
		dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});
		AlertDialog alertDialog = dialogBuilder.create();
		alertDialog.show();
	}

	private void addUrlToFirebase(String url) {
		databaseReference.child("childs").orderByChild("email").equalTo(childEmail).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					DataSnapshot childSnapshot = dataSnapshot.getChildren().iterator().next();
					String childUid = childSnapshot.getKey();
					System.out.println("Childdd" + childEmail + ":" + childUid);

					// Update the blocked websites for the child user
					DatabaseReference blockWebsitesRef = databaseReference.child("childs").child(childUid).child("blockWebsites");
					blockWebsitesRef.push().setValue(url, new DatabaseReference.CompletionListener() {
						@Override
						public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
							if (databaseError == null) {
								// Website is added to blocked list successfully
								Toast.makeText(getContext(), "Website blocked successfully", Toast.LENGTH_SHORT).show();

								// Update the ListView
								fetchBlockedWebsites(childUid);

							} else {
								Toast.makeText(getContext(), "Error blocking website", Toast.LENGTH_SHORT).show();
								// Handle error
							}
						}
					});
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				// Handle error
			}
		});
//		DatabaseReference urlsRef = databaseReference.child("users").child("childs").child(childEmail);
//		String key = urlsRef.push().getKey();
//		urlsRef.child(key).setValue(url);
	}
	private void fetchChildUid(String childEmail) {
		databaseReference.child("childs").orderByChild("email").equalTo(childEmail)
				.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						if (dataSnapshot.exists()) {
							DataSnapshot childSnapshot = dataSnapshot.getChildren().iterator().next();
							childUid = childSnapshot.getKey();
							fetchBlockedWebsites(childUid);
						}
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
						// Handle error
						Toast.makeText(getContext(), "Error fetching child UID", Toast.LENGTH_SHORT).show();
					}
				});
	}

	private void updateListView(DataSnapshot dataSnapshot) {

		ArrayList<String> blockedWebsites = new ArrayList<>();
		for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
			String website = childSnapshot.getValue(String.class);
			blockedWebsites.add(website);
		}

		try {
			ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, blockedWebsites);
			listViewApps.setAdapter(adapter);
		}catch (Exception ex){

		}

		listViewApps.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				String selectedWebsite = blockedWebsites.get(position);
				showDeleteDialog(selectedWebsite, childUid);
				return true;
			}
		});

	}
	private void showDeleteDialog(String website, String childUid) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("Delete Website");
		builder.setMessage("Do you want to delete this website: " + website + "?");
		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteWebsiteFromFirebase(website, childUid);
			}
		});
		builder.setNegativeButton("Cancel", null);
		builder.show();
	}
	private void deleteWebsiteFromFirebase(String website, String childUid) {
		DatabaseReference blockWebsitesRef = databaseReference.child("childs").child(childUid).child("blockWebsites");
		Query websiteQuery = blockWebsitesRef.orderByValue().equalTo(website);
		websiteQuery.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					DataSnapshot websiteSnapshot = dataSnapshot.getChildren().iterator().next();
					websiteSnapshot.getRef().removeValue()
							.addOnSuccessListener(new OnSuccessListener<Void>() {
								@Override
								public void onSuccess(Void aVoid) {
									Toast.makeText(getContext(), "Website deleted successfully", Toast.LENGTH_SHORT).show();
									fetchBlockedWebsites(childUid);
								}
							})
							.addOnFailureListener(new OnFailureListener() {
								@Override
								public void onFailure(@NonNull Exception e) {
									Toast.makeText(getContext(), "Error deleting website", Toast.LENGTH_SHORT).show();
								}
							});
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				// Handle error
				Toast.makeText(getContext(), "Error deleting website", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void fetchBlockedWebsites(String childUid) {
		DatabaseReference blockWebsitesRef = databaseReference.child("childs").child(childUid).child("blockWebsites");
		blockWebsitesRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					updateListView(dataSnapshot);
				} else {
					// No blocked websites found
					Toast.makeText(getContext(), "No blocked websites found", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				// Handle error
				Toast.makeText(getContext(), "Error fetching blocked websites", Toast.LENGTH_SHORT).show();
			}
		});
	}
}
