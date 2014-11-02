/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.codec;

import com.thoughtworks.xstream.XStream;
import java.nio.charset.Charset;
/**
 *
 * @author jbuhacoff
 */
public class XStreamCodec implements ObjectCodec {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XStreamCodec.class);

    private XStream xs = new XStream();

    @Override
    public byte[] encode(Object input) {
        log.debug("encode object to byte[]");
        return xs.toXML(input).getBytes(Charset.forName("UTF-8"));
    }

    @Override
    public Object decode(byte[] encoded) {
        log.debug("decode byte[] to object");
        return xs.fromXML(new String(encoded, Charset.forName("UTF-8")));
    }

}
