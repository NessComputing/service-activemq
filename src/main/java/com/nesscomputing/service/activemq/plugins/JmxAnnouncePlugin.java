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

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.BrokerPlugin;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.nesscomputing.config.Config;
import com.nesscomputing.config.ConfigModule;
import com.nesscomputing.galaxy.GalaxyConfigModule;
import com.nesscomputing.jmx.JmxModule;
import com.nesscomputing.jmx.starter.guice.JmxStarterModule;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.LifecycleModule;
import com.nesscomputing.logging.Log;

/**
 * JMX Export, Ness style.
 */
public class JmxAnnouncePlugin implements BrokerPlugin
{
    private static final Log LOG = Log.findLog();

    @Inject
    private Lifecycle lifecycle;

    public JmxAnnouncePlugin()
    {
        final Config config = Config.getConfig();
        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       new ConfigModule(config),
                                                       new LifecycleModule(),
                                                       new GalaxyConfigModule(),
                                                       new JmxStarterModule(config),
                                                       new JmxModule(),
                                                       new Module() {
                                                           @Override
                                                           public void configure(final Binder binder) {
                                                               binder.requireExplicitBindings();
                                                               binder.disableCircularProxies();
                                                           }
                                                       });

        injector.injectMembers(this);

        LOG.info("JMX Exporter initialized");
    }

    @Override
    public Broker installPlugin(final Broker broker) throws Exception
    {
        return new JmxAnnounceBroker(broker);
    }

    public class JmxAnnounceBroker extends BrokerFilter
    {
        public JmxAnnounceBroker(final Broker next)
        {
            super(next);
        }

        @Override
        public void start() throws Exception
        {
            lifecycle.executeTo(LifecycleStage.START_STAGE);

            super.start();

            LOG.info("Exported Broker via JMX!");
        }

        @Override
        public void stop() throws Exception
        {
            super.stop();

            lifecycle.executeTo(LifecycleStage.STOP_STAGE);

            LOG.info("Unexported Broker via JMX!");
        }
    }
}
