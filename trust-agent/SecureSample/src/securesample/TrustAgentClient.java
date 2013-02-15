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
import java.net.Socket;
import javax.net.SocketFactory;

public class TrustAgentClient {

    private String serverHostname = null;
    private int serverPort = 0;
    private byte[] data = null;
    private Socket sock = null;
    private InputStream sockInput = null;
    private OutputStream sockOutput = null;

    public TrustAgentClient(String serverHostname, int serverPort, byte[] data) {
        this.serverHostname = serverHostname;
        this.serverPort = serverPort;
        this.data = data;
    }

    public void sendRequest() {
        System.err.println("Opening connection to " + serverHostname + " port " + serverPort);
        try {
            sock = new Socket(serverHostname, serverPort);
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
        

        // Sleep for a bit so the action doesn't happen to fast - this is purely for reasons of demonstration, and not required technically.
        try {
            Thread.sleep(50);
        } catch (Exception e) {
            System.err.println("ignorning exception [" + e.getMessage() + "] thrown during sleep");
            
        };

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
        String hostname = "10.1.71.104";
        int port = 9998;
        byte[] data = "<client_request></client_request>".getBytes();

        TrustAgentClient client = new TrustAgentClient(hostname, port, data);
        client.sendRequest();
        
        data = "<identity_request></identity_request>".getBytes();

        client = new TrustAgentClient(hostname, port, data);
        client.sendRequest();

        data = "<quote_request><nonce>+nao5lHKxcMoqIGY3LuAYQ==</nonce><pcr_list>3,19</pcr_list></quote_request>".getBytes();
       // data = "<quote_request><nonce>Iamnonce</nonce><pcr_list>3,19</pcr_list></quote_request>".getBytes();
        client = new TrustAgentClient(hostname, port, data);
        client.sendRequest();
    }
}
