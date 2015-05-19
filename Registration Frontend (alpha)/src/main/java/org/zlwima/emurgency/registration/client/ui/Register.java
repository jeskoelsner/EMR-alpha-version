package org.zlwima.emurgency.registration.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.logging.Logger;
import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.registration.client.Controller;
import org.zlwima.emurgency.registration.client.resources.Resources;
import org.zlwima.emurgency.registration.client.widgets.GenderSelector;
import org.zlwima.emurgency.registration.client.widgets.ImageButton;
import org.zlwima.emurgency.registration.client.widgets.LabeledPasswordBox;
import org.zlwima.emurgency.registration.client.widgets.LabeledTextBox;
import org.zlwima.emurgency.registration.shared.FieldVerifier;

public class Register extends Composite {

	private static final Logger logger = Logger.getLogger( Register.class.getName() );
	
	@UiField GenderSelector gender;
	
	@UiField ImageButton registerButton;
	@UiField ImageButton exitButton;
	
	@UiField LabeledPasswordBox password1;
	@UiField LabeledPasswordBox password2;
	
	@UiField LabeledTextBox email;
	@UiField LabeledTextBox firstName;
	@UiField LabeledTextBox lastName;
	@UiField LabeledTextBox mobilePhone;
	@UiField LabeledTextBox zipCode;
	@UiField LabeledTextBox city;
	@UiField LabeledTextBox street;
	@UiField LabeledTextBox country;

	interface EntryViewUiBinder extends UiBinder<Widget, Register> {
	}
	private static EntryViewUiBinder uiBinder = GWT.create( EntryViewUiBinder.class );

	public Register() {
		Resources.Instance.style().ensureInjected();
		initWidget( uiBinder.createAndBindUi( this ) );

		registerButton.addClickHandler( new ClickHandler() {
			public void onClick( ClickEvent event ) {
				VerifyInputAndRegisterUser();
			}
		} );

		exitButton.addClickHandler( new ClickHandler() {
			public void onClick( ClickEvent event ) {
				Controller.loginView.display();
			}
		} );
	}

	public void display() {
		RootPanel.get( "a" ).clear();
		RootPanel.get( "a" ).add( this );
	}

	/*
	 * this function validates the fields and prepares the hashmap for the RPC
	 */
	private void VerifyInputAndRegisterUser() {
		if( !FieldVerifier.isValidEmail( email.getValue() ) ) {
			Window.alert( "Invalid email entered !!!" );
			return;
		} else if( password1.getValue().isEmpty() || !password1.getValue().equals( password2.getValue() ) ) {
			Window.alert( "Passwords do no match or empty !!!" );
			return;
		}
		
		HashMap<String, String> userAsHashMap = new HashMap<String, String>();
		userAsHashMap.put( Shared.USER_EMAIL, email.getValue() );
		userAsHashMap.put( Shared.USER_PASSWORD, password1.getValue() );
		userAsHashMap.put( Shared.USER_FIRSTNAME, firstName.getValue() );
		userAsHashMap.put( Shared.USER_LASTNAME, lastName.getValue() );
		userAsHashMap.put( Shared.USER_GENDER, gender.getValue() );
		userAsHashMap.put( Shared.USER_MOBILEPHONE, mobilePhone.getValue() );
		userAsHashMap.put( Shared.USER_ZIPCODE, zipCode.getValue() );
		userAsHashMap.put( Shared.USER_CITY, city.getValue() );
		userAsHashMap.put( Shared.USER_STREET, street.getValue() );
		userAsHashMap.put( Shared.USER_COUNTRY, country.getValue() );
		userAsHashMap.put( Shared.USER_RECEIVESNOTIFICATIONS, "true" );
		userAsHashMap.put( Shared.USER_NOTIFICATIONRADIUS, "1000" );
		
		Controller.rpcService.saveUser( true, userAsHashMap, new AsyncCallback<String>() {
			public void onFailure( Throwable caught ) {
			}

			public void onSuccess( String result ) {
				Window.alert( result );
				Controller.loginView.display();
			}
		} );
	}
}
