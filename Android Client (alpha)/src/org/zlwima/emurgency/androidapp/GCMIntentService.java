package org.zlwima.emurgency.androidapp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.google.gson.Gson;

import org.zlwima.emurgency.androidapp.DashboardActivity.GCMListener;
import org.zlwima.emurgency.androidapp.config.Base;
import org.zlwima.emurgency.androidapp.config.Globals;
import org.zlwima.emurgency.androidapp.rest.RestClient;
import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.backend.model.ParcelableCaseData;

public class GCMIntentService extends GCMBaseIntentService {

	public static final String SENDERID = "241501947536";
	private static GCMListener listener = null;

	public GCMIntentService() {
		super( SENDERID );
	}

	public static void initGCM( Context context, String email, GCMListener gcmListener ) {
		Base.Log( "GCM Request [initGCM()]" );
		GCMRegistrar.checkDevice( context );
		GCMRegistrar.checkManifest( context );

		Globals.EMAIL = email;
		listener = gcmListener;
		
		GCMRegistrar.register( context, SENDERID );
	}

	@Override
	protected void onRegistered( Context context, String registrationId ) {
		Base.Log( "GCM Event [onRegistered()]" );
		try {
			if( RestClient.registerGCM( context, registrationId, Globals.EMAIL ) ) {
				Globals.REGID = registrationId;
				listener.onComplete( context );
			}
		} catch( Exception e ) {
			Base.Log( "Exception [onRegistered()]: " + e.getMessage() );
		}
	}

	@Override
	protected void onUnregistered( Context context, String registrationId ) {
		Base.Log( "GCM Event [onUnregistered()]" );
		if( !registrationId.isEmpty() ) {
			try {
				RestClient.unregisterGCM( context, registrationId );
			} catch( Exception e ) {
				Base.Log( "Exception [onUnregistered()]: " + e.getMessage() );
			}
		}
	}

	@Override
	protected void onMessage( Context context, Intent intent ) {
		Base.Log( "GCM Event [OnMessage()]" );

		String regId = intent.getExtras().getString( Shared.PARAMETER_REGISTRATIONID );
		Base.Log( "GCM Message -> Incoming: " + regId + " ~ Saved: " + Globals.REGID );
		if( !regId.equals( Globals.REGID ) ) {
			try {
				RestClient.unregisterGCM( context, regId );
			} catch( Exception e ) {
				Base.Log( "Exception [onMessage() -> unregisterGCM]: " + e.getMessage() );
			}
		} else {
			Intent mIntent = new Intent();
			mIntent.setClass( context, MissionActivity.class );
			mIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			mIntent.putExtra( Shared.CASEDATA_OBJECT, new Gson().fromJson( 
					intent.getExtras().getString( Shared.CASEDATA_OBJECT ), ParcelableCaseData.class ) );
			startActivity( mIntent );

		}
	}

	@Override
	protected void onDeletedMessages( Context context, int total ) {
		Base.Log( "GCM Event [onDeletedMessages()] Total: " + total );
	}

	@Override
	public void onError( Context context, String errorId ) {
		Base.Log( "GCM Event [onError()] ErrorId: " + errorId );
	}

	@Override
	protected boolean onRecoverableError( Context context, String errorId ) {
		Base.Log( "GCM Event [onRecoverableError()] ErrorId: " + errorId );
		return super.onRecoverableError( context, errorId );
	}
}
