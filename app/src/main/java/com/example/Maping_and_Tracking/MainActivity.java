package com.example.Maping_and_Tracking;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final int LOCATION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final String TAG = "MainActivity";
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    Button check , getting;

    private LocationRequest locationRequest;
    private LocationManager manager;
    private final int LOCATION_REQUEST_SETTING = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // define
        manager = (LocationManager) getSystemService(MainActivity.LOCATION_SERVICE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getting = findViewById(R.id.btn_getting_data);

        // maps
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // get user data location ( geocoder )
        getting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getting_your_data_location();
            }
        });

    }

    //  ???? ???????? ?????????? ???????? ??????????????
    @Override
    protected void onStart() {
        super.onStart();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
            cheackSetSettingAndStartLocation();
        } else {
            askForPermission();
        }

        // chek on or off wifi
        check = findViewById(R.id.check);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            check_Gps_ON_or_OFF();
        }
        else {
            check.setVisibility(View.GONE);
        }

    }

    private void getting_your_data_location() {
        TextView t1, t2, t3, t4, t5, t6 , t7;

        t1 = findViewById(R.id.tv_1);
        t2 = findViewById(R.id.tv_2);
        t3 = findViewById(R.id.tv_3);
        t4 = findViewById(R.id.tv_4);
        t5 = findViewById(R.id.tv_5);
        t6 = findViewById(R.id.tv_6);
        t7 = findViewById(R.id.tv_7);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {

                Location location = task.getResult();
                if (location != null) {
                    try {
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> address = geocoder.getFromLocation(
                                location.getLatitude(),location.getLongitude(),1
                        );

                        t1.setText(Html.fromHtml("<font color ='#57837B'><b>Latitude :</b><br></font>" +address.get(0).getLatitude()));
                        t2.setText(Html.fromHtml("<font color ='#57837B'><b>Longitude :</b><br></font>" +address.get(0).getLongitude()));
                        t3.setText(Html.fromHtml("<font color ='#57837B'><b>CountryName :</b><br></font>" +address.get(0).getCountryName()));
                        t4.setText(Html.fromHtml("<font color ='#57837B'><b>CountryCode :</b><br></font>" +address.get(0).getCountryCode()));
                        t5.setText(Html.fromHtml("<font color ='#57837B'><b>AddressLine :</b><br></font>" +address.get(0).getAddressLine(0)));
                        t6.setText(Html.fromHtml("<font color ='#57837B'><b>Locality :</b><br></font>" +address.get(0).getLocality()));
                        t7.setText(Html.fromHtml("<font color ='#57837B'><b>PostalCode :</b><br></font>" +address.get(0).getPostalCode()));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

    }

    private void check_Gps_ON_or_OFF() {
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest);
                builder.setAlwaysShow(true);

                Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                        .checkLocationSettings(builder.build());

                result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                        try {
                            LocationSettingsResponse response = task.getResult(ApiException.class);
                            check.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "GPS in ON", Toast.LENGTH_SHORT).show();


                        } catch (ApiException e) {

                            switch (e.getStatusCode()) {

                                case LocationSettingsStatusCodes
                                        .RESOLUTION_REQUIRED:
                                    try {
                                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                        resolvableApiException.startResolutionForResult(MainActivity.this, LOCATION_REQUEST_SETTING);

                                    } catch (IntentSender.SendIntentException ex) {

                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    break;
                            }

                        }

                    }
                });
            }
        });
    }

    // ???????? ???????????? ???????????? ?????? ???????????? ???? ?????? gps ?????? ???????? ????????????????
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_REQUEST_SETTING) {

            switch (resultCode) {
                case Activity.RESULT_OK:
                    Toast.makeText(this, "GPS is True", Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(this, "GPS is required to be turned on", Toast.LENGTH_SHORT).show();
                    return;
            }

        }

    }

    // ?????????? ?????????????? ???????????? ??????????????
    private void askForPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

            }
        }
    }

    // ?????????? ??????????????????
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cheackSetSettingAndStartLocation();
            }
        }
    }

    // ???????????? ?????? ?????? ???????? ????????????????
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.i(TAG, "onSuccess: " + location.toString());
                    Log.i(TAG, "onSuccess: " + location.getLatitude());
                    Log.i(TAG, "onSuccess: " + location.getLongitude());

                    // ?????????? ???????? ???????????????? ???? ?????????? ????????????????

//                    Map<String, String> locationMap = new HashMap<>();
//                    locationMap.put("location", location.getAltitude() + "," + location.getLongitude());
//                    firestore.collection("location")
//                            .document("userid")
//                            .set(locationMap);

                } else {
                    Log.i(TAG, "onSuccess: Location was null ");
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String Error = e.getMessage();
                Log.i(TAG, "onFailure: " + Error);
                Toast.makeText(MainActivity.this, Error, Toast.LENGTH_LONG).show();

            }
        });
    }
    // ???????????? ???? ?????????????? ?????????? ?????????????? ?????????? ?????????????? ?????? ??????????????????
    private void cheackSetSettingAndStartLocation() {
        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> locationSettingsRequestTask = settingsClient.checkLocationSettings(locationSettingsRequest);

        locationSettingsRequestTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdate();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String error = e.getMessage();
                Log.i(TAG, "onFailure 2: " + error);
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ?????????? ?????????? ???????????? ????????????????
    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    // ???? ???????? ?????? ?????????? ??????????????
    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdate();
    }

    // ???? ???????? ?????? ?????????? ????????????
    private void stopLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }


    // ?????? ?????? ????????????
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult == null) {
                Log.i(TAG, "onLocationResult: the location is null");
                return;
            }

            // ?????????? ???????????? ???????????????? ???????????????? ???????????????? ???? ?????????? ????????????????
            for (Location location : locationResult.getLocations()) {
                Log.i(TAG, "onLocationResult: " + location.toString());
                String msg = "Lat : " + location.getLatitude() + " ,log : " + location.getLongitude();
                ((TextView) findViewById(R.id.text_location)).setText(msg);

                HashMap<String, String> locationMap = new HashMap<>();
                locationMap.put("location", location.getAltitude() + "," + location.getLongitude());

                // ?????????? ???????????? ???? ?????? document ?????? ?????????? ?????? ?????? ?????????? ??????????
                firestore.collection("location_user").document("geecoders")
                        .set(new ServicesLocation(location.getLatitude(),location.getLongitude()));

                // ?????????? ???????????? ???? ???????? ???? ???????? ???? ???????? ???????? ??????????
//                firestore.collection("location").document(String.valueOf(System.currentTimeMillis()))
//                        .set(locationMap);

            }
        }
    };


}