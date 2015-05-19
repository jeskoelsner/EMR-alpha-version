package org.zlwima.emurgency.androidapp;

import org.apache.commons.logging.Log;
import org.zlwima.emurgency.androidapp.config.Base;
import org.zlwima.emurgency.androidapp.rest.RestReceiver;
import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.backend.Shared.Commands;
import org.zlwima.emurgency.backend.Shared.LoginCallback;
import org.zlwima.emurgency.backend.Shared.Results;
import org.zlwima.emurgency.backend.model.ParcelableUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class EmurgencyActivity extends Activity implements OnClickListener,
		RestReceiver.Receiver, DialogInterface.OnClickListener {

	// Application mActivity;
	RestReceiver mReceiver;
	Intent mIntent;
	EditText mEmail;
	EditText mPassword;
	ProgressDialog mDialog;
	ParcelableUser mUser;
	AlertDialog quitDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/** first content view: login screen */
		setContentView(R.layout.screen_login);

		Base.Log("\n\n\nApplication started... \n#######################\n\n");

		// mActivity = getApplication();
		mReceiver = new RestReceiver(new Handler());
		mReceiver.setReceiver(this);

		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

		GCMRegistrar.unregister(getApplicationContext());

		/** initialize button click listeners */
		((Button) findViewById(R.id.buttonLogin)).setOnClickListener(this);
		((Button) findViewById(R.id.buttonRegister)).setOnClickListener(this);

		mEmail = ((EditText) findViewById(R.id.loginEmail));
		mPassword = ((EditText) findViewById(R.id.loginPassword));

	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonLogin:
			Base.Log("Login clicked...");
			mIntent = new Intent(Intent.ACTION_SYNC, null, getApplication(),
					RestProcessor.class);

			mUser = new ParcelableUser();
			mUser.setEmail(mEmail.getText().toString());
			mUser.setPassword(mPassword.getText().toString());

			mIntent.putExtra(Shared.USER_OBJECT, mUser);
			mIntent.putExtra(Shared.RECEIVER, mReceiver);
			mIntent.putExtra(Shared.COMMAND, Commands.LOGIN);
			startService(mIntent);
			break;
		case R.id.buttonRegister:
			Base.Log("Register clicked...");
			Intent intent = new Intent(EmurgencyActivity.this,
					RegisterActivity.class);
			startActivity(intent);
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		quit();
	}

	public void quit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				EmurgencyActivity.this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle("Quit");
		builder.setMessage("Wollen Sie die Anwendung wirklich schließen?");
		builder.setPositiveButton("OK", this);
		builder.setNegativeButton("Abbrechen", this);

		quitDialog = builder.create();
		quitDialog.show();
	}
	
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			finish();
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			// do nothing
			break;
		}
	}

	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
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
			Toast.makeText(this, "Login not possible. Service error.",
					Toast.LENGTH_SHORT).show();
			break;
		case RestProcessor.STATUS_FINISHED:
			mDialog.dismiss();
			
			Intent intent;
			
			switch (resultData.getInt(Results.LOGIN_CALLBACK)) {
			case LoginCallback.INVALID:
				Base.Log("::: LOGIN invalid");
				break;
			case LoginCallback.VALID:
				Base.Log("::: LOGIN valid");
				intent = new Intent(EmurgencyActivity.this,
						DashboardActivity.class);
				intent.putExtra(Shared.USER_OBJECT, mUser);
				intent.putExtra(Results.LOGIN_CALLBACK, resultData.getInt(Results.LOGIN_CALLBACK));
				startActivity(intent);
				break;
			case LoginCallback.CONFIRMED:
				Base.Log("::: LOGIN confirmed");
				intent = new Intent(EmurgencyActivity.this,
						DashboardActivity.class);
				intent.putExtra(Shared.USER_OBJECT, mUser);
				intent.putExtra(Results.LOGIN_CALLBACK, resultData.getInt(Results.LOGIN_CALLBACK));
				startActivity(intent);
				break;
			}

			break;
		}
	}



}