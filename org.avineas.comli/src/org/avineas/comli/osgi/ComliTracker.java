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
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.avineas.io.Channel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Service tracker for the various COMLI related objects. Provides some
 * base information/construction of objects and handling.
 */
abstract class ComliTracker<T> extends ServiceTracker {
    protected final Log logger = LogFactory.getLog(getClass());
    protected static final String COMLIPROPERTY = "comli";
    
    /**
     * Constructor that constructs a tracker for any channel interfaces with
     * some extra filtering.
     * 
     * @param context The bundle context
     * @param extra The extra filter
     * @throws Exception In case of invalid filter criteria
     */
    ComliTracker(BundleContext context, String extra) throws Exception {
        super(context, context.createFilter("(&(" + Constants.OBJECTCLASS + "=" + 
              Channel.class.getName() + ")(" + extra + "))"), null);
        open();
    }

    /**
     * Method subclasses must override to create the correct object to tracker
     * by this tracker. This must be an object of the specified generic type. 
     * 
     * @param context The bundle context that is used for creating this tracker
     * @param props The original properties of channel service
     * @param channel The channel itself
     * @param timeout The timeout as specific as property on the channel service.
     * Is here for convenience
     * @return The object created
     */
    protected abstract T getObject(BundleContext context, 
            Dictionary<String, Object> props, Channel channel, long timeout);
    
    @Override
    public Object addingService(ServiceReference sr) {
        Object value = sr.getProperty("timeout");
        int timeout = 3000;
        try {
            if (value != null) {
                timeout = Integer.parseInt(value.toString());
            }
        } catch (Exception exc) {}
        // Copy all other properties
        Hashtable<String, Object> dict = new Hashtable<String, Object>();
        String[] keys = sr.getPropertyKeys();
        if (keys != null) {
            for (String key : keys) {
                dict.put(key, sr.getProperty(key));
            }
        }
        Channel channel = (Channel) context.getService(sr);
        if (channel == null) return null;
        logger.info("Handling started for " + sr);
        return getObject(context, dict, channel, timeout);
    }

    /**
     * Method that is called when an object is moved out of scope. It should
     * take care of cleaning up the object.
     * 
     * @param context The bundle context of this tracker
     * @param object The object to destroy
     */
    protected abstract void destroy(BundleContext context, T object);
    
    @Override
    public void removedService(ServiceReference sr, Object obj) {
        @SuppressWarnings("unchecked")
        T object = (T) obj;
        destroy(context, object);
        context.ungetService(sr);
        logger.info("Handling stopped for " + sr);
    }
}