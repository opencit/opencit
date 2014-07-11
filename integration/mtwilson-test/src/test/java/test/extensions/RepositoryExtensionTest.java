/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.extensions;

/**
 * This test class is a prototype of creating extension points in the repository
 * classes so that plugins can be notified when records are created, stored, 
 * retrieved, searched, and deleted. 
 * For example, a plugin might be interested in any time the TlsPolicyRepository 
 * updates an existing TlsPolicy record, in order to update its own related 
 * data.
 * To make it work, we also need to define high-level operations such as 
 * "link", "unlink", "assign", and "copy" so that related records are available
 * for extensions as well. For example when creating a new host record and 
 * setting some named global TlsPolicy, the extension point might be "store".
 * But when taking two existing host records with different TlsPolicy objects
 * and specifically making one of them reference the other one, that would be
 * a "link" with the subject (host record to modify) and object (the host record
 * to which the subject will be linked). Then a plugin which has implemented
 * an extension for that extension point could do something useful with the
 * subject's old TlsPolicy (archive it maybe) or simply send an informative
 * alert.  Witout high-level actions, the plugin would see just a "store"
 * and it wouldn't be clear what happened to the old TlsPolicy, or what is
 * the significance of the new TlsPolicy. 
 * @author jbuhacoff
 */
public class RepositoryExtensionTest {
    
}
