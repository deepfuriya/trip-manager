package com.example.group10_hw10;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.Toast;

import com.example.group10_hw10.databinding.FragmentTripDetailsBinding;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class TripDetailsFragment extends Fragment implements OnMapReadyCallback, OnMapsSdkInitializedCallback {

    private static final String TRIP_DATA = "trip_data";
    private static final String DOC_ID = "document_id";

    private OkHttpClient client = new OkHttpClient();

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    FusedLocationProviderClient fusedLocationClient;

    String TAG = "deep";
    private Trip trip;
    private Trip tripObjectForMaps;
    private String docId;

    private GoogleMap mGoogleMap;
    private MapView mapView;
    private UiSettings mUiSettings;
    SupportMapFragment mapFragment;

    public TripDetailsFragment() {
        // Required empty public constructor
    }


    public static TripDetailsFragment newInstance(Trip trip, String docId) {
        TripDetailsFragment fragment = new TripDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(TRIP_DATA, trip);
        args.putString(DOC_ID, docId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trip = (Trip) getArguments().getSerializable(TRIP_DATA);
            tripObjectForMaps = trip;
            docId = getArguments().getString(DOC_ID);
        }
        getActivity().setTitle("Trip Details");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        MapsInitializer.initialize(getContext(), MapsInitializer.Renderer.LATEST, this);

    }

    FragmentTripDetailsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTripDetailsBinding.inflate(inflater, container, false);
        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        db.collection("directions")
                .document(docId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        Trip trip = value.toObject(Trip.class);
                        tripObjectForMaps = trip;

                        binding.textViewLocation.setText("Trip to " + trip.getLocation());
                        binding.textViewStartTime.setText(trip.getStart_time());

                        String complete_time = trip.getComplete_time();
                        if (trip.getComplete_time() == null) {
                            complete_time = "N/A";
                        }

                        binding.textViewCompletedAt.setText(complete_time);
                        String status;
                        int color;
                        int buttonView;
                        if (trip.complete == 0) {
                            status = "On Going";
                            color = Color.YELLOW;
                            buttonView = 1;
                        } else {
                            status = "Comopleted";
                            color = Color.GREEN;
                            buttonView = 0;
                        }

                        binding.textViewcurrentStatus.setText(status);
                        binding.textViewcurrentStatus.setTextColor(color);

                        binding.textViewTotalMiles.setVisibility(View.VISIBLE);
                        binding.completeButton.setVisibility(View.VISIBLE);

                        if (buttonView == 1) {
                            binding.textViewTotalMiles.setVisibility(View.GONE);
                        } else {
                            binding.completeButton.setVisibility(View.GONE);
                        }

                        updatePoints();
                    }
                });

        binding.completeButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {

                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                    return;
                }

                CurrentLocationRequest.Builder clr = new CurrentLocationRequest.Builder();
                clr.setDurationMillis(5000)
                        .setMaxUpdateAgeMillis(0)
                        .setGranularity(Granularity.GRANULARITY_FINE)
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .build();

                fusedLocationClient
                        .getCurrentLocation(clr.build(), null)
                        .addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if(task.isSuccessful()){
                                    Location location = task.getResult();
                                    if(location != null){

                                        HashMap<String,Object> updateValue = new HashMap<>();
                                        updateValue.put("complete_time",new Timestamp(new Date()));
                                        updateValue.put("end_lat",location.getLatitude());
                                        updateValue.put("end_long",location.getLongitude());
                                        updateValue.put("complete",1);

                                        db.collection("directions")
                                                .document(docId)
                                                .update(updateValue)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){

                                                            Request request = new Request.Builder()
                                                                    .url("https://maps.googleapis.com/maps/api/distancematrix/json?origins="+trip.getStart_lat()+"%2C"+trip.getStart_long()+"&destinations="+location.getLatitude()+"%2C"+location.getLongitude()+"&key={PUT YOUR GOOGLE MAPS API KEY HERE}&units=imperial")
                                                                    .build();

                                                            Log.d(TAG, "onComplete: -->"+"https://maps.googleapis.com/maps/api/distancematrix/json?origins="+trip.getStart_lat()+"%2C"+trip.getStart_long()+"&destinations="+location.getLatitude()+"%2C"+location.getLongitude()+"&key={PUT YOUR GOOGLE MAPS API KEY HERE}");



                                                            client.newCall(request).enqueue(new Callback() {
                                                                @Override
                                                                public void onFailure(Call call, IOException e) {

                                                                }
                                                                @Override
                                                                public void onResponse(Call call, Response response) throws IOException {
                                                                    
                                                                    if (response.isSuccessful()){
                                                                        JSONObject jsonRespRouteDistance = null;
                                                                        try {
                                                                            jsonRespRouteDistance = new JSONObject(response.body().string())
                                                                                    .getJSONArray("rows")
                                                                                    .getJSONObject(0)
                                                                                    .getJSONArray ("elements")
                                                                                    .getJSONObject(0)
                                                                                    .getJSONObject("distance");
                                                                            String distance = jsonRespRouteDistance.get("text").toString();
                                                                            Log.d(TAG, "onResponse: value of distance"+distance);
                                                                            HashMap<String,Object> updateVal = new HashMap<>();
                                                                            updateVal.put("total_miles",Double.parseDouble(distance.substring(0,distance.indexOf("m"))));
                                                                            db.collection("directions")
                                                                                    .document(docId)
                                                                                    .update(updateVal)
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()){

                                                                                            }else{
                                                                                                getActivity().runOnUiThread(new Runnable() {
                                                                                                    @Override
                                                                                                    public void run() {
                                                                                                        Toast.makeText(getContext(), "Error updating total miles", Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                        }
                                                                                    });

                                                                        } catch (JSONException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }else{
                                                                        Log.d(TAG, "onResponse: ERROR FROM GOOGLE API");
                                                                    }





                                                                }
                                                            });


                                                        }else{
                                                            getActivity().runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Toast.makeText(getContext(), "Error saving data to server", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                    } else {
                                        Log.d("deep", "onSuccess: location is null" );
                                    }

                                } else {
                                    task.getException().printStackTrace();
                                }
                            }
                        });



            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mUiSettings = googleMap.getUiSettings();

        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);

        updatePoints();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapsSdkInitialized(@NonNull MapsInitializer.Renderer renderer) {
        switch (renderer) {
            case LATEST:
                Log.d("MapsDemo", "The latest version of the renderer is used.");
                break;
            case LEGACY:
                Log.d("MapsDemo", "The legacy version of the renderer is used.");
                break;
        }
    }

    public void getLocation(){
        requestPermissionLauncher.launch(ACCESS_FINE_LOCATION);
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, ": ALLOWED");
                } else {
                    Log.d(TAG, ": NOT ALLOWED");
                    Toast.makeText(getContext(), "Please allow location to proceed", Toast.LENGTH_SHORT).show();
                    getLocation();
                }
            });

    public void updatePoints(){
        LatLng point1 = new LatLng(tripObjectForMaps.getStart_lat(),tripObjectForMaps.getStart_long());
        mGoogleMap.addMarker(new MarkerOptions().position(point1));
        Integer paddingValue = 100;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(point1);

        if (tripObjectForMaps.getEnd_lat() != null){
            LatLng point2 = new LatLng(tripObjectForMaps.getEnd_lat(),tripObjectForMaps.getEnd_long());
            mGoogleMap.addMarker(new MarkerOptions().position(point2));
            builder.include(point2);
            paddingValue = 100;
            LatLngBounds latLngBounds = builder.build();
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,paddingValue));

        }else{
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(point1)
                    .zoom(17)
                    .build();
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }


    }
}