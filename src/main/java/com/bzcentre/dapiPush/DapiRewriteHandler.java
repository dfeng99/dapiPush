/**
 * @title			Dapi Push Notification Gateway
 * @version   		0.1
 * @copyright   		Copyright (C) 2017- David Feng, All rights reserved.
 * @license   		GNU General Public License version 3 or later.
 * @author url   	http://www.xflying.com
 * @developers   	David Feng
 */
package com.bzcentre.dapiPush;

import static nginx.clojure.java.Constants.*;

import java.util.Map;

import nginx.clojure.java.NginxJavaRingHandler;

public class DapiRewriteHandler  implements NginxJavaRingHandler {
	@Override
    public Object[] invoke(Map<String, Object> req) {

        return PHASE_DONE;
    }
}
