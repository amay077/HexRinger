package com.amay077.android.hexringer;

import java.util.SortedSet;
import java.util.TreeSet;

import net.geohex.GeoHex;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.amay077.android.location.TimeoutableLocationListener;

public class HexEnterLeaveNotifier extends TimeoutableLocationListener {

	private String[] notifyHexes;
	private String lastHex;
	private HexEnterLeaveListender hexEnterLeaveListener = null;

	public HexEnterLeaveNotifier(LocationManager locaMan, long timeOutMS,
			TimeoutLisener timeoutListener, String[] notifyHexes, String lastHex, HexEnterLeaveListender enterLeaveListener) {

		super(locaMan, timeOutMS, timeoutListener);

		this.notifyHexes = notifyHexes;
		this.lastHex = lastHex;
		this.hexEnterLeaveListener = enterLeaveListener;
	}

	public void onLocationChanged(Location location) {
		super.onLocationChanged(location);
		Log.e("HexEnterLeaveNotifier.onLocationChanged", "called.");
		try {
			// Valid location (WiFi location big changes, Hardware bug, etc...)
			// if (!vaildLocation()) return;

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
				if (lastHex == hitHex) {
					// In to in. Do nothing.
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
			Log.e("HexEnterLeaveNotifier.onLocationChanged", "failed.", e);
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

			double x = 0d, y = 0d, radius = 0d;
			if (zone.intersects(x, y, radius)) {
				intersectsZones.add(zone);
			}
		}

		return (GeoHex.Zone[])intersectsZones.toArray();
	}

	public interface HexEnterLeaveListender {
		void onEnter(String enterHex);
		void onLeave(String leaveHex);
	}
}
