/**
 * 
 */
package com.intel.mountwilson.constant;

/**
 * @author yuvrajsx
 *
 */
public interface HelperConstant {
	
	// String CONFIG_FILE_PATH = "resources/managementconsole";
	String VMWARE_TYPE = "VMWare";
	String SEPARATOR_REGISTER_HOST = "|";
	String HINT_FOR_VCENTERSTRING = "http";
	String ALREADY_REGISTER = "Registered";
	String VMM_NAME_SEPARATOR_TXTHOSTRECORD = "_";
	
	
	String IMAGE_TRUSTED_TRUE = "trustTrue";
	String IMAGE_TRUSTED_FALSE = "trustFalse";
	String IMAGE_TRUSTED_UNKNOWN = "trustUnknown";
	
	String OS_IMAGE_VMWARE = "vmware";
	String SEPARATOR_VMCLIENT = "::";
	String VM_POWER_STATE_ON = "POWERED_ON";
        
	String Trusted_BIOS = "Trusted_BIOS";
	String Trusted_VMM = "Trusted_VMM";
	String OVER_ALL_TRUSTRED = "Trusted";
        String ASSET_TAG = "Asset_Tag";
	
	//Path for Images
	String IMAGES_ROOT_PATH = "imagesRootPath";
	
	// Separator used while displaying vmmInfo in Edit, View and Add host page.
	String OS_VMM_INFORMATION_SEPERATOR = "|";
	
	//Separator used in key while storing HostVmMappingVO Object into a map for VM and Host mapping information. 
	String VM_HOST_MAPPING_SEPERATOR = "-";
	    
}
