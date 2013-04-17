/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.policy;

import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.as.data.MwMleSource;
import com.intel.mtwilson.as.data.TblHostSpecificManifest;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.data.TblModuleManifest;
import com.intel.mtwilson.as.data.TblOem;
import com.intel.mtwilson.as.data.TblOs;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.crypto.X509Util;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.model.PcrEventLog;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.mtwilson.model.Sha1Digest;
import com.intel.mtwilson.policy.impl.HostTrustPolicyManager;
import com.intel.mtwilson.policy.*;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.util.MyJpaDatastore;
import java.util.Set;

/**
 *
 * @author jbuhacoff
 */
public class TestVmwareEsxi51 {
    private static MyJpaDatastore pm = new MyJpaDatastore();
    private transient Logger log = LoggerFactory.getLogger(getClass());
    private transient static ObjectWriter json = new ObjectMapper().writerWithDefaultPrettyPrinter();

    private transient String hostname = "10.1.71.155";
    private transient String connection = "vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
    
    private TblHosts initNewHost() {
        TblHosts host = new TblHosts();
        host.setName(hostname);
        host.setTlsPolicyName("TRUST_FIRST_CERTIFICATE");
        host.setTlsKeystore(null);
        host.setAddOnConnectionInfo(connection); // XXX notice we do not set the IPAddress or Port in the database... don't need to because now we are setting ConnectionString which includes them both and is passed to the host agent and the host agent extracts ip address and port from this string.
        return host;
    }

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
    
    /**
     * Extend ZERO with ZERO (20 byte):
2013-04-11 23:26:58,846 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:59] PCR 20 = 0000000000000000000000000000000000000000
2013-04-11 23:26:58,855 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:59] PCR 20 = b80de5d138758541c5f05265ad144ab9fa86d1db
2013-04-11 23:26:58,856 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:59] PCR 20 = 850659b18eb6fb4ccdcb113ca4266eb945449466
2013-04-11 23:26:58,856 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:59] PCR 20 = 040acda4a47c13e2959cae1a56b76401b713524e
2013-04-11 23:26:58,856 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:62] PCR 20 = ad919ad2b122e5741612e16329aa7626b63ada73
     * 
     * Extend ZERO with SHA1(20 bytes of zero):
2013-04-11 23:31:05,432 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:84] PCR 20 = 0000000000000000000000000000000000000000
2013-04-11 23:31:05,435 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:84] PCR 20 = 2c754ca949cfa83b323df8ed0057333551d15dc2
2013-04-11 23:31:05,436 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:84] PCR 20 = 596788206ef0f0cf6a771fd99bde6d0fcf4eed69
2013-04-11 23:31:05,436 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:84] PCR 20 = 52dca4d81928d58b24da08371b5fbc591149d88d
2013-04-11 23:31:05,436 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:87] PCR 20 = 6fedb305092c25a0c248cc8d1432a56828537928
     * 
     * 
     * Extend ZERO with ZERO (40 byte):
2013-04-11 23:28:36,412 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:68] PCR 20 = 0000000000000000000000000000000000000000
2013-04-11 23:28:36,421 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:68] PCR 20 = fb3d8fb74570a077e332993f7d3d27603501b987
2013-04-11 23:28:36,421 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:68] PCR 20 = a5a3f1d7e009bc2d2b9ef4cead302b4f859701f3
2013-04-11 23:28:36,421 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:68] PCR 20 = fb7713dcf801b33a4e818cc5817fb2975964330f
2013-04-11 23:28:36,422 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:71] PCR 20 = 41d0aac78121b5715fccc2fb9b2b22ae51590d88
* 
* Extend ZERO with SHA1(40 bytes of zero):
2013-04-11 23:30:03,552 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:78] PCR 20 = 0000000000000000000000000000000000000000
2013-04-11 23:30:03,555 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:78] PCR 20 = 37c42bd6cc033b33086291c007c3aa12707a8bb4
2013-04-11 23:30:03,555 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:78] PCR 20 = 356b9870872a266234d38c4ccd79d739d062eb5d
2013-04-11 23:30:03,555 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:78] PCR 20 = c8f7670a0dc2eaf15c90ae7d9697bfd46b4111e3
2013-04-11 23:30:03,556 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:81] PCR 20 = b3ee188648dd01ebf857e9a08f28f482d4afae52
* 
     * @throws Exception 
     */
    @Test
    public void testPcr20WithJustZeros() throws Exception {
        Sha1Digest pcr20 = Sha1Digest.ZERO;
        Sha1Digest sha1forty = Sha1Digest.valueOf(new byte[] { 0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0});
        for(int i=0; i<4; i++) {
            log.debug("PCR 20 = {}", pcr20.toString());
            pcr20 = pcr20.extend(sha1forty); // sha1(40 bytes of zero)
        }
        log.debug("PCR 20 = {}", pcr20.toString());
    }
    
    
    /**
     * A lot of the code in this method is similar to what you find in TestLinuxXen169
     * @throws Exception 
     */
    @Test
    public void testRegisterVmwareHostAndWhitelist() throws Exception {
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
            vmm.setRequiredManifestList("17,18,20"); // XXX TODO the required manifest list should actually come from EITHER 1) the vendor agent, because it knows exactly what that vendor does during boot, or 2) the UI, because the user might want specific things...  or a combination of providing UI defaults from the vendor, then allowing the UI to override... eitehr way,  right now these are hard-coded not only in this test class but also in the application, and that needs to change.
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
        String[] vmmPcrList = vmm.getRequiredManifestList().split(","); // only 17, 18, 20  ... 19 is treated separately below for vmware
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
        PcrEventLog pcr19 = pcrManifest.getPcrEventLog(PcrIndex.PCR19);
        ArrayList<TblHostSpecificManifest> hostSpecificEventLogEntries = new ArrayList<TblHostSpecificManifest>();
        List<Measurement> vmwareEvents = pcr19.getEventLog();
        for(Measurement m : vmwareEvents) {
            log.debug("Adding VMM module/event {} = {}", m.getLabel(), m.getValue().toString());
            TblModuleManifest eventLogEntry = new TblModuleManifest();
            eventLogEntry.setComponentName(m.getInfo().get("ComponentName"));
            eventLogEntry.setDescription(m.getLabel());
            eventLogEntry.setEventID(null); // XXX TODO what is this ???
            eventLogEntry.setExtendedToPCR(PcrIndex.PCR19.toString());
            eventLogEntry.setMleId(vmm);
            eventLogEntry.setNameSpaceID(null); // XXX TODO why do we care ???
            eventLogEntry.setPackageName(m.getInfo().get("PackageName"));
            eventLogEntry.setPackageVendor(m.getInfo().get("PackageVendor"));
            eventLogEntry.setPackageVersion(m.getInfo().get("PackageVersion"));
            if( m.getInfo().get("EventType").equals("HostTpmCommandEvent") ) {
                eventLogEntry.setUseHostSpecificDigestValue(true);
                // now create a host-specific value...
                TblHostSpecificManifest hostSpecificEventLogEntry = new TblHostSpecificManifest();
                hostSpecificEventLogEntry.setDigestValue(m.getValue().toString());
                hostSpecificEventLogEntry.setModuleManifestID(eventLogEntry); // when it is saved it will get an id, and later when this record is saved it will all work out...
                hostSpecificEventLogEntries.add(hostSpecificEventLogEntry); // will be saved to database later 
            }
            else {
                eventLogEntry.setDigestValue(m.getValue().toString());
            }
            pm.getModuleJpa().create(eventLogEntry);
        }
        // whitelist step 8: document that these mle's came from this host (not necessary for attestation, but to make this example complete)
        MwMleSource biosMleSource = pm.getMleSourceJpa().findByMleId(bios.getId());
        if( biosMleSource == null ) {
            biosMleSource = new MwMleSource();
            biosMleSource.setMleId(bios);
            biosMleSource.setHostName(hostname);
            pm.getMleSourceJpa().create(biosMleSource);
        }
        MwMleSource vmmMleSource = pm.getMleSourceJpa().findByMleId(vmm.getId());
        if( vmmMleSource == null ) {
            vmmMleSource = new MwMleSource();
            vmmMleSource.setMleId(vmm);
            vmmMleSource.setHostName(hostname);
            pm.getMleSourceJpa().create(vmmMleSource);
        }
        // aik certificate is skipped since vmware doesn't have aik;  but leaving this code here since it's already guarded by isAikAvailable() 
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
        // create host with the mle id's
        host.setBiosMleId(bios);
        host.setVmmMleId(vmm);
        
        pm.getHostsJpa().create(host);
        
        // now add host-specific information to the database
        for(TblHostSpecificManifest hostSpecificEventLogEntry : hostSpecificEventLogEntries) {
            // now that the host is created and has an id, we can do this:
            hostSpecificEventLogEntry.setHostID(host.getId()); 
            pm.getHostSpecificModuleJpa().create(hostSpecificEventLogEntry);            
        }
        
    }
        
    /*
    @Test
    public void testGeneratePolicyForHost() throws Exception {
        TblHosts host = initNewHost();
        HostAgentFactory agentFactory = new HostAgentFactory();
        HostAgent agent = agentFactory.getHostAgent(host);
        HostReport hostReport = new HostReport();
        hostReport.pcrManifest = agent.getPcrManifest();
        hostReport.variables = agent.getHostAttributes();
        HostTrustPolicyManager policyFactory = new HostTrustPolicyManager(pm.getEntityManagerFactory("ASDataPU"));
//        Set<Rule> rules = policyFactory.generateTrustRulesForHost(host, hostReport);
//        Policy policy = new Policy("Automatically generated policy for "+host.getName(), rules);
        Policy policy = policyFactory.createWhitelistFromHost(host);
        System.out.println(json.writeValueAsString(policy));
    }
    */
}
