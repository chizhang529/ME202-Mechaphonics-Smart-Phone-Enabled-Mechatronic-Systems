package edu.stanford.me202.smartbike;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static edu.stanford.me202.smartbike.R.id.parent;

/**
 * Created by czhang on 4/16/17.
 */

public class RideHistoryListAdapter extends RecyclerView.Adapter<RideHistoryListAdapter.rideViewHolder> {

    private Context context;
    private RealmResults<Ride> results;

    // Constructor
    public RideHistoryListAdapter(Context ctx) {
        this.context = ctx;
        try(Realm realm = Realm.getDefaultInstance()) {
            // results are sorted in descending order of date
            results = realm.where(Ride.class).findAll().sort("date", Sort.DESCENDING);
        }
    }


    public static class rideViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ride_icon)
        ImageView icon;
        @BindView(R.id.ride_location)
        TextView location;
        @BindView(R.id.ride_date)
        TextView date;

        public rideViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public rideViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_info, parent, false);
        rideViewHolder rideHistoryHolder = new rideViewHolder(view);
        return rideHistoryHolder;
    }

    @Override
    public void onBindViewHolder(rideViewHolder holder, int position) {
        Ride ride = results.get(position);
        holder.location.setText(ride.getLocation());
        holder.date.setText(ride.getDate());
        Picasso.with(context)
                .load(ride.getIconID())
                .into(holder.icon);
    }

    @Override
    public int getItemCount() {
        // make sure query here to update results
        try(Realm realm = Realm.getDefaultInstance()) {
            results = realm.where(Ride.class).findAll().sort("date", Sort.DESCENDING);
        }
        return results.size();
    }

    public RealmResults<Ride> getResults() {
        return results;
    }
}
