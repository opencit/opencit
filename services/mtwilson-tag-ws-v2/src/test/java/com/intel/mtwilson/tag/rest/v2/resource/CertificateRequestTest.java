/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.tag.TagCertificateAuthority;
import com.intel.mtwilson.tag.TagConfiguration;
import com.intel.mtwilson.tag.Util;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.tag.model.CertificateCollection;
import com.intel.mtwilson.tag.model.CertificateFilterCriteria;
import com.intel.mtwilson.tag.model.CertificateLocator;
import com.intel.mtwilson.tag.rest.v2.repository.CertificateRepository;
import com.intel.mtwilson.tag.rest.v2.repository.CertificateRequestRepository;
import com.intel.mtwilson.tag.rest.v2.rpc.ProvisionTagCertificate;
import com.intel.mtwilson.tag.selection.SelectionBuilder;
import com.intel.mtwilson.tag.selection.json.TagSelectionModule;
import java.nio.charset.Charset;
import org.junit.Test;
import com.intel.mtwilson.tag.selection.xml.*;
import java.util.Date;

/**
 *
 * @author ssbangal
 */
public class CertificateRequestTest {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificateRequestTest.class);
    
    @Test
    public void testSearchCertificates() throws Exception{
        CertificateRepository repo = new CertificateRepository();        
        CertificateFilterCriteria fc = new CertificateFilterCriteria();
//        fc.issuerContains = "asset";
        CertificateCollection search = repo.search(fc);
        for(Certificate obj : search.getCertificates())
            System.out.println(obj.getSubject()+ "::" + obj.getIssuer());
    }
    
    @Test
    public void testCreateCertRequestFromJson() throws Exception{
        String selection1 = "{\"selections\":[{\"attributes\":[{\"text\":{\"value\":\"Country=US\"},\"oid\":\"2.5.4.789.1\"},{\"text\":{\"value\":\"State=CA\"},\"oid\":\"2.5.4.789.1\"},{\"text\":{\"value\":\"State=TX\"},\"oid\":\"2.5.4.789.1\"},{\"text\":{\"value\":\"City=Folsom\"},\"oid\":\"2.5.4.789.1\"},{\"text\":{\"value\":\"City=El Paso\"},\"oid\":\"2.5.4.789.1\"}]}]}";
        SelectionsType selections = Util.fromJson(selection1);
        TagCertificateAuthority ca = new TagCertificateAuthority(new TagConfiguration(My.configuration().getConfiguration()));
        byte[] tagCertificate = ca.createTagCertificate(UUID.valueOf("76df5add-a808-4e62-916d-e53adadc166b"), selections);
        log.debug("tag certificate {}", tagCertificate);
        // now store it in database...
        CertificateDAO dao = TagJdbi.certificateDao();
        dao.insert(new UUID(), tagCertificate, Sha1Digest.digestOf(tagCertificate).toString(), Sha256Digest.digestOf(tagCertificate).toString(), "76df5add-a808-4e62-916d-e53adadc166b", "mtwilson-tag-ca", new Date(), new Date());
    }

    @Test
    public void testProvisionTagCert() throws Exception{
        String selection1 = "{\"selections\":[{\"attributes\":[{\"text\":{\"value\":\"Country=US\"},\"oid\":\"2.5.4.789.1\"},{\"text\":{\"value\":\"State=CA\"},\"oid\":\"2.5.4.789.1\"},{\"text\":{\"value\":\"State=TX\"},\"oid\":\"2.5.4.789.1\"},{\"text\":{\"value\":\"City=Folsom\"},\"oid\":\"2.5.4.789.1\"},{\"text\":{\"value\":\"City=El Paso\"},\"oid\":\"2.5.4.789.1\"}]}]}";
        SelectionsType selections = Util.fromJson(selection1);
        ProvisionTagCertificate repo = new ProvisionTagCertificate();
        Certificate certificate = repo.createOne(new UUID().toString(), selections, null, null);
        log.debug("tag {}", certificate.getSha1().toHexString());
    }
    
    @Test
    public void testCreateCertRequest() throws Exception{
        CertificateRequestRepository repo = new CertificateRequestRepository();
        
        SelectionsType selections = SelectionBuilder.factory()
                .selection()
                .textAttributeKV("Country", "US")
                .textAttributeKV("State", "CA")
                .textAttributeKV("City", "Folsom")
                .textAttributeKV("City", "El Paso")
                .build();
        String json = Util.toJson(selections); // {"selections":[{"attributes":[{"text":{"value":"Country=US"},"oid":"2.5.4.789.1"},{"text":{"value":"State=CA"},"oid":"2.5.4.789.1"},{"text":{"value":"City=Folsom"},"oid":"2.5.4.789.1"},{"text":{"value":"City=El Paso"},"oid":"2.5.4.789.1"}]}]}
        log.debug("json: {}", json); // 
        String xml = Util.toXml(selections); // <?xml version="1.0" encoding="UTF-8" standalone="yes"?><selections xmlns="urn:mtwilson-tag-selection"><selection><attribute oid="2.5.4.789.1"><text>Country=US</text></attribute><attribute oid="2.5.4.789.1"><text>State=CA</text></attribute><attribute oid="2.5.4.789.1"><text>City=Folsom</text></attribute><attribute oid="2.5.4.789.1"><text>City=El Paso</text></attribute></selection></selections>
        log.debug("xml: {}", xml);
        return;
        /*
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        ProvisionTagCertificate crrun = new ProvisionTagCertificate();
        crrun.setSubject("76df5add-a808-4e62-916d-e53adadc166b");
        crrun.setContentType("application/json");
//        crrun.setContent(mapper.writeValueAsBytes(selection));
        crrun.setContent(json.getBytes(Charset.forName("UTF-8")));
        byte[] tagCertificate = crrun.call();
        log.debug("tag certificate {}", tagCertificate);
        */
    }

    
    @Test
    public void testRetrieveCertificate() throws Exception{
        CertificateRepository repo = new CertificateRepository();        
        CertificateLocator locator = new CertificateLocator();
        locator.id = UUID.valueOf("f53c05a2-c2ed-423d-ac46-3ccf03b4be67");
        Certificate retrieve = repo.retrieve(locator);
        System.out.println(retrieve.getIssuer()+ "::" + retrieve.getSubject());
    }
    
    // example json serialization: {"subject":"449aa4e2-7621-402e-988e-1234f3f1d59a","selections":[{"attributes":[{"text":{"value":"Country=US"},"oid":"2.5.4.789.1"},{"text":{"value":"State=CA"},"oid":"2.5.4.789.1"},{"text":{"value":"City=Folsom"},"oid":"2.5.4.789.1"},{"text":{"value":"City=El Paso"},"oid":"2.5.4.789.1"}]}]}
    @JacksonXmlRootElement(localName="tag_certificate_request")
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    public static class TagCertificateRequest {
        public String subject;
        @JsonUnwrapped // without this annotation, you get selections inside selections: {"subject":"449aa4e2-7621-402e-988e-1234f3f1d59a","selections":{"selections":[{"attributes":[{"text":{"value":"Country=US"},"oid":"2.5.4.789.1"},{"text":{"value":"State=CA"},"oid":"2.5.4.789.1"},{"text":{"value":"City=Folsom"},"oid":"2.5.4.789.1"},{"text":{"value":"City=El Paso"},"oid":"2.5.4.789.1"}]}]}}
        public SelectionsType selections;
    }
    
    @Test
    public void testWrapSelectionsTypeWithCertificateRequest() throws Exception {
        SelectionsType selections = SelectionBuilder.factory()
                .selection()
                .textAttributeKV("Country", "US")
                .textAttributeKV("State", "CA")
                .textAttributeKV("City", "Folsom")
                .textAttributeKV("City", "El Paso")
                .build();
        String json = Util.toJson(selections); // {"selections":[{"attributes":[{"text":{"value":"Country=US"},"oid":"2.5.4.789.1"},{"text":{"value":"State=CA"},"oid":"2.5.4.789.1"},{"text":{"value":"City=Folsom"},"oid":"2.5.4.789.1"},{"text":{"value":"City=El Paso"},"oid":"2.5.4.789.1"}]}]}
        log.debug("json: {}", json); // 
        String xml = Util.toXml(selections); // <?xml version="1.0" encoding="UTF-8" standalone="yes"?><selections xmlns="urn:mtwilson-tag-selection"><selection><attribute oid="2.5.4.789.1"><text>Country=US</text></attribute><attribute oid="2.5.4.789.1"><text>State=CA</text></attribute><attribute oid="2.5.4.789.1"><text>City=Folsom</text></attribute><attribute oid="2.5.4.789.1"><text>City=El Paso</text></attribute></selection></selections>
        log.debug("xml: {}", xml);
        
        TagCertificateRequest tagCertificateRequest = new TagCertificateRequest();
        tagCertificateRequest.subject = "449aa4e2-7621-402e-988e-1234f3f1d59a";
        tagCertificateRequest.selections = selections;
        
        // json
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        mapper.registerModule(new TagSelectionModule());
        log.debug("tag certificate request json: {}", mapper.writeValueAsString(tagCertificateRequest));
        
        // xml with jackson (not jaxb!)
        // get this error: com.fasterxml.jackson.databind.JsonMappingException: Unwrapping serialization not yet supported for XML (through reference chain: com.intel.mtwilson.tag.rest.v2.resource.TagCertificateRequest["selections"])
        // but if unwrapping is disabled then we get the selections inside selections: tag certificate request xml: <tag_certificate_request><subject>449aa4e2-7621-402e-988e-1234f3f1d59a</subject><selections><selections><selection><attributes><attribute><text><value>Country=US</value></text><oid>2.5.4.789.1</oid></attribute><attribute><text><value>State=CA</value></text><oid>2.5.4.789.1</oid></attribute><attribute><text><value>City=Folsom</value></text><oid>2.5.4.789.1</oid></attribute><attribute><text><value>City=El Paso</value></text><oid>2.5.4.789.1</oid></attribute></attributes></selection></selections></selections></tag_certificate_request>
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        xmlMapper.registerModule(new TagSelectionModule());
//        xmlMapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(xmlMapper.getTypeFactory())); // result is still not like the .xsd:   <TagCertificateRequest><subject>449aa4e2-7621-402e-988e-1234f3f1d59a</subject><selections><selection><subject/><attribute><text><value>Country=US</value><encoding/></text><der/><xer/><oid>2.5.4.789.1</oid></attribute><attribute><text><value>State=CA</value><encoding/></text><der/><xer/><oid>2.5.4.789.1</oid></attribute><attribute><text><value>City=Folsom</value><encoding/></text><der/><xer/><oid>2.5.4.789.1</oid></attribute><attribute><text><value>City=El Paso</value><encoding/></text><der/><xer/><oid>2.5.4.789.1</oid></attribute><id/><name/><not_before/><not_after/></selection></selections></TagCertificateRequest>
        log.debug("tag certificate request xml: {}", xmlMapper.writeValueAsString(tagCertificateRequest));
    }

}
