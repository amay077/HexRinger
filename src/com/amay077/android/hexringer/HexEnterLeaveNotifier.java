package com.amay077.android.hexringer;

import java.util.SortedSet;
import java.util.TreeSet;

import net.geohex.GeoHex;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;

import com.amay077.android.hexringer.AlarmBroadcastReceiver.LocationUtil;
import com.amay077.android.hexringer.AlarmBroadcastReceiver.StringUtil;
import com.amay077.android.location.TimeoutableLocationListener;
import com.amay077.android.logging.Log;
import com.amay077.android.preference.PreferenceWrapper;

public class HexEnterLeaveNotifier extends TimeoutableLocationListener {

	private String[] notifyHexes;
	private String lastHex;
	private HexEnterLeaveListender hexEnterLeaveListener = null;
	private Context context = null;
	private PreferenceWrapper pref = null;

	public HexEnterLeaveNotifier(LocationManager locaMan, long timeOutMS,
			TimeoutLisener timeoutListener, String[] notifyHexes, String lastHex,
			Context context,
			HexEnterLeaveListender enterLeaveListener) {
		super(locaMan, timeOutMS, timeoutListener);
		Log.d(this.getClass().getSimpleName(), "ctor called.");

		this.notifyHexes = notifyHexes;
		this.lastHex = lastHex;
		this.hexEnterLeaveListener = enterLeaveListener;
		this.context = context;
		this.pref = new PreferenceWrapper(context);
	}

	public void onLocationChanged(Location location) {
		try {
			super.onLocationChanged(location);
			Log.d(this.getClass().getSimpleName(), "onLocationChanged() called.");

			// TODO  Valid location (WiFi location big changes, Hardware bug, etc...)
			// if (!vaildLocation()) return;

			Log.d(this.getClass().getSimpleName(), "onLocationChanged() " +
					"lat/long/accuracy:"
					+ String.valueOf(location.getLatitude()) + "/"
					+ String.valueOf(location.getLongitude()) + "/"
					+ String.valueOf(location.getAccuracy()));

			// 閾値以上の誤差で、WiFi が有効なら無視する
			if (location.getAccuracy() >= Const.LOCATION_MAX_ACCURACY) {
				try {
					WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
					if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
						Log.d(this.getClass().getSimpleName(), "onLocationChanged() accracy insufficiency.");
						return;
					}
				} catch (Exception e) {
					Log.w(this.getClass().getSimpleName(),
							"onLocationChanged() get WiFi State failed, but continued.", e);
				}
			}

			writeLastLocationToPreference(location);
			sendLocationChangedBroadcastIntent(location);
			Const.showNotify(context, false);

			// Get hit hexes in current location and accuracy, order by nearby
			GeoHex.Zone[] hitHexes = getIntersectGeoHexes(notifyHexes, location);
			String hitHex = hitHexes.length > 0 ? hitHexes[0].code : null;

			if (StringUtil.isNullOrEmpty(lastHex)) {
				if (StringUtil.isNullOrEmpty(hitHex)) {
					// Out to out. Do nothing.
					Log.d(this.getClass().getSimpleName(), "onLocationChanged() location still outside of hex:" + hitHex);
				} else {
					// Out to in. Enter.
					enterHex(hitHex, location);
				}
			} else {
				if (lastHex.equals(hitHex)) {
					// In to in. Do nothing.
					Log.d(this.getClass().getSimpleName(), "onLocationChanged() location still inside of hex:" + hitHex);
				} else if (StringUtil.isNullOrEmpty(hitHex)) {
					// In to out. Leave.
					leaveHex(lastHex, location);
				} else {
					// Hex内→Hex外→別Hex内
					// In -> out -> in other hex. Leave and enter.
					leaveAndEnterHex(lastHex, hitHex, location);
				}
			}
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), "onLocationChanged() failed.");
		}
	}

	private void sendLocationChangedBroadcastIntent(Location location) {
		try {
			Log.d(this.getClass().getSimpleName(), "sendLocationChangedBroadcastIntent() called.");

			Intent intent = new Intent(Const.ACTION_HEXRINGAR_LOCATION_CHANGED);
			intent.putExtra(Const.ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_LAT, location.getLatitude());
			intent.putExtra(Const.ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_LONG, location.getLongitude());
			intent.putExtra(Const.ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_ACCURACY, location.getAccuracy());
			intent.putExtra(Const.ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_TIME, location.getTime());
			context.sendBroadcast(intent);

			Log.d(this.getClass().getSimpleName(), "sendLocationChangedBroadcastIntent() succeeded.");
		} catch (Exception e) {
			Log.d(this.getClass().getSimpleName(), "sendLocationChangedBroadcastIntent() failed.", e);
		}
	}

	private void writeLastLocationToPreference(Location loc) {
		if (loc == null) {
			pref.remove(R.string.pref_last_location_key);
		} else {

			pref.saveString(R.string.pref_last_location_key, LocationUtil.toString(loc));
		}
	}

	private void enterHex(String hitHex, Location location) {
		lastHex = hitHex;
		if (hexEnterLeaveListener != null) {
			hexEnterLeaveListener.onEnter(hitHex, location);
		}
	}
	private void leaveHex(String lastHex, Location location) {
		this.lastHex = lastHex;
		if (hexEnterLeaveListener != null) {
			hexEnterLeaveListener.onLeave(lastHex, location);
		}
	}
	private void leaveAndEnterHex(String lastHex, String hitHex, Location location) {
		this.lastHex = hitHex;
		if (hexEnterLeaveListener != null) {
			hexEnterLeaveListener.onLeave(lastHex, location);
			hexEnterLeaveListener.onEnter(hitHex, location);
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
		void onEnter(String enterHex, Location location);
		void onLeave(String leaveHex, Location location);
	}
}
