/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class X509UtilTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(X509UtilTest.class);

    @Test
    public void testReadPemCertificatesWithHeaders() throws CertificateException {
        String pem = "-----BEGIN CERTIFICATE-----\n" +
"Subject: GlobalSign Trusted Platform Module Root CA\n" +
"\n" +
"MIID1zCCAr+gAwIBAgILBAAAAAABIBkJGa4wDQYJKoZIhvcNAQELBQAwgYcxOzA5\n" +
"BgNVBAsTMkdsb2JhbFNpZ24gVHJ1c3RlZCBDb21wdXRpbmcgQ2VydGlmaWNhdGUg\n" +
"QXV0aG9yaXR5MRMwEQYDVQQKEwpHbG9iYWxTaWduMTMwMQYDVQQDEypHbG9iYWxT\n" +
"aWduIFRydXN0ZWQgUGxhdGZvcm0gTW9kdWxlIFJvb3QgQ0EwHhcNMDkwMzE4MTAw\n" +
"MDAwWhcNNDkwMzE4MTAwMDAwWjCBhzE7MDkGA1UECxMyR2xvYmFsU2lnbiBUcnVz\n" +
"dGVkIENvbXB1dGluZyBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkxEzARBgNVBAoTCkds\n" +
"b2JhbFNpZ24xMzAxBgNVBAMTKkdsb2JhbFNpZ24gVHJ1c3RlZCBQbGF0Zm9ybSBN\n" +
"b2R1bGUgUm9vdCBDQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAPi3\n" +
"Gi0wHyTT7dq24caFAp31gXFDvALRGJrMiP+TunIYPacYD8eBVSNEiVoCUcVfYxzl\n" +
"/DPTxmRyGXgQM8CVh9THrxDTW7N2PSAoZ7fvlmjTiBL/IQ7m1F+9wGI/FuaMTphz\n" +
"w6lBda7HFlIYKTbM/vz24axCHLzJ8Xir2L889D9MMIerBRqouVsDGauH+TIOdw4o\n" +
"IGKhorqfsDro57JHwViMWlbB1Ogad7PBX5X/e9GDNdZTdo4c0bZnKO+dEtzEgKCh\n" +
"JmQ53Mxa9y4xPMGRRnjLsyxuM99vkkYXy7rnxctSo7GtGIJJVabNuXZ0peaY9ku0\n" +
"CUgKAsQndLkTHz8bIh0CAwEAAaNCMEAwDgYDVR0PAQH/BAQDAgEGMA8GA1UdEwEB\n" +
"/wQFMAMBAf8wHQYDVR0OBBYEFB4jY/CFtfYlTu0awFC+ZXzH1BV6MA0GCSqGSIb3\n" +
"DQEBCwUAA4IBAQCVb7lI4d49u7EtCX03/rUCCiaZ64NMxxqRmcSVdUx6yRrbl8NN\n" +
"FNr6ym2kTvwe1+JkTCiDxKzJsOR/jcPczAFiYpFbZQYLA6RK0bzbL9RGcaw5LLhY\n" +
"o/flqsu3N2/HNesWbekoxLosP6NLGEOnpj1B+R3y7HCQq/08U5l3Ete6TRKTAavc\n" +
"0mty+uCFtLXf+tirl7xSaIGD0LwcYNdzLEB9g4je6FQSWL0QOXb+zR755QYupZAw\n" +
"G1PnOgYWfqWowKcQQexFPrKGlzh0ncITV/nBEi++fnnZ7TFiwaKwe+WussrROV1S\n" +
"DDF29dmoMcbSFDL+DgSMabVT6Qr6Ze1rbmSh\n" +
"-----END CERTIFICATE-----\n"+
"-----BEGIN CERTIFICATE-----\n" +
"Subject: VeriSign Trusted Platform Module Root CA\n" +
"\n" +
"MIID9zCCAt+gAwIBAgIQc3HALwPpy5ENrJ49S+Yo0TANBgkqhkiG9w0BAQUFADCB\n" +
"ljELMAkGA1UEBhMCVVMxFzAVBgNVBAoTDlZlcmlTaWduLCBJbmMuMTswOQYDVQQL\n" +
"EzJWZXJpU2lnbiBUcnVzdGVkIENvbXB1dGluZyBDZXJ0aWZpY2F0aW9uIEF1dGhv\n" +
"cml0eTExMC8GA1UEAxMoVmVyaVNpZ24gVHJ1c3RlZCBQbGF0Zm9ybSBNb2R1bGUg\n" +
"Um9vdCBDQTAeFw0wNTEwMjUwMDAwMDBaFw00NTEwMjQyMzU5NTlaMIGWMQswCQYD\n" +
"VQQGEwJVUzEXMBUGA1UEChMOVmVyaVNpZ24sIEluYy4xOzA5BgNVBAsTMlZlcmlT\n" +
"aWduIFRydXN0ZWQgQ29tcHV0aW5nIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MTEw\n" +
"LwYDVQQDEyhWZXJpU2lnbiBUcnVzdGVkIFBsYXRmb3JtIE1vZHVsZSBSb290IENB\n" +
"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2VBrQOh7Y1WHczxt1IGn\n" +
"rlBKKr0K6OZXVllr6F5vuF0lneajCRpxZJUne7v7/apxesr59LrQcDbOktlrGXXz\n" +
"OXjKBaXZBkKOO8ROIE2Ae6rslOMynlPHWP4HKdogZe3LPPViuC14uhgz5iXJ8pFf\n" +
"UQdKxCdKWTzICg0B+l46pp42Fxr83eR72O9kSzEqijkaYdoDx06yxWALguUGzS7H\n" +
"5sycnu2tAGDGFrmsQoh8mK4FUi5vce8JuWuhirCXZzmP/fV4tYndw+HJS/D7XuWk\n" +
"BWcbm0clLTbmYZ7Ae1rl1XTP5pd8Q3cHGB6R0HcXyACyE4Vjp/g0J3HJjHd3L6Tr\n" +
"wwIDAQABoz8wPTAPBgNVHRMBAf8EBTADAQH/MAsGA1UdDwQEAwIBBjAdBgNVHQ4E\n" +
"FgQUDxT14yCIRBbKJr+NH843FepFbwYwDQYJKoZIhvcNAQEFBQADggEBAH6Ujdhq\n" +
"L8b38+swPJ2Jowu7UxcgzRWr2ayLqx8MwQkN1giSLsxcj6sHseMwqHLz2fCFfK2W\n" +
"Si5ZeyIWlB1TOJtwdpcmafFNPs0hOWWyl3D4uY2kfiQFu+GdpRtM7T+lsgDLlXvz\n" +
"t6nW2TscwGRKZA34hhvtE7294JJ56DlIcdSm3CY9MBvJ+pF2LyOC1NddHDf8ywKE\n" +
"XA9CXVmu3dpvwE+s7flQPS2E+y5EaWkXtKso2JTaHMS3PSwSJRhmknf/QtEkPZfb\n" +
"jzbhZZxVu48EZKOJL8lXzqm4hgpf7kX+WrVsCAny6AJkNn1xsQfvT0Y5OaVNH2RF\n" +
"j4ORjyt4A5du3H4=\n" +
"-----END CERTIFICATE-----";
        List<X509Certificate> certs = X509Util.decodePemCertificates(pem);
        for(X509Certificate cert : certs) {
            log.debug("Read certificate for: {}", cert.getSubjectX500Principal().getName());
        }
    }
    
    
    @Test
    public void decodePemCertificate() throws CertificateException {
        String text = "-----BEGIN CERTIFICATE----- \n" +
"MIICvTCCAaWgAwIBAgIGAUduoyedMA0GCSqGSIb3DQEBBQUAMBsxGTAXBgNVBAMTEG10d2lsc29u \n" +
"LXBjYS1haWswHhcNMTQwNzI1MTc0ODEyWhcNMjQwNzI0MTc0ODEyWjAAMIIBIjANBgkqhkiG9w0B \n" +
"AQEFAAOCAQ8AMIIBCgKCAQEAldTItMK3myO/YP/n/KLnsuFLdCD6EaHtb0fbWRxsShTTbH9GTY+y \n" +
"LcpxsddC+QGT8vVwdcaVeZdIzxdNhRHb1zg8+iGI7/O5on3Q1UYmm9cW0r0jbnAOpxTG0yXIbn0a \n" +
"9nqIvx/pjAxXlpaX01gl68xRo5QC/3PtD07d/jYmZfrjs15LKR0Cz831zoc+bPP1d6mM/0bbplDP \n" +
"rplik8C0uIK3fMUTKcAJ1bwfUPmNV8WUt1Y4mObuTNEwjUd8jJnFave23gs395LP3PCEWLgaHCOE \n" +
"2t2rJIQ7b9vbjznKI+uXe1VkPvhUW+WQn52TETG0pJLaH2PHejkuiIPz7wlvHwIDAQABoyIwIDAe \n" +
"BgNVHREBAf8EFDASgRBISVMgSWRlbnRpdHkgS2V5MA0GCSqGSIb3DQEBBQUAA4IBAQBNId5Sn6u8 \n" +
"v5m9ts/K9qO7ZnGjtoMfSJ4PtYfBfjMREWpd/J/P0js32WDYOt2njDiDQBpZB8rAWckPVPxPjvhA \n" +
"XyvHiEr231OUHBByC7+KOchzyGlGtXup/8uYonZsocP4JgezxCSad6QPMFTjPeZpgo9Jn3Ev9iQH \n" +
"cp6eYkgg02bywnlMUAyasmDFEN/YuKS8oW2foW2fnrhlTr7ONc6usWjvWUi6uBRPFQBSgTHdYTCh \n" +
"JC/IP/hvyP4ZYHHskjkzOnw53IzmWR8RCnBScVNZGMbChe4Ld/fVi9wfwSxRTdefkuIoyWwIk39u \n" +
"ml+ObIvHqTJAJOMylwNcZM1k8Bra \n" +
"-----END CERTIFICATE-----";
        X509Certificate certificate = X509Util.decodePemCertificate(text);
        log.debug("Read certificate for: {}", certificate.getIssuerX500Principal().getName());
    }

    @Test
    public void decodePemCertificateCRLF() throws CertificateException {
        String text = "-----BEGIN CERTIFICATE-----\r\n" +
"MIICvTCCAaWgAwIBAgIGAUduoyedMA0GCSqGSIb3DQEBBQUAMBsxGTAXBgNVBAMTEG10d2lsc29u\r\n" +
"LXBjYS1haWswHhcNMTQwNzI1MTc0ODEyWhcNMjQwNzI0MTc0ODEyWjAAMIIBIjANBgkqhkiG9w0B\r\n" +
"AQEFAAOCAQ8AMIIBCgKCAQEAldTItMK3myO/YP/n/KLnsuFLdCD6EaHtb0fbWRxsShTTbH9GTY+y\r\n" +
"LcpxsddC+QGT8vVwdcaVeZdIzxdNhRHb1zg8+iGI7/O5on3Q1UYmm9cW0r0jbnAOpxTG0yXIbn0a\r\n" +
"9nqIvx/pjAxXlpaX01gl68xRo5QC/3PtD07d/jYmZfrjs15LKR0Cz831zoc+bPP1d6mM/0bbplDP\r\n" +
"rplik8C0uIK3fMUTKcAJ1bwfUPmNV8WUt1Y4mObuTNEwjUd8jJnFave23gs395LP3PCEWLgaHCOE\r\n" +
"2t2rJIQ7b9vbjznKI+uXe1VkPvhUW+WQn52TETG0pJLaH2PHejkuiIPz7wlvHwIDAQABoyIwIDAe\r\n" +
"BgNVHREBAf8EFDASgRBISVMgSWRlbnRpdHkgS2V5MA0GCSqGSIb3DQEBBQUAA4IBAQBNId5Sn6u8\r\n" +
"v5m9ts/K9qO7ZnGjtoMfSJ4PtYfBfjMREWpd/J/P0js32WDYOt2njDiDQBpZB8rAWckPVPxPjvhA\r\n" +
"XyvHiEr231OUHBByC7+KOchzyGlGtXup/8uYonZsocP4JgezxCSad6QPMFTjPeZpgo9Jn3Ev9iQH\r\n" +
"cp6eYkgg02bywnlMUAyasmDFEN/YuKS8oW2foW2fnrhlTr7ONc6usWjvWUi6uBRPFQBSgTHdYTCh\r\n" +
"JC/IP/hvyP4ZYHHskjkzOnw53IzmWR8RCnBScVNZGMbChe4Ld/fVi9wfwSxRTdefkuIoyWwIk39u\r\n" +
"ml+ObIvHqTJAJOMylwNcZM1k8Bra\r\n" +
"-----END CERTIFICATE-----";
        X509Certificate certificate = X509Util.decodePemCertificate(text);
        log.debug("Read certificate for: {}", certificate.getIssuerX500Principal().getName());
    }

    @Test
    public void decodePemCertificateFile() throws CertificateException, IOException {
        InputStream in = getClass().getResourceAsStream("/aik.pem");
        X509Certificate aikCertificate = X509Util.decodePemCertificate(IOUtils.toString(in));
        log.debug("aik: {}", aikCertificate.getIssuerX500Principal().getName());
    }

}
