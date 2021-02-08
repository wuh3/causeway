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
package org.apache.isis.tooling.j2adoc.format;

import java.util.Optional;

import org.asciidoctor.ast.StructuralNode;

import org.apache.isis.tooling.j2adoc.J2AdocContext;
import org.apache.isis.tooling.j2adoc.J2AdocUnit;
import org.apache.isis.tooling.j2adoc.convert.J2AdocConverter;
import org.apache.isis.tooling.j2adoc.convert.J2AdocConverterDefault;
import org.apache.isis.tooling.model4adoc.AsciiDocFactory;

import lombok.NonNull;
import lombok.val;

public class UnitFormatterWithSourceAndCallouts
extends UnitFormatterAbstract {

    public UnitFormatterWithSourceAndCallouts(final @NonNull J2AdocContext j2aContext) {
        super(j2aContext);
    }

    protected Optional<String> javaSource(final J2AdocUnit unit) {

        final String javaSource = Snippets.javaSourceFor(unit);
        return Optional.of(
                AsciiDocFactory.toString(doc->
                    AsciiDocFactory.SourceFactory.java(doc, javaSource, unit.getCanonicalName() + ".java")));
    }

    @Override
    protected void memberDescriptions(final J2AdocUnit unit, final StructuralNode parent) {

        val ul = AsciiDocFactory.callouts(parent);

        val converter = J2AdocConverterDefault.of(j2aContext);
        appendMembersToList(ul, unit,
                unit.getTypeDeclaration().getEnumConstantDeclarations(),
                decl -> converter.enumConstantDeclaration(decl),
                (javadoc, j2Unit) -> converter.javadoc(javadoc, unit, J2AdocConverter.Mode.ALL)
        );

        appendMembersToList(ul, unit,
                unit.getTypeDeclaration().getPublicFieldDeclarations(),
                decl -> converter.fieldDeclaration(decl, unit),
                (javadoc, j2Unit) -> converter.javadoc(javadoc, unit, J2AdocConverter.Mode.ALL));

        appendMembersToList(ul, unit,
                unit.getTypeDeclaration().getAnnotationMemberDeclarations(),
                decl -> converter.annotationMemberDeclaration(decl, unit),
                (javadoc, j2Unit) -> converter.javadoc(javadoc, unit, J2AdocConverter.Mode.ALL));

        appendMembersToList(ul, unit,
                unit.getTypeDeclaration().getPublicConstructorDeclarations(),
                decl -> converter.constructorDeclaration(decl, unit),
                (javadoc, j2Unit) -> converter.javadoc(javadoc, unit, J2AdocConverter.Mode.ALL));

        appendMembersToList(ul, unit,
                unit.getTypeDeclaration().getPublicMethodDeclarations(),
                decl -> converter.methodDeclaration(decl, unit),
                (javadoc, j2Unit) -> converter.javadoc(javadoc, unit, J2AdocConverter.Mode.ALL));

    }

    //XXX java language syntax (for callout text), but not used any more
//
//    @Override
//    public String getEnumConstantFormat() {
//        return "`%s`";
//    }
//
//    @Override
//    public String getFieldFormat() {
//        return "`%s %s`";
//    }
//
//    @Override
//    public String getConstructorFormat() {
//        return "`%s(%s)`";
//    }
//
//    @Override
//    public String getGenericConstructorFormat() {
//        return "`%s %s(%s)`";
//    }
//
//    @Override
//    public String getMethodFormat() {
//        return "`%s %s(%s)`";
//    }
//
//    @Override
//    public String getGenericMethodFormat() {
//        return "`%s %s %s(%s)`";
//    }

}
