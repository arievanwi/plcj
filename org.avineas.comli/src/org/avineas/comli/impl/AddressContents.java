/*
 * Copyright, FUJIFILM Manufacturing Europe B.V.
 * Copyright 2012 aVineas IT Consulting
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
 * Address related contents for a COMLI packet. Consists of an
 * address and a quantity (in bytes). Further, it can be coded
 * as binary and hex/ascii.
 * 
 * @author A. van Wijngaarden
 * @since 24-sep-2007
 */
public class AddressContents {
    private static final int ADDRESSOFFSET = 0;
    private static final int ADDRESSSIZE = 4;
    private static final int COUNTOFFSET = ADDRESSOFFSET  + ADDRESSSIZE;
    private static final int COUNTSIZE = 2;
    private static final int DATAOFFSET = COUNTOFFSET + COUNTSIZE;
    public static final int HEADERSIZE = DATAOFFSET;
    private byte[] contents = new byte[Packet.MAXSIZE - Packet.HEADERSIZE];
    private int size = DATAOFFSET;
 
    /**
     * Construct an address contents object from a received data buffer.
     * 
     * @param contents The received contents of a packet
     * @param offset The offset within the buffer to use
     * @param size The size of the buffer to use
     */
    public AddressContents(byte[] contents, int offset, int size) {
        System.arraycopy(contents, offset, this.contents, 0, size);
        this.size = size;
    }
    
    /**
     * Equivalent of {@link #AddressContents(byte[], int, int) constructor} using
     * parameters equal to 0 and the size of the passed contents.
     *  
     * @param contents The contents of the packets
     */
    public AddressContents(byte[] contents) {
        this(contents, 0, contents.length);
    }
    
    /**
     * Construct an address contents block with a start address and a
     * quantity.
     * 
     * @param address The start address
     * @param cnt The quantity in the packet
     */
    public AddressContents(int address, int cnt) {
        setAddress(address);
        setCount(cnt);
    }
    
    public int getAddress() {
        return Coder.fromHexBytes(contents, ADDRESSOFFSET, ADDRESSSIZE);
    }
    
    public void setAddress(int address) {
        System.arraycopy(Coder.toHexBytes(address, ADDRESSSIZE), 0, 
            contents, ADDRESSOFFSET, ADDRESSSIZE);
    }

    public int getCount() {
        return Coder.fromHexBytes(contents, COUNTOFFSET, COUNTSIZE);
    }
    
    public void setCount(int cnt) {
        System.arraycopy(Coder.toHexBytes(cnt, COUNTSIZE), 0, 
            contents, COUNTOFFSET, COUNTSIZE);
    }
    
    public void setData(byte[] data, int offset, int size) {
        System.arraycopy(data, offset, contents, DATAOFFSET, size);
        setCount(size);
        this.size = DATAOFFSET + size;
    }
    
    public byte[] getData() {
        byte[] out = new byte[this.size - DATAOFFSET];
        System.arraycopy(contents, DATAOFFSET, out, 0, out.length);
        return out;
    }

    /**
     * Get this object as byte array to be included in a COMLI packet.
     * 
     * @return The object as coded bytes
     */
    public byte[] getBytes() {
        byte[] toReturn = new byte[this.size];
        System.arraycopy(contents, 0, toReturn, 0, this.size);
        return toReturn;
    }
}