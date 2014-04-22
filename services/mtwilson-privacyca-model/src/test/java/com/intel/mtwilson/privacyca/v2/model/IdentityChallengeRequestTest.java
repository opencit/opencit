/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.privacyca.v2.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class IdentityChallengeRequestTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdentityChallengeRequestTest.class);

    @Test
    public void testEncodeIdentityChallengeRequest() {
        // create a bogus request with fake data to test encoding/decoding
        IdentityChallengeRequest identityChallengeRequest = new IdentityChallengeRequest();
        identityChallengeRequest.setIdentityRequest(new byte[] { 0, 1, 2, 3 });
        identityChallengeRequest.setEndorsementCertificate(new byte[] { 4, 4, 4, 4 });
        byte[] der = identityChallengeRequest.toByteArray();
        log.debug("DER bytes {}", Hex.encodeHexString(der));
        
    }
    
    @Test
    public void testDecodeIdentityChallengeRequest() throws DecoderException {
        String derHex = "300c040400010203040404040404"; // it's the output of testEncodeIdentityChallengeRequest which is the asn.1 sequence tag  then the length of the first byte array (4) and those 4 bytes, then the length of the second byte array (4) and those 4 bytes
        byte[] der = Hex.decodeHex(derHex.toCharArray());
        IdentityChallengeRequest identityChallengeRequest = IdentityChallengeRequest.valueOf(der);
        log.debug("identity request bytes {}", identityChallengeRequest.getIdentityRequest());
        log.debug("endorsement certificate bytes {}", identityChallengeRequest.getEndorsementCertificate());
        assertArrayEquals(new byte[] { 0, 1, 2, 3 }, identityChallengeRequest.getIdentityRequest());
        assertArrayEquals(new byte[] { 4, 4, 4, 4 }, identityChallengeRequest.getEndorsementCertificate());
    }
    
    @Test
    public void testJsonEncode() throws JsonProcessingException {
        // create a bogus request with fake data to test encoding/decoding
        IdentityChallengeRequest identityChallengeRequest = new IdentityChallengeRequest();
        identityChallengeRequest.setIdentityRequest(new byte[] { 0, 1, 2, 3 });
        identityChallengeRequest.setEndorsementCertificate(new byte[] { 4, 4, 4, 4 });
        ObjectMapper mapper = new ObjectMapper(); // note that this will produce camelStyle properties while mtwilson api uses a lowercase_with_underscorse rule ;  so this output is NOT suitable for documentation
        log.debug("json: {}", mapper.writeValueAsString(identityChallengeRequest));
    }
    
    @Test
    public void testJsonDecode() throws Exception {
        String json = "{\"identityRequest\":\"AAECAw==\",\"endorsementCertificate\":\"BAQEBA==\"}";
        ObjectMapper mapper = new ObjectMapper();
        IdentityChallengeRequest identityChallengeRequest = mapper.readValue(json, IdentityChallengeRequest.class);
        log.debug("identity request bytes {}", identityChallengeRequest.getIdentityRequest());
        log.debug("endorsement certificate bytes {}", identityChallengeRequest.getEndorsementCertificate());
        assertArrayEquals(new byte[] { 0, 1, 2, 3 }, identityChallengeRequest.getIdentityRequest());
        assertArrayEquals(new byte[] { 4, 4, 4, 4 }, identityChallengeRequest.getEndorsementCertificate());
    }
}
