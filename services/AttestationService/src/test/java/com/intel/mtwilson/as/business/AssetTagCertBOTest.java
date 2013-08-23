/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.business;

import com.intel.dcsg.cpg.crypto.Sha256Digest;
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
        String attrCert = "MIICGDCCAQACAQEwH6EdpBswGTEXMBUGAWkEEJKnGiKMF0UioYv9PtPQCzmgXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBBQUAAgEBMCIYDzIwMTMwODA4MjIyMTEzWhgPMjAxMzA5MDgyMjIxMTNaMEMwEwYLKwYBBAG9hDcBAQExBAwCVVMwEwYLKwYBBAG9hDgCAgIxBAwCQ0EwFwYLKwYBBAG9hDkDAwMxCAwGRm9sc29tMA0GCSqGSIb3DQEBBQUAA4IBAQCcN8KjjmR2H3LT5aL1SCFS4joy/7vAd3/xdJtkqrb3UAQHMdUUJQHf3frJsMJs22m0So0xs/f1sB15frC1LsQGF5+RYVXsClv0glStWbPYiqEfdM7dc/RDMRtrXKEH3sBlxMT7YS/g5E6qwmKZX9shQ3BYmeZi5A3DTzgHCbA3Cm4/MQbgWGjoamfWZ9EDk4Bww2y0ueRi60PfoLg43rcijr8Wf+JEzCRw040vIaH3DtFdmzvvGRdqE3YlEkrUL3gEIZNY3Po1NL4cb238vT5CHZTt9NyD7xSv0XkwOY4RbSUdYBsxfH3mEcdQ6LtJdfF1BUXfMThKN3TctFcY/dLF";
        AssetTagCertBO atagBO = new AssetTagCertBO();
        AssetTagCertCreateRequest atagRequest = new AssetTagCertCreateRequest();
        atagRequest.setCertificate(Base64.decodeBase64(attrCert.getBytes()));
        
        boolean importAssetTagCertificate = atagBO.importAssetTagCertificate(atagRequest);        
        System.out.println(importAssetTagCertificate);
    }
    
    @Test
    public void revokeAssetTagCert() {        
        String attrCert = "MIICGDCCAQACAQEwH6EdpBswGTEXMBUGAWkEEJKnGiKMF0UioYv9PtPQCzmgXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBBQUAAgEBMCIYDzIwMTMwODA4MjIyMTEzWhgPMjAxMzA5MDgyMjIxMTNaMEMwEwYLKwYBBAG9hDcBAQExBAwCVVMwEwYLKwYBBAG9hDgCAgIxBAwCQ0EwFwYLKwYBBAG9hDkDAwMxCAwGRm9sc29tMA0GCSqGSIb3DQEBBQUAA4IBAQCcN8KjjmR2H3LT5aL1SCFS4joy/7vAd3/xdJtkqrb3UAQHMdUUJQHf3frJsMJs22m0So0xs/f1sB15frC1LsQGF5+RYVXsClv0glStWbPYiqEfdM7dc/RDMRtrXKEH3sBlxMT7YS/g5E6qwmKZX9shQ3BYmeZi5A3DTzgHCbA3Cm4/MQbgWGjoamfWZ9EDk4Bww2y0ueRi60PfoLg43rcijr8Wf+JEzCRw040vIaH3DtFdmzvvGRdqE3YlEkrUL3gEIZNY3Po1NL4cb238vT5CHZTt9NyD7xSv0XkwOY4RbSUdYBsxfH3mEcdQ6LtJdfF1BUXfMThKN3TctFcY/dLF";
        AssetTagCertRevokeRequest atagRequest = new AssetTagCertRevokeRequest();
        atagRequest.setSha256OfAssetCert(Sha256Digest.digestOf(Base64.decodeBase64(attrCert.getBytes())).toByteArray());        
        AssetTagCertBO atagBO = new AssetTagCertBO();
        
        boolean revokeAssetTagCertificate = atagBO.revokeAssetTagCertificate(atagRequest);
        System.out.println(revokeAssetTagCertificate);
    }
    
    @Test
    public void mapAssetTagToHost() {        
        String attrCert = "MIICGDCCAQACAQEwH6EdpBswGTEXMBUGAWkEEJKnGiKMF0UioYv9PtPQCzmgXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBBQUAAgEBMCIYDzIwMTMwODA4MjIyMTEzWhgPMjAxMzA5MDgyMjIxMTNaMEMwEwYLKwYBBAG9hDcBAQExBAwCVVMwEwYLKwYBBAG9hDgCAgIxBAwCQ0EwFwYLKwYBBAG9hDkDAwMxCAwGRm9sc29tMA0GCSqGSIb3DQEBBQUAA4IBAQCcN8KjjmR2H3LT5aL1SCFS4joy/7vAd3/xdJtkqrb3UAQHMdUUJQHf3frJsMJs22m0So0xs/f1sB15frC1LsQGF5+RYVXsClv0glStWbPYiqEfdM7dc/RDMRtrXKEH3sBlxMT7YS/g5E6qwmKZX9shQ3BYmeZi5A3DTzgHCbA3Cm4/MQbgWGjoamfWZ9EDk4Bww2y0ueRi60PfoLg43rcijr8Wf+JEzCRw040vIaH3DtFdmzvvGRdqE3YlEkrUL3gEIZNY3Po1NL4cb238vT5CHZTt9NyD7xSv0XkwOY4RbSUdYBsxfH3mEcdQ6LtJdfF1BUXfMThKN3TctFcY/dLF";
        AssetTagCertAssociateRequest mapReq = new AssetTagCertAssociateRequest(Sha256Digest.digestOf(Base64.decodeBase64(attrCert.getBytes())).toByteArray(), 10);
        AssetTagCertBO atagBO = new AssetTagCertBO();
        
        boolean mapAssetTagCertToHost = atagBO.mapAssetTagCertToHost(mapReq);
        System.out.println(mapAssetTagCertToHost);
    }
    
}
