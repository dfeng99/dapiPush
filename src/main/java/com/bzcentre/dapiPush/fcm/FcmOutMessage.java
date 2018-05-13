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

import java.util.Map;

public class FcmOutMessage {
	// Sender registration ID
	private String to;
	// Condition that determines the message target
	private String condition;
	// Unique id for this message
	private String messageId;
	// Identifies a group of messages
	private String collapseKey;
	// Priority of the message
	private String priority;
	// Flag to wake client devices
	private Boolean contentAvailable;
	// Time to live
	private Integer timeToLive;
	// Flag to request confirmation of message delivery
	private Boolean deliveryReceiptRequested;
	// Test request without sending a message
	private Boolean dryRun;
	// Payload data. A String in JSON format
	private Map<String, Object> dataPayload;
	// Payload notification. A String in JSON format
	private Map<String, String> notificationPayload;

	public FcmOutMessage(String to, String messageId, Map<String, Object> dataPayload) {
		this.to 			= to;
		this.messageId 		= messageId;
		this.dataPayload 	= dataPayload;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getCollapseKey() {
		return collapseKey;
	}

	public void setCollapseKey(String collapseKey) {
		this.collapseKey = collapseKey;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public Boolean isContentAvailable() {
		return contentAvailable;
	}

	public void setContentAvailable(Boolean contentAvailable) {
		this.contentAvailable = contentAvailable;
	}

	public Integer getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(Integer timeToLive) {
		this.timeToLive = timeToLive;
	}

	public Boolean isDeliveryReceiptRequested() {
		return deliveryReceiptRequested;
	}

	public void setDeliveryReceiptRequested(Boolean deliveryReceiptRequested) {
		this.deliveryReceiptRequested = deliveryReceiptRequested;
	}

	public Boolean isDryRun() {
		return dryRun;
	}

	public void setDryRun(Boolean dryRun) {
		this.dryRun = dryRun;
	}

	public Map<String, Object> getDataPayload() {
		return dataPayload;
	}

	public void setDataPayload(Map<String, Object> dataPayload) {
		this.dataPayload = dataPayload;
	}

	public Map<String, String> getNotificationPayload() {
		return notificationPayload;
	}

	public void setNotificationPayload(Map<String, String> notificationPayload) {
		this.notificationPayload = notificationPayload;
	}
}
