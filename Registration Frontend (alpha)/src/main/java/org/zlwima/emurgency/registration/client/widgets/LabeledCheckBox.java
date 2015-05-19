package org.zlwima.emurgency.registration.client.widgets;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class LabeledCheckBox extends Composite {
	AbsolutePanel content = new AbsolutePanel();
	CheckBox checkbox = new CheckBox();	
	Label label = new Label();

	public LabeledCheckBox() {	
		content.setPixelSize( 400, 100 );
		content.add( label, 0, 0 );
		content.add( checkbox, 200, 0 );	
		initWidget( content );
	}
	
	public void setText( String text ) {
		label.setText( text );		
	}
	
	public void setCheckBoxText( String text ){
		checkbox.setText( text );
	}
	
	public void setLabelStyle( String style ) {
		label.addStyleName( style );
	}
	
	public void setValue( String value ) {
		checkbox.setValue( value.equals( "true" ) ? true : false );
	}
	
	public String getValue() {
		return checkbox.getValue() ? "true" : "false";
	}
	
}
