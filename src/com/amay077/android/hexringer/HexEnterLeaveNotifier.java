package com.amay077.android.hexringer;

import java.util.SortedSet;
import java.util.TreeSet;

import net.geohex.GeoHex;
import android.location.Location;
import android.location.LocationManager;

import com.amay077.android.location.TimeoutableLocationListener;
import com.amay077.android.logging.Log;

public class HexEnterLeaveNotifier extends TimeoutableLocationListener {

	private String[] notifyHexes;
	private String lastHex;
	private HexEnterLeaveListender hexEnterLeaveListener = null;

	public HexEnterLeaveNotifier(LocationManager locaMan, long timeOutMS,
			TimeoutLisener timeoutListener, String[] notifyHexes, String lastHex, HexEnterLeaveListender enterLeaveListener) {
		super(locaMan, timeOutMS, timeoutListener);
		Log.d(this.getClass().getSimpleName(), "ctor called.");

		this.notifyHexes = notifyHexes;
		this.lastHex = lastHex;
		this.hexEnterLeaveListener = enterLeaveListener;
	}

	public void onLocationChanged(Location location) {
		try {
			super.onLocationChanged(location);
			Log.d(this.getClass().getSimpleName(), "onLocationChanged called.");

			// Valid location (WiFi location big changes, Hardware bug, etc...)
			// if (!vaildLocation()) return;

			Log.d(this.getClass().getSimpleName(), "onLocationChanged " +
					"lat/long/accuracy:"
					+ String.valueOf(location.getLatitude()) + "/"
					+ String.valueOf(location.getLongitude()) + "/"
					+ String.valueOf(location.getAccuracy()));

			// Get hit hexes in current location and accuracy, order by nearby
			GeoHex.Zone[] hitHexes = getIntersectGeoHexes(notifyHexes, location);
			String hitHex = hitHexes.length > 0 ? hitHexes[0].code : null;

			if (lastHex == null) {
				if (hitHex == null) {
					// Out to out. Do nothing.
				} else {
					// Out to in. Enter.
					enterHex(hitHex);
				}
			} else {
				if (lastHex.equals(hitHex)) {
					// In to in. Do nothing.
					Log.e(this.getClass().getSimpleName(), "onLocationChanged still in hex:" + hitHex);
				} else if (hitHex == null) {
					// In to out. Leave.
					leaveHex(lastHex);
				} else {
					// Hex内→Hex外→別Hex内
					// In -> out -> in other hex. Leave and enter.
					leaveAndEnterHex(lastHex, hitHex);
				}
			}
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), "onLocationChanged failed.");
		}
	}


	private void enterHex(String hitHex) {
		lastHex = hitHex;
		if (hexEnterLeaveListener != null) {
			hexEnterLeaveListener.onEnter(hitHex);
		}
	}
	private void leaveHex(String lastHex) {
		this.lastHex = lastHex;
		if (hexEnterLeaveListener != null) {
			hexEnterLeaveListener.onLeave(lastHex);
		}
	}
	private void leaveAndEnterHex(String lastHex, String hitHex) {
		this.lastHex = hitHex;
		if (hexEnterLeaveListener != null) {
			hexEnterLeaveListener.onLeave(lastHex);
			hexEnterLeaveListener.onEnter(hitHex);
		}
	}


	private GeoHex.Zone[] getIntersectGeoHexes(String[] geoHexes, Location location) {
		SortedSet<GeoHex.Zone> intersectsZones = new TreeSet<GeoHex.Zone>();
		for (String geoHex : geoHexes) {
			GeoHex.Zone zone = GeoHex.decode(geoHex);

			double x = location.getLongitude();
			double y = location.getLatitude();
			double radius = location.getAccuracy();

			float[] results = new float[1];
			Location.distanceBetween(
					location.getLatitude(), location.getLongitude(),
					location.getLatitude(), location.getLongitude() + 0.01d,
					results);
			float meterPerDegree = results[0] * 100f;

			Log.d(this.getClass().getSimpleName(), "getIntersectGeoHexes " +
					"radiumMetre/degree:" + String.valueOf(radius) + "/"
					+ String.valueOf(radius / meterPerDegree));

			if (zone.intersects(x, y, radius / meterPerDegree)) {
				intersectsZones.add(zone);
			}
		}

		GeoHex.Zone[] zoneArray = new GeoHex.Zone[intersectsZones.size()];
		return intersectsZones.toArray(zoneArray);
	}

	public interface HexEnterLeaveListender {
		void onEnter(String enterHex);
		void onLeave(String leaveHex);
	}
}
