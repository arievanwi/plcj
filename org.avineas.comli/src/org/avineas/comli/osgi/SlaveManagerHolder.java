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
package org.avineas.comli.osgi;

import org.avineas.comli.Slave;
import org.avineas.comli.impl.SlaveManager;
import org.avineas.comli.impl.SlaveProvider;
import org.avineas.io.Channel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Holder for slave managers. Takes care of tracking slaves for a specific
 * manager so that the slave manager can delegate to the slaves. This is done
 * via a service tracker that tracks the slaves (must have a property for
 * their identification)
 */
class SlaveManagerHolder implements SlaveProvider {
    private SlaveManager slaveManager;
    private ServiceTracker tracker;
    
    SlaveManagerHolder(BundleContext context, Channel channel, long timeout) {
        this.slaveManager = new SlaveManager(channel, timeout);
        this.slaveManager.setSlaves(this);
        tracker = new ServiceTracker(context, Slave.class.getName(), null);
        tracker.open();
    }
    
    public void close() {
        tracker.close();
        slaveManager.destroy();
    }

    @Override
    public Slave get(int identification) {
        ServiceReference[] references = tracker.getServiceReferences();
        if (references == null) return null;
        for (ServiceReference ref : references) {
            Object property = ref.getProperty("identification");
            if (property != null) {
                if (identification == Integer.parseInt(property.toString())) {
                    return (Slave) tracker.getService(ref);
                }
            }
        }
        return null;
    }
}
