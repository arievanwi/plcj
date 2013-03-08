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

import javax.annotation.PreDestroy;

import org.avineas.io.Channel;
import org.avineas.io.ChannelProvider;

/**
 * Base channel for TCP connections. Is a wrapper for handling incoming and outgoing 
 * TCP socket connections. The channel tries to maintain a permanent connection, meaning that
 * it will open a socket again when it is somehow closed. 
 * 
 * @author Arie van Wijngaarden
 */
class TcpChannel implements Channel {
	private Channel currentChannel;
	private ChannelProvider provider;
	
	TcpChannel(ChannelProvider provider) {
		this.provider = provider;
	}
	
	/**
	 * Wait for a socket to become available.
	 * 
	 * @param timeout The timeout, in ms. to wait for a socket
	 * @return The socket, or null if no socket could be opened
	 */
	private Channel waitForChannel(long timeout) {
		synchronized (this) {
			if (this.currentChannel != null) 
				return this.currentChannel;
		}
		try {
			Channel cur = provider.getChannel(timeout);
			synchronized (this) {
				this.currentChannel = cur;
				return cur;
			}
		} catch (Exception exc) {
		}
		return null;
	}
	
	@Override
	public int read(byte[] data, int offset, long timeout) {
		Channel channel = waitForChannel(timeout);
		if (channel == null) return 0;
		int size = channel.read(data, offset, timeout);
		if (size < 0) {
			_close();
		}
		return size;
	}

	@Override
	public int write(byte[] data, int length) {
		Channel channel = waitForChannel(Long.MAX_VALUE);
		if (channel == null) return -1;
		int size = channel.write(data, length);
		if (size < 0) {
			_close();
		}
		return size;
	}
	
	private synchronized void _close() {
		if (this.currentChannel != null) {
			try {
				this.currentChannel.close();
			} catch (Exception exc) {}
		}
		this.currentChannel = null;
	}
	
	@Override
	@PreDestroy
	public void close() {
		_close();
		try {
			provider.close();
		} catch (Exception exc) {}
	}
	
	@Override
	public String toString() {
		return provider.toString();
	}
}