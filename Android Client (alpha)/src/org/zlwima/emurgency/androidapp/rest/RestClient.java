package org.zlwima.emurgency.androidapp.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.zlwima.emurgency.androidapp.config.Base;
import org.zlwima.emurgency.androidapp.config.Globals;
import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.backend.Shared.Rest;
import org.zlwima.emurgency.backend.model.ParcelableUser;

import android.content.Context;

import com.google.android.gcm.GCMRegistrar;
import com.google.gson.Gson;

public class RestClient {

	public static HttpClient client = null;

	public static HttpClient getClient() {

		Base.Log("trying Client Connection");

		if (client == null) {
			client = new DefaultHttpClient();

			// get default parameters to pass on thread-safe client
			HttpParams params = client.getParams();

			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			// some webservers have problems if this is set to true
			HttpProtocolParams.setUseExpectContinue(params, false);

			HttpConnectionParams.setConnectionTimeout(params,
					Rest.TIMEOUT_MILLIS);
			HttpConnectionParams.setSoTimeout(params, Rest.TIMEOUT_MILLIS);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			ConnManagerParams.setMaxTotalConnections(params, 10);

			SchemeRegistry reg = new SchemeRegistry();
			reg.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), Rest.DEFAULT_PORT));

			// replace default client with a thread-safe one
			client = new DefaultHttpClient(new ThreadSafeClientConnManager(
					params, reg), params);
			Base.Log("established Client Connection");
		}
		return client;
	}

	public static HttpResponse httpExecute(HttpUriRequest httpRequest) {

		HttpResponse response = null;

		try {
			response = getClient().execute(httpRequest);
		} catch (ClientProtocolException e) {
			Base.Log("Got ClientProtocolException...");
		} catch (IOException e) {
			Base.Log("Got IOException...");
		}

		return response;
	}

	public static HttpResponse httpGet(String url) throws Exception {
		HttpGet httpGet = new HttpGet(URI.create(url));
		httpGet.setHeader("Content-Type", Rest.CONTENT_TYPE_JSON);
		httpGet.setHeader("Accept", Rest.CONTENT_TYPE_JSON);

		Base.Log("\tRequest [GET][JSON] to url: " + url);

		return httpExecute(httpGet);
	}

	private static HttpResponse httpPost(String url, String body)
			throws Exception {
		HttpPost httpPost = new HttpPost(URI.create(url));
		httpPost.setHeader("Content-Type", Rest.CONTENT_TYPE_JSON);
		httpPost.setHeader("Accept", Rest.CONTENT_TYPE_JSON);

		httpPost.setEntity(new ByteArrayEntity(body.getBytes("UTF8")));
		Base.Log("\tRequest [POST][JSON]: " + body);

		return httpExecute(httpPost);
	}

	public static int login(ParcelableUser user) throws Exception {
		HttpResponse response = httpPost(Rest.LOGIN_URL, user.toJson());
		String content = streamtoString(response.getEntity().getContent());

		Base.Log("STATUS: " + response.getStatusLine().getStatusCode()
				+ " LINE: " + response.getStatusLine().getReasonPhrase());
		if (response.getStatusLine().getStatusCode() == 200) {
			return Integer.parseInt(content);
		}

		return -1;
	}

	public static int registration(ParcelableUser user) throws Exception {

		HttpResponse response = httpPost(Rest.REGISTRATION_URL, user.toJson());
		String content = streamtoString(response.getEntity().getContent());

		if (response.getStatusLine().getStatusCode() == 200) {
			return Integer.parseInt(content);
		}

		return -1;
	}

	public static int updateLocation(ParcelableUser user) throws Exception {
		user.setRegistrationId(Globals.REGID);

		HttpResponse response = httpPost(Rest.LOCATION_UPDATE, user.toJson());
		String content = streamtoString(response.getEntity().getContent());

		if (response.getStatusLine().getStatusCode() == 200) {
			return Integer.parseInt(content);
		}

		return -1;
	}

	public static boolean registerGCM(Context context, String regId,
			String email) throws Exception {
		HashMap<String, String> body = new HashMap<String, String>(4);
		body.put(Shared.PARAMETER_REGISTRATIONID, regId);
		body.put(Shared.PARAMETER_EMAIL, email);
		body.put(Shared.PARAMETER_MODEL, android.os.Build.MODEL);
		body.put(Shared.PARAMETER_VERSION, android.os.Build.VERSION.RELEASE);

		String testbody = new Gson().toJson(body);
		HttpResponse response = httpPost(Rest.GCM_REGISTER_URL, testbody);

		if (response.getStatusLine().getStatusCode() == 200) {
			GCMRegistrar.setRegisteredOnServer(context, true);
			return true;
		}
		return false;
	}

	public static boolean unregisterGCM(Context context, String regId)
			throws Exception {
		HashMap<String, String> body = new HashMap<String, String>(1);
		body.put(Shared.PARAMETER_REGISTRATIONID, regId);

		String testbody = new Gson().toJson(body);
		HttpResponse response = httpPost(Rest.GCM_UNREGISTER_URL, testbody);

		if (response.getStatusLine().getStatusCode() == 200) {
			GCMRegistrar.setRegisteredOnServer(context, false);
			return true;
		}
		return false;
	}

	public static String streamtoString(InputStream is) throws Exception {
		BufferedReader r = new BufferedReader(
				new InputStreamReader(is, "UTF-8"));

		StringBuilder total = new StringBuilder();
		String line;
		while ((line = r.readLine()) != null) {
			total.append(line);
		}

		return total.toString();
	}

}
