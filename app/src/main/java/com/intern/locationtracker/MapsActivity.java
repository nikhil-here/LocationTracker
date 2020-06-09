package com.intern.locationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, ValueEventListener {

    private GoogleMap mMap;
    private EditText etLatitude, etLongitude;
    private Button btnUpdate;
    private DatabaseReference dbRef;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private final long MIN_TIME = 1000;
    private final long MIN_DIST = 5;
    private static final String TAG = "MapsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //requesting permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        initViews();
        initDatabase();
        initListeners();
    }

    private void initDatabase() {
        dbRef = FirebaseDatabase.getInstance().getReference("location");
    }

    private void initListeners() {
        btnUpdate.setOnClickListener(this);
        dbRef.addValueEventListener(this);
    }

    private void initViews() {
        etLatitude = findViewById(R.id.activity_map_et_latitude);
        etLongitude = findViewById(R.id.activity_map_et_longitude);
        btnUpdate = findViewById(R.id.activity_map_btn_update);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    String latitude = Double.toString(location.getLatitude());
                    String longitude = Double.toString(location.getLongitude());
                    etLatitude.setText(latitude);
                    etLongitude.setText(longitude);
                } catch (Exception e) {
                    Toast.makeText(MapsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onLocationChanged: " + e.getMessage());
                }


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //handling location Manager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        };
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
        }
        catch (Exception e)
        {
            Toast.makeText(MapsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onMapReady: locationManager error " + e.getMessage());
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.activity_map_btn_update:
                dbRef.child("latitude").push().setValue(etLatitude.getText().toString());
                dbRef.child("longitude").push().setValue(etLongitude.getText().toString());

        }
    }

    //database override methods
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        try {
            String databaseLatitudeString = dataSnapshot.child("latitude").getValue().toString().substring(1, dataSnapshot.child("latitude").getValue().toString().length() - 1);
            Log.d(TAG, "LatitudeString is " + databaseLatitudeString);
            String databaseLongitudedeString = dataSnapshot.child("longitude").getValue().toString().substring(1, dataSnapshot.child("longitude").getValue().toString().length() - 1);
            Log.d(TAG, "LatitudeString is " + databaseLongitudedeString);

            String[] stringLat = databaseLatitudeString.split(", ");
            Arrays.sort(stringLat);
            String latitude = stringLat[stringLat.length - 1].split("=")[1];
            String[] stringLong = databaseLongitudedeString.split(", ");
            Arrays.sort(stringLong);
            String longitude = stringLong[stringLong.length - 1].split("=")[1];
            LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            mMap.addMarker(new MarkerOptions().position(latLng).title(latitude + " , " + longitude));
        }
        catch (Exception e)
        {
            Log.d(TAG,"Exception Recorded in db ");
        }

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
