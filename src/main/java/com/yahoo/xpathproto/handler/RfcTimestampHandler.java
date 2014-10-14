/*
Copyright 2014 Yahoo! Inc.
Copyrights licensed under the BSD License. See the accompanying LICENSE file for terms.
*/

package com.yahoo.xpathproto.handler;

import org.apache.commons.jxpath.JXPathContext;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import com.yahoo.xpathproto.ObjectToFieldHandler;
import com.yahoo.xpathproto.dataobject.Config;
import com.yahoo.xpathproto.dataobject.Config.Entry;
import com.yahoo.xpathproto.dataobject.Context;

public class RfcTimestampHandler implements ObjectToFieldHandler {

    private static final Logger logger = LoggerFactory.getLogger(RfcTimestampHandler.class);
    private static DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();

    @Override
    public Object getProtoValue(final JXPathContext context, final Context vars, final Config.Entry entry) {
        final Object object = context.getValue(entry.getPath());
        if (null == object) {
            return null;
        }

        return parseDate(object.toString());
    }

    public Object parseDate(final String dateStr) {
        DateTime date;
        try {
            date = formatter.parseDateTime(dateStr).toDateTime(DateTimeZone.UTC);
        } catch (final IllegalArgumentException e) {
            logger.error("Failed to parse date: " + dateStr, e);
            return null;
        }

        return date.getMillis() / 1000;
    }

    @Override
    public List<Object> getRepeatedProtoValue(final JXPathContext context, final Context vars, final Entry entry) {
        throw new UnsupportedOperationException("Invalid operation");
    }
}
