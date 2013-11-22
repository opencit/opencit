/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.rfc822;

import java.io.IOException;
import java.util.HashMap;

/**
 * Encapsulates a selection of decoders to apply to a transport-encoded message body in order to
 * extract its original form.
 * @author jbuhacoff
 */
public class MessageReader {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MessageReader.class);

    private HashMap<String,Decoder> decoders;
    public MessageReader() {
        // XXX this setup should probably go in a factory 
        decoders = new HashMap<String,Decoder>();
        decoders.put("base64", new Base64Decoder());
        decoders.put("gzip", new GzipDecoder());
        decoders.put("hex", new HexDecoder());
        decoders.put("identity", new IdentityDecoder());
    }
    /**
     * 
     * @param message
     * @return the decoded (if applicable) content of the given message after applying decoders mentioned in the Content-Transfer-Encoding header (if present)
     * @throws IOException 
     */
    public byte[] read(Message message) throws IOException {
        byte[] content = message.getContent();
        String encodingHeader = message.getContentTransferEncoding(); // the value, like "base64" or "gzip, base64"
        if( encodingHeader == null || encodingHeader.isEmpty() || encodingHeader.equals("identity") ) {
            return content; // content is not transport-encoded
        }
        String[] encodings = encodingHeader.split(",");
        for(int i=encodings.length; i>0; i--) {
            String encoding = encodings[i-1].trim(); // remove spaces that may exist between comma and encoding name
            if( decoders.containsKey(encoding) ) {
                log.debug("Decoding with {}", encoding);
                content = decoders.get(encoding).decode(content); // throws IOException
            }
        }
        return content;
    }
}
