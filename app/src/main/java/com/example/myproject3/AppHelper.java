package com.example.myproject3;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class AppHelper {

    private static final String TAG = "AppHelper";

    private static SQLiteDatabase database;
    private static String createTable = "CREATE TABLE IF NOT EXISTS myLocation" +
            "(" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "latitude DOUBLE NOT NULL, " +
            "longitude DOUBLE NOT NULL, " +
            "time TEXT NOT NULL" +
            ")";
    private static String dropTable = "DROP TABLE ";

    public static SQLiteDatabase openDatabase(Context context, String databaseName) {
        Log.d(TAG, "opendatabase 호출됨");

        try {
            database = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
            if (database != null) {
                Log.d(TAG, "데이터베이스 " + databaseName + " 오픈됨.");
                return database;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createTable(String tableName) {
        Log.d(TAG, "createTable 호출됨 : " + tableName);

        if (database != null) {
            if (tableName.equals("myLocation")) {
                database.execSQL(createTable);
                Log.d(TAG, "myLocation 테이블 생성 요청됨");
            }
        } else {
            Log.d(TAG, "데이터베이스를 먼저 오픈하세요");
        }
    }

    public static void insertData(double latitude, double longitude, String time) {
        Log.d(TAG, "insertData() 호출됨");

        if (database != null) {
            String sql = "insert into myLocation(latitude, longitude, time) values(?, ?, ?)";
            Object[] params = {latitude, longitude, time};
            database.execSQL(sql, params); // 두번째인자에 params를 넣으면 SQL이 실행되기전에 params 데이터를 물음표로 대체하면서 삽입

            Log.d(TAG, "데이터 추가함");
        } else {
            Log.d(TAG, "먼저 데이터베이스를 오픈하세요.");
        }
    }

    public static ArrayList<Double> selectLatitude(String tableName) {
        Log.d(TAG, "selectLatitude() 호출됨.");
        ArrayList<Double> latitudeArr = new ArrayList<>();

        if (database != null) {
            String sql = "SELECT latitude from " + tableName;
            Cursor cursor = database.rawQuery(sql, null);

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                latitudeArr.add(cursor.getDouble(0));
            }
            cursor.close();
        }
        return (latitudeArr);
    }

    public static ArrayList<Double> selectLongitude(String tableName) {
        Log.d(TAG, "selectLongitude() 호출됨.");
        ArrayList<Double> longitudeArr = new ArrayList<>();

        if (database != null) {
            String sql = "SELECT longitude from " + tableName;
            Cursor cursor = database.rawQuery(sql, null);

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                longitudeArr.add(cursor.getDouble(0));
            }
            cursor.close();
        }
        return longitudeArr;
    }

    public static ArrayList<String> selectTime(String tableName) {
        Log.d(TAG, "selectTime() 호출됨.");
        ArrayList<String> timeArr = new ArrayList<>();

        if (database != null) {
            String sql = "SELECT time from " + tableName;
            Cursor cursor = database.rawQuery(sql, null);

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                timeArr.add(cursor.getString(0));
            }
            cursor.close();
        }
        return timeArr;
    }

    public static void dropTable(String tableName) {
        Log.d(TAG, "dropTable 호출됨 : " + tableName);

        if (database != null) {
            if (tableName.equals("myLocation")) {
                database.execSQL(dropTable + tableName);
                Log.d(TAG, "myLocation 테이블 제거 요청됨");
            }
        } else {
            Log.d(TAG, "데이터베이스를 먼저 오픈하세요");
        }
    }

}