/*
 * Copyright 2010-2011, aVineas IT Consulting
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
package org.avineas.fins.osgi;

import org.avineas.fins.Transmitter;
import org.avineas.fins.Unit;
import org.avineas.fins.payload.Command;
import org.avineas.fins.payload.Response;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Unit implementation that wraps a service reference to an other unit implementation.
 */
class ServiceUnit implements Unit {
    private Unit delegate;
    private ServiceReference reference;
    private BundleContext context;
    
    ServiceUnit(BundleContext context, ServiceReference reference, Unit unit) {
        this.context = context;
        this.reference = reference;
        this.delegate = unit;
    }
    
    @Override
    public Response handleCommand(Command command) throws Exception {
        return delegate.handleCommand(command);
    }

    @Override
    public void setTransmitter(Transmitter transmitter) {
        delegate.setTransmitter(transmitter);
        if (transmitter == null) {
            context.ungetService(reference);
        }
    }
}