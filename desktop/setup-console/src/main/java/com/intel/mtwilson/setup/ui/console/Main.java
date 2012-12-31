/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.ui.console;

import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.crypto.RsaCredentialX509;
import com.intel.mtwilson.crypto.RsaUtil;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.crypto.X509Builder;
import com.intel.mtwilson.validation.InputModel;
import com.intel.mtwilson.datatypes.InternetAddress;
import com.intel.mtwilson.datatypes.Md5Digest;
import com.intel.mtwilson.datatypes.Sha1Digest;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mtwilson.setup.*;
import com.intel.mtwilson.setup.cmd.*;
import com.intel.mtwilson.setup.model.*;
import com.intel.mtwilson.validation.Fault;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.SecurityUtils;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * A simple console program to obtain user preferences for either a local or
 * remote Mt Wilson configuration. 
 * 
 * The purpose of this program is to obtain and validate user input only - 
 * network access is allowed for input validation (server addresses, database
 * connections, etc) but not to effect any changes. Also this program should not
 * effect any changes on the local host.
 * 
 * The output of this program is a complete "mtwilson.properties" file in
 * the current directory. It can then be used to configure the local instance
 * (using another command) or copied to another server and used there.
 * 
 * How to run it for local setup:
 * java -cp setup-console-0.5.4-SNAPSHOT-with-dependencies.jar com.intel.mtwilson.setup.ui.console.Main local  (will not be used?? setup tool is premium and if it's going to be used it's likely to be used in remote mode, since existing installer already does local setup)
 * (the local argument is optional - default is local)
 * 
 * How to run it for remote setup of installed instance:
 * java -cp setup-console-0.5.4-SNAPSHOT-with-dependencies.jar com.intel.mtwilson.setup.ui.console.Main remote
 * 
 * How to run it for creating a configuration template to apply to selected remote instances:
 * java -cp setup-console-0.5.4-SNAPSHOT-with-dependencies.jar com.intel.mtwilson.setup.ui.console.Main cluster   (not implemented yet)
 * 
 * TODO: Currently the program prompts user to confirm the host key. This is ok, but
 * can be improved by allowing user to set known hosts file (maybe through environment variable)
 * or to check the windows registry (when running on windows) for the Putty known_hosts information.
 * See also: 
 * http://www.davidc.net/programming/java/reading-windows-registry-java-without-jni
 * http://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
 * http://superuser.com/questions/197489/where-does-putty-store-known-hosts-information-on-windows
 * http://kobowi.co.uk/blog/2011/08/convert-winscpputty-ssh-host-keys-to-known_hosts-format/
 * https://bitbucket.org/kobowi/reg2kh/  (python tool that exports putty and winscp registry keys to a known hosts file format)
 * 
 * @author jbuhacoff
 */
public class Main {
    public static final Console console = System.console();
    public static final SetupContext ctx = new SetupContext();

    private static final InternetAddressInput INTERNET_ADDRESS_INPUT = new InternetAddressInput();
    private static final URLInput URL_INPUT = new URLInput();
    private static final YesNoInput YES_NO_INPUT = new YesNoInput();
    private static final IntegerInput INTEGER_INPUT = new IntegerInput();
    private static final StringInput STRING_INPUT = new StringInput();
    
    public static final String MTWILSON_CA_KEYSTORE_PACKAGE = "com/intel/mtwilson/ca/keystores";
    public static final String MTWILSON_SSH_KNOWN_HOSTS_PACKAGE = "com/intel/mtwilson/ssh/known_hosts";
    
    /**
     * Argument 1:  "local" or "remote"  to indicate if we are setting up the local host or if we are setting up a cluster remotely;  case-insensitive
     * @param args 
     */
    public static void main(String[] args) {
        if (console == null) {
            System.err.println("No console.");
            System.exit(1);
        }
        
        // turn off jdk logging because sshj logs to console
        LogManager.getLogManager().reset();
//        Logger globalLogger = Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);  
//        globalLogger.setLevel(java.util.logging.Level.OFF);          
        
        try {
            if( args.length > 0 ) {
                ctx.target = SetupTarget.valueOf(args[0].toUpperCase()); 
            }
            else {
                ctx.target = SetupTarget.LOCAL;
            }
            
            if(ctx.target.equals(SetupTarget.REMOTE)) {
                generateRootCAKey(); // create or load root ca key and put it in the setup context
                
                RemoteSetup remote = new RemoteSetup(ctx);
                remote.setRemoteHost(getRequiredInternetAddressWithPrompt("SSH to remote host"));
                remote.setUsername(getRequiredStringWithPrompt("SSH Username (eg. root)"));
                remote.setPassword(getRequiredPasswordWithPrompt("SSH Password"));
                remote.setRemoteHostTimeout(new Timeout(60, TimeUnit.SECONDS));
                try {
                    remote.open();
                    boolean trustRemoteHost = shouldTrustRemoteHost(remote.getRemoteHostKey().server, remote.getRemoteHostKey().publicKey);
                    if( !trustRemoteHost ) { remote.close(); return; }
                    remote.getRemoteSettings();

//            inputDistinguishedNameForCertificates();
                    if( ctx.rootCa != null ) {
                        remote.deployRootCACertToServer(); // using ssh, write the root CA cert to file on disk so server can trust it
                        // saml
                        remote.downloadSamlCertFromServer();
                        remote.signSamlCertWithCaCert();// XXX TODO  we could check if it's already signed by our CA, and if it's not expiring soon we can just skip this step.
                        remote.uploadSamlCertToServer();
                        // tls
                        remote.downloadTlsCertFromServer();
                        remote.signTlsCertWithCaCert();// XXX TODO  we could check if it's already signed by our CA, and if it's not expiring soon we can just skip this step.
                        remote.uploadTlsCertToServer();
//                        createServerTlsCertificate();
//                        deployServerTlsCertificateToServer(); // using ssh, write it to glassfish keystore.jks
//                        createServerSamlCertificate();
//                        deployServerSamlCertificateToServer(); // using ssh, write it to mt wilson conf dir (inside keystore if possible and separate .crt file)
                    }
                    
                    remote.close();
                }
                catch(UserAuthException e) {
                    System.out.println("Not able to ssh to remote host with given username and password: "+e.toString());
                }
                return;
            }
            
            collectUserInput();         
            createCertificates();
            displaySummary();
        }
        catch(Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    private static boolean shouldTrustRemoteHost(String server, PublicKey serverPublicKey) {
        Preferences prefs = Preferences.userRoot().node(MTWILSON_SSH_KNOWN_HOSTS_PACKAGE);
        byte[] knownHostKey = prefs.getByteArray(server, null);        
        if( knownHostKey == null ) {
            // we don't know this server, so ask user if it is trusted
            System.out.println("Remote host key ("+serverPublicKey.getAlgorithm()+"): "+SecurityUtils.getFingerprint(serverPublicKey)); // can't just do Md5Digest.valueOf(remote.getRemoteHostKey().publicKey.getEncoded()).toString()) because ssh key fingerprints have a specific encoding, for example "ssh-rsa" followed by exponent length, exponent, modulus length, modulus... which is not the same as the byte from publickey.getEncoded() 
            boolean trustRemoteHost = getRequiredYesNoWithPrompt("Trust this remote host? (run 'mtwilson fingerprint' on remote host to see ssh key)");            
            if( trustRemoteHost ) {
                prefs.putByteArray(server, serverPublicKey.getEncoded());
                return true;
            }
            return false;
        }
        else {
            // we know this server, so check if the public key matches
            if( Arrays.equals(knownHostKey, serverPublicKey.getEncoded()) ) {
                return true;
            }
            // key did not match, ask user if s/he wants to accept the new key - with a warning
            System.err.println("WARNING: The remote host key has changed.");
            System.out.println("Remote host key ("+serverPublicKey.getAlgorithm()+"): "+SecurityUtils.getFingerprint(serverPublicKey)); // can't just do Md5Digest.valueOf(remote.getRemoteHostKey().publicKey.getEncoded()).toString()) because ssh key fingerprints have a specific encoding, for example "ssh-rsa" followed by exponent length, exponent, modulus length, modulus... which is not the same as the byte from publickey.getEncoded() 
//            TODO:  show previously trusted key information? 
            boolean trustRemoteHost = getRequiredYesNoWithPrompt("Trust this remote host? (run 'mtwilson fingerprint' on remote host to see ssh key)");            
            if( trustRemoteHost ) {
                prefs.putByteArray(server, serverPublicKey.getEncoded());
                return true;
            }
            return false;
        }
    }
    
    public static void displaySummary() {
        System.out.println("Mt Wilson URL: "+ctx.serverUrl.toExternalForm());
        System.out.println("Mt Wilson Database: "+ctx.attestationServiceDatabase.type.displayName());
        System.out.println("          hostname: "+ctx.attestationServiceDatabase.hostname.toString());
        System.out.println("              port: "+ctx.attestationServiceDatabase.port);
        System.out.println("          username: "+ctx.attestationServiceDatabase.username);
        System.out.println("          password: "+ctx.attestationServiceDatabase.password);
    }
    
    public static void collectUserInput() {
        try {
            inputMtWilsonURL();
            inputMtWilsonDatabase();
            inputManagementServiceAdminCredentials();
            inputEkSigningKeyCredentials();
        }
        catch(SocketException e) {
            System.err.println("Got error: "+e.toString());
        }
        
        
    }
    public static void createCertificates() {
        try {
            if( ctx.target.equals(SetupTarget.LOCAL)) {
    //            inputDistinguishedNameForCertificates();
                generateSelfSignedTlsKey();
                generateSelfSignedSamlSigningKey();
    //            generateSelfSignedPrivacyCAKey(); // privacy ca currently creates its own key during setup
            }
            else {
    //            inputDistinguishedNameForCertificates();
//                generateRootCAKey(); // TODO: we might already have one created & saved locally... but user may want a new one... so if we find one existing we need to ask
//                deployRootCACertToServer(); // using ssh, write the root CA cert to file on disk so server can trust it
//                createServerTlsCertificate();
//                deployServerTlsCertificateToServer(); // using ssh, write it to glassfish keystore.jks
//                createServerSamlCertificate();
//                deployServerSamlCertificateToServer(); // using ssh, write it to mt wilson conf dir (inside keystore if possible and separate .crt file)
            }        
        }
        catch(NoSuchAlgorithmException e) {
        }
        catch(CryptographyException e) {
        }
        catch(IOException e) {
        }
    }
    
    private static String getConfirmedPasswordWithPrompt(String prompt) {
        while(true) {
            System.out.println(prompt);
            char[] password = console.readPassword("Password: ");
            char[] passwordAgain = console.readPassword("Password (again):");
            if( password.length == passwordAgain.length && String.valueOf(password).equals(String.valueOf(passwordAgain)) ) {
                return String.valueOf(password);
            }
            System.out.println("Passwords must match.");
        }
    }

    private static String getRequiredPasswordWithPrompt(String prompt) {
        while(true) {
            System.out.println(prompt);
            char[] password = console.readPassword("Password: ");
            if( password.length > 0 ) {
                return String.valueOf(password);
            }
        }
    }

    private static int getSelectionFromListWithPrompt(List<String> list, String prompt) {
        while(true) {
            System.out.println(prompt);
            for(int i=0; i<list.size(); i++) {
                System.out.println(String.format("[%2d] %s", i+1, list.get(i)));
            }
            String selection = console.readLine("Choose 1-%d: ", list.size());
            try {
                Integer value = Integer.valueOf(selection);
                if( value >=1 && value <= list.size() ) {
                    return value-1;
                }
            }
            catch(java.lang.NumberFormatException e) {
                System.err.println("Press Ctrl+C to exit");
            }
        }
    }

    private static <T> T getRequiredEnumWithPrompt(Class<T> clazz, String prompt) {
        T[] list = clazz.getEnumConstants();
        if( list == null ) { throw new IllegalArgumentException(clazz.getName()+" is not an enum type"); }
        ArrayList<String> strings = new ArrayList<String>();
        for( T item : list ) {
            strings.add(item.toString());
        }
        int selected = getSelectionFromListWithPrompt(strings, prompt);
        return list[selected];
    }
    
    private static String getRequiredStringWithPrompt(String prompt) {
        return getRequiredInputWithPrompt(STRING_INPUT, prompt, "String:");
    }

    
    private static Integer getRequiredIntegerWithPrompt(String prompt) {
        return getRequiredInputWithPrompt(INTEGER_INPUT, prompt, "Integer:");
    }
    

    private static Integer getRequiredIntegerInRangeWithPrompt(int min, int max, String prompt) {
        return getRequiredInputWithPrompt(new IntegerInput(min,max), prompt, String.format("Integer [%d-%d]:", min, max));
    }
    
    /**
     * 
     * @param prompt
     * @return true for Yes, false for No
     */
    private static boolean getRequiredYesNoWithPrompt(String prompt) {
        return getRequiredInputWithPrompt(YES_NO_INPUT, prompt, "[Y]es or [N]o:").booleanValue();
    }

    private static URL getRequiredURLWithPrompt(String prompt) {
        return getRequiredInputWithPrompt(URL_INPUT, prompt, "URL:");
    }

    private static InternetAddress getRequiredInternetAddressWithPrompt(String prompt) {
        return getRequiredInputWithPrompt(INTERNET_ADDRESS_INPUT, prompt, "Hostname or IP Address:");
    }
    private static InternetAddress getRequiredInternetAddressWithDefaultPrompt(String prompt, String defaultValue) {
        return getRequiredInputWithDefaultPrompt(INTERNET_ADDRESS_INPUT, prompt, "Hostname or IP Address:", defaultValue);
    }
    
    private static <T> T getRequiredInputWithPrompt(InputModel<T> model, String caption, String prompt) {
        while(true) {
            System.out.println(caption);
            String input = console.readLine(prompt+" ");
            model.setInput(input);
            if( model.isValid() ) {
                return model.value();
            }
            else {
                // TODO: print faults
                for(Fault f : model.getFaults()) {
                    System.err.println(f.toString());
                }
            }            
            // TODO: allow user to break by typing 'exit', 'cancel', 'abort', etc, and we can throw an exception like UserAbortException (must create it) so the main program can have a chance to save what has already been validated and exit, or skip to the next step, or something.
        }
    }

    private static <T> T getRequiredInputWithDefaultPrompt(InputModel<T> model, String caption, String prompt, String defaultValue) {
        while(true) {
            System.out.println(caption);
            String input = console.readLine(prompt+" ["+defaultValue+"] ");
            if( input == null ) { input = defaultValue; }
            model.setInput(input);
            if( model.isValid() ) {
                return model.value();
            }
            else {
                // TODO: print faults
                for(Fault f : model.getFaults()) {
                    System.err.println(f.toString());
                }
            }            
            // TODO: allow user to break by typing 'exit', 'cancel', 'abort', etc, and we can throw an exception like UserAbortException (must create it) so the main program can have a chance to save what has already been validated and exit, or skip to the next step, or something.
        }
    }
    
    private static InternetAddress getRequiredInternetAddressWithMenuPrompt(String prompt) throws SocketException {
        SetMtWilsonURL cmd = new SetMtWilsonURL();
        List<String> options = cmd.getLocalAddresses();
        if( ctx.serverAddress != null && !options.contains(ctx.serverAddress.toString())) { 
            options.add(ctx.serverAddress.toString());
        }
        options.add("Other");
        int selected = getSelectionFromListWithPrompt(options, prompt);
        InternetAddress address; 
        if( selected == options.size() - 1 ) { // "Other"
            address = getRequiredInternetAddressWithPrompt("Other "+prompt);
        }
        else {
            address = new InternetAddress(options.get(selected));
        }
        return address;
    }
    
    public static void inputMtWilsonURL() throws SocketException {
        if( ctx.target.equals(SetupTarget.LOCAL) ) {
            InternetAddress address = getRequiredInternetAddressWithMenuPrompt("Local Mt Wilson Hostname or IP Address");
            System.out.println("selected: "+address.toString());
            ctx.serverAddress = address;
        }
        else {
            InternetAddress address = getRequiredInternetAddressWithPrompt("Remote Mt Wilson Hostname or IP Address");
            System.out.println("selected: "+address.toString());
            ctx.serverAddress = address;
        }
        
        WebContainerType webContainerType = getRequiredEnumWithPrompt(WebContainerType.class, "Web application container");
        String defaultUrl = String.format("https://%s:%d", ctx.serverAddress, webContainerType.defaultHttpsPort());
        boolean urlOk = getRequiredYesNoWithPrompt(String.format("Default Mt Wilson URL: %s\nIs this ok?", defaultUrl));
        if( urlOk ) {
            try {
                ctx.serverUrl = new URL(defaultUrl);
            }
            catch(MalformedURLException e) {
                System.err.println("There is a problem with this URL: "+defaultUrl);
                ctx.serverUrl = getRequiredURLWithPrompt("Please enter the Mt Wilson URL");
            }
        }
        else {
            ctx.serverUrl = getRequiredURLWithPrompt("Please enter the Mt Wilson URL");
        }
//        ctx.serverPort = ctx.serverUrl.getPort();                
        
    }
    
    public static void inputMtWilsonDatabase() throws SocketException {
        Database db = new Database();
        db.type = getRequiredEnumWithPrompt(DatabaseType.class, "Database system");
        db.driver = db.type.defaultJdbcDriver();
        if( ctx.target.equals(SetupTarget.LOCAL) ) {
            db.hostname = getRequiredInternetAddressWithMenuPrompt("Database server Hostname or IP Address");
        }
        else {
            db.hostname = getRequiredInternetAddressWithDefaultPrompt("Database server Hostname or IP Address", ctx.serverAddress.toString());
        }
        boolean useNonDefaultPort = getRequiredYesNoWithPrompt(String.format("Default port is %d. Do you want to change it?", db.type.defaultPort()));
        if( useNonDefaultPort ) {
            db.port = getRequiredIntegerInRangeWithPrompt(0,65535,"Database port");
        }
        else {
            db.port = db.type.defaultPort();
        }
        db.username = getRequiredStringWithPrompt("Database username");
        db.password = getRequiredPasswordWithPrompt("Database password");
        
        ctx.attestationServiceDatabase = db;
        // TODO: verify the connection & login;  maybe do it outside this function so the entire thing can be repeated as necessary.
    }
    
    public static void inputEkSigningKeyCredentials() {
        System.out.println("In order to authorize Linux hosts using Trust Agent, an EK Signing Key is downloaded from Mt Wilson.");
        System.out.println("You must set a username and password to authenticate administrators who are downloading the key during a Trust Agent install.");
        PrivacyCA pca = new PrivacyCA();
        pca.ekSigningKeyDownloadUsername = getRequiredStringWithPrompt("EK Signing Key Download Username");
        pca.ekSigningKeyDownloadPassword = getConfirmedPasswordWithPrompt("EK Signing Key Download Password");
        
        ctx.privacyCA = pca;
    }


    public static void inputManagementServiceAdminCredentials() {
        System.out.println("You must set a username and password for the first Mt Wilson administrator account.");
        AdminUser admin = new AdminUser();
        admin.username = getRequiredStringWithPrompt("Administrator Username");
        admin.password = getConfirmedPasswordWithPrompt("Administrator Password");
        
        ctx.admin = admin;
    }
    
    private static void inputDistinguishedNameForCertificates() {
        System.out.println("The X509 Certificates are customized with your organization's details. All of these fields are optional. Press enter without entering anything to leave them blank.");
        DistinguishedName dn = new DistinguishedName();
//        dn.commonName = getRequiredStringWithPrompt("Common Name (eg. Product Name)");
        dn.organizationUnit = getRequiredStringWithPrompt("Organization Unit (eg. Product Name)");
        dn.organization = getRequiredStringWithPrompt("Organization (eg. Your Company)");
        dn.locality = getRequiredStringWithPrompt("Locality (eg. Your City)");
        dn.state = getRequiredStringWithPrompt("Locality (eg. Your State or Province)");
        dn.country = getRequiredStringWithPrompt("Country (eg. US)");
        
        ctx.dn = dn;
    }

    /**
     * TODO:  rewrite using X509Builder
     * Precondition:  ctx.serverAddress must be defined
     * @throws NoSuchAlgorithmException
     * @throws CryptographyException
     * @throws IOException 
     */
    private static void generateSelfSignedTlsKey() throws NoSuchAlgorithmException, CryptographyException, IOException {
        System.out.println("Going to generate a TLS/SSL key and certificate");
        KeyPair keypair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
        String alternativeName = null;
        if( ctx.serverAddress.isHostname() ) {
            alternativeName = "dns:"+ctx.serverAddress.toString();
        }
        else { // assume isIPv4() or isIPv6()
            alternativeName = "ip:"+ctx.serverAddress.toString();
        }
        X509Certificate certificate = RsaUtil.generateX509Certificate(ctx.serverAddress.toString()+" TLS", alternativeName, keypair, 3650); // valid for 10 years
        ctx.tlsKeypair = keypair;
        ctx.tlsCertificate = certificate;
    }
    
    /**
     * TODO: rewrite using X509Builder
     * @throws NoSuchAlgorithmException
     * @throws CryptographyException
     * @throws IOException T
     */
    private static void generateSelfSignedSamlSigningKey() throws NoSuchAlgorithmException, CryptographyException, IOException {
        System.out.println("Going to generate a SAML key and certificate");
        KeyPair keypair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
        String alternativeName = null;
        if( ctx.serverAddress.isHostname() ) {
            alternativeName = "dns:"+ctx.serverAddress.toString();
        }
        else { // assume isIPv4() or isIPv6()
            alternativeName = "ip:"+ctx.serverAddress.toString();
        }
        X509Certificate certificate = RsaUtil.generateX509Certificate(ctx.serverAddress.toString()+" SAML", alternativeName, keypair, 3650); // valid for 10 years
        ctx.samlKeypair = keypair;
        ctx.samlCertificate = certificate;
    }

    /**
     * Postcondition:  a Mt Wilson Root CA Key and Certificate are saved on the
     * local host protected by a user-selected password, or else an exception is
     * thrown.
     * 
     * Check if there is already a root CA key created - if so, attempt to unlock it with user password;  if not, create a new one.
     * 
     * If the user cannot unlock the root CA (forgotten password), give the user
     * the option to export the locked key to a file and create a new key to put
     * in the preferences. 
     * 
     * The Mt Wilson Root CA is stored in the user's preferences (not system preferences).
     * 
     * Because this class is a UI class, an explicit package name is provided for
     * the preferences.
     * 
     * XXX We only support one root CA key in the preferences at
     * a time. If there is a business need later to support multiple root Ca keys,
     * we can easily add support for storing them in files or multiple keys in the
     * preferences at that time. It's also possible to implement a different Preferences
     * backing store and specify it as a JVM parameter when starting the program - 
     * See also http://docs.oracle.com/javase/1.4.2/docs/guide/lang/preferences.html and
     * http://docs.oracle.com/javase/6/docs/api/java/util/prefs/PreferencesFactory.html and
     * http://docs.oracle.com/javase/6/docs/api/java/util/prefs/Preferences.html and
     * the java System Property "java.util.prefs.PreferencesFactory"
     * The Preferences API also has an XML import/export facility.
     * 
     * Note that max length of a Preferences value is 8192 characters, and 
     * typical size of a keystore with one key is about 2200 characters.
     * 
     * All CA keys and certificates are stored under com/intel/mtwilson/ca/keystores.
     * Each one has its own keystore in order to make sure that we don't exceed the 8KB
     * size limit on preference values.  The name of the CA is the preference name,
     * which would also be part of the filename if it were to be exported to a file.
     * Inside the CA keystore, all CA's have their keypair in an entry alias "ca"
     * 
     * @throws NoSuchAlgorithmException
     * @throws CryptographyException
     * @throws IOException 
     */
    private static void generateRootCAKey() throws NoSuchAlgorithmException, CryptographyException, IOException, KeyManagementException, KeyStoreException, CertificateException, BackingStoreException {
        final String ROOT = "root";
        final String CA_ALIAS = "ca";
        Preferences prefs = Preferences.userRoot().node(MTWILSON_CA_KEYSTORE_PACKAGE);

        byte[] keystoreBytes = prefs.getByteArray(ROOT, null);
        if( keystoreBytes == null ) {
            System.out.println("Going to generate a Mt Wilson Root CA key and certificate");
            String organizationName = getRequiredStringWithPrompt("Your organization name or company");
            String organizationUnit = getRequiredStringWithPrompt("Your organizational unit, division, product, or service");
            String dn = String.format("CN=Mt Wilson Root CA, O=%s, OU=%s", organizationName, organizationUnit);
            String keystorePassword = getConfirmedPasswordWithPrompt("New Root CA Password");
            KeyPair keypair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
            X509Builder x509 = X509Builder.factory().selfSigned(dn, keypair).expires(3650, TimeUnit.DAYS).keyUsageCertificateAuthority();
            X509Certificate certificate = x509.build();
            if( certificate == null ) {
                System.err.println("Cannot generate certificate:");
                for(Fault f : x509.getFaults()) {
                    System.err.println(String.format("- %s%s", f.toString(), f.getCause()==null?"":" ["+f.getCause().toString()+"]"));
                }
                return;
            }
//            X509Certificate certificate2 = RsaUtil.generateX509Certificate("Mt Wilson Root CA", keypair, 3650); // valid for 10 years
            RsaCredentialX509 rootCa = new RsaCredentialX509(keypair.getPrivate(), certificate);
            ctx.rootCa = rootCa;
            ByteArrayResource mwKeystoreResource = new ByteArrayResource();
            SimpleKeystore keystore = new SimpleKeystore(mwKeystoreResource, keystorePassword); 
            keystore.addKeyPairX509(keypair.getPrivate(), certificate, CA_ALIAS, keystorePassword);
            keystore.save(); // into mwKeystoreResource
            prefs.putByteArray(ROOT,mwKeystoreResource.toByteArray());
            
            /*
            // XXX debugging only... let's see that keystore
            System.out.println("the keystore now has aliases: "+StringUtils.join(keystore.aliases(), ", "));
            System.out.println("the resource has "+mwKeystoreResource.toByteArray().length+" bytes");
            FileOutputStream tmp = new FileOutputStream(new File(System.getProperty("user.home")+File.separator+"tmpkeystore2.jks"));
            IOUtils.copy(mwKeystoreResource.getInputStream(), tmp);
            tmp.close();
            */
        }
        else {
            System.out.println("You already have a Mt Wilson Root CA");
            ByteArrayResource mwKeystoreResource = new ByteArrayResource(keystoreBytes);       
            
            /*
            // XXX debugging only... let's see that keystore
            FileOutputStream tmp = new FileOutputStream(new File(System.getProperty("user.home")+File.separator+"tmpkeystore.jks"));
            IOUtils.copy(mwKeystoreResource.getInputStream(), tmp);
            tmp.close();
            */
            
            while(true) {
                try {
                    String keystorePassword = getRequiredPasswordWithPrompt("Existing Root CA Password");
                    SimpleKeystore keystore = new SimpleKeystore(mwKeystoreResource, keystorePassword); 
                    RsaCredentialX509 rootCa = keystore.getRsaCredentialX509(CA_ALIAS, keystorePassword);
                    ctx.rootCa = rootCa;
                    break;
                }
                catch(Exception e) {
                    System.err.println("Cannot load Mt Wilson Root CA: "+e.toString());
                    boolean tryAgain = getRequiredYesNoWithPrompt("Try again?");
                    if( !tryAgain ) {
                        break;
                    }
                }
            }
        }
        
    }
    

}
