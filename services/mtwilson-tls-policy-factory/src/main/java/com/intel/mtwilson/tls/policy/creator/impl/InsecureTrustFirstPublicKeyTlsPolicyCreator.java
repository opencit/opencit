/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.creator.impl;

import com.intel.dcsg.cpg.codec.ByteArrayCodec;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.tls.policy.TrustDelegate;
import com.intel.dcsg.cpg.tls.policy.impl.FirstPublicKeyTrustDelegate;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyTlsPolicy;
import com.intel.dcsg.cpg.x509.repository.EncodingMutablePublicKeyRepository;
import com.intel.dcsg.cpg.x509.repository.MutablePublicKeyRepository;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactoryUtil;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author jbuhacoff
 */
public class InsecureTrustFirstPublicKeyTlsPolicyCreator extends PublicKeyTlsPolicyCreator {
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InsecureTrustFirstPublicKeyTlsPolicyCreator.class);
    
    @Override
    public PublicKeyTlsPolicy createTlsPolicy(TlsPolicyDescriptor tlsPolicyDescriptor) {
        if( "TRUST_FIRST_CERTIFICATE".equalsIgnoreCase(tlsPolicyDescriptor.getPolicyType()) ) {
            try {
                MutablePublicKeyRepository repository = getPublicKeyRepository(tlsPolicyDescriptor);
                TrustDelegate delegate = new FirstPublicKeyTrustDelegate(repository);
                return new PublicKeyTlsPolicy(repository, delegate);
            }
            catch(CryptographyException e) {
                throw new IllegalArgumentException("Cannot create public key policy from given repository", e);
            }
        }
        return null;
    }
    
    @Override
    protected MutablePublicKeyRepository getPublicKeyRepository(TlsPolicyDescriptor tlsPolicyDescriptor) throws CryptographyException {
        if( tlsPolicyDescriptor.getData() == null ) {
            tlsPolicyDescriptor.setData(new ArrayList<String>());
        }
        ByteArrayCodec codec = getCodecForTlsPolicyDescriptor(tlsPolicyDescriptor);
        if( codec == null ) {
            codec = TlsPolicyFactoryUtil.getCodecByName("base64"); // reasonable default
            // save the codec choice back to the tls policy descriptor
            if( tlsPolicyDescriptor.getMeta() == null ) { 
                tlsPolicyDescriptor.setMeta(new HashMap<String,String>());
                tlsPolicyDescriptor.getMeta().put("encoding", "base64");
            }
        }
        EncodingMutablePublicKeyRepository repository = new EncodingMutablePublicKeyRepository(tlsPolicyDescriptor.getData(), codec);
        return repository;
    }
    
}
