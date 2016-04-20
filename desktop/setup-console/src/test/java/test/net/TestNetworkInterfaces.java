/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author jbuhacoff
 */
public class TestNetworkInterfaces {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Sample output:
Network interface:    lo Software Loopback Interface 1
        /127.0.0.1 (loopback)
        /0:0:0:0:0:0:0:1 (loopback)
Network interface:    eth0 WAN Miniport (IPv6)
Network interface:    eth2 WAN Miniport (IP)
Network interface:    ppp1 RAS Async Adapter
Network interface:    net4 Intel(R) Centrino(R) Advanced-N 6205
        /10.19.162.171
Network interface:    eth5 Bluetooth Device (Personal Area Network)
     * 
     * @throws SocketException 
     */
    @Test
    public void testListNICs() throws SocketException {
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        if( en == null ) {
            System.err.println("Network interface not found");
            return;
        }
        while( en.hasMoreElements() ) {
          NetworkInterface intf = en.nextElement();
          System.out.println("Network interface:    " + intf.getName() + " " + intf.getDisplayName());
          for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
              InetAddress addr = enumIpAddr.nextElement();
            System.out.println("       ["+addr.getHostAddress()+"] " + addr.getCanonicalHostName()+" "+(addr.isLoopbackAddress()?" (loopback)":""));
          }
        }
    }
    
    /**
     * Sample output:
 IP Addr: 10.19.162.171
 Full list of IP addresses:
    JBUHACOF-MOBL.amr.corp.intel.com/10.19.162.171
    JBUHACOF-MOBL.amr.corp.intel.com/0:0:0:0:0:0:0:1 (loopback)
     * 
     * @throws UnknownHostException 
     */
    @Test
    public void testListIPs() throws UnknownHostException {
        InetAddress localhost = InetAddress.getLocalHost();
        System.out.println(" IP Addr: " + localhost.getHostAddress());
        // Just in case this host has multiple IP addresses....
        InetAddress[] allMyIps = InetAddress.getAllByName(localhost.getCanonicalHostName());
        if (allMyIps != null && allMyIps.length > 1) {
          System.out.println(" Full list of IP addresses:");
          for (int i = 0; i < allMyIps.length; i++) {
            InetAddress addr = allMyIps[i];
            System.out.println("    " + addr.toString()+(addr.isLoopbackAddress()?" (loopback)":""));
          }
        }        
    }
    
}
