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
package org.apache.causeway.core.metamodel.postprocessors.all;

import javax.inject.Inject;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.annotprop.CssClassFacetOnActionFromConfiguredRegex;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.annotprop.FaFacetOnMemberFromConfiguredRegex;
import org.apache.causeway.core.metamodel.postprocessors.MetaModelPostProcessorAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

public class CssOnActionFromConfiguredRegexPostProcessor
extends MetaModelPostProcessorAbstract {

    @Inject
    public CssOnActionFromConfiguredRegexPostProcessor(final MetaModelContext mmc) {
        super(mmc);
    }

    @Override
    public void postProcessAction(final ObjectSpecification objectSpecification, final ObjectAction objectAction) {

        if(objectAction.isDeclaredOnMixin()) {
            return; // don't process mixin main method, instead process its peer
        }

        if(!objectAction.containsNonFallbackFacet(FaFacet.class)) {
            FacetUtil.addFacetIfPresent(
                FaFacetOnMemberFromConfiguredRegex
                    .create(objectSpecification, objectAction));
        }
        
        if(!objectAction.containsNonFallbackFacet(CssClassFacet.class)) {
            FacetUtil.addFacetIfPresent(
                CssClassFacetOnActionFromConfiguredRegex
                    .create(objectAction.getId(), objectAction));
        }
    }

}
