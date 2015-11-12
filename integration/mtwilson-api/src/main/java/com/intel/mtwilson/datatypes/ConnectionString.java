/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import com.intel.mtwilson.model.Hostname;
import com.intel.mtwilson.model.InternetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The general connection string format is   vendor:url;options
 * The vendor-specific URL can be anything that does not include a semicolon, but is expected to be an https URL.
 * If it must include semicolons they should be percent-encoded as %3B   (see https://en.wikipedia.org/wiki/Percent-encoding)
 * 
 * @author ssbangal
 */
public class ConnectionString {
    private static Logger log = LoggerFactory.getLogger(ConnectionString.class);

    private static final String httpDelimiter = "//";
    private static final String parameterDelimiter = ":";
    private static final String urlOptionsDelimiter = ";";
    private static final String intelVendorRegEx = "^(https?://)?([a-zA-Z0-9\\._-])+(:)*([0-9])*$";
    private static final String intelVendorRegEx2 = "^(https?://)?([a-zA-Z0-9\\._-])+(:)*([0-9])*(;)*(.)*(;)*(.)*$";
    private String addOnConnectionString;
    private Vendor vendor;
    private InternetAddress hostname;
    private String managementServerName;
    private Integer port;
    private String userName;
    private String password;
    
    public static final String OPT_USERNAME = "u";
    public static final String OPT_PASSWORD = "p";
    public static final String OPT_HOSTNAME = "h";
//    public static final String LONGOPT_USERNAME = "username";
//    public static final String LONGOPT_PASSWORD = "password";
//    public static final String LONGOPT_HOSTNAME = "hostname";

    public static class VendorConnection {
        public Vendor vendor;
        public URL url;
        public Configuration options;
    }
    
    public static VendorConnection parseConnectionString(String connectionString) throws MalformedURLException {
//        log.debug("Connection string: {}", connectionString);  // do not log this regularly because it may contain a password
        VendorConnection vc = new VendorConnection();
        vc.vendor = vendorFromURL(connectionString);
        String vendorURL;
        if( vc.vendor == null ) {
            vc.vendor = guessVendorFromURL(connectionString);
            vendorURL = connectionString;
        }
        else {
            vendorURL = connectionString.substring(vc.vendor.name().length()+1);
        }
        if( vc.vendor != null ) {
//            log.debug("Vendor URL: {}", vendorURL); // do not log this regularly because it may contain a password
            int optionStartIndex = vendorURL.indexOf(urlOptionsDelimiter);
            if( optionStartIndex > -1 ) {
                String urlPart = vendorURL.substring(0, optionStartIndex);
                String optionsPart = vendorURL.substring(optionStartIndex+1); // skip the delimiter
                log.debug("URL part: {}", urlPart);
//                log.debug("Options part: {}", optionsPart);  // do not log this regularly because it may contain a password
                vc.url = new URL(urlPart); // vendorURL without the options
                vc.options = parseOptions(optionsPart);
            }
            else {
                vc.url = new URL(vendorURL);
                vc.options = null;
            }
        }   
        return vc;
    }
    
    private static Configuration parseOptions(String options) {
        while(options.startsWith(urlOptionsDelimiter)) {
            options = options.substring(urlOptionsDelimiter.length());
        }
        Properties p = new Properties();
        String[] keyValuePairs = options.split(urlOptionsDelimiter);
        for(String keyValuePair : keyValuePairs) {
            String[] keyValue = keyValuePair.trim().split("=");
            if( keyValue.length == 2 && !keyValue[0].isEmpty() ) {
                p.setProperty(keyValue[0], keyValue[1]); 
            }
            // ignore any options that are not in key=value format
        }
        // backwards compatible options:   username;password  or username;password;hostname
        if( !p.containsKey(OPT_USERNAME) ) {
            String username = usernameFromURL(urlOptionsDelimiter+options);
            if( username != null ) {
                p.setProperty(OPT_USERNAME, username);
            }
        }
        if( !p.containsKey(OPT_PASSWORD) ) {
            String password = passwordFromURL(urlOptionsDelimiter+options);
            if( password != null ) {
                p.setProperty(OPT_PASSWORD, password);
            }
        }
        if( !p.containsKey(OPT_HOSTNAME) ) {
            String hostname = hostnameFromURL(urlOptionsDelimiter+options);
            if( hostname != null ) {
                p.setProperty(OPT_HOSTNAME, hostname);
            }
        }
        return new MapConfiguration(p);
    }
    
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
        this(vendor.name()+":"+addOnConnectionString);
    }

    /**
     * URL must be something like https://intelhost.com:9999  or https://citrixhost.com:443  or https://vcenter.com:443/sdk
     * @param url
     * @return 
     */
    private static Vendor guessVendorFromURL(String url) {
        Vendor v;
        if (url.contains("/sdk")) {
               v = Vendor.VMWARE;
           } else {
               v = Vendor.INTEL;
           }
        /*
        // In this case we do not have the prefix
        if (url.matches(intelVendorRegEx2)) {
            v = Vendor.INTEL;
        } else {
            if (url.contains("/sdk")) {
                v = Vendor.VMWARE;
            } else {
                v = Vendor.CITRIX;
            }
        }*/
        return v;
    }


    /**
     * This constructor has to be used to extract individual information from the connection string.
     * 
     * For cases where the input connection string may be provided without a prefix, SEE ALSO  ConnectionString.from(TxtHostRecord)
     * where you can provide additional information to help guess the right connection string.
     *
     * @param connectionString
     */
    public ConnectionString(String connectionString) throws MalformedURLException {
        this();
            vendor = vendorFromURL(connectionString);
            // Let us first check if the connection string has the prefix of the vendor or not.
            if( vendor == null ) {
                vendor = guessVendorFromURL(connectionString);
            }
            else { // if( vendor != null )
                connectionString = vendorConnectionFromURL(connectionString);
                if( connectionString == null ) { throw new IllegalArgumentException("Invalid or missing connection string"); }
            }
            addOnConnectionString = connectionString;
            switch(vendor) {
                case INTEL:
                    IntelConnectionString intelConnection = IntelConnectionString.forURL(connectionString);
                    hostname = intelConnection.getHost();
                    port = intelConnection.getPort();
                    managementServerName = hostname.toString();
                    userName = intelConnection.username;
                    password = intelConnection.password;
                    break;
                case CITRIX:
                    // Need to add the vendor explicitly since Intel and Citrix hosts have the exact same connection string.
                    CitrixConnectionString citrixConnection = CitrixConnectionString.forURL(vendor.CITRIX+":"+connectionString);
                    hostname = citrixConnection.getHost();
                    port = citrixConnection.getPort();
                    managementServerName = hostname.toString();
                    userName = citrixConnection.getUsername();
                    password = citrixConnection.getPassword();
                    break;
                case VMWARE:
                    VmwareConnectionString vmwareConnection = VmwareConnectionString.forURL(connectionString);
                    hostname = vmwareConnection.getHost();
                    port = vmwareConnection.getPort();
                    managementServerName = vmwareConnection.getVCenter().toString();
                    userName = vmwareConnection.getUsername();
                    password = vmwareConnection.getPassword();
                    break;
                default:
                    throw new UnsupportedOperationException("Vendor not supported yet: "+vendor.toString());
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
        if( this.vendor == null ) { return ""; } 
        String connStr = getConnectionString();
        if( connStr.toLowerCase().startsWith(this.vendor.name().toLowerCase())) {
            return connStr;
        }
        return String.format("%s:%s", this.vendor.name().toLowerCase(), connStr);
    }

    /**
     * This method returns the formated connection string based on the parameters specified without the prefix of the
     * vendor. Example of Citrix: https://xenserver:port;username;password Example of VMware:
     * https://vcenterserver:port/sdk;username;password Example of Xen/KVM: https://hostname:9999
     *
     * @return
     */
    public String getConnectionString() {
        String connectionString;

        if (this.vendor == Vendor.INTEL) {
            connectionString = (this.addOnConnectionString.isEmpty()) ? 
                    String.format("https://%s:%d/;%s;%s", this.managementServerName, this.port, this.userName, this.password) : 
                    String.format("%s", this.addOnConnectionString);
        } else if (this.vendor == Vendor.VMWARE) {
            connectionString = (this.addOnConnectionString.isEmpty()) ? 
                    String.format("https://%s:%d/sdk;%s;%s;h=%s", this.managementServerName, this.port, this.userName, this.password, this.hostname.toString()) : 
                    String.format("%s", this.addOnConnectionString);
        } else if (this.vendor == Vendor.CITRIX) {
            connectionString = (this.addOnConnectionString.isEmpty()) ? 
                    String.format("https://%s:%d/;%s;%s", this.managementServerName, this.port, this.userName, this.password) : 
                    String.format("%s", this.addOnConnectionString);
        } else {
            connectionString = "";
        }
        return connectionString;
    }
    
    /**
     * Returns just the URL portion of the connection string without any options.
     * This is important because for citrix,  the format %s:%d;%s;%s isn't a valid URL. there must be a slash
     * after the port number.
     * 
     * But this is the method that should be used when you want to display a connection string in the UI or logs -  since
     * this method will not leak any secrets.
     * 
     * @return 
     */
    public URL getURL() {
        try {
            if (this.vendor == Vendor.INTEL) {
                if( this.addOnConnectionString.isEmpty() ) {
                    return new URL(String.format("https://%s:%d", this.managementServerName, this.port));
                } else {
                    return new URL(this.addOnConnectionString);
                }
            } 
            else if (this.vendor == Vendor.VMWARE) {
                if( this.addOnConnectionString.isEmpty() ) {
                    return new URL(String.format("https://%s:%d/sdk", this.managementServerName, this.port));
                }
                else {
                    return new URL(this.addOnConnectionString);
                }
            } 
            else if (this.vendor == Vendor.CITRIX) {
                if( this.addOnConnectionString.isEmpty() ) {
                    return new URL(String.format("https://%s:%d", this.managementServerName, this.port));
                }
                else {
                    return new URL(this.addOnConnectionString);
                }
            } else {
                return null;
            }
        }
        catch(MalformedURLException e) {
            log.error("ConnectionString.getURL: "+e.toString(), e);
//            log.debug("Connection string: ", this.addOnConnectionString);
            throw new IllegalArgumentException("Invalid connection string");
        }
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
        return new Hostname(hostname.toString());
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
                log.error("Cannot create Intel Host URL: {}", e.toString());
                return null;
            }
        }
        
        @Override
        public String toString() {
            return String.format("https://%s:%d/;u=%s;p=%s", hostAddress.toString(), port, username, password);
        }
        
        public static IntelConnectionString forURL(String url) throws MalformedURLException {
            IntelConnectionString cs = new IntelConnectionString();
            VendorConnection info = parseConnectionString(url);
            if( info.url == null ) { throw new IllegalArgumentException("Missing host address in URL"); }            
            if( info.vendor !=  Vendor.INTEL ) {
                throw new IllegalArgumentException("Not an Intel Host URL: "+info.url.toExternalForm());
            }
            cs.hostAddress = new InternetAddress(info.url.getHost());
            cs.port = portFromURL(info.url);
            if( info.options != null ) {
                cs.username = info.options.getString(OPT_USERNAME); // usernameFromURL(url);
                cs.password = info.options.getString(OPT_PASSWORD); // passwordFromURL(url);
            }
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
                log.error("Cannot create Citrix Host URL: {}", e.toString());
                return null;
            }
        }
        @Override
        public String toString() {
            return String.format("https://%s:%d/;u=%s;p=%s", hostAddress.toString(), port, username, password);
        }
        public static CitrixConnectionString forURL(String url) throws MalformedURLException {
//            log.debug("CitrixConnectionString forURL {}", url); //REMOVED debug log statement as it has the password in the connection string
            CitrixConnectionString cs = new CitrixConnectionString();
            VendorConnection info = parseConnectionString(url);
            if( info.url == null ) { throw new IllegalArgumentException("Missing host address in URL"); }
            if( info.vendor !=  Vendor.CITRIX ) {
                throw new IllegalArgumentException("Not a Citrix Host URL: "+info.url.toExternalForm());
            }
            cs.hostAddress = new InternetAddress(info.url.getHost());
            cs.port = portFromURL(info.url);
            if( info.options != null ) {
                cs.username = info.options.getString(OPT_USERNAME); // usernameFromURL(url);
                cs.password = info.options.getString(OPT_PASSWORD); // passwordFromURL(url);
            }
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
        
        public void setVCenter(InternetAddress vcenterAddress) { this.vcenterAddress = vcenterAddress; }
        public void setHost(InternetAddress hostAddress) { this.hostAddress = hostAddress; }
        public void setPort(int port) { this.port = port; }
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
        
        public URL toURL() {
            try {
                return new URL(String.format("https://%s:%d/sdk", vcenterAddress.toString(), port));
            }
            catch(MalformedURLException e) {
                log.error("Cannot create VMware Host URL: {}", e.toString());
                return null;
            }
        }
        @Override
        public String toString() {
            return String.format("https://%s:%d/sdk;u=%s;p=%s;h=%s", vcenterAddress.toString(), port, username, password, hostAddress.toString());
        }
        public static VmwareConnectionString forURL(String url) throws MalformedURLException {
//            log.debug("VmwareConnectionString forURL {}", url);
            VmwareConnectionString cs = new VmwareConnectionString();
            VendorConnection info = parseConnectionString(url);
            if( info.url == null ) { throw new IllegalArgumentException("Missing host address in URL"); }
            if( info.vendor !=  Vendor.VMWARE ) {
                throw new IllegalArgumentException("Not a VMware Host URL: "+info.url.toExternalForm());
            }
            cs.vcenterAddress = new InternetAddress(info.url.getHost());
            cs.port = portFromURL(info.url);
            if( info.options != null ) {
                cs.hostAddress = new InternetAddress(info.options.getString(OPT_HOSTNAME)); // new InternetAddress(hostnameFromURL(url));
                cs.username = info.options.getString(OPT_USERNAME); // usernameFromURL(url);
                cs.password = info.options.getString(OPT_PASSWORD); // passwordFromURL(url);
            }
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
        conn.hostname = new InternetAddress(hostname.toString());
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
        conn.hostname = new InternetAddress(hostname.toString());
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

    public static ConnectionString forIntel(String hostname, Integer port, String username, String password) {
        ConnectionString conn = new ConnectionString();
        conn.vendor = Vendor.INTEL;
        conn.hostname = new InternetAddress(hostname);
        conn.managementServerName = hostname;
        conn.port = port;
        conn.userName = username;
        conn.password = password;
        return conn;
    }
    
    /**
     * Creates a connection string for an Intel host with specified port
     * @param url like https://hostname:9999
     * @return 
     */
    /*
    public static ConnectionString forIntel(URL url) {
        return forIntel(new Hostname(url.getHost()), portFromURL(url));
    }
    */
    
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
        conn.hostname = new InternetAddress(hostname.toString());
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
        conn.hostname = new InternetAddress(hostname.toString());
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
    /*
    public static ConnectionString forCitrix(URL url) {
        return forCitrix(new Hostname(url.getHost()), portFromURL(url), usernameFromURL(url), passwordFromURL(url));
    }
    */
    
    
    
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
        conn.hostname = new InternetAddress(hostname.toString());
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
        conn.hostname = new InternetAddress(hostname.toString());
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
        conn.hostname = new InternetAddress(hostname.toString());
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
    /*
    public static ConnectionString forVmware(URL url) {
        return forVmware(new Hostname(url.getHost()), new Hostname(hostnameFromURL(url)), portFromURL(url), usernameFromURL(url), passwordFromURL(url));
    }
    */
    
    /**
     * Creates a connection string with the given vendor and vendor-specific URL
     * @param vendor like INTEL, CITRIX, VMWARE
     * @param url vendor-specific URL like https://citrix:443;username;password or https://vcenter:443/sdk;u=username;p=password;h=hostname or https://hostname:9999
     * @return 
     */
    /*
    public static ConnectionString forVendor(Vendor vendor, String url) {
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
    */
    
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
    private static String usernameFromURL(String url) {
        String str = url;
        if( str.indexOf(';') == -1 ) {
            return null;
        }
        String params = str.substring(str.indexOf(';')+1); // get everything after the first semicolon 
        String[] parts = params.split(";");
        if( parts.length == 1 ) {
            if( parts[0].toLowerCase().startsWith("u=") ) {
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
    private static String passwordFromURL(String url) {
        String str = url;
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
    private static String hostnameFromURL(String url) {
        String str = url;
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
    
    /*
    private static Vendor vendorFromURL(URL url) {
        return vendorFromURL(url.toExternalForm());
    }
    */
    
    private static Vendor vendorFromURL(String url) {
        for( Vendor v : Vendor.values() ) {
            if( url.toLowerCase().startsWith(v.name().toLowerCase()+":") ) {
                return v;
            }
        }
        return null;
    }
    
//    private static String vendorConnectionFromURL(URL url) throws MalformedURLException {
//        return vendorConnectionFromURL(url.toExternalForm());
//    }

    private static String vendorConnectionFromURL(String url) throws MalformedURLException {
//        log.debug("url: {}", url);  // do not log this regularly because it may contain a password
        Vendor v = vendorFromURL(url);
        if( v == null ) {
            return null;
        }
//        log.debug("vendor name: {}", v.name());
//        log.debug("vendor name length: {}", v.name().length());
        String str = url.substring(v.name().length()+1); // start one character after the vendor prefix (vendor name followed by the colon)
//        log.debug("vendor connection: {}", str);
        return str;
    }
    
    
    public static ConnectionString from(TxtHostRecord host) throws MalformedURLException {
        String connectionString = host.AddOn_Connection_String;
        if( connectionString == null || connectionString.isEmpty() ) {
            if( host.HostName != null && !host.HostName.isEmpty() && host.Port != null ) {
                connectionString = String.format("intel:https://%s:%d", host.HostName, host.Port);
                log.debug("Assuming Intel connection string " + connectionString + " for host: " + host.HostName + " with IP address: "+host.HostName +" and port: "+host.Port);
                return new ConnectionString(connectionString);
            }
            else if(host.IPAddress != null && !host.IPAddress.isEmpty() && host.Port != null ) {
                connectionString = String.format("intel:https://%s:%d", host.IPAddress, host.Port);
                log.debug("Assuming Intel connection string " + connectionString + " for host: " + host.HostName +" with IP address: "+host.IPAddress);
                return new ConnectionString(connectionString);
            }
            else if(host.IPAddress != null && !host.IPAddress.isEmpty() ) {
                connectionString = String.format("intel:https://%s:%d", host.IPAddress, 9999); // NOTE:  empty port is assumed to be a mtwilson 1.x trust agent for backward compatibility;
                log.debug("Assuming Intel connection string " + connectionString + " for host: " + host.HostName +" with IP address: "+host.IPAddress);
                return new ConnectionString(connectionString);
            }
            throw new IllegalArgumentException("Host does not have a connection string or hostname set");
        }
        else if (connectionString.startsWith("intel") ) {
            return new ConnectionString(connectionString);
        }
        else if ( connectionString.startsWith("vmware")  || connectionString.startsWith("citrix") ) {
            // ensure the hostname itself is present as a parameter on the connection string, since we have that information in the TxtHostRecord object 
            ConnectionString cs = new ConnectionString(connectionString);
            cs.hostname = new InternetAddress(host.HostName);
            return cs;
        }
        else {
            // use a combination of connection string and other information in the record (port number, vmm name, ...)
            // to make a better guess than is possible with just the connection string.
            // for example:
            //if( host.Port != null && host.Port == 9999 ) {
            //    ConnectionString.forIntel(host.HostName);
            //}
            return new ConnectionString(connectionString);
        }
    }


}
