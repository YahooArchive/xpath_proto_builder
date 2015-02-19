/*
Copyright 2014 Yahoo! Inc.
Copyrights licensed under the BSD License. See the accompanying LICENSE file for terms.
*/

package com.yahoo.xpathproto;

import org.apache.commons.jxpath.JXPathContext;

import java.util.List;

import com.yahoo.xpathproto.dataobject.Config;
import com.yahoo.xpathproto.dataobject.Context;

/**
 * This interface is used to define a custom handler to provide custom mapping to a field in the protobuf. It also has
 * support for repeated proto values. In case repeated fields are not supported, an UnsupportedOperationException can be
 * thrown in the method definition.
 */
public interface ObjectToFieldHandler extends CustomHandler {

    Object getProtoValue(JXPathContext context, Context vars, Config.Entry entry);

    List<Object> getRepeatedProtoValue(JXPathContext context, Context vars, Config.Entry entry);
}
