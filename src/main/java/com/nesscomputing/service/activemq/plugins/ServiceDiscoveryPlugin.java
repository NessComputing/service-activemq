package com.nesscomputing.service.activemq.plugins;

import java.util.List;
import java.util.UUID;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.TransportConnector;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import com.nesscomputing.config.ConfigModule;
import com.nesscomputing.config.ConfigProvider;
import com.nesscomputing.jackson.NessJacksonModule;
import com.nesscomputing.jmx.JmxModule;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.ServiceDiscoveryLifecycle;
import com.nesscomputing.lifecycle.guice.LifecycleModule;
import com.nesscomputing.logging.Log;
import com.nesscomputing.service.discovery.client.DiscoveryClient;
import com.nesscomputing.service.discovery.client.DiscoveryClientModule;
import com.nesscomputing.service.discovery.client.ServiceInformation;

/**
 * Plugs into ActiveMQ and announces the available Transport connectors on the Ness service discovery.
 */
public class ServiceDiscoveryPlugin implements BrokerPlugin
{
    private static final Log LOG = Log.findLog();

    @Inject
    private DiscoveryClient discoveryClient;

    @Inject
    private Lifecycle lifecycle;

    @Inject
    private ServiceDiscoveryConfig serviceDiscoveryConfig;

    private final List<ServiceInformation> services = Lists.newArrayList();

    public ServiceDiscoveryPlugin()
    {
        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       new ConfigModule(),
                                                       new NessJacksonModule(),
                                                       new LifecycleModule(ServiceDiscoveryLifecycle.class),
                                                       new JmxModule(),
                                                       new DiscoveryClientModule(),
                                                       new AbstractModule() {

                                                        @Override
                                                        protected void configure() {
                                                            bind(ServiceDiscoveryConfig.class).toProvider(ConfigProvider.of(ServiceDiscoveryConfig.class)).in(Scopes.SINGLETON);
                                                        }
        });

        injector.injectMembers(this);

        LOG.info("Service discovery initialized");
    }

    @Override
    public Broker installPlugin(final Broker broker) throws Exception
    {
        return new ServiceDiscoveryBroker(broker);
    }

    public class ServiceDiscoveryBroker extends BrokerFilter
    {
        public ServiceDiscoveryBroker(final Broker next)
        {
            super(next);
        }

        private ServiceInformation getServiceInformation(final TransportConnector connector)
        {
            return new ServiceInformation(serviceDiscoveryConfig.getServiceName(),
                                          serviceDiscoveryConfig.getServiceType(),
                                          UUID.randomUUID(),
                                          ImmutableMap.of("scope", connector.getName(),
                                                          "uri", connector.getUri().toString()));
        }

        @Override
        public void start() throws Exception {
            final List<TransportConnector> connectors = getBrokerService().getTransportConnectors();
            services.clear();

            LOG.info("Found %d connectors", connectors.size());
            for (TransportConnector connector : connectors) {
                final String name = connector.getName();
                final ServiceInformation information = getServiceInformation(connector);
                if (information != null) {
                    LOG.info("Found %s with %s", name, information);
                    if ("internal".equals(name) && serviceDiscoveryConfig.isAnnounceInternal()) {
                        services.add(information);
                    }
                    if ("external".equals(name) && serviceDiscoveryConfig.isAnnounceExternal()) {
                        services.add(information);
                    }
                }
            }

            lifecycle.executeTo(LifecycleStage.ANNOUNCE_STAGE);

            super.start();

            for (ServiceInformation serviceInformation : services) {
                discoveryClient.announce(serviceInformation);
            }

            LOG.info("Service announced!");
        }

        @Override
        public void stop() throws Exception {

            for (ServiceInformation serviceInformation : services) {
                discoveryClient.unannounce(serviceInformation);
                LOG.info("Unannounced %s", serviceInformation);
            }

            super.stop();

            // SD needs a couple of ms to pick up the status change and
            // update the service.
            try {
                Thread.sleep(500L);
            }
            catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            lifecycle.executeTo(LifecycleStage.STOP_STAGE);

            LOG.info("Service unannounced!");
        }
    }
}
