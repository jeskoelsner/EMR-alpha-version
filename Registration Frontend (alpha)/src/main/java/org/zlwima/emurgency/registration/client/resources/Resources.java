package org.zlwima.emurgency.registration.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ClientBundle.Source;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;

public interface Resources extends ClientBundle {
	
    public static final Resources Instance = GWT.create( Resources.class );
    public static final DateTimeFormat dateformat = DateTimeFormat.getFormat( "yyyy-mm-dd" );
    
    public interface Images extends ClientBundle {
		ImageResource user();
		
		ImageResource user_add();
		
		ImageResource user_edit();
		
		ImageResource user_delete();		
		
        @Source("smurfs1.jpg")
        DataResource smurfs1();
    }

    public interface Style extends CssResource {
		
		String confirmed();
		
		String unconfirmed();
		
        String bold();		
    }

    @Source("Style.css")
    @CssResource.NotStrict
    Style style();

    Images images();
}
