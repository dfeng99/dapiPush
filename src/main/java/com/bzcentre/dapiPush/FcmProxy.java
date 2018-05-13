/*
 * Copyright (c) 2018. David Feng
 * Package			Dapi Push Notification APNS/FCM Gateway
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author   David Feng
 * @version 1.0
 */
/*
 *  https://developer.apple.com/library/content/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/CommunicatingwithAPNs.html#//apple_ref/doc/uid/TP40008194-CH11-SW1
 *
 */
package com.bzcentre.dapiPush;

import com.bzcentre.dapiPush.fcm.*;
import com.bzcentre.dapiPush.fcm.processors.MessageProcessor;
import com.bzcentre.dapiPush.fcm.processors.PayloadProcessor;
import com.bzcentre.dapiPush.fcm.processors.ProcessorFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import nginx.clojure.NginxClojureRT;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Mechanisms;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.sm.predicates.ForEveryStanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.gcm.packet.GcmPacketExtension;
import org.jivesoftware.smackx.ping.PingManager;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.bzcentre.dapiPush.dapiSecrets.*;

public final class FcmProxy implements X509TrustManager, StanzaListener {
	
	private static FcmProxy sInstance = null;
	private final static String TAG="[FcmProxy][Worker:"+NginxClojureRT.processId+"]";
	private FCMXMPPConnection fcmClient;
	private X509TrustManager pkixTrustManager; 
	Boolean proxyReady= false;
	final GsonBuilder gsonBuilder = new GsonBuilder();
	private Boolean isConnectionDraining = false;
	private final Map<String, String> pendingMessages = new ConcurrentHashMap<>();
	public static FcmProxy getInstance() {
		if (sInstance == null) {
			throw new IllegalStateException("You have to prepare the client first");
		}
		return sInstance;
	}
	
	public static FcmProxy prepareClient() throws CertificateException {
		synchronized (FcmProxy.class) {
			if (sInstance == null) {
				sInstance = new FcmProxy();
			}
		}
		return sInstance;
	}
	
	private FcmProxy() throws CertificateException{
		FcmSettings fcmSettings;
		String sasl_auth;
		// Create the configuration for this new connection
		//https://firebase.google.com/docs/cloud-messaging/auth-server
		SmackConfiguration.addDisabledSmackClass("org.jivesoftware.smackx.httpfileupload.HttpFileUploadManager");

		try {
			fcmSettings = new FcmSettings();
			sasl_auth = fcmSettings.getProject_info().getProject_number()+ "@" + FcmSettings.FCM_SERVER_CONNECTION;
			NginxClojureRT.log.info(TAG+"sasl_auth: "+sasl_auth);

			final KeyStore keyStore = KeyStore.getInstance("JKS");
			try (//The try-with-resources Statement
					final InputStream is = new FileInputStream(JAVA_TRUSTSTORE)
				) 	{
							keyStore.load(is, JKS_PASSWORD);
							NginxClojureRT.log.info(TAG+"Loading keystore ..."
							);
					} catch (CertificateException e1) {
						NginxClojureRT.log.info(TAG+"Failed to load keystore ...");
					}
		    
            KeyManager[] kms = null;

            String keyManagerFactoryAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
            KeyManagerFactory kmf = null;
            try {
                kmf = KeyManagerFactory.getInstance(keyManagerFactoryAlgorithm);
            }
            catch (NoSuchAlgorithmException e) {
                NginxClojureRT.log.info(TAG+"Could get the default KeyManagerFactory for the '"
                                + keyManagerFactoryAlgorithm + "' algorithm", e);
            }
            if (kmf != null) {
                try {
                   kmf.init(keyStore, JKS_PASSWORD);
                    kms = kmf.getKeyManagers();
                }
                catch (NullPointerException npe) {
                    NginxClojureRT.log.info(TAG+"NullPointerException", npe);
                } catch (UnrecoverableKeyException e1) {
                     NginxClojureRT.log.info(TAG+"UnrecoverableKeyException", e1.getMessage());
                    e1.printStackTrace();
                }
            }

		    NginxClojureRT.log.info(TAG+"Initiating Trust Manager  ...");
		    final TrustManagerFactory tmf = TrustManagerFactory.getInstance(
		    																TrustManagerFactory.getDefaultAlgorithm()
		    															);
			tmf.init(keyStore);
			TrustManager[] tmA = tmf.getTrustManagers();
			pkixTrustManager = (X509TrustManager) tmA[0];

			for(int i =0; i < tmA.length; i++) {
				if( tmA[i] instanceof X509TrustManager){
					// If found, use that as our "default" trust manager
					break;
				}
				pkixTrustManager = (X509TrustManager) tmf.getTrustManagers()[i+1];
			}
			
			if(pkixTrustManager == null) 
				throw new CertificateException();
			
			XMPPTCPConnection.setUseStreamManagementResumptionDefault(true);
			XMPPTCPConnection.setUseStreamManagementDefault(true);
			
			TrustManager[] tms = new TrustManager[] {pkixTrustManager};
			
            final SecureRandom secureRandom = new SecureRandom();
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(kms, tms, secureRandom);
			NginxClojureRT.log.info(TAG+"Initiating SSL Context ..."+sslContext.getProtocol());
						
			String[] enabledSSLProtocols = {sslContext.getProtocol()};
			XMPPTCPConnectionConfiguration.Builder config = 
					XMPPTCPConnectionConfiguration.builder()
					.setXmppDomain(FcmSettings.FCM_SERVER_CONNECTION)
					.setHost(FcmSettings.FCM_SERVER)
					.setPort(FcmSettings.FCM_PORT_PRODUCTION)
//					.setPort(FcmSettings.FCM_PORT_DEVELOPMENT)
					.setSecurityMode(SecurityMode.ifpossible)
					.setSendPresence(false)
					.setEnabledSSLProtocols(enabledSSLProtocols)
					.setCustomSSLContext(sslContext)
					.setCustomX509TrustManager(pkixTrustManager)
					.setSocketFactory(sslContext.getSocketFactory())
					.setUsernameAndPassword(sasl_auth, FCM_AuthKey);
					
					// Create the connection
					fcmClient = new FCMXMPPConnection(config.build());
					
					fcmClient.addConnectionListener(new ConnectionListener(){
						@Override
						public void reconnectionSuccessful() {
							NginxClojureRT.log.info(TAG+"Reconnection successful ...");
							// TODO: handle the reconnecting successful
						}
			
						@Override
						public void reconnectionFailed(Exception e) {
							NginxClojureRT.log.info(TAG+"Reconnection failed: ", e.getMessage());
							// TODO: handle the reconnection failed
						}
			
						@Override
						public void reconnectingIn(int seconds) {
							NginxClojureRT.log.info(TAG+"Reconnecting in %d secs", seconds);
							// TODO: handle the reconnecting in
						}
			
						@Override
						public void connectionClosedOnError(Exception e) {
							NginxClojureRT.log.info(TAG+"Connection closed on error =>"+e.getMessage());
						}
			
						@Override
						public void connectionClosed() {
							NginxClojureRT.log.info(TAG+ "Connection closed. The current connectionDraining flag is: " + isConnectionDraining);
							if (isConnectionDraining) {
								reconnect();
							}
						}
			
						@Override
						public void authenticated(XMPPConnection arg0, boolean arg1) {
							
							NginxClojureRT.log.info(TAG+"authenticated:"+(arg0.isAuthenticated() ?"true":"false"));
							NginxClojureRT.log.info(TAG+fcmClient.getHost()+(fcmClient.isConnected() ? " is Connected! ": " is Not Connected!")
									+"\nxmpp domain:"+fcmClient.getXMPPServiceDomain().toString()
									+"\n SASL:"+fcmClient.getUsedSaslMechansism()
									+"\n isUsingCompression:"+(fcmClient.isUsingCompression() ? "true": "false")
									+"\n Is Secure ? "+(fcmClient.isSecureConnection() ? "true": "false"));
							proxyReady = true;
						}
			
						@Override
						public void connected(XMPPConnection arg0) {
							NginxClojureRT.log.info(TAG+"FCM connected");
						       // Configuring Automatic reconnection
						        ReconnectionManager manager;
								// Disable Roster at login
								Roster.getInstanceFor(fcmClient).setRosterLoadedAtLogin(false);
								manager = ReconnectionManager.getInstanceFor(fcmClient);
						        manager.setReconnectionPolicy(ReconnectionManager.ReconnectionPolicy.RANDOM_INCREASING_DELAY);
						        manager.enableAutomaticReconnection();

								// Log all outgoing packets
						      
						        fcmClient.addPacketInterceptor(stanza -> {
								        	try{
								        		IQ iq = (IQ) stanza;
								        		if(iq != null && iq.getChildElementName().equals("ping")){
								        			return;
								        		}
								        		NginxClojureRT.log.info(TAG+"Sent control stanza: "+ stanza.toXML());
								        	} catch (ClassCastException ce){
								        		if(!stanza.getClass().getName().equals("org.jivesoftware.smack.packet.Message"))
								        			NginxClojureRT.log.info(TAG+"Sent stanza type: "+ stanza.toXML());
								        		else {
								        			NginxClojureRT.log.info(TAG+"Sent message stanza!");
								        		}
								        		// ClassCastException will be thrown if a message type stanza received.
								        	}
						        		}
						        		,ForEveryStanza.INSTANCE
						        		);
						        
						        NginxClojureRT.log.info(TAG+"Set the ping interval 100");
								final PingManager pingManager = PingManager.getInstanceFor(fcmClient);
								pingManager.setPingInterval(100);
								pingManager.registerPingFailedListener(() -> {
									
									NginxClojureRT.log.info(TAG+"The ping failed, restarting the ping interval again ...");
									pingManager.setPingInterval(100);
								});
								
								Mechanisms mechanisms = fcmClient.getFeature(Mechanisms.ELEMENT, Mechanisms.NAMESPACE);
								if(mechanisms != null){
									NginxClojureRT.log.info(mechanisms.toXML());
								} else {
									NginxClojureRT.log.info("===Not Found===:secure?"+fcmClient.isSecureConnection());
								}
								try {	
										fcmClient.login();
								} catch (XMPPException | SmackException | IOException | InterruptedException e) {
									NginxClojureRT.log.info(TAG+"Login Failed! ---"+e.getMessage());
//									e.printStackTrace();
								}
						}
					});
					
					NginxClojureRT.log.info(TAG+"Register a Sync Stanza listener then call connect ...");
					// Handle incoming packets and reject messages that are not from FCM CCS
//					fcmClient.addAyyncStanzaListener(
//					fcmClient.addSyncStanzaListener(							
//							this, 
//							stanza -> stanza.hasExtension(FcmSettings.FCM_ELEMENT_NAME, 
//																					FcmSettings.FCM_NAMESPACE)
//							);
					
					if (fcmClient.connect() != null){
						// Check SASL authentication
						NginxClojureRT.log.info( "SASL PLAIN authentication enabled? " 
															+ SASLAuthentication.isSaslMechanismRegistered("PLAIN"));
					}
		} catch (ParseException e) {
			NginxClojureRT.log.info(TAG+" FCM: ParseException error="+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
 			NginxClojureRT.log.info(TAG+" FCM : IOException error="+e.getMessage());
			e.printStackTrace();
		} catch (XMPPException e) {
 			NginxClojureRT.log.info(TAG+" FCM : XMPPException error="+e.getMessage());
			e.printStackTrace();
		} catch (SmackException e) {
 			NginxClojureRT.log.info(TAG+" FCM : SmackException, Type= "
 															+e.getClass().getSimpleName()
 															+" error="+e.getMessage());
		} catch (InterruptedException e) {
 			NginxClojureRT.log.info(TAG+" FCM : InterruptedException error="+e.getMessage());
 			e.printStackTrace();
		} catch (NullPointerException e){
			NginxClojureRT.log.info(TAG+e.getMessage()+" is Null");
			e.printStackTrace();
		} catch (NoSuchAlgorithmException |KeyStoreException|KeyManagementException e1) {
			e1.printStackTrace();
		}
	}
	
	public synchronized void reconnect() {
		// Try to connect again using exponential back-off!
		final BackOffStrategy backoff = new BackOffStrategy(5, 1000);
		while (backoff.shouldRetry()) {
			try {
				// TODO: use exponential back-off!
				sInstance = new FcmProxy();
				resendPendingMessages();
				backoff.doNotRetry();
			} catch (CertificateException e) {
				NginxClojureRT.log.info(TAG+"The notifier server could not reconnect after the connection draining message.");
				backoff.errorOccured();
			}
		}
	}
	
	private void resendPendingMessages() {
		NginxClojureRT.log.info(TAG+"Sending pending messages through the new connection.");
		NginxClojureRT.log.info(TAG+"Pending messages size: " + pendingMessages.size());
		final Map<String, String> messagesToResend = new HashMap<>(pendingMessages);
		for (Map.Entry<String, String> message : messagesToResend.entrySet()) {
	        fcmPush(message.getKey(), message.getValue());
	    }
	}
	
//	private void pushBlackList(String token, String reason, Date timestamp) {
//		String query="";
//		int rows=0;
//		
//		try {
//			dbconn = connectMySql();
//			Statement stmt = dbconn.createStatement();
//			switch(reason){
//				case "DEVICE_UNREGISTERED": // has been inserted by DapiReceiver before push it, now we update the state
//					query = "UPDATE `notification_push_blacklist` SET `state`='inactive', `timestamp`=STR_TO_DATE('"+timestamp+"', '%a %b %e %H:%i:%s CST %Y') "+
//									"WHERE  `to_token`=\""+token+"\"";
//					break;
//				default:
//					query = "DELETE FROM  `notification_push_blacklist` WHERE `to_token`=\""+token+"\" ;";
//					break;
//			}
//			rows = stmt.executeUpdate(query);
//			if(rows == 0){
//				NginxClojureRT.log.info(TAG+" FCM blacklist: no row is affected, sql =\n"+query);
//			}
//		} catch (ClassNotFoundException e) {
//			NginxClojureRT.log.info(TAG+" FCM blacklist: ClassNotFoundException error="+query);
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			NginxClojureRT.log.info(TAG+" FCM blacklist: InstantiationException error="+query);
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			NginxClojureRT.log.info(TAG+" FCM blacklist: IllegalAccessException error="+query);
//			e.printStackTrace();
//		} catch (SQLException e) {
//			NginxClojureRT.log.info(TAG+" FCM blacklist: SQLException error="+query);
//			e.printStackTrace();
//		}
//	}
	
	public void disconnect(){
			NginxClojureRT.log.info(TAG+"Disconnect FCM proxy:" + fcmClient.getStreamId());
			fcmClient.disconnect();
			proxyReady = false;
	}
	
	public Boolean isConnected(){
		return fcmClient.isConnected();
	}
	
	public Boolean getReadyStatus(){
		return proxyReady;
	}
	
//	private  static Connection connectMySql() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException{
//		 // Load the JDBC driver
//	    String driver = "com.mysql.jdbc.Driver";
//	    //String driver = "org.gjt.mm.mysql.Driver";
//	    Class.forName(driver).newInstance();
//	 
//	    // Create a connection to the database
//	    String url = "jdbc:mysql://localhost/DapiPush?autoReconnect=true&failOverReadOnly=false&useSSL=false&requireSSL=false";
//	    String username = "dapiAir";
//	    String password = "jjlaksi*uJle988.";
//	    return DriverManager.getConnection(url, username, password);
//	}
	
	/**
	 * Sends a downstream message to FCM
	 */
	public void fcmPush(String jsonRequest) {
		// TODO: Resend the message using exponential back-off!
		final Stanza request = new FcmPacketExtension(jsonRequest).toPacket();
		final BackOffStrategy backoff = new BackOffStrategy();
		
		while (backoff.shouldRetry()) {
			try {
				fcmClient.sendStanza(request);
				backoff.doNotRetry();
			} catch (NotConnectedException | InterruptedException e) {
				NginxClojureRT.log.info( TAG+"The packet could not be sent due to a connection problem. Packet: {}", request.toXML());
				backoff.errorOccured();
			}
		}
	}
	
	public Boolean fcmPush(String fcmToken,String jsonPayload){
		StanzaCollector stzCollector = null;
		Stanza responsedStanza = null;
		isConnectionDraining = false;
		gsonBuilder.registerTypeAdapter(new TypeToken<MeetingPayload>(){}.getType(), new MeetingPayload());
		Gson g = gsonBuilder.disableHtmlEscaping().serializeNulls().create();
    	pendingMessages.put(fcmToken, jsonPayload);
		MeetingPayload meetingPayload = g.fromJson(jsonPayload, new TypeToken<MeetingPayload>(){}.getType());
    	if (!isConnectionDraining) {
			final BackOffStrategy backoff = new BackOffStrategy();
			while (backoff.shouldRetry()) {
				if(proxyReady){
			    	try{
							String messageId = FcmSettings.getUniqueMessageId();
							FcmOutMessage outMessage = new FcmOutMessage(fcmToken, messageId, meetingPayload.dataPayload());
							outMessage.setNotificationPayload(meetingPayload.notificationPayload());
							String jsonRequest = MessageHelper.createJsonOutMessage(outMessage);
							Stanza request = new FcmPacketExtension(jsonRequest).toPacket();
	
							NginxClojureRT.log.info(TAG+"Pushing FCM notification ...");
							
							try {
								stzCollector = fcmClient.createStanzaCollectorAndSend(
										stanza -> stanza.hasExtension(FcmSettings.FCM_ELEMENT_NAME, FcmSettings.FCM_NAMESPACE),
										request);
								responsedStanza = stzCollector.nextResultOrThrow();
								//fcmClient.sendStanza(request);
								backoff.doNotRetry();
							} catch (XMPPErrorException | NoResponseException e) {
								NginxClojureRT.log.info(TAG+"Stanza collector error.");
								e.printStackTrace();
								return false;
							} finally {
								if(stzCollector != null)
									stzCollector.cancel();
							}
							
							NginxClojureRT.log.debug(TAG+"Is FCM connected?"+ (isConnected() ? "YES" : "NO"));
						} catch (NotConnectedException | InterruptedException e) {
							backoff.errorOccured();
							NginxClojureRT.log.info(TAG+e.getMessage());
						} catch (JsonSyntaxException e) {
							NginxClojureRT.log.info(TAG+"Check Json syntax, please!");
							e.printStackTrace();
						}
	
			    } else {
					backoff.errorOccured();
			    	NginxClojureRT.log.info(TAG+"FCM proxy not ready");
			    }
			}
    	} else {
    		NginxClojureRT.log.info(TAG+"FCM proxy connection is draining...");
    	}
    	
	    NginxClojureRT.log.info(TAG+ (responsedStanza != null ? "Push OK, call stanzaHandler for result stanza analizing." : "Push Failed") );
		return stanzaHandler(responsedStanza);
	}


	@SuppressWarnings("unchecked")
	public Boolean stanzaHandler(Stanza packet) {

		if(packet == null) {
			NginxClojureRT.log.info("null stanza received");
			return false;
		}
		
		NginxClojureRT.log.info(TAG+ "incoming messages Received: " + packet.toXML());
		GcmPacketExtension gcmPktExt =  GcmPacketExtension.from(packet);
//		FcmPacketExtension fcmPacket = (FcmPacketExtension) packet.getExtension(FcmSettings.FCM_NAMESPACE);
		//Downstream message XMPP response body.
		String json = gcmPktExt.getJson();
		try {
			Map<String, Object> jsonMap = (Map<String, Object>) JSONValue.parseWithException(json);
			Object messageType = jsonMap.get("message_type");

			if (messageType == null) {
				FcmInMessage inMessage = MessageHelper.createFcmInMessage(jsonMap);
				handleUpstreamMessage(inMessage); // normal upstream message
				return false;
			}

			switch (messageType.toString()) {
				case "ack":
					handleAckReceipt(jsonMap);
					break;
				case "nack":				
					handleNackReceipt(jsonMap);
					return false;
			case "receipt":
					handleDeliveryReceipt(jsonMap);
					break;
				case "control":
					handleControlMessage(jsonMap);
					break;
				default:
					NginxClojureRT.log.info(TAG+ "Received unknown FCM message type: " + messageType.toString());
			}
			
		} catch (ParseException e) {
			NginxClojureRT.log.info( "Error parsing JSON: " + json, e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * Handles an upstream message from a device client through FCM
	 */
	public void handleUpstreamMessage(FcmInMessage inMessage) {
		final String action = (String) inMessage.getDataPayload()
										.get(FcmSettings.PAYLOAD_ATTRIBUTE_ACTION);
		if (action != null) {
			PayloadProcessor processor = ProcessorFactory.getProcessor(action);
			processor.handleMessage(inMessage);
		}

		// Send ACK to FCM
		String ack = MessageHelper.createJsonAck(inMessage.getFrom(), inMessage.getMessageId());
		fcmPush(ack);
	}
	/**
	 * Handles an ACK message from FCM
	 */
	public void handleAckReceipt(Map<String, Object> jsonMap) {
		String sendFromToken = jsonMap.get("from").toString();
		NginxClojureRT.log.debug(TAG+"Delivered to FCM : "+sendFromToken);
		removeMessageFromPendingMessages(jsonMap);
		MessageProcessor.pushBlackList(sendFromToken, "whiteList", null);
	}

	/**
	 * Handles a NACK message from FCM
	 */
	public void handleNackReceipt(Map<String, Object> jsonMap) {
		removeMessageFromPendingMessages(jsonMap);
		String errorCode = (String) jsonMap.get("error");
		String sendFromToken = jsonMap.get("from").toString();
		Date timestamp = new Date();
		
		if (errorCode == null) {
			errorCode = "whiteList";
			MessageProcessor.pushBlackList(sendFromToken, errorCode, timestamp);
			NginxClojureRT.log.info(TAG+ "Received null NACK FCM Error Code");
			return;
		}		
		String errorDescription = jsonMap.get("error_description").toString();
		NginxClojureRT.log.info( TAG+"Received FCM NACK Error Code:"+errorCode+" description: "+sendFromToken +" "+errorDescription);
		
		switch (errorCode) {
			case "CONNECTION_DRAINING":
				handleConnectionDrainingFailure();
				break;
			case "SERVICE_UNAVAILABLE":
			case "INTERNAL_SERVER_ERROR":
			case "BAD_ACK":				
			case "DEVICE_MESSAGE_RATE_EXCEEDED":
			case "TOPICS_MESSAGE_RATE_EXCEEDED":
			case "INVALID_JSON":
			case "BAD_REGISTRATION":				
				handleServerFailure(jsonMap);
				break;
			case "DEVICE_UNREGISTERED":
				handleUnrecoverableFailure(jsonMap);
				break;
			default:
				NginxClojureRT.log.info(TAG+ "Received unknown FCM Error Code: " + errorCode);
		}
	}
	
	/**
	 * Remove the message from the pending messages list
	 */
	private void removeMessageFromPendingMessages(Map<String, Object> jsonMap) {
		// Get the message_id attribute
		final String messageId = (String) jsonMap.get("message_id");
		if (messageId != null) {
			// Remove the messageId from the pending messages list
			pendingMessages.remove(messageId);
		}
	}
	
	/**
	 * Handles a Delivery Receipt message from FCM (when a device confirms that it received a particular message)
	 */
	public void handleDeliveryReceipt(Map<String, Object> jsonMap) {
		NginxClojureRT.log.info(TAG+" TODO: handle the delivery receipt");
	}

	/**
	 * Handles a Control message from FCM
	 */
	public void handleControlMessage(Map<String, Object> jsonMap) {
		// TODO: handle the control message
		String controlType = (String) jsonMap.get("control_type");

		if (controlType.equals("CONNECTION_DRAINING")) {
			handleConnectionDrainingFailure();
		} else {
			NginxClojureRT.log.info( TAG+"Received unknown FCM Control message: " + controlType);
		}
	}

	private void handleServerFailure(Map<String, Object> jsonMap) {
		// TODO: Resend the message
		NginxClojureRT.log.info(TAG+ "TODO: handleServerFailure");

	}

	private void handleUnrecoverableFailure(Map<String, Object> jsonMap) {
		String errorCode = (String) jsonMap.get("error");
		String sendFromToken = jsonMap.get("from").toString();
		Date timestamp = new Date();
		MessageProcessor.pushBlackList(sendFromToken, errorCode, timestamp);
	}

	private void handleConnectionDrainingFailure() {
		// TODO: handle the connection draining failure. Force reconnect?
		isConnectionDraining = true;
		NginxClojureRT.log.info(TAG+ "FCM Connection is draining! Initiating reconnection ...");
	}


	/**
	 * Sends a message to multiple recipients (list). Kind of like the old HTTP message with the list of regIds in the
	 * "registration_ids" field.
	 */
	public void sendBroadcast(FcmOutMessage outMessage, List<String> recipients) {
		NginxClojureRT.log.info(TAG+"Senging broadcast...");
		Map<String, Object> map = MessageHelper.createAttributeMap(outMessage);
		for (String toRegId : recipients) {
			String messageId = FcmSettings.getUniqueMessageId();
			map.put("message_id", messageId);
			map.put("to", toRegId);
			String jsonRequest = MessageHelper.createJsonMessage(map);
			fcmPush(jsonRequest);
		}
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) {
		NginxClojureRT.log.info(TAG+"X509 TM check Client Trusted");
		
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) {
		NginxClojureRT.log.info(TAG+"X509 TM checking Server Trusted");
		try {
		      pkixTrustManager.checkServerTrusted(chain, authType);
		     } catch (CertificateException excep) {
		    	 NginxClojureRT.log.info(TAG+"X509 TM checking Server CertificateException"+excep.getMessage());
		      /*
		       * Possibly pop up a dialog box asking whether to trust the
		       * cert chain.
		       */
		     }		
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {

		return pkixTrustManager.getAcceptedIssuers();
	}

	@Override
	public void processStanza(Stanza packet) {
		NginxClojureRT.log.info(TAG+"Incoming stanza received.");
		this.stanzaHandler(packet);
		
	}
}
