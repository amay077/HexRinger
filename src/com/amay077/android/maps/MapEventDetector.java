package com.amay077.android.maps;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

/**
 * GoogleMap の MapView には onZoom や onScroll がないので、それの代わりにイベントを発生させるクラス。
 *
 * @author h_okuyama
 *
 */
public class MapEventDetector {

	// fields -----------------------------------------------------------------
	//private MapView mapview = null;
	private OnMapEventListener listener = null;
	private GeoPoint center = null;
	private int zoomLevel = 0;

	// ctor -------------------------------------------------------------------
	public MapEventDetector(OnMapEventListener listener) {
		this.listener = listener;
	}

	// setter/getter ----------------------------------------------------------
	// overrides --------------------------------------------------------------
	// public methods ---------------------------------------------------------

	public void onDrawEvent(MapView mapview) {

		// perform onScroll
		GeoPoint center = mapview.getMapCenter();
		if (this.center == null || !this.center.equals(center)) {
			// call onScroll
			if (this.listener != null) { this.listener.onMapCenterChanged(mapview); }

			this.center = center;
		}

		// perform onZoom
		if (this.zoomLevel != mapview.getZoomLevel()) {
			// call onZoom
			if (this.listener != null) { this.listener.onZoomLevelChanged(mapview, this.zoomLevel); }

			this.zoomLevel = mapview.getZoomLevel();
		}

	}

	// private methods --------------------------------------------------------

}
