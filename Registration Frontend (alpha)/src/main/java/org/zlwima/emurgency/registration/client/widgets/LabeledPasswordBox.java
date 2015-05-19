package org.zlwima.emurgency.registration.client.widgets;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;

public class LabeledPasswordBox extends Composite {
	AbsolutePanel content = new AbsolutePanel();
	Label label = new Label( "header" );
	PasswordTextBox textbox = new PasswordTextBox();

	public LabeledPasswordBox() {	
		content.setPixelSize( 400, 100 );
		content.add( label, 0, 3 );
		initWidget( content );
	}
	
	public void setFocus( boolean focus ) {
		textbox.setFocus( focus );		
	}
	
	public void setText( String text ) {
		label.setText( text );		
	}

	public void setLabelWidth( String width ) {
		content.add( textbox, Integer.parseInt( width ), 0 );
	}
	
	public void setTextBoxWidth( String width ) {
		textbox.setWidth( width + "px" );
	}
	
	public void setLabelStyle( String style ) {
		label.addStyleName( style );
	}
	
	public void setValue( String value ) {
		textbox.setValue( value );
	}
	
	public String getValue() {
		return textbox.getValue();
	}
	
}
