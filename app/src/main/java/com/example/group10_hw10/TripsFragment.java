package com.example.group10_hw10;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.group10_hw10.databinding.FragmentTripsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class TripsFragment extends Fragment implements TripsRecyclerViewAdapter.TripRecleyerViewInterface{

    ArrayList<Trip> tripArrayList = new ArrayList<>();
    ArrayList<String> docIdArrayList = new ArrayList<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    String TAG = "deep";

    RecyclerView recyclerView;
    TripsRecyclerViewAdapter adapter;
    LinearLayoutManager layoutManager;

    public TripsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.topmenu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
       if(item.getItemId() == R.id.logoutButton){
            mListener.logout();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle("Trips");
    }

    FragmentTripsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTripsBinding.inflate(inflater,container,false);

        binding.recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerView.setLayoutManager(layoutManager);

        adapter = new TripsRecyclerViewAdapter(tripArrayList,docIdArrayList,this);
        binding.recyclerView.setAdapter(adapter);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.newTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.goToNewTrip();
            }
        });

        Query query = db.collection("directions").whereEqualTo("user_id",mAuth.getUid());

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                tripArrayList.clear();
                docIdArrayList.clear();
                if (value != null){
                    for (QueryDocumentSnapshot document : value) {
                        Trip trp = document.toObject(Trip.class);
                        tripArrayList.add(trp);
                        docIdArrayList.add(document.getId());
                    }
                    adapter.notifyDataSetChanged();
//                    Log.d(TAG, "onEvent: "+tripArrayList.toString());
                }
            }
        });
    }

    TripsFragmentInterface mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (TripsFragmentInterface) context;
    }

    @Override
    public void goToTripDetails(Trip t, String docId) {
        mListener.goToTripDetails(t,docId);
    }

    interface TripsFragmentInterface{
        void logout();
        void goToNewTrip();
        void goToTripDetails(Trip trip,String id);
    }
}