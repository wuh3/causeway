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

package org.apache.isis.core.metamodel.adapter.oid;

import org.apache.isis.applib.annotation.Value;


/**
 * An immutable identifier for either a root object (subtype {@link RootOid}) or 
 * an aggregated object (subtype {@link AggregatedOid}).
 * 
 * <p>
 * Note that value objects (strings, ints, {@link Value}s etc) do not have an {@link Oid}. 
 */
public interface Oid {

    /**
     * A string representation of this {@link Oid}.
     * 
     * <p>
     * Note that {@link RootOid} provide a reciprocal static <tt>deString</tt> method so that
     * they can be round-tripped.  However, this feature is not required or provided by
     * {@link AggregatedOid}.
     */
    String enString();

    /**
     * Flags whether this OID is for a transient (not-yet-persisted) object.
     * 
     * <p>
     * In the case of an {@link AggregatedOid}, is determined by the state 
     * of its {@link AggregatedOid#getParentOid() parent}'s {@link RootOid#isTransient() state}.
     */
    boolean isTransient();

    public static enum State {
        PERSISTENT, TRANSIENT;
        public boolean isTransient() {
            return this == TRANSIENT;
        }
    }

}
