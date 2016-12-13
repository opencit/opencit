/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent;

import com.intel.dcsg.cpg.configuration.CommonsConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.pem.Pem;
import com.intel.dcsg.cpg.net.NetUtils;
import com.intel.mtwilson.Environment;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.configuration.EncryptedConfigurationProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jbuhacoff
 */
public class TrustagentConfiguration {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrustagentConfiguration.class);
    private static final String PASSWORD = "PASSWORD"; // transforms into MTWILSON_PASSWORD, KMS_PASSWORD, etc. environment variables

    // Variables such as TRUSTAGENT_HOME, TRUSTAGENT_CONF, etc. for filesystem
    // paths are not defined here; see MyFilesystem instead.
    // Trust Agent administrator username and password is not defined here,
    // see shiro.ini and password.txt files instead and administrator must
    // provide the username and password when connecting to Trust Agent.
    
    public final static String MTWILSON_API_URL = "mtwilson.api.url";
    public final static String MTWILSON_TLS_CERT_SHA256 = "mtwilson.tls.cert.sha256";
    public final static String MTWILSON_API_USERNAME = "mtwilson.api.username"; // NOTE: MUST NOT STORE THE VALUE
    public final static String MTWILSON_API_PASSWORD = "mtwilson.api.password"; // NOTE: MUST NOT STORE THE VALUE
    public final static String TPM_OWNER_SECRET = "tpm.owner.secret"; // 20 bytes hex (40 hex digits)
    public final static String TPM_SRK_SECRET = "tpm.srk.secret"; // 20 bytes hex (40 hex digits)
    public final static String AIK_SECRET = "aik.secret"; // 20 bytes hex (40 hex digits)
    public final static String AIK_INDEX = "aik.index"; // integer, default 1  but original HIS code from NIARL defaulted to zero
    public final static String AIK_HANDLE = "aik.handle"; // this is the Ak handle for TPM2.0, range 81018000-8101FFFF
    public final static String EK_HANDLE = "ek.handle"; // this is the ek handle for TPM2.0, range 81010000-810100FF
    public final static String TRUSTAGENT_HTTP_TLS_PORT = "trustagent.http.tls.port"; // default 1443 
    public final static String TRUSTAGENT_TLS_CERT_DN = "trustagent.tls.cert.dn"; // default CN=trustagent
    public final static String TRUSTAGENT_TLS_CERT_IP = "trustagent.tls.cert.ip"; // default 127.0.0.1  , can be comma-separated list of values
    public final static String TRUSTAGENT_TLS_CERT_DNS = "trustagent.tls.cert.dns";// default localhost  , can be comma-separated list of values
    public final static String TRUSTAGENT_KEYSTORE_PASSWORD = "trustagent.keystore.password";
    public final static String DAA_ENABLED = "daa.enabled"; // default false for 1.2 and 2.0
    public final static String TPM_QUOTE_IPV4 = "tpm.quote.ipv4";
    public static final String HARDWARE_UUID = "hardware.uuid";
    public static final String BINDING_KEY_NAME = "bind";
    public static final String BINDING_KEY_SECRET = "binding.key.secret";
    public static final String BINDING_KEY_INDEX = "binding.key.index";
    public static final String SIGNING_KEY_NAME = "sign";
    public static final String SIGNING_KEY_SECRET = "signing.key.secret";
    public static final String SIGNING_KEY_INDEX = "signing.key.index";
    public static final String TRUSTAGENT_ADMIN_USERNAME = "trustagent.admin.username";
    public final static String JETTY_THREAD_MIN = "jetty.thread.min";
    public final static String JETTY_THREAD_MAX = "jetty.thread.max";
               
    private Configuration conf;

    public Configuration getConf() {
        return conf;
    }
    
    public TrustagentConfiguration(org.apache.commons.configuration.Configuration configuration) {
        this(new CommonsConfiguration(configuration));
    }
    public TrustagentConfiguration(Configuration configuration) {
        this.conf = configuration;
//        initEnvironmentConfiguration(configuration);
    }
    /*
    private void initEnvironmentConfiguration(Configuration given) {
        // using the environment configuration 
        // allows the MTWILSON_API_USERNAME and MTWILSON_API_PASSWORD to be set
        // in the environment instead of in trustagent.properties
        Configuration env = new KeyTransformerConfiguration(new AllCapsNamingStrategy(), new EnvironmentConfiguration()); // transforms mtwilson.ssl.cert.sha1 to MTWILSON_SSL_CERT_SHA1 
        Configuration configuration = new CompositeConfiguration(given, env);        
        setConfiguration(configuration);
    }
    */
    
    /**
     * NOTE: this comes from an environment variable and would only be used
     * during setup for automatic approval of the mtwilson tls cert when the
     * admin sets the env var MTWILSON_TLS_CERT_SHA1 to be a comma-separated list
     * of valid SHA1 digests. During setup the authorized certificates are 
     * saved to the trustagent.jks keystore so this env var is not needed 
     * after setup.
     * 
     * @return 
     */
    public List<String> getMtWilsonTlsCertificateFingerprints() {        
        String fingerprintCsv = conf.get(MTWILSON_TLS_CERT_SHA256, null);
        if( fingerprintCsv == null || fingerprintCsv.isEmpty() ) {
            return Collections.EMPTY_LIST;
        }
        return Arrays.asList(fingerprintCsv.split("\\s*,\\s*"));
            
    }
    
    /*
    public File getWebAppContextFile() {
        return new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "web.xml");
    }
    */
    
    public String getMtWilsonApiUrl() {
        return conf.get(MTWILSON_API_URL, null);// intentionally no default - this must be configured during setup
    }
    public String getMtWilsonApiUsername() {
        return conf.get(MTWILSON_API_USERNAME, null);// intentionally no default - this must be configured during setup
    }
    public String getMtWilsonApiPassword() {
        return conf.get(MTWILSON_API_PASSWORD, null);// intentionally no default - this must be configured during setup
    }
    public String getTpmOwnerSecretHex() {
        return conf.get(TPM_OWNER_SECRET, null); // intentionally no default - this must be generated during setup
    }
    public String getJettyThreadMin() {
        return conf.get(JETTY_THREAD_MIN, "0");
    }
    public String getJettyThreadMax() {
        return conf.get(JETTY_THREAD_MAX, "0");
    }     
    public byte[] getTpmOwnerSecret() {
        try {
            return Hex.decodeHex(getTpmOwnerSecretHex().toCharArray());
        }
        catch(DecoderException e) {
            throw new IllegalArgumentException("Invalid owner secret", e);
        }
    }
    public String getTpmSrkSecretHex() {
        return conf.get(TPM_SRK_SECRET, null); // intentionally no default - this must be generated during setup
    }
    public byte[] getTpmSrkSecret() {
        try {
            return Hex.decodeHex(getTpmSrkSecretHex().toCharArray());
        }
        catch(DecoderException e) {
            throw new IllegalArgumentException("Invalid SRK secret", e);
        }
    }
    public String getAikSecretHex() {
        return conf.get(AIK_SECRET, null); // intentionally no default - this must be generated during setup
    }
    public byte[] getAikSecret() {
        try {
            return Hex.decodeHex(getAikSecretHex().toCharArray());
        }
        catch(DecoderException e) {
            throw new IllegalArgumentException("Invalid AIK secret", e);
        }
    }
    public int getAikIndex() {
        return Integer.valueOf(conf.get(AIK_INDEX, "1")); 
    }
    
    public String getAikHandleHex() {
        return conf.get(AIK_HANDLE); // intentionally no default - this must be generated during setup
    }
    public String getAikHandle() {
        /*
        try {
            return Hex.decodeHex(getAikHandleHex().toCharArray());
        }
        catch(DecoderException e) {
            throw new IllegalArgumentException("Invalid AIK Handle", e);
        }
        */
        try {
            return readFromFile(Folders.configuration() + File.separator + "aikhandle");
        } catch (IOException ex) {
            Logger.getLogger(TrustagentConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException("AiK Handle", ex);
        }
    }
    public void setAikHandle(String khandle) throws IOException {
        writeToFile(Folders.configuration() + File.separator + "aikhandle", khandle);
    }
    
    public String getEkHandleHex() {
        log.debug("EK_HANDLE string: {}", conf.get(EK_HANDLE));
        return conf.get(EK_HANDLE); // intentionally no default - this must be generated during setup
    }
    public String getEkHandle() {
            /*
            try {
            return Hex.decodeHex(getEkHandleHex().toCharArray());
            }
            catch(DecoderException e) {
            throw new IllegalArgumentException("Invalid EK Handle", e);
            }
            */
        try {
            return readFromFile(Folders.configuration() + File.separator + "ekhandle");
        } catch (IOException ex) {
            Logger.getLogger(TrustagentConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException("EK Handle", ex);
        }
    }
    
    public void setEkHandle(String khandle) throws IOException {
        writeToFile(Folders.configuration() + File.separator + "ekhandle", khandle);
    }
   
    public String getAikName() {
            /*
            try {
            return Hex.decodeHex(getEkHandleHex().toCharArray());
            }
            catch(DecoderException e) {
            throw new IllegalArgumentException("Invalid EK Handle", e);
            }
            */
        try {
            return readFromFile(Folders.configuration() + File.separator + "aikname");
        } catch (IOException ex) {
            Logger.getLogger(TrustagentConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException("AikName", ex);
        }
    }
    public void setAikName(String kname) throws IOException {
        writeToFile(Folders.configuration() + File.separator + "aikname", kname);
    }
        
    public File getAikCertificateFile() {
        return new File(Folders.configuration() + File.separator + "aik.pem");        
    }
    public File getAikBlobFile() {
        return new File(Folders.configuration() + File.separator + "aik.blob");        
    }
    public File getAikOpaqueFile() {
        return new File(Folders.configuration() + File.separator + "aik.opaque");        
    }
    
    public int getTrustagentHttpTlsPort() {
        return Integer.valueOf(conf.get(TRUSTAGENT_HTTP_TLS_PORT, "1443"));
    }
    public String getTrustagentTlsCertDn() {
        return conf.get(TRUSTAGENT_TLS_CERT_DN, "CN=trustagent");
    }
        
    public String getTrustagentTlsCertIp() {
        return conf.get(TRUSTAGENT_TLS_CERT_IP, "");
    }
    public String[] getTrustagentTlsCertIpArray() throws SocketException {
//        return conf.getString(TRUSTAGENT_TLS_CERT_IP, "127.0.0.1").split(",");
        String[] TlsCertIPs = conf.get(TRUSTAGENT_TLS_CERT_IP, "").split(",");
        if (TlsCertIPs != null && !TlsCertIPs[0].isEmpty()) {
            log.debug("Retrieved IPs from trust agent configuration: {}", (Object[])TlsCertIPs);
            return TlsCertIPs;
        }
        List<String> TlsCertIPsList = NetUtils.getNetworkAddressList(); // never returns null but may be empty
        String[] ipListArray = new String[TlsCertIPsList.size()];
        if (ipListArray.length > 0) {
            log.debug("Retrieved IPs from network configuration: {}", (Object[])ipListArray);
            return TlsCertIPsList.toArray(ipListArray);
        }
        log.debug("Returning default IP address [127.0.0.1]");
        return new String[]{"127.0.0.1"};
    }
    public String getTrustagentTlsCertDns() {
        return conf.get(TRUSTAGENT_TLS_CERT_DNS, "");
    }
    public String[] getTrustagentTlsCertDnsArray() throws SocketException {
//        return conf.getString(TRUSTAGENT_TLS_CERT_DNS, "localhost").split(",");
        String[] TlsCertDNs = conf.get(TRUSTAGENT_TLS_CERT_DNS, "").split(",");
        if (TlsCertDNs != null && !TlsCertDNs[0].isEmpty()) {
            log.debug("Retrieved Domain Names trust agent from configuration: {}", (Object[])TlsCertDNs);
            return TlsCertDNs;
        }
        List<String> TlsCertDNsList = NetUtils.getNetworkHostnameList(); // never returns null but may be empty
        String[] dnListArray = new String[TlsCertDNsList.size()];
        if (dnListArray.length > 0) {
            log.debug("Retrieved Domain Names from network configuration: {}", (Object[])dnListArray);
            return TlsCertDNsList.toArray(dnListArray);
        }
        log.debug("Returning default Domain Name [localhost]");
        return new String[]{"localhost"};
    }
    
    public File getTrustagentKeystoreFile() {
        return new File(Folders.configuration() + File.separator + "trustagent.jks");
    }
    public String getTrustagentKeystorePassword() {
        return conf.get(TRUSTAGENT_KEYSTORE_PASSWORD, null); // intentionally no default - this must be generated during setup
    }
    
    public File getEndorsementAuthoritiesFile() {
        return new File(Folders.configuration() + File.separator + "endorsement.pem");
    }

    public File getTrustagentUserFile() {
        return new File(Folders.configuration() + File.separator + "users.txt");
    }
    public File getTrustagentPermissionsFile() {
        return new File(Folders.configuration() + File.separator + "permissions.txt");
    }
    public File getTrustagentEtagCacheFile() {
        return new File(Folders.configuration() + File.separator + "etag.cache");
    }
    
    public boolean isDaaEnabled() {
        return Boolean.valueOf(conf.get(DAA_ENABLED, "false"));
    }
    public boolean isTpmQuoteWithIpAddress() {
        return Boolean.valueOf(conf.get(TPM_QUOTE_IPV4, "true"));
    }
    
    public String getHardwareUuid() {
        return conf.get(HARDWARE_UUID, null);
    }
    
    public File getMeasureLogLaunchScript() {
        return new File(Folders.application() + File.separator + "bin" + File.separator + "module_analysis.sh");
    }
    
    public File getTcbMeasurementXmlFile() {
        return new File(Folders.log() + File.separator + "measurement.xml");
    }

    public String getMtwilsonTlsPolicyCertificateSha256() {
        return conf.get("mtwilson.tls.cert.sha256", null);
    }
    
    public Properties getMtWilsonClientProperties() {
        Properties properties = new Properties();
        properties.setProperty("mtwilson.api.url", getMtWilsonApiUrl());
        properties.setProperty("mtwilson.api.username", getMtWilsonApiUsername());
        properties.setProperty("mtwilson.api.password", getMtWilsonApiPassword());
        properties.setProperty("mtwilson.api.tls.policy.certificate.keystore.file", getTrustagentKeystoreFile().getAbsolutePath());
        properties.setProperty("mtwilson.api.tls.policy.certificate.keystore.password", getTrustagentKeystorePassword());
        properties.setProperty("mtwilson.api.tls.policy.certificate.sha256", getMtwilsonTlsPolicyCertificateSha256());
        properties.setProperty("mtwilson.tls.cert.sha256", getMtwilsonTlsPolicyCertificateSha256());
        return properties;
    }
    
    
    public static TrustagentConfiguration loadConfiguration() throws IOException {
        File file = new File(Folders.configuration() + File.separator + "trustagent.properties");
        if( file.exists() ) {
            try(FileInputStream in = new FileInputStream(file)) {
                String content = IOUtils.toString(in);
                if (Pem.isPem(content)) {
                    String password = Environment.get(PASSWORD);
                    Configuration configuration = new EncryptedConfigurationProvider(new FileResource(file), password).load();
                    return new TrustagentConfiguration(configuration);
                }
                Properties properties = new Properties();
                //should not use properties.load(in) here since the line String content = IOUtils.toString(in) above already move the inputstream to the end of stream
                properties.load(new StringReader(content));
                TrustagentConfiguration configuration = new TrustagentConfiguration(new PropertiesConfiguration(properties));
                return configuration;
            }
        }
        else {
            TrustagentConfiguration configuration = new TrustagentConfiguration(new PropertiesConfiguration());
            return configuration;
        }
    }
    
    // Helper methods for the Binding key
    public String getBindingKeySecretHex() {
        return conf.get(BINDING_KEY_SECRET); // intentionally no default - this must be generated during setup
    }
    
    public byte[] getBindingKeySecret() {
        try {
            return Hex.decodeHex(getBindingKeySecretHex().toCharArray());
        }catch(DecoderException e) {
            throw new IllegalArgumentException("Invalid Binding Key secret", e);
        }
    }
    
    public int getBindingKeyIndex() {
        return Integer.valueOf(conf.get(BINDING_KEY_INDEX, "3")); 
    }
    
    public File getBindingKeyModulusFile() {
        return new File(Folders.configuration() + File.separator + "bindingkey.pub");        
    }

    // TODO : Decide the extenstion with which the TCG certificate should be stored.    
    public File getBindingKeyTCGCertificateFile() {
        return new File(Folders.configuration() + File.separator + "bindingkey.ckf");        
    }
    
    public File getBindingKeyTCGCertificateSignatureFile() {
        return new File(Folders.configuration() + File.separator + "bindingkey.sig");        
    }

    public File getBindingKeyX509CertificateFile() {
        return new File(Folders.configuration() + File.separator + "bindingkey.pem");        
    }

    public File getBindingKeyBlobFile() {
        return new File(Folders.configuration() + File.separator + "bindingkey.blob");        
    }

    // Helper methods for the Signing key
    public String getSigningKeySecretHex() {
        return conf.get(SIGNING_KEY_SECRET); // intentionally no default - this must be generated during setup
    }
    
    public byte[] getSigningKeySecret() {
        try {
            return Hex.decodeHex(getSigningKeySecretHex().toCharArray());
        }catch(DecoderException e) {
            throw new IllegalArgumentException("Invalid Signing Key secret", e);
        }
    }
    
    public int getSigningKeyIndex() {
        return Integer.valueOf(conf.get(SIGNING_KEY_INDEX, "4")); 
    }
    
    public File getSigningKeyModulusFile() {
        return new File(Folders.configuration() + File.separator + "signingkey.pub");        
    }

    // TODO : Decide the extenstion with which the TCG certificate should be stored.        
    public File getSigningKeyTCGCertificateFile() {
        return new File(Folders.configuration() + File.separator + "signingkey.ckf");        
    }

    public File getSigningKeyTCGCertificateSignatureFile() {
        return new File(Folders.configuration() + File.separator + "signingkey.sig");        
    }

    public File getSigningKeyX509CertificateFile() {
        return new File(Folders.configuration() + File.separator + "signingkey.pem");        
    }

    public File getSigningKeyBlobFile() {
        return new File(Folders.configuration() + File.separator + "signingkey.blob");        
    }

    public String getTrustAgentAdminUserName() {
        return conf.get(TRUSTAGENT_ADMIN_USERNAME); // intentionally no default - this must be generated during setup
    }

    public static String getTpmVersion() throws IOException {
        File tpmVerFileH = new File(Folders.configuration() + File.separator + "tpm-version");
        
        //set tpm version to 1.2 by default
        String tpmVersion = "1.2";
        if (tpmVerFileH.exists())
            tpmVersion = FileUtils.readFileToString(tpmVerFileH).replaceAll("\n", "");   
        
        return tpmVersion;
    }
    
    /* This is temp solution for tpm2; we save the endorsement certificate (EC) to a file ec.pem */
    public File getEcCertificateFile() {
        return new File(Folders.configuration() + File.separator + "ekcert.pem");        
    }
    
    public File getEkHandleFile() {
        return new File(Folders.configuration() + File.separator + "ekhandle");        
    }
    
    public File getAkHandleFile() {
        return new File(Folders.configuration() + File.separator + "aikhandle");        
    }
    
    public File getAkNameFile() {
        return new File(Folders.configuration() + File.separator + "aikname");        
    }
    
    public static String readFromFile(String inFileName) throws IOException {
        String outBytes = null;
        File inFile = new File(inFileName);
        if( inFile.exists() )
           outBytes = FileUtils.readFileToString(inFile);
        return outBytes;
    }
    
    public static void writeToFile(String outFilename, String outBytes) throws IOException {
        File outFile = new File(outFilename);
        mkdir(outFile);
        try(FileOutputStream out = new FileOutputStream(outFile)) { // throws FileNotFoundException
            IOUtils.write(outBytes, out); // throws IOException
        }
    }
 
    private static void mkdir(File file) throws IOException {
        if (!file.getParentFile().isDirectory()) {
            if (!file.getParentFile().mkdirs()) {
                log.warn("Failed to create client installation path!");
                throw new IOException("Failed to create client installation path!");
            }
        }
    } 
}
