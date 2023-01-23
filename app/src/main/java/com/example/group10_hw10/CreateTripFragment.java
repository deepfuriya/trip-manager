package com.example.group10_hw10;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.group10_hw10.databinding.FragmentCreateTripBinding;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;


public class CreateTripFragment extends Fragment {

    String TAG = "deep";
    FusedLocationProviderClient fusedLocationClient;
    Integer PRIORITY_BALANCED_POWER_ACCURACY = 102;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    String userID = mAuth.getCurrentUser().getUid();

    public CreateTripFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Create Trip");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    FragmentCreateTripBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCreateTripBinding.inflate(inflater, container, false);
        getLocation();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String locationTag = String.valueOf(binding.editTextDestination.getText());
                if (locationTag.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter Destination Name", Toast.LENGTH_SHORT).show();
                } else {

                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        getLocation();
                        return;
                    }else{
                        fusedLocationClient.getCurrentLocation(102, null)
                                .addOnSuccessListener(new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        Log.d(TAG, "onSuccess: LAT LONG"+location.getLatitude()+"LONG---"+location.getLongitude());
                                        HashMap<String,Object> val = new HashMap<>();
                                        val.put("location",locationTag);
                                        val.put("start_time",new Timestamp(new Date()));
                                        val.put("complete_time",null);
                                        val.put("complete",0);
                                        val.put("total_miles",null);
                                        val.put("user_id",userID);
                                        val.put("start_lat",location.getLatitude());
                                        val.put("start_long",location.getLongitude());
                                        val.put("end_lat",null);
                                        val.put("end_long",null);

                                        Trip trip = new Trip(val);

                                        DocumentReference docRef = db.collection("directions").document();

                                                docRef.set(val)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        mListener.goToTripDetails(trip,docRef.getId());
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(getContext(), "Failed to save data to server", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });

                                                    }
                                                });

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), "Failed to get Location", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }


                }
            }
        });
    }

    CreateTripInterface mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (CreateTripInterface) context;
    }


    interface CreateTripInterface{
        void goToTripDetails(Trip trip,String id);
    }

    public void getLocation(){
        requestPermissionLauncher.launch(ACCESS_FINE_LOCATION);
        binding.locationStatus.setText("Loading");
        binding.locationStatus.setTextColor(Color.YELLOW);
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, ": ALLOWED");
                    binding.locationStatus.setText("Success");
                    binding.locationStatus.setTextColor(Color.GREEN);
                } else {
                    Log.d(TAG, ": NOT ALLOWED");
                    Toast.makeText(getContext(), "Please allow location to proceed", Toast.LENGTH_SHORT).show();
                    getLocation();
                }
            });
}