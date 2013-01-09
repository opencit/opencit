/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import com.intel.mtwilson.crypto.Pkcs12;
import com.intel.mtwilson.crypto.RsaCredentialX509;
import com.intel.mtwilson.crypto.RsaUtil;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.crypto.X509Builder;
import com.intel.mtwilson.crypto.X509Util;
import com.intel.mtwilson.datatypes.InternetAddress;
import com.intel.mtwilson.datatypes.Md5Digest;
import com.intel.mtwilson.datatypes.Sha1Digest;
import com.intel.mtwilson.datatypes.TLSPolicy;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mtwilson.setup.model.Database;
import com.intel.mtwilson.setup.model.DatabaseType;
import com.intel.mtwilson.setup.model.PrivacyCA;
import com.intel.mtwilson.setup.model.WebContainerType;
import com.intel.mtwilson.setup.model.WebServiceSecurityPolicy;
import com.intel.mtwilson.validation.BuilderModel;
import com.intel.mtwilson.validation.Fault;
import com.intel.mtwilson.validation.ObjectModel;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Properties;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import sun.security.rsa.RSAPublicKeyImpl;
import sun.security.x509.X509Key;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.util.concurrent.TimeUnit;
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
 * cd /usr/share/glassfish3/glassfish/domains/domain1/config
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
    private final SetupContext ctx;
    private final Properties properties = new Properties();
    private final SSHClient ssh = new SSHClient();
    private InternetAddress remoteHost;
    private String username = null; // ssh
    private String password = null; // ssh
    private RemoteHostKey remoteHostKey = new RemoteHostKey(); // will be populated in open()
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
        String result = SshUtil.remote(ssh, "ls -1 "+remotePath+" 2>/dev/null", remoteTimeout); // XXX using this only internally (no for arbitrary user input) so this should be ok, but it is a good idea to shell-escape it anyway
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
        
        String attestationServicePropertiesFilePath = "/etc/intel/cloudsecurity/attestation-service.properties";
        boolean hasASPropertiesFile = existsRemoteFile(attestationServicePropertiesFilePath);
        if(hasASPropertiesFile) {
                MemoryDstFile file = downloadRemoteFile(attestationServicePropertiesFilePath);
                Properties asprops = new Properties();
                asprops.load(file.getInputStream());
//                asprops.store(System.out, "Attestation Service Properties"); // XXX for debugging only
                importAttestationServiceProperties(asprops);
        }

        String managementServicePropertiesFilePath = "/etc/intel/cloudsecurity/management-service.properties";
        boolean hasMSPropertiesFile = existsRemoteFile(managementServicePropertiesFilePath);
        if(hasMSPropertiesFile) {
                MemoryDstFile file = downloadRemoteFile(managementServicePropertiesFilePath);
                Properties asprops = new Properties();
                asprops.load(file.getInputStream());
//                asprops.store(System.out, "Management Service Properties"); // XXX for debugging only
                importManagementServiceProperties(asprops);
        }
        
        String auditPropertiesFilePath = "/etc/intel/cloudsecurity/audit-handler.properties";
        boolean hasAuditPropertiesFile = existsRemoteFile(auditPropertiesFilePath);
        if(hasAuditPropertiesFile) {
                MemoryDstFile file = downloadRemoteFile(auditPropertiesFilePath);
                Properties asprops = new Properties();
                asprops.load(file.getInputStream());
//                asprops.store(System.out, "Audit Properties"); // XXX for debugging only
                importAuditHandlerProperties(asprops);
        }

        String managementConsolePropertiesFilePath = "/etc/intel/cloudsecurity/management-console.properties";
        boolean hasManagementConsolePropertiesFile = existsRemoteFile(managementConsolePropertiesFilePath);
        if(hasManagementConsolePropertiesFile) {
                MemoryDstFile file = downloadRemoteFile(managementConsolePropertiesFilePath);
                Properties asprops = new Properties();
                asprops.load(file.getInputStream());
//                asprops.store(System.out, "Management Console Properties"); // XXX for debugging only
                importManagementConsoleProperties(asprops);
        }
        
        String privacyCAPropertiesFilePath = "/etc/intel/cloudsecurity/PrivacyCA.properties";
        boolean hasPrivacyCAPropertiesFile = existsRemoteFile(privacyCAPropertiesFilePath);
        if(hasPrivacyCAPropertiesFile) {
                MemoryDstFile file = downloadRemoteFile(privacyCAPropertiesFilePath);
                Properties asprops = new Properties();
                asprops.load(file.getInputStream());
//                asprops.store(System.out, "Management Console Properties"); // XXX for debugging only
                importPrivacyCAProperties(asprops);
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
            // SAML cert file is not there; TODO we could check if the keystore is there and try to extract it, but for now we just abort
            fault("Cannot download SAML cert from server: file is missing");
            return;
        }
        MemoryDstFile file = downloadRemoteFile(ctx.samlCertificateFile);
        InputStream in = file.getInputStream();
        byte[] fileContent = IOUtils.toByteArray(in);
        IOUtils.closeQuietly(in);
        try {
            ctx.samlCertificate = X509Util.decodeDerCertificate(fileContent);
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
        uploadLocalFile(ctx.samlCertificate.getEncoded(), ctx.samlCertificateFile);   
        String pem = X509Util.encodePemCertificate(ctx.samlCertificate)+X509Util.encodePemCertificate(ctx.rootCa.getCertificate());
        uploadLocalFile(pem.getBytes(), ctx.samlCertificateFile+".pem");
    }

    
    /**
     * XXX TODO  this varies by web container / proxy ... for tomcat we need to upload the cert for administrator convenience but for functionality we need to update the java keystore... that might be similar procedure to tomcat but it will not be the same for apache and nginx
     * Right now assuming GLASSFISH,  bu tthis method should be adapted for apache and nginx instead since for glassfish we use the downloadTlsKeystoreFromServer method INSTEAD OF THIS ONE.
     * 
     * @throws IOException 
     */
    public void downloadTlsCertFromServer() throws IOException  {
        if( ctx.tlsCertificateFile == null ) {
            fault("Cannot download TLS cert from server: location is not configured");
            return;
        }
        boolean hasTlsCertFile = existsRemoteFile(ctx.tlsCertificateFile);
        if( !hasTlsCertFile ) {
            // SAML cert file is not there; TODO we could check if the keystore is there and try to extract it, but for now we just abort
            fault("Cannot download TLS cert from server: file is missing");
            return;
        }
        MemoryDstFile file = downloadRemoteFile(ctx.tlsCertificateFile);
        InputStream in = file.getInputStream();
        byte[] fileContent = IOUtils.toByteArray(in);
        IOUtils.closeQuietly(in);
        try {
            ctx.tlsCertificate = X509Util.decodeDerCertificate(fileContent);
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
     * XXX TODO this is web server dependent... should be implemented in object-oriented fashion;  right now assuming GLASSFISH
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
            // SAML cert file is not there; TODO we could check if the keystore is there and try to extract it, but for now we just abort
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
            // XXX hack - using same password for key as for the keystore;  this happens to be the glassfish default but it may not always be true
            ctx.tlsKeyPassword = ctx.tlsKeystorePassword;
            // XXX hack,  glassfish cert is either in "s1as" or in "glassfish-instance" , but it could be anything so we need to grab the actual alias name from the glassfish configuration in getRemoteSetings()
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
     * XXX TODO  this varies by web container / proxy ... for tomcat we need to upload the cert for administrator convenience but for functionality we need to update the java keystore... that might be similar procedure to tomcat but it will not be the same for apache and nginx
     * @throws CertificateEncodingException
     * @throws IOException 
     */
    public void uploadTlsCertToServer() throws CertificateEncodingException, IOException {
        uploadLocalFile(ctx.tlsCertificate.getEncoded(), ctx.tlsCertificateFile);        
        String pem = X509Util.encodePemCertificate(ctx.tlsCertificate)+X509Util.encodePemCertificate(ctx.rootCa.getCertificate());
        uploadLocalFile(pem.getBytes(), ctx.tlsCertificateFile+".pem");
    }
    
    
    /**
     * This method saves ctx.tlsCertificate into the tls.tlsKeystore, and then
     * uploads the modified keystore to the server.  
     * It also saves tls.rootCa certificate into the keystore as an additional
     * trusted certificate.
     * 
     * XXX TODO:  instead of throwing the exceptions, catch them and add faults instead
     */
    public void uploadTlsKeystoreToServer() throws KeyManagementException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        ctx.tlsKeystore.addTrustedCaCertificate(ctx.rootCa.getCertificate(), "mtwilson-rootca");
        ctx.tlsKeystore.addKeyPairX509(ctx.tlsKeypair.getPrivate(), ctx.tlsCertificate, ctx.tlsKeyAlias, ctx.tlsKeyPassword); // XXX TODO  assumes Glassfish default alias; also, do we need to delete the previous one first???
//        ctx.tlsKeystore.addTrustedCertificate(ctx.tlsCertificate, ctx.tlsKeyAlias); // can't use it because glassfish stores the cert along witht the private key, so we need to replace the entire entry, which is password protected
        ctx.tlsKeystore.save(); // saves to bytearrayresource we created when downloaded it
        uploadLocalFile(IOUtils.toByteArray(ctx.tlsKeystore.getResource().getInputStream()), ctx.tlsKeystoreFile);        
    }
        
    
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
//                ctx.tlsCertificate = ctx.tlsKeystore.getX509Certificate(ctx.tlsKeyAlias); // doesn't work, because the certificate is protected by a password since it's part of a privatekeyeentry  ... XXX TODO : assuming Glassfish *and* assuming default TLS alias 
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
    
    public void uploadPrivacyCaKeystoreToServer() throws IOException, KeyStoreException, CertificateEncodingException, NoSuchAlgorithmException, KeyManagementException {
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
        ctx.privacyCA.keystore.setRsaCredentialX509(x509, new X509Certificate[] { ctx.rootCa.getCertificate() }, "1", ctx.privacyCA.ekSigningKeyPassword); // XXX TODO  assumes Glassfish default alias; also, do we need to delete the previous one first???
        ctx.privacyCA.keystore.save(); // saves to bytearrayresource we created when downloaded it
        uploadLocalFile(IOUtils.toByteArray(ctx.privacyCA.keystore.getResource().getInputStream()), ctx.privacyCA.ekSigningKeyFilename);        
        String pem = X509Util.encodePemCertificate(ctx.privacyCA.ekSigningKeyCertificate)+X509Util.encodePemCertificate(ctx.rootCa.getCertificate());
        uploadLocalFile(pem.getBytes(), ctx.privacyCA.ekSigningKeyFilename+".pem");
    }
    
    
    /**
     * Call this before importing management service properties, because AS database settings take precedence over MS datatabase settings if they are the same 
     */
    private void importAttestationServiceProperties(Properties asprops) {
        ctx.attestationServiceDatabase = new Database();
        // XXX TODO should use a differnent class that handles all the defaults etc. that is also used by attestation service.  thought of ASConfig get defaults but even that needs to be rewritten to look for mountwilson.as.db.driver, then mtwilson.db.driver, etc. 
        ctx.attestationServiceDatabase.driver = asprops.getProperty("mountwilson.as.db.driver", asprops.getProperty("mtwilson.db.driver")); 
        ctx.attestationServiceDatabase.username = asprops.getProperty("mountwilson.as.db.user", asprops.getProperty("mtwilson.db.user"));
        ctx.attestationServiceDatabase.password = asprops.getProperty("mountwilson.as.db.password", asprops.getProperty("mtwilson.db.password"));
        ctx.attestationServiceDatabase.schema = asprops.getProperty("mountwilson.as.db.schema", "mw_as");
        if( asprops.getProperty("mountwilson.as.db.host", asprops.getProperty("mtwilson.db.server")) != null ) {
            ctx.attestationServiceDatabase.hostname = new InternetAddress(asprops.getProperty("mountwilson.as.db.host", asprops.getProperty("mtwilson.db.server"))); 
            // If database hostname is 127.0.0.1 or localhost, we need to rewrite it as target server address in order to access it
            if( ctx.attestationServiceDatabase.hostname.toString().equals("127.0.0.1") ||  ctx.attestationServiceDatabase.hostname.toString().equals("localhost") ) {
                ctx.attestationServiceDatabase.hostname = new InternetAddress(ctx.serverAddress.toString());
            }
        }                    
        if(asprops.getProperty("mountwilson.as.db.port", asprops.getProperty("mtwilson.db.port"))!=null) {
            ctx.attestationServiceDatabase.port = Integer.valueOf(asprops.getProperty("mountwilson.as.db.port", asprops.getProperty("mtwilson.db.port")));
        }
        if( ctx.attestationServiceDatabase.type == null && ctx.attestationServiceDatabase.driver != null ) {
            ctx.attestationServiceDatabase.type = DatabaseType.fromDriver(ctx.attestationServiceDatabase.driver); 
        }
        if( ctx.attestationServiceDatabase.type == null && ctx.attestationServiceDatabase.port != null ) {
            ctx.attestationServiceDatabase.type = DatabaseType.fromPort(ctx.attestationServiceDatabase.port); 
        }


        // XXX TODO:  this configuration item should be deprecated. see comments in SetupContext
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
        ctx.samlIssuer = asprops.getProperty("saml.issuer", ctx.serverAddress.toString()); // XXX TODO should not be configurable, should be what is in the saml certificate subject.  unless a URL is required (looks like it), in that case we should create it automatically using server address.

        if( asprops.getProperty("com.intel.mountwilson.as.trustagent.timeout") != null ) {
            ctx.trustAgentTimeout = new Timeout(Integer.valueOf(asprops.getProperty("com.intel.mountwilson.as.trustagent.timeout")), TimeUnit.SECONDS);
        }

        if( ctx.privacyCA == null ) { // importPrivacyCAProperties may have already created it
            ctx.privacyCA = new PrivacyCA();
        }
        if( asprops.getProperty("privacyca.server") != null ) {
            ctx.privacyCA.hostname = new InternetAddress(asprops.getProperty("privacyca.server"));
            // If privacy ca hostname is 127.0.0.1 or localhost, we need to rewrite it as target server address in order to access it
            if( ctx.privacyCA.hostname.toString().equals("127.0.0.1") ||  ctx.privacyCA.hostname.toString().equals("localhost") ) {
                ctx.privacyCA.hostname = new InternetAddress(ctx.serverAddress.toString());
            }
        }

        // important:  encrypts the connection strings in the database.
        ctx.dataEncryptionKeyBase64 = asprops.getProperty("mtwilson.as.dek");
        
    }
    
    
    private void importManagementServiceProperties(Properties asprops) {
        ctx.managementServiceDatabase = new Database();
        // XXX TODO should use a differnent class that handles all the defaults etc. that is also used by attestation service.  thought of ASConfig get defaults but even that needs to be rewritten to look for mountwilson.ms.db.driver, then mtwilson.db.driver, etc. 
        ctx.managementServiceDatabase.driver = asprops.getProperty("mountwilson.ms.db.driver", asprops.getProperty("mtwilson.db.driver")); 
        ctx.managementServiceDatabase.username = asprops.getProperty("mountwilson.ms.db.user", asprops.getProperty("mtwilson.db.user"));
        ctx.managementServiceDatabase.password = asprops.getProperty("mountwilson.ms.db.password", asprops.getProperty("mtwilson.db.password"));
        ctx.managementServiceDatabase.schema = asprops.getProperty("mountwilson.ms.db.schema", "mw_as");
        if( asprops.getProperty("mountwilson.ms.db.host", asprops.getProperty("mtwilson.db.server")) != null ) {
            ctx.managementServiceDatabase.hostname = new InternetAddress(asprops.getProperty("mountwilson.ms.db.host", asprops.getProperty("mtwilson.db.server"))); 
            // If database hostname is 127.0.0.1 or localhost, we need to rewrite it as target server address in order to access it
            if( ctx.managementServiceDatabase.hostname.toString().equals("127.0.0.1") ||  ctx.managementServiceDatabase.hostname.toString().equals("localhost") ) {
                ctx.managementServiceDatabase.hostname = new InternetAddress(ctx.serverAddress.toString());
            }
        }                    
        if(asprops.getProperty("mountwilson.ms.db.port", asprops.getProperty("mtwilson.db.port"))!=null) {
            ctx.managementServiceDatabase.port = Integer.valueOf(asprops.getProperty("mountwilson.ms.db.port", asprops.getProperty("mtwilson.db.port")));
        }
        if( ctx.managementServiceDatabase.type == null && ctx.managementServiceDatabase.driver != null ) {
            ctx.managementServiceDatabase.type = DatabaseType.fromDriver(ctx.managementServiceDatabase.driver); 
        }
        if( ctx.managementServiceDatabase.type == null && ctx.managementServiceDatabase.port != null ) {
            ctx.managementServiceDatabase.type = DatabaseType.fromPort(ctx.managementServiceDatabase.port); 
        }

        if( asprops.getProperty("mtwilson.api.baseurl") != null ) {
            try {
                ctx.serverUrl = new URL(asprops.getProperty("mtwilson.api.baseurl"));
            }
            catch(MalformedURLException e) {
                // XXX TODO: we need to extend ObjectModel so we can log all the faults with the configuration!!!  or maybe this belongs in SetupContext which should extend ObjectModel?  but this means all the interpretation of the prpoerties (converting from string to whatever other datataypes) must havppen inside the validate() method of that object.  which actually sounds just fine.
            }
        }
        
        ctx.automationKeyAlias = asprops.getProperty("mtwilson.api.key.alias");
        ctx.automationKeyPassword = asprops.getProperty("mtwilson.api.key.password");
        ctx.automationKeystoreFile = asprops.getProperty("mtwilson.api.keystore");
        
        // here we are mapping the old ssl policy implementation to the new one. the mapping is not perfect but we try to capture the best intent.  specifically, "requireTrustedCertificate" can either mean a CA or a specific self-signed host certificate. we assume a CA.
        boolean automationVerifyHostname = Boolean.valueOf(asprops.getProperty("mtwilson.api.ssl.verifyHostname", "true"));
        boolean automationRequireTrustedCertificate = Boolean.valueOf(asprops.getProperty("mtwilson.api.ssl.requireTrustedCertificate", "true"));
        if( automationVerifyHostname && automationRequireTrustedCertificate) {
            ctx.automationTlsPolicy = TLSPolicy.TRUST_CA_VERIFY_HOSTNAME;
        }
        else if( automationVerifyHostname && !automationRequireTrustedCertificate ) {
            ctx.automationTlsPolicy = TLSPolicy.TRUST_FIRST_CERTIFICATE;
        }
        else {
            ctx.automationTlsPolicy = TLSPolicy.INSECURE;
        }
            
        // these security parameters actually apply to all services
        ctx.securityPolicy = new WebServiceSecurityPolicy();
        ctx.securityPolicy.isTlsRequired = Boolean.valueOf(asprops.getProperty("mtwilson.ssl.required", "true"));
        ctx.securityPolicy.trustedClients = StringUtils.split(asprops.getProperty("mtwilson.api.trust", ""), ',');
        
        ctx.managementServiceKeystoreDir = asprops.getProperty("mtwilson.ms.keystore.dir");

        ctx.biosPCRs = asprops.getProperty("mtwilson.ms.biosPCRs", "0");
        ctx.vmmPCRs = asprops.getProperty("mtwilson.ms.vmmPCRs", "18;19;20");
        
        ctx.samlCertificateFile = asprops.getProperty("mtwilson.saml.certificate");
        if( !ctx.samlCertificateFile.startsWith("/") ) {
            ctx.samlCertificateFile = String.format("/etc/intel/cloudsecurity/%s", ctx.samlCertificateFile);
        }
        
        
    }
    
    private void importAuditHandlerProperties(Properties asprops) {
        ctx.auditDatabase = new Database();
        // XXX TODO should use a differnent class that handles all the defaults etc. that is also used by attestation service.  thought of ASConfig get defaults but even that needs to be rewritten to look for mountwilson.audit.db.driver, then mtwilson.db.driver, etc. 
        ctx.auditDatabase.driver = asprops.getProperty("mountwilson.audit.db.driver", asprops.getProperty("mtwilson.db.driver")); 
        ctx.auditDatabase.username = asprops.getProperty("mountwilson.audit.db.user", asprops.getProperty("mtwilson.db.user"));
        ctx.auditDatabase.password = asprops.getProperty("mountwilson.audit.db.password", asprops.getProperty("mtwilson.db.password"));
        ctx.auditDatabase.schema = asprops.getProperty("mountwilson.audit.db.schema", "mw_as");
        if( asprops.getProperty("mountwilson.audit.db.host", asprops.getProperty("mtwilson.db.server")) != null ) {
            ctx.auditDatabase.hostname = new InternetAddress(asprops.getProperty("mountwilson.audit.db.host", asprops.getProperty("mtwilson.db.server"))); 
            // If database hostname is 127.0.0.1 or localhost, we need to rewrite it as target server address in order to access it
            if( ctx.auditDatabase.hostname.toString().equals("127.0.0.1") ||  ctx.auditDatabase.hostname.toString().equals("localhost") ) {
                ctx.auditDatabase.hostname = new InternetAddress(ctx.serverAddress.toString());
            }
        }                    
        if(asprops.getProperty("mountwilson.audit.db.port", asprops.getProperty("mtwilson.db.port"))!=null) {
            ctx.auditDatabase.port = Integer.valueOf(asprops.getProperty("mountwilson.audit.db.port", asprops.getProperty("mtwilson.db.port")));
        }
        if( ctx.auditDatabase.type == null && ctx.auditDatabase.driver != null ) {
            ctx.auditDatabase.type = DatabaseType.fromDriver(ctx.auditDatabase.driver); 
        }
        if( ctx.auditDatabase.type == null && ctx.auditDatabase.port != null ) {
            ctx.auditDatabase.type = DatabaseType.fromPort(ctx.auditDatabase.port); 
        }
        
        ctx.auditEnabled = Boolean.valueOf(asprops.getProperty("mountwilson.audit.enabled", "true"));
        ctx.auditLogUnchangedColumns = Boolean.valueOf(asprops.getProperty("mountwilson.audit.logunchangedcolumns", "false"));
        ctx.auditAsync = Boolean.valueOf(asprops.getProperty("mountwilson.audit.async", "true"));
    }
    
    private void importManagementConsoleProperties(Properties asprops) {
        ctx.portalUserKeystoreDir = asprops.getProperty("mtwilson.mc.keystore.dir");
        ctx.portalHostTypeList = asprops.getProperty("mtwilson.mc.hostTypes", "Xen;KVM;VMWare");
        ctx.portalSessionTimeout = new Timeout(Integer.valueOf(asprops.getProperty("mtwilson.mc.sessionTimeOut", "1800")), TimeUnit.SECONDS);
        ctx.portalApiKeyExpirationNotice = new Timeout(Integer.valueOf(asprops.getProperty("mtwilson.mc.apiKeyExpirationNoticeInMonths", "3"))*30, TimeUnit.DAYS); // for example 3 months is stored as 90 days, since the TimeUnit type does not have MONTHS
    }
    
    
    // XXX TODO:  what about trust-dashboard.properties and whitelist-portal.properies?   they are either same as management console (keystore dir, session timeout) or they are so UI-specific that maybe we don't care, and it's ok for them to be in separate files.... 

    private void importPrivacyCAProperties(Properties asprops) {
        if( ctx.privacyCA == null ) { // importAttestationServiceProperties may have already created it
            ctx.privacyCA = new PrivacyCA();
        }
        
        ctx.privacyCA.ekSigningKeyFilename = asprops.getProperty("P12filename", "PrivacyCA.p12"); // probably should not be configurable... should have a specific location.
        if( !ctx.privacyCA.ekSigningKeyFilename.startsWith("/") ) {
            ctx.privacyCA.ekSigningKeyFilename = String.format("/etc/intel/cloudsecurity/%s", ctx.privacyCA.ekSigningKeyFilename);
        }
        ctx.privacyCA.ekSigningKeyPassword = asprops.getProperty("P12password");
        ctx.privacyCA.ekSigningKeyDownloadUsername = asprops.getProperty("ClientFilesDownloadUsername");
        ctx.privacyCA.ekSigningKeyDownloadPassword = asprops.getProperty("ClientFilesDownloadPassword");
        ctx.privacyCA.pcaCertificateValidity = new Timeout(Integer.valueOf(asprops.getProperty("PrivCaCertValiditydays", "3652")), TimeUnit.DAYS);
    }
    
    private void importGlassfishProperties() {
        // hmm... just need location of keystore and exported certificate to replace with our ca-signed copy
    }
    private void importTomcatProperties() {
        // hmm... just need location of keystore and exported certificate to replace with our ca-signed copy
    }
    
    private void detectWebContainerType() throws IOException {
        String envFilePath = "/opt/intel/cloudsecurity/attestation-service/attestation-service.env";
        boolean hasEnvFile = existsRemoteFile(envFilePath);
        if(hasEnvFile) {
                MemoryDstFile file = downloadRemoteFile(envFilePath);
                Properties asprops = new Properties(); // this works for MOST shell-style variables, and comments are the same as in properties files
                asprops.load(file.getInputStream());
//                asprops.store(System.out, "Attestation Service Environment"); // XXX for debugging only
                if( asprops.getProperty("GLASSFISH_HOME") != null ) {
                    ctx.webContainerType = WebContainerType.GLASSFISH;
                    ctx.tlsKeystoreFile = asprops.getProperty("GLASSFISH_HOME") + "/domains/domain1/config/keystore.jks";
                    ctx.tlsKeystorePassword = "changeit";
                    ctx.tlsCertificateFile = asprops.getProperty("GLASSFISH_HOME") + "/domains/domain1/config/ssl."+ctx.serverAddress.toString()+".crt";
                    // TODO maybe move this to importGlassfishProperties ?
                }
                else if( asprops.getProperty("CATALINA_HOME") != null ) {
                    ctx.webContainerType = WebContainerType.TOMCAT;
                    // XXX TODO ... see http://tomcat.apache.org/tomcat-6.0-doc/ssl-howto.html about finding tomcat's ssl certificate
                    // TODO maybe move this to importTomcatProperties ?
                }
                // XXX TODO:  also need to handle a situation where Apache or Nginx is configured for SSL and proxies plaintext to a local tomcat or glassfish 
        }
        
    }
    
}
