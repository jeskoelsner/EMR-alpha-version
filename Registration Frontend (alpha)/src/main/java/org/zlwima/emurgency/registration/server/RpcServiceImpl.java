package org.zlwima.emurgency.registration.server;

import com.google.gson.Gson;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.*;
import javax.mail.internet.*;
import org.zlwima.emurgency.backend.Backend;
import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.backend.SimpleHttpRequest;
import org.zlwima.emurgency.backend.model.User;
import org.zlwima.emurgency.registration.client.RpcService;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class RpcServiceImpl extends RemoteServiceServlet implements RpcService {

	protected final Logger logger = Logger.getLogger( RpcServiceImpl.class.getName() );

	public boolean loginUser( String email, String passwd ) {
		logger.log( Level.INFO, "loginUser... {0} {1}", new Object[] { email, passwd } );
		return Backend.getInstance().findUserByEmailAndPassword( email, passwd );
	}

	public String saveUser( Boolean IS_NEW_USER, HashMap<String, String> userAsHashMap ) {
		String email = userAsHashMap.get( Shared.USER_EMAIL );
		User user = Backend.getInstance().findUserByEmail( email );
		if( user != null && IS_NEW_USER ) {
			return "User exists already !!!";
		} else if( user == null ) {
			if( IS_NEW_USER ) {
				user = new User();
				user.setEmail( email );
				user.setLevel( -1 );
				sendEmail( email );				
			} else {
				return "User cannot be found in database !!!";
			}
		}
		user.setPassword( userAsHashMap.get( Shared.USER_PASSWORD ) );
		user.setFirstName( userAsHashMap.get( Shared.USER_FIRSTNAME ) );
		user.setLastName( userAsHashMap.get( Shared.USER_LASTNAME ) );
		user.setGender( userAsHashMap.get( Shared.USER_GENDER ) );
		user.setMobilePhone( userAsHashMap.get( Shared.USER_MOBILEPHONE ) );
		user.setZipcode( userAsHashMap.get( Shared.USER_ZIPCODE ) );
		user.setCity( userAsHashMap.get( Shared.USER_CITY ) );
		user.setStreet( userAsHashMap.get( Shared.USER_STREET ) );
		user.setCountry( userAsHashMap.get( Shared.USER_COUNTRY ) );
		user.setReceivesNotifications( userAsHashMap.get( Shared.USER_RECEIVESNOTIFICATIONS ).equals( "true" ) );		
		user.setNotificationRadius( Long.parseLong( userAsHashMap.get( Shared.USER_NOTIFICATIONRADIUS ) ) );
		Backend.getInstance().saveUser( user );
		
		// forward new user request to KU Leuven API
		User forwardUser = new User();
		forwardUser.setEmail( user.getEmail() );
		forwardUser.setFirstName( user.getFirstName() );
		forwardUser.setLastName( user.getLastName() );
		logger.log( Level.INFO, "GSON: {0}", new Gson().toJson( forwardUser ) );	
		SimpleHttpRequest.httpPost( "http://as-emurgency.appspot.com/api/user/add", new Gson().toJson( forwardUser ) );
		
		return "Success :)";
	}

	public HashMap<String, String> getUserDetails( String email ) {
		User user = Backend.getInstance().findUserByEmail( email );
		HashMap<String, String> details = new HashMap<String, String>();
		if( user != null ) {
			details.put( Shared.USER_EMAIL, email );
			details.put( Shared.USER_PASSWORD, user.getPassword() );
			details.put( Shared.USER_LEVEL, String.valueOf( user.getLevel() ) );			
			details.put( Shared.USER_FIRSTNAME, user.getFirstName() );
			details.put( Shared.USER_LASTNAME, user.getLastName() );
			details.put( Shared.USER_GENDER, user.getGender() );
			details.put( Shared.USER_MOBILEPHONE, user.getMobilePhone() );
			details.put( Shared.USER_ZIPCODE, user.getZipcode() );
			details.put( Shared.USER_CITY, user.getCity() );
			details.put( Shared.USER_STREET, user.getStreet() );
			details.put( Shared.USER_COUNTRY, user.getCountry() );
			details.put( Shared.USER_RECEIVESNOTIFICATIONS, user.getReceivesNotifications().toString() );
			details.put( Shared.USER_NOTIFICATIONRADIUS, String.valueOf( user.getNotificationRadius() ) );
		}
		return details;
	}

	public void sendEmail( String email ) {
		logger.log( Level.INFO, "Sending an email to ...{0}", email );
		Properties properties = System.getProperties();
		properties.setProperty( "mail.smtp.host", "localhost" );
		Session session = Session.getDefaultInstance( properties );
		try {
			MimeMessage message = new MimeMessage( session );
			message.addRecipient( Message.RecipientType.TO, new InternetAddress( email ) );
			message.setSubject( "Emurgency Volunteer Registration Confirmation" );
			message.setText( "Hello,\n \t You are now successfully registered. You can edit your details upon login..." );
			logger.log( Level.INFO, "Sending the message{0} \nto: {1}", new Object[] { message.getSubject(), message.getAllRecipients().toString() } );
			Transport.send( message );
			logger.info( "Sent message successfully...." );
		} catch( MessagingException mex ) {
			logger.info( mex.toString() );
		}
	}
}
