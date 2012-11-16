/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.wlm.business;

import com.intel.mtwilson.as.controller.TblOemJpaController;
import com.intel.mtwilson.as.data.TblOem;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.OemData;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.wlm.helper.BaseBO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author dsmagadx
 */
public class OemBO extends BaseBO {

    Logger log = LoggerFactory.getLogger(getClass().getName());
    TblOemJpaController tblOemJpaController = null;

    public OemBO() {
        tblOemJpaController = new TblOemJpaController(getEntityManagerFactory());
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

        }catch(ASException ase){
            throw ase;
        } catch (Exception e) {
//            throw new ASException(ErrorCode.SYSTEM_ERROR, "Error while fetching OEM data." + e.getMessage(), e);
            throw new ASException(e);
        }


        return allOemData;
    }

    /**
     * 
     * @param oemData
     * @return 
     */
    public String updateOem(OemData oemData) {

        try {
            TblOem tblOem = tblOemJpaController.findTblOemByName(oemData.getName());
            
            if(tblOem == null)
                throw new ASException(ErrorCode.WS_OEM_DOES_NOT_EXIST, String.format(ErrorCode.WS_OEM_DOES_NOT_EXIST.getMessage(),
                    oemData.getName()));
            
            tblOem.setDescription(oemData.getDescription());
            
            tblOemJpaController.edit(tblOem);
            
        } catch(ASException ase){
            throw ase;
        } catch (Exception e) {
//            throw new ASException(ErrorCode.SYSTEM_ERROR, String.format("Error while updating OEM '%s'. %s", 
//                    oemData.getName(), e.getMessage()), e);
            
            throw new ASException(e);
        }
        return "true";
    }

    /**
     * 
     * @param oemData
     * @return 
     */
    public String createOem(OemData oemData) {
        try {
            TblOem tblOem = tblOemJpaController.findTblOemByName(oemData.getName());
            
            if(tblOem != null)
                throw new ASException(ErrorCode.WS_OEM_ALREADY_EXISTS, oemData.getName());
            
            tblOem = new TblOem();
            tblOem.setName(oemData.getName());
            tblOem.setDescription(oemData.getDescription());
            
            tblOemJpaController.create(tblOem);
            
        } catch(ASException ase){
            throw ase;
        } catch (Exception e) {
//            throw new ASException(ErrorCode.SYSTEM_ERROR, String.format("Error while creating OEM '%s'. %s ", 
//                    oemData.getName(), e.getMessage()), e);
            throw new ASException(e);
        }
        return "true";
    }

    /**
     * 
     * @param osName
     * @return 
     */
    public String deleteOem(String osName) {
        try{
            TblOem tblOem = tblOemJpaController.findTblOemByName(osName);
            
            if(tblOem == null){
                throw new ASException(ErrorCode.WS_OEM_DOES_NOT_EXIST, osName);
            }
            
            Collection<TblMle> tblMleCollection = tblOem.getTblMleCollection();
            if( tblMleCollection != null ) {
                log.info("OEM is currently associated with # MLEs: " + tblMleCollection.size());
            
                if(!tblMleCollection.isEmpty()){
                    throw new ASException(ErrorCode.WS_OEM_ASSOCIATION_EXISTS, osName, tblMleCollection.size());
                }
            }
            
            tblOemJpaController.destroy(tblOem.getId());
            } catch(ASException ase){
                throw ase;
                 
            } catch (Exception e) {
//                 throw new ASException(ErrorCode.SYSTEM_ERROR, String.format("Error while deleting OEM '%s'. %s ", 
//                         osName, e.getMessage()), e);
                throw new ASException(e);
            }
        return "true";
    }
}
