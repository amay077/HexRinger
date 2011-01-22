package com.amay077.android.hexringer;

import java.util.Set;
import com.amay077.android.hexringer.AlarmBroadcastReceiver.StringUtil;
import com.amay077.android.hexringer.R;
import com.amay077.android.logging.Log;
import com.amay077.android.maps.GeoHexOverlay;
import com.amay077.android.maps.MyLocationOverlayEx;
import com.amay077.android.preference.PreferenceWrapper;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.content.Intent;
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

    // UI components
    private MapView mapview = null;
    private MyLocationOverlay myLocOverlay = null;
    private GeoHexOverlay watchHexOverlay = new GeoHexOverlay();
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
            pref.saveString(R.string.pref_watch_hexes_key,
            		StringUtil.fromArray(watchHexes, Const.ARRAY_SPLITTER));

            Const.setNextAlarm(MainActivity.this, pref.getAsInt(R.string.pref_watchinterval_key, 5));
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

        mapview.getOverlays().add(watchHexOverlay);
        myLocOverlay = new MyLocationOverlayEx(this, mapview);
        mapview.getOverlays().add(myLocOverlay);
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
    }

    private void initializeUI() {
        mapview = (MapView)findViewById(R.id.mapView);
        mapview.setBuiltInZoomControls(true);

        buttonStartMonitoring = (Button)findViewById(R.id.ButtonStartMonitoring);
        buttonStartMonitoring.setOnClickListener(buttonStartMonitoring_onClick);

        buttonStopMonitoring = (Button)findViewById(R.id.ButtonStopMonitoring);
        buttonStopMonitoring.setOnClickListener(buttonStopMonitoring_onClick);

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

//    // オプションメニューが表示される度に呼び出されます
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        menu.findItem(MENU_ID_CONFIG).setVisible(visible);
//        visible = !visible;
//        return super.onPrepareOptionsMenu(menu);
//    }

    // オプションメニューアイテムが選択された時に呼び出されます
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

        	String test = pref.getString(R.string.pref_watchinterval_key, "def");

            Toast.makeText(this, test, Toast.LENGTH_SHORT).show();
        	Intent intent = new Intent(this, HexRingerPreferenceActivity.class);
        	startActivity(intent);
            ret = true;
            break;
        }
        return ret;
    }

    @Override
    protected void onPause() {
        Log.d(this.getClass().getSimpleName(), "onPause called.");
    	super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(this.getClass().getSimpleName(), "onResume called.");
    	super.onResume();
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
