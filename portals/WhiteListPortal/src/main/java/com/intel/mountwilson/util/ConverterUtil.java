/**
 * 
 */
package com.intel.mountwilson.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import com.intel.mountwilson.datamodel.MLEDataVO;
import com.intel.mountwilson.datamodel.OEMDataVO;
import com.intel.mountwilson.datamodel.OSDataVO;
import com.intel.mtwilson.datatypes.ManifestData;
import com.intel.mtwilson.datatypes.MleData;
import com.intel.mtwilson.datatypes.OemData;
import com.intel.mtwilson.datatypes.OsData;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * @author yuvrajsx
 *
 */
public class ConverterUtil {

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
		MultivaluedMap<String, String> map = new MultivaluedMapImpl();
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
