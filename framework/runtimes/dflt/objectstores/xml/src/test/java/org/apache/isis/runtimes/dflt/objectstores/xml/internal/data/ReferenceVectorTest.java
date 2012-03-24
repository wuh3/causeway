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

package org.apache.isis.runtimes.dflt.objectstores.xml.internal.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidDefault;

public class ReferenceVectorTest {
    
    private final String objectType = "FOO";
    
    private ReferenceVector referenceVector;
    private RootOidDefault oid;

    @Before
    public void setUp() throws Exception {
        oid = RootOidDefault.createTransient(objectType, ""+1);
        referenceVector = new ReferenceVector();
    }

    @Test
    public void validatesSerialOidIsStoredInElements() throws Exception {
        referenceVector.add(oid);
        assertTrue(referenceVector.elementAt(0).equals(oid));
    }

    @Test
    public void validatesSerialOidIsRemovedInElements() throws Exception {
        referenceVector.add(oid);
        referenceVector.remove(oid);
        assertTrue(referenceVector.size() == 0);
    }

    @Test
    public void validatesReferenceVectorIsEqual() throws Exception {
        assertTrue(referenceVector.equals(referenceVector));
        assertTrue(referenceVector.equals(new ReferenceVector()));
        assertFalse(referenceVector.equals(new Object()));
    }

    @Test
    public void validateReferenceVectorHashCode() throws Exception {
        assertTrue(referenceVector.hashCode() == 630);
    }

    @Test
    public void validateReferenceToString() throws Exception {
        assertTrue(referenceVector.toString() != null);
    }

}
