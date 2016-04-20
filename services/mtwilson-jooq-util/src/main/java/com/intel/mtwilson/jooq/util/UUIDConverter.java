/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jooq.util;

import com.intel.dcsg.cpg.io.UUID;
import org.jooq.Converter;

/**
 *
 * @author jbuhacoff
 */
public class UUIDConverter implements Converter<Object,UUID> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UUIDConverter.class);
    
    @Override
    public UUID from(Object t) {
        if( t == null ) { return null; }
        log.debug("field class {}", t.getClass().getName());
        log.debug("field value {}", t);
        if( t instanceof String ) {
            log.debug("field string bytes length {}", ((String)t).getBytes().length);
//            return UUID.valueOf((String)t);
            if( ((String)t).getBytes().length == 16 ) {
                log.debug("converting from 16-bytes uuid string {}", ((String)t).getBytes());
                return UUID.valueOf(((String)t).getBytes());
            }
            if( ((String)t).getBytes().length == 36 || ((String)t).getBytes().length == 32 ) {
                log.debug("converting from 36-char uuid string {}", ((String)t));
                return UUID.valueOf((String)t);
            }
            throw new UnsupportedOperationException("Unrecognized UUID string format");
        }
        if( t instanceof byte[] ) {
            log.debug("field byte array length {}", ((byte[])t).length);
            if( ((byte[])t).length == 16 ) {
                return UUID.valueOf((byte[])t);
            }
            if( ((byte[])t).length == 36 || ((byte[])t).length == 32 ) {
                return UUID.valueOf(new String((byte[])t));
            }
        }
        if( t instanceof java.util.UUID ) {
            return UUID.valueOf( (java.util.UUID)t);
        }
        if( t instanceof java.math.BigInteger ) {
            return UUID.valueOf( (java.math.BigInteger)t);
        }
        throw new UnsupportedOperationException("Unsupported type for UUID: "+t.getClass().getName());
    }

    @Override
    public Object to(UUID u) {
        return u.toString();
    }

    @Override
    public Class<Object> fromType() {
        return Object.class;
    }

    @Override
    public Class<UUID> toType() {
        return UUID.class;
    }
    
}
