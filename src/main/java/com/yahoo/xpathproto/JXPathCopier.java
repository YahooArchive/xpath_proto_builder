/*
Copyright 2014 Yahoo! Inc.
Copyrights licensed under the BSD License. See the accompanying LICENSE file for terms.
*/

package com.yahoo.xpathproto;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import com.google.protobuf.WireFormat.FieldType;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message.Builder;

/**
 * This class is used to copy the value of the field specified by the xpath. It supports multiple types of values that
 * can be copied like int, long, float, double, boolean , string and Object (custom). It also handles repeated fields in
 * protos for the types defined.
 */
public class JXPathCopier {

    private final JXPathContext source;
    private final Builder target;

    public JXPathCopier(final JXPathContext source, final Builder target) {
        this.source = source;
        this.target = target;

        this.source.setLenient(true);
    }

    public JXPathContext getSource() {
        return source;
    }

    public Builder getTarget() {
        return target;
    }

    public Object getValue(final String path) {
        return source.getValue(path);
    }

    public JXPathContext getRelativeContext(final String path) {
        return getRelativeContext(source, path);
    }

    public JXPathCopier copyObject(final Object sourceObject, final String targetField) {
        if (sourceObject != null) {
            setTargetField(target, sourceObject, targetField);
        }

        return this;
    }

    public JXPathCopier copyScalarObject(
        final Object sourceObject, final String targetField, final Descriptors.FieldDescriptor fieldDescriptor) {
        Descriptors.FieldDescriptor.JavaType javaType = fieldDescriptor.getJavaType();
        switch (javaType) {
            case INT:
                copyAsInteger(sourceObject, targetField);
                break;
            case LONG:
                copyAsLong(sourceObject, targetField);
                break;
            case FLOAT:
                copyAsFloat(sourceObject, targetField);
                break;
            case DOUBLE:
                copyAsDouble(sourceObject, targetField);
                break;
            case BOOLEAN:
                copyAsBoolean(sourceObject, targetField);
                break;
            case STRING:
                copyAsString(sourceObject, targetField);
                break;
            case BYTE_STRING:
                throw new RuntimeException("bytes type not handled for field: " + targetField);
            case ENUM:
                copyAsEnum(sourceObject, targetField, fieldDescriptor);
                break;
            case MESSAGE:
                throw new RuntimeException("Protobuf Message type not handled: " + targetField);
        }

        return this;
    }

    public JXPathCopier copyAsScalar(final String sourcePath, final String targetField) {
        Descriptors.FieldDescriptor fieldDescriptor = target.getDescriptorForType().findFieldByName(targetField);
        if (null == fieldDescriptor) {
            throw new RuntimeException("Unknown target field in protobuf: " + targetField);
        }

        Descriptors.FieldDescriptor.JavaType javaType = fieldDescriptor.getJavaType();
        boolean repeated = fieldDescriptor.isRepeated();
        if (repeated) {
            Iterator iterator = source.iterate(sourcePath);
            while (iterator.hasNext()) {
                Object value = iterator.next();
                copyScalarObject(value, targetField, fieldDescriptor);
            }
        } else {
            Object value = source.getValue(sourcePath);
            copyScalarObject(value, targetField, fieldDescriptor);
        }

        return this;
    }

    public JXPathCopier copyAsScalar(final String sourcePath) {
        return copyAsScalar(sourcePath, sourcePath);
    }

    public JXPathCopier copyAsString(final String sourcePath, final String targetField) {
        Object sourceObject = source.getValue(sourcePath);
        return copyAsString(sourceObject, targetField);
    }

    public JXPathCopier copyAsString(final String sourcePath) {
        return copyAsString(sourcePath, sourcePath);
    }

    private JXPathCopier copyAsString(final Object sourceObject, final String targetField) {
        if (sourceObject != null) {
            setTargetField(target, sourceObject.toString(), targetField);
        }

        return this;
    }
    
    public JXPathCopier copyAsEnum(
        final Object sourceObject, final String targetField, final Descriptors.FieldDescriptor fieldDescriptor) {
        String enumName = sourceObject.toString();
        Object value = fieldDescriptor.getEnumType().findValueByName(enumName);
        if (value != null) {
            setTargetField(target, value, targetField);
        }
        return this;
    }

    public JXPathCopier copyAsInteger(final String sourcePath, final String targetField) {
        Object sourceObject = source.getValue(sourcePath);
        return copyAsInteger(sourceObject, targetField);
    }

    public JXPathCopier copyAsInteger(final String sourcePath) {
        return copyAsInteger(sourcePath, sourcePath);
    }

    private JXPathCopier copyAsInteger(final Object sourceObject, final String targetField) {
        if (sourceObject != null) {
            try {
                Integer object = Integer.parseInt(sourceObject.toString());
                setTargetField(target, object, targetField);
            } catch (NumberFormatException nfe) {
            }
        }

        return this;
    }

    public JXPathCopier copyAsLong(final String sourcePath, final String targetField) {
        Object sourceObject = source.getValue(sourcePath);
        return copyAsLong(sourceObject, targetField);
    }

    public JXPathCopier copyAsLong(final String sourcePath) {
        return copyAsLong(sourcePath, sourcePath);
    }

    public JXPathCopier copyAsLong(final Object sourceObject, final String targetField) {
        if (sourceObject != null) {
            try {
                Long object = Long.parseLong(sourceObject.toString());
                setTargetField(target, object, targetField);
            } catch (NumberFormatException nfe) {
            }
        }

        return this;
    }

    public JXPathCopier copyAsDouble(final String sourcePath, final String targetField) {
        Object sourceObject = source.getValue(sourcePath);
        return copyAsDouble(sourceObject, targetField);
    }

    public JXPathCopier copyAsDouble(final String sourcePath) {
        return copyAsDouble(sourcePath, sourcePath);
    }

    public JXPathCopier copyAsDouble(final Object sourceObject, final String targetField) {
        if (sourceObject != null) {
            try {
                Double object = Double.parseDouble(sourceObject.toString());
                setTargetField(target, object, targetField);
            } catch (NumberFormatException nfe) {
            }
        }

        return this;
    }

    public JXPathCopier copyAsFloat(final String sourcePath, final String targetField) {
        Object sourceObject = source.getValue(sourcePath);
        return copyAsFloat(sourceObject, targetField);
    }

    public JXPathCopier copyAsFloat(final String sourcePath) {
        return copyAsFloat(sourcePath, sourcePath);
    }

    public JXPathCopier copyAsFloat(final Object sourceObject, final String targetField) {
        if (sourceObject != null) {
            try {
                Float object = Float.parseFloat(sourceObject.toString());
                setTargetField(target, object, targetField);
            } catch (NumberFormatException nfe) {
            }
        }

        return this;
    }

    public JXPathCopier copyAsBoolean(final String sourcePath, final String targetField) {
        Object sourceObject = source.getValue(sourcePath);
        return copyAsBoolean(sourceObject, targetField);
    }

    public JXPathCopier copyAsBoolean(final String sourcePath) {
        return copyAsBoolean(sourcePath, sourcePath);
    }

    public JXPathCopier copyAsBoolean(final Object sourceObject, final String targetField) {
        if (sourceObject != null) {
            try {
                Boolean object = Boolean.parseBoolean(sourceObject.toString());
                setTargetField(target, object, targetField);
            } catch (NumberFormatException nfe) {
            }
        }

        return this;
    }

    public static JXPathContext getRelativeContext(final JXPathContext context, final String path) {
        Pointer pointer = context.getPointer(path);
        if ((pointer == null) || (pointer.getNode() == null)) {
            return null;
        }

        return context.getRelativeContext(pointer);
    }

    private static void setTargetField(final Builder target, final Object sourceObject, final String targetField)
        throws IllegalArgumentException {
        Descriptors.FieldDescriptor fieldDescriptor = target.getDescriptorForType().findFieldByName(targetField);
        if (null == fieldDescriptor) {
            throw new RuntimeException("Unknown target field in protobuf: " + targetField);
        }

        if (fieldDescriptor.isRepeated()) {
            target.addRepeatedField(fieldDescriptor, sourceObject);
        } else {
            target.setField(fieldDescriptor, sourceObject);
        }
    }
}
