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

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Util class for back off strategy
 */

public class BackOffStrategy {
	
	private static final Logger logger = Logger.getLogger(BackOffStrategy.class.getName());

	public static final int DEFAULT_RETRIES = 3;
	public static final long DEFAULT_WAIT_TIME_IN_MILLI = 1000;

	private long timeToWait;
	private int numberOfTriesLeft;
	final private int numberOfRetries;
	final private long defaultTimeToWait;
	final private Random random = new Random();

	public BackOffStrategy() {
		this(DEFAULT_RETRIES, DEFAULT_WAIT_TIME_IN_MILLI);
	}

	public BackOffStrategy(int numberOfRetries, long defaultTimeToWait) {
		this.numberOfRetries = numberOfRetries;
		this.numberOfTriesLeft = numberOfRetries;
		this.defaultTimeToWait = defaultTimeToWait;
		this.timeToWait = defaultTimeToWait;
	}

	/**
	 * @return true if there are tries left
	 */
	public boolean shouldRetry() {
		return numberOfTriesLeft > 0;
	}

	public void errorOccured2() throws Exception {
		numberOfTriesLeft--;
		if (!shouldRetry()) {
			throw new Exception("Retry Failed: Total of attempts: " + numberOfRetries + ". Total waited time: " + timeToWait + "ms.");
		}
		waitUntilNextTry();
		timeToWait *= 2;
		// we add a random time (recommendation from google)
		timeToWait += random.nextInt(500);
	}

	public void errorOccured() {
		numberOfTriesLeft--;
		if (!shouldRetry()) {
			logger.log(Level.INFO, "Retry Failed: Total of attempts: " + numberOfRetries + ". Total waited time: " + timeToWait + "ms.");
		}
		waitUntilNextTry();
		timeToWait *= 2;
		// we add a random time (google recommendation)
		timeToWait += random.nextInt(500);
	}

	private void waitUntilNextTry() {
		try {
			Thread.sleep(timeToWait);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Error waiting until next try for the backoff strategy.", e);
		}
	}

	public long getTimeToWait() {
		return this.timeToWait;
	}

	/**
	 * Use this method when the call was successful otherwise it will continue in an infinite loop
	 */
	public void doNotRetry() {
		numberOfTriesLeft = 0;
	}

	/**
	 * Reset back off state. Call this method after successful attempts if you want to reuse the class.
	 */
	public void reset() {
		this.numberOfTriesLeft = numberOfRetries;
		this.timeToWait = defaultTimeToWait;
	}

}
