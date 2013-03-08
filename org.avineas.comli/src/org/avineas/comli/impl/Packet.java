/*
 * Copyright, FUJIFILM Manufacturing Europe B.V.
 * Copyright 2010-2012 aVineas IT Consulting
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
package org.avineas.comli.impl;

import org.avineas.comli.Coder;

/**
 * Abstraction of a COMLI packet as received at datalink
 * level, meaning: destination, stamp, message type and
 * contents.
 * 
 * @author A. van Wijngaarden
 * @since 24-sep-2007
 */
public class Packet {
    /** Acknowledgment packet. */
    public static final byte ACKTYPE = 0x31;
    public static final byte ACK = 0x6;
    public static final int MAXSIZE = 74;
    private static final int DESTOFFSET = 0;
    private static final int STAMPOFFSET = 2;
    private static final int TYPEOFFSET = 3;
    private static final int CONTENTSOFFSET = 4;
    public static final int HEADERSIZE = CONTENTSOFFSET;
    
    protected byte[] contents = new byte[MAXSIZE];
    protected int size = HEADERSIZE;
    
    /**
     * Construct a packet from a received byte array.
     * 
     * @param data The contents of the data
     */
    public Packet(byte[] data) {
        this(data, 0, data.length);
    }
    
    /**
     * Construct a packet from a received byte array.
     * 
     * @param data The packet buffer
     * @param offset The offset in the buffer where the data starts
     * @param size The size of the data received
     */
    public Packet(byte[] data, int offset, int size) {
        System.arraycopy(data, offset, contents, 0, size);
        this.size = size;
    }
    
    /**
     * Construct a packet from the parameters
     * 
     * @param destination The destination for the packet
     * @param stamp The stamp
     * @param type The message type
     * @param contents The contents of the message
     */
    public Packet(int destination, byte stamp, byte type, byte[] contents) {
        setDestination(destination);
        setStamp(stamp);
        setType(type);
        setContents(contents);
    }

    /**
     * Set the packet stamp.
     */
    public void setStamp(byte stamp) {
        contents[STAMPOFFSET] = stamp;
    }

    /**
     * Set the packet type.
     */
    public void setType(byte type) {
        contents[TYPEOFFSET] = type;
    }

    /**
     * Set the destination of this packet. Destination 0 is the master.
     */
    public void setDestination(int destination) {
        byte[] dest = Coder.toHexBytes(destination, 2);
        System.arraycopy(dest, 0, contents, DESTOFFSET, 2);
    }

    /**
     * Set the contents/payload of the packet.
     */
    public void setContents(byte[] contents) {
        System.arraycopy(contents, 0, this.contents, CONTENTSOFFSET, 
            contents.length);
        size = HEADERSIZE + contents.length;
    }

    /**
     * Get the contents of this packet. Is all the payload data, without header.
     */
    public byte[] getContents() {
        byte[] data = new byte[size - HEADERSIZE];
        System.arraycopy(contents, CONTENTSOFFSET, data, 0, data.length);
        return data;
    }

    /**
     * Get the destination of this packet.
     */
    public int getDestination() {
        return Coder.fromHexBytes(contents, DESTOFFSET, 2);
    }

    /**
     * Get the stamp of this packet.
     */
    public byte getStamp() {
        return contents[STAMPOFFSET];
    }

    /**
     * Get the packet type.
     */
    public byte getType() {
        return contents[TYPEOFFSET];
    }
    
    /**
     * Get the bytes of this packet to be transferred over the
     * line.
     * 
     * @param buffer The buffer to write the data in
     * @param offset The start offset to start writing
     * @return The number of bytes written
     */
    public int getBytes(byte[] buffer, int offset) {
        System.arraycopy(contents, 0, buffer, offset, size);
        return size;
    }
    
    /**
     * Get the bytes of this packet, array variant
     * 
     * @return The packet as byte array
     */
    public byte[] getBytes() {
        byte[] out = new byte[size];
        getBytes(out, 0);
        return out;
    }
    
    /**
     * Is the packet type an ACK?
     * 
     * @param type The packet type to check
     * @return True if this packet type is an ack
     */
    public static boolean isAck(byte type) {
        return type == ACKTYPE;
    }
    
    /**
     * Is the type passed a request type?
     *  
     * @param type The packet type to check
     * @return True if this type is a request
     */
    public static boolean isRequest(byte type) {
        byte[] data = new byte[]{type};
        String strValue = new String(data);
        return "24579:<BEGIKLOQSUWZ]^".contains(strValue);
    }
    
    /**
     * Is the type passed a response type?
     * 
     * @param type The packet type to check
     * @return True if this packet type is a response
     */
    public static boolean isTransfer(byte type) {
        return !isAck(type) && !isRequest(type);
    }
}