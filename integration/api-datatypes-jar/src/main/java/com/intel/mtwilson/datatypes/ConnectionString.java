/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import java.net.MalformedURLException;
import org.apache.commons.lang3.StringUtils;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ssbangal
 */
public class ConnectionString {

    private static final String httpDelimiter = "//";
    private static final String parameterDelimiter = ":";
    private static final String urlDelimiter = ";";
    private static final String intelVendorRegEx = "^(https?://)?([a-zA-Z0-9\\._-])+(:)*([0-9])*$";
    private String addOnConnectionString;
    private Vendor vendor;
    private String managementServerName;
    private Integer port;
    private String userName;
    private String password;

    public ConnectionString() {
        this.addOnConnectionString = "";
        this.managementServerName = "";
        this.port = 0;
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
            Logger.getLogger(ConnectionString.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ConnectionString.class.getName()).log(Level.SEVERE, null, ex);
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
        String connectionString = "";

        if (this.vendor == Vendor.INTEL) {
            connectionString = String.format("intel:https://%s:%d", this.managementServerName, this.port);
        } else if (this.vendor == Vendor.VMWARE) {
            connectionString = (this.addOnConnectionString.isEmpty()) ? 
                    String.format("vmware:https://%s:%d/sdk;%s;%s", this.managementServerName, this.port, this.userName, this.password) : 
                    String.format("vmware:%s", this.addOnConnectionString);
        } else if (this.vendor == Vendor.CITRIX) {
            connectionString = (this.addOnConnectionString.isEmpty()) ? 
                    String.format("citrix:https://%s:%d;%s;%s", this.managementServerName, this.port, this.userName, this.password) : 
                    String.format("citrix:%s", this.addOnConnectionString);
        } else {
            connectionString = "";
        }
        return connectionString;
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
  
}
