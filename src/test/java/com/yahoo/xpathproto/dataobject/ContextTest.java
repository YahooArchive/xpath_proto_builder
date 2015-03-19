package com.yahoo.xpathproto.dataobject;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ContextTest {

    @Test
    public void testSubstitution() {
        Context context = new Context();
        context.setValue("var", "value");

        Assert.assertEquals(context.substituteVar("var"), "var");
        Assert.assertEquals(context.substituteVar("$var"), "value");
        Assert.assertNull(context.substituteVar("$foo"));
    }
}
