/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.reader.impl;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyTlsPolicy;
import com.intel.dcsg.cpg.x509.repository.HashSetMutablePublicKeyRepository;
import com.intel.dcsg.cpg.x509.repository.PublicKeyRepository;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyReader;
import java.security.PublicKey;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class PublicKeyDescriptor implements TlsPolicyReader {

    @Override
    public TlsPolicy createTlsPolicy(TlsPolicyDescriptor tlsPolicyDescriptor) {
        if( "public-key".equalsIgnoreCase(tlsPolicyDescriptor.getName()) ) {
            try {
                PublicKeyRepository repository = getPublicKeyRepository(tlsPolicyDescriptor);
                return new PublicKeyTlsPolicy(repository); //return TlsPolicyBuilder.factory().strict(repository).skipHostnameVerification().build();
            }
            catch(CryptographyException e) {
                throw new IllegalArgumentException("Cannot create public key policy from given repository", e);
            }
        }
        return null;
    }
    
    private PublicKeyRepository getPublicKeyRepository(TlsPolicyDescriptor tlsPolicyDescriptor) throws CryptographyException {
        HashSetMutablePublicKeyRepository repository = new HashSetMutablePublicKeyRepository();
        if( "public-key".equals(tlsPolicyDescriptor.getName()) && tlsPolicyDescriptor.getData() != null  ) {
            for(String publicKeyBase64 : tlsPolicyDescriptor.getData()) {
                PublicKey publicKey = RsaUtil.decodeDerPublicKey(Base64.decodeBase64(publicKeyBase64));
                repository.addPublicKey(publicKey);
            }
            return repository;
        }
        return null;
    }
    
    public static class PublicKeyMetadata {
        public String encoding; // base64
    }
}
