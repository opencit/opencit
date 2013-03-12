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
import com.intel.mountwilson.datamodel.MleDetailsEntityVO;
import com.intel.mountwilson.datamodel.TrustedHostVO;
import com.intel.mtwilson.TrustAssertion;
import com.intel.mtwilson.datatypes.MleData;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
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
		hostRecord.IPAddress=dataVO.getHostIPAddress();
		hostRecord.Port=Integer.parseInt(dataVO.getHostPort());
		
		String[] osVMMInfo = dataVO.getVmmName().split(Pattern.quote(HelperConstant.OS_VMM_INFORMATION_SEPERATOR));
		String osNameWithVer = osVMMInfo[0];
		String osName = null;
		String osVer = "";
		
		String[] s = osNameWithVer.split(" ");
			osName = s[0];
			if (s.length == 2) {
				osVer = s[1];
			}else {
				for (int i = 1; i < s.length; i++) {
					osVer+=s[i]+" ";
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
			if (Boolean.parseBoolean(trustAssertion.getStringAttribute(HelperConstant.Trusted_BIOS))) {
				hostVO.setBiosStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_TRUE));
			}else {
				hostVO.setBiosStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_FALSE));
			}
			if (Boolean.parseBoolean(trustAssertion.getStringAttribute(HelperConstant.Trusted_VMM))) {
				hostVO.setVmmStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_TRUE));
			}else {
				hostVO.setVmmStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_FALSE));
			}
			
			if (Boolean.parseBoolean(trustAssertion.getStringAttribute(HelperConstant.OVER_ALL_TRUSTRED))) {
				hostVO.setOverAllStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_TRUE));
				hostVO.setOverAllStatusBoolean(true);
			}else {
				hostVO.setOverAllStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_FALSE));
				hostVO.setOverAllStatusBoolean(false);
			}
                        if(trustAssertion.error() != null){
                            hostVO.setErrorMessage(trustAssertion.error().getMessage());
                            hostVO.setErrorCode(1);
                        }
                
                        // Bug: 457 - Refresh button is not updating the time stamp
                        hostVO.setUpdatedOn(trustAssertion.getDate().toString());

                } else {
			hostVO.setBiosStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_UNKNOWN));
			hostVO.setVmmStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_UNKNOWN));
			hostVO.setOverAllStatus(TDPConfig.getConfiguration().getString(HelperConstant.IMAGE_TRUSTED_UNKNOWN));
			hostVO.setOverAllStatusBoolean(false);
			hostVO.setErrorMessage(errorMessage);
                        // Bug: 445 - To shown the updated date when the host is in the unknown state
                        hostVO.setUpdatedOn(new SimpleDateFormat("EEE MMM d HH:MM:ss z yyyy").format(new Date()));
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
                                        if (hostOSType.getVmmImageNeeded().toString() == "false")
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
			
			// TODO : add date once available from saml assertion for time been its empty.
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
        StreamResult xmlOutput = null;
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
		entityVO.setHostIPAddress(txtHostDetail.IPAddress);
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
    	return entityVO;
    }

	private static String getConvertedHostName(String hostName) {
	String newHostID = hostName.replaceAll(Pattern.quote("."), "_");
	return newHostID.replaceAll(Pattern.quote(" "), "_");
}

	public static List<MleDetailsEntityVO> getMleVOListWhereOEMNotNull(List<MleData> mleDataList) {
		List<MleDetailsEntityVO> detailsEntityVOs = new ArrayList<MleDetailsEntityVO>();
		for (MleData data : mleDataList) {
			if (data.getOemName() != null && !(data.getOemName().equals(""))) {
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
			if (data.getOemName() == null || data.getOemName().equals("")) {
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
}
