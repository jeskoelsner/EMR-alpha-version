package org.zlwima.emurgency.webservice;

import com.google.gson.Gson;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zlwima.emurgency.backend.Backend;
import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.backend.Shared.LoginCallback;
import org.zlwima.emurgency.backend.Shared.RegisterCallback;
import org.zlwima.emurgency.backend.Shared.UpdateLocationCallback;
import org.zlwima.emurgency.backend.SimpleHttpRequest;
import org.zlwima.emurgency.backend.model.User;

@Controller
public class RestFullController {
	
	protected static final Logger logger = Logger.getLogger( RestFullController.class.getName() );
	
	/*
	 * API call to register a new user...  
	 */		
	@RequestMapping(value = "/register", method = RequestMethod.POST, headers = "Accept=application/json, application/xml")
	public @ResponseBody int addUser( @RequestBody String request ) {
		logger.log( Level.INFO, "received request to register new user {0}", request );
		User requestUser = new Gson().fromJson( request, User.class);
		
		if( Backend.getInstance().findUserByEmail( requestUser.getEmail() ) != null ) {
			return RegisterCallback.EXISTS_ALREADY;
		}
		
		if( !requestUser.getEmail().isEmpty() ) {
			requestUser.setLevel( -1 );	// just to be sure
			Backend.getInstance().addUser( requestUser );

			// forward new user request to KU Leuven API
			User forwardUser = new User();
			forwardUser.setEmail( requestUser.getEmail() );
			forwardUser.setFirstName( requestUser.getFirstName() );
			forwardUser.setLastName( requestUser.getLastName() );
			logger.log( Level.INFO, "GSON: {0}", new Gson().toJson( forwardUser ) );
			SimpleHttpRequest.httpPost( "http://as-emurgency.appspot.com/api/user/add", forwardUser.toJson() );
										
			return RegisterCallback.REGISTERED;
		}
		
		return RegisterCallback.ERROR;
	}
	
	/*
	 * API call to login a user...
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST, headers = "Accept=application/xml, application/json")
	public @ResponseBody int loginRequest( @RequestBody String request ) {
		logger.log( Level.INFO, "received loginRequest() with requestBody: {0}", request );
		User requestUser = new Gson().fromJson( request, User.class);
		User user = Backend.getInstance().findUserByEmail( requestUser.getEmail() );
		if( user != null ) { 
			if( user.getPassword().equals( requestUser.getPassword() ) ) {
				if( user.getLevel() > 0 ) {
					return LoginCallback.CONFIRMED;
				} else {
					return LoginCallback.VALID;
				}
			}
		}
		return LoginCallback.INVALID;						
	}

	/*
	 * API call to update a clients location on the server...
	 */
	@RequestMapping(value = "/updateLocation", method = RequestMethod.POST, headers = "Accept=application/xml, application/json")
	public @ResponseBody int updateLocation( @RequestBody String request ) {
		logger.log( Level.INFO, "received updateLocation() with requestBody: {0}", request );	
		User requestUser = new Gson().fromJson( request, User.class);
		User user = Backend.getInstance().findUserByEmail( requestUser.getEmail() );
		if( user.getRegistrationId().equals( requestUser.getRegistrationId() ) ) {
			requestUser.getLocation().setTimestamp( System.currentTimeMillis() );	
			Backend.getInstance().updateSingleUserField( Shared.USER_EMAIL, requestUser.getEmail(),	Shared.USER_LOCATION, requestUser.getLocation() );
			return( user.getLevel() > 0 ? 
					UpdateLocationCallback.UPDATED_USER_IS_CONFIRMED : 
					UpdateLocationCallback.UPDATED_USER_IS_NOT_CONFIRMED );
		} else {
			return UpdateLocationCallback.FAILED;
		}		
	}
	
}

//  @Resource(name = "webservice") Webservice webservice;

//	@XmlRootElement(name = "userlist")
//	public static class UserList {	
//		private List<User> data;
//		public List<User> getData() {
//			return data;
//		}
//		public void setData( List<User> data ) {
//			this.data = data;
//		}
//	}

//	@RequestMapping(value = "/getAllUsers", method = RequestMethod.GET, headers = "Accept=application/xml, application/json")
//	public @ResponseBody UserList getAllUsers() {		
//		UserList userlist = new UserList();
//		userlist.setData( Backend.getInstance().getAllUsers() );
//		return userlist;
//	}
	
//	@RequestMapping(value = "/deleteAllUsers", method = RequestMethod.GET, headers = "Accept=application/xml, application/json")
//	public @ResponseBody void deleteUserList() {
//		Backend.getInstance().deleteUserList();
//	}	
