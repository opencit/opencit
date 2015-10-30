/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.ms.business;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.i18n.LocaleUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
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
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import com.intel.mtwilson.user.management.rest.v2.model.Status;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.user.management.rest.v2.model.UserComment;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 *
 * @author dsmagadx
 */
public class ApiClientBO {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApiClientBO.class);

    Marker sysLogMarker = MarkerFactory.getMarker(LogMarkers.USER_CONFIGURATION.getValue());
    private final ObjectMapper yaml;
    private ApiClientX509JpaController apiClientX509JpaController ;
    private ApiRoleX509JpaController apiRoleX509JpaController;
    
    public ApiClientBO() {
        try {
            yaml = createYamlMapper();
            apiClientX509JpaController = My.jpa().mwApiClientX509();
            apiRoleX509JpaController = My.jpa().mwApiRoleX509();
        } catch (IOException ex) {
            log.error("Error during persistence manager initialization", ex);
            throw new MSException(ErrorCode.SYSTEM_ERROR, ex.getClass().getSimpleName());
        }
    }

    public void create(ApiClientCreateRequest apiClientRequest) {
        create(apiClientRequest, null, null);
    }
    /**
     * 
     * @param apiClientRequest 
     */
    public void create(ApiClientCreateRequest apiClientRequest, String userUuid, String userCertUuid) {


        try {
            X509Certificate x509Certificate;
            try {
                x509Certificate = X509Util.decodeDerCertificate(apiClientRequest.getCertificate());
            } catch (CertificateException e) {
                throw new MSException(e, ErrorCode.MS_INVALID_CERTIFICATE_DATA, e.getMessage());
            }
            log.debug("Validating api client create request");
            validate(apiClientRequest, x509Certificate);
            createApiClientAndRole(apiClientRequest, x509Certificate, userUuid, userCertUuid);
            
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
        try {
            if (My.jpa().mwApiClientX509().findApiClientX509ByFingerprint(fingerprint) != null) {
                return true;
            }
            // if(new ApiClientX509JpaController(getMSEntityManagerFactory()).findApiClientX509ByFingerprint(fingerprint) != null) {
            // return true;
            // }
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    /**
     * 
     * @param apiClientRequest
     * @param x509Certificate 
     */
    private void createApiClientAndRole(ApiClientCreateRequest apiClientRequest,
            X509Certificate x509Certificate, String userUuid, String userCertUuid) {

        try {
            //Feb 12,2014 - Sudhir: First we need to create the user in the portal user table. Then we need to use that user ID and create the entry
            // in the api client x509 table.
            // Since we are reusing this function even from the new API v2, we need to check who is calling into this. If the new API is calling into
            // this API, then we should not be creating the user. It is expected that users of new API v2 should do that.
            if (userUuid == null || userUuid.isEmpty()) {
                MwPortalUser pUser = new MwPortalUser();
                userUuid = new UUID().toString();
                pUser.setUuid_hex(userUuid);
                // We will not set the keystore here. The caller who calls into the keystore.createuserinresource is responsible for updating the 
                // portal user table with the new keystore.
                pUser.setStatus(ApiClientStatus.PENDING.toString());
                pUser.setUsername(getSimpleNameFromCert(x509Certificate));
                My.jpa().mwPortalUser().create(pUser);                
            }

            ApiClientX509 apiClientX509 = new ApiClientX509();

            if (userCertUuid != null && !userCertUuid.isEmpty())
                apiClientX509.setUuid_hex(userCertUuid);
            else
                apiClientX509.setUuid_hex(new UUID().toString());
            
            // Feb 12, 2014: Adding the reference to the user table in the x509 table.
            apiClientX509.setUser_uuid_hex(userUuid);
            
            apiClientX509.setCertificate(apiClientRequest.getCertificate());
            apiClientX509.setEnabled(false);
            apiClientX509.setExpires(x509Certificate.getNotAfter());
            apiClientX509.setFingerprint(getFingerPrint(x509Certificate));
            apiClientX509.setIssuer(x509Certificate.getIssuerDN().getName());
            apiClientX509.setName(x509Certificate.getSubjectX500Principal().getName());
            apiClientX509.setSerialNumber(x509Certificate.getSerialNumber().intValue());
            apiClientX509.setStatus(ApiClientStatus.PENDING.toString());
            apiClientX509.setComment(commentWithRequestedRoles(apiClientRequest.getRoles()));
            //apiClientX509.setUuid_hex(null);
            //apiClientX509.setLocale(apiClientRequest.);

            apiClientX509JpaController.create(apiClientX509);
            setRolesForApiClient(apiClientX509, apiClientRequest.getRoles());
            populateShiroUserTables(apiClientRequest, x509Certificate);
            
        } catch (Exception ex) {
            log.error("Error during API Client registration. ", ex);
            throw new MSException(ErrorCode.MS_API_USER_REGISTRATION_ERROR, ex.getClass().getSimpleName());
            // throw new MSException(ex,ErrorCode.MS_API_CLIENT_CREATE_ERROR);
        }
    }
    
    private String commentWithRequestedRoles(String[] roles) throws JsonProcessingException {
        UserComment comment = new UserComment();
        comment.roles = new HashSet<>();
        comment.roles.addAll(Arrays.asList(roles));
        return yaml.writeValueAsString(comment);
    }
    
    private ObjectMapper createYamlMapper() {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        yamlFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        ObjectMapper mapper = new ObjectMapper(yamlFactory);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        return mapper;
    }
    public ObjectMapper getUserCommentMapper() { return yaml; }
    
    private void populateShiroUserTables(ApiClientCreateRequest apiClientRequest, X509Certificate x509Certificate) {
        log.debug("Adding new v2 user {}", x509Certificate.getSubjectX500Principal().getName());
        try(LoginDAO loginDAO = MyJdbi.authz()) {
            log.debug("Looking for existing user");
            User user = loginDAO.findUserByName(getSimpleNameFromCert(x509Certificate));
            if (user == null) {
                log.debug("No existing user, inserting record");
                user = new User();
                user.setId(new UUID());
//                user.setLocale(Locale.US);
                user.setComment("");
                user.setUsername(getSimpleNameFromCert(x509Certificate));
                loginDAO.insertUser(user.getId(), user.getUsername(), null, user.getComment());
            }
            log.debug("Looking for existing certificate");
            UserLoginCertificate userLoginCertificate = loginDAO.findUserLoginCertificateByUsername(getSimpleNameFromCert(x509Certificate));
            if (userLoginCertificate == null) {                        
                log.debug("No existing certificate, inserting record");
                userLoginCertificate = new UserLoginCertificate();
                userLoginCertificate.setId(new UUID());
                userLoginCertificate.setCertificate(apiClientRequest.getCertificate());
                userLoginCertificate.setComment(commentWithRequestedRoles(apiClientRequest.getRoles()));
                userLoginCertificate.setEnabled(false);
                userLoginCertificate.setExpires(x509Certificate.getNotAfter());
                userLoginCertificate.setSha1Hash(Sha1Digest.digestOf(apiClientRequest.getCertificate()).toByteArray());
                // we are going to use the same fingerprint as currently being used so that it is easy for us to do the lookup
                userLoginCertificate.setSha256Hash(getFingerPrint(x509Certificate));//Sha256Digest.digestOf(apiClientRequest.getCertificate()).toByteArray());
                userLoginCertificate.setStatus(Status.PENDING);
                userLoginCertificate.setUserId(user.getId());
                loginDAO.insertUserLoginCertificate(userLoginCertificate.getId(), userLoginCertificate.getUserId(), userLoginCertificate.getCertificate(), 
                        userLoginCertificate.getSha1Hash(), userLoginCertificate.getSha256Hash(), userLoginCertificate.getExpires(), 
                        userLoginCertificate.isEnabled(), userLoginCertificate.getStatus(), userLoginCertificate.getComment());
                log.debug("Created user login certificate with sha256 {}", Sha256Digest.valueOf(userLoginCertificate.getSha256Hash()).toHexString());
            }
            
            // administrator must explicitly assign roles (implemented by updateShiroUserTables),  do not add them here. it's unsafe to automatically assign roles to a registration request which is essentially unvalidated user input.
            /*
            String[] roles = apiClientRequest.getRoles();
            log.debug("New user roles are {}", (Object[])roles);
            for (String role : roles) {
                log.debug("Looking for role {}", role);
                com.intel.mtwilson.user.management.rest.v2.model.Role findRoleByName = loginDAO.findRoleByName(role);
                if (findRoleByName != null) {
                    log.debug("Adding role {} to new user {}", role, userLoginCertificate.getUserId());
                    loginDAO.insertUserLoginCertificateRole(userLoginCertificate.getId(), findRoleByName.getId());
                }
            }
            */
            
        } catch (Exception ex) {
            log.error("Error while populating Shiro tables during API Client registration. ", ex);
            throw new MSException(ErrorCode.MS_API_USER_REGISTRATION_ERROR, ex.getClass().getSimpleName());
        }

    }

    private void updateShiroUserTables(ApiClientUpdateRequest apiClientUpdateRequest, String userName) {
        log.debug("Updating v2 user tables for {}", userName);
        try(LoginDAO loginDAO = MyJdbi.authz()) {
            log.debug("Looking up user login certificate {}", Hex.encodeHexString(apiClientUpdateRequest.fingerprint));
            UserLoginCertificate userLoginCertificate = loginDAO.findUserLoginCertificateBySha256(apiClientUpdateRequest.fingerprint);
            if (userLoginCertificate != null) {
                log.debug("Found user login certificate {}", userLoginCertificate.getId());
                userLoginCertificate.setEnabled(apiClientUpdateRequest.enabled);
                userLoginCertificate.setStatus(Status.valueOf(apiClientUpdateRequest.status));
                if (apiClientUpdateRequest.comment != null && !apiClientUpdateRequest.comment.isEmpty())
                    userLoginCertificate.setComment(apiClientUpdateRequest.comment);
                loginDAO.updateUserLoginCertificateById(userLoginCertificate.getId(), userLoginCertificate.isEnabled(), 
                        userLoginCertificate.getStatus(), userLoginCertificate.getComment());
            }
            log.debug("Looking up user {}", userName);
            User user = loginDAO.findUserByName(userName);
            if (user != null) {
                log.debug("Found user {}", user.getId());
                if (apiClientUpdateRequest.comment != null && !apiClientUpdateRequest.comment.isEmpty())
                    user.setComment(apiClientUpdateRequest.comment);
                String localeTag = null;
                if (user.getLocale() != null)
                    localeTag = LocaleUtil.toLanguageTag(user.getLocale());
                loginDAO.updateUser(user.getId(), localeTag, user.getComment());
            }
            
            log.debug("Update request roles: {}", (Object[])apiClientUpdateRequest.roles);
            // Clear the existing roles and update it with the new ones only if specified by the user
            if ((apiClientUpdateRequest.roles != null) && (userLoginCertificate != null)) {
                // Let us first delete the existing roles
                log.debug("Looking for existing roles for user login certificate {}", userLoginCertificate.getId());
                List<com.intel.mtwilson.user.management.rest.v2.model.Role> rolesByUserLoginCertificateId = loginDAO.findRolesByUserLoginCertificateId(userLoginCertificate.getId());
                for (com.intel.mtwilson.user.management.rest.v2.model.Role roleMapping : rolesByUserLoginCertificateId) {
                    log.debug("Removing role {} from user {}", roleMapping.getRoleName(), userName);
                    loginDAO.deleteUserLoginCertificateRole(userLoginCertificate.getId(), roleMapping.getId());
                }
                
                // Let us add the new roles
                log.debug("Adding roles");
                for (String role : apiClientUpdateRequest.roles) {
                    String roleName = Role.valueOf(role).getName();
                    log.debug("Adding role {} with value {}.", role, roleName);
                    com.intel.mtwilson.user.management.rest.v2.model.Role findRoleByName = loginDAO.findRoleByName(roleName);
                    if (findRoleByName != null) {
                        log.debug("Adding role {} to user {}", findRoleByName.getRoleName(), userName);
                        loginDAO.insertUserLoginCertificateRole(userLoginCertificate.getId(), findRoleByName.getId());
                    }
                }
                
            }            
        } catch (Exception ex) {
            log.error("Error while populating Shiro tables during API Client registration. ", ex);
            throw new MSException(ErrorCode.MS_API_USER_REGISTRATION_ERROR, ex.getClass().getSimpleName());
        }

    }
    
    /**
     * 
     * @param apiClientX509 
     */
    private void clearRolesForApiClient(ApiClientX509 apiClientX509)  {
        for (ApiRoleX509 role : apiClientX509.getApiRoleX509Collection()) {
            try {
                log.debug("clearRolesForApiClient: Deleting role {}", role.getApiRoleX509PK().getRole());
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
        for (String role : roles) {
            try {
                log.debug("Adding v1 role {} to new user {}", role, apiClientX509.getName());
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
    public void update(ApiClientUpdateRequest apiClientRequest, String uuid) {
        ApiClientX509 apiClientX509;
        String userName;
        try {
            if (uuid != null && !uuid.isEmpty()) {
                apiClientX509 = apiClientX509JpaController.findApiClientX509ByUUID(uuid);
                userName = uuid;
            } else {
                apiClientX509 = apiClientX509JpaController.findApiClientX509ByFingerprint(apiClientRequest.fingerprint);
                userName = apiClientRequest.fingerprint.toString();
            }

            if (apiClientX509 != null) {
                // User also exists in the V1 API tables. So, they have to be updated accordingly
                log.info("Specified user has been created using the V1 APIs.");
                apiClientX509.setEnabled(apiClientRequest.enabled);
                apiClientX509.setStatus(apiClientRequest.status);

                // Update the comment if there is value. Otherwise we might overwrite an existing value.
                if (apiClientRequest.comment != null && !apiClientRequest.comment.isEmpty())
                    apiClientX509.setComment(apiClientRequest.comment);            

                apiClientX509JpaController.edit(apiClientX509); // IllegalOrphanException, NonexistentEntityException, Exception
                log.debug("Updated the Api client X509 table");
                
                // Clear the existing roles and update it with the new ones only if specified by the user
                if (apiClientRequest.roles != null) {
                    clearRolesForApiClient(apiClientX509);
                    setRolesForApiClient(apiClientX509, apiClientRequest.roles);
                } 
                userName = apiClientX509.getUserNameFromName();                
            } else {
                // Check if the user is in the V2 table. Otherwise we need to throw an error
                log.debug("Looking up user login certificate {}", Hex.encodeHexString(apiClientRequest.fingerprint));
                try (LoginDAO loginDao = MyJdbi.authz()) {
                    UserLoginCertificate userLoginCertificate = loginDao.findUserLoginCertificateBySha256(apiClientRequest.fingerprint);
                    if (userLoginCertificate == null) {
                        log.error("User with fingerprint {} is not configured in the system.", Hex.encodeHexString(apiClientRequest.fingerprint)); //Sha1Digest.valueOf(apiClientRequest.fingerprint).toHexString());
                        throw new MSException(ErrorCode.MS_USER_DOES_NOT_EXISTS, userName);
                    } else {
                        User user = loginDao.findUserById(userLoginCertificate.getUserId());
                        if (user != null) {
                            userName = user.getUsername();
                        }
                    }
                }
            }
            
                        
            MwPortalUserJpaController mwPortalUserJpaController = My.jpa().mwPortalUser();//new MwPortalUserJpaController(getMSEntityManagerFactory());
            MwPortalUser portalUser = mwPortalUserJpaController.findMwPortalUserByUserName(userName);
            if(portalUser != null) {
                portalUser.setEnabled(apiClientRequest.enabled);
                portalUser.setStatus(apiClientRequest.status);
                if (apiClientRequest.comment != null && !apiClientRequest.comment.isEmpty())
                    portalUser.setComment(apiClientRequest.comment);
                mwPortalUserJpaController.edit(portalUser);
            }
            
            updateShiroUserTables(apiClientRequest, userName);
            
            // Capture the change in the syslog
            Object[] paramArray = {userName, Arrays.toString(apiClientRequest.roles), apiClientRequest.status};
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

        public void delete(ApiClientUpdateRequest apiClientRequest, String uuid) {
        ApiClientX509 apiClientX509;
        String userName;
        try {
            if (uuid != null && !uuid.isEmpty()) {
                apiClientX509 = apiClientX509JpaController.findApiClientX509ByUUID(uuid);
                userName = uuid;
            } else {
                apiClientX509 = apiClientX509JpaController.findApiClientX509ByFingerprint(apiClientRequest.fingerprint);
                userName = apiClientRequest.fingerprint.toString();
            }

            if (apiClientX509 != null) {
                log.info("ApiClientBO:Delete - Specified user has been created using the V1 APIs.");

                // Clear the roles.
                clearRolesForApiClient(apiClientX509);
                userName = apiClientX509.getUserNameFromName();                
                
                apiClientX509JpaController.destroy(apiClientX509.getId()); // IllegalOrphanException, NonexistentEntityException, Exception
                log.debug("ApiClientBO:Delete - Deleted the Api client X509 table");
                
            } else {
                // Check if the user is in the V2 table. Otherwise we need to throw an error
                log.debug("ApiClientBO:Delete - Looking up user login certificate {}", Hex.encodeHexString(apiClientRequest.fingerprint));
                try (LoginDAO loginDao = MyJdbi.authz()) {
                    UserLoginCertificate userLoginCertificate = loginDao.findUserLoginCertificateBySha256(apiClientRequest.fingerprint);
                    if (userLoginCertificate == null) {
                        log.error("ApiClientBO:Delete - User with fingerprint {} is not configured in the system.", Sha1Digest.valueOf(apiClientRequest.fingerprint).toHexString());
                        throw new MSException(ErrorCode.MS_USER_DOES_NOT_EXISTS, userName);
                    } else {
                        User user = loginDao.findUserById(userLoginCertificate.getUserId());
                        if (user != null) {
                            userName = user.getUsername();
                        }
                    }
                }
            }
                                    
            MwPortalUserJpaController mwPortalUserJpaController = My.jpa().mwPortalUser();
            MwPortalUser portalUser = mwPortalUserJpaController.findMwPortalUserByUserName(userName);
            if(portalUser != null) {
                log.debug("ApiClientBO:Delete - About to delete the portal user {}.", portalUser.getUsername());
                mwPortalUserJpaController.destroy(portalUser.getId());
                log.info("ApiClientBO:Delete - Deleted the portal user successfully.");
            }
            
            //updateShiroUserTables(apiClientRequest, userName);
            log.debug("Deleting v2 user tables for {}", userName);
            try(LoginDAO loginDAO = MyJdbi.authz()) {
                log.debug("Looking up user login certificate {}", Hex.encodeHexString(apiClientRequest.fingerprint));

                UserLoginCertificate userLoginCertificate = loginDAO.findUserLoginCertificateBySha256(apiClientRequest.fingerprint);
                if (userLoginCertificate != null) {
                    log.debug("Found user login certificate {}", userLoginCertificate.getId());
                    
                    log.debug("Looking for existing roles for user login certificate {}", userLoginCertificate.getId());
                    List<com.intel.mtwilson.user.management.rest.v2.model.Role> rolesByUserLoginCertificateId = loginDAO.findRolesByUserLoginCertificateId(userLoginCertificate.getId());
                    for (com.intel.mtwilson.user.management.rest.v2.model.Role roleMapping : rolesByUserLoginCertificateId) {
                        log.debug("Removing role {} from user {}", roleMapping.getRoleName(), userName);
                        loginDAO.deleteUserLoginCertificateRole(userLoginCertificate.getId(), roleMapping.getId());
                    }

                    log.debug("ApiClientBO:Delete - About to delete the user login certificate entry for {}.", userName);
                    loginDAO.deleteUserLoginCertificateById(userLoginCertificate.getId());
                    log.info("ApiClientBO:Delete - Deleted the user login certificate entry for user {}.", userName);
                }

                log.debug("Looking up user {}", userName);
                User user = loginDAO.findUserByName(userName);
                if (user != null) {
                    log.debug("Found user {}", user.getId());
                    loginDAO.deleteUser(user.getId());
                    log.info("ApiClientBO:Delete - Deleted the user {} successfully.", userName);
                }

            } catch (Exception ex) {
                log.error("Error while deleting the V2 user tables. ", ex);
                throw new MSException(ErrorCode.MS_API_USER_REGISTRATION_ERROR, ex.getClass().getSimpleName());
            }
            
            
            // Capture the change in the syslog
            Object[] paramArray = {userName, Arrays.toString(apiClientRequest.roles), apiClientRequest.status};
            log.debug(sysLogMarker, "Deleted the user {} with roles: {} to {}.", paramArray);

        } catch (MSException me) {
            log.error("Error during API Client delete. " + me.getErrorMessage());
            throw me;
            
        } catch (Exception ex) {
            // throw new MSException(ex);
            log.error("Error during API user delete. ", ex);
            throw new MSException(ErrorCode.MS_API_USER_DELETION_ERROR, ex.getClass().getSimpleName());
        }
    }
        
    /**
     * 
     * @param apiClientRequest 
     */
    public void updateV2(ApiClientUpdateRequest apiClientRequest) {
        String userName;
        String userLoginCertificateFingerprint = Hex.encodeHexString(apiClientRequest.fingerprint);
        
        try {
            // Check if the user is in the V2 table. Otherwise we need to throw an error
            log.debug("Looking up user login certificate [{}]...", userLoginCertificateFingerprint);
            try (LoginDAO loginDao = MyJdbi.authz()) {
                UserLoginCertificate userLoginCertificate = loginDao.findUserLoginCertificateBySha256(apiClientRequest.fingerprint);
                if (userLoginCertificate == null) {
                    log.error("User with certificate fingerprint {} is not configured in the system.", userLoginCertificateFingerprint);
                    throw new MSException(ErrorCode.MS_USER_DOES_NOT_EXISTS, userLoginCertificateFingerprint);
                }
                
                User user = loginDao.findUserById(userLoginCertificate.getUserId());
                if (user == null) {
                    log.error("User with certificate fingerprint {} is not configured in the system.", userLoginCertificateFingerprint);
                    throw new MSException(ErrorCode.MS_USER_DOES_NOT_EXISTS, userLoginCertificateFingerprint);
                }
                userName = user.getUsername();
            }
            
            updateShiroUserTables(apiClientRequest, userName);

            // Capture the change in the syslog
            Object[] paramArray = {userName, Arrays.toString(apiClientRequest.roles), apiClientRequest.status};
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
    private ApiClientInfo toApiClientInfo(ApiClientX509 apiClientX509) throws SQLException, IOException {
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
        ArrayList<String> roleNames = new ArrayList<>();
        /*
        for(ApiRoleX509 role : apiClientX509.getApiRoleX509Collection()) {
            roleNames.add(role.getApiRoleX509PK().getRole());
        }
        */
        // set the roles array from new user login certificate tables
        try(LoginDAO dao = MyJdbi.authz()) {
            log.debug("searching for user login certificate with sha1 {}", Sha1Digest.digestOf(info.certificate).toHexString());
            UserLoginCertificate userLoginCertificate = dao.findUserLoginCertificateBySha1(Sha1Digest.digestOf(info.certificate).toByteArray());
            log.debug("found user login certificate {}", userLoginCertificate.getId().toString());
            List<com.intel.mtwilson.user.management.rest.v2.model.Role> v2roles = dao.findRolesByUserLoginCertificateId(userLoginCertificate.getId());
            log.debug("found {} roles", v2roles.size());
            for(com.intel.mtwilson.user.management.rest.v2.model.Role v2role : v2roles) {
                log.debug("found role name: {}", v2role.getRoleName());
                roleNames.add(v2role.getRoleName());
            }
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
        
        try(LoginDAO loginDAO = MyJdbi.authz()) {
            log.debug("Searching for users based on the criteria.");
            List<ApiClientInfo> list = new ArrayList<>();

            if( criteria.enabledEqualTo != null && criteria.statusEqualTo != null ) {
                List<UserLoginCertificate> userLoginCertificates = loginDAO.findUserLoginCertificatesByStatusAndEnabled(Status.valueOf(criteria.statusEqualTo), criteria.enabledEqualTo);
                if (userLoginCertificates != null && userLoginCertificates.size() > 0) {
                    for (UserLoginCertificate userLoginCertificate : userLoginCertificates) {
                        list.add(convertToApiClientInfo(userLoginCertificate));
                    }
                }
            } else if (criteria.statusEqualTo != null) {
                List<UserLoginCertificate> userLoginCertificates = loginDAO.findUserLoginCertificatesByStatus(Status.valueOf(criteria.statusEqualTo));
                if (userLoginCertificates != null && userLoginCertificates.size() > 0) {
                    for (UserLoginCertificate userLoginCertificate : userLoginCertificates) {
                        list.add(convertToApiClientInfo(userLoginCertificate));
                    }
                }
            } else if (criteria.enabledEqualTo != null) {
                List<UserLoginCertificate> userLoginCertificates = loginDAO.findUserLoginCertificatesByEnabled(criteria.enabledEqualTo);
                if (userLoginCertificates != null && userLoginCertificates.size() > 0) {
                    for (UserLoginCertificate userLoginCertificate : userLoginCertificates) {
                        list.add(convertToApiClientInfo(userLoginCertificate));
                    }
                }
            } else if (criteria.fingerprintEqualTo != null) {
                UserLoginCertificate userLoginCertificate = loginDAO.findUserLoginCertificateBySha256(criteria.fingerprintEqualTo);
                if (userLoginCertificate != null) {
                    list.add(convertToApiClientInfo(userLoginCertificate));
                }
            } else if (criteria.nameEqualTo != null) {
                UserLoginCertificate userLoginCertificate = loginDAO.findUserLoginCertificateByUsername(criteria.nameEqualTo);
                if (userLoginCertificate != null) {
                    list.add(convertToApiClientInfo(userLoginCertificate));
                }
            }
            
            return list;
        /*try {
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
            ArrayList<ApiClientInfo> response = new ArrayList<>();
            if( list == null ) {
                return response; // empty list
            }
            for(ApiClientX509 apiClientX509 : list) {
                response.add(toApiClientInfo(apiClientX509));
            }
            
            return response;*/
            
        } catch (MSException me) {
            log.error("Error during searching for the API Client information. " + me.getErrorMessage());            
            throw me;
            
        } catch (Exception ex) {
            // throw new MSException(ex);
            log.error("Error during search for API user. ", ex);
            throw new MSException(ErrorCode.MS_API_USER_SEARCH_ERROR, ex.getClass().getSimpleName());            
        }
    }    

    private String getSimpleNameFromCert(X509Certificate x509Certificate) {
        String certName = x509Certificate.getSubjectX500Principal().getName();
        certName = certName.substring((certName.indexOf("CN=")+(("CN=").length())), certName.indexOf(",OU="));
        return certName;
    }
    
    private ApiClientInfo convertToApiClientInfo(UserLoginCertificate userLoginCertificate) throws SQLException, IOException {
        ApiClientInfo info = new ApiClientInfo();
        info.certificate = userLoginCertificate.getCertificate();
        info.fingerprint = userLoginCertificate.getSha256Hash();
        info.issuer = userLoginCertificate.getX509Certificate().getIssuerDN().getName();
        info.serialNumber = userLoginCertificate.getX509Certificate().getSerialNumber().intValue();
        info.expires = userLoginCertificate.getExpires();
        info.enabled = userLoginCertificate.isEnabled();
        info.status = userLoginCertificate.getStatus().toString();
        info.comment = userLoginCertificate.getComment();
        // set the roles array
        ArrayList<String> roleNames = new ArrayList<>();

        try(LoginDAO dao = MyJdbi.authz()) {
            List<com.intel.mtwilson.user.management.rest.v2.model.Role> v2roles = dao.findRolesByUserLoginCertificateId(userLoginCertificate.getId());
            log.debug("found {} roles", v2roles.size());
            for(com.intel.mtwilson.user.management.rest.v2.model.Role v2role : v2roles) {
                log.debug("found role name: {}", v2role.getRoleName());
                roleNames.add(v2role.getRoleName());
            }
            User findUserById = dao.findUserById(userLoginCertificate.getUserId());
            if (findUserById != null)
                info.name = findUserById.getUsername();
        }
        info.roles = roleNames.toArray(new String[0]);
        return info;        
    }
}
