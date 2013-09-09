/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import com.intel.mtwilson.api.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 0.5.3
 * @author jbuhacoff
 */
public class SshUtil {
    private static Logger log = LoggerFactory.getLogger(SshUtil.class);
    
    private static interface Command {
        void run(String[] args);
    }
    
    /**
     * Executes a remote command with no timeout
     * 
     * @param ssh
     * @param command
     * @return
     * @throws IOException which could be a ConnectionException or TransportException
     */
    public static String remote(SSHClient ssh, String command) throws IOException {
        return remote(ssh, command, null);
    }
    
    /**
     * The sshj client is designed to permit one command per "session". But
     * you can start multiple sessions per connection so this is ok.
     * 
     * @param ssh the ssh client
     * @param command string to execute on the remote shell
     * @param timeoutSeconds or null to wait indefinitely for the command to complete
     * @return
     * @throws IOException which could be a ConnectionException or TransportException
     */
    public static String remote(SSHClient ssh, String command, Timeout timeout) throws IOException {
        Session session = ssh.startSession();
        try {
            Session.Command cmd = session.exec(command); // ConnectionException, TransportException
            if( timeout == null ) {
                cmd.join();
            }
            else {
                cmd.join((int)timeout.toSeconds(), TimeUnit.SECONDS);  // the parameters are the timeout. if you want to wait indefinitely call join()
            }
            log.debug("Command exit status: {}", cmd.getExitStatus());
            String output = IOUtils.toString(cmd.getInputStream()); // IOException
            return output;
        }
        finally {
            session.close();
        }
    }
        
    
    
    
    
    
    
    
    
    
    
    
    

    /**
     * This should not be a @Test method because it requires the root password
     * of the server and we should not store that. So invoke it by running this
     * class via the main() method which will prompt for the root password.
     * 
     * @throws KeyStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws UnrecoverableEntryException
     * @throws KeyManagementException
     * @throws ApiException
     * @throws SignatureException 
     */
    public static void executeRemoteCommand(String ipAddress, String rootPassword, SshRemoteCommand command) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, KeyManagementException, ApiException, SignatureException {
        SSHClient ssh = new SSHClient();
        //ssh.loadKnownHosts(); // this is only if we have a known_hosts file...
        //ssh.addHostKeyVerifier("..."); // this is only if we know the fingerprint of the remote host we're connecting to
        ssh.addHostKeyVerifier(new HostKeyVerifier() {@Override public boolean verify(String arg0, int arg1, PublicKey arg2) { return true; } }); // this accepts all remote public keys
        ssh.connect(ipAddress);
        try {
            ssh.authPassword("root", rootPassword);
            command.execute(ssh);
        }
        finally {
            ssh.disconnect();
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static interface SshRemoteCommand {
        void execute(SSHClient ssh) throws IOException;
    }
    
    private static class ShowTrustHosts implements SshRemoteCommand {
        private String[] args;
        public ShowTrustHosts(String[] args) {
            this.args = args;
        }
        @Override
        public void execute(SSHClient ssh) throws IOException  {

            // find out what is the previous list of trusted hosts
            String previousWhitelistString = remote(ssh, "msctl show mtwilson.api.trust");
            System.out.println(previousWhitelistString);
        }
    }
    
    private static class AddLocalHostTrust implements SshRemoteCommand {
        private String[] args;
        public AddLocalHostTrust(String[] args) {
            this.args = args;
        }
        @Override
        public void execute(SSHClient ssh) throws IOException {
            
            // find out what is the previous list of trusted hosts
            String previousWhitelistString = remote(ssh, "msctl show mtwilson.api.trust");
            String[] previousWhitelist = previousWhitelistString.trim().split(","); // trim is required to remove the newline at the end of the output
            log.debug("Previous trusted clients network address list: {}", previousWhitelistString);
            // get local ip address and add it to the list
            InetAddress addr = InetAddress.getLocalHost();
            String[] updatedWhitelist = (String[]) ArrayUtils.add(previousWhitelist, addr.getHostAddress());
            String updatedWhitelistString = StringUtils.join(updatedWhitelist, ",");
            log.debug("Updated trusted clients network address list: {}", updatedWhitelistString);
            // set the new list on the server and restart the application
            remote(ssh, String.format("msctl edit mtwilson.api.trust \"%s\"", updatedWhitelistString));
            remote(ssh, "msctl restart");
            
        }
    }
    
    private static class SetTrustHosts implements SshRemoteCommand {
        private String[] args;
        public SetTrustHosts(String[] args) {
            this.args = args;
        }
        @Override
        public void execute(SSHClient ssh) throws IOException  {
            // whitelist should be in args[2]
            String previousWhitelistString = args[2];
            
            // now restore the original trusted hosts whitelist
            remote(ssh, String.format("msctl edit mtwilson.api.trust \"%s\"", previousWhitelistString));        
            log.info("Restored previous trusted clients network address list");
            remote(ssh, "msctl restart");
            
        }
    }
    
    
    
    
    
    
    
    
    
    /**
     * Syntax:
     * java -cp path/to/apiclient.jar com.intel.mtwilson.RemoteCommand <command> [parameters]
     * Configuration options:
     * --conf=filename
     * 
     * @param args 
     */
    public static void main(String[] args) throws IOException, KeyManagementException, NoSuchAlgorithmException, GeneralSecurityException, ApiException {
        
            for(int i=0;  i<args.length ;i++) {
                System.out.println("RemoteCommand ARG "+i+" = "+args[i]);
            }
        
        if( args.length == 0 ) {
            printUsage();
            System.exit(1);
        }

        if( args[0].equals("AddTrustLocalHost") ) {
            if( args.length < 2 ) {
                System.err.println("Usage: AddTrustLocalHost ServiceURL");
                System.err.println("ServiceURL is the URL to the management service");
                System.exit(1);
            }
                                    // args[1] should be an api baseurl (from which we will extract an ip address or hostname and connect to it with ssh)
            URL url = new URL(args[1]);
            String host = url.getHost();

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Remote password: ");
            String password = in.readLine();
            
            executeRemoteCommand(host, password, new AddLocalHostTrust(args));

            System.exit(0);
        }
        
        if( args[0].equals("SetTrustHosts") ) {
            if( args.length < 2 ) {
                System.err.println("Usage: SetTrustHosts ServiceURL value-for-mtwilson.api.trust");
                System.err.println("ServiceURL is the URL to the management service");
                System.exit(1);
            }

            URL url = new URL(args[1]);
            String host = url.getHost();

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Remote password: ");
            String password = in.readLine();
            
            executeRemoteCommand(host, password, new SetTrustHosts(args));
            


            System.exit(0);
        }
        
        if( args[0].equals("ShowTrustHosts") ) {
            if( args.length < 2 ) {
                System.err.println("Usage: ShowTrustHosts ServiceURL");
                System.err.println("ServiceURL is the URL to the management service");
                System.exit(1);
            }

            URL url = new URL(args[1]);
            String host = url.getHost();

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Remote password: ");
            String password = in.readLine();
            
            executeRemoteCommand(host, password, new ShowTrustHosts(args));
            


            System.exit(0);
        }

    }
    
    private static void printUsage() {
        System.err.println("Usage:");
        System.err.println("CreateUser /path/to/directory");
        System.err.println("    Will prompt for username and password.");
        System.err.println("    Will create username.jks in directory.");
    }
    
}
