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
package org.apache.causeway.extensions.pdfjs.metamodel;

import org.apache.causeway.extensions.pdfjs.metamodel.domains.mixin.MixinDomain;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = {PdfjsViewer_MixinDomain_IntegTest.AppManifest.class},
        properties = {
                "causeway.core.meta-model.introspector.mode=FULL",
        }
)
@ActiveProfiles("test")
public class PdfjsViewer_MixinDomain_IntegTest extends PdfjsViewer_Abstract_IntegTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            AppManifestBase.class,
            MixinDomain.class,
    })
    @ComponentScan(basePackageClasses = {MixinDomain.class})
    public static class AppManifest {
    }

    @Override
    public Class<?> getDomainModuleClass() {
        return MixinDomain.class;
    }

    @Test
    void dump_facets() {
        super.dump_facets();
    }
}
