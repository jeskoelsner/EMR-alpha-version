package org.zlwima.emurgency.androidapp;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.gson.Gson;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.zlwima.emurgency.androidapp.config.Base;
import org.zlwima.emurgency.androidapp.config.Globals;
import org.zlwima.emurgency.androidapp.ui.MapsDotOverlay;
import org.zlwima.emurgency.androidapp.ui.MyCustomLocationOverlay;
import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.backend.Shared.WebsocketCallback;
import org.zlwima.emurgency.backend.model.ParcelableCaseData;
import org.zlwima.emurgency.backend.model.ParcelableLocation;
import org.zlwima.emurgency.backend.model.ParcelableVolunteer;

public class MissionActivity extends MapActivity implements
		OnTouchListener, OnClickListener, OnChronometerTickListener, LocationListener, DialogInterface.OnClickListener, SensorEventListener {

	// GMaps
	private MapView mMap;
	private MapController mMapController;
	private List<Overlay> mMapOverlays;
	
	// Geo Data
	private int caseDistance;
	private String caseAddress;
	
	private GeoPoint caseLocation;
	private GeoPoint userLocation;
	private ArrayList<GeoPoint> caseUserPoints;
	private ArrayList<GeoPoint> caseVolunteerPoints;
	
	private static ParcelableCaseData caseData = null;
	private static ParcelableVolunteer userAsVolunteer = new ParcelableVolunteer();
	private static ParcelableLocation actualLocation = new ParcelableLocation();

	// Variables
	private boolean screenIsLocked = true;
	private boolean displayIsZoomed = false;
	private int minPos;
	private int maxPos;
	private int validArea;

	// UI
	private TextView volunteerCount;
	private TextView volunteerCount2;
	private Chronometer missionTimer;
	private Chronometer missionTimer2;
	private TextView caseDistanceText;
	private TextView caseAddressText;
	private Button unLocker;
	private Button centerButton;
	private RelativeLayout unlockField;
	private RelativeLayout glasPanel;
	private RelativeLayout menuPanel;
	private RelativeLayout topMenuPanel;
	
	private AlertDialog caseClosedDialog;

	// Maps Location Manager
	LocationManager locationManager;
	Criteria mCriteria;
	Location locationFix;
	
	// Window & Screenlock
	Window window;
	
	// WebSocket & Manager
	private WebSocketConnection webSocketConnection = new WebSocketConnection();
	private	boolean webSocketConnected = false;
	private RelativeLayout.LayoutParams layoutSetup;

	private class AndroidWebSocketHandler extends WebSocketHandler {
			@Override
			public void onOpen() {
				Base.Log( "websocket[onOpen()]: connected" );
				webSocketConnected = true;
				sendWebSocketMessage( Shared.WebsocketCallback.CLIENT_SENDS_CASE_ID );
			}
			@Override
			public void onTextMessage( String payload ) {
				Base.Log( "::::SOCKET MSG RECEIVED: "+ payload );
				try {
					JSONObject json = new JSONObject( payload );
					
					// Get type of message (close command | message with updated data)
					switch( json.getInt( Shared.MESSAGE_TYPE ) ) {
						case Shared.WebsocketCallback.SERVER_SENDS_CLOSE_CASE:
							Base.Log("websocket[SERVER_SENDS_CLOSE_CASE]");
							closeCase();
							break;
						case Shared.WebsocketCallback.SERVER_SENDS_CASEDATA:
							Base.Log("websocket[SERVER_SENDS_CASEDATA]");						
							// Get caseData from JSON
							String caseDataAsJson = json.getString( Shared.CASEDATA_OBJECT );
							ParcelableCaseData caseData = new Gson().fromJson( caseDataAsJson, ParcelableCaseData.class );							
							Intent mIntent = new Intent().putExtra( Shared.CASEDATA_OBJECT, caseData );
							updateLocations( MissionActivity.this, mIntent, false );							
							break;
					}
				} catch( JSONException e ) {
					Base.Log( "websocket[ERROR Json]: " + e.getMessage() );
				}
			}
			@Override
			public void onClose( int code, String reason ) {
				Base.Log( "websocket[onClose()]: " + reason );
			}
	}
	
	private void initNewWebSocketConnection() {
		webSocketConnection.disconnect();
		try {		
			webSocketConnection.connect( Shared.Rest.WEBSOCKET_URL + "/" + caseData.getCaseId() , new AndroidWebSocketHandler() );
		} catch( WebSocketException ex ) {
			Base.Log( "::: ERROR CONNETING WEBSOCKET during initNewWebSocketConnection() :::" );
		}
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	public void closeCase() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				MissionActivity.this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle("Case beendet");
		builder.setMessage("Der Case wurde serverseitig beendet.");
		builder.setPositiveButton("OK", this);

		caseClosedDialog = builder.create();
		caseClosedDialog.show();
	}
	
	public void onClick(DialogInterface dialog, int which) {
		if (dialog.equals(caseClosedDialog)) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				finish();
				break;
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Base.Log( "Mission Event [onDestroy]" );
		webSocketConnection.disconnect();
		stopLocationUpdates();
		
		//((LocationOverlayHelper) mMapOverlays.get(1)).disableMyLocation();
		
		//Lock device
		DevicePolicyManager mDPM;
		mDPM = (DevicePolicyManager) getSystemService( Context.DEVICE_POLICY_SERVICE );
	}

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		Base.Log( "Mission Event [onCreate]" );

		// first content view: login screen
		setContentView( R.layout.screen_mission );

		//Unlock
		window = getWindow();
		window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
		window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
		
		//TODO enable notifications
		locationManager = (LocationManager) getApplication().getSystemService( LOCATION_SERVICE );
		
		mMap = (MapView) findViewById( R.id.mapView );
		mMapController = mMap.getController();
		mMapOverlays = mMap.getOverlays();
		
		// initial email of user and location from LocationPoller
		userAsVolunteer.setEmail(Globals.EMAIL);		
		userAsVolunteer.setLocation(initLocationUpdate());
		
		// ringtone manager / notification sound
		Uri notification = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION );
		Ringtone r = RingtoneManager.getRingtone( getApplicationContext(), notification );
		r.play();

		// initialize button click listeners
		unLocker = ((Button) findViewById( R.id.buttonUnlock ));
		unlockField = ((RelativeLayout) findViewById( R.id.unlockField ));
		glasPanel = ((RelativeLayout) findViewById( R.id.glasPanel ));
		menuPanel = ((RelativeLayout) findViewById( R.id.menuPanel ));
		topMenuPanel = ((RelativeLayout) findViewById( R.id.topMenuPanel ));
		centerButton = ((Button) findViewById( R.id.centerButton ));
		volunteerCount = ((TextView) findViewById( R.id.volunteerCount ));
		missionTimer = ((Chronometer) findViewById( R.id.missionTimer ));
		volunteerCount2 = ((TextView) findViewById( R.id.volunteerCount2 ));
		missionTimer2 = ((Chronometer) findViewById( R.id.missionTimer2 ));
		caseDistanceText = ((TextView) findViewById( R.id.caseDistanceText ));
		caseAddressText = ((TextView) findViewById( R.id.caseAddressText ));
		
		// start locationupdates
		updateLocations( MissionActivity.this, getIntent(), false );
		
		// start websocket connection
		initNewWebSocketConnection();

		// init listener
		centerButton.setOnClickListener( this );
		unLocker.setOnTouchListener( this );
		missionTimer.setOnChronometerTickListener( this );
		missionTimer.start();
		missionTimer2.setOnChronometerTickListener( this );
		missionTimer2.start();
		
	}

	private void zoomMap( ArrayList<GeoPoint> points ) {
		int minLat = Integer.MAX_VALUE;
		int maxLat = Integer.MIN_VALUE;
		int minLon = Integer.MAX_VALUE;
		int maxLon = Integer.MIN_VALUE;

		for( GeoPoint point : points ) {
			int lat = point.getLatitudeE6();
			int lon = point.getLongitudeE6();
			maxLat = Math.max( lat, maxLat );
			minLat = Math.min( lat, minLat );
			maxLon = Math.max( lon, maxLon );
			minLon = Math.min( lon, minLon );
		}

		// leave some padding from corners
		double hpadding = 0.2;
		double vpadding = 0.2;
		maxLat = maxLat + (int) ((maxLat - minLat) * hpadding);
		minLat = minLat - (int) ((maxLat - minLat) * hpadding);
		maxLon = maxLon + (int) ((maxLon - minLon) * vpadding);
		minLon = minLon - (int) ((maxLon - minLon) * vpadding);

		mMapController.zoomToSpan( Math.abs( maxLat - minLat ),	Math.abs( maxLon - minLon ) );
		mMapController.animateTo( new GeoPoint( (maxLat + minLat) / 2, (maxLon + minLon) / 2 ) );

		mMap.invalidate();
	}
	
	public Criteria getCriteria(){
		if(mCriteria == null) {
			mCriteria = new Criteria();
			mCriteria.setAccuracy( Criteria.ACCURACY_FINE );
			mCriteria.setAltitudeRequired( true );
			mCriteria.setBearingRequired( false );
			mCriteria.setPowerRequirement( Criteria.POWER_LOW );
		}
		return mCriteria;
	}

	public ParcelableLocation initLocationUpdate() {
		String provider = locationManager.getBestProvider( getCriteria(), true );
		Base.Log( "best provider from getLocation() would be: " + provider );
		
		Location currentLocation = locationManager.getLastKnownLocation( provider );
		if(currentLocation == null) {
			currentLocation = new Location("network");
			currentLocation.setLatitude(50.7799);
			currentLocation.setLongitude(6.1007);
			currentLocation.setProvider("network");
			currentLocation.setTime(0);
			currentLocation.setAltitude(0);
		}
		
		locationFix = currentLocation;

		ParcelableLocation currentLocationFix;
		currentLocationFix = new ParcelableLocation();
		currentLocationFix.setLatitude(currentLocation.getLatitude());
		currentLocationFix.setLongitude(currentLocation.getLongitude());
		currentLocationFix.setProvider(currentLocation.getProvider());
		currentLocationFix.setTimestamp(currentLocation.getTime());
		currentLocationFix.setAltitude(currentLocation.getAltitude());
		
		return currentLocationFix;
	}
	
	public void startLocationUpdates(){
		Base.Log( "::: startLocationUpdates() " );
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this, null);
	}
	
	public void stopLocationUpdates(){
		locationManager.removeUpdates(this);
	}
	
	public void onLocationChanged( Location location ) {
		Base.Log("::: Own Location updated. Sending to Websocket...");
		locationFix = location;
		
		actualLocation.setLongitude( location.getLongitude() );
		actualLocation.setLatitude( location.getLatitude() );
		actualLocation.setAltitude( location.getAltitude() );
		actualLocation.setProvider( location.getProvider() );
		actualLocation.setTimestamp( location.getTime() );

		userAsVolunteer.setEmail( Globals.EMAIL );
		userAsVolunteer.setLocation( actualLocation );
		
		sendWebSocketMessage( WebsocketCallback.CLIENT_SENDS_LOCATION_UPDATE );
	}
	
	public float getDirection(Location self, Location target) {
		float azimuth = 0;// get azimuth from the orientation sensor (it's quite simple)
		Location currentLoc = self;
		// convert radians to degrees
		azimuth = azimuth * 180 / (float) Math.PI;
		GeomagneticField geoField = new GeomagneticField(
		             Double.valueOf(currentLoc.getLatitude()).floatValue(),
		             Double.valueOf(currentLoc.getLongitude()).floatValue(),
		             Double.valueOf(currentLoc.getAltitude()).floatValue(),
		             System.currentTimeMillis());
		azimuth += geoField.getDeclination(); // converts magnetic north into true north
		float bearing = currentLoc.bearingTo(target); // (it's already in degrees)
		return azimuth - bearing;
	}
	
	public void createOverlays( Context context, ArrayList<GeoPoint> points ) {
		Drawable drawPoint = null;
		MapsDotOverlay mapsDotOverlay;
		
		// remove anything if there is (like.. never...)
		mMapOverlays.clear();
		
		// add case overlay
		drawPoint = context.getResources().getDrawable( R.drawable.case_target_marker );
		mapsDotOverlay = new MapsDotOverlay( context, drawPoint, true);
		mapsDotOverlay.addOverlay( points.get( 0 ) );
		mMapOverlays.add( mapsDotOverlay );
		
		// volunteer overlay
		drawPoint = context.getResources().getDrawable( R.drawable.case_volunteer_marker );
		mapsDotOverlay = new MapsDotOverlay( context, drawPoint, false );
		ArrayList<GeoPoint> volunteerList = new ArrayList<GeoPoint>();
		volunteerList.addAll(points.subList(2, points.size()));
		mapsDotOverlay.addOverlays(volunteerList);
		mMapOverlays.add( mapsDotOverlay );
		
		// user overlay  NO ( compass + direction arrow ) 
		drawPoint = context.getResources().getDrawable( R.drawable.case_self_marker );
		mapsDotOverlay = new MapsDotOverlay( context, drawPoint, false );
		mapsDotOverlay.addOverlay( points.get( 1 ) );
		mMapOverlays.add( mapsDotOverlay );
		
		// rotator
		MyCustomLocationOverlay myLocOverlay = new MyCustomLocationOverlay(context, mMap, R.drawable.case_rotator, points.get( 1 ));
		myLocOverlay.disableMyLocation();
		myLocOverlay.enableCompass();
		mMapOverlays.add( myLocOverlay );
		
		// TODO
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_NORMAL);
		
		// start updates so maps works
		startLocationUpdates();
		
		//refresh map
		mMap.invalidate();
	}

	public void updateOverlays( ArrayList<GeoPoint> points ) {
		ParcelableLocation myLoc = userAsVolunteer.getLocation();
		
		MapsDotOverlay singleOverlay = (MapsDotOverlay) mMapOverlays.get(2);
		ArrayList<GeoPoint> selfList = new ArrayList<GeoPoint>();
		selfList.add(new GeoPoint( (int) (myLoc.getLatitude() * 1e6), (int) (myLoc.getLongitude() * 1e6) ));
		singleOverlay.updateOverlays( selfList );
		
		MapsDotOverlay volunteerOverlay = (MapsDotOverlay) mMapOverlays.get(1);
		volunteerOverlay.updateOverlays( points );
		
		MyCustomLocationOverlay compassOverlay = (MyCustomLocationOverlay) mMapOverlays.get(3);
		compassOverlay.updatePoint( new GeoPoint( (int) (myLoc.getLatitude() * 1e6), (int) (myLoc.getLongitude() * 1e6) ) );
		
		//refresh map
		mMap.invalidate();
	}
	
	public void updateArrow(float rotation) {
//		ParcelableLocation myLoc = userAsVolunteer.getLocation();
//		
//		MyCustomLocationOverlay rotatorOverlay = (MyCustomLocationOverlay) mMapOverlays.get(3);
//		
//		mMap.invalidate();
	}

	public void updateLocations( Context context, Intent intent, boolean zoomCenter ) {
		Base.Log( "MissionActivity: updateLocations()" );
		
		// incoming full caseData
		caseData = intent.getExtras().getParcelable( Shared.CASEDATA_OBJECT );
		
		// calculate distance and display it	
		caseDistance = (int) Shared.calculateDistance( 
				caseData.getCaseLocation().getLatitude(), 
				caseData.getCaseLocation().getLongitude(), 
				userAsVolunteer.getLocation().getLatitude(), 
				userAsVolunteer.getLocation().getLongitude() );
		caseDistanceText.setText( "+/- \n" + caseDistance + " m");
		
		// TODO get address of case and display
		caseAddress = caseData.getCaseAddress();
		caseAddressText.setText( caseAddress );
		
		// save caseLocation
		caseLocation = new GeoPoint( 
				(int) (caseData.getCaseLocation().getLatitude() * 1e6), 
				(int) (caseData.getCaseLocation().getLongitude() * 1e6) );
		
		// save own location
		userLocation = new GeoPoint( 
				(int) (userAsVolunteer.getLocation().getLatitude() * 1e6), 
				(int) (userAsVolunteer.getLocation().getLongitude() * 1e6) );
		
		// points: user & case
		caseUserPoints = new ArrayList<GeoPoint>();
		caseUserPoints.add( caseLocation );
		caseUserPoints.add( userLocation );

		// points: case & all volunteer (including user)
		caseVolunteerPoints = new ArrayList<GeoPoint>();

		// fetch all volunteers
		ArrayList<ParcelableVolunteer> volunteerList = caseData.getVolunteers();
		for( ParcelableVolunteer aVolunteer : volunteerList ) {
			// skip user (already included)
			// TODO broken exclude...
			if(aVolunteer.getLocation() != userAsVolunteer.getLocation()) {
				ParcelableLocation volLoc = aVolunteer.getLocation();
				caseVolunteerPoints.add( new GeoPoint( (int) (volLoc.getLatitude() * 1e6), (int) (volLoc.getLongitude() * 1e6) ) );
			}
		}
		
		// display all accepted users
		volunteerCount.setText( volunteerList.size() + " volunteer(s) accepted already" );
		volunteerCount2.setText( volunteerList.size() + " active volunteer(s)" );

		if(mMapOverlays.size() != 4) {
			createOverlays( context, caseUserPoints);
		}else {
			updateOverlays( caseVolunteerPoints );
		}
		
		// autozoom to all volunteers AND+OR user and case
		caseVolunteerPoints.addAll(caseUserPoints);
		zoomMap( (zoomCenter && !screenIsLocked) ? caseUserPoints : caseVolunteerPoints );
	}

	public void onClick( View v ) {
		Base.Log( "Mission Event [onClick]" );
		if( v.getId() == R.id.centerButton ) {
			zoomMap( displayIsZoomed ? caseVolunteerPoints : caseUserPoints );
			((Button) v).setText( displayIsZoomed ? "Zoom Fit" : "Zoom All" );
			displayIsZoomed = !displayIsZoomed;
		}
		
		startLocationUpdates();
	}
	
	public boolean onTouch( View view, MotionEvent me ) {
		switch( me.getAction() ) {
			case MotionEvent.ACTION_DOWN:
				Base.Log( "ACTION_DOWN: " + me.getX() + " - " + me.getY() );
				layoutSetup = new RelativeLayout.LayoutParams( unLocker.getWidth(), unLocker.getHeight() );
				minPos = 4;
				maxPos = unlockField.getWidth() - unLocker.getWidth() + minPos;
				Base.Log( "" + maxPos );
				validArea = maxPos - 30; // 30 Tolerance
				break;
			case MotionEvent.ACTION_UP:
				Base.Log( "ACTION_UP: " + me.getX() + " - " + me.getY() );
				relockScreen( view );
				break;
			case MotionEvent.ACTION_MOVE:
				int posX = (int) me.getRawX() - (int) (0.5 * unLocker.getWidth());
				if( posX >= validArea && screenIsLocked ) {
					
					unlockScreen();
					
				} else if( posX >= minPos && posX <= maxPos ) {
					layoutSetup.setMargins( posX + minPos, 8, 0, 0 );
					view.setLayoutParams( layoutSetup );
				}
				break;
		}
		return false;
	}
	
	private void unlockScreen(){
		screenIsLocked = false;
		Base.Log("UNLOCKING SCREEN");
		glasPanel.setVisibility( View.INVISIBLE );
		menuPanel.setVisibility( View.VISIBLE );
		topMenuPanel.setVisibility( View.VISIBLE );
		mMap.setClickable( true );

		//unlocking screen means accepting case
		sendWebSocketMessage( WebsocketCallback.CLIENT_SENDS_ACCEPT_MISSION );
	}

	private void relockScreen( View view ) {
		layoutSetup.setMargins( 4, 8, 0, 0 );
		view.setLayoutParams( layoutSetup );
	}

	/**
	 * Countdown with a chronometer
	 */
	public void onChronometerTick( Chronometer chronometer ) {
		Date date = new Date( SystemClock.elapsedRealtime() - chronometer.getBase() );
		missionTimer.setText( "Mission active: " + DateFormat.format( "mm:ss", date.getTime() ) );
		missionTimer2.setText( "Mission active: " + DateFormat.format( "mm:ss", date.getTime() ) );
	}
		
	private void sendWebSocketMessage( int messageType ) {
		
		if(webSocketConnected) {
			Base.Log( "::::SOCKET MSG SEND: "+ messageType );
			JSONObject json = new JSONObject();
			try {
				json.put( Shared.MESSAGE_TYPE, messageType );
				json.put( Shared.CASE_ID, caseData.getCaseId() );
				if( messageType != Shared.WebsocketCallback.CLIENT_SENDS_CASE_ID ) {
					json.put( Shared.VOLUNTEER_OBJECT, new Gson().toJson( userAsVolunteer ) );
				}
			} catch( JSONException e ) {
				Base.Log( "SENDING ERROR: " + e.getMessage() );
			}
			webSocketConnection.sendTextMessage( json.toString() );
		}
	}


	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
	}

	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
	}
	
	private SensorManager sensorManager;
	private Sensor orientation;
	
	@Override
	protected void onPause() {
		sensorManager.unregisterListener(this);
		super.onPause();
	}
	
	float currentAzimuth = 0;
	float maxAzimuth = 0;
	float minAzimuth = 0;
	float rangeAzimuth = 2;
	float direction;
	
	public void onSensorChanged(SensorEvent event) {
		currentAzimuth = Math.round(event.values[0]);
		if( minAzimuth == 0 && maxAzimuth == 0 ) {
			minAzimuth = (currentAzimuth - rangeAzimuth) % 360;
			maxAzimuth = (currentAzimuth + rangeAzimuth) % 360;
		}
		
		if( (minAzimuth > currentAzimuth || currentAzimuth > maxAzimuth) && locationFix != null ) {
			minAzimuth = currentAzimuth - rangeAzimuth;
			maxAzimuth = currentAzimuth + rangeAzimuth;
			
	    	direction = currentAzimuth - locationFix.bearingTo( locationFix );
	    	Base.Log(String.format("Direction: %f - Azimuth: %f", direction, currentAzimuth));
	    	updateArrow( direction );
		}
	    
	}
	
}
