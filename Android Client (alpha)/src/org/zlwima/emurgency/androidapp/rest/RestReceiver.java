package org.zlwima.emurgency.androidapp.rest;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class RestReceiver extends ResultReceiver {
	private Receiver mReceiver;

	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData) {
		if (mReceiver != null) {
			mReceiver.onReceiveResult(resultCode, resultData);
		}
	}

	public interface Receiver {
		public void onReceiveResult(int resultCode, Bundle resultData);
	}

	public void setReceiver(Receiver receiver) {
		mReceiver = receiver;
	}

	public RestReceiver(Handler handler) {
		super(handler);
	}

}
