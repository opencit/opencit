/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.rfc822;

import com.intel.dcsg.cpg.io.ByteArray;
import com.intel.mtwilson.collection.MultivaluedHashMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.Header;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.mail.internet.InternetHeaders;
import org.apache.commons.io.IOUtils;

/**
 * A utility class to read and write message/rfc822 files. These resemble HTTP messages (without the first line)
 * or SMTP messages (without the required Date, From, and To fields). At least one header is required; the standard
 * is to provide at least a Subject, From, or Date header, but any other header will do as well such as 
 * Content-Length or Content-Type. 
 * 
 * If a Content-Transfer-Encoding header is present and its value is "base64" the content will be automatically
 * base64-decoded on reading and base64-encoded on writing. "hex" is also supported as a transfer encoding but
 * is only recommended for short bodies. Any other value is not currently supported (but might
 * be in the future). 
 * 
 * XXX TODO If a non-supported value is present, there ought to be a flag like isContentEncoded to let the caller know that
 * we did not decode it and that an additional decoding step is necessary to obtain the original content.
 * 
 * Note that when parsing a message/rfc-822 entity this class reasonably assumes that all the content after the
 * headers is the body. There is no end delimiter.  If you are extracting a message/rfc822 from a container format,
 * such as a multipart/alternative, or multipart/encrypted or multipart/signed, or application/x-pem-file, the container
 * format is responsible for delimiting the messages and then there are two options to proceed: first, the container's
 * parser must first extract the message areas
 * using its delimiters or boundaries and then pass each message/rfc822 object to this class for further parsing; 
 * second, the container can request this class to parse the message by providing an end-of-message delimiter which
 * this class can look for (the message includes all bytes up to but not including the delimiter). 
 * 
 * When message/rfc822 is embedded in a container format,
 * the Content-Transfer-Encoding: base64 should almost always be applied or else its content must be scanned to ensure
 * that it doesn't accidentally include something that might be mistaken for the boundary.
 * 
 * XXX TODO: maybe add instance methods read() to replace the static parse() methods. This would allow a caller to
 * create an "empty" instance of InternetMessage and then read a stream into it, filling in the headers immediately
 * but then processing the content as a stream and exposing that to the caller.  it could be done a different way,
 * the point is to allow creation of the internetmessage from a stream.
 * 
 * XXX TODO: maybe make InternetMessage immutable by removing the setters; since it's comprised only of a byte array
 * and a header map, anyone can manipulate those externally and then create a new InternetMessage. Or maybe make a
 * MutableInternetMessage object specifically for doing transofmrations on the message itself, like changing the encoding
 * which automatically changes the header, etc. Maybe create subclasses of InternetMEssage like Base64EncodedInternetMessage
 * which would return the raw/plain content from inherited getContent()  and also provide the encoded content from
 * getEncodedContent() ? but if the caller is using more than one feature this would be cumbersome - like to combine
 * gzip and base64 encoding. Including that kind of functionality in InternetMessage itself is dubious because it
 * cannot keep up with what a using application would need. 
 * 
 * 
 * @author jbuhacoff
 */
public class Message {
    private static final Logger log = LoggerFactory.getLogger(Message.class);
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String NEWLINE = "\r\n";
    private MultivaluedHashMap<String,String> headers = new MultivaluedHashMap<>();
    private byte[] content;
    
    public Message() {
        this.content = null;
    }
    
    public Message(byte[] content) {
        this.content = content;
    }
    public Message(byte[] content, Map<String,String> headers) {
        this.content = content;
        if( headers != null ) {
            for(Map.Entry<String,String> entry : headers.entrySet()) {
                this.headers.add(entry.getKey(), entry.getValue());
            }
        }
    }
    public Message(byte[] content, MultivaluedHashMap<String,String> headers) {
        this.content = content;
        if( headers != null ) {
            this.headers.put(headers);
        }
    }
    
    public byte[] getContent() { return content; }
    public void setContent(byte[] content) { this.content = content; }
    
    public MultivaluedHashMap<String,String> getHeaderMap() { return headers; }
    
    public String getContentType() { return headers.getFirst(CONTENT_TYPE); }
    public void setContentType(String contentType) { headers.put(CONTENT_TYPE, contentType); }
    public String getContentTransferEncoding() { return headers.getFirst(CONTENT_TRANSFER_ENCODING); }
    public void setContentTransferEncoding(String contentTransferEncoding) { headers.put(CONTENT_TRANSFER_ENCODING, contentTransferEncoding); }
    public Integer getContentLength() { String contentLength = headers.getFirst(CONTENT_LENGTH); if( contentLength == null ) { return null; } return Integer.valueOf(contentLength); }
    public void setContentLength(Integer length) { if( length == null ) { headers.remove(CONTENT_LENGTH); } else { headers.put(CONTENT_LENGTH, length.toString()); } }
    
    public static Message parse(String content) throws IOException {
        return parse(content.getBytes("UTF-8"));
    }
    
    public static Message parse(byte[] content) throws IOException {
        return parse(new ByteArrayInputStream(content));
    }
    
    /**
     * Use this method to parse a message/rfc822 entity whose body may be in raw binary form. If the
     * body is base64-encoded and the Content-Transfer-Encoding header is set to "base64" the base64-decoding
     * will be performed automatically. If the body is hex-encoded and the header is set to "hex" the hex-decoding
     * will be performed automatically.
     * 
     * At least one header is required.
     * 
     * The caller is expected to close the input stream.
     * 
     * @param in providing a message/rfc822 entity with at least one header and optional body
     * @return
     * @throws IOException 
     */
    public static Message parse(InputStream in) throws IOException {
        // first extract the headers; the message is required to have at least one header
        try {
            MultivaluedHashMap<String,String> map = new MultivaluedHashMap<>();
            InternetHeaders internetHeaders = new InternetHeaders(in);
            Enumeration e = internetHeaders.getAllHeaders();
            while(e.hasMoreElements()) {
                Header header = (Header)e.nextElement();
                map.add(header.getName(), header.getValue());
                // XXX TODO we should also maintain an ArrayList with the original header order so if we need to rewrite the message we can preserve the order for any headers that have been kept
            }
            // after the headers are read from the input stream, the stream is positioned at the start of the body
            byte[] content = IOUtils.toByteArray(in);
            // decode the content, if a content-transfer-encoding header is set (if it's not set this is a no-op)
            //byte[] plain = decode(content, map.get(CONTENT_TRANSFER_ENCODING));
            // create a new instance with the raw content and 
            return new Message(content, map);
        }
        catch(MessagingException | IOException e) {
            throw new IOException("Cannot parse message", e);
        }
    }

    /**
     * 
     * @return the header block of this InternetMessage ; if there are no headers then a Content-Length header is added automatically
     */
    public String getHeaderText() {
        StringBuilder header = new StringBuilder();
        if( headers.isEmpty() ) {
            // at least one header is required so we generate a content-length header
            headers.put(CONTENT_LENGTH, String.valueOf(content.length));
        }
        Set<String> attrNames = headers.keys();
        ArrayList<String> sortedAttrNames = new ArrayList<>(attrNames);
        Collections.sort(sortedAttrNames);
        for(String attrName : sortedAttrNames) {
            List<String> values = headers.get(attrName);
            for(String value : values) {
                header.append(String.format("%s: %s", attrName, value));
                header.append(NEWLINE);
            }
        }
        return header.toString();
    }
    
    /**
     * Writes header text, newline, and raw body content
     * 
     * The headers are sorted alphabetically by header name.  This is done so that if the file needs
     * to be authenticated as-is, a MAC can be reliably produced even after serializing and de-serializing
     * the file multiple times because the headers will always be in the same order.  
     * 
     * An alternative implementation would be to maintain a List with the order of header names read from the
     * file and when serializing first check if the map key set is same length as the headers in the list, and
     * if so then write them in the same order that we know, and if the keyset is different in any way then
     * we can write them out alphabetized because the set is not the same anyway.
     * 
     * RFC822 specifies an order for standard headers which is currently ignored.  We're also ignoring the
     * order the headers may have been in originally (if this instance was deserialized from an existing message) which
     * might be bad because changing the order might invalidate a signature that is over the entire message. On
     * the other hand, if there was a signature then the original message is still available that was used to
     * instantiate this object so the signature should be verified over that original message anyway.
     * 
     * @param out an open output stream; caller is responsible for closing it afterwards
     */
    // removing this method because it's confusing when we ALSO have messagewriter and messagereader; ... use toByteArray() instead
    /*
    public void write(OutputStream out) throws IOException {
        out.write(getHeaderText().getBytes("UTF-8"));
        out.write(NEWLINE.getBytes("UTF-8"));
        out.write(content);
    }
    * */
    public byte[] toByteArray() {
        Charset utf8 = Charset.forName("UTF-8"); // if utf-8 is missing (non-compliant platform) the exception would be thrown here so it cannot be confused with ioexception from the outputstream below. also, java7 has constants for charsets which might make this unnecessary (if we switch to java7)
        return ByteArray.concat(getHeaderText().getBytes(utf8), NEWLINE.getBytes(utf8), content);
    }
    
    /**
     * 
     * 
     * @return an HTTP-like format with optional headers followed by blank line and optionally-encoded content
     */
    @Override
    public String toString() {
        Integer length = getContentLength();
        if( length == null ) { length = content.length; }
        String contentType = getContentType();
        if( contentType == null ) { contentType = "unknown type"; }
        String contentTransferEncoding = getContentTransferEncoding();
        if( contentTransferEncoding == null ) { contentTransferEncoding = ""; }
        return String.format("Message [%s] [%d bytes] [%s]", contentType, length,contentTransferEncoding );
    }

}
