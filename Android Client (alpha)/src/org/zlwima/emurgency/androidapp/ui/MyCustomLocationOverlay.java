package org.zlwima.emurgency.androidapp.ui;

import org.zlwima.emurgency.androidapp.config.Base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class MyCustomLocationOverlay extends MyLocationOverlay {
	Context mContext;
	MapView mMap;
	Bitmap mMarker;
	
	Point mScreenPoints;
	GeoPoint mLocation;
	
	Matrix rotationMatrix;
	Bitmap rotatedBitmap;

	public MyCustomLocationOverlay(Context context, MapView mapView, int markerResource, GeoPoint geoPoint) {
		super(context, mapView);
		mContext = context;
		mMap = mapView;
		mMarker = BitmapFactory.decodeResource(context.getResources(), markerResource);
		mLocation = geoPoint;
		mScreenPoints = new Point();
		rotationMatrix = new Matrix();
	}
	
	@Override
	protected void drawCompass(Canvas canvas, float bearing) {
		
		if(mLocation != null) {
			
			//icon is prerotated, correct it
			float correctedBearing = bearing - 45;
			
			//we need full 360° instead range [-180,180] range
			if (correctedBearing < 0) {
				correctedBearing = correctedBearing + 360;
			}
			
			//get coordinates to pixels
			mMap.getProjection().toPixels(mLocation, mScreenPoints);
			
			//rotate it
			rotationMatrix.setRotate(correctedBearing, (mMarker.getWidth() / 2), (mMarker.getHeight() / 2));
			
			rotatedBitmap = Bitmap.createBitmap(mMarker, 0, 0, 
					mMarker.getWidth(), mMarker.getHeight(), 
					rotationMatrix, true);
			
		    canvas.drawBitmap(rotatedBitmap, mScreenPoints.x - (rotatedBitmap.getWidth() / 2), mScreenPoints.y - (rotatedBitmap.getHeight() / 2), null);
		    mMap.invalidate();
		}
		
	}
	
	public void updatePoint(GeoPoint geoPoint){
		mLocation = geoPoint;
	}

}
