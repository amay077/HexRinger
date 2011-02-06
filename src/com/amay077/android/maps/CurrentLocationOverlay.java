package com.amay077.android.maps;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

/**
 * 現在地に青い円を描画する Overlay
 *
 * @author orangesignal http://d.hatena.ne.jp/orangesignal/20101228/1293513030
 */
final public class CurrentLocationOverlay extends ItemizedOverlay<OverlayItem> {

	private Location currentLocation;

	/**
	 * デフォルトコンストラクタです。
	 */
	public CurrentLocationOverlay(Drawable center) {
		super(boundCenter(center));
		populate();
	}

	@Override
	protected OverlayItem createItem(final int i) {
		return new OverlayItem(new GeoPoint((int) (currentLocation.getLatitude() * 1E6), (int) (currentLocation.getLongitude() * 1E6)), null, null);
	}

	@Override public int size() { return currentLocation == null ? 0 : 1; }

	@Override
	public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
		if (shadow) {
			return;
		}

		// 精度誤差を表す円を描画します。
		if (currentLocation != null && currentLocation.hasAccuracy()) {
			// 現在地から経度を加算した位置との距離を求めます。
			final double testLongitude = currentLocation.getLongitude() + 0.01;	// 経度 +0.01 度は赤道付近でおよそ 1.1km なのでこれより小さい値を使用すると描画性能に影響すると思われる。(大きい値は問題ないはず)
			float[] results = new float[1];
			Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), currentLocation.getLatitude(), testLongitude, results);

			final GeoPoint geoPoint = getItem(0).getPoint();
			final GeoPoint testGeoPoint = new GeoPoint(geoPoint.getLatitudeE6(), (int) (testLongitude * 1E6));

			// 現在地と経度を加算した位置とのピクセル数を求めます。
			final Projection projection = mapView.getProjection();
			final Point point = projection.toPixels(geoPoint, null);
			final Point testPoint = projection.toPixels(testGeoPoint, null);
			final int pixels = Math.abs(point.x - testPoint.x);

			if (pixels > 0) {
				// 半径ピクセル数 = 精度誤差(メートル) * 現在地と経度を加算した位置とのピクセル数 / 現在地から経度を加算した位置との距離(メートル)
				final float radius = currentLocation.getAccuracy() * pixels / results[0];
				// 円を描画します。
				final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
				paint.setStyle(Paint.Style.FILL);
				paint.setColor(Color.argb(0x22, 0x33, 0x99, 0xFF));
				canvas.drawCircle(point.x, point.y, radius, paint);
				paint.setStyle(Paint.Style.STROKE);
				paint.setColor(Color.argb(0xAA, 0x00, 0x66, 0xFF));
				canvas.drawCircle(point.x, point.y, radius, paint);
			}
		}

		super.draw(canvas, mapView, shadow);
	}

	public void setCurrentLocation(final Location currentLocation) {
		this.currentLocation = currentLocation;
		populate();
	}

}

