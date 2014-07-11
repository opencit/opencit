/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tls.policy.jaxrs.resource;

import com.intel.mtwilson.tls.policy.model.HostTlsPolicyCollection;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyFilterCriteria;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyLocator;
import com.intel.mtwilson.tls.policy.repository.HostTlsPolicyRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Path;

/**
 * Example registration of new certificate policy with one certificate:
 * 
 * <pre>
 * {"name":"new policy",
 * "private":false,
 * "comment":"for example",
 * "descriptor":{"policy_type":"certificate",
 * "meta":{"encoding":"base64"},
 * "data":["MIIBwzCCASygAwIBAgIJANE6wc0/mOjZMA0GCSqGSIb3DQEBCwUAMBExDz
 * ANBgNVBAMTBnRlc3RjYTAeFw0xNDA2MjQyMDQ1MjdaFw0xNDA3MjQyMDQ1MjdaMBExD
 * zANBgNVBAMTBnRlc3RjYTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAt9EmIilK
 * 3qSRGMRxEtcGj42dsJUf5h2OZIG25Er7dDxJbdw6KrOQhVUUx+2DUOQLMsr3sJt9D5e
 * yWC4+vhoiNRMUjamR52/hjIBosr2XTfWKdKG8NsuDzwljHkB/6uv3P+AfQQ/eStXc42
 * cv8J6vZXeQF6QMf63roW8i6SNYHwMCAwEAAaMjMCEwDgYDVR0PAQH/BAQDAgIEMA8GA
 * 1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADgYEAXov/vFVOMAznD+BT8tBfAT1R
 * /nWFmrFB7os4Ry1mYjbr0lrW2vtUzA2XFx6nUzafYdyL1L4PnI7LGYqRqicT6WzGb1g
 * rNTJUJhrI7FkGg6TXQ4QSf6EmcEwsTlGHk9rxp9YySJt/xrhboP33abdXMHUWOXnJEH
 * u4la8tnuzwSvM="]}}
 * </pre>
 * 
 * @author ssbangal
 */
@V2
@Path("/tls-policies")
public class HostTlsPolicyResource extends AbstractJsonapiResource<com.intel.mtwilson.tls.policy.model.HostTlsPolicy, HostTlsPolicyCollection, HostTlsPolicyFilterCriteria, NoLinks<com.intel.mtwilson.tls.policy.model.HostTlsPolicy>, HostTlsPolicyLocator> {

    private HostTlsPolicyRepository repository;
    
    public HostTlsPolicyResource() {
        repository = new HostTlsPolicyRepository();
    }
    
    @Override
    protected HostTlsPolicyRepository getRepository() { return repository; }

    
    @Override
    protected HostTlsPolicyCollection createEmptyCollection() {
        return new HostTlsPolicyCollection();
    }
      
}
