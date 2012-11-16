/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package securesample;

/**
 *
 * @author dsmagadX
 */
import java.io.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TrustAgentClientSecure {

    private String serverHostname = null;
    private int serverPort = 0;
    private byte[] data = null;
    private SSLSocket sock = null;
    private InputStream sockInput = null;
    private OutputStream sockOutput = null;

    public TrustAgentClientSecure(String serverHostname, int serverPort, byte[] data) {
        this.serverHostname = serverHostname;
        this.serverPort = serverPort;
        this.data = data;
    }

    public void sendRequest() {
        System.err.println("Opening connection to " + serverHostname + " port " + serverPort);
        try {
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            sock = (SSLSocket) sslsocketfactory.createSocket(serverHostname, serverPort);

            
            sockInput = sock.getInputStream();
            sockOutput = sock.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return;
        }

        System.err.println("About to start reading/writing to/from socket.");

        byte[] buf = new byte[5000];
        int bytes_read = 0;

        try {
            sockOutput.write(data, 0, data.length);
            bytes_read = sockInput.read(buf);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
       
        System.err.println("Sent " + bytes_read + " bytes to server and received them back again, msg = " + (new String(buf).trim()));

        System.err.println("Done reading/writing to/from socket, closing socket.");

        try {
            sock.close();
        } catch (IOException e) {
            System.err.println("Exception closing socket.");
            e.printStackTrace(System.err);
        }
        System.err.println("Exiting.");
    }

    public static void main(String argv[]) {
        
        
        if(argv.length < 1){
            System.err.println("java -jar SecureSample.jar <ipaddress>");
            System.exit(1);
        }
        
        
        String hostname = argv[0];
        int port = 9999;
        try{
        
        System.out.println("Sending BAD request");
        byte[] data = "<client_request></client_request>".getBytes();
        TrustAgentClientSecure client = new TrustAgentClientSecure(hostname, port, data);
        client.sendRequest();
        
        System.out.println("Sending Generate Identity");
        data = "<identity_request></identity_request>".getBytes();
        client = new TrustAgentClientSecure(hostname, port, data);
        client.sendRequest();

        System.out.println("Sending Generate Quote");
        data = "<quote_request><nonce>+nao5lHKxcMoqIGY3LuAYQ==</nonce><pcr_list>3-5,4-8</pcr_list></quote_request>".getBytes();
       // data = "<quote_request><nonce>Iamnonce</nonce><pcr_list>3,19</pcr_list></quote_request>".getBytes();
        client = new TrustAgentClientSecure(hostname, port, data);
        client.sendRequest();
        }catch(Throwable e){
            System.err.println("Error while contacting Trust Agent " + e.getMessage());
        }
    }
}
