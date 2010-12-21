package com.amay077.android.hexringar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.geohex.GeoHex;
import net.geohex.GeoHex.Loc;
import net.geohex.GeoHex.Zone;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.Style;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * GeoHex を描画する Overlay
 *
 * MapView に追加すると GeoHex を画面いっぱいに描画します。
 * MapView の ZoomLevel に応じて、GeoHex のレベルも変化します。
 * タップで選択/選択解除ができます。
 * 選択された GeoHex(のコード)群は、getSelectedGeoHexCodes() で得られます。
 *
 * @author h_okuyama
 */
public class GeoHexOverlay extends Overlay {

	// fields -----------------------------------------------------------------
	final private int MIN_ZOOMLEVEL = 2;

	/** GeoHex の描画スタイル */
	private Paint hexPaint = new Paint();

	/** 選択した GeoHex の描画スタイル */
	private Paint selectionPaint = new Paint();

	/** 選択した GeoHex の Code 群 */
	private Map<String, Point[]> selectedGeoHexCodes = new HashMap<String, Point[]>();

	/** 選択時の GeoHex のレベル(今は GoogleMap の ZoomLV と連動) */
	private int geoHexLevel;

	// ctor -------------------------------------------------------------------
	public GeoHexOverlay() {
		hexPaint.setStyle(Style.STROKE);
		hexPaint.setColor(Color.BLACK);
		hexPaint.setStrokeWidth(1f);
		hexPaint.setAntiAlias(true);

		selectionPaint.setStyle(Style.FILL);
		selectionPaint.setColor(Color.argb(64, 128, 0, 0));
		selectionPaint.setStrokeWidth(2f);
		selectionPaint.setAntiAlias(true);
	}

	// setter/getter ----------------------------------------------------------
	/** 選択された GeoHex のコード群 を設定します。 */
	public void setSelectedGeoHexCodes(Map<String, Point[]> selectedGeoHexCodes) {
		this.selectedGeoHexCodes = selectedGeoHexCodes;
	}

	/** 選択された GeoHex のコード群 を取得します。 */
	public Map<String, Point[]> getSelectedGeoHexCodes() {
		return selectedGeoHexCodes;
	}

	// overrides --------------------------------------------------------------
	/** GeoHex を画面いっぱいに描画 & 選択されている GeoHex も描画 */
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		if (shadow) { return; }

		// 世界地図だと 0 度またぎの対応が面倒なので、Level3 くらいまでの対応にする
		if (mapView.getZoomLevel() <= MIN_ZOOMLEVEL) { return; }

		// GeoHex のレベルは GoogleMap と連動
		geoHexLevel = mapView.getZoomLevel();

		GeoPoint geoCenter = mapView.getMapCenter();
		Projection proj = mapView.getProjection();

		// 中心位置の GeoHex を取得
		Zone centerZone = GeoHex.getZoneByLocation(geoCenter.getLatitudeE6() / 1E6, geoCenter.getLongitudeE6() / 1E6, geoHexLevel);
		// 中心からどのくらい膨らましたら画面いっぱいになるかを計算
		int inflate = (int) Math.ceil(getLatitudeSpanInMetre(mapView) / (centerZone.getHexSize() * 2) / 2d);

		// 画面いっぱい分の GeoHex群を得て描画
		Point[] points;
		List<Zone> zones = centerZone.inflate(inflate);
		for (Zone zone : zones) {
			points = getGeoHexZonePoints(zone, proj);
			drawPolyline(canvas, points, hexPaint);
		}

		// 選択したものを描画
		// TODO: 直接 values 使えばいいじゃん
		for (String code : getSelectedGeoHexCodes().keySet()) {
			Zone z = GeoHex.getZoneByCode(code);
			points = getGeoHexZonePoints(z, proj);
			drawPolyline(canvas, points, selectionPaint);
		}
	}

	/** GeoHex の選択 or 選択解除 */
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {

		// 世界地図だと 0 度またぎの対応が面倒なので、Level3 くらいまでの対応にする
		if (mapView.getZoomLevel() <= MIN_ZOOMLEVEL) { return false; }

		// タップ位置の GeoHex を得る
		Zone zone = GeoHex.getZoneByLocation(p.getLatitudeE6() / 1E6, p.getLongitudeE6() / 1E6, geoHexLevel);
		if (zone == null) { return false; }

		// 選択 or 選択解除
		// TODO: レベル上位&下位の選択済み GeoHex の対応
		if (getSelectedGeoHexCodes().keySet().contains(zone.code)) {
			getSelectedGeoHexCodes().remove(zone.code);
		} else {
			getSelectedGeoHexCodes().put(zone.code, getGeoHexZonePoints(zone, mapView.getProjection()));
		}

		// 再描画
		mapView.invalidate();

		return true;
	}

	// public methods ---------------------------------------------------------


	// private methods --------------------------------------------------------
	/** 指定した GeoHex を構成する画面座標値を得ます。(最後を閉じるので7点の座標値) */
	public Point[] getGeoHexZonePoints(Zone zone, Projection proj) {
		Loc[] locs = zone.getHexCoords();

		Point[] points = new Point[locs.length + 1];
		for (int i = 0; i < locs.length; i++) {
			Loc loc = locs[i];
			Point p = new Point();
			proj.toPixels(new GeoPoint((int)(loc.lat * 1E6), (int)(loc.lon * 1E6)), p);
			points[i] = new Point();
			points[i].set(p.x, p.y);

		}

		// 閉じる
		points[points.length-1] = new Point();
		points[points.length-1].set(points[0].x, points[0].y);
		return points;
	}

	/** Point[] をポリライン/ポリゴン として描画します。 */
	private void drawPolyline(Canvas canvas, Point[] points, Paint paint) {

		Path path = null;
		for (int i = 0; i < points.length; i++) {
			Point p1 = points[i % points.length];

			if (path == null) {
				path = new Path();
				path.moveTo(p1.x, p1.y);
			} else {
				path.lineTo(p1.x, p1.y);
			}

		}
		canvas.drawPath(path, paint);
	}

	/** MapView の縦方向のワールドサイズをメートル値で取得します。 */
	private double getLatitudeSpanInMetre(MapView mapView) {

		float heightInMetre = (float)(mapView.getLatitudeSpan() / 1E6) * 111136f;
		return heightInMetre;

	}
}
