package com.amay077.android.hexringer;

import java.text.SimpleDateFormat;
import java.util.Set;

import net.geohex.GeoHex;

import com.amay077.android.hexringer.AlarmBroadcastReceiver.LocationUtil;
import com.amay077.android.hexringer.AlarmBroadcastReceiver.StringUtil;
import com.amay077.android.hexringer.R;
import com.amay077.android.location.TimeoutableLocationListener;
import com.amay077.android.location.TimeoutableLocationListener.TimeoutLisener;
import com.amay077.android.logging.Log;
import com.amay077.android.maps.CurrentLocationOverlay;
import com.amay077.android.maps.GeoHexOverlay;
import com.amay077.android.preference.PreferenceWrapper;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


/** HexRinger Main Activity */
public class MainActivity extends MapActivity {
    private static final int MENU_ID_RECENT_LOCATION = (Menu.FIRST + 1);
    private static final int MENU_ID_WATCH_HEXES = (Menu.FIRST + 2);
    private static final int MENU_ID_CONFIG = (Menu.FIRST + 3);

    // Fields
    private PreferenceWrapper pref = null;
    private BroadcastReceiver locationChangedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Log.d("locationChangedReceiver", "onReceive() called.");

				if (intent.getAction().equals(Const.ACTION_HEXRINGAR_LOCATION_CHANGED) &&
					currentLocOverlay != null) {
					// Change MapView current position.
					Location loc = new Location("");
					loc.setLatitude(intent.getDoubleExtra(Const.ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_LAT, 0d));
					loc.setLongitude(intent.getDoubleExtra(Const.ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_LONG, 0d));
					loc.setAccuracy(intent.getFloatExtra(Const.ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_ACCURACY, 0f));
					loc.setTime(intent.getLongExtra(Const.ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_TIME, 0));

					updateCurrentLocation(loc);
				}
			} catch (Exception e) {
				Log.w("locationChangedReceiver", "onReceive() failed.", e);
			}
		}
	};

    // UI components
    private MapView mapview = null;
//    private MyLocationOverlay myLocOverlay = null;
    private GeoHexOverlay watchHexOverlay = new GeoHexOverlay();
    private CurrentLocationOverlay currentLocOverlay = null;
    private Button buttonStartMonitoring = null;
    private Button buttonStopMonitoring = null;

    // UI event handler
    /** モニタリング開始ボタンが押されたときの処理 */
    private View.OnClickListener buttonStartMonitoring_onClick = new View.OnClickListener() {

        public void onClick(View v) {
        	try {
				Log.d("MainActivity.OnClickListener", "buttonStartMonitoring_onClick() called.");

				// TODO : Is selecting Hex?
				Set<String> watchHexesSet = watchHexOverlay.getSelectedGeoHexCodes();
				if (watchHexesSet.size() == 0) {
					Toast.makeText(MainActivity.this,
							"エリアを選択してください",
							Toast.LENGTH_SHORT).show();
					return;
				}

				pref.saveBoolean(R.string.pref_alarm_enabled_key, true);

				Const.setNextAlarm(MainActivity.this,
						0, false); // 初回はすぐに開始
				toggleMonitoringButton(pref.getBoolean(R.string.pref_alarm_enabled_key, false));

				Toast.makeText(MainActivity.this,
						"マナーモードの自動設定を開始しました",
						Toast.LENGTH_SHORT).show();
        	} catch (Exception e) {
				Log.w("MainActivity.OnClickListener", "buttonStartMonitoring_onClick() failed.", e);
			}
        }
    };

    private View.OnClickListener buttonStopMonitoring_onClick = new View.OnClickListener() {

        public void onClick(View v) {
            try {
				Log.d("MainActivity.OnClickListener", "buttonStopMonitoring_onClick() called.");
				Const.cancelAlarmManager(MainActivity.this);

				pref.saveBoolean(R.string.pref_alarm_enabled_key, false);
				toggleMonitoringButton(pref.getBoolean(R.string.pref_alarm_enabled_key, false));

				Toast.makeText(MainActivity.this,
						"マナーモードの自動設定を停止しました",
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Log.w("MainActivity.OnClickListener", "buttonStopMonitoring_onClick() failed.", e);
			}
        }
    };

    private GeoHexOverlay.OnTapHexListener onTapHexListener = new GeoHexOverlay.OnTapHexListener() {

		@Override
		public void onTap(GeoHexOverlay sender, String hexCode) {

			try {
				Log.d("MainActivity.OnTapHexListener", "watchHexOverlay_onTap() called.");
				Log.d("MainActivity.OnTapHexListener", "watchHexOverlay_onTap() hex:" + hexCode);
				if (pref.getBoolean(R.string.pref_alarm_enabled_key, false)) {
					Toast.makeText(MainActivity.this,
							"エリアを選択するには、[STOP] をして下さい",
							Toast.LENGTH_SHORT).show();
					return;
				}

				// 選択 or 選択解除
				Set<String> watchHexesSet = watchHexOverlay.getSelectedGeoHexCodes();
				Set<String> hexCodes = sender.getSelectedGeoHexCodes();
				if (hexCodes.contains(hexCode)) {
					hexCodes.remove(hexCode);
				} else {
					hexCodes.clear();
					hexCodes.add(hexCode);
				}

				String prevWatchHexes = pref.getString(R.string.pref_watch_hexes_key, "");
				String[] watchHexes = new String[watchHexesSet.size()];
				watchHexesSet.toArray(watchHexes);
				String newWatchHexed = StringUtil.fromArray(watchHexes, Const.ARRAY_SPLITTER);

				// 監視する Hex が変わったら、前回位置を破棄する
				if (!prevWatchHexes.equals(newWatchHexed)) {
				    pref.saveString(R.string.pref_last_hex_key, "");
				}

				pref.saveString(R.string.pref_watch_hexes_key, newWatchHexed);

				// 再描画
				mapview.invalidate();
			} catch (Exception e) {
				Log.w("MainActivity.OnTapHexListener", "watchHexOverlay_onTap() failed.", e);
			}
		}
	};

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.d(getApplication().getPackageName(), "started.");
        Log.writeApplicationInfo(this);
        Log.d(this.getClass().getSimpleName(), "onCreate called.");


        initializeUI();

        currentLocOverlay = new CurrentLocationOverlay(
        		getResources().getDrawable(R.drawable.currentlocation));
        watchHexOverlay.setOnTapHexListener(onTapHexListener);
        watchHexOverlay.setBitmap(
        		BitmapFactory.decodeResource(getResources(), R.drawable.ringer_map));

        mapview.getOverlays().add(watchHexOverlay);
        mapview.getOverlays().add(currentLocOverlay);
        pref = new PreferenceWrapper(this.getApplicationContext());

        toggleMonitoringButton(pref.getBoolean(R.string.pref_alarm_enabled_key, false));

        Set<String> watchHexes = watchHexOverlay.getSelectedGeoHexCodes();
        String watchHexesStr = pref.getString(R.string.pref_watch_hexes_key, null);
        if (!StringUtil.isNullOrEmpty(watchHexesStr)) {
	        String[] array = StringUtil.toArray(watchHexesStr, Const.ARRAY_SPLITTER);
	        for (String string : array) {
	        	watchHexes.add(string);
			}
        }

        moveToRecentLocation();
    }

    private void beginPanningToCurrentLocation() {
		LocationManager locaMan = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

		TimeoutLisener timeOutListener = new TimeoutLisener() {
			@Override
			public void onTimeouted(LocationListener sender) {
				Toast.makeText(MainActivity.this,
						"位置の取得ができませんでした。電波の届く場所で再度お試し下さい",
						Toast.LENGTH_SHORT).show();
				return;
			}
		};

		locaMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
			new TimeoutableLocationListener(locaMan, Const.LOCATION_REQUEST_TIMEOUT_MS, timeOutListener) {
				@Override
				public void onLocationChanged(Location location) {
					super.onLocationChanged(location);
					updateCurrentLocation(location);
				}
			}
		);
    }

    private void initializeUI() {
        mapview = (MapView)findViewById(R.id.mapView);
        mapview.setBuiltInZoomControls(true);

        buttonStartMonitoring = (Button)findViewById(R.id.ButtonStartMonitoring);
        buttonStartMonitoring.setOnClickListener(buttonStartMonitoring_onClick);

        buttonStopMonitoring = (Button)findViewById(R.id.ButtonStopMonitoring);
        buttonStopMonitoring.setOnClickListener(buttonStopMonitoring_onClick);

    }

	private void updateCurrentLocation(Location loc) {
		if (loc == null || loc.getLatitude() == 0
				|| loc.getLongitude() == 0 || loc.getAccuracy() == 0) {
			return;
		}

		setTitle("HexRinger (最終位置確認:" + new SimpleDateFormat("H時mm分").format(loc.getTime()) + ")");

		currentLocOverlay.setCurrentLocation(loc);
		MapController controller = mapview.getController();
		controller.animateTo(new GeoPoint((int)(loc.getLatitude() * 1E6), (int)(loc.getLongitude() * 1E6)));
	}

    private void toggleMonitoringButton(boolean enabledAlarm) {
        buttonStartMonitoring.setVisibility(enabledAlarm ? View.GONE : View.VISIBLE);
        buttonStopMonitoring.setVisibility(enabledAlarm ? View.VISIBLE : View.GONE);
    }

    private void moveToRecentLocation() {
        Location lastLocation = LocationUtil.fromString(
        		pref.getString(R.string.pref_last_location_key, ""));
        if (lastLocation != null && pref.getBoolean(R.string.pref_alarm_enabled_key, false)) {
        	updateCurrentLocation(lastLocation);
        } else {
        	beginPanningToCurrentLocation();
        }
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    // オプションメニューが最初に呼び出される時に1度だけ呼び出されます
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(this.getClass().getSimpleName(), "onCreateOptionsMenu called.");
        // メニューアイテムを追加します
        menu.add(Menu.NONE, MENU_ID_RECENT_LOCATION, Menu.NONE, "最新の位置").setIcon(R.drawable.earth);
        menu.add(Menu.NONE, MENU_ID_WATCH_HEXES, Menu.NONE, "監視エリア").setIcon(R.drawable.hex);
        menu.add(Menu.NONE, MENU_ID_CONFIG, Menu.NONE, "設定").setIcon(R.drawable.gears);
        return super.onCreateOptionsMenu(menu);
    }


    /** オプションメニューアイテムが選択された時に呼び出されます */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(this.getClass().getSimpleName(), "onOptionsItemSelected called.");
        boolean ret = true;
        switch (item.getItemId()) {
        default:
            ret = super.onOptionsItemSelected(item);
            break;
        case MENU_ID_RECENT_LOCATION:
        	moveToRecentLocation();

            ret = true;
            break;
        case MENU_ID_WATCH_HEXES:
            String watchHexesStr = pref.getString(R.string.pref_watch_hexes_key, null);
            if (!StringUtil.isNullOrEmpty(watchHexesStr)) {
    	        String[] array = StringUtil.toArray(watchHexesStr, Const.ARRAY_SPLITTER);
    	        for (String string : array) {
    	        	GeoHex.Zone zone = GeoHex.decode(string);
    	        	mapview.getController().animateTo(new GeoPoint((int)(zone.lat * 1E6), (int)(zone.lon * 1E6)));
    	        	break;
    			}
            }

            ret = true;
            break;
        case MENU_ID_CONFIG:
        	Intent intent = new Intent(this, HexRingerPreferenceActivity.class);
        	startActivity(intent);
            ret = true;
            break;
        }
        return ret;
    }

    @Override
    protected void onPause() {
    	super.onPause();
        Log.d(this.getClass().getSimpleName(), "onPause called.");

    	// register Receiver
        unregisterReceiver(locationChangedReceiver);
    }

    @Override
    protected void onResume() {
    	super.onResume();
        Log.d(this.getClass().getSimpleName(), "onResume called.");

    	// register Receiver
        registerReceiver(locationChangedReceiver,
        		new IntentFilter(Const.ACTION_HEXRINGAR_LOCATION_CHANGED));

        moveToRecentLocation();
    }

    @Override
    protected void onStart() {
        Log.d(this.getClass().getSimpleName(), "onStart called.");
    	super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.d(this.getClass().getSimpleName(), "onRestart called.");
    	super.onRestart();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(this.getClass().getSimpleName(), "onRestoreInstanceState called.");
    	super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStop() {
        Log.d(this.getClass().getSimpleName(), "onStop called.");
    	super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(this.getClass().getSimpleName(), "onDestroy called.");
    	super.onDestroy();
        Log.d(getApplication().getPackageName(), "finished.");
    }

	@Override
	protected void finalize() throws Throwable {
		Log.d(this.getClass().getSimpleName(), "finalize called.");
		super.finalize();
	}
}
