/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent;

/**
 *
 * @author dsmagadX
 */
import com.intel.mountwilson.common.CommandUtil;
import java.io.IOException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import com.intel.mountwilson.common.Config;
import com.intel.mountwilson.common.TAConfig;
import java.net.InetAddress;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

public class TASecureServer extends BaseServer {

    static final Logger log = LoggerFactory.getLogger(TASecureServer.class.getName());
    private SSLServerSocket serverSock = null;

    public TASecureServer(int serverPort) throws Exception {
        try {
            //619 allow keystore password to be specificed as a env variable
            CommandUtil.initJavaSslProperties();
            
            //System.err.println("keystore pw set to " + System.getProperty("javax.net.ssl.keyStorePassword"));
            
            SSLServerSocketFactory sslserversocketfactory =
                    (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            serverSock =
                    (SSLServerSocket) sslserversocketfactory.createServerSocket(getSecurePort());
        } catch (IOException e) {
            log.error( "Error while creating socket.", e);
            throw e;
        }
    }

    public void waitForConnections() {
        SSLSocket sock = null;
        /*
         Take ownership of the TPM
         */
        takeOwnerShip();
        
        while (true) {
            try {

                sock = (SSLSocket) serverSock.accept();
                log.info("Have accepted new socket.");
                
                // issue #1038 when mtwilson.tpm.quote.ipaddress we automatically  use our own address in the quote by overwriting the last 4 bytes of the nonce with it;
                InetAddress inetAddress = sock.getLocalAddress();
                log.debug("Trust Agent accepted connection with local address {}", inetAddress.getHostAddress());

                /*
                 Take ownership of the TPM. This time if already ownership is done 
                 * then this method will return. This is fix the bug where sometimes 
                 * tcsd is not up. 
                 */
                takeOwnerShip();

                
                handleConnection(inetAddress, sock.getInputStream(), sock.getOutputStream());

            } catch (Exception e) {
                log.error( null, e);
            } finally {
              
                try {
                    log.info("Closing socket.");
                    if(sock != null) {
                        sock.close();
                    }
                } catch (IOException ex) {
                    log.error(null, ex);
                }
            }

            log.info("Finished with socket, waiting for next connection.");
        }
    }

    public static void main(String argv[]) {

        TASecureServer server;
		try {
                    Security.addProvider(new BouncyCastleProvider());
			server = new TASecureServer(getSecurePort());
			server.waitForConnections();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("Error while starting TA" , e);
		}
        
    }

    private static int getSecurePort() {
        return Integer.parseInt(Config.getInstance().getProperty("secure.port"));
    }
}