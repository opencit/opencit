/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.mtwilson.jackson.PublicKeyDeserializer;
import com.intel.mtwilson.jackson.PublicKeySerializer;
import java.security.KeyPair;
import java.security.PublicKey;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author jbuhacoff
 */
public class JacksonAbstractClassTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JacksonAbstractClassTest.class);
    
    public static abstract class A {
        
    }
    public static class B extends A {
        public String b;
    }
    public static class C extends A {
        public String c;
    }
    
    private static ObjectMapper json;
        
    @BeforeClass
    public static void createMapper() {
        json = new ObjectMapper();
        json.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        json.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        json.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        json.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }    
    @Test
    public void testAbstractClass() throws Exception {
        B b = new B();
        b.b = "ok b";
        String jsonB = json.writeValueAsString(b);
        log.debug("B: {}", jsonB);
        
        A a = json.readValue(jsonB, A.class);
        String jsonA = json.writeValueAsString(a);
        log.debug("A: {}", jsonA);
    }
    
}
