package com.task.phone.joisterproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adhiraj on 27/8/15.
 */
public class DBHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "LocationsTable";

    private static final String TABLE_LOCATIONS = "LocationsJoister";

    public static final String KEY_BUILDING_NAME = "building_name";
    public static final String KEY_ROAD_NAME = "road_name";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static DBHandler instance;


    public static synchronized DBHandler getHelper(Context context) {
        if (instance == null)
            instance = new DBHandler(context);

        return instance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE_LOCATIONS = "CREATE TABLE " + TABLE_LOCATIONS + "("
                + KEY_BUILDING_NAME + " TEXT,"
                + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT,"
                + KEY_ROAD_NAME + " TEXT)";

        try {

            db.execSQL(CREATE_TABLE_LOCATIONS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);

        // Create tables again
        onCreate(db);
    }

    public void addLocations(JSONObject locationResponse) {
        try {

            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            try {
                values.put(KEY_BUILDING_NAME, locationResponse.getString(KEY_BUILDING_NAME));
                values.put(KEY_LATITUDE, locationResponse.getString(KEY_LATITUDE));
                values.put(KEY_LONGITUDE, locationResponse.getString(KEY_LONGITUDE));
                values.put(KEY_ROAD_NAME, locationResponse.getString(KEY_ROAD_NAME));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Inserting Row
            db.insertWithOnConflict(TABLE_LOCATIONS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            //db.close(); // Closing database connection
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONArray getLocationJoister() {
        JSONArray locationJoister = new JSONArray();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_LOCATIONS;

        System.out.println(selectQuery);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        int i = 0;

        try{

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    JSONObject locationResponse = new JSONObject();
                    try {
                        locationResponse.put(KEY_BUILDING_NAME, cursor.getString(0));
                        locationResponse.put(KEY_LATITUDE, cursor.getString(1));
                        locationResponse.put(KEY_LONGITUDE, cursor.getString(2));
                        locationResponse.put(KEY_ROAD_NAME, cursor.getString(3));

                        locationJoister.put(i, locationResponse);
                        i++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {

            if(!cursor.isClosed()) {
                cursor.close();
            }
        }

        //db.close();
        return locationJoister;
    }
}


