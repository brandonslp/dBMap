package com.lp.brandon.dbmap;


import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GetPreferences preferences;
    private List<MarkerdBEntity> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        preferences = new GetPreferences(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
        settingmap(mMap);
    }

    private void settingmap(GoogleMap googleMap) {
        mMap = googleMap;
        //add uisettings
        mMap.getUiSettings().setCompassEnabled(true); //brujula
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (mMap!=null){
            mMap.setMyLocationEnabled(true); //activar ubicador
            LatLng mylocation = getLocation();
            mMap.animateCamera(CameraUpdateFactory.newLatLng(mylocation));
            addMarkers();
        }
    }

    private LatLng getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria,false);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        return new LatLng(locationManager.getLastKnownLocation(provider).getLatitude(),locationManager.getLastKnownLocation(provider).getLongitude());
    }

    private void addMarkers(){
        DbController dbController = new DbController(this);
        list = dbController.getAll();
        for (MarkerdBEntity m:list){
            Log.v("Brandon-lp","marker dB-> "+m.getdB()+" Latitude -> "+m.getLatitude()+" longitude -> "+m.getLongitude());
            String info="No enviado";
            if (m.isStatus()) info = "Enviado";
            LatLng position = new LatLng(Double.valueOf(m.getLatitude()),
                    Double.valueOf(m.getLongitude()));
            m.getdB();
            settingsMarkers(info, position, m.getdB());
            addRadius(new LatLng(Double.valueOf(m.getLatitude()), Double.valueOf(m.getLongitude())));
        }
        setOnclick();
    }

    private void setOnclick(){
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                for (MarkerdBEntity m:list){
                    LatLng position = new LatLng(Double.valueOf(m.getLatitude()),
                            Double.valueOf(m.getLongitude()));
                    if (Math.abs(position.latitude - latLng.latitude) < 0.05 && Math.abs(position.longitude - latLng.longitude) < 0.05) {
                        Log.v("Brandon-lp","long click -> "+m.getdB());

                    }
                }
            }
        });
    }

    private void settingsMarkers(String info, LatLng position,double dB){
        mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(dB+"dB")
                .snippet(info)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        addRadius(position);
    }

    private void addRadius(LatLng center){
        mMap.addCircle(new CircleOptions()
                        .center(center)
                        .radius(Float.parseFloat(preferences.getRadiusMap()))
                        .fillColor(Color.argb(10,0,188,212))
                        .strokeColor(0x00BCD4));
    }
}
