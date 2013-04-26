/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509.repository;

import com.intel.dcsg.cpg.crypto.Md5Digest;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * A CertificateRepository represents a source of trusted certificates for
 * the verification being attempted. This means if there is a global trusted root
 * CA list and a per-server list they should be combined by the implementation
 * to provide one coherent view to the TlsPolicy.
 * 
 * All methods in this interface return an Iterable, which could either be a "static" List
 * or it could be a handle into a database query that buffers results for the caller.
 * 
 * Caller must be able to call iterator() multiple times and get either the same or updated results.
 * 
 * Please note that this interface does NOT include any search methods for 
 * implementation-specific fields such as "comments", or "enabled", or "alias". 
 * Applications should extend this interface to add such search methods for their
 * own use.
 * 
 * @author jbuhacoff
 */
public interface SearchableCertificateRepository {

    /**
     * XXX TODO probably convert name parameter to something type safe
     * so that callers are not confused about the DN format... is it
     * /CN=XYZ,OU=ABC/ or is it just "CN=XYZ,OU=ABC" or something else?
     * 
     * @param name full DN for the subject
     * @return matching certificates (possibly empty);  must not return null
     */
    Iterable<X509Certificate> findBySubject(String name);

    /**
     * @param name can appear anywhere in the DN
     * @return matching certificates (possibly empty);  must not return null
     */
    Iterable<X509Certificate> findBySubjectLike(String name);

    /**
     * XXX TODO probably convert name parameter to something type safe
     * so that callers are not confused about the DN format... is it
     * /CN=XYZ,OU=ABC/ or is it just "CN=XYZ,OU=ABC" or something else?
     * 
     * @return matching certificates (possibly empty);  must not return null
     */
    Iterable<X509Certificate> findByIssuer(String name);

    /**
     * 
     * @param name can appear anywhere in the DN
     * @return matching certificates (possibly empty);  must not return null
     */
    Iterable<X509Certificate> findByIssuerLike(String name);

    
    /**
     * @param md5 the MD5 digest of the DER-encoded certificate
     * @return matching certificates (possibly empty);  must not return null
     */
    Iterable<X509Certificate> findByMD5(Md5Digest md5);

    /**
     * @param sha1 the SHA1 digest of the DER-encoded certificate
     * @return matching certificates (possibly empty);  must not return null
     */
    Iterable<X509Certificate> findBySHA1(Sha1Digest sha1);

    /**
     * The matching certificates will be valid on the given date. 
     * 
     * Certificates where the NotValidBefore date is same or less than the 
     * given date and NotValidAfter date is same or more than the given date
     * would be returned.
     * 
     * In the diagram below, the given date would be within or on the
     * brackets 
     * for each matching certificate. 
     * 
     * Timeline:
     * . . . . [  valid  ] . . . . -> time
     *         ^^^^^^^^^^^
     * 
     * @return matching certificates (possibly empty);  must not return null
     */
    Iterable<X509Certificate> findByValidOn(Date validOn);

    /**
     * The matching certificates will not be valid after the given date.
     * 
     * Certificates where the NotValidAfter date is same or less than the 
     * given date would be returned.
     * 
     * In the diagram below, the given date would be to the right of 
     * the right bracket for matching certificates.
     * 
     * Timeline:
     * . . . . [  valid  ] . . . . -> time
     *                    ^^^^^^^^^
     * 
     * @return matching certificates (possibly empty);  must not return null
     */
    Iterable<X509Certificate> findByNotValidAfter(Date notValidAfter);

    /**
     * The matching certificates will not be valid after the given date.
     * 
     * Certificates where the NotValidBefore date is same or more than the 
     * given date would be returned.
     * 
     * In the diagram below, the given date would be to the left of 
     * the left bracket for matching certificates.
     * 
     * Timeline:
     * . . . . [  valid  ] . . . . -> time
     * ^^^^^^^^
     * 
     * @return matching certificates (possibly empty);  must not return null
     */
    Iterable<X509Certificate> findByNotValidBefore(Date notValidBefore);


    
}
