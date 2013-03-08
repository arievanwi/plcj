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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Channel provider for incoming TCP connections. Provides channels for incoming connections
 * and can therefore be used to separately handle connections to different parties.
 * 
 * @author Arie van Wijngaarden
 */
public class ServerSocketChannelProvider extends TcpChannelProvider {
	private ServerSocket socket;
	
	/**
	 * Listen to connections on a specific TCP port with a default backlog.
	 * 
	 * @param port The port to listen on
	 * @throws IOException In case of errors
	 */
	public ServerSocketChannelProvider(int port) throws IOException {
		socket = new ServerSocket(port);
	}
	
	/**
	 * Listen to connections on a specific TCP port with a specified backlog
	 * 
	 * @param port The port to listen on
	 * @param backlog The number of pending connections that are allowed
	 * @throws IOException In case of errors
	 */
	public ServerSocketChannelProvider(int port, int backlog) throws IOException {
	    socket = new ServerSocket(port, backlog);
	}
	
	@Override
	protected Socket connect(long timeout) throws Exception {
		socket.setSoTimeout(BaseTcpChannel.getTimeout(timeout));
		return socket.accept();
	}
	
	@Override
	public void close() {
		try {
			socket.close();
		} catch (Exception exc) {}
	}

	@Override
	public String toString() {
		return socket.toString();
	}
}