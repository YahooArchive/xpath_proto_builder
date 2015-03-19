package com.yahoo.xpathproto.handler;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RfcTimestampHandlerTest {

    @Test
    public void testRfcTimestamp() {
        Object ts = RfcTimestampHandler.parseDate("2014-01-17T14:11:35Z");
        Assert.assertEquals(ts, new Long(1389967895));

        ts = RfcTimestampHandler.parseDate("2013-12-27T05:13:21Z");
        Assert.assertEquals(ts, new Long(1388121201));
    }

    @Test
    public void testRfcTimestampError() {
        Assert.assertNull(RfcTimestampHandler.parseDate("abc"));
    }
}
