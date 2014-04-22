/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.setup.SetupContext;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Enumeration;
import org.apache.commons.configuration.Configuration;
/**
 *
 * @author jbuhacoff
 */
public class SetMtWilsonURL implements Command {

   
    private Configuration options = null;
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    
    @Override
    public void execute(String[] args) throws Exception {
        List<String> list = getLocalAddresses();
        for(int i=0; i<list.size(); i++) {
            System.out.println(String.format("[%2d] %s", i+1, list.get(i)));
        }
    }
    
    public List<String> getLocalAddresses() throws SocketException {
        ArrayList<String> list = new ArrayList<String>();
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        if( en == null ) {
            return list;
        }
        HashSet<String> set = new HashSet<String>();
        while( en.hasMoreElements() ) {
            NetworkInterface intf = en.nextElement();
            Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
            while( enumIpAddr.hasMoreElements() ) {
                InetAddress addr = enumIpAddr.nextElement();
                // skip IPv6 addresses
                if( addr.getAddress().length == 4 ) {
                    set.add(addr.getHostAddress());
                    set.add(addr.getHostName());
                }
            }
        }
        list.addAll(set);
        return list;
    }
    
    
}
