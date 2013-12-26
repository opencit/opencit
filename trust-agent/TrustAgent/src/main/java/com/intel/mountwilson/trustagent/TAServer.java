/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent;

/**
 *
 * @author dsmagadX
 */
import com.intel.mountwilson.common.Config;
import static com.intel.mountwilson.trustagent.TASecureServer.log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TAServer extends BaseServer {

    static Logger log = LoggerFactory.getLogger(TAServer.class.getName());
    private ServerSocket serverSock = null;

    public TAServer(int serverPort) throws IOException {

        try {
            serverSock = new ServerSocket(serverPort);
        } catch (IOException e) {
            log.error("Error while creating socket.", e);
            throw e;
        }
    }

    public void waitForConnections() {
        Socket sock = null;
        InputStream sockInput;
        OutputStream sockOutput;
        /*
         Take ownership of the TPM
         */
        takeOwnerShip();
        
        while (true) {
            try {
                sock = serverSock.accept();
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
                
                sockInput = sock.getInputStream();
                sockOutput = sock.getOutputStream();


                handleConnection(inetAddress, sockInput, sockOutput);

            } catch (Exception e) {
                log.error( null, e);
            }finally{
              
                try {
                		log.info("Closing socket.");
                        if(sock!=null)
                            sock.close();
                } catch (IOException ex) {
                    log.error( null, ex);
                }
            }

            log.info("Finished with socket, waiting for next connection.");
        }
        
    }
    
    public void close() {
    	try {
    		serverSock.close();
    	}
    	catch(IOException e) {
    		log.warn("Failed to close server socket");
    	}
    }

    public static void main(String argv[]) throws IOException {
		TAServer server = new TAServer(getPort());
		server.waitForConnections();
		server.close();
    }

    private static int getPort() {
        return Integer.parseInt(Config.getInstance().getProperty("nonsecure.port"));
    }
}
