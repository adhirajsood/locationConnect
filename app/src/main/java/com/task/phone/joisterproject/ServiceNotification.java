package com.task.phone.joisterproject;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by adhiraj on 29/8/15.
 */
public class ServiceNotification extends Service implements LocationListener {
    public static final String ACTION = "com.task.phone.joisterproject.TestService";

    //The minimum distance to change updates in metters
    private static final long GPS_MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; //10 metters
    //The minimum time beetwen updates in milliseconds
    private static final long GPS_MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    private String lat,lng;
    DBHandler dbHandler;
    Location location;
    Handler handler = new Handler();
    @Override
    public void onCreate() {


        final long in10Minutes = 10 * 60 * 1000;


        location = getLocation(this,this);
        if (location!=null){
            lat = String.valueOf(location.getLatitude());
            lng = String.valueOf(location.getLongitude());

        }else {
            Toast.makeText(this, "Location not enabled!", Toast.LENGTH_SHORT).show();
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                checkLocation();
                handler.postDelayed(this, in10Minutes);
            }
        };
        handler.postDelayed(runnable, in10Minutes);


    }


    public void checkLocation(){
        dbHandler = new DBHandler(this);
        JSONArray jsonArray = dbHandler.getLocationJoister();

        for (int i =0;i<jsonArray.length();i++) {
            Object str = null;
            try {
                str = jsonArray.get(i);

                JSONObject jobj = new JSONObject(str.toString());
                String latitude = jobj.getString("latitude");
                String LatMap = latitude.replace(",", "");

                String longitude = jobj.getString("longitude");
                String LongMap = longitude.replace(",", "");

                Location locationA = new Location("point A");
                locationA.setLatitude(Double.valueOf(lat));
                locationA.setLongitude(Double.valueOf(lng));
                Location locationB = new Location("point B");
                locationB.setLatitude(Double.valueOf(LatMap));
                locationB.setLongitude(Double.valueOf(LongMap));
                float distanceYou = locationA.distanceTo(locationB)/1000;
                String s = String.format("%.2f", distanceYou);

                if (distanceYou<=1){
                   notifyUser(jobj.getString("building_name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }




    public void notifyUser(String building_name) {
        try{
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.directions)
                            .setContentTitle("Joister")
                            .setContentText(building_name+" is available for connect")
                            .setAutoCancel(true);
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(this, Home.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(Home.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if(alarmSound != null) {
                mBuilder.setSound(alarmSound);
            }
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify("Joister",1, mBuilder.build());
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Something went wrong in service sending notification");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
}
