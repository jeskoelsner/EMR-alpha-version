package org.zlwima.emurgency.registration.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.HashMap;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface RpcService extends RemoteService {

	boolean loginUser( String email, String pwd );

	String saveUser( Boolean IS_NEW_USER, HashMap<String,String> userAsHashMap );
	
	HashMap<String,String> getUserDetails( String email );

}
