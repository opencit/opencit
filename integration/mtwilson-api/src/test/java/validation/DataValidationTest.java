/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package validation;

import com.intel.mtwilson.datatypes.ConnectionString;
//import com.intel.mtwilson.datatypes.RegExAnnotation;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class DataValidationTest {
    
    @Test
    public void testCreateTxtHostVmwareWithEmptyIPAddress() {
        TxtHostRecord hostObj = new TxtHostRecord();
        hostObj.HostName = "10.1.71.146";
        hostObj.Port = 0;
        hostObj.AddOn_Connection_String = "https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        hostObj.BIOS_Name = "EPSD";
        hostObj.BIOS_Oem = "EPSD";
        hostObj.BIOS_Version = "v60";
        hostObj.Description = "Test";
        hostObj.Email = "";
        hostObj.IPAddress = "";
        hostObj.VMM_Name = "ESXi";
        hostObj.VMM_Version ="5.0.0-469512";
        hostObj.VMM_OSName = "VMware_ESXi";
        hostObj.VMM_OSVersion = "5.0.0";
        TxtHost hostAddObj = new TxtHost(hostObj);
        assertTrue(hostAddObj.getHostName() != null);
    }

    @Test
    public void testCreateTxtHostVmwareWithNullIPAddress() {
        TxtHostRecord hostObj = new TxtHostRecord();
        hostObj.HostName = "10.1.71.146";
        hostObj.Port = 0;
        hostObj.AddOn_Connection_String = "https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        hostObj.BIOS_Name = "EPSD";
        hostObj.BIOS_Oem = "EPSD";
        hostObj.BIOS_Version = "v60";
        hostObj.Description = "Test";
        hostObj.Email = "";
        hostObj.IPAddress = null;
        hostObj.VMM_Name = "ESXi";
        hostObj.VMM_Version ="5.0.0-469512";
        hostObj.VMM_OSName = "VMware_ESXi";
        hostObj.VMM_OSVersion = "5.0.0";
        TxtHost hostAddObj = new TxtHost(hostObj);
        assertTrue(hostAddObj.getHostName() != null);
    }

    @Test//(expected=IllegalArgumentException.class)
    public void testCreateTxtHostVmwareWithNonemptyIPAddress() {
        TxtHostRecord hostObj = new TxtHostRecord();
        hostObj.HostName = "10.1.71.146";
        hostObj.Port = 0;
        hostObj.AddOn_Connection_String = "https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        hostObj.BIOS_Name = "EPSD";
        hostObj.BIOS_Oem = "EPSD";
        hostObj.BIOS_Version = "v60";
        hostObj.Description = "Test";
        hostObj.Email = "";
        hostObj.IPAddress = "1.2.3.4";
        hostObj.VMM_Name = "ESXi";
        hostObj.VMM_Version ="5.0.0-469512";
        hostObj.VMM_OSName = "VMware_ESXi";
        hostObj.VMM_OSVersion = "5.0.0";
        TxtHost hostAddObj = new TxtHost(hostObj);
        assertTrue(hostAddObj.getHostName() != null);
    }
    
    @Test
    public void testSplit() {
        String text = "KVM|";
        String[] parts = text.split("[|]");
        System.out.println(parts.length);
        
        String text2 = "KVM";
        String[] parts2 = text2.split("[|]");
        System.out.println(parts2.length);
    }

    private static final HashMap<String,Pattern> patternMap = new HashMap<String,Pattern>();
    
    private static Pattern getPattern(String regex) {
        Pattern pattern = patternMap.get(regex);
        if( pattern == null ) {
            pattern = Pattern.compile("^" + regex + "$");
            patternMap.put(regex, pattern);
        }
        return pattern;
    }
    
    private static void validateInput(String input, Pattern pattern) {
             
            if (input != null && !input.isEmpty()) {
                System.err.println("validating " + input + " against " + pattern.pattern());
                Matcher matcher = pattern.matcher(input);
                if (!matcher.matches()) {
                    System.err.println("Illegal characters found in : " + input);
                    throw new IllegalArgumentException();
                }
            } else {
                System.err.println("Skipping validating " + input + " against " + pattern.pattern());
            }
    }
    
    @Test
    public void testRegex() {
        //validateInput("Administrator", getPattern(RegExAnnotation.PASSWORD));
    }
}
