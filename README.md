xpath_proto_builder
==================

[![Build Status](https://travis-ci.org/yahoo/xpath_proto_builder.svg?branch=master)](https://travis-ci.org/yahoo/xpath_proto_builder)

xpath_proto_builder is a library to convert objects (JSON, XML, POJO) into protobuf using xpath notation. It primarily uses [apache's jxpath](http://commons.apache.org/proper/commons-jxpath/users-guide.html) library to query input data and [google's protobuf](https://developers.google.com/protocol-buffers/) library to populate a protobuf message.

This code is licensed under the BSD license.

How to get the Library
----------------------
The library is hosted on public Maven central and can be used by simply adding the dependency to your pom file.
Add the following lines to your pom file:
   ```xml
   <dependencies>
   ...
   ...
      <dependency>
         <groupId>com.yahoo.xpathproto</groupId>
         <artifactId>xpathproto</artifactId>
         <version>0.1.4</version>
      </dependency>
   ...
   ...
   </dependencies>
   ```
You can get the latest version number from [here](http://search.maven.org/#search%7Cga%7C1%7Cxpathproto).

Usage
-----

The xpath_proto_builder requires:
  * An input data source in json, xml or pojo form as suported by the jxpath library.
  * A target Protobuf message.
  * An xpath based configuration used to map input data source fields into protobuf.

A sample code snippet is shown below:

 ```java
    InputStream stream = ObjectTransformerTest.class.getResourceAsStream("/testdata/transformerdata.json");
    Map<String, Object> tdata = mapper.readValue(stream, Map.class);
    
    ProtoBuilder transformer = new ProtoBuilder("/testdata/transformerconfig.json");
    TransformTestProtos.TransformedMessage.Builder builder =
                    (TransformTestProtos.TransformedMessage.Builder) transformer.builder(tdata);
 ```

### Example Configuration and Protobuf

 The code below shows the protobuf and the corresponding transformation configuration:
 
 ```
 package proto.horoscopesnippet;

 option java_package = "com.yahoo.xpathproto.horoscope";
 option java_outer_classname = "HoroscopeSnippetProtos";

 message HoroscopeSnippet {
  required string id = 1;
  required string sign = 2;
  required string label = 3;
  required string title = 4;
  required Summary summary = 5;
  required string link = 6;
  optional string extended_link = 7;
  optional string author = 10;
  required string publish_date = 11;
  optional uint64 ts_update = 12;
 }

 message Summary {
  required string text = 1;
 }
 ```

 The following is an example of a complete configuration file for the protobuf message above
 
 ```json
 {
   "definitions": {
     "rss_transform": {
       "proto": "com.yahoo.xpathproto.horoscope.HoroscopeSnippetProtos$HoroscopeSnippet",
       "transforms": [
         { "path": "rss/channel/item[1]", "definition": "item_transform" }
       ]
     },

     "item_transform": {
       "transforms": [
         { "field": "id" },
         { "field": "sign" },
         { "field": "label" },
         { "field": "title" },
         { "field": "link" },
         { "field": "summary", "path": "description", "definition": "summary_transform" },
         { "field": "extended_link", "path": "link" },
         { "field": "author" },
         { "field": "publish_date", "path": "pubDate" },
         { "field": "ts_update", "handler": "com.yahoo.xpathproto.handler.TimeStampHandler" }
       ]
     },

     "summary_transform": {
       "proto": "com.yahoo.xpathproto.horoscope.HoroscopeSnippetProtos$Summary",
       "transforms": [
         { "field": "text", "path": "." }
       ]
     }

   }
 }
 ```
 
To try it out, you can get the input data from "http://shine.yahoo.com/horoscope/leo/?format=horoscopeRss". This example is also covered as one of the [unit test](/src/test/java/com/yahoo/xpathproto/horoscope/TransformTestHoroscope.java).

Transform Configuration
-----------------------
The library uses an xpath based configuration file __in json format__ to transform input data source fields into the protobuf message. The configuration supports the following type of transformations:

__Pre-requisite__: Each config file should start with a defintion field followed by the description of each definition.

```json
{
  "definitions": {
    "root_transform": {
    ...
    ...
    ...
    }
  }
}
```

1. Field Mapping

 A simple mapping for fields.
 ```json
    { "field": "foo" } // field name is the same as input path
    { "field": "foo", "path": "bar" } // field to simple path mapping
    { "field": "foo", "path": "x/y" } // field to xpath mapping
 ```
The transformation is able to handle protobuf fields that are "repeated".


2. Nested Transformations

 ```json
 {
   "definitions": {
     "root_transform": {
       "proto": "com.yahoo.xpathproto.TransformTestProtos$TransformedMessage",
       "transforms": [
           { "path": "select", "definition": "select_transform" },
       ]
     },
 
     "select_transform": {
       "transforms": [
         { "field": "nested" }
       ]
     }
   }
 }
 ```

3. Variables

 The library also supports defining variables those can be used later in the transform configuration.
 
 ```json
  { "field": "src", "path": "_src", "variable": "var_src" }
  { "field": "var_src", "path": "$var_src" }
 ```

 The user can also use the ProtoBuilder constructor that takes a Context as a parameter to pre-define variable mapping.

4. Custom Handlers

 The library also supports more complex handlers. They need to implement either of [ObjectToFieldHandler](/src/main/java/com/yahoo/xpathproto/ObjectToFieldHandler.java) or [ObjectToProtoHandler](/src/main/java/com/yahoo/xpathproto/ObjectToProtoHandler.java) depending on the requirement.

 For example, the below two handlers implement ObjectToFieldHandler
 1. [RfcTimestampHandler](/src/main/java/com/yahoo/xpathproto/handler/RfcTimestampHandler.java)
 2. [TimeStampHandler](/src/main/java/com/yahoo/xpathproto/handler/TimeStampHandler.java)
 

 There is also a reference class that implements ObjectToProtoHandler [here](/src/test/java/com/yahoo/xpathproto/ImageHandler.java).


3rd Party Packages not distributed with this project
----------------------------------------------------
The xpath_proto_builder project uses several 3rd party open source libraries and tools.
This file summarizes the tools used, their purpose, and the licenses under which they're released.

Except as specifically stated below, the 3rd party software packages are not distributed as part of this project, but instead are separately downloaded from the respective provider and built on the developer’s machine as a pre-build step.

##### Protobuf version 2.5.0
(Protocol buffers are Google's language-neutral, platform-neutral, extensible mechanism for serializing structured data)
https://developers.google.com/protocol-buffers/

##### Guava version 12.0.1
(The Guava project contains several of Google's core libraries used in Java-based projects)
https://code.google.com/p/guava-libraries/

##### Faster XML Jackson Core and Databind
(This project contains core low-level incremental ("streaming") parser and generator abstractions used by Jackson Data Processor)
https://github.com/FasterXML/jackson-core
https://github.com/FasterXML/jackson-databind

##### slf4j Logger version 1.7.5
(The Simple Logging Facade for Java (SLF4J) serves as a simple facade or abstraction for various logging frameworks (e.g. java.util.logging, logback, log4j) allowing the end user to plug in the desired logging framework at deployment time.)
http://repo2.maven.org/maven2/org/slf4j/

##### TestNG version 6.8.7
(TestNG is a testing framework inspired from JUnit and NUnit but introducing some new functionalities that make it more powerful and easier to use)
http://search.maven.org/remotecontent?filepath=org/testng/testng/6.8.8/testng-6.8.8.jar

##### Joda Time version 2.3
(Joda-Time provides a quality replacement for the Java date and time classes.)
https://github.com/JodaOrg/joda-time/releases/download/v2.4/joda-time-2.4-dist.tar.gz

##### Apache Commons JXPath, Cli, IO, Lang
(The Commons is an Pache project focused on all aspects of reusable Java components)
http://commons.apache.org/

