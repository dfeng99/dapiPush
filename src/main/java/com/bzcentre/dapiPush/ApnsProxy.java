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

import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.auth.ApnsSigningKey;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import io.netty.util.concurrent.Future;
import nginx.clojure.NginxClojureRT;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.bzcentre.dapiPush.dapiSecrets.*;

public final class ApnsProxy {  
	
	private  ApnsClient apnsClient;
	final static String apnsHost = ApnsClientBuilder.DEVELOPMENT_APNS_HOST;
//	final static String apnsHost = ApnsClientBuilder.PRODUCTION_APNS_HOST;
	final static int defaultPort = ApnsClientBuilder.DEFAULT_APNS_PORT;
	final static int alterPort = ApnsClientBuilder.ALTERNATE_APNS_PORT;
	final static long defaultPingIdleTime = ApnsClientBuilder.DEFAULT_PING_IDLE_TIME_MILLIS;
	final static String TAG="[ApnsProxy][Worker:"+NginxClojureRT.processId+"]";
	Boolean proxyReady= false;
	private static ApnsProxy sInstance = null;
	private static long clientID = new Timestamp(System.currentTimeMillis()).getTime();
	
	public static ApnsProxy getInstance() {
		if (sInstance == null) {
			throw new IllegalStateException("You have to prepare the apns proxy first");
		}
		return sInstance;
	}
	
	public static ApnsProxy prepareClient() {
		synchronized (ApnsProxy.class) {
			if (sInstance == null) {
				sInstance = new ApnsProxy();
			}
		}
		return sInstance;
	}
	
	private ApnsProxy(){
//		final String developmentServer = "api.development.push.apple.com:443";
		//	private final String productionServer = "api.push.apple.com:443";
		NginxClojureRT.log.debug(TAG+"Starting APNs proxy ... ... ...");
		try {
			apnsClient = new ApnsClientBuilder().setApnsServer(apnsHost).
					setIdlePingInterval(defaultPingIdleTime, TimeUnit.MILLISECONDS).
					setSigningKey(
							ApnsSigningKey.loadFromPkcs8File(
									new File(APNs_AuthKey), 
									myTeamID, 
									myKeyID)
					).build(); // Token based authentication
			
				proxyReady = true;
				NginxClojureRT.log.info(TAG+"APNs proxy initiated ---"+clientID);
		} catch (IOException e) {
 			NginxClojureRT.log.info(TAG+"APNs blacklist: IOException error=");
			e.printStackTrace();
		} catch (InvalidKeyException e) {
 			NginxClojureRT.log.info(TAG+"APNs blacklist: InvalidKeyException error=");
 			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
 			NginxClojureRT.log.info(TAG+"APNs blacklist: NoSuchAlgorithmException error=");
 			e.printStackTrace();
		} 
	}
	
	public void disconnect(){
		final Future<Void> closeFuture = apnsClient.close();
		try {
			closeFuture.await().addListener((Future<Void> connectFuture)->{ //lambda expression
		         	NginxClojureRT.log.info(TAG+"Successfully disconnected from "+apnsHost+"-"+clientID);
				         	proxyReady = false;
				         	apnsClient = null;
				         	clientID=0;
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
			NginxClojureRT.log.debug(TAG+"Failed to disconnect from "+apnsHost);
		}
	}
	
	public Boolean getReadyStatus(){
		return proxyReady;
	}
	
	public Boolean isConnected(){
		return (apnsClient != null);
	}
	
	public PushNotificationResponse<SimpleApnsPushNotification> apnsPush(String myToken,String myPayload){
		NginxClojureRT.log.debug(TAG+"Pushing through APNs gateway.");
	    if(proxyReady){
	    	
	    	final SimpleApnsPushNotification pushNotification;
	    	{
	    	    pushNotification =  new SimpleApnsPushNotification(myToken, myBundleID, myPayload);
	    	}
	    	
	    	final Future<PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture =
	    																	apnsClient.sendNotification(pushNotification);
	    	
	    	NginxClojureRT.log.debug(TAG+"waitting for apns response");
	    	
	    	try {
				return sendNotificationFuture.get();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			NginxClojureRT.log.info(TAG+"APNs push failed!  provider  disconnected!");
		}
		return null;
	}
}
