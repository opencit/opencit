/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.policy;

//import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intel.mtwilson.My;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.as.data.*;
import com.intel.mtwilson.util.DataCipher;
import com.intel.mtwilson.util.Aes128DataCipher;
import com.intel.mtwilson.crypto.Aes128;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.datatypes.HostTrustStatus;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.model.*;
import com.intel.mtwilson.policy.Policy;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.TrustReport;
import com.intel.mtwilson.policy.impl.HostTrustPolicyManager;
import com.intel.mtwilson.policy.impl.TrustMarker;
import com.intel.mtwilson.policy.rule.PcrMatchesConstant;
import com.intel.mtwilson.util.ASDataCipher;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.crypto.RsaUtil;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.codec.binary.Base64;

/**
 * The tests in this class check the functions of the business and data layer directly,
 * not through the API.  
 * 
 * In order to run these tests with Linux hosts you need to have aikqverify setup on your
 * developer machine just as it is on the server.  Here is documentation copied from TAHelper:
 * 
 * Here are example properties that Jonathan has at C:/Intel/CloudSecurity/attestation-service.properties:
 * 
com.intel.mountwilson.as.home=C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome
com.intel.mountwilson.as.aikqverify.cmd=aikqverify.exe
com.intel.mountwilson.as.openssl.cmd=openssl.bat
 * 
 * The corresponding files must exist. From the above example:
 * 
 *    C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome
 *    C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome/data   (can be empty, TAHelper will save files there)
 *    C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome/bin
 *         contains:  aikqverify.exe, cygwin1.dll, openssl.bat
 * 
 * 
 * @author jbuhacoff
 */
public class TestCitrixXen126 {

    private transient Logger log = LoggerFactory.getLogger(getClass());
    private transient static com.fasterxml.jackson.databind.ObjectWriter json = new ObjectMapper().writerWithDefaultPrettyPrinter();
    private transient static com.fasterxml.jackson.databind.ObjectWriter xml = new XmlMapper().writerWithDefaultPrettyPrinter(); 
    private transient String hostname = "10.1.70.126";
    private transient String connection = "citrix:https://10.1.70.126:443;root;P@ssw0rd";
    
    @BeforeClass
    public static void setMwHostsDek() throws CryptographyException, IOException {
        ASDataCipher.cipher = new Aes128DataCipher(new Aes128(Base64.decodeBase64(My.configuration().getDataEncryptionKeyBase64())));
    }
    
    /**
     * 
     * Example quote request sent to trust agent:
     * 
     * <?xml version="1.0" encoding="UTF-8" standalone="yes"?><quote_request><nonce>KDk7urTjoZHh3kqJd5wAGQ==</nonce><pcr_list>0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23</pcr_list></quote_request>
     * 
     * Example response from trust agent:
     * 
     * <client_request> <timestamp>Tue Apr 09 12:46:18 PDT 2013</timestamp><clientIp>fe80:0:0:0:21e:67ff:fe10:4460%4</clientIp><error_code>0</error_code><error_message>OK</error_message><aikcert>-----BEGIN CERTIFICATE-----MIICuzCCAaOgAwIBAgIGAT3v+sHdMA0GCSqGSIb3DQEBBQUAMBkxFzAVBgNVBAMMDkhJU19Qcml2
  YWN5X0NBMB4XDTEzMDQwOTE4MDcwMVoXDTIzMDQwOTE4MDcwMVowADCCASIwDQYJKoZIhvcNAQEB
  BQADggEPADCCAQoCggEBAMDta0nKMmwUAWReFPbNhDiw2NOOTuLj/zzuS8Q4Yqi/JF14dlLcYYnv
  xm0gaNTQEipzr7C9H7FoX8QnuMktx1rnH0dknXKwwMetvWjoHVm3miA8R8SHDMhhso09v6FHBimg
  SdhKpTUK3FNcp/gyw9es4dl/akG2namb501WMK8T07Rp9USPRBKrhUXwRSh8lrNKgZUf6Jtg8ZHC
  g5VHtiVCXKoYD3yrpWhfWq08jyZq/EhiO3AXF++euAlc/7WEU2Q7R3YWQUC2tBZ9Chg8tzdi+ekn
  BI7BpW0EdKmoVb0JrkujQaJH/H6BSLESRfNsbLSp2qc3ueAcaOeOiIVayoUCAwEAAaMiMCAwHgYD
  VR0RAQH/BBQwEoEQSElTIElkZW50aXR5IEtleTANBgkqhkiG9w0BAQUFAAOCAQEABbgddQMo2u97
  iEEvmPBgjjpLVz55n1keXyGk/wXEv3LcO9z60iwX2kpcetP+dtxSUS0UgK5FH0toqWo7UkW7T8I0
  chpMRi8BNAbMKZw3d4i8rCFrzd/Sx6WRMOehBQU/qUYzAKIrTE1RsbJRWY6K58SiLizEx8RItURZ
  DVkgxpYCeJqxJpmAxsmTfMKaNoCCB75ytaKu96/45t1Gp2ZUifv3uEnTwcRr+IkGS0OiJrdEZrjl
  G4He9p69/MomZ+Ob8xqsiElfEETXdpEgzxrlSF8raBLcG0xbdnTY9JnWpsnP059FwUj5HYaFXgfG
  vNaANw/v5hQcRpGMyp6AV1F6mw==
  -----END CERTIFICATE-----</aikcert><quote>AAP///8AAAHgiR6wtVa4P87xwQ8/pkZDReNPj5E6P3gPEaS0mWn8qoDNbjlXwzsida6OANejVsuKNFboNmiEafsEaZZlOj94DxGktJlp/KqAzW45V8M7InX3aBjNGB2oWBGT96FRj1v8qqharzPG6NpbnV+zrvzxRzorReAZtHrqOj94DxGktJlp/KqAzW45V8M7InU6P3gPEaS0mWn8qoDNbjlXwzsidQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAL/D/9eUDpKBo+v9+k4EEoaaP1XYoUxrVzXb37vZJpJZR+o9opgnOf3bfxUwS43Vi2n+PT3NbeytJK1VEQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAcqTJzLVhtHj0cUkhNFYFOsBwnVXWZMAB7Fw7EXlIWkzwBNJyq7ES4b5AdTwPLU3wBFVXODY7zG1WvaHyD0SBKHM37dh2+0brl/6EFGSG1a9QbiWpMsmHQIJ6TO6n3Gm8WeQ/fVvtlq+YjMvkbxPgpyGAfJ9KRkZdVTdNLAqZPXMaoQaFtqdIJJ46D6Xjsics88q6NoRP0ZQ2dVwFOr8rId8+xQf9S74moh+YtpDGDRSMApuKTBid53OOHXqZFn9X9FQz9Cy+OFhQiQZQohsIBE3v9SxPFDgUcteY9CP83G/yVXN6fRUmCIT1jiwSR4FxDd7k7EDGFw53usim4sCB4Q==</quote></client_request>
     * 
     * So here is the quote extracted from the above response:
     * AAP///8AAAHgiR6wtVa4P87xwQ8/pkZDReNPj5E6P3gPEaS0mWn8qoDNbjlXwzsida6OANejVsuKNFboNmiEafsEaZZlOj94DxGktJlp/KqAzW45V8M7InX3aBjNGB2oWBGT96FRj1v8qqharzPG6NpbnV+zrvzxRzorReAZtHrqOj94DxGktJlp/KqAzW45V8M7InU6P3gPEaS0mWn8qoDNbjlXwzsidQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAL/D/9eUDpKBo+v9+k4EEoaaP1XYoUxrVzXb37vZJpJZR+o9opgnOf3bfxUwS43Vi2n+PT3NbeytJK1VEQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAcqTJzLVhtHj0cUkhNFYFOsBwnVXWZMAB7Fw7EXlIWkzwBNJyq7ES4b5AdTwPLU3wBFVXODY7zG1WvaHyD0SBKHM37dh2+0brl/6EFGSG1a9QbiWpMsmHQIJ6TO6n3Gm8WeQ/fVvtlq+YjMvkbxPgpyGAfJ9KRkZdVTdNLAqZPXMaoQaFtqdIJJ46D6Xjsics88q6NoRP0ZQ2dVwFOr8rId8+xQf9S74moh+YtpDGDRSMApuKTBid53OOHXqZFn9X9FQz9Cy+OFhQiQZQohsIBE3v9SxPFDgUcteY9CP83G/yVXN6fRUmCIT1jiwSR4FxDd7k7EDGFw53usim4sCB4Q==
     * 
     * And sample output from this method:
     * 
Pcr 0 = 891eb0b556b83fcef1c10f3fa6464345e34f8f91
Pcr 1 = 3a3f780f11a4b49969fcaa80cd6e3957c33b2275
Pcr 2 = ae8e00d7a356cb8a3456e836688469fb04699665
Pcr 3 = 3a3f780f11a4b49969fcaa80cd6e3957c33b2275
Pcr 4 = f76818cd181da8581193f7a1518f5bfcaaa85aaf
Pcr 5 = 33c6e8da5b9d5fb3aefcf1473a2b45e019b47aea
Pcr 6 = 3a3f780f11a4b49969fcaa80cd6e3957c33b2275
Pcr 7 = 3a3f780f11a4b49969fcaa80cd6e3957c33b2275
Pcr 8 = 0000000000000000000000000000000000000000
Pcr 9 = 0000000000000000000000000000000000000000
Pcr 10 = 0000000000000000000000000000000000000000
Pcr 11 = 0000000000000000000000000000000000000000
Pcr 12 = 0000000000000000000000000000000000000000
Pcr 13 = 0000000000000000000000000000000000000000
Pcr 14 = 0000000000000000000000000000000000000000
Pcr 15 = 0000000000000000000000000000000000000000
Pcr 16 = 0000000000000000000000000000000000000000
Pcr 17 = bfc3ffd7940e9281a3ebfdfa4e0412869a3f55d8
Pcr 18 = a14c6b5735dbdfbbd926925947ea3da2982739fd
Pcr 19 = db7f15304b8dd58b69fe3d3dcd6decad24ad5511
Pcr 20 = 0000000000000000000000000000000000000000
Pcr 21 = 0000000000000000000000000000000000000000
Pcr 22 = 0000000000000000000000000000000000000000
Pcr 23 = 0000000000000000000000000000000000000000
     * 
     * 
     * @throws Exception 
     * 
     */
    @Test
    public void testHostAgentPcrManifest() throws Exception {
        TblHosts host = initNewHost();
        HostAgentFactory factory = new HostAgentFactory();
        HostAgent agent = factory.getHostAgent(host);
        PcrManifest pcrManifest = agent.getPcrManifest();
        assertNotNull(pcrManifest);
        for(int i=0; i<24; i++) {
            Pcr pcr = pcrManifest.getPcr(i);
            log.debug("Pcr {} = {}", i, pcr.getValue().toString());
        }
    }
    
    private TblHosts initNewHost() {
        TblHosts host = new TblHosts();
        host.setName(hostname);
        host.setTlsPolicyName("INSECURE");
        host.setTlsKeystore(null);
        host.setAddOnConnectionInfo(connection); // XXX notice we do not set the IPAddress or Port in the database... don't need to because now we are setting ConnectionString which includes them both and is passed to the host agent and the host agent extracts ip address and port from this string.
        return host;
    }
    
    /**
     * A lot of the code in this method is similar to what you find in TestVmwareEsxi51
     * @throws Exception 
     */
    @Test
    public void testRegisterXenHostAndWhitelist() throws Exception {
        // first, if it's already registered we need to delete it
        TblHosts host = My.jpa().mwHosts().findByName(hostname);
        if( host != null ) {
            log.debug("Host {} is already in database, deleting", host.getName());
            // must first delete any saml assertions that are recorded for this host
            List<TblSamlAssertion> samls = My.jpa().mwSamlAssertion().findByHostID(host);
            for(TblSamlAssertion saml : samls) {
                My.jpa().mwSamlAssertion().destroy(saml.getId());
            }
            My.jpa().mwHosts().destroy(host.getId());
        }
        host = initNewHost();
        // now go to the host and fetch the PCR values -- this is similar to what management service does 
        HostAgentFactory factory = new HostAgentFactory();
        HostAgent agent = factory.getHostAgent(host);
        TxtHostRecord hostInfo = agent.getHostDetails();
        // whitelist step 1:  create OEM
        TblOem oem = My.jpa().mwOem().findTblOemByName(hostInfo.BIOS_Oem);
        if( oem == null ) {
            oem = new TblOem();
            oem.setName(hostInfo.BIOS_Oem);
            oem.setDescription("Automatic whitelist from "+hostname);
            My.jpa().mwOem().create(oem);
        }
        // whitelist step 2:  create BIOS MLE
        TblMle bios = My.jpa().mwMle().findBiosMle(hostInfo.BIOS_Name, hostInfo.BIOS_Version, hostInfo.BIOS_Oem);
        if( bios == null ) {
            bios = new TblMle();
            bios.setAttestationType("PCR");
            bios.setDescription("Automatic whitelist from "+hostname);
            bios.setMLEType("BIOS");
            bios.setName(hostInfo.BIOS_Name);
            bios.setVersion(hostInfo.BIOS_Version);
            bios.setOemId(oem);
            bios.setRequiredManifestList("0,17"); // XXX TODO the required manifest list should actually come from EITHER 1) the vendor agent, because it knows exactly what that vendor does during boot, or 2) the UI, because the user might want specific things...  or a combination of providing UI defaults from the vendor, then allowing the UI to override... eitehr way,  right now these are hard-coded not only in this test class but also in the application, and that needs to change.
            My.jpa().mwMle().create(bios);
        }
        // whitelist step 3:  create OS
        TblOs os = My.jpa().mwOs().findTblOsByNameVersion(hostInfo.VMM_OSName, hostInfo.VMM_OSVersion);
        if( os == null ) {
            os = new TblOs();
            os.setName(hostInfo.VMM_OSName);
            os.setVersion(hostInfo.VMM_OSVersion);
            os.setDescription("Automatic whitelist from "+hostname);
            My.jpa().mwOs().create(os);
        }
        // whitelist step 4:  create VMM MLE
        TblMle vmm = My.jpa().mwMle().findVmmMle(hostInfo.VMM_Name, hostInfo.VMM_Version, hostInfo.VMM_OSName, hostInfo.VMM_OSVersion);
        if( vmm == null ) {
            vmm = new TblMle();
            vmm.setAttestationType("PCR");
            vmm.setDescription("Automatic whitelist from "+hostname);
            vmm.setMLEType("VMM");
            vmm.setName(hostInfo.VMM_Name);
            vmm.setVersion(hostInfo.VMM_Version);
            vmm.setOsId(os);
            vmm.setRequiredManifestList("18,19"); // XXX TODO the required manifest list should actually come from EITHER 1) the vendor agent, because it knows exactly what that vendor does during boot, or 2) the UI, because the user might want specific things...  or a combination of providing UI defaults from the vendor, then allowing the UI to override... eitehr way,  right now these are hard-coded not only in this test class but also in the application, and that needs to change.
            My.jpa().mwMle().create(vmm);
        }
        // whitelist step 5: get PCRs
        PcrManifest pcrManifest = agent.getPcrManifest();        
        // whitelist step 6: create whitelist entries for BIOS PCRs
        String[] biosPcrList = bios.getRequiredManifestList().split(",");
        for(String biosPcrIndex : biosPcrList) {
            Pcr pcr = pcrManifest.getPcr(Integer.valueOf(biosPcrIndex));
            log.debug("Adding BIOS PCR {} = {}", pcr.getIndex().toString(), pcr.getValue().toString());
            TblPcrManifest pcrWhitelist = new TblPcrManifest();
            pcrWhitelist.setMleId(bios);
            pcrWhitelist.setName(pcr.getIndex().toString());
            pcrWhitelist.setValue(pcr.getValue().toString());
            pcrWhitelist.setPCRDescription("Automatic BIOS whitelist from "+hostname);
            My.jpa().mwPcrManifest().create(pcrWhitelist);
        }
        // whitelist step 7: create whitelist entries for VMM PCRs
        String[] vmmPcrList = vmm.getRequiredManifestList().split(",");
        for(String vmmPcrIndex : vmmPcrList) {
            Pcr pcr = pcrManifest.getPcr(Integer.valueOf(vmmPcrIndex));
            log.debug("Adding VMM PCR {} = {}", pcr.getIndex().toString(), pcr.getValue().toString());
            TblPcrManifest pcrWhitelist = new TblPcrManifest();
            pcrWhitelist.setMleId(vmm);
            pcrWhitelist.setName(pcr.getIndex().toString());
            pcrWhitelist.setValue(pcr.getValue().toString());
            pcrWhitelist.setPCRDescription("Automatic VMM whitelist from "+hostname);
            My.jpa().mwPcrManifest().create(pcrWhitelist);
        }
        // whitelist step 8: document that these mle's came from this host (not necessary for attestation, but to make this example complete)
        MwMleSource biosMleSource = My.jpa().mwMleSource().findByMleId(bios.getId());
        if( biosMleSource == null ) {
            biosMleSource = new MwMleSource();
            biosMleSource.setMleId(bios);
            biosMleSource.setHostName(hostname);
            My.jpa().mwMleSource().create(biosMleSource);
        }
        MwMleSource vmmMleSource = My.jpa().mwMleSource().findByMleId(vmm.getId());
        if( vmmMleSource == null ) {
            vmmMleSource = new MwMleSource();
            vmmMleSource.setMleId(vmm);
            vmmMleSource.setHostName(hostname);
            My.jpa().mwMleSource().create(vmmMleSource);
        }
        // aik certificate
        if( agent.isAikAvailable() ) {
            if( agent.isAikCaAvailable() ) {
                X509Certificate aikcert = agent.getAikCertificate();
                host.setAIKCertificate(X509Util.encodePemCertificate(aikcert));
                host.setAikPublicKey(RsaUtil.encodePemPublicKey(aikcert.getPublicKey()));
                host.setAikSha1(Sha1Digest.valueOf(aikcert.getPublicKey().getEncoded()).toString());
            }
            else {
                PublicKey aikpubkey = agent.getAik();
                host.setAIKCertificate(null);
                host.setAikPublicKey(RsaUtil.encodePemPublicKey(aikpubkey));
                host.setAikSha1(Sha1Digest.valueOf(aikpubkey.getEncoded()).toString());
            }
        }
        // register host
        host.setBiosMleId(bios);
        host.setVmmMleId(vmm);
        
        My.jpa().mwHosts().create(host);
    }
    
    
    /**
     * You need to run testRegisterHostAndWhitelist() first in order to create the host record and the whitelist
     * Example policy:
     * 
{
  "name" : "Host trust policy for host with AIK 10.1.71.169",
  "rules" : [ {
    "markers" : [ "BIOS" ],
    "expectedPcr" : {
      "value" : "891eb0b556b83fcef1c10f3fa6464345e34f8f91",
      "index" : "0"
    }
  }, {
    "markers" : [ "VMM" ],
    "expectedPcr" : {
      "value" : "bfc3ffd7940e9281a3ebfdfa4e0412869a3f55d8",
      "index" : "17"
    }
  }, {
    "markers" : [ "VMM" ],
    "expectedPcr" : {
      "value" : "a14c6b5735dbdfbbd926925947ea3da2982739fd",
      "index" : "18"
    }
  }, {
    "markers" : [ "VMM" ],
    "expectedPcr" : {
      "value" : "db7f15304b8dd58b69fe3d3dcd6decad24ad5511",
      "index" : "19"
    }
  } ]
}     * 
     * 
     * @throws Exception 
     */
    @Test
    public void loadTrustPolicyForHost() throws Exception {
        TblHosts host = My.jpa().mwHosts().findByName(hostname);
        assertNotNull(host); 
        HostTrustPolicyManager hostTrustPolicyFactory = new HostTrustPolicyManager(My.persistenceManager().getASData());
        Policy trustPolicy = hostTrustPolicyFactory.loadTrustPolicyForHost(host, hostname); // must include both bios and vmm policies
        log.debug(json.writeValueAsString(trustPolicy));
//        log.debug(xml.writeValueAsString(trustPolicy)); // notice the xml is NOT the same as the json at all... doesn't have all the info, and somehow has mixed "faults" in there somewhere even though the Rule objects do not have a fault method or field...
    }
    
    /**
     * You need to run testRegisterHostAndWhitelist() first in order to create the host record and the whitelist
     * 
     * This test method calls "getTrustReportForHost" which does not do any database logging
     * 
     * The following is an example of the report you get if the host exists & has a bios and mle in the whitelist but
     * those bios and mle do not have any associated mw_pcr_manifest records  - therfore nothing to check against the
     * host:
     * 
{
  "policyName" : "Host trust policy for 10.1.71.169",
  "results" : [ {
    "rule" : {
      "markers" : [ "BIOS" ],
      "expectedPcr" : {
        "value" : "891eb0b556b83fcef1c10f3fa6464345e34f8f91",
        "index" : "0"
      }
    },
    "faults" : [ ],
    "trusted" : true,
    "ruleName" : "com.intel.mtwilson.policy.rule.PcrMatchesConstant"
  }, {
    "rule" : {
      "markers" : [ "VMM" ],
      "expectedPcr" : {
        "value" : "bfc3ffd7940e9281a3ebfdfa4e0412869a3f55d8",
        "index" : "17"
      }
    },
    "faults" : [ ],
    "trusted" : true,
    "ruleName" : "com.intel.mtwilson.policy.rule.PcrMatchesConstant"
  }, {
    "rule" : {
      "markers" : [ "VMM" ],
      "expectedPcr" : {
        "value" : "a14c6b5735dbdfbbd926925947ea3da2982739fd",
        "index" : "18"
      }
    },
    "faults" : [ ],
    "trusted" : true,
    "ruleName" : "com.intel.mtwilson.policy.rule.PcrMatchesConstant"
  }, {
    "rule" : {
      "markers" : [ "VMM" ],
      "expectedPcr" : {
        "value" : "db7f15304b8dd58b69fe3d3dcd6decad24ad5511",
        "index" : "19"
      }
    },
    "faults" : [ ],
    "trusted" : true,
    "ruleName" : "com.intel.mtwilson.policy.rule.PcrMatchesConstant"
  } ],
  "trusted" : true
}
     * 
     * 
     * Here's an example of the same host, but where the whitelist has changed a little (pcr 18 for vmm now has 00 at the end)
     * so you can see what it looks like when a rule fails:
     * 
{
  "policyName" : "Host trust policy for 10.1.71.169",
  "results" : [ {
    "rule" : {
      "markers" : [ "BIOS" ],
      "expectedPcr" : {
        "value" : "891eb0b556b83fcef1c10f3fa6464345e34f8f91",
        "index" : "0"
      }
    },
    "faults" : [ ],
    "trusted" : true,
    "ruleName" : "com.intel.mtwilson.policy.rule.PcrMatchesConstant"
  }, {
    "rule" : {
      "markers" : [ "VMM" ],
      "expectedPcr" : {
        "value" : "BFC3FFD7940E9281A3EBFDFA4E0412869A3F55D8",
        "index" : "17"
      }
    },
    "faults" : [ ],
    "trusted" : true,
    "ruleName" : "com.intel.mtwilson.policy.rule.PcrMatchesConstant"
  }, {
    "rule" : {
      "markers" : [ "VMM" ],
      "expectedPcr" : {
        "value" : "A14C6B5735DBDFBBD926925947EA3DA298273900",
        "index" : "18"
      }
    },
    "faults" : [ {
      "cause" : null,
      "pcrIndex" : "18",
      "expectedValue" : "A14C6B5735DBDFBBD926925947EA3DA298273900",
      "actualValue" : "a14c6b5735dbdfbbd926925947ea3da2982739fd",
      "faultName" : "com.intel.mtwilson.policy.fault.PcrValueMismatch"
    } ],
    "trusted" : false,
    "ruleName" : "com.intel.mtwilson.policy.rule.PcrMatchesConstant"
  }, {
    "rule" : {
      "markers" : [ "VMM" ],
      "expectedPcr" : {
        "value" : "DB7F15304B8DD58B69FE3D3DCD6DECAD24AD5511",
        "index" : "19"
      }
    },
    "faults" : [ ],
    "trusted" : true,
    "ruleName" : "com.intel.mtwilson.policy.rule.PcrMatchesConstant"
  } ],
  "trusted" : false
}
     * 
     * 
     * @throws Exception 
     */
    @Test
    public void checkTrustReportForXen() throws Exception {
        TblHosts host = My.jpa().mwHosts().findByName(hostname);
        assertNotNull(host); 
        HostTrustBO hostTrustBO = new HostTrustBO();
        TrustReport trustReport = hostTrustBO.getTrustReportForHost(host, hostname);        
        log.debug(json.writeValueAsString(trustReport));
//        log.debug(xml.writeValueAsString(trustReport)); // xml doesn't seem to seriailze the same info somehow... 
        
        
        HostTrustStatus trust = new HostTrustStatus();
        trust.bios = trustReport.isTrustedForMarker(TrustMarker.BIOS.name());
        trust.vmm = trustReport.isTrustedForMarker(TrustMarker.VMM.name());
        trust.location = trustReport.isTrustedForMarker(TrustMarker.LOCATION.name());


        
        log.debug("Summary of trust status:  bios({}), vmm({}), location({})", new Object[] { trust.bios, trust.vmm, trust.location });
        
        
        /**
         * Example output for the next block:
         * 
2013-04-09 23:09:19,986 DEBUG [main] t.p.TestLinuxXen169 [TestLinuxXen169.java:520] There are 4 reports with trusted PcrMatchesConstant
2013-04-09 23:09:19,986 DEBUG [main] t.p.TestLinuxXen169 [TestLinuxXen169.java:522] PCR Report com.intel.mtwilson.policy.PcrMatchesConstant trusted? true
2013-04-09 23:09:19,987 DEBUG [main] t.p.TestLinuxXen169 [TestLinuxXen169.java:523] ---> {
  "expectedPcr" : {
    "value" : "891eb0b556b83fcef1c10f3fa6464345e34f8f91",
    "index" : "0"
  }
}
2013-04-09 23:09:19,987 DEBUG [main] t.p.TestLinuxXen169 [TestLinuxXen169.java:522] PCR Report com.intel.mtwilson.policy.PcrMatchesConstant trusted? true
2013-04-09 23:09:19,987 DEBUG [main] t.p.TestLinuxXen169 [TestLinuxXen169.java:523] ---> {
  "expectedPcr" : {
    "value" : "bfc3ffd7940e9281a3ebfdfa4e0412869a3f55d8",
    "index" : "17"
  }
}
2013-04-09 23:09:19,987 DEBUG [main] t.p.TestLinuxXen169 [TestLinuxXen169.java:522] PCR Report com.intel.mtwilson.policy.PcrMatchesConstant trusted? true
2013-04-09 23:09:19,988 DEBUG [main] t.p.TestLinuxXen169 [TestLinuxXen169.java:523] ---> {
  "expectedPcr" : {
    "value" : "a14c6b5735dbdfbbd926925947ea3da2982739fd",
    "index" : "18"
  }
}
2013-04-09 23:09:19,988 DEBUG [main] t.p.TestLinuxXen169 [TestLinuxXen169.java:522] PCR Report com.intel.mtwilson.policy.PcrMatchesConstant trusted? true
2013-04-09 23:09:19,988 DEBUG [main] t.p.TestLinuxXen169 [TestLinuxXen169.java:523] ---> {
  "expectedPcr" : {
    "value" : "db7f15304b8dd58b69fe3d3dcd6decad24ad5511",
    "index" : "19"
  }
}
         * 
         */
        // now look for all pcr matches:
        List<RuleResult> pcrReports = trustReport.getResults();
        log.debug("There are {} reports with trusted PcrMatchesConstant", pcrReports.size());
        for(RuleResult pcrReport : pcrReports) {
            if( pcrReport.getRule() instanceof PcrMatchesConstant ) {
                log.debug("PCR Report {} trusted? {}", pcrReport.getRuleName(), pcrReport.isTrusted());
                log.debug("---> {}", json.writeValueAsString(pcrReport.getRule()));
            }
        }
    }

    /**
     * This one calls getTrustStatus(), which logs to the mw_ta_log table
     * but returns an object with just 3 true/false flags, so we don't have
     * much to display here -- but check the mw_ta_log table after running
     * this test to see the real results. should be one record per PCR and
     * also one "overall trust" record with MleId=0.
     * @throws Exception 
     */
    @Test
    public void checkHostTrustStatusForXen() throws Exception {
        TblHosts host = My.jpa().mwHosts().findByName(hostname);
        assertNotNull(host); 
        HostTrustBO hostTrustBO = new HostTrustBO();
        HostTrustStatus trustStatus = hostTrustBO.getTrustStatus(host, host.getName());        
        log.debug("Bios trusted? {}", trustStatus.bios);
        log.debug("Vmm trusted? {}", trustStatus.vmm);
        log.debug("Location trusted? {}", trustStatus.location);
    }

}
