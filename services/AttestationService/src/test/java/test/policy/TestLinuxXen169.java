/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.policy;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.as.controller.*;
import com.intel.mtwilson.as.data.*;
import com.intel.mtwilson.audit.helper.AuditConfig;
import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.crypto.X509Util;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.jpa.PersistenceManager;
import com.intel.mtwilson.model.*;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.policy.TrustPolicy;
import com.intel.mtwilson.policy.TrustReport;
import com.intel.mtwilson.policy.impl.HostTrustPolicyFactory;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Properties;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.MapConfiguration;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class TestLinuxXen169 {
    private static CustomPersistenceManager pm;

    private transient Logger log = LoggerFactory.getLogger(getClass());
    private transient static ObjectWriter json = new ObjectMapper().writerWithDefaultPrettyPrinter();
    private transient String hostname = "10.1.71.169";
    private transient String connection = "intel:https://10.1.71.169:9999";
    
    /**
     * The CustomPersistenceManager allows you (developer) to connect
     * to the database directly for the tests. It also adds some other
     * convenience functions such as instantiating JPA controllers.
     */
    public static class CustomPersistenceManager extends PersistenceManager {
        @Override
        public void configure() {
            Properties p = new Properties();
            p.setProperty("mtwilson.db.host", "10.1.71.88");
            p.setProperty("mtwilson.db.schema", "mw_as");
            p.setProperty("mtwilson.db.user", "root");
            p.setProperty("mtwilson.db.password", "password");
            p.setProperty("mtwilson.db.port", "3306");
            MapConfiguration c = new MapConfiguration(p);
            addPersistenceUnit("ASDataPU", ASConfig.getJpaProperties(c));
            addPersistenceUnit("MSDataPU", MSConfig.getJpaProperties(c));
            addPersistenceUnit("AuditDataPU", AuditConfig.getJpaProperties(c));
        }
        public byte[] getDek() {
            return Base64.decodeBase64("hPKk/2uvMFRAkpJNJgoBwA=="); // arbitrary dek, since it's a development server it's good to use same as what is configured there, but it doesn't matter as it only affects records we are writing, and hopefully after each test is complete there is zero net effect on the database
        }
        
        TblHostsJpaController hostsJpa = null;
        TblMleJpaController mleJpa = null;
        TblOsJpaController osJpa = null;
        TblOemJpaController oemJpa = null;
        TblPcrManifestJpaController pcrJpa = null;
        HostTrustPolicyFactory hostTrustFactory = null;
        public CustomPersistenceManager() {
            
        }
        
        public TblHostsJpaController getHostsJpa() throws CryptographyException {
            if( hostsJpa == null ) {
                hostsJpa = new TblHostsJpaController(getEntityManagerFactory("ASDataPU"), getDek());
            }
            return hostsJpa;
        }
        public TblMleJpaController getMleJpa() {
            if( mleJpa == null ) {
                mleJpa = new TblMleJpaController(getEntityManagerFactory("ASDataPU"));
            }
            return mleJpa;
        }
        public TblOsJpaController getOsJpa() {
            if( osJpa == null ) {
                osJpa = new TblOsJpaController(getEntityManagerFactory("ASDataPU"));
            }
            return osJpa;
        }
        public TblOemJpaController getOemJpa() {
            if( oemJpa == null ) {
                oemJpa = new TblOemJpaController(getEntityManagerFactory("ASDataPU"));
            }
            return oemJpa;
        }
        public TblPcrManifestJpaController getPcrJpa() {
            if( pcrJpa == null ) {
                pcrJpa = new TblPcrManifestJpaController(getEntityManagerFactory("ASDataPU"));
            }
            return pcrJpa;
        }
        public HostTrustPolicyFactory getHostTrustFactory() {
            if( hostTrustFactory == null ) {
                hostTrustFactory = new HostTrustPolicyFactory(getEntityManagerFactory("ASDataPU"));
            }
            return hostTrustFactory;
        }
    }
    
    @BeforeClass
    public static void createPersistenceManager() {
        pm = new CustomPersistenceManager();
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
        host.setTlsPolicyName("TRUST_FIRST_CERTIFICATE");
        host.setTlsKeystore(null);
        host.setAddOnConnectionInfo(connection); // XXX notice we do not set the IPAddress or Port in the database... don't need to because now we are setting ConnectionString which includes them both and is passed to the host agent and the host agent extracts ip address and port from this string.
        return host;
    }
    
    @Test
    public void testRegisterHostAndWhitelist() throws Exception {
        // first, if it's already registered we need to delete it
        TblHosts host = pm.getHostsJpa().findByName(hostname);
        if( host != null ) {
            log.debug("Host {} is already in database, deleting", host.getName());
            pm.getHostsJpa().destroy(host.getId());
        }
        host = initNewHost();
        // now go to the host and fetch the PCR values -- this is similar to what management service does 
        HostAgentFactory factory = new HostAgentFactory();
        HostAgent agent = factory.getHostAgent(host);
        TxtHostRecord hostInfo = agent.getHostDetails();
        // whitelist step 1:  create OEM
        TblOem oem = pm.getOemJpa().findTblOemByName(hostInfo.BIOS_Oem);
        if( oem == null ) {
            oem = new TblOem();
            oem.setName(hostInfo.BIOS_Oem);
            oem.setDescription("Automatic whitelist from "+hostname);
            pm.getOemJpa().create(oem);
        }
        // whitelist step 2:  create BIOS MLE
        TblMle bios = pm.getMleJpa().findBiosMle(hostInfo.BIOS_Name, hostInfo.BIOS_Version, hostInfo.BIOS_Oem);
        if( bios == null ) {
            bios = new TblMle();
            bios.setAttestationType("PCR");
            bios.setDescription("Automatic whitelist from "+hostname);
            bios.setMLEType("BIOS");
            bios.setName(hostInfo.BIOS_Name);
            bios.setVersion(hostInfo.BIOS_Version);
            bios.setOemId(oem);
            bios.setRequiredManifestList("0"); // XXX TODO the required manifest list should actually come from EITHER 1) the vendor agent, because it knows exactly what that vendor does during boot, or 2) the UI, because the user might want specific things...  or a combination of providing UI defaults from the vendor, then allowing the UI to override... eitehr way,  right now these are hard-coded not only in this test class but also in the application, and that needs to change.
            pm.getMleJpa().create(bios);
        }
        // whitelist step 3:  create OS
        TblOs os = pm.getOsJpa().findTblOsByNameVersion(hostInfo.VMM_OSName, hostInfo.VMM_OSVersion);
        if( os == null ) {
            os = new TblOs();
            os.setName(hostInfo.VMM_OSName);
            os.setVersion(hostInfo.VMM_OSVersion);
            os.setDescription("Automatic whitelist from "+hostname);
            pm.getOsJpa().create(os);
        }
        // whitelist step 4:  create VMM MLE
        TblMle vmm = pm.getMleJpa().findVmmMle(hostInfo.VMM_Name, hostInfo.VMM_Version, hostInfo.VMM_OSName, hostInfo.VMM_OSVersion);
        if( vmm == null ) {
            vmm = new TblMle();
            vmm.setAttestationType("PCR");
            vmm.setDescription("Automatic whitelist from "+hostname);
            vmm.setMLEType("VMM");
            vmm.setName(hostInfo.VMM_Name);
            vmm.setVersion(hostInfo.VMM_Version);
            vmm.setOsId(os);
            vmm.setRequiredManifestList("17,18,19"); // XXX TODO the required manifest list should actually come from EITHER 1) the vendor agent, because it knows exactly what that vendor does during boot, or 2) the UI, because the user might want specific things...  or a combination of providing UI defaults from the vendor, then allowing the UI to override... eitehr way,  right now these are hard-coded not only in this test class but also in the application, and that needs to change.
            pm.getMleJpa().create(vmm);
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
            pm.getPcrJpa().create(pcrWhitelist);
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
            pm.getPcrJpa().create(pcrWhitelist);
        }
        
        // aik certificate
        if( agent.isAikAvailable() ) {
            if( agent.isAikCaAvailable() ) {
                X509Certificate aikcert = agent.getAikCertificate();
                host.setAIKCertificate(X509Util.encodePemCertificate(aikcert));
                host.setAikSha1(Sha1Digest.valueOf(aikcert.getPublicKey().getEncoded()).toString());
            }
            else {
                PublicKey aikpubkey = agent.getAik();
                host.setAIKCertificate(X509Util.encodePemPublicKey(aikpubkey));
                host.setAikSha1(Sha1Digest.valueOf(aikpubkey.getEncoded()).toString());
            }
        }
        // register host
        host.setBiosMleId(bios);
        host.setVmmMleId(vmm);
        pm.getHostsJpa().create(host);
    }
    
    
    /**
     * You need to run testRegisterHostAndWhitelist() first in order to create the host record and the whitelist
     * @throws Exception 
     */
    @Test
    public void verifyCreateTrustPolicyForHost() throws Exception {
        TblHosts host = pm.getHostsJpa().findByName(hostname);
        assertNotNull(host); 
        HostTrustPolicyFactory hostTrustPolicyFactory = pm.getHostTrustFactory();
        TrustPolicy trustPolicy = hostTrustPolicyFactory.loadTrustPolicyForHost(host); // must include both bios and vmm policies
        log.debug(json.writeValueAsString(trustPolicy));
        
    }
    
    /**
     * You need to run testRegisterHostAndWhitelist() first in order to create the host record and the whitelist
     * 
     * The following is an example of the report you get if the host exists & has a bios and mle in the whitelist but
     * those bios and mle do not have any associated mw_pcr_manifest records  - therfore nothing to check against the
     * host:
     * 
{
  "policy" : {
    "checks" : [ {
      "policy" : {
        "checks" : [ {
          "policy" : {
            "checks" : [ ]
          },
          "policyName" : "com.intel.mtwilson.policy.impl.TrustedBios"
        }, {
          "policy" : {
            "checks" : [ ]
          },
          "policyName" : "com.intel.mtwilson.policy.impl.TrustedVmm"
        }, {
          "policy" : {
            "checks" : [ ]
          },
          "policyName" : "com.intel.mtwilson.policy.impl.TrustedLocation"
        } ]
      },
      "policyName" : "com.intel.mtwilson.policy.RequireAll"
    } ]
  },
  "marks" : [ ],
  "faults" : [ {
    "cause" : null,
    "report" : {
      "policy" : {
        "checks" : [ {
          "policy" : {
            "checks" : [ ]
          },
          "policyName" : "com.intel.mtwilson.policy.impl.TrustedBios"
        }, {
          "policy" : {
            "checks" : [ ]
          },
          "policyName" : "com.intel.mtwilson.policy.impl.TrustedVmm"
        }, {
          "policy" : {
            "checks" : [ ]
          },
          "policyName" : "com.intel.mtwilson.policy.impl.TrustedLocation"
        } ]
      },
      "marks" : [ ],
      "faults" : [ {
        "cause" : null,
        "report" : {
          "policy" : {
            "checks" : [ ]
          },
          "marks" : [ ],
          "faults" : [ {
            "cause" : null,
            "faultName" : "com.intel.mtwilson.policy.fault.RequireAllEmptySet"
          } ],
          "trusted" : false,
          "policyName" : "com.intel.mtwilson.policy.impl.TrustedBios"
        },
        "faultName" : "com.intel.mtwilson.policy.fault.Cite"
      }, {
        "cause" : null,
        "report" : {
          "policy" : {
            "checks" : [ ]
          },
          "marks" : [ ],
          "faults" : [ {
            "cause" : null,
            "faultName" : "com.intel.mtwilson.policy.fault.RequireAllEmptySet"
          } ],
          "trusted" : false,
          "policyName" : "com.intel.mtwilson.policy.impl.TrustedVmm"
        },
        "faultName" : "com.intel.mtwilson.policy.fault.Cite"
      }, {
        "cause" : null,
        "report" : {
          "policy" : {
            "checks" : [ ]
          },
          "marks" : [ ],
          "faults" : [ {
            "cause" : null,
            "faultName" : "com.intel.mtwilson.policy.fault.RequireAllEmptySet"
          } ],
          "trusted" : false,
          "policyName" : "com.intel.mtwilson.policy.impl.TrustedLocation"
        },
        "faultName" : "com.intel.mtwilson.policy.fault.Cite"
      } ],
      "trusted" : false,
      "policyName" : "com.intel.mtwilson.policy.RequireAll"
    },
    "faultName" : "com.intel.mtwilson.policy.fault.Cite"
  } ],
  "trusted" : false,
  "policyName" : "com.intel.mtwilson.policy.RequireAll"
}
     * 
     * 
     * @throws Exception 
     */
    @Test
    public void verifyTrustStatusForXen() throws Exception {
        TblHosts host = pm.getHostsJpa().findByName(hostname);
        assertNotNull(host); 
        HostTrustBO hostTrustBO = new HostTrustBO(pm);
        TrustReport trustReport = hostTrustBO.getTrustReportForHost(host);        
        log.debug(json.writeValueAsString(trustReport));
    }
}
