/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.rfc822;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jbuhacoff
 */
public class MessageWriter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MessageWriter.class);

    private HashMap<String,Encoder> encoders = null; // all available encoders
    private ArrayList<String> encodings = null; // which encodings to apply, in order
    public MessageWriter() {
        // XXX this setup should probably go in a factory 
        encoders = new HashMap<String,Encoder>();
        encoders.put("base64", new Base64Encoder());
        encoders.put("gzip", new GzipEncoder());
        encoders.put("hex", new HexEncoder());
        encoders.put("identity", new IdentityEncoder());
        // XXX this setup should probably go in a factory, or as an optional String[] or List parameter to write()
        encodings = new ArrayList<String>();
        encodings.add("gzip");
        encodings.add("base64");
    }
    /**
     * Caller should call setContentType on the returned object to indicate the nature of the content that is enclosed in it.
     * 
     * @param content to encapsulate in a Message object after applying encodings
     * @return a new Message object with encoded content and Content-Length and Content-Transfer-Encoding headers set
     * @throws IOException 
     */
    public Message write(byte[] content) throws IOException {
        for(String encoding : encodings) {
            if( encoders.containsKey(encoding) ) {
                log.debug("Encoding with {}", encoding);
                content = encoders.get(encoding).encode(content);
            }
        }
        Message message = new Message(content);
        message.setContentTransferEncoding(StringUtils.join(encodings, ", "));
        message.setContentLength(content.length);
        message.setContent(content);
        return message;
    }
}
