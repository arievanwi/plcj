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

import java.io.IOException;

import org.avineas.fins.payload.Command;
import org.avineas.fins.payload.Response;

/**
 * Interface that abstracts away the way how FINS units can send commands to remote
 * or local FINS units.
 * 
 * @author Arie van Wijngaarden
 * @since 6-11-2005
 */
public interface Transmitter {
    /**
     * Send a command to an other FINS unit and receive the response. This method can be used
     * by {@link Unit} implementations to communicate with other FINS units. It automatically takes
     * care of local or remote delivery of the command and waits for the response on this command
     * for a specific period of time. The response is returned to the caller.
     * 
     * @param to The destination fins address to send the command to
     * @param command The command that must be sent
     * @return The response on the command or null if nothing was received at
     * all
     * @throws IOException In case of severe errors
     */
    public Response sendPacket(Address to, Command command) throws IOException;
}