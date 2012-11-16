/**
 * This Class contains all Constant variables used in other class while data manipulation.
 */
package com.intel.mountwilson.constant;

/**
 * @author yuvrajsx
 *
 */
public interface HelperConstant {
	
	String IMAGE_TRUSTED_TRUE = "trustTure";
	String IMAGE_TRUSTED_FALSE = "trustFalse";
	String IMAGE_TRUSTED_UNKNOWN = "trustUnknow";
	
	String OS_IMAGE_VMWARE = "vmware";
	
	/*String OS_IMAGE_UBUNTU = "ubuntu";
	String OS_IMAGE_SUSE = "suse";
	String OS_IMAGE_RHEL = "rhel";
	
	String HYPER_IMAGE_KVM = "kvm";
	String HYPER_IMAGE_XEN = "xen";*/
	String SEPARATOR_VMCLIENT = "::";
	String VM_POWER_STATE_ON = "POWERED_ON";
        
	String Trusted_BIOS = "Trusted_BIOS";
	String Trusted_VMM = "Trusted_VMM";
	String OVER_ALL_TRUSTRED = "Trusted";
	
	//Path for Images
	String IMAGES_ROOT_PATH = "imagesRootPath";
	
	// Separator used while displaying vmmInfo in Edit, View and Add host page.
	String OS_VMM_INFORMATION_SEPERATOR = "|";
	
	//Separator used in key while storing HostVmMappingVO Object into a map for VM and Host mapping information. 
	String VM_HOST_MAPPING_SEPERATOR = "-";
	
}
