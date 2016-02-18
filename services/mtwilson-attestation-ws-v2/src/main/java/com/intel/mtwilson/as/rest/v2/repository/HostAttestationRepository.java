/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.rest.v2.model.HostAttestation;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationFilterCriteria;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblSamlAssertion;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationLocator;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.model.Nonce;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.Date;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author ssbangal
 */
public class HostAttestationRepository implements DocumentRepository<HostAttestation, HostAttestationCollection, HostAttestationFilterCriteria, HostAttestationLocator> {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostAttestationRepository.class);
    
    private static final int DEFAULT_CACHE_VALIDITY_SECS = 3600;
    private static final int CACHE_VALIDITY_SECS = ASConfig.getConfiguration().getInt("saml.validity.seconds", DEFAULT_CACHE_VALIDITY_SECS);
        
    @Override
    @RequiresPermissions("host_attestations:search")    
    public HostAttestationCollection search(HostAttestationFilterCriteria criteria) {
        log.debug("HostAttestation:Search - Got request to search for host attestations.");        
        HostAttestationCollection hostAttestationCollection = new HostAttestationCollection();
        try {
            if (criteria.id != null) {
                TblSamlAssertion tblSamlAssertion = My.jpa().mwSamlAssertion().findByAssertionUuid(criteria.id.toString());
                TblHosts tblHosts = My.jpa().mwHosts().findHostById(tblSamlAssertion.getHostId().getId());
                if (tblHosts != null) {
                    hostAttestationCollection.getHostAttestations().add(new HostTrustBO().buildHostAttestation(tblHosts, tblSamlAssertion));
                }
            } else {
                TblHosts tblHosts;
                if (criteria.hostUuid != null) {
                    tblHosts = My.jpa().mwHosts().findHostByUuid(criteria.hostUuid.toString());
                } else if (criteria.aikSha1 != null && !criteria.aikSha1.isEmpty()) {
                    tblHosts = My.jpa().mwHosts().findByAikSha1(criteria.aikSha1);
                } else if ( criteria.aikPublicKeySha1 != null && !criteria.aikPublicKeySha1.isEmpty()) {
                    tblHosts = My.jpa().mwHosts().findByAikPublicKeySha1(criteria.aikPublicKeySha1);
                } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                    tblHosts = My.jpa().mwHosts().findByName(criteria.nameEqualTo);
                } else {
                    tblHosts = null;  // no condition specified
                }
                
                if (tblHosts != null) {
                    if (criteria.numberOfDays == 0 && criteria.fromDate == null) {
                        log.debug("HostAttestation:Search - No additional criteria are specified. Retrieving the default valid SAML assertions.");
                        List<TblSamlAssertion> tblSamlAssertionList = My.jpa().mwSamlAssertion().findListByHostAndExpiry(tblHosts.getName());
                        if (tblSamlAssertionList != null && !tblSamlAssertionList.isEmpty()) {
                            for (TblSamlAssertion tblSamlAssertion : tblSamlAssertionList) {
                                hostAttestationCollection.getHostAttestations().add(new HostTrustBO().buildHostAttestation(tblHosts, tblSamlAssertion));
                            }
                        }
                    } else {
                        log.debug("HostAttestation:Search - Additional criteria are specified.");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Calendar cal = Calendar.getInstance();
                        Date toDate, fromDate;
                        
                        if (criteria.numberOfDays != 0) {
                            log.debug("HostAttestation:Search - Number of days criteria is specified with value {}.", criteria.numberOfDays);
                            // calculate from and to dates
                            toDate = new Date(); // Get the current date and time
                            cal.setTime(toDate);
                            toDate = dateFormat.parse(dateFormat.format(cal.getTime()));
                            // To get the fromDate, we substract the number of days fromm the current date.
                            cal.add(Calendar.DATE, -(criteria.numberOfDays)); 
                            fromDate = dateFormat.parse(dateFormat.format(cal.getTime()));
                        } else {
                            if (criteria.fromDate != null && !criteria.fromDate.isEmpty() && criteria.toDate != null && !criteria.toDate.isEmpty()) {
                                log.debug("HostAttestation:Search - Dates are specified for the search criteria with values {} - {}.", criteria.fromDate, criteria.toDate);
                                Iso8601Date fromIso8601Date = Iso8601Date.valueOf(criteria.fromDate);
                                cal.setTime(fromIso8601Date); // This would set the time to ex:2015-05-30 00:00:00
                                fromDate = dateFormat.parse(dateFormat.format(cal.getTime()));
                            
                                Iso8601Date toIso8601Date = Iso8601Date.valueOf(criteria.toDate);
                                cal.setTime(toIso8601Date);
                                toDate = dateFormat.parse(dateFormat.format(cal.getTime()));
                            } else {
                                String errorMsg = "HostAttestation:Search - Invalid options specified for attestation search.";
                                log.error(errorMsg);
                                throw new Exception(errorMsg);
                            }
                        }
                        log.debug("HostAttestation:Search - Calculated the date values {} - {}.", dateFormat.format(fromDate), dateFormat.format(toDate));
                        List<TblSamlAssertion> tblSamlAssertionList = My.jpa().mwSamlAssertion().getListByDate(tblHosts.getName(), fromDate, toDate);
                        if (tblSamlAssertionList != null && !tblSamlAssertionList.isEmpty()) {
                            for (TblSamlAssertion tblSamlAssertion : tblSamlAssertionList) {
                                hostAttestationCollection.getHostAttestations().add(new HostTrustBO().buildHostAttestation(tblHosts, tblSamlAssertion));
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("HostAttestation:Search - Error during retrieval of host attestation status from cache.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("HostAttestation:Search - Returning back {} of results.", hostAttestationCollection.getHostAttestations().size());                
        return hostAttestationCollection;
    }

    @Override
    @RequiresPermissions("host_attestations:retrieve")    
    public HostAttestation retrieve(HostAttestationLocator locator) {
        if (locator == null || locator.id == null) { return null;}
        log.debug("HostAttestation:Retrieve - Got request to retrieve the host attestation with id {}.", locator.id.toString());        
        String id = locator.id.toString();
        try {
            TblSamlAssertion tblSamlAssertion = My.jpa().mwSamlAssertion().findByAssertionUuid(id);
            TblHosts tblHosts = My.jpa().mwHosts().findHostById(tblSamlAssertion.getHostId().getId());
            if (tblHosts != null) {   //no need to check tblSamlAssertion; can't be null
                log.debug("HostAttestation:Retrieve - Retrieved the details from mw_hosts and mw_saml_assertion for host with id {}.", tblHosts.getId());
                return new HostTrustBO().buildHostAttestation(tblHosts, tblSamlAssertion);
            }
        } catch (IOException | CryptographyException ex) {
            log.error("HostAttestation:Retrieve - Error during retrieval of host attestation status from cache.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }
        
    @Override
    @RequiresPermissions("host_attestations:store")    
    public void store(HostAttestation item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_attestations:create")
    public void create(HostAttestation item) {
        if (item.getId() == null) {
            item.setId(new UUID());
        }
        
        log.debug("HostAttestation:Create - Got request to create host attestation.");  //with id {}.", item.getHostUuid());
        HostAttestationLocator locator = new HostAttestationLocator();
        locator.id = item.getId();

        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            TblHosts obj;
            if (item.getHostUuid() != null) {
                obj = jpaController.findHostByUuid(item.getHostUuid());
                if (obj == null) {
                    log.error("Host specified with id {} is not valid.", item.getHostUuid());
                    throw new RepositoryInvalidInputException();
                }
            } else if (item.getAikSha1() != null && !item.getAikSha1().isEmpty()) {
                obj = jpaController.findByAikSha1(item.getAikSha1());
                if (obj == null) {
                    log.error("Host specified with aik sha1 {} is not valid.", item.getAikSha1());
                    throw new RepositoryInvalidInputException();
                }
            } else if (item.getAikPublicKeySha1() != null && !item.getAikPublicKeySha1().isEmpty()) {
                obj = jpaController.findByAikPublicKeySha1(item.getAikPublicKeySha1());
                if (obj == null) {
                    log.error("Host specified with aik public key sha1 {} is not valid.", item.getAikPublicKeySha1());
                    throw new RepositoryInvalidInputException();
                }
            } else if (item.getHostName() != null && !item.getHostName().isEmpty()) {
                obj = jpaController.findByName(item.getHostName());
                if (obj == null) {
                    log.error("Host specified with name {} is not valid.", item.getHostName());
                    throw new RepositoryInvalidInputException();
                }
            } else {
                log.error("HostAttestation:Create - Invalid input specified. Must specify Host UUID, AIK SHA1, or Host Name.");
                throw new RepositoryInvalidInputException(locator);
            }
            String challengeHex = item.getChallenge();
            HostAttestation hostAttestation;
            if( challengeHex == null || challengeHex.isEmpty() ) {
                hostAttestation = new HostTrustBO().getTrustWithSaml(obj, obj.getName(), item.getId().toString(), true);
            }
            else {
                if( !Digest.sha1().isValidHex(challengeHex) ) {
                    throw new RepositoryCreateException("Invalid challenge");
                }
                Nonce challenge = new Nonce(Digest.sha1().valueHex(challengeHex).getBytes());
                hostAttestation = new HostTrustBO().getTrustWithSaml(obj, obj.getName(), item.getId().toString(), true, challenge);
            }
            // issue #4978 use specified nonce, if available
            item.setAikSha1(hostAttestation.getAikSha1());
            item.setChallenge(hostAttestation.getChallenge());
            item.setCreatedOn(hostAttestation.getCreatedOn());
            item.setEtag(hostAttestation.getEtag());
            item.setHostName(hostAttestation.getHostName());
            item.setHostTrustResponse(hostAttestation.getHostTrustResponse());
            item.setHostUuid(hostAttestation.getHostUuid());
            item.setId(hostAttestation.getId());
            item.setModifiedOn(hostAttestation.getModifiedOn());
            item.setSaml(hostAttestation.getSaml());
            item.setTrustReport(hostAttestation.getTrustReport());
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Error during remote attestation", ex);
            throw new RepositoryCreateException(ex);
        }
    }

    @Override
    @RequiresPermissions("host_attestations:delete")    
    public void delete(HostAttestationLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_attestations:delete")    
    public void delete(HostAttestationFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
//    private HostAttestation convert(TblTaLog obj, String hostName) {
//        HostAttestation convObj = new HostAttestation();
//        convObj.setId(UUID.valueOf(obj.getUuid_hex()));
//        convObj.setHostUuid(obj.getHost_uuid_hex());
//        convObj.setHostName(hostName);
//        convObj.setHostTrustResponse(new HostTrustResponse(new Hostname(hostName), getHostTrustStatusObj(obj)));
//        return convObj;
//    }

//    private HostTrustStatus getHostTrustStatusObj(TblTaLog tblTaLog) {
//        HostTrustStatus hostTrustStatus = new HostTrustStatus();
//        
//        String[] parts = tblTaLog.getError().split(",");
//        
//        for(String part : parts){
//            String[] subparts = part.split(":");
//            if(subparts[0].equalsIgnoreCase("BIOS")){
//                hostTrustStatus.bios = (Integer.valueOf(subparts[1]) != 0);
//            }else{
//                hostTrustStatus.vmm = (Integer.valueOf(subparts[1]) != 0);
//            }
//        }
//        return hostTrustStatus;
//    }    

}
