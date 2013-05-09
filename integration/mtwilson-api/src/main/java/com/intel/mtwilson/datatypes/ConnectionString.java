/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import com.intel.mtwilson.model.Hostname;
import com.intel.mtwilson.model.InternetAddress;
import java.net.MalformedURLException;
import org.apache.commons.lang3.StringUtils;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class ConnectionString {
    private static Logger log = LoggerFactory.getLogger(ConnectionString.class);

    private static final String httpDelimiter = "//";
    private static final String parameterDelimiter = ":";
    private static final String urlDelimiter = ";";
    private static final String intelVendorRegEx = "^(https?://)?([a-zA-Z0-9\\._-])+(:)*([0-9])*$";
    private String addOnConnectionString;
    private Vendor vendor;
    private Hostname hostname;
    private String managementServerName;
    private Integer port;
    private String userName;
    private String password;

    public ConnectionString() {
        this.addOnConnectionString = "";
        this.hostname = null;
        this.port = 0;
        this.managementServerName = "";
        this.userName = "";
        this.password = "";
    }

    /**
     * This constructor can be used if the user has already formated the connection string for the vendor. This option
     * can be used if the user is using http instead of default https.
     *
     * @param vendor
     * @param addOnConnectionString
     */
    public ConnectionString(Vendor vendor, String addOnConnectionString) throws MalformedURLException {
        try {
            this.vendor = vendor;
            // Before we add the connection string, we need to verify the format of the connection string
            // It should be valid URL followed by a ;username and ;password
            String url = "";
            if (addOnConnectionString.indexOf(urlDelimiter) > 0)
                url = addOnConnectionString.substring(0, addOnConnectionString.indexOf(urlDelimiter));
            else 
                url = addOnConnectionString;
            
            // If there is any issue with the URL format other than the username & password, then the following statement would throw
            // an exception.
            URL hostURL = new URL(url);
            
            if (vendor != Vendor.INTEL) {
                // Now we need to verify if we have both the user name and password specified
                String[] userParams = addOnConnectionString.substring(addOnConnectionString.indexOf(urlDelimiter) + urlDelimiter.length()).split(";");
                if (userParams.length != 2)
                    throw new MalformedURLException("Invalid connection string specified for the host. Please verify if all the parameters are provided.");
            }
            
            this.addOnConnectionString = addOnConnectionString;
            
        } catch (MalformedURLException ex) {
            log.error(ex.toString());
            throw ex;
        }
    }

    /**
     * This constructor can be used typically if the vendor is INTEL, which applies for open source Xen & KVM. Do not
     * use this constructor for VMware or Citrix hosts since their connection strings require additional parameters.
     *
     * @param vendor
     * @param hostName
     * @param port
     */
    public ConnectionString(Vendor vendor, String managementServerName, Integer port) {
        this();
        this.vendor = vendor;
        this.managementServerName = managementServerName;
        this.port = port;
    }

    /**
     * This constructor applies to either Citrix or VMware hosts with default port of 443. If you need to specify the
     * port number different than 443, use the constructor which takes port# as another parameter.
     *
     * @param vendor
     * @param managementServerName
     * @param userName
     * @param password
     */
    public ConnectionString(Vendor vendor, String managementServerName, String userName, String password) {
        this();
        this.vendor = vendor;
        this.managementServerName = managementServerName;
        this.port = 443; // Default port
        this.userName = userName;
        this.password = password;
    }

    /**
     * This constructor applies for either Citrix or VMware hosts with specific user defined port.
     *
     * @param vendor
     * @param managementServerName
     * @param port
     * @param userName
     * @param password
     */
    public ConnectionString(Vendor vendor, String managementServerName, Integer port, String userName, String password) {
        this();
        this.vendor = vendor;
        this.managementServerName = managementServerName;
        this.port = port;
        this.userName = userName;
        this.password = password;
    }

    /**
     * This constructor has to be used to extract individual information from the connection string.
     *
     * @param connectionString
     */
    public ConnectionString(String connectionString) throws MalformedURLException {
        this();
        try {
            // Let us first check if the connection string has the prefix of the vendor or not.
            if (connectionString.startsWith("intel") || connectionString.startsWith("citrix") || connectionString.startsWith("vmware")) {
                this.vendor = Vendor.valueOf(connectionString.substring(0, connectionString.indexOf(parameterDelimiter)).toUpperCase());
                if (connectionString.startsWith("intel")) {
                    String url = connectionString.substring(connectionString.indexOf(parameterDelimiter) + parameterDelimiter.length());
                    URL hostURL = new URL(url);
                    this.managementServerName = hostURL.getHost();
                    this.port = hostURL.getPort();
                } else {
                    // in case of Citrix and VMware, we just extract the complete connection string excluding the prefix
                    this.addOnConnectionString = connectionString.substring(connectionString.indexOf(parameterDelimiter) + parameterDelimiter.length());
                }
            } else if (connectionString.startsWith("http")) {
                // In this case we do not have the prefix
                if (connectionString.matches(intelVendorRegEx)) {
                    // The connection string is for a KVM or Xen host.
                    this.vendor = Vendor.INTEL;
                    URL hostURL = new URL(connectionString);
                    this.managementServerName = hostURL.getHost();
                    this.port = hostURL.getPort();
                } else {
                    this.addOnConnectionString = connectionString;
                    if (connectionString.contains("/sdk")) {
                        this.vendor = Vendor.VMWARE;
                    } else {
                        this.vendor = Vendor.CITRIX;
                    }
                }
            }
        } catch (MalformedURLException ex) {
            log.error(ex.toString());
            throw ex;
        }
    }

    /**
     * Returns the formated connection string based on the parameters specified by the user. Example of Citrix:
     * citrix:https://xenserver:port;username;password Example of VMware:
     * vmware:https://vcenterserver:port/sdk;username;password 
     * Example of Xen/KVM: intel:https://hostname:9999
     *
     * @return
     */
    public String getConnectionStringWithPrefix() {
        if( this.vendor == null ) { return ""; } // XXX should we return null to indicate an error? or maybe this shouldn't even be possible to have a ConnectionString object without a vendor?
        return String.format("%s:%s", this.vendor.name().toLowerCase(), getConnectionString());
    }

    /**
     * This method returns the formated connection string based on the parameters specified without the prefix of the
     * vendor. Example of Citrix: citrix:https://xenserver:port;username;password Example of VMware:
     * https://vcenterserver:port/sdk;username;password Example of Xen/KVM: https://hostname:9999
     *
     * @return
     */
    public String getConnectionString() {
        String connectionString = "";

        if (this.vendor == Vendor.INTEL) {
            connectionString = String.format("https://%s:%d", this.managementServerName, this.port);
        } else if (this.vendor == Vendor.VMWARE) {
            connectionString = (this.addOnConnectionString.isEmpty()) ? 
                    String.format("https://%s:%d/sdk;%s;%s", this.managementServerName, this.port, this.userName, this.password) : 
                    String.format("%s", this.addOnConnectionString);
        } else if (this.vendor == Vendor.CITRIX) {
            connectionString = (this.addOnConnectionString.isEmpty()) ? 
                    String.format("https://%s:%d;%s;%s", this.managementServerName, this.port, this.userName, this.password) : 
                    String.format("%s", this.addOnConnectionString);
        } else {
            connectionString = "";
        }
        return connectionString;
    }

    /**
     * This retrieves the addOnConnectionString component of the connection string. Before calling this method
     * ConnectionString(String connectionString) constructor has to be called by passing in the completed connection
     * string.
     *
     * @return
     */
    public String getAddOnConnectionString() {
        return addOnConnectionString;
    }

    
    /**
     * Returns the hostname of the host represented by this connection string.
     * @return 
     */
    public Hostname getHostname() {
        return hostname;
    }
    
    /**
     * This retrieves the hostName component of the connection string. Before calling this method
     * ConnectionString(String connectionString) constructor has to be called by passing in the completed connection
     * string.
     *
     * @return
     */
    public String getManagementServerName() {
        return managementServerName;
    }

    /**
     * This retrieves the port number component of the connection string. Before calling this method
     * ConnectionString(String connectionString) constructor has to be called by passing in the completed connection
     * string.
     *
     * @return
     */
    public Integer getPort() {
        return port;
    }

    /**
     * This would return the host type of the host, which can be either INTEL, VMWARE or CITRIX.
     * @return 
     */
    public Vendor getVendor() {
        return vendor;
    }
  
    public static class IntelConnectionString {
        private InternetAddress hostAddress;
        private int port;
        
        public InternetAddress getHost() { return hostAddress; }
        public int getPort() { return port; }
        public URL toURL() {
            try {
                return new URL(String.format("https://%s:%d", hostAddress.toString(), port));
            }
            catch(MalformedURLException e) {
                log.error("Cannot create VCenter URL: {}", e.toString());
                return null;
            }
        }
        
        @Override
        public String toString() {
            return String.format("https://%s:%d", hostAddress.toString(), port);
        }
        
        public static IntelConnectionString forURL(URL url) {
            IntelConnectionString cs = new IntelConnectionString();
            cs.hostAddress = new InternetAddress(url.getHost());
            cs.port = portFromURL(url);
            return cs;
        }
    }

    public static class CitrixConnectionString {
        private InternetAddress hostAddress;
        private int port;
        private String username;
        private String password;
        public InternetAddress getHost() { return hostAddress; }
        public int getPort() { return port; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public URL toURL() {
            try {
                return new URL(String.format("https://%s:%d", hostAddress.toString(), port));
            }
            catch(MalformedURLException e) {
                log.error("Cannot create VCenter URL: {}", e.toString());
                return null;
            }
        }
        @Override
        public String toString() {
            return String.format("https://%s:%d;u=%s;p=%s", hostAddress.toString(), port, username, password);
        }
        public static CitrixConnectionString forURL(URL url) {
            CitrixConnectionString cs = new CitrixConnectionString();
            cs.hostAddress = new InternetAddress(url.getHost());
            cs.port = portFromURL(url);
            cs.username = usernameFromURL(url);
            cs.password = passwordFromURL(url);
            return cs;
        }
    }

    public static class VmwareConnectionString {
        private InternetAddress hostAddress;
        private InternetAddress vcenterAddress;
        private int port;
        private String username;
        private String password;
        public InternetAddress getVCenter() { return vcenterAddress; }
        public InternetAddress getHost() { return hostAddress; }
        public int getPort() { return port; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public URL toURL() {
            try {
                return new URL(String.format("https://%s:%d/sdk", vcenterAddress.toString(), port));
            }
            catch(MalformedURLException e) {
                log.error("Cannot create VCenter URL: {}", e.toString());
                return null;
            }
        }
        @Override
        public String toString() {
            return String.format("https://%s:%d/sdk;u=%s;p=%s;h=%s", vcenterAddress.toString(), port, username, password, hostAddress.toString());
        }
        public static VmwareConnectionString forURL(URL url) {
            VmwareConnectionString cs = new VmwareConnectionString();
            cs.vcenterAddress = new InternetAddress(url.getHost());
            cs.port = portFromURL(url);
            cs.hostAddress = new InternetAddress(hostnameFromURL(url));
            cs.username = usernameFromURL(url);
            cs.password = passwordFromURL(url);
            return cs;
        }
    }
    
    /**
     * Creates a connection string for an Intel host with default port 9999
     * @param hostname DNS name or IP address
     * @return 
     */
    public static ConnectionString forIntel(String hostname) {
        return forIntel(new Hostname(hostname));
    }
    
    /**
     * Creates a connection string for an Intel host with default port 9999
     * @param hostname DNS name or IP address
     * @return 
     */
    public static ConnectionString forIntel(Hostname hostname) {
        ConnectionString conn = new ConnectionString();
        conn.vendor = Vendor.INTEL;
        conn.managementServerName = hostname.toString();
        conn.port = 9999; // default Intel Trust Agent port
        return conn;
    }
    
    /**
     * Creates a connection string for an Intel host with specified port
     * @param hostname DNS name or IP address
     * @param port to connect to Trust Agent
     * @return 
     */
    public static ConnectionString forIntel(Hostname hostname, Integer port) {
        ConnectionString conn = new ConnectionString();
        conn.vendor = Vendor.INTEL;
        conn.managementServerName = hostname.toString();
        conn.port = port;
        return conn;
    }

    /**
     * Creates a connection string for an Intel host with specified port
     * @param hostname DNS name or IP address
     * @param port to connect to Trust Agent
     * @return 
     */
    public static ConnectionString forIntel(String hostname, Integer port) {
        return forIntel(new Hostname(hostname), port);
    }

    
    /**
     * Creates a connection string for an Intel host with specified port
     * @param url like https://hostname:9999
     * @return 
     */
    public static ConnectionString forIntel(URL url) {
        return forIntel(new Hostname(url.getHost()), portFromURL(url));
    }
    
    /**
     * Creates a connection string for a Citrix host with default port 443
     * @param hostname DNS name or IP address
     * @param username for Citrix host management
     * @param password for Citrix host management
     * @return 
     */
    public static ConnectionString forCitrix(Hostname hostname, String username, String password) {
        ConnectionString conn = new ConnectionString();
        conn.vendor = Vendor.CITRIX;
        conn.hostname = hostname;
        conn.managementServerName = hostname.toString();
        conn.port = 443;
        conn.userName = username;
        conn.password = password;
        return conn;
    }
    
    /**
     * Creates a connection string for a Citrix host with specified default port
     * @param hostname DNS name or IP address
     * @param port to connect to Citrix host management API
     * @param username for Citrix host management
     * @param password for Citrix host management
     * @return 
     */
    public static ConnectionString forCitrix(Hostname hostname, Integer port, String username, String password) {
        ConnectionString conn = new ConnectionString();
        conn.vendor = Vendor.CITRIX;
        conn.hostname = hostname;
        conn.managementServerName = hostname.toString();
        conn.port = port;
        conn.userName = username;
        conn.password = password;
        return conn;
    }
    
    /**
     * Creates a connection string for a Citrix host with specified port
     * @param url like https://hostname:443;username;password or https://hostname:443;u=username;p=password
     * @return 
     */
    public static ConnectionString forCitrix(URL url) {
        return forCitrix(new Hostname(url.getHost()), portFromURL(url), usernameFromURL(url), passwordFromURL(url));
    }
    
    
    
    /**
     * Creates a connection string for a VMware host with default port 443
     * @param hostname DNS name or IP address of the host
     * @param vcenter DNS name or IP address of the vcenter appliance
     * @param username for Vcenter host management
     * @param password for Vcenter host management
     * @return 
     */
    public static ConnectionString forVmware(Hostname hostname, Hostname vcenter, String username, String password) {
        ConnectionString conn = new ConnectionString();
        conn.vendor = Vendor.VMWARE;
        conn.hostname = hostname;
        conn.managementServerName = vcenter.toString();
        conn.port = 443;
        conn.userName = username;
        conn.password = password;
        return conn;
    }

    /**
     * Creates a connection string for a VMware host with specified port
     * @param hostname DNS name or IP address of the host
     * @param vcenter DNS name or IP address of the vcenter appliance
     * @param port to connect to the vcenter API
     * @param username for Vcenter host management
     * @param password for Vcenter host management
     * @return 
     */
    public static ConnectionString forVmware(Hostname hostname, Hostname vcenter, Integer port, String username, String password) {
        ConnectionString conn = new ConnectionString();
        conn.vendor = Vendor.VMWARE;
        conn.hostname = hostname;
        conn.managementServerName = vcenter.toString();
        conn.port = port;
        conn.userName = username;
        conn.password = password;
        return conn;
    }
    
    /**
     * Creates a connection string for a VMware host with specified Vcenter URL
     * @param hostname DNS name or IP address of the host
     * @param vcenter URL for the Vcenter API
     * @param username for Vcenter host management
     * @param password for Vcenter host management
     * @return 
     */
    public static ConnectionString forVmware(Hostname hostname, URL vcenter, String username, String password) {
        ConnectionString conn = new ConnectionString();
        conn.vendor = Vendor.VMWARE;
        conn.hostname = hostname;
        conn.managementServerName = vcenter.toString();
        conn.port = portFromURL(vcenter);
        conn.userName = username;
        conn.password = password;
        return conn;
    }

    /**
     * Creates a connection string for a VMware host with specified port
     * @param url like https://vcenter:443/sdk;username;password;hostname or https://hostname:443;u=username;p=password;h=hostname
     * @return 
     */
    public static ConnectionString forVmware(URL url) {
        return forVmware(new Hostname(url.getHost()), new Hostname(hostnameFromURL(url)), portFromURL(url), usernameFromURL(url), passwordFromURL(url));
    }
    
    /**
     * Creates a connection string with the given vendor and vendor-specific URL
     * @param vendor like INTEL, CITRIX, VMWARE
     * @param url vendor-specific URL like https://citrix:443;username;password or https://vcenter:443/sdk;u=username;p=password;h=hostname or https://hostname:9999
     * @return 
     */
    public static ConnectionString forVendor(Vendor vendor, URL url) {
        if( vendor == null ) { return null; }
        switch(vendor) {
            case INTEL:
                return forIntel(url);
            case CITRIX:
                return forCitrix(url);
            case VMWARE:
                return forVmware(url);
            default:
                log.error("Unknown vendor: "+vendor.name());
                return null;
        }
    }
    
    /**
     * The URL object returns -1 for the port number if it was not explicitly defined in the URL.
     * So this method checks for -1 and returns default https or default http port depending on 
     * the protocol of the URL. If the port is not defined in the URL and the scheme is neither
     * https nor http then the -1 is returned.
     * @param url
     * @return 
     */
    private static int portFromURL(URL url) {
        if( url.getPort() == -1  && url.getProtocol().equals("https") ) {
            return 443;
        }
        else if( url.getPort() == -1  && url.getProtocol().equals("http") ) {
            return 80;
        }
        else {
            return url.getPort();
        }        
    }
    
    /**
     * For supported URLs that include a username and password, the username is either
     * the first parameter after the first semicolon, or a parameter that starts with u=
     * 
     * For example:
     * https://citrix:443;username;password
     * https://citrix:443;u=username;p=password
     * https://citrix:443;p=password;u=username
     * https://vcenter:443/sdk;username;password;hostname
     * https://vcenter:443/sdk;u=username;p=password;u=hostname
     * https://vcenter:443/sdk;u=hostname;u=username;p=password
     * 
     * @param url
     * @return 
     */
    private static String usernameFromURL(URL url) {
        String str = url.toExternalForm();
        if( str.indexOf(';') == -1 ) {
            return null;
        }
        String params = str.substring(str.indexOf(';')+1); // get everything after the first semicolon 
        String[] parts = params.split(";");
        if( parts.length == 1 ) {
            if( parts[0].startsWith("u=") ) {
                return parts[0].substring(2); // value after the "u="  for URL like https://citrix:443;u=username
            }
            return parts[0]; // the only value after semicolon for URL like https://citrix:443;username
        }
        if( parts.length == 2 ) {
            if( parts[0].startsWith("u=") ) {
                return parts[0].substring(2); // value after the "u="  for URL like https://citrix:443;u=username;p=password
            }
            if( parts[1].startsWith("u=") ) {
                return parts[1].substring(2); // value after the "u="  for URL like https://citrix:443;p=password;u=username
            }
            return parts[0]; // the first value after semicolon for URL like https://citrix:443;username;password
        }
        if( parts.length == 3 ) {
            if( parts[0].startsWith("u=") ) {
                return parts[0].substring(2); // value after the "u="  for URL like https://vcenter:443/sdk;u=username;p=password;h=hostname
            }
            if( parts[1].startsWith("u=") ) {
                return parts[1].substring(2); // value after the "u="  for URL like https://vcenter:443/sdk;h=hostname;u=username;p=password or https://vcenter:443/sdk;p=password;u=username;h=hostname
            }
            if( parts[2].startsWith("u=") ) {
                return parts[2].substring(2); // value after the "u="  for URL like https://vcenter:443/sdk;h=hostname;p=password;u=username
            }
            return parts[0]; // the first value after semicolon for URL like https://vcenter:443/sdk;username;password;hostname
        }
        return null;
    }
    
    /**
     * For supported URLs that include a username and password, the password is either
     * the second parameter after the first semicolon, or a parameter that starts with p=
     * 
     * For example:
     * https://citrix:443;username;password
     * https://citrix:443;u=username;p=password
     * https://citrix:443;p=password;u=username
     * https://vcenter:443/sdk;username;password;hostname
     * https://vcenter:443/sdk;u=username;p=password;u=hostname
     * https://vcenter:443/sdk;u=hostname;u=username;p=password
     * 
     * @param url
     * @return 
     */
    private static String passwordFromURL(URL url) {
        String str = url.toExternalForm();
        if( str.indexOf(';') == -1 ) {
            return null;
        }
        String params = str.substring(str.indexOf(';')+1); // get everything after the first semicolon 
        String[] parts = params.split(";");
        if( parts.length == 1 ) {
            return null; // no password in URL like  https://citrix:443;username or https://vcenter:443/sdk;username
        }
        if( parts.length == 2 ) {
            if( parts[0].startsWith("p=") ) {
                return parts[0].substring(2); // value after the "p="  for URL like https://citrix:443;u=username;p=password
            }
            if( parts[1].startsWith("p=") ) {
                return parts[1].substring(2); // value after the "p="  for URL like https://citrix:443;p=password;u=username
            }
            return parts[1]; // the second value after semicolon for URL like https://citrix:443;username;password
        }
        if( parts.length == 3 ) {
            if( parts[0].startsWith("p=") ) {
                return parts[0].substring(2); // value after the "p="  for URL like https://vcenter:443/sdk;p=password;u=username;h=hostname
            }
            if( parts[1].startsWith("p=") ) {
                return parts[1].substring(2); // value after the "p="  for URL like https://vcenter:443/sdk;h=hostname;p=password;u=username or https://vcenter:443/sdk;u=username;p=password;h=hostname
            }
            if( parts[2].startsWith("p=") ) {
                return parts[2].substring(2); // value after the "p="  for URL like https://vcenter:443/sdk;h=hostname;u=username;p=password
            }
            return parts[1]; // the second value after semicolon for URL like https://vcenter:443/sdk;username;password;hostname
        }
        return null;
    }    
    
    /**
     * For vmware URLs, the hostname in the URL is the vcenter hostname, and the "real" hostname
     * is passed as a parameter next to username and password. So this function searches for that
     * parameter and returns it if it was found.
     * 
     * For example:
     * https://vcenter:443/sdk;username;password;hostname
     * https://vcenter:443/sdk;u=username;p=password;u=hostname
     * https://vcenter:443/sdk;u=hostname;u=username;p=password
     * 
     * @param url
     * @return 
     */
    private static String hostnameFromURL(URL url) {
        String str = url.toExternalForm();
        if( str.indexOf(';') == -1 ) {
            return null;
        }
        String params = str.substring(str.indexOf(';')+1); // get everything after the first semicolon 
        String[] parts = params.split(";");
        if( parts.length == 1 ) {
            return null; // no hostname in URL like https://vcenter:443/sdk;username
        }
        if( parts.length == 2 ) {
            if( parts[0].startsWith("h=") ) {
                return parts[0].substring(2); // value after the "p="  for URL like https://vcenter:443/sdk;h=hostname;u=username
            }
            if( parts[1].startsWith("h=") ) {
                return parts[1].substring(2); // value after the "p="  for URL like https://vcenter:443/sdk;u=username;h=hostname
            }
            return null; // no hostname URL like https://vcenter:443/sdk;username;password
        }
        if( parts.length == 3 ) {
            if( parts[0].startsWith("h=") ) {
                return parts[0].substring(2); // value after the "h="  for URL like https://vcenter:443/sdk;h=hostname;u=username;p=password
            }
            if( parts[1].startsWith("h=") ) {
                return parts[1].substring(2); // value after the "h="  for URL like https://vcenter:443/sdk;p=password;h=hostname;u=username or https://vcenter:443/sdk;u=username;h=hostname;p=password
            }
            if( parts[2].startsWith("h=") ) {
                return parts[2].substring(2); // value after the "h="  for URL like https://vcenter:443/sdk;p=password;u=username;h=hostname
            }
            return parts[2]; // the third value after semicolon for URL like https://vcenter:443/sdk;username;password;hostname
        }
        return null;
    }        
}
