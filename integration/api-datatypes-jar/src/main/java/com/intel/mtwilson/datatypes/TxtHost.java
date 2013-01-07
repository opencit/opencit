package com.intel.mtwilson.datatypes;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author dsmagadx
 */
public class TxtHost {
    private Hostname hostname;
//    private ServicePort servicePort;
    private IPAddress ipAddress;
    private Integer port;
    private String connectionString;
    private Bios bios;
    private Vmm vmm;
    private String description;
    private String email;
    private String location;
    private HostTrustStatus trustStatus;
    private String aikCertificate;  // may be null

    public TxtHost(TxtHostRecord host, HostTrustStatus trustStatus) {
        this(host);
        this.trustStatus = new HostTrustStatus(trustStatus); // make our own copy
    }
    
    /**
     * To create a new TxtHost instance, you call the constructor with
     * a TxtHostRecord structure. 
     * @param host 
     */
    @JsonCreator
    public TxtHost(TxtHostRecord host /*
            @JsonProperty("HostName")  String HostName,
            @JsonProperty("IPAddress")  String IPAddress,
            @JsonProperty("Port")  Integer Port,
            @JsonProperty("BIOS_Name")  String BIOS_Name,
            @JsonProperty("BIOS_Version")  String BIOS_Version,
            @JsonProperty("VMM_Name")  String VMM_Name,
            @JsonProperty("VMM_Version")  String VMM_Version,
            @JsonProperty("AddOn_Connection_String")  String AddOn_Connection_String,
            @JsonProperty("Description")  String Description,
            @JsonProperty("Email")  String Email */) {
        hostname = new Hostname(host.HostName);
        bios = new Bios(host.BIOS_Name, host.BIOS_Version, host.BIOS_Oem);
        vmm = new Vmm(host.VMM_Name, host.VMM_Version, host.VMM_OSName, host.VMM_OSVersion);
        ipAddress = (host.IPAddress == null || host.IPAddress.isEmpty()) ? null : new IPAddress(host.IPAddress);
        port = host.Port;
        connectionString = host.AddOn_Connection_String;
        description = host.Description;
        email = host.Email;
        location = host.Location;
        trustStatus = new HostTrustStatus(); //defaults to all false
        aikCertificate = host.AIK_Certificate; // may be null

        if (requiresConnectionString()) {
            if (connectionString == null || connectionString.isEmpty()) {
                throw new IllegalArgumentException(String.format("AddOn connection string for connecting to vCenter server for host: %s", hostname));
            }
            if(port != null && port != 0  ){
                throw new IllegalArgumentException(String.format("Port should be blank for VMWare Host : %s", hostname));
            }
        } else { // requires IP Address and Port
            if (ipAddress == null  || port == null ) {
                throw new IllegalArgumentException("Missing IP address or port");
            }
            if(connectionString != null && !connectionString.isEmpty() ){
            	throw new IllegalArgumentException(String.format("Addon connection string should be blank for Host : %s", hostname));
            }
        }
    }

    // Sample JSON output (not used)
    // {"hostName":"RHEL 62 KVM","port":9999,"description":"RHEL 62 KVM Integration ENV","addOn_Connection_String":"http://example.server.com:234/vcenter/","bios":{"name":"EPSD","version":"60"},"vmm":{"name":"ESX","version":"0.4.1"},"ipaddress":"10.1.71.103","email":null}
    public Hostname getHostName() {
        return hostname;
    }

    public Bios getBios() {
        return bios;
    }

    public Vmm getVmm() {
        return vmm;
    }

    public IPAddress getIPAddress() {
        return ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public String getAddOn_Connection_String() {
        if( connectionString == null && ipAddress != null && port != null ) {
            // for backwards compatibility with cilents that don't submit a connection string for intel hosts
            return "intel:https://"+ipAddress.toString()+":"+port.toString(); // XXX or mabye just throw an IllegalArgumentException , this may not be the right place to kludge this.
        }
        return connectionString;
    }

    public String getDescription() {
        return description;
    }

    public String getEmail() {
        return email;
    }

    public String getLocation() {
        return location;
    }
    
    public String getAikCertificate() {
        return aikCertificate;
    }

    final public boolean requiresConnectionString() {
        if (vmm.getName().toUpperCase().contains("ESX")) {
            return true;
        }
        return false;
    }
    
    final public boolean isBiosTrusted() { return trustStatus.bios; }
    final public boolean isVmmTrusted() { return trustStatus.vmm; }
    final public boolean isLocationTrusted() { return trustStatus.location; }
    
}
