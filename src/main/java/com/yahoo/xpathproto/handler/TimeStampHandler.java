/*
Copyright 2014 Yahoo! Inc.
Copyrights licensed under the BSD License. See the accompanying LICENSE file for terms.
*/

package com.yahoo.xpathproto.handler;

import org.apache.commons.jxpath.JXPathContext;

import java.util.List;

import com.yahoo.xpathproto.ObjectToFieldHandler;
import com.yahoo.xpathproto.dataobject.Config;
import com.yahoo.xpathproto.dataobject.Config.Entry;
import com.yahoo.xpathproto.dataobject.Context;

public class TimeStampHandler implements ObjectToFieldHandler {

    @Override
    public Object getProtoValue(final JXPathContext context, final Context vars, final Config.Entry entry) {
        long curTime = System.currentTimeMillis() / 1000;
        return curTime;
    }

    @Override
    public List<Object> getRepeatedProtoValue(final JXPathContext context, final Context vars, final Entry entry) {
        throw new UnsupportedOperationException("Invalid operation");
    }
}
