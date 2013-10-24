/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.business;

import com.intel.mtwilson.atag.model.X509AttributeCertificate;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.data.MwAssetTagCertificate;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.helper.BaseBO;
import com.intel.mtwilson.crypto.X509Util;
import com.intel.mtwilson.datatypes.AssetTagCertAssociateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertRevokeRequest;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.Vendor;
import com.intel.mtwilson.jpa.PersistenceManager;
import com.intel.mtwilson.util.ResourceFinder;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class AssetTagCertBO extends BaseBO{
    
    private Logger log = LoggerFactory.getLogger(getClass());

    public AssetTagCertBO() {
    }

    public AssetTagCertBO(PersistenceManager pm) {
        super(pm);
    }
    
    /**
     * This functions stores a new asset tag certificate that was provisioned by the Asset tag
     * provisioning service for a host.This certificate would be associated to the host for
     * which it was provisioned only when that host gets registered with Mt.Wilson
     * @param atagObj
     * @return 
     */
    public boolean importAssetTagCertificate(AssetTagCertCreateRequest atagObj) {
        boolean result = false;
        X509AttributeCertificate x509AttrCert;
        
        try {
            try {
                x509AttrCert = X509AttributeCertificate.valueOf(atagObj.getCertificate());
            } catch (IllegalArgumentException ce) {
                log.error("Error during retrieval of a new asset tag certificate. Error Details - {}.", ce.getMessage());
                throw new ASException(ce, ErrorCode.AS_INVALID_ASSET_TAG_CERTIFICATE, ce.getMessage());
            }            
            
            MwAssetTagCertificate atagCert = new MwAssetTagCertificate();
            atagCert.setCertificate(atagObj.getCertificate());
            atagCert.setUuid(x509AttrCert.getSubject());
            atagCert.setNotAfter(x509AttrCert.getNotAfter());
            atagCert.setNotBefore(x509AttrCert.getNotBefore());
            atagCert.setRevoked(false);
            atagCert.setSHA1Hash(Sha1Digest.digestOf(atagObj.getCertificate()).toByteArray());
            //atagCert.setSHA256Hash(Sha256Digest.digestOf(atagObj.getCertificate()).toByteArray()); // not used with TPM 1.2
            
            // We are just writing some default value here, which would be changed when the host would be mapped to this
            // certificate.
            //atagCert.setPCREvent(Sha1Digest.digestOf(atagCert.getSHA1Hash()).toByteArray());
            Sha1Digest sha1D = Sha1Digest.digestOf(atagObj.getCertificate());
            Sha1Digest expectedPcr = Sha1Digest.ZERO.extend( Sha1Digest.digestOf( sha1D.toBase64().getBytes() ) );
            atagCert.setPCREvent(expectedPcr.toByteArray() );

            log.debug("assetTag writing cert to DB");
            My.jpa().mwAssetTagCertificate().create(atagCert);
            result = true;
            
        } catch (ASException ase) {
            log.error("Error during creation of a new asset tag certificate. Error Details - {}:{}.", ase.getErrorCode(), ase.getErrorMessage());
            throw ase;
        } catch (Exception ex) {
            log.error("Unexpected error during creation of a new asset tag certificate. Error Details - {}.", ex.getMessage());
            throw new ASException(ex);
        }
        return result;       
    }

    /**
     * This function would be used to associate a asset tag certificate with the host for which it is 
     * provisioned for.
     * @param atagObj
     * @return 
     */
    public boolean mapAssetTagCertToHost(AssetTagCertAssociateRequest atagObj) {
        boolean result = false;
        Sha1Digest expectedHash = null;
        
        try {
            My.initDataEncryptionKey(); // needed for connection string decryption
            // Find the asset tag certificate for the specified Sha256Hash value
            if (atagObj.getSha1OfAssetCert() != null) {
                List<MwAssetTagCertificate> atagCerts = My.jpa().mwAssetTagCertificate().findAssetTagCertificateBySha1Hash(atagObj.getSha1OfAssetCert());
                // below code is for debugging.. we will delete it later.
                // List<MwAssetTagCertificate> atagCerts = My.jpa().mwAssetTagCertificate().findAssetTagCertificatesByHostUUID("494cb5dc-a3e1-4e46-9b52-e694349b1654");
                if (atagCerts.isEmpty() || atagCerts.size() > 1) {
                    log.error("mapAssetTagCertToHost : Either the asset tag certificate does not exist or there were multiple matches for the specified hash.");
                    throw new ASException(ErrorCode.AS_INVALID_ASSET_TAG_CERTIFICATE_HASH);
                } else {
                    // Now that we have the asset tag identified, let us update the entry with the host ID for which it has
                    // to be associated.
                    MwAssetTagCertificate atagCert = atagCerts.get(0);
                    atagCert.setHostID(atagObj.getHostID());
                    
                    // Now that the mapping is done, we need to calculate what the expected PCR value should be and put it in
                    // the PCREvent column. Since this is host specific, we need to check the host type and accordingly update
                    TblHosts hostObj = My.jpa().mwHosts().findTblHosts(atagObj.getHostID());
                    ConnectionString cs = new ConnectionString(hostObj.getAddOnConnectionInfo());
                    if (cs.getVendor() == Vendor.CITRIX) {
                        // Citrix stores the SHA1 digest value as such in the NVRAM
                        Sha1Digest tag = Sha1Digest.digestOf(atagCert.getCertificate());
                        log.debug("mapAssetTagCertToHost : Sha1 Hash of the certificate with UUID {} is {}.", tag.toString());
                        
                        // When Citrix code reads NVRAM, it reads it as string and then calculates the SHA1 has of it
                        Sha1Digest citrixInput = Sha1Digest.digestOf(tag.toBase64().getBytes()); // Sha1Digest.digestOf(tag.toString().getBytes("UTF-8"));
                        log.debug("mapAssetTagCertToHost : Sha1 of Sha1 Hash of the certificate with UUID {} is {}.", citrixInput.toString());
                        
                        // It then appends a 20 byte zero array to the SHA1 of SHA1 hash for extending into PCR 22
                        //byte[] destination = new byte[Sha1Digest.ZERO.toByteArray().length + citrixInput.toByteArray().length];                   
                        //System.arraycopy(Sha1Digest.ZERO.toByteArray(), 0, destination, 0, Sha1Digest.ZERO.toByteArray().length);                     
                        //System.arraycopy(citrixInput.toByteArray(), 0, destination, Sha1Digest.ZERO.toByteArray().length, citrixInput.toByteArray().length); 
                        
                        // Final value that is written into PCR 22 is the SHA1 of the zero appended value
                        expectedHash =Sha1Digest.ZERO.extend(citrixInput);
                        log.debug("mapAssetTagCertToHost : Final expected PCR for the certificate with UUID {} is {}.", expectedHash.toString());
                        
                    } else if (cs.getVendor() == Vendor.VMWARE) {
                        
                        // TODO : Need to implement how VMware calculates PCR 22
                    } else {
                        // Default open source
                        // TODO : Need to implement how VMware calculates PCR 22
                    }
                    
                    atagCert.setPCREvent(expectedHash.toByteArray());
                    My.jpa().mwAssetTagCertificate().edit(atagCert);
                    
                    result = true;
                }
            } else {
                log.error("Sha256Hash for the asset tag is not specified.");
                throw new ASException(ErrorCode.AS_INVALID_ASSET_TAG_CERTIFICATE_HASH);
            }            
        } catch (ASException ase) {
            log.error("Error during mapping of host to the asset tag certificate. Error Details - {}:{}.", ase.getErrorCode(), ase.getErrorMessage());
            throw ase;
        } catch (Exception ex) {
            log.error("Unexpected error during mapping of host to the asset tag certificate. Error Details - {}.", ex.getMessage());
            throw new ASException(ex);
        }
        return result;       
    }
   
    /**
     * This function removes the mapping between the host and the asset tag certificate. This needs to be 
     * instantiated when ever the host is deleted from Mt.Wilson.
     * 
     * For removing the mapping, the user need not specify the sha256Hash value. Only the hostID would be 
     * enough.
     * 
     * @param atagObj
     * @return 
     */
    public boolean unmapAssetTagCertFromHost(AssetTagCertAssociateRequest atagObj) {
        boolean result = false;
        
        try {
            // Find the asset tag certificate for the specified Sha256Hash value
            if (atagObj.getHostID() != 0) {
                List<MwAssetTagCertificate> atagCerts = My.jpa().mwAssetTagCertificate().findAssetTagCertificatesByHostID(atagObj.getHostID());
                if (atagCerts.isEmpty()) {
                    // There is nothing to unmap. So, we will just return back success
                    log.info("The host is currently not mapped to any asset tag certificate. So, nothing to unmap.");
                    return true;
                } else {
                    // Now that we have the asset tag identified, let us remove the host mapping
                    // to be associated.
                    for (MwAssetTagCertificate atagTempCert : atagCerts){
                        if (validateAssetTagCert(atagTempCert)) {
                            atagTempCert.setHostID(null);
                            My.jpa().mwAssetTagCertificate().edit(atagTempCert);
                            return true;
                        }
                    }
                }
            } else {
                log.error("Host specified for the asset tag unmap request is not valid.");
                throw new ASException(ErrorCode.AS_HOST_SPECIFIED_IS_CURRENTLY_NOT_MAPPED_TO_ASSET_TAG_CERTIFICATE);
            }            
        } catch (ASException ase) {
            log.error("Error during unmapping of the host from asset tag certificate. Error Details - {}:{}.", ase.getErrorCode(), ase.getErrorMessage());
            throw ase;
        } catch (Exception ex) {
            log.error("Unexpected error during unmapping of the host from asset tag certificate. Error Details - {}.", ex.getMessage());
            throw new ASException(ex);
        }
        return result;       
    }

    /**
     * Updates the asset tag certificate entry and sets the revoked flag to true so that this
     * asset tag certificate will not be considered during attestation of the asset tag.
     * @param atagObj
     * @return 
     */
    public boolean revokeAssetTagCertificate(AssetTagCertRevokeRequest atagObj) {
        boolean result = false;
        
        try {
            // Find the asset tag certificate for the specified Sha256Hash value
            if (atagObj.getSha1fAssetCert() != null) {
                List<MwAssetTagCertificate> atagCerts = My.jpa().mwAssetTagCertificate().findAssetTagCertificateBySha1Hash(atagObj.getSha1fAssetCert());
                if (atagCerts.isEmpty() || atagCerts.size() > 1) {
                    log.error("Either the asset tag certificate does not exist or there were multiple matches for the specified hash.");
                    throw new ASException(ErrorCode.AS_INVALID_ASSET_TAG_CERTIFICATE_HASH);
                } else {
                    // Now that we have the asset tag identified, set the revoked flag to true.
                    MwAssetTagCertificate atagCert = atagCerts.get(0);
                    atagCert.setRevoked(true);
                    My.jpa().mwAssetTagCertificate().edit(atagCert);
                    result = true;
                }
            } else {
                log.error("Sha256Hash for the asset tag is not specified.");
                throw new ASException(ErrorCode.AS_INVALID_ASSET_TAG_CERTIFICATE_HASH);
            }            
        } catch (ASException ase) {
            log.error("Error during revocation of the asset tag certificate. Error Details - {}:{}.", ase.getErrorCode(), ase.getErrorMessage());
            throw ase;
        } catch (Exception ex) {
            log.error("Unexpected error during revocation of the new asset tag certificate. Error Details - {}.", ex.getMessage());
            throw new ASException(ex);
        }
        return result;       
    }

    /**
     * Finds a valid asset tag certificate for the specified host.
     * @param uuid
     * @return 
     */
    public MwAssetTagCertificate findValidAssetTagCertForHost(String uuid){
        uuid = uuid.replace("\n", "");
        MwAssetTagCertificate atagCert = null;

        try {
            // Find the asset tag certificates for the specified UUID of the host. Not that this might return back multiple
            // values. We need to evaluate each of the certificates to make sure that they are valid
            if (uuid != null && !uuid.isEmpty()) {
                List<MwAssetTagCertificate> atagCerts = My.jpa().mwAssetTagCertificate().findAssetTagCertificatesByHostUUID(uuid);
                if (atagCerts.isEmpty()) {
                    log.info("Asset tag certificate has not been provisioned for the host with UUID : {}.", uuid);
                    return null;
                } else {
                    // For each of the asset tag certs that are returned back, we need to validate the certificate first.
                    for (MwAssetTagCertificate atagTempCert : atagCerts){
                        if (validateAssetTagCert(atagTempCert)) {
                            log.debug("Valid asset tag certificate found for host with UUID {}.", uuid);
                            return atagTempCert;
                        }
                    }
                    log.info("No valid asset tag certificate found for host with UUID {}.", uuid);
                }
            } else {
                log.error("UUID specified for the host is not valid.");
                throw new ASException(ErrorCode.AS_HOST_NOT_FOUND);
            }            
        } catch (ASException ase) {
            log.error("Error during querying of valid asset tag certificate. Error Details - {}:{}.", ase.getErrorCode(), ase.getErrorMessage());
            throw ase;
        } catch (Exception ex) {
            log.error("Unexpected error during querying of valid asset tag certificate. Error Details - {}.", ex.getMessage());
            throw new ASException(ex);
        }
        
        return atagCert;
    }
    
    public MwAssetTagCertificate findValidAssetTagCertForHost(Integer hostID){
        MwAssetTagCertificate atagCert = null;

        try {
            // Find the asset tag certificates for the specified UUID of the host. Not that this might return back multiple
            // values. We need to evaluate each of the certificates to make sure that they are valid
            if (hostID != 0) {
                List<MwAssetTagCertificate> atagCerts = My.jpa().mwAssetTagCertificate().findAssetTagCertificatesByHostID(hostID);
                if (atagCerts.isEmpty()) {
                    log.info("Asset tag certificate has not been provisioned for the host with ID : {}.", hostID);
                    return null;
                } else {
                    // For each of the asset tag certs that are returned back, we need to validate the certificate first.
                    // Ideally there should be only one that is valid.
                    for (MwAssetTagCertificate atagTempCert : atagCerts){
                        if (validateAssetTagCert(atagTempCert)) {
                            log.debug("Valid asset tag certificate found for host with ID {}.", hostID);
                            return atagTempCert;
                        }
                    }
                    log.info("No valid asset tag certificate found for host with ID {}.", hostID);
                }
            } else {
                log.error("ID specified for the host is not valid.");
                throw new ASException(ErrorCode.AS_HOST_NOT_FOUND);
            }            
        } catch (ASException ase) {
            log.error("Error during querying of valid asset tag certificate using host ID. Error Details - {}:{}.", ase.getErrorCode(), ase.getErrorMessage());
            throw ase;
        } catch (Exception ex) {
            log.error("Unexpected error during querying of valid asset tag certificate using host ID. Error Details - {}.", ex.getMessage());
            throw new ASException(ex);
        }
        
        return atagCert;
    }

    /**
     * Validates the asset tag certificate and returns back true/false accordingly.
     * 
     * @param atagObj
     * @return 
     */
    private boolean validateAssetTagCert(MwAssetTagCertificate atagObj){
        boolean isValid = false;
        
        try {
            // First let us verify if the revoked flag is set
            if (atagObj.getRevoked() == true)
                return false;
            
            // X509AttributeCertificate provides a helper function that validates both the dates and the signature.
            // For that we need to first get the CA certificate that signed the Attribute Certificate. We need to
            // extract this from the PEM file list and pass it to the helper function
            X509AttributeCertificate atagAttrCertForHost = X509AttributeCertificate.valueOf(atagObj.getCertificate());
            
            List<X509Certificate> atagCaCerts = null;
            try {
                InputStream atagCaIn = new FileInputStream(ResourceFinder.getFile("AssetTagCA.pem")); 
                //InputStream atagCaIn = new FileInputStream(new File("c:/development/AssetTagCA.pem")); 
                atagCaCerts = X509Util.decodePemCertificates(IOUtils.toString(atagCaIn));
                IOUtils.closeQuietly(atagCaIn);
                log.debug("Added {} certificates from AssetTagCA.pem", atagCaCerts.size());
            }
            catch(Exception ex) {
                log.error("Error loading the Asset Tag pem file to extract the CA certificate(s).");
            }
            
            // The below isValid function verifies both the signature and the dates.
            for (X509Certificate atagCACert : atagCaCerts) {
                if (atagAttrCertForHost.isValid(atagCACert))
                    return true;
            }
            
        } catch (Exception ex) {
            throw new ASException (ex);
        }
                
        return isValid;        
    }
}
