/**
 * This controller is used to get data for viewing in JSP Elements 
 * e.g. List of all MLEs.
 */
package com.intel.mountwilson.controller;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.intel.mountwilson.Service.IMLEClientService;
import com.intel.mountwilson.Service.IOEMClientService;
import com.intel.mountwilson.Service.IOSClientService;
import com.intel.mountwilson.common.WLMPConfig;
import com.intel.mountwilson.common.WLMPortalException;
import com.intel.mountwilson.datamodel.MLEDataVO;
import com.intel.mountwilson.datamodel.OEMDataVO;
import com.intel.mountwilson.datamodel.OSDataVO;
import com.intel.mountwilson.datamodel.VmmHostDataVo;
import com.intel.mountwilson.util.JSONView;
import com.intel.mtwilson.WhitelistService;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * @author Yuvraj Singh
 *
 */
public class WLMDataController extends MultiActionController {
	
	// variable declaration Used for Logging.
        Logger log = LoggerFactory.getLogger(getClass().getName());	
        
	//Services layer objects use to call into services layer for bussiness logic.
	private IOSClientService osClientService;
	private IMLEClientService mleClientService;
	private IOEMClientService oemClientService; 
	
	//No Parameter Constructor
	public WLMDataController(){
		
	}
	
	/**
	 * Method to add OS Data into REST Services.
	 * 
	 * @param req (HttpServletRequest Object)
	 * @param res (HttpServletResponse Object)
	 * @return
	 */
	public ModelAndView addOSData(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.addOSData >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		try {
			OSDataVO dataVONew = new OSDataVO();
			
			//Getting OS data from request parameter
			dataVONew.setOsName(req.getParameter("osName"));
			dataVONew.setOsVersion(req.getParameter("osVersion"));
			dataVONew.setOsDescription(req.getParameter("osDescription"));
			
			//Calling into Service Layer(OSClientServiceImpl) to add OS Data.
			responseView.addObject("result",osClientService.addOSInfo(dataVONew,getWhitelistService(req)));
		} catch (WLMPortalException e) {
			log.error("Error Wile Adding OS Data. Root cause "+e.getMessage());
			responseView.addObject("result",false);
			responseView.addObject("message",StringEscapeUtils.escapeHtml(e.getMessage()));
		}
		log.info("WLMDataController.addOSData <<<");
		return responseView;
	}
	
	/**
	 * Method to Update previously add OS Data into a REST Services.
	 * 
	 *@param req (HttpServletRequest Object)
	 * @param res (HttpServletResponse Object)
	 * @return
	 */
	public ModelAndView updateOSData(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.updateOSData >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		int selectedPage ;
		try {
			
			//Get Current select page no used in pagination.
			selectedPage = Integer.parseInt(req.getParameter("selectedPageNo"));
			
			//Get updated OS data from req object and create OSDataVO Object from it.
			OSDataVO dataVONew = new OSDataVO();
			dataVONew.setOsName(req.getParameter("osName"));
			dataVONew.setOsVersion(req.getParameter("osVer"));
			dataVONew.setOsDescription(req.getParameter("inputDec"));
			
			//Calling into Service Layer(OSClientServiceImpl) to update OS Data.
			boolean updateDone = osClientService.updateOSInfo(dataVONew,getWhitelistService(req));
			
			// Once OS data is updated, get List of all OS for a current page to show while pagination.  
			if (updateDone) {
				//Get map view of OS data from Services based on there page no.
				Map<Integer, List<OSDataVO>> mapOfData = getPartitionListOfAllOS(req);
				
				responseView.addObject("OSDataVo", mapOfData.get(selectedPage));
				responseView.addObject("noOfPages", mapOfData.size());
				
				responseView.addObject("result",updateDone);
			}else{
				log.error("Error Wile Editing OS Data. Api Client return false.");
				responseView.addObject("result",false);
				responseView.addObject("message","Error Wile Editing OS Data. Api Client return false.");
			}
		} catch (WLMPortalException e) {
			log.error("Error Wile Editing OS Data. Root cause "+e.getMessage());
			responseView.addObject("result",false);
			responseView.addObject("message",StringEscapeUtils.escapeHtml(e.getMessage()));
		}
		log.info("WLMDataController.updateOSData <<<");
		return responseView;
	}
	
	/**
	 * @param req (HttpServletRequest Object)
	 * @param res (HttpServletResponse Object)
	 * @return
	 */
	public ModelAndView deleteOSData(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.deleteOSData >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		int selectedPage ;
		try {
			selectedPage = Integer.parseInt(req.getParameter("selectedPageNo"));
			OSDataVO dataVONew = new OSDataVO();
			dataVONew.setOsName(req.getParameter("osName"));
			dataVONew.setOsVersion(req.getParameter("osVer"));
			dataVONew.setOsDescription(req.getParameter("inputDec"));
			
			boolean updateDone = osClientService.deleteOS(dataVONew,getWhitelistService(req));
			if (updateDone) {
				Map<Integer, List<OSDataVO>> mapOfData = getPartitionListOfAllOS(req);
				
				if (selectedPage > mapOfData.size()) {
					selectedPage = mapOfData.size();
				}
				responseView.addObject("OSDataVo", mapOfData.get(selectedPage));
				responseView.addObject("noOfPages", mapOfData.size());
				responseView.addObject("result",updateDone);
			}else {
				log.error("Error Wile deleting OS Data. Server Error.");
				responseView.addObject("result",false);
				responseView.addObject("message","Api Client return false.");
			}
			
		} catch (WLMPortalException e) {
			log.error("Error Wile deleting OS Data. Root cause "+e.getMessage());
			responseView.addObject("result",false);
			responseView.addObject("message",StringEscapeUtils.escapeHtml(e.getMessage()));
		}
		log.info("WLMDataController.deleteOSData <<<");
		return responseView;
		
	}
	
	public ModelAndView getHostOSForVMM(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.getHostOSForVMM >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		List<VmmHostDataVo> list = new ArrayList<VmmHostDataVo>();
		VmmHostDataVo dataVo =null;
		List<String> VmmNames =  getVMMNameList(WLMPConfig.getConfiguration().getString("mtwilson.wlmp.openSourceHypervisors"));
		
		try {
			List<OSDataVO> osList = osClientService.getAllOS(getWhitelistService(req));
			for (OSDataVO osDataVO : osList) {
				dataVo = new VmmHostDataVo();
				dataVo.setHostOS(osDataVO.getOsName());
				dataVo.setHostVersion(osDataVO.getOsVersion());
				if (osDataVO.getOsName().toLowerCase().contains(WLMPConfig.getConfiguration().getString("mtwilson.wlmp.moduleAttestation").toLowerCase())) {
					dataVo.setVmmNames(getVMMNameList(WLMPConfig.getConfiguration().getString("mtwilson.wlmp.vmwareHypersior")));
					if (osDataVO.getOsVersion().toLowerCase().contains(WLMPConfig.getConfiguration().getString("mtwilson.wlmp.moduleAttestationVersion").toLowerCase())) {
						dataVo.setAttestationType("MODULE");
					}else {
						dataVo.setAttestationType("PCR");
					}
				}else {
					dataVo.setVmmNames(VmmNames);
					dataVo.setAttestationType("PCR");
				}
				list.add(dataVo);
			}
		} catch (WLMPortalException e) {
			log.error("Error While getting Host OS Data for VMM. Root cause "+e.getStackTrace());
			responseView.addObject("result",false);
			responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
			return responseView;
		}
		responseView.addObject("HostList",list);
		responseView.addObject("result",true);
		responseView.addObject("message","");
		log.info("WLMDataController.getHostOSForVMM <<<");
		return responseView;
		
	}
	
	public ModelAndView getHostOSForBios(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.getHostOSForBios >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		
		List<OEMDataVO> list = null;
		try {
			list = oemClientService.getAllOEM(getWhitelistService(req));
			responseView.addObject("result",true);
		} catch (WLMPortalException e) {
			log.error("Error While getting Host OS Data for BIOS. Root cause "+e.getStackTrace());
			responseView.addObject("result",false);
			responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
		}
		responseView.addObject("HostList",list);
		responseView.addObject("message","");
		
		log.info("WLMDataController.getHostOSForBios <<<");
		return responseView;
		
	}
	
	public ModelAndView getUploadedMenifestFile(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.getUploadedMenifestFile >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		Object manifest = req.getSession().getAttribute("manifestValue");
		boolean result = false;
		if ( manifest != null) {
			result = true;
			responseView.addObject("manifestValue",manifest);
		}
		responseView.addObject("result",result);
		log.info("WLMDataController.getUploadedMenifestFile <<<");
		return responseView;
		
	}
	
	public ModelAndView uploadManifest(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.uploadManifest >>");
		req.getSession().removeAttribute("manifestValue");
		ModelAndView responseView = new ModelAndView(new JSONView());
		List<Map<String, String>> manifestValue = new ArrayList<Map<String,String>>();
		
		// Check that we have a file upload request
		boolean isMultipart = ServletFileUpload.isMultipartContent(req);
		System.out.println(isMultipart);
		if (!isMultipart) {
			responseView.addObject("result",false);
			return responseView;
		}
		
		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		try {
			@SuppressWarnings("unchecked")
			List<FileItem>  items = upload.parseRequest(req);
			
			// Process the uploaded items
			Iterator<FileItem> iter = items.iterator();
			while (iter.hasNext()) {
			    FileItem item = (FileItem) iter.next();

			    if (!item.isFormField()) {
			    	String lines[] = item.getString().split("\\r?\\n");
			    	for (String values : lines) {
			    		if (values.length() > 2) {
			    			String val[] = values.split(":");
				    		if (val.length == 2) {
				    			Map<String, String> manifest = new HashMap<String, String>();
				    			manifest.put(val[0], val[1]);
				    			manifestValue.add(manifest);
							}else {
								responseView.addObject("result",false);
								return responseView;
							}
						}
					}
			    }
			}
		log.info("Uploaded Content :: "+manifestValue.toString());
		req.getSession().setAttribute("manifestValue",manifestValue);
		/*responseView.addObject("manifestValue",manifestValue);*/
		responseView.addObject("result",manifestValue.size() > 0 ? true : false);
			
		} catch (FileUploadException e) {
			e.printStackTrace();
			responseView.addObject("result",false);
		}catch (Exception e) {
			e.printStackTrace();
			responseView.addObject("result",false);
		}
		
		log.info("WLMDataController.uploadManifest <<<");
		return responseView;
		
	}
	
	public ModelAndView viewSingleMLEData(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.viewSingleMLEData>>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		MLEDataVO dataVO = new MLEDataVO();
                MLEDataVO detailMLEVO = null;
                
		try {
			dataVO.setMleName(req.getParameter("mleName"));
			dataVO.setMleVersion(req.getParameter("mleVersion"));
                        dataVO.setAttestation_Type(req.getParameter("attestation_Type"));
			
                        String mleType = req.getParameter("mleType");
            
			if (mleType != null && mleType.equalsIgnoreCase("VMM")) {
				dataVO.setOsName(req.getParameter("osName"));
				dataVO.setOsVersion(req.getParameter("osVersion"));
			}else {
				dataVO.setOemName(req.getParameter("oemName"));
			}
		} catch (Exception e) {
			log.error("Error While in request parameters Data. "+e.getMessage());
			responseView.addObject("result",false);
			responseView.addObject("message","Error While request parameters are Null. Please check.");
		}
		
		try {
                        // TODO: Had to temporarily store the detailed MLE object so that it can be reused to retrieve the mleSource details.
                        // The MleData object expects the MLE_Type detail to be present to get the OS/OEM details. Need to fix this
                        detailMLEVO = mleClientService.getSingleMleData(dataVO,getWhitelistService(req));
			responseView.addObject("dataVo", detailMLEVO);
                        responseView.addObject("result",true);
		} catch (WLMPortalException e) {
			responseView.addObject("result",false);
			responseView.addObject("message",StringEscapeUtils.escapeHtml(e.getMessage()));
			log.error(e.toString());
		}
		
                // Now that we got the details of the MLE, we need to get the host details that
                // was used for white listing this MLE.
		try {
			responseView.addObject("mleSource", mleClientService.getMleSourceHost(detailMLEVO,getWhitelistService(req)));
		} catch (WLMPortalException e) {
			responseView.addObject("result",false);
			responseView.addObject("message",StringEscapeUtils.escapeHtml(e.getMessage()));
			log.error(e.toString());
		}

                log.info("WLMDataController.viewSingleMLEData <<<");
		return responseView;		
	}
        
        public ModelAndView getWhiteListForMle(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.getWhiteListForMle>>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		MLEDataVO dataVO = new MLEDataVO();
		try {
			dataVO.setMleName(req.getParameter("mleName"));
			dataVO.setMleVersion(req.getParameter("mleVersion"));
            dataVO.setAttestation_Type(req.getParameter("attestation_Type"));
            
            String mleType = req.getParameter("mleType");
            
			if (mleType != null && mleType.equalsIgnoreCase("VMM")) {
				dataVO.setOsName(req.getParameter("osName"));
				dataVO.setOsVersion(req.getParameter("osVersion"));
			}else {
				dataVO.setOemName(req.getParameter("oemName"));
			}
		} catch (Exception e) {
			log.error("Error While in request parameters Data. "+e.getMessage());
			responseView.addObject("result",false);
			responseView.addObject("message","Error While request parameters are Null. Please check.");
		}
		
		try {
                        responseView.addObject("whiteList",mleClientService.getManifestListForModuleTypeMle(dataVO,getWhitelistService(req)));
                        responseView.addObject("result",true);
		} catch (WLMPortalException e) {
			responseView.addObject("result",false);
			responseView.addObject("message",StringEscapeUtils.escapeHtml(e.getMessage()));
			log.error(e.toString());
		}
		
		log.info("WLMDataController.getWhiteListForMle <<<");
		return responseView;
		
	}
	
	@SuppressWarnings("serial")
	public ModelAndView getAddMle(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.getAddMle>>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		String mleOb = null;
		boolean newMle = false;
		try {
			mleOb = req.getParameter("mleObject");
			newMle = Boolean.parseBoolean(req.getParameter("newMle"));
		} catch (Exception e1) {
			log.error("Error While in request parameters Data. "+e1.getMessage());
			responseView.addObject("result",false);
			responseView.addObject("message","Error While request parameters are Null. Please check.");
		}
		System.out.println(mleOb);
		MLEDataVO dataVO = new MLEDataVO();
		
		
		try {
			Type mleDataType = new TypeToken<MLEDataVO>() {}.getType();
			dataVO = new Gson().fromJson(mleOb, mleDataType);
			
			for (Map<String, String> manifestMap : dataVO.getManifestList()) {
				for (Entry<String, String>  manifest : manifestMap.entrySet()) {
					manifest.setValue(manifest.getValue().toUpperCase());
				}
			}
			
			System.out.println("dataVo >>"+dataVO);
			if (newMle) {
				responseView.addObject("result", mleClientService.addMLEInfo(dataVO,getWhitelistService(req)));
			}else {
				responseView.addObject("result", mleClientService.updateMLEInfo(dataVO,getWhitelistService(req)));
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			responseView.addObject("result",false);
			responseView.addObject("message",StringEscapeUtils.escapeHtml(e.getMessage()));
			return responseView;
		}
		
		log.info("WLMDataController.getAddMle <<<");
		return responseView;
		
	}
	
	public ModelAndView deleteMLEData(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.deleteMLEData>>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		MLEDataVO dataVO = new MLEDataVO();
		int selectedPage ;
		try {
			selectedPage = Integer.parseInt(req.getParameter("selectedPageNo"));
			dataVO.setMleName(req.getParameter("mleName"));
			dataVO.setMleVersion(req.getParameter("mleVersion"));
			
			String mleType = req.getParameter("mleType");
			
			if (mleType != null && mleType.equalsIgnoreCase("VMM")) {
				dataVO.setOsName(req.getParameter("osName"));
				dataVO.setOsVersion(req.getParameter("osVersion"));
			}else {
				dataVO.setOemName(req.getParameter("oemName"));
			}
		} catch (Exception e) {
			log.error("Error While in request parameters Data. "+e.getMessage());
			responseView.addObject("result",false);
			responseView.addObject("message","Error While request parameters are Null. Please check.");
			return responseView;
		}
		
		try {
			boolean deleteDone = mleClientService.deleteMLE(dataVO,getWhitelistService(req));
			if (deleteDone) {
				Map<Integer, List<MLEDataVO>> mapOfData = getPartitionListOfAllMle(req);
				
				if (selectedPage > mapOfData.size()) {
					selectedPage = mapOfData.size();
				}
				
				responseView.addObject("MLEDataVo", mapOfData.get(selectedPage));
				responseView.addObject("noOfPages", mapOfData.size());
				responseView.addObject("result",true);
			}
		} catch (WLMPortalException e) {
			responseView.addObject("result",false);
			responseView.addObject("message",StringEscapeUtils.escapeHtml(e.getMessage()));
			log.error(e.toString());
			return responseView;
		}
		
		log.info("WLMDataController.deleteMLEData <<<");
		return responseView;
		
	}
	
	public ModelAndView addOEMData(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.addOEMData>>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		
		try {
			OEMDataVO dataVONew = new OEMDataVO();
			dataVONew.setOemName(req.getParameter("oemName"));
			dataVONew.setOemDescription(req.getParameter("oemDescription"));
			
			System.out.println("New OEM data >>> "+dataVONew.toString());
			responseView.addObject("result",oemClientService.addOEMInfo(dataVONew,getWhitelistService(req)));
			
		} catch (WLMPortalException e) {
			log.error("Error Wile Adding OEM Data. Root cause "+e.getStackTrace());
			responseView.addObject("result",false);
			responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
		}
		
		log.info("WLMDataController.addOEMData <<<");
		return responseView;
		
	}
	
	public ModelAndView updateOEMData(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.updateOEMData >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		int selectedPage ;
		try {
			selectedPage = Integer.parseInt(req.getParameter("selectedPageNo"));
			OEMDataVO dataVONew = new OEMDataVO();
			dataVONew.setOemName(req.getParameter("oemName"));
			dataVONew.setOemDescription(req.getParameter("inputDec"));
			boolean updateDone = oemClientService.updateOEMInfo(dataVONew,getWhitelistService(req));
			if (updateDone) {
				Map<Integer, List<OEMDataVO>> mapOfData = getPartitionListOfAllOEM(req);
				
				responseView.addObject("OEMDataVo", mapOfData.get(selectedPage));
				responseView.addObject("noOfPages", mapOfData.size());
				
				responseView.addObject("result",updateDone);
			}
			
		} catch (Exception e) {
			log.error("Error Wile Editing OEM Data. Root cause "+e.getMessage());
			responseView.addObject("result",false);
			responseView.addObject("message",StringEscapeUtils.escapeHtml(e.getMessage()));
		}
		log.info("WLMDataController.updateOEMData <<<");
		return responseView;
		
	}
	
	public ModelAndView deleteOEMData(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.deleteOEMData >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		int selectedPage ;
		try {
			selectedPage = Integer.parseInt(req.getParameter("selectedPageNo"));
			OEMDataVO dataVONew = new OEMDataVO();
			dataVONew.setOemName(req.getParameter("oemName"));
			//dataVONew.setOemDescription(req.getParameter("inputDec"));
			boolean updateDone = oemClientService.deleteOEM(dataVONew,getWhitelistService(req));
			if (updateDone) {
				Map<Integer, List<OEMDataVO>> mapOfData = getPartitionListOfAllOEM(req);
				
				if (selectedPage > mapOfData.size()) {
					selectedPage = mapOfData.size();
				}
				responseView.addObject("OEMDataVo", mapOfData.get(selectedPage));
				responseView.addObject("noOfPages", mapOfData.size());
				responseView.addObject("result",updateDone);
			}
			
		} catch (Exception e) {
			log.error("Error Wile Deleting OEM Data. Root cause "+e.getMessage());
			responseView.addObject("result",false);
			responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
		}
		log.info("WLMDataController.deleteOEMData <<<");
		return responseView;
		
	}
	
	
	/*
	 * Method to get All MLE Data for Pagination
	 */
	public ModelAndView getViewMle(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.getViewMle >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		try {
			Map<Integer, List<MLEDataVO>> map = getPartitionListOfAllMle(req);
			
			responseView.addObject("MLEDataVo", map.get(1));
			responseView.addObject("noOfPages", map.size());
		} catch (WLMPortalException e) {
			log.error( e.toString());
			e.printStackTrace();
			responseView.addObject("MLEDataVo", "");
			responseView.addObject("result", false);
			responseView.addObject("message",StringEscapeUtils.escapeHtml(e.getMessage()));
			return responseView;
		}
		responseView.addObject("result", true);
		responseView.addObject("message", "");
		log.info("WLMDataController.getViewMle <<");
		return responseView;
	}
	
	
	@SuppressWarnings("unchecked")
	public ModelAndView getViewMleForPageNo(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.getViewMleForPageNo >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		try {
			HttpSession session = req.getSession();
			Map<Integer, List<MLEDataVO>> map  = (Map<Integer, List<MLEDataVO>>) session.getAttribute("MleList");
			responseView.addObject("MLEDataVo", map.get(Integer.parseInt(req.getParameter("pageNo"))));
		} catch (Exception e) {
			log.error(e.toString());
			e.printStackTrace();
			responseView.addObject("MLEDataVo", "");
			responseView.addObject("result", false);
			responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
			return responseView;
		}
		responseView.addObject("result", true);
		responseView.addObject("message", "");
		log.info("WLMDataController.getViewMleForPageNo <<");
		return responseView;
	}
	
	/*Methods to get data for pagination for OS Component */
	public ModelAndView getAllOSList(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.getAllOSList >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		try {
			Map<Integer, List<OSDataVO>> map = getPartitionListOfAllOS(req);
			
			responseView.addObject("OSDataVo", map.get(1));
			responseView.addObject("noOfPages", map.size());
		} catch (WLMPortalException e) {
			log.error(e.toString());
			e.printStackTrace();
			responseView.addObject("OSDataVo", "");
			responseView.addObject("result", false);
			responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
			return responseView;
		}
		responseView.addObject("result", true);
		responseView.addObject("message", "");
		log.info("WLMDataController.getAllOSList <<");
		return responseView;
	}
	
	@SuppressWarnings("unchecked")
	public ModelAndView getViewOSForPageNo(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.getViewOSForPageNo >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		try {
			HttpSession session = req.getSession();
			Map<Integer, List<MLEDataVO>> map  = (Map<Integer, List<MLEDataVO>>) session.getAttribute("OSList");
			responseView.addObject("OSDataVo", map.get(Integer.parseInt(req.getParameter("pageNo"))));
		} catch (Exception e) {
			log.error(e.toString());
			e.printStackTrace();
			responseView.addObject("OSDataVo", "");
			responseView.addObject("result", false);
			responseView.addObject("message",StringEscapeUtils.escapeHtml(e.getMessage()));
			return responseView;
		}
		responseView.addObject("result", true);
		responseView.addObject("message", "");
		log.info("WLMDataController.getViewOSForPageNo <<");
		return responseView;
	}
	
	/*Methods to get data for pagination for OEM Component */
	public ModelAndView getAllOEMList(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.getAllOEMList >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		try {
			Map<Integer, List<OEMDataVO>> map= getPartitionListOfAllOEM(req);
			
			responseView.addObject("OEMDataVo", map.get(1));
			responseView.addObject("noOfPages", map.size());
		} catch (WLMPortalException e) {
			log.error(e.toString());
			e.printStackTrace();
			responseView.addObject("OEMDataVo", "");
			responseView.addObject("result", false);
			responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
			return responseView;
		}
		responseView.addObject("result", true);
		responseView.addObject("message", "");
		log.info("WLMDataController.getAllOEMList <<");
		return responseView;
	}
	
	@SuppressWarnings("unchecked")
	public ModelAndView getViewOEMForPageNo(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.getViewOEMForPageNo >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		try {
			HttpSession session = req.getSession();
			Map<Integer, List<MLEDataVO>> map  = (Map<Integer, List<MLEDataVO>>) session.getAttribute("OEMList");
			responseView.addObject("OEMDataVo", map.get(Integer.parseInt(req.getParameter("pageNo"))));
		} catch (Exception e) {
			log.error(e.toString());
			e.printStackTrace();
			responseView.addObject("OEMDataVo", "");
			responseView.addObject("result", false);
			responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
			return responseView;
		}
		responseView.addObject("result", true);
		responseView.addObject("message", "");
		log.info("WLMDataController.getViewOEMForPageNo <<");
		return responseView;
	}
        public ModelAndView logOutUser(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMDataController.logOutUser >>");
		ModelAndView responseView = new ModelAndView("Login");
		try {
			HttpSession session = req.getSession(false);
                        if (session != null) {
                            session.invalidate();
                        }
                        //res.sendRedirect(req.getContextPath()+"/login.htm");
		} catch (Exception e) {
			log.error(e.toString());
			e.printStackTrace();
		}
		log.info("WLMDataController.logOutUser <<");
		return responseView;
	}
        
	
	//Method is used to return List of all hypervisors for both VMWare and OpenSource configure in whitelist-portal.properties 
	private List<String> getVMMNameList(String openSourceHypervisors) {
		return Arrays.asList(openSourceHypervisors.split(";"));
	}
	
	/**
	 * Method to get a Map View for all MLE data from REST Services according to there page no.
	 * Return data is used in pagination.
	 * 
	 * @param req object of HttpServletRequest
	 * @return
	 * @throws WLMPortalException
	 */
	private Map<Integer, List<MLEDataVO>> getPartitionListOfAllMle(HttpServletRequest req) throws WLMPortalException {
		Map<Integer, List<MLEDataVO>> map =new HashMap<Integer, List<MLEDataVO>>(); 
		
		//Get List of all MLE.
		List<MLEDataVO> dataVOs = mleClientService.getAllMLE(getWhitelistService(req));
		int no_row_per_page = WLMPConfig.getConfiguration().getInt("mtwilson.wlmp.pagingSize");
		
		//Divide List of all MLE into a subList based on the value of host per page.
		List<List<MLEDataVO>> list = Lists.partition(dataVOs, no_row_per_page);
		
		//Creating a Map view of MLE list based on the Page No.
		int i=1;
		for (List<MLEDataVO> listForMap : list) {
			map.put(i, listForMap);
			i++;
		}
		
		//setting map into session attribute;
		HttpSession session = req.getSession();
		session.setAttribute("MleList", map);
		
		return map;
	}
	
	/**
	 * Method to get a Map View for all OS data from REST Services according to there page no.
	 * Return data is used in pagination.
	 * 
	 * @param req object of HttpServletRequest
	 * @return
	 * @throws WLMPortalException
	 */
	private Map<Integer, List<OSDataVO>> getPartitionListOfAllOS(HttpServletRequest req) throws WLMPortalException {
		Map<Integer, List<OSDataVO>> mapOfData =new HashMap<Integer, List<OSDataVO>>(); 
		
		//Get List of all OS.
		List<OSDataVO> dataVOs = osClientService.getAllOS(getWhitelistService(req));
		int no_row_per_page = WLMPConfig.getConfiguration().getInt("mtwilson.wlmp.pagingSize");
		
		//Divide List of all OS into a subList based on the value of host per page. 
		List<List<OSDataVO>> list = Lists.partition(dataVOs, no_row_per_page);
		
		//Creating a Map view of OS list based on the Page No.
		int i=1;
		for (List<OSDataVO> listForMap : list) {
			mapOfData.put(i, listForMap);
			i++;
		}
		
		//setting map into session attribute;
		HttpSession session = req.getSession();
		session.setAttribute("OSList", mapOfData);
		
		return mapOfData;
	}
	
	/**
	 * Method to get a Map View for all OEM data from REST Services according to there page no.
	 * Return data is used in pagination.
	 * 
	 * @param req object of HttpServletRequest
	 * @return
	 * @throws WLMPortalException
	 */
	private Map<Integer, List<OEMDataVO>> getPartitionListOfAllOEM(HttpServletRequest req) throws WLMPortalException {
		Map<Integer, List<OEMDataVO>> map =new HashMap<Integer, List<OEMDataVO>>();
		
		//Get List of all OEM.
		List<OEMDataVO> dataVOs = oemClientService.getAllOEM(getWhitelistService(req));
		int no_row_per_page = WLMPConfig.getConfiguration().getInt("mtwilson.wlmp.pagingSize");
		
		//Divide List of all OEM into a subList based on the value of host per page. 
		List<List<OEMDataVO>> list = Lists.partition(dataVOs, no_row_per_page);
		int i=1;
		for (List<OEMDataVO> listForMap : list) {
			map.put(i, listForMap);
			i++;
		}
		
		//setting map into session attribute;
		HttpSession session = req.getSession();
		session.setAttribute("OEMList", map);
		return map;
	}
	
    /**
     * This method will return a WhitelistService Object from a Session.
     * This object is stored into Session at time of user login.
     * Check CheckLoginController.java for more Clarification.
     * 
     * @param req
     * @return
     * @throws WLMPortalException
     */
    private WhitelistService getWhitelistService(HttpServletRequest req) throws WLMPortalException{
        
    	//getting already created session object by passing false while calling into getSession();
    	HttpSession session = req.getSession(false);
        WhitelistService service = null;
        if(session !=null){
            try{
            	//getting WhitelistService Object from Session, stored while log-in time.  
                service = (WhitelistService)session.getAttribute("apiClientObject");    
            } catch (Exception e) {
				log.error("Error while creating ApiCliennt Object. "+e.getMessage());
				throw new WLMPortalException("Error while creating ApiCliennt Object. "+StringEscapeUtils.escapeHtml(e.getMessage()),e);
			}
        }
        return service;
    }
	
	
    // Methods to create services layer object, used by other methods while calling into a Service Layer.
    //these method are called by spring container while dependencies injuction.
	public void setOsClientServiceImpl(IOSClientService clientService){
		this.osClientService = clientService;
	}
	
	public void setMleClientService(IMLEClientService mleClientService){
		this.mleClientService = mleClientService;
	}
	
	public void setOemClientService(IOEMClientService oemClientService){
		this.oemClientService = oemClientService;
	}

}
