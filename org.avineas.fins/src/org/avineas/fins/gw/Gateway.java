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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.avineas.fins.Address;
import org.avineas.fins.Unit;

/**
 * Class that implements the FINS gateway functionality. It takes care of forwarding
 * FINS messages to the right UDP node/port combination. The resolution of FINS nodes
 * to IP nodes is done using a pre-defined node map that should be configured at 
 * bootstrap time, but the gateway is learning itself from frames it receives from
 * remote nodes. <br/>
 * The local nodes should be configured at boot time as well.
 * <br/>
 * If debugging for this object is enabled, all datagram frames are printed to the
 * logger. If info is enabled, only the summary is logged
 * 
 * @author Arie van Wijngaarden
 * @since 6-11-2005
 */
public class Gateway {    
    private Log logger = LogFactory.getLog(Gateway.class);
    private DatagramSocket channel;
    private int tries;
    private long timeout;
    private Map<String, Destination> remoteNodes;
    private Map<String, NodeUnit> units;
    private Thread thread;
    private boolean runDown;

    public Gateway() {
        runDown = false;
        thread = null;
        tries = 3;
        timeout = 3000;
        units = new HashMap<String, NodeUnit>();
    }

    /*
     * Private method to add/set destination information: a map from a remote
     * FINS node to a IP/port combination.
     */
    private synchronized void setDestination(String finsNode, Destination dest) {
        if (remoteNodes == null) remoteNodes = new HashMap<String, Destination>();
        remoteNodes.put(new Address(finsNode).getNodeAsString(), dest);
    }
    
    private synchronized Destination getDestination(String finsNode) {
        if (remoteNodes == null) return null;
        return remoteNodes.get(finsNode);
    }
    
    /**
     * Method that is called during bootstrap to set the remote node definition. This
     * is the mapping from FINS nodes to IP/port combinations of the destination
     * gateways.
     * 
     * @param nodes A map with FINS node/UDP host/port combinations. Fins nodes
     * should be specified as string value "network/node" the UDP combination as
     * "ipaddress:port", like "0/1" -> "remotehost:9000"
     * @throws UnknownHostException In case the IP host is not known
     */
    public void setRemoteNodes(Map<String, String> nodes) throws UnknownHostException {
        Iterator<Map.Entry<String, String>> it = nodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String fins = entry.getKey();
            String inet = entry.getValue();
            setDestination(fins, new Destination(inet));
        }
    }

    /**
     * Add a unit to the unit set, meaning that it is registered to be working with this
     * gateway.
     * 
     * @param addr The address of the unit, should be in unit format like 12/3/1, indicating
     * the network, node and unit respectively
     * @param unit The unit to register with this address
     */
    public void addUnit(Address addr, Unit unit) {
        synchronized (units) {
            NodeUnit nodeUnit = new NodeUnit(addr, this, unit);
            units.put(addr.toString(), nodeUnit);
        }
    }

    /**
     * Remove a unit with a specific address.
     * 
     * @param addr The address to remove
     * @return An indication whether a unit was serving this address
     */
    public boolean removeUnit(Address addr) {
        synchronized (units) {
            NodeUnit unit = units.remove(addr.toString());
            if (unit != null) {
                unit.close();
            }
            return (unit != null);
        }
    }
    
    /**
     * Method that should be called at boot strap to set the units that are local
     * to this gateway. This map should contain a unit as "network/node/unit" string
     * and the value must be a valid Unit implementation. As an alternative, 
     * {@link #addUnit(Address, Unit)} can be used to add units at run-time.
     * 
     * @param unitMap The map with FINS unit address/Unit interface combinations
     */
    public void setUnits(Map<String, Unit> unitMap) {
        for (Map.Entry<String, Unit> entry : unitMap.entrySet()) {
            String address = entry.getKey();
            Unit unit = entry.getValue();
            addUnit(new Address(address), unit);
        }
    }
    
    /**
     * Set the local UDP port of this gateway.
     * 
     * @param port The UDP port number
     * @throws SocketException
     */
    public synchronized void setPort(int port) throws SocketException {
        if (channel != null) 
            channel.close();
        channel = new DatagramSocket(port);
    }
    
    private void traceDatagram(String prefix, DatagramPacket packet) {
        if (!logger.isDebugEnabled()) return;
        StringBuffer buffer = new StringBuffer(prefix).append(":");
        byte[] data = packet.getData();
        int offset = packet.getOffset();
        for (int cnt = 0; cnt < packet.getLength(); cnt++) {
            int thisByte = data[cnt + offset] & 0xff;
            buffer.append(" ").append(Integer.toHexString(thisByte));
        }
        logger.debug(buffer.toString());
    }
    
    /**
     * The after properties set method of this gateway. The method initializes
     * a new thread that continuously reads from the UDP port for getting data
     * for local (or remote) nodes. This method <b>must</b> be called before
     * actual receiving of data can take place.
     */
    @PostConstruct
    public void init() throws SocketException {
        if (thread != null) return;
        if (channel == null) setPort(9600);

        // Initialize the nodes and handling of the packets.
        thread = new Thread() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void run() {
                byte[] data = new byte[Frame.MAXFRAMESIZE];
                logger.info(Gateway.this + " started");
                for (;;) {
                    try {
                        // Read a datagram from the network
                        DatagramPacket dpacket = 
                            new DatagramPacket(data, 0, data.length); 
                        channel.receive(dpacket);
                        
                        // Update the FINS node/gateway information
                        Destination dest = new Destination(dpacket.getAddress(),
                                dpacket.getPort());
                        traceDatagram(dest + " -> " + channel.getLocalPort(), dpacket);
                        Frame packet = new Frame(data, dpacket.getLength());
                        Address from = packet.getSource();
                        setDestination(from.getNodeAsString(), dest);

                        // Handle the packet. It is either forwarded to
                        // a remote machine or locally handled. 
                        // Note that there is a possibility that gateways keep
                        // each other busy with sending data to each other. This
                        // cannot be prevented here.
                        Address to = packet.getDestination();
                        NodeUnit unit = units.get(to.toString());
                        if (unit != null) {
                            logger.info("received " + 
                                    (packet.isReply() ? "reply" : "packet") + 
                                    " frame from: " + dest + 
                                    ", for local unit: " + to + " from: " +
                                    packet.getSource());
                            Frame reply = unit.handleFrame(packet);
                            if (reply != null)
                                send(reply);
                        }
                        else {
                            logger.info("frame for node " + to + 
                                    " cannot be handled locally, trying forward");
                            send(packet);
                        }
                    }
                    catch (Exception exc) {
                        if (!runDown) {
                            synchronized (this) {
                                // Will normally only occur when the port is changed on the fly
                                logger.error("exception handling frame", exc);
                            }
                        }
                    }
                    // In case we were shut down, stop
                    if (runDown) break;
                }
                logger.info("FINS gateway shutdown complete");
            }
        };
        thread.start();
    }
     
    /**
     * Method called by the NodeUnit functionality to actually transmit a FINS frame
     * to the correct node.
     * 
     * @param packet The FINS packet to transmit
     * @throws IOException In case of sending failures
     */
    void send(Frame packet) throws IOException {
        // Try to find the destination, either local or remote
        NodeUnit unit = units.get(packet.getDestination().toString());
        // Is it a local node?
        if (unit != null) {
            // Handle the frame locally. This means that we call
            // handleFrame and if this one gives a reply, let that reply handle
            // by the initiator
            Frame response = unit.handleFrame(packet);
            if (response != null) {
               unit = units.get(packet.getSource().toString());
               unit.handleFrame(response);
            }
            return;
        }
        // It must be a remote node. Check where it must be sent to
        String node = packet.getDestination().getNodeAsString();
        Destination dest = getDestination(node);
        if (dest == null) {
            logger.warn("don't have an IP destination for node: " + node + 
                    ", dropping packet");
            return;
        }
        // OK, got everything correct. Send it to a remote IP/FINS node
        byte[] data = packet.getBytes();
        DatagramPacket dpacket = new DatagramPacket(data, data.length, 
                dest.getAddress(), dest.getPort());
        traceDatagram(channel.getLocalPort() + " -> " + dest, dpacket);
        channel.send(dpacket);
        logger.info("sent " + (packet.isReply() ? "reply" : "packet") + 
                " frame to destination: " +  dest + ", from unit: " + 
                packet.getSource() + " to unit: " + 
                packet.getDestination()); 
    }
    
    /**
     * Get the time-out time, in ms, in which replies should be received.
     * 
     * @return The time out value
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Set the timeout, in ms in which replies should be received back
     * from remote nodes.
     * 
     * @param timeout The timeout in ms
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Get the number of tries that take place during sending to an other node
     * before the message is considered to be un-deliverable.
     * 
     * @return The number of tries, defaults to 3
     */
    public int getTries() {
        return tries;
    }

    /**
     * Set the number of times a packet will be sent before it is considered as not
     * deliverable.
     * 
     * @param tries The number of times a packet will be sent and waited for
     */
    public void setTries(int tries) {
        this.tries = tries;
    }

    /**
     * Destroy method for shutting down the thread. Must be called on destruction of
     * this gateway.
     */
    @PreDestroy
    public void destroy() {
        logger.info(this + " shutdown initiated");
        runDown = true;
        channel.close();
        thread.interrupt();
        for (NodeUnit unit : units.values()) {
            unit.close();
        }
    }

    @Override
    public String toString() {
        return "FINS gateway on UDP port " + channel.getLocalPort();
    }
}