/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.client.JaxrsClientBuilder;
import com.intel.mtwilson.jaxrs2.client.JsonResource;
import com.intel.mtwilson.tag.model.KvAttribute;
import com.intel.mtwilson.tag.model.KvAttributeCollection;
import java.util.Properties;
import javax.ws.rs.client.WebTarget;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class TestKvAttributesJsonResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestKvAttributesJsonResource.class);

    public static class KvAttributesJsonResource<KvAttribute> extends JsonResource {
        private WebTarget target;
        public KvAttributesJsonResource(WebTarget target) {
            super(target);
        }
    }
    
    @Test
    public void testKvAttributeCreate() throws Exception {
        Properties properties = new Properties();
        KvAttributesJsonResource kvAttributes = new KvAttributesJsonResource(JaxrsClientBuilder.factory().configuration(properties).build().getTarget());
        KvAttribute kvAttribute = new KvAttribute();
        kvAttribute.setId(new UUID());
        kvAttributes.create(new KvAttribute());
        kvAttributes.retrieve(kvAttribute.getId());
        kvAttributes.delete(kvAttribute.getId());
    }
}
