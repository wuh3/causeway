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
package org.apache.isis.viewer.wicket.model.models;

import java.util.List;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.common.model.feature.PropertyUiModel;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;

import lombok.val;

public class ScalarPropertyModel 
extends ScalarModel 
implements PropertyUiModel {
    
    private static final long serialVersionUID = 1L;
    
    private final PropertyMemento propertyMemento;

    /**
     * Creates a model representing a property of a parent object, with the
     * {@link #getObject() value of this model} to be current value of the
     * property.
     */
    public ScalarPropertyModel(
            EntityModel parentEntityModel, 
            PropertyMemento pm,
            EntityModel.Mode mode, 
            EntityModel.RenderingHint renderingHint) {
        
        super(parentEntityModel, pm, mode, renderingHint);
        this.propertyMemento = pm;
        reset();
        getAndStore(parentEntityModel);
    }
    
    public ScalarPropertyModel copyHaving(
            EntityModel.Mode mode, 
            EntityModel.RenderingHint renderingHint) {
        return new ScalarPropertyModel(
                getParentUiModel(), 
                propertyMemento,
                mode,
                renderingHint);
    }
    
    private transient OneToOneAssociation property;
    
    @Override
    public OneToOneAssociation getMetaModel() {
        if(property==null) {
            property = propertyMemento.getProperty(getSpecificationLoader()); 
        }
        return property;  
    }

    private transient ManagedProperty managedProperty;
    
    public ManagedProperty getManagedProperty() {
        if(managedProperty==null) {
            val parentAdapter = getParentUiModel().load();
            managedProperty = ManagedProperty.of(parentAdapter, getMetaModel()); 
        }
        return managedProperty;  
    } 
    
    @Override
    public ObjectSpecification getScalarTypeSpec() {
        return getMetaModel().getSpecification();
    }

    @Override
    public String getIdentifier() {
        return getMetaModel().getIdentifier().toNameIdentityString();
    }

    @Override
    public String getCssClass() {
        return getMetaModel().getCssClass("isis-");
    }

    @Override
    public boolean whetherHidden(final Where where) {
        return getManagedProperty()
                .checkVisibility(where)
                .isPresent();
    }

    @Override
    public String whetherDisabled(final Where where) {
        return getManagedProperty()
                .checkUsability(where)
                .map(InteractionVeto::getReason)
                .orElse(null);
    }

    @Override
    public String validate(final ManagedObject proposedAdapter) {
        final ManagedObject parentAdapter = getParentUiModel().load();
        try {
            final Consent valid = getMetaModel().isAssociationValid(parentAdapter, proposedAdapter,
                    InteractionInitiatedBy.USER);
            return valid.isAllowed() ? null : valid.getReason();
        } catch (final Exception ex) {
            return ex.getLocalizedMessage();
        }
    }

    @Override
    public boolean isRequired() {
        return isRequired(getMetaModel());
    }

    @Override
    public <T extends Facet> T getFacet(final Class<T> facetType) {
        return getMetaModel().getFacet(facetType);
    }

    public void reset() {
        val parentAdapter = getParentUiModel().load();
        setObjectFromPropertyIfVisible(this, getMetaModel(), parentAdapter);
    }

    @Override
    public ManagedObject load() {
        return loadFromSuper();
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public String toStringOf() {
        return getName() + ": " + propertyMemento.toString();
    }
    
    public String getReasonInvalidIfAny() {
        val adapter = getParentUiModel().load();
        val associate = getObject();
        Consent validity = getMetaModel().isAssociationValid(adapter, associate, InteractionInitiatedBy.USER);
        return validity.isAllowed() ? null : validity.getReason();
    }
    
    /**
     * Apply changes to the underlying adapter (possibly returning a new adapter).
     *
     * @return adapter, which may be different from the original (if a {@link ViewModelFacet#isCloneable(Object) cloneable} view model, for example.
     */
    public ManagedObject applyValue(ManagedObject adapter) {
        val property = getMetaModel();

        val associate = getObject();
        property.set(adapter, associate, InteractionInitiatedBy.USER);

        return ManagedObjects.copyIfClonable(adapter);

    }
    
    @Override
    protected List<ObjectAction> calcAssociatedActions() {
        val parentAdapter = getParentUiModel().load();
        return ObjectAction.Util.findForAssociation(parentAdapter, getMetaModel());
    }

    // -- HELPER
    
    private void getAndStore(final EntityModel parentEntityModel) {
        val parentAdapterMemento = parentEntityModel.getObjectAdapterMemento();
        val parentAdapter = super.getCommonContext().reconstructObject(parentAdapterMemento);
        val property = propertyMemento.getProperty(getSpecificationLoader());
        setObjectFromPropertyIfVisible(ScalarPropertyModel.this, property, parentAdapter);
    }

    
}
