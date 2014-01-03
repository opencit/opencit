/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import java.util.Date;

/**
 * Encapsulates available criteria for a certificate search.
 * <p>
 * The available criteria are public members documented below. They can be used in a {@code GET} query URL 
 * or as the body of a {@code POST} request, for example.
 * <p>
 * When used in the query part of a URL, each of the public members of this class is optional. For example,
 * a query could be constructed like this:
 * <pre>
GET /certificates?issuerContains=Intel&amp;validOn=2013-08-15
 * </pre>
 * 
 * @author jbuhacoff
 */
public class CertificateSearchCriteria {
    /**
     * UUID of the certificate
     * Acceptable formats:
     * <ul>
     * <li>UUID format, for example {@code 213f8135-9d10-4167-b9cc-4b342a5c611a}</li>
     * <li>Hex format, for example {@code 213f81359d104167b9cc4b342a5c611a}</li>
     * </ul>
     * 
     */
    public UUID id;
    
    /**
     * 
     */
    public String subjectEqualTo;
    
    /**
     * 
     */
    public String subjectContains;
    
    /**
     * 
     */
    public String issuerEqualTo;
    
    /**
     * 
     */
    public String issuerContains;
    
    /**
     * Specified value for certificate status, for example "active" or "revoked".
     * <p>
     * Possible status codes are defined in {@link Certificate}
     */
    public String statusEqualTo;
    
    /**
     * Certificate is valid on this date:  notBefore &lt; validOn &le; notAfter.
     */
    public Date validOn;
    
    /**
     * Certificate is valid sometime before this date:  validBefore &le; notAfter.
     * 
     */
    public Date validBefore;
    
    /**
     * Certificate is valid sometime after this date:  validAfter &ge; notBefore.
     * 
     */
    public Date validAfter;
    
    /**
     * SHA-1 digest of the certificate.
     * 
     */
    public Sha1Digest sha1;
    
    /**
     * SHA256 digest of the certificate.
     */
    public Sha256Digest sha256;
    
    /**
     * Can be true or false to restrict the search, or null to consider all certificate regardless of revocation
     * status.
     * 
     */
    public Boolean revoked;
}
