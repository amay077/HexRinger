package com.amay077.android.maps;


import android.content.Context;
import android.graphics.Canvas;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class MyLocationOverlayEx extends MyLocationOverlay {

    private MapView mv;

    public MyLocationOverlayEx(Context context, MapView mapView) {
        super(context, mapView);
        this.mv = mapView;
    }

    @Override
    public synchronized boolean draw(Canvas canvas, MapView mapView,
            boolean shadow, long when) {
        boolean ret = super.draw(canvas, mapView, shadow, when);

        drawMyLocation(canvas, mv, getLastFix(), getMyLocation(), 5000);

        return ret;
    }
}