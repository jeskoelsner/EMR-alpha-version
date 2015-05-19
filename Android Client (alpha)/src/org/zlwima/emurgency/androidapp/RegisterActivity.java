package org.zlwima.emurgency.androidapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.zlwima.emurgency.androidapp.config.Base;
import org.zlwima.emurgency.androidapp.rest.RestReceiver;
import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.backend.Shared.Commands;
import org.zlwima.emurgency.backend.Shared.RegisterCallback;
import org.zlwima.emurgency.backend.Shared.Results;
import org.zlwima.emurgency.backend.model.ParcelableUser;

public class RegisterActivity extends Activity implements OnClickListener, RestReceiver.Receiver {

	RestReceiver mReceiver;
	Intent mIntent;
	EditText mFirstName;
	EditText mLastName;
	EditText mEmail;
	EditText mPassword;
	EditText mPasswordCheck;
	
	ProgressDialog mDialog;

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		/** first content view: login screen */
		setContentView( R.layout.screen_register );

		//mActivity = getApplication();
		mReceiver = new RestReceiver( new Handler() );
		mReceiver.setReceiver( this );

		/** initialize button click listeners */
		((Button) findViewById( R.id.buttonRegisterSubmit )).setOnClickListener( this );

		/** initialize form fields */
		mFirstName = ((EditText) findViewById( R.id.regFirstName ));
		mLastName = ((EditText) findViewById( R.id.regLastName ));
		mEmail = ((EditText) findViewById( R.id.regEmail ));
		mPassword = ((EditText) findViewById( R.id.regPass ));
		mPasswordCheck = ((EditText) findViewById( R.id.regPassValid ));
	}

	public boolean validateRegistration() {
		//TODO Validation check
		return true;
	}

	public void submitRegistration() {
		if( validateRegistration() ) {
			mIntent = new Intent( Intent.ACTION_SYNC, null, getApplication(), RestProcessor.class );
			
			ParcelableUser user = new ParcelableUser();
			user.setFirstName(mFirstName.getText().toString());
			user.setLastName(mLastName.getText().toString());
			user.setEmail(mEmail.getText().toString());
			user.setPassword(mPassword.getText().toString());
			
			mIntent.putExtra( Shared.USER_OBJECT, user);
			mIntent.putExtra( Shared.RECEIVER, mReceiver );
			mIntent.putExtra( Shared.COMMAND, Commands.REGISTRATION );
			startService( mIntent );
		}
	}

	public void onClick( View v ) {
		switch( v.getId() ) {
			case R.id.buttonRegisterSubmit:
				Base.Log( "Registration submit..." );
				submitRegistration();
				break;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void setActivity( Class activityclass ) {
		Intent intent = new Intent( RegisterActivity.this, activityclass );
		startActivity( intent );
	}

	public void onReceiveResult( int resultCode, Bundle resultData ) {
		switch( resultCode ) {
			case RestProcessor.STATUS_RUNNING:
				mDialog = new ProgressDialog(this);
				mDialog.setTitle("Loading");
				mDialog.setMessage("Please wait...");
				mDialog.show();
				break;
			case RestProcessor.STATUS_ERROR:
				mDialog.dismiss();
				String error = resultData.getString("errorMsg");
				Base.Log(error);
				Toast.makeText(this, "Registration not possible. Service error.", Toast.LENGTH_SHORT).show();
				break;
			case RestProcessor.STATUS_FINISHED:
				mDialog.dismiss();
				switch(resultData.getInt( Results.REGISTRATION_CALLBACK )){
					case RegisterCallback.REGISTERED:
						setActivity( EmurgencyActivity.class );
						break;
					case RegisterCallback.ERROR:
						Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
						break;
					case RegisterCallback.EXISTS_ALREADY:
						Toast.makeText(this, "Already registered", Toast.LENGTH_SHORT).show();
						break;
						
				}
				break;
		}
	}

}
