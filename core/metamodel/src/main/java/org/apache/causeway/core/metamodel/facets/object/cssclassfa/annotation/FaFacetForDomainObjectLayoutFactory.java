/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.causeway.core.metamodel.facets.object.cssclassfa.annotation;

import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.layout.component.CssClassFaPosition;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaStaticFacetAbstract;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.FaFacetForDomainObjectLayoutAnnotation;

public class FaFacetForDomainObjectLayoutFactory
extends FaStaticFacetAbstract {

    public static FaFacet create(
            final DomainObjectLayout domainObjectLayout,
            final FacetHolder holder) {

        if (domainObjectLayout == null) {
            return null;
        }

        final String cssClassFa = _Strings.emptyToNull(domainObjectLayout.cssClassFa());
        final CssClassFaPosition position = domainObjectLayout.cssClassFaPosition();

        return cssClassFa != null
                ? new FaFacetForDomainObjectLayoutAnnotation(cssClassFa, position, holder)
                : null;
    }

    private FaFacetForDomainObjectLayoutFactory(
            final String value,
            final CssClassFaPosition position, //NOSONAR false positive: method is used in create()
            final FacetHolder holder) {

        super(value, position, holder);
    }
}
