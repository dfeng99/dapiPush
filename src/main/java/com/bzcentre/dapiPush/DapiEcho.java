/**
 * @title			Dapi Push Notification Gateway
 * @version   		0.1
 * @copyright   		Copyright (C) 2017- David Feng, All rights reserved.
 * @license   		GNU General Public License version 3 or later.
 * @author url   	http://www.xflying.com
 * @developers   	David Feng
 */
package com.bzcentre.dapiPush;

/**
 * @author davidfeng
 *
 */
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import nginx.clojure.MessageAdapter;
import nginx.clojure.NginxClojureRT;
import nginx.clojure.NginxHttpServerChannel;
import nginx.clojure.java.NginxJavaRequest;
import nginx.clojure.java.NginxJavaRingHandler;

public class DapiEcho implements NginxJavaRingHandler {
	/**
	 * 
	 */
	public DapiEcho() {
		// TODO Auto-generated constructor stub
	}

	    @Override
	    public Object[] invoke(Map<String, Object> request) {
	        NginxJavaRequest r = (NginxJavaRequest)request;
	        NginxHttpServerChannel sc = r.hijack(true);
	        sc.addListener(sc, new MessageAdapter<NginxHttpServerChannel>() {
	            int total = 0;
	            @Override
	            public void onOpen(NginxHttpServerChannel data) {
	                NginxClojureRT.log.debug("WSEcho onOpen!");
	            }

	            @Override
	            public void onTextMessage(NginxHttpServerChannel sc, String message, boolean remaining) throws IOException {
	                if (NginxClojureRT.log.isDebugEnabled()) {
	                    NginxClojureRT.log.debug("WSEcho onTextMessage: msg=%s, rem=%s", message, remaining);
	                }
	                total += message.length();
	                sc.send(message, !remaining, false);
	            }

	            @Override
	            public void onBinaryMessage(NginxHttpServerChannel sc, ByteBuffer message, boolean remining) throws IOException {
	                if (NginxClojureRT.log.isDebugEnabled()) {
	                    NginxClojureRT.log.debug("WSEcho onBinaryMessage: msg=%s, rem=%s, total=%d", message, remining, total);
	                }
	                total += message.remaining();
	                sc.send(message, !remining, false);
	            }

	            @Override
	            public void onClose(NginxHttpServerChannel req, long status, String reason) {
	                if (NginxClojureRT.log.isDebugEnabled()) {
	                  NginxClojureRT.log.info("WSEcho onClose2: total=%d, status=%d, reason=%s", total, status, reason);
	                }
	            }

	            @Override
	            public void onError(NginxHttpServerChannel data, long status) {
	                if (NginxClojureRT.log.isDebugEnabled()) {
	                      NginxClojureRT.log.info("WSEcho onError: total=%d, status=%d", total, status);
	                    }
	            }

	        });
	        return null;
	    }
}
