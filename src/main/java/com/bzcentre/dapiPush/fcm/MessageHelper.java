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

package com.bzcentre.dapiPush.fcm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper for the transformation of JSON messages to attribute maps and vice
 * versa in the XMPP Server
 */

public class MessageHelper {

	static Gson g = new GsonBuilder().disableHtmlEscaping().create();
	
	/**
	 * Creates a JSON from a FCM outgoing message attributes
	 */
	public static String createJsonOutMessage(FcmOutMessage outMessage) {
		return createJsonMessage(createAttributeMap(outMessage));
	}

	/**
	 * Creates a JSON encoded ACK message for a received upstream message
	 */
	public static String createJsonAck(String to, String messageId) {
			Map<String, Object> map = new HashMap<>();
			map.put("message_type", "ack");
			map.put("to", to);
			map.put("message_id", messageId);
		return createJsonMessage(map);
	}

	public static String createJsonMessage(Map<String, Object> jsonMap) {
		return g.toJson(jsonMap);
	}

	/**
	 * Creates a MAP from a FCM outgoing message attributes
	 */
	public static Map<String, Object> createAttributeMap(FcmOutMessage msg) {
		Map<String, Object> map = new HashMap<>();
		if (msg.getTo() != null) {
			map.put("to", msg.getTo());
		}
		if (msg.getMessageId() != null) {
			map.put("message_id", msg.getMessageId());
		}
		if (msg.getCondition() != null) {
			map.put("condition", msg.getCondition());
		}
		if (msg.getCollapseKey() != null) {
			map.put("collapse_key", msg.getCollapseKey());
		}
		if (msg.getPriority() != null) {
			map.put("priority", msg.getPriority());
		}
		if (msg.isContentAvailable() != null && msg.isContentAvailable()) {
			map.put("content_available", true);
		}
		if (msg.getTimeToLive() != null) {
			map.put("time_to_live", msg.getTimeToLive());
		} else {
			map.put("time_to_live", 600); // default
		}
		if (msg.isDeliveryReceiptRequested() != null && msg.isDeliveryReceiptRequested()) {
			map.put("delivery_receipt_requested", true);
		}
		if (msg.isDryRun() != null && msg.isDryRun()) {
			map.put("dry_run", true);
		}
		if (msg.getNotificationPayload() != null) {
			map.put("notification", msg.getNotificationPayload());
		}
		
		map.put("data", msg.getDataPayload());
		return map;
	}

	/**
	 * Creates an incoming message according the bean
	 */
	@SuppressWarnings("unchecked")
	public static FcmInMessage createFcmInMessage(Map<String, Object> jsonMap) {
		String from = jsonMap.get("from").toString();
		// Package name of the application that sent this message
		String category = jsonMap.get("category").toString();
		// Unique id of this message
		String messageId = jsonMap.get("message_id").toString();
		Map<String, Object> dataPayload = (Map<String, Object>) jsonMap.get("data");
		return new FcmInMessage(from, category, messageId, dataPayload);
	}

}
