/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.runtimes.dflt.runtime.persistence;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.ServicesInjectorDefault;
import org.apache.isis.core.metamodel.services.container.DomainObjectContainerDefault;
import org.apache.isis.runtimes.dflt.runtime.persistence.adapterfactory.pojo.PojoAdapterFactory;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.ObjectFactory;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;

public final class PersistenceConstants {

    /**
     * Key used to lookup implementation of {@link ObjectAdapterFactory} in
     * {@link IsisConfiguration}.
     */
    public static final String ADAPTER_FACTORY_CLASS_NAME = ConfigurationConstants.ROOT + "persistor.adapter-factory";
    public static final String ADAPTER_FACTORY_CLASS_NAME_DEFAULT = PojoAdapterFactory.class.getName();

    /**
     * Key used to lookup implementation of {@link OidGenerator} in
     * {@link IsisConfiguration}.
     */
    public static final String OID_GENERATOR_CLASS_NAME = ConfigurationConstants.ROOT + "persistor.oid-generator";
    public static final String OID_GENERATOR_CLASS_NAME_DEFAULT = RootOidGenerator.class.getName();

    /**
     * Key used to lookup implementation of {@link ObjectFactory} in
     * {@link IsisConfiguration}.
     */
    public static final String OBJECT_FACTORY_CLASS_NAME = ConfigurationConstants.ROOT + "persistor.object-factory";
    public static final String OBJECT_FACTORY_CLASS_NAME_DEFAULT = "org.apache.isis.runtimes.dflt.bytecode.dflt.objectfactory.CglibObjectFactory";

    /**
     * Key used to lookup implementation of {@link ServicesInjector} in
     * {@link IsisConfiguration}.
     */
    public static final String SERVICES_INJECTOR_CLASS_NAME = ConfigurationConstants.ROOT + "persistor.services-injector";
    public static final String SERVICES_INJECTOR_CLASS_NAME_DEFAULT = ServicesInjectorDefault.class.getName();

    /**
     * Key used to lookup implementation of {@link DomainObjectContainer} in
     * {@link IsisConfiguration}.
     */
    public static final String DOMAIN_OBJECT_CONTAINER_CLASS_NAME = ConfigurationConstants.ROOT + "persistor.domain-object-container";
    public static final String DOMAIN_OBJECT_CONTAINER_NAME_DEFAULT = DomainObjectContainerDefault.class.getName();

    private PersistenceConstants() {
    }

}
