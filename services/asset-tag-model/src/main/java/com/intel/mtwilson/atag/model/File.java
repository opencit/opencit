/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

import com.intel.dcsg.cpg.io.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XXX TODO should we add Md5 or Sha256 hashes of the file for the purpose of at-a-glance equality? (not needed for security, the secure files already are encrypted with integrity measurement, and this hash would be of the encrypted version)
 * 
 * @author jbuhacoff
 */
public class File extends Document {
    private static Logger log = LoggerFactory.getLogger(File.class);
    private String name;
    private String contentType; //  XXX should be MediaType enum (TEXT_PLAIN, APPLICATION_OCTET_STREAM, etc.) but need to register a customer jackson serializer for it so going with String text/plain, application/octet-stream  for now.
    private byte[] content; 
//    private boolean encrypted; // we can detect encrypted files because they'll have the PEM header or the openssl salt bytes.  but it might still be good to keep a flag so we don't need to detect every time...
    // private EncryptedFormat encryptedFormat; // our encryption+integrity PEM format or openssl ... to avoid detection every time.  but not really necessary.
    
    public File() {
    }

    public File(long id, UUID uuid) {
        setId(id);
        setUuid(uuid);
    }
    
    public File(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }


    public byte[] getContent() {
        return content;
    }
    
    public String getContentType() {
        return contentType;//.getName();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
}
