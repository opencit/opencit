/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey.provider;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.map.SerializationConfig.Feature;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * A hypothetical example JSON output using the default ObjectMapper settings 
 * might look like this (notice camelCase on connectionUrl and biosMLE):
 * 

<host_collection><hosts><host><id>623b6ebc-e6b5-4eef-a466-75d03bd12a06</id><name>hostabc</name><connectionUrl>http://1.2.3.4</connectionUrl><description>test host</description><biosMLE>bios-4.3.2</biosMLE></host></hosts></host_collection>
* 
 * 
 * But the same example object when this provider is on the classpath will
 * look like this (notice the underscores connection_url and bios_mle):
 * 

<host_collection><hosts><host><id>8d4f8fbc-b7d8-4827-9aa6-44df82ffb62f</id><name>hostabc</name><connection_url>http://1.2.3.4</connection_url><description>test host</description><bios_mle>bios-4.3.2</bios_mle></host></hosts></host_collection>
 * 
 * References:
 * 
 * https://github.com/FasterXML/jackson-dataformat-xml/wiki/Jackson-XML-annotations
 * http://stackoverflow.com/questions/14712312/how-to-serialize-java-object-as-xml-attribute-with-jackson
 * http://stackoverflow.com/questions/12904250/jackson-xml-globally-set-element-name-for-container-types
 * 
 * 
 * @author jbuhacoff
 */
@Provider
@Produces({MediaType.APPLICATION_XML,MediaType.TEXT_XML})
public class JacksonXmlMapperProvider implements ContextResolver<XmlMapper> {
 
    private final XmlMapper xmlMapper;
 
    public JacksonXmlMapperProvider() {
        xmlMapper = createDefaultMapper();
    }
 
    @Override
    public XmlMapper getContext(Class<?> type) {
        return xmlMapper;
    }
 
    private XmlMapper createDefaultMapper() {
//        JsonFactory jsonFactory = new JsonFactory();
//        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        XmlMapper mapper = new XmlMapper(/*jsonFactory*/);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        return mapper;
    }
 
}    
