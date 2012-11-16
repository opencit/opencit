package com.intel.mtwilson.datatypes;

import org.apache.commons.lang3.Validate;

/**
 * Representation of a Trust Agent service port comprised of either
 * an IP Address and Port or of an arbitrary Connection String.
 * 
 * @since 0.5.1
 * @author jbuhacoff
 */
public class ServicePort {
    private IPAddress address = null;
    private Integer port = null;
    private String connectionString = null;
    
    public ServicePort(IPAddress address, Integer port) {
        setAddress(address);
        setPort(port);
    }

    public ServicePort(String connectionString) {
        setConnectionString(connectionString);
    }

    public final void setAddress(IPAddress value) {
        Validate.notNull(value);
        address = value;
    }

    public final void setPort(Integer value) {
        Validate.notNull(value);
        port = value;
    }

    public final void setConnectionString(String value) {
        Validate.notNull(value);
        connectionString = value;
    }

    public IPAddress getAddress() { return address; }
    public Integer getPort() { return port; }
    public String getConnectionString() { return connectionString; }
    
    public String toString() {
        if( address != null && port != null ) {
            return String.format("%s:%s", address, port);
        }
        else {
            return connectionString;
        }
    }
    
    
}
