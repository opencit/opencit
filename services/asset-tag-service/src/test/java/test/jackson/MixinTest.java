/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import test.restlet.*;
import com.intel.dcsg.cpg.io.UUID;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.io.ByteArray;
import com.intel.dcsg.cpg.net.InternetAddress;
import com.intel.dcsg.cpg.validation.Model;
import com.intel.dcsg.cpg.validation.ObjectModel;
import com.intel.mtwilson.atag.resource.CertificateResource;
import java.util.Iterator;
import java.util.Stack;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.restlet.data.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class MixinTest {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Test
    public void writeRevokeAction() throws IOException {
        CertificateResource.CertificateRevokeAction revoke = new CertificateResource.CertificateRevokeAction();
        revoke.setUuid(new UUID());
        ObjectMapper mapper = new ObjectMapper();
        log.debug("Revoke action: {}", mapper.writeValueAsString(revoke));  
        //  Revoke action: {"uuid":"9f564dbb-65da-47a1-ba5e-6deff70322fb","effective":1377887432003,"action":"REVOKE","valid":true,"faults":[]}
    }
    
    /**
     * Reference:  http://wiki.fasterxml.com/JacksonMixInAnnotations
     * 
     * Not working yet.  Example output:
2013-08-30 11:57:12,703 DEBUG [main] t.j.MixinTest [MixinTest.java:57] Actions: [{"action":"REVOKE"},{"action":"PROVISION","provision":{"uuid":null,"host":"localhost","action":"PROVISION","valid":true,"faults":[]}}]
2013-08-30 11:57:12,713 DEBUG [main] t.j.MixinTest [MixinTest.java:61] Actions with mixin: [{"action":"REVOKE"},{"action":"PROVISION","provision":{"uuid":null,"host":"localhost","action":"PROVISION","valid":true,"faults":[]}}]
2013-08-30 11:57:12,715 DEBUG [main] t.j.MixinTest [MixinTest.java:66] Actions with mixin on subclass: [{"action":"REVOKE"},{"action":"PROVISION","provision":{"uuid":null,"host":"localhost","action":"PROVISION","valid":true,"faults":[]}}]
     * 
     * The goal is to hide the "valid" attribute from ObjectModel or to rename it "accepted".
     * 
     * @throws IOException 
     */
    @Test
    public void writeActionChoices() throws IOException {
        CertificateResource.CertificateActionChoice[] choices = new CertificateResource.CertificateActionChoice[2];
        choices[0] = new CertificateResource.CertificateActionChoice();
        choices[0].action = CertificateResource.CertificateActionName.REVOKE;
        choices[1] = new CertificateResource.CertificateActionChoice();
        choices[1].action = CertificateResource.CertificateActionName.PROVISION;
        choices[1].provision = new CertificateResource.CertificateProvisionAction();
        choices[1].provision.setHost(new InternetAddress("localhost"));
        
        ObjectMapper mapper = new ObjectMapper();        
        log.debug("Actions: {}", mapper.writeValueAsString(choices));  

        mapper.addMixInAnnotations(Model.class, ObjectModelMixin.class);
        
        // not working on base class
        mapper.addMixInAnnotations(ObjectModel.class,ObjectModelMixin.class);
        log.debug("Actions with mixin: {}", mapper.writeValueAsString(choices));  
        
        // try on the subclasses
        mapper.addMixInAnnotations(CertificateResource.CertificateProvisionAction.class,ObjectModelMixin.class);
        mapper.addMixInAnnotations(CertificateResource.CertificateRevokeAction.class,ObjectModelMixin.class);
        log.debug("Actions with mixin on subclass: {}", mapper.writeValueAsString(choices));  
        
    }

    /*
    public static abstract class ObjectModelMixin {
//        ObjectModelMixin(@JsonProperty("width") int w, @JsonProperty("height") int h) { }

//          @JsonProperty("accepted") abstract boolean isValid(); // rename property
          @JsonIgnore abstract boolean isValid(); // we don't need it!
  
}    */
        public interface ObjectModelMixin {
            @JsonProperty("accepted") boolean isValid();
        }
    
}
