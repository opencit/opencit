/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.model.x509;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * OID: 2.5.4.789.2
 * @author jbuhacoff
 */
public class UTF8NameValueSequence extends ASN1Encodable {
    public final static String OID = "2.5.4.789.2";
    private DERUTF8String name;
    private DERSequence values;
    
    public UTF8NameValueSequence(String name, String... values) {
        this.name = new DERUTF8String(name);
        ASN1EncodableVector v = new ASN1EncodableVector();
        for(String value : values) {
            v.add(new DERUTF8String(value));
        }
        this.values = new DERSequence(v);
    }
    
    public UTF8NameValueSequence(DERUTF8String name, DERSequence values) {
        this.name = name;
        this.values = values;
    }
    
    public UTF8NameValueSequence(ASN1Sequence sequence) {
        this.name = DERUTF8String.getInstance(sequence.getObjectAt(0));
        this.values = (DERSequence) DERSequence.getInstance(sequence.getObjectAt(1)).getDERObject(); //new DERSequence(sequence.getObjectAt(1));
    }

    @JsonIgnore
    public DERUTF8String getNameDER() {
        return name;
    }

    @JsonIgnore
    public DERSequence getValuesDER() {
        return values;
    }
    
    public String getName() {
        return name.getString();
    }
    
    public List<String> getValues() {
        ArrayList<String> list = new ArrayList<>();
        Enumeration e = values.getObjects();
        while(e.hasMoreElements()) {
            list.add(DERUTF8String.getInstance(e.nextElement()).getString());
        }
        return list;
    }
    
    
    @Override
    public DERObject toASN1Object() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(name);
        v.add(values);
        return new DERSequence(v);
    }
    
}
