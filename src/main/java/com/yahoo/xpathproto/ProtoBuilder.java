/*
Copyright 2014 Yahoo! Inc.
Copyrights licensed under the BSD License. See the accompanying LICENSE file for terms.
*/

package com.yahoo.xpathproto;

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.yahoo.xpathproto.dataobject.Config;
import com.yahoo.xpathproto.dataobject.Context;

public class ProtoBuilder {

    private static String DEFAULT_TRANSFORMER = "root_transform";
    private static final Logger logger = LoggerFactory.getLogger(ProtoBuilder.class);
    private static Cache<String, Config> configCache = CacheBuilder.newBuilder().maximumSize(10).build();

    private Context context;
    private final String builderConfig;
    private final String transform;
    private JXPathContext jXPathContext;

    /**
     * Instantiates a new proto builder from the config file provided by the user. The default transformation definition
     * "root_transform" is used.
     *
     * @param builderConfig - The path to the config file for the corresponding json
     */
    public ProtoBuilder(final String builderConfig) {
        this(builderConfig, DEFAULT_TRANSFORMER);
    }

    /**
     * Instantiates a new proto builder from the config file provided by the user. The transformation definition is also
     * provided by the user and is picked up from the config file.
     *
     * @param builderConfig - The path to the config file for the corresponding json
     * @param transform     - The transformation definition that should be used from the config file.
     */
    public ProtoBuilder(final String builderConfig, final String transform) {
        this(builderConfig, transform, null);
    }

    /**
     * Instantiates a new proto builder from the config file provided by the user. The transformation definition is also
     * provided by the user and is picked up from the config file. Also, a Context object is used for variable
     * substitution.
     *
     * @param builderConfig - The path to the config file for the corresponding json
     * @param transform     - The transformation definition that should be used from the config file.
     * @param context       - The context object
     */
    public ProtoBuilder(final String builderConfig, final String transform, final Context context) {
        this.builderConfig = builderConfig;
        this.transform = transform;
        this.context = context;
    }

    /**
     * Gives a message builder from the content provided.
     *
     * @param content - The content object that is converted to JXPathContext to build the message.
     * @return The corresponding message builder object for the input.
     */
    public Message.Builder builder(final Object content) {
        this.jXPathContext = JXPathContext.newContext(content);
        this.jXPathContext.setLenient(true);

        if (context == null) {
            return transformUsing(new Context(), builderConfig, transform);
        } else {
            return transformUsing(context, builderConfig, transform);
        }
    }

    /**
     * Gives a message builder from the content provided.
     *
     * @param content - The content object that is converted to JXPathContext to build the message.
     * @return The corresponding message object for the input.
     */
    public Message build(final Object content) {
        return this.builder(content).build();
    }

    private <T extends Message.Builder> T transformUsing(final Context vars, final String configPath,
                                                         final String definitionName) {
        Config config;
        try {
            config = configCache.get(configPath, new ConfigLoader(configPath));
        } catch (ExecutionException e) {
            throw new RuntimeException("There was a problem loading the config from the file: " + configPath);
        }

        return (T) transformUsing(vars, config, new JXPathCopier(jXPathContext, null), definitionName);
    }

    private static Message.Builder transformUsing(final Context vars, final Config config,
                                                  JXPathCopier copier, final String definitionName) {
        Config.Definition definition = config.definitions.get(definitionName);
        if (null == definition) {
            throw new IllegalArgumentException("Cannot find transform definition: " + definitionName);
        }

        if (null != definition.getProto()) {
            Message.Builder target = createMessageBuilder(definition.getProto());
            copier = new JXPathCopier(copier.getSource(), target);
        } else if (copier.getTarget() == null) {
            throw new IllegalArgumentException("proto class must be specified at the top level definition name: "
                                               + definitionName);
        }

        return applyTransforms(vars, config, copier, definition);
    }

    private static Message.Builder applyTransforms(final Context vars, final Config config, final JXPathCopier copier,
                                                   final Config.Definition definition) {
        for (Config.Entry transform : definition.getTransforms()) {
            if (transform.getDefinition() != null) {
                transformUsingDefinition(vars, config, copier, transform);
            } else if (transform.getHandler() != null) {
                transformUsingHandler(vars, config, copier, transform);
            } else {
                if (transform.getPath().startsWith("$")) {
                    Object value = vars.getValue(transform.getPath().substring(1));
                    if (value != null) {
                        copier.copyObject(value, transform.getField());
                    }
                } else {
                    copier.copyAsScalar(transform.getPath(), transform.getField());
                }

                if (transform.getVariable() != null) {
                    vars.setValue(transform.getVariable(), copier.getValue(transform.getPath()));
                }
            }
        }

        return copier.getTarget();
    }

    private static void transformUsingDefinition(final Context vars, final Config config,
                                                 final JXPathCopier copier, final Config.Entry transform) {
        boolean isRepeated = false;
        if (transform.getField() != null) {
            Descriptors.FieldDescriptor fieldDescriptor =
                copier.getTarget().getDescriptorForType().findFieldByName(transform.getField());
            if (null == fieldDescriptor) {
                throw new RuntimeException("Unknown target field in protobuf: " + transform.getField());
            }

            isRepeated = fieldDescriptor.isRepeated();
        }

        JXPathContext context = copier.getSource();
        if (isRepeated) {
            List list = context.selectNodes(transform.getPath());
            Iterator iterator = list.iterator();
            int limit = 0;
            int count = 0;

            if (transform.getLimit() != null) {
                limit = transform.getLimit();
            }
            logger.debug("Applying limit of {} for field {}", limit, transform.getField());

            while (iterator.hasNext() && (count != limit || limit == 0)) {
                Object value = iterator.next();
                JXPathCopier innerCopier = new JXPathCopier(JXPathContext.newContext(value), copier.getTarget());
                Message.Builder innerBuilder = transformUsing(vars, config, innerCopier, transform.getDefinition());
                if ((transform.getField() != null) && (null != innerBuilder) && (innerBuilder.isInitialized())) {
                    copier.copyObject(innerBuilder.build(), transform.getField());
                }
                count++;
            }
        } else {
            JXPathContext innerContext = JXPathCopier.getRelativeContext(context, transform.getPath());
            if (innerContext != null) {
                JXPathCopier innerCopier = new JXPathCopier(innerContext, copier.getTarget());
                Message.Builder innerBuilder = transformUsing(vars, config, innerCopier, transform.getDefinition());
                if ((transform.getField() != null) && (null != innerBuilder) && (innerBuilder.isInitialized())) {
                    copier.copyObject(innerBuilder.build(), transform.getField());
                }
            }
        }
    }

    private static void transformUsingHandler(final Context vars, final Config config,
                                              final JXPathCopier copier, final Config.Entry transform) {
        Descriptors.FieldDescriptor fieldDescriptor =
            copier.getTarget().getDescriptorForType().findFieldByName(transform.getField());
        if (null == fieldDescriptor) {
            throw new RuntimeException("Unknown target field in protobuf: " + transform.getField());
        }

        JXPathContext context = copier.getSource();
        Object handler = null;
        try {
            handler = Class.forName(transform.getHandler()).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to create the handler: " + transform.getHandler(), e);
        }

        if (handler instanceof ObjectToFieldHandler) {
            invokeFieldHandler(vars, copier, transform, fieldDescriptor, context,
                               (ObjectToFieldHandler) handler);
        } else if (handler instanceof ObjectToProtoHandler) {
            invokeProtoHandler(vars, copier, transform, fieldDescriptor, context,
                               (ObjectToProtoHandler) handler);
        } else {
            throw new RuntimeException(
                "Handler must implement one of the ObjectToProtoHandler or ObjectFieldHandler interface: "
                + transform.getHandler());
        }
    }

    private static void invokeProtoHandler(final Context vars, final JXPathCopier copier, final Config.Entry transform,
                                           final Descriptors.FieldDescriptor fieldDescriptor,
                                           final JXPathContext context,
                                           final ObjectToProtoHandler handler) {
        if (fieldDescriptor.isRepeated()) {
            List<Message.Builder> builders = handler.getRepeatedProtoBuilder(context, vars, transform);
            for (Message.Builder builder : builders) {
                copier.copyObject(builder.build(), transform.getField());
            }
        } else {
            Message.Builder builder = handler.getProtoBuilder(context, vars, transform);
            if (builder != null) {
                copier.copyObject(builder.build(), transform.getField());
            }
        }
    }

    private static void invokeFieldHandler(final Context vars, final JXPathCopier copier, final Config.Entry transform,
                                           final Descriptors.FieldDescriptor fieldDescriptor,
                                           final JXPathContext context,
                                           final ObjectToFieldHandler handler) {
        if (fieldDescriptor.isRepeated()) {
            List<Object> values = handler.getRepeatedProtoValue(context, vars, transform);
            for (Object value : values) {
                if (value != null) {
                    copier.copyObject(value, transform.getField());
                }
            }
        } else {
            Object value = handler.getProtoValue(context, vars, transform);
            copier.copyObject(value, transform.getField());
        }
    }

    private static Message.Builder createMessageBuilder(final String className) {
        try {
            Class messageClass = Class.forName(className);
            Method getDefaultInstanceMethod = messageClass.getMethod("getDefaultInstance", (Class[]) null);
            Message message = (Message) getDefaultInstanceMethod.invoke((Object[]) null, (Object[]) null);
            Message.Builder builder = message.newBuilderForType();
            return builder;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
