
package org.zlwima.emurgency.staticclientapp;

import java.net.URI;
import javax.swing.ImageIcon;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONObject;
import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.backend.model.ParcelableLocation;
import org.zlwima.emurgency.backend.model.ParcelableUser;
import org.zlwima.emurgency.staticclientapp.websockets.WebSocket;
import org.zlwima.emurgency.staticclientapp.websockets.WebSocketEventHandler;
import org.zlwima.emurgency.staticclientapp.websockets.WebSocketMessage;


public class StaticClientApp extends javax.swing.JFrame {
	private WebSocket websocket = null;
	private ImageIcon icon;

    public StaticClientApp() {
        initComponents();
		icon =  new ImageIcon( this.getClass().getResource( "/images/icon_16x16.png" ) );
		setIconImage( icon.getImage() );
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        infoLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        messageArea = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        loginField = new javax.swing.JTextField();
        passwordField = new javax.swing.JTextField();
        submitButton = new javax.swing.JButton();
        latitudeField = new javax.swing.JTextField();
        longitudeField = new javax.swing.JTextField();
        altitudeField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("EMuRgency Static Client App");
        setIconImages(null);
        setResizable(false);

        infoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        infoLabel.setText("no websocket connection...");
        infoLabel.setToolTipText("");

        messageArea.setColumns(20);
        messageArea.setLineWrap(true);
        messageArea.setRows(5);
        messageArea.setToolTipText("websocket messages received from server...");
        messageArea.setEnabled(false);
        jScrollPane1.setViewportView(messageArea);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1)
                    .addComponent(infoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(infoLabel)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        loginField.setToolTipText("email");

        passwordField.setToolTipText("password");

        submitButton.setText("Login & Submit");
        submitButton.setToolTipText("LOGIN");
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        latitudeField.setText("50.0");
        latitudeField.setToolTipText("latitude");

        longitudeField.setText("60.0");
        longitudeField.setToolTipText("longitude");

        altitudeField.setText("0.0");
        altitudeField.setToolTipText("altitude");

        jLabel1.setText("latitude:");

        jLabel2.setText("longitude:");

        jLabel3.setText("altitude:");

        jLabel4.setText("email:");

        jLabel5.setText("password:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                            .addComponent(latitudeField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel2)
                                .addComponent(jLabel3))
                            .addGap(18, 18, 18)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(altitudeField)
                                .addComponent(longitudeField, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))))
                    .addComponent(submitButton)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel4)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(loginField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel5)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 16, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(loginField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 108, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(latitudeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(longitudeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(altitudeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addComponent(submitButton)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
		try {
			ParcelableUser user = new ParcelableUser();
			user.setEmail( loginField.getText() );
			user.setPassword( passwordField.getText() );
			PostMethod post = new PostMethod( Shared.Rest.LOGIN_URL );
			post.addRequestHeader( "Content-Type", Shared.Rest.CONTENT_TYPE_JSON );
			post.addRequestHeader( "Accept", Shared.Rest.CONTENT_TYPE_JSON );
			StringRequestEntity requestEntity = new StringRequestEntity( user.toJson(), "application/json", "UTF-8" );
			post.setRequestEntity( requestEntity );
			new HttpClient().executeMethod( post );
			int response = Integer.parseInt( post.getResponseBodyAsString( 1 ) );
			infoLabel.setText( "received: " + response );
			if( response == Shared.LoginCallback.CONFIRMED ) {
				connectToWebsocket();
			}
		} catch( Exception ex ) {
			System.out.println( ex.getMessage() );
		}
    }//GEN-LAST:event_submitButtonActionPerformed

	private void connectToWebsocket() {
		try {
			
			if( websocket != null ) {
				websocket.close();
			}
			
			websocket = new WebSocket( new URI( Shared.Rest.WEBSOCKET_URL + "/STATIC" ) );

			websocket.setEventHandler( new WebSocketEventHandler() {
				public void onOpen() {
					System.out.println( "--open" );
					ParcelableUser user = new ParcelableUser();
					user.setEmail( loginField.getText() );
					user.setPassword( passwordField.getText() );
					user.setStaticLocation( new ParcelableLocation(
							Double.parseDouble( latitudeField.getText() ),
							Double.parseDouble( longitudeField.getText() ) ) );

					JSONObject json = new JSONObject();
					try {
						json.put( Shared.MESSAGE_TYPE, Shared.WebsocketCallback.CLIENT_IS_STATIC_CLIENT );
						json.put( Shared.USER_OBJECT, user.toJson() );
						websocket.send( json.toString() );
					} catch( Exception ex ) {
						System.out.println( ex.getMessage() );						
					} 
				}

				public void onMessage( WebSocketMessage message ) {
					messageArea.setText( messageArea.getText() + " " + message.getText() );
				}

				public void onClose() {
					System.out.println( "--close" );
				}

				public void onPing() {
					System.out.println( "--PING" );
				}

				public void onPong() {
					System.out.println( "--PONG" );					
				}
			} );

			websocket.connect();
			// submitButton.setEnabled( false );

		} catch( Exception ex ) {
			System.out.println( ex.getMessage() );
		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main( String args[] ) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
			for( javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels() ) {
				if( "Nimbus".equals( info.getName() ) ) {
					javax.swing.UIManager.setLookAndFeel( info.getClassName() );
					break;
				}
			}
		} catch( ClassNotFoundException ex ) {
			java.util.logging.Logger.getLogger( StaticClientApp.class.getName() ).log( java.util.logging.Level.SEVERE, null, ex );
		} catch( InstantiationException ex ) {
			java.util.logging.Logger.getLogger( StaticClientApp.class.getName() ).log( java.util.logging.Level.SEVERE, null, ex );
		} catch( IllegalAccessException ex ) {
			java.util.logging.Logger.getLogger( StaticClientApp.class.getName() ).log( java.util.logging.Level.SEVERE, null, ex );
		} catch( javax.swing.UnsupportedLookAndFeelException ex ) {
			java.util.logging.Logger.getLogger( StaticClientApp.class.getName() ).log( java.util.logging.Level.SEVERE, null, ex );
		}
		//</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater( new Runnable() {
			@Override
			public void run() {
				new StaticClientApp().setVisible( true );
			}
		} );
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField altitudeField;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField latitudeField;
    private javax.swing.JTextField loginField;
    private javax.swing.JTextField longitudeField;
    private javax.swing.JTextArea messageArea;
    private javax.swing.JTextField passwordField;
    private javax.swing.JButton submitButton;
    // End of variables declaration//GEN-END:variables
}
