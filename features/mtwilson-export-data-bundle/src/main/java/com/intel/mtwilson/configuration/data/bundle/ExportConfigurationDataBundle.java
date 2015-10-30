/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration.data.bundle;

import com.intel.dcsg.cpg.configuration.CommonsConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.core.data.bundle.Namespace;
import com.intel.mtwilson.core.data.bundle.TarGzipBundle;
import com.intel.mtwilson.jaxrs2.mediatype.ZipMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/configuration/databundle")
public class ExportConfigurationDataBundle implements Command {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExportConfigurationDataBundle.class);
    
    public static final String PCA_EK_CACERTS_FILE_PROPERTY = "mtwilson.privacyca.ek.cacerts.file";
    public static final String PCA_AIK_CACERTS_FILE_PROPERTY = "mtwilson.privacyca.aik.cacerts.file";
    public static final String TLS_CACERTS_FILE_PROPERTY = "mtwilson.tls.cacerts.file";
    public static final String SAML_CACERTS_FILE_PROPERTY = "mtwilson.saml.cacerts.file";
    
    private byte[] getBytesFromProperties(Properties properties) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            properties.store(out, null);
            return out.toByteArray();
        }        
    }
    
    private byte[] getBytesFromFile(File file) throws IOException {
        if( file == null ) { throw new NullPointerException(); }
        try(FileInputStream in = new FileInputStream(file)) {
            return IOUtils.toByteArray(in);
        }
    }
    
    private File getFile(String path) {
        File f = new File(path);
        if (f.isAbsolute()) {
            return f;
        } else {
            return new File(Folders.configuration() + File.separator + path);
        }
    }
    
    @GET
//    @Produces(ZipMediaType.ARCHIVE_TAR_GZ) // some browsers might automatically decompress this, user doesn't notice, and then it uses up more disk space on storage and bandwidth on transfer
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RequiresPermissions("configuration_databundle:retrieve")   
    public byte[] retrieveConfigurationDatabundle() {
        try {
            return getBundle();
        }
        catch(IOException e) {
            log.error("Cannot create configuration data bundle", e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    private byte[] getBundle() throws IOException {
        
        Configuration configuration = ConfigurationFactory.getConfiguration();
        
        File endorsementcaPemFile = getFile(configuration.get(PCA_EK_CACERTS_FILE_PROPERTY, "EndorsementCA.pem"));
        File privacycaPemFile = getFile(configuration.get(PCA_AIK_CACERTS_FILE_PROPERTY, "PrivacyCA.pem"));
        File tlsPemFile = getFile(configuration.get(TLS_CACERTS_FILE_PROPERTY, "ssl.crt.pem"));
        File samlPemFile = getFile(configuration.get(SAML_CACERTS_FILE_PROPERTY, "saml.crt.pem"));
        
        byte[] endorsementCA = getBytesFromFile(endorsementcaPemFile);
        byte[] privacyCA = getBytesFromFile(privacycaPemFile);
        byte[] tls = getBytesFromFile(tlsPemFile);
        byte[] saml = getBytesFromFile(samlPemFile);
        
        Properties properties = new Properties();
        if( configuration.keys().contains("mtwilson.api.url")) {
            properties.setProperty("mtwilson.api.url", configuration.get("mtwilson.api.url"));
        }
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (TarGzipBundle bundle = new TarGzipBundle()) {
                Namespace config = bundle.namespace("com.intel.mtwilson.configuration");
                config.set("EndorsementCA.pem", endorsementCA);
                config.set("PrivacyCA.pem", privacyCA);
                config.set("SAML.pem", saml);
                config.set("TLS.pem", tls);
                config.set("mtwilson.properties", getBytesFromProperties(properties));

                bundle.write(out);
            }
            return out.toByteArray();
        }

    }

    private Configuration options = new PropertiesConfiguration();
    
    /**
     * For console usage only.
     * @param options  not used
     */
    @Override
    public void setOptions(org.apache.commons.configuration.Configuration options) {
        this.options = new CommonsConfiguration(options);
    }

    /**
     * For console usage only.
     * @param args name of the file to write
     */
    @Override
    public void execute(String[] args) throws Exception {
        if( args.length < 1 && !options.keys().contains("stdout") ) {
            throw new IllegalArgumentException("Output filename required");
        }
        byte[] content = getBundle();
        if( options.keys().contains("stdout") ) {
            System.out.write(content);
        }
        else {
            try( FileOutputStream out = new FileOutputStream(new File(args[0])) ) {
                out.write(content);
            }
        }
    }
    
    
}
