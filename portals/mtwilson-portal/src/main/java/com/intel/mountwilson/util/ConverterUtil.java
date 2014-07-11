/**
 * 
 */
package com.intel.mountwilson.util;

import com.intel.mountwilson.common.TDPConfig;
import com.intel.mountwilson.constant.HelperConstant;
import com.intel.mountwilson.datamodel.HostDetailsEntityVO;
import com.intel.mountwilson.datamodel.HostType;
import com.intel.mountwilson.datamodel.HostType.hostOS;
import com.intel.mountwilson.datamodel.HostType.hostVMM;
import com.intel.mountwilson.datamodel.MLEDataVO;
import com.intel.mountwilson.datamodel.MleDetailsEntityVO;
import com.intel.mountwilson.datamodel.OEMDataVO;
import java.util.Iterator;
import com.intel.mountwilson.datamodel.OSDataVO;
import com.intel.mountwilson.datamodel.TrustedHostVO;
import com.intel.mtwilson.TrustAssertion;
import com.intel.mtwilson.datatypes.ManifestData;
import com.intel.mtwilson.datatypes.MleData;
import com.intel.mtwilson.datatypes.OemData;
import com.intel.mtwilson.datatypes.OsData;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.saml.TrustAssertion.HostTrustAssertion;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
//import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


/**
 * @author yuvrajsx
 *
 */
public class ConverterUtil {
	//variable used to change date into given format to display on screen.
	//private static final DateFormat formatter=  new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
	
	public static String getOSAndVMMInfoString(MleDetailsEntityVO mleObject){
		return mleObject.getOsName()+" "+mleObject.getOsVersion()+HelperConstant.OS_VMM_INFORMATION_SEPERATOR+mleObject.getMleName()+":"+mleObject.getMleVersion();
	}

	public static TxtHost getTxtHostFromHostVO(HostDetailsEntityVO dataVO) {
		TxtHostRecord hostRecord = new TxtHostRecord();
		hostRecord.AddOn_Connection_String = dataVO.getvCenterDetails();
		hostRecord.BIOS_Name = dataVO.getBiosName();
		hostRecord.BIOS_Oem=dataVO.getOemName();
		hostRecord.BIOS_Version=dataVO.getBiosBuildNo();
		hostRecord.Description=dataVO.getHostDescription();
		hostRecord.Email=dataVO.getEmailAddress();
		hostRecord.HostName=dataVO.getHostName();
		hostRecord.IPAddress=dataVO.getHostName();
		hostRecord.Port=Integer.parseInt(dataVO.getHostPort());
        if( dataVO.getTlsPolicyId() != null && !dataVO.getTlsPolicyId().isEmpty() ) {
            TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
            tlsPolicyChoice.setTlsPolicyId(dataVO.getTlsPolicyId());
            hostRecord.tlsPolicyChoice = tlsPolicyChoice;
        }
        else if (dataVO.getTlsPolicyType() != null && !dataVO.getTlsPolicyType().isEmpty() ) {
            TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
            tlsPolicyDescriptor.setPolicyType(dataVO.getTlsPolicyType());
            if( dataVO.getTlsPolicyData() != null && !dataVO.getTlsPolicyData().isEmpty() ) {
                ArrayList<String> data = new ArrayList<>();
                data.add(dataVO.getTlsPolicyData());
                tlsPolicyDescriptor.setData(data);
            }
            TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
            tlsPolicyChoice.setTlsPolicyDescriptor(tlsPolicyDescriptor);
            hostRecord.tlsPolicyChoice = tlsPolicyChoice;
        }
		String[] osVMMInfo = dataVO.getVmmName().split(Pattern.quote(HelperConstant.OS_VMM_INFORMATION_SEPERATOR));
		String osNameWithVer = osVMMInfo[0];
		String osName;
		String osVer = "";
		StringBuffer res = new StringBuffer();
		String[] s = osNameWithVer.split(" ");
			osName = s[0];
			if (s.length == 2) {
				osVer = s[1];
			}else {
				for (int i = 1; i < s.length; i++) {
					res.append(s[i]+" ");
				}
			}
		
		String hypervisor = osVMMInfo[1];
		
		hostRecord.VMM_Name=hypervisor;
		hostRecord.VMM_OSName=osName;
		hostRecord.VMM_OSVersion=osVer;
		hostRecord.VMM_Version=dataVO.getVmmBuildNo();
		return new TxtHost(hostRecord);
	}

	
	public static TrustedHostVO getTrustedHostVoFromTrustAssertion(HostDetailsEntityVO hostDetailsEntityVO, TrustAssertion trustAssertion,String errorMessage){
		TrustedHostVO hostVO = new TrustedHostVO();
		hostVO.setHostName(hostDetailsEntityVO.getHostName());
		if (trustAssertion != null) {
        		HostTrustAssertion hostTrustAssertion = trustAssertion.getTrustAssertion(hostDetailsEntityVO.getHostName());  
			if (Boolean.parseBoolean(hostTrustAssertion.getStringAttribute(HelperConstant.Trusted_BIOS))) {
				hostVO.setBiosStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_TRUE));
			}else {
				hostVO.setBiosStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_FALSE));
			}
			if (Boolean.parseBoolean(hostTrustAssertion.getStringAttribute(HelperConstant.Trusted_VMM))) {
				hostVO.setVmmStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_TRUE));
			}else {
				hostVO.setVmmStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_FALSE));
			}
			
			if (Boolean.parseBoolean(hostTrustAssertion.getStringAttribute(HelperConstant.OVER_ALL_TRUSTRED))) {
				hostVO.setOverAllStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_TRUE));
				hostVO.setOverAllStatusBoolean(true);
			}else {
				hostVO.setOverAllStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_FALSE));
				hostVO.setOverAllStatusBoolean(false);
			}

                        if (hostTrustAssertion.getStringAttribute(HelperConstant.ASSET_TAG) != null){
                            if (Boolean.parseBoolean(hostTrustAssertion.getStringAttribute(HelperConstant.ASSET_TAG))) {
                                    hostVO.setAssetTagStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_TRUE));
//                                    String assetTagDetails = "";
                                    StringBuilder atdBuilder = new StringBuilder();
                                    // We need to retrive all the asset tag specific attributes and show it to the user
                                    Set<String> attributeNames = hostTrustAssertion.getAttributeNames();
                                    
                                    for (String attrName : attributeNames) {
                                        if (attrName.startsWith("ATAG") && !attrName.contains("UUID")) {
                                            atdBuilder.append(hostTrustAssertion.getStringAttribute(attrName) + "\n");
                                        }
                                    }
                                    hostVO.setAssetTagDetails(atdBuilder.toString());
                            }else {
                                    hostVO.setAssetTagStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_FALSE));
                                    hostVO.setAssetTagDetails("Un-Trusted");
                            }
                        } else {
                            hostVO.setAssetTagStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_UNKNOWN));
                            hostVO.setAssetTagDetails("Not verified");
                        }
                        
                        if(trustAssertion.error() != null){
                            hostVO.setErrorMessage(trustAssertion.error().getMessage());
                            hostVO.setErrorCode(1);
                        }
                
                        // Bug: 457 - Refresh button is not updating the time stamp
//                        hostVO.setUpdatedOn(trustAssertion.getDate().toString());
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
                        df.setTimeZone(TimeZone.getTimeZone("UTC"));
                        hostVO.setUpdatedOn(df.format(trustAssertion.getDate()));
                } else {
			hostVO.setBiosStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_UNKNOWN));
			hostVO.setVmmStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_UNKNOWN));
                        hostVO.setAssetTagStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_UNKNOWN));
			hostVO.setOverAllStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_UNKNOWN));
			hostVO.setOverAllStatusBoolean(false);
			hostVO.setErrorMessage(errorMessage);
                        // Bug: 445 - To shown the updated date when the host is in the unknown state
//                        hostVO.setUpdatedOn(new SimpleDateFormat("EEE MMM d HH:MM:ss z yyyy").format(new Date()));
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
                        df.setTimeZone(TimeZone.getTimeZone("UTC"));
                        hostVO.setUpdatedOn(df.format(new Date()));
                        hostVO.setErrorCode(1);                        
		}
		
		if (hostDetailsEntityVO.getVmmName() != null) {
			
			hostVO.setLocation(hostDetailsEntityVO.getLocation());
                        boolean skipAddingVMMImage = false; // We will use this flag for VMware and Citrix XenServer for which there is no separate OS & VMM.
			
			//Setting up a Image for os and hypervisor
			String s = hostDetailsEntityVO.getVmmName();
			String biosName = s.split(Pattern.quote(HelperConstant.OS_VMM_INFORMATION_SEPERATOR))[0];
			String hypervisor = s.split(Pattern.quote(HelperConstant.OS_VMM_INFORMATION_SEPERATOR))[1];
			
                        // Setting this flag will ensure that we process the VMs for that particular host type.
                        // Since we are supporting only VMware at this time, we are setting it to true only for
                        // hosts of VMware type.
			if ((biosName.toLowerCase().contains(HelperConstant.OS_IMAGE_VMWARE.toLowerCase()))){
				hostVO.setVmm(true);
			}else {
				hostVO.setVmm(false);
			}
			
			//getting all Host OS Type from enum
			hostOS[] hostOSTypes = HostType.hostOS.values();
			hostVO.setOsName("");
			for (hostOS hostOSType : hostOSTypes) {
				if (biosName.toLowerCase().contains(hostOSType.getValue().toLowerCase())) {
					hostVO.setOsName(TDPConfig.getConfiguration().getString(HelperConstant.IMAGES_ROOT_PATH)+hostOSType.getImageName());
                                        if (hostOSType.getVmmImageNeeded().contains("false"))
                                            skipAddingVMMImage = true;
                                        break;
				}
			}
			
			//getting all Host VMM Type from enum
			hostVMM[] hostVMMTypes = HostType.hostVMM.values();
			hostVO.setHypervisorName("");
			for (hostVMM hostOSType : hostVMMTypes) {
				if((hypervisor.toLowerCase().contains(hostOSType.getValue().toLowerCase())) &&
                                        (skipAddingVMMImage == false)){
					hostVO.setHypervisorName(TDPConfig.getConfiguration().getString(HelperConstant.IMAGES_ROOT_PATH)+hostOSType.getImageName());
                                        break;
				}
			}
			
			//hostVO.setUpdatedOn(formatter.format(hostDetailsEntityVO.getUpdatedOn()));//
//                        if (trustAssertion != null)
//                            hostVO.setUpdatedOn(trustAssertion.getDate().toString());
//                        else
//                            // Bug: 445 - To shown the updated date when the host is in the unknown state
//                            hostVO.setUpdatedOn(new SimpleDateFormat("EEE MMM d HH:MM:ss z yyyy").format(new Date()));
                            
			hostVO.setHostID(hostDetailsEntityVO.getHostId());
		}
		
		return hostVO;
	}
        
    public static String formateXMLString(String inputXML){
        StreamResult xmlOutput;
        try {
        Source xmlInput = new StreamSource(new StringReader(inputXML));
        StringWriter stringWriter = new StringWriter();
        xmlOutput = new StreamResult(stringWriter);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer(); 
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(xmlInput, xmlOutput);
    } catch (Exception e) {
        throw new RuntimeException(e); // simple exception handling, please review it
    }
        return xmlOutput.getWriter().toString();
    }
    
   public static HostDetailsEntityVO getHostVOObjectFromTxtHostRecord(TxtHostRecord txtHostDetail) {
    	HostDetailsEntityVO entityVO = new HostDetailsEntityVO();
		entityVO.setHostId(getConvertedHostName(txtHostDetail.HostName));
		entityVO.setHostName(txtHostDetail.HostName);
		entityVO.setHostIPAddress(txtHostDetail.HostName);
		entityVO.setHostPort(txtHostDetail.Port.toString());
		entityVO.setHostDescription(txtHostDetail.Description);
		entityVO.setBiosName(txtHostDetail.BIOS_Name);
		entityVO.setBiosBuildNo(txtHostDetail.BIOS_Version);
		entityVO.setVmmName(txtHostDetail.VMM_OSName+" "+txtHostDetail.VMM_OSVersion+HelperConstant.OS_VMM_INFORMATION_SEPERATOR+txtHostDetail.VMM_Name);
		entityVO.setVmmBuildNo(txtHostDetail.VMM_Version);
		entityVO.setvCenterDetails(txtHostDetail.AddOn_Connection_String);
		entityVO.setOemName(txtHostDetail.BIOS_Oem);
		entityVO.setLocation(txtHostDetail.Location);
		entityVO.setEmailAddress(txtHostDetail.Email);
        if( txtHostDetail.tlsPolicyChoice != null ) {
            if( txtHostDetail.tlsPolicyChoice.getTlsPolicyId() != null ) {
                entityVO.setTlsPolicyId(txtHostDetail.tlsPolicyChoice.getTlsPolicyId());
            }
            else if(  txtHostDetail.tlsPolicyChoice.getTlsPolicyDescriptor() != null ) {
                entityVO.setTlsPolicyType(txtHostDetail.tlsPolicyChoice.getTlsPolicyDescriptor().getPolicyType());
                if( txtHostDetail.tlsPolicyChoice.getTlsPolicyDescriptor().getData() != null ) {
                    Iterator<String> it = txtHostDetail.tlsPolicyChoice.getTlsPolicyDescriptor().getData().iterator();
                    if(it.hasNext()) {
                        entityVO.setTlsPolicyData(it.next());
                    }
                }
            }
        }
    	return entityVO;
    }

	private static String getConvertedHostName(String hostName) {
	String newHostID = hostName.replaceAll(Pattern.quote("."), "_");
	return newHostID.replaceAll(Pattern.quote(" "), "_");
}

	public static List<MleDetailsEntityVO> getMleVOListWhereOEMNotNull(List<MleData> mleDataList) {
		List<MleDetailsEntityVO> detailsEntityVOs = new ArrayList<MleDetailsEntityVO>();
		for (MleData data : mleDataList) {
			if (data.getOemName() != null && !(data.getOemName().length() == 0)) {
				MleDetailsEntityVO entityVO = new MleDetailsEntityVO();
				entityVO.setMleId(null);
				entityVO.setMleName(data.getName());
				entityVO.setMleVersion(data.getVersion());
				entityVO.setAttestationType(data.getAttestationType());
				entityVO.setMleType(data.getMleType());
				//entityVO.setManifestList(data.getManifestList().toString());
				entityVO.setOsName(data.getOsName());
				entityVO.setOsVersion(data.getOsVersion());
				entityVO.setOemName(data.getOemName());
				detailsEntityVOs.add(entityVO);
			}
		}
		return detailsEntityVOs;
	}

	public static List<MleDetailsEntityVO> getMleVOListWhereOEMIsNull(List<MleData> searchMLE) {
		List<MleDetailsEntityVO> detailsEntityVOs = new ArrayList<MleDetailsEntityVO>();
		for (MleData data : searchMLE) {
			if (data.getOemName() == null || data.getOemName().length() == 0) {
				MleDetailsEntityVO entityVO = new MleDetailsEntityVO();
				entityVO.setMleId(null);
				entityVO.setMleName(data.getName());
				entityVO.setMleVersion(data.getVersion());
				entityVO.setAttestationType(data.getAttestationType());
				entityVO.setMleType(data.getMleType());
				//entityVO.setManifestList(data.getManifestList().toString());
				entityVO.setOsName(data.getOsName());
				entityVO.setOsVersion(data.getOsVersion());
				entityVO.setOemName(data.getOemName());
				detailsEntityVOs.add(entityVO);
			}
		}
		return detailsEntityVOs;
	}

	public static List<HostDetailsEntityVO> getHostVOListFromTxtHostRecord(List<TxtHostRecord> txtHostDetails) {
		List<HostDetailsEntityVO> detailsEntityVOs = new ArrayList<HostDetailsEntityVO>();
		for (TxtHostRecord tblHostDetail : txtHostDetails) {
			detailsEntityVOs.add(getHostVOObjectFromTxtHostRecord(tblHostDetail));
		}
    	return detailsEntityVOs;
	}
    
    

	/**
	 * Method convert OS List from Api CLient into List OS OSDataVO.
	 * 
	 * @param osListFromApiClient
	 * @return
	 */
	public static List<OSDataVO> getListToOSDataVO(List<OsData> osListFromApiClient){
		List<OSDataVO> list = new ArrayList<OSDataVO>();
		for (OsData data : osListFromApiClient) {
			OSDataVO osVo = new OSDataVO();
			osVo.setOsName(data.getName());
			osVo.setOsVersion(data.getVersion());
			osVo.setOsDescription(data.getDescription());
			list.add(osVo);
		}
		return list;
	}
	
	public static List<MLEDataVO> getListToMLEDataVO(List<MleData> mleDateFromApiClient){
		List<MLEDataVO> list = new ArrayList<MLEDataVO>();
		for (MleData data : mleDateFromApiClient) {
			list.add(getMleDataVoObject(data));
		}
		return list;
	}
	
	public static MLEDataVO getMleDataVoObject(MleData data) {
		MLEDataVO mleVo = new MLEDataVO();
		mleVo.setMleName(data.getName());
		mleVo.setMleVersion(data.getVersion());
		mleVo.setMleDescription(data.getDescription());
		mleVo.setOsName(data.getOsName());
		mleVo.setOsVersion(data.getOsVersion());
		mleVo.setAttestation_Type(data.getAttestationType());
		mleVo.setOemName(data.getOemName());
		mleVo.setManifestList(getManifestListFromApiClient(data.getManifestList()));
		mleVo.setMleType(data.getMleType());
		return mleVo;
	}
	
	public static MleData getMleApiClientObject(MLEDataVO dataVO) {
		MleData data = new MleData();
		data.setName(dataVO.getMleName());
		data.setVersion(dataVO.getMleVersion());
		data.setMleType(dataVO.getMleType());
		data.setOemName(dataVO.getOemName());
		data.setOsName(dataVO.getOsName());
		data.setOsVersion(dataVO.getOsVersion());
		data.setAttestationType(dataVO.getAttestation_Type());
		data.setDescription(dataVO.getMleDescription());
		
		data.setManifestList(getApiClientManifestData(dataVO.getManifestList()));
		return data;
	}


	private static List<Map<String, String>> getManifestListFromApiClient(List<ManifestData> manifestList) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> manifest;
		if (manifestList != null) {
			for (ManifestData manifestData : manifestList) {
				manifest = new HashMap<String, String>();
				manifest.put("Name", manifestData.getName());
				manifest.put("Value", manifestData.getValue());
				list.add(manifest);
			}
		}
		return list;
	}

	private static List<ManifestData> getApiClientManifestData(List<Map<String, String>> manifestList) {
		List<ManifestData> list = new ArrayList<ManifestData>();
		if (manifestList != null) {
			for (Map<String,String> map : manifestList) {
				list.add(new ManifestData(map.get("name"), map.get("value")));
			}
		}
		return list;
	}
	
	public static Map<String, Object> getMLEDataVOToMap(MLEDataVO mleDataVO){
		Map<String, Object> mleRestServices = new HashMap<String, Object>();
		List<Map<String, String>> manifestList = new ArrayList<Map<String,String>>(); 
		mleRestServices.put("Name",mleDataVO.getMleName());
		mleRestServices.put("Version",mleDataVO.getMleVersion());
		mleRestServices.put("Description",mleDataVO.getMleDescription());
		mleRestServices.put("OsName",mleDataVO.getOsName());
		mleRestServices.put("OsVersion",mleDataVO.getOsVersion());
		mleRestServices.put("Attestation_Type",mleDataVO.getAttestation_Type());
		mleRestServices.put("OemName",mleDataVO.getOemName());
		mleRestServices.put("MLE_Type",mleDataVO.getMleType());
		
		for (Map<String, String> manifest : mleDataVO.getManifestList()) {
			Map<String, String> tem = new HashMap<String, String>();
			tem.put("Name", manifest.get("name"));
			tem.put("Value", manifest.get("value"));
			manifestList.add(tem);
		}
		mleRestServices.put("MLE_Manifests",manifestList);
		
		return mleRestServices;
	}
	
	public static Map<String, String> getOSDataVoMap(OSDataVO os){
		Map<String, String> map = new HashMap<String, String>();
		map.put("Name", os.getOsName());
		map.put("Version", os.getOsVersion());
		map.put("Description", os.getOsDescription());
		return map;
	}
	
	public static MultivaluedMap<String, String> getOSDataMultivaluedMap(OSDataVO os){
		//MultivaluedMap<String, String> map = new MultivaluedMapImpl();
                MultivaluedMap<String, String> map = new MultivaluedStringMap();
		map.add("Name", os.getOsName());
		map.add("Version", os.getOsVersion());
		map.add("Description", os.getOsDescription());
		return map;
	}

	public static List<OEMDataVO> getListToOEMDataVO(List<OemData> oemList) {
		List<OEMDataVO> list = new ArrayList<OEMDataVO>();
		for (OemData data : oemList) {
			OEMDataVO osVo = new OEMDataVO();
			osVo.setOemName(data.getName());
			osVo.setOemDescription(data.getDescription());
			list.add(osVo);
		}
		return list;
	}
	
	public static Map<String, String> getOEMDataVoToMap(OEMDataVO oem){
		Map<String, String> map = new HashMap<String, String>();
		map.put("Name", oem.getOemName());
		map.put("Description", oem.getOemDescription());
		return map;
	}    
}
