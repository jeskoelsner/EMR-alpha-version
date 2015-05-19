package org.zlwima.emurgency.webservice.websocket;

import com.sun.grizzly.websockets.WebSocket;

public class StaticClient {
	private String email;
	private WebSocket websocket;
	private long lastTimestamp;

	private StaticClient() {
	}
	
	public StaticClient( String email, WebSocket websocket, long lastTimestamp ) {
		this.email = email;
		this.websocket = websocket;
		this.lastTimestamp = lastTimestamp;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail( String email ) {
		this.email = email;
	}

	public WebSocket getWebsocket() {
		return websocket;
	}

	public void setWebsocket( WebSocket websocket ) {
		this.websocket = websocket;
	}

	public long getTimestamp() {
		return lastTimestamp;
	}

	public void setTimestamp( long lastTimestamp ) {
		this.lastTimestamp = lastTimestamp;
	}
}
