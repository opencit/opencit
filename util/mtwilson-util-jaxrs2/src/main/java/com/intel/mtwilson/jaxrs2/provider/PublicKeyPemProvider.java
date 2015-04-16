/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.provider;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.security.PublicKey;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.apache.commons.io.IOUtils;

/**
 * Reference: https://jersey.java.net/documentation/latest/message-body-workers.html
 * @author jbuhacoff
 */
@Provider
@Consumes({CryptoMediaType.APPLICATION_X_PEM_FILE})
@Produces({CryptoMediaType.APPLICATION_X_PEM_FILE})
public class PublicKeyPemProvider implements
      MessageBodyWriter<PublicKey>,
      MessageBodyReader<PublicKey> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        /**
         * Example type: class sun.security.rsa.RSAPublicKeyImpl
         * Example genericType: interface java.security.PublicKey
         */
        return PublicKey.class.isAssignableFrom(type) && ( mediaType.toString().equals(CryptoMediaType.APPLICATION_X_PEM_FILE) );
    }

    @Override
    public long getSize(PublicKey t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(PublicKey t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            String pem = RsaUtil.encodePemPublicKey(t);
            IOUtils.write(pem, entityStream);
        }
        catch(Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return PublicKey.class.isAssignableFrom(type) && ( mediaType.toString().equals(CryptoMediaType.APPLICATION_X_PEM_FILE) );
    }

    @Override
    public PublicKey readFrom(Class<PublicKey> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        try {
            String pem = IOUtils.toString(entityStream);
            PublicKey cert = RsaUtil.decodePemPublicKey(pem);
            return cert;
        }
        catch(CryptographyException e) {
            throw new IOException(e);
        }
    }
    
}
