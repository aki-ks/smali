/*
 * [The "BSD licence"]
 * Copyright (c) 2009 Ben Gruver
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.baksmali.Adaptors;

import org.jf.dexlib.EncodedValue.EncodedValue;
import org.jf.dexlib.*;
import org.jf.dexlib.util.AccessFlags;

import java.util.*;

public class ClassDefinition {
    private ClassDefItem classDefItem;
    private ClassDataItem classDataItem;

    private HashMap<Integer, AnnotationSetItem> methodAnnotations = new HashMap<Integer, AnnotationSetItem>();

    public ClassDefinition(ClassDefItem classDefItem) {
        this.classDefItem = classDefItem;
        this.classDataItem = classDefItem.getClassData();
        buildAnnotationMaps();
    }

    private void buildAnnotationMaps() {
        AnnotationDirectoryItem annotationDirectory = classDefItem.getAnnotationDirectory();
        if (annotationDirectory == null) {
            return;
        }

        List<AnnotationDirectoryItem.MethodAnnotation> methodAnnotationList = annotationDirectory.getMethodAnnotations();

        if (methodAnnotations != null) {
            for (AnnotationDirectoryItem.MethodAnnotation methodAnnotation: methodAnnotationList) {
                methodAnnotations.put(methodAnnotation.getMethod().getIndex(), methodAnnotation.getAnnotationSet());
            }
        }
    }

    private List<String> accessFlags = null;
    public List<String> getAccessFlags() {
        if (accessFlags == null) {
            accessFlags = new ArrayList<String>();

            for (AccessFlags accessFlag: AccessFlags.getAccessFlagsForClass(classDefItem.getAccessFlags())) {
                accessFlags.add(accessFlag.toString());
            }
        }
        return accessFlags;
    }

    private String classType = null;
    public String getClassType() {
        if (classType == null) {
            classType = classDefItem.getClassType().getTypeDescriptor();
        }
        return classType;
    }

    private String superType = null;
    public String getSuperType() {
        if (superType == null) {
            superType = classDefItem.getSuperclass().getTypeDescriptor();
        }
        return superType; 
    }

    private List<String> interfaces = null;
    public List<String> getInterfaces() {
        if (interfaces == null) {
            interfaces = new ArrayList<String>();

            List<TypeIdItem> interfaceList = classDefItem.getInterfaces();

            if (interfaceList != null) {
                for (TypeIdItem typeIdItem: interfaceList) {
                    interfaces.add(typeIdItem.getTypeDescriptor());
                }
            }
        }
        return interfaces;
    }

    private List<FieldDefinition> staticFields = null;
    public List<FieldDefinition> getStaticFields() {
        if (staticFields == null) {
            staticFields = new ArrayList<FieldDefinition>();

            if (classDataItem != null) {

                EncodedArrayItem encodedStaticInitializers = classDefItem.getStaticInitializers();

                List<EncodedValue> staticInitializers;
                if (encodedStaticInitializers != null) {
                    staticInitializers = encodedStaticInitializers.getEncodedArray().getValues();
                } else {
                    staticInitializers = new ArrayList<EncodedValue>();
                }

                int i=0;
                for (ClassDataItem.EncodedField field: classDataItem.getStaticFields()) {
                    EncodedValue encodedValue = null;
                    if (i < staticInitializers.size()) {
                        encodedValue = staticInitializers.get(i);
                    }
                    staticFields.add(new FieldDefinition(field, encodedValue));
                    i++;
                }
            }
        }
        return staticFields;
    }

    private List<FieldDefinition> instanceFields = null;
    public List<FieldDefinition> getInstanceFields() {
        if (instanceFields == null) {
            instanceFields = new ArrayList<FieldDefinition>();

            if (classDataItem != null) {
                for (ClassDataItem.EncodedField field: classDataItem.getInstanceFields()) {
                    instanceFields.add(new FieldDefinition(field));
                }
            }
        }
        return instanceFields;       
    }

    private List<MethodDefinition> directMethods = null;
    public List<MethodDefinition> getDirectMethods() {
        if (directMethods == null) {
            directMethods = new ArrayList<MethodDefinition>();

            if (classDataItem != null) {
                for (ClassDataItem.EncodedMethod method: classDataItem.getDirectMethods()) {
                    AnnotationSetItem annotationSet = methodAnnotations.get(method.getMethod().getIndex());
                    directMethods.add(new MethodDefinition(method, annotationSet));
                }
            }
        }
        return directMethods;
    }

    private List<MethodDefinition> virtualMethods = null;
    public List<MethodDefinition> getVirtualMethods() {
        if (virtualMethods == null) {
            virtualMethods = new ArrayList<MethodDefinition>();

            if (classDataItem != null) {
                for (ClassDataItem.EncodedMethod method: classDataItem.getVirtualMethods()) {
                    AnnotationSetItem annotationSet = methodAnnotations.get(method.getMethod().getIndex());
                    virtualMethods.add(new MethodDefinition(method, annotationSet));
                }
            }
        }
        return virtualMethods;
    }

    public List<AnnotationAdaptor> getAnnotations() {
        AnnotationDirectoryItem annotationDirectory = classDefItem.getAnnotationDirectory();
        if (annotationDirectory == null) {
            return null;
        }

        AnnotationSetItem annotationSet = annotationDirectory.getClassAnnotations();
        if (annotationSet == null) {
            return null;
        }

        List<AnnotationAdaptor> annotationAdaptors = new ArrayList<AnnotationAdaptor>();

        for (AnnotationItem annotationItem: annotationSet.getAnnotationItems()) {
            annotationAdaptors.add(new AnnotationAdaptor(annotationItem));
        }
        return annotationAdaptors;
    }
}