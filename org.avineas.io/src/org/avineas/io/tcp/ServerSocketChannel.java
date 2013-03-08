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

/**
 * Base channel for incoming TCP connections. Accepts one connection at a time and
 * keeps reading from it until the connection is lost and then tries to re-establish the
 * connection. As such it is a kind of permanent virtual circuit.
 * 
 * @author Arie van Wijngaarden
 */
public class ServerSocketChannel extends TcpChannel {
    /**
     * Construct a server socket channel on a specific local port.
     * 
     * @param port The port to listen on
     * @throws IOException In case of errors
     */
	public ServerSocketChannel(int port) throws IOException {
		super(new ServerSocketChannelProvider(port));
	}
}