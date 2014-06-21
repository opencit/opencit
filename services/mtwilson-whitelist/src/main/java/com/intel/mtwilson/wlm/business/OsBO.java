/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.wlm.business;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblOsJpaController;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.data.TblOs;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.datatypes.OsData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author dsmagadx
 */
public class OsBO {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MleBO.class);
    TblOsJpaController tblOsJpaController = null;

    public OsBO() {
        try {
            tblOsJpaController = My.jpa().mwOs();
        } catch (IOException ex) {
            log.error("Error during persistence manager initialization", ex);
            throw new ASException(ErrorCode.SYSTEM_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     *
     * @return
     */
    public List<OsData> getAllOs() {
        List<OsData> allOsData = new ArrayList<OsData>();
        try {
            List<TblOs> allRecords = tblOsJpaController.findTblOsEntities();

            for (TblOs tblOs : allRecords) {
                OsData osData = new OsData(tblOs.getName(), tblOs.getVersion(), tblOs.getDescription());
                allOsData.add(osData);
            }

        } catch (ASException ase) {
            throw ase;
        } catch (Exception e) {
            log.error("Error during retrieval of OS details.", e);
            throw new ASException(ErrorCode.WS_OS_RETRIEVAL_ERROR, e.getClass().getSimpleName());
        }
        return allOsData;
    }

    /**
     *
     * @param osData
     * @return
     */
    public String updateOs(OsData osData, String uuid) {
        TblOs tblOs;
        try {
            // Feature: 917 - Added support for UUID
            if (uuid != null && !uuid.isEmpty()) {
                tblOs = tblOsJpaController.findTblOsByUUID(uuid);
            } else {
                tblOs = tblOsJpaController.findTblOsByNameVersion(osData.getName(), osData.getVersion());
            }

            if (tblOs == null) {
                throw new ASException(ErrorCode.WS_OS_DOES_NOT_EXIST, osData.getName(), osData.getVersion());
            }

            tblOs.setDescription(osData.getDescription());

            tblOsJpaController.edit(tblOs);

        } catch (ASException ase) {
            throw ase;
        } catch (Exception e) {
            log.error("Error during OS update.", e);
            throw new ASException(ErrorCode.WS_OS_UPDATE_ERROR, e.getClass().getSimpleName());
        }
        return "true";
    }

    /**
     *
     * @param osData
     * @return
     */
    public String createOs(OsData osData, String uuid) {
        try {
            TblOs tblOs = tblOsJpaController.findTblOsByNameVersion(osData.getName(), osData.getVersion());

            if (tblOs != null) {
                throw new ASException(ErrorCode.WS_OS_ALREADY_EXISTS, osData.getName(), osData.getVersion());
            }

            tblOs = new TblOs();
            tblOs.setName(osData.getName());
            tblOs.setVersion(osData.getVersion());
            tblOs.setDescription(osData.getDescription());
            // Feature: 917 - Added support for UUID
            if (uuid != null && !uuid.isEmpty()) {
                tblOs.setUuid_hex(uuid);
            } else {
                tblOs.setUuid_hex(new UUID().toString());
            }

            tblOsJpaController.create(tblOs);

        } catch (ASException ase) {
            throw ase;
        } catch (Exception e) {
            log.error("Error during OS creation.", e);
            throw new ASException(ErrorCode.WS_OS_CREATE_ERROR, e.getClass().getSimpleName());
        }
        return "true";
    }

    /**
     *
     * @param osName
     * @param osVersion
     * @return
     */
    public String deleteOs(String osName, String osVersion, String uuid) {
        TblOs tblOs;

        try {
            // Feature: 917 - Added support for UUID
            if (uuid != null && !uuid.isEmpty()) {
                tblOs = tblOsJpaController.findTblOsByUUID(uuid);
            } else {
                tblOs = tblOsJpaController.findTblOsByNameVersion(osName, osVersion);
            }

            if (tblOs == null) {
                throw new ASException(ErrorCode.WS_OS_DOES_NOT_EXIST, osName, osVersion);
            }

            Collection<TblMle> tblMleCollection = tblOs.getTblMleCollection();
            if (tblMleCollection != null) {
                log.debug("OS is currently associated with # MLEs: " + tblMleCollection.size());

                if (!tblMleCollection.isEmpty()) {
                    throw new ASException(ErrorCode.WS_OS_ASSOCIATION_EXISTS, osName, osVersion, tblMleCollection.size());
                }
            }
            tblOsJpaController.destroy(tblOs.getId());

        } catch (ASException ase) {
            throw ase;
        } catch (Exception e) {
            log.error("Error during OS deletion.", e);
            throw new ASException(ErrorCode.WS_OS_DELETE_ERROR, e.getClass().getSimpleName());
        }
        return "true";
    }
}
