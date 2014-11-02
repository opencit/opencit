/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import org.junit.Test;

/**
 * This junit test is an example of one approach to extensible APIs, using
 * an "envelope" object to store the core request and extended attributes
 * The envelope reminds me of SOAP, and the client and server code both 
 * need to explicitly create envelopes and store attributes in them.
 * The idea of main content and extended attributes probably is nice for
 * helping developers keep the API forward-compatible with an "add only"
 * strategy using the extensions, but on the other hand it can become
 * strange to see what future additions do as extensions when they might
 * be seen as additions (or even required new additions) to the main request.
 * Overall it might be better to have a layout where initial and later 
 * attributes are on "equal footing" while maintaining an easy "add only"
 * mechanism. 
 * 
 * @author jbuhacoff
 */
public class InterfaceTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InterfaceTest.class);

    public static class KeyServer {
        public Envelope<KeyResponse> getKey(Envelope<KeyRequest> keyRequest) {
            KeyResponse keyResponse = new KeyResponse();
            keyResponse.setKey(new byte[] { 0, 1, 2, 3 });
            Envelope<KeyResponse> envelope = new Envelope<KeyResponse>();
            envelope.setContent(keyResponse);
            envelope.setExtensions(new Object[] { new Object(), new Object() });
            return envelope;
        }
    }
    
    public static class KeyResponse {
        public byte[] key;
        public void setKey(byte[] key) {
            this.key = key;
        }

        public byte[] getKey() {
            return key;
        }
            
            
    }
    
    public static class KeyRequest {
        public String keyId;
        // TODO:  how to do extended attributes????  or they should go in the ENVELOPE  , ADJACENT to the core request
    }
        
    public static class Envelope<T> {
        private T content; // for example a KeyRequest
        private Object[] extensions; // not convenient, will have to check and cast each one!!
        public void setContent(T content) {
            this.content = content;
        }
        public T getContent() {
            return content;
        }

        public Object[] getExtensions() {
            return extensions;
        }
        

            public void setExtensions(Object[] extensions) {
                this.extensions = extensions;
            }
        
    }
    
    /**
     * In this test, we have a "client" that provides an extensible document as input to a "server" method.
     * The server method looks for some core attributes of the document and passes the entire document
     * to another server method. The second server method looks for non-core attributes of the document.
     */
    @Test
    public void testExtensibleComponent() {
        // the client creates a request document
        KeyRequest keyRequest = new KeyRequest(); // TODO: make a more specific example
        Envelope<KeyRequest> envelope = new Envelope<KeyRequest>();
        envelope.setContent(keyRequest);
        // the client sends the request to the server
        KeyServer keyServer = new KeyServer();
        Envelope<KeyResponse> key = keyServer.getKey(envelope);
        log.debug("key response: {}", key.getContent().getKey());
        // the client obtains extended attributes from the response
        for(Object ext : key.getExtensions()) { 
            log.debug("extension: {}", ext.toString());
        }
    }
    }
