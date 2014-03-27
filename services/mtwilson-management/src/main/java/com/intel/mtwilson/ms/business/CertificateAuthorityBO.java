/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.ms.business;

import com.intel.mtwilson.as.controller.MwCertificateX509JpaController;
import com.intel.mtwilson.as.data.MwCertificateX509;
import com.intel.dcsg.cpg.crypto.PasswordHash;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.ms.common.MSException;
import com.intel.mtwilson.ms.controller.MwConfigurationJpaController;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.ms.BaseBO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
public class CertificateAuthorityBO extends BaseBO {

    private Logger log = LoggerFactory.getLogger(getClass());
    private MwConfigurationJpaController mwConfigurationJPA = new MwConfigurationJpaController(getMSEntityManagerFactory());
    private MwCertificateX509JpaController mwX509CertificateJPA = new MwCertificateX509JpaController(getMSEntityManagerFactory());
    
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
        List<MwCertificateX509> list = mwX509CertificateJPA.findCertificateByCommentLike(MTWILSON_ROOT_CA_PURPOSE);
        if( list.isEmpty() ) { return null; }
        MwCertificateX509 first = list.get(0);
        return first;
    }
}
