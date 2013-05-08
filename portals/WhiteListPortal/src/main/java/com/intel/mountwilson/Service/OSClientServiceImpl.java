/**
 * This class contains methods use to get or update Rest Services for OS Component.
 */
package com.intel.mountwilson.Service;

import com.intel.mountwilson.common.WLMPortalException;
import com.intel.mountwilson.datamodel.OSDataVO;
import com.intel.mountwilson.util.ConnectionUtil;
import com.intel.mountwilson.util.ConverterUtil;
import com.intel.mtwilson.WhitelistService;
import com.intel.mtwilson.datatypes.OsData;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
/**
 * @author Yuvraj Singh
 *
 */
@Service
public class OSClientServiceImpl implements IOSClientService {

        Logger log = LoggerFactory.getLogger(getClass().getName());
	
	public OSClientServiceImpl(){
		
	}
	
	/**
	 * Method to get List of all OS.
	 * 
	 * @param apiClientServices
	 * @return List of OSDataVO Objects.
	 * @throws WLMPortalException
	 */
	@Override
	public List<OSDataVO> getAllOS(WhitelistService apiClientServices) throws WLMPortalException {
                                log.info("OSClientServiceImpl.getAllOS >>");
                                List<OSDataVO> list = null;
                                try {
                                        list =ConverterUtil.getListToOSDataVO(apiClientServices.listAllOS());
                                }catch (Exception e) {
                                                        throw ConnectionUtil.handleException(e);
                                }
                                log.info("OSClientServiceImpl.getAllOS <<");
                                return list;
	}

	
	/**
	 * Method to add OS Type into a rest services
	 * 
	 * @param dataVO
	 * @param apiClientServices
	 * @return Boolean variable e.g true if OS is added successfully 
	 * @throws WLMPortalException
	 */
	@Override
	public boolean addOSInfo(OSDataVO dataVO,WhitelistService apiClientServices) throws WLMPortalException {
		log.info("OSClientServiceImpl.addOSInfo >>");
		boolean result = false;
		try {
			result = apiClientServices.addOS(new OsData(dataVO.getOsName(), dataVO.getOsVersion(), dataVO.getOsDescription()));
		} catch (Exception e) {
			log.error(e.getMessage());
			throw ConnectionUtil.handleException(e);
		}
		log.info("OSClientServiceImpl.addOSInfo <<");
       	return result;
	}

	/**
	 * Method to update OS Type into a rest services
	 * 
	 * @param dataVO
	 * @param apiClientServices
	 * @return Boolean variable e.g true if OS is updated successfully 
	 * @throws WLMPortalException
	 */
	@Override
	public boolean updateOSInfo(OSDataVO dataVO,WhitelistService apiClientServices) throws WLMPortalException {
		log.info("OSClientServiceImpl.updateOSInfo >>");
		
		boolean result = false;
		try {
			result = apiClientServices.updateOS(new OsData(dataVO.getOsName(), dataVO.getOsVersion(), dataVO.getOsDescription()));
		} catch (Exception e) {
			log.error(e.getMessage());
			throw ConnectionUtil.handleException(e);
		}
		log.info("OSClientServiceImpl.updateOSInfo <<");
       	return result;
	}

	/**
	 * Method to delete OS Type from a rest services
	 * 
	 * @param dataMap
	 * @param apiClientServices
	 * @return Boolean variable e.g true if OS is deleted successfully 
	 * @throws WLMPortalException
	 */
	@Override
	public boolean deleteOS(OSDataVO dataVO,WhitelistService apiClientServices) throws WLMPortalException {
		log.info("OSClientServiceImpl.deleteOS >>");
		boolean result = false;
		try {
			result = apiClientServices.deleteOS(new OsData(dataVO.getOsName(), dataVO.getOsVersion(), dataVO.getOsDescription()));
			System.out.println(result);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw ConnectionUtil.handleException(e);
		}
		log.info("OSClientServiceImpl.deleteOS <<");
       	return result;
	}
	
}
