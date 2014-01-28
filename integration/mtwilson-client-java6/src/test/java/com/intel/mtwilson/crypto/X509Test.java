/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

import com.intel.dcsg.cpg.x509.X509Util;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class X509Test {
    // see RsaUtil.toX509Certificate(String)
    @Test
    public void getAikCertificate() throws CertificateException, IOException {
        String pem = "-----BEGIN CERTIFICATE-----\nMIICuzCCAaOgAwIBAgIGATk0IZZzMA0GCSqGSIb3DQEBBQUAMBkxFzAVBgNVBAMMDkhJU19Qcml2\nYWN5X0NBMB4XDTEyMDgxNzEwMjk0MFoXDTIyMDgxNzEwMjk0MFowADCCASIwDQYJKoZIhvcNAQEB\nBQADggEPADCCAQoCggEBAOQp/gShT6ahgewNVksE4+ChgHKX7O1a8O+m9caEkQlOKBO2BqHKoPCd\nLaBJTeEd+3VHVkLqD85hrf34PnO97ZLzMItIqvfR0SuvW4FstUF2G0BlRFsfpujxYwMjVqrWl3pN\nC0QAp0kT/ecqltl+5FcC06UHSTDQsInEKWqBHd7/9bW7opTMD54R6Dxk5xObi5QDG1jq4/etyaG5\nzKZAmai+WJpAIICZRqE7n80CtbQQZXGLtJcUnyqgjJQxzvn57o8VY+JEMs7n1txhzPhRymDXi3Js\n/Al7/zp4tqdNTtdzBPIi7t99rNTRHsOxUQeKfnwyOZQbeomhHfLzzIX7850CAwEAAaMiMCAwHgYD\nVR0RAQH/BBQwEoEQSElTIElkZW50aXR5IEtleTANBgkqhkiG9w0BAQUFAAOCAQEABUXh2KsbZYae\nxjfot4Wpm9sJDvMb5vxoKhvpLhxEYcg+H5DcN4SSFAcTOidhcz5PfyK+UN8lkm03UIRIiFvNiF3T\nEwgTuqjTfOg2HF9jJbg0lID8h203ALefCCAD4vdODm5NOOlCCzm5yXHRv/hRE4ntrwF1SgGrERg5\nLJaHng35XUDsEJrHqyZfNlJRchRbyEzpy62gQqf6X6jSI3Rb6gBHz9Clzu/fqBqAD6WZx7gZY1yc\nNZ79XGREDljsyB9LXGD6P7K4HsPv69gWh7DDj48u/jvJKS7WtKMqiYTQ4dD3igLuxZhtC6LIcPoQ\ndxJD6ypQoF26HkjWo0/9s6E9Bg==\n-----END CERTIFICATE-----";
        System.out.println("PEM: "+pem);
        String base64 = pem.replace("-----BEGIN CERTIFICATE-----","").replace("-----END CERTIFICATE-----","").replaceAll("\\s+", "");
        System.out.println("Base64: "+base64);
        X509Certificate cert = X509Util.decodeDerCertificate(Base64.decodeBase64(base64));
        cert.checkValidity();
        System.out.println("Subject: "+cert.getSubjectX500Principal().getName());
        System.out.println("Issuer: "+cert.getIssuerX500Principal().getName());
        System.out.println("Signature Algorithm: "+cert.getSigAlgName());
    }
}
