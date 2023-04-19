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
package org.apache.causeway.core.metamodel.facets.collections.collection;

import java.util.List;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.CollectionSemantics;
import org.apache.causeway.core.metamodel.commons.matchers.CausewayMatchers;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactory;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacetFromFeature;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.facets.collections.collection.hidden.HiddenFacetForCollectionAnnotation;
import org.apache.causeway.core.metamodel.facets.collections.collection.typeof.TypeOfFacetForCollectionAnnotation;

import lombok.val;

@SuppressWarnings("removal")
class CollectionAnnotationFacetFactoryTest
extends FacetFactoryTestAbstract {

    CollectionAnnotationFacetFactory facetFactory;

    private static void processHidden(
            final CollectionAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        val collectionIfAny = processMethodContext.synthesizeOnMethod(Collection.class);
        facetFactory.processHidden(processMethodContext, collectionIfAny);
    }

    private static void processTypeOf(
            final CollectionAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        val collectionIfAny = processMethodContext.synthesizeOnMethod(Collection.class);
        facetFactory.processTypeOf(processMethodContext, collectionIfAny);
    }

    @BeforeEach
    public void setUp() throws Exception {
        facetFactory = new CollectionAnnotationFacetFactory(getMetaModelContext());
    }

    @AfterEach
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    @Deprecated(forRemoval = true, since = "2.0.0-RC2")
    static class Hidden extends CollectionAnnotationFacetFactoryTest {

        @Test
        void withAnnotation() {

            class Order {
            }
            @SuppressWarnings("unused")
            class Customer {
                @Collection(hidden = Where.REFERENCES_PARENT)
                public List<Order> getOrders() { return null; }
                public void setOrders(final List<Order> orders) {}
            }

            // given
            collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod)->{

                // when
                processHidden(facetFactory, processMethodContext);

                // then
                final HiddenFacet hiddenFacet = facetedMethod.getFacet(HiddenFacet.class);
                assertNotNull(hiddenFacet);
                assertTrue(hiddenFacet instanceof HiddenFacetForCollectionAnnotation);
                final HiddenFacetForCollectionAnnotation hiddenFacetImpl = (HiddenFacetForCollectionAnnotation) hiddenFacet;
                assertThat(hiddenFacetImpl.where(), is(Where.REFERENCES_PARENT));

                final Facet hiddenFacetForColl = facetedMethod.getFacet(HiddenFacet.class);
                assertNotNull(hiddenFacetForColl);
                assertTrue(hiddenFacet == hiddenFacetForColl);

            });
        }

    }

    static class TypeOf extends CollectionAnnotationFacetFactoryTest {


        @Test
        void whenCollectionAnnotation() {

            class Order {
            }
            @SuppressWarnings("unused")
            class Customer {
                @Collection(typeOf = Order.class)
                public List<Order> getOrders() { return null; }
                public void setOrders(final List<Order> orders) {}
            }

            // given
            collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod)->{
                // when
                processTypeOf(facetFactory, processMethodContext);
                // then
                final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
                assertNotNull(facet);
                assertTrue(facet instanceof TypeOfFacetForCollectionAnnotation);
                assertThat(facet.value().getElementType(), CausewayMatchers.classEqualTo(Order.class));
            });
        }

        @Test
        void whenInferFromType() {

            class Order {
            }
            @SuppressWarnings("unused")
            class Customer {
                public Order[] getOrders() { return null; }
                public void setOrders(final Order[] orders) {}
            }

            // given
            collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod)->{
                // when
                processTypeOf(facetFactory, processMethodContext);

                // then
                final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
                assertNotNull(facet);
                assertTrue(facet instanceof TypeOfFacet);
                assertThat(facet.value().getElementType(), CausewayMatchers.classEqualTo(Order.class));
                assertThat(facet.value().getCollectionSemantics(), Matchers.is(Optional.of(CollectionSemantics.ARRAY)));
            });
        }

        @Test
        void whenInferFromGenerics() {

            class Order {
            }
            @SuppressWarnings("unused")
            class Customer {
                public java.util.Collection<Order> getOrders() { return null; }
                public void setOrders(final java.util.Collection<Order> orders) {}
            }

            // given
            collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod)->{
                // when
                processTypeOf(facetFactory, processMethodContext);

                // then
                final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
                assertNotNull(facet);
                assertTrue(facet instanceof TypeOfFacetFromFeature);
                assertThat(facet.value().getElementType(), CausewayMatchers.classEqualTo(Order.class));
            });
        }

    }

}
