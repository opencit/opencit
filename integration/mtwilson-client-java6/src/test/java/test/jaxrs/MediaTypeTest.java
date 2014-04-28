/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.jaxrs;

import javax.ws.rs.core.MediaType;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class MediaTypeTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MediaTypeTest.class);

     @Test
  public void testMediaType() {
      MediaType rfc822 = MediaType.valueOf("message/rfc822");
      log.debug("rfc822: {}", rfc822.toString());
  }

     @Test
  public void testMediaTypeWithCharset() {
      MediaType rfc822 = MediaType.valueOf("message/rfc822; charset=\"UTF-8\"");
      log.debug("rfc822 with charset: {}", rfc822.toString());
  }
     
     @Test
     public void testMediaTypeEncryption() {
         MediaType openssl = MediaType.valueOf("encrypted/openssl; alg=\"aes-256-ofb\"; digest-alg=\"sha256\"; enclosed=\"application/zip\"");
         log.debug("openssl: {}", openssl.toString());
     }

}
