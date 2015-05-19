package org.zlwima.emurgency.androidapp.location;

import org.zlwima.emurgency.androidapp.MissionActivity;
import org.zlwima.emurgency.androidapp.config.Base;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.os.Handler;

public class LocationOverlayHelper extends MyLocationOverlay {
	private Context mContext;
	private MapView mMap;
	private Bitmap mMarker;

	private Point screenPts;
	
	public LocationOverlayHelper(Context context, MapView mapView, int marker) {
		super(context, mapView);
		mContext = context;
		mMap = mapView;
		mMarker = BitmapFactory.decodeResource( mContext.getResources(), marker);
	}
	
	@Override
	protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation, long when) {
		screenPts = mapView.getProjection().toPixels(myLocation, null);
		
//		canvas.drawBitmap(
//				mMarker, 
//	            screenPts.x - (mMarker.getWidth()  / 2), 
//	            screenPts.y - (mMarker.getHeight() / 2), 
//	            null
//	    );
//		
		super.drawMyLocation(canvas, mapView, lastFix, myLocation, when);
	}
	
	@Override
	public synchronized void onLocationChanged(Location location) {
		Base.Log("ONLOCATION CALLED");
	}
	
}