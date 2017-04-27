package edu.stanford.me202.smartbike;

import android.content.DialogInterface;
import android.media.Image;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmObject;

public class RideHistoryActivity extends AppCompatActivity {

    @BindView(R.id.rideHistory)
    RecyclerView rideHistoryList;
    @BindView(R.id.locationText)
    EditText rideLocation;
    @BindView(R.id.userAvatar)
    ImageView userAvatar;
    @BindView(R.id.addRide_btn)
    Button addRideBtn;

    private Realm realm;
    private Calendar calendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a", Locale.US);
    private String dateToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);
        // butterknife
        ButterKnife.bind(this);
        // realm DB
        realm = Realm.getDefaultInstance();

        // default settings (hardcode for now), will be added to Realm and show up
        // when Realm initially has no objects
        Ride r1 = new Ride(R.drawable.bluetooth, "Los Angeles", "07/01/2017 09:07:21 AM");
        Ride r2 = new Ride(R.drawable.bluetooth, "San Francisco", "02/04/2016 10:20:30 PM");
        // modification of realm must happen in transaction but read-only does not require transaction
        realm.beginTransaction();
        if (realm.isEmpty()) {
            realm.copyToRealm(r1);
            realm.copyToRealm(r2);
        }
        realm.commitTransaction();

        Picasso.with(this)
                .load(R.drawable.auto)
                .transform(new CircleTransform())
                .into(userAvatar);
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

        final RideHistoryListAdapter adapter = new RideHistoryListAdapter(this);
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

                            // delete data from Realm
                            realm.beginTransaction();
                            Ride ride2delete = realm.where(Ride.class)
                                    .equalTo("date", adapter.getResults().get(position).getDate())
                                    .findFirst();
                            ride2delete.deleteFromRealm(ride2delete);
                            realm.commitTransaction();

                            // update recycler view
                            adapter.notifyDataSetChanged();
                            return;
                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {  //not removing items if cancel is done
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing, just update recycler view
                            adapter.notifyDataSetChanged();
                            return;
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
                String locationStr = rideLocation.getText().toString();
                // update time
                calendar = Calendar.getInstance();
                dateToday = dateFormat.format(calendar.getTime());

                if (locationStr.isEmpty()) {
                    Toast toast1 = Toast.makeText(RideHistoryActivity.this, "Error: location CANNOT be empty!", Toast.LENGTH_SHORT);
                    toast1.show();
                } else {
                    // add new ride to realm
                    Ride newRide = new Ride(R.drawable.unknownlocation, locationStr, dateToday);
                    realm.beginTransaction();
                    realm.copyToRealm(newRide);
                    realm.commitTransaction();

                    // show error message
                    Toast toast2 = Toast.makeText(RideHistoryActivity.this, "Hooray! Added a new ride!", Toast.LENGTH_SHORT);
                    toast2.show();

                    // clear user input
                    rideLocation.setText("");
                }

                // update Recycler view
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // close realm instance
        realm.close();
    }

}
