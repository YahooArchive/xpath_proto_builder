/*
Copyright 2014 Yahoo! Inc.
Copyrights licensed under the BSD License. See the accompanying LICENSE file for terms.
*/

package com.yahoo.xpathproto.horoscope;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yahoo.xpathproto.ProtoBuilder;

public class TransformTestHoroscope {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testHoroscope() throws Exception {
        String body =
            "<rss version=\"2.0\"><channel><title>Yahoo! Astrology - Daily Horoscope for Aries</title><link>http://shine.yahoo.com/horoscope/aries/overview-daily-20130507.html</link><description>Get your Daily Aries forecast from Yahoo! Astrology</description><language>en-US</language><lastBuildDate>Tue, 07 May 2013 00:00:00 +0000</lastBuildDate><ttl>86400</ttl><image><title>Yahoo! Astrology - Daily Horoscope for Aries</title><width>150</width><height>125</height><link>#</link><url>http://l.yimg.com/os/mit/media/m/astrology/images/astro_symbols_solar_large_aries-460140.png</url></image><item><title>Daily Overview for Aries provided by Astrology.com</title><link>http://shine.yahoo.com/horoscope/aries/overview-daily-20130507.html</link><pubDate>Tue, 07 May 2013 00:00:00 +0000</pubDate><description><![CDATA[ See if you can get your friends or colleagues to follow along as you make your way through today's exciting events -- some may lag behind, but the ones who stay close are in for quite a ride! <ul><li><a href=\"http://shine.yahoo.com/team-mom/easy-mothers-day-breakfast-casserole-161800925.html\">An Easy Mother's Day Breakfast Casserole</a></li><li><a href=\"http://shine.yahoo.com/healthy-living/study-finds-cure-gray-hair-8212-finally-133700194.html\">New Study Finds a Cure for Gray Hair&#8212;Finally</a></li><li><a href=\"http://shine.yahoo.com/shine-food/slow-cooker-sweets-delicious-bread-pudding-crock-pot-180600415.html\">Slow-cooker Sweets: How to Make Delicious Bread Pudding in Your Crock Pot</a></li><li><a href=\"http://shine.yahoo.com/secrets-to-your-success/first-name-boost-salary-181600198.html\">Can Your First Name Boost Your Salary?</a></li><li><a href=\"http://shine.yahoo.com/shine-food/foods-smell-151700015.html\">Foods that Make You Smell</a></li></ul> ]]></description></item></channel></rss><!-- fe409.global.media.gq1.yahoo.com uncompressed/chunked Tue May 7 19:14:59 UTC 2013 -->";
        Document document = loadXml(body);

        ProtoBuilder transformer = new ProtoBuilder("/testdata/transform_horoscope_config.json", "rss_transform");
        HoroscopeSnippetProtos.HoroscopeSnippet.Builder builder =
            (HoroscopeSnippetProtos.HoroscopeSnippet.Builder) transformer.builder(document);

        builder.setId("ARI");
        builder.setSign("ARI");
        builder.setLabel("Aries");

        System.out.println(builder.build());

        Assert.assertEquals(builder.getTitle(), "Daily Overview for Aries provided by Astrology.com");
        Assert.assertEquals(builder.getLink(), "http://shine.yahoo.com/horoscope/aries/overview-daily-20130507.html");
        Assert.assertEquals(builder.getPublishDate(), "Tue, 07 May 2013 00:00:00 +0000");
    }

    private Document loadXml(String body) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(body.getBytes()));
    }
}
