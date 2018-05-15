package com.mschloapps.basicgps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SQLiteAdapter extends SQLiteOpenHelper {

    private static String databaseName = "storedLocations.db";
    private static int databaseVersion = 1;

    public SQLiteAdapter (Context context) {
        super (context, databaseName, null, databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE IF NOT EXISTS Locations (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, lat DOUBLE, lng DOUBLE, alt DOUBLE)";
        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void deleteLoc (String nm) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Locations", "name = ?", new String[] { nm });
        db.close();
    }

    public void deleteAllLoc () {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Locations", null, null);
    }

    public long addLoc (GPSLoc loc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", loc.getName());
        values.put("lat", loc.getLat());
        values.put("lng", loc.getLong());
        values.put("alt", loc.getAlt());
        long res = db.insert("Locations",null, values);
        db.close();
        return res;
    }

    public void updateLoc (GPSLoc loc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("lat", loc.getLat());
        values.put("lng", loc.getLong());
        values.put("alt", loc.getAlt());
        db.update("Locations", values, "name = ?", new String[] {String.valueOf(loc.getName())});
        db.close();
    }

    public List<GPSLoc> getAllLocations () {
        SQLiteDatabase db = this.getReadableDatabase();
        List<GPSLoc> allLocs = new ArrayList<>();
        String sql = "SELECT * FROM Locations ORDER BY name ASC";
        Cursor cursor = db.rawQuery(sql, null);
        GPSLoc loc = null;
        while (cursor.moveToNext()) {
            String nm = cursor.getString(1);
            double lat = cursor.getDouble(2);
            double lng = cursor.getDouble(3);
            double alt = cursor.getDouble(4);
            loc = new GPSLoc(nm, lat, lng, alt);
            allLocs.add(loc);
        }
        cursor.close();
        db.close();
        return allLocs;
    }

    public long getNumLocations() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, "Locations");
        db.close();
        return count;
    }

    public boolean nameExists(String nm) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM Locations WHERE name = ?";
        Cursor cursor = db.rawQuery(sql, new String[] {nm});

        boolean res;
        if (cursor.getCount() > 0) {
            res = true;
        } else {
            res = false;
        }
        cursor.close();
        db.close();
        return res;
    }


}
