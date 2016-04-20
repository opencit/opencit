/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.security.core;

/**
 * Basic and Digest are the Http authorization standards but are not supported
 * by Mt Wilson.
 * 
 * The MtWilson scheme is similar to Digest but uses different parameters and
 * does not include a server-nonce.
 * 
 * Both PublicKey and X509 schemes use private/public key pairs. The X509
 * scheme differs from PublicKey in the way signatures are calculated (the
 * X509 specification includes the signature algorithm OID in the signature itself,
 * but regular private key signatures
 * do not add any OID's since OID's are specified by X509 not by RSA/DSA/etc.
 * 
 * @author jbuhacoff
 */
public enum AuthorizationScheme {
    Basic("Basic"), 
    Digest("Digest"), 
    MtWilson("MtWilson"),  // a variation on the Digest scheme without server nonces
    PublicKey("PublicKey"),  // a new scheme using RSA keys
    X509("X509");  // a new scheme using RSA keys and X509 Certificates

    private String text;

    private AuthorizationScheme(String text) {
        this.text = text;
    }
    
    @Override
    public String toString() {
        return text;
    }
}
