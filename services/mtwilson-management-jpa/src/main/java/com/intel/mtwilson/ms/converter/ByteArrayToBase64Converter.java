/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.ms.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.apache.commons.codec.binary.Base64;
//import org.eclipse.persistence.internal.oxm.conversion.Base64;
 
/**
 *
 * @author jbuhacoff
 */
@Converter
public class ByteArrayToBase64Converter implements AttributeConverter<byte[],String> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ByteArrayToBase64Converter.class);
    
    @Override
    public String convertToDatabaseColumn(byte[] attribute) {
        if( attribute == null ) { return null; }
        try {
            log.debug("Converting {} bytes to base64: {}", attribute.length, attribute);
            return Base64.encodeBase64String(attribute);
        }
        catch(Exception e) {
            log.error("Failed to base64-encode {} bytes", attribute.length, e);
            return null;
        }
    }

    @Override
    public byte[] convertToEntityAttribute(String dbData) {
        if( dbData == null ) { return null; }
        try {
            log.debug("Converting {} base64 bytes to data: {}", dbData.length(), dbData);
            return Base64.decodeBase64(dbData);
        }
        catch(Exception e) {
            log.error("Failed to base64-decode {} bytes", dbData.length(), e);
            return null;
        }
    }
    
}
