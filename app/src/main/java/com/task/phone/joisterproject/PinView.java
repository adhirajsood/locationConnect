package com.task.phone.joisterproject;


import android.view.View;

import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by adhiraj on 28/8/15.
 */
public class PinView {

    public static View pinViewClick(Marker marker, final View v) {
        TextView txtBuildingName= (TextView) v.findViewById(R.id.txtBuildingName);
        TextView txtRoadName =(TextView) v.findViewById(R.id.txtRoadName);

        String roadName = marker.getSnippet();
        String buildingName = marker.getTitle();
        txtBuildingName.setText(buildingName);
        txtRoadName.setText(roadName);

        return v;

    }
}
