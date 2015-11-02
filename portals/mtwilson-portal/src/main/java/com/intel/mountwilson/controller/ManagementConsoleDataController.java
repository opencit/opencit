/**
 *
 */
package com.intel.mountwilson.controller;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.intel.mountwilson.Service.IDemoPortalServices;
import com.intel.mountwilson.Service.IMLEClientService;
import com.intel.mountwilson.Service.IManagementConsoleServices;
import com.intel.mountwilson.Service.IOEMClientService;
import com.intel.mountwilson.Service.IOSClientService;
import com.intel.mountwilson.common.DemoPortalException;
import com.intel.mountwilson.common.MCPConfig;
import com.intel.mountwilson.common.ManagementConsolePortalException;
import com.intel.mountwilson.common.TDPConfig;
import com.intel.mountwilson.common.WLMPConfig;
import com.intel.mountwilson.common.WLMPortalException;
import com.intel.mountwilson.constant.HelperConstant;
import com.intel.mountwilson.datamodel.ApiClientDetails;
import com.intel.mountwilson.datamodel.ApiClientListType;
import com.intel.mountwilson.datamodel.HostDetails;
import com.intel.mountwilson.datamodel.HostDetailsEntityVO;
import com.intel.mountwilson.datamodel.HostVmMappingVO;
import com.intel.mountwilson.datamodel.MLEDataVO;
import com.intel.mountwilson.datamodel.OEMDataVO;
import com.intel.mountwilson.datamodel.OSDataVO;
import com.intel.mountwilson.datamodel.VmmHostDataVo;
import com.intel.mountwilson.util.BasicView;
import com.intel.mountwilson.util.JSONView;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.agent.vmware.VMwareClient;
import com.intel.mtwilson.agent.vmware.VMwareConnectionPool;
import com.intel.mtwilson.agent.vmware.VmwareClientFactory;
import com.intel.mtwilson.api.*;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.HostVMMType;
import com.intel.mtwilson.datatypes.HostWhiteListTarget;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringEscapeUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.i18n.LocaleUtil;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.mtwilson.ApacheHttpClient;
import com.intel.mtwilson.My;
import com.intel.mtwilson.datatypes.TagDataType;
import com.intel.mtwilson.security.http.apache.ApacheBasicHttpAuthorization;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.creator.impl.CertificateTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.creator.impl.InsecureTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.creator.impl.InsecureTrustFirstPublicKeyTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.creator.impl.PublicKeyDigestTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.creator.impl.PublicKeyTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactory;
import com.intel.mtwilson.tls.policy.factory.impl.TxtHostRecordTlsPolicyFactory;
//import com.intel.mtwilson.tag.model.Selection;
//import com.intel.mtwilson.tag.model.SelectionCollection;
//import com.intel.mtwilson.tag.model.SelectionFilterCriteria;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Locale;
import org.apache.http.auth.UsernamePasswordCredentials;
//import org.codehaus.jackson.JsonParseException;
//import org.codehaus.jackson.map.JsonMappingException;
//import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
//import com.intel.mtwilson.tag.rest.v2.repository.SelectionRepository;
/**
 * @author yuvrajsx
 *
 */
@Controller
public class ManagementConsoleDataController extends MultiActionController {

    //private static final Logger log = Logger.getLogger(ManagementConsoleDataController.class.getName());
    private Logger log = LoggerFactory.getLogger(getClass());
    private IManagementConsoleServices services;
    //Services Layer object, used to invoke service layer methods.
    private IDemoPortalServices demoPortalServices;
    //Services layer objects use to call into services layer for bussiness logic.
    private IOSClientService osClientService;
    private IMLEClientService mleClientService;
    private IOEMClientService oemClientService;
    private VMwareConnectionPool pool = new VMwareConnectionPool(new VmwareClientFactory());
    private final int DEFAULT_ROWS_PER_PAGE = 10;

    public ManagementConsoleDataController() {
        Extensions.register(TlsPolicyFactory.class, TxtHostRecordTlsPolicyFactory.class);
        Extensions.register(TlsPolicyCreator.class, PublicKeyDigestTlsPolicyCreator.class);
        Extensions.register(TlsPolicyCreator.class, PublicKeyTlsPolicyCreator.class);
        Extensions.register(TlsPolicyCreator.class, CertificateDigestTlsPolicyCreator.class);
        Extensions.register(TlsPolicyCreator.class, CertificateTlsPolicyCreator.class);
        Extensions.register(TlsPolicyCreator.class, InsecureTlsPolicyCreator.class);
        Extensions.register(TlsPolicyCreator.class, InsecureTrustFirstPublicKeyTlsPolicyCreator.class);
    }

    
    /**
     * @param HttpServletRequest
     * @param HttpServletResponse
     * @return
     */
    public ModelAndView uploadFlatFileRegisterHost(HttpServletRequest req, HttpServletResponse res) {
        log.debug("ManagementConsoleDataController.uploadFlatFileRegisterHost >>");
        req.getSession().removeAttribute("hostVO");
        //ModelAndView responseView = new ModelAndView(new JSONView());
        ModelAndView responseView = new ModelAndView(new BasicView());
        List<HostDetails> listOfRegisterHost = new ArrayList<>();

        log.debug("Action is :{}", req.getMethod());
        log.debug("Content type is {}", req.getContentType());
        
        
        // Check that we have a file upload request
        boolean isRequestMultiType = ServletFileUpload.isMultipartContent(req);
        log.debug("Is data multipart {} ", isRequestMultiType);        
        if (!isRequestMultiType) {
            responseView.addObject("result", false);
            //log.warn("File Upload is not MultiPart. Please check you File Uploaded Plugin.");
            return responseView;
        }
        
        // Create a factory for disk-based file items
        FileItemFactory factory = new DiskFileItemFactory();

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Parse the request
        try {
            @SuppressWarnings("unchecked")
            List<FileItem> items = upload.parseRequest(req);
            
            // Process the uploaded items
            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                log.debug ("File is {}", item.getName());
                
                if (!item.isFormField()) {
                    String lines[] = item.getString().split("\\r?\\n");
                    for (String values : lines) {
                        log.debug("Parsing entry {}", values);
                        //Split host name and host value with Separator e.g. |
                        if (values.indexOf(HelperConstant.SEPARATOR_REGISTER_HOST) >= 0) {
                            String val[] = values.split(Pattern.quote(HelperConstant.SEPARATOR_REGISTER_HOST));
                            if (val.length == 3) {
                                HostDetails host = new HostDetails();
                                host.setHostType(val[0]);
                                host.setHostName(val[1]);
                                String portOrVCenter = val[2];

                                if (portOrVCenter.toLowerCase().contains(HelperConstant.HINT_FOR_VCENTERSTRING.toLowerCase())) {
                                    host.setvCenterString(portOrVCenter);
                                    host.setVmWareType(true);
                                } else {
                                    host.setHostPortNo(portOrVCenter);
                                    host.setVmWareType(false);
                                }
                                listOfRegisterHost.add(host);
                            }
                        } else {
                            responseView.addObject("result", false);
                            log.info("Please provide the host details in the format specified.");
                            return responseView;
                        }
                    }
                }
            }
            //log.info("Uploaded Content :: " + listOfRegisterHost.toString());
            req.getSession().setAttribute("hostVO", listOfRegisterHost);
            responseView.addObject("result", listOfRegisterHost.size() > 0 ? true : false);

        } catch (FileUploadException e) {
            e.printStackTrace();
            responseView.addObject("result", false);
        } catch (Exception e) {
            e.printStackTrace();
            responseView.addObject("result", false);
        }

        //log.info("ManagementConsoleDataController.uploadFlatFileRegisterHost <<<");
        //responseView.addObject("result", true);
        return responseView;
    }

    /**
     * This Method will use to Retrieve the pre-fetched Host to be Register,
     * uploaded with flat file.
     *
     * @param req
     * @param res
     * @return
     */
    public ModelAndView getUploadedRegisterHostValues(HttpServletRequest req, HttpServletResponse res) {
        //log.info("ManagementConsoleDataController.getUploadedRegisterHostValues >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        @SuppressWarnings("unchecked")
        List<HostDetails> listOfRegisterHost = (List<HostDetails>) req.getSession().getAttribute("hostVO");
        boolean result = false;

        if (listOfRegisterHost != null) {
            try {
                ApiClient apiClient = getApiClientService(req, ApiClient.class);
                if (apiClient == null) {
                    throw new IllegalStateException("Failed to initialize the API client object.");
                }
                responseView = getListofRegisteredHost(listOfRegisterHost, responseView, apiClient);
                result = true;
            } catch (Exception ex) {
                //log.warn("Exception Checking for already register host. " + ex.getMessage());
                responseView.addObject("message", "Exception Checking for already register host. " + StringEscapeUtils.escapeHtml(ex.getMessage()));
                responseView.addObject("result", result);
                return responseView;
            }
        }
        responseView.addObject("result", result);
        //log.info("ManagementConsoleDataController.getUploadedRegisterHostValues <<<");
        return responseView;

    }

    /**
     * This Method will use to get Data of white List Configuration.
     *
     * @param req
     * @param res
     * @return
     */
    public ModelAndView uploadWhiteListConfiguration(HttpServletRequest req, HttpServletResponse res) {
        //log.info("ManagementConsoleDataController.uploadWhiteListConfiguration >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        String hostVOString = req.getParameter("registerHostVo");
        log.debug("uploadWhiteListConfiguration registerHostVo = {}", hostVOString);
        boolean result = false;
        HostDetails hostDetailsObj;

        try {
            String whiteListConfigVOString = req.getParameter("whiteListConfigVO");
        log.debug("uploadWhiteListConfiguration whiteListConfigVO = {}", whiteListConfigVOString);
            HostConfigData hostConfig;

            @SuppressWarnings("serial")
            Type whiteListType = new TypeToken<HostConfigData>() {
            }.getType();
            hostConfig = new Gson().fromJson(whiteListConfigVOString, whiteListType);
            //hostConfig = getObjectFromJSONString(whiteListConfigVOString, HostConfigData.class);

            // these are handled separately because their values are in enum HostWhiteListTarget
            // also note that the parameter names are camelCase, because the deserialization happens
            // above via Gson, so any jackson annotations ilke @JsonProperty are ignored.
            hostConfig.setBiosWLTarget(getBiosWhiteListTarget(req.getParameter("biosWLTagrget"))); 
            hostConfig.setVmmWLTarget(getVmmWhiteListTarget(req.getParameter("vmmWLTarget")));
            System.err.println("whiteListConfigVO>>" + hostConfig);

            @SuppressWarnings("serial")
            Type hostInfo = new TypeToken<HostDetails>() {
            }.getType();
            hostDetailsObj = new Gson().fromJson(hostVOString, hostInfo);
            //hostDetailsObj = getObjectFromJSONString(hostVOString, HostDetails.class);
            hostConfig.setHostVmmType(getHostVmmTypeTarget(hostDetailsObj.getHostType()));


            // Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);

            result = services.saveWhiteListConfiguration(hostDetailsObj, hostConfig, apiObj);
            result = true;
        } catch (Exception ex) {
            //log.warn("Exception during whitelist configuration. " + ex.getMessage());
            responseView.addObject("message", StringEscapeUtils.escapeHtml(ex.getMessage()));
            responseView.addObject("result", result);
            return responseView;
        }
        responseView.addObject("result", result);
        //log.info("ManagementConsoleDataController.uploadWhiteListConfiguration <<<");
        return responseView;

    }

    /**
     * This Method will use to get Data of white List Configuration, using
     * Automatic tools.
     *
     * @param req
     * @param res
     * @return
     *//*
     public ModelAndView defineWhiteListConfig(HttpServletRequest req, HttpServletResponse res) {
     log.info("ManagementConsoleDataController.defineWhiteListConfig >>");
     ModelAndView responseView = new ModelAndView(new JSONView());
     String whiteListConfigVOString = req.getParameter("whiteListConfigVO");
     System.out.println(whiteListConfigVOString);
     boolean result = false;
     HostConfigData whiteList;
		
     try {
     @SuppressWarnings("serial")
     Type hostInfo = new TypeToken<HostConfigData>() {}.getType();
     whiteList = new Gson().fromJson(whiteListConfigVOString, hostInfo);
     whiteList = getObjectFromJSONString(whiteListConfigVOString, HostConfigData.class);
			
     whiteList.setBiosWLTarget(getWhiteListTarget(req.getParameter("biosWLTagrget")));
     whiteList.setVmmWLTarget(getWhiteListTarget(req.getParameter("vmmWLTarget")));
			
     System.err.println(whiteList);
			
			
     // Now save the whiteListConfig object to the session
     HttpSession httpSession = req.getSession(false);
     httpSession.setAttribute("White-List", whiteList);
     result = true;
			
     } catch (Exception ex) {
     log.severe("Exception during whitelist configuration. " + ex.getMessage());
     responseView.addObject("message","Error during white list configuration. " + ex.getMessage());
     responseView.addObject("result",result);
     ex.printStackTrace();
     return responseView;
     }
     responseView.addObject("result",result);
     log.info("ManagementConsoleDataController.defineWhiteListConfig <<<");
     return responseView;
		
     }*/

    private HostWhiteListTarget getBiosWhiteListTarget(String target) {
        List<HostWhiteListTarget> biosTargetList = new ArrayList<HostWhiteListTarget>();
        biosTargetList.add(HostWhiteListTarget.BIOS_HOST);
        biosTargetList.add(HostWhiteListTarget.BIOS_OEM);
        for (HostWhiteListTarget whiteListTargets : biosTargetList) {
            if (whiteListTargets.getValue().equals(target)) {
                return whiteListTargets;
            }
        }
        return null;
    }

    private HostWhiteListTarget getVmmWhiteListTarget(String target) {
        List<HostWhiteListTarget> vmmTargetList = new ArrayList<HostWhiteListTarget>();
        vmmTargetList.add(HostWhiteListTarget.VMM_OEM);
        vmmTargetList.add(HostWhiteListTarget.VMM_HOST);
        vmmTargetList.add(HostWhiteListTarget.VMM_GLOBAL);
        for (HostWhiteListTarget whiteListTargets : vmmTargetList) {
            if (whiteListTargets.getValue().equals(target)) {
                return whiteListTargets;
            }
        }
        return null;
    }

    private HostVMMType getHostVmmTypeTarget(String target) {
        for (HostVMMType hostVmmType : HostVMMType.values()) {
            if (hostVmmType.getValue().equals(target)) {
                return hostVmmType;
            }
        }
        return null;
    }

    /**
     * This Method will use to get Data for pre configured white List
     * Configuration.
     *
     * @param req
     * @param res
     * @return
     */
    public ModelAndView getDefinedWhiteListConfig(HttpServletRequest req, HttpServletResponse res) {
        //log.info("ManagementConsoleDataController.getDefinedWhiteListConfig >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        boolean result;
        HostConfigData whiteList;

        try {
            // Now get the whiteListConfig object from the session
            HttpSession httpSession = req.getSession(false);
            whiteList = (HostConfigData) httpSession.getAttribute("White-List");
            System.err.println(whiteList);
            responseView.addObject("whiteListConfig", whiteList);
            responseView.addObject("biosWLTarget", whiteList.getBiosWLTarget().getValue() == null ? "" : whiteList.getBiosWLTarget().getValue());
            responseView.addObject("vmmWLTarget", whiteList.getVmmWLTarget().getValue() == null ? "" : whiteList.getVmmWLTarget().getValue());
            result = true;

        } catch (Exception ex) {
            result = false;
            //log.warn("Exception during whitelist configuration. " + ex.getMessage());
            responseView.addObject("message", StringEscapeUtils.escapeHtml(ex.getMessage()));
            responseView.addObject("result", result);
            return responseView;
        }
        responseView.addObject("result", result);
        //log.info("ManagementConsoleDataController.getDefinedWhiteListConfig <<<");
        return responseView;

    }

    /**
     * This Method is used to retrieve the datacenter names of a vSphere Server.
     *
     * @param req
     * @param res
     * @return
     */
   /* public ModelAndView retrieveDatacenters(HttpServletRequest req, HttpServletResponse res) {
        //log.info("ManagementConsoleDataController.retrieveDatacenters >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        String vCenterConnection;
        String dcCombined;

        try {
            vCenterConnection = req.getParameter("vCentertConnection");
        } catch (Exception e) {
            //log.warn("Error while getting Input parameter from request." + StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("message", "Input Parameters are NUll.");
            responseView.addObject("result", false);
            return responseView;
        }

        try {
            log.debug("Acquiring vmware client...");
            URL url = new URL(vCenterConnection);
            VMwareClient client = pool.getClientForConnection(new TlsConnection(url, new InsecureTlsPolicy())); 
            List<String> datacenters = services.getDatacenterNames(client);

            StringBuilder dcBuilder = new StringBuilder();
            for (String dc : datacenters) {
                dcBuilder.append(dc);
                dcBuilder.append(",");
            }
            dcCombined = dcBuilder.toString();
            
            dcCombined = dcCombined.substring(0, dcCombined.length() - 1);
        } catch (Exception e) {
            //log.warn("Error while getting data from VMware vCeneter. " + StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("message", e.getMessage());
            responseView.addObject("result", false);
            return responseView;
        }

        responseView.addObject("datacenters", dcCombined);
        responseView.addObject("result", true);
        //log.info("ManagementConsoleDataController.retrieveDatacenters <<<");
        return responseView;
    }*/

    /**
     * This Method is used to retrieve all cluster names within the vSphere
     * Server.
     *
     * @param req
     * @param res
     * @return
     */
    public ModelAndView retrieveAllClusters(HttpServletRequest req, HttpServletResponse res) {
//        Extensions.register(TlsPolicyFactory.class, TxtHostRecordTlsPolicyFactory.class);
//        Extensions.register(TlsPolicyCreator.class, PublicKeyDigestTlsPolicyCreator.class);
//        Extensions.register(TlsPolicyCreator.class, PublicKeyTlsPolicyCreator.class);
//        Extensions.register(TlsPolicyCreator.class, CertificateDigestTlsPolicyCreator.class);
//        Extensions.register(TlsPolicyCreator.class, CertificateTlsPolicyCreator.class);
//        Extensions.register(TlsPolicyCreator.class, InsecureTlsPolicyCreator.class);
//        Extensions.register(TlsPolicyCreator.class, InsecureTrustFirstPublicKeyTlsPolicyCreator.class);
        //log.info("ManagementConsoleDataController.retrieveAllClusters >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        String vCenterConnection;
        String clusterCombined;
        String tlsPolicyDetails;
        
        try {
            vCenterConnection = req.getParameter("vCentertConnection");
            tlsPolicyDetails = req.getParameter("tlsPolicyChoice");
            
        } catch (Exception e) {
            //log.warn("Error while getting Input parameter from request." + StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("message", "Input Parameters are NUll.");
            responseView.addObject("result", false);
            return responseView;
        }

        try {
            log.debug("retrieveAllClusters: Acquiring vmware client...");
            URL url = new URL(vCenterConnection);
            
            log.debug("retrieveAllClusters: Tlspolicy chosen is {}", tlsPolicyDetails);
            
            String tlsPolicyId = tlsPolicyDetails.split(";")[0];
            String tlsPolicyName = tlsPolicyDetails.split(";")[1];
            
            if (tlsPolicyId == null || tlsPolicyId.isEmpty())
                tlsPolicyId = tlsPolicyName;
            
            TlsPolicyChoice vCenterTlsPolicy = new TlsPolicyChoice();
            vCenterTlsPolicy.setTlsPolicyId(tlsPolicyId);
            
            TxtHostRecord host = new TxtHostRecord();
            host.HostName = "192.168.1.1";
            host.AddOn_Connection_String = vCenterConnection;
            host.tlsPolicyChoice = vCenterTlsPolicy;
            
            TlsPolicyFactory factory = TlsPolicyFactory.createFactory(host);
            TlsPolicy tlsPolicy = factory.getTlsPolicy();
            
            VMwareClient client = pool.getClientForConnection(new TlsConnection(url, tlsPolicy)); 
            List<String> clusters = services.getClusterNamesWithDC(client);
            Collections.sort(clusters);
            StringBuilder clusterBuilder = new StringBuilder();
            for (String cluster : clusters) {
                clusterBuilder.append(cluster);
                clusterBuilder.append(",");
            }
            clusterCombined = clusterBuilder.toString();
            clusterCombined = clusterCombined.substring(0, clusterCombined.length() - 1);
        } catch (Exception e) {
            //log.warn("Error while getting data from VMware vCeneter. " + StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("message", e.getMessage());
            responseView.addObject("result", false);
            return responseView;
        }

        responseView.addObject("clusters", clusterCombined);
        responseView.addObject("result", true);
        //log.info("ManagementConsoleDataController.retrieveAllClusters <<<");
        return responseView;
    }

    /**
     * This Method is use to get Retrieve Host data from VMWare Cluster. using
     * Cluster name and vCenterConnection String.
     *
     * @param req
     * @param res
     * @return
     */
    public ModelAndView retriveHostFromCluster(HttpServletRequest req, HttpServletResponse res) {
        //log.info("ManagementConsoleDataController.retriveHostFromCluster >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        String clusterName;
        String vCenterConnection;
        String tlsPolicyDetails;
        
        try {
            clusterName = req.getParameter("clusterName");
            if (clusterName == null) {
                throw new IllegalArgumentException("Cluster name cannot be null.");
            }
            clusterName = clusterName.substring(clusterName.indexOf("] ") + 2);
            
            vCenterConnection = req.getParameter("vCentertConnection");
            tlsPolicyDetails = req.getParameter("tlsPolicyChoice");
            
        } catch (Exception e) {
            //log.warn("Error while getting Input parameter from request." + StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("message", "Input parameters are null.");
            responseView.addObject("result", false);
            return responseView;
        }

        try {
            log.debug("Acquiring vmware client...");
            URL url = new URL(vCenterConnection);
            log.debug("retrieveAllClusters: Tlspolicy chosen is {}", tlsPolicyDetails);
            
            String tlsPolicyId = tlsPolicyDetails.split(";")[0];
            String tlsPolicyName = tlsPolicyDetails.split(";")[1];
            
            if (tlsPolicyId == null || tlsPolicyId.isEmpty())
                tlsPolicyId = tlsPolicyName;
            
            TlsPolicyChoice vCenterTlsPolicy = new TlsPolicyChoice();
            vCenterTlsPolicy.setTlsPolicyId(tlsPolicyId);
            
            TxtHostRecord host = new TxtHostRecord();
            host.HostName = "192.168.1.1";
            host.AddOn_Connection_String = vCenterConnection;
            host.tlsPolicyChoice = vCenterTlsPolicy;
            
            TlsPolicyFactory factory = TlsPolicyFactory.createFactory(host);
            TlsPolicy tlsPolicy = factory.getTlsPolicy();
            
            VMwareClient client = pool.getClientForConnection(new TlsConnection(url, tlsPolicy)); 
            List<HostDetails> hosts = services.getHostNamesForCluster(client, clusterName);

            for (HostDetails hostDetail : hosts) {
                // Since we have retrieved the hosts from the VMware cluster, we just mark all the hosts as of VMware type
                hostDetail.setHostType("vmware");
            }
            ApiClient apiClient = getApiClientService(req, ApiClient.class);
            if (apiClient == null) {
                throw new IllegalStateException("Failed to initialize the API client object.");
            }
            responseView = getListofRegisteredHost(hosts, responseView, apiClient);
        } catch (Exception e) {
            //log.warn("Error while getting data from VMware vCeneter. " + StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("message", e.getMessage());
            responseView.addObject("result", false);
            return responseView;
        }

        responseView.addObject("result", true);
        //log.info("ManagementConsoleDataController.retriveHostFromCluster <<<");
        return responseView;

    }

    /**
     * This Method is use to register multiple host on server..
     *
     * @param req
     * @param res
     * @return
     */
    public ModelAndView registerMultipleHost(HttpServletRequest req, HttpServletResponse res) {
        //log.info("ManagementConsoleDataController.registerMultipleHost >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        String hostListString;

        if (req.getParameter("hostToBeRegister") != null) {
            hostListString = req.getParameter("hostToBeRegister");
        } else {
            //log.warn("hostToBeRegister parameter is Null");
            responseView.addObject("result", false);
            responseView.addObject("message", "Input Parameters are NULL.");
            return responseView;
        }
        log.debug("hostListString = " + hostListString);

        try {
            // Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);
            List<HostDetails> hostRecords;
            Type listType = new TypeToken<List<HostDetails>>() {
            }.getType();
            hostRecords = new Gson().fromJson(hostListString, listType);
            responseView.addObject("hostVOs", services.registerHosts(apiObj, hostRecords));
        } catch (Exception e) {
            log.error("Error while registering the hosts: {}", e.getMessage());
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("result", false);
            return responseView;
        }

        responseView.addObject("result", true);
        //log.info("ManagementConsoleDataController.registerMultipleHost <<<");
        return responseView;
    }

    /**
     * This Method is use to get All pending registration request.
     *
     * @param req
     * @param res
     * @return
     */
    public ModelAndView getAllPendingRegistrationRequest(HttpServletRequest req, HttpServletResponse res) {
        //log.info("ManagementConsoleDataController.getAllPendingRegistrationRequest >>");
        ModelAndView responseView = new ModelAndView(new JSONView());

        try {
            // Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);

            responseView.addObject("pendingRequest", services.getApiClients(apiObj, ApiClientListType.PENDING));
            responseView.addObject("allRoles", services.getAllRoles(apiObj));
        } catch (Exception e) {

            log.error("Error While getting pending requests. " + e.getMessage());
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("result", false);
            return responseView;

        }
        responseView.addObject("result", true);
        //log.info("ManagementConsoleDataController.getAllPendingRegistrationRequest <<<");
        return responseView;
    }

    /**
     *
     * @param req
     * @param res
     * @return
     */
    public ModelAndView getAllApprovedRequests(HttpServletRequest req, HttpServletResponse res) {
        //log.info("ManagementConsoleDataController.getAllApprovedRequests >>");
        ModelAndView responseView = new ModelAndView(new JSONView());

        try {
            // Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);
            responseView.addObject("approvedRequests", services.getApiClients(apiObj, ApiClientListType.ALL));
        } catch (Exception e) {
            log.error("Error While getting all approved requests. " + e.getMessage());
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("result", false);
            return responseView;

        }
        responseView.addObject("result", true);
        //log.info("ManagementConsoleDataController.getAllApprovedRequests <<<");
        return responseView;
    }

    /**
     *
     * @param req
     * @param res
     * @return
     */
    public ModelAndView getAllExpiringApiClients(HttpServletRequest req, HttpServletResponse res) {
        //log.info("ManagementConsoleDataController.getAllExpiringApiClients >>");
        ModelAndView responseView = new ModelAndView(new JSONView());

        try {
            // Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);

            responseView.addObject("expiringApiClients", services.getApiClients(apiObj, ApiClientListType.EXPIRING));
            responseView.addObject("expirationMonths", MCPConfig.getConfiguration().getInt("mtwilson.mc.apiKeyExpirationNoticeInMonths", 3));

        } catch (Exception e) {

            log.error("Error While getting all expiring API clients: {}", e.getMessage());
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("result", false);
            return responseView;

        }
        responseView.addObject("result", true);
        //log.info("ManagementConsoleDataController.getAllExpiringApiClients <<<");
        return responseView;
    }

    /**
     *
     * @param req
     * @param res
     * @return
     */
    public ModelAndView getApiClientsForDelete(HttpServletRequest req, HttpServletResponse res) {
        //log.info("ManagementConsoleDataController.getApiClientsForDelete >>");
        ModelAndView responseView = new ModelAndView(new JSONView());

        try {
            // Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);

            responseView.addObject("apiClientList", services.getApiClients(apiObj, ApiClientListType.DELETE));
        } catch (Exception e) {
            log.error("Error while getting Api clients: {}", e.getMessage());
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("result", false);
            return responseView;
        }
        responseView.addObject("result", true);
        //log.info("ManagementConsoleDataController.getApiClientsForDelete <<<");
        return responseView;
    }

    /**
     * This Method is use Approve selected request from Approve request page.
     *
     * @param req
     * @param res
     * @return
     */
    public ModelAndView approveSelectedRequest(HttpServletRequest req, HttpServletResponse res) {
        //log.info("ManagementConsoleDataController.approveSelectedRequest >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        String hostDetailsString;
        ApiClientDetails apiClientDetailsObj;
        boolean result;

        try {
            // Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);

            hostDetailsString = req.getParameter("requestVO");
            apiClientDetailsObj = getObjectFromJSONString(hostDetailsString, ApiClientDetails.class);
            result = services.updateRequest(apiClientDetailsObj, apiObj, true);
        } catch (Exception ex) {
            log.error("Error approving access request: {}", ex.getMessage());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(ex.getMessage()));
            return responseView;
        }

        responseView.addObject("result", result);
        //log.info("ManagementConsoleDataController.approveSelectedRequest <<<");
        return responseView;
    }

    /**
     * This Method is use Reject selected request from Approve request page.
     *
     * @param req
     * @param res
     * @return
     */
    public ModelAndView rejectSelectedRequest(HttpServletRequest req, HttpServletResponse res) {
        //log.info("ManagementConsoleDataController.rejectSelectedRequest >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        String hostDetailsString;
        ApiClientDetails apiClientDetailsObj;
        boolean result = false;

        try {
            // Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);

            hostDetailsString = req.getParameter("requestVO");
            apiClientDetailsObj = getObjectFromJSONString(hostDetailsString, ApiClientDetails.class);
            result = services.updateRequest(apiClientDetailsObj, apiObj, false);
        } catch (Exception ex) {

            log.error(ex.getMessage());
            responseView.addObject("result", result);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(ex.getMessage()));
            return responseView;

        }

        responseView.addObject("result", result);
        //log.info("ManagementConsoleDataController.rejectSelectedRequest <<<");
        return responseView;
    }

    /**
     * This Method is use to delete selected request from Delete request page.
     *
     * @param req
     * @param res
     * @return
     */
    public ModelAndView deleteSelectedRequest(HttpServletRequest req, HttpServletResponse res) {
        //log.info("ManagementConsoleDataController.deleteSelectedRequest >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        String fingerprint;
        boolean result = false;

        try {

            // Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);

            fingerprint = req.getParameter("fingerprint");
            result = services.deleteSelectedRequest(fingerprint, apiObj);

        } catch (Exception ex) {

            log.error(ex.getMessage());
            responseView.addObject("result", result);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(ex.getMessage()));
            return responseView;

        }

        responseView.addObject("result", result);
        //log.info("ManagementConsoleDataController.deleteSelectedRequest <<<");
        return responseView;
    }

    public ModelAndView logOutUser(HttpServletRequest req, HttpServletResponse res) {
        //log.info("ManagementConsoleDataController.logOutUser >>");
        ModelAndView responseView = new ModelAndView("Login");
        try {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        //log.info("ManagementConsoleDataController.logOutUser <<");
        return responseView;
    }
    
    public ModelAndView openPreferences(HttpServletRequest req, HttpServletResponse res) throws ManagementConsolePortalException {
        ModelAndView responseView = new ModelAndView("Preferences");
        String username = req.getParameter("username");
        
        try {
            List<Map<String, Object>> localeList = new ArrayList<Map<String, Object>>();
            for (String localeName : demoPortalServices.getLocales(getApiClientService(req, ApiClient.class))) {
                log.debug("Retrieved locale for preferences page DDL: {}", localeName);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("localeName", localeName);
                localeList.add(map);
            }
            responseView.addObject("locales", localeList);
            
            String selectedLocale = demoPortalServices.getLocale(username, getApiClientService(req, ApiClient.class));
            log.debug("Retrieved selected locale for preferences page DDL: {}", selectedLocale);
            responseView.addObject("selectedLocale", selectedLocale);
        } catch (DemoPortalException e) {
            e.printStackTrace();
            log.error(e.toString());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("message", "");
        return responseView;
    }

    /*
     * Method to provide Services Object while calling servies methods. used by Spring Conatiner.
     * 
     */
    public void setServices(IManagementConsoleServices services) {
        this.services = services;
    }

    @SuppressWarnings("unchecked")
    private <T> T getObjectFromJSONString(String objectString, Class<T> type) {
        Type apiClientDetailsObj = TypeToken.of(type).getType();
        Gson gson = new Gson();
        return (T) gson.fromJson(objectString, apiClientDetailsObj);
    }

    /**
     * This method will return a AttestationService/ApiCLient Object from a
     * Session. This object is stored into Session at time of user login. Check
     * CheckLoginController.java for more Clarification.
     *
     * @param req
     * @return
     * @return AttestationService
     * @throws ManagementConsolePortalException
     * @throws DemoPortalException
     */
    @SuppressWarnings("unchecked")
    private <T> T getApiClientService(HttpServletRequest req, Class<T> type) throws ManagementConsolePortalException {

        //getting already created session object by passing false while calling into getSession();
        HttpSession session = req.getSession(false);
        T service = null;
        if (session != null) {
            try {
                //getting ApiClient Object from Session and downcast that object to Type T.  
                service = (T) session.getAttribute("api-object");
            } catch (Exception e) {
                log.error("Error while creating ApiClient Object: " + e.getMessage(), e);
                throw new ManagementConsolePortalException("Error while creating ApiClient Object: " + e.getMessage(), e);
            }

        }
        return service;
    }
    
    protected static final ObjectMapper mapper = new ObjectMapper();
    
    /***** UNUSED
    private <T> T fromJSON(String document, Class<T> valueType) throws IOException, ApiException {
        try {
            return mapper.readValue(document, valueType);
        }
        catch(com.fasterxml.jackson.core.JsonParseException e) {
           
            throw new ApiException("Cannot parse response", e);
        }
    }*/

    private ModelAndView getListofRegisteredHost(List<HostDetails> listOfRegisterHost, ModelAndView responseView, ApiClient apiClient) throws IOException, NoSuchAlgorithmException, KeyManagementException, ApiException, SignatureException {
        List<HostDetails> listToSend = new ArrayList<HostDetails>();

        for (HostDetails hostDetails : listOfRegisterHost) {
            HostDetails details = hostDetails;
            try {
                List<TxtHostRecord> list = apiClient.queryForHosts2(hostDetails.getHostName());
                if (list.size() <= 0) {
                    details.setRegistered(false);
                } else {
                    TxtHostRecord response = list.get(0);
                    details.setRegistered(true);
                    details.setStatus(HelperConstant.ALREADY_REGISTER);
                    System.err.println("response.VMM_Name >>" + response.VMM_Name);

                    String biosOem = response.BIOS_Oem;
                    if (biosOem.contains(" ")) {
                        biosOem = biosOem.split(" ")[0];
                    }

                    if (response.VMM_Name.toLowerCase().contains(hostDetails.getHostName().toLowerCase())) {
                        responseView.addObject("vmmConfigValue_" + hostDetails.getHostName(), HostWhiteListTarget.VMM_HOST.getValue());
                    } else if (response.VMM_Name.toLowerCase().contains(biosOem.toLowerCase())) {
                        responseView.addObject("vmmConfigValue_" + hostDetails.getHostName(), HostWhiteListTarget.VMM_OEM.getValue());
                    } else {
                        responseView.addObject("vmmConfigValue_" + hostDetails.getHostName(), HostWhiteListTarget.VMM_GLOBAL.getValue());
                    }

                    System.err.println("response.biosWLTarget >>" + response.VMM_Name);
                    if (response.VMM_Name.toLowerCase().contains(hostDetails.getHostName().toLowerCase())) {
                        responseView.addObject("biosConfigValue_" + hostDetails.getHostName(), HostWhiteListTarget.BIOS_HOST.getValue());
                    } else {
                        responseView.addObject("biosConfigValue_" + hostDetails.getHostName(), HostWhiteListTarget.BIOS_OEM.getValue());
                    }
                }

            } catch (Exception e) {
                log.error("Error While getting host info using QueryForHost method, cause : " + e.getMessage());
                details.setRegistered(false);
            }
            listToSend.add(details);
        }

        responseView.addObject("hostVO", listToSend);
        List<String> wlBios = new ArrayList<String>();
        wlBios.add(HostWhiteListTarget.BIOS_OEM.getValue());
        wlBios.add(HostWhiteListTarget.BIOS_HOST.getValue());
        responseView.addObject("wlBiosList", wlBios);

        List<String> wlVMM = new ArrayList<String>();
        wlVMM.add(HostWhiteListTarget.VMM_OEM.getValue());
        wlVMM.add(HostWhiteListTarget.VMM_HOST.getValue());
        wlVMM.add(HostWhiteListTarget.VMM_GLOBAL.getValue());
        responseView.addObject("wlVMMList", wlVMM);
        responseView.addObject("SpecificHostValue", HostWhiteListTarget.VMM_HOST.getValue());
        
        // Get list of selections here and add them to responseView
     
        //List<String> selectionList = new ArrayList<String>();
        //String requestURL = My.configuration().getAssetTagServerURL() + "/selections";
        //1.3.6.1.4.1.99999.3"; 
        //ApacheHttpClient client = new ApacheHttpClient(My.configuration().getAssetTagServerURL(), new ApacheBasicHttpAuthorization(new UsernamePasswordCredentials(My.configuration().getAssetTagApiUsername(),My.configuration().getAssetTagApiPassword())), null, new InsecureTlsPolicy());
        //ApiRequest request = new ApiRequest(MediaType., "");
        //ApiResponse response = client.get(requestURL);    

        //List<String> selectionList = new ArrayList<String>();
        //selectionList.add("N/A");
        //SelectionRepository repo = new SelectionRepository();  
        //SelectionFilterCriteria criteria = new SelectionFilterCriteria();
        //criteria.nameEqualTo = "";
        //SelectionCollection collection = new SelectionCollection();
        
        //collection = repo.search(criteria);
        //List<Selection> list = collection.getSelections();
        
        //for(Selection s: list) {
        //    selectionList.add(s.getName());
        //}
        
        //responseView.addObject("selectionList",selectionList);
        
        return responseView;
    }

    //Begin_Added by Soni-Function for CA
    public ModelAndView getAllCAStatus(HttpServletRequest req, HttpServletResponse res) {
        //log.info("ManagementConsoleDataController.getAllCAStatus >>");
        ModelAndView responseView = new ModelAndView(new JSONView());

        try {
            // Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);

            responseView.addObject("caStatus", services.getCADetails(apiObj));

        } catch (Exception e) {
            log.error("Error While getting ca status. " + e.getMessage());
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("result", false);
            return responseView;

        }
        responseView.addObject("result", true);

        //log.info("ManagementConsoleDataController.getAllCAStatus <<<");
        return responseView;
    }
    //End_Added by Soni-Function for CA

    private String readCertFile(String file) throws IOException {
        StringBuilder stringBuilder;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
        }
        return stringBuilder.toString();
    }

    //Begin_Added by Soni-Function to download SAML certificate
    public ModelAndView getSAMLCertificate(HttpServletRequest req, HttpServletResponse res) {
        //log.info("In Data Contoller ManagementConsoleDataController.getSAMLCertificate  >>");
        //ModelAndView responseView = new ModelAndView("SAMLDownload");
        ModelAndView responseView = new ModelAndView(new JSONView());
        res.setContentType("application/octet-stream ");
        res.setHeader("Content-Disposition",
                "attachment;filename=mtwilson-saml.crt");

        try {
            // Now get the API object from the session

            ApiClient apiObj = getApiClientService(req, ApiClient.class);
            if (apiObj == null) {
                throw new IllegalArgumentException("API client object cannot be null.");
            }
            // InputStream in = new ByteArrayInputStream(apiObj.getSamlCertificate().getEncoded());
            //System.err.println("SAMLcertificate     "+apiObj.getSamlCertificate().getEncoded());
            //IOUtils.copy(in, res.getOutputStream());
            //ServletOutputStream out = res.getOutputStream();
        /*  FileOutputStream out = new FileOutputStream("C:/mtwilson-saml.crt");
             System.err.println("ServletOutputStream"+out);
             byte[] outputByte = new byte[4096];
             //copy binary contect to output stream
             while(in.read(outputByte, 0, 4096) != -1)
             {System.err.println("outputByte           "+outputByte);
             out.write(outputByte, 0, 4096);
             System.err.println("ServletOutputStream"+out);
             }
      
             System.err.println("ServletOutputStream"+out);*/

            responseView.addObject("SAMLcertificate", apiObj.getSamlCertificate().getEncoded());
            responseView.addObject("result", true);
            //log.info("ManagementConsoleDataController.getSAMLCertificate <<<");

            /*	 in.close();
             out.flush();
             out.close();
             */

        } catch (Exception e) {
            log.error("Error While getting Downlaoding Certificate. " + e.getMessage());
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("result", false);
            return responseView;


        }

        return responseView;

    }

    //Begin_Added by stdale-Function to download MC certificate
    public ModelAndView getRootCACertificate(HttpServletRequest req, HttpServletResponse res) {
        //log.info("In Data Contoller ManagementConsoleDataController.getRootCACertificate  >>");
        //ModelAndView responseView = new ModelAndView("SAMLDownload");
        ModelAndView responseView = new ModelAndView(new JSONView());
        res.setContentType("application/octet-stream ");
        res.setHeader("Content-Disposition", "attachment;filename=MtWilsonRootCA.crt.pem");

        try {
            //Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);
            if (apiObj == null) {
                throw new IllegalArgumentException("API client object cannot be null.");
            }
            //X509Certificate[] certs = (X509Certificate[]) apiObj.getRootCaCertificates().toArray();
            Set<X509Certificate> certs = apiObj.getRootCaCertificates();
            X509Certificate cert;
            Iterator i = certs.iterator();
            if (i.hasNext()) {
                cert = (X509Certificate) i.next();
            } else {
                throw new IllegalArgumentException("RootCACertificate not found.");
            }
            String ret = "-----BEGIN CERTIFICATE-----\n";
            ret += DatatypeConverter.printBase64Binary(cert.getEncoded());
            ret += "\n-----END CERTIFICATE-----";
            // MSConfig msc = new MSConfig();
            //Properties prop = msc.getDefaults();
            //String file = prop.getProperty("mtwilson.rootca.certificate.file");                                 
            //String ret = readCertFile(file);
            responseView.addObject("Certificate", ret);
            responseView.addObject("result", true);
            log.info("ManagementConsoleDataController.getRootCACertificate <<<");
        } catch (Exception e) {
            log.error("Error While getting Root CA Downlaoding Certificate. " + e.getMessage());
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("result", false);
            return responseView;
        }
        return responseView;

    }

    public ModelAndView getPrivacyCACertificate(HttpServletRequest req, HttpServletResponse res) {
        //log.info("In Data Contoller ManagementConsoleDataController.getPrivacyCACertificate  >>");
        //ModelAndView responseView = new ModelAndView("SAMLDownload");
        ModelAndView responseView = new ModelAndView(new JSONView());
        res.setContentType("application/octet-stream ");
        res.setHeader("Content-Disposition", "attachment;filename=PrivacyCA.pem");

        try {
            ApiClient apiObj = getApiClientService(req, ApiClient.class);
            if (apiObj == null) {
                throw new IllegalArgumentException("API client object cannot be null.");
            }
            Set<X509Certificate> certs = apiObj.getPrivacyCaCertificates();
            X509Certificate cert;
            Iterator i = certs.iterator();
            if (i.hasNext()) {
                cert = (X509Certificate) i.next();
            } else {
                throw new IllegalArgumentException("PrivacyCACertificate not found.");
            }
            String ret = "-----BEGIN CERTIFICATE-----\n";
            ret += DatatypeConverter.printBase64Binary(cert.getEncoded());
            ret += "\n-----END CERTIFICATE-----";
            responseView.addObject("Certificate", ret);
            responseView.addObject("result", true);
            log.info("ManagementConsoleDataController.getPrivacyCACertificate <<<");
            /*
             MSConfig msc = new MSConfig();
             Properties prop = msc.getDefaults();
             String file = prop.getProperty("mtwilson.privacyca.certificate.file");                                 
             String ret = readCertFile(file);
             responseView.addObject("Certificate",ret);
             responseView.addObject("result",true);
             log.info("ManagementConsoleDataController.getPrivacyCACertificat <<<"); 
             */

        } catch (Exception e) {
            log.error("Error While getting Privacy CA Downlaoding Certificate. " + e.getMessage());
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("result", false);
            return responseView;
        }
        return responseView;

    }

    public ModelAndView getPrivacyCACertificateList(HttpServletRequest req, HttpServletResponse res) {
        //log.info("In Data Contoller ManagementConsoleDataController.getPrivacyCACertificateList  >>");
        //ModelAndView responseView = new ModelAndView("SAMLDownload");
        ModelAndView responseView = new ModelAndView(new JSONView());
        res.setContentType("application/octet-stream ");
        res.setHeader("Content-Disposition", "attachment;filename=PrivacyCA.list.pem");

        try {
            // Now get the API object from the session
            MSConfig msc = new MSConfig();
            Properties prop = msc.getDefaults();
            String file = prop.getProperty("mtwilson.privacyca.certificate.list.file");
            String ret = readCertFile(file);
            responseView.addObject("Certificate", ret);
            responseView.addObject("result", true);
            log.info("ManagementConsoleDataController.getPrivacyCACertificateList <<<");

        } catch (Exception e) {
            log.error("Error While getting Privacy CA List Downlaoding Certificate. " + e.getMessage());
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("result", false);
            return responseView;
        }
        return responseView;

    }

    public ModelAndView getTLSCertificate(HttpServletRequest req, HttpServletResponse res) {
        //log.info("In Data Contoller ManagementConsoleDataController.getTLSCertificate  >>");
        //ModelAndView responseView = new ModelAndView("SAMLDownload");
        ModelAndView responseView = new ModelAndView(new JSONView());
        res.setContentType("application/octet-stream ");
        res.setHeader("Content-Disposition", "attachment;filename=ssl.crt.pem");

        try {
            // Now get the API object from the session

            ApiClient apiObj = getApiClientService(req, ApiClient.class);
            if (apiObj == null) {
                throw new IllegalArgumentException("API client object cannot be null.");
            }
            Set<X509Certificate> certs = apiObj.getTlsCertificates();
            X509Certificate cert;
            Iterator i = certs.iterator();
            if (i.hasNext()) {
                cert = (X509Certificate) i.next();
            } else {
                throw new IllegalArgumentException("TLSCertificate not found.");
            }
            String ret = "-----BEGIN CERTIFICATE-----\n";
            ret += DatatypeConverter.printBase64Binary(cert.getEncoded());
            ret += "\n-----END CERTIFICATE-----";
            responseView.addObject("Certificate", ret);
            responseView.addObject("result", true);
            log.info("ManagementConsoleDataController.getTLSCertificate <<<");

            /*
             MSConfig msc = new MSConfig();
             Properties prop = msc.getDefaults();
             String file = prop.getProperty("mtwilson.tls.certificate.file");                                 
             String ret = readCertFile("/etc/intel/cloudsecurity/ssl.crt.pem");
             responseView.addObject("Certificate",ret);
             responseView.addObject("result",true);
             log.info("ManagementConsoleDataController.getPrivacyCACertificat <<<"); 
             */
        } catch (Exception e) {
            log.error("Error While getting TLS Downlaoding Certificate. " + e.getMessage());
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("result", false);
            return responseView;
        }
        return responseView;

    }
    //Begin_Added by Soni-Function to download SAML certificate

    public ModelAndView getSamlCertificate(HttpServletRequest req, HttpServletResponse res) {
        //log.info("In Data Contoller ManagementConsoleDataController.getSAMLCertificate  >>");
        //ModelAndView responseView = new ModelAndView("SAMLDownload");
        ModelAndView responseView = new ModelAndView(new JSONView());
        res.setContentType("application/octet-stream ");
        res.setHeader("Content-Disposition",
                "attachment;filename=mtwilson-saml.crt");

        try {
            // Now get the API object from the session

            ApiClient apiObj = getApiClientService(req, ApiClient.class);
            if (apiObj == null) {
                throw new IllegalArgumentException("API client object cannot be null.");
            }
            Set<X509Certificate> certs = apiObj.getSamlCertificates();
            X509Certificate cert;
            Iterator i = certs.iterator();
            if (i.hasNext()) {
                cert = (X509Certificate) i.next();
            } else {
                throw new IllegalArgumentException("TLSCertificate not found.");
            }
            String ret = "-----BEGIN CERTIFICATE-----\n";
            ret += DatatypeConverter.printBase64Binary(cert.getEncoded());
            ret += "\n-----END CERTIFICATE-----";
            responseView.addObject("Certificate", ret);
            responseView.addObject("result", true);
            log.info("ManagementConsoleDataController.getSAMLCertificate <<<");

            /*	 in.close();
             out.flush();
             out.close();
             */

        } catch (Exception e) {
            log.error("Error While getting Downlaoding Certificate. " + e.getMessage());
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("result", false);
            return responseView;


        }

        return responseView;

    }
    //End_Added by stdale-Function to download RootCA certificate

    public ModelAndView dummyPostbackFunction(HttpServletRequest req, HttpServletResponse res) {
        //log.info("ManagementConsoleDataController.dummyPostbackFunction >>");
        ModelAndView responseView = new ModelAndView(new JSONView());

        responseView.addObject("result", true);
        //log.info("ManagementConsoleDataController.dummyPostbackFunction <<<");
        return responseView;
    }

    //////////////////// from demo portal //////////////////////
    /**
     * Method is used to get Trust Status for Hosts to show on Home Screen.
     *
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView getDashBoardData(HttpServletRequest req, HttpServletResponse res) {
        //log.info("DemoPortalDataController.getDashBoardData >>");
        Map<Integer, List<HostDetailsEntityVO>> map;
        ModelAndView responseView = new ModelAndView(new JSONView());
        try {
            //Get map view for All Host based on the value of Page_NO(this values is available from TDPConfig) 
            map = getAllHostDetailsFromDB(req);

            //calling into a Service layer to get trust status for Host on Page No 1(using map.get(1)).
            responseView.addObject("hostVo", demoPortalServices.getTrustStatusForHost(map.get(1), getAttestationService(req, AttestationService.class), getTrustedCertificates(req)));

            //setting no of page required to show all Host data while applying pagination in JSP
            responseView.addObject("noOfPages", map.size());
        } catch (Exception e) {
            log.error(e.toString());
            responseView.addObject("hostVo", "");
            responseView.addObject("result", false);    
            if (e.getMessage() == null) {
                responseView.addObject("parseError", true);
                responseView.addObject("message", "Please review the server log for error details.");
            } else if (e.getMessage().toLowerCase().contains("currently there are no hosts configured")) {
                responseView.addObject("noHosts", true);
                responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            } else if (e.getMessage().toLowerCase().contains("peer not authenticated")) {
                // PEER NOT AUTH FIX
                responseView.addObject("ResetPeer", true);
                responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            } else if (e.getMessage().toLowerCase().contains("Cannot parse response")) {
                responseView.addObject("parseError", true);
                responseView.addObject("message", "There was a error parsing the response from the server.  Please reload the page to fix this issue");
            } else {
                responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            }
            return responseView;
        }
        responseView.addObject("result", true);
        responseView.addObject("message", "");
        //log.info("DemoPortalDataController.getDashBoardData <<<");
        return responseView;
    }

    /**
     * Method is used to get trust status of host for selected page no. Method
     * will get called when user choose any page no from pagination.
     *
     * @param req
     * @param res
     * @return
     */
    public ModelAndView getHostTrustSatusForPageNo(HttpServletRequest req, HttpServletResponse res) {
        //log.info("DemoPortalDataController.getHostTrustSatusForPageNo >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        try {
            //getting selected Page No.
            int selectedPage = Integer.parseInt(req.getParameter("pageNo"));
            HttpSession session = req.getSession();
            @SuppressWarnings("unchecked")
            //getting Map view of all Host stored into session while calling getDashBoardData().
            Map<Integer, List<HostDetailsEntityVO>> mapOfData = (Map<Integer, List<HostDetailsEntityVO>>) session.getAttribute("HostVoList");

            //calling into a Service layer to get trust status of Host for selected Page No.
            responseView.addObject("hostVo", demoPortalServices.getTrustStatusForHost(mapOfData.get(selectedPage), getAttestationService(req, AttestationService.class), getTrustedCertificates(req)));
            responseView.addObject("noOfPages", mapOfData.size());
        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
            responseView.addObject("hostVo", "");
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("result", true);
        responseView.addObject("message", "");
        //log.info("DemoPortalDataController.getHostTrustSatusForPageNo <<");
        return responseView;
    }

    /**
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView getVMwareSubGridData(HttpServletRequest req, HttpServletResponse res) {
        //log.info("DemoPortalDataController.getVMwareSubGridData >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        try {
            Map<String, HostVmMappingVO> vmMappingData = getHostVmMappingdata();
            responseView.addObject("VMsForHost", demoPortalServices.getVMsForHost(req.getParameter("hostName"), req.getParameter("hostID"), vmMappingData, getAttestationService(req, AttestationService.class)));
            saveHostVmMappingdata(vmMappingData);
        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("result", true);
        responseView.addObject("message", "");
        //log.info("DemoPortalDataController.getVMwareSubGridData <<<");
        return responseView;
    }

    /**
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView getHostTrustStatus(HttpServletRequest req, HttpServletResponse res) {
        //log.info("DemoPortalDataController.getHostTrustStatus >>");

        ModelAndView responseView = new ModelAndView(new JSONView());
        String block = "Success";
        log.info(block);
        try {
            responseView.addObject("hostVo", demoPortalServices.getSingleHostTrust(req.getParameter("hostName"), getAttestationService(req, AttestationService.class), getTrustedCertificates(req)));
        } catch (DemoPortalException e) {
//            block = "Fail";
            //log.error(e.toString());
            log.info("F-Error");
            //e.printStackTrace();
            String msg = StringEscapeUtils.escapeHtml(e.getMessage());
            if (msg.equals("Premature end of file.")) {
                msg = "Could not connect to host, please verify connection";
                e.printStackTrace();
            }
            responseView.addObject("hostVo", "");
            responseView.addObject("result", false);
            //responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("message", msg);
            return responseView;
        }
        log.info(block);
        responseView.addObject("result", true);
        responseView.addObject("message", "");
        //log.info("DemoPortalDataController.getHostTrustStatus <<<");
        return responseView;
    }

    /**
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView getAllOemInfo(HttpServletRequest req, HttpServletResponse res) {
        //log.info("DemoPortalDataController.getAllOemInfo >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        try {
            responseView.addObject("oemInfo", demoPortalServices.getAllOemInfo(getAttestationService(req, ApiClient.class)));
        } catch (DemoPortalException e) {
            log.error(e.toString());
            e.printStackTrace();
            responseView.addObject("oemInfo", "");
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("result", true);
        responseView.addObject("message", "");
        //log.info("DemoPortalDataController.getAllOemInfo <<<");
        return responseView;
    }

    /**
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView getOSVMMInfo(HttpServletRequest req, HttpServletResponse res) {
        //log.info("DemoPortalDataController.getOSVMMInfo >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        try {
            responseView.addObject("osInfo", demoPortalServices.getOSAndVMMInfo(getAttestationService(req, ApiClient.class)));
        } catch (DemoPortalException e) {
            log.error(e.toString());
            e.printStackTrace();
            responseView.addObject("osInfo", "");
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("result", true);
        responseView.addObject("message", "");
        //log.info("DemoPortalDataController.getOSVMMInfo <<<");
        return responseView;
    }

    public ModelAndView saveNewHostInfo(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.saveNewHostInfo >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        String hostObject;
        boolean newhost;
        try {
            hostObject = req.getParameter("hostObject");
            if (hostObject == null) {
                throw new IllegalArgumentException("Host object cannot be null.");
            }
            newhost = Boolean.parseBoolean(req.getParameter("newhost"));
        } catch (Exception e1) {
            hostObject = null;
            newhost = false;
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e1.getMessage()));
        }
        //System.out.println(hostObject);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        HostDetailsEntityVO dataVO;

        try {
            dataVO = mapper.readValue(hostObject, HostDetailsEntityVO.class);
        } catch (JsonParseException e) {
            log.error("Error While Parsing request parameters Data. " + e.getMessage());
            responseView.addObject("result", false);
            responseView.addObject("message", "Error While Parsing request parameters Data.");
            return responseView;
        } catch (JsonMappingException e) {
            log.error("Error While Mapping request parameters to Mle Data Object. " + StringEscapeUtils.escapeHtml(e.getMessage()));
            responseView.addObject("result", false);
            responseView.addObject("message", "Error While Mapping request parameters to Mle Data Object.");
            return responseView;
        } catch (IOException e) {
            log.error("IO Exception " + e.getMessage());
            responseView.addObject("result", false);
            responseView.addObject("message", "Error While Mapping request parameters to Mle Data Object.");
            return responseView;
        }

        dataVO.setUpdatedOn(new Date(System.currentTimeMillis()));

        try {
            if (newhost) {
                //System.err.println("dataForNew : "+dataVO);
                responseView.addObject("result", demoPortalServices.saveNewHostData(dataVO, getAttestationService(req, AttestationService.class)));
            } else {
                //System.err.println("dataForOLD : "+dataVO);
                responseView.addObject("result", demoPortalServices.updateHostData(dataVO, getAttestationService(req, AttestationService.class)));
            }
        } catch (DemoPortalException e) {
            log.error(e.getMessage());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        //log.info("WLMDataController.saveNewHostInfo <<<");
        return responseView;

    }

    /**
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView getInfoForHostID(HttpServletRequest req, HttpServletResponse res) {
        //log.info("DemoPortalDataController.getInfoForHostID >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        try {
            String hostName = req.getParameter("hostName");
            responseView.addObject("hostData", demoPortalServices.getSingleHostDetailFromDB(hostName, getAttestationService(req, AttestationService.class)));
        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
            responseView.addObject("oemInfo", "");
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("result", true);
        responseView.addObject("message", "");
        //log.info("DemoPortalDataController.getInfoForHostID <<<");
        return responseView;
    }

    /**
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView deleteHostDetails(HttpServletRequest req, HttpServletResponse res) {
        //log.info("DemoPortalDataController.deleteHostDetails >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        int selectedPage;
        try {
            selectedPage = Integer.parseInt(req.getParameter("selectedPageNo"));
            Map<String, HostVmMappingVO> vmMappingData = getHostVmMappingdata();
            boolean updateDone = demoPortalServices.deleteHostDetails(req.getParameter("hostID"), req.getParameter("hostName"), getAttestationService(req, AttestationService.class), vmMappingData);
            if (updateDone) {
                saveHostVmMappingdata(vmMappingData);
                Map<Integer, List<HostDetailsEntityVO>> mapOfData = getAllHostDetailsFromDB(req);

                if (selectedPage > mapOfData.size()) {
                    selectedPage = mapOfData.size();
                }
                responseView.addObject("hostVo", mapOfData.get(selectedPage));
                responseView.addObject("noOfPages", mapOfData.size());
                responseView.addObject("result", updateDone);
            } else {
                log.error("Error Wile deleting OS Data. Server Error.");
                responseView.addObject("result", false);
                responseView.addObject("message", "Api Client return false.");
            }
        } catch (DemoPortalException e) {
            log.error(e.toString());
            e.printStackTrace();
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("message", "");
        //log.info("DemoPortalDataController.deleteHostDetails<<<");
        return responseView;
    }

    /**
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView powerOnOffVM(HttpServletRequest req, HttpServletResponse res) {
        //log.info("DemoPortalDataController.powerOnOffVM >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        try {

            String hostName = req.getParameter("hostName");
            String vmName = req.getParameter("vmName");
            String hostID = req.getParameter("hostID");
            short trustPolicy = Short.parseShort(req.getParameter("trustPolicy"));
            short locationPloicy = Short.parseShort(req.getParameter("locationPloicy"));
            boolean isPowerOnCommand = Boolean.parseBoolean(req.getParameter("isPowerON"));

            responseView.addObject("result", demoPortalServices.powerOnOffHostVMs(hostName, vmName, hostID, isPowerOnCommand, getAttestationService(req, AttestationService.class)));

            Map<String, HostVmMappingVO> vmMappingData = getHostVmMappingdata();
            HostVmMappingVO hostVmMappingVO = vmMappingData.get(hostID + HelperConstant.VM_HOST_MAPPING_SEPERATOR + vmName);
            if (hostVmMappingVO == null) {
                throw new IllegalArgumentException("hostVmMappingVO cannot be null.");
            }
            hostVmMappingVO.setTrustedHostPolicy(trustPolicy);
            hostVmMappingVO.setLocationPolicy(locationPloicy);
            if (isPowerOnCommand) {
                hostVmMappingVO.setVmStatus((short) 1);
            } else {
                hostVmMappingVO.setVmStatus((short) 0);
            }
            vmMappingData.put(hostID + HelperConstant.VM_HOST_MAPPING_SEPERATOR + vmName, hostVmMappingVO);
            saveHostVmMappingdata(vmMappingData);

        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("message", "");
        //log.info("DemoPortalDataController.powerOnOffVM<<<");
        return responseView;
    }

    /**
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView migrateVMToHost(HttpServletRequest req, HttpServletResponse res) {
        //log.info("DemoPortalDataController.migrateVMToHost >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        try {

            String hostToTransfer = req.getParameter("hostToTransfer");
            String sourceHost = req.getParameter("sourceHost");
            String vmName = req.getParameter("vmName");
            String hostID = req.getParameter("hostID");
            String targetHosthostID = req.getParameter("targetHosthostID");
            short trustPolicy = Short.parseShort(req.getParameter("trustPolicy"));
            short locationPloicy = Short.parseShort(req.getParameter("locationPloicy"));

            responseView.addObject("result", demoPortalServices.migrateVMToHost(vmName, sourceHost, hostToTransfer, hostID, getAttestationService(req, AttestationService.class)));

            Map<String, HostVmMappingVO> vmMappingData = getHostVmMappingdata();
            HostVmMappingVO hostVmMappingVO = vmMappingData.get(hostID + HelperConstant.VM_HOST_MAPPING_SEPERATOR + vmName);
            if (hostVmMappingVO == null) {
                throw new IllegalArgumentException("hostVmMappingVO cannot be null.");
            }
            hostVmMappingVO.setHostId(targetHosthostID);
            hostVmMappingVO.setTrustedHostPolicy(trustPolicy);
            hostVmMappingVO.setLocationPolicy(locationPloicy);

            vmMappingData.put(targetHosthostID + HelperConstant.VM_HOST_MAPPING_SEPERATOR + vmName, hostVmMappingVO);
            vmMappingData.remove(hostID + HelperConstant.VM_HOST_MAPPING_SEPERATOR + vmName);
            saveHostVmMappingdata(vmMappingData);

        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("message", "");
        //log.info("DemoPortalDataController.migrateVMToHost<<<");
        return responseView;
    }

    /**
     * Method to get Host list to for View Host page.
     *
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView getAllHostForView(HttpServletRequest req, HttpServletResponse res) {
        //log.info("DemoPortalDataController.getAllHostForView >>");
        Map<Integer, List<HostDetailsEntityVO>> map;
        ModelAndView responseView = new ModelAndView(new JSONView());
        try {
            map = getAllHostDetailsFromDB(req);

            responseView.addObject("hostVo", map.get(1));
            responseView.addObject("noOfPages", map.size());
        } catch (DemoPortalException e) {
            log.error(e.toString());
            e.printStackTrace();
            responseView.addObject("hostVo", "");
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("result", true);
        responseView.addObject("message", "");
        //log.info("DemoPortalDataController.getAllHostForView<<<");
        return responseView;
    }

    /**
     * Method to get Host list to for View Host Page for given page no.
     *
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView getHostForViewForPage(HttpServletRequest req, HttpServletResponse res) {
        //log.info("DemoPortalDataController.getHostForViewForPage >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        try {
            int selectedPage = Integer.parseInt(req.getParameter("pageNo"));
            HttpSession session = req.getSession();
            @SuppressWarnings("unchecked")
            Map<Integer, List<HostDetailsEntityVO>> mapOfData = (Map<Integer, List<HostDetailsEntityVO>>) session.getAttribute("HostVoList");
            responseView.addObject("hostVo", mapOfData.get(selectedPage));
        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
            responseView.addObject("hostVo", "");
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("result", true);
        responseView.addObject("message", "");
        //log.info("DemoPortalDataController.getHostForViewForPage<<<");
        return responseView;
    }

    /**
     * Method to get Trust Verification Details using SAML.
     *
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView trustVerificationDetailsXML(HttpServletRequest req, HttpServletResponse res) {
        //log.info("DemoPortalDataController.trustVerificationDetailsXML >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        String hostName = req.getParameter("hostName");
        try {
            responseView.addObject("trustSamlDetails", demoPortalServices.trustVerificationDetails(hostName, getAttestationService(req, AttestationService.class), getTrustedCertificates(req)));
            responseView.addObject("hostName", hostName);
            responseView.addObject("result", true);
        } catch (DemoPortalException e) {
            log.error(e.toString());
            e.printStackTrace();
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            e.printStackTrace();
            return responseView;
        }
        responseView.addObject("message", "");
        responseView.addObject("result", true);
        return responseView;
    }

    /**
     * Method to Bulk Update trust status for selected host.
     *
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView updateTrustForSelected(HttpServletRequest req, HttpServletResponse res) {
        //log.info("DemoPortalDataController.updateTrustForSelected >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        String hostList = req.getParameter("selectedHost");
        List<String> hosts = null;

        if (hostList != null) {
            hosts = Arrays.asList(hostList.split(";"));
        }

        try {
            responseView.addObject("result", demoPortalServices.getBlukTrustUpdatedForHost(hosts, getAttestationService(req, ApiClient.class), getTrustedCertificates(req)));
        } catch (DemoPortalException e) {
            e.printStackTrace();
            log.error(e.toString());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("message", "");
        //log.info("DemoPortalDataController.updateTrustForSelected<<<");
        return responseView;
    }

    /**
     * Method to Bulk Update trust status for selected host.
     *
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView getHostsReport(HttpServletRequest req, HttpServletResponse res) {
        //log.info("DemoPortalDataController.getHostsReport >>");
        ModelAndView responseView = new ModelAndView(new JSONView());

        String[] list = req.getParameterValues("selectedHost");
        if (list == null) {
            responseView.addObject("message", "No hosts were selected for reports.");
            log.info("DemoPortalDataController.getHostsReport<<<");
            return responseView;
        }
        List<String> hosts = Arrays.asList(list);

        try {
            responseView.addObject("reports", demoPortalServices.getHostTrustReport(hosts, getAttestationService(req, ApiClient.class)));
            responseView.addObject("result", true);
        } catch (DemoPortalException e) {
            log.error(e.getMessage());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("message", "");
        //log.info("DemoPortalDataController.getHostsReport<<<");
        return responseView;
    }

    public ModelAndView getFailurereportForHost(HttpServletRequest req, HttpServletResponse res) {
        //log.info("DemoPortalDataController.getFailurereportForHost >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        try {
            responseView.addObject("reportdata", demoPortalServices.getFailureReportData(req.getParameter("hostName"), getAttestationService(req, ApiClient.class)));
            responseView.addObject("result", true);
        } catch (Exception e) {
            log.error(e.getMessage());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("message", "");
        //log.info("DemoPortalDataController.getFailurereportForHost <<");
        return responseView;
    }

    public void setDemoPortalServices(IDemoPortalServices demoPortalServices) {
        this.demoPortalServices = demoPortalServices;
    }

    /**
     * This method is used as a utility method to get Map Views for all host
     * based on Page_no value.
     *
     * @param req
     * @return Map<Integer, List<HostDetailsEntityVO>> @throws
     * DemoPortalException
     */
    private Map<Integer, List<HostDetailsEntityVO>> getAllHostDetailsFromDB(HttpServletRequest req) throws DemoPortalException {
        Map<Integer, List<HostDetailsEntityVO>> map = new HashMap<Integer, List<HostDetailsEntityVO>>();

        //Get List of all host available. 
        List<HostDetailsEntityVO> listOfVos = demoPortalServices.getHostListFromDB(getAttestationService(req, AttestationService.class));
        int no_row_per_page = Integer.parseInt(TDPConfig.getConfiguration().getString("mtwilson.tdbp.paginationRowCount", "10")); // providing default value because it's not a critical configuration setting

        //Divide List of all host into a subList based on the value of host per page. 
        List<List<HostDetailsEntityVO>> list = Lists.partition(listOfVos, no_row_per_page);

        //Creating a Map view of host list based on the Page No.
        int i = 1;
        for (List<HostDetailsEntityVO> listForMap : list) {
            map.put(i, listForMap);
            i++;
        }

        //setting map into session attribute;
        HttpSession session = req.getSession();
        session.setAttribute("HostVoList", map);
        return map;
    }

    /**
     * This method will return a AttestationService/ApiClient Object from a
     * Session. This object is stored into Session at time of user login. Check
     * CheckLoginController.java for more Clarification.
     *
     * @param req
     * @return
     * @return AttestationService
     * @throws DemoPortalException
     */
    @SuppressWarnings("unchecked")
    private <T> T getAttestationService(HttpServletRequest req, Class<T> type) throws DemoPortalException {

        //getting already created session object by passing false while calling into getSession();
        HttpSession session = req.getSession(false);
        T service = null;
        if (session != null) {
            try {

                //getting ApiClient Object from Session and downcast that object to Type T.  
                service = (T) session.getAttribute("apiClientObject");
            } catch (Exception e) {
                log.error("Error while creating ApiCliennt Object. " + e.getMessage());
                throw new DemoPortalException("Error while creating ApiCliennt Object. " + e.getMessage(), e);
            }

        }
        return service;
    }

    /**
     * This method will return a X509Certificate Object from a Request Session.
     * This object is stored into Session at time of user login. Check
     * CheckLoginController.java for more Clarification.
     *
     * @param req
     * @return
     * @throws DemoPortalException
     */
    private X509Certificate[] getTrustedCertificates(HttpServletRequest req) throws DemoPortalException {
        HttpSession session = req.getSession(false);
        X509Certificate[] trustedCertificate;
        if (session != null) {
            try {
                //getting Object from Session and downcast that object to X509Certificate. 
                trustedCertificate = (X509Certificate[]) session.getAttribute("trustedCertificates");
            } catch (Exception e) {
                log.error("Error while creating ApiCliennt Object. " + e.getMessage());
                throw new DemoPortalException("Error while creating ApiCliennt Object. " + e.getMessage(), e);
            }

        } else {
            return null;
        }
        return trustedCertificate;
    }

    private Map<String, HostVmMappingVO> getHostVmMappingdata() {
        ServletContext application = getServletContext();
        @SuppressWarnings("unchecked")
        Map<String, HostVmMappingVO> vmMappingData = (Map<String, HostVmMappingVO>) application.getAttribute("HostVmMapping-DataTable");
        if (vmMappingData == null || vmMappingData.size() <= 0) {
            vmMappingData = new HashMap<String, HostVmMappingVO>();
            application.setAttribute("HostVmMapping-DataTable", vmMappingData);
        }
        return vmMappingData;
    }

    private void saveHostVmMappingdata(Map<String, HostVmMappingVO> vmMappingData) {
        ServletContext application = getServletContext();
        application.setAttribute("HostVmMapping-DataTable", vmMappingData);
    }

    ///////////////////////// from whitelist portal //////////////////////
    /**
     * Method to add OS Data into REST Services.
     *
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView addOSData(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.addOSData >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        try {
            OSDataVO dataVONew = new OSDataVO();

            //Getting OS data from request parameter
            dataVONew.setOsName(req.getParameter("osName"));
            dataVONew.setOsVersion(req.getParameter("osVersion"));
            dataVONew.setOsDescription(req.getParameter("osDescription"));

            //Calling into Service Layer(OSClientServiceImpl) to add OS Data.
            responseView.addObject("result", osClientService.addOSInfo(dataVONew, getWhitelistService(req)));
        } catch (WLMPortalException e) {
            log.error("Error Wile Adding OS Data. Root cause " + e.getMessage());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
        }
        //log.info("WLMDataController.addOSData <<<");
        return responseView;
    }

    /**
     * Method to Update previously add OS Data into a REST Services.
     *
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView updateOSData(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.updateOSData >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        int selectedPage;
        try {

            //Get Current select page no used in pagination.
            selectedPage = Integer.parseInt(req.getParameter("selectedPageNo"));

            //Get updated OS data from req object and create OSDataVO Object from it.
            OSDataVO dataVONew = new OSDataVO();
            dataVONew.setOsName(req.getParameter("osName"));
            dataVONew.setOsVersion(req.getParameter("osVer"));
            dataVONew.setOsDescription(req.getParameter("inputDec"));

            //Calling into Service Layer(OSClientServiceImpl) to update OS Data.
            boolean updateDone = osClientService.updateOSInfo(dataVONew, getWhitelistService(req));

            // Once OS data is updated, get List of all OS for a current page to show while pagination.  
            if (updateDone) {
                //Get map view of OS data from Services based on there page no.
                Map<Integer, List<OSDataVO>> mapOfData = getPartitionListOfAllOS(req);

                responseView.addObject("OSDataVo", mapOfData.get(selectedPage));
                responseView.addObject("noOfPages", mapOfData.size());

                responseView.addObject("result", updateDone);
            } else {
                log.error("Error Wile Editing OS Data. Api Client return false.");
                responseView.addObject("result", false);
                responseView.addObject("message", "Error Wile Editing OS Data. Api Client return false.");
            }
        } catch (WLMPortalException e) {
            log.error("Error Wile Editing OS Data. Root cause " + e.getMessage());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
        }
        //log.info("WLMDataController.updateOSData <<<");
        return responseView;
    }

    /**
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView deleteOSData(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.deleteOSData >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        int selectedPage;
        try {
            selectedPage = Integer.parseInt(req.getParameter("selectedPageNo"));
            OSDataVO dataVONew = new OSDataVO();
            dataVONew.setOsName(req.getParameter("osName"));
            dataVONew.setOsVersion(req.getParameter("osVer"));
            dataVONew.setOsDescription(req.getParameter("inputDec"));

            boolean updateDone = osClientService.deleteOS(dataVONew, getWhitelistService(req));
            if (updateDone) {
                Map<Integer, List<OSDataVO>> mapOfData = getPartitionListOfAllOS(req);

                if (selectedPage > mapOfData.size()) {
                    selectedPage = mapOfData.size();
                }
                responseView.addObject("OSDataVo", mapOfData.get(selectedPage));
                responseView.addObject("noOfPages", mapOfData.size());
                responseView.addObject("result", updateDone);
            } else {
                log.error("Error Wile deleting OS Data. Server Error.");
                responseView.addObject("result", false);
                responseView.addObject("message", "Api Client return false.");
            }

        } catch (WLMPortalException e) {
            log.error("Error Wile deleting OS Data. Root cause " + e.getMessage());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
        }
        //log.info("WLMDataController.deleteOSData <<<");
        return responseView;

    }

    public ModelAndView getHostOSForVMM(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.getHostOSForVMM >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        List<VmmHostDataVo> list = new ArrayList<VmmHostDataVo>();
        VmmHostDataVo dataVo;
        List<String> VmmNames = getVMMNameList(WLMPConfig.getConfiguration().getString("mtwilson.wlmp.openSourceHypervisors"));

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
                    } else {
                        dataVo.setAttestationType("PCR");
                    }
                } else {
                    dataVo.setVmmNames(VmmNames);
                    dataVo.setAttestationType("PCR");
                }
                list.add(dataVo);
            }
        } catch (WLMPortalException e) {
            log.error("Error While getting Host OS Data for VMM. Root cause " + e.getStackTrace());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("HostList", list);
        responseView.addObject("result", true);
        responseView.addObject("message", "");
        //log.info("WLMDataController.getHostOSForVMM <<<");
        return responseView;

    }

    public ModelAndView getHostOSForBios(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.getHostOSForBios >>");
        ModelAndView responseView = new ModelAndView(new JSONView());

        List<OEMDataVO> list = null;
        try {
            list = oemClientService.getAllOEM(getWhitelistService(req));
            responseView.addObject("result", true);
        } catch (WLMPortalException e) {
            log.error("Error While getting Host OS Data for BIOS. Root cause " + e.getStackTrace());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
        }
        responseView.addObject("HostList", list);
        responseView.addObject("message", "");

        //log.info("WLMDataController.getHostOSForBios <<<");
        return responseView;

    }

    public ModelAndView getUploadedMenifestFile(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.getUploadedMenifestFile >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        Object manifest = req.getSession().getAttribute("manifestValue");
        boolean result = false;
        if (manifest != null) {
            result = true;
            responseView.addObject("manifestValue", manifest);
        }
        responseView.addObject("result", result);
        //log.info("WLMDataController.getUploadedMenifestFile <<<");
        return responseView;

    }

    public ModelAndView uploadManifest(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.uploadManifest >>");
        req.getSession().removeAttribute("manifestValue");
        ModelAndView responseView = new ModelAndView(new JSONView());
        List<Map<String, String>> manifestValue = new ArrayList<Map<String, String>>();

        // Check that we have a file upload request
        boolean isMultipart = ServletFileUpload.isMultipartContent(req);
        log.debug("Is content multipart? {}", isMultipart);
        if (!isMultipart) {
            responseView.addObject("result", false);
            return responseView;
        }

        // Create a factory for disk-based file items
        FileItemFactory factory = new DiskFileItemFactory();

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Parse the request
        try {
            @SuppressWarnings("unchecked")
            List<FileItem> items = upload.parseRequest(req);

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
                            } else {
                                responseView.addObject("result", false);
                                return responseView;
                            }
                        }
                    }
                }
            }
            //log.info("Uploaded Content :: "+manifestValue.toString());
            req.getSession().setAttribute("manifestValue", manifestValue);
            /*responseView.addObject("manifestValue",manifestValue);*/
            responseView.addObject("result", manifestValue.size() > 0 ? true : false);

        } catch (FileUploadException e) {
            e.printStackTrace();
            responseView.addObject("result", false);
        } catch (Exception e) {
            e.printStackTrace();
            responseView.addObject("result", false);
        }

        //log.info("WLMDataController.uploadManifest <<<");
        return responseView;

    }

    public ModelAndView viewSingleMLEData(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.viewSingleMLEData>>");
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
            } else {
                dataVO.setOemName(req.getParameter("oemName"));
            }
        } catch (Exception e) {
            log.error("Error While in request parameters Data. " + e.getMessage());
            responseView.addObject("result", false);
            responseView.addObject("message", "Error While request parameters are Null. Please check.");
        }

        try {
            detailMLEVO = mleClientService.getSingleMleData(dataVO, getWhitelistService(req));
            responseView.addObject("dataVo", detailMLEVO);
            responseView.addObject("result", true);
        } catch (WLMPortalException e) {
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            log.error(e.toString());
        }

        // Now that we got the details of the MLE, we need to get the host details that
        // was used for white listing this MLE.
        try {
            responseView.addObject("mleSource", mleClientService.getMleSourceHost(detailMLEVO, getWhitelistService(req)));
        } catch (WLMPortalException e) {
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            log.error(e.toString());
        }

        log.info("WLMDataController.viewSingleMLEData <<<");
        return responseView;
    }

    public ModelAndView getWhiteListForMle(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.getWhiteListForMle>>");
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
            } else {
                dataVO.setOemName(req.getParameter("oemName"));
            }
        } catch (Exception e) {
            log.error("Error While in request parameters Data. " + e.getMessage());
            responseView.addObject("result", false);
            responseView.addObject("message", "Error While request parameters are Null. Please check.");
        }

        try {
            responseView.addObject("whiteList", mleClientService.getManifestListForModuleTypeMle(dataVO, getWhitelistService(req)));
            responseView.addObject("result", true);
        } catch (WLMPortalException e) {
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            log.error(e.toString());
        }

        //log.info("WLMDataController.getWhiteListForMle <<<");
        return responseView;

    }

    @SuppressWarnings("serial")
    public ModelAndView getAddMle(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.getAddMle>>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        String mleOb = null;
        boolean newMle = false;
        try {
            mleOb = req.getParameter("mleObject");
            newMle = Boolean.parseBoolean(req.getParameter("newMle"));
        } catch (Exception e1) {
            log.error("Error While in request parameters Data. " + e1.getMessage());
            responseView.addObject("result", false);
            responseView.addObject("message", "Error While request parameters are Null. Please check.");
        }
        log.debug("Adding MLE: {}", mleOb);
        MLEDataVO dataVO = new MLEDataVO();


        try {
            Type mleDataType = new TypeToken<MLEDataVO>() {
            }.getType();
            dataVO = new Gson().fromJson(mleOb, mleDataType);

            for (Map<String, String> manifestMap : dataVO.getManifestList()) {
                for (Map.Entry<String, String> manifest : manifestMap.entrySet()) {
                    manifest.setValue(manifest.getValue().toUpperCase());
                }
            }

            System.out.println("dataVo >>" + dataVO);
            if (newMle) {
                responseView.addObject("result", mleClientService.addMLEInfo(dataVO, getWhitelistService(req)));
            } else {
                responseView.addObject("result", mleClientService.updateMLEInfo(dataVO, getWhitelistService(req)));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }

        //log.info("WLMDataController.getAddMle <<<");
        return responseView;

    }

    public ModelAndView deleteMLEData(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.deleteMLEData>>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        MLEDataVO dataVO = new MLEDataVO();
        int selectedPage;
        try {
            selectedPage = Integer.parseInt(req.getParameter("selectedPageNo"));
            dataVO.setMleName(req.getParameter("mleName"));
            dataVO.setMleVersion(req.getParameter("mleVersion"));

            String mleType = req.getParameter("mleType");

            if (mleType != null && mleType.equalsIgnoreCase("VMM")) {
                dataVO.setOsName(req.getParameter("osName"));
                dataVO.setOsVersion(req.getParameter("osVersion"));
            } else {
                dataVO.setOemName(req.getParameter("oemName"));
            }
        } catch (Exception e) {
            log.error("Error While in request parameters Data. " + e.getMessage());
            responseView.addObject("result", false);
            responseView.addObject("message", "Error While request parameters are Null. Please check.");
            return responseView;
        }

        try {
            boolean deleteDone = mleClientService.deleteMLE(dataVO, getWhitelistService(req));
            if (deleteDone) {
                Map<Integer, List<MLEDataVO>> mapOfData = getPartitionListOfAllMle(req);

                if (selectedPage > mapOfData.size()) {
                    selectedPage = mapOfData.size();
                }

                responseView.addObject("MLEDataVo", mapOfData.get(selectedPage));
                responseView.addObject("noOfPages", mapOfData.size());
                responseView.addObject("result", true);
            }
        } catch (WLMPortalException e) {
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            log.error(e.toString());
            return responseView;
        }

        //log.info("WLMDataController.deleteMLEData <<<");
        return responseView;

    }

    public ModelAndView addOEMData(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.addOEMData>>");
        ModelAndView responseView = new ModelAndView(new JSONView());

        try {
            OEMDataVO dataVONew = new OEMDataVO();
            dataVONew.setOemName(req.getParameter("oemName"));
            dataVONew.setOemDescription(req.getParameter("oemDescription"));

            System.out.println("New OEM data >>> " + dataVONew.toString());
            responseView.addObject("result", oemClientService.addOEMInfo(dataVONew, getWhitelistService(req)));

        } catch (WLMPortalException e) {
            log.error("Error Wile Adding OEM Data. Root cause " + e.getStackTrace());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
        }

        //log.info("WLMDataController.addOEMData <<<");
        return responseView;

    }

    public ModelAndView updateOEMData(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.updateOEMData >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        int selectedPage;
        try {
            selectedPage = Integer.parseInt(req.getParameter("selectedPageNo"));
            OEMDataVO dataVONew = new OEMDataVO();
            dataVONew.setOemName(req.getParameter("oemName"));
            dataVONew.setOemDescription(req.getParameter("inputDec"));
            boolean updateDone = oemClientService.updateOEMInfo(dataVONew, getWhitelistService(req));
            if (updateDone) {
                Map<Integer, List<OEMDataVO>> mapOfData = getPartitionListOfAllOEM(req);

                responseView.addObject("OEMDataVo", mapOfData.get(selectedPage));
                responseView.addObject("noOfPages", mapOfData.size());

                responseView.addObject("result", updateDone);
            }

        } catch (Exception e) {
            log.error("Error Wile Editing OEM Data. Root cause " + e.getMessage());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
        }
        //log.info("WLMDataController.updateOEMData <<<");
        return responseView;

    }

    public ModelAndView deleteOEMData(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.deleteOEMData >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        int selectedPage;
        try {
            selectedPage = Integer.parseInt(req.getParameter("selectedPageNo"));
            OEMDataVO dataVONew = new OEMDataVO();
            dataVONew.setOemName(req.getParameter("oemName"));
            //dataVONew.setOemDescription(req.getParameter("inputDec"));
            boolean updateDone = oemClientService.deleteOEM(dataVONew, getWhitelistService(req));
            if (updateDone) {
                Map<Integer, List<OEMDataVO>> mapOfData = getPartitionListOfAllOEM(req);

                if (selectedPage > mapOfData.size()) {
                    selectedPage = mapOfData.size();
                }
                responseView.addObject("OEMDataVo", mapOfData.get(selectedPage));
                responseView.addObject("noOfPages", mapOfData.size());
                responseView.addObject("result", updateDone);
            }

        } catch (Exception e) {
            log.error("Error Wile Deleting OEM Data. Root cause " + e.getMessage());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
        }
        //log.info("WLMDataController.deleteOEMData <<<");
        return responseView;

    }

    /*
     * Method to get All MLE Data for Pagination
     */
    public ModelAndView getViewMle(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.getViewMle >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        try {
            Map<Integer, List<MLEDataVO>> map = getPartitionListOfAllMle(req);

            responseView.addObject("MLEDataVo", map.get(1));
            responseView.addObject("noOfPages", map.size());
        } catch (WLMPortalException e) {
            log.error(e.toString());
            e.printStackTrace();
            responseView.addObject("MLEDataVo", "");
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("result", true);
        responseView.addObject("message", "");
        //log.info("WLMDataController.getViewMle <<");
        return responseView;
    }

    @SuppressWarnings("unchecked")
    public ModelAndView getViewMleForPageNo(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.getViewMleForPageNo >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        try {
            HttpSession session = req.getSession();
            Map<Integer, List<MLEDataVO>> map = (Map<Integer, List<MLEDataVO>>) session.getAttribute("MleList");
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
        //log.info("WLMDataController.getViewMleForPageNo <<");
        return responseView;
    }

    /*Methods to get data for pagination for OS Component */
    public ModelAndView getAllOSList(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.getAllOSList >>");
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
        //log.info("WLMDataController.getAllOSList <<");
        return responseView;
    }

    @SuppressWarnings("unchecked")
    public ModelAndView getViewOSForPageNo(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.getViewOSForPageNo >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        try {
            HttpSession session = req.getSession();
            Map<Integer, List<MLEDataVO>> map = (Map<Integer, List<MLEDataVO>>) session.getAttribute("OSList");
            responseView.addObject("OSDataVo", map.get(Integer.parseInt(req.getParameter("pageNo"))));
        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
            responseView.addObject("OSDataVo", "");
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("result", true);
        responseView.addObject("message", "");
        //log.info("WLMDataController.getViewOSForPageNo <<");
        return responseView;
    }

    /*Methods to get data for pagination for OEM Component */
    public ModelAndView getAllOEMList(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.getAllOEMList >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        try {
            Map<Integer, List<OEMDataVO>> map = getPartitionListOfAllOEM(req);

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
        //log.info("WLMDataController.getAllOEMList <<");
        return responseView;
    }

    @SuppressWarnings("unchecked")
    public ModelAndView getViewOEMForPageNo(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMDataController.getViewOEMForPageNo >>");
        ModelAndView responseView = new ModelAndView(new JSONView());
        try {
            HttpSession session = req.getSession();
            Map<Integer, List<MLEDataVO>> map = (Map<Integer, List<MLEDataVO>>) session.getAttribute("OEMList");
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
        //log.info("WLMDataController.getViewOEMForPageNo <<");
        return responseView;
    }

    //Method is used to return List of all hypervisors for both VMWare and OpenSource configure in whitelist-portal.properties 
    private List<String> getVMMNameList(String openSourceHypervisors) {
        return Arrays.asList(openSourceHypervisors.split(";"));
    }

    /**
     * Method to get a Map View for all MLE data from REST Services according to
     * there page no. Return data is used in pagination.
     *
     * @param req object of HttpServletRequest
     * @return
     * @throws WLMPortalException
     */
    private Map<Integer, List<MLEDataVO>> getPartitionListOfAllMle(HttpServletRequest req) throws WLMPortalException {
        Map<Integer, List<MLEDataVO>> map = new HashMap<Integer, List<MLEDataVO>>();

        //Get List of all MLE.
        List<MLEDataVO> dataVOs = mleClientService.getAllMLE(getWhitelistService(req));
        int no_row_per_page = WLMPConfig.getConfiguration().getInt("mtwilson.wlmp.pagingSize", DEFAULT_ROWS_PER_PAGE);

        //Divide List of all MLE into a subList based on the value of host per page.
        List<List<MLEDataVO>> list = Lists.partition(dataVOs, no_row_per_page);

        //Creating a Map view of MLE list based on the Page No.
        int i = 1;
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
     * Method to get a Map View for all OS data from REST Services according to
     * there page no. Return data is used in pagination.
     *
     * @param req object of HttpServletRequest
     * @return
     * @throws WLMPortalException
     */
    private Map<Integer, List<OSDataVO>> getPartitionListOfAllOS(HttpServletRequest req) throws WLMPortalException {
        Map<Integer, List<OSDataVO>> mapOfData = new HashMap<Integer, List<OSDataVO>>();

        //Get List of all OS.
        List<OSDataVO> dataVOs = osClientService.getAllOS(getWhitelistService(req));
        int no_row_per_page = WLMPConfig.getConfiguration().getInt("mtwilson.wlmp.pagingSize", DEFAULT_ROWS_PER_PAGE);

        //Divide List of all OS into a subList based on the value of host per page. 
        List<List<OSDataVO>> list = Lists.partition(dataVOs, no_row_per_page);

        //Creating a Map view of OS list based on the Page No.
        int i = 1;
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
     * Method to get a Map View for all OEM data from REST Services according to
     * there page no. Return data is used in pagination.
     *
     * @param req object of HttpServletRequest
     * @return
     * @throws WLMPortalException
     */
    private Map<Integer, List<OEMDataVO>> getPartitionListOfAllOEM(HttpServletRequest req) throws WLMPortalException {
        Map<Integer, List<OEMDataVO>> map = new HashMap<Integer, List<OEMDataVO>>();

        //Get List of all OEM.
        List<OEMDataVO> dataVOs = oemClientService.getAllOEM(getWhitelistService(req));
        int no_row_per_page = WLMPConfig.getConfiguration().getInt("mtwilson.wlmp.pagingSize", DEFAULT_ROWS_PER_PAGE);

        //Divide List of all OEM into a subList based on the value of host per page. 
        List<List<OEMDataVO>> list = Lists.partition(dataVOs, no_row_per_page);
        int i = 1;
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
     * This method will return a WhitelistService Object from a Session. This
     * object is stored into Session at time of user login. Check
     * CheckLoginController.java for more Clarification.
     *
     * @param req
     * @return
     * @throws WLMPortalException
     */
    private WhitelistService getWhitelistService(HttpServletRequest req) throws WLMPortalException {

        //getting already created session object by passing false while calling into getSession();
        HttpSession session = req.getSession(false);
        WhitelistService service = null;
        if (session != null) {
            try {
                //getting WhitelistService Object from Session, stored while log-in time.  
                service = (WhitelistService) session.getAttribute("apiClientObject");
            } catch (Exception e) {
                log.error("Error while creating ApiCliennt Object. " + e.getMessage());
                throw new WLMPortalException("Error while creating ApiCliennt Object. " + StringEscapeUtils.escapeHtml(e.getMessage()), e);
            }
        }
        return service;
    }
    
    /**
     * Method to retrieve available locales.
     *
     * @param req (HttpServletRequest Object)
     * @param res (HttpServletResponse Object)
     * @return
     */
    public ModelAndView getLocales(HttpServletRequest req, HttpServletResponse res) throws ManagementConsolePortalException {
        ModelAndView responseView = new ModelAndView(new JSONView());

        try {
            List<Map<String, Object>> localeList = new ArrayList<Map<String, Object>>();
            for (String localeName : demoPortalServices.getLocales(getApiClientService(req, ApiClient.class))) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("localeName", localeName);
                localeList.add(map);
            }
            responseView.addObject("locales", localeList);
        } catch (DemoPortalException e) {
            e.printStackTrace();
            log.error(e.toString());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
            return responseView;
        }
        responseView.addObject("message", "");
        return responseView;
    }
    
    /**
     * Returns locale for specified portal user.
     * 
     * @param req
     * @param res
     * @return
     * @throws ManagementConsolePortalException 
     */
    public ModelAndView getLocale(HttpServletRequest req, HttpServletResponse res) throws ManagementConsolePortalException {
        ModelAndView responseView = new ModelAndView(new JSONView());
        String username = req.getParameter("username");
        
        try {
            responseView.addObject("locale", demoPortalServices.getLocale(username, getApiClientService(req, ApiClient.class)));
        } catch (DemoPortalException e) {
            e.printStackTrace();
            log.error(e.toString());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
        }
        responseView.addObject("message", "");
        return responseView;
    }
    
    /**
     * Sets locale for specified portal user.
     * 
     * @param req
     * @param res
     * @return
     * @throws ManagementConsolePortalException 
     */
    public ModelAndView setLocale(HttpServletRequest req, HttpServletResponse res) throws ManagementConsolePortalException {
        ModelAndView responseView = new ModelAndView(new JSONView());
        String username = req.getParameter("username");
        String locale = req.getParameter("locale");
        log.debug("Calling api to set locale [{}] for user [{}]", locale, username);
        
        try {
            ApiClient apiClient = getApiClientService(req, ApiClient.class);
            if (apiClient == null) {
                throw new IllegalStateException("Failed to initialize the API client object.");
            }
            apiClient.setLocale(LocaleUtil.forLanguageTag(locale));
            HttpSession session = req.getSession();
            session.setAttribute("api-object", apiClient);
            session.setAttribute("apiClientObject",apiClient);            
            responseView.addObject("locale", demoPortalServices.setLocale(username, locale, apiClient));
        } catch (DemoPortalException e) {
            e.printStackTrace();
            log.error(e.toString());
            responseView.addObject("result", false);
            responseView.addObject("message", StringEscapeUtils.escapeHtml(e.getMessage()));
        }
        responseView.addObject("message", "");
        return responseView;
    }

    // Methods to create services layer object, used by other methods while calling into a Service Layer.
    //these method are called by spring container while dependencies injuction.
    public void setOsClientServiceImpl(IOSClientService clientService) {
        this.osClientService = clientService;
    }

    public void setMleClientService(IMLEClientService mleClientService) {
        this.mleClientService = mleClientService;
    }

    public void setOemClientService(IOEMClientService oemClientService) {
        this.oemClientService = oemClientService;
    }
}
