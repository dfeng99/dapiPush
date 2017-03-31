/**
 *  Dapi Push Notification Provider for APNS and FCM
 *  License: MIT
 *  Copyrights: 2017 
 */
package com.bzcentre.dapiPush;

import java.util.Map;

import nginx.clojure.NginxClojureRT;
import nginx.clojure.java.NginxJavaRingHandler;
import static nginx.clojure.MiniConstants.*;

/**
 * @author davidfeng
 *
 */
public class JVMExitHandler implements NginxJavaRingHandler{
	private final ApnsProxy apnsProxy= new ApnsProxy();
	
public JVMExitHandler() {

 }
	
	@Override
    public Object[] invoke(Map<String, Object> ctx) {
		  if(apnsProxy.getReadyStatus() == true){
			  apnsProxy.disconnect();
			  NginxClojureRT.log.info("Nginx stop/reload disconnect with push servers");
		  } else {
			  NginxClojureRT.log.info("push service already disconnected before stop/reload!");
		  }
        return new Object[] {
        		NGX_HTTP_NO_CONTENT, null, null };
	}
}
