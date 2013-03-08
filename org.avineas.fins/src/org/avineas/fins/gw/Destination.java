/*
 * Copyright 2005, the original author or authors.
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
package org.avineas.fins.gw;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Container class of an internet (IP) address and a port number.
 * 
 * @author Arie van Wijngaarden
 * @since 5-11-2005
 */
class Destination {
    private InetAddress address;
    private int port;
    
    public Destination() {
    }
    
    /**
     * Constructor that creates a destination from a string in the format
     * "hostname/port number" or "ip address/port number".
     * 
     * @param asString The string representation
     * @throws UnknownHostException In case the host name is not known.
     */
    public Destination(String asString) throws UnknownHostException {
        String values[] = asString.split("[\\s/:]+");
        setAddress(InetAddress.getByName(values[0]));
        setPort(Integer.parseInt(values[1]));
    }
    
    public Destination(InetAddress addr, int port) {
        setAddress(addr);
        setPort(port);
    }
    
    public Destination(String host, int port) throws UnknownHostException{
        this(InetAddress.getByName(host), port);
    }
    
    public InetAddress getAddress() {
        return address;
    }
    
    public void setAddress(InetAddress address) {
        this.address = address;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    @Override
    public String toString() {
        return address.getHostAddress() + ":" + port;
    }   
    
    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        return toString().equals(other.toString());
    }
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}