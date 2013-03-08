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
package org.avineas.fins;

import org.avineas.fins.payload.Command;
import org.avineas.fins.payload.Response;

/**
 * Interface definition that a FINS unit must implement to receive and communicate
 * FINS commands. Unit implementations are wired to a gateway, which must be done via dependency 
 * injection or manual construction. Additionally, in OSGi environments, it is
 * sufficient to register the unit as service implementing this interface: it will be automatically
 * picked-up by the default gateway object constructed in those environments. 
 * 
 * @author Arie van Wijngaarden
 * @since 6-11-2005
 */
public interface Unit {
    /**
     * Handle a packet/command received from another FINS unit. It should process it and
     * construct a response and return it. Note that this method may be called at any
     * time and in a multi-threaded way. The method should be aware that it is possible that,
     * given the used network protocol, a command may be delivered multiple times, even if a correct
     * response was passed back (the response may be lost in transit).
     * 
     * @param command The command that is issued from the remote node
     * @return The response as constructed
     * @throws Exception In case of severe errors, the method may throw an exception. In that
     * case the gateway does not send a reply to the unit expecting it
     */
    public Response handleCommand(Command command) throws Exception;
    
    /**
     * Set the transmitter to use for sending commands to other FINS units. The method is called 
     * during construction/initialization of the gateway it is connected to to set the transmitter
     * that can be used to send messages to another unit. The method is called both to <b>set</b>
     * the transmitter and to <b>reset</b> the transmitter: a null value is passed to reset the
     * transmitter, making this unit basically an orphan that needs to be wired or picked-up by
     * another gateway.
     * 
     * @param transmitter The transmitter to use for sending messages. The transmitter passed
     * may be null. In case of a null transmitter, any previously set not-null transmitter must not 
     * be used anymore for communication since it is destroyed
     */
    public void setTransmitter(Transmitter transmitter);
}