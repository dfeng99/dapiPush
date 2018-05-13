/**
 *  Dapi Push Notification Provider for APNS and FCM
 *  License: MIT
 *  Copyrights: 2017 
 */
package com.bzcentre.dapiPush;

import nginx.clojure.AppEventListenerManager.*;
import nginx.clojure.NginxClojureRT;
import nginx.clojure.java.NginxJavaRingHandler;

import java.util.Map;

import static nginx.clojure.MiniConstants.*;
import static nginx.clojure.MiniConstants.NGX_HTTP_NO_CONTENT;

/**
 * @author davidfeng
 *
 */
public class JVMExitHandler implements NginxJavaRingHandler{
	
public JVMExitHandler() {
	NginxClojureRT.log.info("Shutting down DapiReceiver... ...");
 }
	
	@Override
    public Object[] invoke(Map<String, Object> ctx) {
		NginxClojureRT.log.info("Publishing shutdown event...");
//		NginxClojureRT.broadcastEvent(DapiReceiver.SEVER_SENT_EVENTS, DapiReceiver.DISCONNECT);
        PostedEvent event = new PostedEvent(DapiReceiver.SERVER_SENT_EVENTS, DapiReceiver.DISCONNECT);
        NginxClojureRT.getAppEventListenerManager().broadcast(event);

		if(ctx.size() > 0)
				NginxClojureRT.log.info("JVM Exit context : \n[Keys]"+ctx.keySet().toString()+" \n[values:]"+ctx.values().toString());

        return new Object[] {
        		NGX_HTTP_NO_CONTENT, null, null };
	}
}
