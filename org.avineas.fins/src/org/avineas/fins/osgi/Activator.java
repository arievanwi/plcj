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

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.avineas.fins.Address;
import org.avineas.fins.Unit;
import org.avineas.fins.gw.Gateway;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

/**
 * Bundle activator for OSGi based environment usage of the FINS bundle. When this
 * bundle is started, it automatically starts a gateway for this machine on the default
 * UDP port and handles automatically any units defined within the OSGi framework by tracking
 * services. The activator looks for configuration data at PID fins.gateway. The following properties
 * are accepted:
 * <ul>
 * <li><i>port</i>. If specified this is the UDP port the gateway will listen on. If blank, the gateway
 * is shut down (or doesn't come up).</li>
 * <li><i>remote.nodes</i>. Contains a string with the map of FINS nodes to remote machine. Format is
 * something like: 1/2=localhost:9001 2/4=remotenode:9002 (first part is the FINS network/node,
 * second part the host and port, separated by an equals sign, multiple values are space separated).
 * </li>
 * <li><i>timeout</i>. Contains the time-out in ms the gateway waits for responses from other nodes.
 * Defaults to 3000 ms.</li>
 * <li><i>tries</i>. Contains the number of times a packet is sent before it is considered to be
 * undeliverable. Defaults to 3.</li>
 * </ul>
 */
public class Activator implements BundleActivator {
    private Log logger = LogFactory.getLog(Activator.class);
    // Our identifier
    private static final String PID = "fins.gateway";
    // The port configuration item
    private static final String PORT = "port";
    // The remote nodes configuration item
    private static final String NODES = "remote.nodes";
    // The tries
    private static final String TRIES = "tries";
    // The timeout
    private static final String TIMEOUT = "timeout";
    private static final String UNITADDRESS = "fins.unit.address";
    private BundleContext context;
    private ServiceListener listener;
	private Gateway gateway;

   /**
     * Register a new service unit.
     * 
     * @param address The address where the unit communicates on
     * @param reference The service reference
     */
    private void registerUnit(Address addr, ServiceReference reference) {
        // Get the service object.
        Unit unit = (Unit) context.getService(reference);
        if (unit == null) return;   // Invalid object
        ServiceUnit toRegister = new ServiceUnit(context, reference, unit);
        gateway.addUnit(addr, toRegister);
        logger.info(unit + " on " + addr + " connected to " + gateway);
    }

    /**
     * Unregister a unit. The unit is removed from the gateway.
     * 
     * @param address The address on which the service handles actions
     */
    private void unregisterUnit(Address address) {
        if (gateway.removeUnit(address)) {
            logger.info("unit on " + address + " removed from " + gateway);
        }
    }
    
    /**
     * Stop tracking unit services. This method is called when the gateway is destroyed.
     */
	private void untrackServices() {
	    if (listener != null) {
	        context.removeServiceListener(listener);
	        listener = null;
	    }
	}
	
	/**
	 * Track the unit services that come and go in this environment. A service listener is
	 * registered to follow units that come and go. Furthermore, the units that may already be 
	 * present are registered directly with the gateway. 
	 */
	private void trackServices() {
        // Start tracking units.
        listener = new ServiceListener() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void serviceChanged(ServiceEvent event) {
                ServiceReference reference = event.getServiceReference();
                Object addr = reference.getProperty(UNITADDRESS);
                // If the address is not defined on the unit, log a message and
                // return
                if (addr == null) {
                    logger.error(reference + " doesn't have a " + UNITADDRESS + " property, ignored");
                    return;
                }
                Address address = new Address(addr.toString());
                switch (event.getType()) {
                case ServiceEvent.REGISTERED:
                    unregisterUnit(address);  // Just in case.
                    registerUnit(address, reference);
                    break;
                case ServiceEvent.UNREGISTERING:
                    unregisterUnit(address);
                    break;
                }
            }
        };
        try {
            String filter = "(" + Constants.OBJECTCLASS + "=" + Unit.class.getName() + ")";
            context.addServiceListener(listener, filter);
            ServiceReference[] references = context.getServiceReferences(null, filter);
            if (references != null) {
                for (ServiceReference reference : references) {
                    listener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, reference));
                }
            }
        } catch (Exception exc) {
            logger.error("problem occurred while performing a first fetch on all units", exc);
        }
	}
	
	/**
	 * Create a gateway given the passed properties. If a gateway is already active, the original
	 * is destroyed first.
	 * 
	 * @param d The properties of the dictionary
	 * @throws ConfigurationException In case of problems
	 */
	@SuppressWarnings("rawtypes")
    private synchronized void createGateway(Dictionary d) throws ConfigurationException {
	    Dictionary dict = d;
	    if (dict == null) dict = new Hashtable();
	    // Destroy the gateway after un-tracking the services.
	    if (gateway != null) {
	        untrackServices();
	        gateway.destroy();
	    }
	    // Check the port, if it is an empty string, no gateway should be started.
	    String port = (String) dict.get(PORT);
	    if (port != null && port.trim().length() == 0) return;
        gateway = new Gateway();
        try {
            if (port != null)
                gateway.setPort(Integer.parseInt(port));
        } catch (Exception exc) {
            throw new ConfigurationException(PORT, exc.getMessage(), exc);
        }
        try {
            String tries = (String) dict.get(TRIES);
            if (tries != null) {
                gateway.setTries(Integer.parseInt(tries));
            }
        } catch (Exception exc) {
            throw new ConfigurationException(TRIES, exc.getMessage(), exc);
        }
        try {
            String timeout = (String) dict.get(TIMEOUT);
            if (timeout != null) {
                gateway.setTimeout(Integer.parseInt(timeout));
            }
        } catch (Exception exc) {
            throw new ConfigurationException(TIMEOUT, exc.getMessage(), exc);
        }
        String nodeMap = (String) dict.get(NODES);
        if (nodeMap != null) {
            String[] splitted = nodeMap.split("\\s+");
            Map<String, String> remoteNodes = new HashMap<String, String>();
            for (String entry : splitted) {
                String[] keyValue = entry.split("=");
                remoteNodes.put(keyValue[0].trim(), keyValue[1].trim());
            }
            try {
                gateway.setRemoteNodes(remoteNodes);
            } catch (Exception exc) {
                throw new ConfigurationException(NODES, exc.getMessage(), exc);
            }
        }
        try {
            gateway.init();
        } catch (Exception exc) {
            logger.error("gateway " + gateway + " cannot be initialized");
        }
        // Start tracking the unit services.
        trackServices();
	}
	
	/**
	 * Start the bundle. During start, this bundle registers a managed service to listen
	 * for configuration updates on the gateway to start. If no such update is received during
	 * registration (indicating that no configuration admin is present), a gateway is constructed
	 * with default properties.
	 */
	@Override
	public void start(BundleContext context) throws Exception {
	    this.context = context;
	    // Register ourself as managed service.
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(Constants.SERVICE_PID, PID);
        context.registerService(ManagedService.class.getName(), 
            new ManagedService() {
                @SuppressWarnings({ "rawtypes", "synthetic-access" })
                @Override
                public void updated(Dictionary dict)
                        throws ConfigurationException {
                    createGateway(dict);
                }
        }, properties);
        // Check if there is a configuration admin service, if not just create a gateway.
        // Otherwise we will sooner or later get an update.
        if (context.getServiceReference(ConfigurationAdmin.class.getName()) == null) {
            createGateway(null);
        }
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	    if (gateway != null) {
	        untrackServices();
	        gateway.destroy();
	    }
	}
}