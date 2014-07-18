/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.Pkcs12;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
//import com.intel.mtwilson.datatypes.TLSPolicy;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.mtwilson.model.*;
import com.intel.mtwilson.setup.model.Database;
import com.intel.mtwilson.setup.model.DatabaseType;
import com.intel.mtwilson.setup.model.PrivacyCA;
import com.intel.mtwilson.setup.model.WebContainerType;
import com.intel.mtwilson.setup.model.WebServiceSecurityPolicy;
import com.intel.dcsg.cpg.validation.BuilderModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.Console;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.xfer.InMemoryDestFile;
import net.schmizz.sshj.xfer.InMemorySourceFile;
import net.schmizz.sshj.xfer.scp.SCPException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import sun.security.x509.X500Name;

/**
 * 
 * Test case for signing remote Glassfish TLS certificate:
 * 
 * First import the mtwilson root ca you created into your computer's Trusted Certificate Authorities list (double click the file, follow prompts)
 * cd /usr/share/glassfish4/glassfish/domains/domain1/config
 * keytool -list -keystore keystore.jks -storepass changeit
 * keytool -keystore keystore.jks -storepass changeit -exportcert -alias glassfish-instance -file ssl.selfsigned.crt
 * openssl x509 -in ssl.selfsigned.crt -inform der -text
 * cp keystore.jks keystore.jks.bak
 * Run the setup tool... now keytool -list will show an extra cert (mtwilson-rootca) and the glassfish cert will be signed by that root ca
 * keytool -list -keystore keystore.jks -storepass changeit
 * keytool -keystore keystore.jks -storepass changeit -exportcert -alias glassfish-instance -file ssl.casigned.crt
 * openssl x509 -in ssl.casigned.crt -inform der -text
 * keytool -keystore keystore.jks -storepass changeit -exportcert -alias mtwilson-rootca -file mtwilson.rootca.crt
 * openssl x509 -in mtwilson.rootca.crt -inform der -text
 * Restart glassfish or just cause it to reload ssl settings (Configurations -> server-config -> Network Config -> Network Listeners -> http-listener-2 -> SSL -> Save)
 * Check that the connection to server is now trusted by visiting https://yourserver:8181/TrustDashBoard 
 * (If you're using Chrome, you must restart your browser due to a bug in Chome that will still show the site as untrusted, until you restart the browser)
 * 
 * Test case for signing remote Privacy CA certificate:
 * cd /etc/intel/cloudsecurity
 * openssl pkcs12 -in PrivacyCA.p12 -out PrivacyCA.p12.txt   (the password you need is in PrivacyCA.properties, copy/paste it into all prompts)
 * openssl x509 -in PrivacyCA.p12.text -inform pem -text   (you will see subject and issuer are both CN=HIS_Privacy_CA)
 * cp PrivacyCA.p12 PrivacyCA.p12.bak
 * Run the setup tool...
 * keytool -list -keystore PrivacyCA.p12 -storetype PKCS12 -storepass changeit
 * keytool -exportcert -keystore PrivacyCA.p12 -storetype PKCS12 -storepass changeit -alias 1 -file PrivacyCA.p12.casigned.crt
 * keytool -printcert -file PrivacyCA.p12.casigned.crt
 * openssl pkcs12 -in PrivacyCA.p12 -out PrivacyCA.p12.casigned.txt   (the password you need is in PrivacyCA.properties, copy/paste it into all prompts)
 * openssl x509 -in PrivacyCA.p12.casigned.txt -inform pem -text   (you will see subject and issuer are both CN=HIS_Privacy_CA)
 * Should see an extra cert in that file now, for mtwilson root ca,  and should see mt wilson root ca as issuer of the privacy ca cert.
 * 
 * @author jbuhacoff
 */
public class RemoteSetup extends BuilderModel implements Closeable {
    public static final Console console = System.console(); // safe because Main checks that it exists before calling this command
    private final SetupContext ctx;
    private final Properties properties = new Properties();
    private final SSHClient ssh = new SSHClient();
    private InternetAddress remoteHost;
    private String username = null; // ssh
    private String password = null; // ssh
    private final RemoteHostKey remoteHostKey = new RemoteHostKey(); // will be populated in open()
    private Timeout remoteTimeout = null; // leave null to specify no timeout; set to any positive value to specify a timeout in seconds
    
    /**
     *
     * @param ctx, must include serverAddress
     */
    public RemoteSetup(SetupContext ctx) {
        this.ctx = ctx;
    }
    
    public void setRemoteHost(InternetAddress address) { this.remoteHost = address; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRemoteHostTimeout(Timeout timeout) { remoteTimeout = timeout; }
    public RemoteHostKey getRemoteHostKey() { return remoteHostKey; }
    
    /**
     * Precondition: remoteHost, username, and password are set and username/password are valid credentials for remoteHost
     * Postcondition: ssh connection to remoteHost is open and remoteHostKey is set and ready for verification
     * @throws IOException 
     */
    public void open() throws IOException {
        RemoteHostKeyDeferredVerifier hostKeyVerifier = new RemoteHostKeyDeferredVerifier(remoteHostKey);
        ssh.addHostKeyVerifier(hostKeyVerifier); // this accepts all remote public keys, then you have to verify the remote host key before continuing!!!
        ssh.connect(remoteHost.toString());
        ssh.authPassword(username, password);         // throws UserAuthException (extends SSHException extends IOException) if credentials don't work
    }
    
    /**
     * It is always safe to call this method.
     * If there is an ssh conection open to the remoteHost, it will be closed.
     * @throws IOException if there was an error closing the ssh connection
     */
    @Override
    public void close() throws IOException {
        ssh.disconnect();        
    }
    
    public static class RemoteHostKey {
        public String server;
        public int port;
        public PublicKey publicKey;
    }
    
    
    /**
     * This verifier always succeeds, so the connection is made, but it saves
     * the remote key information so the user can verify it later and then
     * (hopefully) terminate the connection if the key did not check out.
     * This is useful in conjunction with a UI which displays the remote host
     * key and asks the user to verify. We need to save that key for the UI
     * to display, without driving the UI from here.
     * You can either provide a reference to your own RemoteHostKey object which
     * will be populated, or you can use the non-arg constructor and then call
     * getRemoteHostKey() to get a new populated object.
     */
    public static class RemoteHostKeyDeferredVerifier implements HostKeyVerifier {
        private RemoteHostKey remoteHostKey;
        public RemoteHostKeyDeferredVerifier() {
            this.remoteHostKey = new RemoteHostKey();
        }
        public RemoteHostKeyDeferredVerifier(RemoteHostKey remoteHostKey) {
            this.remoteHostKey = remoteHostKey;
        }
        @Override
        public boolean verify(String string, int i, PublicKey pk) {
            remoteHostKey.server = string;
            remoteHostKey.port = i;
            remoteHostKey.publicKey = pk;
            return true;
        }
        public RemoteHostKey getRemoteHostKey() { return remoteHostKey; }
    }
    
    /**
     * 
     * @param path
     * @return
     * @throws IOException which could also be a ConnectionException or  TransportException
     */
    private boolean existsRemoteFile(String remotePath) throws IOException {
        String result = SshUtil.remote(ssh, "ls -1 "+remotePath+" 2>/dev/null", remoteTimeout); 
        if( result == null || result.isEmpty() ) { return false; }
        return true;
    }
    
    public static class MemoryDstFile extends InMemoryDestFile {
        private ByteArrayOutputStream out = new ByteArrayOutputStream();
        @Override
        public OutputStream getOutputStream() throws IOException {
            return out;
        }
        public byte[] toByteArray() { return out.toByteArray(); }
        public InputStream getInputStream() { return new ByteArrayInputStream(toByteArray()); }
    }

    public static class MemorySrcFile extends InMemorySourceFile {
        private String name;
        private byte[] array;

        public MemorySrcFile(byte[] data) {
            name = "File"+this.hashCode();
            array = data;
        }
        public MemorySrcFile(String filename, byte[] data) {
            name = filename;
            array = data;
        }
        
        public byte[] toByteArray() { return array; }
        
        @Override
        public InputStream getInputStream() { return new ByteArrayInputStream(array); }

        @Override
        public String getName() { return name; }

        @Override
        public long getLength() { return array.length; }
    }

    private MemoryDstFile downloadRemoteFile(String remotePath) throws IOException {
        try {
            MemoryDstFile file = new MemoryDstFile();
            ssh.newSCPFileTransfer().download(remotePath, file);
            return file;
        }
        catch(SCPException e) {
            throw new FileNotFoundException(e.toString());
        }
    }

    /**
     * 
     * @param data content the file to upload
     * @param remotePath including the filename
     * @throws IOException 
     */
    private void uploadLocalFile(byte[] data, String remotePath) throws IOException {
        try {
            MemorySrcFile file = new MemorySrcFile(data);
            ssh.newSCPFileTransfer().upload(file, remotePath);
        }
        catch(SCPException e) {
            throw e;//throw new FileNotFoundException(e.toString());
        }
    }
    
    /**
     * Populates the SetupContext with information from Mt Wilson configuration and the environment such as  which web application container is being used
     * 
     * currently works with mt wilson version 1.0-RC1, 1.0-RC2, and 1.1,  that have separate files attestation-service.properties, management-service.properties, etc.
     * 
     * @throws IOException 
     */
    public void getRemoteSettings() throws IOException {
        ctx.serverAddress = remoteHost;

        // we download the mtwilson.properties file if it's there, but we don't process it directly - we provide it as a default for the service-specific functions
        Properties mwprops = new Properties();
        String mtwilsonPropertiesFilePath = "/etc/intel/cloudsecurity/mtwilson.properties";
        boolean hasMWPropertiesFile = existsRemoteFile(mtwilsonPropertiesFilePath);
        if(hasMWPropertiesFile) {
                MemoryDstFile file = downloadRemoteFile(mtwilsonPropertiesFilePath);
                mwprops.load(file.getInputStream());
        }
        
        String attestationServicePropertiesFilePath = "/etc/intel/cloudsecurity/attestation-service.properties";
        boolean hasASPropertiesFile = existsRemoteFile(attestationServicePropertiesFilePath);
        if(hasASPropertiesFile) {
                MemoryDstFile file = downloadRemoteFile(attestationServicePropertiesFilePath);
                Properties asprops = new Properties();
                asprops.load(file.getInputStream());
//                asprops.store(System.out, "Attestation Service Properties"); // for debugging only
                importAttestationServiceProperties(asprops, mwprops);
        }

        String managementServicePropertiesFilePath = "/etc/intel/cloudsecurity/management-service.properties";
        boolean hasMSPropertiesFile = existsRemoteFile(managementServicePropertiesFilePath);
        if(hasMSPropertiesFile) {
                MemoryDstFile file = downloadRemoteFile(managementServicePropertiesFilePath);
                Properties asprops = new Properties();
                asprops.load(file.getInputStream());
//                asprops.store(System.out, "Management Service Properties"); // for debugging only
                importManagementServiceProperties(asprops, mwprops);
        }
        
        String auditPropertiesFilePath = "/etc/intel/cloudsecurity/audit-handler.properties";
        boolean hasAuditPropertiesFile = existsRemoteFile(auditPropertiesFilePath);
        if(hasAuditPropertiesFile) {
                MemoryDstFile file = downloadRemoteFile(auditPropertiesFilePath);
                Properties asprops = new Properties();
                asprops.load(file.getInputStream());
//                asprops.store(System.out, "Audit Properties"); // for debugging only
                importAuditHandlerProperties(asprops, mwprops);
        }

        String managementConsolePropertiesFilePath = "/etc/intel/cloudsecurity/management-console.properties";
        boolean hasManagementConsolePropertiesFile = existsRemoteFile(managementConsolePropertiesFilePath);
        if(hasManagementConsolePropertiesFile) {
                MemoryDstFile file = downloadRemoteFile(managementConsolePropertiesFilePath);
                Properties asprops = new Properties();
                asprops.load(file.getInputStream());
//                asprops.store(System.out, "Management Console Properties"); // for debugging only
                importManagementConsoleProperties(asprops, mwprops);
        }
        
        String privacyCAPropertiesFilePath = "/etc/intel/cloudsecurity/PrivacyCA.properties";
        boolean hasPrivacyCAPropertiesFile = existsRemoteFile(privacyCAPropertiesFilePath);
        if(hasPrivacyCAPropertiesFile) {
                MemoryDstFile file = downloadRemoteFile(privacyCAPropertiesFilePath);
                Properties asprops = new Properties();
                asprops.load(file.getInputStream());
//                asprops.store(System.out, "Management Console Properties"); // for debugging only
                importPrivacyCAProperties(asprops, mwprops);
        }
        
        
        detectWebContainerType();
        
    }
    
    /**
     * Precondition:   SetupContext.rootCa is set
     */
    public void deployRootCACertToServer() throws IOException, CertificateEncodingException {
//        MemorySrcFile caCertDer = new MemorySrcFile();
        uploadLocalFile(ctx.rootCa.getCertificate().getEncoded(), "/etc/intel/cloudsecurity/MtWilsonRootCA.crt");
        String pem = X509Util.encodePemCertificate(ctx.rootCa.getCertificate());
        uploadLocalFile(pem.getBytes(), "/etc/intel/cloudsecurity/MtWilsonRootCA.crt.pem"); // we also do a PEM version because if customer wants to bring our entire certificate hierarchy under theirs they need to sign our root ca with their certificate - and then append their certificate to this PEM file.
   }
    
    
    public void downloadSamlCertFromServer() throws IOException  {
        if( ctx.samlCertificateFile == null ) {
            fault("Cannot download SAML cert from server: location is not configured");
            return;
        }
        boolean hasSamlCertFile = existsRemoteFile(ctx.samlCertificateFile);
        if( !hasSamlCertFile ) {
            // SAML cert file is not there;
            fault("Cannot download SAML cert from server: file is missing");
            return;
        }
        MemoryDstFile file = downloadRemoteFile(ctx.samlCertificateFile);
        InputStream in = file.getInputStream();
        String fileContent = IOUtils.toString(in);
        IOUtils.closeQuietly(in);
        try {
            ctx.samlCertificate = X509Util.decodePemCertificate(fileContent); 
        }
        catch(CertificateException e) {
            fault(e, "Cannot read SAML certificate");
        }
    }
    
    /**
     * Precondition:  ctx.rootCa and ctx.samlCertificate are set
     */
    public void signSamlCertWithCaCert() {
        X509Builder x509 = X509Builder.factory()
                .subjectName(X500Name.asX500Name(ctx.samlCertificate.getSubjectX500Principal()))
                .subjectPublicKey(ctx.samlCertificate.getPublicKey())
                .expires(3650, TimeUnit.DAYS)
                .issuer(ctx.rootCa)
                .keyUsageDigitalSignature();
        if( ctx.serverAddress.isHostname() ) {
            x509.dnsAlternativeName(ctx.serverAddress.toString());
        }
        else {
            x509.ipAlternativeName(ctx.serverAddress.toString());
        }
        X509Certificate newSamlCert = x509.build();
        if( newSamlCert == null ) {
            fault(x509, "Cannot create a new SAML certificate");
            return;
        }
        ctx.samlCertificate = newSamlCert;
    }
    
    public void uploadSamlCertToServer() throws CertificateEncodingException, IOException {
//        uploadLocalFile(ctx.samlCertificate.getEncoded(), ctx.samlCertificateFile);   // not needed 
        String pem = X509Util.encodePemCertificate(ctx.samlCertificate)+X509Util.encodePemCertificate(ctx.rootCa.getCertificate());
        uploadLocalFile(pem.getBytes(), ctx.samlCertificateFile);
    }

    
    /**
     * @throws IOException 
     */
    public void downloadTlsCertFromServer() throws IOException  {
        if( ctx.tlsCertificateFile == null ) {
            fault("Cannot download TLS cert from server: location is not configured");
            return;
        }
        boolean hasTlsCertFile = existsRemoteFile(ctx.tlsCertificateFile);
        if( !hasTlsCertFile ) {
            // SAML cert file is not there; 
            fault("Cannot download TLS cert from server: file is missing");
            return;
        }
        MemoryDstFile file = downloadRemoteFile(ctx.tlsCertificateFile);
        InputStream in = file.getInputStream();
//        byte[] fileContent = IOUtils.toByteArray(in);
        String fileContent = IOUtils.toString(in);
        IOUtils.closeQuietly(in);
        try {
            ctx.tlsCertificate = X509Util.decodePemCertificate(fileContent);
        }
        catch(CertificateException e) {
            fault(e, "Cannot read TLS certificate");
        }
    }
    
    /**
     * For Java Web Containers (Glassfish, Tomcat)  downloads the keystore.jks
     * file and opens it using the known password to obtain the current TLS
     * certificate.  This method can be used INSTEAD OF downloadTlsCertFromServer.
     * 
     * 
     * @throws IOException 
     */
    public void downloadTlsKeystoreFromServer() throws IOException  {
        if( ctx.tlsKeystoreFile == null ) {
            fault("Cannot download TLS keystore from server: location is not configured");
            return;
        }
        boolean hasTlsKeystoreFile = existsRemoteFile(ctx.tlsKeystoreFile);
        if( !hasTlsKeystoreFile ) {
            // SAML cert file is not there; 
            fault("Cannot download TLS keystore from server: file is missing");
            return;
        }
        MemoryDstFile file = downloadRemoteFile(ctx.tlsKeystoreFile);
        InputStream in = file.getInputStream();
        byte[] fileContent = IOUtils.toByteArray(in);
        IOUtils.closeQuietly(in);
        try {
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            ctx.tlsKeystore = new SimpleKeystore(resource, ctx.tlsKeystorePassword);
            ctx.tlsKeyPassword = ctx.tlsKeystorePassword;
            if( ctx.tlsKeyAlias == null && ArrayUtils.contains(ctx.tlsKeystore.aliases(), "s1as") ) {
                ctx.tlsKeyAlias = "s1as";
            }
            if( ctx.tlsKeyAlias == null && ArrayUtils.contains(ctx.tlsKeystore.aliases(), "glassfish-instance") ) {
                ctx.tlsKeyAlias = "glassfish-instance";
            }
            if( ctx.tlsKeyAlias != null ) {
                RsaCredentialX509 x509 = ctx.tlsKeystore.getRsaCredentialX509(ctx.tlsKeyAlias, ctx.tlsKeyPassword);
                ctx.tlsKeypair = new KeyPair(x509.getPublicKey(), x509.getPrivateKey());
                ctx.tlsCertificate = x509.getCertificate();
            }
        }
        catch(Exception e) { // KeyManagementException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateEncodingException
            fault(e, "Cannot read TLS keystore");
        }
    }
    
    /**
     * Precondition:  ctx.rootCa and ctx.tlsCertificate are set
     * 
     * Glassfish default TLS certificate has a name like "localhost-instance" which of
     * course doesn't match either the IP or any DNS name that may be assigned.
     * So when we sign the certificate we set an additional common name to the server address
     * if the original one doesn't already match.
     */
    public void signTlsCertWithCaCert() {
        String subjectName = ctx.tlsCertificate.getSubjectX500Principal().getName();
        if( !subjectName.contains("CN="+ctx.serverAddress.toString())) {
            subjectName = "CN="+ctx.serverAddress.toString()+","+subjectName;
        }
        X509Builder x509 = X509Builder.factory()
                .subjectName(subjectName) // X500Name.asX500Name(ctx.tlsCertificate.getSubjectX500Principal()))
                .subjectPublicKey(ctx.tlsCertificate.getPublicKey())
                .expires(3650, TimeUnit.DAYS)
                .issuer(ctx.rootCa)
                .keyUsageKeyEncipherment()
                .keyUsageDataEncipherment()
                .keyUsageDigitalSignature();
        if( ctx.serverAddress.isHostname() ) {
            x509.dnsAlternativeName(ctx.serverAddress.toString());
        }
        else {
            x509.ipAlternativeName(ctx.serverAddress.toString());
        }
        X509Certificate newTlsCert = x509.build();
        if( newTlsCert == null ) {
            fault(x509, "Cannot create a new TLS certificate");
            return;
        }
        ctx.tlsCertificate = newTlsCert;
    }
    
    /**
     * @throws CertificateEncodingException
     * @throws IOException 
     */
    public void uploadTlsCertToServer() throws CertificateEncodingException, IOException {
//        uploadLocalFile(ctx.tlsCertificate.getEncoded(), ctx.tlsCertificateFile);  // not needed,  see uploadTlsKeystoreToServer  instead because the cert in the keystore is what the web application server reads
        String pem = X509Util.encodePemCertificate(ctx.tlsCertificate)+X509Util.encodePemCertificate(ctx.rootCa.getCertificate());
        uploadLocalFile(pem.getBytes(), ctx.tlsCertificateFile); // tlsCertificateFile should point to the .pem version anyway
    }
    
    
    /**
     * This method saves ctx.tlsCertificate into the tls.tlsKeystore, and then
     * uploads the modified keystore to the server.  
     * It also saves tls.rootCa certificate into the keystore as an additional
     * trusted certificate.
     * 
     */
    public void uploadTlsKeystoreToServer() throws KeyManagementException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        ctx.tlsKeystore.addTrustedCaCertificate(ctx.rootCa.getCertificate(), "mtwilson-rootca");
        ctx.tlsKeystore.addKeyPairX509(ctx.tlsKeypair.getPrivate(), ctx.tlsCertificate, ctx.tlsKeyAlias, ctx.tlsKeyPassword); 
//        ctx.tlsKeystore.addTrustedCertificate(ctx.tlsCertificate, ctx.tlsKeyAlias); // can't use it because glassfish stores the cert along witht the private key, so we need to replace the entire entry, which is password protected
        ctx.tlsKeystore.save(); // saves to bytearrayresource we created when downloaded it
        uploadLocalFile(IOUtils.toByteArray(ctx.tlsKeystore.getResource().getInputStream()), ctx.tlsKeystoreFile);        
    }
    
    /**
     * Downloads admin portal user keystore, adds the new TLS certificate and Root CA certificate, and uploads it again.
     * This cannot be done for *all* keystores because the keystore password is required and only the user knows it.
     * The operator of this tool will know the admin user password so he can update that user easily, and then be able
     * to login again and approve new users etc.  For all other users, we need a feature in the UI to allow them to
     * accept the new server cert after it has been modified -  show that screen when we show "peer not authenticated".
     */
    public void addTlsAndRootCaToAdminPortalUserKeystore() throws IOException {
        
    }
    //commenting out unused function (6/11 1.2)
    /*
    private List<String> listRemoteFiles(String remotePath) throws IOException {
        String result = SshUtil.remote(ssh, "ls -1 "+remotePath+" 2>/dev/null", remoteTimeout); //  using this only internally (no for arbitrary user input) so this should be ok, but it is a good idea to shell-escape it anyway
        if( result == null || result.isEmpty() ) { return Collections.EMPTY_LIST; }
        ArrayList<String> list = new ArrayList<String>();
        String[] lines = StringUtils.split(result, "\n");
        for(String line : lines) {
            line = line.trim();
            if( !line.isEmpty() ) { list.add(line); }
        }
        return list;
    }
    */
    
    public void downloadPrivacyCaKeystoreFromServer() throws IOException {
        if( ctx.privacyCA.ekSigningKeyFilename == null ) {
            fault("Cannot download PrivacyCA keystore from server: location is not configured");
            return;
        }
        boolean hasPcaKeystoreFile = existsRemoteFile(ctx.privacyCA.ekSigningKeyFilename);
        if( !hasPcaKeystoreFile ) {
            fault("Cannot download PrivacyCA keystore from server: file is missing");
            return;
        }
        MemoryDstFile file = downloadRemoteFile(ctx.privacyCA.ekSigningKeyFilename);
        InputStream in = file.getInputStream();
        byte[] fileContent = IOUtils.toByteArray(in);
        IOUtils.closeQuietly(in);
        try {
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            ctx.privacyCA.keystore = new Pkcs12(resource, ctx.privacyCA.ekSigningKeyPassword);
            // pkcs12 files either don't have aliases or the aliases are just the key index; either way for the PrivacyCA.p12 file that mt wilson generates, the alias name is "1"
//                ctx.tlsCertificate = ctx.tlsKeystore.getX509Certificate(ctx.tlsKeyAlias); // doesn't work, because the certificate is protected by a password since it's part of a privatekeyeentry  ...  assuming Glassfish *and* assuming default TLS alias 
            RsaCredentialX509 x509 = ctx.privacyCA.keystore.getRsaCredentialX509("1", ctx.privacyCA.ekSigningKeyPassword);
            ctx.privacyCA.ekSigningKeyPair = new KeyPair(x509.getPublicKey(), x509.getPrivateKey());
            ctx.privacyCA.ekSigningKeyCertificate = x509.getCertificate();
        }
        catch(Exception e) { // KeyManagementException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateEncodingException
            fault(e, "Cannot read Privacy CA keystore");
        }
        
    }
    
    public void signPrivacyCaCertWithRootCaCert() {
        if( ctx.privacyCA.ekSigningKeyCertificate == null ) {
            fault("Cannot sign Privacy CA EK Signing Key Certificate: missing certificate");
            return;
        }
        X509Builder x509 = X509Builder.factory()
                .subjectName(X500Name.asX500Name(ctx.privacyCA.ekSigningKeyCertificate.getSubjectX500Principal()))
                .subjectPublicKey(ctx.privacyCA.ekSigningKeyCertificate.getPublicKey())
                .expires(3650, TimeUnit.DAYS)
                .issuer(ctx.rootCa)
                .keyUsageCertificateAuthority();
        if( ctx.serverAddress.isHostname() ) {
            x509.dnsAlternativeName(ctx.serverAddress.toString());
        }
        else {
            x509.ipAlternativeName(ctx.serverAddress.toString());
        }
        X509Certificate newPcaCert = x509.build();
        if( newPcaCert == null ) {
            fault(x509, "Cannot create a new TLS certificate");
            return;
        }
        ctx.privacyCA.ekSigningKeyCertificate = newPcaCert;
        
    }
    
    public void uploadPrivacyCaKeystoreToServer() throws IOException, KeyStoreException, CertificateEncodingException, NoSuchAlgorithmException, KeyManagementException, CryptographyException {
        if( ctx.privacyCA.ekSigningKeyPair == null ) {
            fault("Cannot upload Privacy CA Keystore to server: missing EK Signing Key Private Key");
            return;
        }
        if( ctx.privacyCA.ekSigningKeyCertificate == null ) {
            fault("Cannot upload Privacy CA Keystore to server: missing EK Signing Key Certificate");
            return;
        }
        if( ctx.privacyCA.keystore == null ) {
            fault("Cannot upload Privacy CA Keystore to server: missing keystore");
            return;
        }
        RsaCredentialX509 x509 = new RsaCredentialX509(ctx.privacyCA.ekSigningKeyPair.getPrivate(), ctx.privacyCA.ekSigningKeyCertificate);
            ctx.privacyCA.keystore.setRsaCredentialX509(x509, new X509Certificate[] { ctx.rootCa.getCertificate() }, "1", ctx.privacyCA.ekSigningKeyPassword); 
        ctx.privacyCA.keystore.save(); // saves to bytearrayresource we created when downloaded it
        uploadLocalFile(IOUtils.toByteArray(ctx.privacyCA.keystore.getResource().getInputStream()), ctx.privacyCA.ekSigningKeyFilename);        
        String pem = X509Util.encodePemCertificate(ctx.privacyCA.ekSigningKeyCertificate)+X509Util.encodePemCertificate(ctx.rootCa.getCertificate());
        uploadLocalFile(pem.getBytes(), ctx.privacyCA.ekSigningKeyFilename+".pem");
    }
    
    
    /**
     * Call this before importing management service properties, because AS database settings take precedence over MS datatabase settings if they are the same 
     */
    private void importAttestationServiceProperties(Properties asprops, Properties mwprops) {
        ctx.attestationServiceDatabase = new Database();
        ctx.attestationServiceDatabase.driver = asprops.getProperty("mountwilson.as.db.driver", asprops.getProperty("mtwilson.db.driver", mwprops.getProperty("mtwilson.db.driver"))); 
        ctx.attestationServiceDatabase.username = asprops.getProperty("mountwilson.as.db.user", asprops.getProperty("mtwilson.db.user", mwprops.getProperty("mtwilson.db.user")));
        ctx.attestationServiceDatabase.password = asprops.getProperty("mountwilson.as.db.password", asprops.getProperty("mtwilson.db.password", mwprops.getProperty("mtwilson.db.password")));
        ctx.attestationServiceDatabase.schema = asprops.getProperty("mountwilson.as.db.schema", asprops.getProperty("mtwilson.db.schema", mwprops.getProperty("mtwilson.db.driver", "mw_as")));
        if( asprops.getProperty("mountwilson.as.db.host", asprops.getProperty("mtwilson.db.host", mwprops.getProperty("mtwilson.db.host"))) != null ) {
            ctx.attestationServiceDatabase.hostname = new InternetAddress(asprops.getProperty("mountwilson.as.db.host", asprops.getProperty("mtwilson.db.host", mwprops.getProperty("mtwilson.db.host")))); 
            // If database hostname is 127.0.0.1 or localhost, we need to rewrite it as target server address in order to access it
            if( ctx.attestationServiceDatabase.hostname.toString().equals("127.0.0.1") ||  ctx.attestationServiceDatabase.hostname.toString().equals("localhost") ) {
                ctx.attestationServiceDatabase.hostname = new InternetAddress(ctx.serverAddress.toString());
            }
        }                    
        if(asprops.getProperty("mountwilson.as.db.port", asprops.getProperty("mtwilson.db.port", mwprops.getProperty("mtwilson.db.port")))!=null) {
            ctx.attestationServiceDatabase.port = Integer.valueOf(asprops.getProperty("mountwilson.as.db.port", asprops.getProperty("mtwilson.db.port", mwprops.getProperty("mtwilson.db.port"))));
        }
        if( ctx.attestationServiceDatabase.type == null && ctx.attestationServiceDatabase.driver != null ) {
            ctx.attestationServiceDatabase.type = DatabaseType.fromDriver(ctx.attestationServiceDatabase.driver); 
        }
        if( ctx.attestationServiceDatabase.type == null && ctx.attestationServiceDatabase.port != null ) {
            ctx.attestationServiceDatabase.type = DatabaseType.fromPort(ctx.attestationServiceDatabase.port); 
        }


        ctx.aikqverifyHome = asprops.getProperty("com.intel.mountwilson.as.home"); // such as "/var/opt/intel/aikverifyhome", but we cannot set default here, same detection issue as the command names.
        ctx.aikqverifyCommand = asprops.getProperty("com.intel.mountwilson.as.aikqverify.cmd"); // even if we wanted to set a default here, we have to use the TARGET SERVER platform choice, not our local choice (since we are likely running on windows and server is likely linux)
        ctx.opensslCommand = asprops.getProperty("com.intel.mountwilson.as.openssl.cmd"); // same issue as aikqverify

        ctx.samlKeyAlias = asprops.getProperty("saml.key.alias", "samlkey1");
        ctx.samlKeyPassword = asprops.getProperty("saml.key.password", "changeit");
        ctx.samlKeystoreFile = asprops.getProperty("saml.keystore.file", "SAML.jks");
        if( !ctx.samlKeystoreFile.startsWith("/") ) {
            ctx.samlKeystoreFile = String.format("/etc/intel/cloudsecurity/%s", ctx.samlKeystoreFile);
        }
        ctx.samlKeystorePassword = asprops.getProperty("saml.keystore.password", "changeit");
        ctx.samlValidityPeriodInSeconds = Integer.valueOf(asprops.getProperty("saml.validity.seconds", "3600")); // in seconds
        ctx.samlIssuer = asprops.getProperty("saml.issuer", ctx.serverAddress.toString()); 

        if( asprops.getProperty("com.intel.mountwilson.as.trustagent.timeout") != null ) {
            ctx.trustAgentTimeout = new Timeout(Integer.valueOf(asprops.getProperty("com.intel.mountwilson.as.trustagent.timeout")), TimeUnit.SECONDS);
        }

        if( ctx.privacyCA == null ) { // importPrivacyCAProperties may have already created it
            ctx.privacyCA = new PrivacyCA();
        }
        if( asprops.getProperty("privacyca.server", mwprops.getProperty("privacyca.server")) != null ) {
            ctx.privacyCA.hostname = new InternetAddress(asprops.getProperty("privacyca.server", mwprops.getProperty("privacyca.server")));
            // If privacy ca hostname is 127.0.0.1 or localhost, we need to rewrite it as target server address in order to access it
            if( ctx.privacyCA.hostname.toString().equals("127.0.0.1") ||  ctx.privacyCA.hostname.toString().equals("localhost") ) {
                ctx.privacyCA.hostname = new InternetAddress(ctx.serverAddress.toString());
            }
        }

        // important:  encrypts the connection strings in the database.
        ctx.dataEncryptionKeyBase64 = asprops.getProperty("mtwilson.as.dek");
        
    }
    
    
    private void importManagementServiceProperties(Properties msprops, Properties mwprops) {
        ctx.managementServiceDatabase = new Database();
        ctx.managementServiceDatabase.driver = msprops.getProperty("mountwilson.ms.db.driver", msprops.getProperty("mtwilson.db.driver", mwprops.getProperty("mtwilson.db.driver"))); 
        ctx.managementServiceDatabase.username = msprops.getProperty("mountwilson.ms.db.user", msprops.getProperty("mtwilson.db.user", mwprops.getProperty("mtwilson.db.user")));
        ctx.managementServiceDatabase.password = msprops.getProperty("mountwilson.ms.db.password", msprops.getProperty("mtwilson.db.password", mwprops.getProperty("mtwilson.db.password")));
        ctx.managementServiceDatabase.schema = msprops.getProperty("mountwilson.ms.db.schema", msprops.getProperty("mtwilson.db.schema", mwprops.getProperty("mtwilson.db.schema", "mw_as")));
        if( msprops.getProperty("mountwilson.ms.db.host", msprops.getProperty("mtwilson.db.host", mwprops.getProperty("mtwilson.db.host"))) != null ) {
            ctx.managementServiceDatabase.hostname = new InternetAddress(msprops.getProperty("mountwilson.ms.db.host", msprops.getProperty("mtwilson.db.host", mwprops.getProperty("mtwilson.db.host")))); 
            // If database hostname is 127.0.0.1 or localhost, we need to rewrite it as target server address in order to access it
            if( ctx.managementServiceDatabase.hostname.toString().equals("127.0.0.1") ||  ctx.managementServiceDatabase.hostname.toString().equals("localhost") ) {
                ctx.managementServiceDatabase.hostname = new InternetAddress(ctx.serverAddress.toString());
            }
        }                    
        if(msprops.getProperty("mountwilson.ms.db.port", msprops.getProperty("mtwilson.db.port", mwprops.getProperty("mtwilson.db.port")))!=null) {
            ctx.managementServiceDatabase.port = Integer.valueOf(msprops.getProperty("mountwilson.ms.db.port", msprops.getProperty("mtwilson.db.port", mwprops.getProperty("mtwilson.db.port"))));
        }
        if( ctx.managementServiceDatabase.type == null && ctx.managementServiceDatabase.driver != null ) {
            ctx.managementServiceDatabase.type = DatabaseType.fromDriver(ctx.managementServiceDatabase.driver); 
        }
        if( ctx.managementServiceDatabase.type == null && ctx.managementServiceDatabase.port != null ) {
            ctx.managementServiceDatabase.type = DatabaseType.fromPort(ctx.managementServiceDatabase.port); 
        }

        if( msprops.getProperty("mtwilson.api.baseurl", mwprops.getProperty("mtwilson.api.baseurl")) != null ) {
            try {
                ctx.serverUrl = new URL(msprops.getProperty("mtwilson.api.baseurl", mwprops.getProperty("mtwilson.api.baseurl")));
            }
            catch(MalformedURLException e) {
                console.printf("Ignoring MalformedURL exception caught during importManagmentServiceProperties");
            }
        }
        
        ctx.automationKeyAlias = msprops.getProperty("mtwilson.api.key.alias");
        ctx.automationKeyPassword = msprops.getProperty("mtwilson.api.key.password");
        ctx.automationKeystoreFile = msprops.getProperty("mtwilson.api.keystore");
        
        /*
         * 
        // here we are mapping the old ssl policy implementation to the new one. the mapping is not perfect but we try to capture the best intent.  specifically, "requireTrustedCertificate" can either mean a CA or a specific self-signed host certificate. we assume a CA.
        boolean automationVerifyHostname = Boolean.valueOf(msprops.getProperty("mtwilson.api.ssl.verifyHostname", mwprops.getProperty("mtwilson.api.ssl.verifyHostname", "true")));
        boolean automationRequireTrustedCertificate = Boolean.valueOf(msprops.getProperty("mtwilson.api.ssl.requireTrustedCertificate", mwprops.getProperty("mtwilson.api.ssl.requireTrustedCertificate", "true")));
        if( automationVerifyHostname && automationRequireTrustedCertificate) {
            ctx.automationTlsPolicy = TLSPolicy.TRUST_CA_VERIFY_HOSTNAME;
        }
        else if( automationVerifyHostname && !automationRequireTrustedCertificate ) {
            ctx.automationTlsPolicy = TLSPolicy.TRUST_FIRST_CERTIFICATE;
        }
        else {
            ctx.automationTlsPolicy = TLSPolicy.INSECURE;
        }
        */
            
        // these security parameters actually apply to all services
        ctx.securityPolicy = new WebServiceSecurityPolicy();
        ctx.securityPolicy.isTlsRequired = Boolean.valueOf(msprops.getProperty("mtwilson.ssl.required", mwprops.getProperty("mtwilson.ssl.required", "true")));
        ctx.securityPolicy.trustedClients = StringUtils.split(msprops.getProperty("mtwilson.api.trust", mwprops.getProperty("mtwilson.api.trust", "")), ',');
        
        ctx.managementServiceKeystoreDir = msprops.getProperty("mtwilson.ms.keystore.dir");

        ctx.biosPCRs = msprops.getProperty("mtwilson.ms.biosPCRs", "0;17");
        ctx.vmmPCRs = msprops.getProperty("mtwilson.ms.vmmPCRs", "18;19;20");
        
        ctx.samlCertificateFile = msprops.getProperty("mtwilson.saml.certificate.file", mwprops.getProperty("mtwilson.saml.certificate.file") ); // dropping support for   mtwilson.saml.certificate (DER format) that was in management-service.properties
        if( !ctx.samlCertificateFile.startsWith("/") ) {
            ctx.samlCertificateFile = String.format("/etc/intel/cloudsecurity/%s", ctx.samlCertificateFile);
        }
        
        
    }
    
    private void importAuditHandlerProperties(Properties ahprops, Properties mwprops) {
        ctx.auditDatabase = new Database();
        ctx.auditDatabase.driver = ahprops.getProperty("mountwilson.audit.db.driver", ahprops.getProperty("mtwilson.db.driver", mwprops.getProperty("mtwilson.db.driver"))); 
        ctx.auditDatabase.username = ahprops.getProperty("mountwilson.audit.db.user", ahprops.getProperty("mtwilson.db.user", mwprops.getProperty("mtwilson.db.user")));
        ctx.auditDatabase.password = ahprops.getProperty("mountwilson.audit.db.password", ahprops.getProperty("mtwilson.db.password", mwprops.getProperty("mtwilson.db.password")));
        ctx.auditDatabase.schema = ahprops.getProperty("mountwilson.audit.db.schema", ahprops.getProperty("mtwilson.db.schema", mwprops.getProperty("mtwilson.db.schema", "mw_as")));
        if( ahprops.getProperty("mountwilson.audit.db.host", ahprops.getProperty("mtwilson.db.host", mwprops.getProperty("mtwilson.db.host"))) != null ) {
            ctx.auditDatabase.hostname = new InternetAddress(ahprops.getProperty("mountwilson.audit.db.host", ahprops.getProperty("mtwilson.db.host", mwprops.getProperty("mtwilson.db.host")))); 
            // If database hostname is 127.0.0.1 or localhost, we need to rewrite it as target server address in order to access it
            if( ctx.auditDatabase.hostname.toString().equals("127.0.0.1") ||  ctx.auditDatabase.hostname.toString().equals("localhost") ) {
                ctx.auditDatabase.hostname = new InternetAddress(ctx.serverAddress.toString());
            }
        }                    
        if(ahprops.getProperty("mountwilson.audit.db.port", ahprops.getProperty("mtwilson.db.port", mwprops.getProperty("mtwilson.db.port")))!=null) {
            ctx.auditDatabase.port = Integer.valueOf(ahprops.getProperty("mountwilson.audit.db.port", ahprops.getProperty("mtwilson.db.port", mwprops.getProperty("mtwilson.db.port"))));
        }
        if( ctx.auditDatabase.type == null && ctx.auditDatabase.driver != null ) {
            ctx.auditDatabase.type = DatabaseType.fromDriver(ctx.auditDatabase.driver); 
        }
        if( ctx.auditDatabase.type == null && ctx.auditDatabase.port != null ) {
            ctx.auditDatabase.type = DatabaseType.fromPort(ctx.auditDatabase.port); 
        }
        
        ctx.auditEnabled = Boolean.valueOf(ahprops.getProperty("mountwilson.audit.enabled", "true"));
        ctx.auditLogUnchangedColumns = Boolean.valueOf(ahprops.getProperty("mountwilson.audit.logunchangedcolumns", "false"));
        ctx.auditAsync = Boolean.valueOf(ahprops.getProperty("mountwilson.audit.async", "true"));
    }
    
    private void importManagementConsoleProperties(Properties asprops, Properties mwprops) {
        ctx.portalUserKeystoreDir = asprops.getProperty("mtwilson.mc.keystore.dir");
        ctx.portalHostTypeList = asprops.getProperty("mtwilson.mc.hostTypes", "Xen;KVM;VMWare");
        ctx.portalSessionTimeout = new Timeout(Integer.valueOf(asprops.getProperty("mtwilson.portal.sessionTimeOut", "1800")), TimeUnit.SECONDS);
        ctx.portalApiKeyExpirationNotice = new Timeout(Integer.valueOf(asprops.getProperty("mtwilson.mc.apiKeyExpirationNoticeInMonths", "3"))*30, TimeUnit.DAYS); // for example 3 months is stored as 90 days, since the TimeUnit type does not have MONTHS
    }
    
    
    
    private void importPrivacyCAProperties(Properties pcaprops, Properties mwprops) {
        if( ctx.privacyCA == null ) { // importAttestationServiceProperties may have already created it
            ctx.privacyCA = new PrivacyCA();
        }
        
        ctx.privacyCA.ekSigningKeyFilename = pcaprops.getProperty("P12filename", "PrivacyCA.p12"); // probably should not be configurable... should have a specific location.
        if( !ctx.privacyCA.ekSigningKeyFilename.startsWith("/") ) {
            ctx.privacyCA.ekSigningKeyFilename = String.format("/etc/intel/cloudsecurity/%s", ctx.privacyCA.ekSigningKeyFilename);
        }
        ctx.privacyCA.ekSigningKeyPassword = pcaprops.getProperty("P12password");
        ctx.privacyCA.ekSigningKeyDownloadUsername = pcaprops.getProperty("ClientFilesDownloadUsername");
        ctx.privacyCA.ekSigningKeyDownloadPassword = pcaprops.getProperty("ClientFilesDownloadPassword");
        ctx.privacyCA.pcaCertificateValidity = new Timeout(Integer.valueOf(pcaprops.getProperty("PrivCaCertValiditydays", "3652")), TimeUnit.DAYS);
    }
    // commenting out unused function for removal later
    //private void importGlassfishProperties() {
        // hmm... just need location of keystore and exported certificate to replace with our ca-signed copy
    //}
    // commenting out unused function for removal later
    //private void importTomcatProperties() {
    //    // hmm... just need location of keystore and exported certificate to replace with our ca-signed copy
    //}
    
    private void detectWebContainerType() throws IOException {
        String envFilePath = "/opt/intel/cloudsecurity/attestation-service/attestation-service.env";
        boolean hasEnvFile = existsRemoteFile(envFilePath);
        if(hasEnvFile) {
                MemoryDstFile file = downloadRemoteFile(envFilePath);
                Properties asprops = new Properties(); // this works for MOST shell-style variables, and comments are the same as in properties files
                asprops.load(file.getInputStream());
//                asprops.store(System.out, "Attestation Service Environment"); // for debugging only
                if( asprops.getProperty("GLASSFISH_HOME") != null ) {
                    ctx.webContainerType = WebContainerType.GLASSFISH;
                    ctx.tlsKeystoreFile = asprops.getProperty("GLASSFISH_HOME") + "/domains/domain1/config/keystore.jks";
                    ctx.tlsKeystorePassword = "changeit";
                    ctx.tlsCertificateFile = asprops.getProperty("GLASSFISH_HOME") + "/domains/domain1/config/ssl."+ctx.serverAddress.toString()+".crt";
                }
                else if( asprops.getProperty("CATALINA_HOME") != null ) {
                    ctx.webContainerType = WebContainerType.TOMCAT;
                }
        }
        
    }
    
}
