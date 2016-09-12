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
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.exec.CommandLine;

/**
 *
 * @author hxia5
 */
public class HostInfoCmdWin implements ICommand {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostInfoCmdWin.class);
    private static boolean vmmEnabled = false;
    TADataContext context = null;

    public HostInfoCmdWin(TADataContext context) {
        this.context = context;
    }

    @Override
    public void execute() throws TAException {
        try {

            getOsName();
            getOsVersion();
            getBiosAndVersion();
            //getVmmAndVersion();
            getVmm();
            if (vmmEnabled) {
                getVmmVersion();
            } else {
                context.setVmmVersion("NA");
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
    
    private void getOsVersion() throws TAException, IOException {
        /* String osVersion = "";
        try {
            osVersion = jWMI.getWMIValue("Select version from Win32_OperatingSystem", "version");
            if (osVersion.equals("")) {
                log.error("Error executing jWMI.getWMIvalue to retrieve the OS details");
            }
            context.setOsVersion(osVersion);
            log.debug("OS Version: " + context.getOsVersion());
        } catch (Exception ex) {
            Logger.getLogger(HostInfoCmdWin.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
        
        CommandLine command = new CommandLine("wmic");
        command.addArgument("os");
        command.addArgument("get");
        command.addArgument("version", false);
        Result result = ExecUtil.execute(command);
        if (result.getExitCode() != 0) {
            log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
            throw new TAException(ErrorCode.ERROR, result.getStderr());
        }
        log.debug("command stdout: {}", result.getStdout());
        if (result.getStdout() != null) {
            String[] resultArray = result.getStdout().split("\n");
            if (resultArray.length >1) {
                context.setOsVersion(resultArray[1]);
                log.debug("OS version: " + context.getOsVersion());
            } else {
                log.error("[wmic os get version] does not return OS full name");
            }
        } else {
            log.error("Error executing the [wmic os get version] to retrieve the OS details");
        }
    }
    
    private void getOsName() throws TAException, IOException {
        
        CommandLine command = new CommandLine("wmic");
        command.addArgument("os");
        command.addArgument("get");
        command.addArgument("caption", false);
        Result result = ExecUtil.execute(command);
        if (result.getExitCode() != 0) {
            log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
            throw new TAException(ErrorCode.ERROR, result.getStderr());
        }
        log.debug("command stdout: {}", result.getStdout());
        if (result.getStdout() != null) {
            String[] resultArray = result.getStdout().split("\n");
            if (resultArray.length >1) {
                context.setOsName(resultArray[1]);
                log.debug("OS full Name: " + context.getOsName());
            } else {
                log.error("[wmic os get caption] does not return OS full name");
            }
        } else {
            log.error("Error executing the [wmic os get caption] to retrieve the OS details");
        }
    }
    
    private void getVmm() throws TAException, IOException {
        vmmEnabled = false;
        CommandLine command = new CommandLine("wmic");
        command.addArgument("path");
        command.addArgument("WIN32_ServerFeature");
        command.addArgument("get");
        command.addArgument("ID", false);
        Result result = ExecUtil.execute(command);
        if (result.getExitCode() != 0) {
            log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
            throw new TAException(ErrorCode.ERROR, result.getStderr());
        }
        log.debug("command stdout: {}", result.getStdout());
        context.setVmmName("Microsoft Windows VMM NA");
        if (result.getStdout() != null) {
            String[] resultArray = result.getStdout().split("\n");
            String vmmID = "" + 20;
            for (String str : resultArray) {
                str = str.replaceAll("\\s+", ""); //remove all whitespace
                if (str.equals(vmmID)) {
                    log.debug("Setting Hyper-V");
                    context.setVmmName("Microsoft Windows Hyper-V");
                    vmmEnabled=true;
                    break;
                }
            }            
        } else {
            log.error("Error executing the [wmic path WIN32_ServerFeature get ID] to retrieve the VMM details");
        }
    }
    
    /*
    private void getVmmVersion() throws TAException, IOException {     
        CommandLine command = new CommandLine("wmic");
        command.addArgument("datafile");
        command.addArgument("where");
        command.addArgument("name=c:\\\\windows\\\\system32\\\\vmms.exe\"");
        command.addArgument("get");
        command.addArgument("version", false);
        
        Result result = ExecUtil.execute(command);
        if (result.getExitCode() != 0) {
            log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
            throw new TAException(ErrorCode.ERROR, result.getStderr());
        }
        log.debug("command stdout: {}", result.getStdout());
        if (result.getStdout() != null) {
            String[] resultArray = result.getStdout().split("\n");
            if (resultArray.length >1) {
                context.setVmmVersion(resultArray[1]);
                log.debug("VMM version: " + context.getVmmVersion());
            } else {
                context.setVmmVersion("NA");
                log.error("GetVmmVersion does not return OS full name");
            }
        } else {
            context.setVmmVersion("NA");
            log.error("Error executing getVmmVersion to retrieve the OS details");
        }
    }
    */
    
    private void getVmmVersion() throws TAException, IOException {

        String getVerCMD = Folders.application() + File.separator + "bin" + File.separator + "getvmmver.cmd";              
        CommandLine command = new CommandLine("cmd.exe");
        command.addArgument("/c");
        command.addArgument(getVerCMD, false);

        Result result = ExecUtil.execute(command);
        if (result.getExitCode() != 0) {
            log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
            throw new TAException(ErrorCode.ERROR, result.getStderr());
        }
        log.debug("command stdout: {}", result.getStdout());
        if (result.getStdout() != null) {
            String[] resultArray = result.getStdout().split("\n");
            if (resultArray.length >= 1) {
                context.setVmmVersion(resultArray[0]);
                log.debug("VMM version: " + context.getVmmVersion());
            } else {
                context.setVmmVersion("NA");
                log.error("GetVmmVersion does not return OS full name");
            }
        } else {
            context.setVmmVersion("NA");
            log.error("Error executing getVmmVersion to retrieve the OS details");
        }
    }
        
    //#5841: Private method 'trim' is unused.
    //private String trim(String text) {
    //    if( text == null ) { return null; }
    //    return text.trim();
    //}

    /*
     * Sample response of dmidecode -s bios-vendor -> Intel Corp. Sample
     * response of dmidecode -s bios-vendor -> S5500.86B.01.00.0060.090920111354
     */
    private void getBiosAndVersion() throws TAException, IOException {
        /*
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
        */
        getBiosOem();
        getBiosVersion();
    }
    
    private void getBiosVersion() throws TAException, IOException {
        CommandLine command = new CommandLine("wmic");
        command.addArgument("bios");
        command.addArgument("get");
        command.addArgument("smbiosbiosversion", false);
        Result result = ExecUtil.execute(command);
        if (result.getExitCode() != 0) {
            log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
            throw new TAException(ErrorCode.ERROR, result.getStderr());
        }
        log.debug("command stdout: {}", result.getStdout());
        if (result.getStdout() != null) {
            String[] resultArray = result.getStdout().split("\n");
            if (resultArray.length >1) {
                context.setBiosVersion(resultArray[1]);
                log.debug("Bios Version: " + context.getBiosVersion());
            } else {
                log.error("[wmic bios get smbiosbiosversion] does not return Bios Version");
            }
        } else {
            log.error("Error executing the [wmic bios get smbiosbiosversion]");
        }   
    }
    
    private void getBiosOem() throws TAException, IOException {
        CommandLine command = new CommandLine("wmic");
        command.addArgument("bios");
        command.addArgument("get");
        command.addArgument("manufacturer", false);
        Result result = ExecUtil.execute(command);
        if (result.getExitCode() != 0) {
            log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
            throw new TAException(ErrorCode.ERROR, result.getStderr());
        }
        log.debug("command stdout: {}", result.getStdout());
        if (result.getStdout() != null) {
            String[] resultArray = result.getStdout().split("\n");
            if (resultArray.length >1) {
                context.setBiosOem(resultArray[1]);
                log.debug("OS full Name: " + context.getBiosOem());
            } else {
                log.error("[wmic bios get manufacturer]");
            }
        } else {
            log.error("Error executing the [wmic bios get manufacturer]");
        }   
    }
    //#5817: Private method 'getVmmAndVersion' is unused.
    //private void getVmmAndVersion() throws TAException, IOException {
    //    context.setVmmName("Windows Hyper-V");
    //    context.setVmmVersion("1.0");
    //    log.debug("VMM Name: " + context.getVmmName());
    //    log.debug("VMM Version: " + context.getVmmVersion());
    //}

    /**
     * Retrieves the CPU ID of the processor. This is used to identify the processor generation.
     * 
     * @throws TAException
     * @throws IOException 
     */
    private void getProcessorInfo() throws TAException, IOException {
        /*
        try {
            String processorInfo = "";
            processorInfo = jWMI.getWMIValue("Select ProcessorId from Win32_Processor", "ProcessorId");
            
            log.debug("Processor Information: " + processorInfo);
            context.setProcessorInfo(processorInfo);
            log.debug("Context is being set with processor info: " + context.getProcessorInfo());
        } catch (Exception ex) {
            Logger.getLogger(HostInfoCmdWin.class.getName()).log(Level.SEVERE, "Error retrieving the processor information", ex);
        }
        */
        CommandLine command = new CommandLine("wmic");
        command.addArgument("cpu");
        command.addArgument("get");
        command.addArgument("ProcessorId", false);
        Result result = ExecUtil.execute(command);
        if (result.getExitCode() != 0) {
            log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
            throw new TAException(ErrorCode.ERROR, result.getStderr());
        }
        log.debug("command stdout: {}", result.getStdout());
        if (result.getStdout() != null) {
            String[] resultArray = result.getStdout().split("\n");
            if (resultArray.length >1) {
                context.setProcessorInfo(resultArray[1]);
                log.debug("OS full Name: " + context.getProcessorInfo());
            } else {
                log.error("[wmic os get ProcessorId] does not return ProcessorId");
            }
        } else {
            log.error("Error executing the [wmic os get ProcessorId]");
        }        
            
    }
    
    /**
     * Retrieves the host UUId information
     * @throws TAexception
     * @throws IOException
     */
    public void getHostUUID() throws TAException, IOException {
        /*
        try {
            String uuidInfo = "";
            uuidInfo = jWMI.getWMIValue("Select UUID from Win32_ComputerSystemProduct", "UUID");
            log.debug("UUID info: " + uuidInfo);
            context.setHostUUID(uuidInfo);
            log.debug("Context is being set with UUID: " + context.getHostUUID());
        } catch (Exception ex) {
            Logger.getLogger(HostInfoCmdWin.class.getName()).log(Level.SEVERE, "Error retrieving the UUID information", ex);
        }
        */
        CommandLine command = new CommandLine("wmic");
        command.addArgument("path");
        command.addArgument("Win32_ComputerSystemProduct");
        command.addArgument("get");
        command.addArgument("uuid", false);
        Result result = ExecUtil.execute(command);
        if (result.getExitCode() != 0) {
            log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
            throw new TAException(ErrorCode.ERROR, result.getStderr());
        }
        log.debug("command stdout: {}", result.getStdout());
        if (result.getStdout() != null) {
            String[] resultArray = result.getStdout().split("\n");
            if (resultArray.length >1) {
                context.setHostUUID(resultArray[1]);
                log.debug("Host UUID: " + context.getHostUUID());
            } else {
                log.error("[wmic path Win32_ComputerSystemProduct] does not return uuid");
            }
        } else {
            log.error("Error executing the [wmic path Win32_ComputerSystemProduct]");
        }
    }
}
