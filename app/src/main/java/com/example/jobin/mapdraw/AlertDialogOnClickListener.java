package com.example.jobin.mapdraw;

import android.content.DialogInterface;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Jobin on 4/3/16.
 */

public class AlertDialogOnClickListener implements DialogInterface.OnClickListener {
    private LatLng point;
    private MarkerOptions marker;
    private GoogleMap mMap;

    public AlertDialogOnClickListener() {
        super();
    }

    public AlertDialogOnClickListener(LatLng point, MarkerOptions marker, GoogleMap mMap) {
        super();
        this.point = point;
        this.marker = marker;
        this.mMap = mMap;
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                //add marker
                marker.position(point)
                        .draggable(true);
                mMap.addMarker(marker);
                //Toast.makeText(getApplicationContext(), "you have pressed Default", Toast.LENGTH_SHORT).show();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                //add custom marker
                BitmapDescriptor custom_icon = BitmapDescriptorFactory.fromResource(R.drawable.map_marker_flag_icon);
                marker.position(point)
                        .icon(custom_icon)
                        .draggable(true);
                mMap.addMarker(marker);
                //Toast.makeText(getApplicationContext(), "you have pressed Custom", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}