package com.amay077.android.maps;

import com.google.android.maps.MapView;

public interface OnMapEventListener {
	public abstract void onMapCenterChanged(MapView mapview);

	public abstract void onZoomLevelChanged(MapView mapview, int beforeZoomLevel);
}
