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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.avineas.io.ReadChannel;
import org.avineas.io.WriteChannel;

/**
 * A set with static methods that can be used for reading and writing COMLI packets.
 * Can be used by both master and slave implementations to do the actual formatting of
 * packets and sending them out.
 * 
 * @author A. van Wijngaarden
 * @since 24-sep-2007
 */
public class LinkHandler {
    private static final int MAXSIZE = 100;
    private static final byte STX = 0x2;
    private static final byte ETX = 0x3;
    private static final int INTERCHARTIMEOUT = 400;
    
    private static void print(Log logger, String prefix, byte[] contents,
            int offset, int length) {
        StringBuffer buffer = new StringBuffer(prefix);
        for (int cnt = 0; cnt < length; cnt++) {
            int thisByte = contents[cnt + offset] & 0xff;
            buffer.append(" ").append(Integer.toHexString(thisByte));
        }
        logger.info(buffer.toString());
    }
    
    /**
     * Write a packet to another party.
     * 
     * @param out The output stream to write over
     * @param packet The packet to send
     * @throws IOException In case of IO errors
     */
    public static void write(WriteChannel out, Packet packet,
            Log logger) throws IOException {
        byte[] data = new byte[MAXSIZE];
        data[0] = STX;
        int offset = packet.getBytes(data, 1) + 1;
        data[offset++] = ETX;
        int bcc = 0;
        for (int cnt = 1; cnt < offset; cnt++) {
            bcc ^= data[cnt];
        }
        data[offset++] = (byte) (bcc & 0xff);
        out.write(data, offset);
        print(logger, " -> ", data, 0, offset);
    }

    /**
     * Read some data into a buffer (from a channel) starting at a specific offset and
     * with a specific inter-character timeout.
     * 
     * @param channel The channel to read from
     * @param buffer The buffer to read into, must be large enough
     * @param offset The offset to start reading into the buffer
     * @param length The length to read
     * @param timeout The timeout between the characters
     * @return The number of bytes read, -1 on error
     */
    private static int read(ReadChannel channel, byte[] buffer, int offset, int length, 
            long timeout) {
        int cnt;
        byte[] charb = new byte[1];
        for (cnt = 0; cnt < length; cnt++) {
            int size = channel.read(charb, 0, timeout);
            if (size < 0) return -1;
            if (size == 0) break;
            buffer[offset + cnt] = charb[0];
        }
        return cnt;
    }
    
    /**
     * Read a packet from the remote party.
     * 
     * @param in The input stream to read from
     * @param timeout The time to wait for a message to appear
     * @param logger The logger to use for debug messages
     * @return The packet read, if any
     * @throws IOException In case of protocol failures
     */
    public static Packet read(ReadChannel in, long timeout, 
            Log logger) throws IOException {
        Packet packet = null;
        int number;
        byte[] firstByte = new byte[1];
        while ((number = read(in, firstByte, 0, 1, timeout)) > 0 && firstByte[0] != STX) {
            logger.warn("received spurious byte " + 
                    Integer.toHexString(firstByte[0] & 0xff));
        }
        if (number < 0) {
            throw new IOException("end of stream while reading channel");
        }
        if (number > 0) {
            // Read the header away.
            byte[] data = new byte[2 * Packet.MAXSIZE];
            if (read(in, data, 0, Packet.HEADERSIZE, INTERCHARTIMEOUT) == Packet.HEADERSIZE) {
                int offset = Packet.HEADERSIZE;
                packet = new Packet(data, 0, offset);
                // There are three types of packets:
                // - Request type. Consists of address + quantity.
                // - Transfer type. Consists of address + quantity + data.
                // - Acknowledgment. Consists of single ACK.
                // This means that after we checked for an acknowledgment
                // type, we just know the size.
                int remaining;
                if (Packet.isAck(packet.getType())) {
                    remaining = 3;
                }
                else {
                    // Need to get the address and the size.
                    if (read(in, data, Packet.HEADERSIZE,  AddressContents.HEADERSIZE, 
                            INTERCHARTIMEOUT) == AddressContents.HEADERSIZE) {
                        offset += AddressContents.HEADERSIZE;
                        AddressContents contents = new AddressContents(data, 
                            Packet.HEADERSIZE, AddressContents.HEADERSIZE);
                        int dataSize = contents.getCount();
                        if (Packet.isRequest(packet.getType()))
                            remaining = 2;
                        else
                            remaining = dataSize + 2;
                    }
                    else
                        throw new IOException("unexpected timeout while reading packet");
                }
                if (read(in, data, offset, remaining, INTERCHARTIMEOUT) == remaining) {
                    // Check the last bytes.
                    print(logger, " <-  " + STX, data, 0, offset + remaining);
                    if (data[offset + remaining - 2] != ETX) {
                        // Indicate failure.
                        throw new IOException("no ETX found at end of message");
                    }
                    int bcc = 0;
                    for (int cnt = 0; cnt < offset + remaining; cnt++) {
                        bcc ^= data[cnt];
                    }
                    if (bcc != 0) {
                        throw new IOException("BCC incorrect in packet");
                    }
                    packet = new Packet(data, 0, offset + remaining - 2);
                }
                else
                    throw new IOException("unexpected timeout while reading remainder of packet");
            }
        }
        return packet;
    }
}