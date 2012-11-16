package com.intel.mtwilson.datatypes;

public enum HostWhiteListTarget {
	
	BIOS_OEM("OEM"),
	BIOS_HOST("Host"),
	VMM_GLOBAL("Global"),
	VMM_OEM("OEM"),
        VMM_HOST("Host");
        
	
	private String value;
	
	private HostWhiteListTarget(String value){
		this.setValue(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
