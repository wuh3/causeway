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
package org.apache.causeway.core.metamodel.facets.members.publish.execution;

import java.util.Optional;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.commons.internal.base._Optionals;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.metamodel.facets.ActionConfigOptions;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;

import lombok.val;

public abstract class ExecutionPublishingFacetForActionAnnotation extends ExecutionPublishingFacetAbstract {

    static class Enabled extends ExecutionPublishingFacetForActionAnnotation {
        Enabled(FacetHolder holder) {
            super(holder);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    static class Disabled extends ExecutionPublishingFacetForActionAnnotation {
        Disabled(FacetHolder holder) {
            super(holder);
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    public static Optional<ExecutionPublishingFacet> create(
            final Optional<Action> actionsIfAny,
            final CausewayConfiguration configuration,
            final FacetHolder holder) {

        val publishingPolicy = ActionConfigOptions.actionExecutionPublishingPolicy(configuration);

        return _Optionals.orNullable(

        actionsIfAny
        .map(Action::executionPublishing)
        .filter(publishing -> publishing != Publishing.NOT_SPECIFIED)
        .<ExecutionPublishingFacet>map(publishing -> {
            switch (publishing) {
                case AS_CONFIGURED:
                    switch (publishingPolicy) {
                        case NONE:
                            return new ExecutionPublishingFacetForActionAnnotationAsConfigured.None(holder);
                        case IGNORE_QUERY_ONLY:
                        case IGNORE_SAFE:
                            return hasSafeSemantics(holder)
                                    ? new ExecutionPublishingFacetForActionAnnotationAsConfigured.IgnoreSafe(holder)
                                    : new ExecutionPublishingFacetForActionAnnotationAsConfigured.IgnoreSafeYetNot(holder);
                        case ALL:
                            return new ExecutionPublishingFacetForActionAnnotationAsConfigured.All(holder);
                        default:
                            throw new IllegalStateException(String.format("configured executionPublishing policy '%s' not recognised", publishingPolicy));
                        }
                case DISABLED:
                    return new ExecutionPublishingFacetForActionAnnotation.Disabled(holder);
                case ENABLED:
                    return new ExecutionPublishingFacetForActionAnnotation.Enabled(holder);
                default:
                    throw new IllegalStateException(String.format("executionPublishing '%s' not recognised", publishing));
            }
        })
        ,
        () -> {
            // if not specified
            switch (publishingPolicy) {
                case NONE:
                    return new ExecutionPublishingFacetForActionFromConfiguration.None(holder);
                case IGNORE_QUERY_ONLY:
                case IGNORE_SAFE:
                    return hasSafeSemantics(holder)
                            ? new ExecutionPublishingFacetForActionFromConfiguration.IgnoreSafe(holder)
                            : new ExecutionPublishingFacetForActionFromConfiguration.IgnoreSafeYetNot(holder);
                case ALL:
                    return new ExecutionPublishingFacetForActionFromConfiguration.All(holder);
                default:
                    throw new IllegalStateException(String.format("configured executionPublishing policy '%s' not recognised", publishingPolicy));
            }
        });
    }

    private static boolean hasSafeSemantics(final FacetHolder holder) {
        final ActionSemanticsFacet actionSemanticsFacet = holder.getFacet(ActionSemanticsFacet.class);
        if(actionSemanticsFacet == null) {
            throw new IllegalStateException("Require ActionSemanticsFacet in order to process");
        }
        return actionSemanticsFacet.value().isSafeInNature();
    }

    ExecutionPublishingFacetForActionAnnotation(final FacetHolder holder) {
        super(holder);
    }

}
