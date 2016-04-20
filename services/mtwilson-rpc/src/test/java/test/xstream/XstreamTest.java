/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.xstream;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import org.junit.Test;
import com.intel.mtwilson.rpc.v2.model.Rpc;
import com.intel.mtwilson.rpc.v2.model.RpcPriv;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.thoughtworks.xstream.XStream;
import java.security.cert.X509Certificate;

/**
 *
 * @author jbuhacoff
 */
public class XstreamTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XstreamTest.class);
    
    @Test
    public void testWriteObject() throws Exception {
        // make a sample object
        RpcPriv rpc = new RpcPriv();
        rpc.setId(new UUID());
        rpc.setName("test rpc");
        rpc.setStatus(Rpc.Status.QUEUE);
        rpc.setInput(new byte[] { 0, 1, 2, 3 });
        X509Certificate cert = X509Builder.factory().selfSigned("CN=Test", RsaUtil.generateRsaKeyPair(1024)).build();
        rpc.setOutput(cert.getEncoded());
        
        // write to xml
        XStream xs = new XStream();
        String xml = xs.toXML(rpc);
        log.debug("xml: {}", xml);
        
        // read from xml
        Object pojo = xs.fromXML(xml);
        log.debug("pojo: {}", pojo.getClass().getName());
        RpcPriv rpc2 = (RpcPriv)pojo;
        log.debug("id: {}" , rpc2.getId());
        log.debug("status: {}", rpc2.getStatus());
        log.debug("input: {}", rpc2.getInput());
        X509Certificate cert2 = X509Util.decodeDerCertificate(rpc2.getOutput());
        log.debug("cert: {}", cert2.getSubjectX500Principal().getName());
    }
}
