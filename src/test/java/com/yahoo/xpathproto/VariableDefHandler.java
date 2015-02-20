package com.yahoo.xpathproto;

import java.util.List;

import com.yahoo.xpathproto.dataobject.Config.Entry;
import org.apache.commons.jxpath.JXPathContext;
import com.yahoo.xpathproto.ObjectToFieldHandler;
import com.yahoo.xpathproto.dataobject.Context;

public class VariableDefHandler implements ObjectToFieldHandler {

    public VariableDefHandler() {}

    @Override
    public Object getProtoValue(JXPathContext context, Context vars, Entry entry) {
        String value = (String) context.getValue(entry.getPath());
        return "handler_massaged_" + value;
    }

    @Override
    public List<Object> getRepeatedProtoValue(JXPathContext context, Context vars, Entry entry) {
        throw new UnsupportedOperationException("Invalid operation");
    }

}
