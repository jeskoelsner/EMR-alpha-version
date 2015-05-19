package org.zlwima.emurgency.webservice.gcm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Context initializer that loads the API key from WEB-INF/classes
 */
public class ApiKeyInitializer implements ServletContextListener {
	
	private static final String PATH = "/api.key";
	static final String ATTRIBUTE_ACCESS_KEY = "apiKey";
	
	static final Logger logger = Logger.getLogger( ApiKeyInitializer.class.getName() );
	
	@Override
	public void contextInitialized( ServletContextEvent event ) {
		String key = getKey();
		logger.log( Level.INFO, "activ key is: {0}", key);
		event.getServletContext().setAttribute( ATTRIBUTE_ACCESS_KEY, key );
	}

	protected String getKey() {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream( PATH );
		if( stream == null ) {
			throw new IllegalStateException( "Could not find file " + PATH + " on web resources" );
		}
		BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );
		try {
			String key = reader.readLine();
			return key;
		} catch( IOException e ) {
			throw new RuntimeException( "Could not read file " + PATH, e );
		} finally {
			try {
				reader.close();
			} catch( IOException e ) {
				logger.log( Level.WARNING, "Exception closing " + PATH, e );
			}
		}
	}

	@Override
	public void contextDestroyed( ServletContextEvent event ) {
	}

}
