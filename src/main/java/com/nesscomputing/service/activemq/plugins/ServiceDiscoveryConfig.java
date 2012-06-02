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
