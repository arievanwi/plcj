/*
 * Copyright 2010-2012 aVineas IT Consulting
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

import java.util.HashMap;
import java.util.Map;

import org.avineas.comli.Slave;

/**
 * Simple implementation of a slave provider. Just maintains a map with
 * key/value pairs.
 * 
 * @author Arie van Wijngaarden
 */
public class SimpleSlaveProvider implements SlaveProvider {
    private Map<Integer, Slave> slaves = new HashMap<Integer, Slave>();
    
    /**
     * Set the slaves to be managed. Must be a map with as key the
     * integer value of the slave and as value a slave implementation.
     * The manager will take care of dispatching received messages from
     * the master to the correct slave. Note that this method should only
     * be used for static wiring, in an OSGi environment slaves are dynamically
     * added based on the declaration of slave instances.
     * 
     * @param slaves The slaves map
     */
    public void setSlaves(Map<Integer, Slave> slaves) {
        this.slaves = slaves;
    }

    @Override
    public Slave get(int identification) {
        return slaves.get(identification);
    }
}
