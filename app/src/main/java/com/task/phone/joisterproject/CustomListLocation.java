package com.task.phone.joisterproject;

import android.app.Activity;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adhiraj on 27/8/15.
 */

public class CustomListLocation extends ArrayAdapter<String> {
    private final String myLat,myLong;
    private JSONArray jsonArray;
    String nameUser;
    LayoutInflater inflater;
    public CustomListLocation(Activity context,
                           JSONArray jsonArray,String myLat, String myLong) {
        super(context, R.layout.list_row);
        this.jsonArray = jsonArray;
        this.myLat = myLat;
        this.myLong = myLong;
        this.inflater = context.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return this.jsonArray.length();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        view = inflater.inflate(R.layout.list_row, null);

        TextView buildingName = (TextView) view.findViewById(R.id.txtBuildingName);
        TextView distance = (TextView) view.findViewById(R.id.txtDistance);
        TextView roadName = (TextView) view.findViewById(R.id.txtRoadName);


        try {

            Object str = jsonArray.get(position);
            JSONObject jobj = new JSONObject(str.toString());


            String longitude = jobj.getString("longitude");
            String latitude = jobj.getString("latitude");


            Location locationA = new Location("point A");
            locationA.setLatitude(Double.valueOf(myLat));
            locationA.setLongitude(Double.valueOf(myLong));
            Location locationB = new Location("point B");
            locationB.setLatitude(Double.valueOf(latitude));
            locationB.setLongitude(Double.valueOf(longitude));
            float distanceYou = locationA.distanceTo(locationB)/1000;
            String s = String.format("%.2f", distanceYou);

            buildingName.setText(jobj.getString("building_name"));
            roadName.setText(jobj.getString("road_name"));
            distance.setText(s+" Km");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }
}