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

import org.avineas.io.Channel;
import org.avineas.io.ChannelProvider;

/**
 * Base provider for TCP connections. Is a base class for implementing simple variants
 * for server and client sockets.
 * 
 * @author Arie van Wijngaarden
 */
abstract class TcpChannelProvider implements ChannelProvider {
	/**
	 * Method subclasses must implement to provide a socket to the channel. The time must be
	 * limited to the time passed.
	 *  
	 * @return The socket, if found within the timeout. Otherwise null
	 * @throws Exception In case of errors
	 */
	protected abstract Socket connect(long timeout) throws Exception;

	/**
	 * Wait for a socket to become available.
	 * 
	 * @param timeout The timeout, in ms. to wait for a socket
	 * @return The socket, or null if no socket could be opened
	 */
	private Channel waitForChannel(long timeout) {
		Socket socket = null;
		try {
			socket = connect(timeout);
		} catch (InterruptedException exc) {
			Thread.currentThread().interrupt();
		} catch (Exception exc) {
		}
		return (socket == null) ? null : new BaseTcpChannel(socket);
	}
	
	@Override
	public Channel getChannel(long timeout) {
		return waitForChannel(timeout);
	}
}