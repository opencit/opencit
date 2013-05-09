/**
 * 
 */
package com.intel.mountwilson.Service;

import com.intel.mountwilson.common.WLMPortalException;
import com.intel.mountwilson.datamodel.OEMDataVO;
import com.intel.mtwilson.api.*;
import java.util.List;

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
