/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.business;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.mtwilson.My;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.as.data.MwAssetTagCertificate;
import com.intel.mtwilson.atag.model.AttributeOidAndValue;
import com.intel.mtwilson.atag.model.X509AttributeCertificate;
import com.intel.mtwilson.datatypes.AssetTagCertAssociateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertRevokeRequest;
import com.intel.mtwilson.datatypes.TagDataType;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.util.encoders.Base64Encoder;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class AssetTagCertBOTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AssetTagCertBOTest.class);
    
    @Test
    public void testJdbcConnection() throws Exception {
        log.debug("JDBC URL: {}", My.jdbc().url());        
        Connection c = My.jdbc().connection();
        Statement s = c.createStatement();
        s.executeQuery("SELECT 1");
        s.close();
        c.close();
    }
    
    @Test
    public void testAtagConfigValues() throws IOException, ApiException {
        AssetTagCertBO certBO = new AssetTagCertBO();
        String oid = "1.3.6.1.4.1.99999.3";
        TagDataType tag = certBO.getTagInfoByOID(oid);
        System.out.println(tag.name);
    }
    
    @Test
    public void testAssetTagCert() throws IOException{
        List<MwAssetTagCertificate> atagCerts = My.jpa().mwAssetTagCertificate().findAssetTagCertificatesByHostUUID("494cb5dc-a3e1-4e46-9b52-e694349b1654");
         if (atagCerts.isEmpty()) {
                    System.out.println("Asset tag certificate has not been provisioned for the host with UUID");
         } else {
                  //For each of the asset tag certs that are returned back, we need to validate the certificate first.
                  for (MwAssetTagCertificate atagTempCert : atagCerts){
                      //This is what is stored in NVRAM
                     Sha1Digest certSha1 = Sha1Digest.digestOf(atagTempCert.getCertificate());
                     System.out.println("sha1 of cert == " + certSha1.toString());
                     
                     // When Citrix code reads NVRAM, it reads it as string
                     certSha1 = Sha1Digest.digestOf(certSha1.toString().getBytes("UTF-8"));
                     System.out.println("sha1 of sha1 of cert == " + certSha1.toString());
                     
                    byte[] destination = new byte[Sha1Digest.ZERO.toByteArray().length + certSha1.toByteArray().length];                   
                    System.arraycopy(Sha1Digest.ZERO.toByteArray(), 0, destination, 0, Sha1Digest.ZERO.toByteArray().length);                     
                    System.arraycopy(certSha1.toByteArray(), 0, destination, Sha1Digest.ZERO.toByteArray().length, certSha1.toByteArray().length);  

                    // Final sha1 from citrix
                     Sha1Digest finalDigest = Sha1Digest.digestOf(destination);
                     System.out.println("Final SHA1 :" + finalDigest.toString());
                 }
          }
    }
    
    @Test
    public void insertAssetTagCert() {
        String attrCert = "MIICGzCCAQMCAQEwH6EdpBswGTEXMBUGAWkEEK3AjNJLBUBSvVDG4bbdZsmgXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBCwUAAgEBMCIYDzIwMTMwODI4MDMwODU2WhgPMjAxMzA5MjgwMzA4NTZaMEYwFAYMKwYBBAGGjR8BAQEBMQQMAlVTMBQGDCsGAQQBho0fAgICAjEEDAJDQTAYBgwrBgEEAYaNHwMDAwMxCAwGRm9sc29tMA0GCSqGSIb3DQEBCwUAA4IBAQA6EpPzMArxcoqy+oReAEAgr9fKi9pLt45eQd4svGnu0qfKnPrUiEJxedOULUd+O8aPs7sBYE3yS1lAHzAhS0BuTPvYLh4kYl5xftjl0KzCqgXJSHbCe/FcZmjj0CYt/avzxXslYguJicUqDnn7/I8Mr00qOx4AahJd8dbsTT0LGnX4vgD5d7AP9B27Ul5BqIdm1r3sg87adgltsHjz7GCgOIfNoCUYWGc11ERPlhTZq+qoRpGyxXi0LgbvQeMBX36V446WUrt3fG5ezlN4vOduOjEkWqGnjf32VYEdP34TsOCmD3bYzBB5HC1fDn7PLiuupkVPrWQ1stm+OD0cu0ii";
        AssetTagCertBO atagBO = new AssetTagCertBO();
        AssetTagCertCreateRequest atagRequest = new AssetTagCertCreateRequest();
        atagRequest.setCertificate(Base64.decodeBase64(attrCert.getBytes()));
        
        boolean importAssetTagCertificate = atagBO.importAssetTagCertificate(atagRequest);        
        System.out.println(importAssetTagCertificate);
    }
    
    @Test
    public void revokeAssetTagCert() {        
        String attrCert = "MIICGzCCAQMCAQEwH6EdpBswGTEXMBUGAWkEEK3AjNJLBUBSvVDG4bbdZsmgXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBCwUAAgEBMCIYDzIwMTMwODI4MDMwODU2WhgPMjAxMzA5MjgwMzA4NTZaMEYwFAYMKwYBBAGGjR8BAQEBMQQMAlVTMBQGDCsGAQQBho0fAgICAjEEDAJDQTAYBgwrBgEEAYaNHwMDAwMxCAwGRm9sc29tMA0GCSqGSIb3DQEBCwUAA4IBAQA6EpPzMArxcoqy+oReAEAgr9fKi9pLt45eQd4svGnu0qfKnPrUiEJxedOULUd+O8aPs7sBYE3yS1lAHzAhS0BuTPvYLh4kYl5xftjl0KzCqgXJSHbCe/FcZmjj0CYt/avzxXslYguJicUqDnn7/I8Mr00qOx4AahJd8dbsTT0LGnX4vgD5d7AP9B27Ul5BqIdm1r3sg87adgltsHjz7GCgOIfNoCUYWGc11ERPlhTZq+qoRpGyxXi0LgbvQeMBX36V446WUrt3fG5ezlN4vOduOjEkWqGnjf32VYEdP34TsOCmD3bYzBB5HC1fDn7PLiuupkVPrWQ1stm+OD0cu0ii";
        AssetTagCertRevokeRequest atagRequest = new AssetTagCertRevokeRequest();
        atagRequest.setSha256OfAssetCert(Sha256Digest.digestOf(Base64.decodeBase64(attrCert.getBytes())).toByteArray());        
        AssetTagCertBO atagBO = new AssetTagCertBO();
        
        boolean revokeAssetTagCertificate = atagBO.revokeAssetTagCertificate(atagRequest);
        System.out.println(revokeAssetTagCertificate);
    }
    
    @Test
    public void mapAssetTagToHost() {        
        String attrCert = "MIICGzCCAQMCAQEwH6EdpBswGTEXMBUGAWkEEK3AjNJLBUBSvVDG4bbdZsmgXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBCwUAAgEBMCIYDzIwMTMwODI4MDMwODU2WhgPMjAxMzA5MjgwMzA4NTZaMEYwFAYMKwYBBAGGjR8BAQEBMQQMAlVTMBQGDCsGAQQBho0fAgICAjEEDAJDQTAYBgwrBgEEAYaNHwMDAwMxCAwGRm9sc29tMA0GCSqGSIb3DQEBCwUAA4IBAQA6EpPzMArxcoqy+oReAEAgr9fKi9pLt45eQd4svGnu0qfKnPrUiEJxedOULUd+O8aPs7sBYE3yS1lAHzAhS0BuTPvYLh4kYl5xftjl0KzCqgXJSHbCe/FcZmjj0CYt/avzxXslYguJicUqDnn7/I8Mr00qOx4AahJd8dbsTT0LGnX4vgD5d7AP9B27Ul5BqIdm1r3sg87adgltsHjz7GCgOIfNoCUYWGc11ERPlhTZq+qoRpGyxXi0LgbvQeMBX36V446WUrt3fG5ezlN4vOduOjEkWqGnjf32VYEdP34TsOCmD3bYzBB5HC1fDn7PLiuupkVPrWQ1stm+OD0cu0ii";
        AssetTagCertAssociateRequest mapReq = new AssetTagCertAssociateRequest(Sha256Digest.digestOf(Base64.decodeBase64(attrCert.getBytes())).toByteArray(), 10);
        AssetTagCertBO atagBO = new AssetTagCertBO();
        
        boolean mapAssetTagCertToHost = atagBO.mapAssetTagCertToHost(mapReq);
        System.out.println(mapAssetTagCertToHost);
    }

    @Test
    public void findValidAssetTagCertForHost() {        
        String hostUUID = "adc08cd2-4b05-4052-bd50-c6e1b6dd66c9";
        AssetTagCertBO atagBO = new AssetTagCertBO();
        
        MwAssetTagCertificate mapAssetTagCertToHost = atagBO.findValidAssetTagCertForHost(hostUUID);
        if (mapAssetTagCertToHost != null)
            System.out.println(mapAssetTagCertToHost.getId());
    }
    
    @Test
    public void showAttributesInCert() {
        String attrCert = "MIICGzCCAQMCAQEwH6EdpBswGTEXMBUGAWkEEK3AjNJLBUBSvVDG4bbdZsmgXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBCwUAAgEBMCIYDzIwMTMwODI4MDMwODU2WhgPMjAxMzA5MjgwMzA4NTZaMEYwFAYMKwYBBAGGjR8BAQEBMQQMAlVTMBQGDCsGAQQBho0fAgICAjEEDAJDQTAYBgwrBgEEAYaNHwMDAwMxCAwGRm9sc29tMA0GCSqGSIb3DQEBCwUAA4IBAQA6EpPzMArxcoqy+oReAEAgr9fKi9pLt45eQd4svGnu0qfKnPrUiEJxedOULUd+O8aPs7sBYE3yS1lAHzAhS0BuTPvYLh4kYl5xftjl0KzCqgXJSHbCe/FcZmjj0CYt/avzxXslYguJicUqDnn7/I8Mr00qOx4AahJd8dbsTT0LGnX4vgD5d7AP9B27Ul5BqIdm1r3sg87adgltsHjz7GCgOIfNoCUYWGc11ERPlhTZq+qoRpGyxXi0LgbvQeMBX36V446WUrt3fG5ezlN4vOduOjEkWqGnjf32VYEdP34TsOCmD3bYzBB5HC1fDn7PLiuupkVPrWQ1stm+OD0cu0ii";
        X509AttributeCertificate atagAttrCert = X509AttributeCertificate.valueOf(Base64.decodeBase64(attrCert.getBytes()));
        for (AttributeOidAndValue atagAttr : atagAttrCert.getTags()) {
            System.out.println("ATAG_" + atagAttr.getOid() + ":" + atagAttr.getValue());
        }
        
    }
}
