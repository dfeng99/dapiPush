/**
 * @title			Dapi Push Notification Gateway
 * @version   		0.1
 * @copyright   		Copyright (C) 2017- David Feng, All rights reserved.
 * @license   		GNU General Public License version 3 or later.
 * @author url   	http://www.xflying.com
 * @developers   	David Feng
 */
package com.bzcentre.dapiPush;

import nginx.clojure.java.NginxJavaRingHandler;
import static nginx.clojure.java.Constants.*;

import java.io.IOException;
import java.util.Map;

public class DapiAccessHandler implements NginxJavaRingHandler {

	public Object[] invoke(Map<String, Object> request) throws IOException{
		if (GET != request.get(REQUEST_METHOD) 
		        || !"websocket".equals(((Map<String,Object>) request.get(HEADERS)).get("upgrade"))) {
		      return new Object[]{404, null, null};
		    }
	    return PHASE_DONE;
	  }
}
