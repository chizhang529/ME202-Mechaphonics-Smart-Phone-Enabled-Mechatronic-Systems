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

import static edu.stanford.me202.smartbike.R.id.parent;

/**
 * Created by czhang on 4/16/17.
 */

public class RideHistoryListAdapter extends RecyclerView.Adapter<RideHistoryListAdapter.rideViewHolder> {

    private ArrayList<Ride> rideHistoryList = new ArrayList<>();
    private Context context;

    // Constructor
    public RideHistoryListAdapter(Context ctx, ArrayList<Ride> rideHistoryList) {
        this.context = ctx;
        this.rideHistoryList = rideHistoryList;
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
        holder.location.setText(rideHistoryList.get(position).getLocation());
        holder.date.setText(rideHistoryList.get(position).getDate());
        Picasso.with(context)
                .load(rideHistoryList.get(position).getIconID())
                .into(holder.icon);
    }

    @Override
    public int getItemCount() {
        return rideHistoryList.size();
    }
}
