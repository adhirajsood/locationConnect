package com.task.phone.joisterproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

/**
 * Created by adhiraj on 28/8/15.
 */
public class BackroundNotify extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.task.phone.joisterproject.alarm";
    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, ServiceNotification.class);

        context.startService(i);
    }

}
