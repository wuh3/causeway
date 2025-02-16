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

import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.security.authorization.Authorizor;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.causeway.testdomain.model.interaction.InteractionDemo;
import org.apache.causeway.testdomain.util.interaction.InteractionTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingInteractionDomain.class,
                ActionInteractionTest_usingAllowSafeSemantics.AuthorizorDenyUse.class
        },
        properties = {
                "causeway.security.actionsWithSafeSemanticsRequireOnlyViewingPermission=TRUE",
                "causeway.core.meta-model.introspector.mode=FULL",
        })
@TestPropertySource({
    //CausewayPresets.DebugMetaModel,
    //CausewayPresets.DebugProgrammingModel,
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
@DirtiesContext(methodMode = MethodMode.BEFORE_METHOD, classMode = ClassMode.BEFORE_CLASS)
class ActionInteractionTest_usingAllowSafeSemantics extends InteractionTestAbstract {

    @Service
    @Named("regressiontests.AuthorizorDenyUse")
    @javax.annotation.Priority(PriorityPrecedence.EARLY)
    @Qualifier("Testing")
    public static class AuthorizorDenyUse implements Authorizor {

        @Override
        public boolean isVisible(final InteractionContext authentication, final Identifier identifier) {
            return true; // grant view of any action (for testing)
        }

        @Override
        public boolean isUsable(final InteractionContext authentication, final Identifier identifier) {
            return false; // deny use of any action (for testing)
        }

    }

    @Test
    void assert_prereq() {
        val config = super.objectManager.getConfiguration();
        assertTrue(config.getSecurity().isActionsWithSafeSemanticsRequireOnlyViewingPermission());
    }

    @Test
    void whenSafeAction_shouldAllowUse() {
        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "actSafely", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();
        val managedAction = actionInteraction.getManagedAction().get(); // should not throw
        val actionMeta = managedAction.getAction();
        assertEquals(SemanticsOf.SAFE, actionMeta.getSemantics());
    }

    @Test
    void whenNonSafeAction_shouldDenyUse() {
        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "actUnsafely", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();
        val veto = actionInteraction.getInteractionVeto().orElseThrow(); // should not throw
        assertEquals("Not authorized to edit", veto.getReasonAsString().orElse(null));
    }

}
