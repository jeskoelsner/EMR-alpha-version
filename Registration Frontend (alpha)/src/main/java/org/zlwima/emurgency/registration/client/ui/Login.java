package org.zlwima.emurgency.registration.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.logging.Logger;
import org.zlwima.emurgency.registration.client.Controller;
import org.zlwima.emurgency.registration.client.resources.Resources;
import org.zlwima.emurgency.registration.client.widgets.ImageButton;
import org.zlwima.emurgency.registration.client.widgets.LabeledTextBox;

public class Login extends Composite {

	private static final Logger logger = Logger.getLogger( Login.class.getName() );
	
	@UiField ImageButton loginButton;
	@UiField ImageButton registerButton;
	@UiField LabeledTextBox emailBox;
	@UiField LabeledTextBox passwordBox;

	interface EntryViewUiBinder extends UiBinder<Widget, Login> {
	}
	private static EntryViewUiBinder uiBinder = GWT.create( EntryViewUiBinder.class );

	public Login() {
		Resources.Instance.style().ensureInjected();
		initWidget( uiBinder.createAndBindUi( this ) );

		passwordBox.getBox().addKeyPressHandler( new KeyPressHandler() {
			public void onKeyPress( KeyPressEvent event ) {
				if( event.getCharCode() == 10 || event.getCharCode() == 13 ) {
					sendLoginRequest();
				}
			}
		} );

		loginButton.addClickHandler( new ClickHandler() {
			public void onClick( ClickEvent event ) {
				sendLoginRequest();
			}
		} );

		registerButton.addClickHandler( new ClickHandler() {
			public void onClick( ClickEvent event ) {
				Controller.registerView.display();
			}
		} );

	}

	private void sendLoginRequest() {
		final String userEmail = emailBox.getValue();
		Controller.rpcService.loginUser( userEmail, passwordBox.getValue(), new AsyncCallback<Boolean>() {
			public void onFailure( Throwable caught ) {
				System.out.print( "ERROR during loginUser()..." );
			}
			public void onSuccess( Boolean LOGIN_VALID ) {
				if( LOGIN_VALID ) {
					Controller.editView.display( userEmail );
				} else {
					Window.alert( "Login failed !!!" );
				}
			}
		} );
	}

	public void display() {
		RootPanel.get( "a" ).clear();
		RootPanel.get( "a" ).add( this );
		emailBox.setFocus( true );
	}
}
