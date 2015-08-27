package com.task.phone.joisterproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class Home extends Activity implements LocationListener, IAsyncCallback {

    //The minimum distance to change updates in metters
    private static final long GPS_MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; //10 metters
    //The minimum time beetwen updates in milliseconds
    private static final long GPS_MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    private String lat,lng;
    DBHandler dbHandler;
    Location location;
    ListView lv;
    private GoogleMap googleMap;
    ArrayList<Marker> mListMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("Location");
        tabSpec.setContent(R.id.tabLocation);
        tabSpec.setIndicator("Location");
        tabHost.addTab(tabSpec);


        tabSpec = tabHost.newTabSpec("Map");
        tabSpec.setContent(R.id.tabMap);
        tabSpec.setIndicator("Map");
        tabHost.addTab(tabSpec);

        for(int i=0;i<tabHost.getTabWidget().getChildCount();i++)
        {
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.parseColor("#000000"));
        }

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
        mListMarkers = new ArrayList<Marker>();



        lv = (ListView) findViewById(R.id.listView);

        location = getLocation(this,this);
        if (location!=null){
            lat = String.valueOf(location.getLatitude());
            lng = String.valueOf(location.getLongitude());

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }else {
            Toast.makeText(this,"Location not enabled!",Toast.LENGTH_SHORT).show();
            return;
        }
        dbHandler = new DBHandler(this);
        JSONArray jsonArray = dbHandler.getLocationJoister();
        if (jsonArray.length()>0){
          System.out.println("json data   "+jsonArray);
            setListView(jsonArray);
            setMapView(jsonArray);
        }else {
            APIManager.getInstance().sendAsyncCall("GET",lat,lng,Home.this);
        }

    }

    private void setListView(final JSONArray jsonArray) {
        CustomListLocation adapter = new CustomListLocation(Home.this, jsonArray,lat,lng);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    Object str = jsonArray.get(position);
                    JSONObject jobj =  new JSONObject(str.toString());
                    Intent intent = new Intent(Home.this,LocationDetails.class);
                    intent.putExtra("building_name",jobj.getString("building_name"));
                    intent.putExtra("latitude",jobj.getString("latitude"));
                    intent.putExtra("longitude", jobj.getString("longitude"));
                    intent.putExtra("road_name",jobj.getString("road_name"));
                    intent.putExtra("myLat",lat);
                    intent.putExtra("myLong",lng);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                };
            }
        });
    }

    private void setMapView(final JSONArray jsonArray) {

        try {
        for (int i =0;i<jsonArray.length();i++) {
            Object str = jsonArray.get(i);
            JSONObject jobj =  new JSONObject(str.toString());

            String latitude = jobj.getString("latitude");
            String LatMap = latitude.replace(",", "");
            String longitude = jobj.getString("longitude");
            String LongMap = longitude.replace(",", "");
            String buildingName = jobj.getString("building_name");
            String roadName = jobj.getString("road_name");

            if (latitude != null && longitude != null) {
                addMapMarker(Double.valueOf(LatMap), Double.valueOf(LongMap), buildingName, roadName);

            }
        }

            } catch (JSONException e) {
                e.printStackTrace();
            }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Location getLocation(Context mContext, LocationListener listener)
    {
        LocationManager locationManager;
        Location location = null;

        try
        {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            if(locationManager==null){
                return null;

            }

            //getting GPS status
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            //getting network status
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled)
            {
                // no network provider is enabled

                return null;
            }
            else
            {
                if ( isNetworkEnabled)
                {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            GPS_MIN_TIME_BW_UPDATES,
                            GPS_MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);

                    if (locationManager != null)
                    {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                if (isGPSEnabled)
                {
                    if (location == null)
                    {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                GPS_MIN_TIME_BW_UPDATES,
                                GPS_MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);
                        if (locationManager != null)
                        {
                            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }

            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            Log.e("Get Loc", "Failed to connect to LocationManager", e);
        }
        return location;
    }


    public void addMapMarker(double latitude, double longitude, String buildingName, String roadName){

        if(null != googleMap){


                mListMarkers.add(googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(buildingName)
                        .snippet(roadName)
                        .flat(true)));


        }

        /*googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.custom_marker, null);
                return PinView.pinViewClick(marker,v);
            }
        });*/
    }

    @Override
    public void onLocationChanged(Location location) {

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

    @Override
    public void onSuccessResponse(String successResponse) {
        System.out.println("got the success " + successResponse);
        try {
            JSONObject jsonObject = new JSONObject(successResponse);
            String result = jsonObject.getString("result");
            if (result.equals("success")){
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i=0;i<jsonArray.length();i++){
                    JSONObject dataObject = jsonArray.getJSONObject(i);
                    dbHandler.addLocations(dataObject);

                }
            }else {
                Toast.makeText(this,"No result!",Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(int errorCode, String errorResponse) {

    }
}
