/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import static com.intel.dcsg.cpg.crypto.RsaUtil.decodePemPrivateKey;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rksavino
 */
public class RsaUtilTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testDecodePemPrivateKey() throws CertificateException, CryptographyException {
        String pem = "-----BEGIN PRIVATE KEY-----\n" +
"MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCMuQTiiEVH26BbXPkvZrMfSSMJ\n" +
"8zm/8p5imDNc+rNMEeDx6KSkGFJ5jkP/r9P0KynuxljVUBTv1auuM/UVYabqd1PiejL7iJGC9GQ+\n" +
"rMba0BSCOEqAKCmiXgzayMSxtbQepAoMmeLZU8N2NPetwGlp5oA2lZ5gFHMB6g3NkqMnmI2OdZfg\n" +
"wnlWbEYO/p0Gr+4emzb+LxaBWa2HY+UTf/BqN8qDY4tn1t/osJX9FX0enuNOhbC/8/OW//yN3p93\n" +
"U0bkzukP5kpeG3d6lnnoU6Nqd1zu3jVUih+wkc11rWVm5vv++7B3sZnOhi5P6fcXDNy0hNQDcb4w\n" +
"g5vLNLxPyIvnAgMBAAECggEAR/iAhRNIy52J//AnCZUu2ztejkI1MPR3CNnquw0FEky6jT58pehC\n" +
"liSqpmRYtLI2ugX7fO1+J8NSEMBGAm91HMl2v4fb90U7loxDkMJw2Yw+UPNVNwJTeNU9IdA4uOLD\n" +
"gkBHW3aAzMHIX/ZrDhQwVZcrz77pxx3uS/ZWpOLwStk5w4TgSOLAgCFh6xzVGJDCF/uGqi1bB8uU\n" +
"2IRfi6t+oGVqipVgBMubr0kDOz62/i+DGxFM4mqwyrnBWQWsKak+3WkviBIY3A26ZJdPo7Od+BHM\n" +
"++Agx+oTHBuXF9JMyzR3/4RVuSYF2cGOHM0lOz6vd//RZPErJ108wa2Mbe4rAQKBgQDRjGfUzwPR\n" +
"pjHgJbo+1NsT5/thL3AXjMOT9NZwrpOlcp4CWWyLUGu4hR76PUe8c/DmU3mO5WQC2XnDD2/iZsyi\n" +
"YRzUfu3i6cmd/bvY3h3fKZhTflgO7hrQqcSCaCuKVFmhMFcUjHtAZJdYjuK02TtI4IbAzkD2sybW\n" +
"t/3V+HPFpwKBgQCr6th3u6USzs9whhf1+bIQubxdWSf1hY9rbrUMRuYidS3if6kLp1SqM079ERGy\n" +
"Fk5awHKtGzIUSmO/XH+ISxDR1NpyKghMdi3H7xH8OePguqX1xHA9dzNcPQYvTUlc2xfJR2CEafgj\n" +
"688IW83aiLL9nr97LnMmaCRTjoe8ea5PwQKBgG09Sd+57966SiP05wvcQT02YWj/purP+iFGsQfb\n" +
"KqNEhjTpU1mpGg7+bvC7Q4tt0bbw61zgHuwRXZWflY8sUh+QiswIQ5FYmT+gx+29lUsCTyOvqM5X\n" +
"uGjxJ71n+tLJOFR8c9kFhzdBeVi0XGE1Id/TFMyseVdie6vSDSUfuZLfAoGAHZsFyCjCVqjSxFfz\n" +
"TGLBrWuu6oxt+zQS7LdV6lVTUF/JlRXS8LTSObU4RxBncbmxqKM3nGPwjdn5r3yoYbo7nUcAj2R5\n" +
"pWE8pQ+gk0UM3yiFZ/t6MLuUVrATnpW6qoj4YasvDd6J4vW643+bxaSA3ng2FWp8XgLVLqZ+7ZAu\n" +
"2oECgYAnGL2/V3FKaEFqUMT9dfcZVYbDJl7dfHLnOjAdyBvbYlJgsqiNsSHB0xlem4SxQIIAI/9l\n" +
"4+JISwJ3qYx8+p8vyNGl7Kon4CVt+m14GhW4gdbhGfzNMPtEVK7eHvwYlME7b9xiQ0PpE9mAybpo\n" +
"sjzDOOaxRUm90R/4sw0yx81R6g==\n" +
"-----END PRIVATE KEY-----\n" +
"-----BEGIN CERTIFICATE-----\n" +
"MIIC0jCCAbqgAwIBAgIIbxi7iN/xWpYwDQYJKoZIhvcNAQELBQAwKTERMA8GA1UECxMIbXR3aWxz\n" +
"b24xFDASBgNVBAMTC210d2lsc29uLWNhMB4XDTE0MDcyNjAyMzIwOVoXDTE1MDcyNjAyMzIwOVow\n" +
"KTERMA8GA1UECxMIbXR3aWxzb24xFDASBgNVBAMTC210d2lsc29uLWNhMIIBIjANBgkqhkiG9w0B\n" +
"AQEFAAOCAQ8AMIIBCgKCAQEAjLkE4ohFR9ugW1z5L2azH0kjCfM5v/KeYpgzXPqzTBHg8eikpBhS\n" +
"eY5D/6/T9Csp7sZY1VAU79WrrjP1FWGm6ndT4noy+4iRgvRkPqzG2tAUgjhKgCgpol4M2sjEsbW0\n" +
"HqQKDJni2VPDdjT3rcBpaeaANpWeYBRzAeoNzZKjJ5iNjnWX4MJ5VmxGDv6dBq/uHps2/i8WgVmt\n" +
"h2PlE3/wajfKg2OLZ9bf6LCV/RV9Hp7jToWwv/Pzlv/8jd6fd1NG5M7pD+ZKXht3epZ56FOjandc\n" +
"7t41VIofsJHNda1lZub7/vuwd7GZzoYuT+n3FwzctITUA3G+MIObyzS8T8iL5wIDAQABMA0GCSqG\n" +
"SIb3DQEBCwUAA4IBAQA52z9Pzv4ktQm1cbagCGWBcUacGUO6hd79dTMmXV7QjglE7xuL7FauPirH\n" +
"nrs1reDdzARPYFPlEXZql7kg+9JV9cYIsAl97LVn+K1QXAoTx/NbYk0KOAsD5k/Q1WA1yPbwxhPS\n" +
"yzmWygrfInPdQt31uaxeppa9sQEwBpGiH+13/bOH/2MfGayeviUB0Rksglg2qdyOauWcEcWTd/h2\n" +
"jeDYohoXcHcxJ3CY/Md8l5IuSR/yckw+j+Dl98pk0ZD7M7lU2wRWDqxwsZSYycyVcfTmNhjRE1Rw\n" +
"R/mHVXSOLb1IPPFiy9fiL2LajybjWt3MOt5E9iE6yR/vpq2HfDvup/3u\n" +
"-----END CERTIFICATE-----";
        PrivateKey pk = decodePemPrivateKey(pem);
//        List<X509Certificate> certs2 = X509Util.decodePemCertificates(pem2);
        log.debug("Private Key: {}", pk.toString());
    }
}
