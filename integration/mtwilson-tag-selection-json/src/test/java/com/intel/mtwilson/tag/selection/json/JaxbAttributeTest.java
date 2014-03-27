/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.selection.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
//import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.jaxb.XmlJaxbAnnotationIntrospector;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.junit.Test;

/**
 * http://stackoverflow.com/questions/22433679/how-to-use-jaxbannotationintrospector-correctly
 * https://github.com/FasterXML/jackson-module-jaxb-annotations/issues/6
 * https://github.com/FasterXML/jackson-module-jaxb-annotations/issues/27
 * 
 * @author jbuhacoff
 */
public class JaxbAttributeTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JaxbAttributeTest.class);

    @XmlRootElement(name="problem")
    public static class ProblemJaxb {
        @XmlAttribute(name="id")     
        public String id;
        public String description;
    }

    @Test
    public void testGenerateXmlWrong() throws JsonProcessingException {
        ProblemJaxb problem = new ProblemJaxb();
        problem.id = "aaa";
        problem.description = "test";
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.setAnnotationIntrospector(new XmlJaxbAnnotationIntrospector(xmlMapper.getTypeFactory())); // works!  <problem xmlns="" id="aaa"><description>test</description></problem>
//        xmlMapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(xmlMapper.getTypeFactory()));  // <problem xmlns=""><id>aaa</id><description>test</description></problem>
//        xmlMapper.getSerializationConfig().with(new JaxbAnnotationIntrospector(xmlMapper.getTypeFactory())); // <ProblemJaxb xmlns=""><id>aaa</id><description>test</description></ProblemJaxb>
        log.debug("ProblemJaxb: {}", xmlMapper.writeValueAsString(problem)); 
    }

    @JacksonXmlRootElement(localName="problem")
    public static class ProblemJackson {
        @JacksonXmlProperty(isAttribute=true)
        public String id;
        public String description;
    }
    
    @Test
    public void testGenerateXmlCorrect() throws JsonProcessingException {
        ProblemJackson problem = new ProblemJackson();
        problem.id = "aaa";
        problem.description = "test";
        XmlMapper xmlMapper = new XmlMapper();
        log.debug("ProblemJackson: {}", xmlMapper.writeValueAsString(problem)); // <problem xmlns="" id="aaa"><description>test</description></problem>
    }
    
    /*
    @XmlRootElement(name="problem")
    public static class ProblemJaxb2 {
        @XmlAttribute(name="id")     
        @JacksonXmlProperty(isAttribute=true)
        public String id;
        public String description;
    }
    */
    
    /*
    @Test
    public void testGenerateXml2() throws JsonProcessingException {
        ProblemJaxb2 problem = new ProblemJaxb2();
        problem.id = "aaa";
        problem.description = "test";
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(xmlMapper.getTypeFactory())); 
        log.debug("ProblemJaxb2: {}", xmlMapper.writeValueAsString(problem)); // ProblemJaxb2: <problem xmlns=""><id>aaa</id><description>test</description></problem>
    }
    */
    

}
