package edu.stanford.me202.smartbike;

import android.content.DialogInterface;
import android.media.Image;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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

    private Realm realm;
   // private ArrayList<Ride> rideHistory = new ArrayList<>();
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private String dateToday = dateFormat.format(calendar.getTime());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);
        ButterKnife.bind(this);
        // realm DB
        realm = Realm.getDefaultInstance();

        // default settings
        Ride r1 = new Ride(R.drawable.losangeles, "Los Angeles", "04/15/2017");
        Ride r2 = new Ride(R.drawable.sanfrancisco, "San Francisco", "04/15/2016");
        // modification of realm must happen in transaction but read-only does not require transaction
        realm.beginTransaction();
        if (realm.isEmpty()) {
            realm.copyToRealm(r1);
            realm.copyToRealm(r2);
        }
        realm.commitTransaction();

        Picasso.with(this)
                .load(R.drawable.czhang)
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

        final RideHistoryListAdapter adapter = new RideHistoryListAdapter(this);
        rideHistoryList.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition(); //get position which is swipe

                if (direction == ItemTouchHelper.LEFT) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RideHistoryActivity.this);
                    builder.setMessage(R.string.deleteRide);
                    builder.setIcon(R.drawable.warning);

                    builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() { //when click on DELETE
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            realm.beginTransaction();
                            Ride ride2delete = realm.where(Ride.class)
                                    .equalTo("location", adapter.getResults().get(position).getLocation())
                                    .findFirst();
                            ride2delete.deleteFromRealm();
                            realm.commitTransaction();

                            RideHistoryListAdapter adapter1 = new RideHistoryListAdapter(RideHistoryActivity.this);
                            rideHistoryList.setAdapter(adapter1);
                            return;
                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {  //not removing items if cancel is done
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            RideHistoryListAdapter adapter2 = new RideHistoryListAdapter(RideHistoryActivity.this);
                            rideHistoryList.setAdapter(adapter2);
                            return;
                        }
                    }).show();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rideHistoryList);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public void addRide(View view) {
        String locationStr = rideLocation.getText().toString();

        if (locationStr.isEmpty()) {
            Toast toast1 = Toast.makeText(this, "Error: location CANNOT be empty!", Toast.LENGTH_SHORT);
            toast1.show();
        } else {
            // add new ride to realm
            Ride newRide = new Ride(R.drawable.unknownlocation, locationStr, dateToday);
            realm.beginTransaction();
            realm.copyToRealm(newRide);
            realm.commitTransaction();

            // show error message
            Toast toast2 = Toast.makeText(this, "Hooray! Added a new ride!", Toast.LENGTH_SHORT);
            toast2.show();

            // clear user input
            rideLocation.setText("");
        }

        RideHistoryListAdapter updateAdapter = new RideHistoryListAdapter(this);
        rideHistoryList.setAdapter(updateAdapter);
    }
}
