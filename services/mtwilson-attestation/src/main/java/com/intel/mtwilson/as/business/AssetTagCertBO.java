/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.business;

import com.intel.mtwilson.tag.model.X509AttributeCertificate;
import com.intel.mtwilson.datatypes.TagDataType;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.ApacheHttpClient;
import com.intel.mtwilson.My;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.api.ApiResponse;
import com.intel.mtwilson.as.data.MwAssetTagCertificate;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.datatypes.AssetTagCertAssociateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertRevokeRequest;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.security.http.apache.ApacheBasicHttpAuthorization;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.List;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.commons.io.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import java.security.cert.CertificateException;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author ssbangal
 */
public class AssetTagCertBO {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AssetTagCertBO.class);

    public AssetTagCertBO() {
    }

//    public AssetTagCertBO(PersistenceManager pm) {
//        super(pm);
//    }
    
    /**
     * This functions stores a new asset tag certificate that was provisioned by the Asset tag
     * provisioning service for a host.This certificate would be associated to the host for
     * which it was provisioned only when that host gets registered with Mt.Wilson
     * @param atagObj
     * @return 
     */
    public boolean importAssetTagCertificate(AssetTagCertCreateRequest atagObj, String uuid) {
        boolean result;
        X509AttributeCertificate x509AttrCert;
        
        try {
            try {
                x509AttrCert = X509AttributeCertificate.valueOf(atagObj.getCertificate());
            } catch (IllegalArgumentException ce) {
                log.error("Error during retrieval of a new asset tag certificate. Error Details - {}.", ce.getMessage());
                throw new ASException(ce, ErrorCode.AS_INVALID_ASSET_TAG_CERTIFICATE, ce.getMessage());
            }            
            
            MwAssetTagCertificate atagCert = new MwAssetTagCertificate();
            if (uuid != null && !uuid.isEmpty())
                atagCert.setUuid_hex(uuid);
            else
                atagCert.setUuid_hex(new UUID().toString());
            atagCert.setCertificate(atagObj.getCertificate());
            atagCert.setUuid(x509AttrCert.getSubject().toLowerCase());
            atagCert.setNotAfter(x509AttrCert.getNotAfter());
            atagCert.setNotBefore(x509AttrCert.getNotBefore());
            atagCert.setRevoked(false);
            //atagCert.setSHA1Hash(Sha1Digest.digestOf(atagObj.getCertificate()).toByteArray());
            atagCert.setSHA1Hash(Sha1Digest.digestOf(x509AttrCert.getEncoded()).toByteArray());
            log.debug("Certificate creation time is {}", x509AttrCert.getSerialNumber());
            log.debug("Certificate SHA1 is {}", Sha1Digest.digestOf(x509AttrCert.getEncoded()).toHexString());
            atagCert.setCreate_time(x509AttrCert.getSerialNumber());
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
            
            // here we need to check a config option, mtwilson.atag.associate.hosts.auto
            // now try to match a host to it
            log.debug("trying to associate tag to existing host using " + Hex.encodeHexString(atagCert.getSHA1Hash()));
            AssetTagCertAssociateRequest request = new AssetTagCertAssociateRequest();
            request.setSha1OfAssetCert(atagCert.getSHA1Hash());
            //result = 
            mapAssetTagCertToHost(request);
            
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
     * provisioned for.  It does not require you know the ID of the host you are associating to.  
     * Here you are giving the hash of the cert to the code and letting it find a matching host
     * @param atagObj
     * @return true if host was found, false if not
     */
    public boolean mapAssetTagCertToHost(AssetTagCertAssociateRequest atagObj) {
        boolean result = false;
        log.debug("mapAssetTagCertToHost");
        AssetTagCertAssociateRequest request = new AssetTagCertAssociateRequest();
        try {
            My.initDataEncryptionKey(); // needed for connection string decryption
            if (atagObj.getSha1OfAssetCert() != null) {
                log.debug("trying to associate tag to existing host using " + Hex.encodeHexString(atagObj.getSha1OfAssetCert()));
                List<MwAssetTagCertificate> atagCerts = My.jpa().mwAssetTagCertificate().findAssetTagCertificateBySha1Hash(atagObj.getSha1OfAssetCert());
                // below code is for debugging.. we will delete it later.
                // List<MwAssetTagCertificate> atagCerts = My.jpa().mwAssetTagCertificate().findAssetTagCertificatesByHostUUID("494cb5dc-a3e1-4e46-9b52-e694349b1654");
                if (atagCerts.isEmpty() ) {
                    log.error("mapAssetTagCertToHost: The asset tag certificate does not exist");
                    throw new ASException(ErrorCode.AS_INVALID_ASSET_TAG_CERTIFICATE_HASH);
                } else if (atagCerts.size() > 1) {
                    log.error("mapAssetTagCertToHost: There were multiple matches for the specified hash");
                    throw new ASException(ErrorCode.AS_INVALID_ASSET_TAG_CERTIFICATE_HASH);
                } else {
                    MwAssetTagCertificate atagCert = atagCerts.get(0);
                    request.setSha1OfAssetCert(atagCert.getSHA1Hash());
                    String uuid = atagCert.getUuid().toLowerCase().trim();
                    log.debug("searching using " + uuid);
                    TblHosts tblHost = My.jpa().mwHosts().findByHwUUID(uuid);
                    if(tblHost != null) {
                        log.debug("found host matching uuid of cert, going to assoicate with host id = " + tblHost.getId());
                        request.setHostID(tblHost.getId());
                        //atagObj.setHostID(tblHost.getId());
                        result = mapAssetTagCertToHostById(request);
                    }else{
                        log.debug("found no matching uuid of cert");
                        result = false;
                    }
                }
            }
        }catch(IOException | ASException | CryptographyException ex){
            log.error("Unexpected error during mapping of host to the asset tag certificate. Error Details - {}.", ex.getMessage());
            throw new ASException(ex);
        }       
        
        return result;
    }
    
    /**
     * This function would be used to associate a asset tag certificate with the host for which it is 
     * provisioned for.  It requires you know the ID of the host it is to be associated with 
     * @param atagObj
     * @return 
     */
    public boolean mapAssetTagCertToHostById(AssetTagCertAssociateRequest atagObj) {
        boolean result;
        log.debug("mapAssetTagCertToHostById");
        
        // Before we map the asset tag cert to the host, we first need to unmap any associations if it already exists
        try {
            unmapAssetTagCertFromHostById(atagObj);
            log.debug("Successfully unmapped the asset tag certificate assocation with host {}. ", atagObj.getHostID());
            
        } catch (Exception ex) {
            log.error("Error during unmap of asset tag cert from host with id {}. {}", atagObj.getHostID(), ex.getMessage());
        }
        
        try {
            My.initDataEncryptionKey(); // needed for connection string decryption
            // Find the asset tag certificate for the specified Sha256Hash value
            if (atagObj.getSha1OfAssetCert() != null) {
                List<MwAssetTagCertificate> atagCerts = My.jpa().mwAssetTagCertificate().findAssetTagCertificateBySha1Hash(atagObj.getSha1OfAssetCert());
                // below code is for debugging.. we will delete it later.
                // List<MwAssetTagCertificate> atagCerts = My.jpa().mwAssetTagCertificate().findAssetTagCertificatesByHostUUID("494cb5dc-a3e1-4e46-9b52-e694349b1654");
                if (atagCerts.isEmpty() ) {
                    log.error("mapAssetTagCertToHostById: The asset tag certificate does not exist");
                    throw new ASException(ErrorCode.AS_INVALID_ASSET_TAG_CERTIFICATE_HASH);
                }
                else if( atagCerts.size() > 1) {
                    log.error("mapAssetTagCertToHostById: There were multiple matches for the specified hash");
                    throw new ASException(ErrorCode.AS_INVALID_ASSET_TAG_CERTIFICATE_HASH);
                } else {
                    // Now that we have the asset tag identified, let us update the entry with the host ID for which it has
                    // to be associated.
                    MwAssetTagCertificate atagCert = atagCerts.get(0);
                    atagCert.setHostID(atagObj.getHostID());
                    
                    // Now that the mapping is done, we need to calculate what the expected PCR value should be and put it in
                    // the PCREvent column.
                    Sha1Digest tag = Sha1Digest.digestOf(atagCert.getCertificate());
                    log.debug("mapAssetTagCertToHostById : Sha1 Hash of the certificate with UUID {} is {}.", atagCert.getUuid(), tag.toString());
                    Sha1Digest expectedHash = Sha1Digest.ZERO.extend(tag);
                    log.debug("mapAssetTagCertToHostById : Final expected PCR for the certificate with UUID {} is {}.", atagCert.getUuid(), expectedHash.toString());

                    atagCert.setPCREvent(expectedHash.toByteArray());
                    My.jpa().mwAssetTagCertificate().edit(atagCert);
                    
                    result = true;
                }
            } else {
                log.error("Sha1Hash for the asset tag is not specified.");
                throw new ASException(ErrorCode.AS_INVALID_ASSET_TAG_CERTIFICATE_HASH);
            }            
        } catch (ASException ase) {
            log.error("Error during mapping of host to the asset tag certificate. Error Details - {}:{}.", ase.getErrorCode(), ase.getErrorMessage());
            throw ase;
        } catch (Exception ex) {
            log.error("Unexpected error during mapping of host by id to the asset tag certificate. Error Details - {}.", ex.getMessage());
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
    public boolean unmapAssetTagCertFromHostById(AssetTagCertAssociateRequest atagObj) {
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
                        // There is no need to validate during unmapping the asset tag request
                        // if (validateAssetTagCert(atagTempCert)) {
                        atagTempCert.setHostID(null);
                        My.jpa().mwAssetTagCertificate().edit(atagTempCert);
                        log.debug("Successfully upmapped the host with id {} from the asset tag certificate.", atagObj.getHostID());
                        return true;
                        //}
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
    public boolean revokeAssetTagCertificate(AssetTagCertRevokeRequest atagObj, String uuid) {
        boolean result;
        List<MwAssetTagCertificate> atagCerts;
        try {
            // Find the asset tag certificate for the specified Sha256Hash value
            if (uuid != null && !uuid.isEmpty()) {
                log.debug("UUID {} is specified for revoking the asset tag certificate", uuid);
                atagCerts = My.jpa().mwAssetTagCertificate().findAssetTagCertificatesByUuid(uuid);
            } else if (atagObj.getSha1OfAssetCert() != null) {
                log.error("SHA1 {} is specified for revoking the asset tag certificate", atagObj.getSha1OfAssetCert());
                atagCerts = My.jpa().mwAssetTagCertificate().findAssetTagCertificateBySha1Hash(atagObj.getSha1OfAssetCert());
            } else {
                log.error("Sha1 for the asset tag is not specified.");
                throw new ASException(ErrorCode.AS_INVALID_ASSET_TAG_CERTIFICATE_HASH);
            }            

            if (atagCerts.isEmpty() || atagCerts.size() > 1) {
                log.warn("Either the asset tag certificate does not exist or there were multiple matches for the specified hash.");
//                throw new ASException(ErrorCode.AS_INVALID_ASSET_TAG_CERTIFICATE_HASH);
                result = true;
            } else {
                // Now that we have the asset tag identified, set the revoked flag to true.
                MwAssetTagCertificate atagCert = atagCerts.get(0);
                atagCert.setRevoked(true);
                My.jpa().mwAssetTagCertificate().edit(atagCert);
                result = true;
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

        try {
            // Find the asset tag certificates for the specified UUID of the host. Not that this might return back multiple
            // values. We need to evaluate each of the certificates to make sure that they are valid
            // The below query has been modified to return back the results ordered by the insert date with the latest one first
            // So if the host has been provisioned multiple times, we will pick up the latest one.
            if (uuid != null && !uuid.isEmpty()) {
                List<MwAssetTagCertificate> atagCerts = My.jpa().mwAssetTagCertificate().findAssetTagCertificatesByHostUUID(uuid.toLowerCase());
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
                    return null;
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
    }
    
    public MwAssetTagCertificate findValidAssetTagCertForHost(Integer hostID){
        try {
            // Find the asset tag certificates for the specified UUID of the host. Note that this might return back multiple
            // values. We need to evaluate each of the certificates to make sure that they are valid
            // The below query has been modified to return back the results ordered by the insert date with the latest one first
            // So if the host has been provisioned multiple times, we will pick up the latest one.
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
        
        return null;
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
            try (InputStream atagCaIn = new FileInputStream(My.configuration().getAssetTagCaCertificateFile())) {
                atagCaCerts = X509Util.decodePemCertificates(IOUtils.toString(atagCaIn));
                //IOUtils.closeQuietly(atagCaIn);
                log.debug("Added {} certificates from AssetTagCA.pem", atagCaCerts.size());
            } catch(IOException | CertificateException ex) {
                log.error("Error loading the Asset Tag pem file to extract the CA certificate(s).",ex);
            }
            
            // The below isValid function verifies both the signature and the dates.
            if (atagCaCerts != null ) {
                for (X509Certificate atagCACert : atagCaCerts) {
                    if (atagAttrCertForHost.isValid(atagCACert))
                        return true;
                }
            }
            
        } catch (Exception ex) {
            throw new ASException (ex);
        }
                
        return isValid;        
    }
    
    protected static final ObjectMapper mapper = new ObjectMapper();
     
    private <T> T fromJSON(String document, Class<T> valueType) throws IOException, ApiException {
        try {
            return mapper.readValue(document, valueType);
        }
        catch(com.fasterxml.jackson.core.JsonParseException e) {
           
            throw new ApiException("Cannot parse response", e);
        }
    }
    
    public TagDataType getTagInfoByOID(String oid) throws IOException, ApiException, NoSuchAlgorithmException, KeyManagementException, SignatureException {
        log.error("attempting to connect to asset tag host");
        String requestURL = My.configuration().getAssetTagServerURL() + "/tags?oidEqualTo="+oid;
        
        //1.3.6.1.4.1.99999.3"; 
        ApacheHttpClient client = new ApacheHttpClient(My.configuration().getAssetTagServerURL(), new ApacheBasicHttpAuthorization(new UsernamePasswordCredentials(My.configuration().getAssetTagApiUsername(),My.configuration().getAssetTagApiPassword())), null, new InsecureTlsPolicy());

        //ApiRequest request = new ApiRequest(MediaType., "");
        ApiResponse response = client.get(requestURL);    

        String str = new String(response.content);
        System.out.println("getTagInfoByOID response = " + str);        
        TagDataType[] tag = fromJSON(str, TagDataType[].class);       
  
        if(tag == null || tag[0] == null)
            throw new ApiException("Error while getting tag from server");
        return tag[0];
    }

}
