package org.zlwima.emurgency.androidapp;

import org.zlwima.emurgency.androidapp.config.Base;
import org.zlwima.emurgency.androidapp.location.LocationPoller;
import org.zlwima.emurgency.androidapp.location.LocationPollerParameter;
import org.zlwima.emurgency.androidapp.rest.RestReceiver;
import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.backend.Shared.LoginCallback;
import org.zlwima.emurgency.backend.Shared.Results;
import org.zlwima.emurgency.backend.Shared.UpdateLocationCallback;
import org.zlwima.emurgency.backend.model.ParcelableUser;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;

public class DashboardActivity extends Activity implements OnClickListener,
		RestReceiver.Receiver, DialogInterface.OnClickListener {
	
	private RestReceiver mReceiver = new RestReceiver(new Handler());
	private LinearLayout linearLayout;
	private LinearLayout extraLinearLayout;

	private AlertDialog logoutDialog;
	private AlertDialog hideDialog;

	private static PendingIntent pendingIntent;
	private static AlarmManager alarmManager;
	private static LocationPollerParameter locationPollerParameter;
	private static Intent locationPollerIntent;
	
	private GCMListener gcmListener;
	
	Bundle mBundle;
	
	//every 30 seconds update
	static int PERIOD = 10000; 
	
	//provider priority list. first checking GPS (with timeout). then network provider
	String[] PROVIDER = new String[] { 
			//LocationManager.GPS_PROVIDER,
			LocationManager.NETWORK_PROVIDER
	};
	
	//in this case GPS listening timeout
	int TIMEOUT = 15000;
	
	public interface GCMListener {
        public void onComplete(Context context);
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/** first content view: login screen */
		setContentView(R.layout.screen_dashboard);

		mReceiver.setReceiver(this);
		
		gcmListener = new GCMListener() {

			public void onComplete(Context context) {
				if (mBundle.getInt(Results.LOGIN_CALLBACK) == LoginCallback.CONFIRMED) {
					Base.Log("::: LOGIN confirmed -> location updates on");
					initLocationUpdates();
				} else {
					Base.Log("::: LOGIN confirmed -> location updates off");
				}
			}
	    };

		mBundle = getIntent().getExtras();
		ParcelableUser user = mBundle.getParcelable(Shared.USER_OBJECT);

		/** initialize button click listeners */
		((Button) findViewById(R.id.dashboardLogoutButton))
				.setOnClickListener(this);
		((Button) findViewById(R.id.dashboardHideButton))
				.setOnClickListener(this);
		
		/** init listview */
		extraLinearLayout = ((LinearLayout) findViewById(R.id.dashboardList));
		linearLayout = ((LinearLayout) findViewById(R.id.dashboardBubbleBox));
		
		GCMIntentService.initGCM(this, user.getEmail(), gcmListener);
		
		if (mBundle.getInt(Results.LOGIN_CALLBACK) == LoginCallback.CONFIRMED) {
			Base.Log("::: LOGIN confirmed -> set Bubble");
			changeStatusBubble(true);
		} else {
			Base.Log("::: LOGIN confirmed -> unset Bubble");
			changeStatusBubble(false);
		}

		//TODO START FROM HERE
	    extraLinearLayout.setOrientation(LinearLayout.VERTICAL);
	    TextView tv1 = new TextView(this);
	    tv1.setText("First Text!");
	    TextView tv2 = new TextView(this);
	    tv2.setText("Second Text!");
	    extraLinearLayout.addView(tv1);
	    extraLinearLayout.addView(tv2);
		
	}
	
	@Override
	protected void onDestroy() {
		stopLocationUpdates();
		super.onDestroy();
	}
	
	boolean onStop = false;
	
	@Override
	protected void onStop() {
		super.onStop();
		Base.Log( "Dashboard [onStop]: pausing location poller" );
		onStop = true;
		stopLocationUpdates();
	}
	
	@Override
	protected void onRestart(){
		super.onRestart();
		if(onStop) {
			Base.Log( "Dashboard [onRestart]: resuming location updates" );
			startLocationUpdates();
			onStop = false;
		}
	}

	private void changeStatusBubble(boolean verified) {
		linearLayout.removeAllViews();
		LayoutInflater inflater = getLayoutInflater();
		inflater.inflate(verified ? R.layout.template_verified_textview
				: R.layout.template_unverified_textview, linearLayout);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dashboardLogoutButton:
			logout();
			break;
		case R.id.dashboardHideButton:
			hide();
			break;
		}
	}

	@Override
	public void onBackPressed() {
		logout();
	}

	public void logout() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				DashboardActivity.this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle("Logout");
		builder.setMessage("Wollen Sie sich wirklich ausloggen?");
		builder.setPositiveButton("OK", this);
		builder.setNegativeButton("Abbrechen", this);

		logoutDialog = builder.create();
		logoutDialog.show();
	}

	public void hide() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				DashboardActivity.this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle("Hide");
		builder.setMessage("Wollen Sie die App wirklich verbergen?");
		builder.setPositiveButton("OK", this);
		builder.setNegativeButton("Abbrechen", this);

		hideDialog = builder.create();
		hideDialog.show();
	}
	
	public void initLocationUpdates(){
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		startLocationUpdates();
	}
	
	public void startLocationUpdates(){
		locationPollerIntent = new Intent(this, LocationPoller.class);
		Bundle bundle = new Bundle();
		locationPollerParameter = new LocationPollerParameter( bundle );
		// try GPS and fall back to NETWORK_PROVIDER
		locationPollerParameter.setProviders(PROVIDER);
		locationPollerParameter.setTimeout(TIMEOUT);
		locationPollerParameter.setReceiver(mReceiver);
		locationPollerIntent.putExtras(bundle);
		
		pendingIntent = PendingIntent.getBroadcast(this, 0, locationPollerIntent, 0);
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime(), PERIOD, pendingIntent);
	}
	
	public void stopLocationUpdates() {
		pendingIntent.cancel();
		alarmManager.cancel(pendingIntent);
	}

	public void onClick(DialogInterface dialog, int which) {
		if (dialog.equals(logoutDialog)) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				// locationUpdateManager.stopLocationUpdates();

				stopLocationUpdates();

				GCMRegistrar.unregister(this);
				finish();
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				// do nothing
				break;
			}
		} else if (dialog.equals(hideDialog)) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				Intent startMain = new Intent(Intent.ACTION_MAIN);
				startMain.addCategory(Intent.CATEGORY_HOME);
				startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(startMain);
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				// do nothing
				break;
			}
		}
	}

	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
		case RestProcessor.STATUS_RUNNING:
			break;
		case RestProcessor.STATUS_ERROR:
			break;
		case RestProcessor.STATUS_FINISHED:
			switch (resultData.getInt(Results.LOCATION_UPDATE_CALLBACK)) {
			case UpdateLocationCallback.UPDATED_USER_IS_CONFIRMED:
				Base.Log("Location Update Result: UPDATED_USER_IS_CONFIRMED");
				changeStatusBubble(true);
				break;
			case UpdateLocationCallback.UPDATED_USER_IS_NOT_CONFIRMED:
				Base.Log("Location Update Result: UPDATED_USER_IS_NOT_CONFIRMED");
				changeStatusBubble(false);
				stopLocationUpdates();
				GCMRegistrar.unregister(this);
				break;
			case UpdateLocationCallback.FAILED:
				Base.Log("Location Update Result: FAILED");
				stopLocationUpdates();
				GCMRegistrar.unregister(this);
				finish();
				break;
			}
			break;
		}
	}

}
