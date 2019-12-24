/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.myproject3;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.appcompat.widget.SearchView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import android.Manifest;

import android.content.pm.PackageManager;

import android.net.Uri;

import android.provider.Settings;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import java.io.IOException;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * The only activity in this sample.
 * <p>
 * Note: Users have three options in "Q" regarding location:
 * <ul>
 * <li>Allow all the time</li>
 * <li>Allow while app is in use, i.e., while app is in foreground</li>
 * <li>Not allow location at all</li>
 * </ul>
 * Because this app creates a foreground service (tied to a Notification) when the user navigates
 * away from the app, it only needs location "while in use." That is, there is no need to ask for
 * location all the time (which requires additional permissions in the manifest).
 * <p>
 * "Q" also now requires developers to specify foreground service type in the manifest (in this
 * case, "location").
 * <p>
 * Note: For Foreground Services, "P" requires additional permission in manifest. Please check
 * project manifest for more information.
 * <p>
 * Note: for apps running in the background on "O" devices (regardless of the targetSdkVersion),
 * location may be computed less frequently than requested when the app is not in the foreground.
 * Apps that use a foreground service -  which involves displaying a non-dismissable
 * notification -  can bypass the background location limits and request location updates as before.
 * <p>
 * This sample uses a long-running bound and started service for location updates. The service is
 * aware of foreground status of this activity, which is the only bound client in
 * this sample. After requesting location updates, when the activity ceases to be in the foreground,
 * the service promotes itself to a foreground service and continues receiving location updates.
 * When the activity comes back to the foreground, the foreground service stops, and the
 * notification associated with that foreground service is removed.
 * <p>
 * While the foreground service notification is displayed, the user has the option to launch the
 * activity from the notification. The user can also remove location updates directly from the
 * notification. This dismisses the notification and stops the service.
 */
public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // Tracks the state of onMapReady
    private boolean mMapStatus = false;

    // UI elements.
    private FloatingActionButton mRequestLocationUpdatesButton;
    private FloatingActionButton mRemoveLocationUpdatesButton;
    private FloatingActionButton mClearButton;
    private FloatingActionButton mRouteButton;
    private FloatingActionButton fab;
    private FloatingActionButton fab5;

    private Animation fab_open, fab_close, route_open, route_close, rotate_forward, rotate_backward,
            next_open, next_close;
    private Boolean isFabOpen = false;
    private Boolean isRouteOpen = false;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    private GoogleMap mMap;

    ArrayList<Double> latitudeArr;
    ArrayList<Double> longitudeArr;
    ArrayList<String> timeArr;

    ArrayList<Marker> MarkerArr = new ArrayList<>();

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int DEFAULT_ZOOM = 15;
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private final LatLng mDefaultLocation = new LatLng(37.56, 126.97);
    private LatLng searchLatLng;

    private Marker mMarker = null;
    private Polyline mPolyline = null;

    private int markerIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
        myReceiver = new MyReceiver();
        setContentView(R.layout.activity_main);

        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        AppHelper.openDatabase(getApplicationContext(), "localList");
        AppHelper.createTable("myLocation");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMapStatus = true;

        if (checkPermissions()) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestPermissions();
        }

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                markerIndex = 0;
            }
        });


        latitudeArr = AppHelper.selectLatitude("myLocation");
        longitudeArr = AppHelper.selectLongitude("myLocation");
        timeArr = AppHelper.selectTime("myLocation");

        for (int i = 0; i < latitudeArr.size(); i++) {
            Geocoder mGeoCoder = new Geocoder(getApplicationContext());
            try {
                List<Address> mResultList = mGeoCoder.getFromLocation(latitudeArr.get(i),
                        longitudeArr.get(i), 1);
                LatLng currentLatLng = new LatLng(latitudeArr.get(i), longitudeArr.get(i));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(currentLatLng);
                String markerTitle = mResultList.get(0).getAddressLine(0);
                String markerSnippet = timeArr.get(i);
                markerOptions.title(markerTitle);
                markerOptions.snippet(markerSnippet);
                markerOptions.draggable(false);

                Marker marker = mMap.addMarker(markerOptions);
                MarkerArr.add(marker);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        getDeviceLocation();

    }


    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (checkPermissions()) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        fab = findViewById(R.id.fab);
        mRequestLocationUpdatesButton = findViewById(R.id.fab1);
        mRemoveLocationUpdatesButton = findViewById(R.id.fab2);
        mClearButton = findViewById(R.id.fab3);
        mRouteButton = findViewById(R.id.fab4);
        fab5 = findViewById(R.id.fab5);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        route_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.route_open);
        route_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.route_close);
        next_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.next_open);
        next_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.next_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

        fab.setOnClickListener(new FABClickListener() {
            @Override
            public void onClick(View v) {
                anim();
            }
        });

        mRouteButton.setOnClickListener(new FABClickListener() {
            @Override
            public void onClick(View v) {
                mPolyline.setVisible(!mPolyline.isVisible());
                if (mPolyline.isVisible()) {
                    LatLng currentLatLng = new LatLng((mLastKnownLocation.getLatitude() + searchLatLng.latitude)/2,
                            (mLastKnownLocation.getLongitude() + searchLatLng.longitude)/2);
                    mMap.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(currentLatLng, 12));
                }
            }
        });

        mClearButton.setOnClickListener(new FABClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                MarkerArr.clear();
                AppHelper.dropTable("myLocation");
                AppHelper.createTable("myLocation");
            }
        });

        mRequestLocationUpdatesButton.setOnClickListener(new FABClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    mService.requestLocationUpdates();
                }
            }
        });

        mRemoveLocationUpdatesButton.setOnClickListener(new FABClickListener() {
            @Override
            public void onClick(View view) {
                mService.removeLocationUpdates();
            }
        });

        fab5.setOnClickListener(new FABClickListener() {
            @Override
            public void onClick(View v) {
                if (MarkerArr.size() != 0) {
                    Marker marker = MarkerArr.get(markerIndex);
                    marker.showInfoWindow();
                    mMap.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(marker.getPosition(), DEFAULT_ZOOM));
                    if (markerIndex == MarkerArr.size() - 1) {
                        markerIndex = 0;
                    } else {
                        markerIndex = markerIndex + 1;
                    }
                }
            }
        });

        // Restore the state of the buttons when the activity (re)launches.
        setButtonsState(Utils.requestingLocationUpdates(this));

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);


        latitudeArr = AppHelper.selectLatitude("myLocation");
        longitudeArr = AppHelper.selectLongitude("myLocation");
        timeArr = AppHelper.selectTime("myLocation");

        if (mMapStatus) {
            MarkerArr.clear();
            for (int i = 0; i < latitudeArr.size(); i++) {
                Geocoder mGeoCoder = new Geocoder(getApplicationContext());
                try {
                    List<Address> mResultList = mGeoCoder.getFromLocation(latitudeArr.get(i),
                            longitudeArr.get(i), 1);
                    LatLng currentLatLng = new LatLng(latitudeArr.get(i), longitudeArr.get(i));
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(currentLatLng);
                    String markerTitle = mResultList.get(0).getAddressLine(0);
                    String markerSnippet = timeArr.get(i);
                    markerOptions.title(markerTitle);
                    markerOptions.snippet(markerSnippet);
                    markerOptions.draggable(false);

                    Marker marker = mMap.addMarker(markerOptions);
                    MarkerArr.add(marker);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (isRouteOpen) {
            mRequestLocationUpdatesButton.setClickable(false);
            mRemoveLocationUpdatesButton.setClickable(false);
            mClearButton.setClickable(false);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));

        Log.d(TAG, "focus");
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
            } else {
                // Permission denied.
                setButtonsState(false);
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                Geocoder mGeoCoder = new Geocoder(getApplicationContext());
                try {
                    List<Address> mResultList = mGeoCoder.getFromLocation(location.getLatitude(),
                            location.getLongitude(), 1);
                    Toast.makeText(MainActivity.this, mResultList.get(0).getAddressLine(0),
                            Toast.LENGTH_SHORT).show();
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    String markerTitle = mResultList.get(0).getAddressLine(0);
                    String markerSnippet = DateFormat.getDateTimeInstance().format(new Date());

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(currentLatLng);
                    markerOptions.title(markerTitle);
                    markerOptions.snippet(markerSnippet);
                    markerOptions.draggable(false);

                    Marker marker = mMap.addMarker(markerOptions);
                    MarkerArr.add(marker);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false));
        }
    }

    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mRequestLocationUpdatesButton.setEnabled(false);
            mRemoveLocationUpdatesButton.setEnabled(true);
        } else {
            mRequestLocationUpdatesButton.setEnabled(true);
            mRemoveLocationUpdatesButton.setEnabled(false);
        }
    }

    class FABClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                if (!isFabOpen) {
                    mRouteButton.startAnimation(route_open);
                    mRouteButton.setClickable(true);
                    fab5.startAnimation(next_close);
                    fab5.setClickable(false);
                }
                mRouteButton.setEnabled(false);
                isRouteOpen = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                if (mMarker != null) {
                    mMarker.remove();
                }
                if (mPolyline != null) {
                    mPolyline.remove();
                }
                if (!isFabOpen) {
                    mRouteButton.startAnimation(route_close);
                    mRouteButton.setClickable(false);
                    fab5.startAnimation(next_open);
                    fab5.setClickable(true);
                }
                isRouteOpen = false;
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                Geocoder mGeoCoder = new Geocoder(getApplicationContext());
                try {
                    if (mMarker != null) {
                        mMarker.remove();
                    }
                    if (mPolyline != null) {
                        mPolyline.remove();
                    }
                    List<Address> mResultLocation = mGeoCoder.getFromLocationName(s, 1);
                    searchLatLng = new LatLng(mResultLocation.get(0).getLatitude(),
                            mResultLocation.get(0).getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(searchLatLng);
                    String markerTitle = mResultLocation.get(0).getAddressLine(0);
                    markerOptions.title(markerTitle);
                    markerOptions.draggable(false);

                    mMarker = mMap.addMarker(markerOptions);
                    mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(searchLatLng, DEFAULT_ZOOM));
                    ArrayList<LatLng> pathList = new ArrayList<>();
                    pathList.add(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                    pathList.add(searchLatLng);
                    drawCurrentPath(pathList);
                    if (mPolyline != null) {
                        mRouteButton.setEnabled(true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return true;
    }

    public void drawCurrentPath(ArrayList<LatLng> checkedLocations){
        RoadTracker mRoadTracker = new RoadTracker();
        ArrayList<LatLng> jsonData = mRoadTracker.getJsonData(checkedLocations.get(0), checkedLocations.get(1));
        if (jsonData == null) {
            return;
        }
        PolylineOptions options = new PolylineOptions().width(12).color(Color.argb(230,238, 78, 139)).visible(false);
        options.addAll(jsonData);
        mPolyline = mMap.addPolyline(options);
    }

    public void anim() {
        if (isFabOpen) {
            fab.startAnimation(rotate_backward);
            mRequestLocationUpdatesButton.startAnimation(fab_close);
            mRemoveLocationUpdatesButton.startAnimation(fab_close);
            mClearButton.startAnimation(fab_close);
            mRequestLocationUpdatesButton.setClickable(false);
            mRemoveLocationUpdatesButton.setClickable(false);
            mClearButton.setClickable(false);
            if (isRouteOpen) {
                mRouteButton.startAnimation(route_open);
                mRouteButton.setClickable(true);
            } else {
                fab5.startAnimation(next_open);
                fab5.setClickable(true);
            }
            isFabOpen = false;
        } else {
            fab.startAnimation(rotate_forward);
            mRequestLocationUpdatesButton.startAnimation(fab_open);
            mRemoveLocationUpdatesButton.startAnimation(fab_open);
            mClearButton.startAnimation(fab_open);
            mRequestLocationUpdatesButton.setClickable(true);
            mRemoveLocationUpdatesButton.setClickable(true);
            mClearButton.setClickable(true);
            if (isRouteOpen) {
                mRouteButton.startAnimation(route_close);
                mRouteButton.setClickable(false);
            } else {
                fab5.startAnimation(next_close);
                fab5.setClickable(false);
            }
            isFabOpen = true;
        }
    }
}

