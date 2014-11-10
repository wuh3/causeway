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

package org.apache.isis.viewer.wicket.ui.components.standalonecollection;

import java.util.List;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.components.collection.count.CollectionCountProvider;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorHelper;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorPanel;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorProvider;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.multiple.CollectionContentsMultipleViewsPanelFactory;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

public class StandaloneCollectionPanel extends PanelAbstract<EntityCollectionModel>
        implements CollectionCountProvider, CollectionSelectorProvider {

    private static final long serialVersionUID = 1L;

    private static final String ID_ACTION_NAME = "actionName";
    private static final String ID_ADDITIONAL_LINKS = "additionalLinks";
    private static final String ID_SELECTOR_DROPDOWN = "selectorDropdown";
    private CollectionSelectorPanel selectorDropdownPanel;

    public StandaloneCollectionPanel(final String id, final EntityCollectionModel entityCollectionModel) {
        super(id, entityCollectionModel);
        buildGui(entityCollectionModel);
    }

    private void buildGui(final EntityCollectionModel entityCollectionModel) {

        ActionModel actionModel = entityCollectionModel.getActionModelHint();
        ObjectAction action = actionModel.getActionMemento().getAction();
        addOrReplace(new Label(StandaloneCollectionPanel.ID_ACTION_NAME, Model.of(action.getName())));

        final CollectionSelectorHelper selectorHelper = new CollectionSelectorHelper(entityCollectionModel, getComponentFactoryRegistry(), new CollectionContentsMultipleViewsPanelFactory());

        final List<ComponentFactory> componentFactories = selectorHelper.findOtherComponentFactories();

        if (componentFactories.size() <= 1) {
            permanentlyHide(ID_SELECTOR_DROPDOWN);
        } else {
            CollectionSelectorPanel selectorDropdownPanel;
            selectorDropdownPanel = new CollectionSelectorPanel(ID_SELECTOR_DROPDOWN, entityCollectionModel, new CollectionContentsMultipleViewsPanelFactory());

            final Model<ComponentFactory> componentFactoryModel = new Model<>();

            final int selected = selectorHelper.honourViewHintElseDefault(selectorDropdownPanel);

            ComponentFactory selectedComponentFactory = componentFactories.get(selected);
            componentFactoryModel.setObject(selectedComponentFactory);

            this.setOutputMarkupId(true);
            addOrReplace(selectorDropdownPanel);

            this.selectorDropdownPanel = selectorDropdownPanel;
        }

        final ComponentFactoryRegistry componentFactoryRegistry = getComponentFactoryRegistry();
        componentFactoryRegistry.addOrReplaceComponent(this, ComponentType.COLLECTION_CONTENTS, entityCollectionModel);
    }

    @Override
    public Integer getCount() {
        final EntityCollectionModel model = getModel();
        return model.getCount();
    }

    @Override
    public CollectionSelectorPanel getSelectorDropdownPanel() {
        return selectorDropdownPanel;
    }
}
