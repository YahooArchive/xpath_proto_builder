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
    public Message.Builder getProtoBuilder(final JXPathContext context, final Context vars, final Config.Entry entry) {
        JXPathContext curContext = context;
        if (!entry.getPath().isEmpty()) {
            curContext = JXPathCopier.getRelativeContext(context, entry.getPath());
        }

        if (null == curContext) {
            return null;
        }

        return copyObjectToImageAsset(curContext);
    }

    @Override
    public List<Message.Builder> getRepeatedProtoBuilder(
        final JXPathContext context, final Context vars, final Config.Entry entry) {
        List<Message.Builder> builders = new ArrayList();

        Iterator iterator = context.iterate(entry.getPath());
        while (iterator.hasNext()) {
            Object value = iterator.next();
            builders.add(copyObjectToImageAsset(JXPathContext.newContext(value)));
        }

        return builders;
    }

    public static TransformTestProtos.ContentImage.Builder copyObjectToImageAsset(final JXPathContext context) {
        TransformTestProtos.ContentImage.Builder imageBuilder = TransformTestProtos.ContentImage.newBuilder();

        JXPathCopier imageCopier = new JXPathCopier(context, imageBuilder);
        imageCopier.copyAsString("content_type", "type").copyAsInteger("width").copyAsInteger("height")
            .copyAsString("url");
        return imageBuilder.isInitialized() ? imageBuilder : null;
    }

}
