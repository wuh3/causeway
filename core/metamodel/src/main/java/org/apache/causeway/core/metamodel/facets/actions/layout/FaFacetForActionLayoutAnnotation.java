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
package org.apache.causeway.core.metamodel.facets.actions.layout;

import java.util.Optional;

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.layout.component.CssClassFaPosition;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaStaticFacetAbstract;

public class FaFacetForActionLayoutAnnotation
extends FaStaticFacetAbstract {

    public static Optional<FaFacetForActionLayoutAnnotation> create(
            final Optional<ActionLayout> actionLayoutIfAny,
            final FacetHolder holder) {

        class Annot {
            private Annot(final ActionLayout actionLayout) {
                this.cssClassFa = _Strings.emptyToNull(actionLayout.cssClassFa());
                this.cssClassFaPosition = actionLayout.cssClassFaPosition();
            }
            String cssClassFa;
            CssClassFaPosition cssClassFaPosition;
        }

        return actionLayoutIfAny
                .map(Annot::new)
                .filter(a -> a.cssClassFa != null )
                .map(a -> new FaFacetForActionLayoutAnnotation(
                        a.cssClassFa, a.cssClassFaPosition, holder));
    }

    private FaFacetForActionLayoutAnnotation(
            final String value,
            final CssClassFaPosition position,
            final FacetHolder holder) {
        super(value, position, holder);
    }

}
