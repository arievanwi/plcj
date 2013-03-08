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

import java.util.Dictionary;

import org.avineas.comli.Master;
import org.avineas.comli.impl.MasterImpl;
import org.avineas.io.Channel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Master tracker: tracks master channels and creates a comli master for
 * them. The properties of the channel are copied one-to-one to the 
 * master.
 * 
 * @author Arie van Wijngaarden
 */
class MasterTracker extends ComliTracker<ServiceRegistration> {
    
    public MasterTracker(BundleContext context) throws Exception {
        super(context, COMLIPROPERTY + "=master");
    }
    
    @Override
    protected ServiceRegistration getObject(BundleContext context,
            Dictionary<String, Object> properties,
            Channel channel, long timeout) {
        // Construct the master.
        MasterImpl master = new MasterImpl(channel, timeout);
        // See if the tries are overwritten. If so, set the tries property
        Object tries = properties.get("tries");
        try {
            if (tries != null) {
                master.setTries(Integer.parseInt(tries.toString()));
            }
        } catch (Exception exc) {}
        ServiceRegistration reg = 
                context.registerService(Master.class.getName(), master, properties);
        return reg;
    }

    @Override
    protected void destroy(BundleContext context, ServiceRegistration object) {
        object.unregister();
    }
}