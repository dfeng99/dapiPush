/**
 * @title			Dapi Push Notification Gateway
 * @version   		0.1
 * @copyright   		Copyright (C) 2017- David Feng, All rights reserved.
 * @license   		GNU General Public License version 3 or later.
 * @author url   	http://www.xflying.com
 * @developers   	David Feng
 */
package com.bzcentre.dapiPush;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import com.relayrides.pushy.apns.ApnsClient;
import com.relayrides.pushy.apns.ApnsClientBuilder;
import com.relayrides.pushy.apns.ClientNotConnectedException;
import com.relayrides.pushy.apns.PushNotificationResponse;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import nginx.clojure.NginxClojureRT;

public final class ApnsProxy {
	private static ApnsClient apnsClient;
	final static String APNs_AuthKey = "path/to/the/authkey";
	final static String myBundleID = "com.yourbundle.id";
	final static String myKeyID = "put KEYID here";
	final static String myTeamID = "put team ID here";
	Boolean proxyReady= false;
    
	public ApnsProxy(){
//		final String developmentServer = "api.development.push.apple.com:443";
		//	private final String productionServer = "api.push.apple.com:443";

		try {
			apnsClient = new ApnsClientBuilder().build(); // Token based authentication
			
//			final ApnsClient apnsClient = new ApnsClientBuilder() // TLS based authentication
//			        .setClientCredentials(new File("/path/to/certificate.p12"), "p12-file-password")
//			        .build();
			
			apnsClient.registerSigningKey(new File(APNs_AuthKey),
					myTeamID, myKeyID,myBundleID);
			
			connect();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void connect(){
		
		final Future<Void> connectFuture = apnsClient.connect(ApnsClient.DEVELOPMENT_APNS_HOST);
		
		try {
			connectFuture.await().addListener(new GenericFutureListener<Future<Void>>(){
				@Override
				   public void operationComplete(Future<Void> connectFuture) {
				         	proxyReady = true;
				         	NginxClojureRT.log.info("APNs successfully connected!");
				     }			
			});
		} catch (InterruptedException e) {
			NginxClojureRT.log.debug("APNs failed to connect!");
		}
	}
	
	public void disconnect(){
		final Future<Void> disconnectFuture = apnsClient.disconnect();
		try {
			disconnectFuture.await().addListener(new GenericFutureListener<Future<Void>>(){
				@Override
				   public void operationComplete(Future<Void> connectFuture) {
				         	proxyReady = true;
				         	NginxClojureRT.log.info("APNs successfully disconnected!");
				     }			
			});
		} catch (InterruptedException e) {
			NginxClojureRT.log.debug("Failed to disconnect");
		}
	}
	
	public Boolean getReadyStatus(){
		return proxyReady;
	}
	
	public Boolean apnsPush(String myToken,String myPayload){
	    if(proxyReady){
	    	final SimpleApnsPushNotification pushNotification;
	    	{
	    	    pushNotification =  new SimpleApnsPushNotification(myToken, myBundleID, myPayload);
	    	}
	    	
	    	final Future<PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture =
	    			apnsClient.sendNotification(pushNotification);
	    	
	    	try {
	    	    final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse =
	    	            sendNotificationFuture.get();

	    	    if (pushNotificationResponse.isAccepted()) {
	    	        System.out.println("Push notification accepted by APNs gateway.");
	    	    } else {
	    	        System.out.println("Notification rejected by the APNs gateway: " +
	    	                pushNotificationResponse.getRejectionReason());

	    	        if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
	    	            System.out.println("\t…and the token is invalid as of " +
	    	                pushNotificationResponse.getTokenInvalidationTimestamp());
	    	        }
	    	    }
	    	} catch (final ExecutionException e) {
	    		 NginxClojureRT.log.debug("Failed to send push notification.");
	    	    e.printStackTrace();

	    	    if (e.getCause() instanceof ClientNotConnectedException) {
	    	    	NginxClojureRT.log.debug("Waiting for client to reconnect…");
	    	        try {
						apnsClient.getReconnectionFuture().await();
					} catch (InterruptedException e1) {
						 NginxClojureRT.log.debug("Failed to reconnect.");
						 return false;
					}
	    	        NginxClojureRT.log.debug("Reconnected. Message maybe not being sent!");
	    	    }
	    	} catch (InterruptedException e) {
	    		NginxClojureRT.log.debug("apnsPush: InterruptedException");
	    		return false;
			}
	    	return true;
		} else {
			NginxClojureRT.log.debug("APNs push failed!  provider  disconnected!");
		}
		return false;
	}
	
	public Boolean fcmPush(String myToken, String myPayload){
		return false;
	}
}
