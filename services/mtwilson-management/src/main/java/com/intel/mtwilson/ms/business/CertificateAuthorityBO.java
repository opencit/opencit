/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.ms.business;

import com.intel.mtwilson.My;
import com.intel.mtwilson.as.data.MwCertificateX509;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.ms.common.MSException;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author dsmagadx
 */
public class CertificateAuthorityBO {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificateAuthorityBO.class);
        
    public static final String CA_PASSWORD_CONF_KEY = "mtwilson.ca.password";
    public static final String CA_ENABLED_CONF_KEY = "mtwilson.ca.enabled";
    public static final String MTWILSON_ROOT_CA_PURPOSE = "MTWILSON_ROOT_CA";

    public CertificateAuthorityBO() {
    }

//    public void enableCaWithPassword(PasswordHash newPassword) {
//        try {
//            mwConfigurationJPA.setMwConfiguration(CA_PASSWORD_CONF_KEY, newPassword.toString());
//            mwConfigurationJPA.setMwConfiguration(CA_ENABLED_CONF_KEY, Boolean.TRUE.toString());
//        } catch (NonexistentEntityException ex) {
//            log.error("Error enabling CA. ", ex);
//            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Cannot enable CA: " + ex.getMessage());
//        } catch (Exception ex) {
//            log.error("Error enabling CA. ", ex);
//            // throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Cannot enable CA: " + ex.getMessage());
//            throw new MSException(ErrorCode.MS_CA_ENABLE_ERROR, ex.getClass().getSimpleName());
//        }
//    }
//
//    public void disableCa() {
//        try {
//            mwConfigurationJPA.setMwConfiguration(CA_PASSWORD_CONF_KEY, "");
//            mwConfigurationJPA.setMwConfiguration(CA_ENABLED_CONF_KEY, Boolean.FALSE.toString());
//        } catch (NonexistentEntityException ex) {
//            log.error("Error disabling CA. ", ex);
//            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Cannot disable CA: " + ex.getMessage());
//        } catch (Exception ex) {
//            log.error("Error disabling CA. ", ex);
//            // throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Cannot disable CA: " + ex.getMessage());
//            throw new MSException(ErrorCode.MS_CA_DISABLE_ERROR, ex.getClass().getSimpleName());
//        }
//    }
    
    public MwCertificateX509 getCaCertificate() {
        try {
            List<MwCertificateX509> list = My.jpa().mwCertificateX509().findCertificateByCommentLike(MTWILSON_ROOT_CA_PURPOSE);
            if( list.isEmpty() ) { return null; }
            MwCertificateX509 first = list.get(0);
            return first;
        } catch (IOException ex) {
            log.error("Error during retrieval of CA root certificate.", ex);
            throw new MSException(ErrorCode.MS_ROOT_CA_CERT_ERROR, ex.getClass().getSimpleName());
        }
    }
}
