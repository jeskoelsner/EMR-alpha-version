package org.zlwima.emurgency.registration.client.widgets;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;

public class GenderSelector extends Composite {
	AbsolutePanel content = new AbsolutePanel();
	RadioButton buttonMale = new RadioButton( "gender", "male" );	
	RadioButton buttonFemale = new RadioButton( "gender", "female" );	
	Label label = new Label();

	public GenderSelector() {	
		content.setPixelSize( 400, 100 );
		content.add( label, 0, 0 );
		content.add( buttonMale, 140, 0 );
		content.add( buttonFemale, 240, 0 );		
		initWidget( content );
	}
	
	public void setText( String text ) {
		label.setText( text );		
	}
	
	public void setLabelStyle( String style ) {
		label.addStyleName( style );
	}
	
	public void setValue( String value ) {
		buttonMale.setValue( false );
		buttonFemale.setValue( false );
		if( value.equals( "male" ) ) {
			buttonMale.setValue( true );
		} else if( value.equals( "female" ) ) {
			buttonFemale.setValue( true );			
		}
	}
	
	public String getValue() {
		if( buttonMale.getValue() ) {
			return "male";
		} else if( buttonFemale.getValue() ) {
			return "female";
		} else {
			return "";
		}
	}
	
}
