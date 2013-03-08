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

import org.avineas.io.Channel;
import org.osgi.framework.BundleContext;

/**
 * Slave tracker: tracks slave channels and automatically creates 
 * slave managers for those channels and exports them.
 */
class SlaveTracker extends ComliTracker<SlaveManagerHolder> {
    
    public SlaveTracker(BundleContext context) throws Exception {
        super(context, COMLIPROPERTY + "=slave");
    }

    @Override
    protected SlaveManagerHolder getObject(BundleContext context, 
            Dictionary<String, Object> props, Channel channel,
            long timeout) {
        SlaveManagerHolder manager = new SlaveManagerHolder(context, channel, timeout);
        return manager;
    }

    @Override
    protected void destroy(BundleContext context, SlaveManagerHolder object) {
        object.close();
    }
}