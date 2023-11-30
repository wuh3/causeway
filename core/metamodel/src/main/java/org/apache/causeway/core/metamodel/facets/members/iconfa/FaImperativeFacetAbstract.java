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
package org.apache.causeway.core.metamodel.facets.members.iconfa;

import java.util.function.BiConsumer;

import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

import lombok.Getter;

/**
 * One of two bases for the {@link FaFacet}.
 *
 * @see FaStaticFacetAbstract
 * @since 2.0
 */
public abstract class FaImperativeFacetAbstract
extends FacetAbstract
implements FaImperativeFacet {

    private static final Class<? extends Facet> type() {
        return FaFacet.class;
    }

    @Getter(onMethod_ = {@Override})
    private final Either<FaStaticFacet, FaImperativeFacet> specialization = Either.right(this);

    protected FaImperativeFacetAbstract(final FacetHolder holder) {
        super(type(), holder);
    }

    protected FaImperativeFacetAbstract(final FacetHolder holder, final Precedence precedence) {
        super(type(), holder, precedence);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("position", "!imperative");
        visitor.accept("classes", "!imperative");
    }

}
