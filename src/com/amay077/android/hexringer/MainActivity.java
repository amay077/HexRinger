package com.amay077.android.hexringer;

import java.util.Timer;
import java.util.TimerTask;

import net.geohex.GeoHex;

import com.amay077.android.hexringer.R;
import com.amay077.android.logging.Log;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


// HexRinger
public class MainActivity extends MapActivity {
    private static final int MENU_ID_START = (Menu.FIRST + 1);
    private static final int MENU_ID_CONFIG = (Menu.FIRST + 2);

    // UI components
    private MapView mapview = null;
    private Button buttonStartMonitoring = null;
    private Button buttonStopMonitoring = null;

    // UI event handler
    /** モニタリング開始ボタンが押されたときの処理 */
    private View.OnClickListener buttonStartMonitoring_onClick = new View.OnClickListener() {

        public void onClick(View v) {
            Editor editor = preference.edit();
            editor.putBoolean(Const.PREF_KEY_ALARM_ENABLED, true);
            editor.commit();

            Const.setAlarmManager(MainActivity.this);
            toggleMonitoringButton(preference.getBoolean(Const.PREF_KEY_ALARM_ENABLED, false));
        }
    };

    private View.OnClickListener buttonStopMonitoring_onClick = new View.OnClickListener() {

        public void onClick(View v) {
            Const.cancelAlarmManager(MainActivity.this);

            Editor editor = preference.edit();
            editor.putBoolean(Const.PREF_KEY_ALARM_ENABLED, false);
            editor.commit();
            toggleMonitoringButton(preference.getBoolean(Const.PREF_KEY_ALARM_ENABLED, false));
        }
    };

    // Fields
    private MyLocationOverlay myLocOverlay = null;
    private GeoHexOverlay watchHexOverlay = new GeoHexOverlay();
    private String currentWatchArea = "";

    private Handler handler = new Handler();
    private AudioManager mAudio = null;
    private SharedPreferences preference = null;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.d("MainActivity", "onCreate");

        initializeUI();

        mapview.getOverlays().add(watchHexOverlay);
        myLocOverlay = new MyLocationOverlayEx(this, mapview);
        mapview.getOverlays().add(myLocOverlay);
        mAudio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        preference = PreferenceManager.getDefaultSharedPreferences(this);

        toggleMonitoringButton(preference.getBoolean(Const.PREF_KEY_ALARM_ENABLED, false));
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

    private void enabledMyLocation() {
        while (true) {
            try {
                myLocOverlay.enableMyLocation();
                //myLocOverlay.enableCompass();
                return;
            } catch (Exception e) {
                SystemClock.sleep(2000);
            }
        }
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
            startWatchTimer();

            ret = true;
            break;
        case MENU_ID_CONFIG:
            Toast.makeText(this, "設定画面を表示", Toast.LENGTH_SHORT).show();
            ret = true;
            break;
        }
        return ret;
    }

    private boolean theFirst = true;
    private boolean sended = false;

    private void startWatchTimer() {

        enabledMyLocation();

        theFirst = true;

        Timer watchTimer = new Timer();

        watchTimer.schedule(new TimerTask() {

            @Override
            public void run() {

                // 1. 現在位置を取得
                final GeoPoint point = myLocOverlay.getMyLocation();

                Log.d("startWatchTimer", point != null ? String.valueOf(point.getLatitudeE6()) : "point is null");

                if (point == null) {
                    return;
                }

                if (theFirst) {
                    theFirst = false;
                    handler.post(new Runnable() {
                        public void run() {
                            mapview.getController().animateTo(point);
                        }
                    });
                }

                for (String geoHexCode : watchHexOverlay.getSelectedGeoHexCodes().keySet()) {

                    // 監視する GeoHex を取得
                    GeoHex.Zone watchZone = GeoHex.decode(geoHexCode);

                    // 監視する GeoHex と同じレベルで、現在位置の GeoHex を取得
                    // ※同じレベルにすることで Code の一致でエリア内判定をする。
                    final GeoHex.Zone currentZone = GeoHex.getZoneByLocation(point.getLatitudeE6() / 1E6,
                            point.getLongitudeE6() / 1E6, watchZone.level);

                    if (watchZone.code.equals(currentZone.code)) {
                        if (currentWatchArea == currentZone.code) {
                            return;
                        }

                        currentWatchArea = currentZone.code;

                        // Notify!
                        // 4. ヒットしたらマナーモードにする
                        Log.d("startWatchTimer", "found! - GeoHex code : " + currentZone.code);

                        handler.post(new Runnable() {

                            public void run() {
//								Toast.makeText(MainActivity.this, "found! - GeoHex code : " + currentZone.code, Toast.LENGTH_SHORT).show();
                                Toast.makeText(MainActivity.this, "通知エリアに入りました！", Toast.LENGTH_SHORT).show();
                                mAudio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

                                if (!sended) {
//									sendMail();
                                    sended = true;
                                }
                            }

//							private void sendMail() {
//								Intent it = new Intent();
//								it.setAction(Intent.ACTION_SENDTO);
//								String to = "my-wife@home.net";
//								it.setData(Uri.parse("mailto:" + to));
//								it.putExtra(Intent.EXTRA_SUBJECT, "今、帰宅中");
//								it.putExtra(Intent.EXTRA_TEXT, "ほげ");
//								startActivity(it);
//							}
                        });
                    }
                }
            }
        }, 0, 1000); // 1秒ごと

    }
}
