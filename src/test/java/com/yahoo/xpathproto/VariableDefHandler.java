/*
Copyright 2014 Yahoo! Inc.
Copyrights licensed under the BSD License. See the accompanying LICENSE file for terms.
*/

package com.yahoo.xpathproto;

import java.util.List;

import com.yahoo.xpathproto.dataobject.Config.Entry;
import org.apache.commons.jxpath.JXPathContext;
import com.yahoo.xpathproto.ObjectToFieldHandler;
import com.yahoo.xpathproto.dataobject.Context;

public class VariableDefHandler implements ObjectToFieldHandler {

    public VariableDefHandler() {}

    @Override
    public Object getProtoValue(final JXPathContext context, final Context vars, final Entry entry) {
        String value = (String) context.getValue(entry.getPath());
        return "handler_massaged_" + value;
    }

    @Override
    public List<Object> getRepeatedProtoValue(final JXPathContext context, final Context vars, final Entry entry) {
        throw new UnsupportedOperationException("Invalid operation");
    }

}
