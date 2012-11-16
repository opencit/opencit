/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.x500;

import java.util.HashMap;
import java.util.Map;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is only for convenience and is not a complete implementation.
 * It would have been convenient to use X500Name directly but it was causing an
 * exception when parsing the DN on some platforms. The LdapName class
 * is not convenient to use, so this class was written as a simple wrapper.
 * 
 * See also sun.security.x509.X500Name and javax.naming.ldap.LdapName
 * 
 * @author jbuhacoff
 */
public class DN {
    private Logger log = LoggerFactory.getLogger(getClass());
    private String dn;
    private Map<String,String> map = new HashMap<String,String>();
    
    public DN(String dn) {
        this.dn = dn;
        parseLdapName(dn);
    }
    
    private void parseLdapName(String distinguishedName) {
        try {
            LdapName dn = new LdapName(distinguishedName);
            for(int i=0; i<dn.size(); i++) {
                Rdn rdn = dn.getRdn(i);
                map.put(rdn.getType(), rdn.getValue().toString());
            }
        }
        catch(InvalidNameException e) {
            log.error("Cannot extract Common Name from Distinguished Name", e);
        }
    }
    
    /**
     * 
     * @return the original string version of the distinguished name
     */
    @Override
    public String toString() { return dn; }
    
    /**
     * Retrieve any Relative Distinguished Name
     * @param rdn name such as "CN", "OU", "L", etc.
     * @return 
     */
    public String get(String rdn) { return map.get(rdn); }
    
    public String getCommonName() { return map.get("CN"); }
    
    public String getOrg() { return map.get("O"); }
    
    public String getOrgUnit() { return map.get("OU"); }
    
    public String getLocality() { return map.get("L"); }
    
    public String getState() { return map.get("S"); }
    
    public String getCountry() { return map.get("C"); }
    
}
