package org.zlwima.emurgency.webservice.gcm;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.android.gcm.server.Sender;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.grizzly.websockets.WebSocketEngine;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.zlwima.emurgency.backend.Backend;
import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.backend.model.ParcelableCaseData;
import org.zlwima.emurgency.backend.model.ParcelableLocation;
import org.zlwima.emurgency.backend.model.ParcelableUser;
import org.zlwima.emurgency.backend.model.User;
import org.zlwima.emurgency.webservice.websocket.EmrWebSocketApplication;

@SuppressWarnings("serial")
public class GcmServlet extends HttpServlet {

	protected static final Logger logger = Logger.getLogger( GcmServlet.class.getName() );
		
	private Sender sender;
	private JSONObject requestAsJson;
	private HttpServletRequest request;
	private HttpServletResponse response;

	private EmrWebSocketApplication webSocketApplication = new EmrWebSocketApplication();

	private void cleanupWebsockets() {
		logger.log( Level.INFO, "::: CLEANING WEBSOCKETS :::" );
		WebSocketEngine.getEngine().unregister( webSocketApplication );
		webSocketApplication.shutdown();		
	}
	
	@Override
	public void destroy() {
		logger.log( Level.INFO, "::: DESTROYING GCM SERVLET :::" );
		webSocketApplication.staticClientTimeoutCheckerShouldRenew = false;
		cleanupWebsockets();
	}
		
	@Override
	public void init( ServletConfig config ) throws ServletException {
		super.init( config );
		String key = (String) config.getServletContext().getAttribute( ApiKeyInitializer.ATTRIBUTE_ACCESS_KEY );
		logger.log( Level.INFO, "::: INITIALISING WEBSOCKETS WITHIN GCMSERLVET (key: {0})", key );
		sender = new Sender( key );
		/*
		 * Register Websocket Application
		 */
		cleanupWebsockets();
		WebSocketEngine.getEngine().register( webSocketApplication );	
		webSocketApplication.startStaticClientTimeoutChecker();
	}

	@Override
	protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws IOException {
		this.request = request;
		this.response = response;
		
		final String path = request.getPathInfo();
		if( path.equals( "/register" ) ) {
			getRequestAsJson();
			register();
		} else if( path.equals( "/unregister" ) ) {
			getRequestAsJson();
			unregister();
		} else if( path.equals( "/sendMission" ) ) {
			initiateNewCaseAndSendToClients();
		}
	}
	
	/*
	 * 	Register GCM client on server and persist registrationId
	 */
	private void register() {
		String email = (String) requestAsJson.get( Shared.PARAMETER_EMAIL );
		String regId = (String) requestAsJson.get( Shared.PARAMETER_REGISTRATIONID );
		String model = (String) requestAsJson.get( Shared.PARAMETER_MODEL );
		String version = (String) requestAsJson.get( Shared.PARAMETER_VERSION );
		
		logger.log( Level.INFO, "GcmServlet is registering email: {0} on model: {2} with version: {3} with regId: {1}",
				new Object[] { email, regId, model, version } );

		Backend.getInstance().updateSingleUserField(
				Shared.USER_EMAIL, email,
				Shared.USER_REGISTRATIONID, regId );

		response.setStatus( HttpServletResponse.SC_OK );
	}

	/*
	 * 	Unregister GCM client on server and persist registrationId = ""
	 */
	private void unregister() {
		String regId = (String) requestAsJson.get( Shared.PARAMETER_REGISTRATIONID );
		logger.log( Level.INFO, "GcmServlet is unregistering regId: {0}", regId );

		Backend.getInstance().updateSingleUserField(
				Shared.USER_REGISTRATIONID, regId,
				Shared.USER_REGISTRATIONID, "" );

		response.setStatus( HttpServletResponse.SC_OK );
	}
	
	/*
	 *	Sending startCase message to all clients in immediate proximity...
	 * 	using MulticastResult result = sender.send( message, devices, 5 );
	 */
	private void initiateNewCaseAndSendToClients() throws IOException {
		String caseLatitude = request.getParameter( Shared.PARAMETER_CASE_LATITUDE );
		String caseLongitude = request.getParameter( Shared.PARAMETER_CASE_LONGITUDE );
		String caseAddress = request.getParameter( Shared.PARAMETER_CASE_ADDRESS );
		String caseNotes = request.getParameter( Shared.PARAMETER_CASE_NOTES );
		
		logger.log( Level.INFO, "sendMissionToClients() with address {2} starting case at: {0} long: {1} ... notes: {3} ",
				new Object[] { caseLatitude, caseLongitude, caseAddress, caseNotes } );
		
		double latitude, longitude;
		try{
			latitude = Double.parseDouble( caseLatitude );
			longitude = Double.parseDouble( caseLongitude );
		} catch( NumberFormatException nfe ) {
			logger.log( Level.INFO, "NumberFormatException: {0}", new Object[] { nfe.getMessage() } );
			return;
		}
		
		logger.log( Level.INFO, "newCase as double : {0} / {1}", new Object[] { latitude, longitude } );
		
		final ParcelableCaseData newCaseData = new ParcelableCaseData(
				"X", System.currentTimeMillis(), caseAddress, caseNotes, new ParcelableLocation( latitude, longitude ) );
		
		ArrayList<ParcelableUser> notifiedUsers = new ArrayList<ParcelableUser>();
		
		List<User> userList = Backend.getInstance().getUsersRegisteredOnGCM();
		for( User user : userList ) {
			logger.log( Level.INFO, "Checking distance of user {0}", user );
			
			int distance = (int) Shared.calculateDistance( 
					latitude, longitude, user.getLocation().getLatitude(), user.getLocation().getLongitude() );
			
			if( distance < user.getNotificationRadius() ) {
				logger.log( Level.INFO, "Alarming {0} with distance {1} to case", new Object[]{ user.getEmail(), distance } );
				
				Builder builder = new Message.Builder()
						.addData( Shared.PARAMETER_REGISTRATIONID, user.getRegistrationId() )
						.addData( Shared.CASE_ID, newCaseData.getCaseId() )
						.addData( Shared.CASEDATA_OBJECT, new Gson().toJson( newCaseData ) );
		
				sender.send( builder.build(), user.getRegistrationId(), 5 );
				logger.log( Level.INFO, "BUILD: {0}", new Object[] { builder.build().toString() } );
				notifiedUsers.add( user );
			} else {
				logger.log( Level.INFO, "User {0} is too far away... {1} meters", new Object[]{ user.getEmail(), distance } );
			}
		}
		
		newCaseData.setNotifiedUsers( notifiedUsers );
		webSocketApplication.manageNewCase( newCaseData );
		response.setStatus( HttpServletResponse.SC_OK );
	}
	
	/*
	 * Get servlet requestContent as Json
	 * ToKnow: after reading the content 1 time, it is not available anymore !
	 */
	private void getRequestAsJson() throws IOException {
		String str = Shared.streamToString( request.getInputStream() );
		Type JSONObjectType = new TypeToken<JSONObject>(){}.getType();
		requestAsJson = new Gson().fromJson( str, JSONObjectType );
	}
	
}
