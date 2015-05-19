package org.zlwima.emurgency.registration.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
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

public class Edit extends Composite {

	private static final Logger logger = Logger.getLogger( Edit.class.getName() );
	
	@UiField Label status;
	
	@UiField GenderSelector gender;
	
	@UiField ImageButton saveButton;
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
	@UiField LabeledTextBox notificationRadius;	

	interface EntryViewUiBinder extends UiBinder<Widget, Edit> {
	}
	private static EntryViewUiBinder uiBinder = GWT.create( EntryViewUiBinder.class );

	public Edit() {
		Resources.Instance.style().ensureInjected();
		initWidget( uiBinder.createAndBindUi( this ) );
		
		email.setEnabled( false );		

		saveButton.addClickHandler( new ClickHandler() {
			public void onClick( ClickEvent event ) {
				VerifyInputAndSaveUser();
			}
		} );

		exitButton.addClickHandler( new ClickHandler() {
			public void onClick( ClickEvent event ) {
				Controller.loginView.display();
			}
		} );
	}

	public void display( String userEmail ) {
		Controller.rpcService.getUserDetails( userEmail, new AsyncCallback<HashMap<String, String>>() {
			public void onFailure( Throwable caught ) {
			}
			public void onSuccess( HashMap<String, String> details ) {
				email.setValue( details.get( Shared.USER_EMAIL ) );
				firstName.setValue( details.get( Shared.USER_FIRSTNAME ) );
				lastName.setValue( details.get( Shared.USER_LASTNAME ) );
				mobilePhone.setValue( details.get( Shared.USER_MOBILEPHONE ) );
				zipCode.setValue( details.get( Shared.USER_ZIPCODE ) );
				city.setValue( details.get( Shared.USER_CITY ) );
				street.setValue( details.get( Shared.USER_STREET ) );
				country.setValue( details.get( Shared.USER_COUNTRY ) );
				password1.setValue( details.get( Shared.USER_PASSWORD ) );
				password2.setValue( details.get( Shared.USER_PASSWORD ) );
				gender.setValue( details.get( Shared.USER_GENDER ) );
				notificationRadius.setValue( details.get( Shared.USER_NOTIFICATIONRADIUS ) );
				int level = Integer.parseInt( details.get( Shared.USER_LEVEL ) );
				if( level > 0 ) {
					status.setStyleName( Controller.res.style().confirmed() );
					status.setText( "Level: " + level );
				} else {
					status.setStyleName( Controller.res.style().unconfirmed() );
					status.setText( "Level: " + level );
				}
			}
		} );
		
		RootPanel.get( "a" ).clear();
		RootPanel.get( "a" ).add( this );
	}

	/*
	 * this functions validates the fields and prepares the hashmap for the RPC
	 */
	private void VerifyInputAndSaveUser() {
		if( password1.getValue().isEmpty() || !password1.getValue().equals( password2.getValue() ) ) {
			Window.alert( "Passwords do no match or empty !!!" );
			return;
		} else if( !FieldVerifier.isValidInteger( notificationRadius.getValue(), 1, 1000 ) ) {
			Window.alert( "Notification Radius needs to be between 1 and 1000" );
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
		userAsHashMap.put( Shared.USER_NOTIFICATIONRADIUS, notificationRadius.getValue() );		

		Controller.rpcService.saveUser( false, userAsHashMap, new AsyncCallback<String>() {
			public void onFailure( Throwable caught ) {
			}
			public void onSuccess( String result ) {
				Window.alert( result );
				Controller.loginView.display();
			}
		} );
	}
}
