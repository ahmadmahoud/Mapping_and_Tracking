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
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
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

import java.util.HashMap;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {
    private final int LOCATION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final String TAG = "MainActivity";
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    Button check;

    private LocationRequest locationRequest;
    private LocationManager manager;
    private final int LOCATION_REQUEST_SETTING = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        check = findViewById(R.id.check);
        manager = (LocationManager) getSystemService( MainActivity.LOCATION_SERVICE );
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        // التاكد من ان الموقع يعمل على هاتف المستخدم
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            check_Gps_ON_or_OFF();
            return;
        }
        check.setVisibility(View.GONE);


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

    // يتبع للخطوه السابق وهي التاكد من عمل gps على موقع المستخدم
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

    // اعطاء صلاحيات الموقع للتطبيق
    private void askForPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

            }
        }
    }

    //  ما يحدث عندما يفتح التطبيق
    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
            cheackSetSettingAndStartLocation();
        } else {
            askForPermission();
        }

    }


    // نتيجة البيرميشن
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cheackSetSettingAndStartLocation();
            }
        }
    }

    // الحصول على اخر موقع للمستخدم
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

                    // تخزين موقع المستخدم في قواعد البيانات

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
    // التاكد من اعدادات بداية التطبيق وحصول التطبيق على البيرميشن
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

    // تشغيل تحديث الموقع للمستخدم
    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    // ما يحدث عند ايقاف التطبيق
    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdate();
    }

    // ما يحدث عند ايقاف الموقع
    private void stopLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }


    // كول باك للموقع
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult == null) {
                Log.i(TAG, "onLocationResult: the location is null");
                return;
            }

            // تحديث بيانات المستخدم باستمرار وتخزينها في قواعد البيانات
            for (Location location : locationResult.getLocations()) {
                Log.i(TAG, "onLocationResult: " + location.toString());
                String msg = "Lat : " + location.getLatitude() + " ,log : " + location.getLongitude();
                ((TextView) findViewById(R.id.text_location)).setText(msg);

                HashMap<String, String> locationMap = new HashMap<>();
                locationMap.put("location", location.getAltitude() + "," + location.getLongitude());

                // تحديث الموقع في نفس document دون تكرار فقط عمل تحديث عليها
                firestore.collection("location_user").document("geecoders")
                        .set(new ServicesLocation(location.getLatitude(),location.getLongitude()));

                // تحديث الموقع في اكثر من عنصر كل عنصر باسم مختلف
//                firestore.collection("location").document(String.valueOf(System.currentTimeMillis()))
//                        .set(locationMap);

            }
        }
    };

}