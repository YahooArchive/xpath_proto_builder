/*
Copyright 2014 Yahoo! Inc.
Copyrights licensed under the BSD License. See the accompanying LICENSE file for terms.
*/

package com.yahoo.xpathproto.dataobject;

import java.util.Map;
import java.util.TreeMap;

public class Context {

    private final Map<String, Object> variables = new TreeMap<>();

    public Object getValue(final String name) {
        return variables.get(name);
    }

    public void setValue(final String name, final Object value) {
        variables.put(name, value);
    }

    @Override
    public String toString() {
        return "Context [variables=" + variables + "]";
    }
}
