package com.example.jobin.mapdraw;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.GoogleMap.OnPolylineClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, OnMapClickListener, OnMapLongClickListener,
        OnRequestPermissionsResultCallback {

    //Request code for location permission request.
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    //Flag indicating whether a requested permission has been denied after returning
    private boolean mPermissionDenied = false;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // Root of the layout of this Activity.
    //private View view;

    private static final String TAG = "MapsActivity";
    private double distance;
    private boolean placeMarkerFlag;
    private GoogleMap mMap;
    private ArrayList<LatLng> arrayPoints;
    private ArrayList<ArrayList<LatLng>> arrayPointsList;
    private PolylineOptions polylineOptions;
    private Polyline polyline;
    private MarkerOptions markerOptions;
    private LatLng latLng;

    private RadioGroup rgViews;
    private Button placeMarker;
    private Button drawLine;
    private EditText etLocation;
    private TextView polyline_distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //view = findViewById(R.id.main_layout);

        arrayPoints = new ArrayList<LatLng>();
        arrayPointsList = new ArrayList<>();

        // get handle to the fragment and pass it to resource id of the <fragment> element
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        // set the callback on the fragment
        mapFragment.getMapAsync(this);

        /* Getting reference to UI bits in activity_maps */
        etLocation = (EditText) findViewById(R.id.et_location);
        rgViews = (RadioGroup) findViewById(R.id.rg_views);
        placeMarker = (Button) findViewById(R.id.btn_marker);
        drawLine = (Button) findViewById(R.id.btn_line);
        polyline_distance = (TextView) findViewById(R.id.line_distance);

        // Defining editText search event listener for the editText
        OnEditorActionListener findClickListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // Getting user input location
                    String location = etLocation.getText().toString();

                    if (location != null && !location.equals("")) {
                        InputMethodManager imm = (InputMethodManager) v.getContext()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        new GeocoderTask(latLng, markerOptions, mMap, v.getContext()).execute
                                (location);
                    }
                    return true;
                }
                return false;
            }
        };

        // Setting editText search click event listener for the keyboard search icon
        etLocation.setOnEditorActionListener(findClickListener);

        setMapTypeOptions();
        setPlaceMarkerListener();
        setDrawLineListener();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (checkPlayServices()) {
            mMap = googleMap;

            enableMyLocation();

            // Sets default map type to be "hybrid"
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

            mMap.getUiSettings().setZoomControlsEnabled(true);

            mMap.setOnMapClickListener(this);
            mMap.setOnMapLongClickListener(this);
            mMap.setOnPolylineClickListener(new OnPolylineClickListener() {
                @Override
                public void onPolylineClick(Polyline polyline) {
                    distance = SphericalUtil.computeLength(polyline.getPoints());
                    polyline_distance.setText("Distance: " +
                            formatNumber(distance));
                }
            });

            mMap.setOnMarkerDragListener(new OnMarkerDragListener() {

                @Override
                public void onMarkerDragStart(Marker marker) {
                    // simulate long click to delete marker
                    //marker.remove();
                    // TODO Auto-generated method stub
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onMarkerDrag(Marker marker) {

                    // Getting the current position of the marker
                    LatLng pos = marker.getPosition();

                    // Updating the infowindow contents with the new marker coordinates
                    marker.setSnippet(String.format("%.4f", pos.latitude) + "," + String.format("%" +
                            ".4f", pos.longitude));

                    // Updating the infowindow for the user
                    marker.showInfoWindow();

                }
            });
        }
    }

    // Enables the My Location layer if the fine location permission has been granted.
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.i(TAG, "LOCATION permission has NOT been granted.");

            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
            //requestLocationPermission();

        } else {
            Log.v(TAG, "LOCATION permission has BEEN granted. Enabling MyLocation");
            if (mMap != null) {
                // Access to the location has been granted to the app.
                mMap.setMyLocationEnabled(true);
                Log.i(TAG, "location enabled");
            }
        }
    }


    /*
     * Callback received when a permissions request has been completed.
    */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Received permission result for location permission.
            Log.i(TAG, "Received response for Location permission request.");

            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Enable the my location layer if the permission has been granted.
                enableMyLocation();
            } else {
                // Display the missing permission error dialog when the fragments resume.
                mPermissionDenied = true;
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    //Displays a dialog with error message explaining that the location permission is missing.
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    private void setMapTypeOptions() {
        rgViews.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_normal) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else if (checkedId == R.id.rb_hybrid) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                } else if (checkedId == R.id.rb_satellite) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else if (checkedId == R.id.rb_terrain) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                }
            }
        });
    }

    private void setPlaceMarkerListener() {
        placeMarker.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                placeMarkerFlag = true;
            }

        });
    }

    private void setDrawLineListener() {
        drawLine.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Save and Reset last drawn line, if it exists
                if (arrayPoints != null && arrayPoints.size() > 0) {
                    arrayPointsList.add(arrayPoints);
                    arrayPoints = new ArrayList<LatLng>();
                }
                placeMarkerFlag = false;
            }

        });
    }


    @Override
    public void onMapClick(final LatLng point) {

        if (placeMarkerFlag) {
            // place marker only
            drawMarker(point);
        } else {
            // draw lines to connect the points on map
            drawLine(point);
        }

    }

    private void drawMarker(LatLng point) {
        // Creating an instance of MarkerOptions
        MarkerOptions marker = new MarkerOptions();

        // Setting latitude and longitude for the marker
        marker.position(point);

        // Making this marker draggable
        marker.draggable(true);

        // Title for this marker
        marker.title("Marker Coordinates");

        // Coordinates for this marker as infowindow contents
        marker.snippet(String.format("%.4f", point.latitude) + "," + String.format("%.4f", point
                .longitude));

        AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Default Icon", new
                AlertDialogOnClickListener(point, marker, mMap));
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Custom Icon", new
                AlertDialogOnClickListener(point, marker, mMap));
        alertDialog.show();
    }

    private void drawLine(LatLng point) {

        // Creating an instance of Polyline options
        polylineOptions = new PolylineOptions();

        // Setting color and width of Polyline on map
        polylineOptions.color(Color.RED);
        polylineOptions.width(5);

        // TODO: distance of each line segment if needed.
        //double distance = SphericalUtil.computeDistanceBetween(pointA.getPosition(), pointB.getPosition());

        // Adding points to array to draw line and add line to google map
        arrayPoints.add(point);

        polylineOptions.addAll(arrayPoints);
        polyline = mMap.addPolyline(polylineOptions);
        polyline.setClickable(true);
    }

    private String formatNumber(double distance) {
        String unit = "m";
        if (distance < 1) {
            distance *= 1000;
            unit = "mm";
        } else if (distance > 1000) {
            distance /= 1000;
            unit = "km";
        }

        return String.format("%4.3f%s", distance, unit);
    }

    @Override
    public void onMapLongClick(LatLng point) {
        mMap.clear();
        arrayPoints.clear();
        distance = 0.0;
        polyline_distance.setText("Distance: " +
                formatNumber(distance));
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
