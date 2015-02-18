/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.io.Attributes;
import com.intel.dcsg.cpg.io.ByteArray;
import java.net.URL;
import java.util.Date;

/**
 * Encapsulates some common attributes of documents such as
 * href, etag, createdOn, modifiedOn.  Specific fields are
 * optional or required according to the application.
 * 
 * @author jbuhacoff
 */
public class DocumentAttributes extends Attributes {
    private URL href;
    private String etag;
    private Date createdOn; 
    private Date modifiedOn;

    public URL getHref() {
        return href;
    }
    
    public void setHref(URL href) {
        this.href = href;
    }
    

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    

    /**
     * If an etag is already set, returns the existing etag.
     * Otherwise, if the modifiedOn date is set, returns a SHA-1 
     * digest of the modifiedOn date.
     * Otherwise, returns null.
     * 
     * @return 
     */
    public String getEtag() {
        if( etag != null ) { return etag; }
        if( modifiedOn != null ) {
            String hex = Long.toHexString(modifiedOn.getTime());
            ByteArray byteArray = ByteArray.fromHex(hex);
            Sha1Digest digest = Sha1Digest.digestOf(byteArray.getBytes());
            return digest.toHexString();
        }
        return null;
    }
    
    
}
