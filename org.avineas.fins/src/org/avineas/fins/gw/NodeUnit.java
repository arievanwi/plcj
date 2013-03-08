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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.avineas.fins.Address;
import org.avineas.fins.Unit;
import org.avineas.fins.Transmitter;
import org.avineas.fins.payload.Command;
import org.avineas.fins.payload.Response;

/**
 * Helper class for the gateway units. This class is strongly related to the gateway 
 * functionality and is a bridge to the Unit interface that handles packets and
 * sends packets.
 * 
 * @author Arie van Wijngaarden
 * @since 6-11-2005
 */
class NodeUnit implements Transmitter {
    private Log logger = LogFactory.getLog(NodeUnit.class);
    private Address address;
    private List<Frame> receivedPackets;
    private Gateway gateway;
    private Unit unit;
    
    /**
     * Constructor called by the gateway to set this unit up.
     * 
     * @param addr The local node address
     * @param provider The gateway
     * @param unit The unit this object is wrapping
     */
    NodeUnit(Address addr, Gateway provider, Unit unit) {
        address = addr;
        this.gateway = provider;
        this.unit = unit;
        receivedPackets = new ArrayList<Frame>();
        unit.setTransmitter(this);
    }
    
    /**
     * Method that handles the receiving of a frame from a remote/local
     * fins node. The method unwraps the frame and delegates the actual
     * work to the handleCommand method of the unit assigned to this object
     * 
     * @param packet The packet as received
     * @return The reply packet, if any. Null will be returned if
     * the packet does not need a reply
     */
    Frame handleFrame(Frame packet) {
        if (packet.isReply()) {
            synchronized (receivedPackets) {
                receivedPackets.add(packet);
                receivedPackets.notifyAll();
            }
            return null;
        }
        byte[] payload = packet.getPayload();
        Command command = new Command(payload);
        logger.debug("unit: " + address + " received " + 
                command.getRequestCode() + " command from " + 
                packet.getSource() + ", " +
                payload.length + " bytes");
        Frame reply = null;
        try {
            Response response = unit.handleCommand(command);
            if (packet.mustReply() && response != null)
                reply = packet.constructReply(response.getBytes());
        } catch (Exception exc) {
            logger.error("exception during command handling", exc);
        }
        
        return reply;
    }

    /**
     * The transmitter method that actually transmits a command to a remote
     * node and waits for the result (synchronously). The timeout and retry times
     * are the values as set on the gateway.
     */
    @Override
    public Response sendPacket(Address to, Command command) throws IOException {
        byte[] payload = command.getBytes();
        Frame reply = null;
        int tries = gateway.getTries();
        long timeout = gateway.getTimeout();
        for (int trycnt = 0; reply == null && trycnt < tries; trycnt++) {
            // Construct the frame to send
            Frame frame = new Frame(address, to, payload);
            // Send the frame
            gateway.send(frame);
            // Just see how long we waited
            long started = System.currentTimeMillis();
            synchronized (receivedPackets) {
                for (;;) {
                    // How long is the remaining time to wait?
                    long toWait;
                    while (receivedPackets.size() == 0 && 
                      (toWait = started - System.currentTimeMillis() + timeout) > 0) {
                        try {
                            receivedPackets.wait(toWait);
                        }
                        catch (Exception exc) {}
                    }
                    // Did we actually get something?
                    if (receivedPackets.size() > 0) {
                        // Extract the reply and check if it matches
                        // the sent frame
                        reply = receivedPackets.get(0);
                        receivedPackets.remove(0);
                        if (reply.getSid() == frame.getSid() && 
                            reply.getSource().equals(frame.getDestination())) {
                            break;
                        }
                        logger.warn("spurious reply received from: " +
                                reply.getSource() + 
                                ", reply sid: " + reply.getSid() + ", sent sid: " +
                                frame.getSid());
                        reply = null;
                    }
                    else { // No, nothing. Means timeout.
                        logger.warn("unit: " + address + 
                                ", timeout sending " + command.getRequestCode() + 
                                " command to: " + to);
                        break;
                    }
                }
            }
        }
        if (reply == null) {
            logger.error("unit: " + address + 
                    " could not send " + command.getRequestCode() + 
                    " command to: " + to);
            return null;
        }
        logger.debug("unit: " + address + " successfully sent " + 
                command.getRequestCode() + " command to: " + to +
                ", " + payload.length + " bytes");
        return new Response(reply.getPayload());
    }
    
    /**
     * Close down this unit.
     */
    void close() {
        unit.setTransmitter(null);
    }
}