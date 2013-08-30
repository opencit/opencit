/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.business;

import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.mtwilson.as.data.MwAssetTagCertificate;
import com.intel.mtwilson.atag.model.AttributeOidAndValue;
import com.intel.mtwilson.atag.model.X509AttributeCertificate;
import com.intel.mtwilson.datatypes.AssetTagCertAssociateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertRevokeRequest;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class AssetTagCertBOTest {
    
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
