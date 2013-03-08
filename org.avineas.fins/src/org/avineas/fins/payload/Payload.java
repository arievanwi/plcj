/*
 * Copyright 2005, the original author or authors.
 * Copyright 2010-2011, aVineas IT Consulting
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
package org.avineas.fins.payload;

/**
 * Abstraction of a FINS payload. A payload is the summary of the contents of
 * a frame, meaning everything without the FINS header. A payload type is
 * defined by its Main Request Code and Sub Request Code, shortened to mrc and src and
 * simplified to "request code".
 * 
 * @author Arie van Wijngaarden
 * @since 5-11-2005
 */
public class Payload {
    public static final int MAXPAYLOADSIZE = 2000;
    protected static final int MRCOFFSET = 0;
    protected static final int SRCOFFSET = 1;
    protected static final int HEADERSIZE = 2;
    protected byte[] data;
    protected int dataSize;

    public Payload() {
        data = new byte[MAXPAYLOADSIZE];
        dataSize = 2;
    }
    
    public Payload(byte[] payload) {
        data = payload;
        dataSize = payload.length;
    }

    public void setRequestCode(int command) {
        setRequestCode((command >> 8) & 0xff, command & 0xff);
    }
    
    /**
     * A request code consists of a main request code and a sub request code.
     * 
     * @param mrc The main request code
     * @param src The sub request code
     */
    public void setRequestCode(int mrc, int src) {
        setMrc(mrc);
        setSrc(src);
    }
    
    public String getRequestCode() {
        return getMrc() + "/" + getSrc();
    }
    
    public int getMrc() {
        return data[MRCOFFSET];
    }
    
    public int getSrc() {
        return data[SRCOFFSET];
    }
    
    public void setMrc(int mrc) {
        data[MRCOFFSET] = (byte) mrc;
    }
    
    public void setSrc(int src) {
        data[SRCOFFSET] = (byte) src;
    }
    
    /**
     * Get this payload as a byte array which can be simply included in a
     * FINS frame
     * 
     * @param out The buffer where the data is copied to
     * @param offset The offset to start copying
     * @param size The size to copy
     * @return The size copied, may be less than "size" in case the data is less
     */
    public int getBytes(byte[] out, int offset, int size) {
        int tocopy = Math.min(size, dataSize);
        System.arraycopy(data, 0, out, offset, tocopy);
        return tocopy;
    }
    
    /**
     * Bytes variant that creates a new byte array.
     * 
     * @return The byte array containing this payload
     */
    public byte[] getBytes() {
        byte[] out = new byte[dataSize];
        System.arraycopy(data, 0, out, 0, dataSize);
        return out;
    }
    
    /**
     * Encode an integer into a byte array using FINS conventions.
     * 
     * @param value The value to encode
     * @param to The buffer to encode to
     * @param offset The offset in the buffer to start the copying
     * @param length The length to copy, the value is encoded using this amount
     * of bytes
     */
    public static void encode(int value, byte[] to, int offset, int length) {
        for (int cnt = 0; cnt < length; cnt++) {
           to[cnt + offset] = (byte) ((value >> (8 * (length - cnt - 1))) & 0xff);
        }
    }
    
    /**
     * Counterpart of the encode method that decodes an integer from a byte array
     * 
     * @param from The byte array to decode from
     * @param offset The offset in the byte array to start the action
     * @param length The length to decode
     * @return The integer, decoded from the array
     */
    public static int decode(byte[] from, int offset, int length) {
        int value = 0;
        for (int cnt = 0; cnt < length; cnt++) {
            int thisByte = from[cnt + offset] & 0xff;
            value |= (thisByte << (8 * (length - cnt - 1)));
        }
        return value;
    }
}