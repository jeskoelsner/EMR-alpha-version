package org.zlwima.emurgency.androidapp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import org.zlwima.emurgency.androidapp.rest.RestClient;
import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.backend.Shared.Commands;
import org.zlwima.emurgency.backend.Shared.Results;
import org.zlwima.emurgency.backend.model.ParcelableUser;

public class RestProcessor extends IntentService {
	ResultReceiver mReceiver;
	Context mContext;
	
	public static final int STATUS_RUNNING = 0x1;
	public static final int STATUS_ERROR = 0x2;
	public static final int STATUS_FINISHED = 0x3;

	public RestProcessor() {
		super( "RestProcessor started..." );
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
	}

	@Override
	protected void onHandleIntent( Intent intent ) {
		mReceiver = intent.getParcelableExtra( Shared.RECEIVER );

		Bundle b = new Bundle();

		String command = intent.getStringExtra( Shared.COMMAND );

		mReceiver.send( STATUS_RUNNING, Bundle.EMPTY );

		try {
			if( command.equals( Commands.LOGIN ) ) {
				ParcelableUser user = intent.getParcelableExtra( Shared.USER_OBJECT );
				b.putInt( Results.LOGIN_CALLBACK, RestClient.login( user ) );
			} else if( command.equals( Commands.REGISTRATION ) ) {
				ParcelableUser user = intent.getParcelableExtra( Shared.USER_OBJECT );
				b.putInt( Results.REGISTRATION_CALLBACK, RestClient.registration( user ) );
			} else if( command.equals( Commands.UPDATE_LOCATION ) ) {		
				ParcelableUser user = intent.getParcelableExtra( Shared.USER_OBJECT );
				b.putInt( Results.LOCATION_UPDATE_CALLBACK, RestClient.updateLocation( user ) );
			}

		} catch( Exception e ) {
			b.putString( "errorMsg", Log.getStackTraceString( e ) );
			mReceiver.send( STATUS_ERROR, b );
		}

		mReceiver.send( STATUS_FINISHED, b );
	}

}
