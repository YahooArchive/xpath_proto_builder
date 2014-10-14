/*
Copyright 2014 Yahoo! Inc.
Copyrights licensed under the BSD License. See the accompanying LICENSE file for terms.
*/

package com.yahoo.xpathproto;

import org.apache.commons.jxpath.JXPathContext;

import java.util.List;

import com.yahoo.xpathproto.dataobject.Config;
import com.yahoo.xpathproto.dataobject.Context;

import com.google.protobuf.Message;

/**
 * This interface is used to define a custom mapping to an entire message in the protobuf. It also has support for
 * repeated proto values. In case repeated fields are not supported, an UnsupportedOperationException can be thrown in
 * the method definition.
 */
public interface ObjectToProtoHandler {

    Message.Builder getProtoBuilder(JXPathContext context, Context vars, Config.Entry entry);

    List<Message.Builder> getRepeatedProtoBuilder(JXPathContext context, Context vars, Config.Entry entry);
}
