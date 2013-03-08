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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.avineas.comli.Master;
import org.avineas.io.Channel;

/**
 * COMLI master. Takes care of sending and receiving data from/to slaves that are
 * connected to the same channel.
 * 
 * @author Arie van Wijngaarden
 */
public class MasterImpl implements Master {
    private static Log logger = LogFactory.getLog(MasterImpl.class);
    private Channel channel;
    private long responseTimeout;
    private int tries = 3;
    private byte stamp = 0x30;
    
    /**
     * Create a COMLI master part from a channel and a response timeout
     * 
     * @param channel The channel to communicate over
     * @param timeout The time out for responses, in ms.
     */
    public MasterImpl(Channel channel, long timeout) {
        this.channel = channel;
        responseTimeout = timeout;
    }
    
    private Packet send(Packet packet) {
        Packet response = null;
        int cnt;
        for (cnt = 0; cnt < tries; cnt++) {
            try {
                LinkHandler.write(channel, packet, logger);
                // Wait for the reply.
                response = LinkHandler.read(channel, responseTimeout, logger);
                if (response == null) {
                    logger.warn("time-out in reading response from slave");
                }
                else if (response.getDestination() != 0) {
                    throw new Exception("destination in response packet is not the master");
                }
                else if (response.getStamp() != packet.getStamp()) {
                    throw new Exception("stamp mismatch in slave reply");
                }
                else 
                    break;
            } catch (Exception exc) {
                logger.error("failure performing round-trip to slave", exc);
            }
        }
        if (cnt >= tries)
        	logger.error("could not execute round-trip to slave " + packet.getDestination());
        return response;
    }

    private Packet doTrip(int destination, byte type, int startAddress, int count, 
            byte[] contents, int offset, int size) { 
        AddressContents values = new AddressContents(startAddress, count);
        if (contents != null)
            values.setData(contents, offset, size);
        Packet packet = new Packet(destination, stamp, type, values.getBytes());
        stamp++;
        if (stamp > 0x32)
            stamp = 0x31;
        Packet response = send(packet);
        return response;
    }
        
    /**
     * Perform a data transfer to a slave node.
     * 
     * @param destination The slave this message is meant for
     * @param type The transfer type
     * @param startAddress The start address
     * @param contents The contents of the addresses
     * @param offset The offset in the buffer to start the data transfer
     * @param size The size of the buffer to send
     * @return Indication whether the transfer was successful
     */
    @Override
    public boolean transfer(int destination, byte type, int startAddress, byte[] contents, 
            int offset, int size) {
        if (!Packet.isTransfer(type)) {
            throw new RuntimeException("(bugcheck): " + type + " is not a transfer type");
        }
        Packet response = doTrip(destination, type, startAddress, size, contents, offset, size);
        if (response == null) return false;
        if (!Packet.isAck(response.getType())) {
            logger.error("no ack response received on transfer");
            return false;
        }
        return true;
    }
    
    /**
     * Request data from a slave.
     * 
     * @param destination The destination/slave number
     * @param type The type of the message
     * @param startAddress The start address
     * @param size The size to request
     * @return The received data or null if something went wrong
     */
    @Override
    public byte[] request(int destination, byte type, int startAddress, int size) {
        if (!Packet.isRequest(type)) {
            throw new RuntimeException("(bugcheck): " + Integer.toHexString(type) + 
                    " is not a request");
        }
        Packet response = doTrip(destination, type, startAddress, size, null, 0, 0);
        if (response == null) return null;
        if (!Packet.isTransfer(response.getType())) {
            logger.error("no transfer type received on request");
            return null;
        }
        AddressContents contents = new AddressContents(response.getContents());
        return contents.getData();
    }
    
    /**
     * Set the number of times a message is tried to be sent before
     * it is said to be failed.
     * 
     * @param tries The number of tries, defaults to 3
     */
    public void setTries(int tries) {
        this.tries = tries;
    }
}