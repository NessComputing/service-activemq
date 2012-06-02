/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nesscomputing.service.activemq.plugins;

import org.skife.config.Config;
import org.skife.config.Default;
import org.skife.config.DefaultNull;

public abstract class ServiceDiscoveryConfig
{
    /**
     * The service name to announce. Defaults to activemq.
     */
    @Config("ness.announce.service-name")
    @Default("activemq")
    public String getServiceName()
    {
        return null;
    }

    /**
     * The service type to announce. Can be empty.
     */
    @Config("ness.announce.service-type")
    @DefaultNull
    public String getServiceType()
    {
        return null;
    }

    /**
     * Announce the internal transport connector if configured.
     *
     * This is enabled by default.
     */
    @Config("ness.announce.internal")
    @Default("true")
    public boolean isAnnounceInternal()
    {
        return true;
    }

    /**
     * Announce the external transport connector if configured.
     *
     * This is disabled and should only be enabled for testing/debugging.
     */
    @Config("ness.announce.external")
    @Default("false")
    public boolean isAnnounceExternal()
    {
        return false;
    }
}
