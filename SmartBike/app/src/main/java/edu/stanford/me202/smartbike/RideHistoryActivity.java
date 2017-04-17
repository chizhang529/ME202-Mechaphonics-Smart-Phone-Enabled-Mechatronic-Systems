package edu.stanford.me202.smartbike;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RideHistoryActivity extends AppCompatActivity {

    @BindView(R.id.rideHistory)
    RecyclerView rideHistoryList;

    private ArrayList<Ride> rideHistory = new ArrayList<>();
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);
        ButterKnife.bind(this);

        Ride r1 = new Ride(R.drawable.losangeles, "Los Angeles", calendar);
        Ride r2 = new Ride(R.drawable.sanfrancisco, "San Francisco", calendar);
        rideHistory.add(r1);
        rideHistory.add(r2);
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

        RideHistoryListAdapter adapter = new RideHistoryListAdapter(this, rideHistory);
        rideHistoryList.setAdapter(adapter);

    }
}
