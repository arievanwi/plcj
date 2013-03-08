/*
 * Copyright 2010-2012, aVineas IT Consulting
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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * OSGi bundle activator for this bundle.
 * 
 * @author Arie van Wijngaarden
 */
public class Activator implements BundleActivator {
    private ServiceTracker masterTracker;
    private ServiceTracker slaveTracker;
    
    @Override
    public void start(BundleContext context) throws Exception {
        // Start tracking the channel for the comli slave and master channels.
        slaveTracker = new SlaveTracker(context);
        masterTracker = new MasterTracker(context);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        slaveTracker.close();
        masterTracker.close();
    }
}