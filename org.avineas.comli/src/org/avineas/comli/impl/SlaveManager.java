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
import org.avineas.comli.Slave;
import org.avineas.io.Channel;

/**
 * Manager of a line containing a link to multiple slaves. This allows
 * for multi-drop solutions. The manager maintains a set with slaves
 * and dispatches received messages to the correct slave.
 * 
 * @author A. van Wijngaarden
 * @since 26-sep-2007
 */
public class SlaveManager {
    private static Log logger = LogFactory.getLog(SlaveManager.class);
    private SlaveProvider slaves = new SimpleSlaveProvider();
    private Thread reader;
    private byte lastStamp = 0;

    /**
     * Construct a slave manager.
     * 
     * @param channel The channel to interact over
     * @param timeout The timeout, in ms. used for packets on the line
     */
    public SlaveManager(final Channel channel, final long timeout) {
        reader = new Thread(new Runnable(){
            @SuppressWarnings("synthetic-access")
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Packet packet = LinkHandler.read(channel, Long.MAX_VALUE,
                                logger);
                        if (packet == null) continue;
                        Packet response = handlePacket(packet);
                        if (response != null)
                            LinkHandler.write(channel, response, logger);
                    } catch (Exception exc) {
                        if (!Thread.currentThread().isInterrupted())
                            logger.warn("exception while reading/handling data", exc);
                    }
                }
            }
        });
        reader.start();
    }
    
    private Packet handlePacket(Packet packet) {
        int destination = packet.getDestination();
        if (destination == 0) {
            logger.info("received message for master, skipped");
            return null;
        }
        if (packet.getStamp() == lastStamp) {
            logger.info("re-transmit received, skipped");
            return null;
        }
        lastStamp = packet.getStamp();
        Slave slave = slaves.get(destination);
        if (slave == null) {
            logger.info("slave " + destination + " is not managed, skipped");
            return null;
        }
        AddressContents contents = new AddressContents(packet.getContents());
        Packet response = null;
        // Check the message type and delegate.
        if (Packet.isRequest(packet.getType())) {
            // It is a request, should answer with a transfer.
            byte[] toReturn = new byte[contents.getCount()];
            byte reply = slave.handleRequest(packet.getType(), contents.getAddress(),
                toReturn);
            if (reply != 0) {
                contents.setData(toReturn, 0, toReturn.length);
                response = new Packet(0, packet.getStamp(), reply, contents.getBytes());
            }
        }
        else {
            // It is a transfer, should answer with an ack.
            if (slave.handleTransfer(packet.getType(), contents.getAddress(), contents.getData())) {
                byte[] replyContents = new byte[] {Packet.ACK};
                response = new Packet(0, packet.getStamp(), Packet.ACKTYPE, replyContents);
            }
        }
        return response;
    }
    
    /**
     * Destroy the manager. Should be wired or called as end handler to close
     * down the reader.
     */
    public void destroy() {
        this.reader.interrupt();
    }

    /**
     * Set the provider that looks up a slave based on the identification
     * of the slave. The manager uses the provider to look-up the slave
     * when a message for a specific slave comes in.
     * 
     * @param slaves The slave provider
     */
    public void setSlaves(SlaveProvider slaves) {
        this.slaves = slaves;
    }
}
