package org.zlwima.emurgency.androidapp.ui;

import java.util.ArrayList;

import org.zlwima.emurgency.androidapp.config.Base;

import android.R.bool;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

public class MapsDotOverlay extends ItemizedOverlay {
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	private Canvas mCanvas;
	private Drawable mMarker;
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, false);
	}
	
	public MapsDotOverlay(Context context, Drawable defaultMarker, boolean centerBottom) {
		super(defaultMarker);
		if(centerBottom) {
			boundCenterBottom(defaultMarker);
		}else {
			boundCenter(defaultMarker);
		}
		mMarker = defaultMarker;
		mContext = context;
	}
	
	public void addOverlay(GeoPoint point) {
		mOverlays.add(new OverlayItem(point, "", ""));
	    populate();
	}
	
	public void addOverlays(ArrayList<GeoPoint> points) {
		for( GeoPoint point : points) {
			addOverlay(point);
		}
	}
	
	public void updateOverlays(ArrayList<GeoPoint> points) {
		mOverlays.clear();
		addOverlays(points);
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}
	
}
