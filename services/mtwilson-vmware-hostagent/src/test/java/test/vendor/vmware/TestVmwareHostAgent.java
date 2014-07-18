/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.vendor.vmware;

import com.intel.mtwilson.My;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.crypto.Aes128;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.mtwilson.util.DataCipher;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.model.PcrEventLog;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.mtwilson.model.Sha1Digest;
import com.intel.mtwilson.util.ASDataCipher;
import com.intel.mtwilson.util.Aes128DataCipher;
import java.io.IOException;
import java.net.MalformedURLException;
import org.apache.commons.codec.binary.Base64;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In order to run these tests, you need aikqverify set up on your machine. The
 * following documentation is copied from TAHelper:
 *
 * In order to use the TAHelper, you need to have attestation-service.properties
 * on your machine.
 *
 * Here are example properties that Jonathan has at
 * C:/Intel/CloudSecurity/attestation-service.properties:
 * * 
com.intel.mountwilson.as.home=C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome
 * com.intel.mountwilson.as.aikqverify.cmd=aikqverify.exe
 * com.intel.mountwilson.as.openssl.cmd=openssl.bat
 *
 * The corresponding files must exist. From the above example:
 *
 * C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome
 * C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome/data (can be
 * empty, TAHelper will save files there)
 * C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome/bin contains:
 * aikqverify.exe, cygwin1.dll
 *
 *
 * @author jbuhacoff
 */
public class TestVmwareHostAgent {

    private transient Logger log = LoggerFactory.getLogger(getClass());
    private static String hostname = "10.1.71.175";
    private static String connection = "vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;10.1.71.175";
//    private static String hostname = "10.1.71.155";
//    private static String connection = "vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
    private static HostAgent agent;

    @BeforeClass
    public static void createHostAgent() throws IOException, CryptographyException {
        agent = getAgent();
    }

    public static HostAgent getAgent() throws IOException, CryptographyException {
        ASDataCipher.cipher = new Aes128DataCipher(new Aes128(Base64.decodeBase64(My.configuration().getDataEncryptionKeyBase64())));
//        TblHosts.dataCipher = new DummyCipher();
        TblHosts host = new TblHosts();
        host.setName(hostname);
        host.setTlsPolicyName("INSECURE");
        host.setTlsKeystore(null);
        host.setAddOnConnectionInfo(connection);
        HostAgentFactory factory = new HostAgentFactory();
        HostAgent hostAgent = factory.getHostAgent(host);
        return hostAgent;
    }

    public static class DummyCipher implements DataCipher {

        @Override
        public String encryptString(String plaintext) {
            return plaintext;
        }

        @Override
        public String decryptString(String ciphertext) {
            return ciphertext;
        }
    }

    @Test
    public void testGetPcrManifestFrom175() throws MalformedURLException, CryptographyException, IOException {
        /*
         TlsPolicy tlsPolicy = new TrustFirstCertificateTlsPolicy(new );
         ConnectionString conn = new ConnectionString("vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;10.1.71.175");
         HostAgentFactory factory = new HostAgentFactory();
         HostAgent hostAgent = factory.getHostAgent(conn, tlsPolicy);        
         */
        HostAgent agent = getAgent();
        PcrManifest pcrManifest = agent.getPcrManifest();
        for (int i = 0; i < 24; i++) {
            PcrEventLog pcrEventLog = pcrManifest.getPcrEventLog(i);
            if (pcrEventLog == null) {
                log.debug("no event log for pcr {}", i);
            }
        }
    }

    /**
     * Example quote request sent to trust agent:
     *
     * <?xml version="1.0" encoding="UTF-8"
     * standalone="yes"?><quote_request><nonce>KDk7urTjoZHh3kqJd5wAGQ==</nonce><pcr_list>0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23</pcr_list></quote_request>
     *
     * Example response from trust agent:
     *
     * <client_request> <timestamp>Tue Apr 09 12:46:18 PDT
     * 2013</timestamp><clientIp>fe80:0:0:0:21e:67ff:fe10:4460%4</clientIp><error_code>0</error_code><error_message>OK</error_message><aikcert>-----BEGIN
     * CERTIFICATE-----MIICuzCCAaOgAwIBAgIGAT3v+sHdMA0GCSqGSIb3DQEBBQUAMBkxFzAVBgNVBAMMDkhJU19Qcml2
     * YWN5X0NBMB4XDTEzMDQwOTE4MDcwMVoXDTIzMDQwOTE4MDcwMVowADCCASIwDQYJKoZIhvcNAQEB
     * BQADggEPADCCAQoCggEBAMDta0nKMmwUAWReFPbNhDiw2NOOTuLj/zzuS8Q4Yqi/JF14dlLcYYnv
     * xm0gaNTQEipzr7C9H7FoX8QnuMktx1rnH0dknXKwwMetvWjoHVm3miA8R8SHDMhhso09v6FHBimg
     * SdhKpTUK3FNcp/gyw9es4dl/akG2namb501WMK8T07Rp9USPRBKrhUXwRSh8lrNKgZUf6Jtg8ZHC
     * g5VHtiVCXKoYD3yrpWhfWq08jyZq/EhiO3AXF++euAlc/7WEU2Q7R3YWQUC2tBZ9Chg8tzdi+ekn
     * BI7BpW0EdKmoVb0JrkujQaJH/H6BSLESRfNsbLSp2qc3ueAcaOeOiIVayoUCAwEAAaMiMCAwHgYD
     * VR0RAQH/BBQwEoEQSElTIElkZW50aXR5IEtleTANBgkqhkiG9w0BAQUFAAOCAQEABbgddQMo2u97
     * iEEvmPBgjjpLVz55n1keXyGk/wXEv3LcO9z60iwX2kpcetP+dtxSUS0UgK5FH0toqWo7UkW7T8I0
     * chpMRi8BNAbMKZw3d4i8rCFrzd/Sx6WRMOehBQU/qUYzAKIrTE1RsbJRWY6K58SiLizEx8RItURZ
     * DVkgxpYCeJqxJpmAxsmTfMKaNoCCB75ytaKu96/45t1Gp2ZUifv3uEnTwcRr+IkGS0OiJrdEZrjl
     * G4He9p69/MomZ+Ob8xqsiElfEETXdpEgzxrlSF8raBLcG0xbdnTY9JnWpsnP059FwUj5HYaFXgfG
     * vNaANw/v5hQcRpGMyp6AV1F6mw== -----END
     * CERTIFICATE-----</aikcert><quote>AAP///8AAAHgiR6wtVa4P87xwQ8/pkZDReNPj5E6P3gPEaS0mWn8qoDNbjlXwzsida6OANejVsuKNFboNmiEafsEaZZlOj94DxGktJlp/KqAzW45V8M7InX3aBjNGB2oWBGT96FRj1v8qqharzPG6NpbnV+zrvzxRzorReAZtHrqOj94DxGktJlp/KqAzW45V8M7InU6P3gPEaS0mWn8qoDNbjlXwzsidQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAL/D/9eUDpKBo+v9+k4EEoaaP1XYoUxrVzXb37vZJpJZR+o9opgnOf3bfxUwS43Vi2n+PT3NbeytJK1VEQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAcqTJzLVhtHj0cUkhNFYFOsBwnVXWZMAB7Fw7EXlIWkzwBNJyq7ES4b5AdTwPLU3wBFVXODY7zG1WvaHyD0SBKHM37dh2+0brl/6EFGSG1a9QbiWpMsmHQIJ6TO6n3Gm8WeQ/fVvtlq+YjMvkbxPgpyGAfJ9KRkZdVTdNLAqZPXMaoQaFtqdIJJ46D6Xjsics88q6NoRP0ZQ2dVwFOr8rId8+xQf9S74moh+YtpDGDRSMApuKTBid53OOHXqZFn9X9FQz9Cy+OFhQiQZQohsIBE3v9SxPFDgUcteY9CP83G/yVXN6fRUmCIT1jiwSR4FxDd7k7EDGFw53usim4sCB4Q==</quote></client_request>
     *
     * So here is the quote extracted from the above response:
     * AAP///8AAAHgiR6wtVa4P87xwQ8/pkZDReNPj5E6P3gPEaS0mWn8qoDNbjlXwzsida6OANejVsuKNFboNmiEafsEaZZlOj94DxGktJlp/KqAzW45V8M7InX3aBjNGB2oWBGT96FRj1v8qqharzPG6NpbnV+zrvzxRzorReAZtHrqOj94DxGktJlp/KqAzW45V8M7InU6P3gPEaS0mWn8qoDNbjlXwzsidQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAL/D/9eUDpKBo+v9+k4EEoaaP1XYoUxrVzXb37vZJpJZR+o9opgnOf3bfxUwS43Vi2n+PT3NbeytJK1VEQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAcqTJzLVhtHj0cUkhNFYFOsBwnVXWZMAB7Fw7EXlIWkzwBNJyq7ES4b5AdTwPLU3wBFVXODY7zG1WvaHyD0SBKHM37dh2+0brl/6EFGSG1a9QbiWpMsmHQIJ6TO6n3Gm8WeQ/fVvtlq+YjMvkbxPgpyGAfJ9KRkZdVTdNLAqZPXMaoQaFtqdIJJ46D6Xjsics88q6NoRP0ZQ2dVwFOr8rId8+xQf9S74moh+YtpDGDRSMApuKTBid53OOHXqZFn9X9FQz9Cy+OFhQiQZQohsIBE3v9SxPFDgUcteY9CP83G/yVXN6fRUmCIT1jiwSR4FxDd7k7EDGFw53usim4sCB4Q==
     *
     * And sample output from this method:
     *     * 
Pcr 0 = 891eb0b556b83fcef1c10f3fa6464345e34f8f91 Pcr 1 =
     * 3a3f780f11a4b49969fcaa80cd6e3957c33b2275 Pcr 2 =
     * ae8e00d7a356cb8a3456e836688469fb04699665 Pcr 3 =
     * 3a3f780f11a4b49969fcaa80cd6e3957c33b2275 Pcr 4 =
     * f76818cd181da8581193f7a1518f5bfcaaa85aaf Pcr 5 =
     * 33c6e8da5b9d5fb3aefcf1473a2b45e019b47aea Pcr 6 =
     * 3a3f780f11a4b49969fcaa80cd6e3957c33b2275 Pcr 7 =
     * 3a3f780f11a4b49969fcaa80cd6e3957c33b2275 Pcr 8 =
     * 0000000000000000000000000000000000000000 Pcr 9 =
     * 0000000000000000000000000000000000000000 Pcr 10 =
     * 0000000000000000000000000000000000000000 Pcr 11 =
     * 0000000000000000000000000000000000000000 Pcr 12 =
     * 0000000000000000000000000000000000000000 Pcr 13 =
     * 0000000000000000000000000000000000000000 Pcr 14 =
     * 0000000000000000000000000000000000000000 Pcr 15 =
     * 0000000000000000000000000000000000000000 Pcr 16 =
     * 0000000000000000000000000000000000000000 Pcr 17 =
     * bfc3ffd7940e9281a3ebfdfa4e0412869a3f55d8 Pcr 18 =
     * a14c6b5735dbdfbbd926925947ea3da2982739fd Pcr 19 =
     * db7f15304b8dd58b69fe3d3dcd6decad24ad5511 Pcr 20 =
     * 0000000000000000000000000000000000000000 Pcr 21 =
     * 0000000000000000000000000000000000000000 Pcr 22 =
     * 0000000000000000000000000000000000000000 Pcr 23 =
     * 0000000000000000000000000000000000000000
     *
     *
     * @throws IOException
     */
    @Test
    public void getPcrManifestFromVmware() throws IOException {
        PcrManifest pcrManifest = agent.getPcrManifest();
        assertNotNull(pcrManifest);
        for (int i = 0; i < 24; i++) {
            Pcr pcr = pcrManifest.getPcr(i);
            log.debug("Pcr {} = {}", i, pcr.getValue().toString());
        }
    }

    /**
     * Output: Event name: /b.b00 vmbTrustedBoot=true tboot=0x0x101a000
     * no-auto-partition bootUUID=772753050c0a140bdfbf92e306b9793d Digest:
     * 2213253cbdf7a0d8b3709e971e6beb7ea6a81a92 This matches what VMware sends
     * in its HostTpmCommandEventDetails object: 2013-04-10 20:48:54,181 DEBUG
     * [main] c.i.m.a.v.VMWare51Esxi51 [VMWare51Esxi51.java:68] Event
     * com.vmware.vim25.HostTpmCommandEventDetails 2013-04-10 20:48:54,182 DEBUG
     * [main] c.i.m.a.v.VMWare51Esxi51 [VMWare51Esxi51.java:81] Event name
     * /b.b00 vmbTrustedBoot=true tboot=0x0x101a000 no-auto-partition
     * bootUUID=772753050c0a140bdfbf92e306b9793d 2013-04-10 20:48:54,184 DEBUG
     * [main] c.i.m.a.v.VMWare51Esxi51 [VMWare51Esxi51.java:116] Event Digest is
     * 2213253CBDF7A0D8B3709E971E6BEB7EA6A81A92
     *
     */
    @Test
    public void testTypicalCommandEventDigest() {
        String eventName = "/b.b00 vmbTrustedBoot=true tboot=0x0x101a000 no-auto-partition bootUUID=772753050c0a140bdfbf92e306b9793d";
        System.out.println("Event name: " + eventName);
        System.out.println("Digest: " + Sha1Digest.valueOf(eventName.getBytes()).toString());
    }

    /**
     * Example output:
     *     * 
BIOS Name: null BIOS Version: S5500.86B.01.00.0060.090920111354 BIOS OEM:
     * Intel Corporation VMM Name: null VMM Version: 799733 OS Name: VMware ESXi
     * OS Version: 5.1.0 AIK Certificate: null
     *
     * @throws IOException
     */
    @Test
    public void getHostInformationFromVmware() throws IOException {
        TxtHostRecord hostDetails = agent.getHostDetails();
        log.debug("BIOS Name: {}", hostDetails.BIOS_Name);
        log.debug("BIOS Version: {}", hostDetails.BIOS_Version);
        log.debug("BIOS OEM: {}", hostDetails.BIOS_Oem);
        log.debug("VMM Name: {}", hostDetails.VMM_Name);
        log.debug("VMM Version: {}", hostDetails.VMM_Version);
        log.debug("OS Name: {}", hostDetails.VMM_OSName);
        log.debug("OS Version: {}", hostDetails.VMM_OSVersion);
        log.debug("AIK Certificate: {}", hostDetails.AIK_Certificate);
        log.debug("Processor Info: {}", hostDetails.Processor_Info);
    }
}
