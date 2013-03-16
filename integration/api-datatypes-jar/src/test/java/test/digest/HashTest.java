/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.digest;

import com.intel.mtwilson.model.Sha1Digest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class HashTest {
    @Test
    public void testUpdateUpdateSameAsLongerMessage() throws NoSuchAlgorithmException {
        Sha1Digest a = new Sha1Digest("0000000000000000000000000000000000000000");
        Sha1Digest b = new Sha1Digest("0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f");
        Sha1Digest c = new Sha1Digest("aabbccddeeff00112233445566778899aabbccdd");
        MessageDigest hash = MessageDigest.getInstance(a.algorithm());
        hash.update(a.toByteArray());
        hash.update(b.toByteArray());
        Sha1Digest r = new Sha1Digest(hash.digest());
        System.out.println("a.extend(b) = "+r.toString());
        hash.reset();
        byte[] cat = new byte[40];
        System.arraycopy(a.toByteArray(), 0, cat, 0, 20);
        System.arraycopy(b.toByteArray(), 0, cat, 20, 20);
        hash.update(cat);
        Sha1Digest r2 = new Sha1Digest(hash.digest());        
        System.out.println("a||b = "+r2.toString());
    }
}
