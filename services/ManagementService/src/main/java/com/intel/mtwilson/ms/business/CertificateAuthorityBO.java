/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.business;

import com.intel.mtwilson.crypto.Password;
import com.intel.mtwilson.ms.helper.BaseBO;
import com.intel.mtwilson.ms.common.MSException;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.controller.ApiRoleX509JpaController;
import com.intel.mtwilson.ms.controller.MwConfigurationJpaController;
import com.intel.mtwilson.ms.controller.exceptions.MSDataException;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.ms.controller.exceptions.PreexistingEntityException;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.mtwilson.ms.data.ApiRoleX509;
import com.intel.mtwilson.ms.data.ApiRoleX509PK;
import com.intel.mtwilson.ms.data.MwConfiguration;
import com.intel.mtwilson.util.CertUtils;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    
    public static final String CA_PASSWORD_CONF_KEY = "mtwilson.ca.password";
    public static final String CA_ENABLED_CONF_KEY = "mtwilson.ca.enabled";

    public CertificateAuthorityBO() {
    }

    public void enableCaWithPassword(Password newPassword) {
        try {
            mwConfigurationJPA.setMwConfiguration(CA_PASSWORD_CONF_KEY, newPassword.toString());
            mwConfigurationJPA.setMwConfiguration(CA_ENABLED_CONF_KEY, Boolean.TRUE.toString());
        } catch (NonexistentEntityException ex) {
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Cannot enable CA: " + ex.getMessage());
        } catch (Exception ex) {
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Cannot enable CA: " + ex.getMessage());
        }
    }

    public void disableCa() {
        try {
            mwConfigurationJPA.setMwConfiguration(CA_PASSWORD_CONF_KEY, "");
            mwConfigurationJPA.setMwConfiguration(CA_ENABLED_CONF_KEY, Boolean.FALSE.toString());
        } catch (NonexistentEntityException ex) {
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Cannot disable CA: " + ex.getMessage());
        } catch (Exception ex) {
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Cannot disable CA: " + ex.getMessage());
        }
    }
}
