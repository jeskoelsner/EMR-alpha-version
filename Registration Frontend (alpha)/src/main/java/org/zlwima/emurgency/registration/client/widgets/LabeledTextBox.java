package org.zlwima.emurgency.registration.client.widgets;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class LabeledTextBox extends Composite {
	AbsolutePanel content = new AbsolutePanel();
	Label label = new Label( "header" );
	TextBox textbox = new TextBox();

	public LabeledTextBox() {	
		content.setPixelSize( 400, 100 );
		content.add( label, 0, 3 );
		initWidget( content );
	}
	
	public void setEnabled( boolean enabled ) {
		textbox.setEnabled( enabled );		
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
	
	public TextBox getBox() {
		return this.textbox;
	}
	
}
