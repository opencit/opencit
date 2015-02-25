/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.repository;

/**
 * Classes that implement Locator for their models are the
 * formal documentation of the URL parameters available. 
 * Sometimes a URL parameter won't be used, for example
 * a host certificate locator has "id" and "sha1" fields
 * from the URL /hosts/{id}/tls-policy/certificates/{sha1}
 * but if the client is searching at /hosts/{id}/tls-policy/certificates
 * the same locator is used but it only has "id" populated,
 * not "sha1".
 * @author jbuhacoff
 */
public interface Locator<T> {
    /**
     * The locator is populated with parameters, for example
     * the path /hosts/{id}/tls-policy/certificates/{sha1} 
     * has two parameters, "id" and "sha1". When a client 
     * posts a certificate resource to that URL, the client
     * doesn't need to specify the host id because it's
     * implied by the URL. But if the client DOES specify
     * the host id, we need to make sure that we ignore it
     * and replace it with the host id from the URL. 
     * So we call  locator.copyTo(item) and it will copy
     * the "id" and "sha1" from the URL (if they are populated)
     * to the item. 
     * @param item 
     */
    void copyTo(T item);
}
