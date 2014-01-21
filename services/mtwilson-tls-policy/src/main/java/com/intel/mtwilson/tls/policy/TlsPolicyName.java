/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy;

/**
 *
 * @author jbuhacoff
 */
public enum TlsPolicyName {
    STRICT,
    STRICT_AFTER_FIRST,
    PUBLIC_KEY, // check only the public key, ignore the certificate with validity dates, issuer, and hostname
    PUBLIC_KEY_AFTER_FIRST, // check only the public key, ignore the certificate with validity dates, issuer, and hostname
    INSECURE,
    // backward-compatible names:
    TRUST_CA_VERIFY_HOSTNAME, // same as STRICT
    TRUST_FIRST_CERTIFICATE, // same as STRICT_AFTER_FIRST
    TRUST_KNOWN_CERTIFICATE, // same as PUBLIC_KEY
}
