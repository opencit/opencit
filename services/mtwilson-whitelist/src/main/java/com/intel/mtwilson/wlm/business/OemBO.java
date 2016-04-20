/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.wlm.business;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.controller.TblOemJpaController;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.data.TblOem;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.datatypes.OemData;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author dsmagadx
 */
public class OemBO {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OemBO.class);
    TblOemJpaController tblOemJpaController = null;

    public OemBO() {
        try {
            tblOemJpaController = My.jpa().mwOem();
        } catch (IOException ex) {
            log.error("Error during persistence manager initialization", ex);
            throw new ASException(ErrorCode.SYSTEM_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     *
     * @return
     */
    public List<OemData> getAllOem() {
        List<OemData> allOemData = new ArrayList<OemData>();
        try {
            List<TblOem> allRecords = tblOemJpaController.findTblOemEntities();

            for (TblOem tblOem : allRecords) {
                OemData oemData = new OemData(tblOem.getName(), tblOem.getDescription());
                allOemData.add(oemData);
            }

        } catch (ASException ase) {
            throw ase;
        } catch (Exception e) {
            log.error("Error during retrieval of OEM details.", e);
            throw new ASException(ErrorCode.WS_OEM_RETRIEVAL_ERROR, e.getClass().getSimpleName());
        }


        return allOemData;
    }

    /**
     *
     * @param oemData
     * @return
     */
    public String updateOem(OemData oemData, String uuid) {
        TblOem tblOem;
        try {
            // Feature: 917 - Added support for UUID
            if (uuid != null && !uuid.isEmpty()) {
                tblOem = tblOemJpaController.findTblOemByUUID(uuid);
            } else {
                tblOem = tblOemJpaController.findTblOemByName(oemData.getName());
            }

            if (tblOem == null) {
                throw new ASException(ErrorCode.WS_OEM_DOES_NOT_EXIST, oemData.getName());
            }

            tblOem.setDescription(oemData.getDescription());

            tblOemJpaController.edit(tblOem);

        } catch (ASException ase) {
            throw ase;
        } catch (Exception e) {
            log.error("Error during OEM update.", e);
            throw new ASException(ErrorCode.WS_OEM_UPDATE_ERROR, e.getClass().getSimpleName());
        }
        return "true";
    }

    /**
     *
     * @param oemData
     * @return
     */
    public String createOem(OemData oemData, String uuid) {
        try {
            TblOem tblOem = tblOemJpaController.findTblOemByName(oemData.getName());

            if (tblOem != null) {
                throw new ASException(ErrorCode.WS_OEM_ALREADY_EXISTS, oemData.getName());
            }

            tblOem = new TblOem();
            tblOem.setName(oemData.getName());
            tblOem.setDescription(oemData.getDescription());
            // Feature: 917 - Added support for UUID
            if (uuid != null && !uuid.isEmpty()) {
                tblOem.setUuid_hex(uuid);
            } else {
                tblOem.setUuid_hex(new UUID().toString());
            }

            tblOemJpaController.create(tblOem);

        } catch (ASException ase) {
            throw ase;
        } catch (Exception e) {
            log.error("Error during OEM creation.", e);
            throw new ASException(ErrorCode.WS_OEM_CREATE_ERROR, e.getClass().getSimpleName());
        }
        return "true";
    }

    /**
     *
     * @param oemName
     * @return
     */
    public String deleteOem(String oemName, String uuid) {
        TblOem tblOem;
        try {
            // Feature: 917 - Added support for UUID
            if (uuid != null && !uuid.isEmpty()) {
                tblOem = tblOemJpaController.findTblOemByUUID(uuid);
            } else {
                tblOem = tblOemJpaController.findTblOemByName(oemName);
            }

            if (tblOem == null) {
                throw new ASException(ErrorCode.WS_OEM_DOES_NOT_EXIST, oemName);
            }

            Collection<TblMle> tblMleCollection = tblOem.getTblMleCollection();
            if (tblMleCollection != null) {
                log.debug("OEM is currently associated with # MLEs: " + tblMleCollection.size());

                if (!tblMleCollection.isEmpty()) {
                    throw new ASException(ErrorCode.WS_OEM_ASSOCIATION_EXISTS, oemName, tblMleCollection.size());
                }
            }

            tblOemJpaController.destroy(tblOem.getId());
        } catch (ASException ase) {
            throw ase;

        } catch (Exception e) {
            log.error("Error during OEM deletion.", e);
            throw new ASException(ErrorCode.WS_OEM_DELETE_ERROR, e.getClass().getSimpleName());
        }
        return "true";
    }
}
