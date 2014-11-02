/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.ByteArray;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class HmacTest1 {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HmacTest1.class);
    
    @Test
    public void testHmacSha256Rfc822() throws Exception {
        String secret = "PASSWORD";
        String document = "Content-Type: application/mtwilson-signature; alg=\"hmac\"; digest-alg=\"sha256\"; headers=\"content-type,link,date\"\nLink: <helloworld.txt>; rel=signed\nDate: Thu Feb  6 19:17:32 PST 2014\n\nhello world\n";
        log.debug("Document:\n{}<<<EOF", document);
        HmacCredential credential = new HmacCredential("username", secret);
        byte[] signature = credential.signature(document.getBytes(Charset.forName("UTF-8")));
        log.debug("Signature: {}", Base64.encodeBase64String(signature)); // base64: 3CxEv2m8TiBVnTcrfYMrhtgcpdBmP3XuqerzMnnm90c=
        log.debug("Signature: {}", Hex.encodeHexString(signature)); // hex: dc2c44bf69bc4e20559d372b7d832b86d81ca5d0663f75eea9eaf33279e6f747
    }
    
    /**
     * TODO:   fix  "parameters" key with no value showing up for the date header 
     * when there aren't any parameters and it should be omitted
     * 
     * @throws Exception 
     */
    @Test
    public void testHmacSha256Json() throws Exception {
        
        ObjectMapper mapper = new ObjectMapper();
        ApplicationMtWilsonSignature sig = new ApplicationMtWilsonSignature("3CxEv2m8TiBVnTcrfYMrhtgcpdBmP3XuqerzMnnm90c=");
        Header contentType = new Header("Content-Type", "application/mtwilson-signature", new HeaderParameter("alg","hmac"), new HeaderParameter("digest-alg","sha256"), new HeaderParameter("headers","content-type,link,date"));
        log.debug("content-type: {}", mapper.writeValueAsString(contentType));
        Header link = new Header("Link", "helloworld.txt", new HeaderParameter("rel","signed"));
        log.debug("link: {}", mapper.writeValueAsString(link));
        Header date = new Header("Date", "Thu Feb  6 19:17:32 PST 2014");
        log.debug("date: {}", mapper.writeValueAsString(date));
        sig.headers.add(contentType);
        sig.headers.add(link);
        sig.headers.add(date);
        log.debug("mtwilson-api-client: {} ", sig.toString());
        
        log.debug("json: {}", mapper.writeValueAsString(sig));
        
        /**

{"headers":
[
  {"name":"Content-Type","value":"application/mtwilson-signature","parameters":{"alg":"hmac","digest-alg":"sha256","headers":"content-type,link,date"}},
  {"name":"Link","value":"helloworld.txt","parameters":{"rel":"signed"}},
  {"name":"Date","value":"Thu Feb  6 19:17:32 PST 2014","parameters"}
],
"data":"3CxEv2m8TiBVnTcrfYMrhtgcpdBmP3XuqerzMnnm90c="}

*/
        
    }
    
    @Test
    public void testHmacSha256Xml() throws Exception {
        
        XmlMapper mapper = new XmlMapper();
        ApplicationMtWilsonSignature sig = new ApplicationMtWilsonSignature("3CxEv2m8TiBVnTcrfYMrhtgcpdBmP3XuqerzMnnm90c=");
        Header contentType = new Header("Content-Type", "application/mtwilson-signature", new HeaderParameter("alg","hmac"), new HeaderParameter("digest-alg","sha256"), new HeaderParameter("headers","content-type,link,date"));
        log.debug("content-type: {}", mapper.writeValueAsString(contentType));
        Header link = new Header("Link", "helloworld.txt", new HeaderParameter("rel","signed"));
        log.debug("link: {}", mapper.writeValueAsString(link));
        Header date = new Header("Date", "Thu Feb  6 19:17:32 PST 2014");
        log.debug("date: {}", mapper.writeValueAsString(date));
        sig.headers.add(contentType);
        sig.headers.add(link);
        sig.headers.add(date);
        log.debug("mtwilson-api-client: {} ", sig.toString());
        
        log.debug("xml: {}", mapper.writeValueAsString(sig));
        
        /**

<signature xmlns=""><headers><header><name>Content-Type</name><value>application/mtwilson-signature</value><parameters><alg>hmac</alg><digest-alg>sha256</digest-alg><headers>content-type,link,date</headers></parameters></header><header><name>Link</name><value>helloworld.txt</value><parameters><rel>signed</rel></parameters></header><header><name>Date</name><value>Thu Feb  6 19:17:32 PST 2014</value></header></headers><data>3CxEv2m8TiBVnTcrfYMrhtgcpdBmP3XuqerzMnnm90c=</data></signature>
* 
         */
        
    }
    
    /*
     * TODO:  fix error: 
     * 
com.fasterxml.jackson.dataformat.yaml.snakeyaml.emitter.EmitterException: expected NodeEvent, but got <com.fasterxml.jackson.dataformat.yaml.snakeyaml.events.DocumentEndEvent()>
	at com.fasterxml.jackson.dataformat.yaml.snakeyaml.emitter.Emitter.expectNode(Emitter.java:409)
	at com.fasterxml.jackson.dataformat.yaml.snakeyaml.emitter.Emitter.access$1600(Emitter.java:63)
	at com.fasterxml.jackson.dataformat.yaml.snakeyaml.emitter.Emitter$ExpectBlockMappingSimpleValue.expect(Emitter.java:642)
	at com.fasterxml.jackson.dataformat.yaml.snakeyaml.emitter.Emitter.emit(Emitter.java:217)
	at com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.close(YAMLGenerator.java:287)
	at com.fasterxml.jackson.databind.ObjectMapper._configAndWriteValue(ObjectMapper.java:2820)
	at com.fasterxml.jackson.databind.ObjectMapper.writeValueAsString(ObjectMapper.java:2268)
	at com.intel.dcsg.cpg.crypto.HmacTest1.testHmacSha256Yaml(HmacTest1.java:132)
     * 
    @Test
    public void testHmacSha256Yaml() throws Exception {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        yamlFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        ObjectMapper mapper = new ObjectMapper(yamlFactory);

        ApplicationMtWilsonSignature sig = new ApplicationMtWilsonSignature("3CxEv2m8TiBVnTcrfYMrhtgcpdBmP3XuqerzMnnm90c=");
        Header contentType = new Header("Content-Type", "application/mtwilson-signature", new HeaderParameter("alg","hmac"), new HeaderParameter("digest-alg","sha256"), new HeaderParameter("headers","content-type,link,date"));
        log.debug("content-type: {}", mapper.writeValueAsString(contentType));
        Header link = new Header("Link", "helloworld.txt", new HeaderParameter("rel","signed"));
        log.debug("link: {}", mapper.writeValueAsString(link));
        Header date = new Header("Date", "Thu Feb  6 19:17:32 PST 2014");
        log.debug("date: {}", mapper.writeValueAsString(date));
        sig.headers.add(contentType);
        sig.headers.add(link);
        sig.headers.add(date);
        log.debug("mtwilson-api-client: {} ", sig.toString());
        
        log.debug("json: {}", mapper.writeValueAsString(sig));
        
    }
    */
    
    @Test
    public void testReadXml() throws Exception {
        String xml = "<signature xmlns=\"\"><headers><header><name>Content-Type</name><value>application/mtwilson-signature</value><parameters><alg>hmac</alg><digest-alg>sha256</digest-alg><headers>content-type,link,date</headers></parameters></header><header><name>Link</name><value>helloworld.txt</value><parameters><rel>signed</rel></parameters></header><header><name>Date</name><value>Thu Feb  6 19:17:32 PST 2014</value></header></headers><data>3CxEv2m8TiBVnTcrfYMrhtgcpdBmP3XuqerzMnnm90c=</data></signature>";
        XmlMapper mapper = new XmlMapper();
        ApplicationMtWilsonSignature signature = mapper.readValue(xml, ApplicationMtWilsonSignature.class);
        log.debug("# headers: {}", signature.headers.size());
        for(Header header : signature.headers) {
            log.debug("header name {} value {}", header.name, header.value);
            for(HeaderParameter p : header.parameters.list) {
                log.debug("parameter name {} value {}", p.name, p.value);
            }
        }
        log.debug("data: {}", signature.signature);
        
        String document = writeAsRfc822(signature);
        log.debug("document:\n{}", document);
        
        byte[] documentBytes = writeAsRfc822Document(signature.headers, null /* original input */);
        //  TODO now verfiy the signature with this ...
        
    }
    
    /**
     * Example output:
     * 
Content-Type: application/mtwilson-signature; alg="hmac"; digest-alg="sha256"; headers="content-type,link,date"
Link: <helloworld.txt>; rel="signed"
Date: Thu Feb  6 19:17:32 PST 2014

3CxEv2m8TiBVnTcrfYMrhtgcpdBmP3XuqerzMnnm90c=
     * 
     * @param signature
     * @return 
     */
    private String writeAsRfc822(ApplicationMtWilsonSignature signature) {
        String headers = writeAsRfc822Headers(signature.headers);
        String document = String.format("%s\n%s\n", headers, signature.signature);
        return document;
    }
    
    private String writeAsRfc822Headers(List<Header> headers) {
        ArrayList<String> headerStrings = new ArrayList<String>();
        for(Header header : headers) {
            // first collect the parameters, if any
            ArrayList<String> parameterStrings = new ArrayList<String>();
            for(HeaderParameter p : header.parameters.list) {
                parameterStrings.add(String.format("%s=\"%s\"", p.name, p.value));
            }
            String headerValue = header.value;
            if( header.name.equals("Link") ) {
                headerValue = String.format("<%s>", header.value);
            }
            String value = String.format("%s%s", headerValue, parameterStrings.isEmpty() ? "" : "; "+StringUtils.join(parameterStrings, "; "));
            headerStrings.add(String.format("%s: %s\n", header.name, value));
        }
        String headerText = StringUtils.join(headerStrings, "");
        return headerText;
    }
    
    private byte[] writeAsRfc822Document(List<Header> headers, byte[] original) {
        String headerText = writeAsRfc822Headers(headers)+"\n";
        byte[] header = headerText.getBytes(Charset.forName("UTF-8"));
        byte[] document = ByteArray.concat(header, original);
        return document;
    }
    
    public static class Header {
        final public static String CONTENT_TYPE = "Content-Type";
        final public static String LINK = "Link";
        final public static String DATE = "Date";
        
@JsonInclude(JsonInclude.Include.ALWAYS) // jackson 2.0
        public String name;
@JsonInclude(JsonInclude.Include.NON_EMPTY) // jackson 2.0
        public String value;
@JsonInclude(JsonInclude.Include.NON_EMPTY) // jackson 2.0
//    @JacksonXmlElementWrapper(localName="parameters")
//    @JacksonXmlProperty(localName="parameer")    
@JsonSerialize(using=HeaderParameterListSerializer.class)
@JsonDeserialize(using=HeaderParameterListDeserializer.class)
//        public ArrayList<HeaderParameter> parameters = new ArrayList<HeaderParameter>();
        public HeaderParameterList parameters = new HeaderParameterList();

        public Header() { }
        public Header(String name, String value) {
            this.name = name;
            this.value = value;
        }
        public Header(String name, String value, HeaderParameter... parameters) {
            this.name = name;
            this.value = value;
            for(HeaderParameter parameter : parameters) {
                this.parameters.list.add(parameter);
            }
        }
        
    }

    public static class HeaderParameter {
        public String name;
        public String value;
        public HeaderParameter() { }
        public HeaderParameter(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    
    
@JacksonXmlRootElement(localName="signature")
    public static class ApplicationMtWilsonSignature {
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="headers")
    @JacksonXmlProperty(localName="header")    
        public ArrayList<Header> headers = new ArrayList<Header>();
@JsonInclude(JsonInclude.Include.ALWAYS) // jackson 2.0
        @JsonProperty("data")
        public String signature;

public ApplicationMtWilsonSignature() { }
        public ApplicationMtWilsonSignature(String signature) { this.signature = signature; }
    }

public static class HeaderParameterListSerializer extends JsonSerializer<HeaderParameterList> {

        @Override
        public void serialize(HeaderParameterList t, JsonGenerator jgen, SerializerProvider sp) throws IOException, JsonProcessingException {
            if( !t.list.isEmpty() ) {
    jgen.writeStartObject();
    
            for(HeaderParameter p : t.list) {
    log.debug("list serializing header  {} = {}", p.name, p.value);
                jgen.writeStringField(p.name.toLowerCase(),p.value);
            }
    jgen.writeEndObject();
            }
        }
    
}

public static class HeaderParameterListDeserializer extends JsonDeserializer<HeaderParameterList> {

        @Override
        public HeaderParameterList deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
            HeaderParameterList list = new HeaderParameterList();
//            log.debug("current name: {}", jp.getCurrentName());
//            log.debug(" current token {}", jp.getCurrentToken().name());
//            log.debug("text: {}", jp.getText());
//            log.debug("value as string {}", jp.getValueAsString());
            
            // not sure if this guard is necessary -- will this condition always be true when we start?
            if( jp.getCurrentName().equals("parameters") && jp.getCurrentToken().equals(JsonToken.START_OBJECT))  {
                while(true) {
                    HeaderParameter parameter = new HeaderParameter();
                    // next tokens will be:   FIELD_NAME, VALUE_STRING, FIELD_NAME, VALUE_STRING, ...   
                    JsonToken token = jp.nextToken(); // assert:  token.equals(JsonToken.FIELD_NAME)
//                    log.debug("next token: {}", token.name());
                    if( token.equals(JsonToken.END_OBJECT) ) {
                        break;
                    }
                    parameter.name = jp.getValueAsString();
                    token = jp.nextToken(); // assert: token.equals(JsonToken.VALUE_STRING)
//                    log.debug("next token: {}", token.name());
                    parameter.value = jp.getValueAsString();
                    
                    list.list.add(parameter);
                    
                }
            }
//            jp.getCodec().
            
            return list;
        }
    
}

public static class HeaderParameterList {
    public ArrayList<HeaderParameter> list = new ArrayList<HeaderParameter>();
}


}
