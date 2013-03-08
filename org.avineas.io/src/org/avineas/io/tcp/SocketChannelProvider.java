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

/**
 * Channel provider for outgoing connection sockets. 
 * 
 * @author Arie van Wijngaarden
 */
public class SocketChannelProvider extends TcpChannelProvider {
	private String host;
	private int port;
	
	public SocketChannelProvider(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	@Override
	protected Socket connect(long timeout) throws Exception {
		Socket socket = new Socket(host, port);
		return socket;
	}
	
	@Override
	public String toString() {
		return "Socket " + host + "/" + port;
	}

	@Override
	public void close() {
	}
}
