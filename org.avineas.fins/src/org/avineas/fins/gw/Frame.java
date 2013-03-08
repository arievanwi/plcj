/*
 * Copyright, Fuji Film B.V., Tilburg
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

import org.avineas.fins.Address;
import org.avineas.fins.payload.Payload;

/**
 * Abstraction of a FINS packet as communicated between gateways.
 * 
 * @author A. van Wijngaarden
 * @since 3-nov-2005
 */
class Frame {
    /**
     * The maximim payload data size of a FINS frame.
     */
    public static int MAXFINSDATA = Payload.MAXPAYLOADSIZE;
    private static int HEADERSIZE = 10;
    /**
     * Get the maximum number of bytes needed to receive a frame.
     */
    public static int MAXFRAMESIZE = MAXFINSDATA + HEADERSIZE;
    
    private static int ICFOFFSET = 0;
    private static int RSVOFFSET = 1;
    private static int GCTOFFSET = 2;
    private static int DESTINATIONOFFSET = 3;
    private static int SOURCEOFFSET = 6;
    private static int SIDOFFSET = 9;
    
    private static byte GCT = 2;
    private static byte ISREPLY = 0x40;
    
    private static byte sid = 1;
    private byte[] data;
    private int dataLength;  // The current frame length in bytes
    
    /**
     * Construct a packet from a byte array received from a remote system.
     * This allows for easier access of payload, addresses, etc.
     * 
     * @param buffer The buffer, as received
     * @param size The size in the buffer that is filled
     */
    public Frame(byte[] buffer, int size) {
        data = new byte[size];
        System.arraycopy(buffer, 0, data, 0, size);
        dataLength = size;
    }
    
    public Frame(byte[] buffer) {
        this(buffer, buffer.length);
    }
    
    /**
     * Construct a new packet. This means that data is initialized for
     * transmission but no data is actually present in the payload
     * (nor addresses, etc.).
     */
    public Frame() {
        data = new byte[MAXFRAMESIZE];
        data[RSVOFFSET] = 0;
        data[SIDOFFSET] = sid++;
        data[ICFOFFSET] = (byte) 0x80;
        data[GCTOFFSET] = GCT;
        dataLength = HEADERSIZE;
    }
    
    /**
     * Constructor that constructs a complete packet in one go.
     * 
     * @param source The source address
     * @param destination The destination address
     * @param payload The payload
     * @param offset The offset in the buffer where the payload starts
     * @param size The size of the payload from the offset
     */
    public Frame(Address source, Address destination, 
            byte[] payload, int offset, int size) {
        this();
        setSource(source);
        setDestination(destination);
        setPayload(payload, offset, size);
    }
    
    /**
     * Simpeler constructor that uses a predefined payload buffer.
     * 
     * @param source The source address
     * @param destination The destination address
     * @param payload The payload
     */
    public Frame(Address source, Address destination, byte[] payload) {
        this();
        setSource(source);
        setDestination(destination);
        if (payload != null)
            setPayload(payload);
    }
    
    public Address getDestination() {
        return new Address(data, DESTINATIONOFFSET);
    }
    
    public Address getSource() {
        return new Address(data, SOURCEOFFSET);
    }
    
    public void setDestination(Address addr) {
        addr.set(data, DESTINATIONOFFSET);
    }

    public void setSource(Address addr) {
        addr.set(data, SOURCEOFFSET);
    }

    public void setPayload(byte[] payload, int offset, int size) {
        System.arraycopy(payload, offset, data, HEADERSIZE, size);
        dataLength = size + HEADERSIZE;
    }
    
    public void setPayload(byte[] payload) {
        setPayload(payload, 0, payload.length);
    }
    
    public int getSid() {
        return data[SIDOFFSET];
    }
    
    public void setSid(int newSid) {
        data[SIDOFFSET] = (byte) newSid;
    }

    /**
     * Construct a reply from an existing packet giving a specific
     * payload as reply.
     * 
     * @param payload The payload in the response. Must at least contain the Mrc/Src
     * and the response code.
     * @return A new constructed packet
     */
    public Frame constructReply(byte[] payload) {
        Frame reply = new Frame();
        reply.setDestination(getSource());
        reply.setSource(getDestination());
        reply.setSid(getSid());
        reply.data[ICFOFFSET] = (byte) ((data[ICFOFFSET] | ISREPLY) & 0xff);
        if (payload != null)
            reply.setPayload(payload);
        return reply;
    }
    
    /**
     * Checks whether this packet is a reply.
     */
    public boolean isReply() {
        return (data[ICFOFFSET] & ISREPLY) != 0;
    }

    /**
     * Checks whether the indication in this packet indicates that
     * a packet must be send as reply to this packet.
     * 
     * @return Indication whether a reply must be send for this
     * packet
     */
    public boolean mustReply() {
        return (data[ICFOFFSET] & 1) == 0 && !isReply();
    }
    
    /**
     * Get the payload from the packet in a pre-defined byte array
     * 
     * @param payload The buffer to copy the payload in
     * @param offset The offset to start copying
     * @param size The maximum size to copy
     * @return The size copied, which is the minimum of the buffer size
     * and the actual size of the payload
     */
    public int getPayload(byte[] payload, int offset, int size) {
        int copy = Math.min(size, dataLength - HEADERSIZE); 
        System.arraycopy(data, HEADERSIZE, payload, offset, copy);
        return copy;
    }
    
    /**
     * Get the size of the payload in the packet.
     * 
     * @return The size of the  payload in bytes
     */
    public int getPayloadSize() {
        return dataLength;
    }
    
    /**
     * Get this frame as byte array.
     */
    public byte[] getBytes() {
        byte[] out = new byte[dataLength];
        System.arraycopy(data, 0, out, 0, dataLength);
        return out;
    }

    /**
     * Get the payload of this frame as byte array.
     */
    public byte[] getPayload() {
        byte[] data = new byte[dataLength - HEADERSIZE];
        getPayload(data, 0, data.length);
        return data;
    }
}