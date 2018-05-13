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
package com.bzcentre.dapiPush;

import nginx.clojure.MessageAdapter;
import nginx.clojure.NginxClojureRT;
import nginx.clojure.NginxHttpServerChannel;
import nginx.clojure.java.NginxJavaRequest;
import nginx.clojure.java.NginxJavaRingHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

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
