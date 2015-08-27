package com.task.phone.joisterproject;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by adhiraj on 27/8/15.
 */
public class LocationDetails extends Activity{
    String latitude,longitude;
    private GoogleMap googleMap;
    ArrayList<Marker> mListMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_location_details);


        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.getUiSettings().setMapToolbarEnabled(false);

            googleMap.setMyLocationEnabled(true);

        }else{
            googleMap.clear();
        }
        Intent intent = getIntent();
        latitude = intent.getStringExtra("latitude");
        longitude = intent.getStringExtra("longitude");
        String buildingName = intent.getStringExtra("building_name");
        String roadName = intent.getStringExtra("road_name");
        String myLat = intent.getStringExtra("myLat");
        String myLong = intent.getStringExtra("myLong");

        Location locationA = new Location("point A");
        locationA.setLatitude(Double.valueOf(myLat));
        locationA.setLongitude(Double.valueOf(myLong));
        Location locationB = new Location("point B");
        locationB.setLatitude(Double.valueOf(latitude));
        locationB.setLongitude(Double.valueOf(longitude));
        float distanceYou = locationA.distanceTo(locationB)/1000;
        String s = String.format("%.2f", distanceYou);

        TextView txtBuildingName = (TextView) findViewById(R.id.txtBuildingName);
        TextView txtDistance = (TextView) findViewById(R.id.txtDistance);
        TextView txtRoadName = (TextView) findViewById(R.id.txtRoadName);

        txtBuildingName.setText(buildingName);
        txtRoadName.setText(roadName);
        txtDistance.setText(s+" Km");


        LatLng latLng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        if(null != googleMap){

                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)))
                        .title(buildingName)
                        .snippet(roadName)
                        .flat(true));


        }
    }

    public void openGoogleMaps(View view){

        Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitude+","+longitude+"");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
}
