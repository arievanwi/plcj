/*
 * Copyright 2011 aVineas IT Consulting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.avineas.io.tcp;

import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.annotation.PreDestroy;

import org.avineas.io.Channel;

/**
 * Base channel for TCP connections. Is a wrapper for reading and writing 
 * a socket.
 * 
 * @author Arie van Wijngaarden
 */
class BaseTcpChannel implements Channel {
	private Socket socket;

	BaseTcpChannel(Socket socket) {
		this.socket = socket;
	}
	
	/**
	 * Close the socket currently available, if any.
	 */
	private synchronized void closeSocket() {
		try {
			socket.close();
			socket = null;
		} catch (Exception e) {}
	}
	
	/**
	 * Get the timeout for setting the SO timeout, given a ms wait time.
	 * 
	 * @param timeout The timeout to wait for the socket
	 * @return The timeout to pass to setSoTimeout.
	 */
	static int getTimeout(long timeout) {
		int thisTimeout = Integer.MAX_VALUE;
		if (timeout < thisTimeout) {
			thisTimeout = (int) timeout;
		}
		return thisTimeout;
	}
	
	/**
	 * Get the socket that is currently active.
	 * 
	 * @return The socket subject of this channel
	 */
	private synchronized Socket getSocket() {
		return socket;
	}
	
	@Override
	public int read(byte[] data, int offset, long timeout) {
		Socket socket = getSocket();
		if (socket == null) return -1;
		try {
			socket.setSoTimeout(getTimeout(timeout));
		} catch (Exception exc) {
			return -1;
		}
		int size = 0;
		try {
			size = socket.getInputStream().read(data, offset, data.length - offset);
		} catch (SocketTimeoutException exc) {
			size = 0;
		} catch (Exception exc) {
			size = -1;
		}
		if (size < 0) {
			closeSocket();
		}
		return size;
	}

	@Override
	public int write(byte[] data, int length) {
		Socket socket = getSocket();
		if (socket == null) return -1;
		try {
			socket.getOutputStream().write(data, 0, length);
		} catch (Exception exc) {
			closeSocket();
			return -1;
		}
		return data.length;
	}
	
	@Override
	@PreDestroy
	public void close() {
		closeSocket();
	}
}