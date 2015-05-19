package org.zlwima.emurgency.webservice.websocket;

import com.google.gson.Gson;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.websockets.DataFrame;
import com.sun.grizzly.websockets.WebSocket;
import com.sun.grizzly.websockets.WebSocketApplication;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.zlwima.emurgency.backend.Backend;
import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.backend.SimpleHttpRequest;
import org.zlwima.emurgency.backend.model.ParcelableCaseData;
import org.zlwima.emurgency.backend.model.ParcelableUser;
import org.zlwima.emurgency.backend.model.ParcelableVolunteer;


public class EmrWebSocketApplication extends WebSocketApplication {
	
	protected static final Logger logger = Logger.getLogger( EmrWebSocketApplication.class.getName() );

	private final long NEW_CASE_TIMEOUT_VALUE		= 10 * 60 * 1000;	// in milliseconds	
	private final long STATIC_CLIENT_TIMEOUT_CHECK	= 5 * 60 * 1000;	// in milliseconds
	private final long STATIC_CLIENT_TIMEOUT_VALUE	= 10 * 60 * 1000;	// in milliseconds
	
	public final Timer staticClientTimeoutChecker = new Timer();
	
	public boolean staticClientTimeoutCheckerShouldRenew = false;

	private final List<StaticClient> staticClients = Collections.synchronizedList( new ArrayList<StaticClient>() );		

	private final List<ParcelableCaseData> activeCaseList = Collections.synchronizedList( new ArrayList<ParcelableCaseData>() );
	
	public List<ParcelableCaseData> getActiveCaseList() {
		return this.activeCaseList;
	}	
	
	/*
	 * Only accept WS requests that provide a valid caseId
	 */
	@Override
	public boolean isApplicationRequest( Request request ) {
		String caseId = request.requestURI().toString().substring( 1 );
		ParcelableCaseData caseData = findCaseByCaseId( caseId );
		logger.log( Level.INFO, "::: isApplicationRequest() caseId: {0} *** caseData: {1}", new Object[] { caseId, caseData } );
		return true;		
//		if( caseId.equals( "STATIC" ) ) {
//			return true;
//		}
//		return( caseData != null ? true : false );
	}

	@Override
	public void onConnect( WebSocket socket ) {
		super.onConnect( socket );
		logger.log( Level.INFO, "::: onConnect() for socket {0} [total:{1}]", new Object[] { socket, getWebSockets().size() } );
	}
	
	@Override
	public void onClose( WebSocket socket, DataFrame frame ) {
		super.onClose( socket, frame );
		for( StaticClient sc : staticClients ) {
			if( sc.getWebsocket().equals( socket ) ) {
				staticClients.remove( sc );
				logger.log( Level.INFO, "::: onClose() REMOVED socket {0} from static clients [total static clients:{1}]", new Object[] { socket, staticClients.size() } );
			}
		}
		logger.log( Level.INFO, "::: onClose() for socket {0} [total websockets:{1}]", new Object[] { socket, getWebSockets().size() } );
	}
		
	@Override
	public void onMessage( WebSocket socket, String text ) {
		logger.log( Level.INFO, "RECEIVED onMessage() from socket {0} with text: {1}", new Object[] { socket, text } );
		try {
			JSONObject json = new JSONObject( text );
			int messageType = json.getInt( Shared.MESSAGE_TYPE );
			
			// register a static client... expects json: MESSAGE_TYPE, USER_OBJECT
			if( messageType == Shared.WebsocketCallback.CLIENT_IS_STATIC_CLIENT ) {
				logger.info( "WebsocketCallback.CLIENT_IS_STATIC_CLIENT" );
				ParcelableUser user = new Gson().fromJson( json.getString( Shared.USER_OBJECT ), ParcelableUser.class );				
				
				Backend.getInstance().updateSingleUserField( 
						Shared.USER_EMAIL, user.getEmail(), Shared.USER_STATIC_LOCATION, user.getStaticLocation() );
				
				staticClients.add( new StaticClient( 
						user.getEmail(), 
						socket, 
						System.currentTimeMillis() ) );
				
				socket.send( "wOOt" );
				return;
			}
			
			String caseId = json.getString( Shared.CASE_ID );
			ParcelableCaseData caseData = findCaseByCaseId( caseId );
			if( caseData == null ) {
				logger.info( "~~~~~~~~~~~~~~~~~~~~ ?! onMessage() IGNORED because case is not activ ?! ~~~~~~~~~~~~~~~~~~~~~~~~~~" );
				return;	// case does not exist or has been closed already !
			}
			ParcelableVolunteer volunteer;			
			switch( messageType ) {				
				case Shared.WebsocketCallback.CLIENT_SENDS_CASE_ID:
					logger.info( "WebsocketCallback.CLIENT_SENDS_CASE_ID" );
					caseData.getWebsockets().add( socket );
					logger.log( Level.INFO, ">>> WEBSOCKETS IN LIST: {0}", new Object[] { caseData.getWebsockets().size() } );
					break;				
				
				case Shared.WebsocketCallback.CLIENT_SENDS_ACCEPT_MISSION:
					logger.info( "WebsocketCallback.CLIENT_SENDS_ACCEPT_MISSION" );
					volunteer = new Gson().fromJson( json.getString( Shared.VOLUNTEER_OBJECT ), ParcelableVolunteer.class );
					caseData.getVolunteers().add( volunteer );
					broadcastUpdatedCaseData( caseData );
					break;
				
				case Shared.WebsocketCallback.CLIENT_SENDS_LOCATION_UPDATE:
					logger.info( "WebsocketCallback.CLIENT_SENDS_LOCATION_UPDATE" );
					volunteer = new Gson().fromJson( json.getString( Shared.VOLUNTEER_OBJECT ), ParcelableVolunteer.class );
					Backend.getInstance().updateSingleUserField( 
							Shared.USER_EMAIL, volunteer.getEmail(), Shared.USER_LOCATION, volunteer.getLocation() );
					updateVolunteerDataForCaseId( caseData, volunteer );
					broadcastUpdatedCaseData( caseData );
					break;
				
				default:
					logger.info( "WebsocketCallback.DEFAULT ???" );
					break;
			}
		} catch( JSONException ex ) {
			logger.log( Level.INFO, "onMessage()... JSONException {0}", ex.toString() );
		}
	}
	
	/*
	 * Publishes the updated and trimmed caseData to all connected websocket clients of a specific case
	 */
	private void broadcastUpdatedCaseData( ParcelableCaseData caseData ) {
		logger.log( Level.INFO, "publishUpdatedCaseData() for case: {0} ", new Object[] { caseData.getCaseId() } );		
		caseData.setCaseRunningTimeMillis( System.currentTimeMillis() - caseData.getCaseStartTimeMillis() );
		
		try {
			JSONObject json = new JSONObject();
			json.put( Shared.MESSAGE_TYPE, Shared.WebsocketCallback.SERVER_SENDS_CASEDATA );
			json.put( Shared.CASEDATA_OBJECT, new Gson().toJson( caseData.getTrimmedCaseData() ) );
			
			for( Object socket : caseData.getWebsockets() ) {
				((WebSocket)socket).send( json.toString() );
				logger.log( Level.INFO, "PUBLISHED: {0} TO SOCKET {1}", new Object[] { json.toString(), socket } );
			}
		} catch( JSONException ex ) {
			logger.log( Level.INFO, "JSONEXCEPTION in broadcastUpdatedCaseData({0}) {1}", new Object[] { caseData.getCaseId(), ex } );
		}

	}
	
	/*
	 * Publishes the CLOSECASE event all connected websocket clients of a specific case
	 */	
	private void broadcastCloseCaseEvent( ParcelableCaseData caseData ) {
		try {			
			JSONObject json = new JSONObject();
			json.put( Shared.MESSAGE_TYPE, Shared.WebsocketCallback.SERVER_SENDS_CLOSE_CASE );
			json.put( Shared.CASE_ID, caseData.getCaseId() );
			
			for( Object socket : caseData.getWebsockets() ) {
				logger.log( Level.INFO, "CLOSING CASE {0} ON SOCKET {1}", new Object[] { caseData.getCaseId(), socket } );
				((WebSocket)socket).send( json.toString() );
			}
		} catch( JSONException ex ) {
			logger.log( Level.INFO, "JSONEXCEPTION in broadcastCloseCaseEvent({0}) {1}", new Object[] { caseData.getCaseId(), ex } );
		}
	}
	
	/*
	 * Updates a volunteer if he/she is in the list of active volunteers of a specific case
	 */
	private void updateVolunteerDataForCaseId( ParcelableCaseData caseData, ParcelableVolunteer volunteer ) {
		ArrayList<ParcelableVolunteer> activeVolunteers = caseData.getVolunteers();
		for( ParcelableVolunteer aVolunteer : activeVolunteers ) {
			if( aVolunteer.getEmail().equals( volunteer.getEmail() ) ) {
				activeVolunteers.remove( aVolunteer );
				activeVolunteers.add( volunteer );
				break;
			}
		}
	}
	
	/*
	 * Returns an active case determined by its caseId
	 */
	private ParcelableCaseData findCaseByCaseId( String caseId ) {
		ParcelableCaseData foundCase = null;
		for( ParcelableCaseData aCase : activeCaseList ) {
			if( aCase.getCaseId().equals( caseId ) ) {
				foundCase = aCase;
				break;
			}	
		}
		return foundCase;
	}
		
	/*
	 * Start Timer for a new case and manage the corresponding ArrayList
	 */
	public void manageNewCase( final ParcelableCaseData caseData ) {
		activeCaseList.add( caseData );
		logger.log( Level.INFO, "-> ACTIVE CASE LIST INCLUSIVE ADDED CASE: {0} ", new Object[] { activeCaseList.size() } );
		TimerTask newCaseTimeoutTask = new TimerTask() {		
			@Override
			public void run() {
				broadcastCloseCaseEvent( caseData );
				
				activeCaseList.remove( caseData );
				logger.log( Level.INFO, "-> ACTIVE CASE LIST AFTER CLOSING: {0} ", new Object[] { activeCaseList.size() } );
			}
		};
		new Timer().schedule( newCaseTimeoutTask, NEW_CASE_TIMEOUT_VALUE );
		
		// forward new case event to KU Leuven API
		ForwardedCaseData forwardedCaseData = new ForwardedCaseData(
			caseData.getCaseId(),
			caseData.getCaseLocation().getLatitude(),
			caseData.getCaseLocation().getLongitude(),
			caseData.getCaseAddress(),				
			caseData.getCaseNotes(),
			caseData.getNotifiedUsers().size()
		);
		logger.log( Level.INFO, "GSON: {0}", new Gson().toJson( forwardedCaseData ) );			
		SimpleHttpRequest.httpPost( "http://as-emurgency.appspot.com/api/case/add", new Gson().toJson( forwardedCaseData ) );
	}
	
	public void startStaticClientTimeoutChecker() {
		final TimerTask staticClientTimeoutTask = new TimerTask() {
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				
				for( StaticClient sc : staticClients ) {
					long x = now - sc.getTimestamp();
					
					if( x > STATIC_CLIENT_TIMEOUT_VALUE ) {
						logger.log( Level.INFO, "-> REMOVED STATIC CLIENT ON TIMOUT {0}", new Object[] { sc } );
						sc.getWebsocket().close();
					}
				}
				
				if( staticClientTimeoutCheckerShouldRenew ) {
					staticClientTimeoutChecker.schedule( this, STATIC_CLIENT_TIMEOUT_CHECK );					
				}
			}
		};
		staticClientTimeoutChecker.schedule( staticClientTimeoutTask, STATIC_CLIENT_TIMEOUT_CHECK );
		staticClientTimeoutCheckerShouldRenew = true;
	}
	
	public void shutdown() {
		for( WebSocket webSocket : getWebSockets() ) {
			if( webSocket.isConnected() ) {
	            webSocket.onClose( null );
			}
		}
        getWebSockets().clear();
	}
	
}