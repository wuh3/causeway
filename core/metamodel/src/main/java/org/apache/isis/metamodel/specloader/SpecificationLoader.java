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
package org.apache.isis.metamodel.specloader;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.metamodel.commons.ClassUtil;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.specimpl.IntrospectionState;

import lombok.val;

/**
 * Builds the meta-model, utilizing an instance of {@link ProgrammingModel}
 */
public interface SpecificationLoader {
    
    /**
     * Creates the meta-model, that is the set of {@link ObjectSpecification}s.
     * @see {@link #disposeMetaModel()}
     */
    void createMetaModel();
    
    /**
     * Clears all instance references to {@link ObjectSpecification}s.
     * @see {@link #createMetaModel()}
     */
    void disposeMetaModel();
    
    void validateMetaModel();

    // -- LOOKUP

    /**
     * @ThreadSafe
     * <p>
     *     Must be implemented thread-safe to avoid concurrent modification exceptions when the caller
     *     iterates over all the specifications and performs an activity that might give rise to new
     *     ObjectSpec's being discovered, eg. performing meta-model validation.
     * </p>
     * 
     * @return snapshot of all the (currently) loaded specifications, a defensive-copy 
     */
    Collection<ObjectSpecification> snapshotSpecifications();

    /**
     * Lookup a specification that has bean loaded before.
     * @param objectSpecId
     */
    ObjectSpecification lookupBySpecIdElseLoad(ObjectSpecId objectSpecId);

    void reloadSpecification(Class<?> domainType);

    /**
     * Return the specification for the specified class of object.
     *
     * <p>
     * It is possible for this method to return <tt>null</tt>, for example if
     * the configured {@link org.apache.isis.metamodel.specloader.classsubstitutor.ClassSubstitutor}
     * has filtered out the class.
     * 
     * @return {@code null} if {@code domainType==null}
     */
    ObjectSpecification loadSpecification(@Nullable Class<?> domainType, IntrospectionState upTo);

    // -- SHORTCUTS

    default ObjectSpecification loadSpecification(@Nullable final Class<?> domainType) {
        return loadSpecification(domainType, IntrospectionState.TYPE_INTROSPECTED);
    }

    default ObjectSpecification loadSpecification(
            @Nullable String className, 
            @Nullable IntrospectionState introspectionState) {

        if(_Strings.isNullOrEmpty(className)) {
            return null;
        }
        val type = ClassUtil.forNameElseFail(className);
        return introspectionState!=null 
                ? loadSpecification(type, introspectionState)
                        : loadSpecification(type);
    }

    default ObjectSpecification loadSpecification(@Nullable String className) {
        return loadSpecification(className, null);
    }

    




}
