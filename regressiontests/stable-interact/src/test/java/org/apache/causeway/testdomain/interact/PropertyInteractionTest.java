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
package org.apache.causeway.testdomain.interact;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.facets.objectvalue.labelat.LabelAtFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.multiline.MultiLineFacet;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.causeway.testdomain.model.interaction.InteractionDemo;
import org.apache.causeway.testdomain.util.interaction.InteractionTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingInteractionDomain.class
        },
        properties = {
                "causeway.core.meta-model.introspector.mode=FULL",
                "causeway.applib.annotation.domain-object.editing=TRUE",
                "causeway.core.meta-model.validator.explicit-object-type=FALSE", // does not override any of the imports
                "logging.level.DependentArgUtils=DEBUG"
        })
@TestPropertySource({
    //CausewayPresets.DebugMetaModel,
    //CausewayPresets.DebugProgrammingModel,
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
class PropertyInteractionTest extends InteractionTestAbstract {

    @Test
    void propertyInteraction_whenEnabled_shouldHaveNoVeto() {

        val tester =
                testerFactory.propertyTester(InteractionDemo.class, "stringMultiline", Where.OBJECT_FORMS);

        tester.assertVisibilityIsNotVetoed();
        tester.assertUsabilityIsNotVetoed();

        // verify, that the meta-model is valid
        assertMetamodelValid();

        // verify, that we have the LabelAtFacet
        val labelAtFacet = tester.getFacetOnMemberElseFail(LabelAtFacet.class);
        val labelPos = labelAtFacet.label();
        assertEquals(LabelPosition.TOP, labelPos);

        // verify, that we have the MultiLineFacet
        val multiLineFacet = tester.getFacetOnMemberElseFail(MultiLineFacet.class);
        val numberOfLines = multiLineFacet.numberOfLines();
        assertEquals(3, numberOfLines);

        tester.assertValue("initial");
        tester.assertValueUpdateUsingNegotiation("new Value");
        tester.assertValueUpdateUsingNegotiationTextual("parsable Text");
    }

    @Test
    void propertyInteraction_whenDisabled_shouldHaveVeto() {

        val tester =
                testerFactory.propertyTester(InteractionDemo.class, "stringDisabled", Where.OBJECT_FORMS);

        tester.assertVisibilityIsNotVetoed();
        tester.assertUsabilityIsVetoedWith("Disabled for demonstration.");
    }


}
