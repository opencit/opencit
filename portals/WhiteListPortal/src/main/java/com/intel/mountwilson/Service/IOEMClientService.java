/**
 * 
 */
package com.intel.mountwilson.Service;

import java.util.List;

import com.intel.mountwilson.common.WLMPortalException;
import com.intel.mountwilson.datamodel.OEMDataVO;
import com.intel.mtwilson.WhitelistService;

/**
 * @author yuvrajsx
 *
 */
public interface IOEMClientService {
		
	public List<OEMDataVO> getAllOEM(WhitelistService apiClientServices) throws WLMPortalException;
	public boolean addOEMInfo(OEMDataVO dataVO, WhitelistService apiClientServices) throws WLMPortalException;
	public boolean updateOEMInfo(OEMDataVO dataVO, WhitelistService apiClientServices) throws WLMPortalException;
	public boolean deleteOEM(OEMDataVO dataVO, WhitelistService apiClientServices) throws WLMPortalException;
}
