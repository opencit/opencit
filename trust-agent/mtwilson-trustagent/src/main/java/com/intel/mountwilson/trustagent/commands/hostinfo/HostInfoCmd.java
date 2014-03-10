/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent.commands.hostinfo;

import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author dsmagadx
 */
public class HostInfoCmd implements ICommand {

    Logger log = LoggerFactory.getLogger(getClass().getName());
    TADataContext context = null;

    public HostInfoCmd(TADataContext context) {
        this.context = context;
    }

    @Override
    public void execute() throws TAException {
        try {

            getOsAndVersion();
            getBiosAndVersion();
            if(context.getOsName() != null &&  context.getOsName().toLowerCase().contains("xenserver")){
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
        } catch (Exception ex) {
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
        List<String> result = CommandUtil.runCommand("lsb_release -a");

        for (String str : result) {
            String[] parts = str.split(":");

            if (parts != null && parts.length > 1) {
                if (parts[0].trim().equalsIgnoreCase("Distributor ID")) {
                    if (parts[1] != null) {
                        context.setOsName(parts[1].trim());
                    }
                } else if (parts[0].trim().equalsIgnoreCase("Release")) {
                    if (parts[1] != null) {
                        context.setOsVersion(parts[1].trim());
                    }

                }
            }
        }
        log.debug("OS Name: " + context.getOsName());
        log.debug("OS Version: " + context.getOsVersion());

    }

    /*
     * Sample response of dmidecode -s bios-vendor -> Intel Corp. Sample
     * response of dmidecode -s bios-vendor -> S5500.86B.01.00.0060.090920111354
     */
    private void getBiosAndVersion() throws TAException, IOException {

        List<String> result = CommandUtil.runCommand("dmidecode -s bios-vendor");
        if (result != null && result.size() > 0) {
            context.setBiosOem(result.get(0));
        }
        log.debug("Bios OEM: " + context.getBiosOem());


        result = CommandUtil.runCommand("dmidecode -s bios-version");

        if (result != null && result.size() > 0) {
            context.setBiosVersion(result.get(0));
        }
        log.debug("Bios Version: " + context.getBiosVersion());


    }
    /*
     * Sample response of "virsh version" command: 
     * root@mwdevubuk02h:~# virsh version 
     * Compiled against library: libvir 0.9.2 
     * Using library: libvir 0.9.2 
     * Using API: QEMU 0.9.2 
     * Running hypervisor: QEMU 0.14.1
     */

    private void getVmmAndVersion() throws TAException, IOException {

        List<String> result = CommandUtil.runCommand("virsh version");

        for (String str : result) {
            String[] parts = str.split(":");

            if (parts != null && parts.length > 1) {
                if (parts[0].trim().equalsIgnoreCase("Running hypervisor")) {
                    if (parts[1] != null) {
                        String[] subParts = parts[1].trim().split(" ");
                        if (subParts[0] != null) {
                            context.setVmmName(subParts[0]);
                        }
                        if (subParts[1] != null) {
                            context.setVmmVersion(subParts[1]);
                        }
                    }
                }
            }
            log.debug("VMM Name: " + context.getVmmName());
            log.debug("VMM Version: " + context.getVmmVersion());

        }
    }

    /**
     * Retrieves the CPU ID of the processor. This is used to identify the processor generation.
     * 
     * @throws TAException
     * @throws IOException 
     */
       private void getProcessorInfo() throws TAException, IOException {
           
            List<String> result = CommandUtil.runCommand("dmidecode --type processor");
            String processorInfo = "";
            
            // Sample output would look like below for a 2 CPU system. We will extract the processor info between CPU and the @ sign
            //Processor Information
            //Socket Designation: CPU1
            //Type: Central Processor
            //Family: Xeon
            //Manufacturer: Intel(R) Corporation
            //ID: C2 06 02 00 FF FB EB BF -- This is the CPU ID
            //Signature: Type 0, Family 6, Model 44, Stepping 2
            
            for (String entry : result) {
                if (entry != null && !entry.isEmpty() && entry.trim().startsWith("ID:")) {                    
                    String[] parts = entry.trim().split(":");
                     if (parts != null && parts.length > 1) {
                        processorInfo = parts[1];
                        break;
                     }
                }            
            }
            
            log.debug("Processor Information " + processorInfo);
            context.setProcessorInfo(processorInfo);
            log.debug("Context is being set with processor info: " + context.getProcessorInfo());
    }
    
    /**
     * Retrieves the host UUId information
     * @throws TAexception
     * @throws IOException
     */
      public void getHostUUID() throws TAException, IOException {
          List<String> result = CommandUtil.runCommand("dmidecode -s system-uuid");
          String hostUUID = "";
          // sample output would look like UUID: 4235D571-8542-FFD3-5BFE-6D9DAC874C84
          for(String entry: result) {
              hostUUID = entry;
          }
          
          context.setHostUUID(hostUUID);
          log.info("Context set with host UUID info: " + context.getHostUUID());
          
      }
}
