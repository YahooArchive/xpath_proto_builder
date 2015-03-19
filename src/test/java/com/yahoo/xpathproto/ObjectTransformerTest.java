/*
Copyright 2014 Yahoo! Inc.
Copyrights licensed under the BSD License. See the accompanying LICENSE file for terms.
*/

package com.yahoo.xpathproto;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import com.yahoo.xpathproto.TransformTestProtos.MessageEnum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yahoo.xpathproto.handler.RfcTimestampHandler;

public class ObjectTransformerTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testJson() throws Exception {

        InputStream tdatastream = ObjectTransformerTest.class.getResourceAsStream("/testdata/transformerdata.json");
        Map<String, Object> tdata = mapper.readValue(tdatastream, Map.class);

        ProtoBuilder transformer = new ProtoBuilder("/testdata/transformerconfig.json", "test_transform");
        TransformTestProtos.TransformedMessage.Builder builder =
            (TransformTestProtos.TransformedMessage.Builder) transformer.builder(tdata);

        System.out.println(builder.build());

        TransformTestProtos.TransformedMessage.Builder testBuilder =
            TransformTestProtos.TransformedMessage.newBuilder();
        testBuilder.setSrc("src")
            .setVarSrc("src")
            .setSrcPath("src/path")
            .setStringValue("string_value")
            .setIntValue(100)
            .setLongValue(1000)
            .setBoolValue(true)
            .addStrValues("v1")
            .addStrValues("v2")
            .setNested("select/nested")
            .setTsUpdate(builder.getTsUpdate())
            .setEnumValue(MessageEnum.FIRST)
            .setConstantValue("constant")
            .setVarValue("var_value")
            .setImageByTransform(
                TransformTestProtos.ContentImage.newBuilder().setUrl("foo").setHeight(100)
                    .setWidth(100))
            .addImagesByTransform(TransformTestProtos.ContentImage.newBuilder().setUrl("image1"))
            .addImagesByTransform(TransformTestProtos.ContentImage.newBuilder().setUrl("image2"))
            .setImageByHandler(
                TransformTestProtos.ContentImage.newBuilder().setUrl("foo").setHeight(100)
                    .setWidth(100))
            .addImagesByHandler(TransformTestProtos.ContentImage.newBuilder().setUrl("image1"))
            .addImagesByHandler(TransformTestProtos.ContentImage.newBuilder().setUrl("image2"));

        Assert.assertEquals(testBuilder.build(), builder.build());
    }

    @Test
    public void testObjectToFieldTranformer() throws Exception {
        InputStream tdatastream = ObjectTransformerTest.class.getResourceAsStream("/testdata/transformerdata.json");

        @SuppressWarnings("unchecked")
        Map<String, Object> tdata = mapper.readValue(tdatastream, Map.class);
        ProtoBuilder transformer =
            new ProtoBuilder("/testdata/transformer_field_handler_config.json", "test_transform");

        TransformTestProtos.TransformedMessage.Builder builder =
            (TransformTestProtos.TransformedMessage.Builder) transformer.builder(tdata);

        ArrayList<String> expectedStrValues = new ArrayList<String>() {
            {
                add("Main");
                add("Secondary");
            }
        };

        Assert.assertEquals(builder.getSrcPath(), "str_values_main");
        Assert.assertEquals(builder.getStrValuesCount(), 2);
        Assert.assertEquals(builder.getStrValuesList(), expectedStrValues);
    }
}
