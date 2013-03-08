/*
 * Copyright, Fuji Film B.V., Tilburg
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
package org.avineas.fins;

/**
 * FINS addressing. Convenience methods for handling FINS addresses. FINS addresses
 * consist of 3 numbers: a network address containing a group of nodes that can communicate
 * with each other. Every node can have one or more units which can be seen as specific
 * destinations on a node. The string representation of FINS addresses is a/b/c where a is the
 * network number, b is the node number and c is the unit number, like 12/2/1.
 * 
 * @author A. van Wijngaarden
 * @since 3-nov-2005
 */
public final class Address {
    private byte networkAddress;
    private byte nodeNumber;
    private byte unitAddress;
    
    /**
     * Default constructor. Constructs an address with network 0, node 1 and unit 0.
     */
    public Address() {
        this(0, 1, 0);
    }
    
    /**
     * Construct an address from a byte array, normally part of a frame.
     * 
     * @param from The byte array to extract the address from
     * @param offset The offset in the byte array to start the decoding
     */
    public Address(byte[] from, int offset) {
        this(from[offset], from[offset + 1], from[offset + 2]);
    }

    /**
     * Construct an address with specific network, node and unit numbers.
     * 
     * @param na The network address
     * @param nn The node address
     * @param unit The unit on the node
     */
    public Address(int na, int nn, int unit) {
        networkAddress = (byte) na;
        nodeNumber = (byte) nn;
        unitAddress = (byte) unit;
    }
    
    /**
     * Construct an address from a string. This string must have the form
     * network/node/unit where the unit value is optional (in case all units from
     * a node should be referred or the address is actually a node address).
     * 
     * @param addr The string representation of the address
     */
    public Address(String addr) {
        String[] values = addr.split("[\\s/]+");
        if (values.length < 3) {
            unitAddress = 0;
        }
        else {
            unitAddress = (byte) Integer.parseInt(values[2]);
        }
        networkAddress = (byte) Integer.parseInt(values[0]);
        nodeNumber = (byte) Integer.parseInt(values[1]);
    }
    
    /**
     * Get the network address.
     * 
     * @return The byte representing the network address
     */
    public byte getNetworkAddress() {
        return networkAddress;
    }
    
    /**
     * Set the network address, this is the first byte of the FINS addressing.
     * 
     * @param networkAddress The network address of this address object
     */
    public void setNetworkAddress(byte networkAddress) {
        this.networkAddress = networkAddress;
    }
    
    /**
     * Get the node number.
     * 
     * @return The byte representing the node number
     */
    public byte getNodeNumber() {
        return nodeNumber;
    }
    
    /**
     * Set the node number of this address. The node number is the second/middle byte in FINS
     * addressing.
     * 
     * @param nodeNumber The node number
     */
    public void setNodeNumber(byte nodeNumber) {
        this.nodeNumber = nodeNumber;
    }
    
    /**
     * Get the unit address value of this address. The unit address is the last byte in the FINS
     * addressing.
     * 
     * @return The unit address byte value
     */
    public byte getUnitAddress() {
        return unitAddress;
    }
    
    /**
     * Set the unit address.
     * 
     * @param unitAddress The unit address to set
     */
    public void setUnitAddress(byte unitAddress) {
        this.unitAddress = unitAddress;
    }
    
    /**
     * Encode this address into a message, normally a FINS packet or frame.
     * 
     * @param to The byte array to encode the address in 
     * @param offset The offset to start in the byte array
     */
    public void set(byte[] to, int offset) {
        int off = offset;
        to[off++] = networkAddress;
        to[off++] = nodeNumber;
        to[off++] = unitAddress;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (!other.getClass().equals(Address.class)) return false;
        return toString().equals(other.toString());
    }
    
    @Override
    public String toString() {
        return networkAddress + "/" + nodeNumber + "/" + unitAddress;
    }
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    
    /**
     * Get the node part of this address as string value. This is the network address
     * and node number separated by a slash, like 1/4.
     * 
     * @return The string representation of the node part
     */
    public String getNodeAsString() {
        return networkAddress + "/" + nodeNumber;
    }
}