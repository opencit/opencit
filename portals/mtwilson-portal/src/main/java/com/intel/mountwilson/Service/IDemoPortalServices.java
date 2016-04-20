/**
 * 
 */
package com.intel.mountwilson.Service;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import com.intel.mountwilson.common.DemoPortalException;
import com.intel.mountwilson.datamodel.HostDetailsEntityVO;
import com.intel.mountwilson.datamodel.HostReportTypeVO;
import com.intel.mountwilson.datamodel.HostVmMappingVO;
import com.intel.mountwilson.datamodel.TrustedHostVO;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.api.*;
import com.intel.mtwilson.datatypes.PcrLogReport;

/**
 * @author yuvrajsx
 *
 */
public interface IDemoPortalServices {

	/**
	 * @param hostList
	 * @param apiClientServices
	 * @param trustedCertificates
	 * @return
	 * @throws DemoPortalException
	 */
	public List<TrustedHostVO> getTrustStatusForHost(List<HostDetailsEntityVO> hostList,AttestationService apiClientServices,X509Certificate[] trustedCertificates) throws DemoPortalException;

	/**
	 * @param hostName
	 * @param apiClientServices
	 * @param trustedCertificates
	 * @return
	 * @throws DemoPortalException
	 */
	public TrustedHostVO getSingleHostTrust(String hostName,AttestationService apiClientServices, X509Certificate[] trustedCertificates) throws DemoPortalException;

	/**
	 * @param client
	 * @return
	 * @throws DemoPortalException
	 */
	Map<String, List<Map<String, String>>> getAllOemInfo(ApiClient client)	throws DemoPortalException;

	/**
	 * @param client
	 * @return
	 * @throws DemoPortalException
	 */
	Map<String, Boolean> getOSAndVMMInfo(ApiClient client) throws DemoPortalException;

	/**
	 * @param dataVO
	 * @param apiClientServices
	 * @return
	 * @throws DemoPortalException
	 */
	public boolean saveNewHostData(HostDetailsEntityVO dataVO, AttestationService apiClientServices) throws DemoPortalException;

	/**
	 * @param dataVO
	 * @param apiClientServices
	 * @return
	 * @throws DemoPortalException
	 */
	public boolean updateHostData(HostDetailsEntityVO dataVO, AttestationService apiClientServices)throws DemoPortalException;

	/**
	 * @param hostID
	 * @param hostName
	 * @param apiClientServices
	 * @param vmMappingData
	 * @return
	 * @throws DemoPortalException
	 */
	public boolean deleteHostDetails(String hostID, String hostName,AttestationService apiClientServices, Map<String, HostVmMappingVO> vmMappingData)throws DemoPortalException;

	/**
	 * @param hostName
	 * @param hostID
	 * @param vmMappingData
	 * @param service
	 * @return
	 * @throws DemoPortalException
	 */
	public List<HostVmMappingVO> getVMsForHost(String hostName,String hostID, Map<String, HostVmMappingVO> vmMappingData,AttestationService service) throws DemoPortalException;

	/**
	 * @param hostName
	 * @param vmName
	 * @param hostID
	 * @param isPowerOnCommand
	 * @param service
	 * @return
	 * @throws DemoPortalException
	 */
	public boolean powerOnOffHostVMs(String hostName, String vmName,String hostID, boolean isPowerOnCommand,AttestationService service)throws DemoPortalException;

	/**
	 * @param vmName
	 * @param sourceHost
	 * @param hostToTransfer
	 * @param hostID
	 * @param service
	 * @return
	 * @throws DemoPortalException
	 */
	public boolean migrateVMToHost(String vmName,String sourceHost, String hostToTransfer, String hostID,AttestationService service)throws DemoPortalException;

	/**
	 * @param service
	 * @return
	 * @throws DemoPortalException
	 */
	List<HostDetailsEntityVO> getHostListFromDB(AttestationService service) throws DemoPortalException;
	
	HostDetailsEntityVO getSingleHostDetailFromDB(String hostName, AttestationService service) throws DemoPortalException;

	boolean getBlukTrustUpdatedForHost(List<String> hostNames,AttestationService apiClientServices,X509Certificate[] trustedCertificates) throws DemoPortalException;

	/**
	 * @param hostName
	 * @param apiClientServices
	 * @param trustedCertificates
	 * @return
	 * @throws DemoPortalException
	 */
	String trustVerificationDetails(String hostName,AttestationService apiClientServices,X509Certificate[] trustedCertificates) throws DemoPortalException;
        
    public List<HostReportTypeVO> getHostTrustReport(List<String> hostNames,ApiClient client)throws DemoPortalException;

	/**
	 * This method is used to get failure report for Host.
	 * 
	 * @param hostName
	 * @param attestationService
	 * @return
	 * @throws DemoPortalException
	 * @throws Exception
	 */
	public List<PcrLogReport> getFailureReportData(String hostName,ApiClient attestationService) throws DemoPortalException, Exception;
        
        /**
         * Retrieves available i18n locales
         * 
         * @param ManagementService
	 * @return
	 * @throws DemoPortalException
         */
        public String[] getLocales(ManagementService apiClientServices) throws DemoPortalException;
        
        /**
         * Returns locale for specified portal user.
         * 
         * @param username
         * @param apiclient
         * @return
         * @throws DemoPortalException 
         */
        public String getLocale(String username, ApiClient apiclient) throws DemoPortalException;
        
        /**
         * Sets locale for specified portal user.
         * 
         * @param username
         * @param locale
         * @param apiclient
         * @return
         * @throws DemoPortalException 
         */
        public String setLocale(String username, String locale, ApiClient apiclient) throws DemoPortalException;
}

