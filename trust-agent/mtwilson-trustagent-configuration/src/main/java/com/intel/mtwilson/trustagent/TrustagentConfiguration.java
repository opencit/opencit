/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent;

import com.intel.dcsg.cpg.configuration.CommonsConfigurationAdapter;
import com.intel.dcsg.cpg.configuration.CompositeConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.EnvironmentConfiguration;
import com.intel.dcsg.cpg.configuration.KeyTransformerConfiguration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.util.AllCapsNamingStrategy;
import com.intel.mtwilson.configuration.AbstractConfiguration;
import java.io.File;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyFilesystem;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author jbuhacoff
 */
public class TrustagentConfiguration extends AbstractConfiguration {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrustagentConfiguration.class);

    // Variables such as TRUSTAGENT_HOME, TRUSTAGENT_CONF, etc. for filesystem
    // paths are not defined here; see MyFilesystem instead.
    // Trust Agent administrator username and password is not defined here,
    // see shiro.ini and password.txt files instead and administrator must
    // provide the username and password when connecting to Trust Agent.
    
    public final static String MTWILSON_API_URL = "mtwilson.api.url";
    public final static String MTWILSON_TLS_CERT_SHA1 = "mtwilson.tls.cert.sha1";
    public final static String MTWILSON_API_USERNAME = "mtwilson.api.username"; // NOTE: MUST NOT STORE THE VALUE
    public final static String MTWILSON_API_PASSWORD = "mtwilson.api.password"; // NOTE: MUST NOT STORE THE VALUE
    public final static String TPM_OWNER_SECRET = "tpm.owner.secret"; // 20 bytes hex (40 hex digits)
    public final static String TPM_SRK_SECRET = "tpm.srk.secret"; // 20 bytes hex (40 hex digits)
    public final static String AIK_SECRET = "aik.secret"; // 20 bytes hex (40 hex digits)
    public final static String AIK_INDEX = "aik.index"; // integer, default 1  but original HIS code from NIARL defaulted to zero
    public final static String TRUSTAGENT_HTTP_TLS_PORT = "trustagent.http.tls.port"; // default 1443 
    public final static String TRUSTAGENT_TLS_CERT_DN = "trustagent.tls.cert.dn"; // default CN=trustagent
    public final static String TRUSTAGENT_TLS_CERT_IP = "trustagent.tls.cert.ip"; // default 127.0.0.1  , can be comma-separated list of values
    public final static String TRUSTAGENT_TLS_CERT_DNS = "trustagent.tls.cert.dns";// default localhost  , can be comma-separated list of values
    public final static String TRUSTAGENT_KEYSTORE_PASSWORD = "trustagent.keystore.password";
    public final static String DAA_ENABLED = "daa.enabled"; // default false for 1.2 and 2.0
    public final static String TPM_QUOTE_IPV4 = "tpm.quote.ipv4";
    public static final String HARDWARE_UUID = "hardware.uuid";
    
    public TrustagentConfiguration(org.apache.commons.configuration.Configuration configuration) {
        this(new CommonsConfigurationAdapter(configuration));
    }
    public TrustagentConfiguration(Configuration configuration) {
        super();
        initEnvironmentConfiguration(configuration);
        
    }
    
    private void initEnvironmentConfiguration(Configuration given) {
        // using the environment configuration 
        // allows the MTWILSON_API_USERNAME and MTWILSON_API_PASSWORD to be set
        // in the environment instead of in trustagent.properties
        Configuration env = new KeyTransformerConfiguration(new AllCapsNamingStrategy(), new EnvironmentConfiguration()); // transforms mtwilson.ssl.cert.sha1 to MTWILSON_SSL_CERT_SHA1 
        Configuration configuration = new CompositeConfiguration(given, env);        
        setConfiguration(configuration);
    }
    
    
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
        String fingerprintCsv = getConfiguration().getString(MTWILSON_TLS_CERT_SHA1);
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
        return getConfiguration().getString(MTWILSON_API_URL);// intentionally no default - this must be configured during setup
    }
    public String getMtWilsonApiUsername() {
        return getConfiguration().getString(MTWILSON_API_USERNAME);// intentionally no default - this must be configured during setup
    }
    public String getMtWilsonApiPassword() {
        return getConfiguration().getString(MTWILSON_API_PASSWORD);// intentionally no default - this must be configured during setup
    }
    public String getTpmOwnerSecretHex() {
        return getConfiguration().getString(TPM_OWNER_SECRET); // intentionally no default - this must be generated during setup
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
        return getConfiguration().getString(TPM_SRK_SECRET); // intentionally no default - this must be generated during setup
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
        return getConfiguration().getString(AIK_SECRET); // intentionally no default - this must be generated during setup
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
        return getConfiguration().getInteger(AIK_INDEX, 1); 
    }
    
    public File getAikCertificateFile() {
        return new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "aik.pem");        
    }
    public File getAikBlobFile() {
        return new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "aik.blob");        
    }
    
    public int getTrustagentHttpTlsPort() {
        return getConfiguration().getInteger(TRUSTAGENT_HTTP_TLS_PORT, 1443); // TODO:  choose another default port; 9999 is the old v1 port
    }
    public String getTrustagentTlsCertDn() {
        return getConfiguration().getString(TRUSTAGENT_TLS_CERT_DN, "CN=trustagent");
    }
    
    // TODO: move to cpg-net NetworkUtil.getNetworkAddressList() and catch the SocketException to return empty list
    private List<String> getNetworkIPs() throws SocketException {
        ArrayList<String> ipList = new ArrayList<>();
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        Enumeration<InetAddress> inetAddresses;
        for (NetworkInterface netint : Collections.list(nets)) {
            inetAddresses = netint.getInetAddresses();
            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                log.debug("Found InetAddress [{}] with IP [{}] and Domain Name [{}]", inetAddress.getCanonicalHostName(), inetAddress.getHostAddress(), inetAddress.getHostName());
                if (inetAddress.getHostAddress() != null && !inetAddress.getHostAddress().isEmpty())
                    ipList.add(inetAddress.getHostAddress());
            }
        }
        return ipList;
    }
    // TODO: move to cpg-net NetworkUtil.getNetworkHostnameList() and catch the SocketException to return empty list
    private List<String> getNetworkDNs() throws SocketException {
        ArrayList<String> dnList = new ArrayList<>();
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        Enumeration<InetAddress> inetAddresses;
        for (NetworkInterface netint : Collections.list(nets)) {
            inetAddresses = netint.getInetAddresses();
            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                log.debug("Found InetAddress [{}] with IP [{}] and Domain Name [{}]", inetAddress.getCanonicalHostName(), inetAddress.getHostAddress(), inetAddress.getHostName());
                if (inetAddress.getHostName() != null && !inetAddress.getHostName().isEmpty() && !inetAddress.getHostName().equals(inetAddress.getHostAddress()))
                    dnList.add(inetAddress.getHostName());
            }
        }
        return dnList;
    }
    
    public String[] getTrustagentTlsCertIp() throws SocketException {
//        return getConfiguration().getString(TRUSTAGENT_TLS_CERT_IP, "127.0.0.1").split(",");
        String[] TlsCertIPs = getConfiguration().getString(TRUSTAGENT_TLS_CERT_IP, "").split(",");
        if (TlsCertIPs != null && !TlsCertIPs[0].isEmpty()) {
            log.debug("Retrieved IPs [{}] from trust agent configuration", TlsCertIPs);
            return TlsCertIPs;
        }
        List<String> TlsCertIPsList = getNetworkIPs();
        String[] ipListArray = new String[TlsCertIPsList.size()];
        if (ipListArray != null && ipListArray.length > 0) {
            log.debug("Retrieved IPs [{}] from network configuration", ipListArray);
            return TlsCertIPsList.toArray(ipListArray);
        }
        log.debug("Returning default IP address [127.0.0.1]");
        return new String[]{"127.0.0.1"};
    }
    public String[] getTrustagentTlsCertDns() throws SocketException {
//        return getConfiguration().getString(TRUSTAGENT_TLS_CERT_DNS, "localhost").split(",");
        String[] TlsCertDNs = getConfiguration().getString(TRUSTAGENT_TLS_CERT_DNS, "").split(",");
        if (TlsCertDNs != null && !TlsCertDNs[0].isEmpty()) {
            log.debug("Retrieved Domain Names [{}] trust agent from configuration", TlsCertDNs);
            return TlsCertDNs;
        }
        List<String> TlsCertDNsList = getNetworkDNs();
        String[] dnListArray = new String[TlsCertDNsList.size()];
        if (dnListArray != null && dnListArray.length > 0) {
            log.debug("Retrieved Domain Names [{}] from network configuration", dnListArray);
            return TlsCertDNsList.toArray(dnListArray);
        }
        log.debug("Returning default Domain Name [localhost]");
        return new String[]{"localhost"};
    }
    
    public File getTrustagentKeystoreFile() {
        return new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "trustagent.jks");
    }
    public String getTrustagentKeystorePassword() {
        return getConfiguration().getString(TRUSTAGENT_KEYSTORE_PASSWORD); // intentionally no default - this must be generated during setup
    }

    public File getTrustagentUserFile() {
        return new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "users.txt");
    }
    public File getTrustagentPermissionsFile() {
        return new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "permissions.txt");
    }
    public File getTrustagentEtagCacheFile() {
        return new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "etag.cache");
    }
    
    public boolean isDaaEnabled() {
        return getConfiguration().getBoolean(DAA_ENABLED, false);
    }
    public boolean isTpmQuoteWithIpAddress() {
        return getConfiguration().getBoolean(TPM_QUOTE_IPV4, true);
    }
    
    public String getHardwareUuid() {
        return getConfiguration().getString(HARDWARE_UUID);
    }
    
    public static TrustagentConfiguration loadConfiguration() throws IOException {
        File file = new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "trustagent.properties");
        if( file.exists() ) {
            try(FileInputStream in = new FileInputStream(file)) {
                Properties properties = new Properties();
                properties.load(in);
                TrustagentConfiguration configuration = new TrustagentConfiguration(new PropertiesConfiguration(properties));
                return configuration;
            }
        }
        else {
            Properties properties = new Properties();
            TrustagentConfiguration configuration = new TrustagentConfiguration(new PropertiesConfiguration(properties));
            return configuration;
        }
    }
}
