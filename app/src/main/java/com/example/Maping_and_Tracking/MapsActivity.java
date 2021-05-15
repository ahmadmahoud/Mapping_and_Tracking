package com.example.Maping_and_Tracking;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.graphics.drawable.BitmapDrawable;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private Geocoder geocoder;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getserviceslocation_user();
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.setTrafficEnabled(true);

        // لتحديد موقع المستخدم عن طريقة زر باعلى الشاشه
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        mMap.setMyLocationEnabled(true);


        // التعامل مع عنوان معين وتحديد موقعه على الخريطة

//            LatLng latLng = new LatLng(24.589864,46.6161046);
//            MarkerOptions markerOptions = new MarkerOptions()
//                    .position(latLng).title("حديقة العاشر").snippet("مكان رائع تقضي فيه يوم اجازتك");
//            mMap.addMarker(markerOptions);
//            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,12);
//            mMap.animateCamera(cameraUpdate);


        // كيفية البحث والحصول على معلومات موقع معين

//        try {
//            List<Address> addresses = geocoder.getFromLocationName("zagazig",1);
//
//            Address address = addresses.get(0);
//            Log.i(TAG, "onMapReady: "+address.toString());
//
//            LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
//            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(address.getLocality());
//            mMap.addMarker(markerOptions);
//
//            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,16);
//            mMap.animateCamera(cameraUpdate);
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.i(TAG, "onMapReady: "+e);
//        }
//
//        mMap.setOnMapLongClickListener(this); // اضافة نقطة على الخريطة عن طريق الضغط المطول و اظهار بيانات
//        mMap.setOnMapClickListener(this); // اضافة نقطة وحيده على الخريطة بمجرد الضغط دون اظهار بيانات


    }

    // اضافة نقطة وحيده على الخريطة بمجرد الضغط دون اظهار بيانات
//    @Override
//    public void onMapClick(@NonNull LatLng latLng) {
//        mMap.clear();
//        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
//        mMap.addMarker(markerOptions);
//    }

    // اضافة نقطة على الخريطة عن طريق الضغط المطول و اظهار بيانات
//    @Override
//    public void onMapLongClick(@NonNull LatLng latLng) {
//        try {
//            List<Address> addresseslist = geocoder.getFromLocation(latLng.latitude, latLng.latitude, 1);
//            if (addresseslist.isEmpty()) {
//                Toast.makeText(this, "No information", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            Address address = addresseslist.get(0);
//            String streename = address.getAddressLine(0);
//            Log.i(TAG, "onMapLongClick: " + streename);
//            mMap.addMarker(new MarkerOptions().position(latLng).title(streename));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    // الحصول على بيانات من الفاير ستور "فايز بيز"
//    private void getserviceslocation() {
//        firebaseFirestore.collection("location_map").addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
//                for(DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
//                    ServicesLocation servicesLocation = snapshot.toObject(ServicesLocation.class);
//                    Log.i(TAG, "onEvent: " + servicesLocation);
//                    LatLng latLng = new LatLng(servicesLocation.getLat(),servicesLocation.getLng());
//                    mMap.addMarker(new MarkerOptions().position(latLng));
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
//                }
//
//            }
//        });
//    }

    // الحصول على موقع المستخدم الذي يتم عمل له update
    private void getserviceslocation_user() {
        firebaseFirestore.collection("location_user").document("geecoders").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot document, @Nullable FirebaseFirestoreException error) {
            ServicesLocation servicesLocation =   document.toObject(ServicesLocation.class);

            if (servicesLocation == null) return;

                Log.i(TAG, "onEvent: "+servicesLocation);
                LatLng latLng = new LatLng(servicesLocation.getLat(),servicesLocation.getLng());

                MarkerOptions markerOptions = new MarkerOptions().position(latLng);

                mMap.addMarker(new MarkerOptions().position(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));

            }
        });
    }
}