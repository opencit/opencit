/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.policy;

import com.intel.mtwilson.My;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.as.data.MwMleSource;
import com.intel.mtwilson.as.data.TblHostSpecificManifest;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.util.Aes128DataCipher;
import com.intel.mtwilson.crypto.Aes128;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.data.TblModuleManifest;
import com.intel.mtwilson.as.data.TblOem;
import com.intel.mtwilson.as.data.TblOs;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.as.data.TblSamlAssertion;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.datatypes.ConnectionString;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.intel.dcsg.cpg.crypto.RsaUtil;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import org.apache.commons.codec.binary.Base64;
import com.intel.mtwilson.util.ASDataCipher;
import java.io.IOException;
import java.util.Map;
import org.junit.BeforeClass;

/**
 *
 * @author jbuhacoff
 */
public class TestVmwareEsxi51 {
    private transient Logger log = LoggerFactory.getLogger(getClass());
    private transient static ObjectWriter json = new ObjectMapper().writerWithDefaultPrettyPrinter();

    private transient String hostname = "10.1.71.175";
    private transient String connection = "vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;10.1.71.175";
//    private transient String hostname = "10.1.71.155";
//    private transient String connection = "vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
    
    @BeforeClass
    public static void setMwHostsDek() throws CryptographyException, IOException {
        ASDataCipher.cipher = new Aes128DataCipher(new Aes128(Base64.decodeBase64(My.configuration().getDataEncryptionKeyBase64())));
    }
    
    private TblHosts initNewHost() {
        TblHosts host = new TblHosts();
        host.setName(hostname);
        host.setTlsPolicyName("INSECURE");
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
        TblHosts host = My.jpa().mwHosts().findByName(hostname);
        if( host != null ) {
            log.warn("Host {} is already in database, deleting", host.getName());
            // before we delete a host we first need to delete all its saml assertions, otherwise the database throws a constraint violation...
            List<TblSamlAssertion> samlRecordList = My.jpa().mwSamlAssertion().findByHostID(host);
            for(TblSamlAssertion samlRecord : samlRecordList) {
                My.jpa().mwSamlAssertion().destroy(samlRecord.getId());
            }
            // also delete the ONE host-specific module, if it was defined for this host.  done here and not below with other modules because 1) there can only be one host-specific module per host in mtwilson-1.1 -- which is weird, and 2) it is looked up by host id and after we delete the host we won't hav an id anymore...
            List<TblHostSpecificManifest> hostSpecificToDelete = My.jpa().mwHostSpecificManifest().findByHostID(host.getId()); // XXX note how there can be only ONE host specific module per host, according to the Jpa Controller's method... not good!!
            if( hostSpecificToDelete != null ) {
                My.jpa().mwHostSpecificManifest().destroy(hostSpecificToDelete.get(0).getId());
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
            vmm.setRequiredManifestList("18,19,20"); // XXX TODO the required manifest list should actually come from EITHER 1) the vendor agent, because it knows exactly what that vendor does during boot, or 2) the UI, because the user might want specific things...  or a combination of providing UI defaults from the vendor, then allowing the UI to override... eitehr way,  right now these are hard-coded not only in this test class but also in the application, and that needs to change.
            My.jpa().mwMle().create(vmm);
        }
        // whitelist step 5: get PCRs
        PcrManifest pcrManifest = agent.getPcrManifest();        
        // whitelist step 6: create whitelist entries for BIOS PCRs
        String[] biosPcrList = bios.getRequiredManifestList().split(",");
        for(String biosPcrIndex : biosPcrList) {
            // first delete any pcr's already defined for this vmm, since we will redefine them now
            TblPcrManifest pcrWhitelist = My.jpa().mwPcrManifest().findByMleIdNamePcrBank(bios.getId(), biosPcrIndex, "SHA1");
            if( pcrWhitelist != null ) {
                My.jpa().mwPcrManifest().destroy(pcrWhitelist.getId());
            }
            // now define the pcr
            pcrWhitelist = new TblPcrManifest();
            Pcr pcr = pcrManifest.getPcr(Integer.valueOf(biosPcrIndex));
            log.debug("Adding BIOS PCR {} = {}", pcr.getIndex().toString(), pcr.getValue().toString());
            pcrWhitelist.setMleId(bios);
            pcrWhitelist.setName(pcr.getIndex().toString());
            pcrWhitelist.setValue(pcr.getValue().toString());
            pcrWhitelist.setPCRDescription("Automatic BIOS whitelist from "+hostname);
            My.jpa().mwPcrManifest().create(pcrWhitelist);
        }
        // whitelist step 7: create whitelist entries for VMM PCRs
        String[] vmmPcrList = vmm.getRequiredManifestList().split(","); // only 17, 18, 20  ... 19 is treated separately below for vmware
        for(String vmmPcrIndex : vmmPcrList) {
            // first delete any pcr's already defined for this vmm, since we will redefine them now
            TblPcrManifest pcrWhitelist = My.jpa().mwPcrManifest().findByMleIdNamePcrBank(vmm.getId(), vmmPcrIndex, "SHA1");
            if( pcrWhitelist != null ) {
                My.jpa().mwPcrManifest().destroy(pcrWhitelist.getId());
            }
            // now define the pcr
            pcrWhitelist = new TblPcrManifest();
            Pcr pcr = pcrManifest.getPcr(Integer.valueOf(vmmPcrIndex));
            log.debug("Adding VMM PCR {} = {}", pcr.getIndex().toString(), pcr.getValue().toString());
            pcrWhitelist.setMleId(vmm);
            pcrWhitelist.setName(pcr.getIndex().toString());
            if( pcr.getIndex().equals(PcrIndex.PCR19) ) {// for pcr 19 we skip setting the value, since the value is calculated dynamically based on the events - some of which are host specific which is why we can't record a value here to apply to all hosts
                pcrWhitelist.setValue("");  // XXX not allows to set it to null due to database schema constraint...     i think mt wilson 1.1 just sets this to a single blank space. TODO either remove the constraint, recognizing that for pcrs that include host-specific events we cannot have a meaningful static whitelist value, or completely change the database schema to model teh policy/rule objects 
            }
            else {
                pcrWhitelist.setValue(pcr.getValue().toString()); 
            }
            pcrWhitelist.setPCRDescription("Automatic VMM whitelist from "+hostname);
            My.jpa().mwPcrManifest().create(pcrWhitelist);
        }
        // whitelist step 7.1:  delete existing whitelist entries for modules that correspond to VMM PCR's
        List<TblModuleManifest> existingEventLogEntries = My.jpa().mwModuleManifest().findByMleId(vmm.getId());
        for(TblModuleManifest eventLogEntry : existingEventLogEntries) {
            My.jpa().mwModuleManifest().destroy(eventLogEntry.getId()); // don't need to be concerned with the host-specific version because if it existed,  (and there can be only ONE host-specific module in mtwilson-1.1)  then it was already deleted above when we deleted the host
        }
        // whitelist step 7.5: create whitelist entries for modules that correspond to VMM PCR's
        PcrEventLog pcr19 = pcrManifest.getPcrEventLog(PcrIndex.PCR19);
        ArrayList<TblHostSpecificManifest> hostSpecificEventLogEntries = new ArrayList<TblHostSpecificManifest>();
        List<Measurement> vmwareEvents = pcr19.getEventLog();
        for(Measurement m : vmwareEvents) {
            Map<String, String> mInfo = m.getInfo();
            TblModuleManifest eventLogEntry = new TblModuleManifest();
            log.debug("Adding VMM module/event {} = {}", m.getLabel(), m.getValue().toString());
            
            // NOTE : here we are switching component name and description because the trust dashboard UI shows component name... even though it's EVENTS that are actually of different types and not all of them have a meaningful "component name" ... so... we put the description, which is relevant to every event,  as the component name,  and log the compnet name in the description. when the trust dashboard is fixed this can be reverted to look more natural.
             eventLogEntry.setComponentName(m.getLabel());
//            eventLogEntry.setComponentName(String.format("%s-%s", m.getInfo().get("PackageName"), m.getInfo().get("PackageVersion")));  // omitting PackageVendor because it's always VMware for vmware modules... and anyway this record is linked to an MLE which is a vmware MLE, so same name across different MLE's will not be a problem... and it's unlikely that the same mle will have two or more drivers by different vendors with the same name and version! but if it happens it won't be a problem, as long as the hashes are still different. 
            //m.getInfo().get("ComponentName") // in mtwilson 1.0 component name was something like "componentName.ata_pata.v02" even if the module name was really ata_pata_cmd64x and had a vesrion number like 0.2.5-3vmw10.0.0.799733 which was more than sufficient to distinguish it from other modules.
            eventLogEntry.setDescription(mInfo.get("ComponentName"));
            log.debug("Looking up event type {}", mInfo.get("EventName"));
            eventLogEntry.setEventID(My.jpa().mwEventType().findEventTypeByName(mInfo.get("EventName"))); // XXX we really don't need these event types, they are too specific to vmware and not configurable anyway... what's the point of looking it up? ... and what we did with prefixing "Vim25Api." just makes it more confusing, because now OUR "Event Type" isn't the same text as VMWARE's "Event Type"
            eventLogEntry.setExtendedToPCR(PcrIndex.PCR19.toString());
            eventLogEntry.setMleId(vmm);
            eventLogEntry.setNameSpaceID(My.jpa().mwPackageNamespace().findTblPackageNamespace(1)); // XXX why do we have this package namespace?   it's too specific to vmware, not configurable, and we always use the same record. 
            eventLogEntry.setPackageName(mInfo.get("PackageName"));
            eventLogEntry.setPackageVendor(mInfo.get("PackageVendor"));
            eventLogEntry.setPackageVersion(mInfo.get("PackageVersion"));
            // XXX TODO this magic belongs in the vmware-specific rule factory. only a vmware factory would know that the "HostTpmCommandEvent" value is different for every host because it contains the host's UUID.
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
            My.jpa().mwModuleManifest().create(eventLogEntry);
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
        // aik certificate is skipped since vmware doesn't have aik;  but leaving this code here since it's already guarded by isAikAvailable() 
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
        // create host with the mle id's
        host.setBiosMleId(bios);
        host.setVmmMleId(vmm);
        
        My.jpa().mwHosts().create(host);
        
        // now add host-specific information to the database
        for(TblHostSpecificManifest hostSpecificEventLogEntry : hostSpecificEventLogEntries) {
            // now that the host is created and has an id, we can do this:
            hostSpecificEventLogEntry.setHostID(host.getId()); 
            My.jpa().mwHostSpecificManifest().create(hostSpecificEventLogEntry);            
        }
        
    }

    @Test
    public void loadTrustPolicyForHost() throws Exception {
        TblHosts host = My.jpa().mwHosts().findByName(hostname);
        assertNotNull(host); 
        HostTrustPolicyManager hostTrustPolicyFactory = new HostTrustPolicyManager(My.persistenceManager().getASData());
        Policy trustPolicy = hostTrustPolicyFactory.loadTrustPolicyForHost(host, hostname); // must include both bios and vmm policies
        log.debug(json.writeValueAsString(trustPolicy));
//        log.debug(xml.writeValueAsString(trustPolicy)); // notice the xml is NOT the same as the json at all... doesn't have all the info, and somehow has mixed "faults" in there somewhere even though the Rule objects do not have a fault method or field...
    }
    
    @Test
    public void testApplyPolicyForHost() throws Exception {
        TblHosts host = My.jpa().mwHosts().findByName(hostname);
        assertNotNull(host); 
        HostAgentFactory agentFactory = new HostAgentFactory();
        HostAgent agent = agentFactory.getHostAgent(host); // new ConnectionString(connection));//agentFactory.getHostAgent(host);
        HostReport hostReport = new HostReport();
        hostReport.pcrManifest = agent.getPcrManifest();
        hostReport.variables = agent.getHostAttributes();
        HostTrustPolicyManager hostTrustPolicyFactory = new HostTrustPolicyManager(My.persistenceManager().getASData());
        Policy policy = hostTrustPolicyFactory.loadTrustPolicyForHost(host, hostname);
//        log.debug("TRUST POLICY: {}", json.writeValueAsString(policy));
        PolicyEngine engine = new PolicyEngine();
        TrustReport report = engine.apply(hostReport, policy);
        log.debug("TRUST REPORT: {}", json.writeValueAsString(report));
    }
    
    
    @Test
    public void testExtractUUID() {
        String input = "/b.b00 vmbTrustedBoot=true tboot=0x0x101a000 no-auto-partition bootUUID=772753050c0a140bdfbf92e306b9793d"; // a command line event from a vmware esxi server event log for pcr 19
        Pattern uuidPattern = Pattern.compile(".*bootUUID=([a-fA-F0-9]+)[^a-fA-F0-9]?.*");
        Matcher uuidMatcher = uuidPattern.matcher(input);
        if( uuidMatcher.matches() ) {
            log.debug("Found UUID: {}", uuidMatcher.group(1)); 
        }
    }
    

}
