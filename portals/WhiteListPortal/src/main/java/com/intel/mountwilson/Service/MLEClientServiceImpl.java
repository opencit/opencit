/**
 * This Class contains all Method for MLE Component (Add, Edit, View and Delete)
 */
package com.intel.mountwilson.Service;

import java.util.List;
import com.intel.mountwilson.common.WLMPortalException;
import com.intel.mountwilson.datamodel.MLEDataVO;
import com.intel.mountwilson.util.ConnectionUtil;
import com.intel.mountwilson.util.ConverterUtil;
import com.intel.mtwilson.WhitelistService;
import com.intel.mtwilson.datatypes.MLESearchCriteria;
import com.intel.mtwilson.datatypes.ModuleWhiteList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Yuvraj Singh
 *
 */
public class MLEClientServiceImpl implements IMLEClientService {
        Logger log = LoggerFactory.getLogger(getClass().getName());
	
	public MLEClientServiceImpl(){
		
	}
	
	
	/**
	 * Method to get List of all MLEs types.
	 * 
	 * @param apiClientServices
	 * @return
	 * @throws WLMPortalException
	 */
	@Override
	public List<MLEDataVO> getAllMLE(WhitelistService apiClientServices) throws WLMPortalException {
		log.info("MLEClientServiceImpl.getAllMLE >>");
		List<MLEDataVO> list = null;
       try {
    	   list =ConverterUtil.getListToMLEDataVO(apiClientServices.searchMLE(""));
       }catch (Exception e) {
			throw ConnectionUtil.handleException(e);
       }
       log.info("MLEClientServiceImpl.getAllMLE <<");
       return list;
	}

	/**
	 * Method to add MLE Type into a rest services using Api CLient Object.
	 * 
	 * @param dataVO : contain details of MLE to be Added.
	 * @param apiClientServices
	 * @return
	 * @throws WLMPortalException 
	 */
	@Override
	public boolean addMLEInfo(MLEDataVO dataVO,WhitelistService apiClientServices) throws WLMPortalException {
		log.info("MLEClientServiceImpl.addMLEInfo >>");
		boolean result = false;
		try {
			result = apiClientServices.addMLE(ConverterUtil.getMleApiClientObject(dataVO));
		} catch (Exception e) {
			log.error(e.getMessage());
			/* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */
			throw ConnectionUtil.handleException(e);
			/* Soni_End_17/09/2012_issue_for_consistent_Error_Message  */
			//throw new WLMPortalException(e.getMessage(),e);
		}
        log.info("MLEClientServiceImpl.addMLEInfo <<");
       	
       	return result;
	}

	/**
	 * Method to update MLE Type into a rest services
	 * 
	 * @param dataVO
	 * @param apiClientServices
	 * @return Boolean variable e.g true if MLE is updated successfully 
	 * @throws WLMPortalException
	 */
	@Override
	public boolean updateMLEInfo(MLEDataVO dataVO,WhitelistService apiClientServices) throws WLMPortalException {
		log.info("MLEClientServiceImpl.updateMLEInfo >>");
		boolean result = false;
		try {
			result =apiClientServices.updateMLE(ConverterUtil.getMleApiClientObject(dataVO));
		} catch (Exception e) {
			log.error(e.getMessage());
			/* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */
			throw ConnectionUtil.handleException(e);
			/* Soni_End_17/09/2012_issue_for_consistent_Error_Message  */
			//throw new WLMPortalException("Error While Updating MLE Data.  "+e.getMessage(),e);
		}
		log.info("MLEClientServiceImpl.updateMLEInfo <<");
		return result;
	}

	/**
	 * Method to delete MLE Type from a rest services
	 * 
	 * @param dataVO
	 * @param apiClientServices
	 * @return Boolean variable e.g true if MLE is deleted successfully 
	 * @throws WLMPortalException
	 */
	@Override
	public boolean deleteMLE(MLEDataVO dataVO,WhitelistService apiClientServices) throws WLMPortalException {
		log.info("MLEClientServiceImpl.deleteMLE >>");
		boolean result = false; 
		try {
			MLESearchCriteria criteria = new MLESearchCriteria();
			criteria.mleName = dataVO.getMleName();
    	    criteria.mleVersion = dataVO.getMleVersion();
    	    if (dataVO.getOemName() != null) {
				criteria.oemName = dataVO.getOemName();
				criteria.osName = "";
				criteria.osVersion = "";
			}else {
				criteria.osName = dataVO.getOsName();
				criteria.osVersion = dataVO.getOsVersion();
				criteria.oemName = "";
			}
			result = apiClientServices.deleteMLE(criteria);
		} catch (Exception e) {
			log.error(e.getMessage());
			//throw new WLMPortalException("Error While Deleting  Mle data.  "+e.getMessage(),e);
			/* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */
			throw ConnectionUtil.handleException(e);
			/* Soni_End_17/09/2012_issue_for_consistent_Error_Message  */

			
		}
       

       log.info("MLEClientServiceImpl.deleteMLE <<");
       	return result;
	}
	
	/**
	 * Method to get Single MLE Details from a rest services.
	 * 
	 * @param dataVO
	 * @param apiClientServices
	 * @return
	 * @throws WLMPortalException
	 */
	@Override
	public MLEDataVO getSingleMleData(MLEDataVO dataVO,WhitelistService apiClientServices) throws WLMPortalException {
		log.info("MLEClientServiceImpl.getSingleMleData >>");
		MLEDataVO mleObject = null;
       try {
    	   MLESearchCriteria criteria = new MLESearchCriteria();
    	   criteria.mleName = dataVO.getMleName();
    	   criteria.mleVersion = dataVO.getMleVersion();
    	   if (dataVO.getOemName() != null) {
    		   criteria.oemName = dataVO.getOemName();
    		   criteria.osName = "";
    		   criteria.osVersion = "";
    	   }else{
    		   criteria.osName = dataVO.getOsName();
    		   criteria.osVersion = dataVO.getOsVersion();
    		   criteria.oemName = "";
    	   }
    	   mleObject = ConverterUtil.getMleDataVoObject(apiClientServices.getMLEManifest(criteria));
       }catch (Exception e) {
			throw ConnectionUtil.handleException(e);
      }
       log.info("MLEClientServiceImpl.getSingleMleData <<");
       return mleObject;
	}
        
        /**
	 * Method to get Single MLE Details from a rest services.
	 * 
	 * @param dataVO
	 * @param apiClientServices
	 * @return
	 * @throws WLMPortalException
	 */
	@Override
	public List<ModuleWhiteList> getManifestListForModuleTypeMle(MLEDataVO dataVO,WhitelistService apiClientServices) throws WLMPortalException {
		log.info("MLEClientServiceImpl.ManifestListForModuleTypeMle >>");
                 List<ModuleWhiteList> moduleManifestList = null;
       try {
    	 moduleManifestList =  apiClientServices.listModuleWhiteListForMLE(dataVO.getMleName(), dataVO.getMleVersion(), dataVO.getOsName(), dataVO.getOsVersion(), "");
       }catch (Exception e) {
    	   /* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */
			throw ConnectionUtil.handleException(e);
			/* Soni_End_17/09/2012_issue_for_consistent_Error_Message  */
			//throw new WLMPortalException("Error While Getting White List For Module Type. Error "+e.getMessage(),e);
      }
       log.info("MLEClientServiceImpl.ManifestListForModuleTypeMle <<");
       return moduleManifestList;
	}
	
}
