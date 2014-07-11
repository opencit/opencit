/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.mtwilson.KeystoreUtil;
import com.intel.mtwilson.My;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.datatypes.Role;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.mtwilson.ms.data.MwPortalUser;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.setup.helper.SCPersistenceManager;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import com.intel.mtwilson.user.management.rest.v2.model.Status;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class BootstrapUser implements Command {
    private SCPersistenceManager scManager = new SCPersistenceManager();
//    private MwPortalUserJpaController keystoreJpa ;// new MwPortalUserJpaController(scManager.getEntityManagerFactory("MSDataPU"));
    private static final Logger log = LoggerFactory.getLogger(BootstrapUser.class.getName());
    public static final Console console = System.console();
    MwPortalUserJpaController portalUserJpa; // new MwPortalUserJpaController(persistenceManager.getEntityManagerFactory("MSDataPU")); 
    
    
    private Configuration options = null;
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    
    /**
     * Creates a new API Client in current directory, registers it with Mt Wilson (on localhost or as configured), and then checks the database for the expected record to validate that it's being created.
     * @param args
     * @throws Exception 
     */
    @Override
    public void execute(String[] args) throws Exception {
        Configuration serviceConf = MSConfig.getConfiguration();
//        keystoreJpa = My.jpa().mwKeystore();
        portalUserJpa = My.jpa().mwPortalUser();
        //File directory;
        //String directoryPath = options.getString("keystore.users.dir", "/var/opt/intel/management-console/users"); //serviceConf.getString("mtwilson.mc.keystore.dir", "/var/opt/intel/management-console/users");
        //if( directoryPath == null || directoryPath.isEmpty() ) {
        //    directory = new File(directoryPath);
        //    if( !directory.exists() || !directory.isDirectory() ) {
        //        directory = new File(".");
        //    }
        //    directoryPath = readInputStringWithPromptAndDefault("Keystore directory", directory.getAbsolutePath());
        //}
        //directory = new File(directoryPath);
        
        String baseurl = options.getString("mtwilson.api.baseurl");
        if( baseurl == null || baseurl.isEmpty() ) { 
            baseurl = firstNonEmpty(new String[] { serviceConf.getString("mtwilson.api.baseurl"), System.getenv("MTWILSON_API_BASEURL"), "https://"+getLocalHostAddress()+":8181" }); 
            baseurl = readInputStringWithPromptAndDefault("Mt Wilson URL", baseurl);
        }
        
        String username ;
        String password ;
        if( args.length > 0 ) { username = args[0]; } else { username = readInputStringWithPrompt("Username"); }
        if( args.length > 1 ) { password = args[1]; } else { password = readInputStringWithPrompt("Password"); }
        if( password != null && password.startsWith("env:") && password.length() > 4 ) {
            password = System.getenv(password.substring(4)); 
        }
        if( password ==  null || password.isEmpty() ) {
            System.out.println("Password is required");
            return;
        }
        
        MwPortalUser keyTest = portalUserJpa.findMwPortalUserByUserName(username);
        // Bug # 883: We should be checking the status since the user could be deleted, in which case the status would be "cancelled".
        // The possible values for the status include approved, cancelled, rejected, expired and pending. We should allow the
        // creation of the user if the status is not "approved" or "pending" in which cases the user already is in active state or someone has
        // not yet approved the user creation request.
        // if(keyTest != null) {
        if ((keyTest != null) && (keyTest.getStatus().equalsIgnoreCase("approved") || keyTest.getStatus().equalsIgnoreCase("pending"))) {        
          log.debug("A user already exists with the specified User Name: {}", username);
          throw new SetupException(String.format("User account '%s' already exists", username));
        }
        
        // create user
        System.out.println(String.format("Creating keystore for %s in db and registering user with service at %s", username,baseurl));        
        /*
        com.intel.mtwilson.client.TextConsole.main(new String[] { "CreateUser", directory.getAbsolutePath(), username, password });
        File keystoreFile = new File(directory.getAbsolutePath() + File.separator + Filename.encode(username) + ".jks");
        if( !keystoreFile.exists() ) {
            System.out.println("Failed to create keystore "+keystoreFile.getAbsolutePath());
            return;
        }
        
        // register user
        System.out.println(String.format("Registering %s with service at %s", username, baseurl));
        com.intel.mtwilson.client.TextConsole.main(new String[] { "RegisterUser", keystoreFile.getAbsolutePath(), baseurl, "Attestation,Whitelist,Security", password });
        */
        // stdalex 1/16 jks2db!disk
        // load the new key
         ByteArrayResource certResource = new ByteArrayResource();
         SimpleKeystore keystore = KeystoreUtil.createUserInResource(certResource, username, password, new URL(baseurl),new String[] { Role.Whitelist.toString(),Role.Attestation.toString(),Role.Security.toString(),Role.AssetTagManagement.toString()});
         // Feb 12, 2014: Sudhir: Since the portal user would be created by the above call, we just need to update with the keystore.
         MwPortalUser pUser = portalUserJpa.findMwPortalUserByUserName(username);
         if(pUser != null){
            pUser.setKeystore(certResource.toByteArray());
            portalUserJpa.edit(pUser);
         }
         RsaCredentialX509 rsaCredentialX509 = keystore.getRsaCredentialX509(username, password);
        // check database for record
//        ApiClientBO bo = new ApiClientBO();
//        ApiClientInfo apiClientRecord = bo.find(rsaCredentialX509.identity());
//        ApiClientInfo apiClientRecord = findApiClientRecord(serviceConf, rsaCredentialX509.identity());
//        if( apiClientRecord == null ) {
        // approve user
         try {
            approveApiClientRecord(serviceConf,  username, rsaCredentialX509.identity());
            System.err.println(String.format("Approved %s [fingerprint %s]", username, Hex.encodeHexString(rsaCredentialX509.identity())));        
         }
         catch(Exception e) {
             System.err.println(String.format("Failed to approve %s [fingerprint %s]: %s", username, Hex.encodeHexString(rsaCredentialX509.identity()), e.getMessage()));
         }
    }
    
    private void approveApiClientRecord(Configuration conf, String username, byte[] fingerprint) throws SetupException {
        //SetupWizard wizard = new SetupWizard(conf);
        try {
            /*
            Connection c = wizard.getMSDatabaseConnection();        
            PreparedStatement s = c.prepareStatement("UPDATE mw_api_client_x509 SET enabled=b'1',status='Approved' WHERE hex(fingerprint)=?"); 
            //s.setBytes(1, fingerprint);
            s.setString(1, Hex.encodeHexString(fingerprint));
            s.executeUpdate();
            s.close();
            c.close();
            */
           
            System.err.println(String.format("Searching for user by name: %s", username)); 
            MwPortalUser apiClient = portalUserJpa.findMwPortalUserByUserName(username);   
            apiClient.setStatus("Approved");
            apiClient.setEnabled(true);
            System.err.println(String.format("Attempt to approve %s [fingerprint %s]", username, Hex.encodeHexString(fingerprint))); 
            portalUserJpa.edit(apiClient);
//            ApiClientX509JpaController x509jpaController = new ApiClientX509JpaController(persistenceManager.getEntityManagerFactory("MSDataPU"));
            ApiClientX509JpaController x509jpaController = My.jpa().mwApiClientX509();
            ApiClientX509 client = x509jpaController.findApiClientX509ByFingerprint(fingerprint);
            if( client == null ) {
                log.error("Cannot find client record with fingerprint {}", Hex.encodeHexString(fingerprint));
                throw new IllegalStateException("Cannot find client record with fingerprint "+Hex.encodeHexString(fingerprint));
            }
            client.setStatus("Approved");
            client.setEnabled(true);
            x509jpaController.edit(client);
            
            try(LoginDAO loginDAO = MyJdbi.authz()) {
                UserLoginCertificate userLoginCertificate = loginDAO.findUserLoginCertificateBySha256(fingerprint);
                if (userLoginCertificate != null) {
                    userLoginCertificate.setEnabled(true);
                    userLoginCertificate.setStatus(Status.APPROVED);
                    userLoginCertificate.setComment("Approved during setup");
                    loginDAO.updateUserLoginCertificateById(userLoginCertificate.getId(), userLoginCertificate.isEnabled(), 
                            userLoginCertificate.getStatus(), userLoginCertificate.getComment());
                }

            } catch (Exception ex) {
                throw new SetupException("Error updating user and user certificate tables. " + ex.getMessage(), ex);
            }
            
        } catch (SetupException se) {
            throw se;
        } catch(Exception e) {
            throw new SetupException("Cannot update API Client record: "+e.getMessage(), e);
        }        
    }

    
    private static String getLocalHostAddress() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (UnknownHostException ex) {
            return "127.0.0.1";
        }
    }
    
    private String readInputStringWithPrompt(String prompt) throws IOException {
        if (console == null) {
            throw new IOException("no console.");
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(String.format("%s: ", prompt));
        String input = in.readLine();
//        in.close(); // don't close System.in !!
        return input;
    }

    private String readInputStringWithPromptAndDefault(String prompt, String defaultValue) throws IOException {
        if (console == null) {
            throw new IOException("no console.");
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(String.format("%s [%s]: ", prompt, defaultValue));
        String input = in.readLine();
//        in.close(); // don't close System.in !!
        if( input == null || input.isEmpty() ) {
            input = defaultValue;
        }
        return input;
    }
    
    private String firstNonEmpty(String[] values) {
        for(String value : values) {
            if( value != null && !value.isEmpty() ) {
                return value;
            }
        }
        return null;
    }

}
