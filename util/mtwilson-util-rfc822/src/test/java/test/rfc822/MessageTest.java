/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.rfc822;

import com.intel.dcsg.cpg.rfc822.Message;
import com.intel.dcsg.cpg.rfc822.MessageReader;
import com.intel.dcsg.cpg.rfc822.MessageWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import org.apache.commons.codec.binary.Base64;
import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class MessageTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Sample output:
QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=
     */
    @Test
    public void testCreatePemFormatNoHeaders() {
        // create random content for the output
        byte[] content = new byte[32];
        Random rnd = new Random();
        rnd.nextBytes(content);
        Message message = new Message(Base64.encodeBase64(content));
        log.debug("message:\n"+message.toString());
    }

    /**
     * Sample output:
Content-Length: 178

QEStdcdTFsENMiRgrUgvP1S9ZbKmp0zXP6NbzDd9iN+cROuKMHOqY0AnwCQ6Q1r9nZMUN8owhk63
fK6M0VPKA6TjFVDmX7VGKb0Sk8rfIddBV3TKZKPerYwl3VSv7iut9QZC7zXiZHgZVKJiBCpsoSql
wQxBTlElZg+ho8yY4G0=
     */
    @Test
    public void testWriteDataChunked() throws IOException {
        // create random content for the output
        byte[] content = new byte[128];
        Random rnd = new Random();
        rnd.nextBytes(content);
        Message message = new Message(Base64.encodeBase64Chunked(content));
        log.debug("message:\n"+new String(message.toByteArray(), "UTF-8"));
    }
    
    @Test
    public void testWriteThenReadChunked() throws IOException {
        // create random content for the output
        byte[] content = new byte[128]; // content length will be 178
        Random rnd = new Random();
        rnd.nextBytes(content);
        byte[] contentBase64 = Base64.encodeBase64Chunked(content);
        Message message = new Message(contentBase64);
        byte[] messageBytes = message.toByteArray();
        log.debug("message:\n"+new String(messageBytes, "UTF-8"));
        // now read it back in 
        Message message2 = Message.parse(messageBytes);
        assertArrayEquals(contentBase64, message2.getContent());
        assertEquals(178, message2.getContentLength().intValue()); // the content length is the base64 encoded content which we used to construct the original InternetMessage
        // now decode the message content and check we got back what we put in
        byte[] decoded = Base64.decodeBase64(message2.getContent());
        assertEquals(128, decoded.length);
        assertArrayEquals(content, decoded);
    }
    
    
    /**
     * Sample data:
blank-attr2: 
attr1: value1

1Jz33w6EWlV15Hj/wG06XT1LdR8oiyV3orM+UwLno8s=
     */
    @Test
    public void testCreatePemFormatWithHeaders() {
        // create random content for the output
        byte[] content = new byte[32];
        Random rnd = new Random();
        rnd.nextBytes(content);
        // create some headers
        HashMap<String,String> headers = new HashMap<String,String>();
        headers.put("attr1", "value1");
        headers.put("blank-attr2", "");
        Message message = new Message(Base64.encodeBase64(content));
        log.debug("message:\n"+message.toString());
    }
    
    @Test
    public void testParseMessageFormatNoHeaders() throws IOException {
        String input = "" +
"QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=\n";
        Message message = Message.parse(input);
        assertEquals("", new String(message.getContent(), "UTF-8")); // headers are required so if they are missing the body will not be found
        assertFalse(message.getHeaderMap().isEmpty());
        assertEquals(1, message.getHeaderMap().size());
        assertTrue(message.getHeaderMap().containsKey("QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=")); // the base64 string is interpreted as a header 
    }

    @Test
    public void testParsePemFormatWithHeaders() throws IOException {
        String input = "" +
"attr2: value2\n"+
"attr1: value1\n"+
"\n"+
"QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=\n";
        Message message = Message.parse(input);
        assertEquals("QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=\n", new String(message.getContent(), "UTF-8"));
        assertEquals(2,message.getHeaderMap().size());
        assertTrue(message.getHeaderMap().containsKey("attr1"));
        assertEquals("value1", message.getHeaderMap().getFirst("attr1"));
        assertTrue(message.getHeaderMap().containsKey("attr2"));
        assertEquals("value2", message.getHeaderMap().getFirst("attr2"));
    }
    

    @Test
    public void testParsePemFormatWithHeadersHavingOneBlankValue() throws IOException {
        String input = "" +
"attr2: \n"+
"attr1: value1\n"+
"\n"+
"QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=\n";
        Message message = Message.parse(input);
        assertEquals("QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=\n", new String(message.getContent(), "UTF-8"));
        assertEquals(2,message.getHeaderMap().size());
        assertTrue(message.getHeaderMap().containsKey("attr1"));
        assertEquals("value1", message.getHeaderMap().getFirst("attr1"));
        assertTrue(message.getHeaderMap().containsKey("attr2"));
        assertEquals("",message.getHeaderMap().getFirst("attr2"));
        assertNull("",message.getHeaderMap().getFirst("attr-missing"));
    }

    @Test
    public void testReaderWriter() throws IOException {
        String text = "About a Boy is a 2002 comedy-drama film co-written and directed by brothers Chris Weitz and Paul Weitz. It is an adaptation of the 1998 novel of the same name by Nick Hornby. The film stars Hugh Grant, Nicholas Hoult, Toni Collette, and Rachel Weisz. The film at times uses double voice-over narration, where the audience hears both Will's and Marcus's thoughts.\n" +
"It was nominated for an Academy Award for Best Adapted Screenplay. Actors Hugh Grant and Toni Collette were nominated for a Golden Globe and a BAFTA Award respectively for their performances.";
        MessageWriter writer = new MessageWriter(); // default gzip and base64 encoding... XXX need to change that...
        Message message = writer.write(text.getBytes("UTF-8"));
        message.setContentType("text/plain; charset=\"UTF-8\"");
        log.debug("Message:\n{}", message.toString());
        MessageReader reader = new MessageReader();
        byte[] content = reader.read(message);
        String result = new String(content, "UTF-8");
        assertEquals(text, result);
    }
}
