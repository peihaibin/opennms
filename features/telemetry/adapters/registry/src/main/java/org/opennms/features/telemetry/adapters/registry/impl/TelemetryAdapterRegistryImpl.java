/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.telemetry.adapters.registry.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.core.soa.lookup.ServiceLookupBuilder;
import org.opennms.features.telemetry.adapters.registry.api.TelemetryAdapterRegistry;
import org.opennms.netmgt.telemetry.adapters.api.Adapter;
import org.opennms.netmgt.telemetry.adapters.api.AdapterFactory;
import org.opennms.netmgt.telemetry.config.api.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * Maintains the list of available telemtryd adapters, aggregating
 * those expose the the service loader and via the OSGi registry.
 *
 * @author chandrag
 * @author jwhite
 */
public class TelemetryAdapterRegistryImpl implements TelemetryAdapterRegistry, ServiceLookup<String, Void> {

    private static final Logger LOG = LoggerFactory.getLogger(TelemetryAdapterRegistry.class);

    private static final ServiceLoader<AdapterFactory> s_adapterFactoryLoader = ServiceLoader.load(AdapterFactory.class);

    protected static final String TYPE = "type";

    private final Map<String, AdapterFactoryRegistration> m_adapterFactoryByClassName = new HashMap<>();
    private final ServiceLookup<String, Void> delegate;

    @Autowired
    private ApplicationContext applicationContext;

    public TelemetryAdapterRegistryImpl() {
        this(ServiceLookupBuilder.GRACE_PERIOD_MS, ServiceLookupBuilder.WAIT_PERIOD_MS, ServiceLookupBuilder.LOOKUP_DELAY_MS);
    }

    public TelemetryAdapterRegistryImpl(long gracePeriodMs, long waitPeriodMs, long lookupDelayMs) {
        this.delegate = new ServiceLookupBuilder(new ServiceLookup<String, Void>() {
            @Override
            public <T> T lookup(String criteria, Void filter) {
                return (T) m_adapterFactoryByClassName.get(criteria);
            }
        }).blocking(gracePeriodMs, lookupDelayMs, waitPeriodMs).build();

        // Register all of the factories exposed via the service loader
        for (AdapterFactory adapterFactory : s_adapterFactoryLoader) {
            final String className = adapterFactory.getAdapterClass().getCanonicalName();
            m_adapterFactoryByClassName.put(className, new AdapterFactoryRegistration(className, adapterFactory, true));
        }
    }

    @SuppressWarnings("rawtypes")
    public synchronized void onBind(AdapterFactory adapterFactory, Map properties) {
        LOG.debug("Bind called with {}: {}", adapterFactory, properties);
        if (adapterFactory != null) {
            final String className = getClassName(properties);
            if (className == null) {
                LOG.warn("Unable to determine the class name for AdapterFactory: {}, with properties: {}. The adapter will not be registered.",
                        adapterFactory, properties);
                return;
            }
            m_adapterFactoryByClassName.put(className, new AdapterFactoryRegistration(className, adapterFactory, false));
        }
    }

    @SuppressWarnings("rawtypes")
    public synchronized void onUnbind(AdapterFactory adapterFactory, Map properties) {
        LOG.debug("Unbind called with {}: {}", adapterFactory, properties);
        if (adapterFactory != null) {
            final String className = getClassName(properties);
            if (className == null) {
                LOG.warn("Unable to determine the class name for AdapterFactory: {}, with properties: {}. The adapter will not be unregistered.",
                        adapterFactory, properties);
                return;
            }
            m_adapterFactoryByClassName.remove(className);
        }
    }

    @Override
    public Adapter getAdapter(String className, Protocol protocol, Map<String, String> properties) {
        final AdapterFactoryRegistration registration = delegate.lookup(className, null);
        if (registration != null) {
            final Adapter adapter = registration.getAdapterFactory().createAdapter(protocol, properties);
            if (registration.shouldAutowire()) {
                // Autowire!
                final AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
                beanFactory.autowireBean(adapter);
                beanFactory.initializeBean(adapter, "adapter");
            }
            return adapter;
        }

        return null;
    }

    private static String getClassName(Map<?, ?> properties) {
        final Object type = properties.get(TYPE);
        if (type != null && type instanceof String) {
            return (String) type;
        }
        return null;
    }

    @Override
    public <T> T lookup(String criteria, Void filter) {
        return delegate.lookup(criteria, filter);
    }

    private static class AdapterFactoryRegistration {
        private final String clazz;
        private final AdapterFactory factory;
        private final boolean autowire;

        public AdapterFactoryRegistration(String clazz, AdapterFactory factory, boolean autowire) {
            this.clazz = Objects.requireNonNull(clazz);
            this.factory = Objects.requireNonNull(factory);
            this.autowire = autowire;
        }

        public String getAdapterClass() {
            return clazz;
        }

        public AdapterFactory getAdapterFactory() {
            return factory;
        }

        public boolean shouldAutowire() {
            return autowire;
        }
    }
}
