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

/**
 * Base channel for outgoing TCP connections. This class automatically reconnects to
 * the remote port if somehow the connection is lost. As such it is a kind of permanent virtual
 * circuit to an other party.
 * 
 * @author Arie van Wijngaarden
 */
public class SocketChannel extends TcpChannel {
    /**
     * Create a socket to a specific remote TCP port.
     * 
     * @param host The host name to connect to
     * @param port The port at the remote host to connect to
     */
	public SocketChannel(String host, int port) {
		super(new SocketChannelProvider(host, port));
	}
}
