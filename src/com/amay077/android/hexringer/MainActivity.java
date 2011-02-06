package com.amay077.android.hexringer;

import java.util.Set;

import com.amay077.android.hexringer.AlarmBroadcastReceiver.LocationUtil;
import com.amay077.android.hexringer.AlarmBroadcastReceiver.StringUtil;
import com.amay077.android.hexringer.R;
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
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


/** HexRinger Main Activity */
public class MainActivity extends MapActivity {
    private static final int MENU_ID_START = (Menu.FIRST + 1);
    private static final int MENU_ID_CONFIG = (Menu.FIRST + 2);

    // Fields
    private PreferenceWrapper pref = null;
    private BroadcastReceiver locationChangedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("locationChangedReceiver", "onReceive() called.");

			if (intent.getAction().equals(Const.ACTION_HEXRINGAR_LOCATION_CHANGED) &&
				currentLocOverlay != null) {
				// TODO: Change MapView current position.

				Location loc = new Location("");
				loc.setLatitude(intent.getDoubleExtra(Const.ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_LAT, 0d));
				loc.setLongitude(intent.getDoubleExtra(Const.ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_LONG, 0d));
				loc.setAccuracy(intent.getFloatExtra(Const.ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_ACCURACY, 0f));

				updateCurrentLocation(loc);
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
            Log.d(this.getClass().getSimpleName(), "buttonStartMonitoring_onClick called.");

            // TODO : Is selecting Hex?
        	Set<String> watchHexesSet = watchHexOverlay.getSelectedGeoHexCodes();
        	String[] watchHexes = new String[watchHexesSet.size()];
        	watchHexesSet.toArray(watchHexes);

            pref.saveBoolean(R.string.pref_alarm_enabled_key, true);

            String prevWatchHexes = pref.getString(R.string.pref_watch_hexes_key, "");
            String newWatchHexed = StringUtil.fromArray(watchHexes, Const.ARRAY_SPLITTER);

            // 監視する Hex が変わったら、前回位置を破棄する
            if (!prevWatchHexes.equals(newWatchHexed)) {
                pref.saveString(R.string.pref_last_hex_key, "");
            }

            pref.saveString(R.string.pref_watch_hexes_key, newWatchHexed);

            Const.setNextAlarm(MainActivity.this,
            		pref.getAsInt(R.string.pref_watchinterval_key,
            				getString(R.string.pref_watchinterval_default)));
            toggleMonitoringButton(pref.getBoolean(R.string.pref_alarm_enabled_key, false));
        }
    };

    private View.OnClickListener buttonStopMonitoring_onClick = new View.OnClickListener() {

        public void onClick(View v) {
            Log.d(this.getClass().getSimpleName(), "buttonStopMonitoring_onClick called.");
        	Const.cancelAlarmManager(MainActivity.this);

            pref.saveBoolean(R.string.pref_alarm_enabled_key, false);
            toggleMonitoringButton(pref.getBoolean(R.string.pref_alarm_enabled_key, false));
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

        mapview.getOverlays().add(watchHexOverlay);
//        myLocOverlay = new MyLocationOverlayEx(this, mapview);
//        mapview.getOverlays().add(myLocOverlay);
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

        Location lastLocation = LocationUtil.fromString(
        		pref.getString(R.string.pref_last_location_key, ""));
        updateCurrentLocation(lastLocation);
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

		currentLocOverlay.setCurrentLocation(loc);
		MapController controller = mapview.getController();
		controller.animateTo(new GeoPoint((int)(loc.getLatitude() * 1E6), (int)(loc.getLongitude() * 1E6)));
	}

    private void toggleMonitoringButton(boolean enabledAlarm) {
        buttonStartMonitoring.setVisibility(enabledAlarm ? View.GONE : View.VISIBLE);
        buttonStopMonitoring.setVisibility(enabledAlarm ? View.VISIBLE : View.GONE);
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
        menu.add(Menu.NONE, MENU_ID_START, Menu.NONE, "開始");
        menu.add(Menu.NONE, MENU_ID_CONFIG, Menu.NONE, "設定");
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
        case MENU_ID_START:
            Toast.makeText(this, "監視を開始！", Toast.LENGTH_SHORT).show();

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
