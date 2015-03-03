/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.tpm12.x509;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DEROctetString;

/**
 * OID: 2.5.4.789.2
 * @author jbuhacoff
 */
public class TpmCertifyKeySignature extends ASN1Encodable {
    public final static String OID = "2.5.4.133.3.2.41.1";
    private DEROctetString bytes;
    
    public TpmCertifyKeySignature(byte[] bytes) {
        this.bytes = new DEROctetString(bytes);
    }
    
    public static TpmCertifyKeySignature valueOf(byte[] asn1) throws IOException {
        ASN1Object object = ASN1Object.fromByteArray(asn1);
        DEROctetString der = (DEROctetString)object.getDERObject();
        return new TpmCertifyKeySignature(der.getOctets());
    }
    
    public byte[] getBytes() {
        return bytes.getOctets();
    }
    
    @Override
    public DERObject toASN1Object() {
        return bytes;
    }
    
}
