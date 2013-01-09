package com.intel.mtwilson.as.business;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mountwilson.as.helper.TrustAgentSecureClient;
import com.intel.mountwilson.manifest.data.IManifest;
import com.intel.mountwilson.manifest.data.ModuleManifest;
import com.intel.mountwilson.manifest.data.PcrManifest;
import com.intel.mountwilson.manifest.data.PcrModuleManifest;
import com.intel.mtwilson.agent.vmware.VCenterHost;
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
import com.intel.mtwilson.as.helper.BaseBO;
import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.datatypes.*;
import com.vmware.vim25.HostTpmAttestationReport;
import com.vmware.vim25.HostTpmCommandEventDetails;
import com.vmware.vim25.HostTpmDigestInfo;
import com.vmware.vim25.HostTpmEventLogEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * All settings should be via setters, not via constructor, because this class may be instantiated by a factory.
 * 
 * @author dsmagadx
 */
public class HostBO extends BaseBO {

	private static final String COMMAND_LINE_MANIFEST = "/b.b00 vmbTrustedBoot=true tboot=0x0x101a000";
	private static final String LOCATION_PCR = "22";
        private Logger log = LoggerFactory.getLogger(getClass());
	private TblMle biosMleId = null;
	private TblMle vmmMleId = null;
        private byte[] dataEncryptionKey = null;
        
        public void setDataEncryptionKey(byte[] key) { dataEncryptionKey = key; }
        
	public HostResponse addHost(TxtHost host) {
            String certificate = null;
            String location = null;
            HashMap<String, ? extends IManifest> pcrMap = null;
		try {
            checkForDuplicate(host);

            getBiosAndVMM(host);

            log.info("Getting Server Identity.");
            
                        // BUG #497  setting default tls policy name and empty keystore for all new hosts. XXX TODO allow caller to provide keystore contents in pem format in the call ( in the case of the other tls policies ) or update later
                        TblHosts tblHosts = new TblHosts();
                        tblHosts.setSSLPolicy("TRUST_FIRST_CERTIFICATE");
                        tblHosts.setSSLCertificate(new byte[0]);
                        tblHosts.setName(host.getHostName().toString());
                        tblHosts.setAddOnConnectionInfo(host.getAddOn_Connection_String());

			if (canFetchAIKCertificateForHost(host.getVmm().getName())) { // datatype.Vmm
				certificate = getAIKCertificateForHost(host);
                        }
			else { // ESX host so get the location for the host and store in the
					// table

                            pcrMap = getHostPcrManifest(tblHosts, host); // BUG #497 sending both the new TblHosts record and the TxtHost object just to get the TlsPolicy into the initial call so that with the trust_first_certificate policy we will obtain the host certificate now while adding it

                            log.info("Getting location for host from VCenter");
                            location = getLocation(pcrMap);
                        }
                        log.info("Saving Host in database");

                        saveHostInDatabase(tblHosts, host, certificate, location, pcrMap);

		} catch (ASException ase) {
			throw ase;
		} 
                catch(CryptographyException e) {
                    throw new ASException(e,ErrorCode.AS_ENCRYPTION_ERROR, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
                } 
                catch (Exception e) {
			throw new ASException(e);
		}
		return new HostResponse(ErrorCode.OK);
	}

    private void createHostSpecificManifest(List<TblHostSpecificManifest> tblHostSpecificManifests, TblHosts tblHosts) {
        TblHostSpecificManifestJpaController thsmController = new TblHostSpecificManifestJpaController(getEntityManagerFactory());
        
        for(TblHostSpecificManifest tblHostSpecificManifest : tblHostSpecificManifests){
                tblHostSpecificManifest.setHostID(tblHosts.getId());
                thsmController.create(tblHostSpecificManifest);
        }
    }

	private String getLocation(HashMap<String, ? extends IManifest> pcrMap) {

		if (pcrMap != null) {// Pcr map will be null for host which dont support TPM
			
			if(pcrMap.containsKey(LOCATION_PCR))
				return new TblLocationPcrJpaController(getEntityManagerFactory())
					.findTblLocationPcrByPcrValue(((PcrManifest) pcrMap
							.get(LOCATION_PCR)).getPcrValue());
		}

		return null;
	}

        // BUG #497 adding TblHosts parameter to this call so we can send the TlsPolicy to the HostAgentFactory for making the connection
	private HashMap<String, ? extends IManifest> getHostPcrManifest(TblHosts tblHosts, TxtHost host) {
		try {

			HashMap<String, ? extends IManifest> pcrMap = getPcrModuleManifestMap(tblHosts, host);
			return pcrMap;
			
		} catch (ASException e) {
			if (!e.getErrorCode().equals(ErrorCode.AS_VMW_TPM_NOT_SUPPORTED)) {
				throw e;
			} else {
				log.info("VMWare host does not support TPM. Ignoring the error for now");
			}
			return null;
		}
	}

        
        // BUG #497 XXX TODO need to rewrite to get this from the HostAgentFactory and HostAgent interfaces
	private HashMap<String, ? extends IManifest> getPcrModuleManifestMap(
                        TblHosts tblHosts, // BUG #497 adding TblHosts parameter to this call so we can send the TlsPolicy to the HostAgentFactory for making the connection
			TxtHost host) {

            VCenterHost postProcessing = new VCenterHost() { // BUG #497 the VCenterHost class is now completely abstract and here we are providing the desired post-processing for the information obtained from vcenter

			@Override
			public HashMap<String, ? extends IManifest> processReport(String esxiVersion,
					HostTpmAttestationReport report) {
                            log.info( "ESX version :{}", esxiVersion);
				HashMap<String, PcrModuleManifest> pcrManifestMap = new HashMap<String, PcrModuleManifest>();

				for (HostTpmDigestInfo hostTpmDigestInfo : report
						.getTpmPcrValues()) {
					if (LOCATION_PCR.equals(String.valueOf(hostTpmDigestInfo
							.getPcrNumber()))) {
						String digestValue = VMwareClient.byteArrayToHexString(hostTpmDigestInfo
								.getDigestValue());

						pcrManifestMap.put(String.valueOf(hostTpmDigestInfo
								.getPcrNumber()), new PcrModuleManifest(
								hostTpmDigestInfo.getPcrNumber(), digestValue));

					} else if ("19".equals(String.valueOf(hostTpmDigestInfo
							.getPcrNumber()))) {

						PcrModuleManifest manifest = new PcrModuleManifest(
								hostTpmDigestInfo.getPcrNumber(),
								VMwareClient.byteArrayToHexString(hostTpmDigestInfo
										.getDigestValue()));

						pcrManifestMap.put(String.valueOf(hostTpmDigestInfo
								.getPcrNumber()), manifest);
						// get the command line manifest
						getModuleManifest(report, pcrManifestMap);
					}
				}

				return pcrManifestMap;
			}

			@Override
			public HashMap<String, ? extends IManifest> processDigest(String esxiVersion,
					List<HostTpmDigestInfo> htdis) {
				HashMap<String, PcrManifest> pcrMap = new HashMap<String, PcrManifest>();

				for (HostTpmDigestInfo htdi : htdis) {

					if (LOCATION_PCR
							.equals(String.valueOf(htdi.getPcrNumber()))) {
						String digest = VMwareClient.byteArrayToHexString(htdi
								.getDigestValue());
						pcrMap.put(String.valueOf(htdi.getPcrNumber()),
								new PcrManifest(htdi.getPcrNumber(), digest));

					}
				}

				return pcrMap;
			}

                        // PREMIUM FEATURE, should be moved to PremiumHostBO, but this is actually inside a VCenterHost object so ... need to discuss this one.
			private void getModuleManifest(HostTpmAttestationReport report,
					HashMap<String, PcrModuleManifest> pcrMap) {

				for (HostTpmEventLogEntry logEntry : report.getTpmEvents()) {

					if (pcrMap.containsKey(String.valueOf(logEntry
							.getPcrIndex()))) {

						PcrModuleManifest pcrModuleManifest = pcrMap.get(String
								.valueOf(logEntry.getPcrIndex()));

						if (logEntry.getEventDetails() instanceof HostTpmCommandEventDetails) {
							HostTpmCommandEventDetails commandEventDetails = (HostTpmCommandEventDetails) logEntry
									.getEventDetails();
							ModuleManifest moduleManifest = new ModuleManifest();

							if (commandEventDetails.getCommandLine() != null && commandEventDetails.getCommandLine().contains(
									"no-auto-partition")) {

								moduleManifest
										.setEventName("Vim25Api.HostTpmCommandEventDetails");
								moduleManifest.setComponentName("commandLine."
										+ getCommandLine(commandEventDetails));
								moduleManifest
										.setDigestValue(VMwareClient.byteArrayToHexString(commandEventDetails
												.getDataHash()));

								// Add to the module manifest map of the pcr
								pcrModuleManifest.getModuleManifests().put(
										moduleManifest.getMFKey(),
										moduleManifest);
							}
						}

					}

				}
			}

			private String getCommandLine(
					HostTpmCommandEventDetails commandEventDetails) {
				String commandLine = commandEventDetails.getCommandLine();
				if (commandLine != null
						&& commandLine.contains("no-auto-partition")) {
					commandLine = "";
				}

				return commandLine;
			}

		};
            
            HostAgentFactory hostAgentFactory = new HostAgentFactory();            
            HashMap<String, ? extends IManifest>  pcrMap = hostAgentFactory.getManifest(tblHosts, postProcessing);
            return pcrMap;
	}

	private boolean canFetchAIKCertificateForHost(String vmmName) {
		return (!vmmName.contains("ESX"));
	}

	public HostResponse updateHost(TxtHost host) {
                List<TblHostSpecificManifest> tblHostSpecificManifests = null;
		try {

			TblHosts tblHosts = getHostByName(host.getHostName()); // datatype.Hostname

			getBiosAndVMM(host);

			if (tblHosts == null) {
				throw new ASException(ErrorCode.AS_HOST_NOT_FOUND,host.getHostName().toString());
			}

			log.info("Getting identity.");
			if (canFetchAIKCertificateForHost(host.getVmm().getName())) { // datatype.Vmm
				String certificate = getAIKCertificateForHost(host);
				tblHosts.setAIKCertificate(certificate);
			}else { // ESX host so get the location for the host and store in the
                            if(vmmMleId.getId().intValue() != tblHosts.getVmmMleId().getId().intValue() ){
                                log.info("VMM is updated. Update the host specific manifest");
                                
                                HashMap<String, ? extends IManifest> pcrMap = getHostPcrManifest(tblHosts,host); // BUG #497 added tblHosts parameter
                                //Building objects and validating that manifests are created ahead of create of host
                                tblHostSpecificManifests = getHostSpecificManifest(pcrMap); 
                            }
                        }
                        
                        log.info("Saving Host in database");
			tblHosts.setAddOnConnectionInfo(host.getAddOn_Connection_String());
			tblHosts.setBiosMleId(biosMleId);
                        // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
			// tblHosts.setUpdatedOn(new Date(System.currentTimeMillis()));
			tblHosts.setDescription(host.getDescription());
			tblHosts.setEmail(host.getEmail());
			if (host.getIPAddress() != null)
				tblHosts.setIPAddress(host.getIPAddress().toString()); // datatype.IPAddress
			tblHosts.setName(host.getHostName().toString()); // datatype.Hostname
			tblHosts.setPort(host.getPort());
			tblHosts.setVmmMleId(vmmMleId);

			log.info("Updating Host in database");
			new TblHostsJpaController(getEntityManagerFactory(), dataEncryptionKey).edit(tblHosts);
                        
                        if(tblHostSpecificManifests != null){
                            log.info("Updating Host Specific Manifest in database");
                            deleteHostSpecificManifest(tblHosts.getId());
                            createHostSpecificManifest(tblHostSpecificManifests, tblHosts);
                        }

		} catch (ASException ase) {
			throw ase;
		} 
                catch(CryptographyException e) {
                    throw new ASException(e,ErrorCode.AS_ENCRYPTION_ERROR, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
                } 
                catch (Exception e) {
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
                        
			new TblHostsJpaController(getEntityManagerFactory(), dataEncryptionKey)
					.destroy(tblHosts.getId());
		} catch (ASException ase) {
			throw ase;
		}
                catch(CryptographyException e) {
                    throw new ASException(ErrorCode.SYSTEM_ERROR, e.getCause() == null ? e.getMessage() : e.getCause().getMessage(), e);
                } 
                catch (Exception e) {
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
		
		if(hostSpecificManifest != null){
			log.info("Deleting Host specific manifest.");
			tblHostSpecificManifestJpaController.destroy(hostSpecificManifest.getId());
		}
	}

	private void deleteTALogs(Integer hostId) throws IllegalOrphanException {
		
		TblTaLogJpaController tblTaLogJpaController = new TblTaLogJpaController(getEntityManagerFactory());
		
		List<TblTaLog> taLogs = tblTaLogJpaController.findLogsByHostId(hostId, new Date());
		
		if(taLogs != null){
			
			for(TblTaLog taLog : taLogs){
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
         * Deletes all the SAML assertions for the specified host. This should be called before deleting the host.
         * @param hostId 
         */
        private void deleteSAMLAssertions(TblHosts hostId) {
            TblSamlAssertionJpaController samlJpaController = new TblSamlAssertionJpaController(getEntityManagerFactory());
            
            List<TblSamlAssertion> hostSAMLAssertions = samlJpaController.findByHostID(hostId);
            		
            if(hostSAMLAssertions != null){
                for(TblSamlAssertion hostSAML : hostSAMLAssertions){
                        try {
                                samlJpaController.destroy(hostSAML.getId());
                        } catch (NonexistentEntityException e) {
                                log.warn("Ta Log is already deleted " + hostSAML.getId());
                        }
                }
                log.info("Deleted all the logs for the given host " + hostId);
            }	
	}

	private String getAIKCertificateForHost(TxtHost host) {
            // TODO: should use TA Helper insetad of TrustAgentSecureClient directly
		return new TrustAgentSecureClient(host.getIPAddress(), host.getPort())
				.getAIKCertificate();
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
			throw new ASException(ErrorCode.AS_BIOS_INCORRECT, host.getBios().getName(),host.getBios().getVersion());
		}
		this.vmmMleId = mleController.findVmmMle(host.getVmm().getName(), host
				.getVmm().getVersion(), host.getVmm().getOsName(), host
				.getVmm().getOsVersion());
		if (vmmMleId == null) {
			throw new ASException(ErrorCode.AS_VMM_INCORRECT, host.getVmm().getName(),host.getVmm().getVersion());
		}
	}

	private void saveHostInDatabase(TblHosts newRecordWithTlsPolicyAndKeystore, TxtHost host, String certificate,
			String location, HashMap<String, ? extends IManifest> pcrMap) throws CryptographyException {
		
		
		//Building objects and validating that manifests are created ahead of create of host
		List<TblHostSpecificManifest> tblHostSpecificManifests = getHostSpecificManifest(pcrMap); 
		
		
		TblHosts tblHosts = newRecordWithTlsPolicyAndKeystore; // new TblHosts();
		

		TblHostsJpaController hostController = new TblHostsJpaController(
				getEntityManagerFactory(), dataEncryptionKey);
		tblHosts.setAddOnConnectionInfo(host.getAddOn_Connection_String());
		tblHosts.setBiosMleId(biosMleId);
                // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
		// tblHosts.setCreatedOn(new Date(System.currentTimeMillis()));
		// tblHosts.setUpdatedOn(new Date(System.currentTimeMillis()));
		tblHosts.setDescription(host.getDescription());
		tblHosts.setEmail(host.getEmail());
		if (host.getIPAddress() != null)
			tblHosts.setIPAddress(host.getIPAddress().toString()); // datatype.IPAddress
		tblHosts.setName(host.getHostName().toString()); // datatype.Hostname

		if (host.getPort() != null)
			tblHosts.setPort(host.getPort());
		tblHosts.setVmmMleId(vmmMleId);
		tblHosts.setAIKCertificate(certificate);
		tblHosts.setLocation(location);
		// create the host
		hostController.create(tblHosts);

		log.info("Save host specific manifest if any.");
                createHostSpecificManifest(tblHostSpecificManifests, tblHosts);

	}

	// PREMIUM FEATURE ?
	private List<TblHostSpecificManifest> getHostSpecificManifest(HashMap<String, ? extends IManifest> pcrMap) {
		List<TblHostSpecificManifest> tblHostSpecificManifests = new ArrayList<TblHostSpecificManifest>();

		if (pcrMap != null && pcrMap.containsKey("19")) {

			PcrModuleManifest pcrModuleManifest = (PcrModuleManifest) pcrMap
					.get("19");
			
				for (ModuleManifest moduleManifest : pcrModuleManifest.getModuleManifests().values()) {
				
					log.info("Creating host specific manifest for "
							+ moduleManifest.getEventName());

					
					TblModuleManifest tblModuleManifest = new TblModuleManifestJpaController(
							getEntityManagerFactory()).findByMleNameEventName(this.vmmMleId.getId(),
							moduleManifest.getComponentName(),
							moduleManifest.getEventName());

					TblHostSpecificManifest tblHostSpecificManifest = new TblHostSpecificManifest();
					tblHostSpecificManifest.setDigestValue(moduleManifest.getDigestValue());
//					tblHostSpecificManifest.setHostID(tblHosts.getId());
					tblHostSpecificManifest.setModuleManifestID(tblModuleManifest);
					tblHostSpecificManifests.add(tblHostSpecificManifest);
					
				}				
				
				return tblHostSpecificManifests;
			
		} else {
			log.warn("No PCR 19 found.SO not saving host specific manifest.");
			return tblHostSpecificManifests;
		}

	}
	


	public HostResponse isHostRegistered(String hostnameOrAddress) {
            try {
		TblHostsJpaController tblHostsJpaController = new TblHostsJpaController(
				getEntityManagerFactory(), dataEncryptionKey);
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
            }
            catch (ASException e) {
                throw e;
            }
            catch(CryptographyException e) {
                throw new ASException(e,ErrorCode.AS_ENCRYPTION_ERROR, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
            }
            catch(Exception e) {
                throw new ASException( e);
            }
	}

	private void checkForDuplicate(TxtHost host) throws CryptographyException {
		TblHostsJpaController tblHostsJpaController = new TblHostsJpaController(
				getEntityManagerFactory(), dataEncryptionKey);
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
         * This is not a REST API method, it is public because it is used by HostTrustBO.
         * @param hostName
         * @return
         * @throws CryptographyException 
         */
	public TblHosts getHostByName(Hostname hostName) throws CryptographyException { // datatype.Hostname
		TblHosts tblHosts = new TblHostsJpaController(getEntityManagerFactory(), dataEncryptionKey)
				.findByName(hostName.toString());
		return tblHosts;
	}

        /**
         * Author: Sudhir
         * 
         * Searches for the hosts using the criteria specified.
         * 
         * @param searchCriteria: If in case the user has not provided any search criteria, then all the hosts
         * would be returned back to the caller
         * @return 
         */
        public List<TxtHostRecord> queryForHosts(String searchCriteria) {
            try {
                TblHostsJpaController tblHostsJpaController = new TblHostsJpaController(getEntityManagerFactory(), dataEncryptionKey);
                List<TxtHostRecord> txtHostList = new ArrayList<TxtHostRecord>();
                List<TblHosts> tblHostList;

                
                if (searchCriteria != null && !searchCriteria.isEmpty())
                    tblHostList = tblHostsJpaController.findHostsByNameSearchCriteria(searchCriteria);
                else
                    tblHostList = tblHostsJpaController.findTblHostsEntities();

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
            }
            catch(CryptographyException e) {
                throw new ASException(e,ErrorCode.AS_ENCRYPTION_ERROR, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
            } 
            catch (Exception e) {
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

