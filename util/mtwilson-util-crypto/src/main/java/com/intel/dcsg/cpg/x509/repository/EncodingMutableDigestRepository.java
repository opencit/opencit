/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509.repository;

import com.intel.mtwilson.codec.ByteArrayCodec;
import com.intel.dcsg.cpg.crypto.digest.Digest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Lightweight implementation of MutableDigestRepository backed by HashSet of String
 * with automatic encoding and decoding using the specified codec, for example
 * a HexCodec or Base64Codec.
 * 
 * Because the HashSet of String doesn't capture the algorithm for each digest,
 * it is assumed when using this class that all digests are computed using the 
 * same algorithm.
 * 
 * The getDigests method returns an immutable list in accordance with the
 * DigestRepository contract. The mutable in MutableDigestRepository
 * is implemented via the addDigest method.
 * 
 * @author jbuhacoff
 */
public class EncodingMutableDigestRepository implements MutableDigestRepository {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EncodingMutableDigestRepository.class);
    private String algorithm; // MD5, SHA-1, SHA-256, SHA-384, SHA-512
    private Collection<String> data;
    private ByteArrayCodec codec;
    
    public EncodingMutableDigestRepository(Collection<String> store, ByteArrayCodec codec, String algorithm) {
        this.data = store;
        this.codec = codec;
        this.algorithm = algorithm;
    }

    @Override
    public void addDigest(Digest digest) {
        if( digest.getAlgorithm().equals(algorithm) ) {
            String encoded = codec.encode(digest.getBytes());
            log.debug("Encoded {} digest: {}", algorithm, encoded);
            data.add(encoded);
        }
        throw new IllegalArgumentException(String.format("Repository requires %s digests", algorithm));
    }

    @Override
    public List<Digest> getDigests() {
        ArrayList<Digest> digests = new ArrayList<>();
        for(String item : data) {
            Digest digest = new Digest(algorithm, codec.decode(item));
            digests.add(digest);
        }
        return Collections.unmodifiableList(digests);        
    }
}
