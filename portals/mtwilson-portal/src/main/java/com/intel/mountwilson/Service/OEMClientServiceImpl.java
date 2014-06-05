/**
 * This Class contains all Method for OEM Component (Add, Edit, View and Delete)
 */
package com.intel.mountwilson.Service;

import com.intel.mountwilson.common.WLMPortalException;
import com.intel.mountwilson.datamodel.OEMDataVO;
import com.intel.mountwilson.util.ConnectionUtil;
import com.intel.mountwilson.util.ConverterUtil;
import com.intel.mtwilson.api.*;
import com.intel.mtwilson.datatypes.OemData;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yuvraj Singh
 *
 */
public class OEMClientServiceImpl implements IOEMClientService {
    
        Logger log = LoggerFactory.getLogger(getClass().getName());
	
	/**
	 * Method to get List of all OEMs .
	 * 
	 * @param apiClientServices : object of WhitelistService Interface.
	 * @return List of OEMDataVO Objects.
	 * @throws WLMPortalException
	 */
	@Override
	public List<OEMDataVO> getAllOEM(WhitelistService apiClientServices) throws WLMPortalException {
            log.info("OEMClientServiceImpl.getAllOEM >>");
            try {
                List<OEMDataVO> list = ConverterUtil.getListToOEMDataVO(apiClientServices.listAllOEM());
                log.info("OEMClientServiceImpl.getAllOEM <<");
                return list;
            } catch (Exception e) {
                log.error(e.getMessage());
                throw ConnectionUtil.handleWLMPortalException(e);
            }
	}

	/**
	 * Method to add OEM Type into a Rest Services
	 * 
	 * @param dataVO
	 * @param apiClientServices
	 * @return Boolean variable e.g true if OEM is added successfully 
	 * @throws WLMPortalException
	 */
	@Override
	public boolean addOEMInfo(OEMDataVO dataVO, WhitelistService apiClientServices) throws WLMPortalException {
            log.info("OEMClientServiceImpl.addOEMInfo >>");
            try {
                apiClientServices.addOEM(new OemData(dataVO.getOemName(), dataVO.getOemDescription()));
                boolean result = true;
                log.info("OEMClientServiceImpl.addOEMInfo <<");
                return result;
            } catch (Exception e) {
                log.error(e.getMessage());
                throw ConnectionUtil.handleWLMPortalException(e);
            }
	}

	
	/**
	 * Method to update OEM into a Rest Services
	 * 
	 * @param dataVO
	 * @param apiClientServices
	 * @return Boolean variable e.g true if OEM is updated successfully 
	 * @throws WLMPortalException
	 */
	@Override
	public boolean updateOEMInfo(OEMDataVO dataVO,WhitelistService apiClientServices) throws WLMPortalException {
            log.info("OEMClientServiceImpl.updateOEMInfo >>");
            try {
                apiClientServices.updateOEM(new OemData(dataVO.getOemName(), dataVO.getOemDescription()));
                boolean result = true;
                log.info("OEMClientServiceImpl.updateOEMInfo <<");
                return result;
            } catch (Exception e) {
                log.error(e.getMessage());
                throw ConnectionUtil.handleWLMPortalException(e);
            }
	}

	/**
	 * Method to delete OEM into a Rest Services
	 * 
	 * @param dataMap
	 * @param apiClientServices
	 * @return Boolean variable e.g true if OEM is deleted successfully 
	 * @throws WLMPortalException
	 */
	@Override
	public boolean deleteOEM(OEMDataVO dataVO,WhitelistService apiClientServices) throws WLMPortalException {
            log.info("OEMClientServiceImpl.deleteOEM >>");
            try {
                System.out.println(apiClientServices + "----" + dataVO.getOemName());
                boolean result = apiClientServices.deleteOEM(dataVO.getOemName());
                log.info("OEMClientServiceImpl.deleteOEM <<");
                return result;
            } catch (Exception e) {
                log.error(e.getMessage());
                throw ConnectionUtil.handleWLMPortalException(e);
            }
	}
}
