/*
Copyright 2014 Yahoo! Inc.
Copyrights licensed under the BSD License. See the accompanying LICENSE file for terms.
*/

package com.yahoo.xpathproto;

import org.apache.commons.jxpath.JXPathContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.protobuf.Message;
import com.yahoo.xpathproto.JXPathCopier;
import com.yahoo.xpathproto.ObjectToProtoHandler;
import com.yahoo.xpathproto.TransformTestProtos;
import com.yahoo.xpathproto.dataobject.Config;
import com.yahoo.xpathproto.dataobject.Context;

public class ImageHandler implements ObjectToProtoHandler {

    @Override
    public Message.Builder getProtoBuilder(JXPathContext context, Context vars, Config.Entry entry) {
        if (!entry.getPath().isEmpty()) {
            context = JXPathCopier.getRelativeContext(context, entry.getPath());
        }

        if (null == context) {
            return null;
        }

        return copyObjectToImageAsset(context);
    }

    @Override
    public List<Message.Builder> getRepeatedProtoBuilder(JXPathContext context, Context vars, Config.Entry entry) {
        List<Message.Builder> builders = new ArrayList();

        Iterator iterator = context.iterate(entry.getPath());
        while (iterator.hasNext()) {
            Object value = iterator.next();
            builders.add(copyObjectToImageAsset(JXPathContext.newContext(value)));
        }

        return builders;
    }

    public static TransformTestProtos.ContentImage.Builder copyObjectToImageAsset(JXPathContext context) {
        TransformTestProtos.ContentImage.Builder imageBuilder = TransformTestProtos.ContentImage.newBuilder();

        JXPathCopier imageCopier = new JXPathCopier(context, imageBuilder);
        imageCopier.copyAsString("content_type", "type").copyAsInteger("width").copyAsInteger("height")
            .copyAsString("url");
        return imageBuilder.isInitialized() ? imageBuilder : null;
    }

}
