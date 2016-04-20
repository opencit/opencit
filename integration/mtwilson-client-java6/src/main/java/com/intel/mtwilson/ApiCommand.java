/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.datatypes.*;
import com.intel.dcsg.cpg.io.Filename;
import com.intel.mtwilson.api.*;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.rfc822.Rfc822Date;
import com.intel.dcsg.cpg.tls.policy.TlsUtil;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Properties;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated use com.intel.mtwilson.client.TextConsole
 * @since 0.5.2
 * @author jbuhacoff
 */
public class ApiCommand {
    private static Logger log = LoggerFactory.getLogger(ApiCommand.class);
    
    private static String getLocalhost() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return String.format("%s (%s)", addr.getHostName(), addr.getHostAddress());
        } catch (UnknownHostException ex) {
            return String.format("Unknown (%s)", ex.getMessage());
        }
    }
    
    /**
     * Syntax:
     * java -cp path/to/apiclient.jar com.intel.mtwilson.ApiCommand [configuration options] [command name] [command parameters]
     * Configuration options:
     * --conf=filename
     * 
     * @param args 
     */
    public static void main(String[] args) throws IOException, KeyManagementException, NoSuchAlgorithmException, GeneralSecurityException, ApiException, CryptographyException, ClientException, com.intel.dcsg.cpg.crypto.CryptographyException {
        
            for(int i=0;  i<args.length ;i++) {
                System.out.println("ApiCommand ARG "+i+" = "+args[i]);
            }
        
        if( args.length == 0 ) {
            printUsage();
            System.exit(1);
        }
        /*
        try {
            Command cmd = (Command)Class.forName("com.intel.mtwilson.ApiCommand."+args[0]).newInstance();
            cmd.run(args);
        }
        catch(ClassNotFoundException e) {
            System.err.println("Unrecognized command: "+args[0]+": "+e.getMessage());
            System.exit(1);
        }
        catch(InstantiationException e) {
            System.err.println("Cannot load command: "+args[0]+": "+e.getMessage());
            System.exit(1);            
        }
        catch(IllegalAccessException e) {
            System.err.println("Cannot load command: "+args[0]+": "+e.getMessage());
            System.exit(1);            
        }
        * 
        */
        
        if( args[0].equals("BootstrapUser") ) {
            // args[1] should be path to folder
            File directory = new File(args[1]);
            // args[2] should be the url of the server to register with
            URL server = new URL(args[2]);
            // we assume the user needs only the Security role; everything else can be done via the management console (including adding more roles to him/herself)
            String roles[] = new String[] { Role.Security.toString() };
            
            String username = null, password = null, tlsProtocol = "TLS";
            // args[2] is optional username (if not provided we will prompt)
            if( args.length > 3 ) { username = args[3]; }
            // args[3] is optional password plaintext (not recommended) or environment variable name (recommended) (if not provided we will prompt)
            if( args.length > 4 ) { password = args[4]; }
            if( args.length > 5 ) { tlsProtocol = args[5]; } // issue #870 allow user to specify tls protocol version, default to TLS
            
            // prompt for username and password
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            
            if( username == null || username.isEmpty() ) {
                System.out.print("Username: ");
                username = in.readLine();
            }
            if( password == null || password.isEmpty() ) {
                System.out.print("Password: ");
                password = in.readLine();
                System.out.print("Password again: ");
                String passwordAgain = in.readLine();
                if(password != null && passwordAgain != null) {
                    if( !password.equals(passwordAgain) ) {
                        System.err.println("The two passwords don't match");
                        System.exit(1);
                    }
                }else{
                    System.err.println("Error reading passwords.  Please run command again");
                    System.exit(1);
                }
                    
            }
            else if( password.startsWith("env:") && password.length() > 4 ) {
                String varName = password.substring(4);
                password = System.getenv(varName);
            }

            if( password == null || password.isEmpty() || password.length() < 6 ) {
                System.err.println("The password must be at least six characters");
                System.exit(1);
            }
                        
            // create the keystore
            String subject = username; //(see changes in RsaUtil ).format("CN=%s", username);            
            File keystoreFile = new File(directory.getAbsoluteFile() + File.separator + Filename.encode(username) + ".jks");
            SimpleKeystore keystore = new SimpleKeystore(keystoreFile, password);
            KeyPair keypair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
            X509Certificate certificate = RsaUtil.generateX509Certificate(subject, keypair, RsaUtil.DEFAULT_RSA_KEY_EXPIRES_DAYS);
            keystore.addKeyPairX509(keypair.getPrivate(), certificate, username, password);
            keystore.save();
            System.out.println("Created new user keystore: "+keystoreFile.getAbsolutePath());
            
            // register with the web service
            // download server's ssl certificates and add them to the keystore
            TlsUtil.addSslCertificatesToKeystore(keystore, server, tlsProtocol);
            // register the user with the server
            
            RsaCredentialX509 rsaCredential = keystore.getRsaCredentialX509(username, password);
            Properties p = new Properties();
            p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");
            p.setProperty("mtwilson.api.ssl.verifyHostname", "true");            
            ApiClient c = new ApiClient(server, rsaCredential, keystore, new MapConfiguration(p)); //ConfigurationFactory.fromSystemEnvironment());
            ApiClientCreateRequest user = new ApiClientCreateRequest();
            user.setCertificate(rsaCredential.getCertificate().getEncoded());
            user.setRoles(roles);
            try {
                c.register(user);
            }
            catch(javax.net.ssl.SSLException e) {
                if( e.getMessage().contains("hostname in certificate didn't match")  && !"false".equals(System.getenv("MTWILSON_API_SSL_VERIFY_HOSTNAME")) ) {
                    System.err.println(e.getMessage());
                    System.out.print("Do you want to continue anyway? [Y/N] ");
                    String ignoreHostname = in.readLine();
                    if( ignoreHostname != null && ignoreHostname.length() > 0 && ignoreHostname.toUpperCase().charAt(0) == 'Y' ) {
                        System.err.println("To avoid this prompt in the future, address the server by the hostname in its SSL certificate or set the environment variable MTWILSON_API_SSL_VERIFY_HOSTNAME=false");
                        Properties p2 = new Properties();
                        p2.setProperty("mtwilson.api.ssl.verifyHostname", "false");
                        c = new ApiClient(server, rsaCredential, keystore, new MapConfiguration(p2));
                        c.register(user);
                    }
                    else {
                        System.exit(2);
                    }
                }
                else {
                    throw e;
                }
            }
            // download server's saml certificate and save in the keystore
            X509Certificate samlCertificate = c.getSamlCertificate();
            keystore.addTrustedSamlCertificate(samlCertificate, server.getHost());
            log.debug("Added SAML Certificate with alias {}, subject {}, fingerprint {}, from server {}", server.getHost(), samlCertificate.getSubjectX500Principal().getName(), DigestUtils.shaHex(samlCertificate.getEncoded()), server.getHost());
            keystore.save();        

            /*
            // assume the management service is on the localhost, add 127.0.0.1 to mtwilson.api.trust in management-service.properties
            // fixed 2012-07-31: instead of adding 127.0.0.1, add the external IP of local host (same as server's external IP) since we're accessing it as , for example, https://1.2.3.4 then our IP will appear as 1.2.3.4 not 127.0.0.1 , so we want to whitelist the IP that the serve rwill see.
            
            Properties p = new Properties();
            p.load(new FileInputStream("/etc/intel/cloudsecurity/management-service.properties"));
            String previousWhitelistCSV = p.getProperty("mtwilson.api.trust","");
            String[] previousWhitelist = StringUtils.split(previousWhitelistCSV, ", ");
            String[] updatedWhitelist = (String[]) ArrayUtils.add(previousWhitelist, server.getHost()); // was:  "127.0.0.1"   now: same as server's IP, ex. 1.2.3.4
            String updatedWhitelistCSV = StringUtils.join(updatedWhitelist, ",");

            Runtime.getRuntime().exec("msctl edit mtwilson.api.trust \""+updatedWhitelistCSV+"\"");
            Runtime.getRuntime().exec("msctl restart");
            
            // self-approve
            ApiClientUpdateRequest update = new ApiClientUpdateRequest();
            update.fingerprint = rsaCredential.identity();
            update.enabled = true;
            update.roles = roles;
            update.status = ApiClientStatus.APPROVED.toString();
            update.comment = "via command line from "+getLocalhost()+" at "+Rfc822Date.format(new Date()); 
            c.updateApiClient(update); // ApiException, SignatureException
            
            // restore previous mtwilson.api.trust setting
            Runtime.getRuntime().exec("msctl edit mtwilson.api.trust "+previousWhitelistCSV);
            Runtime.getRuntime().exec("msctl restart");
            */
            /*
            Runtime.getRuntime().exec("msctl approve-user "+directory.getAbsoluteFile()+" "+Filename.encode(username)+" "+password);
            * 
            System.out.println("Bootstrap complete");
            */
            return; //System.exit(0);
        }
        
        //if( args[0].equals("CreateUser") ) {
        //
        //}
        
        if( args[0].equals("RegisterUser") ) {
            return; //System.exit(0);
        }

        if( args[0].equals("ApproveUser") ) {
            if( args.length < 4 ) {
                System.err.println("Usage: ApproveUser /path/to/username.jks ServiceURL Role1[,Role2,...]");
                System.err.println("ServiceURL is the URL to the management service");
                System.err.println("Try these roles:  Attestation,Whitelist,Security");
                System.exit(1);
            }
            // args[1] should be path to keystore (/path/to/directory/username.jks)
            File keystoreFile = new File(args[1]);
            // args[2] should be the url of the server to register with
            URL server = new URL(args[2]);
            // args[3] should be the roles being requested, comma-separated values  (Attestation,Whitelist,Security)
            String[] roles = StringUtils.split(args[3], ",");
            
            String username = Filename.decode(keystoreFile.getName().substring(0, keystoreFile.getName().lastIndexOf("."))); // username is everything before ".jks"
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Password: ");
            String password = in.readLine();
            
            SimpleKeystore keystore = new SimpleKeystore(keystoreFile, password);

            // approve the user with the server (requires server to trust our IP address)
            
            RsaCredentialX509 rsaCredential = keystore.getRsaCredentialX509(username, password);
            Properties p = new Properties();
            p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");
            p.setProperty("mtwilson.api.ssl.verifyHostname", "true");            
            ApiClient c = new ApiClient(server, rsaCredential, keystore, new MapConfiguration(p)); //ConfigurationFactory.fromSystemEnvironment());

            ApiClientUpdateRequest update = new ApiClientUpdateRequest();
            update.fingerprint = rsaCredential.identity();
            update.enabled = true;
            update.roles = roles;
            update.status = ApiClientStatus.APPROVED.toString();
                update.comment = "via command line from "+getLocalhost()+" at "+Rfc822Date.format(new Date()); 
            
            try {
                c.updateApiClient(update); // ApiException, SignatureException
            }
            catch(javax.net.ssl.SSLException e) {
                if( e.getMessage().contains("hostname in certificate didn't match")  && !"false".equals(System.getenv("MTWILSON_API_SSL_VERIFY_HOSTNAME")) ) {
                    System.err.println(e.getMessage());
                    System.out.print("Do you want to continue anyway? [Y/N] ");
                    String ignoreHostname = in.readLine();
                    if( ignoreHostname != null && ignoreHostname.length() > 0 && ignoreHostname.toUpperCase().charAt(0) == 'Y' ) {
                        System.err.println("To avoid this prompt in the future, address the server by the hostname in its SSL certificate or set the environment variable MTWILSON_API_SSL_VERIFY_HOSTNAME=false");
                        Properties p2 = new Properties();
                        p2.setProperty("mtwilson.api.ssl.verifyHostname", "false");
                        c = new ApiClient(server, rsaCredential, keystore, new MapConfiguration(p2));
                        c.updateApiClient(update); // ApiException, SignatureException
                    }
                    else {
                        System.exit(2);
                    }
                }
                else {
                    throw e;
                }
            }
            System.out.println("OK");
            return; //System.exit(0);
        }        
        /*
        
        int i = 0;
        try {
            ApiClient api = null;
            // look for configuration options
            while(args.length > i && args[i].startsWith("--")) {
                if( args[i].startsWith("--conf=") ) {
                    String configurationFilename = args[i].substring("--conf=".length());
                    System.out.println("Configuration filename: "+configurationFilename); 
                    api = new ApiClient(new File(configurationFilename));
                }
                ++i;
            }
            if( api == null ) {
                CompositeConfiguration config = new CompositeConfiguration();
                config.addConfiguration(new SystemConfiguration());
                config.addConfiguration(ConfigurationFactory.fromSystemEnvironment()); // lower priority than system properties set on java vm
                api = new ApiClient(config);
            }
            
            // look for the command name
            String command = null;
            if( args.length > i ) {
                command = args[i];
                ++i;
            }
            if( command == null ) {
                printUsage();
                return;
            }
            
            // execute the selected command
            if( "GetHostLocation".equals(command) ) {
                if( args.length > i ) {
                    api.getHostLocation(new Hostname(args[i]));
                    ++i;
                }
                else {
                    printUsage("GetHostLocation", "<hostname>");
                }
            }
        } catch (SignatureException ex) {
            log.error("Error signing request: "+ex.getMessage(), ex);
        } catch (KeyStoreException ex) {
            log.error("Error loading credentials: "+ex.getMessage(), ex);
        } catch (CertificateException ex) {
            log.error("Invalid certificate for key: "+ex.getMessage(), ex);
        } catch (UnrecoverableEntryException ex) {
            log.error("Error loading key: "+ex.getMessage(), ex);
        } catch (NoSuchAlgorithmException ex) {
            log.error("Algorithm not available: "+ex.getMessage(), ex);
        } catch (KeyManagementException ex) {
            log.error("Error in key management: "+ex.getMessage(), ex);
        } catch (MalformedURLException ex) {
            log.error("Cannot understand URL: "+ex.getMessage(), ex);
        } catch (IOException ex) {
            log.error("IO Error: "+ex.getMessage(), ex);
        } catch (ApiException ex) {
            log.error("API Error "+ex.getErrorCode()+": "+ex.getMessage(), ex);
        }
        * 
        */
    }
    
    private static void printUsage() {
        System.err.println("Usage:");
        System.err.println("CreateUser /path/to/directory");
        System.err.println("    Will prompt for username and password.");
        System.err.println("    Will create username.jks in directory.");
    }
    /*
    private static void printUsage(String command, String parameters) {
        System.err.println("Usage: mtwilson [configuration options] "+command+" "+parameters);
    }
    */
}
