package edu.stanford.me202.smartbike;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class RideHistoryActivity extends AppCompatActivity {
    private final static String TAG = RideHistoryActivity.class.getSimpleName();

    private String currentUid;

    // get recycler view adapter
    private final RideHistoryListAdapter adapter = new RideHistoryListAdapter(this);

    @BindView(R.id.rideHistory)
    RecyclerView rideHistoryList;
    @BindView(R.id.locationText)
    EditText rideLocation;
    @BindView(R.id.userAvatar)
    ImageView userAvatar;
    @BindView(R.id.addRide_btn)
    ImageButton addRideBtn;
    @BindView(R.id.exit_btn)
    ImageButton exitBtn;

    // Initialize FireBase database reference
    private DatabaseReference fireDB;
    private FirebaseAuth fireDBAuth;
    private Realm realm;
    private Calendar calendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a", Locale.US);
    private String dateToday;
    private String timeStamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);
        // butterknife
        ButterKnife.bind(this);
        // realm DB
        realm = Realm.getDefaultInstance();
        // Firebase
        fireDB = FirebaseDatabase.getInstance().getReference();
        fireDBAuth = FirebaseAuth.getInstance();

        // get current user leaf: all rides will be added under this leaf
        currentUid = fireDBAuth.getCurrentUser().getUid();
        Log.d(TAG, currentUid);

        // configure Firebase here
        // pull down all data related to this user from Firebase
        fireDB.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                realm.beginTransaction();
                realm.deleteAll();
                // TODO: How to hide some Realm components
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Ride r = postSnapshot.getValue(Ride.class);
                    realm.copyToRealmOrUpdate(r);
                }

                realm.commitTransaction();
                // update recycler view
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (fireDBAuth.getCurrentUser().getEmail().contentEquals("czhang94@stanford.edu")) {
            Picasso.with(this)
                    .load(R.drawable.czhang)
                    .transform(new CircleTransform())
                    .into(userAvatar);
        } else {
            Picasso.with(this)
                    .load(R.drawable.man)
                    .transform(new CircleTransform())
                    .into(userAvatar);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // create Recycler view here
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rideHistoryList.setLayoutManager(manager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rideHistoryList.getContext(),
                manager.getOrientation());
        rideHistoryList.addItemDecoration(dividerItemDecoration);
        rideHistoryList.setAdapter(adapter);

        // handles user swipes, here only configure it to handle swipe to left motion
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RideHistoryActivity.this);
                builder.setTitle(R.string.deleteRide);
                builder.setIcon(R.drawable.warning);
                builder.setCancelable(false);

                if (direction == ItemTouchHelper.LEFT) {
                    builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() { //when click on DELETE
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int position = viewHolder.getAdapterPosition(); //get position which is swipe

                            Ride ride2delete = realm.where(Ride.class)
                                    .equalTo("timeStamp", adapter.getResults().get(position).getTimeStamp())
                                    .findFirst();
                            // delete data from Firebase
                            fireDB.child(currentUid).child(ride2delete.getTimeStamp()).removeValue();
                            // delete data from Realm
                            realm.beginTransaction();
                            ride2delete.deleteFromRealm();
                            realm.commitTransaction();

                            // update recycler view
                            adapter.notifyDataSetChanged();
                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {  //not removing items if cancel is done
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing, just update recycler view
                            adapter.notifyDataSetChanged();
                        }
                    }).show();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rideHistoryList);


        addRideBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String locationStr = rideLocation.getText().toString().trim();
                // update time
                calendar = Calendar.getInstance();
                dateToday = dateFormat.format(calendar.getTime());
                timeStamp = Long.toString(calendar.getTimeInMillis());

                if (locationStr.isEmpty()) {
                    // show error message
                    Toast toast1 = Toast.makeText(RideHistoryActivity.this, R.string.emptylocationerror, Toast.LENGTH_SHORT);
                    toast1.show();
                } else {
                    int cityIndex = getCityIndex(locationStr);
                    Ride newRide = new Ride(cityIndex, locationStr, dateToday, timeStamp);
                    // add new ride to FireDB
                    fireDB.child(currentUid).child(newRide.getTimeStamp()).setValue(newRide);
                    // add new ride to realm
                    realm.beginTransaction();
                    realm.copyToRealm(newRide);
                    realm.commitTransaction();

                    // show message
                    Toast toast2 = Toast.makeText(RideHistoryActivity.this, R.string.addNewRide, Toast.LENGTH_SHORT);
                    toast2.show();

                    // clear user input
                    rideLocation.setText("");
                }

                // update Recycler view
                adapter.notifyDataSetChanged();
            }
        });

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fireDBAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        if (firebaseAuth.getCurrentUser() == null) {
                            // redirect to log in
                            Intent intent = new Intent(RideHistoryActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });

                fireDBAuth.signOut();

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // close realm instance
        realm.close();
    }

    // a helper funtion for indexing some big cities in the world
    private int getCityIndex(String locationStr) {
        int cityIndex = R.drawable.unknownlocation;

        switch (locationStr) {
            case "New York":
                cityIndex = R.drawable.newyork;
            break;

            case "Barcelona":
                cityIndex = R.drawable.barcelona;
            break;

            case "Beijing":
                cityIndex = R.drawable.beijing;
            break;

            case "London":
                cityIndex = R.drawable.london;
            break;

            case "Moscow":
                cityIndex = R.drawable.moscow;
            break;

            case "Shanghai":
                cityIndex = R.drawable.shanghai;
            break;

            case "Washington":
                cityIndex = R.drawable.washington;
            break;

            case "Tokyo":
                cityIndex = R.drawable.tokyo;
            break;

            default: break;
        }

        return cityIndex;
    }

}
