/*
Copyright 2014 Yahoo! Inc.
Copyrights licensed under the BSD License. See the accompanying LICENSE file for terms.
*/

package com.yahoo.xpathproto;

import org.apache.commons.jxpath.JXPathContext;

import java.util.ArrayList;
import java.util.List;

import com.yahoo.xpathproto.dataobject.Config.Entry;
import com.yahoo.xpathproto.dataobject.Context;

public class DummyObjectToFieldHandler implements ObjectToFieldHandler {

    @Override
    public Object getProtoValue(final JXPathContext context, final Context vars, final Entry entry) {
        return new String("str_values_main");
    }

    @Override
    public List<Object> getRepeatedProtoValue(final JXPathContext context, final Context vars, final Entry entry) {
        @SuppressWarnings("serial")
        List<Object> strValues = new ArrayList<Object>() {
            {
                add("Main");
                add("Secondary");
            }
        };
        return strValues;
    }

}
