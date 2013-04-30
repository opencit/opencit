package com.intel.mtwilson.as.business;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mountwilson.as.helper.TrustAgentSecureClient;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.vmware.VMwareClient;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.as.controller.TblHostSpecificManifestJpaController;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.controller.TblLocationPcrJpaController;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.controller.TblModuleManifestJpaController;
import com.intel.mtwilson.as.controller.TblSamlAssertionJpaController;
import com.intel.mtwilson.as.controller.TblTaLogJpaController;
import com.intel.mtwilson.as.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblHostSpecificManifest;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.data.TblModuleManifest;
import com.intel.mtwilson.as.data.TblSamlAssertion;
import com.intel.mtwilson.as.data.TblTaLog;
import com.intel.mtwilson.as.data.helper.DataCipher;
import com.intel.mtwilson.as.helper.BaseBO;
import com.intel.mtwilson.crypto.Aes128;
import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.crypto.X509Util;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.jpa.PersistenceManager;
import com.intel.mtwilson.model.*;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.Rule;
import com.intel.mtwilson.policy.impl.HostTrustPolicyManager;
import com.intel.mtwilson.util.ResourceFinder;
import com.vmware.vim25.HostTpmAttestationReport;
import com.vmware.vim25.HostTpmCommandEventDetails;
import com.vmware.vim25.HostTpmDigestInfo;
import com.vmware.vim25.HostTpmEventLogEntry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All settings should be via setters, not via constructor, because this class
 * may be instantiated by a factory.
 *
 * @author dsmagadx
 */
public class HostBO extends BaseBO {

	private static final String COMMAND_LINE_MANIFEST = "/b.b00 vmbTrustedBoot=true tboot=0x0x101a000";
	public static final PcrIndex LOCATION_PCR = PcrIndex.PCR22;
        private Logger log = LoggerFactory.getLogger(getClass());
        private TblMle biosMleId = null;
        private TblMle vmmMleId = null;
        private byte[] dataEncryptionKey = null;
        private TblLocationPcrJpaController locationPcrJpaController = new TblLocationPcrJpaController(getEntityManagerFactory());
        private TblMleJpaController mleController = new TblMleJpaController(getEntityManagerFactory());
        private TblHostsJpaController hostController;
        private HostTrustPolicyManager hostTrustPolicyFactory = new HostTrustPolicyManager(getEntityManagerFactory());
        private TblHostSpecificManifestJpaController hostSpecificManifestJpaController = new TblHostSpecificManifestJpaController(getEntityManagerFactory());
        private TblModuleManifestJpaController moduleManifestJpaController = new TblModuleManifestJpaController(getEntityManagerFactory());

        private static class Aes128DataCipher implements DataCipher {
            private Logger log = LoggerFactory.getLogger(getClass());
            private Aes128 cipher;
            public Aes128DataCipher(Aes128 cipher) { this.cipher = cipher; }
            
            @Override
            public String encryptString(String plaintext) {
                try {
                    return cipher.encryptString(plaintext);
                }
                catch(CryptographyException e) {
                    log.error("Failed to encrypt data", e);
                    return null;
                }
            }

            @Override
            public String decryptString(String ciphertext) {
                try {
                    return cipher.decryptString(ciphertext);
                }
                catch(CryptographyException e) {
                    log.error("Failed to decrypt data", e);
                    return null;
                }
            }
            
        }
        
        public void setDataEncryptionKey(byte[] key) {
                    try {
                        TblHosts.dataCipher = new Aes128DataCipher(new Aes128(key));
                    }
                    catch(CryptographyException e) {
                        log.error("Cannot initialize data encryption cipher", e);
                    }      
        }
        
    public HostBO() {
        super();
    }
    
    public HostBO(PersistenceManager pm) {
        super(pm);
    }
        
	public HostResponse addHost(TxtHost host) {
            
           System.err.println("HOST BO ADD HOST STARTING");
            
          TblMle  biosMleId = findBiosMleForHost(host); 
          TblMle  vmmMleId = findVmmMleForHost(host); 

                log.error("HOST BO ADD HOST STARTING");

                try {
                        checkForDuplicate(host);

                        getBiosAndVMM(host);

                        log.info("Getting Server Identity.");

                        // BUG #497  setting default tls policy name and empty keystore for all new hosts. XXX TODO allow caller to provide keystore contents in pem format in the call ( in the case of the other tls policies ) or update later
                        TblHosts tblHosts = new TblHosts();
                        tblHosts.setTlsPolicyName("TRUST_FIRST_CERTIFICATE");
                        tblHosts.setTlsKeystore(null);
                        System.err.println("stdalex addHost " + host.getHostName() + " with cs == " + host.getAddOn_Connection_String());
                        tblHosts.setAddOnConnectionInfo(host.getAddOn_Connection_String());
                        if (host.getHostName() != null) {
                                tblHosts.setName(host.getHostName().toString());
                        }
                        if (host.getIPAddress() != null) {
                                tblHosts.setIPAddress(host.getIPAddress().toString());
                        }
                        if (host.getPort() != null) {
                                tblHosts.setPort(host.getPort());
                        }

                        HostAgentFactory factory = new HostAgentFactory();
                        HostAgent agent = factory.getHostAgent(tblHosts);

                        if( agent.isAikAvailable() ) { // INTEL and CITRIX
                                // stores the AIK public key (and certificate, if available) in the host record, and sets AIK_SHA1=SHA1(AIK_PublicKey) on the host record too
                                setAikForHost(tblHosts, host); 
                                // Intel hosts return an X509 certificate for the AIK public key, signed by the privacy CA.  so we must verify the certificate is ok.
                                if( agent.isAikCaAvailable() ) {
                                    // we have to check that the aik certificate was signed by a trusted privacy ca
                                    X509Certificate hostAikCert = X509Util.decodePemCertificate(tblHosts.getAIKCertificate());
                                    hostAikCert.checkValidity(); // AIK certificate must be valid today
                                    boolean validCaSignature = isAikCertificateTrusted(hostAikCert);
                                    if( !validCaSignature ) {
                                        throw new ASException(ErrorCode.AS_INVALID_AIK_CERTIFICATE, host.getHostName().toString());
                                    }
                                }
                        }

                        // retrieve the complete manifest for  the host, includes ALL pcr's and if there is module info available it is included also.
                        PcrManifest pcrManifest = agent.getPcrManifest();  // currently Vmware has pcr+module, but in 1.2 we are adding module attestation for Intel hosts too ;   citrix would be just pcr for now i guess
                        

                        // send the pcr manifest to a vendor-specific class in order to extract any host-specific information
                        // for vmware this is the "HostTpmCommandLineEventDetails" which is a host-specific value and must be
                        // saved into mw_host_specific _manifest  (using the MLE information obtained with getBiosAndVmm(host) above...)
//                        HostReport hostReport = new HostReport();
//                        hostReport.aik = null; // TODO should be what we get above if it's available
//                        hostReport.pcrManifest = pcrManifest;
//                        hostReport.tpmQuote = null;
//                        hostReport.variables = new HashMap<String,String>(); // for example if we know a UUID ... we would ADD IT HERE

//                        TrustPolicy hostSpecificTrustPolicy = hostTrustPolicyFactory.createHostSpecificTrustPolicy(hostReport, biosMleId, vmmMleId); // XXX TODO add the bios mle and vmm mle information to HostReport ?? only if they are needed by some policies...
                        List<TblHostSpecificManifest>   tblHostSpecificManifests = createHostSpecificManifestRecords(vmmMleId, pcrManifest);
                        // now for vmware specifically,  we have to pass this along to the vmware-specific function because it knows which modules are host-specific (the commandline event)  and has to store those in mw_host_specific  ...
//                            pcrMap = getHostPcrManifest(tblHosts, host); // BUG #497 sending both the new TblHosts record and the TxtHost object just to get the TlsPolicy into the initial call so that with the trust_first_certificate policy we will obtain the host certificate now while adding it
                        

                        // for all hosts (used to be just vmware, but no reason right now to make it vmware-specific...), if pcr 22 happens to match our location database, populate the location field in the host record
                            tblHosts.setLocation( getLocation(pcrManifest) );
                        
                        
                        //Bug: 597, 594 & 583. Here we were trying to get the length of the TlsKeystore without checking if it is NULL or not. 
                        // If in case it is NULL, it would throw NullPointerException                        
                        log.info("Saving Host in database with TlsPolicyName {} and TlsKeystoreLength {}", tblHosts.getTlsPolicyName(), tblHosts.getTlsKeystore() == null ? "null" : tblHosts.getTlsKeystore().length);

                        log.error("HOST BO CALLING SAVEHOSTINDATABASE");
                        saveHostInDatabase(tblHosts, host, pcrManifest, tblHostSpecificManifests, biosMleId, vmmMleId);

		} catch (ASException ase) {
			throw ase;
		} 
//                catch(CryptographyException e) {
//                    throw new ASException(e,ErrorCode.AS_ENCRYPTION_ERROR, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
//                } 
        catch (Exception e) {
			throw new ASException(e);
		}
		return new HostResponse(ErrorCode.OK);
	}

    private boolean isAikCertificateTrusted(X509Certificate hostAikCert) {
        log.debug("isAikCertificateTrusted {}", hostAikCert.getSubjectX500Principal().getName());
        // TODO read privacy ca certs from database and see if any one of them signed it. 
        // read privacy ca certificate.  if there is a privacy ca list file available (PrivacyCA.pem) we read the list from that. otherwise we just use the single certificate in PrivacyCA.cer (DER formatt)
        HashSet<X509Certificate> pcaList = new HashSet<X509Certificate>();
        try {
            InputStream privacyCaIn = new FileInputStream(ResourceFinder.getFile("PrivacyCA.p12.pem")); // may contain multiple trusted privacy CA certs
            List<X509Certificate> privacyCaCerts = X509Util.decodePemCertificates(IOUtils.toString(privacyCaIn));
            pcaList.addAll(privacyCaCerts);
            IOUtils.closeQuietly(privacyCaIn);
            log.debug("Added {} certificates from PrivacyCA.p12.pem", privacyCaCerts.size());
        }
        catch(Exception e) {
            // FileNotFoundException: cannot find PrivacyCA.pem
            // CertificateException: error while reading certificates from file
            log.warn("Cannot load PrivacyCA.p12.pem");            
        }
        try {
            InputStream privacyCaIn = new FileInputStream(ResourceFinder.getFile("PrivacyCA.cer")); // may contain multiple trusted privacy CA certs
            X509Certificate privacyCaCert = X509Util.decodeDerCertificate(IOUtils.toByteArray(privacyCaIn));
            pcaList.add(privacyCaCert);
            IOUtils.closeQuietly(privacyCaIn);
            log.debug("Added certificate from PrivacyCA.cer");
        }
        catch(Exception e) {
            // FileNotFoundException: cannot find PrivacyCA.cer
            // CertificateException: error while reading certificate from file
            log.warn("Cannot load PrivacyCA.cer", e);            
        }
        boolean validCaSignature = false;
        for(X509Certificate pca : pcaList) {
            try {
                if( Arrays.equals(pca.getSubjectX500Principal().getEncoded(), hostAikCert.getIssuerX500Principal().getEncoded()) ) {
                    log.debug("Found matching CA: {}", pca.getSubjectX500Principal().getName());
                    pca.checkValidity(hostAikCert.getNotBefore()); // Privacy CA certificate must have been valid when it signed the AIK certificate
                    hostAikCert.verify(pca.getPublicKey()); // verify the trusted privacy ca signed this aik cert.  throws NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
                    // TODO read the CRL for this privacy ca and ensure this AIK cert has not been revoked
                    // TODO check if the privacy ca cert is self-signed... if it's not self-signed  we should check for a path leading to a known root ca in the root ca's file
                    validCaSignature = true;
                }
            }
            catch(Exception e) {
                log.warn("Failed to verify AIK signature with CA", e); // but don't re-throw because maybe another cert in the list is a valid signer
            }
        }
        return validCaSignature;
    }

	private String getLocation(PcrManifest pcrManifest) {
        if( pcrManifest == null ) { return null; }
        if( pcrManifest.containsPcr(LOCATION_PCR) ) {
            String value = pcrManifest.getPcr(LOCATION_PCR).getValue().toString();
            return locationPcrJpaController.findTblLocationPcrByPcrValue(value);
        }
		return null;
    }
    
    private void createHostSpecificManifest(List<TblHostSpecificManifest> tblHostSpecificManifests, TblHosts tblHosts) {
        
        for(TblHostSpecificManifest tblHostSpecificManifest : tblHostSpecificManifests){
                tblHostSpecificManifest.setHostID(tblHosts.getId());
                hostSpecificManifestJpaController.create(tblHostSpecificManifest);
        }
    }


        public HostResponse updateHost(TxtHost host) {
                List<TblHostSpecificManifest> tblHostSpecificManifests = null;
                try {

                        TblHosts tblHosts = getHostByName(host.getHostName()); // datatype.Hostname
                        if (tblHosts == null) {
                                throw new ASException(ErrorCode.AS_HOST_NOT_FOUND, host.getHostName().toString());
                        }

          TblMle  biosMleId = findBiosMleForHost(host); 
          TblMle  vmmMleId = findVmmMleForHost(host); 
            

                        // need to update with the new connection string before we attempt to connect to get any updated info from host (aik cert, manifest, etc)
                        if (tblHosts.getTlsPolicyName() == null && tblHosts.getTlsPolicyName().isEmpty()) { // XXX new code to test
                                tblHosts.setTlsPolicyName("TRUST_FIRST_CERTIFICATE"); // XXX  bug #497  the TxtHost object doesn't have the ssl certificate and policy
                        }
//                        tblHosts.setTlsKeystore(null);  // XXX new code to test: it's either null or it's already set so don't change it // XXX  bug #497  the TxtHost object doesn't have the ssl certificate and policy 
                        tblHosts.setAddOnConnectionInfo(host.getAddOn_Connection_String());
                        if (host.getHostName() != null) {
                                tblHosts.setName(host.getHostName().toString());
                        }
                        if (host.getIPAddress() != null) {
                                tblHosts.setIPAddress(host.getIPAddress().toString());
                        }
                        if (host.getPort() != null) {
                                tblHosts.setPort(host.getPort());
                        }

                        HostAgentFactory factory = new HostAgentFactory();
                        HostAgent agent = factory.getHostAgent(tblHosts);
                            log.info("Getting identity.");
                                setAikForHost(tblHosts, host);
                        
                        
                            if(vmmMleId.getId().intValue() != tblHosts.getVmmMleId().getId().intValue() ){
                                log.info("VMM is updated. Update the host specific manifest");
                                // retrieve the complete manifest for  the host, includes ALL pcr's and if there is module info available it is included also.
                                PcrManifest pcrManifest = agent.getPcrManifest();  // currently Vmware has pcr+module, but in 1.2 we are adding module attestation for Intel hosts too ;   citrix would be just pcr for now i guess


                                // send the pcr manifest to a vendor-specific class in order to extract any host-specific information
                                // for vmware this is the "HostTpmCommandLineEventDetails" which is a host-specific value and must be
                                // saved into mw_host_specific _manifest  (using the MLE information obtained with getBiosAndVmm(host) above...)
//                                HostReport hostReport = new HostReport();
//                                hostReport.aik = null; // TODO should be what we get above if it's available
//                                hostReport.pcrManifest = pcrManifest;
//                                hostReport.tpmQuote = null;
//                                hostReport.variables = new HashMap<String,String>(); // for example if we know a UUID ... we would ADD IT HERE
//                                TrustPolicy hostSpecificTrustPolicy = hostTrustPolicyFactory.createHostSpecificTrustPolicy(hostReport, biosMleId, vmmMleId); // XXX TODO add the bios mle and vmm mle information to HostReport ?? only if they are needed by some policies...

                                tblHostSpecificManifests = createHostSpecificManifestRecords(vmmMleId, pcrManifest);
                            }

                        log.info("Saving Host in database");
                        tblHosts.setBiosMleId(biosMleId);
                        // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
                        // tblHosts.setUpdatedOn(new Date(System.currentTimeMillis()));
                        tblHosts.setDescription(host.getDescription());
                        tblHosts.setEmail(host.getEmail());
                        if (host.getIPAddress() != null) {
                                tblHosts.setIPAddress(host.getIPAddress().toString()); // datatype.IPAddress
                        }
                        if( host.getPort() != null ) { tblHosts.setPort(host.getPort()); }                        
                        tblHosts.setVmmMleId(vmmMleId);

			log.info("Updating Host in database");
			hostController.edit(tblHosts);
                        
                        if(tblHostSpecificManifests != null){
                            log.info("Updating Host Specific Manifest in database");
                            deleteHostSpecificManifest(tblHosts.getId());
                            createHostSpecificManifest(tblHostSpecificManifests, tblHosts);
                        }

                } catch (ASException ase) {
                        throw ase;
                } catch (CryptographyException e) {
                        throw new ASException(e, ErrorCode.AS_ENCRYPTION_ERROR, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
                } catch (Exception e) {
                        throw new ASException(e);
                }

                return new HostResponse(ErrorCode.OK);
        }

        public HostResponse deleteHost(Hostname hostName) { // datatype.Hostname

                try {
                        TblHosts tblHosts = getHostByName(hostName);
                        if (tblHosts == null) {
                                throw new ASException(ErrorCode.AS_HOST_NOT_FOUND, hostName);
                        }
                        log.info("Deleting Host from database");

                        deleteHostSpecificManifest(tblHosts.getId());

                        deleteTALogs(tblHosts.getId());

                        deleteSAMLAssertions(tblHosts);

                        new TblHostsJpaController(getEntityManagerFactory()).destroy(tblHosts.getId());
                } catch (ASException ase) {
                        throw ase;
                } catch (CryptographyException e) {
                        throw new ASException(ErrorCode.SYSTEM_ERROR, e.getCause() == null ? e.getMessage() : e.getCause().getMessage(), e);
                } catch (Exception e) {
                        throw new ASException(e);
                }
                return new HostResponse(ErrorCode.OK);
        }

        // PREMIUM FEATURE ? 
        private void deleteHostSpecificManifest(Integer hostId)
                throws NonexistentEntityException {
                TblHostSpecificManifestJpaController tblHostSpecificManifestJpaController;

                tblHostSpecificManifestJpaController = new TblHostSpecificManifestJpaController(getEntityManagerFactory());
                TblHostSpecificManifest hostSpecificManifest =
                        tblHostSpecificManifestJpaController.findByHostID(hostId);

                if (hostSpecificManifest != null) {
                        log.info("Deleting Host specific manifest.");
                        tblHostSpecificManifestJpaController.destroy(hostSpecificManifest.getId());
                }
        }

        private void deleteTALogs(Integer hostId) throws IllegalOrphanException {

                TblTaLogJpaController tblTaLogJpaController = new TblTaLogJpaController(getEntityManagerFactory());

                List<TblTaLog> taLogs = tblTaLogJpaController.findLogsByHostId(hostId, new Date());

                if (taLogs != null) {

                        for (TblTaLog taLog : taLogs) {
                                try {
                                        tblTaLogJpaController.destroy(taLog.getId());
                                } catch (NonexistentEntityException e) {
                                        log.warn("Ta Log is already deleted " + taLog.getId());
                                }
                        }
                        log.info("Deleted all the logs for the given host " + hostId);
                }

        }

        /**
         * Deletes all the SAML assertions for the specified host. This should
         * be called before deleting the host.
         *
         * @param hostId
         */
        private void deleteSAMLAssertions(TblHosts hostId) {
                TblSamlAssertionJpaController samlJpaController = new TblSamlAssertionJpaController(getEntityManagerFactory());

                List<TblSamlAssertion> hostSAMLAssertions = samlJpaController.findByHostID(hostId);

                if (hostSAMLAssertions != null) {
                        for (TblSamlAssertion hostSAML : hostSAMLAssertions) {
                                try {
                                        samlJpaController.destroy(hostSAML.getId());
                                } catch (NonexistentEntityException e) {
                                        log.warn("Ta Log is already deleted " + hostSAML.getId());
                                }
                        }
                        log.info("Deleted all the logs for the given host " + hostId);
                }
        }

	private void setAikForHost(TblHosts tblHosts, TxtHost host) {
            HostAgentFactory factory = new HostAgentFactory(); // we could call IntelHostAgentFactory but then we have to create the TlsPolicy object ourselves... the HostAgentFactory does that for us.
            HostAgent agent = factory.getHostAgent(tblHosts);
            if( agent.isAikAvailable() ) {
                if( agent.isAikCaAvailable() ) {
                    X509Certificate cert = agent.getAikCertificate();
                    try {
                        String certPem = X509Util.encodePemCertificate(cert);
                        tblHosts.setAIKCertificate(certPem);
                        tblHosts.setAikPublicKey(X509Util.encodePemPublicKey(cert.getPublicKey())); // NOTE: we are getting the public key from the cert, NOT by calling agent.getAik() ... that's to ensure that someone doesn't give us a valid certificate and then some OTHER public key that is not bound to the TPM
                        tblHosts.setAikSha1(Sha1Digest.valueOf(cert.getPublicKey().getEncoded()).toString());
                    }
                    catch(Exception e) {
                        log.error("Cannot encode AIK certificate: "+e.toString(), e);
                    }
                }
                else {
                    // XXX Stewart Citrix TODO ... probably pem-encode with RSA PUBLIC KEY header
                    PublicKey publicKey = agent.getAik();
                    String pem = X509Util.encodePemPublicKey(publicKey); 
                    tblHosts.setAIKCertificate(null);
                    tblHosts.setAikPublicKey(pem);
                    tblHosts.setAikSha1(Sha1Digest.valueOf(publicKey.getEncoded()).toString());
                }
            }
 	}

        /**
         *
         * @param host must not be null
         */
//	private void validate(TxtHost host) {
//		HashSet<String> missing = new HashSet<String>();
        // phase 1, check for required fields
		/*
         * if( host.getHostName() == null || host.getHostName().isEmpty() ) {
         * missing.add("HostName"); } if( host.getBIOS_Name() == null ||
         * host.getBIOS_Name().isEmpty() ) { missing.add("BIOS_Name"); } if(
         * host.getVMM_Name() == null || host.getVMM_Name().isEmpty() ) {
         * missing.add("VMM_Name"); } if( !missing.isEmpty() ) { throw new
         * ASException(ErrorCode.VALIDATION_ERROR,
         * "Missing "+TextUtil.join(missing)); }
         */
        // phase 2, check for conditionally required fields
        // String errorMessage = "";
        // If in case we are adding a ESX host we need to ensure that we are
        // getting the connection string
        // for the vCenter server as well.
//		log.info( "VMM Name {}", host.getVmm());

        /*
         * if (requiresConnectionString(host.getVmm().getName())) {
         * if(host.getAddOn_Connection_String() == null ||
         * host.getAddOn_Connection_String().isEmpty()) { missing.add(
         * "AddOn connection string for connecting to vCenter server for host: "
         * +host.getHostName()); } } else { if( host.getIPAddress() == null ||
         * host.getIPAddress().isEmpty() ) { missing.add("IPAddress"); } if(
         * host.getPort() == null ) { missing.add("Port"); } }
         */
//		if (!missing.isEmpty()) {
//			throw new ASException(ErrorCode.VALIDATION_ERROR, "Missing "
//					+ TextUtil.join(missing));
//		}
//	}

        /*
         * private boolean requiresConnectionString(String vmmName) { if(
         * hostname.contains("ESX") ) { return true; } return false; }
         */
        private void getBiosAndVMM(TxtHost host) {
                TblMleJpaController mleController = new TblMleJpaController(
                        getEntityManagerFactory());
                this.biosMleId = mleController.findBiosMle(host.getBios().getName(),
                        host.getBios().getVersion(), host.getBios().getOem());
                if (biosMleId == null) {
                        throw new ASException(ErrorCode.AS_BIOS_INCORRECT, host.getBios().getName(), host.getBios().getVersion());
                }
                this.vmmMleId = mleController.findVmmMle(host.getVmm().getName(), host
                        .getVmm().getVersion(), host.getVmm().getOsName(), host
                        .getVmm().getOsVersion());
                if (vmmMleId == null) {
                        throw new ASException(ErrorCode.AS_VMM_INCORRECT, host.getVmm().getName(), host.getVmm().getVersion());
                }
        }

	private TblMle findBiosMleForHost(TxtHost host) {
		
		TblMle biosMleId = mleController.findBiosMle(host.getBios().getName(),
				host.getBios().getVersion(), host.getBios().getOem());
		if (biosMleId == null) {
			throw new ASException(ErrorCode.AS_BIOS_INCORRECT, host.getBios().getName(),host.getBios().getVersion());
		}
        return biosMleId;
	}
	private TblMle findVmmMleForHost(TxtHost host) {
		TblMle vmmMleId = mleController.findVmmMle(host.getVmm().getName(), host
				.getVmm().getVersion(), host.getVmm().getOsName(), host
				.getVmm().getOsVersion());
		if (vmmMleId == null) {
			throw new ASException(ErrorCode.AS_VMM_INCORRECT, host.getVmm().getName(),host.getVmm().getVersion());
		}
        return vmmMleId;
	}

    // BUG #607 changing HashMap<String, ? extends IManifest> pcrMap to PcrManifest
	private void saveHostInDatabase(TblHosts newRecordWithTlsPolicyAndKeystore, TxtHost host, PcrManifest pcrManifest, List<TblHostSpecificManifest> tblHostSpecificManifests, TblMle biosMleId, TblMle vmmMleId) throws CryptographyException, MalformedURLException {
		
		
		
		TblHosts tblHosts = newRecordWithTlsPolicyAndKeystore; // new TblHosts();
		log.info("saveHostInDatabase with tls policy {} and keystore size {}", tblHosts.getTlsPolicyName(), tblHosts.getTlsKeystore() == null ? "null" : tblHosts.getTlsKeystore().length);
		log.error("saveHostInDatabase with tls policy {} and keystore size {}", tblHosts.getTlsPolicyName(), tblHosts.getTlsKeystore() == null ? "null" : tblHosts.getTlsKeystore().length);

		
		tblHosts.setAddOnConnectionInfo(host.getAddOn_Connection_String());
		tblHosts.setBiosMleId(biosMleId);
                // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
                // tblHosts.setCreatedOn(new Date(System.currentTimeMillis()));
                // tblHosts.setUpdatedOn(new Date(System.currentTimeMillis()));
                tblHosts.setDescription(host.getDescription());
                tblHosts.setEmail(host.getEmail());
                if (host.getIPAddress() != null) {
                        tblHosts.setIPAddress(host.getIPAddress().toString()); // datatype.IPAddress
                }
                tblHosts.setName(host.getHostName().toString()); // datatype.Hostname

                if (host.getPort() != null) {
                        tblHosts.setPort(host.getPort());
                }
		tblHosts.setVmmMleId(vmmMleId);
                
                // Bug:583: Since we have seen exception related to this in the log file, we will check for contents
                // before setting the location value.
//                if (location != null) {
//                    tblHosts.setLocation(location);
//                }

                // create the host
                log.error("COMMITING NEW HOST DO DATABASE");
                hostController.create(tblHosts);

                log.info("Save host specific manifest if any.");
                createHostSpecificManifest(tblHostSpecificManifests, tblHosts);

        }

    /*
     * It looks for a very specific event that
     * is extended into pcr 19 in vmware hosts.  So the vmware host-specific policy factory creates a TrustPolicy
     * that has that event,  and here we just convert it to a TblHostSpecificManifest record.
     * BUG #607 ... given a complete list of pcrs and module values from the host, and list of pcr's that should be used ... figures out 
     * what host-specific module values should be recorded in the database... apparently hard-coded to pcr 19
     * and vmware information... so this is a candidate for moving into VmwareHostTrustPolicyFactory,
     * and instaed of returning a "host-specific manifest" it should return a list of policies with module-included
     * or module-equals type rules.    XXX for now converting to PcrManifest but this probably still needs to be moved.
    */
	private List<TblHostSpecificManifest> createHostSpecificManifestRecords(TblMle vmmMleId, PcrManifest pcrManifest) {
		List<TblHostSpecificManifest> tblHostSpecificManifests = new ArrayList<TblHostSpecificManifest>();

		if (pcrManifest != null && pcrManifest.containsPcrEventLog(PcrIndex.PCR19) ) {
            PcrEventLog pcrEventLog = pcrManifest.getPcrEventLog(19);
			
				for (Measurement m : pcrEventLog.getEventLog()) {
				
					log.info("getHostSpecificManifest creating host specific manifest for event '"
							+ m.getInfo().get("EventName") +"' field '"+m.getLabel()+"' component '"+m.getInfo().get("ComponentName") +"'");

					
                    // we are looking for the "commandline" event specifically  (vmware)
                    if(  m.getInfo().get("EventName") != null && m.getInfo().get("EventName").equals("Vim25Api.HostTpmCommandEventDetails") ) { 
                    
                        TblModuleManifest tblModuleManifest = moduleManifestJpaController.findByMleNameEventName(vmmMleId.getId(),
                                m.getInfo().get("ComponentName"),
                                m.getInfo().get("EventName"));

                        TblHostSpecificManifest tblHostSpecificManifest = new TblHostSpecificManifest();
                        tblHostSpecificManifest.setDigestValue(m.getValue().toString());
    //					tblHostSpecificManifest.setHostID(tblHosts.getId());
                        tblHostSpecificManifest.setModuleManifestID(tblModuleManifest);
                        tblHostSpecificManifests.add(tblHostSpecificManifest);
                    }
					
				}				
				
				return tblHostSpecificManifests;
			
		} else {
			log.warn("No PCR 19 found.SO not saving host specific manifest.");
			return tblHostSpecificManifests;
		}


        }

        public HostResponse isHostRegistered(String hostnameOrAddress) {
                try {
                        TblHostsJpaController tblHostsJpaController = new TblHostsJpaController(getEntityManagerFactory());
                        TblHosts tblHosts = tblHostsJpaController.findByName(hostnameOrAddress);
                        if (tblHosts != null) {
                                return new HostResponse(ErrorCode.OK); // host name exists in
                                // database
                        }
                        tblHosts = tblHostsJpaController.findByIPAddress(hostnameOrAddress);
                        if (tblHosts != null) {
                                return new HostResponse(ErrorCode.OK); // host IP address exists in
                                // database
                        }
                        return new HostResponse(ErrorCode.AS_HOST_NOT_FOUND);
                } catch (ASException e) {
                        throw e;
                } catch (CryptographyException e) {
                        throw new ASException(e, ErrorCode.AS_ENCRYPTION_ERROR, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
                } catch (Exception e) {
                        throw new ASException(e);
                }
        }

        private void checkForDuplicate(TxtHost host) throws CryptographyException {
                TblHostsJpaController tblHostsJpaController = new TblHostsJpaController(
                        getEntityManagerFactory());
                TblHosts tblHosts = tblHostsJpaController.findByName(host.getHostName()
                        .toString()); // datatype.Hostname
                if (tblHosts != null) {
                        throw new ASException(
                                ErrorCode.AS_HOST_EXISTS,
                                host.getHostName());
                }

                // BUG #497  every host requires a connection string now, and will not have the "ip address" field anymore. 
                /*
                 if (!host.requiresConnectionString() && host.getIPAddress() != null ) {
                 tblHosts = tblHostsJpaController.findByIPAddress(host
                 .getIPAddress().toString()); // datatype.IPAddress

                 if (tblHosts != null) {
                 throw new ASException(
                 ErrorCode.AS_IPADDRESS_EXISTS,
                 host.getIPAddress());
                 }
                 }
                 */
        }

        /**
         * This is not a REST API method, it is public because it is used by
         * HostTrustBO.
         *
         * @param hostName
         * @return
         * @throws CryptographyException
         */
        public TblHosts getHostByName(Hostname hostName) throws CryptographyException { // datatype.Hostname
                TblHosts tblHosts = new TblHostsJpaController(getEntityManagerFactory())
                        .findByName(hostName.toString());
                return tblHosts;
        }
	public TblHosts getHostByAik(Sha1Digest aik) throws CryptographyException { // datatype.Hostname
		TblHosts tblHosts = new TblHostsJpaController(getEntityManagerFactory())
				.findByAikSha1(aik.toString());
		return tblHosts;
	}

        /**
         * Author: Sudhir
         *
         * Searches for the hosts using the criteria specified.
         *
         * @param searchCriteria: If in case the user has not provided any
         * search criteria, then all the hosts would be returned back to the
         * caller
         * @return
         */
        public List<TxtHostRecord> queryForHosts(String searchCriteria) {
                try {
                        TblHostsJpaController tblHostsJpaController = new TblHostsJpaController(getEntityManagerFactory());
                        List<TxtHostRecord> txtHostList = new ArrayList<TxtHostRecord>();
                        List<TblHosts> tblHostList;


                        if (searchCriteria != null && !searchCriteria.isEmpty()) {
                                tblHostList = tblHostsJpaController.findHostsByNameSearchCriteria(searchCriteria);
                        } else {
                                tblHostList = tblHostsJpaController.findTblHostsEntities();
                        }

                        if (tblHostList != null) {

                                log.info(String.format("Found [%d] host results for search criteria [%s]", tblHostList.size(), searchCriteria));

                                for (TblHosts tblHosts : tblHostList) {
                                        TxtHostRecord hostObj = createTxtHostFromDatabaseRecord(tblHosts);
                                        txtHostList.add(hostObj);
                                }
                        } else {
                                log.info(String.format("Found no hosts for search criteria [%s]", searchCriteria));
                        }

                        return txtHostList;
                } catch (ASException e) {
                        throw e;
                } catch (CryptographyException e) {
                        throw new ASException(e, ErrorCode.AS_ENCRYPTION_ERROR, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
                } catch (Exception e) {
                        throw new ASException(e);
                }

        }

        public TxtHostRecord createTxtHostFromDatabaseRecord(TblHosts tblHost) {
                TxtHostRecord hostObj = new TxtHostRecord();
                hostObj.HostName = tblHost.getName();
                hostObj.IPAddress = tblHost.getIPAddress();
                hostObj.Port = tblHost.getPort();
                hostObj.AddOn_Connection_String = tblHost.getAddOnConnectionInfo();
                hostObj.Description = tblHost.getDescription();
                hostObj.Email = tblHost.getEmail();
                hostObj.Location = tblHost.getLocation();
                hostObj.BIOS_Name = tblHost.getBiosMleId().getName();
                hostObj.BIOS_Oem = tblHost.getBiosMleId().getOemId().getName();
                hostObj.BIOS_Version = tblHost.getBiosMleId().getVersion();
                hostObj.VMM_Name = tblHost.getVmmMleId().getName();
                hostObj.VMM_Version = tblHost.getVmmMleId().getVersion();
                hostObj.VMM_OSName = tblHost.getVmmMleId().getOsId().getName();
                hostObj.VMM_OSVersion = tblHost.getVmmMleId().getOsId().getVersion();

                return hostObj;
        }
}
