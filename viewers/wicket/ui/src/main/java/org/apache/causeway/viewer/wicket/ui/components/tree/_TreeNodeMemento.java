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
package org.apache.causeway.viewer.wicket.ui.components.tree;

import java.io.Serializable;
import java.util.Objects;

import org.apache.causeway.applib.graph.tree.TreePath;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.model.models.ObjectAdapterModel;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;

import lombok.Getter;
import lombok.NonNull;

/**
 * Memento for a pair of {@link Bookmark} and {@link TreePath}.
 */
class _TreeNodeMemento implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter private final Bookmark bookmark;
    @Getter private final TreePath treePath;
    private final int hashCode;

    public _TreeNodeMemento(final TreePath treePath) {
        this.bookmark = null;
        this.treePath = treePath;
        this.hashCode = Objects.hash(0, treePath.hashCode());
    }

    public _TreeNodeMemento(final @NonNull Bookmark bookmark, final @NonNull TreePath treePath) {
        this.bookmark = bookmark;
        this.treePath = treePath;
        this.hashCode = Objects.hash(bookmark.hashCode(), treePath.hashCode());
    }

    public boolean isTreePathMemento() {
        return bookmark==null;
    }

    public Object getPojo(final MetaModelContext mmc) {
        _Assert.assertFalse(isTreePathMemento());
        return /*isTreePathMemento()
                ? null
                : */asObjectAdapterModel(mmc).getObject().getPojo();
    }

    public ObjectAdapterModel asObjectAdapterModel(final MetaModelContext mmc) {
        _Assert.assertFalse(isTreePathMemento());
        return UiObjectWkt.ofBookmark(mmc, getBookmark());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof _TreeNodeMemento) {
            final _TreeNodeMemento other = (_TreeNodeMemento) obj;
            return treePath.equals(other.treePath)
                    && bookmark.equals(other.bookmark);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

}