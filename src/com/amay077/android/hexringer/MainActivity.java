package com.amay077.android.hexringer;

import java.util.Set;
import com.amay077.android.hexringer.AlarmBroadcastReceiver.StringUtil;
import com.amay077.android.hexringer.R;
import com.amay077.android.logging.Log;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
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
    private SharedPreferences preference = null;

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
        	// TODO : Is selecting Hex?
        	Set<String> watchHexesSet = watchHexOverlay.getSelectedGeoHexCodes();
        	String[] watchHexes = new String[watchHexesSet.size()];
        	watchHexesSet.toArray(watchHexes);

            Editor editor = preference.edit();
            editor.putBoolean(Const.PREF_KEY_ALARM_ENABLED, true);
            editor.putString(Const.PREF_KEY_WATCH_HEXES,
            		StringUtil.fromArray(watchHexes, Const.ARRAY_SPLITTER));
            editor.commit();

            Const.setAlarmManager(MainActivity.this);
            toggleMonitoringButton(preference.getBoolean(Const.PREF_KEY_ALARM_ENABLED, false));

            String Alarm1 = Settings.System.getString(getContentResolver(),
                    Settings.System.NEXT_ALARM_FORMATTED);
            Log.d("buttonStartMonitoring_onClick", Alarm1);
        }
    };

    private View.OnClickListener buttonStopMonitoring_onClick = new View.OnClickListener() {

        public void onClick(View v) {
            String Alarm1 = Settings.System.getString(getContentResolver(),
                    Settings.System.NEXT_ALARM_FORMATTED);
            Log.d("buttonStopMonitoring_onClick", Alarm1);

        	Const.cancelAlarmManager(MainActivity.this);

            Editor editor = preference.edit();
            editor.putBoolean(Const.PREF_KEY_ALARM_ENABLED, false);
            editor.commit();
            toggleMonitoringButton(preference.getBoolean(Const.PREF_KEY_ALARM_ENABLED, false));
        }
    };

    /** Called when the activity is first created. */ 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.d("MainActivity.onCreate", "called");

        initializeUI();

        mapview.getOverlays().add(watchHexOverlay);
        myLocOverlay = new MyLocationOverlayEx(this, mapview);
        mapview.getOverlays().add(myLocOverlay);
        preference = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        toggleMonitoringButton(preference.getBoolean(Const.PREF_KEY_ALARM_ENABLED, false));

        Set<String> watchHexes = watchHexOverlay.getSelectedGeoHexCodes();
        String watchHexesStr = preference.getString(Const.PREF_KEY_WATCH_HEXES, null);
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
        Log.d("MainActivity", "onCreateOptionsMenu");
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
        Log.d("MainActivity", "onOptionsItemSelected");
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
            Toast.makeText(this, "設定画面を表示", Toast.LENGTH_SHORT).show();
            ret = true;
            break;
        }
        return ret;
    }
}
