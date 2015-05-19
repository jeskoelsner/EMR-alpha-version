package org.zlwima.emurgency.registration.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import org.zlwima.emurgency.registration.client.resources.Resources;
import org.zlwima.emurgency.registration.client.ui.Edit;
import org.zlwima.emurgency.registration.client.ui.Login;
import org.zlwima.emurgency.registration.client.ui.Register;

public class Controller implements EntryPoint{
	
	public static final RpcServiceAsync rpcService = GWT.create( RpcService.class );
	
	public static final Resources res = Resources.Instance;
	
	public static final Login loginView = new Login();
	
	public static final Register registerView = new Register();
	
	public static final Edit editView = new Edit();
	
	public void onModuleLoad() {
		
		res.style().ensureInjected();
		
		Controller.loginView.display();
		
	}
	
}
