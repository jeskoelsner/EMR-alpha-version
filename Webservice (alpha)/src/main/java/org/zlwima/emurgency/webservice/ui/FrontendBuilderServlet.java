package org.zlwima.emurgency.webservice.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.zlwima.emurgency.backend.Backend;
import org.zlwima.emurgency.backend.model.User;

@SuppressWarnings("serial")
public class FrontendBuilderServlet extends HttpServlet {

	static final Logger logger = Logger.getLogger( FrontendBuilderServlet.class.getName() );

	@Override
	protected void doPost( HttpServletRequest req, HttpServletResponse resp ) throws IOException {
		doGet( req, resp );
	}

	@Override
	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws IOException {		
		List<User> userList = Backend.getInstance().getUsersRegisteredOnGCM();
		
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "/startcase.html" );
		
		Document document = Jsoup.parse( stream, "UTF-8", "137.226.188.51" );
		Element htmlElement = document.getElementById( "devices" );				
		if( userList.isEmpty() ) {
			htmlElement.append( "<h2>There are no users registered on the GCM server right now...</h2>" );
		} else {
			htmlElement.append( "<h2>The following users are registered with a GCM registrationId...</h2>" );
			for( User user : userList ) {
				htmlElement.append( "<h4> User: " + user.getEmail() + "<br/>" + user.getRegistrationId() + "<br/></h4>" );
			}
		}

		response.setContentType( "text/html" );
		response.getWriter().print( document.html() );		
		response.setStatus( HttpServletResponse.SC_OK );
	}

}
