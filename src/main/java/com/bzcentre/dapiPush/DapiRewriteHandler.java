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
