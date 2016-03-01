/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.model.*;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.mtwilson.ms.common.MSException;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.setup.RemoteSetup;
import com.intel.mtwilson.setup.SetupContext;
import com.intel.mtwilson.setup.Timeout;
import com.intel.mtwilson.setup.model.AdminUser;
import com.intel.mtwilson.setup.model.Database;
import com.intel.mtwilson.setup.model.DatabaseType;
import com.intel.mtwilson.setup.model.DistinguishedName;
import com.intel.mtwilson.setup.model.PrivacyCA;
import com.intel.mtwilson.setup.model.SetupTarget;
import com.intel.mtwilson.setup.model.WebContainerType;
import com.intel.mtwilson.setup.ui.console.IntegerInput;
import com.intel.mtwilson.setup.ui.console.InternetAddressInput;
import com.intel.mtwilson.setup.ui.console.StringInput;
import com.intel.mtwilson.setup.ui.console.URLInput;
import com.intel.mtwilson.setup.ui.console.YesNoInput;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.InputModel;
import com.intel.dcsg.cpg.validation.Model;
import java.io.Console;
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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import net.schmizz.sshj.common.SecurityUtils;
import net.schmizz.sshj.userauth.UserAuthException;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class ConfigureRemote implements Command {
    public static final Console console = System.console(); // safe because Main checks that it exists before calling this command
  
    private static final InternetAddressInput INTERNET_ADDRESS_INPUT = new InternetAddressInput();
    private static final URLInput URL_INPUT = new URLInput();
    private static final YesNoInput YES_NO_INPUT = new YesNoInput();
    private static final IntegerInput INTEGER_INPUT = new IntegerInput();
    private static final StringInput STRING_INPUT = new StringInput();
    
    public static final String MTWILSON_CA_KEYSTORE_PACKAGE = "com/intel/mtwilson/ca/keystores";
    public static final String MTWILSON_SSH_KNOWN_HOSTS_PACKAGE = "com/intel/mtwilson/ssh/known_hosts";
    private SetupContext ctx = new SetupContext();
    
    private Configuration options = null;
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    @Override
    public void execute(String[] args) throws Exception {

        ctx.target = SetupTarget.REMOTE;

        try {
                generateRootCAKey(); // create or load root ca key and put it in the setup context
                
                RemoteSetup remote = new RemoteSetup(ctx);
                if (remote == null) {
                    throw new IllegalStateException("ConfigureRemote command failed: remote object is null.");
                }
                remote.setRemoteHost(getRequiredInternetAddressWithPrompt("SSH to remote host"));
                remote.setUsername(getRequiredStringWithPrompt("SSH Username (eg. root)"));
                remote.setPassword(getRequiredPasswordWithPrompt("SSH Password"));
                remote.setRemoteHostTimeout(new Timeout(60, TimeUnit.SECONDS));
                try {
                    remote.open();
                    if (remote.getRemoteHostKey() != null && remote.getRemoteHostKey().getHost() != null && remote.getRemoteHostKey().getPublicKey() != null) {
                        boolean trustRemoteHost = shouldTrustRemoteHost(remote.getRemoteHostKey().getHost(), remote.getRemoteHostKey().getPublicKey());
                        if( !trustRemoteHost ) { remote.close(); return; }
                        remote.getRemoteSettings();

    //            inputDistinguishedNameForCertificates();
                        if( ctx.rootCa != null ) {
                            remote.deployRootCACertToServer(); // using ssh, write the root CA cert to file on disk so server can trust it
                            // saml
                            remote.downloadSamlCertFromServer();
                            remote.signSamlCertWithCaCert();
                            remote.uploadSamlCertToServer();
                            // tls
                            remote.downloadTlsKeystoreFromServer(); 
                            if( ctx.tlsCertificate == null ) {
                                System.err.println("FAILED TO READ TLS CERT"); 
                                printFaults(remote);
                                remote.close(); return; 
                            }
    //                        remote.downloadTlsCertFromServer();
                            remote.signTlsCertWithCaCert();
                            remote.uploadTlsCertToServer(); 
                            remote.uploadTlsKeystoreToServer(); 
                            // privacy ca
                            remote.downloadPrivacyCaKeystoreFromServer();
                            remote.signPrivacyCaCertWithRootCaCert();
                            remote.uploadPrivacyCaKeystoreToServer();
                        }

                        remote.close();
                    }
                }
                catch(UserAuthException e) {
                    System.out.println("Not able to ssh to remote host with given username and password: "+e.toString());
                }
            
            collectUserInput();         
            createCertificates();
            displaySummary();
        }
        catch(Exception e) {
            e.printStackTrace(System.err);
        }

    }
    
    
    
    private boolean shouldTrustRemoteHost(String server, PublicKey serverPublicKey) throws IOException {
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
            boolean trustRemoteHost = getRequiredYesNoWithPrompt("Trust this remote host? (run 'mtwilson fingerprint' on remote host to see ssh key)");            
            if( trustRemoteHost ) {
                prefs.putByteArray(server, serverPublicKey.getEncoded());
                return true;
            }
            return false;
        }
    }
    
    public void displaySummary() {
        System.out.println("Mt Wilson URL: "+ctx.serverUrl.toExternalForm());
        if( ctx.attestationServiceDatabase != null ) {
            System.out.println("Mt Wilson Database: "+(ctx.attestationServiceDatabase.type==null?"unknown vendor":ctx.attestationServiceDatabase.type.displayName()));
            System.out.println("          hostname: "+(ctx.attestationServiceDatabase.hostname==null?"unknown hostname":ctx.attestationServiceDatabase.hostname.toString()));
            System.out.println("              port: "+ctx.attestationServiceDatabase.port);
            System.out.println("          username: "+ctx.attestationServiceDatabase.username);
            System.out.println("          password: "+ctx.attestationServiceDatabase.password);
            
        }
    }
    
    public void collectUserInput() throws IOException {
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
    public void createCertificates() {
        try {
            if( ctx.target.equals(SetupTarget.LOCAL)) {
    //            inputDistinguishedNameForCertificates();
                generateSelfSignedTlsKey();
                generateSelfSignedSamlSigningKey();
    //            generateSelfSignedPrivacyCAKey(); // privacy ca currently creates its own key during setup
            }
            else {
    //            inputDistinguishedNameForCertificates();
//                generateRootCAKey(); // 
//                deployRootCACertToServer(); // using ssh, write the root CA cert to file on disk so server can trust it
//                createServerTlsCertificate();
//                deployServerTlsCertificateToServer(); // using ssh, write it to glassfish keystore.jks
//                createServerSamlCertificate();
//                deployServerSamlCertificateToServer(); // using ssh, write it to mt wilson conf dir (inside keystore if possible and separate .crt file)
            }        
        }
        catch(NoSuchAlgorithmException e) {
            console.printf("Ignoring NoSuchAlgorith exception thrown during createCertificates");
        }
        catch(CryptographyException e) {
            console.printf("Ignoring Cryptography exception thrown during createCertificates");
        }
        catch(IOException e) {
            console.printf("Ignoring IO exception thrown during createCertificates");
        }
    }
    
    private String getConfirmedPasswordWithPrompt(String prompt) throws IOException {
        if (console == null) {
            throw new IOException("no console.");
        }
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

    private String getRequiredPasswordWithPrompt(String prompt) throws IOException {
        if (console == null) {
            throw new IOException("no console.");
        }
        while(true) {
            System.out.println(prompt);
            char[] password = console.readPassword("Password: ");
            if( password.length > 0 ) {
                return String.valueOf(password);
            }
        }
    }

    private int getSelectionFromListWithPrompt(List<String> list, String prompt) throws IOException {
        if (console == null) {
            throw new IOException("no console.");
        }
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

    private <T> T getRequiredEnumWithPrompt(Class<T> clazz, String prompt) throws IOException {
        T[] list = clazz.getEnumConstants();
        if( list == null ) { throw new IllegalArgumentException(clazz.getName()+" is not an enum type"); }
        ArrayList<String> strings = new ArrayList<String>();
        for( T item : list ) {
            strings.add(item.toString());
        }
        int selected = getSelectionFromListWithPrompt(strings, prompt);
        return list[selected];
    }
    
    private String getRequiredStringWithPrompt(String prompt) throws IOException {
        return getRequiredInputWithPrompt(STRING_INPUT, prompt, "String:");
    }

    // commenting out unused function (6/11 1.2)
    //private Integer getRequiredIntegerWithPrompt(String prompt) throws IOException {
    //    return getRequiredInputWithPrompt(INTEGER_INPUT, prompt, "Integer:");
    //}
    

    private Integer getRequiredIntegerInRangeWithPrompt(int min, int max, String prompt) throws IOException {
        return getRequiredInputWithPrompt(new IntegerInput(min,max), prompt, String.format("Integer [%d-%d]:", min, max));
    }
    
    /**
     * 
     * @param prompt
     * @return true for Yes, false for No
     */
    private boolean getRequiredYesNoWithPrompt(String prompt) throws IOException {
        return getRequiredInputWithPrompt(YES_NO_INPUT, prompt, "[Y]es or [N]o:").booleanValue();
    }

    private URL getRequiredURLWithPrompt(String prompt) throws IOException {
        return getRequiredInputWithPrompt(URL_INPUT, prompt, "URL:");
    }

    private InternetAddress getRequiredInternetAddressWithPrompt(String prompt) throws IOException {
        return getRequiredInputWithPrompt(INTERNET_ADDRESS_INPUT, prompt, "Hostname or IP Address:");
    }
    private InternetAddress getRequiredInternetAddressWithDefaultPrompt(String prompt, String defaultValue) throws IOException {
        return getRequiredInputWithDefaultPrompt(INTERNET_ADDRESS_INPUT, prompt, "Hostname or IP Address:", defaultValue);
    }
    
    private <T> T getRequiredInputWithPrompt(InputModel<T> model, String caption, String prompt) throws IOException {
        if (console == null) {
            throw new IOException("no console.");
        }
        while(true) {
            System.out.println(caption);
            String input = console.readLine(prompt+" ");
            model.setInput(input);
            if( model.isValid() ) {
                return model.value();
            }
            else {
                printFaults(model);
            } 
        }
    }
    
    private void printFaults(Model model) {
        System.err.println("--- Errors ---");
        for(Fault f : model.getFaults()) {
            printFault(f, 0); // level 0 means no indentation
        }
    }
    
    /**
     * 
     * @param f
     * @param level of indentation;  use 0 for top-level faults, and increment once for each level of logical nesting
     */
    private void printFault(Fault f, int level) {
        String indentation;
        StringBuilder indentationBuilder = new StringBuilder();
        for(int i=0; i<level; i++) {
            indentationBuilder.append("  "); // each level is indented two spaces from the previous level
        }
        indentation = indentationBuilder.toString();
        System.err.println(String.format("%s- %s", indentation, f.toString()));
        if( !f.getFaults().isEmpty() ) {
            System.err.println(String.format("%s  Related errors:", indentation));
            int size = f.getFaults().size();
            for(int i=0; i<size; i++) {
                printFault(f.getFaults().get(i), level+1);
            }
        }
    }

    private <T> T getRequiredInputWithDefaultPrompt(InputModel<T> model, String caption, String prompt, String defaultValue) throws IOException {
        if (console == null) {
            throw new IOException("no console.");
        }
        while(true) {
            System.out.println(caption);
            String input = console.readLine(prompt+" ["+defaultValue+"] ");
            if( input == null ) { input = defaultValue; }
            model.setInput(input);
            if( model.isValid() ) {
                return model.value();
            }
            else {
                for(Fault f : model.getFaults()) {
                    System.err.println(f.toString());
                }
            }
        }
    }
    
    private InternetAddress getRequiredInternetAddressWithMenuPrompt(String prompt) throws SocketException, IOException {
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
    
    public void inputMtWilsonURL() throws SocketException, IOException {
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
    
    public void inputMtWilsonDatabase() throws SocketException, IOException {
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
    }
    
    public void inputEkSigningKeyCredentials() throws IOException {
        System.out.println("In order to authorize Linux hosts using Trust Agent, an EK Signing Key is downloaded from Mt Wilson.");
        System.out.println("You must set a username and password to authenticate administrators who are downloading the key during a Trust Agent install.");
        PrivacyCA pca = new PrivacyCA();
        pca.ekSigningKeyDownloadUsername = getRequiredStringWithPrompt("EK Signing Key Download Username");
        pca.ekSigningKeyDownloadPassword = getConfirmedPasswordWithPrompt("EK Signing Key Download Password");
        
        ctx.privacyCA = pca;
    }


    public void inputManagementServiceAdminCredentials() throws IOException {
        System.out.println("You must set a username and password for the first Mt Wilson administrator account.");
        AdminUser admin = new AdminUser();
        admin.username = getRequiredStringWithPrompt("Administrator Username");
        admin.password = getConfirmedPasswordWithPrompt("Administrator Password");
        
        ctx.admin = admin;
    }
    //private void inputDistinguishedNameForCertificates() {
    //    System.out.println("The X509 Certificates are customized with your organization's details. All of these fields are optional. Press enter without entering anything to leave them blank.");
    //    DistinguishedName dn = new DistinguishedName();
//        dn.commonName = getRequiredStringWithPrompt("Common Name (eg. Product Name)");
    //    dn.organizationUnit = getRequiredStringWithPrompt("Organization Unit (eg. Product Name)");
    //    dn.organization = getRequiredStringWithPrompt("Organization (eg. Your Company)");
    //    dn.locality = getRequiredStringWithPrompt("Locality (eg. Your City)");
    //    dn.state = getRequiredStringWithPrompt("Locality (eg. Your State or Province)");
    //    dn.country = getRequiredStringWithPrompt("Country (eg. US)");
    //    
    //    ctx.dn = dn;
    //}

    /**
     * Precondition:  ctx.serverAddress must be defined
     * @throws NoSuchAlgorithmException
     * @throws CryptographyException
     * @throws IOException 
     */
    private void generateSelfSignedTlsKey() throws NoSuchAlgorithmException, CryptographyException, IOException {
        System.out.println("Going to generate a TLS/SSL key and certificate");
        KeyPair keypair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
        String alternativeName ;
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
     * @throws NoSuchAlgorithmException
     * @throws CryptographyException
     * @throws IOException T
     */
    private void generateSelfSignedSamlSigningKey() throws NoSuchAlgorithmException, CryptographyException, IOException {
        System.out.println("Going to generate a SAML key and certificate");
        KeyPair keypair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
        String alternativeName;
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
    private void generateRootCAKey() throws NoSuchAlgorithmException, CryptographyException, IOException, KeyManagementException, KeyStoreException, CertificateException, BackingStoreException {
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
                    System.err.println(String.format("- %s: %s", f.getClass().getName(), f.toString()));
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
