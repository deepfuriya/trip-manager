package com.example.group10_hw10;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TripsRecyclerViewAdapter extends RecyclerView.Adapter<TripsRecyclerViewAdapter.TripsViewHolder> {

    ArrayList<Trip> arrayList = new ArrayList<>();
    ArrayList<String> docIdArrayList = new ArrayList<>();
    TripRecleyerViewInterface mListener;

    public TripsRecyclerViewAdapter(ArrayList<Trip> a,ArrayList<String> d,TripRecleyerViewInterface mListener){
        this.arrayList = a;
        this.docIdArrayList = d;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public TripsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_item_row,parent,false);
        TripsViewHolder tripsViewHolder = new TripsViewHolder(view,mListener);
        return tripsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TripsViewHolder holder, int position) {
        Trip trip = arrayList.get(position);
        String docId = docIdArrayList.get(position);

        holder.textViewLocationName.setText("Trip to "+trip.getLocation());

        String temp1 = "N/A";
        if (!(trip.getComplete_time() == null)){
            temp1 = trip.getComplete_time();
        }

        holder.textViewCompletedTimeValue.setText(temp1);
        holder.textViewStartTiming.setText(trip.getStart_time());

        String status;
        int color;
        String miles;
        if (trip.complete == 0) {
            status = "On Going";
            color = Color.YELLOW;
            miles = "";

        } else {
            status = "Comopleted";
            color = Color.GREEN;
            miles = trip.getTotal_miles()+" miles";
        }

        holder.textViewStatus.setText(status);
        holder.textViewStatus.setTextColor(color);
        holder.textViewMiles.setText(miles);

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.goToTripDetails(trip,docId);
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public class TripsViewHolder extends RecyclerView.ViewHolder{

        TextView textViewLocationName;
        TextView textViewStartTiming;
        TextView textViewCompletedTimeValue;
        TextView textViewStatus;
        View rootView;
        TextView textViewMiles;
        TripRecleyerViewInterface mListener;

        public TripsViewHolder(@NonNull View itemView,TripRecleyerViewInterface mListener) {
            super(itemView);
            rootView = itemView;
            this.mListener = mListener;
            textViewLocationName = itemView.findViewById(R.id.textViewLocationName);
            textViewStartTiming = itemView.findViewById(R.id.textViewStartTiming);
            textViewCompletedTimeValue = itemView.findViewById(R.id.textViewCompletedTimeValue);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewMiles = itemView.findViewById(R.id.textViewMiles);
        }
    }
    
    interface TripRecleyerViewInterface{
        void goToTripDetails(Trip t,String docId);
    }
}
