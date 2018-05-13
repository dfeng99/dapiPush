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

public class GcmAckResponse{
	final static String INVALID_JSON = "INVALID_JSON";
	final static String BAD_REGISTRATION = "BAD_REGISTRATION";
	final static String DEVICE_UNREGISTERED = "DEVICE_UNREGISTERED";
	final static String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
	final static String BAD_ACK = "BAD_ACK";
	final static String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
	final static String DEVICE_MESSAGE_RATE_EXCEEDED = "DEVICE_MESSAGE_RATE_EXCEEDED";
	final static String TOPICS_MESSAGE_RATE_EXCEEDED = "TOPICS_MESSAGE_RATE_EXCEEDED";
	final static String CONNECTION_DRAINING = "CONNECTION_DRAINING";
	final static String INVALID_APNS_CREDENTIAL = "INVALID_APNS_CREDENTIAL";
	
	private String from;
	private String message_id;
	private String message_type;
	private String registration_id;
	private String error;
	private String error_description;
	
	public String getFrom() {
		return this.from;
	}
	public void setFrom(String str) {
		this.from = str;
	}
	public String getMessage_id() {
		return this.message_id;
	}
	public void setMessage_id(String str) {
		this.message_id = str;
	}
	public String getMessage_type() {
		return this.message_type;
	}
	public void setMessage_type(String str) {
		this.message_type = str;
	}
	public String getRegistration_id() {
		return this.registration_id;
	}
	public void setRegistration_id(String str) {
		this.registration_id = str;
	}
	public String getError() {
		return this.error;
	}
	public void setError(String str) {
		this.error = str;
	}
	public String getError_description() {
		return this.error_description;
	}
	public void setError_description(String str) {
		this.error_description = str;
	}
}
