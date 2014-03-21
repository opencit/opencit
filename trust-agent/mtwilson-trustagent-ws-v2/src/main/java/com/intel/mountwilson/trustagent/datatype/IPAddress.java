package com.intel.mountwilson.trustagent.datatype;

import java.net.URI;

/**
 * Same as the IPAddress class in AttestationService package
 * com.intel.mountwilson.as.datatype.IPAddress, but with
 * the JSON annotations disabled and without the parse() method.
 * @author jbuhacoff
 */
public class IPAddress {

    private String address = null;

    public IPAddress() {
    }

    public IPAddress(String address) {
        setAddress(address);
    }


    public String getAddress() {
        return address;
    }

    public final void setAddress(String address) {
        if (isValid(address)) {
            this.address = address;
        } else {
            throw new IllegalArgumentException("Invalid IPAddress: " + address);
        }
    }

    /**
     * Returns the address so that you can easily concatenate to a string.
     * Example: assert new IPAddress("1.2.3.4").toString().equals("1.2.3.4");
     *
     * @see java.lang.Object#toString()
     */
//    @JsonValue
    @Override
    public String toString() {
        return address;
    }

    /**
     * This method does NOT check the network for the existence of the given
     * address, it only checks its format for validity and, if an IPv4 or IPv6
     * address is given, checks that it is within the allowed range.
     *
     * @param address to check for validity, such as 1.2.3.4
     * @return true if the address appears to be a valid IPv4 or IPv6 address,
     * false if the address is null or otherwise invalid
     */
    public static boolean isValid(String address) {
        try {
            /*
             * because URI format for host is hostname ; but problem is that
             * ipv4 is valid and [ipv6] is valid but [ipv4] is not valid and
             * ipv6 is not valid so we need to know in advance which it is or it
             * won't validate properly.. .which defeats the purpose of this
             * check... so we look for ":" to distinguish ipv4 from ipv6
             */
            if( address.contains(":") ) {
                // IPv6 format
                URI valid = new URI(String.format("//[%s]", address));
                return valid.getHost() != null;
            }
            else {
                // IPv4 format
                URI valid = new URI(String.format("//%s", address));
                // also make sure that there are only digits and dots
                // because URI also accepts valid hostnames, which are not addresses
                return valid.getHost() != null && address.matches("[\\d\\.]+");
            }
        } catch (Throwable e) {
            return false; 
            // happens when IP address is invalid format like
            // 1b.2.3i.4
        } 
    }
}
