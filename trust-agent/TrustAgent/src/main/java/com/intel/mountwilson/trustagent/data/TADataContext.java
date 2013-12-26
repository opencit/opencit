/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent.data;

import java.io.File;

import com.intel.mountwilson.common.Config;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.HisConfig;
import com.intel.mtwilson.util.ResourceFinder;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author dsmagadX
 */
public class TADataContext {

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
    private String assetTagHash;
    private String nvRamPassword;
    
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
        return getDataFolder() + Config.getInstance().getProperty("nonce.filename");
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

    // issue #1038 prevent trust agent relay by default; customer can turn this off in configuration file by setting  mtwilson.tpm.quote.ipv4=false
    public boolean isQuoteWithIPAddress() {
        String enabled = Config.getInstance().getProperty("mtwilson.tpm.quote.ipv4");
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
        return getDataFolder() + Config.getInstance().getProperty("aikquote.filename");
    }

    public String getAikBlobFileName() {
        return getCertificateFolder() + Config.getInstance().getProperty("aikblob.filename");
    }

    public String getAikCertFileName() {
        return getCertificateFolder() + Config.getInstance().getProperty("aikcert.filename");
    }

    public String getEKCertFileName() {
        return getCertificateFolder() + Config.getInstance().getProperty("ekcert.filename");
    }

    public String getDaaChallengeFileName() {
        return getCertificateFolder() + Config.getInstance().getProperty("daa.challenge.filename");
    }

    public String getDaaResponseFileName() {
        return getCertificateFolder() + Config.getInstance().getProperty("daa.response.filename");
    }

    public String getCertificateFolder() {
        return Config.getHomeFolder() + File.separator + Config.getInstance().getProperty("cert.folder") + File.separator;
    }

    public String getDataFolder() {
        return Config.getHomeFolder() + File.separator + Config.getInstance().getProperty("data.folder") + File.separator;
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

    public void setResponseXML(String responseXML) {
        this.responseXML = responseXML;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public String getIdentityAuthKey() {
        try {
            File aikAuthFile = ResourceFinder.getFile("trustagent.properties");
            FileInputStream aikAuthFileInput = new FileInputStream(aikAuthFile);
            Properties tpmOwnerProperties = new Properties();
            tpmOwnerProperties.load(aikAuthFileInput);
            aikAuthFileInput.close();
            return tpmOwnerProperties.getProperty("HisIdentityAuth");
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
    
    public String getMeasureLogLaunchScript() {
        return Config.getInstance().getProperty("modulesScript.filename");
    } 
    
    public String getMeasureLogXmlFile() {
        return Config.getInstance().getProperty("modulesXml.filename");
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
    
    public String getNvRamPassword(){
        return this.nvRamPassword;
    }
    
    public void setNvRamPasword(String nvRamPassword){
        this.nvRamPassword = nvRamPassword;
    }
}
