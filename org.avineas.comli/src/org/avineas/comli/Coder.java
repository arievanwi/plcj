/*
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
package org.avineas.comli;

/**
 * Coder for encoding and decoding values into an array of bytes. Useful for
 * master usage and slave implements.
 * 
 * @author A. van Wijngaarden
 */
public class Coder {
    private Coder() {
        // Only static methods. Should not be instantiated
    }
    /**
     * Convert an integer value to a byte array containing the ASCII representation
     * of the integer.
     * 
     * @param value The value to check
     * @param size The size in bytes of the output array
     * @return A byte array containing the ascii representation of the integer
     */
    public static byte[] toHexBytes(int value, int size) {
        String bytes = Integer.toHexString(value).toUpperCase();
        if (bytes.length() > size)
            throw new RuntimeException("(bugcheck): " + value + 
                " does not fit into " + size + " bytes");
        while (bytes.length() < size)
            bytes = "0" + bytes;
        try {
            return bytes.getBytes("US-ASCII");
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }
    
    /**
     * Variant to put data immediately into a byte array.
     * 
     * @param value The value to set
     * @param size The size in which to code
     * @param to The output buffer
     * @param offset The offset in the output buffer
     */
    public static void toHexBytes(int value, int size, byte[] to, int offset) {
        byte[] hex = toHexBytes(value, size);
        System.arraycopy(hex, 0, to, offset, size);
    }
    
    /**
     * Convert a byte array with a hex specification to an integer. It is
     * assumed that the byte array contains ascii characters.
     * 
     * @param data The buffer from which to decode
     * @param offset The offset in the buffer to start the coding
     * @param size The size to use
     * @return The value of the converted data
     */
    public static int fromHexBytes(byte[] data, int offset, int size) {
        String strValue = new String(data, offset, size);
        return Integer.parseInt(strValue, 16);
    }
    
    private static int swap(int in) {
        int out = 0;
        for (int cnt = 0; cnt < 8; cnt++) {
            if ((in & (1 << cnt)) != 0) {
            out |= (1 << (7 - cnt));
            }
        }
        return out;
    }
    
    /**
     * Encode an integer (actually a word) value into a byte array.
     * 
     * @param value The value to encode
     * @param data The buffer to encode into
     * @param off The offset where to start coding
     */
    public static void encode(int value, byte[] data, int off) {
        int offset = off;
        int highbyte = ((value >> 8) & 0xff);
        data[offset++] = (byte) swap(highbyte);
        int lowbyte = value & 0xff;
        data[offset] = (byte) swap(lowbyte);
    }
    
    /**
     * Decode an integer (actually a word) value from a byte array
     * 
     * @param data The buffer to decode from
     * @param off The offset where to start the coding
     * @return The word value
     */
    public static int decode(byte[] data, int off) {
        int offset = off;
        int highbyte = swap(data[offset++]);
        int lowbyte = swap(data[offset]);
        return ((highbyte << 8) | (lowbyte)) & 0xffff; 
     }
}
