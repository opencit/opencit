/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.pem;

import com.intel.dcsg.cpg.rfc822.Message;
import com.intel.dcsg.cpg.rfc822.MessageReader;
import com.intel.dcsg.cpg.rfc822.MessageWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class SignatureWriter {
    
    public Message sign(Message message, SecretKey key, String keyId) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        MessageReader reader = new MessageReader();
        byte[] content = reader.read(message); // to automatically decode the emssage if there's base64 or gizip encoding ... that functionality should probably be included somewhere else... not necessarily in Message so that it can stay free of surprises, but maybe a sublcass EncodedMessage that automatically does it...
        byte[] contentDigest = MessageDigest.getInstance("SHA-256").digest(content);
        String contentId = Base64.encodeBase64String(contentDigest);
        // if the message object does not have a content-id , we add it.  
        List<String> contentIds = message.getHeaderMap().get("Content-ID");
        if( contentIds == null ) { contentIds = new ArrayList<String>(); }
        if( !contentIds.contains(contentId)) {
            message.getHeaderMap().add("Content-ID", contentId);
        }
        // do an hmac of the content with the secret key
        byte[] signatureBytes = signature(content, key, "HmacSHA256");
        MessageWriter writer = new MessageWriter();
        Message signatureMessage = writer.write(signatureBytes); // with base64...   XXX TODO the MessageReader/MessageWriter interface is confusing... need to come up with alternate names of structure to make it easier to understand whether we are just writing into the conetnt of a message or write the entire message it self... XXX TODO  getting a Message object out of messagewriter isn't intuitive.
        signatureMessage.setContentType("application/signature.java; alg=\"HmacSHA256\"; key=\""+keyId+"\"; digest-alg=\"SHA-256\"");
        signatureMessage.getHeaderMap().add("Link", "<cid:"+contentId+">; rel=\"cite\"");
        return signatureMessage;//.toByteArray();
    }
    
    // same as in HmacCredential
    public byte[] signature(byte[] document, SecretKey key, String signatureAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException {
//        SecretKeySpec key = new SecretKeySpec(secretkey.getEncoded(), signatureAlgorithm);
        Mac mac = Mac.getInstance(signatureAlgorithm); // a string like "HmacSHA256" ; throws NoSuchAlgorithmException
        mac.init(key); // throws InvalidKeyException
        return mac.doFinal(document);
        
    }
}
