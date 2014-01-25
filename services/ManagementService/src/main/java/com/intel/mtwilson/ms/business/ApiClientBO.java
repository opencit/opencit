/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.ms.business;

import com.intel.mtwilson.My;
import com.intel.mtwilson.MyJpa;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.ms.common.MSException;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.controller.ApiRoleX509JpaController;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.ms.controller.exceptions.MSDataException;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.ms.controller.exceptions.PreexistingEntityException;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.mtwilson.ms.data.ApiRoleX509;
import com.intel.mtwilson.ms.data.ApiRoleX509PK;
import com.intel.mtwilson.ms.data.MwPortalUser;
import com.intel.mtwilson.ms.helper.BaseBO;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 *
 * @author dsmagadx
 */
public class ApiClientBO extends BaseBO {

    private Logger log = LoggerFactory.getLogger(getClass());
    Marker sysLogMarker = MarkerFactory.getMarker(LogMarkers.USER_CONFIGURATION.getValue());
    
    public ApiClientBO() {
    }

    /**
     * 
     * @param apiClientRequest 
     */
    public void create(ApiClientCreateRequest apiClientRequest) {


        try {
            X509Certificate x509Certificate;
            try {
                x509Certificate = X509Util.decodeDerCertificate(apiClientRequest.getCertificate());
            } catch (CertificateException e) {
                throw new MSException(e, ErrorCode.MS_INVALID_CERTIFICATE_DATA, e.getMessage());
            }
            validate(apiClientRequest, x509Certificate);
            createApiClientAndRole(apiClientRequest, x509Certificate);
            
            // Log the details into the syslog
            Object[] paramArray = {Arrays.toString(getFingerPrint(x509Certificate)), Arrays.toString(apiClientRequest.getRoles())};
            log.debug(sysLogMarker, "Created a request for new API Client: {} with roles: {}", paramArray);

        } catch (MSException me) {
            log.error("Error during API Client registration. " + me.getErrorMessage());
            throw me;
            
        } catch (Exception ex) {
            log.error("Error during API Client registration. ", ex);
            throw new MSException(ErrorCode.MS_API_USER_REGISTRATION_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     * Validates the Api Client certificate and the roles requested for.
     * 
     * @param apiClientRequest
     * @param x509Certificate 
     */
    private void validate(ApiClientCreateRequest apiClientRequest,
            X509Certificate x509Certificate) {
        try {
            // x509Certificate.checkValidity();
            // Since we are running into issues because of the timezone difference between the ApiClient and
            // the mount wilson server, we are checking if the certificate that is being passed is going to be
            // valid within the next 24 hours.
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.HOUR, 24);
            x509Certificate.checkValidity(cal.getTime());
            
        } catch (CertificateExpiredException ex) {
            throw new MSException(ex,ErrorCode.MS_EXPIRED_CERTIFICATE, ex.getMessage());
            
        } catch (CertificateNotYetValidException ex) {
            throw new MSException(ex,ErrorCode.MS_CERTIFICATE_NOT_YET_VALID, ex.getMessage());
            
        }

        for (String role : apiClientRequest.getRoles()) {
            try {
                Role.valueOf(role);
            } catch (IllegalArgumentException ie) {
                
                throw new MSException(ie,ErrorCode.MS_API_CLIENT_INVALID_ROLE, role );
            }

        }
        
        if(isDuplicate(getFingerPrint(x509Certificate))){
        	throw new MSException(ErrorCode.MS_DUPLICATE_CERTIFICATE);
        }

    }

    /**
     * 
     * @param fingerprint
     * @return 
     */
    private boolean isDuplicate(byte[] fingerprint) {		
        if(new ApiClientX509JpaController(getMSEntityManagerFactory()).findApiClientX509ByFingerprint(fingerprint) != null) {
                return true;
        }
        return false;
    }

    /**
     * 
     * @param apiClientRequest
     * @param x509Certificate 
     */
    private void createApiClientAndRole(ApiClientCreateRequest apiClientRequest,
            X509Certificate x509Certificate) {

        try {

            ApiClientX509 apiClientX509 = new ApiClientX509();

            apiClientX509.setCertificate(apiClientRequest.getCertificate());
            apiClientX509.setEnabled(false);
            apiClientX509.setExpires(x509Certificate.getNotAfter());
            apiClientX509.setFingerprint(getFingerPrint(x509Certificate));
            apiClientX509.setIssuer(x509Certificate.getIssuerDN().getName());
            apiClientX509.setName(x509Certificate.getSubjectX500Principal().getName());
            apiClientX509.setSerialNumber(x509Certificate.getSerialNumber().intValue());
            apiClientX509.setStatus(ApiClientStatus.PENDING.toString());
            // XXX SAVY TODO api client set Uuid and Locale
            //apiClientX509.setUuid_hex(null);
            //apiClientX509.setLocale(apiClientRequest.);

            new ApiClientX509JpaController(getMSEntityManagerFactory()).create(apiClientX509);

            setRolesForApiClient(apiClientX509, apiClientRequest.getRoles());
            
        } catch (Exception ex) {
            log.error("Error during API Client registration. ", ex);
            throw new MSException(ErrorCode.MS_API_USER_REGISTRATION_ERROR, ex.getClass().getSimpleName());
            // throw new MSException(ex,ErrorCode.MS_API_CLIENT_CREATE_ERROR);
        }
    }
    
    /**
     * 
     * @param apiClientX509 
     */
    private void clearRolesForApiClient(ApiClientX509 apiClientX509)  {
        ApiRoleX509JpaController apiRoleX509JpaController = new ApiRoleX509JpaController(getMSEntityManagerFactory());
        for (ApiRoleX509 role : apiClientX509.getApiRoleX509Collection()) {
            try {
                apiRoleX509JpaController.destroy(role.getApiRoleX509PK());
                
            } catch (NonexistentEntityException ex) {
                
                throw new MSException(ex,ErrorCode.MS_API_CLIENT_INVALID_ROLE,  role);                
            }
        }                
    }
    
    /**
     * 
     * @param apiClientX509
     * @param roles 
     */
    private void setRolesForApiClient(ApiClientX509 apiClientX509, String[] roles) {
        ApiRoleX509JpaController apiRoleX509JpaController = new ApiRoleX509JpaController(getMSEntityManagerFactory());
        for (String role : roles) {
            try {
                ApiRoleX509 apiRoleX509 = new ApiRoleX509();
                ApiRoleX509PK apiRoleX509PK = new ApiRoleX509PK(apiClientX509.getId(),
                        Role.valueOf(role).toString());
                apiRoleX509.setApiRoleX509PK(apiRoleX509PK);
                apiRoleX509.setApiClientX509(apiClientX509);
                apiRoleX509JpaController.create(apiRoleX509);
                
            } catch (PreexistingEntityException ex) {
                throw new MSException(ex,ErrorCode.MS_API_CLIENT_ROLE_ALEADY_EXISTS);
                
            } catch (MSDataException ex) {
                throw new MSException(ex,ErrorCode.MS_API_CLIENT_CREATE_ERROR);
            } 
        }
    }

    /**
     * 
     * @param x509Certificate
     * @return 
     */
    private byte[] getFingerPrint(X509Certificate x509Certificate) {
        try {
            return X509Util.sha256fingerprint(x509Certificate);
            
        } catch (CertificateEncodingException ce) {
            throw new MSException(ce,ErrorCode.MS_CERTIFICATE_ENCODING_ERROR, ce.getMessage());
            
        } catch (NoSuchAlgorithmException nle) {
            throw new MSException(nle,ErrorCode.MS_UN_SUPPORTED_HASH_ALGORITHM, nle.getMessage());
        }
    }

    /**
     * 
     * @param apiClientRequest 
     */
    public void update(ApiClientUpdateRequest apiClientRequest) {
        
        try {
            ApiClientX509JpaController apiClientX509JpaController = new ApiClientX509JpaController(getMSEntityManagerFactory());

            ApiClientX509 apiClientX509 = apiClientX509JpaController.findApiClientX509ByFingerprint(apiClientRequest.fingerprint);

            apiClientX509.setEnabled(apiClientRequest.enabled);
            apiClientX509.setStatus(apiClientRequest.status);
            apiClientX509.setComment(apiClientRequest.comment);            

            apiClientX509JpaController.edit(apiClientX509); // IllegalOrphanException, NonexistentEntityException, Exception

            clearRolesForApiClient(apiClientX509);
            setRolesForApiClient(apiClientX509, apiClientRequest.roles);
                        
            MwPortalUserJpaController mwPortalUserJpaController = My.jpa().mwPortalUser();//new MwPortalUserJpaController(getMSEntityManagerFactory());
            MwPortalUser portalUser = mwPortalUserJpaController.findMwPortalUserByUserName(apiClientX509.getUserNameFromName());
            if(portalUser != null) {
                portalUser.setEnabled(apiClientRequest.enabled);
                portalUser.setStatus(apiClientRequest.status);
                portalUser.setComment(apiClientRequest.comment);
                mwPortalUserJpaController.edit(portalUser);
            }
            
            
            // Capture the change in the syslog
            Object[] paramArray = {Arrays.toString(apiClientRequest.fingerprint), Arrays.toString(apiClientRequest.roles), apiClientRequest.status};
            log.debug(sysLogMarker, "Updated the status of API Client: {} with roles: {} to {}.", paramArray);

        } catch (MSException me) {
            log.error("Error during API Client update. " + me.getErrorMessage());
            throw me;
            
        } catch (Exception ex) {
            // throw new MSException(ex);
            log.error("Error during API user update. ", ex);
            throw new MSException(ErrorCode.MS_API_USER_UPDATE_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     * 
     * @param apiClientX509
     * @return 
     */
    private ApiClientInfo toApiClientInfo(ApiClientX509 apiClientX509) {
        ApiClientInfo info = new ApiClientInfo();
        info.certificate = apiClientX509.getCertificate();
        info.fingerprint = apiClientX509.getFingerprint();
        info.name = apiClientX509.getName();
        info.issuer = apiClientX509.getIssuer();
        info.serialNumber = apiClientX509.getSerialNumber();
        info.expires = apiClientX509.getExpires();
        info.enabled = apiClientX509.getEnabled();
        info.status = apiClientX509.getStatus();
        info.comment = apiClientX509.getComment();
        // set the roles array
        ArrayList<String> roleNames = new ArrayList<String>();
        for(ApiRoleX509 role : apiClientX509.getApiRoleX509Collection()) {
            roleNames.add(role.getApiRoleX509PK().getRole());
        }
        info.roles = roleNames.toArray(new String[0]);
        return info;
    }
    
    /**
     * 
     * @param fingerprint
     * @return 
     */
    public ApiClientInfo find(byte[] fingerprint) {
        
        try {
            ApiClientX509JpaController apiClientX509JpaController = new ApiClientX509JpaController(getMSEntityManagerFactory());

            ApiClientX509 apiClientX509 = apiClientX509JpaController.findApiClientX509ByFingerprint(fingerprint);

            ApiClientInfo info = toApiClientInfo(apiClientX509);
            
            return info;
            
        } catch (MSException me) {
            log.error("Error during retrieving of the API Client information. " + me.getErrorMessage());
            throw me;
            
        } catch (Exception ex) {
            // throw new MSException(ex);
            log.error("Error during search for API user. ", ex);
            throw new MSException(ErrorCode.MS_API_USER_SEARCH_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     * Currently supporting only one selected criteria. In the future, we 
     * should support multiple criteria (such as date and issuer, issuer and status, etc)
     * @param criteria
     * @return 
     */
    public List<ApiClientInfo> search(ApiClientSearchCriteria criteria) {
        
        try {
            ApiClientX509JpaController apiClientX509JpaController = new ApiClientX509JpaController(getMSEntityManagerFactory());
            List<ApiClientX509> list;
            if( criteria.enabledEqualTo != null && criteria.statusEqualTo != null ) {
                list = apiClientX509JpaController.findApiClientX509ByEnabledStatus(criteria.enabledEqualTo, criteria.statusEqualTo); // findByEnabledStatus
            }
            else if( criteria.enabledEqualTo != null ) {
                list = apiClientX509JpaController.findApiClientX509ByEnabled(criteria.enabledEqualTo); //findByEnabled
            }
            else if( criteria.expiresAfter != null ) {
                list = apiClientX509JpaController.findApiClientX509ByExpiresAfter(criteria.expiresAfter); //findByExpiresAfter
            }
            else if( criteria.expiresBefore != null ) {
                list = apiClientX509JpaController.findApiClientX509ByExpiresBefore(criteria.expiresBefore); //findByExpiresBefore
            }
            else if( criteria.fingerprintEqualTo != null ) {
                ApiClientX509 apiClientX509 = apiClientX509JpaController.findApiClientX509ByFingerprint(criteria.fingerprintEqualTo); //findByFingerprint
                list = new ArrayList<ApiClientX509>();
                list.add(apiClientX509);
            }
            else if( criteria.issuerEqualTo != null ) {
                list = apiClientX509JpaController.findApiClientX509ByIssuer(criteria.issuerEqualTo); //findByIssuer
            }
            else if( criteria.nameContains != null ) {
                list = apiClientX509JpaController.findApiClientX509ByNameLike(criteria.nameContains); //findByNameContains
            }
            else if( criteria.nameEqualTo != null ) {
                list = apiClientX509JpaController.findApiClientX509ByName(criteria.nameEqualTo); //findByName
            }
            else if( criteria.serialNumberEqualTo != null ) {
                list = apiClientX509JpaController.findApiClientX509BySerialNumber(criteria.serialNumberEqualTo); //findBySerialNumber
            }
            else if( criteria.statusEqualTo != null ) {
                list = apiClientX509JpaController.findApiClientX509ByStatus(criteria.statusEqualTo); //findByStatus
            }
            else if( criteria.commentContains != null ) {
                list = apiClientX509JpaController.findApiClientX509ByCommentLike(criteria.commentContains); //findByNameContains
            }
            else {
                // no criteria means return all records
                list = apiClientX509JpaController.findApiClientX509Entities();
            }
            ArrayList<ApiClientInfo> response = new ArrayList<ApiClientInfo>();
            if( list == null ) {
                return response; // empty list
            }
            for(ApiClientX509 apiClientX509 : list) {
                response.add(toApiClientInfo(apiClientX509));
            }
            
            return response;
            
        } catch (MSException me) {
            log.error("Error during searching for the API Client information. " + me.getErrorMessage());            
            throw me;
            
        } catch (Exception ex) {
            // throw new MSException(ex);
            log.error("Error during search for API user. ", ex);
            throw new MSException(ErrorCode.MS_API_USER_SEARCH_ERROR, ex.getClass().getSimpleName());            
        }
    }    
}
