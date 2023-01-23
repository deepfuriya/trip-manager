package com.example.group10_hw10;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginListener,SignUpFragment.SignUpListener,TripsFragment.TripsFragmentInterface, CreateTripFragment.CreateTripInterface {
    String TAG = "deep";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null){
            goToTripsFragment();
        }else{
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.rootView, new LoginFragment())
                    .commit();
        }

    }






    @Override
    public void createNewAccount() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView,new SignUpFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void login() {
        getSupportFragmentManager().popBackStack();
    }


    @Override
    public void goToTripsFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView,new TripsFragment())
                .commit();
    }

    @Override
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView,new LoginFragment())
                .commit();
    }

    @Override
    public void goToNewTrip() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView,new CreateTripFragment())
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void goToTripDetails(Trip trip,String id) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView,new TripDetailsFragment().newInstance(trip,id))
                .addToBackStack(null)
                .commit();
    }
}