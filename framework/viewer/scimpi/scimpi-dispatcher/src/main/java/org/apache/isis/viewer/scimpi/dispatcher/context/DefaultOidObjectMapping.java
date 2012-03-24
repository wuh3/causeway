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

package org.apache.isis.viewer.scimpi.dispatcher.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.objecttype.ObjectTypeFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidDefault;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;

public class DefaultOidObjectMapping implements ObjectMapping {
    private static final Logger LOG = Logger.getLogger(DefaultOidObjectMapping.class);
    private final Map<String, TransientObjectMapping> requestTransients = new HashMap<String, TransientObjectMapping>();
    private final Map<String, TransientObjectMapping> sessionTransients = new HashMap<String, TransientObjectMapping>();
    private Class<? extends Oid> oidType;

    @Override
    public void append(final DebugBuilder debug) {
        append(debug, requestTransients, "request");
        append(debug, sessionTransients, "session");
    }

    protected void append(final DebugBuilder debug, final Map<String, TransientObjectMapping> transients, final String type) {
        final Iterator<String> ids = new HashSet<String>(transients.keySet()).iterator();
        if (ids.hasNext()) {
            debug.appendTitle("Transient objects (" + type + ")");
            while (ids.hasNext()) {
                final String key = ids.next();
                debug.appendln(key, transients.get(key).debug());
            }
        }
    }

    @Override
    public void appendMappings(final DebugBuilder request) {
    }

    @Override
    public void clear() {
        requestTransients.clear();

        final List<String> remove = Lists.newArrayList();
        for (final String id : sessionTransients.keySet()) {
            final Oid oid = sessionTransients.get(id).getOid();
            if (!oid.isTransient()) {
                remove.add(id);
                sessionTransients.put(id, null);
            }
        }
        for (final String id : remove) {
            sessionTransients.remove(id);
        }
    }

    @Override
    public void endSession() {
        sessionTransients.clear();
    }

    @Override
    public String mapTransientObject(final ObjectAdapter object) {
        try {
            final List<ObjectAdapter> savedObject = new ArrayList<ObjectAdapter>();
            final JSONObject data = encodeTransientData(object, savedObject);
            return "D" + data.toString(4);
        } catch (final JSONException e) {
            throw new ScimpiException(e);
        }
    }

    private JSONObject encodeTransientData(final ObjectAdapter adapter, final List<ObjectAdapter> savedObject) throws JSONException {
        if (savedObject.contains(adapter)) {
            return null;
        }
        savedObject.add(adapter);

        final JSONObject data = createJsonObject(adapter);

        final ObjectSpecification specification = adapter.getSpecification();
        for (final ObjectAssociation association : specification.getAssociations()) {
            final ObjectAdapter fieldValue = association.get(adapter);
            final String fieldName = association.getId();
            if (fieldValue == null) {
                data.put(fieldName, (Object) null);
            } else if (association.getSpecification().isEncodeable()) {
                final EncodableFacet encodeableFacet = fieldValue.getSpecification().getFacet(EncodableFacet.class);
                data.put(fieldName, encodeableFacet.toEncodedString(fieldValue));
            } else if (association instanceof OneToManyAssociation) {
                final List<JSONObject> collection = new ArrayList<JSONObject>();
                final CollectionFacet facet = fieldValue.getSpecification().getFacet(CollectionFacet.class);
                for (final ObjectAdapter element : facet.iterable(fieldValue)) {
                    collection.add(encodeTransientData(element, savedObject));
                }
                data.put(fieldName, collection);
            } else {
                if (fieldValue.isTransient() || fieldValue.isParented()) {
                    final JSONObject saveData = encodeTransientData(fieldValue, savedObject);
                    if (saveData == null) {
                        data.put(fieldName, mapObject(fieldValue, Scope.INTERACTION));
                    } else {
                        data.put(fieldName, saveData);
                    }
                } else {
                    data.put(fieldName, mapObject(fieldValue, Scope.INTERACTION));
                }
            }
        }
        return data;
    }

    public JSONObject createJsonObject(final ObjectAdapter adapter) throws JSONException {
        final JSONObject data = new JSONObject();

        final Oid oid = adapter.getOid();
        data.put("_oidType", oid.getClass().getName());
        
        if(oid instanceof RootOid) {
            RootOid ows = (RootOid) oid;
            data.put("_objectType", ows.getObjectType());
            data.put("_id", enString(ows)); // can be used to recreate
            return data;
        }
        
        // original behaviour, deals with SerialOid and also enhanced to handles if the parentOid is an OWS
        final ObjectSpecification objectSpec = adapter.getSpecification();
        data.put("_objectType", objectSpec.getObjectType());

        if (!(oid instanceof AggregatedOid)) {
            throw new ScimpiException("Unsupported OID type " + oid);
        } 
        
        final AggregatedOid aoid = (AggregatedOid) oid;
        final Oid parentOid = aoid.getParentOid();
        final String aggregatedId = aoid.getLocalId();
        
        String encodedOid = enString(parentOid, aggregatedId);
        data.put("_id", encodedOid);
        return data;

    }

    @Override
    public String mapObject(final ObjectAdapter inObject, final Scope scope) {
        // TODO need to ensure that transient objects are remapped each time so
        // that any changes are added to
        // session data
        // continue work here.....here

        ObjectAdapter adapter = inObject;

        final Oid oid = adapter.getOid();
        if (oidType == null) {
            oidType = oid.getClass();
        }

        String encodedOid;
        if(oid instanceof RootOid) {
            RootOid ows = (RootOid) oid;
            encodedOid = enString(ows);
        } else if (oid instanceof AggregatedOid) {
            final AggregatedOid aoid = (AggregatedOid) oid;
            final String aggregatedId = aoid.getLocalId();
            final Oid parentOid = aoid.getParentOid();
            adapter = getAdapterManager().getAdapterFor(parentOid);
            
            encodedOid = enString(parentOid, aggregatedId);
        } else if (oid instanceof RootOidDefault) {
            final RootOidDefault rootOidDefault = (RootOidDefault) oid;
            encodedOid = rootOidDefault.getIdentifier();
        } else {
            throw new ScimpiException("Unsupported OID type " + oid);
        }

        
        final boolean isTransient = adapter.isTransient();
        final String transferableId = (isTransient ? "T" : "P") + adapter.getSpecification().getFullIdentifier() + "@" + encodedOid;
        LOG.debug("encoded " + oid + " as " + transferableId + " ~ " + encodedOid);

        if (inObject.isTransient()) {

            // TODO cache these in requests so that Mementos are only created
            // once.
            // TODO if Transient/Interaction then return state; other store
            // state in session an return OID
            // string
            final TransientObjectMapping mapping = new TransientObjectMapping(inObject);
            if (scope == Scope.REQUEST) {
                requestTransients.put(transferableId, mapping);
            } else if (scope == Scope.INTERACTION || scope == Scope.SESSION) {
                sessionTransients.put(transferableId, mapping);
            } else {
                throw new ScimpiException("Can't hold globally transient object");
            }
        }
        return transferableId;
    }

    @Override
    public ObjectAdapter mappedTransientObject(final String data) {
        final String objectData = data; // StringEscapeUtils.unescapeHtml(data);
        LOG.debug("data" + objectData);

        try {
            final JSONObject jsonObject = new JSONObject(objectData);
            return restoreTransientObject(jsonObject);
        } catch (final JSONException e) {
            throw new ScimpiException("Problem reading data: " + data, e);
        }
    }

    private ObjectAdapter restoreTransientObject(final JSONObject jsonObject) throws JSONException {

        ObjectAdapter adapter = getAdapter(jsonObject);
        
        final String objectType = jsonObject.getString("_objectType");
        final ObjectSpecification specification = getSpecificationLoader().lookupByObjectType(objectType); 
        for (final ObjectAssociation association : specification.getAssociations()) {
            final String fieldName = association.getId();

            final Object fieldValue = jsonObject.has(fieldName) ? jsonObject.get(fieldName) : null;

            if (association.getSpecification().isEncodeable()) {
                if (fieldValue == null) {
                    ((OneToOneAssociation) association).initAssociation(adapter, null);
                } else {
                    final EncodableFacet encodeableFacet = association.getSpecification().getFacet(EncodableFacet.class);
                    final ObjectAdapter fromEncodedString = encodeableFacet.fromEncodedString((String) fieldValue);
                    ((OneToOneAssociation) association).initAssociation(adapter, fromEncodedString);
                }
            } else if (association instanceof OneToManyAssociation) {
                final JSONArray collection = (JSONArray) fieldValue;
                for (int i = 0; i < collection.length(); i++) {
                    final JSONObject jsonElement = (JSONObject) collection.get(i);
                    final ObjectAdapter objectToAdd = restoreTransientObject(jsonElement);
                    ((OneToManyAssociation) association).addElement(adapter, objectToAdd);
                }

                /*
                 * CollectionFacet facet =
                 * fieldValue.getSpecification().getFacet
                 * (CollectionFacet.class); for (ObjectAdapter element :
                 * facet.iterable(fieldValue)) {
                 * collection.add(saveData(element, savedObject)); }
                 * data.put(fieldName, collection);
                 */
            } else {
                if (fieldValue == null) {
                    ((OneToOneAssociation) association).initAssociation(adapter, null);
                } else {
                    if (fieldValue instanceof JSONObject) {
                        final ObjectAdapter fieldObject = restoreTransientObject((JSONObject) fieldValue);
                        ((OneToOneAssociation) association).initAssociation(adapter, fieldObject);
                    } else {
                        final ObjectAdapter field = mappedObject((String) fieldValue);
                        ((OneToOneAssociation) association).initAssociation(adapter, field);
                    }
                }
            }
        }
        return adapter;
    }

    private ObjectAdapter getAdapter(final JSONObject jsonObject) throws JSONException {

        final String objectType = jsonObject.getString("_objectType");
        final ObjectSpecification objectSpec = getSpecificationLoader().lookupByObjectType(objectType);
        
        final String id = jsonObject.getString("_id");

        if (objectSpec.isAggregated() && !objectSpec.isCollection()) {
            final String[] split = id.split("@");
            final String parentOidStr = split[0];
            final String aggregatedLocalId = split[1];
            
            RootOid parentOid;
            if(RootOid.class.isAssignableFrom(oidType)) {
                parentOid = getOidStringifier().deString(parentOidStr);
            } else if (RootOidDefault.class.isAssignableFrom(oidType)) {
                parentOid = RootOidDefault.createTransient(objectType, parentOidStr);
            } else {
                // REVIEW: for now, don't support holding references to aggregates whose parent is also an aggregate
                throw new ScimpiException("Unsupported OID type " + oidType);
            }
                
            final AggregatedOid oid = new AggregatedOid(parentOid, aggregatedLocalId);
            return getPersistenceSession().recreateAdapter(oid, objectSpec);
        } else {
            return mappedObject("T" + objectType + "@" + id); // yuk!
        }
    }

    @Override
    public ObjectAdapter mappedObject(final String id) {
        final char type = id.charAt(0);
        final String[] split = id.split("@");
        final String objectType = split[0].substring(1);
        final String oidData = split[1];
        final String aggregatedId = split.length > 2?split[2]:null;
            
        // HACK - to remove after fix!
        if (oidType == null) {
            oidType = getPersistenceSession().getServices().get(0).getOid().getClass();
        }

        final ObjectSpecification spec = getSpecificationLoader().lookupByObjectType(objectType);
        
        if ((type == 'T')) {
            TransientObjectMapping mapping = sessionTransients.get(id);
            if (mapping == null) {
                mapping = requestTransients.get(id);
            }
            if (mapping == null) {
                
                final Object pojo = spec.createObject();

                // create as a (transient) root adapter
                Oid oid = deString(objectType, oidData, State.TRANSIENT);
                return getPersistenceSession().recreateAdapter(oid, pojo);
            }
            
            final ObjectAdapter mappedTransientObject = mapping.getObject();
            LOG.debug("retrieved " + mappedTransientObject.getOid() + " for " + id);
            return mappedTransientObject;
            
        } else {
            
            try {
                LOG.debug("decoding " + oidData);

                if (aggregatedId != null) {
                    final RootOid parentOid = deString(objectType, oidData, State.PERSISTENT);
                    Oid oid = new AggregatedOid(parentOid, aggregatedId);
                    getPersistenceSession().loadObject(parentOid, spec);
                    return getAdapterManager().getAdapterFor(oid);
                } 

                Oid oid = deString(objectType, oidData, State.PERSISTENT);
                return getPersistenceSession().loadObject(oid, spec);

            } catch (final SecurityException e) {
                throw new IsisException(e);
            }
        }
    }

    @Override
    public void reloadIdentityMap() {
        final Iterator<TransientObjectMapping> mappings = sessionTransients.values().iterator();
        while (mappings.hasNext()) {
            final TransientObjectMapping mapping = mappings.next();
            mapping.reload();
        }
    }

    @Override
    public void unmapObject(final ObjectAdapter object, final Scope scope) {
        sessionTransients.remove(object.getOid());
        requestTransients.remove(object.getOid());
    }


    ///////////////////////////////////////
    // helpers
    ///////////////////////////////////////

    enum State { TRANSIENT, PERSISTENT }
    
    private RootOid deString(String objectType, final String oidData, State stateHint) {
        if(RootOid.class.isAssignableFrom(oidType)) {
            return getOidStringifier().deString(oidData);
        } else {
            throw new ScimpiException("Unsupported OID type " + oidType);
        }
    }


    private String enString(RootOid ows) {
        return getOidStringifier().enString(ows);
    }

    private String enString(final Oid parentOid, final String aggregatedId) {
        return enString(parentOid) + "@" + aggregatedId;
    }

    private String enString(final Oid oid) {
        final String parentOidStr;
        if(oid instanceof RootOid) {
            RootOid ows = (RootOid) oid;
            parentOidStr = enString(ows);
        } else if (oid instanceof RootOidDefault) {
            final RootOidDefault parentSerialOid = (RootOidDefault) oid;
            parentOidStr = parentSerialOid.getIdentifier();
        } else {
            throw new ScimpiException("Unsupported OID type " + oid);
        }
        return parentOidStr;
    }

    
    ///////////////////////////////////////
    // from context
    ///////////////////////////////////////
    
    protected OidStringifier getOidStringifier() {
        return getPersistenceSession().getOidGenerator().getOidStringifier();
    }
    
    protected SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

}
