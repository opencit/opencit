/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent.data;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import java.io.File;

import com.intel.mountwilson.common.Config;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.model.TpmQuoteResponse;
import com.intel.mtwilson.util.ResourceFinder;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author dsmagadX
 */
public class TADataContext {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TADataContext.class);

    private ErrorCode errorCode = ErrorCode.OK;
    private String selectedPCRs = null;
    private String nonceBase64;
    private String AIKCertificate = null;
    private byte[] tpmQuote = null;
    private String responseXML = null;
    private byte[] daaChallenge;
    private byte[] daaResponse;
    private String osName;
    private String osVersion;
    private String biosOem;
    private String biosVersion;
    private String vmmName;
    private String vmmVersion;
    private String modulesStr;
    private String processorInfo;
    private String hostUUID;
    private String ipaddress;  // localhost ip address
    private String assetTagHash = null;
    private TpmQuoteResponse tpmQuoteResponse;
    private String sessionId;
    private String tcbMeasurement;
    private String selectedPcrBanks;

    public String getSelectedPcrBanks() {
        return selectedPcrBanks;
    }

    public void setSelectedPcrBanks(String selectedPcrBanks) {
        this.selectedPcrBanks = selectedPcrBanks;
    }
    
    public String getSessionId() {
        if( sessionId == null ) {
            sessionId = RandomUtil.randomHexString(4);
            log.debug("Generated session id {}", sessionId);
        }
        return sessionId;
    }
    
    public String getBiosOem() {
        return biosOem;
    }

    public void setBiosOem(String biosName) {
        this.biosOem = biosName;
    }

    public String getBiosVersion() {
        return biosVersion;
    }

    public void setBiosVersion(String biosVersion) {
        this.biosVersion = biosVersion;
    }

    public String getVmmName() {
        return vmmName;
    }

    public void setVmmName(String vmmName) {
        this.vmmName = vmmName;
    }

    public String getVmmVersion() {
        return vmmVersion;
    }

    public void setVmmVersion(String vmmVersion) {
        this.vmmVersion = vmmVersion;
    }

    public String getNonceFileName() {
        return getDataFolder() + File.separator + "nonce"; // like /opt/trustagent/var/nonce   
    }

    public String getResponseXML() {
        return responseXML;
    }

    public String getSelectedPCRs() {
        return selectedPCRs;
    }

    public void setSelectedPCRs(String selectedPCRs) {
        this.selectedPCRs = selectedPCRs;
    }

    public byte[] getTpmQuote() {
        return tpmQuote;
    }

    public void setTpmQuote(byte[] tpmQuote) {
        this.tpmQuote = tpmQuote; //Arrays.copyOf(tpmQuote, tpmQuote.length);
    }

    public String getNonce() {
        return nonceBase64;
    }

    public void setNonce(String nonce) {
        this.nonceBase64 = nonce;
    }

    // issue #1038 prevent trust agent relay by default; customer can turn this off in configuration file by setting  tpm.quote.ipv4=false
    public boolean isQuoteWithIPAddress() {
        String enabled = Config.getInstance().getProperty("tpm.quote.ipv4");
        if( enabled == null || "true".equalsIgnoreCase(enabled) || "enabled".equalsIgnoreCase(enabled) ) {
            return true;
        }
        return false;
    }

    public String getIPAddress() {
        return ipaddress;
    }

    // set by TrustAgent when it initializes the context for this request
    public void setIPAddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    
    public String getQuoteFileName() {
        return getDataFolder() + File.separator + "aikquote";
    }
    
    public String getPcrsFileName() {
        return getDataFolder() + File.separator + "pcrs";
    }

    public String getAikBlobFileName() {
        return getCertificateFolder() + File.separator + Config.getInstance().getProperty("aikblob.filename");
    }

    public String getAikCertFileName() {
        return getCertificateFolder() + File.separator + Config.getInstance().getProperty("aikcert.filename");
    }

    // used only by the CreateIdentityDaaCmd 
    public String getEKCertFileName() {
        return getCertificateFolder() + File.separator + Config.getInstance().getProperty("ekcert.filename");
    }

    public String getDaaChallengeFileName() {
        return getDataFolder() + File.separator + Config.getInstance().getProperty("daa.challenge.filename");
    }

    public String getDaaResponseFileName() {
        return getDataFolder() + File.separator +  Config.getInstance().getProperty("daa.response.filename");
    }

    public String getCertificateFolder() {
        return Folders.configuration();
    }

    public String getDataFolder() {
        String path = Folders.repository() + File.separator + getSessionId();
        File dir = new File(path);
        if( !dir.exists() ) {
            if( dir.mkdirs() ) {
                log.debug("Created session data folder {}", path);
            }
            else {
                log.error("Error creating data folder {}", path);
            }
        }
        //return Config.getHomeFolder() + File.separator + Config.getInstance().getProperty("data.folder") + File.separator;
        return path;
    }

    public void setAIKCertificate(String certBytes) {
        this.AIKCertificate = certBytes;
    }

    public String getAIKCertificate() {
        return AIKCertificate;
    }

    public void setDaaChallenge(byte[] bytes) {
        daaChallenge = bytes;
    }

    public byte[] getDaaChallenge() {
        return daaChallenge;
    }

    public void setDaaResponse(byte[] bytes) {
        daaResponse = bytes;
    }

    public byte[] getDaaResponse() {
        return daaResponse;
    }

    /**
     * @deprecated use setTpmQuoteResponse
     * @param responseXML 
     */
    public void setResponseXML(String responseXML) {
        this.responseXML = responseXML;
    }
    public void setTpmQuoteResponse(TpmQuoteResponse tpmQuoteResponse) {
        this.tpmQuoteResponse = tpmQuoteResponse;
    }

    public TpmQuoteResponse getTpmQuoteResponse() {
        if(tpmQuoteResponse != null)
            return tpmQuoteResponse;
        else
            return null;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public String getIdentityAuthKey() {
        try {
            TrustagentConfiguration configuration = TrustagentConfiguration.loadConfiguration();
            return configuration.getAikSecretHex();
            /*
            File aikAuthFile = ResourceFinder.getFile("trustagent.properties");
            FileInputStream aikAuthFileInput = new FileInputStream(aikAuthFile);
            Properties tpmOwnerProperties = new Properties();
            tpmOwnerProperties.load(aikAuthFileInput);
            aikAuthFileInput.close();
            return tpmOwnerProperties.getProperty("HisIdentityAuth");
            */
        }
        catch(IOException e) {
            throw new IllegalStateException("Cannot read trustagent.properties", e);
        }
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getModulesFolder() {
    	return Config.getHomeFolder() + File.separator;
    }
    
    public File getMeasureLogLaunchScript() {
//        return Config.getInstance().getProperty("modulesScript.filename");
        return new File(Folders.application() + File.separator + "bin" + File.separator + "module_analysis.sh"); // Config.getInstance().getProperty("modulesScript.filename"));
    }
    
    public File getMeasureLogXmlFile() {
        //return Config.getInstance().getProperty("modulesXml.filename");
        return new File(Folders.repository() + File.separator + "measureLog.xml"); // Config.getInstance().getProperty("modulesXml.filename"));
    }
    
    public File getTcbMeasurementXmlFile() {
        return new File(Folders.log() + File.separator + "measurement.xml");
    }
    
    public String getTcbMeasurement() {
        return tcbMeasurement;
    }
    
    public void setTcbMeasurement(String tcbMeasurement) {
        this.tcbMeasurement = tcbMeasurement;
    }
    
    public void setModules(String allModules) {
        this.modulesStr = allModules;
    }

    public String getModules() {
        return modulesStr;
    } 

    public String getProcessorInfo() {
        return processorInfo;
    }

    public void setProcessorInfo(String processorInfo) {
        this.processorInfo = processorInfo;
    }
        
    public String getHostUUID() {
     return hostUUID;
    }
    public void setHostUUID(String hostUUID) {
        this.hostUUID = hostUUID;
    }
    
    public String getAssetTagHash() {
        return assetTagHash;
    }
    
    public void setAssetTagHash(String assetTagHash) {
        this.assetTagHash = assetTagHash;
    }
}