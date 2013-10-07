/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.restlet;

import com.intel.mtwilson.atag.model.Tag;
import com.intel.mtwilson.atag.model.RdfTriple;
import com.intel.mtwilson.atag.model.CertificateRequestTagValue;
import com.intel.mtwilson.atag.model.CertificateRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import example.restlet.ObjectBox;
import example.restlet.Fruit;
import com.intel.mtwilson.atag.client.At;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.My;
import com.intel.mtwilson.atag.RestletApplication;
import com.intel.mtwilson.atag.X509AttrBuilder;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.atag.model.Configuration;
import com.intel.mtwilson.atag.model.Selection;
import com.intel.mtwilson.atag.model.SelectionTagValue;
import com.intel.mtwilson.atag.resource.CertificateResource.CertificateActionChoice;
import com.intel.mtwilson.atag.resource.CertificateResource.CertificateRevokeAction;
import java.net.URL;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.restlet.representation.Representation;

/**
 * One difference between Component and Server is that Component automatically writes INFO logs for incoming requests
 * (like access log)
 *
 * @author jbuhacoff
 */
public class TagApiTest2 {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static Component component;
    private ObjectMapper mapper = new ObjectMapper();
    public final static String OID_CUSTOMER_ROOT = "1.3.6.1.4.1.99999"; // instead of OID_ASSET_TAG_SOLUTION + ".9"; // http://oid-info.com/get/1.3.6.1.4.1  is private organizations on internet, 999999 is INVENTED value that is not curently registered , for use in our demonstrations


    private static int getPort(URL url) {
        int port = url.getPort();
        if( port == - 1 ) {
            if( "http".equals(url.getProtocol()) ) {
                port = 80;
            }
            else if( "https".equals(url.getProtocol())) {
                port = 443;
            }
        }
        return port;
    }
    
    // if you make changes here,  probably need to review start() in the com.intel.dcsg.cpg.atag.cmd.StartHttpServer class
    @BeforeClass
    public static void startServer() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, getPort(My.configuration().getAssetTagServerURL()));
//        component.getServers().add(Protocol.FILE);,  // apparently this one is not required for the Directory() resource
        component.getClients().add(Protocol.FILE); //  required to enable the Directory() resource defined in RestletApplication... dont' know why it has to be added to the clients, unless when we say "file://..." we are acting as a client of a file system resource  internally ???
        component.getClients().add(Protocol.CLAP); //  required to enable the Directory() resource defined in RestletApplication... dont' know why it has to be added to the clients, unless when we say "file://..." we are acting as a client of a file system resource  internally ???
        component.getDefaultHost().attach("", new RestletApplication()); // if the restlet attaches to "/fruit", this must be "", not "/";  but if the restlet attaches to "fruit", then this can be "/"
        component.start();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        component.stop();
    }

    @Test
    public void testPostTagsGetJson() throws IOException {
        String name = "city";
        String oid = "2.2.2.2";
        ArrayList<String> values = new ArrayList<String>(); // notice we do not post the id and uuid... those are generaetd by the server (would be ignored if we sent them)
        values.add("folsom");
        values.add("sacramento");
        String json = At.tags().post(new Tag[]{new Tag(name, oid, values)}, MediaType.APPLICATION_JSON).getText(); //.write(System.out);
        log.debug("Posted tag: {}", json);
        // output: {"id":14,"uuid":"980e4d44-bf8f-422d-916a-964b2a26bfbe","name":"city","oid":"2.2.2.2","values":["folsom","sacramento"]}
    }
    /*
     @Test
     public void testPostTagGetTag() throws IOException {
     String name = "city";
     String oid = "2.2.2.2";
     ArrayList<String> values = new ArrayList<String>(); // notice we do not post the id and uuid... those are generaetd by the server (would be ignored if we sent them)
     values.add("folsom");
     values.add("sacramento");
     Tag tag = new ClientResource(baseurl() + "tags").post(new Tag(name, oid, values), Tag.class);
     log.debug("Posted tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s values:%s", String.valueOf(tag.getId()), tag.getUuid(), tag.getName(), tag.getOid(), StringUtils.join(tag.getValues(), ", ")));
     // output: Posted tag: id:12  uuid:f2453130-2b2d-4457-9c2f-b7a79d5ffe8d  name:city  oid:2.2.2.2 values:folsom, sacramento
     // if jackson can't deserialize the UUID object you get something like this:  Caused by: com.fasterxml.jackson.core.JsonParseException: Unexpected character ('}' (code 125)): was expecting a colon to separate field name and value
     // but this has now been fixed by adding Jackson annotations to the UUID class in cpg-io
     }
     */

    @Test
    public void testPostMultipleTags() throws IOException {
        ArrayList<String> states = new ArrayList<String>(); // notice we do not post the id and uuid... those are generaetd by the server (would be ignored if we sent them)
        states.add("CA");
        states.add("TX");
        ArrayList<String> cities = new ArrayList<String>(); // notice we do not post the id and uuid... those are generaetd by the server (would be ignored if we sent them)
        cities.add("folsom");
        cities.add("sacramento");
        Tag[] tags = new Tag[]{new Tag("state", "1.1.1.1", states), new Tag("city", "2.2.2.2", cities)};
//        String json = new ClientResource(baseurl() + "tags").post(tags, MediaType.TEXT_PLAIN).getText();
//        log.debug("Posted: {}", json);
        // output: Posted: [{"id":15,"uuid":"93bcc6ec-6617-479c-acb8-71c1ad5103ef","name":"state","oid":"1.1.1.1","values":["CA","TX"]},{"id":16,"uuid":"4975a6e6-3901-4ca2-89c9-ab32b8d390b6","name":"city","oid":"2.2.2.2","values":["folsom","sacramento"]}]

        Tag[] results = At.tags().post(tags, Tag[].class);
        log.debug("Posted {} tags", results.length);
        for (int i = 0; i < results.length; i++) {
            Tag tag = results[i];
            log.debug("Posted tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s values:%s", String.valueOf(tag.getId()), tag.getUuid(), tag.getName(), tag.getOid(), StringUtils.join(tag.getValues(), ", ")));
        }

    }

    @Test
    public void testAddValuesToExistingTag() throws IOException {
        // create a tag so we have an existing tag
        ArrayList<String> states = new ArrayList<String>(); // notice we do not post the id and uuid... those are generaetd by the server (would be ignored if we sent them)
        states.add("CA");
        states.add("TX");
        Tag[] results = At.tags().post(new Tag[]{new Tag("state", "1.1.1.1", states)}, Tag[].class);
        Tag existingTag = results[0];
        log.debug("Posted tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s values:%s", String.valueOf(existingTag.getId()), existingTag.getUuid(), existingTag.getName(), existingTag.getOid(), StringUtils.join(existingTag.getValues(), ", ")));
        // output: Posted tag: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1 values:CA, TX
        // now add more values.  NOTE:   String[] works but ArrayList<String>() doesn't.  it needs to match the method signature for the resource...
        /*
         ArrayList<String> moreStates = new ArrayList<String>(); // notice we do not post the id and uuid... those are generaetd by the server (would be ignored if we sent them)
         states.add("NY");
         states.add("AZ");          */
        String[] moreStates = new String[]{"NY", "AZ"};
//        Tag updatedTag = new ClientResource(baseurl() + "tags/"+existingTag.getUuid()+"/values").post(moreStates, Tag.class);
//        String[] updatedValues = new ClientResource(baseurl() + "tags/"+existingTag.getUuid()+"/values").post(moreStates, String[].class); // shows up on server as empty string... try sending String[] instead
        String[] updatedValues = At.tagValues(existingTag.getUuid()).post(moreStates, String[].class); // shows up on server as empty string... try sending String[] instead
        log.debug("Updated tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s values:%s", String.valueOf(existingTag.getId()), existingTag.getUuid(), existingTag.getName(), existingTag.getOid(), StringUtils.join(updatedValues, ", ")));
        // output: Updated tag: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1 values:CA, TX, NY, AZ
    }

    @Test
    public void testReplaceValuesForExistingTag() throws IOException {
        // create a tag so we have an existing tag
        ArrayList<String> states = new ArrayList<String>(); // notice we do not post the id and uuid... those are generaetd by the server (would be ignored if we sent them)
        states.add("CA");
        states.add("TX");
        Tag[] results = At.tags().post(new Tag[]{new Tag("state", "1.1.1.1", states)}, Tag[].class);
        Tag existingTag = results[0];
        log.debug("Posted tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s values:%s", String.valueOf(existingTag.getId()), existingTag.getUuid(), existingTag.getName(), existingTag.getOid(), StringUtils.join(existingTag.getValues(), ", ")));
        // output: Posted tag: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1 values:CA, TX
        // now add more values.  NOTE:   String[] works but ArrayList<String>() doesn't.  it needs to match the method signature for the resource...
        /*
         ArrayList<String> moreStates = new ArrayList<String>(); // notice we do not post the id and uuid... those are generaetd by the server (would be ignored if we sent them)
         states.add("NY");
         states.add("AZ");          */
        String[] replacementStates = new String[]{"NY", "AZ"};
//        Tag updatedTag = new ClientResource(baseurl() + "tags/"+existingTag.getUuid()+"/values").post(moreStates, Tag.class);
//        String[] updatedValues = new ClientResource(baseurl() + "tags/"+existingTag.getUuid()+"/values").post(moreStates, String[].class); // shows up on server as empty string... try sending String[] instead
        String[] updatedValues = At.tagValues(existingTag.getUuid()).put(replacementStates, String[].class); // shows up on server as empty string... try sending String[] instead
        log.debug("Updated tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s values:%s", String.valueOf(existingTag.getId()), existingTag.getUuid(), existingTag.getName(), existingTag.getOid(), StringUtils.join(updatedValues, ", ")));
        // output: Updated tag: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1 values:CA, TX, NY, AZ
    }

    @Test
    public void testDeleteValuesForExistingTag() throws IOException {
        // create a tag so we have an existing tag
        ArrayList<String> states = new ArrayList<String>(); // notice we do not post the id and uuid... those are generaetd by the server (would be ignored if we sent them)
        states.add("CA");
        states.add("TX");
        Tag[] results = At.tags().post(new Tag[]{new Tag("state", "1.1.1.1", states)}, Tag[].class);
        Tag existingTag = results[0];
        log.debug("Posted tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s values:%s", String.valueOf(existingTag.getId()), existingTag.getUuid(), existingTag.getName(), existingTag.getOid(), StringUtils.join(existingTag.getValues(), ", ")));
        // output: Posted tag: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1 values:CA, TX
        // now add more values.  NOTE:   String[] works but ArrayList<String>() doesn't.  it needs to match the method signature for the resource...
        /*
         ArrayList<String> moreStates = new ArrayList<String>(); // notice we do not post the id and uuid... those are generaetd by the server (would be ignored if we sent them)
         states.add("NY");
         states.add("AZ");          */
        String[] replacementStates = new String[0];
//        Tag updatedTag = new ClientResource(baseurl() + "tags/"+existingTag.getUuid()+"/values").post(moreStates, Tag.class);
//        String[] updatedValues = new ClientResource(baseurl() + "tags/"+existingTag.getUuid()+"/values").post(moreStates, String[].class); // shows up on server as empty string... try sending String[] instead
        String[] updatedValues = At.tagValues(existingTag.getUuid()).put(replacementStates, String[].class); // shows up on server as empty string... try sending String[] instead
        log.debug("Updated tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s values:%s", String.valueOf(existingTag.getId()), existingTag.getUuid(), existingTag.getName(), existingTag.getOid(), StringUtils.join(updatedValues, ", ")));
        // output: Updated tag: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1 values:CA, TX, NY, AZ
    }

    @Test
    public void testGetExistingTagByUuid() throws IOException {
        // create a tag so we have an existing tag
        Tag[] results = At.tags().post(new Tag[]{new Tag("state", "1.1.1.1")}, Tag[].class);
        Tag insertedTag = results[0];
        log.debug("Posted tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s", String.valueOf(insertedTag.getId()), insertedTag.getUuid(), insertedTag.getName(), insertedTag.getOid()));
        // output: Posted tag: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1
        Tag existingTag = At.tag(insertedTag.getUuid()).get(Tag.class); // shows up on server as empty string... try sending String[] instead
        log.debug("Existing tag by uuid: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s", String.valueOf(existingTag.getId()), existingTag.getUuid(), existingTag.getName(), existingTag.getOid()));
        // output: Existing tag: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1 
    }

    @Test
    public void testGetExistingTagByOid() throws IOException {
        // create a tag so we have an existing tag
        Tag[] results = At.tags().post(new Tag[]{new Tag("state", "1.1.1.1")}, Tag[].class);
        Tag insertedTag = results[0];
        log.debug("Posted tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s", String.valueOf(insertedTag.getId()), insertedTag.getUuid(), insertedTag.getName(), insertedTag.getOid()));
        // output: Posted tag: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1
        Tag existingTag = At.tag(insertedTag.getOid()).get(Tag.class); // shows up on server as empty string... try sending String[] instead
        log.debug("Existing tag by oid: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s", String.valueOf(existingTag.getId()), existingTag.getUuid(), existingTag.getName(), existingTag.getOid()));
        // output: Existing tag: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1 
    }

    @Test
    public void testGetExistingTagByName() throws IOException {
        // create a tag so we have an existing tag
        Tag[] results = At.tags().post(new Tag[]{new Tag("state", "1.1.1.1")}, Tag[].class);
        Tag insertedTag = results[0];
        log.debug("Posted tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s", String.valueOf(insertedTag.getId()), insertedTag.getUuid(), insertedTag.getName(), insertedTag.getOid()));
        // output: Posted tag: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1
        Tag existingTag = At.tag(insertedTag.getName()).get(Tag.class); // shows up on server as empty string... try sending String[] instead
        log.debug("Existing tag by name: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s", String.valueOf(existingTag.getId()), existingTag.getUuid(), existingTag.getName(), existingTag.getOid()));
        // output: Existing tag: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1 
    }
    
    @Test
    public void testGetExistingTagJson() throws IOException {
        // create a tag so we have an existing tag
        Tag[] newTags = new Tag[]{new Tag("state", "1.1.1.1")};
        log.debug("Tags to insert: {}", mapper.writeValueAsString(newTags));
        Tag[] results = At.tags().post(newTags, Tag[].class);
        Tag insertedTag = results[0];
        log.debug("Posted tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s", String.valueOf(insertedTag.getId()), insertedTag.getUuid(), insertedTag.getName(), insertedTag.getOid()));
        // output: Posted tag: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1
        String existingTag = At.tag(insertedTag.getUuid()).get(String.class); // shows up on server as empty string... try sending String[] instead
        log.debug("Existing tag: {}", existingTag);
        String existingTags = At.tags().get(String.class);
        log.debug("Existing tags: {}", existingTags);
        // output: Existing tag: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1 
    }
    
    @Test
    public void testUpdateExistingTag() throws IOException {
        // create a tag so we have an existing tag
        Tag[] insertResults = At.tags().post(new Tag[]{new Tag("state", "1.1.1.1")}, Tag[].class);
        Tag existingTag = insertResults[0];
        log.debug("Posted tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s values: %s", String.valueOf(existingTag.getId()), existingTag.getUuid(), existingTag.getName(), existingTag.getOid(), StringUtils.join(existingTag.getValues(), ", ")));
        // output: Posted tag: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1
        existingTag.setName("state2");
        existingTag.setOid("1.1.1.2");
        ArrayList<String> states = new ArrayList<String>(); // notice we do not post the id and uuid... those are generaetd by the server (would be ignored if we sent them)
        states.add("CA");
        states.add("TX");
        existingTag.setValues(states);
        Tag updatedTag = At.tag(existingTag.getUuid()).put(existingTag, Tag.class); // shows up on server as empty string... try sending String[] instead
        log.debug("Updated tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s values: %s", String.valueOf(updatedTag.getId()), updatedTag.getUuid(), updatedTag.getName(), updatedTag.getOid(), StringUtils.join(updatedTag.getValues(), ", ")));
        // output: Existing tag: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1 
    }

    @Test
    public void testDeleteExistingTag() throws IOException {
        // create a tag so we have an existing tag
        Tag[] insertResults = At.tags().post(new Tag[]{new Tag("state", "1.1.1.1", new ArrayList<String>())}, Tag[].class);
        Tag existingTag = insertResults[0];
        log.debug("Posted tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s values: %s", String.valueOf(existingTag.getId()), existingTag.getUuid(), existingTag.getName(), existingTag.getOid(), StringUtils.join(existingTag.getValues(), ", ")));
        // output: Posted tag: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1
        At.tag(existingTag.getUuid()).delete();
        // now that we deleted the record, try loading it again -- to ensure it's gone
        Tag deletedTag = At.tag(existingTag.getUuid()).get(Tag.class);
        assertNull(deletedTag);
        if (deletedTag != null) {
            // of course when deleting works, we don't reach this line.
            log.debug("Deleted tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s values: %s", String.valueOf(deletedTag.getId()), deletedTag.getUuid(), deletedTag.getName(), deletedTag.getOid(), StringUtils.join(deletedTag.getValues(), ", ")));
        }
    }

    @Test
    public void testSearchTagsByUuid() throws Exception {
        // first make sure that our database has tags we're going to query
        Tag[] insertResults = At.tags().post(new Tag[]{new Tag("country", "1.1.1.1", Arrays.asList(new String[]{"US", "MX"}))}, Tag[].class);
        Tag tag = insertResults[0];
        ClientResource resource = At.tags();
        resource.addQueryParameter("id", tag.getUuid().toString());
        Tag[] searchResults = resource.get(Tag[].class);
        report(searchResults);
        assertEquals(1, searchResults.length); // there should only be ONE tag for any UUID

    }

    @Test
    public void testSearchTagsByNameEqualTo() throws Exception {
        // first make sure that our database has tags we're going to query
        At.tags().post(new Tag[]{new Tag("country", "1.1.1.1", Arrays.asList(new String[]{"US", "MX"}))}, Tag[].class);
        ClientResource resource = At.tags();
        resource.addQueryParameter("nameEqualTo", "country");
        Tag[] searchResults = resource.get(Tag[].class);
        report(searchResults);
        assertTrue(searchResults.length >= 1); // there MAY be more than one tag with the same name
    }

    /**
     * XXX TODO: figure out the following problem: in the function below, if you insert zero or one records, you see
     * "Inserted x records" and then you get search results. But if you enable the line where you insert two records,
     * then you see "Inserted 2 records" but the search query hangs... and you get "recoverable" communication error
     * 1001 from restlet and it says it will automatically try to recover but it just hangs there. So definitely the new
     * records are inserted to the database and the client returns, but then hangs on the NEXT request which doesn't
     * happen if you insert something different. That's mysterious.
     *
     * @throws Exception
     */
    @Test
    public void testSearchTagsByNameContains() throws Exception {
        // first make sure that our database has tags we're going to query
        Tag[] insertResults = At.tags().post(new Tag[]{new Tag("country", "1.1.1.1", Arrays.asList(new String[]{"US", "MX"}))}, Tag[].class);
//        Tag[] insertResults = At.tags().post(new Tag[]{ new Tag("geoCountry", "1.1.1.1", Arrays.asList(new String[] { "US", "MX" })), new Tag("geoState", "2.2.2.2", Arrays.asList(new String[] { "CA", "NY" })) }, Tag[].class);
        log.debug("Inserted {} records", insertResults.length);
//        At.tags().post(new Tag[]{  }, Tag[].class);  // if you try to post twice... get a hang.  
        ClientResource resource = At.tags();
        resource.addQueryParameter("nameContains", "geo");
        Tag[] searchResults = resource.get(Tag[].class);
        report(searchResults);
        assertTrue(searchResults.length >= 2); // we should find at least the two we just added
    }

    @Test
    public void testSearchTagsByValueEqualTo() throws Exception {
        // first make sure that our database has tags we're going to query
        log.debug("Adding tags");
        At.tags().post(new Tag[]{new Tag("country", "1.1.1.1", Arrays.asList(new String[]{"US", "MX"}))}, Tag[].class);
        log.debug("Searching for tags");
        ClientResource resource = At.tags();
        resource.addQueryParameter("valueEqualTo", "US");
        Tag[] searchResults = resource.get(Tag[].class);
        report(searchResults);
        assertTrue(searchResults.length >= 1); // there MAY be more than one tag with the same name
    }

    @Test
    public void testSearchTagsByValueEqualToAndOidEqualTo() throws Exception {
        // first make sure that our database has tags we're going to query
        log.debug("Adding tags");
        At.tags().post(new Tag[]{new Tag("country", "1.1.1.1", Arrays.asList(new String[]{"US", "MX"})), new Tag("country", "2.2.2.2", Arrays.asList(new String[]{"US", "MX"}))}, Tag[].class);
        log.debug("Searching for tags");
        ClientResource resource = At.tags();
        resource.addQueryParameter("valueEqualTo", "US");
        resource.addQueryParameter("oidEqualTo", "1.1.1.1");
        Tag[] searchResults = resource.get(Tag[].class);
        report(searchResults);
        assertTrue(searchResults.length >= 1); // there MAY be more than one tag with the same name
        for (Tag tag : searchResults) {
            assertEquals("1.1.1.1", tag.getOid()); // but ALL of the tags in results should have oid 1.1.1.1, and NONE should have 2.2.2.2
        }
    }

    @Test
    public void testPostRdfTriples() throws IOException {
        RdfTriple[] triples = new RdfTriple[]{new RdfTriple("country", "contains", "state"), new RdfTriple("state", "contains", "city")};
        RdfTriple[] results = At.rdfTriples().post(triples, RdfTriple[].class);
        log.debug("Posted {} rdf-triples", results.length);
        for (int i = 0; i < results.length; i++) {
            RdfTriple triple = results[i];
            log.debug("Posted rdf-triple: {}", String.format("id:%s  uuid:%s  subject:%s  predicate:%s  object:%s", String.valueOf(triple.getId()), triple.getUuid(), triple.getSubject(), triple.getPredicate(), triple.getObject()));
        }

    }

    @Test
    public void testSearchRdfTriplesBySubject() throws Exception {
        RdfTriple[] triples = new RdfTriple[]{new RdfTriple("country", "contains", "state"), new RdfTriple("state", "contains", "city")};
        At.rdfTriples().post(triples, RdfTriple[].class);
        ClientResource resource = At.rdfTriples();
        resource.addQueryParameter("subjectEqualTo", "country");
        RdfTriple[] searchResults = resource.get(RdfTriple[].class);
        report(searchResults);
        assertTrue(searchResults.length >= 1); // there MAY be more than one rdf-triple with the same subject (and probably will be)
        for (RdfTriple triple : searchResults) {
            assertEquals("country", triple.getSubject()); // ALL of the results should have "country" in the subject
        }
    }

    @Test
    public void testGetExistingRdfTriple() throws IOException {
        // create a rdfTriple so we have an existing rdfTriple
        RdfTriple[] results = At.rdfTriples().post(new RdfTriple[]{new RdfTriple("country", "contains", "state")}, RdfTriple[].class);
        RdfTriple insertedRdfTriple = results[0];
        log.debug("Posted rdfTriple: {}", String.format("id:%s  uuid:%s  subject:%s  predicate:%s  object:%s", String.valueOf(insertedRdfTriple.getId()), insertedRdfTriple.getUuid(), insertedRdfTriple.getSubject(), insertedRdfTriple.getPredicate(), insertedRdfTriple.getObject()));
        // output: Posted rdfTriple: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1
        RdfTriple existingRdfTriple = At.rdfTriple(insertedRdfTriple.getUuid()).get(RdfTriple.class); // shows up on server as empty string... try sending String[] instead
        log.debug("Existing rdfTriple: {}", String.format("id:%s  uuid:%s  subject:%s  predicate:%s  object:%s", String.valueOf(existingRdfTriple.getId()), existingRdfTriple.getUuid(), existingRdfTriple.getSubject(), existingRdfTriple.getPredicate(), existingRdfTriple.getObject()));
        // output: Existing rdfTriple: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1 
    }

    @Test
    public void testUpdateExistingRdfTriple() throws IOException {
        // create a rdfTriple so we have an existing rdfTriple
        RdfTriple[] insertResults = At.rdfTriples().post(new RdfTriple[]{new RdfTriple("country", "contains", "state")}, RdfTriple[].class);
        RdfTriple existingRdfTriple = insertResults[0];
        log.debug("Posted rdfTriple: {}", String.format("id:%s  uuid:%s  subject:%s  predicate:%s  object:%s", String.valueOf(existingRdfTriple.getId()), existingRdfTriple.getUuid(), existingRdfTriple.getSubject(), existingRdfTriple.getPredicate(), existingRdfTriple.getObject()));
        // output: Posted rdfTriple: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1
        existingRdfTriple.setSubject("country2");
        RdfTriple updatedRdfTriple = At.rdfTriple(existingRdfTriple.getUuid()).put(existingRdfTriple, RdfTriple.class);
        log.debug("Updated rdfTriple: {}", String.format("id:%s  uuid:%s  subject:%s  predicate:%s  object:%s", String.valueOf(updatedRdfTriple.getId()), updatedRdfTriple.getUuid(), updatedRdfTriple.getSubject(), updatedRdfTriple.getPredicate(), updatedRdfTriple.getObject()));
        // output: Existing rdfTriple: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1 
    }

    @Test
    public void testDeleteExistingRdfTriple() throws IOException {
        // create a rdfTriple so we have an existing rdfTriple
        RdfTriple[] insertResults = At.rdfTriples().post(new RdfTriple[]{new RdfTriple("country", "contains", "state")}, RdfTriple[].class);
        RdfTriple existingRdfTriple = insertResults[0];
        log.debug("Posted rdfTriple: {}", String.format("id:%s  uuid:%s  subject:%s  predicate:%s  object:%s", String.valueOf(existingRdfTriple.getId()), existingRdfTriple.getUuid(), existingRdfTriple.getSubject(), existingRdfTriple.getPredicate(), existingRdfTriple.getObject()));
        // output: Posted rdfTriple: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1
        At.rdfTriple(existingRdfTriple.getUuid()).delete();
        // now that we deleted the record, try loading it again -- to ensure it's gone
        RdfTriple deletedRdfTriple = At.rdfTriple(existingRdfTriple.getUuid()).get(RdfTriple.class);
        assertNull(deletedRdfTriple);
        if (deletedRdfTriple != null) {
            // of course when deleting works, we don't reach this line.
            log.debug("Deleted rdfTriple: {}", String.format("id:%s  uuid:%s  subject:%s  predicate:%s  object:%s", String.valueOf(deletedRdfTriple.getId()), deletedRdfTriple.getUuid(), deletedRdfTriple.getSubject(), deletedRdfTriple.getPredicate(), deletedRdfTriple.getObject()));
        }
    }

    @Test
    public void testCreateSelection() throws IOException {
        ArrayList<String> states = new ArrayList<String>(); // notice we do not post the id and uuid... those are generaetd by the server (would be ignored if we sent them)
        states.add("CA");
        states.add("TX");
        ArrayList<String> cities = new ArrayList<String>(); // notice we do not post the id and uuid... those are generaetd by the server (would be ignored if we sent them)
        cities.add("folsom");
        cities.add("sacramento");
        Tag[] tags = new Tag[]{new Tag("state", "1.1.1.1", states), new Tag("city", "2.2.2.2", cities)};
        Tag[] results = At.tags().post(tags, Tag[].class);
        log.debug("Posted {} tags", results.length);
        for (int i = 0; i < results.length; i++) {
            Tag tag = results[i];
            log.debug("Posted tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s values:%s", String.valueOf(tag.getId()), tag.getUuid(), tag.getName(), tag.getOid(), StringUtils.join(tag.getValues(), ", ")));
        }
        // now create a selection with these tags
        Selection selection = new Selection("default");
        selection.setTags(new ArrayList<SelectionTagValue>());
        selection.getTags().add(new SelectionTagValue(results[0].getName(), results[0].getOid(), results[0].getValues().get(0)));
        selection.getTags().add(new SelectionTagValue(results[1].getName(), results[1].getOid(), results[1].getValues().get(1)));
        log.debug("prepared selections: "+mapper.writeValueAsString(selection));
        Selection[] createdSelections = At.selections().post(new Selection[] { selection }, Selection[].class);
        log.debug("created {} new selections", createdSelections.length);
        for(int i=0; i<createdSelections.length; i++) {
            Selection s = createdSelections[i];
            log.debug("Confirmed created selection: {}", String.format("id:%s  uuid:%s  name:%s", String.valueOf(s.getId()), s.getUuid(), s.getName()));
        }
    }
    
    @Test
    public void testGetSelections() throws IOException {
        String existingSelections = At.selections().get(String.class);
        log.debug("Selections: {}", existingSelections);
    }
    
    @Test
    public void testPostCertificateRequestWithDefaultSelection() throws IOException {
        // no need to insert tags or selections... this test assumes they are already defined in the database and configured for automatic certificate requests
        // we just provide a subject (host uuid) .
        // XXX TODO the certificate request subject is a String, but since we expect a UUID we should probably change that type... 
        CertificateRequest req1 = new CertificateRequest(new UUID().toString()); //Arrays.asList(new CertificateRequestTagValue[]{new CertificateRequestTagValue("country", null, "US"), new CertificateRequestTagValue("state", null, "CA")}));

        CertificateRequest[] certificateRequests = new CertificateRequest[]{ req1 };
        log.debug("testPostCertificateRequests: {}", mapper.writeValueAsString(certificateRequests));
        CertificateRequest[] results = At.certificateRequests().post(certificateRequests, CertificateRequest[].class);
        log.debug("testPostCertificateRequests results: {}", mapper.writeValueAsString(results));
        report(results);
    }    
    
    @Test
    public void testRevokeCertificate() throws IOException {
        // make a request to be automatically approved (must have a default selection and automatic approval configured)
        CertificateRequest req1 = new CertificateRequest(new UUID().toString()); //Arrays.asList(new CertificateRequestTagValue[]{new CertificateRequestTagValue("country", null, "US"), new CertificateRequestTagValue("state", null, "CA")}));
        CertificateRequest[] certificateRequests = new CertificateRequest[]{ req1 };
        log.debug("testPostCertificateRequests: {}", mapper.writeValueAsString(certificateRequests));
        CertificateRequest[] results = At.certificateRequests().post(certificateRequests, CertificateRequest[].class);
        log.debug("testPostCertificateRequests results: {}", mapper.writeValueAsString(results));
        report(results);
        // get a reference to the automatically approved certificate
        UUID certificateUuid = results[0].getCertificate();
        // now revoke the certificate
        CertificateActionChoice action = new CertificateActionChoice();
        action.revoke = new CertificateRevokeAction();
        log.debug("Posting revoke action: {}", mapper.writeValueAsString(action));
        CertificateActionChoice test = mapper.readValue("{\"revoke\":{\"uuid\":null,\"effective\":1381101239544}}", CertificateActionChoice.class);
        assertNotNull(test.revoke);
        assertNotNull(test.revoke.getEffective());
//        assertNotNull(test.revoke.getAction());
        CertificateActionChoice result = At.certificates(certificateUuid).post(action, CertificateActionChoice.class);
        assertNotNull(result.revoke);
        log.debug("Revoke result: {}", mapper.writeValueAsString(result));
        log.debug("Revoke certificate: {}", result.revoke.getUuid());
        log.debug("Revoke effective date: {}", result.revoke.getEffective());
    }
    
    @Test
    public void testPostMultipleCertificateRequestsWithDefaultSelection() throws IOException {
        // no need to insert tags or selections... this test assumes they are already defined in the database and configured for automatic certificate requests
        // we just provide a subject (host uuid) .
        // XXX TODO the certificate request subject is a String, but since we expect a UUID we should probably change that type... 
        CertificateRequest req1 = new CertificateRequest(new UUID().toString()); //Arrays.asList(new CertificateRequestTagValue[]{new CertificateRequestTagValue("country", null, "US"), new CertificateRequestTagValue("state", null, "CA")}));
        CertificateRequest req2 = new CertificateRequest(new UUID().toString()); //Arrays.asList(new CertificateRequestTagValue[]{new CertificateRequestTagValue("country", null, "US"), new CertificateRequestTagValue("state", null, "CA")}));
        CertificateRequest req3 = new CertificateRequest(new UUID().toString()); //Arrays.asList(new CertificateRequestTagValue[]{new CertificateRequestTagValue("country", null, "MX"), new CertificateRequestTagValue("city", null, "Mexico City")}));

        CertificateRequest[] certificateRequests = new CertificateRequest[]{
            req1,
            req2,
            req3
        };
        log.debug("testPostCertificateRequests: {}", mapper.writeValueAsString(certificateRequests));
        CertificateRequest[] results = At.certificateRequests().post(certificateRequests, CertificateRequest[].class);
        log.debug("testPostCertificateRequests results: {}", mapper.writeValueAsString(results));
        report(results);
    }    
    
    
    @Test
    public void testPostCertificateRequestsWithSelection() throws IOException {
        // before we can make a certificate request, we need to define tags and tag values!
        Tag[] tags = new Tag[]{
            new Tag("country", OID_CUSTOMER_ROOT + ".5.1.1.1", new String[]{"US", "MX"}),
            new Tag("state", OID_CUSTOMER_ROOT + ".5.2.2.2", new String[]{"CA", "NY"}),
            new Tag("city", OID_CUSTOMER_ROOT + ".5.3.3.3", new String[]{"Folsom", "Sacramento"}) // note: not including mexico city, so we can validate that a certificate request with an invalid tag will not be issued with that tag
        };
        Tag[] insertedTags = At.tags().post(tags, Tag[].class);
        report(insertedTags);

        Selection selection = new Selection("test selection");
        selection.setTags(new ArrayList<SelectionTagValue>());
        for(Tag insertedTag : insertedTags) {
            for(String value : insertedTag.getValues()) {
                selection.getTags().add(new SelectionTagValue(insertedTag.getName(), insertedTag.getOid(), value));
            }
        }
        Selection[] insertedSelections = At.selections().post(new Selection[] { selection }, Selection[].class);
        report(insertedSelections);
        
        // XXX TODO the certificate request subject is a String, but since we expect a UUID we should probably change that type... 
        CertificateRequest req1 = new CertificateRequest(new UUID().toString(), insertedSelections[0].getName()); //Arrays.asList(new CertificateRequestTagValue[]{new CertificateRequestTagValue("country", null, "US"), new CertificateRequestTagValue("state", null, "CA")}));
        CertificateRequest req2 = new CertificateRequest(new UUID().toString(), insertedSelections[0].getName()); //Arrays.asList(new CertificateRequestTagValue[]{new CertificateRequestTagValue("country", null, "US"), new CertificateRequestTagValue("state", null, "CA")}));
        CertificateRequest req3 = new CertificateRequest(new UUID().toString(), insertedSelections[0].getName()); //Arrays.asList(new CertificateRequestTagValue[]{new CertificateRequestTagValue("country", null, "MX"), new CertificateRequestTagValue("city", null, "Mexico City")}));

        CertificateRequest[] certificateRequests = new CertificateRequest[]{
            req1,
            req2,
            req3
        };
        log.debug("testPostCertificateRequests: {}", mapper.writeValueAsString(certificateRequests));
        CertificateRequest[] results = At.certificateRequests().post(certificateRequests, CertificateRequest[].class);
        log.debug("testPostCertificateRequests results: {}", mapper.writeValueAsString(results));
        report(results);
    }

    // for this test to work you should disable automatic approval... XXX TODO or maybe we should rewrite it to insert the certicate request using a DAO directly and THEN poll for it and approve it
    @Test
    public void testApproveCertificateRequest() throws IOException, NoSuchAlgorithmException {
        // first delete all existing certificate requests
        CertificateRequest[] existingRequests = At.certificateRequests().get(CertificateRequest[].class);
        log.debug("There are {} existing certificate-requests", existingRequests.length);
        for (CertificateRequest existingRequest : existingRequests) {
            log.debug("{}", mapper.writeValueAsString(existingRequest));
            if (existingRequest == null) {
                log.debug("there is a null request in the list returned from the server!");
                continue;
            }
            log.debug("Deleting certificate-request: {}", (existingRequest.getUuid() == null ? "(no uuid, skipping)" : existingRequest.getUuid()));
            At.certificateRequest(existingRequest.getUuid()).delete();
        }
        // create a certificate request
        testPostMultipleCertificateRequestsWithDefaultSelection(); // or testPostCertificateRequestsWithSelection
        // now find the latest pending certificate requests
        CertificateRequest[] recentRequests = At.certificateRequests().get(CertificateRequest[].class);
        // create a certificate for each one with a new randomly generated issuer
        KeyPair cakey = RsaUtil.generateRsaKeyPair(2048); // throws NoSuchAlgorithmException
        X509Certificate cacert = X509Builder.factory().selfSigned("CN=Attr CA,OU=CPG,OU=DCSG,O=Intel,ST=CA,C=US", cakey).build();

        for (CertificateRequest recentRequest : recentRequests) {
            if( recentRequest == null ) {
                log.debug("Skipping null certificate request");
                continue;
            }
            // load the tags in the selection of this request
            Selection selection = At.selections(recentRequest.getSelection()).get(Selection.class);
            log.debug("Building certificate for request: {}", recentRequest.getSubject());
            X509AttrBuilder builder = X509AttrBuilder.factory()
                    .issuerName(cacert)
                    .issuerPrivateKey(cakey.getPrivate())
                    .randomSerial()
                    .subjectUuid(UUID.valueOf(recentRequest.getSubject()))
                    .expires(7, TimeUnit.DAYS);
            for (SelectionTagValue tag : selection.getTags()) {
                log.debug("Adding attribute: {} = {}", tag.getTagOid(), tag.getTagValue());
                builder.attribute(tag.getTagOid(), tag.getTagValue());
            }
            X509AttributeCertificateHolder certificateHolder = new X509AttributeCertificateHolder(builder.build());
            CertificateRequest approved = At.certificateRequestApproval(recentRequest.getUuid()).post(certificateHolder.getEncoded(), CertificateRequest.class);
            log.debug("approved request: {}", mapper.writeValueAsString(approved));
            // example output:  approved request: {"uuid":"75a7fabf-3a3c-4725-ab24-da3a5159529e","subject":"18148d3d-4078-49fa-a7c5-bfc73f1f15e8","tags":null,"status":"Done","certificateId":3}
            // XXX TODO NEXT:  notice "tags" is null -- server needs to validate the tags, and include them in the output too
            // XXX TODO NEXT:  replace the certificateId (long)  with certificate UUID  or just a URL to it. 
            // XXX TODO NEXT:  use the certificateId provided in this response and look up the certificate and display it
        }
        
        

    }

    // works but not when we have a random uuid as the host subject (instead of "host1")!! 
    /*
    @Test
    public void testSearchCertificateRequestsBySubject() throws Exception {
        testPostCertificateRequests();
        ClientResource resource = At.certificateRequests();
        resource.addQueryParameter("subjectEqualTo", "host1");
        CertificateRequest[] searchResults = resource.get(CertificateRequest[].class);
        report(searchResults);
        assertTrue(searchResults.length >= 1); // there MAY be more than one rdf-triple with the same subject (and probably will be)
        for (CertificateRequest certificateRequest : searchResults) {
            assertEquals("host1", certificateRequest.getSubject()); // ALL of the results should have "country" in the subject
        }
    }
    */

    /*
    @Test
    public void testSearchCertificateRequestsBySubjectAndTagName() throws Exception {
        testPostCertificateRequests();
        ClientResource resource = At.certificateRequests();
        resource.addQueryParameter("subjectEqualTo", "host1");
        resource.addQueryParameter("tagNameEqualTo", "country");
        CertificateRequest[] searchResults = resource.get(CertificateRequest[].class);
        report(searchResults);
        assertTrue(searchResults.length >= 1); // there MAY be more than one rdf-triple with the same subject (and probably will be)
        for (CertificateRequest certificateRequest : searchResults) {
            assertEquals("host1", certificateRequest.getSubject()); // ALL of the results should have "country" in the subject
            ArrayList<String> tagNames = new ArrayList<String>();
            for (CertificateRequestTagValue crtv : certificateRequest.getTags()) {
                tagNames.add(crtv.getName());
            }
            assertTrue(tagNames.contains("country"));
        }
    }
    */
    
    /*
    @Test
    public void testSearchCertificateRequestsBySubjectAndTag() throws Exception {
        testPostCertificateRequests();
        ClientResource resource = At.certificateRequests();
        resource.addQueryParameter("subjectEqualTo", "host3");
        resource.addQueryParameter("tagNameEqualTo", "country");
        resource.addQueryParameter("tagValueEqualTo", "MX");
        CertificateRequest[] searchResults = resource.get(CertificateRequest[].class);
        report(searchResults);
        assertTrue(searchResults.length >= 1); // there MAY be more than one rdf-triple with the same subject (and probably will be)
        for (CertificateRequest certificateRequest : searchResults) {
            assertEquals("host3", certificateRequest.getSubject()); // ALL of the results should have "country" in the subject
            assertEquals("MX", certificateRequest.getTags().get(0).getValue()); // ALL of the results should have "country" in the subject
        }
    }
    */
    
    /**
     * Output:
     * 
     * 
     * 
JSON Response: {"uuid":"549f8135-9d10-4167-b9cc-4b342a5c611b","name":"main","content":{"allowTagsInCertificateRequests":"false","approveAllCertificateRequests":"true","allowAutomaticTagSelection":"true","automaticTagSelectionName":"9f1377de-4a76-4ada-8b77-4c1d3ef9bd3a"}}

XML Response: <?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
<comment>main</comment>
<entry key="allowTagsInCertificateRequests">false</entry>
<entry key="allowAutomaticTagSelection">true</entry>
<entry key="approveAllCertificateRequests">true</entry>
<entry key="automaticTagSelectionName">9f1377de-4a76-4ada-8b77-4c1d3ef9bd3a</entry>
</properties>
     * 
     */
    @Test
    public void testReadExistingConfiguration() throws IOException {
        Representation jsonRepresentation = At.configuration("main").get(MediaType.APPLICATION_JSON);
        String json = IOUtils.toString(jsonRepresentation.getStream());
        jsonRepresentation.exhaust();
        jsonRepresentation.release();
        log.debug("JSON Response: {}", json);
        
        Representation xmlRepresentation = At.configuration("main").get(MediaType.APPLICATION_XML);
        String xml = IOUtils.toString(xmlRepresentation.getStream());
        xmlRepresentation.exhaust();
        xmlRepresentation.release();
        log.debug("XML Response: {}", xml);
        
        
//       Configuration main = At.configuration("main").get(Configuration.class);
//        log.debug("Existing configuration: {}", String.format("id:%s uuid:%s name:%s content:%s", String.valueOf(main.getId()), main.getUuid(), main.getName(), main.getContent()));
    }
    
    
    @Test
    public void testUpdateExistingConfiguration() throws IOException {
        Configuration main = At.configuration("main").get(Configuration.class);
        log.debug("Existing configuration: {}", String.format("id:%s uuid:%s name:%s content:%s", String.valueOf(main.getId()), main.getUuid(), main.getName(), main.getContent()));
        main.setJsonContent("{\"allowTagsInCertificateRequests\":\"true\",\"allowAutomaticTagSelection\":false,\"automaticTagSelectionName\":\"default\",\"approveAllCertificateRequests\":false}");
        log.debug("Updating: {}", mapper.writeValueAsString(main));
        Configuration updatedMain = At.configuration("main").put(main, Configuration.class);
        log.debug("Updated configuration: {}", String.format("uuid:%s name:%s content:%s", updatedMain.getUuid(), updatedMain.getName(), updatedMain.getContent()));
        // output: Existing rdfTriple: id:24  uuid:d42fa1a3-54c3-4774-aa95-dc3e61e5ece5  name:state  oid:1.1.1.1 
    }
    
    @Test
    public void testUpdateExistingConfigurationXml() throws IOException {
        // load the existing "main" configuration
        Representation mainRepresentation = At.configuration("main").get(MediaType.APPLICATION_XML);
        String main = IOUtils.toString(mainRepresentation.getStream());
        mainRepresentation.exhaust();
        mainRepresentation.release();
        Configuration mainConfiguration = new Configuration();
        mainConfiguration.setName("main");
        mainConfiguration.setXmlContent(main);
        // add a property to it
        mainConfiguration.getContent().setProperty("testUpdateExistingConfigurationXml", "it works");
        // store the updated configuration
//        Representation updatedMainRepresentation = At.configuration("main").put(mainConfiguration.getXmlContent(), MediaType.APPLICATION_XML);
        At.configuration("main").put(mainConfiguration.getXmlContent(), MediaType.APPLICATION_XML);
//        String updatedMain = IOUtils.toString(updatedMainRepresentation.getStream());
//        updatedMainRepresentation.exhaust();
//        updatedMainRepresentation.release();
//        log.debug("Response: {}", updatedMain);
    }
    
    @Test
    public void testUpdateExistingConfigurationXmlWithCustom() throws IOException {
        ClientResource client = At.configuration("main");
        client.accept(MediaType.APPLICATION_XML);
        Properties p = new Properties();
        p.setProperty("color", "green");
        Configuration c = new Configuration("test", p);
        client.put(c.getXmlContent(), MediaType.TEXT_XML);
//        client.put(c.getXmlContent(), MediaType.APPLICATION_XML);
        
    }
    
 
    private void report(CertificateRequest[] certificateRequests) throws JsonProcessingException {
        log.debug("Report: {} certificate-requests", certificateRequests.length);
        for (CertificateRequest certificateRequest : certificateRequests) {
            /*
            ArrayList<String> tagpairs = new ArrayList<String>();
            List<CertificateRequestTagValue> tags = certificateRequest.getTags();
            for (CertificateRequestTagValue tag : tags) {
                tagpairs.add(String.format("(%s: %s)", tag.getName(), tag.getValue()));
            }
            log.debug("certificate-request: {}", String.format("uuid:%s  subject:%s  tags: %s", certificateRequest.getUuid(), certificateRequest.getSubject(), StringUtils.join(tagpairs, " ")));
            * */
            log.debug("Certificate request: {}", String.format("uuid:%s  subject:%s  selection:%s status:%s", certificateRequest.getUuid(), certificateRequest.getSubject(), certificateRequest.getSelection(), certificateRequest.getStatus()));
        }
    }

    private void report(RdfTriple[] triples) throws JsonProcessingException {
        log.debug("Report: {} triples", triples.length);
        for (RdfTriple triple : triples) {
            log.debug("rdf-triple: {}", String.format("subject: %s  predicate: %s  object: %s", triple.getSubject(), triple.getPredicate(), triple.getObject()));
        }
    }

    private void report(Tag[] tags) throws JsonProcessingException {
        log.debug("Report: {} tags", tags.length);
//        ObjectMapper mapper = new ObjectMapper();
//        log.debug("tags: {}", mapper.writeValueAsString(tags));
        for (Tag tag : tags) {
//        for(int i=0; i<tags.length; i++) {
//            log.debug("index: {}", i);
//            Tag tag = tags[i];
//            log.debug("tag is null? {}", tag);
//            log.debug("id: {}", tag.getId());
//            log.debug("uuid: {}", tag.getUuid());
//            log.debug("name: {}", tag.getName());
//            log.debug("oid: {}", tag.getOid());
//            log.debug("values: {}", tag.getValues());
            log.debug("Tag: {}", String.format("uuid:%s  name:%s  oid:%s values:%s", tag.getUuid(), tag.getName(), tag.getOid(), (tag.getValues() == null ? "" : StringUtils.join(tag.getValues(), ", "))));
//            log.debug("Tag: {}", String.format("id:%s  uuid:%s  name:%s  oid:%s values:%s", String.valueOf(tag.getId()), tag.getUuid(), tag.getName(), tag.getOid(), (tag.getValues() == null ? "" : StringUtils.join(tag.getValues(), ", "))));
        }
    }
    
    private void report(Selection[] selections) throws JsonProcessingException {
        log.debug("Report: {} selections", selections.length);
        for(Selection selection : selections) {
            log.debug("Selection: {}", String.format("uuid: %s  name: %s  #tags: %s", selection.getUuid(), selection.getName(), (selection.getTags() == null ? "null" : String.valueOf(selection.getTags().size()))));
        }
    }
    
    @Test
    public void testGetStaticFile() throws IOException {
        ClientResource resource = new ClientResource(My.configuration().getAssetTagServerURL() + "/index.html");
        Representation representation = resource.get(MediaType.TEXT_HTML);
        log.debug("static index.html: {}", representation.getText());
    }
}
