package com.intel.mtwilson.datatypes;

import java.net.MalformedURLException;

import com.intel.mtwilson.model.Vmm;
import com.intel.mtwilson.model.Bios;
import com.intel.mtwilson.model.Hostname;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonCreator;
//import org.codehaus.jackson.annotate.JsonProperty;
//import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author dsmagadx
 */
public class TxtHost {
    private Hostname hostname;
//    private ServicePort servicePort;
    private String ipAddress;
    private Integer port;
    private String connectionString;
    private Bios bios;
    private Vmm vmm;
    private String description;
    private String email;
    private String location;
    private HostTrustStatus trustStatus;
    private String aikCertificate;  // may be null
    private String aikPublicKey;  // may be null
    private String aikSha1;  // may be null

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
        //ipAddress = (host.HostName == null || host.IPAddress.isEmpty()) ? null : host.IPAddress;
        ipAddress = hostname.toString();
        port = host.Port;
        connectionString = host.AddOn_Connection_String;
        description = host.Description;
        email = host.Email;
        location = host.Location;
        trustStatus = new HostTrustStatus(); //defaults to all false
        aikCertificate = host.AIK_Certificate; // may be null
        aikPublicKey = host.AIK_PublicKey; // may be null
        aikSha1 = host.AIK_SHA1; // may be null

        // BUG #497  now all hosts require a connection string,  but the UI's are not updated yet so we allow not having one here and detect it in  HostAgentFactory
//        if (connectionString == null || connectionString.isEmpty()) {
//            throw new IllegalArgumentException(String.format("Connection string for host or its vCenter (for ESX hosts) is required: %s", hostname));
//        }
        /*
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
        }*/
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

    public String getIPAddress() {
        return ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    /**
     * XXX TODO  the guessing of connection string has now moved to ConnectionString.from()
     * @return
     * @throws MalformedURLException 
     */
    public String getAddOn_Connection_String() throws MalformedURLException {
        ConnectionString connStr = null;
        if( connectionString == null) {
            if (ipAddress != null && port != null) {
                // for backwards compatibility with cilents that don't submit a connection string for intel hosts
                connStr = ConnectionString.forIntel(ipAddress.toString(), port);
                // return "intel:https://"+ipAddress.toString()+":"+port.toString(); // XXX or mabye just throw an IllegalArgumentException , this may not be the right place to kludge this.
            }            
        } else {
            // Let us first check if the user already has specified the connection string in the correct format. If yes, then return back the connection
            // string since we do not need to do any formatting.
            if (connectionString.startsWith("intel") || connectionString.startsWith("vmware")  || connectionString.startsWith("citrix")) {
                return connectionString;
            } else if (connectionString.startsWith("http")) {
                // the connection string can be for any of the 3 types of hosts. Check if we have the userName and password fields. If they are
                // present, then the connection string is for either VMware or Citrix. If not, it is Intel
                if (connectionString.contains(";") && (connectionString.substring(connectionString.indexOf(";")).length()>0)) {
                    if (connectionString.contains("/sdk")) {
                        connStr = new ConnectionString(Vendor.VMWARE, connectionString);
                    } else {
                        connStr = new ConnectionString(Vendor.CITRIX, connectionString);
                    }
                } else {
                    connStr = new ConnectionString(Vendor.INTEL, connectionString);
                }           
            }
        }
        // Now return back the properly formatted connection string.
        return connStr.getConnectionStringWithPrefix();
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

    public String getAikPublicKey() {
        return aikPublicKey;
    }

    public String getAikSha1() {
        return aikSha1;
    }

    final public boolean requiresConnectionString() {
        // BUG #497  now every host requies a connection string 
        return true; /*
        if (vmm.getName().toUpperCase().contains("ESX")) {
            return true;
        }
        return false;*/
    }
    
    final public boolean isBiosTrusted() { return trustStatus.bios; }
    final public boolean isVmmTrusted() { return trustStatus.vmm; }
    final public boolean isLocationTrusted() { return trustStatus.location; }
    final public boolean isAssetTagTrusted() { return trustStatus.asset_tag; }
}
