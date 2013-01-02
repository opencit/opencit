/**
 * 
 */
package com.intel.mountwilson.Service;

import java.util.List;

import com.intel.mountwilson.common.WLMPortalException;
import com.intel.mountwilson.datamodel.MLEDataVO;
import com.intel.mtwilson.WhitelistService;
import com.intel.mtwilson.datatypes.ModuleWhiteList;

/**
 * @author yuvrajsx
 *
 */
public interface IMLEClientService {
	
	public List<MLEDataVO> getAllMLE(WhitelistService apiClientServices) throws WLMPortalException;
	public boolean addMLEInfo(MLEDataVO dataVO, WhitelistService apiClientServices) throws WLMPortalException;
	public boolean updateMLEInfo(MLEDataVO dataVO, WhitelistService apiClientServices) throws WLMPortalException;
	MLEDataVO getSingleMleData(MLEDataVO dataVO,WhitelistService apiClientServices)throws WLMPortalException;
	boolean deleteMLE(MLEDataVO dataVO,WhitelistService apiClientServices)throws WLMPortalException;
        List<ModuleWhiteList> getManifestListForModuleTypeMle(MLEDataVO dataVO,WhitelistService apiClientServices) throws WLMPortalException;
        String getMleSourceHost(MLEDataVO dataVO,WhitelistService apiClientServices)throws WLMPortalException;

}
