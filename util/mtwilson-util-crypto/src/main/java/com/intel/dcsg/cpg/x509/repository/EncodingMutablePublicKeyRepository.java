/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509.repository;

import com.intel.mtwilson.codec.ByteArrayCodec;
import com.intel.mtwilson.codec.ObjectCodec;
import com.intel.dcsg.cpg.crypto.PublicKeyCodec;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Lightweight implementation of MutablePublicKeyRepository backed by HashSet of
 * String with automatic encoding and decoding using the specified codec, for
 * example a HexCodec or Base64Codec.
 *
 * The getPublicKeys method returns an immutable list in accordance with the
 * PublicKeyRepository contract. The mutable in MutablePublicKeyRepository is
 * implemented via the addPublicKey method.
 *
 * @author jbuhacoff
 */
public class EncodingMutablePublicKeyRepository implements MutablePublicKeyRepository {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EncodingMutablePublicKeyRepository.class);
    private Collection<String> data;
    private ByteArrayCodec byteArrayCodec;
    private PublicKeyCodec publicKeyCodec;

    public EncodingMutablePublicKeyRepository(Collection<String> store, ByteArrayCodec byteArrayCodec) {
        this.data = store;
        this.byteArrayCodec = byteArrayCodec;
        this.publicKeyCodec = new PublicKeyCodec(); // accepts binary public key or public key certificate data so we have flexibility on the input
    }

    @Override
    public void addPublicKey(PublicKey publicKey) {
        String encoded = byteArrayCodec.encode(publicKey.getEncoded());
        log.debug("Encoded public key: {}", encoded);
        data.add(encoded);
    }

    @Override
    public List<PublicKey> getPublicKeys() {
        ArrayList<PublicKey> publicKeys = new ArrayList<>();
        for (String item : data) {
            try {
                PublicKey publicKey = publicKeyCodec.decode(byteArrayCodec.decode(item));
                publicKeys.add(publicKey);
            } catch (Exception e) {
                log.warn("Cannot decode public key: {}", item, e);
            }
        }
        return Collections.unmodifiableList(publicKeys);
    }
}
