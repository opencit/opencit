/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent.commands.hostinfo;

import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hxia5
 */
public class HostInfoCmdWin implements ICommand {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostInfoCmdWin.class);

    TADataContext context = null;

    public HostInfoCmdWin(TADataContext context) {
        this.context = context;
    }

    @Override
    public void execute() throws TAException {
        try {

            getOsAndVersion();
            getBiosAndVersion();
            if(context.getOsName() != null &&  context.getOsName().toLowerCase().contains("Windows")){
                context.setVmmName(context.getOsName());
                context.setVmmVersion(context.getOsVersion());
                log.debug("VMM Name: " + context.getVmmName());
                log.debug("VMM Version: " + context.getVmmVersion());

            }else{
                getVmmAndVersion();
            
            }
            // Retrieve the processor information as well.
            getProcessorInfo();
            getHostUUID();
        } catch (TAException | IOException ex) {
            log.debug("Error while getting OS details", ex);
            throw new TAException(ErrorCode.ERROR, "Error while getting OS details.", ex);
        }

    }

    /*
    Sample response of "lsb_release -a" 
    No LSB modules are available.
    Distributor ID: Ubuntu
    Description:    Ubuntu 11.10
    Release:        11.10
    Codename:       oneiric
     */
    
    private void getOsAndVersion() throws TAException, IOException {
        String osName = System.getProperty("os.name");
        String osVersion = "";
        try {
            osVersion = jWMI.getWMIValue("Select version from Win32_OperatingSystem", "version");
            if (osVersion.equals("")) {
                log.error("Error executing jWMI.getWMIvalue to retrieve the OS details");
            }
            context.setOsName(osName);
            context.setOsVersion(osVersion);
            log.debug("OS Name: " + context.getOsName());
            log.debug("OS Version: " + context.getOsVersion());
        } catch (Exception ex) {
            Logger.getLogger(HostInfoCmdWin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String trim(String text) {
        if( text == null ) { return null; }
        return text.trim();
    }

    /*
     * Sample response of dmidecode -s bios-vendor -> Intel Corp. Sample
     * response of dmidecode -s bios-vendor -> S5500.86B.01.00.0060.090920111354
     */
    private void getBiosAndVersion() throws TAException, IOException {
        try {
            String biosManufacturer = jWMI.getWMIValue("Select manufacturer from Win32_BIOS", "manufacturer");
            context.setBiosOem(biosManufacturer);
            log.debug("Bios OEM: " + context.getBiosOem());
            
            String biosVersion = jWMI.getWMIValue("Select SMBIOSBIOSVersion from Win32_BIOS", "SMBIOSBIOSVersion");
            context.setBiosVersion(biosVersion);
            log.debug("Bios Version: " + context.getBiosVersion());
        } catch (Exception ex) {
            Logger.getLogger(HostInfoCmdWin.class.getName()).log(Level.SEVERE, "TA Error Getting Bios and version", ex);
        }
    }
    /*
     * FIXIT: need to find correct tool on Windows to identify hyper-V version
     */

    private void getVmmAndVersion() throws TAException, IOException {
        context.setVmmName("Windows Hyper-V");
        context.setVmmVersion("1.0");
        log.debug("VMM Name: " + context.getVmmName());
        log.debug("VMM Version: " + context.getVmmVersion());
    }

    /**
     * Retrieves the CPU ID of the processor. This is used to identify the processor generation.
     * 
     * @throws TAException
     * @throws IOException 
     */
    private void getProcessorInfo() throws TAException, IOException {
        try {
            String processorInfo = "";
            processorInfo = jWMI.getWMIValue("Select ProcessorId from Win32_Processor", "ProcessorId");
            
            log.debug("Processor Information: " + processorInfo);
            context.setProcessorInfo(processorInfo);
            log.debug("Context is being set with processor info: " + context.getProcessorInfo());
        } catch (Exception ex) {
            Logger.getLogger(HostInfoCmdWin.class.getName()).log(Level.SEVERE, "Error retrieving the processor information", ex);
        }
            
    }
    
    /**
     * Retrieves the host UUId information
     * @throws TAexception
     * @throws IOException
     */
    public void getHostUUID() throws TAException, IOException {
        
        try {
            String uuidInfo = "";
            uuidInfo = jWMI.getWMIValue("Select UUID from Win32_ComputerSystemProduct", "UUID");
            log.debug("UUID info: " + uuidInfo);
            context.setHostUUID(uuidInfo);
            log.debug("Context is being set with UUID: " + context.getHostUUID());
        } catch (Exception ex) {
            Logger.getLogger(HostInfoCmdWin.class.getName()).log(Level.SEVERE, "Error retrieving the UUID information", ex);
        }

    }
}
